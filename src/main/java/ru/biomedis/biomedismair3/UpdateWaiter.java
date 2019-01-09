package ru.biomedis.biomedismair3;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;



/**
 * Created by Anama on 16.02.2016.
 */
public class UpdateWaiter
{


    private static Stage stage;


    private static Stage init()
    {
        Stage dlg = new Stage(StageStyle.TRANSPARENT);
        dlg.initOwner(null);
        dlg.initModality(Modality.APPLICATION_MODAL);

        VBox group=new VBox();
        ProgressIndicator pi=new ProgressIndicator();
        pi.setProgress(-1);
        group.getChildren().add(pi);
        group.getChildren().add(new Label("Updating! Please wait."));

        Scene scene = new Scene(group);

        dlg.setScene(scene);
        dlg.setResizable(false);
        return dlg;
    }


    /**
     * Открыть диалог расчета
     *
     * @return
     */
    public  synchronized static void show()
    {
        if(stage==null)
        {
            stage = init();
        }

        stage.showAndWait();
        //обработка кнопки  отмены




    }


    /**
     * Закрывает окно
     */
    public synchronized  static void close()
    {

         stage.close();
    }


}
