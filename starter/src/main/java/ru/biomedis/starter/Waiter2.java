package ru.biomedis.starter;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Anama on 16.02.2016.
 */
public class Waiter2 extends BaseController
{
    @FXML
    VBox root;
   private Stage stage;

    @Override
    protected void onCompletedInitialise() {

    }

    @Override
    public void setParams(Object... params) {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }



    public static Waiter2 show(Window owner)
    {
        Waiter2 controller;
        Stage dlg = new Stage(StageStyle.TRANSPARENT);
        dlg.initOwner(owner);
        dlg.initModality(Modality.APPLICATION_MODAL);


        URL location = getApp().getClass().getResource("/fxml/waiter2.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(location,getApp().getResources());
        Parent root=null;
        try {
            root = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        controller = (Waiter2)fxmlLoader.getController();
        controller.setWindow(dlg);
        Scene scene = new Scene(root);
        //scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add("/styles/CalcLayer.css");
        dlg.setScene(scene);
        dlg.setResizable(false);
        controller.setStage(dlg);
        return controller;
    }

    public void setStage(Stage stage ){ this.stage=stage;}
    public void show(){
        stage.showAndWait();
    }
    public void close(){
        stage.close();
        stage =null;
    }
    public void minimize(){

        getApp().getMainWindow().setIconified(true);
    }
}
