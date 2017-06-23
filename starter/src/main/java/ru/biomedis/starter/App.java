package ru.biomedis.starter;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

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

    private EntityManagerFactory emf=null;
    //public  ResourceBundle config=null;
    private ResourceBundle strings=null;
    private Stage mainWindow=null;


    private  static File dataDir;
    private  static  File innerDataDir;
    private  static  File tmpDir;



    private static  AppController  controller;







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
        if(AutoUpdater.isIDEStarted()) puName="DB_UNIT_IDE";


        emf= Persistence.createEntityManagerFactory(puName);
    }

    public Stage getMainWindow(){return mainWindow;}


    public EntityManagerFactory getEntityManagerFactory(){
        return emf;
    }

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

        File firstStartFileIndicator=new File(getInnerDataDir(),"fs.ind");
        if(firstStartFileIndicator.exists()) {
            System.out.println("Запуск программы. Первый запуск после свежей установки");
            startMainApp();
            return;
        }

        /******** Обновления ************/







        //загрузим перевод интерфейса на выбранный язык!
        this.strings= ResourceBundle.getBundle("bundles.strings", new UTF8Control());


        BaseController.setApp(this);//установим ссылку на приложение для всех контроллеров
        stage.setTitle(this.strings.getString("app.name"));
        URL ico = getClass().getResource("/images/icon.png");
        stage.getIcons().add(new Image(ico.toExternalForm()));

        /******* Загрузка главного окна ****/

        URL location = getClass().getResource("/fxml/Scene.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(location, this.strings);
        Parent root = fxmlLoader.load();
        controller = (AppController )fxmlLoader.getController();
        BaseController.setMainController(controller);
        controller.setWindow(stage);
        controller.onCompletedInitialise();


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


    protected void startMainApp(){

        closePersisenceContext();

        if(AutoUpdater.isIDEStarted()) {
            Platform.exit();
            return;

        }

        try {
            File currentJar = new File(App.class.getProtectionDomain().getCodeSource().getLocation().toURI());
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




    /**
     * Создаст индикаторный файл первого запуска в папке data c указанным именем
     * @param file дескриптор файла
     */
    private void createFirstStartFile(File file) throws IOException {

        try(FileWriter fw=new FileWriter(file) )
        {
            Calendar dt = Calendar.getInstance();
            fw.write(dt.getTimeInMillis() +"\n");
            fw.write(dt.getTime() +"\n");
        }

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