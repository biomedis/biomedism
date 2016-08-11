package ru.biomedis.biomedismair3.UpdateUtils.FrequenciesBase;

import ru.biomedis.biomedismair3.Log;
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
import java.util.stream.Collectors;

import static ru.biomedis.biomedismair3.Log.logger;

/**
 * Создает файл для правки частот, позже можно будет его загрузить и подправить частоты
 * Created by Ananta on 10.08.2016.
 */
public class CreateFrequenciesFile
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
     *
     * @param file
     * @param md
     * @return True в случае успеха и False если произошла ошибка
     */
    public static boolean export(@NotNull File file, ModelDataApp md)
    {


        List<Section> allRootSection = md.findAllRootSection().stream().filter(i->{
            if(i.getTag()==null) return true;
            else return !i.getTag().equals("USER");
        }).collect(Collectors.toList());

        StringBuilder strb=new StringBuilder();
        allRootSection.forEach(section -> {

            Log.logger.info("Обработка раздела ...");
            strb.append(getSection(section, md,0));});
        Log.logger.info("ОК");


        try {

            OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(file), "UTF8");

            fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            fw.write("<FrequenciesBaseFile>\n");
            fw.write(strb.toString());
            fw.write("</FrequenciesBaseFile>");
            fw.close();

        } catch (IOException e) {
            logger.error("",e);
            return false;
        }
        return true;
    }


    private static String getProgram(Program program, ModelDataApp md, int level)
    {
        StringBuilder strb=new StringBuilder();
        strb.append(noops[level]);
        md.initStringsProgram(program);
        strb.append("<Program ").append("uuid=\"").append(program.getUuid()).append("\" ").append("name=\"").append(TextUtil.escapeXML(program.getNameString())).append("\" ").append("frequencies=\"").append(program.getFrequencies()).append("\"/>\n");
        return strb.toString();

    }

    private static String getProgramList(List<Program> list, ModelDataApp md, int level)
    {

        StringBuilder strb=new StringBuilder();


        md.initStringsProgram(list);
        list.forEach(program -> {
            strb.append(noops[level]);
            strb.append("<Program ").append("uuid=\"").append(program.getUuid()).append("\" ").append("name=\"")
                    .append(TextUtil.escapeXML(program.getNameString())).append("\" ").append("frequencies=\"").append(program.getFrequencies()).append("\"/>\n");
        });

        return strb.toString();
    }

    private static String getComplex(Complex complex, ModelDataApp md, int level)
    {
        StringBuilder strb=new StringBuilder();
        md.initStringsComplex(complex);

        strb.append(noops[level]);
        int lvl=level+1;
        strb.append("<Complex ").append("uuid=\"").append(complex.getUuid()).append("\" ").append("name=\"").append(TextUtil.escapeXML(complex.getNameString())).append("\" ").append(">\n");


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
            strb.append("<Section ").append("uuid=\"").append(section.getUuid()).append("\" ").append("name=\"").append(TextUtil.escapeXML(section.getNameString())).append("\" ").append(">\n");

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
