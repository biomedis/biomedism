package ru.biomedis.biomedismair3.UserUtils;

import ru.biomedis.biomedismair3.ModelDataApp;
import ru.biomedis.biomedismair3.entity.Complex;
import ru.biomedis.biomedismair3.entity.Language;
import ru.biomedis.biomedismair3.entity.Program;
import ru.biomedis.biomedismair3.entity.Section;
import ru.biomedis.biomedismair3.utils.Text.TextUtil;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.stream.Collectors;

import static ru.biomedis.biomedismair3.Log.logger;


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
                    exportSection(section,new File(contentDir,section.getId()+".html"),mda, language,section.getNameString());
                    System.out.println("..OK");
                }

                fw.write("</body></html>\n");

                fw.close();

            }

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
    public static boolean exportSection(@NotNull Section startedSection, @NotNull  File file, ModelDataApp md,Language language,String name)
    {
        if(startedSection==null) return false;


        String tree = getSection(startedSection, md,0,language);
        //внутри раздела - тут это раздылы базы -общие, терапевт итп. Вложенных подразделов нет. Мы представляем подразделы и комплексы как таблицы с заголовками. В них программы с частотами
        try {

            OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(file), "UTF8");

            fw.write("<!DOCTYPE html>\n");
            fw.write("<html><head>  <meta charset=\"utf-8\"/><title>"+name+"</title></head><body>\n");
            fw.write("<h1 style='text-align:left;'>"+name+"</h1>");
           fw.write(tree);
            fw.write("</body></html>");
            fw.close();

        } catch (IOException e) {
            logger.error("",e);
            return false;
        }
        return true;
    }


    private static String getProgram(Program program, ModelDataApp md, int level,Language language)
    {
        StringBuilder strb=new StringBuilder();
        strb.append(noops[level]);
        md.initStringsProgram(program,language,true);
        strb.append("<tr><td><strong>")
                .append(TextUtil.escapeXML(program.getNameString()))
                .append("</strong></td></tr>")
                .append("<tr><td>")
                .append(program.getFrequencies().replace(",",".").replace(";","; ").replace("+","; "))
                .append("</td></tr>");

        return strb.toString();

    }

    private static String getProgramList(List<Program> list, ModelDataApp md,int level,Language language)
    {

        StringBuilder strb=new StringBuilder();


        md.initStringsProgram(list,language,true);
        list.forEach(program -> {
            strb.append(noops[level]);

            strb.append("<tr><td><strong>")
                .append(TextUtil.escapeXML(program.getNameString()))
                .append("</strong></td></tr>")
                .append("<tr><td>").append(program.getFrequencies().replace(",",".").replace(";","; ").replace("+","; "))
                .append("</td></tr>");

        });

        return strb.toString();
    }

    private static String getComplex(Complex complex, ModelDataApp md, int level,Language language)
    {
        StringBuilder strb=new StringBuilder();
        md.initStringsComplex(complex,language,true);

        strb.append(noops[level]);
        int lvl=level+1;
        strb.append("<br/><br/><table border='1' cellspacing='0' cellpadding='5' width='100%'>")
                .append( "<caption><h2>")
                .append(TextUtil.escapeXML(complex.getNameString()))
                .append("</h2></caption>");


        List<Program> list = md.findAllProgramByComplex(complex);
        strb.append(getProgramList(list, md,lvl,language));

        strb.append(noops[level]);
        strb.append("</table>\n");

        return strb.toString();
    }


    private static String getSection(Section section,ModelDataApp md,int level,Language language)
    {
        StringBuilder strb=new StringBuilder();
        strb.append(noops[level]);
        if(level!=0)//исключаем стартовый раздел
        {
            md.initStringsSection(section,language,true);
            strb.append("<br/><br/><table border='1' cellspacing='0' cellpadding='5'  width='100%'>")
                    .append( "<caption><h2>")
                    .append(TextUtil.escapeXML(section.getNameString()))
                    .append("</h2></caption>");


        }
        int lvl=level+1;
        List<Section> childSections = md.findAllSectionByParent(section);
        childSections.forEach(section1 -> strb.append(getSection(section1, md,lvl,language)));//добавятся дочернии разделы

        List<Complex> childComplexes = md.findAllComplexBySection(section);
        childComplexes.forEach(complex -> strb.append(getComplex(complex,md,lvl,language)));//добавим дочерние комплексы

        List<Program> childPrograms = md.findAllProgramBySection(section);
        strb.append(getProgramList(childPrograms,md,lvl,language));

        strb.append(noops[level]);
        if(level!=0) strb.append("</table>\n");
        return strb.toString();
    }
}
