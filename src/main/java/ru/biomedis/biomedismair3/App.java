package ru.biomedis.biomedismair3;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.anantacreative.updater.Version;
import ru.biomedis.biomedismair3.DBImport.AddonsDBImport;
import ru.biomedis.biomedismair3.DBImport.NewDBImport;
import ru.biomedis.biomedismair3.DBImport.OldDBImport;
import ru.biomedis.biomedismair3.Tests.TestsFramework.TestsManager;
import ru.biomedis.biomedismair3.UpdateUtils.FrequenciesBase.LoadLanguageFiles;
import ru.biomedis.biomedismair3.UserUtils.Import.ImportUserBase;
import ru.biomedis.biomedismair3.entity.*;
import ru.biomedis.biomedismair3.utils.Files.ResourceUtil;
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
      private Version version;
      private static  AppController  controller;
      private int updateFixVersion;


    /**
     * текущая реальная версия после применения обновлений
     * @return
     */
    public Version getVersion() {
        return version;
    }

    /**
     *
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

    public static AppController getAppController(){return controller;}

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
      private final boolean updateBaseMenuVisible =true;//показ пункта обновления базы частот

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


    private void setUpdateFixVersion(ProgramOptions programOptions,int updateFixVersion) {
        this.updateFixVersion = updateFixVersion;

        programOptions.setValue(updateFixVersion+"");
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            em.getTransaction().begin();
            em.merge(programOptions);
            em.getTransaction().commit();
        } catch (Exception ex) {
            logger.error("Ошибка обновления опции updateFixVersion",ex);
        } finally {
            if (em != null) {
                em.close();
            }
        }

    }

    public void closePersisenceContext() {
        if(emf!=null)emf.close();
        emf=null;
    }

    public void reopenPersistentContext(){
        System.out.println("Start reopen persistent context");
       closePersisenceContext();
       openPersisenceContext();
       model=new ModelDataApp(emf);
       modelStatic =model;
        System.out.println("Context reopened");
    }
       public void openPersisenceContext()
       { 
           String puName="DB_UNIT";
           if(test)puName="DB_TEST_UNIT";//определение базы для тестов

           emf=Persistence.createEntityManagerFactory(puName);
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

        //временная установка, нужно для обновлений
        tmpDir=new File(dataDir,"tmp");
        if(!tmpDir.exists()){
            tmpDir.mkdir();
            tmpDir=new File(dataDir,"tmp");
        }

System.out.println("Data path: "+dataDir.getAbsolutePath());

        /******** Обновления ************/
        ProgramOptions updateOption = selectUpdateVersion();//получим версию обновления
        System.out.println("Current Version: "+getUpdateVersion());
        int currentUpdateFile=9;//версия ставиться вручную. Если готовили инсталлер, он будет содержать правильную версию  getUpdateVersion(), а если человек скопировал себе jar обновления, то версии будут разные!
        int currentMinorVersion=0;//версия исправлений в пределах мажорной версии currentUpdateFile

        if(getUpdateVersion() < currentUpdateFile)
        {
            //обновим согласно полученной версии, учесть, что нужно на младшие накатывать все апдейты по порядку
            if(getUpdateVersion()==0) {
                update1(updateOption);
                update2(updateOption);


            }
            else if(getUpdateVersion()==1) {
                update2(updateOption);

            }


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

//доп. обновления требующие ModelDataApp
        if(getUpdateVersion() < currentUpdateFile)
        {
            //обновим согласно полученной версии, учесть, что нужно на младшие накатывать все апдейты по порядку
            if(getUpdateVersion()==2) {
                update3(updateOption);
                update4(updateOption);
                update5(updateOption);
                update6(updateOption);
                update7(updateOption);
                update8(updateOption);
                update9(updateOption);
            }else if(getUpdateVersion()==3){
                update4(updateOption);
                update5(updateOption);
                update6(updateOption);
                update7(updateOption);
                update8(updateOption);
                update9(updateOption);
            }else if(getUpdateVersion()==4){
                update5(updateOption);
                update6(updateOption);
                update7(updateOption);
                update8(updateOption);
                update9(updateOption);
            }else if(getUpdateVersion()==5){
                update6(updateOption);
                update7(updateOption);
                update8(updateOption);
                update9(updateOption);
            }else if(getUpdateVersion()==6){

                update7(updateOption);
                update8(updateOption);
                update9(updateOption);
            }else if(getUpdateVersion()==7){

                update8(updateOption);
                update9(updateOption);
            }else if(getUpdateVersion()==8){

                update9(updateOption);
            }

        }else if(getUpdateVersion() > currentUpdateFile){
            logger.error("Запуск апдейта "+currentUpdateFile+" ниже установленного "+getUpdateVersion()+"!");

            BaseController.showInfoConfirmDialog(this.strings.getString("app.error"), "", this.strings.getString("app.update.incorrect_update_message"), null, Modality.APPLICATION_MODAL);
            Platform.exit();
            return;
        }


        //проверка профиля биофона
        checkBiofonProfile();

            //установка нижней версии, если все этапы обновления прошли нормально
            setUpdateFixVersion(selectUpdateFixVersion(),currentMinorVersion);
            //настроим язык программы
            this.version = new Version(4,getUpdateVersion(),currentMinorVersion);


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
            controller = (AppController )fxmlLoader.getController();
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
     * Получает значение версии обновления. Если ее вообще нет создасто нулевую
     * Установит значение в  this.updateFixVersion
     * @return вернет созданную или полученную опцию
     */
    private ProgramOptions selectUpdateFixVersion()
    {
        ProgramOptions updateVersion=null;
        EntityManager em = emf.createEntityManager();
        Query query=em.createQuery("Select o From ProgramOptions o Where o.name = :name").setMaxResults(1);
        query.setParameter("name","updateFixVersion");
        try{
            updateVersion  =(ProgramOptions )query.getSingleResult();
            this.updateFixVersion=Integer.parseInt(updateVersion.getValue());
        }catch (javax.persistence.NoResultException e)
        {
            updateVersion=new ProgramOptions();
            updateVersion.setName("updateFixVersion");
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

            this.updateFixVersion=0;

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

    private void update3(ProgramOptions updateOption) {
        logger.info("ОБНОВЛЕНИЕ 3.");


        //основное изменение в базе - обновление для фр. языка.
        //сделается из файла


        Task<Boolean> task =new Task<Boolean>()  {
            @Override
            protected Boolean call() throws Exception {
                File base_translate=null;
                try {
                    ResourceUtil ru=new ResourceUtil();
                    base_translate = ru.saveResource(getTmpDir(),"fr_translanion_bas.xml","/updates/update3/fr_translanion_bas.xml",true);

                    if(base_translate==null) throw new Exception();

                    LoadLanguageFiles ll=new LoadLanguageFiles();
                    if( ll.parse(Arrays.asList(base_translate),getModel())){


                        setUpdateVersion(updateOption,3);//установим новую версию обновления
                        logger.info("ОБНОВЛЕНИЕ 3  ЗАВЕРШЕНО.");
                        return true;
                    }
                    else  return false;

                } catch (IOException e) {
                    e.printStackTrace();
                    logger.info("ОБНОВЛЕНИЕ 3 НЕ ЗАВЕРШЕНО.");
                    return false;
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.info("ОБНОВЛЕНИЕ 3 НЕ ЗАВЕРШЕНО.");
                    return false;
                }
            }
        };

        task.setOnSucceeded(event -> {
            if(!task.getValue().booleanValue()) {
                BaseController.showErrorDialog("Обновление","","Обновление не установленно",null,Modality.WINDOW_MODAL);
                Platform.exit();
            }

            UpdateWaiter.close();
        });
        task.setOnFailed(event -> {
            Platform.runLater(() -> BaseController.showErrorDialog("Обновление","","Обновление не установленно",null,Modality.WINDOW_MODAL) );
            Platform.exit();
            UpdateWaiter.close();
        });

        Thread threadTask=new Thread(task);
        threadTask.setDaemon(true);
        threadTask.start();
        UpdateWaiter.show();



    }

    private void update4(ProgramOptions updateOption) {
        logger.info("ОБНОВЛЕНИЕ 4.");


        //основное изменение в базе - обновление для итальянского. языка.
        //сделается из файла


        Task<Boolean> task =new Task<Boolean>()  {
            @Override
            protected Boolean call() throws Exception {
                File base_translate=null;
                try {
                    ResourceUtil ru=new ResourceUtil();
                    base_translate = ru.saveResource(getTmpDir(),"it_translanion_bas.xml","/updates/update4/it_base.xml",true);

                    if(base_translate==null) throw new Exception();

                    LoadLanguageFiles ll=new LoadLanguageFiles();
                    if( ll.parse(Arrays.asList(base_translate),getModel())){


                        setUpdateVersion(updateOption,4);//установим новую версию обновления
                        logger.info("ОБНОВЛЕНИЕ 4  ЗАВЕРШЕНО.");
                        return true;
                    }
                    else  return false;

                } catch (IOException e) {
                    e.printStackTrace();
                    logger.info("ОБНОВЛЕНИЕ 4 НЕ ЗАВЕРШЕНО.");
                    return false;
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.info("ОБНОВЛЕНИЕ 4 НЕ ЗАВЕРШЕНО.");
                    return false;
                }
            }
        };

        task.setOnSucceeded(event -> {
            if(!task.getValue().booleanValue())  {
                BaseController.showErrorDialog("Обновление 4","","Обновление не установленно",null,Modality.WINDOW_MODAL);
                Platform.exit();
            }

            UpdateWaiter.close();
        });
        task.setOnFailed(event -> {
            Platform.runLater(() -> BaseController.showErrorDialog("Обновление 4","","Обновление не установленно",null,Modality.WINDOW_MODAL) );
            Platform.exit();
            UpdateWaiter.close();
        });

        Thread threadTask=new Thread(task);
        threadTask.setDaemon(true);
        threadTask.start();
        UpdateWaiter.show();



    }


    private void update5(ProgramOptions updateOption)
    {
        logger.info("ОБНОВЛЕНИЕ 5.");
        Task<Boolean> task =new Task<Boolean>()  {
            @Override
            protected Boolean call() throws Exception {
        try
        {
            logger.info("Проверка наличия столбца MULTYFREQ  в THERAPYPROGRAM ");
            Object singleResult = emf.createEntityManager().createNativeQuery("SELECT MULTYFREQ FROM THERAPYPROGRAM LIMIT 1").getSingleResult();
            logger.info("Столбец  MULTYFREQ  найден.");
        }catch (Exception e){
            logger.info("Столбец  MULTYFREQ не найден.");
            logger.info("Создается  столбец MULTYFREQ  в THERAPYPROGRAM ");
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            try{
                em.createNativeQuery("ALTER TABLE THERAPYPROGRAM ADD MULTYFREQ BOOLEAN(1) DEFAULT 1").executeUpdate();
                em.getTransaction().commit();
                logger.info("Столбец  MULTYFREQ создан.");
                reopenPersistentContext();
                TherapyComplex therapyComplex;
                boolean multyCompl=false;
                /*
                for (TherapyProgram tp : getModel().findAllTherapyPrograms()) {

                     therapyComplex = tp.getTherapyComplex();

                     if(therapyComplex==null)  tp.setMultyFreq(true);
                     else   if( therapyComplex.isMulltyFreq()!=tp.isMultyFreq()){
                         tp.setMultyFreq(therapyComplex.isMulltyFreq());
                     }
                    getModel().updateTherapyProgram(tp);

                }
                */
                logger.info("Столбец  MULTYFREQ обновлен.");

                //добавление базы чстот Тринити

               // logger.info("Добавление раздела Trinity");
               // addTrinityBase();
                //logger.info("Добавление раздела Trinity --- успешно");



            }catch (Exception ex){
                logger.error("ошибка обновления ALTER TABLE THERAPYPROGRAM ADD MULTYFREQ BOOLEAN(1) DEFAULT 1",ex);
                return false;
            }finally {
                if(em!=null) em.close();
            }


        }


        try {
            logger.info("Добавление раздела Trinity");
            addTrinityBase();
            logger.info("Добавление раздела Trinity --- успешно");

        } catch (Exception ex) {
            logger.error("Не удалось выполнить добавление Trinity",ex);
            return false;
        }

                setUpdateVersion(updateOption,5);
            return true;

            }
        };


        task.setOnSucceeded(event -> {
            if(!task.getValue().booleanValue())  {
                BaseController.showErrorDialog("Обновление 5","","Обновление не установленно",null,Modality.WINDOW_MODAL);
                Platform.exit();
            }
            else  logger.info("ОБНОВЛЕНИЕ 5. Завершено");

            UpdateWaiter.close();
        });
        task.setOnFailed(event -> {
            Platform.runLater(() -> BaseController.showErrorDialog("Обновление 5 ","","Обновление не установленно",null,Modality.WINDOW_MODAL) );
            Platform.exit();
            UpdateWaiter.close();
        });

        Thread threadTask=new Thread(task);
        threadTask.setDaemon(true);
        threadTask.start();
        UpdateWaiter.show();



    }

    private void update6(ProgramOptions updateOption)
    {
        logger.info("ОБНОВЛЕНИЕ 6.");
        Task<Boolean> task =new Task<Boolean>()  {
            @Override
            protected Boolean call() throws Exception {
                boolean tpPosFinded=false;
                boolean profPosFinded=false;
                boolean mFreqFinded=false;
                try
                {
                    logger.info("Проверка наличия столбца MULLTYFREQ  в THERAPYCOMPLEX ");
                    Object singleResult = emf.createEntityManager().createNativeQuery("SELECT MULLTYFREQ FROM THERAPYCOMPLEX LIMIT 1").getSingleResult();
                    logger.info("Столбец  MULLTYFREQ  найден.");
                    mFreqFinded=true;
                    EntityManager em = emf.createEntityManager();
                    em.getTransaction().begin();
                    try{
                        em.createNativeQuery("ALTER TABLE THERAPYCOMPLEX DROP `MULLTYFREQ`").executeUpdate();
                        em.getTransaction().commit();
                        logger.info("Столбец  MULLTYFREQ удален.");


                    }catch (Exception ex){
                        logger.error("ошибка обновления ALTER TABLE THERAPYCOMPLEX DROP `MULLTYFREQ`",ex);
                        return false;
                    }finally {
                        if(em!=null) em.close();
                    }

                }catch (Exception e){
                }

                try
                {
                    logger.info("Проверка наличия столбца POSITION  в THERAPYCOMPLEX ");
                    Object singleResult = emf.createEntityManager().createNativeQuery("SELECT `POSITION` FROM THERAPYCOMPLEX LIMIT 1").getSingleResult();
                    logger.info("Столбец  POSITION  найден.");
                    tpPosFinded=true;
                }catch (Exception e){
                    logger.info("Столбец  POSITION не найден.");
                    logger.info("Создается  столбец POSITION  в THERAPYPROGRAM ");
                    EntityManager em = emf.createEntityManager();
                    em.getTransaction().begin();
                    try{
                        em.createNativeQuery("ALTER TABLE THERAPYCOMPLEX ADD `POSITION` BIGINT(19) DEFAULT 1").executeUpdate();
                        em.getTransaction().commit();
                        logger.info("Столбец  POSITION создан.");



                    }catch (Exception ex){
                        logger.error("ошибка обновления ALTER TABLE THERAPYCOMPLEX ADD `POSITION` BIGINT(19) DEFAULT 1",ex);
                        return false;
                    }finally {
                        if(em!=null) em.close();
                    }


                }
                reopenPersistentContext();
                try
                {
                    logger.info("Проверка наличия столбца POSITION  в PROFILE ");
                    Object singleResult = emf.createEntityManager().createNativeQuery("SELECT `POSITION` FROM PROFILE LIMIT 1").getSingleResult();
                    logger.info("Столбец  POSITION  найден.");
                    profPosFinded=true;
                }catch (Exception e){
                    logger.info("Столбец  POSITION не найден.");
                    logger.info("Создается  столбец POSITION  в PROFILE ");
                    EntityManager em = emf.createEntityManager();
                    em.getTransaction().begin();
                    try{
                        em.createNativeQuery("ALTER TABLE PROFILE ADD `POSITION` BIGINT(19) DEFAULT 1").executeUpdate();
                        em.getTransaction().commit();
                        logger.info("Столбец  POSITION создан.");







                    }catch (Exception ex){
                        logger.error("ошибка обновления ALTER TABLE PROFILE ADD `POSITION` BIGINT(19) DEFAULT 1",ex);
                        return false;
                    }finally {
                        if(em!=null) em.close();
                    }


                }

                reopenPersistentContext();

                try {
                    long pos;
                    if(!tpPosFinded) {
                        for (TherapyComplex tc : getModel().findAllTherapyComplexes()) {

                            pos = tc.getId();
                            tc.setPosition(pos);
                            getModel().updateTherapyComplex(tc);

                        }
                        logger.info("Столбец  POSITION TherapyComplex обновлен.");
                    }
                    if(!profPosFinded) {
                        for (Profile profile : getModel().findAllProfiles()) {

                            pos = profile.getId();
                            profile.setPosition(pos);
                            getModel().updateProfile(profile);

                        }
                        logger.info("Столбец  POSITION Profile обновлен.");
                    }

                }catch (Exception e){
                    logger.error("ошибка обновления POSITION",e);
                    return false;
                }


                EntityManager em = emf.createEntityManager();
                em.getTransaction().begin();
                try
                {
                    logger.info("Обновление BUNDLESLENGTH  ");

                    int res = em.createNativeQuery("UPDATE  THERAPYCOMPLEX SET  BUNDLESLENGTH = 3  WHERE BUNDLESLENGTH = 1").executeUpdate();
                    em.getTransaction().commit();
                    logger.info("BUNDLESLENGTH обновлен. Обновлено "+res+" значений");

                }catch (Exception e){
                    logger.error(" Ошибка обновления BUNDLESLENGTH",e);
                    return false;
                }finally {
                    if(em!=null) em.close();
                }


             //   d8652bff-a090-451f-bcef-6380195ad2f5 альфа
             //   c5175f11-cd07-484e-b63c-6b6bd0fcb928    7.84

                Program beta=   getModel().findProgram("c5175f11-cd07-484e-b63c-6b6bd0fcb928");
                Program alfa=   getModel().findProgram("d8652bff-a090-451f-bcef-6380195ad2f5");
                     if(beta==null || alfa==null){
                         logger.error("не найдены комплексы alfa и beta");
                     }else {
                         if(beta.getFrequencies().equals("7.84")){
                             logger.info("Обмен значений альфаи бета ритмов");
                             beta.setFrequencies(alfa.getFrequencies());
                             alfa.setFrequencies("7.84");
                             getModel().updateProgram(alfa);
                             getModel().updateProgram(beta);
                         }

                     }

                try {
                    logger.info("Обновление раздела Trinity");
                    //первый вариант добавления забраковали. Далее используется обновленная версия.
                    //Эта версия обновит если уже было добавлено не верно или создаст верный вариант. При кумулятивном обновлении сработает в update5
                    addTrinityBase();
                    logger.info("Обновление раздела Trinity --- успешно");

                } catch (Exception ex) {
                    logger.error("Не удалось выполнить обновление Trinity",ex);
                    return false;
                }


                setUpdateVersion(updateOption,6);
                return true;

            }
        };


        task.setOnSucceeded(event -> {
            if(!task.getValue().booleanValue())  {
                BaseController.showErrorDialog("Обновление 6","","Обновление не установленно",null,Modality.WINDOW_MODAL);
                Platform.exit();
            }
            else  logger.info("ОБНОВЛЕНИЕ 6. Завершено");

            UpdateWaiter.close();
        });
        task.setOnFailed(event -> {
            Platform.runLater(() -> BaseController.showErrorDialog("Обновление 6","","Обновление не установленно",null,Modality.WINDOW_MODAL) );
            Platform.exit();
            UpdateWaiter.close();
        });

        Thread threadTask=new Thread(task);
        threadTask.setDaemon(true);
        threadTask.start();
        UpdateWaiter.show();



    }
    private void update7(ProgramOptions updateOption)
    {
        logger.info("ОБНОВЛЕНИЕ 7.");
        Task<Boolean> task =new Task<Boolean>()  {
            @Override
            protected Boolean call() throws Exception {

               Language ru= model.getLanguage("ru");

                //при обновлении 6 забыта установка поля ownerSystem!! В методе update6 исправлено, но для тех у кого оно уже стояло нужен этот код.
                    //также был установлен не верный язык(пользовательский вместо ru)
               Section section = model.findAllSectionByTag("TRINITY");
                if(section!=null) {
                    Strings nameStringTrin = section.getName();
                    LocalizedString enName = model.getLocalString(nameStringTrin, model.getDefaultLanguage());
                    if(enName==null)model.addString(nameStringTrin,"Trinity",model.getDefaultLanguage());
                    LocalizedString localString;
                        for (Complex complex : model.findAllComplexBySection(section)) {
                             localString = model.getLocalString(complex.getName(), ru);
                             if(localString==null){
                                 localString = model.getLocalString(complex.getName(), model.getUserLanguage());
                             }
                             if(localString!=null){
                                 localString.setLanguage(ru);
                                 model.updateLocalString(localString);
                             }
                            localString = model.getLocalString(complex.getDescription(), ru);
                            if(localString==null){
                                localString = model.getLocalString(complex.getDescription(), model.getUserLanguage());
                            }
                            if(localString!=null){
                                localString.setLanguage(ru);
                                model.updateLocalString(localString);
                            }

                                complex.setOwnerSystem(true);
                                model.updateComplex(complex);
                                for (Program program : model.findAllProgramByComplex(complex)) {
                                    localString = model.getLocalString(program.getName(), ru);
                                    if(localString==null){
                                        localString = model.getLocalString(program.getName(), model.getUserLanguage());
                                    }
                                    if(localString!=null){
                                        localString.setLanguage(ru);
                                        model.updateLocalString(localString);
                                    }

                                    localString = model.getLocalString(program.getDescription(), ru);
                                    if(localString==null){
                                        localString = model.getLocalString(program.getDescription(), model.getUserLanguage());
                                    }
                                    if(localString!=null){
                                        localString.setLanguage(ru);
                                        model.updateLocalString(localString);
                                    }
                                    if(!program.isOwnerSystem()){
                                        program.setOwnerSystem(true);
                                        model.updateProgram(program);
                                    }

                                }


                        }
                }
                String[] fileUUIDs=new String[]{
                        "b57cb360-bffe-4c90-a5f6-c61afa6c5212",
                "79a622f6-2de5-4693-9ac7-494bb4c37c90",
               "178ce590-1111-4214-89bb-91974fecbde7",
                "2c1310c6-4a56-49d0-8644-2cac1e743ad5",
               "0580dfb7-1da2-4592-9ded-a0fadbe8fcc1",
               "49683ae8-51d0-4806-9058-5aa8716f961d",
               "8e638001-a4fa-4dd6-ae1a-9cbfcae27e27",
              "6b3be161-0c2b-4d61-80be-9a870e6885db",
               "86908606-b908-48ec-a08f-fd3115c23f58",
                "664c5042-2117-4cb6-90a7-0c17e9b8795e",
               "6bbd0b18-bde1-4fa6-9cdc-129ff6bfb4ba",
                "7c9965a7-fbb3-4f24-ba2c-826f803665fa",
              "38415e0d-97fc-4b19-8ed2-e4ee16e5812b",
                "f9747cae-1b8c-4c21-869b-4ff4f1048bd3",
               "648aa672-63eb-448e-8556-d69cf8243d0b",
                "966beca0-a24d-4cd1-8fe2-c6384763b88a",
            "f326b090-3f23-49af-88b2-cb4cc8403a39",
              "28b917de-eb09-42f9-9823-cd904fe5beb6",
               "c810faf4-bee2-4b62-8e27-a44983c2d8e4",
               "0658e74e-a8d5-40be-917b-406e00620b74",
                "f74c8090-fa32-47a5-abad-f1dba3792308",
                "b3943a27-91b6-4d19-9c07-3bc928205992",
               "d65b9e93-ce22-4c01-911d-884446ca2523",
               "8804b3ec-d49b-44a9-a69d-1b3501538abb",
               "759b688a-cb72-49e1-9794-d424c15199d5",
               "30041ef0-1f7c-47bd-8fc3-4f30cbd9297a",
             "b83e5b99-fa70-48ea-8f46-8a18dc229a1e",
               "71761ede-a662-4613-8c64-05088baff6d7",
                "2632cbf9-27d4-4de5-87b6-e42f20782d06",
               "06093a0e-29c3-4f63-8cea-868fd17632a3",
               "2dc5f753-1e5e-4508-8879-ece8058e2f8f",
                "86bb4484-c85b-4288-b84b-9560c9474f12",
              "ea26e299-8eab-4487-bd9b-92b8d98e53a5",
               "a61dd92f-de31-4478-bd00-4b16bfffbc28",
               "c9da8ee8-2b3a-4a91-b650-37b34440bd1c",
                "3b01a1ca-c198-46ea-a8d0-962a2b5e5eed",
                "63d78132-3237-4097-a118-2eccd50236c1",
                "0bef5a32-f83d-45de-ab1a-80e528c77482",
               "3bc953e5-112d-47c4-bbbf-cf0bddb4fe20",
                "1fb032c4-2e30-43b1-b7db-437c30c2a2b2",
                "31cf3fe8-47e4-473f-87df-714eefc0b29e",
                "720ed79b-c029-4546-b017-0f197944a17d",
                "933ca1ed-7028-454f-91f6-6a143e204dc8",
                "93efa3d7-3981-4146-a2bd-e20d1e8a5fb0"
                };

                //
                String[] fixedUUID=new String[]{
                        "4afbd1d5-728e-416b-8ef4-35578f171245",
                        "452f52fb-90d3-4c24-ab62-a94afa89348d",
                        "9d024284-6386-4044-a828-6d537270818e",
                        "984f5825-0df9-4d34-ab1b-f9417024bf18",
                        "3df65e9a-869c-4edd-9d4a-a678b0f13fe2",
                        "74d98b1e-3cec-4b37-84d5-269e98569693",
                        "94750678-aeb0-488e-826f-c5237985990a",
                        "61f51f57-3ca8-4e7d-b77f-a2fd84b1a54b",
                        "3cef78f4-92a8-42f1-b88b-020f1ac46b8d",
                        "d2007c11-a1b5-40d0-a89d-6425c81deb79",
                        "d6a188ac-82a2-4114-a0af-7a776e960c31",
                        "b289f842-6709-4d8a-8013-bc030951abb2",
                        "a7f27e42-2c1c-4ede-b64e-7c3b580b9382",
                        "c6bb8c4f-a834-4231-9182-21b8c9564582",
                        "a08df42f-e476-4252-b2d4-5386b4803e4f",
                        "4cddcf78-861a-4fd0-bf79-cf9885c0ed6c",
                        "5a8b538b-1af5-4d6f-a81b-6eee3dba90f9",
                        "02899969-471e-4e3c-90f7-cf915ad63c90",
                        "bac6401e-6366-4818-a1ac-1d8a378f9c27",
                        "fbdcbb47-a935-4853-bd0f-681752e82683",
                        "9b9d1afa-1c15-4f4b-8002-ec76d01d37ac",
                        "c94518ab-d122-408e-bb39-89d0fbc7c208",
                        "7683715f-47f8-427e-8a07-e623ea236ef4",
                        "99eb8c06-add6-4803-8227-ce146aeac925",
                        "e0a44f61-aa7a-489d-85ba-fed730199fe7",
                        "9a6af62f-08d3-463b-95f0-6f6a006a3e65",
                        "b2a12b99-8d9c-4224-ba7f-94740834932a",
                        "35c71b91-164a-4063-a680-48fd2df6caf5",
                        "2c77a48c-b322-4c3e-9f62-497949dd1f42",
                        "2b6d8946-f60b-4a40-b573-0da52d55c580",
                        "67d79b66-f1bb-4cae-b5dc-dca96fd9b4c2",
                        "6b96f101-98c9-4fea-b652-ec2b2155cad3",
                        "65c4de91-1317-4ada-9fac-15923ada8a05",
                        "154769ad-e41e-4fec-88a6-d05328ada8d8",
                        "8dadb425-ae45-48b0-8e2e-a1914edea72e",
                        "013321e3-6f78-48f1-8106-a6ca739d47a4",
                        "59c0849e-ad39-4e0e-a6dd-156e120ea753",
                        "a10da37b-d9fd-42e7-90a1-4dc82ca36a62",
                        "136314a0-0a22-4131-b423-80620414d0af",
                        "1ea8fd14-edfb-4c91-899f-2fb8588399ac",
                        "bb6afdbc-229a-4afd-b6ba-9349136786d8",
                        "562387a0-bc40-40fc-ae69-2564c63a99b4",
                        "f30ab9ed-ab8a-4d72-86fc-32f63307fe5c",
                        "8df024e8-8e7e-40c3-83d5-de52be1e6fce"
                };

                Map<String,String> transMap=new HashMap<>();




                //замена на точные старые UUID, соответствуют файлам перевода
                int j=0;
                for (Complex complex : model.findAllComplexBySection(section)) {
                    complex.setUuid(fileUUIDs[j++]);
                    model.updateComplex(complex);
                    for (Program program : model.findAllProgramByComplex(complex)) {
                        program.setUuid(fileUUIDs[j++]);
                        model.updateProgram(program);
                    }
                }

                //замена UUID в TrinityBase
                j=0;
                section.setUuid("e10ffe0a-1064-4c02-ad32-c3cf15ea958b");
                getModel().updateSection(section);

                //сначала все программы, потом комплексы не иначе, тк это порядок обработки файла перевода!!
                for (Complex complex : model.findAllComplexBySection(section)) {
                    for (Program program : model.findAllProgramByComplex(complex)) {
                        transMap.put(program.getUuid(),fixedUUID[j]);
                        program.setUuid(fixedUUID[j++]);
                        model.updateProgram(program);
                        //System.out.println("Program" + program.getId());
                    }
                }

                for (Complex complex : model.findAllComplexBySection(section)) {
                    transMap.put(complex.getUuid(),fixedUUID[j]);
                    complex.setUuid(fixedUUID[j++]);
                    model.updateComplex(complex);
                   // System.out.println("complex" + complex.getId());

                }


                setTranslate("/updates/update7/trinity_fr_trans.xml","fr",transMap);
                setTranslate("/updates/update7/trinity_it_trans.xml","it",transMap);
                setTranslate("/updates/update7/trinity_en_trans.xml","en",transMap);
                setTranslate("/updates/update7/trinity_el_trans.xml","el",transMap);



                rootSectionNames("La Nuova base delle  frequenze","La Vecchia base delle frequenze","it");
                rootSectionNames("Base de nouvelles fréquences","Base de fréquences anciennes","fr");
                rootSectionNames("Neue Frequenzen Basis","Alte Frequenzen Basis","de");
                rootSectionNames("Βάση νέων συχνοτήτων","Βάση παλαιών συχνοτήτων","el");


                //заменить частоты в программах
                // a6e99500-ac4a-486a-98e6-d215dab68afd  2;12;26;26.5;66;75.5;94;95.5
                //  8d8a4c44-1e35-4ec9-a653-ec46a7b72804  6.3;6.5;23.5;60.5;61.5;63;64.5;67
                //  8634ffa9-371b-499f-b64f-00f2ea043fee  1550;880;802;800;787;727;672;444
                //  f81443c3-1ad8-4f8a-9a73-d57fdf691839 332;698;721;732;749;752;942;991.5;1026.2;3212;4412

                Program p1=   getModel().findProgram("a6e99500-ac4a-486a-98e6-d215dab68afd");
                Program p2=   getModel().findProgram("8d8a4c44-1e35-4ec9-a653-ec46a7b72804");
                Program p3=   getModel().findProgram("8634ffa9-371b-499f-b64f-00f2ea043fee");
                Program p4=   getModel().findProgram("f81443c3-1ad8-4f8a-9a73-d57fdf691839");
                Program p5=   getModel().findProgram("160ca4f8-ca82-4b7e-8dbe-311d993bf7af");

                if(p1==null || p2==null || p3==null || p4==null|| p5==null){
                    logger.error("не найдены программы для обновления частот");
                }else {
                        p1.setFrequencies("2;12;26;26.5;66;75.5;94;95.5");
                        p2.setFrequencies("6.3;6.5;23.5;60.5;61.5;63;64.5;67");
                        p3.setFrequencies("1550;880;802;800;787;727;672;444");
                        p4.setFrequencies("332;698;721;732;749;752;942;991.5;1026.2;3212;4412");
                        p5.setFrequencies("941.9;425;433;445;941.9;935;1010;1060;457;465;777;778;1214;1216;8478");
                        getModel().updateProgram(p1);
                        getModel().updateProgram(p2);
                        getModel().updateProgram(p3);
                        getModel().updateProgram(p4);
                        getModel().updateProgram(p5);


                }

                setLangNameForSection("6a5d84b9-97b0-415b-9c69-ac1dca3dcad3","Autorenkomplexe","de");
                setLangNameForSection("c1ff40dd-0e9d-413e-a9d9-d613eeeb8f2d","General Programme","de");
                setLangNameForSection("15cf155a-cc24-49ed-98f1-a6c3fdec3539","Frequenzen der chemischen Elemente","de");
                setLangNameForSection("d1a0b290-ccee-4fd9-b635-6861b8a508a7","Vorbeugende komplexe","de");
                setLangNameForSection("600886d6-5c2b-4a0b-b974-d151f1125c42","Sätze von Programmen","de");
                setLangNameForSection("205967cf-02fa-4e96-ac00-a19e5dd2a3fb","Antiparasitäre","de");
                setLangNameForSection("db31c890-3500-4dee-84f3-8c49a7518112","Benutzerbasis","de");

                setUpdateVersion(updateOption,7);
                return true;

            }
        };


        task.setOnSucceeded(event -> {
            if(!task.getValue().booleanValue())  {
                BaseController.showErrorDialog("Обновление 7","","Обновление не установленно",null,Modality.WINDOW_MODAL);
                Platform.exit();
            }
            else  logger.info("ОБНОВЛЕНИЕ 7. Завершено");

            UpdateWaiter.close();
        });
        task.setOnFailed(event -> {
            Platform.runLater(() -> BaseController.showErrorDialog("Обновление 7","","Обновление не установленно",null,Modality.WINDOW_MODAL) );
            Platform.exit();
            UpdateWaiter.close();
        });

        Thread threadTask=new Thread(task);
        threadTask.setDaemon(true);
        threadTask.start();
        UpdateWaiter.show();



    }
    private void update8(ProgramOptions updateOption)
    {
        logger.info("ОБНОВЛЕНИЕ 8.");
        Task<Boolean> task =new Task<Boolean>()  {
            @Override
            protected Boolean call() throws Exception {

                Program p5=   getModel().findProgram("160ca4f8-ca82-4b7e-8dbe-311d993bf7af");

                if(p5==null){
                    logger.error("не найдены программы для обновления частот");
                }else {
                    p5.setFrequencies("941.9;425;433;445;941.9;935;1010;1060;457;465;777;778;1214;1216;8478");
                    getModel().updateProgram(p5);

                }

                //обновление имен элементов базы на немецком
                //6a5d84b9-97b0-415b-9c69-ac1dca3dcad3   авторские
                //c1ff40dd-0e9d-413e-a9d9-d613eeeb8f2d общие программы
                //15cf155a-cc24-49ed-98f1-a6c3fdec3539 хим э
                //d1a0b290-ccee-4fd9-b635-6861b8a508a7 профилактическ
                // 600886d6-5c2b-4a0b-b974-d151f1125c42 наборы программ
                //  205967cf-02fa-4e96-ac00-a19e5dd2a3fb  антипараз
                //   db31c890-3500-4dee-84f3-8c49a7518112 USER BASE
                setLangNameForSection("6a5d84b9-97b0-415b-9c69-ac1dca3dcad3","Autorenkomplexe","de");
                setLangNameForSection("c1ff40dd-0e9d-413e-a9d9-d613eeeb8f2d","General Programme","de");
                setLangNameForSection("15cf155a-cc24-49ed-98f1-a6c3fdec3539","Frequenzen der chemischen Elemente","de");
                setLangNameForSection("d1a0b290-ccee-4fd9-b635-6861b8a508a7","Vorbeugende komplexe","de");
                setLangNameForSection("600886d6-5c2b-4a0b-b974-d151f1125c42","Sätze von Programmen","de");
                setLangNameForSection("205967cf-02fa-4e96-ac00-a19e5dd2a3fb","Antiparasitäre","de");
                setLangNameForSection("db31c890-3500-4dee-84f3-8c49a7518112","Benutzerbasis","de");

                setUpdateVersion(updateOption,8);
                return true;

            }
        };


        task.setOnSucceeded(event -> {
            if(!task.getValue().booleanValue())  {
                BaseController.showErrorDialog("Обновление 8","","Обновление не установленно",null,Modality.WINDOW_MODAL);
                Platform.exit();
            }
            else  logger.info("ОБНОВЛЕНИЕ 8. Завершено");

            UpdateWaiter.close();
        });
        task.setOnFailed(event -> {
            Platform.runLater(() -> BaseController.showErrorDialog("Обновление 8","","Обновление не установленно",null,Modality.WINDOW_MODAL) );
            Platform.exit();
            UpdateWaiter.close();
        });

        Thread threadTask=new Thread(task);
        threadTask.setDaemon(true);
        threadTask.start();
        UpdateWaiter.show();



    }

    private void update9(ProgramOptions updateOption) {
        logger.info("ОБНОВЛЕНИЕ 9.");
        Task<Boolean> task =new Task<Boolean>()  {
            @Override
            protected Boolean call() throws Exception {


                try
                {
                    logger.info("Проверка наличия столбца TIMEFORFREQ  в COMPLEX ");
                    Object singleResult = emf.createEntityManager().createNativeQuery("SELECT `TIMEFORFREQ` FROM COMPLEX LIMIT 1").getSingleResult();
                    logger.info("Столбец  TIMEFORFREQ  найден.");

                }catch (Exception e) {
                    logger.info("Столбец  TIMEFORFREQ не найден.");
                    logger.info("Создается  столбец TIMEFORFREQ  в COMPLEX ");
                    EntityManager em = emf.createEntityManager();
                    em.getTransaction().begin();
                    try {
                        em.createNativeQuery("ALTER TABLE COMPLEX ADD `TIMEFORFREQ` INT DEFAULT 0").executeUpdate();
                        em.getTransaction().commit();
                        logger.info("Столбец  TIMEFORFREQ создан.");


                    } catch (Exception ex) {
                        logger.error("ALTER TABLE COMPLEX ADD `TIMEFORFREQ` INT DEFAULT 0", ex);
                        return false;
                    } finally {
                        if (em != null) em.close();
                    }
                }

                    reopenPersistentContext();

                try {
                    setTimeForFreqToCompex("7683715f-47f8-427e-8a07-e623ea236ef4" ,300);
                    setTimeForFreqToCompex("99eb8c06-add6-4803-8227-ce146aeac925" ,30);
                    setTimeForFreqToCompex("e0a44f61-aa7a-489d-85ba-fed730199fe7" ,30);
                    setTimeForFreqToCompex("9a6af62f-08d3-463b-95f0-6f6a006a3e65" ,30);
                    setTimeForFreqToCompex("b2a12b99-8d9c-4224-ba7f-94740834932a" ,30);
                    setTimeForFreqToCompex("35c71b91-164a-4063-a680-48fd2df6caf5" ,300);
                    setTimeForFreqToCompex("2c77a48c-b322-4c3e-9f62-497949dd1f42" ,60);
                    setTimeForFreqToCompex("6b96f101-98c9-4fea-b652-ec2b2155cad3" ,60);
                    setTimeForFreqToCompex("65c4de91-1317-4ada-9fac-15923ada8a05" ,60);
                    setTimeForFreqToCompex("154769ad-e41e-4fec-88a6-d05328ada8d8" ,60);
                    setTimeForFreqToCompex("8dadb425-ae45-48b0-8e2e-a1914edea72e" ,60);
                    setTimeForFreqToCompex("013321e3-6f78-48f1-8106-a6ca739d47a4" ,30);
                    setTimeForFreqToCompex("59c0849e-ad39-4e0e-a6dd-156e120ea753" ,60);
                    setTimeForFreqToCompex("a10da37b-d9fd-42e7-90a1-4dc82ca36a62" ,180);
                    setTimeForFreqToCompex("136314a0-0a22-4131-b423-80620414d0af" ,180);
                    setTimeForFreqToCompex("1ea8fd14-edfb-4c91-899f-2fb8588399ac" ,90);
                    setTimeForFreqToCompex("bb6afdbc-229a-4afd-b6ba-9349136786d8" ,30);
                    setTimeForFreqToCompex("562387a0-bc40-40fc-ae69-2564c63a99b4" ,30);
                    setTimeForFreqToCompex("f30ab9ed-ab8a-4d72-86fc-32f63307fe5c" ,30);
                    setTimeForFreqToCompex("8df024e8-8e7e-40c3-83d5-de52be1e6fce" ,600);
                    setTimeForFreqToCompex("2b6d8946-f60b-4a40-b573-0da52d55c580" ,30);
                    setTimeForFreqToCompex("67d79b66-f1bb-4cae-b5dc-dca96fd9b4c2" ,60);


                }catch (Exception e){
                    Log.logger.error("Вероятно предыдущие обновления меняющие UUID прошли некорректно",e);
                    return false;
                }
/*
                //uuid="7683715f-47f8-427e-8a07-e623ea236ef4" name="Активация жизненной энергии"  timeForFreq="300"
               // uuid="99eb8c06-add6-4803-8227-ce146aeac925" name="Альфа релакс для восстановления физических сил" timeForFreq="30"
               // uuid="e0a44f61-aa7a-489d-85ba-fed730199fe7" name="Альфа релакс для комфортного состояния"  timeForFreq="30"
                //uuid="9a6af62f-08d3-463b-95f0-6f6a006a3e65" name="Альфа релакс для умиротворения"  timeForFreq="30"
                //uuid="b2a12b99-8d9c-4224-ba7f-94740834932a" name="Балансировка частот"  timeForFreq="30"
                //uuid="35c71b91-164a-4063-a680-48fd2df6caf5" name="Вне времени-1"  timeForFreq="300"
                //uuid="2c77a48c-b322-4c3e-9f62-497949dd1f42" name="Вне времени-2" timeForFreq="60"
                //uuid="6b96f101-98c9-4fea-b652-ec2b2155cad3" name="Полусон перед сном"  timeForFreq="60"
               // uuid="65c4de91-1317-4ada-9fac-15923ada8a05" name="Полусон после сна"  timeForFreq="60"
                //uuid="154769ad-e41e-4fec-88a6-d05328ada8d8" name="Метаболизм-1"  timeForFreq="60"
                //uuid="8dadb425-ae45-48b0-8e2e-a1914edea72e" name="Метаболизм-2"  timeForFreq="60"
                //uuid="013321e3-6f78-48f1-8106-a6ca739d47a4" name="Стиратель мыслей"  timeForFreq="30"
                //uuid="59c0849e-ad39-4e0e-a6dd-156e120ea753" name="Суггестия"  timeForFreq="60"
                //uuid="a10da37b-d9fd-42e7-90a1-4dc82ca36a62" name="Суперпрограмма-1"  timeForFreq="180"
                //uuid="136314a0-0a22-4131-b423-80620414d0af" name="Суперпрограмма-2"  timeForFreq="180"
                //uuid="1ea8fd14-edfb-4c91-899f-2fb8588399ac" name="Суперпрограмма-3"  timeForFreq="90"
                //uuid="bb6afdbc-229a-4afd-b6ba-9349136786d8" name="Тета релакс вдохновение"  timeForFreq="30"
                //uuid="562387a0-bc40-40fc-ae69-2564c63a99b4" name="Тета релакс пробуждение сознания"  timeForFreq="30"
                //uuid="f30ab9ed-ab8a-4d72-86fc-32f63307fe5c" name="Утренняя свежесть"  timeForFreq="30"
                //uuid="8df024e8-8e7e-40c3-83d5-de52be1e6fce" name="Уменьшение тревожности"  timeForFreq="600"
               // uuid="2b6d8946-f60b-4a40-b573-0da52d55c580" name="Дельта для сна"  timeForFreq="30"
                uuid="67d79b66-f1bb-4cae-b5dc-dca96fd9b4c2" name="Измененные состояния сознания"  timeForFreq="60"

*/
                setUpdateVersion(updateOption,9);
                return true;

            }
        };


        task.setOnSucceeded(event -> {
            if(!task.getValue().booleanValue())  {
                BaseController.showErrorDialog("Обновление 9","","Обновление не установленно",null,Modality.WINDOW_MODAL);
                Platform.exit();
            }
            else  logger.info("ОБНОВЛЕНИЕ 9. Завершено");

            UpdateWaiter.close();
        });
        task.setOnFailed(event -> {
            Platform.runLater(() -> BaseController.showErrorDialog("Обновление","","Обновление не установленно",null,Modality.WINDOW_MODAL) );
            Platform.exit();
            UpdateWaiter.close();
        });

        Thread threadTask=new Thread(task);
        threadTask.setDaemon(true);
        threadTask.start();
        UpdateWaiter.show();


    }

    private void setTimeForFreqToCompex(String uuid,int time) throws Exception {

        Complex complex = getModel().getComplex(uuid);
        complex.setTimeForFreq(time);
        getModel().updateComplex(complex);
    }
    /**
     * Установит новое имя для комплекса
     * @param uuid
     * @param name
     * @param langAbbr
     * @throws Exception
     */
    private void setLangNameForComplex(String uuid, String name,String langAbbr) throws Exception {

        Complex complex = model.findComplex(uuid);
        if(complex==null) throw new Exception("Не найден комплекс с UUID = "+uuid);
        LocalizedString lName = model.getLocalString(complex.getName(), model.getLanguage(langAbbr));
        if(lName==null) lName=model.addString(complex.getName(),name,model.getLanguage(langAbbr));
        else {
            lName.setContent(name);
            model.updateLocalString(lName);
        }
    }

    /**
     * Установит новое имя для раздела
     * @param uuid
     * @param name
     * @param langAbbr
     * @throws Exception
     */
    private void setLangNameForSection(String uuid, String name,String langAbbr) throws Exception {

        Section section = model.findSection(uuid);
        if(section==null) throw new Exception("Не найдена секция с UUID = "+uuid);
        LocalizedString lName = model.getLocalString(section.getName(), model.getLanguage(langAbbr));
        if(lName==null) model.addString(section.getName(),name,model.getLanguage(langAbbr));
        else {
            lName.setContent(name);
            model.updateLocalString(lName);
        }
    }

    /**
     * Установит новое имя для программы
     * @param uuid
     * @param name
     * @param langAbbr
     * @throws Exception
     */
    private void setLangNameForProgram(String uuid, String name,String langAbbr) throws Exception {

        Program program = model.findProgram(uuid);
        if(program==null) throw new Exception("Не найдена программа с UUID = "+uuid);
        LocalizedString lName = model.getLocalString(program.getName(), model.getLanguage(langAbbr));
        if(lName==null) model.addString(program.getName(),name,model.getLanguage(langAbbr));
        else {
            lName.setContent(name);
            model.updateLocalString(lName);
        }
    }

    private void rootSectionNames(String newBaseName,String oldBaseName,String langAbbr) throws Exception {

        List<Section> rootSections = model.findAllRootSection();
        Strings nameString = rootSections.get(0).getName();
        LocalizedString lName = model.getLocalString(nameString, model.getLanguage(langAbbr));
        if(lName==null) model.addString(nameString,newBaseName,model.getLanguage(langAbbr));
        else {
            lName.setContent(newBaseName);
            model.updateLocalString(lName);
        }

        nameString = rootSections.get(1).getName();
        lName = model.getLocalString(nameString, model.getLanguage(langAbbr));
        if(lName==null) lName=model.addString(nameString,oldBaseName,model.getLanguage(langAbbr));
        else {
            lName.setContent(oldBaseName);
            model.updateLocalString(lName);
        }
    }
    private void setTranslate(String pathResource,String langAbbr,Map<String,String> transMap) throws Exception {

        ResourceUtil resourceUtil=new ResourceUtil();
        File base_translate = resourceUtil.saveResource(getTmpDir(),langAbbr+"_translanion_bas.xml",pathResource,true);

        if(base_translate==null) throw new Exception();

        LoadLanguageFiles ll=new LoadLanguageFiles(transMap);
        if( ll.parse(Arrays.asList(base_translate),getModel())){


            System.out.println("Обработка "+langAbbr+" языка");
        } else {
            System.out.println("Обработка "+langAbbr+" языка ОШИБКА");
            throw new Exception("Обработка "+langAbbr+" языка ОШИБКА");
        }
    }
    private void addTrinityBase() throws Exception {
        Section section=null;
        boolean alsoNewsVers=false;
        try {
            section = model.findAllSectionByTag("TRINITY");
            if(section!=null) {
                //все программы размещены в комплексах, прямо в разделе!!!. В первой версии этого раздела, просто были программы в кучу. Тут мы проверяем фактически уже новая версия или еще старая
                if(model.findAllComplexBySection(section).size()==0){
                    //обновление. Просто удалим. Потом сделаем реимпорт из обновленного файла

                    model.clearSection(section);


                }else {
                    alsoNewsVers=true;
                }
            }
            if(alsoNewsVers) return;

            //создание
            if(section==null) section = model.createSection(null, "Trinity", "", "TRINITY", true, model.getDefaultLanguage());
            if(section==null) throw new Exception("Ошибка создания раздела Trinity");
            ImportUserBase iUB=new ImportUserBase();

            ResourceUtil ru=new ResourceUtil();
            File trinityBaseFile = ru.saveResource(getTmpDir(),"trinity.xmlb","/updates/update5/trinity.xmlb",true);

            if(trinityBaseFile==null) throw new Exception("Не удалось создать файл для импорта базы");

            iUB.setListener(new ImportUserBase.Listener() {
                @Override
                public void onStartParse() {

                }

                @Override
                public void onEndParse() {

                }

                @Override
                public void onStartAnalize() {

                }

                @Override
                public void onEndAnalize() {

                }

                @Override
                public void onStartImport() {

                }

                @Override
                public void onEndImport() {

                }

                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(boolean fileTypeMissMatch) {

                }
            });
            boolean res = iUB.parse(trinityBaseFile, model, section,true,getModel().getLanguage("ru"));
            if(res==false) {
                model.removeSection(section);
                throw new Exception("Ошибка импорта программ");
            }

        }catch (Exception e){

           if(section!=null) model.removeSection(section);
            throw new Exception("Ошибка импорта программ",e);
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
