package ru.biomedis.biomedismair3;

import javafx.application.Platform;
import javafx.stage.Modality;
import org.anantacreative.updater.FilesUtil;
import org.anantacreative.updater.Update.AbstractUpdateTaskCreator;
import org.anantacreative.updater.Update.UpdateTask;
import org.anantacreative.updater.Update.XML.XmlUpdateTaskCreator;
import org.anantacreative.updater.VersionCheck.DefineActualVersionError;
import org.anantacreative.updater.VersionCheck.XML.XmlVersionChecker;
import ru.biomedis.biomedismair3.utils.OS.OSValidator;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Автообновления
 */
public class AutoUpdater {
    private static AutoUpdater autoUpdater;
    private boolean processed = false;
    private AutoUpdater() {

    }

    public synchronized static AutoUpdater getAutoUpdater(){
        if(autoUpdater ==null) autoUpdater =new AutoUpdater();
        return autoUpdater;
    }

    public boolean isProcessed() {
        return processed;
    }

    private synchronized void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public void startUpdater() {
        if(isProcessed()){

            Platform.runLater(() -> App.getAppController().showWarningDialog("Обновление",
                    "Процесс обновления уже запущен. Дождитесь уведомления",
                    "",App.getAppController().getApp().getMainWindow(),Modality.WINDOW_MODAL));

            return;
        }

        Thread thread =new Thread(() -> {
            System.out.println("startUpdater");



            File rootDirApp=null;
            try {
                rootDirApp = getRootDirApp();
                if(rootDirApp==null) throw new Exception();
                System.out.println(rootDirApp.getAbsolutePath());
            } catch (Exception e) {
                updateNotAvailableOnPlatformMessage();
            }
            String updaterBaseURL = "http://www.biomedis.ru/doc/b_mair/updater";
            if(OSValidator.isWindows()) updaterBaseURL=updaterBaseURL+"/win";
            else if(OSValidator.isMac()) updaterBaseURL=updaterBaseURL+"/mac";
            else if(OSValidator.isUnix()) updaterBaseURL=updaterBaseURL+"/linux";
            else {
                updateNotAvailableOnPlatformMessage();
                return;
            }
            processed=true;
            try {
                XmlVersionChecker versionChecker=new XmlVersionChecker(App.getAppController().getApp().getVersion(),new URL(updaterBaseURL+"/version.xml"));
                if(versionChecker.checkNeedUpdate()){


                   final XmlUpdateTaskCreator updater = new XmlUpdateTaskCreator(new File(App.getInnerDataDir_(),
                            "downloads"+File.separator+versionChecker.getActualVersion().toString().replace(".","_"))
                            , rootDirApp, new AbstractUpdateTaskCreator.Listener() {
                        @Override
                        public void taskCompleted(UpdateTask updateTask,File rootDirApp, File downloadDir) {
                            System.out.println("Закачали обнову");
                            System.out.println(updateTask.toString());

                            FilesUtil.recursiveClear(downloadDir);
                            processed=false;
                        }

                        @Override
                        public void error(Exception e) {
                            processed=false;
                            Platform.runLater(() -> App.getAppController().showErrorDialog("Получение обновлений","",
                                    "Не удалось получить обновления. Попробуйте перезапустить программу и проверить доступ к сети.",App.getAppController().getApp().getMainWindow(),
                                    Modality.WINDOW_MODAL));
                        }

                        @Override
                        public void completeFile(String s, File file) {

                        }

                        @Override
                        public void currentFileProgress(float v) {

                        }

                        @Override
                        public void nextFileStartDownloading(String s, File file) {

                        }

                        @Override
                        public void totalProgress(float v) {

                        }
                    },new URL(updaterBaseURL+"/update.xml"));
                    updater.createTask(false);

                }else  processed=false;

            } catch (MalformedURLException e) {
                Platform.runLater(() -> App.getAppController().showErrorDialog("Доступ к серверу обновлений","Отсутствует доступ к серверу обновлений",
                        "Возможно отсутствует интернет или соединение блокируется брандмауэром",
                        App.getAppController().getApp().getMainWindow(),Modality.WINDOW_MODAL));
                processed=false;
            } catch (DefineActualVersionError e) {
                Platform.runLater(() ->App.getAppController().showErrorDialog("Определение версии обновления","Ошибка получения данных",
                        "Возможно отсутствует интернет или соединение блокируется брандмауэром или ошибка на сервере.",App.getAppController().getApp().getMainWindow(),Modality.WINDOW_MODAL));
                processed=false;

            } catch (AbstractUpdateTaskCreator.CreateUpdateTaskError e) {
                Platform.runLater(() -> App.getAppController().showErrorDialog("Подготовка к обновлению","",
                        "не удалось подготовить обновление",
                        App.getAppController().getApp().getMainWindow(),Modality.WINDOW_MODAL));
                processed=false;
            }
        });

        thread.start();
    }

    private boolean isIDEStarted(){
        File innerDataDir = App.getInnerDataDir_();
        File rootDir = new File(innerDataDir, "../");
        return rootDir.listFiles((dir, name) -> name.equals("pom.xml")).length ==1;
    }

    private File getRootDirApp() throws Exception {
        File rootAppDir;
        if( isIDEStarted()) rootAppDir = new File("");
        else {
            if (OSValidator.isWindows()) rootAppDir = new File(App.getInnerDataDir_(),"../../");
            else if (OSValidator.isMac()) rootAppDir = new File(App.getInnerDataDir_(),"../../");//TODO: корректировать на MAC
            else if (OSValidator.isUnix()) rootAppDir =new File(App.getInnerDataDir_(),"../../");
            else throw new Exception();
        }
        return rootAppDir;
    }

    private void updateNotAvailableOnPlatformMessage(){
        try {
            App.getStaticModel().disableAutoUpdate();
        } catch (Exception e) {
            Log.logger.error("",e);
        }
        Platform.runLater(() ->   App.getAppController().showWarningDialog("Обновление программы","На данной платформе автоматическое обновление не доступно",""
                ,App.getAppController().getControllerWindow(),Modality.WINDOW_MODAL));
    }


    public static class UpdateInProgressException extends Exception{

    }
}
