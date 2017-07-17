package ru.biomedis.biomedismair3.Dialogs;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.util.StringConverter;
import ru.biomedis.biomedismair3.BaseController;
import ru.biomedis.biomedismair3.UserUtils.CreateBaseHelper;
import ru.biomedis.biomedismair3.Waiter;
import ru.biomedis.biomedismair3.entity.Language;
import ru.biomedis.biomedismair3.entity.Section;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Created by Anama on 30.09.2015.
 */
public class ReferenceController extends BaseController {

    private @FXML    WebView webView;
    private WebEngine webEngine;
    private @FXML VBox menuPane;
    private @FXML ComboBox<Language> langBox;
    private long currendSection;



    @Override
    protected void onCompletedInitialise() {
        onHome();

    }

    @Override
    public void setParams(Object... params) {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {

        webEngine =   webView.getEngine();
        webView.setContextMenuEnabled(true);
        initLangs();
        buildMenu();
        onHome();
    }

    private void initLangs() {
        List<Language> langs = getModel().findAllLanguage()
                                           .stream()
                                           .filter(l -> l.isAvaliable())
                                           .collect(Collectors.toList());
        langBox.setConverter(new StringConverter<Language>() {
            @Override
            public String toString(Language object) {
                return object.getName();
            }

            @Override
            public Language fromString(String string) {
                return null;
            }
        });

        Language def = new Language();
        def.setId(0L);
        def.setName("-");
        langBox.getItems().add(def);
        langBox.getItems().addAll(langs);
        langBox.getSelectionModel().select(0);

        langBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.getId()==0) onHome();
            else {
                 if(currendSection==0)onHome();
                 else viewPage(currendSection);
            }
        });
    }


    private Language getAdditionalLang(){
        if(getModel().getProgramLanguage().getId().equals(langBox.getValue().getId())){
            return langBox.getItems().get(0);
        }
        return langBox.getValue();
    }

    private void viewPage(long sectionID){
        currendSection = sectionID;
        Platform.runLater(() -> {
            webEngine.loadContent(buildPage(sectionID));
            if(!getControllerWindow().isMaximized())getControllerWindow().setWidth(getControllerWindow().getWidth()+1);
        });

    }

    private String buildPage(long sectionID) {
        Section section = getModel().findSection(sectionID);
        StringBuilder strb=new StringBuilder();
        strb.append(CreateBaseHelper.getHtmlHeader(section, getModel(),  getModel().getProgramLanguage(),getAdditionalLang()))
            .append(CreateBaseHelper.getSection(section, getModel(), 0, getModel().getProgramLanguage(),getAdditionalLang()))
            .append(CreateBaseHelper.getHtmlBottom());
        return strb.toString();
    }

    private void buildMenu(){
        List<Section> rootSections = getModel().findAllRootSection().stream()
                                        .filter(s -> s.getTag()==null?true:!s.getTag().equals("USER"))
                                        .collect(Collectors.toList());
        getModel().initStringsSection(rootSections,getModel().getProgramLanguage(),true);

        for (Section rootSection : rootSections) {

            if (rootSection.getTag() != null) if (rootSection.getTag().equals("TRINITY")) {
                addRootItem(rootSection.getNameString(), rootSection);
                continue;
            }
            addRootItem(rootSection.getNameString());
            List<Section> innerSections = getModel().findAllSectionByParent(rootSection);
            getModel().initStringsSection(innerSections,getModel().getProgramLanguage(),true);
            for (Section innerSection : innerSections) {
                addMenuItem(innerSection.getNameString(), innerSection);
            }

        }

    }


    private   EventHandler<ActionEvent> menuListener = event -> {
        if(event.getSource() instanceof Hyperlink) {
            Hyperlink link =  (Hyperlink)event.getSource();
            link.setVisited(false);
            long sectionID = (Long)link.getUserData();
            viewPage(sectionID);
        }

    };

    /**
     * Некликабельный пункт
     * @param name
     */
    private void addRootItem(String name){
        Label title = new Label(name);
        title.setFont(new Font(13));
        VBox.setMargin(title,new Insets(10,0,0,0));
        title.setMaxWidth(Double.MAX_VALUE);
        title.setStyle("-fx-background-color: darkgray");
        menuPane.getChildren().add(title);

    }


    /**
     * Кликабельный пункт
     * @param name
     * @param section
     */
    private void addRootItem(String name, Section section){
        Hyperlink link =new Hyperlink(name);
        link.setUserData(section.getId());
        link.setFont(new Font(13));
        link.setOnAction(menuListener);
        link.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(link, new Insets(10,0,0,0));
        menuPane.getChildren().add(link);
    }

    private void addMenuItem(String name, Section section ){
        Hyperlink link =new Hyperlink(name);
        link.setUserData(section.getId());
        link.setOnAction(menuListener);
        VBox.setMargin(link, new Insets(0,0,0,10));
        link.setMaxWidth(Double.MAX_VALUE);
        menuPane.getChildren().add(link);
    }


    public void onHome(){
        String manualPage = getClass().getResource("/reference_main/index.html").toExternalForm();

        Platform.runLater(() -> webEngine.load(manualPage));
    }

    public void onSave(){
        File file;
        DirectoryChooser dirChooser =new DirectoryChooser();
        dirChooser.setTitle("Создание  справочника Выбор директории");
        dirChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        file= dirChooser.showDialog(getApp().getMainWindow());

        if(file==null)return;
        File file1=file;



        Task<Void>  task =new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                CreateBaseHelper.createHelpFiles(file1,getModel(),getModel().getProgramLanguage(),getAdditionalLang());
                return null;
            }
        };

        task.setOnFailed(event -> {

            Waiter.closeLayer();
            Platform.runLater(() -> {
                showExceptionDialog("Создание  справочников","","Завершено c ошибкоай",(Exception)  event.getSource().getException(),this.getControllerWindow(),Modality.WINDOW_MODAL);

            });
        });
        task.setOnSucceeded(event -> {
            Waiter.closeLayer();
            Platform.runLater(() -> {
                showInfoDialog("Создание  справочников","","Завершено",this.getControllerWindow(), Modality.WINDOW_MODAL);

            });
        });
        task.setOnRunning(event -> {
           Platform.runLater(() -> Waiter.openLayer(getControllerWindow(),true));
        });

        Thread t=new Thread(task);
        t.setDaemon(true);
        t.start();

    }

}
