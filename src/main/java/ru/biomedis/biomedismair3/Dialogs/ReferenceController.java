package ru.biomedis.biomedismair3.Dialogs;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import ru.biomedis.biomedismair3.BaseController;
import ru.biomedis.biomedismair3.UserUtils.CreateBaseHelper;
import ru.biomedis.biomedismair3.entity.Section;

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
    private @FXML Button homeBtn;
    private @FXML VBox menuPane;




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
        webView.setContextMenuEnabled(false);
        buildMenu();
        onHome();
    }

    private void viewPage(long sectionID){

        Platform.runLater(() -> webEngine.loadContent(buildPage(sectionID)));

    }

    private String buildPage(long sectionID) {
        Section section = getModel().findSection(sectionID);
        return CreateBaseHelper.getSection(section, getModel(), 0, getModel().getProgramLanguage());
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
        VBox.setMargin(link, new Insets(10,0,0,0));
        menuPane.getChildren().add(link);
    }

    private void addMenuItem(String name, Section section ){
        Hyperlink link =new Hyperlink(name);
        link.setUserData(section.getId());
        link.setOnAction(menuListener);
        VBox.setMargin(link, new Insets(0,0,0,10));
        menuPane.getChildren().add(link);
    }


    public void onHome(){
        String manualPage = getClass().getResource("/reference_main/index.html").toExternalForm();

        Platform.runLater(() -> webEngine.load(manualPage));
    }

}
