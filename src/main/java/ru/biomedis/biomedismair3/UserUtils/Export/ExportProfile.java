package ru.biomedis.biomedismair3.UserUtils.Export;

import ru.biomedis.biomedismair3.ModelDataApp;
import ru.biomedis.biomedismair3.entity.*;

import javax.validation.constraints.NotNull;
import java.io.*;
import java.util.List;

/**
 * Created by Anama on 17.09.2015.
 */
public class ExportProfile {

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
     * Записывает структуру профиля в XML файл
     * @param profile
     * @param file
     * @param md
     * @return True в случае успеха и False если произошла ошибка
     */
    public static boolean export(@NotNull Profile profile,@NotNull File file, ModelDataApp md)
    {
        if(profile==null) return false;


        String prfl = getProfile(profile, md, 0);

        try {

           // FileWriter fw=new FileWriter(file);

            OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(file), "UTF8");

            fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

            fw.write("<UserProfile>\n");
            fw.write(prfl);
            fw.write("</UserProfile>");
            fw.close();

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    private static String getProfile(Profile profile,ModelDataApp md,int level)
    {
        StringBuilder strb=new StringBuilder();
        strb.append(noops[level]);
        strb.append("<Profile ").append("name=\"").append(profile.getName()).append("\">\n");

        int lvl=level+1;
          md.findAllTherapyComplexByProfile(profile).forEach(therapyComplex -> strb.append(getComplex(therapyComplex,md,lvl)));

        strb.append(noops[level]);
        strb.append("</Profile>\n");
        return strb.toString();

    }

    private static String getProgramList(List<TherapyProgram> list, ModelDataApp md,int level)
    {

        StringBuilder strb=new StringBuilder();

        list.stream().filter(p->!p.isMp3()).forEach(program -> {
            strb.append(noops[level]);
            strb.append("<Program ").append("name=\"").append(program.getName()).append("\" description=\"").append(program.getDescription()).
                    append("\" ").append("frequencies=\"").append(program.getFrequencies()).append("\"/>\n");
        });

        return strb.toString();
    }


    private static String getComplex(TherapyComplex complex, ModelDataApp md,int level)
    {
        StringBuilder strb=new StringBuilder();

        strb.append(noops[level]);
        int lvl=level+1;
        strb.append("<Complex ").append("name=\"").append(complex.getName()).append("\" description=\"").append(complex.getDescription()).
                append("\"").append(" mullty=\"").append(complex.isMulltyFreq()).append("\"").append(" timeForFreq=\"").append(complex.getTimeForFrequency()).append("\"").append(" bundlesLength=\"").append(complex.getBundlesLength()).append("\"").append(">\n");


        List<TherapyProgram> list = md.findTherapyPrograms(complex);
        strb.append(getProgramList(list, md,lvl));

        strb.append(noops[level]);
        strb.append("</Complex>\n");

        return strb.toString();
    }



}



