package ru.biomedis.starter;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import org.anantacreative.updater.Pack.Exceptions.PackException;
import org.anantacreative.updater.Version;
import org.anantacreative.updater.VersionCheck.XML.XmlVersionChecker;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;


public class AppController extends BaseController {

    @FXML private Button installUpdatesBtn;
    @FXML private Button startProgramBtn;
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
    @FXML private ImageView errorImage;
    @FXML private ImageView doneImage;
    private Version version=null;

    @Override
    public void setParams(Object... params) {

    }

    private ResourceBundle getRes() {
        return getApp().getResources();
    }

    @Override
    protected void onCompletedInitialise() {
        try {
            version = DataHelper.selectUpdateVersion();
            getControllerWindow().setTitle(getRes().getString("app.name")+" "+version);
            System.out.println("Current Version: "+version);
            checkActualVersion();
            //webContent.getEngine().load("http://biomedis.ru");

        } catch (Exception e) {
            Log.logger.error("Ошибка определения версии программы", e);
            disableUpdateAndEnableStartProgram();
            setTextInfo("Ошибка определения версии программы");
            showErrorImage();
        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        disableUpdateAndStartProgram();
        initErrorImage();
        initDoneImage();
        hideErrorImage();
        hideVersionCheckIndicator();
        hideUpdateProgress();
    }


    public void onInstallUpdates(){
        disableUpdateAndStartProgram();
        showVersonCheckIndicator();
        AutoUpdater.getAutoUpdater().startUpdater(version, new AutoUpdater.Listener() {
            @Override
            public void taskCompleted() {

                Platform.runLater(() -> {
                    setTextInfo("Обновление готово к установке");
                    showDoneImage();
                    disableUpdateAndEnableStartProgram();
                });
            }

            @Override
            public void error(Exception e) {
                disableUpdateAndEnableStartProgram();
            }

            @Override
            public void completeFile(String name) {

            }

            @Override
            public void currentFileProgress(float val) {

            }

            @Override
            public void nextFileStartDownloading(String name) {

            }

            @Override
            public void totalProgress(float val) {
                System.out.println(val);
                setUpdateProgress(val);
            }
        });
    }

    public void hideErrorImage(){
        errorImage.setVisible(false);
    }

    public void showErrorImage(){
        errorImage.setVisible(true);
        hideVersionCheckIndicator();
        hideDoneImage();
    }

    public void hideDoneImage(){
        doneImage.setVisible(false);
    }

    public void showDoneImage(){
        doneImage.setVisible(true);
        hideVersionCheckIndicator();
        hideErrorImage();
    }
    private void initErrorImage(){
        URL errImgUrl = getClass().getResource("/images/error.png");
        errorImage.setImage(new Image(errImgUrl.toExternalForm()));
    }
    private void initDoneImage(){
        URL doneImgUrl = getClass().getResource("/images/correct.png");
        doneImage.setImage(new Image(doneImgUrl.toExternalForm()));
    }


    private void hideVersionCheckIndicator(){
        versionCheckIndicator.setProgress(-1);
        versionCheckIndicator.setVisible(false);


    }

    private void showVersonCheckIndicator(){
        versionCheckIndicator.setProgress(-1);
        versionCheckIndicator.setVisible(true);
        hideErrorImage();
        hideDoneImage();
    }

    private void hideUpdateProgress(){
        updateIndicator.setVisible(false);
    }

    private void setUpdateProgress(double persentVal){
        updateIndicator.setVisible(true);
        updateIndicator.setProgress(persentVal/100.0);
    }

    private void setTextInfo(String text){
        textInfo.setText(text);
    }
    public void onStartProgram(){
        getApp().startMainApp();
    }

    private void checkActualVersion(){
            showVersonCheckIndicator();

        try {

            final XmlVersionChecker versionChecker  = AutoUpdater.getAutoUpdater().getVersionChecker(version);
            versionChecker.checkNeedUpdateAsync()
                          .thenAccept(v->{
                              System.out.println("Актуальная версия" +versionChecker.getActualVersion());
                              if(v){
                                  setTextInfo("Текущая версия: "+ version +". Версия для обновления: " + versionChecker.getActualVersion());
                                  hideVersionCheckIndicator();
                                  enableUpdateAndStartProgram();
                              }else {
                                  setTextInfo("Текущая версия: "+ version +". Обновления не требуются.");
                                  hideVersionCheckIndicator();
                                  enableUpdateAndStartProgram();
                              }


                          })
                          .exceptionally(e->{
                              Log.logger.error("Ошибка определения актуальной версии",e);
                              disableUpdateAndEnableStartProgram();
                              showErrorImage();
                              printVersionCheckError();
                              return null;
                          });

        } catch (AutoUpdater.NotSupportedPlatformException e) {
            Log.logger.error("Ошибка определения актуальной версии",e);
            disableUpdateAndEnableStartProgram();
            showErrorImage();
            printVersionCheckError();
        } catch (MalformedURLException e) {
            Log.logger.error("Ошибка определения актуальной версии",e);
            disableUpdateAndEnableStartProgram();
            showErrorImage();
            printVersionCheckError();
        }
    }


    private void disableUpdateAndEnableStartProgram(){
        installUpdatesBtn.setDisable(true);
        startProgramBtn.setDisable(false);
    }

    private void enableUpdateAndStartProgram(){
        installUpdatesBtn.setDisable(false);
        startProgramBtn.setDisable(false);
    }

    private void disableUpdateAndStartProgram(){
        installUpdatesBtn.setDisable(true);
        startProgramBtn.setDisable(true);
    }

    private void printVersionCheckError(){
        Platform.runLater(() -> setTextInfo("Порверка наличия обновлений завершилась с ошибкой. Попробуйте повторить позже."));
    }





    public void installUpdates() throws Exception {
        App.getAppController().getApp().closePersisenceContext();
        try {
            DataHelper.ZipDBToBackup();
        } catch (PackException e) {
            Platform.runLater(() ->  {
                showExceptionDialog(getRes().getString("app.update"),
                        getApp().getResources().getString("backup_error"),
                        getRes().getString("process_updateing_stoped"),
                        e,
                        getApp().getMainWindow(), Modality.APPLICATION_MODAL);

            });

            throw new Exception();

        }finally {
            getApp().reopenPersistentContext();
        }
        try {
            AutoUpdater.getAutoUpdater().performUpdateTask().thenAccept(v -> {

                Platform.runLater(() ->  {
                    showInfoDialog(getRes().getString("app.update"),
                            getRes().getString("all_files_copied"),
                            getRes().getString("complete_update"),
                            getApp().getMainWindow(), Modality.APPLICATION_MODAL);
                   // restartProgram();
                });

            })
                       //.thenRun(this::restartProgram)
                       .exceptionally(e -> {

                           Exception ee;
                           if (e instanceof Exception) ee = (Exception) e;
                           else ee = new Exception(e);
                           Platform.runLater(() -> showExceptionDialog(getRes().getString("app.update"),
                                   getRes().getString("processing_updating_files_error"), "", ee,
                                   getApp().getMainWindow(), Modality.APPLICATION_MODAL));

                           return null;
                       });
        } catch (Exception e) {

        }
    }

}
