package ru.biomedis.biomedismair3.Dialogs;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.WindowEvent;
import ru.biomedis.biomedismair3.BaseController;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Anama on 30.09.2015.
 */
public class AboutController extends BaseController {

    private @FXML    WebView webView;
    private WebEngine webEngine;

    @Override
    protected void onCompletedInitialization() {

    }

    @Override
    protected void onClose(WindowEvent event) {

    }

    @Override
    public void setParams(Object... params) {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {

        webEngine =   webView.getEngine();
        String manualPage = getClass().getResource("/about/"+getModel().getProgramLanguage().getAbbr()+"/index.html").toExternalForm();

        Platform.runLater(() -> webEngine.load(manualPage));


    }
}
