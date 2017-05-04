package ru.biomedis.biomedismair3.Dialogs;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import ru.biomedis.biomedismair3.BaseController;
import ru.biomedis.biomedismair3.entity.Profile;
import ru.biomedis.biomedismair3.entity.TherapyComplex;
import ru.biomedis.biomedismair3.utils.Date.DateUtil;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;


import static ru.biomedis.biomedismair3.Log.logger;

/**
 * Created by Anama on 30.09.2015.
 */
public class PrintController extends BaseController {

    private
    @FXML
    WebView webView;
    private WebEngine webEngine;
    private
    @FXML
    Node root;

    private Long id;
    private int type;
    private List<Long> ids;
    private ResourceBundle res;

    @Override
    public void setParams(Object... params) {

        if (params.length == 2) {

            type = (Integer) params[1];
            if (type == 2) {

                ids = (List<Long>) params[0];

            } else id = (Long) params[0];


        }

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        res = resources;
        webEngine = webView.getEngine();

        Platform.runLater(() -> webEngine.loadContent(getContent()));


    }

    private String getContent() {

        StringBuilder strB = new StringBuilder();
        strB.append("<html>");
        strB.append("<title></title>");
        strB.append("<head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/></head>");
        strB.append("<body>");

        switch (type) {

            case 1:
                profileContent(strB, id);
                break;

            case 0:
                complexContent(strB, id, false);
                break;
            case 2:
                ids.forEach(itm -> complexContent(strB, itm, true));
                break;
        }


        strB.append("</body>");
        strB.append("</html>");
        return strB.toString();
    }

    public void onPrint() {

        PrinterJob job = PrinterJob.createPrinterJob();

        if (job != null) {
            try {
                if (job.showPrintDialog(root.getScene().getWindow())) {

                    webEngine.print(job);
                    job.endJob();
                }
            } catch (Exception e) {
                logger.error("", e);
                BaseController.showInfoDialog(res.getString("app.print"), res.getString("app.print_error_message"), "", root.getScene().getWindow(), Modality.WINDOW_MODAL);
            }

        }
    }


    private void complexContent(StringBuilder strb, long idComplex, boolean part) {
        TherapyComplex therapyComplex = getModel().findTherapyComplex(idComplex);
        if (getModel().countTherapyPrograms(therapyComplex, false) == 0) return;
        strb.append("<p></p>");

        if (part)
            strb.append("<h2>" + res.getString("app.therapy_complex") + " - " + therapyComplex.getName() + "</h2>");
        else strb.append("<h1>" + res.getString("app.therapy_complex") + " - " + therapyComplex.getName() + "</h1>");

        strb.append("<p>" + res.getString("ui.time_to_freq") + ": " +  String.valueOf((float)therapyComplex.getTimeForFrequency()/(float)60) + res.getString("app.minute") + "<br/>");
        strb.append(res.getString("ui.bundles_length") + ": " + therapyComplex.getBundlesLength() + "<br/>");
        strb.append(res.getString("app.table.complex_descr") + ": " + therapyComplex.getDescription() + "<br/>");

        strb.append(res.getString("app.table.delay") + ": " + DateUtil.replaceTime(DateUtil.convertSecondsToHMmSs(getModel().getTimeTherapyComplex(therapyComplex)), res) +"</p>");

        strb.append("<p></p>");

        // strb.append("<table border=\"1\" cellspacing=\"0\" cellpadding=\"5\" style=\"max-width:600px;\">");
        strb.append("<table border=\"1\" cellspacing=\"0\" cellpadding=\"5\" width=\"100%\">");
        strb.append("<tr>");
        strb.append("<th width=20%>" + res.getString("app.table.program_name") + "</th>");
        strb.append("<th width=80%>" + res.getString("app.table.freqs") + "</th>");
        strb.append("</tr>");

        getModel().findTherapyPrograms(therapyComplex).forEach(itm ->
        {
            strb.append("<tr>");
            strb.append("<td>" + itm.getName() + "</td>");
            strb.append("<td>" + itm.getFrequencies().replace(";", "; ") + "</td>");
            strb.append("</tr>");
        });

        strb.append("</table>");

    }

    private void profileContent(StringBuilder strb, long idProfile) {
        Profile profile = getModel().findProfile(idProfile);
        strb.append("<p></p>");
        strb.append("<h1>" + res.getString("app.profile") + " - " + profile.getName() + "</h1>");
        strb.append("<p>" + res.getString("app.table.delay") + ": " + DateUtil.replaceTime(DateUtil.convertSecondsToHMmSs(getModel().getTimeProfile(profile)), res) + "</p>");

        strb.append("<p></p>");
        getModel().findAllTherapyComplexByProfile(profile).forEach(itm -> complexContent(strb, itm.getId(), true));

    }

}

/*

public void print(final Node node) {
        Printer printer = Printer.getDefaultPrinter();
        PageLayout pageLayout = printer.createPageLayout(Paper.NA_LETTER, PageOrientation.PORTRAIT, Printer.MarginType.DEFAULT);
        double scaleX = pageLayout.getPrintableWidth() / node.getBoundsInParent().getWidth();
        double scaleY = pageLayout.getPrintableHeight() / node.getBoundsInParent().getHeight();
        node.getTransforms().add(new Scale(scaleX, scaleY));

        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null) {
            boolean success = job.printPage(node);
            if (success) {
                job.endJob();
            }
        }
    }
 */