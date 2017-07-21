package ru.biomedis.biomedismair3.UpdateUtils.FrequenciesBase;

import ru.biomedis.biomedismair3.ModelDataApp;
import ru.biomedis.biomedismair3.entity.Complex;
import ru.biomedis.biomedismair3.entity.Language;
import ru.biomedis.biomedismair3.entity.Program;
import ru.biomedis.biomedismair3.entity.Section;
import ru.biomedis.biomedismair3.m2.LanguageDevice;
import ru.biomedis.biomedismair3.utils.Text.TextUtil;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.stream.Collectors;

import static ru.biomedis.biomedismair3.Log.logger;

/**
 * Создает набор файлов для правки переводов. Выдает только те названия в которых есть латинские символы в словах.
 * Используется для корректировки переводов типа греческого итп
 * Created by Ananta on 10.08.2016.
 */
public class CreateLanguageFilesCheckLatin
{

    private static final String[] noops={
            "",
            " ",
            " ",
            " ",
            " ",
            " ",
            " ",
            " ",
            " ",
            " ",
            " ",
            " ",
            " ",
            " ",
            " ",
            " ",
            " ",
            " ",
            " ",
            " ",
            " ",
            " "
    };

    /**
     *
     * Записывает файлы переводов в выбранную папку
     *
     * @param file папка для записи
     * @param md модель данных
     * @return True в случае успеха и False если произошла ошибка
     */
    public static void export(@NotNull File file, ModelDataApp md)
    {


        List<Section> allRootSection = md.findAllRootSection().stream().filter(i->{
            if(i.getTag()==null) return true;
            else return i.getTag().equals("USER")==false;
        }).collect(Collectors.toList());


        for (Language language : md.findAllLanguage())
        {
            if(md.getUserLanguage().getId().longValue()==language.getId().longValue()) continue;
            if(doExport(allRootSection,language,new File(file,language.getName()+".xml"),md)) System.out.println("Обработка языка "+language.getName()+" УСПЕШНО");
            else System.out.println("Обработка языка "+language.getName()+" ОШИБКА");
        }




    }

    private static boolean doExport(List<Section> allRootSection, Language language, File file, ModelDataApp md){

        System.out.print("Обработка языка "+language.getName()+" ... ");
        StringBuilder strb=new StringBuilder();
        allRootSection.forEach(section -> strb.append(getSection(section, md,0,language)));


        try {

            OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(file), "UTF8");

            fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

            fw.write("<LanguageBaseFile lang=\""+language.getAbbr()+"\">\n");
            fw.write("<!--  ИСПОЛЬЗУЙТЕ ВМЕСТО КОВЫЧЕК В НАЗВАНИЯХ И ОПИСАНИЯХ  -  &quot; -->\n");
            fw.write(strb.toString());
            fw.write("</LanguageBaseFile>");
            fw.close();

        } catch (IOException e) {
            logger.error("",e);
            System.out.print("Error "+e.getMessage());
            return false;
        }finally
        {
            System.out.print("Ок");
        }
        return true;
    }




    private static String getProgramList(List<Program> list, ModelDataApp md, int level,Language language)
    {

        StringBuilder strb=new StringBuilder();


        md.initStringsProgram(list,language);

        Language l=md.getDefaultLanguage();

        try {
            l= md.getLanguage("ru");
        }catch (Exception e){}
        String lname;
        String ldescr;
       for (Program program : list){



             lname=md.getString2(program.getName(),l);
             ldescr=md.getString2(program.getDescription(),l);
            if(LanguageDevice.hasLatinInString(lname)==false  && LanguageDevice.hasLatinInString(ldescr)==false) continue;

           //System.out.println(program.getId()+" - "+lname+" - "+program.getNameString());
            strb.append(noops[level]);
            strb.append("<Program ").append("uuid=\"").append(program.getUuid()).append("\"  \n")
                    .append(noops[level]).append("nameRussian=\"").append(TextUtil.escapeXML(lname)).append("\"  \n")
                    .append(noops[level]).append("name=\"").append(TextUtil.escapeXML(program.getNameString())).append("\" \n")

                    .append(noops[level]).append("descriptionRussian=\"").append(TextUtil.escapeXML(ldescr)).append("\"  \n")
                    .append(noops[level]).append("description=\"").append(TextUtil.escapeXML(program.getDescriptionString())).append("\"  \n")
                    .append(noops[level]).append("/>\n\n");
        }

        return strb.toString();
    }

    private static String getComplex(Complex complex, ModelDataApp md, int level, Language language)
    {
        if(!complex.isOwnerSystem()) return "";
        StringBuilder strb=new StringBuilder();
        StringBuilder strb2=new StringBuilder();
        md.initStringsComplex(complex,language);

        Language l=md.getDefaultLanguage();
            boolean latin=false;
        try {
            l= md.getLanguage("ru");
        }catch (Exception e){}

        String lname=md.getString2(complex.getName(),l);
        String ldescr=md.getString2(complex.getDescription(),l);
        latin = LanguageDevice.hasLatinInString(lname)||LanguageDevice.hasLatinInString(ldescr);

           // System.out.println(complex.getId()+" - "+lname+" - "+complex.getNameString());
        strb.append(noops[level]);
        int lvl=level+1;
        strb2.append("<Complex ").append("uuid=\"").append(complex.getUuid()).append("\"  \n")
                .append(noops[level]).append("nameRussian=\"").append(TextUtil.escapeXML(lname)).append("\"  \n")
                .append(noops[level]).append("name=\"").append(TextUtil.escapeXML(complex.getNameString())).append("\" \n")
                .append(noops[level]) .append("descriptionRussian=\"").append(TextUtil.escapeXML(ldescr)).append("\"  \n")
                .append(noops[level]).append("description=\"").append(TextUtil.escapeXML(complex.getDescriptionString())).append("\"  \n")
                .append(noops[level]).append(">\n");


        if(latin) strb.append(strb2);
        List<Program> list = md.findAllProgramByComplex(complex);
        strb.append(getProgramList(list, md,lvl,language));

        strb.append(noops[level]);
        if(latin)strb.append("</Complex>\n");

        return strb.toString();
    }


    private static String getSection(Section section,ModelDataApp md,int level,Language language)
    {
        if(!section.isOwnerSystem()) return "";
        StringBuilder strb=new StringBuilder();
        StringBuilder strb2=new StringBuilder();
    boolean latin =false;
        strb.append(noops[level]);
        if(level!=0)//исключаем стартовый раздел
        {

            Language l=md.getDefaultLanguage();

            try {
                l= md.getLanguage("ru");
            }catch (Exception e){}

            String lname=md.getString2(section.getName(),l);
            String ldescr=md.getString2(section.getDescription(),l);
            latin = LanguageDevice.hasLatinInString(lname)||LanguageDevice.hasLatinInString(ldescr);
            md.initStringsSection(section,language);
            strb2.append("<Section ")
                    .append("uuid=\"").append(section.getUuid()).append("\"  \n")
                    .append(noops[level]) .append("nameRussian=\"").append(TextUtil.escapeXML(lname)).append("\"  \n")
                    .append(noops[level]).append("name=\"").append(TextUtil.escapeXML(section.getNameString())).append("\" \n")
                    .append(noops[level]).append("descriptionRussian=\"").append(TextUtil.escapeXML(ldescr)).append("\"  \n")
                    .append(noops[level]).append("description=\"").append(TextUtil.escapeXML(section.getDescriptionString())).append("\"  \n")
                    .append(noops[level]).append(">\n");

        }
        if(latin) strb.append(strb2);
        int lvl=level+1;
        List<Section> childSections = md.findAllSectionByParent(section);
        childSections.forEach(section1 -> strb.append(getSection(section1, md,lvl,language)));//добавятся дочернии разделы

        List<Complex> childComplexes = md.findAllComplexBySection(section);
        childComplexes.forEach(complex -> strb.append(getComplex(complex,md,lvl,language)));//добавим дочерние комплексы

        List<Program> childPrograms = md.findAllProgramBySection(section);
        strb.append(getProgramList(childPrograms,md,lvl,language));

        strb.append(noops[level]);
        if(level!=0) if(latin) strb.append("</Section>\n");
        return strb.toString();
    }

}
