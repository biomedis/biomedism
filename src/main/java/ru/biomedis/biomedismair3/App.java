package ru.biomedis.biomedismair3;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.biomedis.biomedismair3.DBImport.AddonsDBImport;
import ru.biomedis.biomedismair3.DBImport.NewDBImport;
import ru.biomedis.biomedismair3.DBImport.OldDBImport;
import ru.biomedis.biomedismair3.Tests.TestsFramework.TestsManager;
import ru.biomedis.biomedismair3.entity.Profile;
import ru.biomedis.biomedismair3.entity.ProgramOptions;
import ru.biomedis.biomedismair3.utils.USB.USBHelper;
import ru.biomedis.biomedismair3.utils.UTF8Control;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.io.*;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.*;

import static ru.biomedis.biomedismair3.BaseController.getApp;
import static ru.biomedis.biomedismair3.BaseController.showExceptionDialog;
import static ru.biomedis.biomedismair3.Log.logger;


public class App extends Application {
    
      public   EntityManagerFactory emf=null; 
      //public  ResourceBundle config=null;
      public  ResourceBundle strings=null;
      private Stage mainWindow=null;
      private ModelDataApp model;

      private  static  File dataDir;
      private  static  File innerDataDir;
      private  static  File tmpDir;
      private static Profile biofonProfile;
      public static final  String BIOFON_PROFILE_NAME="B_I_O_F_O_N";

    /**
     * Профиль биофона
     * @return
     */
    public Profile getBiofonProfile() {
        return biofonProfile;
    }
    public  static Profile getBiofonProfile_() {
        return biofonProfile;
    }

    public File getTmpDir() {
        return tmpDir;
    }

    public  File getInnerDataDir() {
        return innerDataDir;
    }

    public File getDataDir(){return dataDir;}



    public static File getTmpDir_() {
        return tmpDir;
    }

    public static File getInnerDataDir_() {
        return innerDataDir;
    }

    public static File getDataDir_(){return dataDir;}


    public ResourceBundle getResources(){return strings;}
      private final boolean test=false;//указывает что будут проводиться интеграционные тесты. Соответсвенно будет подключена другая БД и запущенны тесты
      private final boolean importDB=false;//импорт базы данных
        private final boolean updateBaseMenuVisible =false;//показ пункта обновления базы частот

    public boolean isUpdateBaseMenuVisible() {
        return updateBaseMenuVisible;
    }

    private List<CloseAppListener> closeAppListeners=new ArrayList<>();


    public void addCloseApplistener(CloseAppListener action)
    {
        closeAppListeners.add(action);
    }
    public void removeCloseAppListener(CloseAppListener action)
    {
        closeAppListeners.remove(action);
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

    private static ModelDataApp modelStatic;
    public static ModelDataApp getStaticModel() {
        return modelStatic;
    }
    public ModelDataApp getModel() {
        return model;
    }
      
        private int updateVersion=0;//версия установленного обновления

    public int getUpdateVersion() {
        return updateVersion;
    }

    private void setUpdateVersion(ProgramOptions programOptions,int updateVersion) {
        this.updateVersion = updateVersion;

        programOptions.setValue(updateVersion+"");
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            em.getTransaction().begin();
            em.merge(programOptions);
            em.getTransaction().commit();
        } catch (Exception ex) {
           logger.error("Ошибка обновления опции updateVersion",ex);
        } finally {
            if (em != null) {
                em.close();
            }
        }

    }

    public void closePersisenceContext() {/*if(model!=null) model.flush();*/  emf.close();  emf=null;}
       public void openPersisenceContext()
       { 
           String puName="DB_UNIT";
           if(test)puName="DB_TEST_UNIT";//определение базы для тестов
           
           if(emf==null)emf=Persistence.createEntityManagerFactory(puName);
       }
    
       public Stage getMainWindow(){return mainWindow;}

    private boolean copyWindowsDataContentToRoaming() throws IOException, InterruptedException {

        //скопируем, то что нужно в эту папку, если этого там нет

        File codec=new File("./codec/");
        File dstDir=new File(dataDir.getAbsolutePath(),"codec");

        if(!dstDir.exists()) dstDir.mkdir();
        try {
            for (File file : codec.listFiles()) {
                    if(file.isDirectory())continue;
                    copyFile(file,new File(dstDir,file.getName()));

            }
        } catch (Exception e) {
            logger.error("Ошибка копирования кодека",e);
            return false;
        }
        return true;
    }



    private boolean neadCleanDataFilesAndState=false;
    @Override
    public void start(Stage stage) throws Exception {


        Log.logger.info("Старт программы");





         mainWindow=stage;


            openPersisenceContext();//откроем контекст работы с БД



        String version= System.getProperty("java.version");
            String[] split = version.split("\\.");
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





        /******** Обновления ************/
        ProgramOptions updateOption = selectUpdateVersion();//получим версию обновления

        int currentUpdateFile=2;//версия ставиться вручную. Если готовили инсталлер, он будет содержать правильную версию  getUpdateVersion(), а если человек скопировал себе jar обновления, то версии будут разные!
        int currentMinorVersion=2;//версия исправлений в пределах мажорной версии currentUpdateFile

        if(getUpdateVersion() < currentUpdateFile)
        {
            //обновим согласно полученной версии, учесть, что нужно на младшие накатывать все апдейты по порядку
            if(getUpdateVersion()==0) {
                update1(updateOption);
                update2(updateOption);
            }
            else if(getUpdateVersion()==1) update2(updateOption);

        }else if(getUpdateVersion() > currentUpdateFile){
            logger.error("Запуск апдейта "+currentUpdateFile+" ниже установленного "+getUpdateVersion()+"!");

            BaseController.showInfoConfirmDialog(this.strings.getString("app.error"), "", this.strings.getString("app.update.incorrect_update_message"), null, Modality.APPLICATION_MODAL);
            Platform.exit();
            return;
        }
        /******************/



        model=new ModelDataApp(emf);
        modelStatic =model;

        String data_path = getModel().getOption("data_path");
        if(!data_path.isEmpty()){
            File nData = new File(data_path);
            if(!nData.exists()) nData.mkdirs();
            setDataDir(nData);
        }

        tmpDir=new File(dataDir,"tmp");
        if(!tmpDir.exists()){
            tmpDir.mkdir();
            tmpDir=new File(dataDir,"tmp");
        }

        recursiveDeleteTMP();


        //проверка профиля биофона
        checkBiofonProfile();

        //настроим язык программы



        boolean firstStart=false;


        File firstStartFileIndicator=new File(getInnerDataDir(),"fs.ind");
        if(!firstStartFileIndicator.exists())firstStart=true;
        else firstStart=false;


        if(getModel().countLanguages()>0)
        {
            try {

                String langOpt = getModel().getOption("app.lang");
                if (langOpt.isEmpty() || firstStart==true) {
                    //если настройки нет то установим по по возможностиумолчанию
                    if (getModel().getLanguage(getModel().getSystemLocale().getLanguage()).isAvaliable()) {
                        getModel().setProgramLanguage(new Locale(getModel().getSystemLocale().getLanguage()));
                    } else
                        getModel().setProgramLanguageDefault(); // если системный язык не доступен в локализации то поставим язык по умолчанию

                    //если  запуск первый раз, то установим опцию равную языку
                    setInsertCompexLang(getModel().getProgramLanguage().getAbbr());
                    getModel().setOption("app.lang",getModel().getProgramLanguage().getAbbr());



                } else {
                    //если есть настройка проверим и установим язык программы

                    if (getModel().getLanguage(langOpt).isAvaliable()) getModel().setProgramLanguage(new Locale(langOpt));//по аббривиатуре языка. Этот параметр выставляется в диалоге настроек языка из доступных языков.
                    else getModel().setProgramLanguageDefault(); //если что поставим язык по умолчанию

                }


                //при каждом запуске пересоздаем файл, чтобы знать версию и последнеевремя запуска в мс.
                createFirstStartFile(firstStartFileIndicator);

            } catch (IOException e){
                BaseController.showInfoConfirmDialog(this.strings.getString("app.error"),
                        "", this.strings.getString("app.error_write_ind_file"), null, Modality.APPLICATION_MODAL);
                Platform.exit();
            }
            catch (Exception e) {
                //если что поставим язык по умолчанию
                 Log.logger.error("",e);
                getModel().setProgramLanguageDefault();
            }

            //System.out.println("Язык -" + getModel().getProgramLanguage().getName());
        }else
        {
            getModel().setProgramLanguageDefault();
            //System.out.println("Язык - по умолчанию");
        }




        //загрузим перевод интерфейса на выбранный язык!
        this.strings= ResourceBundle.getBundle("bundles.strings", new UTF8Control());

        //this.config = ResourceBundle.getBundle("bundles.config"); //загрузим конфиг
        //this.strings = ResourceBundle.getBundle("bundles.strings");




        /*
        Для чтения файлов properties используются методы загрузки ресурсов, которые работают специфичным образом. Собственно для чтения используется метод Properties.load, который не использует file.encoding (там в исходниках жёстко указана кодировка ISO-8859-1)
http://gubber.ru/Razrabotka/ResourceBundle-and-UTF-8.html
https://gist.github.com/DemkaAge/8999236

Если файлы properties у Вас загружаются не как ресурсы, а как обычные файлы конфигурации, и Вас не устраивает такое поведение - выход один, написать собственный загрузчик.
         */
/*

        Iterator<Program> iterator = getModel().findAllProgram().iterator();
        Program next;
        while (iterator.hasNext())
        {
             next = iterator.next();
             next.setUuid(UUID.randomUUID().toString());
             getModel().updateProgram(next);
            System.out.println("Обновление программы id="+next.getId() +" UUID="+next.getUuid());
        }

        Iterator<Complex> iterator2 = getModel().findAllComplex().iterator();
        Complex next2;
        while (iterator2.hasNext())
        {
            next2 = iterator2.next();
            next2.setUuid(UUID.randomUUID().toString());
            getModel().updateComplex(next2);
            System.out.println("Обновление комплекса id="+next2.getId() +" UUID="+next2.getUuid());
        }


        Iterator<Section> iterator3 = getModel().findAllSection().iterator();
        Section next3;
        while (iterator3.hasNext())
        {
            next3 = iterator3.next();
            next3.setUuid(UUID.randomUUID().toString());
            getModel().updateSection(next3);
            System.out.println("Обновление секции id="+next3.getId() +" UUID="+next3.getUuid());
        }
*/




        try {
            USBHelper.initContext();
        } catch (USBHelper.USBException e) {
            throw new RuntimeException(e);
        }


        BaseController.setApp(this);//установим ссылку на приложение для всех контроллеров
         stage.setTitle(this.strings.getString("app.name")+getUpdateVersion()+"."+currentMinorVersion);
        // stage.getIcons().add(new Image(App.class.getResourceAsStream("icon.png")));
         URL ico = getClass().getResource("/images/icon.png");
         stage.getIcons().add(new Image(ico.toExternalForm()));
         
            /******* Загрузка главного окна ****/
         
            URL location = getClass().getResource("/fxml/Scene.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(location, this.strings);
            Parent root = fxmlLoader.load();
            AppController  controller = (AppController )fxmlLoader.getController();            
            BaseController.setMainController(controller);
            
           /*********************************/
       
        Scene scene = new Scene(root);
        //scene.getStylesheets().add("/styles/Theme.css");
        scene.getStylesheets().add("/styles/Styles.css");

        stage.setScene(scene);        
        stage.centerOnScreen();

        stage.setMinHeight(550);
        stage.setMinWidth(800);
       // sceneResizeListner(scene);



        //перед закрытием произойдет уведомление всех подписчиков и закрытие глобальных контекстов
        stage.setOnCloseRequest(event -> {
            closeAppListeners.stream().forEach(listeners->listeners.onClose());

            USBHelper.stopHotPlugListener();
            USBHelper.closeContext();
        });


        stage.show();
         
        if(test) 
        {
            TestsManager mdat=new TestsManager(this,"ru.biomedis.biomedismair3.Tests");
            mdat.runAllTests();
        }
        if(importDB)
        {


            System.out.println("\n\n");
            NewDBImport dbImport1=new NewDBImport(getModel());
            if(dbImport1.execute()==false){System.out.println("Ошибка импорта новой базы"); return;}

            OldDBImport dbImport=new OldDBImport(getModel());
            if(dbImport.importDB()==false){System.out.println("Ошибка импорта старой базы"); return;}

            AddonsDBImport addon=new AddonsDBImport(getModel());
            if(addon.execute()==false){System.out.println("Ошибка импорта аддонов"); return;}
        }




        USBHelper.startHotPlugListener(2);
    }


    private void checkBiofonProfile() throws Exception {

        List<Profile> profiles = getModel().searchProfile(BIOFON_PROFILE_NAME);
        if(profiles.isEmpty()){

            biofonProfile =  getModel().createProfile(BIOFON_PROFILE_NAME);

        }else {

             biofonProfile = profiles.get(0);
        }
    }

    /**
     * Очистка внутренней директории данных от файлов data
     * Установка для всех программ статуса - требует генерации
     */
    private void cleanDataFilesAndState() {
        if(neadCleanDataFilesAndState==false)return;

        neadCleanDataFilesAndState=false;

        //удаление dat файлов
        for (File f : getInnerDataDir().listFiles())
        {
            if(f.getName().contains(".dat")) f.delete();

        }
        //необходимость генерации программ
        getModel().setNeedGenerateAllTherapyProgramm();

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

    private void setInsertCompexLang(String abbr){
        try {
            getModel().setOption("app.lang_insert_complex",abbr);
        } catch (Exception e) {
            logger.error("",e);
            showExceptionDialog("Ошибка применения параметра языка", "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);
            return;
        }

    }



    /**
     * Получает значение версии обновления. Если ее вообще нет создасто нулевую
     * Установит значение в  this.updateVersion
     * @return вернет созданную или полученную опцию
     */
    private ProgramOptions selectUpdateVersion()
    {
        ProgramOptions updateVersion=null;
        EntityManager em = emf.createEntityManager();
        Query query=em.createQuery("Select o From ProgramOptions o Where o.name = :name").setMaxResults(1);
        query.setParameter("name","updateVersion");
        try{
            updateVersion  =(ProgramOptions )query.getSingleResult();
            this.updateVersion=Integer.parseInt(updateVersion.getValue());
        }catch (javax.persistence.NoResultException e)
        {
            updateVersion=new ProgramOptions();
            updateVersion.setName("updateVersion");
            updateVersion.setValue("0");
            em = null;
            try {
                em = emf.createEntityManager();
                em.getTransaction().begin();
                em.persist(updateVersion);
                em.getTransaction().commit();

            } finally {
                if (em != null) {
                    em.close();
                }
            }

            this.updateVersion=0;

        }




        return updateVersion;

    }



    /**
     * Обновление1 - добавлены пачки частот в мультичестотный режим. Исправлены ошибки.
     * Добавлена возможность импорта переводов базы  и экспорта всей бызы для перевода
     */
    private void update1( ProgramOptions updateOption) {

        logger.info("ОБНОВЛЕНИЕ 1.");
        try
        {
            logger.info("Проверка наличия столбца BUNDLESLENGTH  в THERAPYCOMPLEX ");
            Object singleResult = emf.createEntityManager().createNativeQuery("SELECT BUNDLESLENGTH FROM THERAPYCOMPLEX LIMIT 1").getSingleResult();
            logger.info("Столбец  BUNDLESLENGTH  найден.");
        }catch (Exception e){
            logger.info("Столбец  BUNDLESLENGTH не найден.");
            logger.info("Создается  столбец BUNDLESLENGTH  в THERAPYCOMPLEX ");
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            try{
                em.createNativeQuery("ALTER TABLE THERAPYCOMPLEX ADD BUNDLESLENGTH INT DEFAULT 1").executeUpdate();
                em.getTransaction().commit();
                logger.info("Столбец  BUNDLESLENGTH создан.");
            }catch (Exception ex){
                throw new RuntimeException("Не удалось выполнить ALTER TABLE THERAPYCOMPLEX ADD BUNDLESLENGTH INT ");
            }finally {
                if(em!=null) em.close();
            }


        }


        try
        {
            //столбец связан с языком вставки комплексов
            logger.info("Проверка наличия столбца ONAME  в THERAPYCOMPLEX ");
            Object singleResult = emf.createEntityManager().createNativeQuery("SELECT ONAME FROM THERAPYCOMPLEX LIMIT 1").getSingleResult();
            logger.info("Столбец  ONAME  найден.");
        }catch (Exception e){
            logger.info("Столбец  ONAME не найден.");
            logger.info("Создается  столбец ONAME  в THERAPYCOMPLEX ");
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            try{
                em.createNativeQuery("ALTER TABLE THERAPYCOMPLEX ADD ONAME VARCHAR(255) DEFAULT ''").executeUpdate();
                em.getTransaction().commit();
                logger.info("Столбец  ONAME создан.");
            }catch (Exception ex){
                throw new RuntimeException("Не удалось выполнить ALTER TABLE THERAPYCOMPLEX ADD ONAME VARCHAR(255) DEFAULT ''");
            }finally {
                if(em!=null) em.close();
            }


        }

        try
        {
            //столбец связан с языком вставки комплексов
            logger.info("Проверка наличия столбца ONAME  в THERAPYPROGRAM ");
            Object singleResult = emf.createEntityManager().createNativeQuery("SELECT ONAME FROM THERAPYPROGRAM LIMIT 1").getSingleResult();
            logger.info("Столбец  ONAME  найден.");
        }catch (Exception e){
            logger.info("Столбец  ONAME не найден.");
            logger.info("Создается  столбец ONAME  в THERAPYPROGRAM ");
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            try{
                em.createNativeQuery("ALTER TABLE THERAPYPROGRAM ADD ONAME VARCHAR(255) DEFAULT ''").executeUpdate();
                em.getTransaction().commit();
                logger.info("Столбец  ONAME создан.");
            }catch (Exception ex){
                throw new RuntimeException("Не удалось выполнить ALTER TABLE THERAPYPROGRAM ADD ONAME VARCHAR(255) DEFAULT ''");
            }finally {
                if(em!=null) em.close();
            }


        }

        setUpdateVersion(updateOption,1);//установим новую версию обновления

        logger.info("ОБНОВЛЕНИЕ 1 ЗАВЕРШЕНО.");
    }


    private void update2(ProgramOptions updateOption) {
        logger.info("ОБНОВЛЕНИЕ 2.");

        setUpdateVersion(updateOption,2);//установим новую версию обновления

        logger.info("ОБНОВЛЕНИЕ 2 ЗАВЕРШЕНО.");
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


    /**
     * Очистка папки временной
     * @return
     */
    public  boolean recursiveDeleteTMP() {

        return recursiveDeleteHelper(this.getTmpDir());

    }

    private  boolean recursiveDeleteHelper(File path)
    {

        // до конца рекурсивного цикла
        if (!path.exists())
            return false;

        //если это папка, то идем внутрь этой папки и вызываем рекурсивное удаление всего, что там есть
        if (path.isDirectory()) {
            for (File f : path.listFiles()) {
                // рекурсивный вызов
                recursiveDeleteHelper(f);
            }
        }
        // вызываем метод delete() для удаления файлов и пустых(!) папок
        if(path ==tmpDir)  return true;
        else  return path.delete();

    }





    public static void copyFile(File srcFile,File destFile) throws Exception {

        destFile=new File(destFile.toURI());

        try(FileChannel source = new FileInputStream(srcFile).getChannel(); FileChannel destination = new FileOutputStream(destFile).getChannel())
        {

            destination.transferFrom(source, 0, source.size());


        } catch (Exception e) {

            if(destFile.exists()) destFile.delete();
            throw new Exception(e);
        }

    }
}
