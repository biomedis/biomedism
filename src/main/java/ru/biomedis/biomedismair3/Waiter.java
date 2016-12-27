package ru.biomedis.biomedismair3;

import javafx.application.Platform;
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
public class Waiter extends BaseController
{
    @FXML
    VBox root;


    @Override
    public void setParams(Object... params) {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }




    private static Waiter controller;
    private static Stage stage;


    private static Stage init(Window owner)
    {
        Stage dlg = new Stage(StageStyle.TRANSPARENT);
        dlg.initOwner(owner);
        dlg.initModality(Modality.WINDOW_MODAL);


        URL location = getApp().getClass().getResource("/fxml/waiter.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(location,getApp().getResources());
        Parent root=null;
        try {
            root = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        controller = (Waiter)fxmlLoader.getController();
        controller.setWindow(dlg);
        Scene scene = new Scene(root);
        //scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add("/styles/CalcLayer.css");
        dlg.setScene(scene);
        dlg.setResizable(false);
        return dlg;
    }


    /**
     * Открыть диалог расчета
     *
     * @return
     */
    public  synchronized static void openLayer(Window owner,boolean show)
    {
        if(controller==null)
        {
            stage = init(owner);
        }

       if(show) stage.showAndWait();
        //обработка кнопки  отмены




    }
    public  synchronized static void show(){stage.showAndWait();}
    /**
     * Скрывает окно
     */
    public synchronized  static void closeLayerSilent()
    {

        Platform.runLater(() -> stage.hide());
    }

    /**
     * Закрывает окно
     */
    public synchronized  static void closeLayer()
    {

        Platform.runLater(() -> stage.hide() );
    }

    /**
     * Удаление окна
     */
    public synchronized  static void destroyLayer()
    {

        controller=null;
        Platform.runLater(() -> {stage.close();stage=null;});

    }

    public void minimize(){

        getApp().getMainWindow().setIconified(true);
    }
}
