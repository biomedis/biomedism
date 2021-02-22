package ru.biomedis.biomedismair3.UserUtils.Export;

import lombok.extern.slf4j.Slf4j;
import ru.biomedis.biomedismair3.ModelDataApp;
import ru.biomedis.biomedismair3.entity.TherapyComplex;
import ru.biomedis.biomedismair3.entity.TherapyProgram;
import ru.biomedis.biomedismair3.utils.Text.TextUtil;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

@Slf4j
public class ExportTherapyComplex {

  private static final String[] noops = new String[]{"", "   ", "       ", "           ",
      "               ", "                   ", "                       ",
      "                           ", "                               ",
      "                                   ", "                                       ",
      "                                           ",
      "                                               ",
      "                                                   ",
      "                                                       ",
      "                                                           "};

  public ExportTherapyComplex() {
  }

  public static boolean export(@NotNull List<TherapyComplex> complexes, @NotNull File file,
      ModelDataApp md) {
    if (complexes == null) {
      return false;
    } else if (complexes.isEmpty()) {
      return false;
    } else {
      StringBuilder cmpl = new StringBuilder();
      complexes.forEach(c -> cmpl.append(getComplex(c, md, 0)));

      try {
        OutputStreamWriter e = new OutputStreamWriter(new FileOutputStream(file), "UTF8");
        e.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        e.write("<UserComplexes>\n");
        e.write(cmpl.toString());
        e.write("</UserComplexes>");
        e.close();
        return true;
      } catch (IOException var5) {
        log.error("", var5);
        return false;
      }
    }
  }


  public static String exportToString(@NotNull List<TherapyComplex> complexes, ModelDataApp md) {

    StringBuilder cmpl = new StringBuilder();
    complexes.forEach(c -> cmpl.append(getComplex(c, md, 0)));

    StringBuilder strb = new StringBuilder();
    strb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    strb.append("<UserComplexes>\n");
    strb.append(cmpl.toString());
    strb.append("</UserComplexes>");
   return strb.toString();
  }

  private static String getProgramList(List<TherapyProgram> list, ModelDataApp md, int level) {
    StringBuilder strb = new StringBuilder();
    list.stream().filter(p -> !p.isMp3()).forEach((program) -> {
      String name = program.getName();
      if (program.getName() == null) {
        if (program.getOname() == null) {
          name = "Unknown name";
        } else {
          name = program.getOname();
        }
      }
      String descr = "";
      if (program.getDescription() != null) {
        descr = program.getDescription();
      }
      strb.append(noops[level]);
      strb.append("<Program ").append("name=\"").append(TextUtil.escapeXML(name))
          .append("\" description=\"").append(TextUtil.escapeXML(descr)).append("\" ")
          .append("frequencies=\"").append(program.getFrequencies().replace(",", ".")).append("\" ")
          .append("multy=\"").append(program.isMultyFreq()).append("\" ")
          .append("srcuuid=\"").append(program.getSrcUUID()).append("\" ")
          .append("/>\n");
    });
    return strb.toString();
  }

  private static String getComplex(TherapyComplex complex, ModelDataApp md, int level) {
    StringBuilder strb = new StringBuilder();
    strb.append(noops[level]);
    int lvl = level + 1;

    String name = complex.getName();
    if (complex.getName() == null) {
      if (complex.getOname() == null) {
        name = "Unknown name";
      } else {
        name = complex.getOname();
      }
    }
    String descr = "";
    if (complex.getDescription() != null) {
      descr = complex.getDescription();
    }

    strb.append("<Complex ").append("name=\"").append(TextUtil.escapeXML(name))
        .append("\" description=\"").append(TextUtil.escapeXML(descr)).append("\"")
        .append(" mullty=\"").append(true).append("\"")
        .append(" timeForFreq=\"").append(complex.getTimeForFrequency()).append("\"")
        .append(" bundlesLength=\"").append(complex.getBundlesLength()).append("\"")
        .append(" srcuuid=\"").append(complex.getSrcUUID()).append("\"")
        .append(" >\n");
    List list = md.findTherapyPrograms(complex);
    strb.append(getProgramList(list, md, lvl));
    strb.append(noops[level]);
    strb.append("</Complex>\n");
    return strb.toString();
  }
}
