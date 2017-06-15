package ru.biomedis.biomedismair3;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import org.anantacreative.updater.FilesUtil;
import org.anantacreative.updater.Update.AbstractUpdateTaskCreator;
import org.anantacreative.updater.Update.UpdateActionException;
import org.anantacreative.updater.Update.UpdateTask;
import org.anantacreative.updater.Update.XML.XmlUpdateTaskCreator;
import org.anantacreative.updater.VersionCheck.DefineActualVersionError;
import org.anantacreative.updater.VersionCheck.XML.XmlVersionChecker;
import ru.biomedis.biomedismair3.utils.OS.OSValidator;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Автообновления
 */
public class AutoUpdater {
    private static AutoUpdater autoUpdater;
    private SimpleBooleanProperty processed = new SimpleBooleanProperty(false);
    private static UpdateTask updateTask;
    private File downloadDir;
    private SimpleBooleanProperty readyToInstall =new SimpleBooleanProperty(false);


    private AutoUpdater() {

    }

    public synchronized static AutoUpdater getAutoUpdater(){
        if(autoUpdater ==null) autoUpdater =new AutoUpdater();
        return autoUpdater;
    }

    public boolean isReadyToInstall() {
        return readyToInstall.get();
    }

    public SimpleBooleanProperty readyToInstallProperty() {
        return readyToInstall;
    }

    public void setReadyToInstall(boolean readyToInstall) {
        this.readyToInstall.set(readyToInstall);
    }

    public boolean isProcessed() {
        return processed.get();
    }

    private synchronized void setProcessed(boolean processed) {
        this.processed.set(processed);
    }

    public SimpleBooleanProperty processedProperty() {
        return processed;
    }

    public File getDownloadUpdateDir(){
        return downloadDir;
    }
    public void startUpdater(boolean silentProcess) {
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
           setProcessed(true);
            setReadyToInstall(false);
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
                            AutoUpdater.updateTask = updateTask;
                            if(!silentProcess) Platform.runLater(() -> App.getAppController().hideProgressBar(true));
                            toUpdate();


                        }

                        @Override
                        public void error(Exception e) {
                            setProcessed(false);
                            Log.logger.error("",e);
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
                           if(!silentProcess) Platform.runLater(() ->  App.getAppController().setProgressBar(v,"Загрузка файлов",""));


                        }
                    },new URL(updaterBaseURL+"/update.xml"));

                    downloadDir = updater.getDownloadsDir();
                    updater.createTask(false);

                }else   {
                    setProcessed(false);
                    if(!silentProcess)  Platform.runLater(() -> App.getAppController().showWarningDialog("Обновление",
                            "Обновления отсутствуют. \nВы уже используете последнюю версию программы",
                            "",App.getAppController().getApp().getMainWindow(),Modality.WINDOW_MODAL));

                    return;

                }

            } catch (MalformedURLException e) {
                Platform.runLater(() -> App.getAppController().showErrorDialog("Доступ к серверу обновлений","Отсутствует доступ к серверу обновлений",
                        "Возможно отсутствует интернет или соединение блокируется брандмауэром",
                        App.getAppController().getApp().getMainWindow(),Modality.WINDOW_MODAL));
                setProcessed(false);
            } catch (DefineActualVersionError e) {
                Platform.runLater(() ->App.getAppController().showErrorDialog("Определение версии обновления","Ошибка получения данных",
                        "Возможно отсутствует интернет или соединение блокируется брандмауэром или ошибка на сервере.",App.getAppController().getApp().getMainWindow(),Modality.WINDOW_MODAL));
                setProcessed(false);

            } catch (AbstractUpdateTaskCreator.CreateUpdateTaskError e) {
                Platform.runLater(() -> App.getAppController().showErrorDialog("Подготовка к обновлению","",
                        "не удалось подготовить обновление",
                        App.getAppController().getApp().getMainWindow(),Modality.WINDOW_MODAL));
                setProcessed(false);
            }
        });

        thread.start();
    }

    public static boolean isIDEStarted(){
        File innerDataDir = App.getInnerDataDir_();
        File rootDir = new File(innerDataDir, "../");
        return rootDir.listFiles((dir, name) -> name.equals("pom.xml")).length ==1;
    }

    private File getRootDirApp() throws Exception {
        File rootAppDir;
        if( isIDEStarted()) rootAppDir = new File("./");
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
                ,App.getAppController().getApp().getMainWindow(),Modality.WINDOW_MODAL));
    }




    private SimpleBooleanProperty isPerformAction=new SimpleBooleanProperty(false);

    public boolean isPerformAction() {
        return isPerformAction.get();
    }

    public SimpleBooleanProperty performActionProperty() {
        return isPerformAction;
    }

    private void setPerformAction(boolean performAction) {
        this.isPerformAction.set(performAction);
    }

    /**
     * Совершает обновление, асинхронно. Рестарт программы нужно делать самим уже после окончания обновления
     * @return CompletableFuture
     */
    public CompletableFuture<Void> performUpdateTask() throws Exception {
        setReadyToInstall(false);
        if(isPerformAction()) throw new Exception();
        Platform.runLater(()->  Waiter.openLayer(App.getAppController().getApp().getMainWindow(), true));
        setPerformAction(true);
        CompletableFuture<Void> future = new CompletableFuture<>();

        CompletableFuture.runAsync(this::makeUpdateActions)
                           .thenAccept(v->{
                            setProcessed(false);

                            FilesUtil.recursiveClear(getDownloadUpdateDir());
                            future.complete(null);
                               setPerformAction(false);
                               Platform.runLater(()->  Waiter.closeLayer());

                        })
                         .exceptionally(e->{

                            setProcessed(false);
                             future.completeExceptionally(e);
                             setPerformAction(false);
                             Platform.runLater(()->  Waiter.closeLayer());
                            return null;
                        });

        return future;
    }

    private void makeUpdateActions() {
        if(updateTask!=null) {
            setProcessed(true);
            try {

                updateTask.update();
                setProcessed(false);
            } catch (UpdateActionException e) {
                setProcessed(false);
                throw new RuntimeException(e);
            }

        }
    }

    private void toUpdate(){

        while (Waiter.isOpen() || CalcLayer.isOpen()){
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                setProcessed(false);
                return;
            }
        }


        Platform.runLater(() -> {
            Optional<ButtonType> buttonType = App.getAppController()
                                                 .showConfirmationDialog("Обновление программы",
                                                         "Все файлы загружены. Программы готова к обновлению",
                                                         "Выполнить обновление(произойдет перезапуск программы) или отложить вопрос до следующего запуска программы?"
                                                         ,
                                                         App.getAppController().getApp().getMainWindow(),
                                                         Modality.APPLICATION_MODAL);
            if(buttonType.isPresent()){
                if(buttonType.get() == App.getAppController().okButtonType){

                    App.getAppController().onInstallUpdates();

                }else {
                    setProcessed(false);
                    setReadyToInstall(true);
                }
            }else {
                setProcessed(false);
                setReadyToInstall(true);
            }
        });
    }


}
