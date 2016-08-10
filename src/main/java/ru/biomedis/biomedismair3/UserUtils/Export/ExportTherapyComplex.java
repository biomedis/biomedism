package ru.biomedis.biomedismair3.UserUtils.Export;

import ru.biomedis.biomedismair3.Log;
import ru.biomedis.biomedismair3.ModelDataApp;
import ru.biomedis.biomedismair3.entity.TherapyComplex;
import ru.biomedis.biomedismair3.entity.TherapyProgram;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

/**
 * Created by Anama on 17.09.2015.
 */

public class ExportTherapyComplex {
    private static final String[] noops = new String[]{"", "   ", "       ", "           ", "               ", "                   ", "                       ", "                           ", "                               ", "                                   ", "                                       ", "                                           ", "                                               ", "                                                   ", "                                                       ", "                                                           "};

    public ExportTherapyComplex() {
    }

    public static boolean export(@NotNull List<TherapyComplex> complexes, @NotNull File file, ModelDataApp md) {
        if(complexes == null) {
            return false;
        } else if(complexes.isEmpty()) {
            return false;
        } else {
            StringBuilder cmpl = new StringBuilder();
            complexes.forEach(c ->cmpl.append(getComplex(c, md, 0)));

            try {
                OutputStreamWriter e = new OutputStreamWriter(new FileOutputStream(file), "UTF8");
                e.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                e.write("<UserComplexes>\n");
                e.write(cmpl.toString());
                e.write("</UserComplexes>");
                e.close();
                return true;
            } catch (IOException var5) {
                Log.logger.error("", var5);
                return false;
            }
        }
    }

    private static String getProgramList(List<TherapyProgram> list, ModelDataApp md, int level) {
        StringBuilder strb = new StringBuilder();
        list.stream().filter(p ->!p.isMp3()).forEach((program) -> {
            strb.append(noops[level]);
            strb.append("<Program ").append("name=\"").append(program.getName().replace("\"","&quot;")).append("\" description=\"").append(program.getDescription().replace("\"","&quot;")).append("\" ").append("frequencies=\"").append(program.getFrequencies()).append("\"/>\n");
        });
        return strb.toString();
    }

    private static String getComplex(TherapyComplex complex, ModelDataApp md, int level) {
        StringBuilder strb = new StringBuilder();
        strb.append(noops[level]);
        int lvl = level + 1;
        strb.append("<Complex ").append("name=\"").append(complex.getName().replace("\"","&quot;")).append("\" description=\"").append(complex.getDescription().replace("\"","&quot;")).append("\"").append(" mullty=\"").append(complex.isMulltyFreq()).append("\"").append(" timeForFreq=\"").append(complex.getTimeForFrequency()).append("\"").append(" bundlesLength=\"").append(complex.getBundlesLength()).append("\"").append(">\n");
        List list = md.findTherapyPrograms(complex);
        strb.append(getProgramList(list, md, lvl));
        strb.append(noops[level]);
        strb.append("</Complex>\n");
        return strb.toString();
    }
}
