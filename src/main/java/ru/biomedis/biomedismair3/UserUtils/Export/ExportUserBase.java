package ru.biomedis.biomedismair3.UserUtils.Export;

import ru.biomedis.biomedismair3.Log;
import ru.biomedis.biomedismair3.ModelDataApp;
import ru.biomedis.biomedismair3.entity.Complex;
import ru.biomedis.biomedismair3.entity.Program;
import ru.biomedis.biomedismair3.entity.Section;

import javax.validation.constraints.NotNull;
import java.io.*;
import java.util.List;

import static ru.biomedis.biomedismair3.Log.logger;

/**
 * Created by Anama on 17.09.2015.
 */
public class ExportUserBase
{

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
     * Записывает структуру базы начиная с заданного раздела в XML файл
     * @param startedSection
     * @param file
     * @param md
     * @return True в случае успеха и False если произошла ошибка
     */
    public static boolean export(@NotNull Section startedSection,@NotNull  File file, ModelDataApp md)
    {
        if(startedSection==null) return false;


        String tree = getSection(startedSection, md,0);

        try {

            OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(file), "UTF8");

            fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

        fw.write("<UserBase>\n");
        fw.write(tree);
        fw.write("</UserBase>");
        fw.close();

        } catch (IOException e) {
            logger.error("",e);
return false;
        }
        return true;
    }


    private static String getProgram(Program program, ModelDataApp md,int level)
    {
        StringBuilder strb=new StringBuilder();
        strb.append(noops[level]);
        md.initStringsProgram(program);
        strb.append("<Program ").append("name=\"").append(program.getNameString()).append("\" description=\"").append(program.getDescriptionString()).
                append("\" ").append("frequencies=\"").append(program.getFrequencies()).append("\"/>\n");
        return strb.toString();

    }

    private static String getProgramList(List<Program> list, ModelDataApp md,int level)
    {

        StringBuilder strb=new StringBuilder();


        md.initStringsProgram(list);
        list.forEach(program -> {
            strb.append(noops[level]);
            strb.append("<Program ").append("name=\"").append(program.getNameString()).append("\" description=\"").append(program.getDescriptionString()).
                    append("\" ").append("frequencies=\"").append(program.getFrequencies()).append("\"/>\n");
        });

        return strb.toString();
    }

    private static String getComplex(Complex complex, ModelDataApp md,int level)
    {
        StringBuilder strb=new StringBuilder();
        md.initStringsComplex(complex);

        strb.append(noops[level]);
        int lvl=level+1;
        strb.append("<Complex ").append("name=\"").append(complex.getNameString()).append("\" description=\"").append(complex.getDescriptionString()).
                append("\"").append(">\n");


                List<Program> list = md.findAllProgramByComplex(complex);
                strb.append(getProgramList(list, md,lvl));

        strb.append(noops[level]);
        strb.append("</Complex>\n");

        return strb.toString();
    }


    private static String getSection(Section section,ModelDataApp md,int level)
    {
        StringBuilder strb=new StringBuilder();
        strb.append(noops[level]);
        if(level!=0)//исключаем стартовый раздел
        {
            md.initStringsSection(section);
            strb.append("<Section ").append("name=\"").append(section.getNameString()).append("\" description=\"").append(section.getDescriptionString()).
                    append("\"").append(">\n");

        }
        int lvl=level+1;
        List<Section> childSections = md.findAllSectionByParent(section);
        childSections.forEach(section1 -> strb.append(getSection(section1, md,lvl)));//добавятся дочернии разделы

        List<Complex> childComplexes = md.findAllComplexBySection(section);
        childComplexes.forEach(complex -> strb.append(getComplex(complex,md,lvl)));//добавим дочерние комплексы

        List<Program> childPrograms = md.findAllProgramBySection(section);
        strb.append(getProgramList(childPrograms,md,lvl));

             strb.append(noops[level]);
            if(level!=0) strb.append("</Section>\n");
        return strb.toString();
    }

}
