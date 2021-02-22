package ru.biomedis.biomedismair3.UserUtils.Export;

import lombok.extern.slf4j.Slf4j;
import ru.biomedis.biomedismair3.ModelDataApp;
import ru.biomedis.biomedismair3.entity.*;
import ru.biomedis.biomedismair3.utils.Text.TextUtil;

import javax.validation.constraints.NotNull;
import java.io.*;
import java.util.List;


@Slf4j
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




        try {
            String prfl = getProfile(profile, md, 0);
           // FileWriter fw=new FileWriter(file);

            OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(file), "UTF8");

            fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

            fw.write("<UserProfile>\n");
            fw.write(prfl);
            fw.write("</UserProfile>");
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

    public static String exportToString(@NotNull Profile profile, ModelDataApp md) throws Exception
    {
        StringBuilder strb = new StringBuilder();

            String prfl = getProfile(profile, md, 0);

            strb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

            strb.append("<UserProfile>\n");
            strb.append(prfl);
            strb.append("</UserProfile>");

        return strb.toString();
    }


    private static String getProfile(Profile profile,ModelDataApp md,int level)
    {
        StringBuilder strb=new StringBuilder();
        strb.append(noops[level]);
        strb.append("<Profile ").append("position=\"").append(profile.getPosition()).append("\" ").append("name=\"").append(TextUtil.escapeXML(profile.getName())).append("\">\n");

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
            String name = program.getName();
            if(program.getName()==null){
                if(program.getOname()==null) name = "Unknown name";
                else name =program.getOname();
            }
            String descr="";
            if(program.getDescription()!=null)descr=program.getDescription();

            strb.append(noops[level]);
            strb.append("<Program ").append("name=\"").append(TextUtil.escapeXML(name)).append("\" description=\"").append(TextUtil.escapeXML(descr)).
                    append("\" ").append("frequencies=\"").append(program.getFrequencies().replace(",",".")).append("\" ")
                    .append("multy=\"").append(program.isMultyFreq()).append("\" ")
                    .append("srcuuid=\"").append(program.getSrcUUID()).append("\" ")
                    .append("/>\n");
        });

        return strb.toString();
    }


    private static String getComplex(TherapyComplex complex, ModelDataApp md,int level)
    {
        StringBuilder strb=new StringBuilder();

        strb.append(noops[level]);
        int lvl=level+1;

        String name = complex.getName();
        if(complex.getName()==null){
            if(complex.getOname()==null) name = "Unknown name";
            else name =complex.getOname();
        }
        String descr="";
        if(complex.getDescription()!=null)descr=complex.getDescription();

        strb.append("<Complex ")
            .append("name=\"")
            .append(TextUtil.escapeXML(name))
            .append("\" description=\"")
            .append(TextUtil.escapeXML(descr)).
                    append("\"")
            .append(" mullty=\"")
            .append(true).append("\"")
            .append(" timeForFreq=\"")
            .append(complex.getTimeForFrequency())
            .append("\"")
            .append(" bundlesLength=\"")
            .append(complex.getBundlesLength())
            .append("\"")
            .append(" srcuuid=\"")
            .append(complex.getSrcUUID())
            .append("\"")
            .append(" >\n");


        List<TherapyProgram> list = md.findTherapyPrograms(complex);
        strb.append(getProgramList(list, md,lvl));

        strb.append(noops[level]);
        strb.append("</Complex>\n");

        return strb.toString();
    }



}



