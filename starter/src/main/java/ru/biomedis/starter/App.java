package ru.biomedis.starter;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.anantacreative.updater.VersionCheck.Version;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import org.terracotta.ipceventbus.event.Event;
import org.terracotta.ipceventbus.event.EventBusServer;
import org.terracotta.ipceventbus.io.Pipe;
import org.terracotta.ipceventbus.proc.AnyProcess;
import org.terracotta.ipceventbus.proc.Bus;
import org.terracotta.ipceventbus.proc.EventJavaProcess;

/**
 * Created by anama on 21.06.17.
 */
public class App extends Application {

    private EntityManagerFactory emf=null;
    //public  ResourceBundle config=null;
    private ResourceBundle strings=null;
    private Stage mainWindow=null;
    private Version starterVersion;


    private  static File dataDir;
    private  static  File innerDataDir;
    private  static  File tmpDir;



    private static  AppController  controller;
    private Locale programLocale;


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

    public Version getStarterVersion() {
        return starterVersion;
    }

    public Stage getMainWindow(){return mainWindow;}


    public EntityManagerFactory getEntityManagerFactory(){
        return emf;
    }

    @Override
    public void start(Stage stage) throws Exception {

        mainWindow=stage;
        starterVersion = new Version(1,4,0);

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
/*
не ясно нужно ли так делать. По факту необязательно. Если человк загрузит поверх еще обновления хуже не будет тк они все кумулятивные и пройдут как положенно
        File firstStartFileIndicator=new File(getInnerDataDir(),"fs.ind");
        if(firstStartFileIndicator.exists()) {
            System.out.println("Запуск программы. Первый запуск после свежей установки");
            startMainApp();
            return;
        }

        */

        /******** Обновления ************/









        BaseController.setApp(this);//установим ссылку на приложение для всех контроллеров
        programLocale = setLocale();
        //загрузим перевод интерфейса на выбранный язык!
        this.strings= ResourceBundle.getBundle("bundles.strings", new UTF8Control());

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
            if(AutoUpdater.getAutoUpdater().isProcessed()){
                event.consume();
                return;
            }
            closeAppListeners.stream().forEach(listeners->listeners.onClose());
            WebHelper.getWebHelper().close();
        });


        stage.show();


    }

    /**
     * Локаль установленная для лаунчера
     * @return
     */
    public Locale getProgramLocale(){
        return programLocale;
    }
    private Locale setLocale() {
        Locale systemLocale= Locale.getDefault();
        boolean hasLocale = getAvailableLangs().stream().filter(l -> l.getLanguage().equals(systemLocale.getLanguage())).count() >0;
        if(!hasLocale)  {
            Locale defaultLocale = new Locale("en");
            Locale.setDefault(defaultLocale);
            return defaultLocale;
        }
        else return systemLocale;

    }

    private List<Locale> getAvailableLangs() {
        try {
            return DataHelper.selectAvailableLangs().stream().map(l->new Locale(l)).collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.EMPTY_LIST;
        }
    }


    private String generateJVMOptions(){

        int mb = 1024*1024;
        int gb = 1024*1024*1024;
        /* PHYSICAL MEMORY USAGE */
        System.out.println("\n**** Sizes in Mega Bytes ****\n");
        com.sun.management.OperatingSystemMXBean operatingSystemMXBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        //RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        //operatingSystemMXBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        com.sun.management.OperatingSystemMXBean os = (com.sun.management.OperatingSystemMXBean)
                java.lang.management.ManagementFactory.getOperatingSystemMXBean();
        long physicalMemorySize = os.getTotalPhysicalMemorySize();
        System.out.println("PHYSICAL MEMORY DETAILS \n");
        System.out.println("total physical memory : " + physicalMemorySize / mb + "MB ");
        long physicalfreeMemorySize = os.getFreePhysicalMemorySize();
        System.out.println("total free physical memory : " + physicalfreeMemorySize / mb + "MB");
        /* DISC SPACE DETAILS */

        return "-Xms350m -Xmx1024m";

    }

    protected void startMainApp(Version version){

        closePersisenceContext();

        if(AutoUpdater.isIDEStarted()) {
            Platform.exit();
            return;

        }
        Optional<ProcessBuilder> processBuilder = prepareAppProcess();
        if(!processBuilder.isPresent()){
            Log.logger.error("Не удалось подготовить процесс к запуску");
            throw new RuntimeException("Не удалось подготовить процесс к запуску");
        }
        Map<String, Pipe> pipeMap = new HashMap<>();


        boolean flag = false;
        try{
                EventBusServer busServer  = new EventBusServer.Builder()
                    .id("starter")     // OPTIONAL: bus id
                    .bind("localhost") // OPTIONAL: bind address
                    .listen(56789)   // OPTIONAL: port to listen to. Default to 56789
                    .build();

                busServer.on("to_starter",e -> {
                    String data = (String)e.getData();
                    if(data==null) return;
                    switch (data){
                        case "run_completed":
                            System.out.println("Application completely running");
                            busServer.trigger("to_main_app", "exit");
                            busServer.unbind("to_starter");
                            if(pipeMap.containsKey("in"))pipeMap.get("in").close();
                            if(pipeMap.containsKey("err"))pipeMap.get("err").close();
                            System.exit(0);
                            break;
                    }
                });
                flag  = true;
            }catch (Exception e){
                Log.logger.error("", e);
            System.out.println("EVENT BUS DO NOT WORK");
            }

        try {
            Process process = processBuilder.get().start();
            pipeMap.put("in", new Pipe("from_main_app_in", new BufferedInputStream(process.getInputStream()), System.out));
            pipeMap.put("err",  new Pipe("from_main_app_err", new BufferedInputStream(process.getErrorStream()), System.err));
            if(!flag) System.exit(0);//остановить, тк eventbus не работает. Мы просто запускаем и останавливаем
            else  {
                if(version.lessThen(new Version(4,14,9))) System.exit(0);//тк версии до указанной не поддерживают обмен событиями по шине, тк для юзеров они отключены
                Waiter.openLayer(getMainWindow(), true);
            }
        } catch (IOException ioException) {
            throw new RuntimeException("Не удалось запустить приложение");
        }


    }

    private Optional<ProcessBuilder> prepareAppProcess(){
        try {
            File currentJar = new File(App.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            File jarDir;
            if(!currentJar.getName().endsWith(".jar")) {
                jarDir = new File("./");;
                // throw new Exception("Не найден путь к jar");
            }else jarDir = currentJar.getParentFile();


            final List<String> command = new ArrayList<>();
            String exec="";
            if(OSValidator.isUnix()){
                exec = new File(jarDir,"../runtime/bin/java").getAbsolutePath();

            }else if(OSValidator.isWindows()){
                exec = new File(jarDir,"./jre/bin/java.exe").getAbsolutePath();

            }else if(OSValidator.isMac()){
                exec = new File(jarDir,"../Plugins/Java.runtime/Contents/Home/bin/java").getAbsolutePath();

            }else return Optional.empty();


            command.add(exec);
            //command.add(generateJVMOptions());
            command.add("-Xms400m");
            command.add("-Xmx1024m");
            command.add("-Dstarter.version="+getStarterVersion().toString());
            command.add("-jar");
            command.add(new File(currentJar.getParentFile(),"dist.jar").getAbsolutePath());

            System.out.println("################################");
            System.out.println("START APPLICATION: "+String.join(" ", command));
            System.out.println("################################");

            return Optional.of(new ProcessBuilder(command));
        }catch (Exception e){
            Log.logger.error("", e);
            return Optional.empty();
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
