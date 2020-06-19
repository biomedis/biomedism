package ru.biomedis.biomedismair3;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;


/**
 *  CalcLayer.openLayer(() -> CalcLayer.closeLayerSilent(), root.getScene().getWindow());
 * 
 */
@Slf4j
public class CalcLayer extends BaseController
{
    @FXML    VBox root;
    @FXML      Label info;
    @FXML    Button cancel;





private CalcActionListener actionListener;

    @Override
    protected void onCompletedInitialise() {

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
        Platform.runLater(() ->
        {
            double xPosParent= getApp().getMainWindow().getX();
            double yPosParent= getApp().getMainWindow().getY();

            root.getScene().getWindow().setX(xPosParent + getApp().getMainWindow().getWidth() - root.getWidth());
            root.getScene().getWindow().setY(yPosParent +30);
        });

    }



    public  static boolean isOpen(){
        if(stage==null) return false;
        return stage.isShowing();
    }


    public void onCancel()
{
cancel.setDisable(true);
    if(actionListener!=null) actionListener.onCancel();
}

protected void enableCancellBtn(){cancel.setDisable(false);}

    public void setActionListener(CalcActionListener a){actionListener=a;}




    private void setInfo(String txt)
    {
        info.setText(txt);
    }
    private void setDisableCancel(boolean val){cancel.setDisable(val);}


    public synchronized static void  setInfoLayer(String txt){controller.setInfo(txt);}

    public synchronized static void  setDisableCancelLayer(boolean val){controller.setDisableCancel(val);}




    private static CalcLayer controller;
    private static Stage stage;


    private static Stage init(Window owner)
    {
        Stage dlg = new Stage(StageStyle.TRANSPARENT);
        dlg.initOwner(owner);
        dlg.initModality(Modality.WINDOW_MODAL);


        URL location = getApp().getClass().getResource("/fxml/CalcLayer.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(location, getApp().getResources());
        Parent root=null;
        try {
            root = fxmlLoader.load();
        } catch (IOException e) {
               log.error("",e);
            return null;
        }
        controller = (CalcLayer)fxmlLoader.getController();
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
     * @param actionListener листнер исполнится при нажатии кнопки отмены
     *                        @param owner владелец окна
     *                         @param show показать сразу окно или можно позжеоткрыть его методом showLayer()
     * @return
     */
    public  synchronized static void openLayer(CalcActionListener actionListener, Window owner,boolean show)
    {

        if(controller==null)
        {
           stage = init(owner);
        }
        controller.setActionListener(actionListener);
        controller.enableCancellBtn();
       if(show) stage.showAndWait();
        //обработка кнопки  отмены




    }

    public synchronized static void showLayer(){
        controller.enableCancellBtn();
        stage.showAndWait();
    }

    public  synchronized static void openLayer(CalcActionListener actionListener, Window owner)
    {
        openLayer(actionListener,owner,true);
    }

    public synchronized  static void closeLayerSilent()
    {

        Platform.runLater(() -> stage.hide());
    }

    public synchronized  static void closeLayer()
    {
        controller.onCancel();
       Platform.runLater(() -> stage.hide() );
    }

    /**
     * Удаление окна
     */
    public synchronized  static void destroyLayer()
    {
        controller.setActionListener(null);
        controller=null;
        Platform.runLater(() -> {stage.close();stage=null;});

    }

    public void minimize(){

        getApp().getMainWindow().setIconified(true);
    }

    public static final CalcLayer getLayer(){return controller;}
}
