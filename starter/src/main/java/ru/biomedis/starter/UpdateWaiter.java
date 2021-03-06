package ru.biomedis.starter;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
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

        Group group=new Group();
        ProgressIndicator pi=new ProgressIndicator();
        pi.setProgress(-1);
        group.getChildren().add(pi);

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
