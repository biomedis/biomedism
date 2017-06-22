package ru.biomedis.starter;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import org.anantacreative.updater.Pack.Exceptions.PackException;
import org.anantacreative.updater.Pack.Packer;
import org.anantacreative.updater.Version;
import org.anantacreative.updater.VersionCheck.DefineActualVersionError;
import org.anantacreative.updater.VersionCheck.XML.XmlVersionChecker;

import javax.persistence.EntityManager;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;


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
    private Version version=null;
    @Override
    public void setParams(Object... params) {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        startProgram.setDisable(true);
        installUpdatesBtn.setDisable(true);
        checkVersion();
        webContent.getEngine().load("http://biomedis.ru");

    }


    public void onInstallUpdates(){
        AutoUpdater.getAutoUpdater().startUpdater(version, new AutoUpdater.Listener() {
            @Override
            public void taskCompleted() {

            }

            @Override
            public void error(Exception e) {

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

            }
        });
    }

    public void onStartProgram(){
        getApp().startMainApp();
    }



    private void setUpdateProgress(int persentVal){
        updateIndicator.setProgress(Double.valueOf(persentVal));
    }
    private void setVersionCheckIndicatorDone(){

        versionCheckIndicator.setProgress(100.0);
    }

    private void setTextInfo(String text){ textInfo.setText(text);}

    private void checkVersion(){

        try {
             version = selectUpdateVersion();
            XmlVersionChecker versionChecker = AutoUpdater.getAutoUpdater().getVersionChecker(version);
            boolean needUpdate = versionChecker.checkNeedUpdate();
            if(!needUpdate){
                System.out.println("Запуск программы, обновление не требуется");
                //getApp().startMainApp();
                return;
            }

            getControllerWindow().setTitle(getRes().getString("app.name")+" "+version);
            System.out.println("Актуальная версия" +versionChecker.getActualVersion());
            System.out.println("Current Version: "+version);

        }catch (AutoUpdater.NotSupportedPlatformException e){
            Log.logger.error(e.getMessage(),e);
            System.out.println("Запуск программы, ошибка получения информации об обновлении");
            //getApp().startMainApp();
            return;
        }catch (MalformedURLException e){
            Log.logger.error(e.getMessage(),e);
            System.out.println("Запуск программы, ошибка получения информации об обновлении");
            //getApp().startMainApp();
            return;
        }catch (DefineActualVersionError e){
            Log.logger.error(e.getMessage(),e);
            System.out.println("Запуск программы, ошибка получения информации об обновлении");
            //getApp().startMainApp();
            return;
        }
        catch (Exception e){
            Log.logger.error(e.getMessage(),e);
            System.out.println("Запуск программы, ошибка получения информации об обновлении");
            //getApp().startMainApp();
            return;
        }


    }




    /**
     * Получает значение версии обновления. Если ее вообще нет создасто нулевую
     * Установит значение в  this.updateVersion
     * @return вернет созданную или полученную опцию
     */
    public Version selectUpdateVersion() throws Exception {
        return new Version(4,selectMinor(),selectFix());
    }

    private int selectFix() throws Exception {
        return selectOptionIntValue("updateFixVersion");
    }
    private int selectMinor() throws Exception {
        return selectOptionIntValue("updateVersion");
    }


    private int  selectOptionIntValue(String name) throws Exception {
        EntityManager entityManager=null;
        Integer res;
        try
        {
            entityManager = getApp().emf.createEntityManager();
            String minor = (String)entityManager
                    .createNativeQuery("SELECT p.value FROM PROGRAMOPTIONS as p WHERE p.name = ?")
                    .setMaxResults(1)
                    .setParameter(1,name)
                    .getSingleResult();

            res = Integer.valueOf(minor);


        }catch (Exception e){
            throw new Exception("Ошибка получения версии",e);

        }finally {
            if(entityManager!=null)entityManager.close();
        }


        return res;
    }


    private boolean  selectOptionBooleanValue(String name) throws Exception {
        EntityManager entityManager=null;
        Boolean res;
        try
        {
            entityManager = getApp().emf.createEntityManager();
            String minor = (String)entityManager
                    .createNativeQuery("SELECT p.value FROM PROGRAMOPTIONS as p WHERE p.name = ?")
                    .setMaxResults(1)
                    .setParameter(1,name)
                    .getSingleResult();

            res = Boolean.valueOf(minor);


        }catch (Exception e){
            throw new Exception("Ошибка получения версии",e);

        }finally {
            if(entityManager!=null)entityManager.close();
        }


        return res;
    }


    //TODO доработать для Мак
    private void ZipDBToBackup() throws PackException {

        File rootDirApp = AutoUpdater.getAutoUpdater().getRootDirApp();
        File backupDir = new File(rootDirApp, "backup_db");
        if (!backupDir.exists()) backupDir.mkdir();
        File dbDir = null;
        if (AutoUpdater.isIDEStarted()) {
            dbDir = rootDirApp;
        } else if (OSValidator.isUnix()) {
            dbDir = new File(rootDirApp, "app");

        } else if (OSValidator.isWindows()) {
            dbDir = new File(rootDirApp, "assets");

        }

        Packer.packFiles(Stream.of(dbDir.listFiles((dir, name) -> name.endsWith(".db"))).collect(Collectors.toList()),
                new File(backupDir, Calendar.getInstance().getTimeInMillis() + ".zip"));


    }

    public void installUpdates() throws Exception {
        App.getAppController().getApp().closePersisenceContext();
        try {
            ZipDBToBackup();
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

    private ResourceBundle getRes() {
        return getApp().getResources();
    }
}
