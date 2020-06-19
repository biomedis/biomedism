package ru.biomedis.biomedismair3.Dialogs;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;
import ru.biomedis.biomedismair3.App;
import ru.biomedis.biomedismair3.BaseController;


import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

@Slf4j
public class DataPathDlg extends BaseController {

    @FXML
    private Label info;
    @FXML
    private Button pathDlg;
    @FXML
    private Button defaultPath;
    private ResourceBundle res;


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
    public void initialize(URL location, ResourceBundle resources) {

        if(getApp().getDataDir().getAbsolutePath().equals(getApp().getInnerDataDir().getAbsolutePath())){
            info.setText(resources.getString("default_path"));
        }else info.setText(getApp().getDataDir().getAbsolutePath());


        this.res=resources;

        if(getApp().getDataDir().getAbsolutePath().equals(getApp().getInnerDataDir().getAbsolutePath()))defaultPath.setDisable(true);
        else defaultPath.setDisable(false);

    }


    public void onDefaultPath(){

        if(getApp().getDataDir().getAbsolutePath().equals(getApp().getInnerDataDir().getAbsolutePath())) return;

        File dataDir=getApp().getDataDir();
        File dir =getApp().getInnerDataDir();

                Optional<ButtonType> buttonType = showConfirmationDialog(res.getString("app.ui.options.data_path"), "", res.getString("app.ui.set_data_dir_clean_q"), getControllerWindow(), Modality.WINDOW_MODAL);
        if(buttonType.isPresent()){
            if(buttonType.get()==okButtonType){
                try {
                    getModel().setOption("data_path","");
                    getApp().setDataDir(getApp().getInnerDataDir());
                    info.setText(res.getString("default_path"));
                    if(!getApp().getDataDir().getAbsolutePath().equals(dataDir.getAbsolutePath()))          cleanDataFilesAndState(dataDir);
                    defaultPath.setDisable(true);
                } catch (Exception e) {
                   log.error("Не удалось установить путь кданным в опициях");
                    showErrorDialog("Сброс пути к данным","","Не удалось установить путь к данным в опциях",getControllerWindow(),Modality.WINDOW_MODAL);
                    return;
                }

            }
        }





    }



    public void onSetPath() {

        File dataDir=getApp().getDataDir();

        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle(res.getString("app.ui.options.data_path"));
        dirChooser.setInitialDirectory(getApp().getDataDir());


        File dir = dirChooser.showDialog(getApp().getMainWindow());
        if (dir == null) return;
        if (!dir.exists()) {
            showWarningDialog(res.getString("app.ui.options.data_path"), res.getString("app.ui.dir_not_exist"), "", getControllerWindow(), Modality.WINDOW_MODAL);
            return;
        }

        if(!dir.canWrite()){
            showWarningDialog(res.getString("app.ui.options.data_path"), res.getString("app.ui.dir_cant_be_write"), "", getControllerWindow(), Modality.WINDOW_MODAL);
            return;
        }


        Optional<ButtonType> buttonType = showConfirmationDialog(res.getString("app.ui.options.data_path"), "", res.getString("app.ui.set_data_dir_clean_q"), getControllerWindow(), Modality.WINDOW_MODAL);
        if(buttonType.isPresent()){
            if(buttonType.get()==okButtonType){



                try {

                    if(dir.getAbsolutePath().equals(getApp().getInnerDataDir().getAbsolutePath()))
                    {
                        //если установили тот путь что по умолчанию
                        getModel().setOption("data_path","");
                        info.setText(res.getString("default_path"));
                        defaultPath.setDisable(true);
                    }else {
                        getModel().setOption("data_path",dir.getAbsolutePath());
                        getApp().setDataDir(dir);
                        info.setText(dir.getAbsolutePath());
                        defaultPath.setDisable(false);
                    }
                } catch (Exception e) {
                    log.error("Не удалось установить путь кданным в опциях");
                    showErrorDialog("Установка пути к данным","","Не удалось установить путь к данным в опциях",getControllerWindow(),Modality.WINDOW_MODAL);
                    return;
                }

                //если новый путь отличается от старого, то очистим файлы по старому пути
                if(!getApp().getDataDir().getAbsolutePath().equals(dataDir.getAbsolutePath()))          cleanDataFilesAndState(dataDir);
            }
        }
    }



    private void copyDataFiles(File prevDir, File newDir) throws Exception {
        for (File f : prevDir.listFiles())
        {
            if(f.getName().contains(".dat")) App.copyFile(f,new File(newDir,f.getName()));

        }
    }


    private void cleanDataFilesAndState(File prevDir) {




        //удаление dat файлов
        for (File f : prevDir.listFiles())
        {
            if(f.getName().contains(".dat")) f.delete();

        }
        //необходимость генерации программ
        getModel().setNeedGenerateAllTherapyProgramm();

    }

}
