package ru.biomedis.biomedismair3.UserUtils;

import lombok.extern.slf4j.Slf4j;
import ru.biomedis.biomedismair3.ModelDataApp;
import ru.biomedis.biomedismair3.entity.Complex;
import ru.biomedis.biomedismair3.entity.Language;
import ru.biomedis.biomedismair3.entity.Program;
import ru.biomedis.biomedismair3.entity.Section;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.stream.Collectors;



@Slf4j
public class CreateBaseHelper{

    public static void createHelpFiles(File dir, ModelDataApp mda) throws Exception{

        System.out.println("Начало создания");

        List<Section> rootSections = mda.findAllRootSection().stream()
                .filter(s -> s.getTag()==null?true:!s.getTag().equals("USER"))
                .collect(Collectors.toList());
        File langDir;
        for (Language language : mda.findAvaliableLangs()) {
            System.out.println("Язык: "+language.getName());

            langDir=new File(dir,language.getAbbr());
            langDir.mkdir();

            mda.initStringsSection(rootSections,language,true);

            File contentDir=new File(langDir,"content");
            contentDir.mkdir();

            for (Section rootSection : rootSections) {

                if(rootSection.getTag()!=null) if(rootSection.getTag().equals("TRINITY")){

                    System.out.print("Раздел : "+rootSection.getNameString()+"...");

                    exportSection(rootSection,new File(langDir,mda.getString(rootSection.getName(),mda.getDefaultLanguage())+".html"),mda, language,null,rootSection.getNameString());

                    continue;
                }
                List<Section> innerSections = mda.findAllSectionByParent(rootSection);
                mda.initStringsSection(innerSections,language,true);


                    System.out.println("База : "+rootSection.getNameString());

                    OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(new File(langDir,mda.getString(rootSection.getName(),mda.getDefaultLanguage())+".html")), "UTF8");

                    fw.write("<!DOCTYPE html>\n");
                    fw.write("<html>\n");
                    fw.write("<head>  <meta charset=\"utf-8\"/><title>"+rootSection.getNameString()+"</title></head>\n");
                    fw.write("<body>\n");



                    for (Section section : innerSections) {
                        System.out.print("Раздел : "+section.getNameString()+"...");
                        fw.write("<a href='./content/"+section.getId()+".html'>"+section.getNameString()+"</a><br/>\n");
                        exportSection(section,new File(contentDir,section.getId()+".html"),mda, null, language,section.getNameString());
                        System.out.println("..OK");
                    }

                fw.write("</body></html>\n");

                fw.close();

            }

        }

        System.out.println("Создание завершено");
    }


    public static void createHelpFiles(File dir, ModelDataApp mda, Language language, Language addLang) throws Exception{

        System.out.println("Начало создания");

        List<Section> rootSections = mda.findAllRootSection().stream()
                                        .filter(s -> s.getTag()==null?true:!s.getTag().equals("USER"))
                                        .collect(Collectors.toList());
        File langDir;

            System.out.println("Язык: "+language.getName());

            langDir=new File(dir,"References_"+language.getAbbr());
            langDir.mkdir();

            mda.initStringsSection(rootSections,language,true);

            File contentDir=new File(langDir,"content");
            contentDir.mkdir();

            for (Section rootSection : rootSections) {

                if(rootSection.getTag()!=null) if(rootSection.getTag().equals("TRINITY")){

                    System.out.print("Раздел : "+rootSection.getNameString()+"...");

                    exportSection(rootSection,new File(langDir,mda.getString(rootSection.getName(),mda.getDefaultLanguage())+".html"),mda, language,addLang,rootSection.getNameString());

                    continue;
                }
                List<Section> innerSections = mda.findAllSectionByParent(rootSection);
                mda.initStringsSection(innerSections,language,true);


                System.out.println("База : "+rootSection.getNameString());

                OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(new File(langDir,mda.getString(rootSection.getName(),mda.getDefaultLanguage())+".html")), "UTF8");

                fw.write("<!DOCTYPE html>\n");
                fw.write("<html>\n");
                fw.write("<head>  <meta charset=\"utf-8\"/><title>"+rootSection.getNameString()+"</title></head>\n");
                fw.write("<body>\n");



                for (Section section : innerSections) {
                    System.out.print("Раздел : "+section.getNameString()+"...");
                    fw.write("<a href='./content/"+section.getId()+".html'>"+section.getNameString()+"</a><br/>\n");
                    exportSection(section,new File(contentDir,section.getId()+".html"),mda, language, addLang,section.getNameString());
                    System.out.println("..OK");
                }

                fw.write("</body></html>\n");

                fw.close();

            }



        System.out.println("Создание завершено");
    }


    private static final String[] noops={
            "",
            "   ",
            "       ",
            "           ",
            "               ",
            "                   ",
            "                       ",
            "                           ",
            "                               ",
            "                                   ",
            "                                       ",
            "                                           ",
            "                                               ",
            "                                                   ",
            "                                                       ",
            "                                                           "
    };

    /**
     *
     * Записывает структуру базы начиная с заданного раздела в html файл
     * @param startedSection
     * @param file
     * @param md
     * @return True в случае успеха и False если произошла ошибка
     */
    public static boolean exportSection(@NotNull Section startedSection, @NotNull  File file, ModelDataApp md,Language language, Language additionalLang, String name)
    {
        if(startedSection==null) return false;
        //внутри раздела - тут это раздылы базы -общие, терапевт итп. Вложенных подразделов нет. Мы представляем подразделы и комплексы как таблицы с заголовками. В них программы с частотами

        try {
            OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(file), "UTF8");
            fw.write(getHtmlHeader(startedSection, md,language,additionalLang));
            fw.write(getSection(startedSection, md,0,language,additionalLang));
            fw.write(getHtmlBottom());
            fw.close();

        } catch (IOException e) {
            log.error("",e);
            return false;
        }
        return true;
    }




    public static String getProgram(Program program, ModelDataApp md, int level,Language language, Language additionalLang)
    {
        StringBuilder strb=new StringBuilder();
        strb.append(noops[level]);
        md.initStringsProgram(program,language,true);
        String name =program.getNameString();

        String nameAdditional ="";
        if(additionalLang!=null){
            if (additionalLang.getId()!=0){
                md.initStringsProgram(program,additionalLang,true);
                nameAdditional = program.getNameString();
            }

        }

        strb.append("<tr>")
        .append("<td><strong>")
                .append(name)
                .append("</strong></td>");
        if(!nameAdditional.isEmpty()) strb.append("<td><strong>")
            .append(nameAdditional)
            .append("</strong></td>");
        strb.append("</tr>");
        strb.append("<tr><td colspan=\"2\">");
        strb.append(program.getFrequencies().replace(",",".").replace(";","; ").replace("+","; "))
                .append("</td></tr>");

        return strb.toString();

    }

    public static String getProgramList(List<Program> list, ModelDataApp md,int level,Language language, Language additionalLang)
    {

        StringBuilder strb=new StringBuilder();

        list.forEach(program -> strb.append(getProgram(program,md, level, language,additionalLang)));

        return strb.toString();
    }

    public static String getComplex(Complex complex, ModelDataApp md, int level,Language language, Language additionalLang)
    {
        StringBuilder strb=new StringBuilder();
        md.initStringsComplex(complex,language,true);
        String name =complex.getNameString();

        String nameAdditional ="";
        if(additionalLang!=null){
            if (additionalLang.getId()!=0){
                md.initStringsComplex(complex,additionalLang,true);
                nameAdditional = complex.getNameString();
            }

        }

        strb.append(noops[level]);
        int lvl=level+1;
        strb.append("<br/><br/><table border='1' cellspacing='0' cellpadding='5' width='100%'>")
                .append( "<caption><h2>")
                .append(name);
        if(!nameAdditional.isEmpty())strb.append("/ ").append(nameAdditional);
        strb.append("</h2></caption>");


        List<Program> list = md.findAllProgramByComplex(complex);
        strb.append(getProgramList(list, md,lvl,language,additionalLang));

        strb.append(noops[level]);
        strb.append("</table>\n");

        return strb.toString();
    }

public static String getHtmlHeader(Section section, ModelDataApp md, Language language,Language additionalLang){
    md.initStringsSection(section,language,true);
    String name = section.getNameString();
    String nameAdditional ="";

    if(additionalLang!=null){
        if (additionalLang.getId()!=0){
            md.initStringsSection(section,additionalLang,true);
            nameAdditional = section.getNameString();
        }

    }

    StringBuilder strb=new StringBuilder();
    strb.append("<!DOCTYPE html>\n");
    strb.append("<html><head>  <meta charset=\"utf-8\"/><title>"+name+"</title></head><body>\n");
    strb.append("<h1 style='text-align:center;'>"+name+(nameAdditional.isEmpty()?"":("/ "+nameAdditional))+"</h1>");
    return strb.toString();
}

public static String getHtmlBottom(){
    return  "</body></html>";
}

    public static String getSection(Section section,ModelDataApp md,int level,Language language, Language additionalLang)
    {

        md.initStringsSection(section,language,true);

        String name = section.getNameString();
        String nameAdditional ="";

        if(additionalLang!=null){
            if (additionalLang.getId()!=0){
                md.initStringsSection(section,additionalLang,true);
                nameAdditional = section.getNameString();
            }

        }

        StringBuilder strb=new StringBuilder();

        strb.append(noops[level]);
        if(level!=0)//исключаем стартовый раздел
        {
            md.initStringsSection(section,language,true);
            strb.append("<br/><br/><table border='1' cellspacing='0' cellpadding='5'  width='100%'>")
                    .append( "<caption><h2>")
                    .append(name);
            if(!nameAdditional.isEmpty()) strb.append("/ ").append(nameAdditional);
            strb.append("</h2></caption>");


        }
        int lvl=level+1;
        List<Section> childSections = md.findAllSectionByParent(section);

        childSections.forEach(section1 -> strb.append(getSection(section1, md,lvl,language,additionalLang)));//добавятся дочернии разделы

        List<Complex> childComplexes = md.findAllComplexBySection(section);
        childComplexes.forEach(complex -> strb.append(getComplex(complex,md,lvl,language,additionalLang)));//добавим дочерние комплексы

        List<Program> childPrograms = md.findAllProgramBySection(section);
        strb.append(getProgramList(childPrograms,md,lvl,language,additionalLang));

        strb.append(noops[level]);
        if(level!=0) strb.append("</table>\n");


        return strb.toString();
    }
}
