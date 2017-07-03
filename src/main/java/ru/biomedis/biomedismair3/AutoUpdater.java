package ru.biomedis.biomedismair3;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.stage.Modality;
import org.anantacreative.updater.FilesUtil;
import org.anantacreative.updater.Update.AbstractUpdateTaskCreator;
import org.anantacreative.updater.Update.UpdateException;
import org.anantacreative.updater.Update.UpdateTask;
import org.anantacreative.updater.Update.XML.XmlUpdateTaskCreator;
import org.anantacreative.updater.Version;
import org.anantacreative.updater.VersionCheck.DefineActualVersionError;
import org.anantacreative.updater.VersionCheck.XML.XmlVersionChecker;
import ru.biomedis.biomedismair3.utils.OS.OSValidator;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

/**
 * Автообновления
 */
public class AutoUpdater {
    private static AutoUpdater autoUpdater;
    private SimpleBooleanProperty processed = new SimpleBooleanProperty(false);
    private static UpdateTask updateTask;
    private File downloadDir;
    private SimpleBooleanProperty readyToInstall = new SimpleBooleanProperty(false);
    private File rootDirApp;


    private AutoUpdater() {

    }

    public File getRootDirApp() {
        return rootDirApp;
    }

    public synchronized static AutoUpdater getAutoUpdater() {
        if (autoUpdater == null) autoUpdater = new AutoUpdater();
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

    private ResourceBundle getRes() {
        return App.getAppController().getApp().getResources();
    }

    public File getDownloadUpdateDir() {
        return downloadDir;
    }

    public XmlVersionChecker getVersionChecker(Version currentVersion) throws NotSupportedPlatformException, MalformedURLException {
        XmlVersionChecker versionChecker = new XmlVersionChecker(currentVersion,
                new URL(getUpdaterBaseUrl() + "/version.xml"));
        return versionChecker;
    }

    private String getUpdaterBaseUrl() throws NotSupportedPlatformException {
        if(OSValidator.isWindows())return "http://www.biomedis.ru/doc/b_mair/starter/win";
        else  if(OSValidator.isUnix())return "http://www.biomedis.ru/doc/b_mair/starter/linux";
        else  if(OSValidator.isMac())return "http://www.biomedis.ru/doc/b_mair/starter/osx";
        else {
            throw new NotSupportedPlatformException();
        }



    }

    public interface Listener {
        void taskCompleted();

        void error(Exception e);

        void completeFile(String name);

        void currentFileProgress(float val);

        void nextFileStartDownloading(String name);

        void totalProgress(float val);
    }




    public void startUpdater(Version currentVersion,Listener listener) {
        if (isProcessed()) {

            Platform.runLater(() -> App.getAppController().showWarningDialog(
                    getRes().getString("app.update"),
                    getRes().getString("upgrade_process"),
                    "", App.getAppController().getApp().getMainWindow(), Modality.WINDOW_MODAL));

            return;
        }

        Thread thread = new Thread(() -> {
            System.out.println("startUpdater");


            rootDirApp = null;
            try {
                rootDirApp = defineRootDirApp();
                if (rootDirApp == null) throw new Exception();
                System.out.println(rootDirApp.getAbsolutePath());
            } catch (Exception e) {
                updateNotAvailableOnPlatformMessage();
            }


            setProcessed(true);
            setReadyToInstall(false);
            try {
                XmlVersionChecker versionChecker = getVersionChecker(currentVersion);
                if (versionChecker.checkNeedUpdate()) {

                    File dlDir = new File(App.getInnerDataDir_(),
                            "downloads" + File.separator + versionChecker.getActualVersion()
                                                                         .toString()
                                                                         .replace(".", "_"));
                    final XmlUpdateTaskCreator updater = new XmlUpdateTaskCreator(
                            FilesUtil.extractRelativePathFrom(rootDirApp, dlDir)
                            , rootDirApp, new AbstractUpdateTaskCreator.Listener() {
                        @Override
                        public void taskCompleted(UpdateTask updateTask, File rootDirApp, File downloadDir) {
                            System.out.println("Закачали обнову");
                            AutoUpdater.updateTask = updateTask;
                            setProcessed(false);
                            setReadyToInstall(true);
                            listener.taskCompleted();
                        }

                        @Override
                        public void error(Exception e) {
                            setProcessed(false);
                            Log.logger.error("", e);
                            listener.error(e);


                        }

                        @Override
                        public void completeFile(String s, File file) {
                            System.out.println("Закачан файл "+s);
                            listener.completeFile(s);
                        }

                        @Override
                        public void currentFileProgress(float v) {
                            listener.currentFileProgress(v);
                        }

                        @Override
                        public void nextFileStartDownloading(String s, File file) {
                            System.out.println("Начат файл "+s);
                            listener.nextFileStartDownloading(s);
                        }

                        @Override
                        public void totalProgress(float v) {
                            listener.totalProgress(v);


                        }
                    }, new URL(getUpdaterBaseUrl() + "/update.xml"));

                    downloadDir = updater.getDownloadsDir();
                    updater.createTask(false);

                } else {
                    setProcessed(false);
                }

            } catch (MalformedURLException e) {
                listener.error(e);
                setProcessed(false);
            } catch (DefineActualVersionError e) {
                listener.error(e);
                setProcessed(false);

            } catch (AbstractUpdateTaskCreator.CreateUpdateTaskError e) {
                listener.error(e);
                setProcessed(false);
            } catch (NotSupportedPlatformException e) {
                listener.error(e);
                setProcessed(false);

                updateNotAvailableOnPlatformMessage();

            }
        });

        thread.start();
    }

    public static boolean isIDEStarted() {
        File innerDataDir = App.getInnerDataDir_();
        File rootDir = new File(innerDataDir, "../");
        return rootDir.listFiles((dir, name) -> name.equals("pom.xml")).length == 1;
    }

    private File defineRootDirApp() throws Exception {
        File rootAppDir;
        if (isIDEStarted()) rootAppDir = new File("./");
        else {
            if (OSValidator.isWindows()) rootAppDir = new File(App.getInnerDataDir_(), "../../");
            else if (OSValidator.isMac())
                rootAppDir = new File(App.getInnerDataDir_(), "../../");//TODO: корректировать на MAC
            else if (OSValidator.isUnix()) rootAppDir = new File(App.getInnerDataDir_(), "../../");
            else throw new Exception();
        }
        return rootAppDir;
    }

    private void updateNotAvailableOnPlatformMessage() {
        try {
            App.getAppController().getModel().disableAutoUpdate();
        } catch (Exception e) {
            Log.logger.error("", e);
        }
        Platform.runLater(() -> App.getAppController().showWarningDialog(
                getRes().getString("app.update"),
                getRes().getString("platform_updates_not_av"),
                ""
                , App.getAppController().getApp().getMainWindow(),
                Modality.WINDOW_MODAL));
    }


    private SimpleBooleanProperty isPerformAction = new SimpleBooleanProperty(false);

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
     * Совершает обновление, асинхронно.
     *
     * @return CompletableFuture
     */
    public void performUpdateTask(UpdateTask.UpdateListener listener) throws Exception {

           setReadyToInstall(false);
           setProcessed(true);
           if (isPerformAction()) throw new UpdateInProcessException();
           setPerformAction(true);
           makeUpdateActions(listener)
                   .thenAccept(v -> {
                       FilesUtil.recursiveClear(getDownloadUpdateDir());
                       setProcessed(false);
                       setPerformAction(false);
                       listener.completed();

                   })
                   .exceptionally(e -> {
                       setProcessed(false);
                       setPerformAction(false);
                       listener.error(new UpdateException(e));
                       return null;
                   });





    }

    private  CompletableFuture<Void> makeUpdateActions(UpdateTask.UpdateListener listener) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        if (updateTask != null) {

                updateTask.update(new UpdateTask.UpdateListener() {
                    @Override
                    public void progress(int i) {
                        listener.progress(i);
                    }

                    @Override
                    public void completed() {
                        future.complete(null);
                    }

                    @Override
                    public void error(UpdateException e) {
                        listener.error(e);

                       future.completeExceptionally(e);
                    }
                });



        }else future.completeExceptionally(new NullPointerException("listener == null"));
        return future;
    }



    private AppController getController(){return App.getAppController();}


    public static class UpdateInProcessException extends Exception{

    }


    private void restartProgram() {
/*
 Runtime.getRuntime().addShutdownHook(new Thread() {
    public void run() {
    ((Window) view).setVisible(false);
    Runtime.halt(0);
    }
    });
 */
        if(AutoUpdater.isIDEStarted()) return;

        try {
            File currentJar = new File(AppController.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if(!currentJar.getName().endsWith(".jar")) throw new Exception("Не найден путь к jar");

            //TODO Сделать для MacOs
            final List<String> command = new ArrayList<>();
            String exec="";
            if(OSValidator.isUnix()){
                exec = new File(currentJar.getParentFile(),"../BiomedisMAir4").getAbsolutePath();

            }else if(OSValidator.isWindows()){
                exec = new File(currentJar.getParentFile(),"../BiomedisMAir4.exe").getAbsolutePath();

            }else return;
            command.add(exec);


            final ProcessBuilder builder = new ProcessBuilder(command);
            builder.start();
            //Platform.exit();
            System.out.println("restartProgram");
            System.exit(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }




    public static class NotSupportedPlatformException extends Exception {
    }
}
