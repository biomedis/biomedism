package ru.biomedis.biomedismair3.UserUtils.Export;

import lombok.extern.slf4j.Slf4j;
import ru.biomedis.biomedismair3.ModelDataApp;
import ru.biomedis.biomedismair3.entity.Complex;
import ru.biomedis.biomedismair3.entity.Program;
import ru.biomedis.biomedismair3.entity.Section;
import ru.biomedis.biomedismair3.utils.Text.TextUtil;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;



@Slf4j
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
            e.printStackTrace();
            log.error("",e);
return false;
        }catch (Exception e){
            e.printStackTrace();
            log.error("",e);
            return false;
        }
        return true;
    }


    public static String exportToFile(@NotNull Section startedSection, ModelDataApp md) throws Exception
    {


        String tree = getSection(startedSection, md,0);



           StringBuilder strb = new StringBuilder();

            strb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

            strb.append("<UserBase>\n");
            strb.append(tree);
            strb.append("</UserBase>");

            return strb.toString();

    }


    private static String getProgram(Program program, ModelDataApp md,int level)
    {
        StringBuilder strb=new StringBuilder();
        strb.append(noops[level]);
        md.initStringsProgram(program);
        strb.append("<Program ").append("name=\"").append(TextUtil.escapeXML(program.getNameString())).append("\" description=\"").append(TextUtil.escapeXML(program.getDescriptionString())).
                append("\" ").append("frequencies=\"").append(program.getFrequencies()).append("\"/>\n");
        return strb.toString();

    }

    private static String getProgramList(List<Program> list, ModelDataApp md,int level)
    {

        StringBuilder strb=new StringBuilder();


        md.initStringsProgram(list);
        list.forEach(program -> {
            strb.append(noops[level]);
            strb.append("<Program ").append("name=\"").append(TextUtil.escapeXML(program.getNameString())).append("\" description=\"").append(TextUtil.escapeXML(program.getDescriptionString())).
                    append("\" ").append("frequencies=\"").append(program.getFrequencies().replace(",",".")).append("\"/>\n");
        });

        return strb.toString();
    }

    private static String getComplex(Complex complex, ModelDataApp md,int level)
    {
        StringBuilder strb=new StringBuilder();
        md.initStringsComplex(complex);

        strb.append(noops[level]);
        int lvl=level+1;
        strb.append("<Complex ").append("name=\"").append(TextUtil.escapeXML(complex.getNameString())).append("\" description=\"").append(TextUtil.escapeXML(complex.getDescriptionString())).
                append("\"").append(" timeForFreq=\"").append(complex.getTimeForFreq()).append("\" ").append(">\n");


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
            strb.append("<Section ").append("name=\"").append(TextUtil.escapeXML(section.getNameString())).append("\" description=\"").append(TextUtil.escapeXML(section.getDescriptionString())).
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
