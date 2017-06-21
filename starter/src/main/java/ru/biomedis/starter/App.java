package ru.biomedis.starter;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.anantacreative.updater.Version;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by anama on 21.06.17.
 */
public class App extends Application {

    public EntityManagerFactory emf=null;
    //public  ResourceBundle config=null;
    public ResourceBundle strings=null;
    private Stage mainWindow=null;


    private  static File dataDir;
    private  static  File innerDataDir;
    private  static  File tmpDir;


    private Version version;
    private static  AppController  controller;



    /**
     * текущая реальная версия после применения обновлений
     * @return
     */
    public Version getVersion() {
        return version;
    }



    public File getTmpDir() {
        return tmpDir;
    }

    public  File getInnerDataDir() {
        return innerDataDir;
    }

    public File getDataDir(){return dataDir;}

    public static AppController getAppController(){return controller;}

    public static File getTmpDir_() {
        return tmpDir;
    }

    public static File getInnerDataDir_() {
        return innerDataDir;
    }

    public static File getDataDir_(){return dataDir;}


    public ResourceBundle getResources(){return strings;}


    private List<CloseAppListener> closeAppListeners=new ArrayList<>();


    public void addCloseApplistener(CloseAppListener action)
    {
        closeAppListeners.add(action);
    }
    public void removeCloseAppListener(CloseAppListener action)
    {
        closeAppListeners.remove(action);
    }

    public static void disableAutoUpdate() {

    }

    public interface CloseAppListener
    {
        public void onClose();
    }

    /**
     * Установит путь к директории данных
     * @param dataDir
     */
    public  void setDataDir(File dataDir) {
        if(!dataDir.exists())dataDir.mkdir();
        App.dataDir = dataDir;

    }


    private int updateVersion=0;//версия установленного обновления

    public int getUpdateVersion() {
        return updateVersion;
    }


    public void closePersisenceContext() {
        if(emf!=null)emf.close();
        emf=null;
    }

    public void reopenPersistentContext(){
        System.out.println("Start reopen persistent context");
        closePersisenceContext();
        openPersisenceContext();
        System.out.println("Context reopened");
    }
    public void openPersisenceContext()
    {
        String puName="DB_UNIT";


        emf= Persistence.createEntityManagerFactory(puName);
    }

    public Stage getMainWindow(){return mainWindow;}



    @Override
    public void start(Stage stage) throws Exception {

        mainWindow=stage;


        openPersisenceContext();//откроем контекст работы с БД



        String javaVersion= System.getProperty("java.version");
        String[] split = javaVersion.split("\\.");
        String[] split1 = split[2].split("_");

        if(Integer.parseInt(split[1])>=8 && (Integer.parseInt(split1[0])+Integer.parseInt(split1[1]))>=40)
        {}
        else
        {
            throw new RuntimeException(this.strings.getString("app.lower_java_version"));
        }

        this.strings= ResourceBundle.getBundle("bundles.strings", new UTF8Control());

        if(emf==null) throw new RuntimeException("Нет соединения с базой");
        try {
            EntityManager entityManager = emf.createEntityManager();

            entityManager.createNativeQuery("SHOW TABLES").getResultList();

            entityManager.close();
        }catch (Exception ex)
        {


            BaseController.showInfoConfirmDialog(this.strings.getString("app.duplicate.title"), "", this.strings.getString("app.duplicate.content"), null, Modality.APPLICATION_MODAL);
            Platform.exit();
            return;
        }



        //путь к папке данных далее устанавливается из опций!!
        dataDir=new File("data");
        if(!dataDir.exists())dataDir.mkdir();
        innerDataDir=dataDir;

        //временная установка, нужно для обновлений
        tmpDir=new File(dataDir,"tmp");
        if(!tmpDir.exists()){
            tmpDir.mkdir();
            tmpDir=new File(dataDir,"tmp");
        }

        System.out.println("Data path: "+dataDir.getAbsolutePath());

        /******** Обновления ************/


        version =selectUpdateVersion();
        System.out.println("Current Version: "+version);




        //загрузим перевод интерфейса на выбранный язык!
        this.strings= ResourceBundle.getBundle("bundles.strings", new UTF8Control());


        BaseController.setApp(this);//установим ссылку на приложение для всех контроллеров
        stage.setTitle(this.strings.getString("app.name")+" "+version);
        URL ico = getClass().getResource("/images/icon.png");
        stage.getIcons().add(new Image(ico.toExternalForm()));

        /******* Загрузка главного окна ****/

        URL location = getClass().getResource("/fxml/Scene.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(location, this.strings);
        Parent root = fxmlLoader.load();
        controller = (AppController )fxmlLoader.getController();
        BaseController.setMainController(controller);

        /*********************************/

        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");

        stage.setScene(scene);
        stage.centerOnScreen();

        stage.setMinHeight(250);
        stage.setMinWidth(400);




        //перед закрытием произойдет уведомление всех подписчиков и закрытие глобальных контекстов
        stage.setOnCloseRequest(event -> {
            closeAppListeners.stream().forEach(listeners->listeners.onClose());
        });


        stage.show();


    }







    /**
     * Создаст индикаторный файл первого запуска в папке data c указанным именем
     * @param file дескриптор файла
     */
    private void createFirstStartFile(File file) throws IOException {

        try(FileWriter fw=new FileWriter(file) )
        {
            Calendar dt = Calendar.getInstance();
            fw.write(getUpdateVersion()+"\n");
            fw.write(dt.getTimeInMillis() +"\n");
            fw.write(dt.getTime() +"\n");
        }

    }




    /**
     * Получает значение версии обновления. Если ее вообще нет создасто нулевую
     * Установит значение в  this.updateVersion
     * @return вернет созданную или полученную опцию
     */
    public Version selectUpdateVersion()
    {
        return new Version(4,selectMinor(),selectFix());
    }

    private int selectFix(){
        return selectOptionIntValue("updateFixVersion");
    }
    private int selectMinor(){
        return selectOptionIntValue("updateVersion");
    }

    public boolean selectIsAutoUpdateEnabled(){
       return selectOptionBooleanValue("enable_autoupdate");
    }

    public void setAutoUpdateEnabled(boolean val) throws Exception{
        EntityManager entityManager=null;

        try
        {
            entityManager = emf.createEntityManager();
           int res = entityManager.createNativeQuery("UPDATE   PROGRAMOPTIONS SET value = :val WHERE name = 'enable_autoupdate'")
                                  .setParameter("val",val)
                                  .executeUpdate();
            if(res!=1) throw new Exception();

        }catch (Exception e){
            throw new RuntimeException("Ошибка установки значения enable_autoupdate");

        }finally {
            if(entityManager!=null)entityManager.close();
        }



    }


    private int  selectOptionIntValue(String name){
        EntityManager entityManager=null;
        Integer res;
        try
        {
            entityManager = emf.createEntityManager();
            String minor = (String)entityManager
                                     .createNativeQuery("SELECT value FROM PROGRAMOPTIONS WHERE name = :name LIMIT 1")
                                     .setParameter("name",name)
                                     .getSingleResult();

              res = Integer.valueOf(minor);


        }catch (Exception e){
            throw new RuntimeException("Ошибка получения версии");

        }finally {
            if(entityManager!=null)entityManager.close();
        }


        return res;
    }


    private boolean  selectOptionBooleanValue(String name){
        EntityManager entityManager=null;
        Boolean res;
        try
        {
            entityManager = emf.createEntityManager();
            String minor = (String)entityManager
                    .createNativeQuery("SELECT value FROM PROGRAMOPTIONS WHERE name = ? LIMIT 1",name)
                    .setParameter("name",name)
                    .getSingleResult();

            res = Boolean.valueOf(minor);


        }catch (Exception e){
            throw new RuntimeException("Ошибка получения версии");

        }finally {
            if(entityManager!=null)entityManager.close();
        }


        return res;
    }

    @Override
    public void stop() throws Exception {
        closePersisenceContext();
        super.stop(); //To change body of generated methods, choose Tools | Templates.
    }


    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }







}