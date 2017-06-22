package ru.biomedis.starter;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.web.WebView;

import java.net.URL;
import java.util.ResourceBundle;


public class AppController extends BaseController {

    @FXML private Button installUpdatesBtn;
    @FXML private Button startProgram;
    @FXML private WebView webContent;
    @FXML private ProgressIndicator versionCheckIndicator;
    @FXML private Label textInfo;
    @FXML private ProgressBar updateIndicator;
    @FXML private Hyperlink linkMain;
    @FXML private Hyperlink linkArticles;
    @FXML private Hyperlink linkForum;
    @FXML private Hyperlink linkVideo;
    @FXML private Hyperlink linkEducation;
    @FXML private Hyperlink linkVideoM;

    @Override
    public void setParams(Object... params) {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        webContent.getEngine().load("http://biomedis.ru");
    }


    public void onInstallUpdates(){

    }

    public void onStartProgram(){

    }
}
