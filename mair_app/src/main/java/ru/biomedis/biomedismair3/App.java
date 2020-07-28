package ru.biomedis.biomedismair3;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;


import org.anantacreative.updater.VersionCheck.Version;
import ru.biomedis.biomedismair3.UpdateUtils.FrequenciesBase.LoadLanguageFiles;
import ru.biomedis.biomedismair3.UpdateUtils.FrequenciesBase.LoadUUIDs;
import ru.biomedis.biomedismair3.UserUtils.Import.ImportUserBase;
import ru.biomedis.biomedismair3.entity.*;
import ru.biomedis.biomedismair3.social.remote_client.SocialClient;
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
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.biomedis.biomedismair3.BaseController.getApp;
import static ru.biomedis.biomedismair3.BaseController.showExceptionDialog;


@Slf4j
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
      private int updateFixVersion;//значение в базе
      private final String socialAPIURL = "https://social.biomedis.life";


  private String getSocialAPIURL() {

    return (isDeveloped() || isIDEStarted())?"http://localhost:8080":socialAPIURL;

  }

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
     // private final boolean importDB=false;//импорт базы данных, легаси не использовать!!!
      private  boolean updateBaseMenuVisible =false;//показ пункта обновления базы частот

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

    private static Version appVersion;
    public static Version getAppVersion(){ return appVersion;}

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
           log.error("Ошибка обновления опции updateVersion",ex);
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
            log.error("Ошибка обновления опции updateFixVersion",ex);
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
            log.error("Ошибка копирования кодека",e);
            return false;
        }
        return true;
    }




    private boolean neadCleanDataFilesAndState=false;

    private boolean needExit=false;

    private boolean isNeedExit() {
        return needExit;
    }

    private void setNeedExit(boolean needExit) {
        this.needExit = needExit;
    }

    private boolean developed;

  /**
   * Режим разработчика
   * Необходимо передать в аргументах командной строки develop=true
   * vm args   -Ddevelop=true
   * @return
   */
  public boolean isDeveloped() {
    return developed;
  }

  private boolean isIDEStarted() {
    File innerDataDir = App.getInnerDataDir_();
    File rootDir = new File(innerDataDir, "../");
    return rootDir.listFiles((dir, name) -> name.equals("pom.xml")).length == 1;
  }

  @Override
    public void start(Stage stage) throws Exception {

    log.info("Старт программы");

      String develop = System.getProperty("develop");
      developed = Boolean.parseBoolean(develop);
      updateBaseMenuVisible = developed;

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
        int currentUpdateFile=14;//версия ставиться вручную. Если готовили инсталлер, он будет содержать правильную версию  getUpdateVersion(), а если человек скопировал себе jar обновления, то версии будут разные!
        int currentMinorVersion=6;//версия исправлений в пределах мажорной версии currentUpdateFile
        //требуется размещение в папке с dist.jar  файла version.txt с текущей версией типа 4.9.0 . Этот файл в обновление нужно включать!!!
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
            log.error("Запуск апдейта "+currentUpdateFile+" ниже установленного "+getUpdateVersion()+"!");

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
        selectUpdateFixVersion();
        Version jarVersion = new Version(4, currentUpdateFile, currentMinorVersion);
        Version curVersion = new Version(4, getUpdateVersion(), updateFixVersion);

//доп. обновления требующие ModelDataApp
        //if(getUpdateVersion() < currentUpdateFile)
        if(curVersion.lessThen(jarVersion))
        {


            //обновим согласно полученной версии, учесть, что нужно на младшие накатывать все апдейты по порядку
            if(getUpdateVersion()==2) {
                CompletableFuture.runAsync(() -> changeDDL())
                        .thenRun(() ->  update3(updateOption))
                                 .thenRun(() -> update4(updateOption))
                                 .thenRun(() -> update5(updateOption))
                                 .thenRun(() -> update6(updateOption))
                                 .thenRun(() -> update7(updateOption))
                                 .thenRun(() -> update8(updateOption))
                                 .thenRun(() -> update9(updateOption))
                                 .thenRun(()->updateIn9(updateOption,updateFixVersion))
                                 .thenRun(() -> update10(updateOption))
                                 .thenRun(()->updateIn10(updateOption,updateFixVersion))
                                 .thenRun(() -> update11(updateOption))
                                 .thenRun(()->updateIn11(updateOption,updateFixVersion))
                                 .thenRun(()->update12(updateOption))
                                 .thenRun(() -> update_in12(updateOption,updateFixVersion))
                                 .thenRun(() -> update13(updateOption))
                                 .thenRun(() -> updateIn13(updateOption,updateFixVersion))
                                 .thenRun(() -> update14(updateOption))
                                 .thenRun(() -> updateIn14(updateOption,updateFixVersion))
                        .thenRun(() -> Platform.runLater(() -> UpdateWaiter.close()))
                        .exceptionally(e->{
                            showUpdateErrorAndExit(e.getMessage());
                            return null;
                        });
            }else if(getUpdateVersion()==3){
                CompletableFuture.runAsync(() -> changeDDL())
                        .thenRun(() ->  update4(updateOption))
                                           .thenRun(() -> update5(updateOption))
                                           .thenRun(() -> update6(updateOption))
                                           .thenRun(() -> update7(updateOption))
                                           .thenRun(() -> update8(updateOption))
                                           .thenRun(() -> update9(updateOption))
                                           .thenRun(()->updateIn9(updateOption,updateFixVersion))
                                 .thenRun(() -> update10(updateOption))
                                 .thenRun(()->updateIn10(updateOption,updateFixVersion))
                                 .thenRun(() -> update11(updateOption))
                                 .thenRun(()->updateIn11(updateOption,updateFixVersion))
                                 .thenRun(()->update12(updateOption))
                                 .thenRun(() -> update_in12(updateOption,updateFixVersion))
                                 .thenRun(() -> update13(updateOption))
                                 .thenRun(() -> updateIn13(updateOption,updateFixVersion))
                                 .thenRun(() -> update14(updateOption))
                                 .thenRun(() -> updateIn14(updateOption,updateFixVersion))
                        .thenRun(() -> Platform.runLater(() -> UpdateWaiter.close()))
                        .exceptionally(e->{
                            showUpdateErrorAndExit(e.getMessage());
                            return null;
                        });
            }else if(getUpdateVersion()==4){
                CompletableFuture.runAsync(() -> changeDDL())
                        .thenRun(() ->  update5(updateOption))
                                           .thenRun(() -> update6(updateOption))
                                           .thenRun(() -> update7(updateOption))
                                           .thenRun(() -> update8(updateOption))
                                           .thenRun(() -> update9(updateOption))
                                 .thenRun(()->updateIn9(updateOption,updateFixVersion))
                                 .thenRun(() -> update10(updateOption))
                                 .thenRun(()->updateIn10(updateOption,updateFixVersion))
                                 .thenRun(() -> update11(updateOption))
                                 .thenRun(()->updateIn11(updateOption,updateFixVersion))
                                 .thenRun(()->update12(updateOption))
                                 .thenRun(() -> update_in12(updateOption,updateFixVersion))
                                 .thenRun(() -> update13(updateOption))
                                 .thenRun(() -> updateIn13(updateOption,updateFixVersion))
                        .thenRun(() -> update14(updateOption))
                        .thenRun(() -> updateIn14(updateOption,updateFixVersion))
                        .thenRun(() -> Platform.runLater(() -> UpdateWaiter.close()))
                        .exceptionally(e->{
                            showUpdateErrorAndExit(e.getMessage());
                            return null;
                        });
            }else if(getUpdateVersion()==5){
                CompletableFuture.runAsync(() -> changeDDL())
                        .thenRun(() ->  update6(updateOption))
                                           .thenRun(() -> update7(updateOption))
                                           .thenRun(() -> update8(updateOption))
                                           .thenRun(() -> update9(updateOption))
                                 .thenRun(()->updateIn9(updateOption,updateFixVersion))
                                 .thenRun(() -> update10(updateOption))
                                 .thenRun(()->updateIn10(updateOption,updateFixVersion))
                                 .thenRun(() -> update11(updateOption))
                                 .thenRun(()->updateIn11(updateOption,updateFixVersion))
                                 .thenRun(()->update12(updateOption))
                                 .thenRun(() -> update_in12(updateOption,updateFixVersion))
                                 .thenRun(() -> update13(updateOption))
                                 .thenRun(() -> updateIn13(updateOption,updateFixVersion))
                        .thenRun(() -> update14(updateOption))
                        .thenRun(() -> updateIn14(updateOption,updateFixVersion))
                        .thenRun(() -> Platform.runLater(() -> UpdateWaiter.close()))
                        .exceptionally(e->{
                            showUpdateErrorAndExit(e.getMessage());
                            return null;
                        });
            }else if(getUpdateVersion()==6){

                CompletableFuture.runAsync(() -> changeDDL())
                        .thenRun(() ->  update7(updateOption))
                                           .thenRun(() -> update8(updateOption))
                                           .thenRun(() -> update9(updateOption))
                                 .thenRun(()->updateIn9(updateOption,updateFixVersion))
                                 .thenRun(() -> update10(updateOption))
                                 .thenRun(()->updateIn10(updateOption,updateFixVersion))
                                 .thenRun(() -> update11(updateOption))
                                 .thenRun(()->updateIn11(updateOption,updateFixVersion))
                                 .thenRun(()->update12(updateOption))
                                 .thenRun(() -> update_in12(updateOption,updateFixVersion))
                                 .thenRun(() -> update13(updateOption))
                                 .thenRun(() -> updateIn13(updateOption,updateFixVersion))
                        .thenRun(() -> update14(updateOption))
                        .thenRun(() -> updateIn14(updateOption,updateFixVersion))
                        .thenRun(() -> Platform.runLater(() -> UpdateWaiter.close()))
                        .exceptionally(e->{
                            showUpdateErrorAndExit(e.getMessage());
                            return null;
                        });
            }else if(getUpdateVersion()==7){

                CompletableFuture.runAsync(() -> changeDDL())
                        .thenRun(() ->  update8(updateOption))
                                           .thenRun(() -> update9(updateOption))
                                 .thenRun(()->updateIn9(updateOption,updateFixVersion))
                                 .thenRun(() -> update10(updateOption))
                                 .thenRun(()->updateIn10(updateOption,updateFixVersion))
                                 .thenRun(() -> update11(updateOption))
                                 .thenRun(()->updateIn11(updateOption,updateFixVersion))
                                 .thenRun(()->update12(updateOption))
                                 .thenRun(() -> update_in12(updateOption,updateFixVersion))
                                 .thenRun(() -> update13(updateOption))
                                 .thenRun(() -> updateIn13(updateOption,updateFixVersion))
                        .thenRun(() -> update14(updateOption))
                        .thenRun(() -> updateIn14(updateOption,updateFixVersion))
                        .thenRun(() -> Platform.runLater(() -> UpdateWaiter.close()))
                        .exceptionally(e->{
                            showUpdateErrorAndExit(e.getMessage());
                            return null;
                        });
            }else if(getUpdateVersion()==8){

                CompletableFuture.runAsync(() -> changeDDL())
                        .thenRun(() ->  update9(updateOption))
                                 .thenRun(()->updateIn9(updateOption,updateFixVersion))
                                 .thenRun(() -> update10(updateOption))
                                 .thenRun(()->updateIn10(updateOption,updateFixVersion))
                                 .thenRun(() -> update11(updateOption))
                                 .thenRun(()->updateIn11(updateOption,updateFixVersion))
                                 .thenRun(()->update12(updateOption))
                                 .thenRun(() -> update_in12(updateOption,updateFixVersion))
                                 .thenRun(() -> update13(updateOption))
                                 .thenRun(() -> updateIn13(updateOption,updateFixVersion))
                        .thenRun(() -> update14(updateOption))
                        .thenRun(() -> updateIn14(updateOption,updateFixVersion))
                        .thenRun(() -> Platform.runLater(() -> UpdateWaiter.close()))
                        .exceptionally(e->{
                            showUpdateErrorAndExit(e.getMessage());
                            return null;
                        });

            }
            else if(getUpdateVersion()==9){
                CompletableFuture.runAsync(() -> changeDDL())
                                 .thenRun(() -> updateIn9(updateOption,updateFixVersion))
                                 .thenRun(() -> update10(updateOption))
                                 .thenRun(()->updateIn10(updateOption,updateFixVersion))
                                 .thenRun(() -> update11(updateOption))
                                 .thenRun(()->updateIn11(updateOption,updateFixVersion))
                                 .thenRun(()->update12(updateOption))
                                 .thenRun(() -> update_in12(updateOption,updateFixVersion))
                                 .thenRun(() -> update13(updateOption))
                                 .thenRun(() -> updateIn13(updateOption,updateFixVersion))
                        .thenRun(() -> update14(updateOption))
                        .thenRun(() -> updateIn14(updateOption,updateFixVersion))
                                 .thenRun(() -> Platform.runLater(() -> UpdateWaiter.close()))
                                 .exceptionally(e->{
                                     showUpdateErrorAndExit(e.getMessage());
                                     return null;
                                 });

            }else if(getUpdateVersion()==10){
                CompletableFuture.runAsync(() -> changeDDL())
                                 .thenRun(()->updateIn10(updateOption, updateFixVersion))
                                 .thenRun(() -> update11(updateOption))
                                 .thenRun(()->updateIn11(updateOption, updateFixVersion))
                                 .thenRun(()->update12(updateOption))
                                 .thenRun(() -> update_in12(updateOption, updateFixVersion))
                                 .thenRun(() -> update13(updateOption))
                                 .thenRun(() -> updateIn13(updateOption,updateFixVersion))
                        .thenRun(() -> update14(updateOption))
                        .thenRun(() -> updateIn14(updateOption,updateFixVersion))
                                 .thenRun(() -> Platform.runLater(() -> UpdateWaiter.close()))
                                 .exceptionally(e->{
                                     showUpdateErrorAndExit(e.getMessage());
                                     return null;
                                 });


                System.out.println("Update >10.0");

            }
            else if(getUpdateVersion()==11){
                CompletableFuture.runAsync(() -> changeDDL())
                                 .thenRun(()->updateIn11(updateOption,updateFixVersion))
                                 .thenRun(()->update12(updateOption))
                                 .thenRun(() -> update_in12(updateOption, updateFixVersion))
                                 .thenRun(() -> update13(updateOption))
                                 .thenRun(() -> updateIn13(updateOption,updateFixVersion))
                        .thenRun(() -> update14(updateOption))
                        .thenRun(() -> updateIn14(updateOption,updateFixVersion))
                                 .thenRun(() -> Platform.runLater(() -> UpdateWaiter.close()))
                                 .exceptionally(e->{
                                     showUpdateErrorAndExit(e.getMessage());
                                     return null;
                                 });


                System.out.println("Update >11.0");

            }
            else if(getUpdateVersion()==12){
                CompletableFuture.runAsync(() -> changeDDL())
                                 .thenRun(() -> update_in12(updateOption, updateFixVersion))
                                 .thenRun(() -> update13(updateOption))
                                 .thenRun(() -> updateIn13(updateOption,updateFixVersion))
                        .thenRun(() -> update14(updateOption))
                        .thenRun(() -> updateIn14(updateOption,updateFixVersion))
                                 .thenRun(() -> Platform.runLater(() -> UpdateWaiter.close()))
                                 .exceptionally(e->{
                                     showUpdateErrorAndExit(e.getMessage());
                                     return null;
                                 });


                System.out.println("Update >11.0");

            } else if(getUpdateVersion()==13){
                CompletableFuture.runAsync(() -> changeDDL())
                                 .thenRun(() -> updateIn13(updateOption,updateFixVersion))
                        .thenRun(() -> update14(updateOption))
                        .thenRun(() -> updateIn14(updateOption,updateFixVersion))
                                 .thenRun(() -> Platform.runLater(() -> UpdateWaiter.close()))
                                 .exceptionally(e->{
                                     showUpdateErrorAndExit(e.getMessage());
                                     return null;
                                 });




            } else if(getUpdateVersion()==14){
                CompletableFuture.runAsync(() -> changeDDL())
                        .thenRun(() -> updateIn14(updateOption,updateFixVersion))
                        .thenRun(() -> Platform.runLater(() -> UpdateWaiter.close()))
                        .exceptionally(e->{
                            showUpdateErrorAndExit(e.getMessage());
                            return null;
                        });


                System.out.println("Update >14.0");

            }
            else {
                showUpdateErrorAndExit("Версия обновления и версия программы конфликтуют. " + getUpdateVersion());
            }

            UpdateWaiter.show();


        }else if(getUpdateVersion() > currentUpdateFile){
            log.error("Запуск апдейта "+currentUpdateFile+" ниже установленного "+getUpdateVersion()+"!");

            BaseController.showInfoConfirmDialog(this.strings.getString("app.error"), "", this.strings.getString("app.update.incorrect_update_message"), null, Modality.APPLICATION_MODAL);
            Platform.exit();
            return;
        }

        System.out.println("Продолжение запуска программы после обновления");
        if(isNeedExit()){
            System.exit(0);
        }
        //проверка профиля биофона
        checkBiofonProfile();

            //установка нижней версии, если все этапы обновления прошли нормально
            setUpdateFixVersion(selectUpdateFixVersion(),currentMinorVersion);
            //настроим язык программы
            this.version = new Version(4,getUpdateVersion(),currentMinorVersion);


        boolean firstStart=false;

        appVersion = new Version(4, currentUpdateFile, currentMinorVersion);

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
                 log.error("",e);
                getModel().setProgramLanguageDefault();
            }

            //System.out.println("Язык -" + getModel().getProgramLanguage().getName());
        }else
        {
            getModel().setProgramLanguageDefault();
            //System.out.println("Язык - по умолчанию");
        }

    SocialClient.init(getSocialAPIURL(), getModel());

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
            controller.setWindow(stage);
            controller.onCompletedInitialization();
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
          if(AutoUpdater.getAutoUpdater().isProcessed()){
            event.consume();
            AppController.getProgressAPI().setInfoMessage(getResources().getString("launcher_is_updating"));
            return;
          }

            closeAppListeners.stream().forEach(listeners->listeners.onClose());

            try {
                USBHelper.stopHotPlugListener();
            } catch (USBHelper.USBException e) {
                log.error("Ошибка остановки слушателей USB",e);
            }catch (Exception e){
               log.error("Ошибка остановки слушателей USB",e);
            }finally {
                USBHelper.closeContext();
            }

        });


        stage.show();

//        if(importDB)
//        {
//
//
//            System.out.println("\n\n");
//            NewDBImport dbImport1=new NewDBImport(getModel());
//            if(dbImport1.execute()==false){System.out.println("Ошибка импорта новой базы"); return;}
//
//            OldDBImport dbImport=new OldDBImport(getModel());
//            if(dbImport.importDB()==false){System.out.println("Ошибка импорта старой базы"); return;}
//
//            AddonsDBImport addon=new AddonsDBImport(getModel());
//            if(addon.execute()==false){System.out.println("Ошибка импорта аддонов"); return;}
//        }
    }

    private void updateIn14(ProgramOptions updateOption, int updateFixVersion) {
        if(updateFixVersion == 0){
            updateIn14_1(updateOption);
            updateIn14_2(updateOption);
            updateIn14_3(updateOption);
          updateIn14_4(updateOption);
          updateIn14_5(updateOption);
        }else if(updateFixVersion == 1){
            updateIn14_2(updateOption);
            updateIn14_3(updateOption);
           updateIn14_4(updateOption);
          updateIn14_5(updateOption);
        }else if(updateFixVersion == 2){
            updateIn14_3(updateOption);
            updateIn14_4(updateOption);
          updateIn14_5(updateOption);
        }else if(updateFixVersion == 3){
            updateIn14_4(updateOption);
            updateIn14_5(updateOption);

        }
        else if(updateFixVersion == 4){
          updateIn14_5(updateOption);
        }

    }

  private void updateIn14_5(ProgramOptions updateOption) {

  }
    private void updateIn14_4(ProgramOptions updateOption) {

    }
    private void updateIn14_1(ProgramOptions updateOption) {

    }

    private void updateIn14_2(ProgramOptions updateOption) {

    }
    private void updateIn14_3(ProgramOptions updateOption) {
        log.info("ОБНОВЛЕНИЕ 14.3");

        File base_translate=null;
        try {
            ResourceUtil ru=new ResourceUtil();
            base_translate = ru.saveResource(getTmpDir(),"ro_translanion.xml","/updates/update14/roman2.xml",true);

            if(base_translate==null) throw new Exception();

            LoadLanguageFiles ll=new LoadLanguageFiles();
            if( ll.parse(Arrays.asList(base_translate),getModel())){

                log.info("ОБНОВЛЕНИЕ 14.3 ЗАВЕРШЕНО.");


            }
            else  wrapException("14.3",new Exception());

        } catch (IOException e) {
            wrapException("14.3",e);
            log.info("ОБНОВЛЕНИЕ 14.3 НЕ ЗАВЕРШЕНО.");

        } catch (Exception e) {
            wrapException("14.3",e);
            log.info("ОБНОВЛЕНИЕ 14.3 НЕ ЗАВЕРШЕНО.");

        }
    }
    private void update14(ProgramOptions updateOption) {

        log.info("ОБНОВЛЕНИЕ 14");

        File base_translate=null;
        try {
            ResourceUtil ru=new ResourceUtil();
            base_translate = ru.saveResource(getTmpDir(),"ro_translanion.xml","/updates/update14/roman_lang.xml",true);

            if(base_translate==null) throw new Exception();

            LoadLanguageFiles ll=new LoadLanguageFiles();
            if( ll.parse(Arrays.asList(base_translate),getModel())){

                log.info("ОБНОВЛЕНИЕ 14 ЗАВЕРШЕНО.");

                setUpdateVersion(updateOption, 14);
            }
            else  wrapException("14",new Exception());

        } catch (IOException e) {
            wrapException("14",e);
            log.info("ОБНОВЛЕНИЕ 14 НЕ ЗАВЕРШЕНО.");

        } catch (Exception e) {
            wrapException("14",e);
            log.info("ОБНОВЛЕНИЕ 14 НЕ ЗАВЕРШЕНО.");

        }


    }

    private void update12(ProgramOptions updateOption) {
        log.info("ОБНОВЛЕНИЕ 12.");

        try {

            addPsyhoComplexesToTrinity();
            setUpdateVersion(updateOption, 12);

        }catch (Exception ex){
            wrapException("12",ex);
        }
    }
    private void update_in12(ProgramOptions updateOption, int updateFixVersion) {
        if(updateFixVersion == 0) {
            updateIn12_1(updateOption);
            updateIn12_2(updateOption);
            updateIn12_3(updateOption);
            updateIn12_4(updateOption);
        }else if(updateFixVersion == 1) {
            updateIn12_2(updateOption);
            updateIn12_3(updateOption);
            updateIn12_4(updateOption);
        }else if(updateFixVersion == 2) {
            updateIn12_3(updateOption);
            updateIn12_4(updateOption);
        }else if(updateFixVersion == 3) {
            updateIn12_4(updateOption);
        }
    }

    private void updateIn12_4(ProgramOptions updateOption) {
        log.info("ОБНОВЛЕНИЕ 12.4");
        //фикс с округлением частоты
    }

    int _cnt_12_2 =0;
    private void updateIn12_2(ProgramOptions updateOption) {
        //поправим время на частоту в базе частот для новых комплексов тринити, тк в прошлом обновлении они встали в базу не корректно.
        _cnt_12_2++;

        boolean flag =false;
        int cnt=0;

        log.info("ОБНОВЛЕНИЕ 12.2");
        try {
        ResourceUtil en=new ResourceUtil();
        File translateFile = en.saveResource(getTmpDir(),"trinity_new_en_.xmlb",
                "/updates/update12/translate_en_new_trinity.xml",true);

        if(translateFile==null) throw new Exception("Не удалось создать файл для импорта базы");

        LoadUUIDs loadUUIDs =new LoadUUIDs();
            if(!loadUUIDs.parse(Stream.of(translateFile).collect(Collectors.toList()))) throw new Exception("Не обработан файл с UUID");



            for (LoadUUIDs.Complex complex : loadUUIDs.getListComplex()) {

                Complex baseComplex = getModel().findComplex(complex.getUuid());
              if(baseComplex == null){
                  flag =true;
                  cnt++;
                  continue;
              }

                if(complex.getUuid().equals("3b9f960c-e94c-4bfb-9144-44a01205fc29"))baseComplex.setTimeForFreq(180);
                else  if(complex.getUuid().equals("0fda2db8-aab6-4a26-826f-13a4bc95e690"))baseComplex.setTimeForFreq(60);
                else  if(complex.getUuid().equals("e25856a1-a42e-4265-9b87-400d7864fdd2"))baseComplex.setTimeForFreq(60);
                else  baseComplex.setTimeForFreq(30);

                getModel().updateComplex(baseComplex);
            }


        } catch (IOException e) {
            wrapException("12.2",e);
            log.info("ОБНОВЛЕНИЕ 12.2 НЕ ЗАВЕРШЕНО.");

        } catch (Exception e) {
            wrapException("12.2",e);
            log.info("ОБНОВЛЕНИЕ 12.2 НЕ ЗАВЕРШЕНО.");

        }
        if(flag ==true && cnt==38){
            if(_cnt_12_2==2) {
                //исправляет проблему  у тех у кого почему-то не поправилась база на 12.1
                wrapException("12.2",new Exception());
                log.info("ОБНОВЛЕНИЕ 12.2 НЕ ЗАВЕРШЕНО.");
            }
            updateIn12_1(updateOption);
            updateIn12_2(updateOption);
        }

    }


    private void updateIn12_3(ProgramOptions updateOption) {
       //необходима была замена файлов формальная
        log.info("ОБНОВЛЕНИЕ 12.3  ЗАВЕРШЕНО.");

    }

    private void updateIn12_1(ProgramOptions updateOption) {
        System.out.println("updateIn12_1");
        //исправляет ошибку предыдущего обновления - у всех юзеров новые комплексы тринити стали с разными UUID + не встали переводы!!!
        //необходимо поправить UUID для этих комплексов + вставить переводы




        try {

            Section  section = model.findAllSectionByTag("TRINITY");
            if(section == null) throw new Exception("Отсутствует раздел Trinity");

            ResourceUtil en=new ResourceUtil();
            File translateFile = en.saveResource(getTmpDir(),"trinity_new_en.xmlb",
                    "/updates/update12/translate_en_new_trinity.xml",true);

            if(translateFile==null) throw new Exception("Не удалось создать файл для импорта базы");

            LoadUUIDs loadUUIDs =new LoadUUIDs();

            if(loadUUIDs.parse(Stream.of(translateFile).collect(Collectors.toList()))){
            //необходимо найти в базе программы и комплексы в разделе Trinity из списка, полученого из базы по именам

                Map<String, Complex> mapComplexes = new HashMap<>();
                List<Complex> allComplexBySection = getModel().findAllComplexBySection(section);
                getModel().initStringsComplex(allComplexBySection,getModel().getLanguage("ru"));




                for (Complex complex : allComplexBySection) {
                        mapComplexes.put(complex.getNameString(),complex);
                        System.out.println("Комплекс из базы : "+complex.getNameString());
                }

                Map<String, LoadUUIDs.Program> mapPrograms = new HashMap<>();
                for (LoadUUIDs.Program program : loadUUIDs.getListPrograms()) {
                    mapPrograms.put(program.getNameRus(), program);
                }

                for (LoadUUIDs.Complex complex : loadUUIDs.getListComplex()) {
                    System.out.println("Комплекс из Файла : "+complex.getNameRus());
                    if(!mapComplexes.containsKey(complex.getNameRus())) throw new Exception("Не верное сопоставление имен комплексов");
                    Complex baseComplex = mapComplexes.get(complex.getNameRus());
                    baseComplex.setUuid(complex.getUuid());

                    getModel().updateComplex(baseComplex);

                    List<Program> allProgramsBySection = getModel().findAllProgramByComplex(baseComplex);
                    getModel().initStringsProgram(allProgramsBySection, getModel().getLanguage("ru"));
                    if(allProgramsBySection.isEmpty()) throw new Exception("В комплексах нет программ");

                    if(!mapPrograms.containsKey(allProgramsBySection.get(0).getNameString())) throw new Exception("Не верное сопостовление имен программ");
                    LoadUUIDs.Program program = mapPrograms.get(allProgramsBySection.get(0).getNameString());
                    allProgramsBySection.get(0).setUuid(program.getUuid());
                    getModel().updateProgram(allProgramsBySection.get(0));

                }



            }else throw new Exception("Не удалось получить список UUID");


            LoadLanguageFiles llf =new LoadLanguageFiles();
            boolean res =  llf.parse(Stream.of(translateFile).collect(Collectors.toList()), getModel());
            if(res==false) throw new Exception("Ошибка импорта переводов программ");


                log.info("ОБНОВЛЕНИЕ 12.1  ЗАВЕРШЕНО.");


        } catch (IOException e) {
            wrapException("12.1",e);
            log.info("ОБНОВЛЕНИЕ 12.1 НЕ ЗАВЕРШЕНО.");

        } catch (Exception e) {
            wrapException("12.1",e);
            log.info("ОБНОВЛЕНИЕ 12.1 НЕ ЗАВЕРШЕНО.");

        }
    }


    private void updateIn9(ProgramOptions updateOption, int updateFixVersion) {
        //после обновлений минорных не устанавливается число в базу, тк оно внесется по ниже автоматически
        if(updateFixVersion == 0){
            updateIn9_1( updateOption);
            updateIn9_2( updateOption);
            updateIn9_3( updateOption);
        }
        else if(updateFixVersion == 1) {
            updateIn9_2( updateOption);
            updateIn9_3( updateOption);
        }else if(updateFixVersion == 2) updateIn9_3( updateOption);
    }

    private void updateIn10(ProgramOptions updateOption, int updateFixVersion) {
        //после обновлений минорных не устанавливается число в базу, тк оно внесется по ниже автоматически
        if(updateFixVersion == 0){
            updateIn10_1(updateOption);
            updateIn10_2(updateOption);
            updateIn10_3(updateOption);
            updateIn10_4(updateOption);
        }  else if(updateFixVersion == 1) {
            updateIn10_2(updateOption);
            updateIn10_3(updateOption);
            updateIn10_4(updateOption);
        }else if(updateFixVersion == 2) {

            updateIn10_3(updateOption);
            updateIn10_4(updateOption);
        }else if(updateFixVersion == 3) {
            updateIn10_4(updateOption);
        }
    }
    private void updateIn11(ProgramOptions updateOption, int updateFixVersion) {
        //после обновлений минорных не устанавливается число в базу, тк оно внесется по ниже автоматически
        if(updateFixVersion == 0){
            updateIn11_1(updateOption);

        }
    }

    private void updateIn10_1(ProgramOptions updateOption) {
        System.out.println("Update_10_1");
    }
    private void updateIn10_2(ProgramOptions updateOption) {
        System.out.println("Update_10_2");
    }
    private void updateIn10_3(ProgramOptions updateOption) {
        System.out.println("Update_10_3");
    }
    private void updateIn10_4(ProgramOptions updateOption) {
        System.out.println("Update_10_4");
    }

    private void updateIn11_1(ProgramOptions updateOption) {
        System.out.println("Update_11_1");
    }

    private void updateIn9_1(ProgramOptions updateOption) {
        System.out.println("Update_9_1");
        File base_translate=null;
        try {
            ResourceUtil ru=new ResourceUtil();
            base_translate = ru.saveResource(getTmpDir(),"en_translanion_profilactic.xml","/updates/update10/translate_eng.xml",true);

            if(base_translate==null) throw new Exception();

            LoadLanguageFiles ll=new LoadLanguageFiles();
            if( ll.parse(Arrays.asList(base_translate),getModel())){

                log.info("ОБНОВЛЕНИЕ 9.1  ЗАВЕРШЕНО.");
            }
            else  wrapException("9.1",new Exception());

        } catch (IOException e) {
            wrapException("9.1",e);
            log.info("ОБНОВЛЕНИЕ 9.1 НЕ ЗАВЕРШЕНО.");

        } catch (Exception e) {
            wrapException("9.1",e);
            log.info("ОБНОВЛЕНИЕ 9.1 НЕ ЗАВЕРШЕНО.");

        }
    }


    private void updateIn9_2(ProgramOptions updateOption) {
        System.out.println("Update_9_2");
        File base_translate=null;
        try {
            ResourceUtil ru=new ResourceUtil();
            base_translate = ru.saveResource(getTmpDir(),"el_translanion.xml","/updates/update10/translate_el.xml",true);

            if(base_translate==null) throw new Exception();

            LoadLanguageFiles ll=new LoadLanguageFiles();
            if( ll.parse(Arrays.asList(base_translate),getModel())){

                log.info("ОБНОВЛЕНИЕ 9.2  ЗАВЕРШЕНО.");
            }
            else  wrapException("9.2",new Exception());

        } catch (IOException e) {
            wrapException("9.2",e);
            log.info("ОБНОВЛЕНИЕ 9.2 НЕ ЗАВЕРШЕНО.");

        } catch (Exception e) {
            wrapException("9.2",e);
            log.info("ОБНОВЛЕНИЕ 9.2 НЕ ЗАВЕРШЕНО.");

        }
    }
    private void updateIn9_3(ProgramOptions updateOption) {
        System.out.println("Update_9_3");
        File base_translate=null;
        try {
            List<Program> programs = getModel().findAllProgram()
                                              .stream()
                                              .filter(p -> p.isOwnerSystem())
                                              .collect(Collectors.toList());

            for (Program program : programs) {
                String f = program.getFrequencies();
                if(f.contains("+")){
                    System.out.println("Было: "+f);
                    program.setFrequencies(f.replace("+",";"));
                    System.out.println("Стало: "+program.getFrequencies());
                    getModel().updateProgram(program);
                }
            }


        } catch (Exception e) {
            wrapException("9.3",e);
            log.info("ОБНОВЛЕНИЕ 9.3 НЕ ЗАВЕРШЕНО.");

        }
    }

    private void updateIn13(ProgramOptions updateOption, int updateFixVersion){
        if(updateFixVersion == 0 ){
            updateIn13_1(updateOption);
            updateIn13_2(updateOption);
            updateIn13_3(updateOption);
            updateIn13_4(updateOption);
            updateIn13_5(updateOption);
        }
        else if(updateFixVersion == 1 ){
            updateIn13_2(updateOption);
            updateIn13_3(updateOption);
            updateIn13_4(updateOption);
            updateIn13_5(updateOption);
        }else if(updateFixVersion == 2 ) {
            updateIn13_3(updateOption);
            updateIn13_4(updateOption);
            updateIn13_5(updateOption);
        }else if(updateFixVersion == 3 ){
            updateIn13_4(updateOption);
            updateIn13_5(updateOption);
        }else if(updateFixVersion == 4 ){

            updateIn13_5(updateOption);
        }
    }

    private void updateIn13_5(ProgramOptions updateOption) {

    }

    private void updateIn13_4(ProgramOptions updateOption) {
        System.out.println("Update_13_4");
    }

    private void updateIn13_3(ProgramOptions updateOption) {
        System.out.println("Update_13_3");

    }

    private void updateIn13_1(ProgramOptions updateOption) {
        System.out.println("Update_13_1");
        ResourceUtil ru=new ResourceUtil();
        File de=null;
        try {
             de = ru.saveResource(getTmpDir(),"de_transl.xml","/updates/update13/deutsch_tr.xml",true);
        } catch (IOException e) {
            wrapException("13_1",e);
        }


        if(de == null ) wrapException("13_1",new Exception());

        LoadLanguageFiles ll=new LoadLanguageFiles();
        if( ll.parse(Arrays.asList(de),getModel())){

            log.info("ОБНОВЛЕНИЕ 13_! ЗАВЕРШЕНО.");
        }
        else  wrapException("13",new Exception());
    }

    private void updateIn13_2(ProgramOptions updateOption) {
        System.out.println("Update_13_2");
        try {
            rootSectionNames("Nauja dažnių bazė", "Sena dažnių bazė", "lt");
            userSectionName("Naudotojo bazė","lt");

            log.info("ОБНОВЛЕНИЕ 13_2 ЗАВЕРШЕНО.");
        } catch (Exception e) {
            wrapException("13.2",e);
        }
    }


    private void update13(ProgramOptions updateOption) {
        System.out.println("Update_13");
        File italian=null;
        File italian_trin=null;
        File lat=null;

        try {
            ResourceUtil ru=new ResourceUtil();
            italian = ru.saveResource(getTmpDir(),"it_translanion.xml","/updates/update13/italian.xml",true);
            italian_trin = ru.saveResource(getTmpDir(),"it2_translanion.xml","/updates/update13/translate_it_trin.xml",true);
            lat = ru.saveResource(getTmpDir(),"lt_translanion.xml","/updates/update13/translate_base_lt.xml",true);

            if(italian==null || italian_trin == null || lat ==null) throw new Exception();

            LoadLanguageFiles ll=new LoadLanguageFiles();
            if( ll.parse(Arrays.asList(italian, italian_trin, lat),getModel())){

                log.info("ОБНОВЛЕНИЕ 13 ЗАВЕРШЕНО.");
            }
            else  wrapException("13",new Exception());

            setUpdateVersion(updateOption, 13);

        } catch (IOException e) {
            wrapException("13",e);
            log.info("ОБНОВЛЕНИЕ 13 НЕ ЗАВЕРШЕНО.");

        } catch (Exception e) {
            wrapException("13",e);
            log.info("ОБНОВЛЕНИЕ 13 НЕ ЗАВЕРШЕНО.");

        }
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
            log.error("",e);
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

        log.info("ОБНОВЛЕНИЕ 1.");
        try
        {
            log.info("Проверка наличия столбца BUNDLESLENGTH  в THERAPYCOMPLEX ");
            Object singleResult = emf.createEntityManager().createNativeQuery("SELECT BUNDLESLENGTH FROM THERAPYCOMPLEX LIMIT 1").getSingleResult();
            log.info("Столбец  BUNDLESLENGTH  найден.");
        }catch (Exception e){
            log.info("Столбец  BUNDLESLENGTH не найден.");
            log.info("Создается  столбец BUNDLESLENGTH  в THERAPYCOMPLEX ");
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            try{
                em.createNativeQuery("ALTER TABLE THERAPYCOMPLEX ADD BUNDLESLENGTH INT DEFAULT 1").executeUpdate();
                em.getTransaction().commit();
                log.info("Столбец  BUNDLESLENGTH создан.");
            }catch (Exception ex){
                throw new RuntimeException("Не удалось выполнить ALTER TABLE THERAPYCOMPLEX ADD BUNDLESLENGTH INT ");
            }finally {
                if(em!=null) em.close();
            }


        }


        try
        {
            //столбец связан с языком вставки комплексов
            log.info("Проверка наличия столбца ONAME  в THERAPYCOMPLEX ");
            Object singleResult = emf.createEntityManager().createNativeQuery("SELECT ONAME FROM THERAPYCOMPLEX LIMIT 1").getSingleResult();
            log.info("Столбец  ONAME  найден.");
        }catch (Exception e){
            log.info("Столбец  ONAME не найден.");
            log.info("Создается  столбец ONAME  в THERAPYCOMPLEX ");
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            try{
                em.createNativeQuery("ALTER TABLE THERAPYCOMPLEX ADD ONAME VARCHAR(255) DEFAULT ''").executeUpdate();
                em.getTransaction().commit();
                log.info("Столбец  ONAME создан.");
            }catch (Exception ex){
                throw new RuntimeException("Не удалось выполнить ALTER TABLE THERAPYCOMPLEX ADD ONAME VARCHAR(255) DEFAULT ''");
            }finally {
                if(em!=null) em.close();
            }


        }

        try
        {
            //столбец связан с языком вставки комплексов
            log.info("Проверка наличия столбца ONAME  в THERAPYPROGRAM ");
            Object singleResult = emf.createEntityManager().createNativeQuery("SELECT ONAME FROM THERAPYPROGRAM LIMIT 1").getSingleResult();
            log.info("Столбец  ONAME  найден.");
        }catch (Exception e){
            log.info("Столбец  ONAME не найден.");
            log.info("Создается  столбец ONAME  в THERAPYPROGRAM ");
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            try{
                em.createNativeQuery("ALTER TABLE THERAPYPROGRAM ADD ONAME VARCHAR(255) DEFAULT ''").executeUpdate();
                em.getTransaction().commit();
                log.info("Столбец  ONAME создан.");
            }catch (Exception ex){
                throw new RuntimeException("Не удалось выполнить ALTER TABLE THERAPYPROGRAM ADD ONAME VARCHAR(255) DEFAULT ''");
            }finally {
                if(em!=null) em.close();
            }


        }

        setUpdateVersion(updateOption,1);//установим новую версию обновления

        log.info("ОБНОВЛЕНИЕ 1 ЗАВЕРШЕНО.");
    }


    private void update2(ProgramOptions updateOption) {
        log.info("ОБНОВЛЕНИЕ 2.");


        setUpdateVersion(updateOption,2);//установим новую версию обновления

        log.info("ОБНОВЛЕНИЕ 2 ЗАВЕРШЕНО.");







    }

    private void update3(ProgramOptions updateOption) {
        log.info("ОБНОВЛЕНИЕ 3.");
        //основное изменение в базе - обновление для фр. языка.
        //сделается из файла


        try {
            ResourceUtil ru = new ResourceUtil();
            File base_translate = ru.saveResource(getTmpDir(),
                    "fr_translanion_bas.xml",
                    "/updates/update3/fr_translanion_bas.xml",
                    true);

            if (base_translate == null) throw new Exception();

            LoadLanguageFiles ll = new LoadLanguageFiles();
            if (ll.parse(Arrays.asList(base_translate), getModel())) {

                setUpdateVersion(updateOption, 3);//установим новую версию обновления
                log.info("ОБНОВЛЕНИЕ 3  ЗАВЕРШЕНО.");

            } else   wrapException("3",  new Exception());
        } catch (IOException e) {
            wrapException("3",  e);
        } catch (Exception e) {
            wrapException("3",  e);
        }



    }

    private void wrapException(String msg, Exception e){
        log.error("",e);
        throw new RuntimeException(msg, e);
    }

    private void showUpdateErrorAndExit(String number){
        setNeedExit(true);
        Platform.runLater(() -> {
            UpdateWaiter.close();
            BaseController.showErrorDialog("Обновление "+number,"","Обновление не установленно",null,Modality.WINDOW_MODAL);

        } );


    }

    private void update4(ProgramOptions updateOption) {
        log.info("ОБНОВЛЕНИЕ 4.");
        //основное изменение в базе - обновление для итальянского. языка.
        //сделается из файла
                File base_translate=null;
                try {
                    ResourceUtil ru=new ResourceUtil();
                    base_translate = ru.saveResource(getTmpDir(),"it_translanion_bas.xml","/updates/update4/it_base.xml",true);

                    if(base_translate==null) throw new Exception();

                    LoadLanguageFiles ll=new LoadLanguageFiles();
                    if( ll.parse(Arrays.asList(base_translate),getModel())){


                        setUpdateVersion(updateOption,4);//установим новую версию обновления
                        log.info("ОБНОВЛЕНИЕ 4  ЗАВЕРШЕНО.");
                    }
                    else  wrapException("4",new Exception());

                } catch (IOException e) {
                   wrapException("4",e);
                    log.info("ОБНОВЛЕНИЕ 4 НЕ ЗАВЕРШЕНО.");

                } catch (Exception e) {
                    wrapException("4",e);
                    log.info("ОБНОВЛЕНИЕ 4 НЕ ЗАВЕРШЕНО.");

                }
    }


    private void update5(ProgramOptions updateOption)
    {
        log.info("ОБНОВЛЕНИЕ 5.");
        try {
            log.info("Добавление раздела Trinity");
            addTrinityBase();
            log.info("Добавление раздела Trinity --- успешно");

        } catch (Exception ex) {
            log.error("Не удалось выполнить добавление Trinity",ex);
            wrapException("5",ex);
        }

                setUpdateVersion(updateOption,5);
    }

    private void update6(ProgramOptions updateOption)
    {
        log.info("ОБНОВЛЕНИЕ 6.");




                EntityManager em = emf.createEntityManager();
                em.getTransaction().begin();
                try
                {
                    log.info("Обновление BUNDLESLENGTH  ");

                    int res = em.createNativeQuery("UPDATE  THERAPYCOMPLEX SET  BUNDLESLENGTH = 3  WHERE BUNDLESLENGTH = 1").executeUpdate();
                    em.getTransaction().commit();
                    log.info("BUNDLESLENGTH обновлен. Обновлено "+res+" значений");

                }catch (Exception e){
                    log.error(" Ошибка обновления BUNDLESLENGTH",e);
                    wrapException("6",e);
                }finally {
                    if(em!=null) em.close();
                }

                try {

                    //   d8652bff-a090-451f-bcef-6380195ad2f5 альфа
                    //   c5175f11-cd07-484e-b63c-6b6bd0fcb928    7.84

                    Program beta=   getModel().findProgram("c5175f11-cd07-484e-b63c-6b6bd0fcb928");
                    Program alfa=   getModel().findProgram("d8652bff-a090-451f-bcef-6380195ad2f5");
                    if(beta==null || alfa==null){
                        log.error("не найдены комплексы alfa и beta");
                    }else {
                        if(beta.getFrequencies().equals("7.84")){
                            log.info("Обмен значений альфаи бета ритмов");
                            beta.setFrequencies(alfa.getFrequencies());
                            alfa.setFrequencies("7.84");
                            getModel().updateProgram(alfa);
                            getModel().updateProgram(beta);
                        }

                    }




                    log.info("Обновление раздела Trinity");
                    //первый вариант добавления забраковали. Далее используется обновленная версия.
                    //Эта версия обновит если уже было добавлено не верно или создаст верный вариант. При кумулятивном обновлении сработает в update5
                    addTrinityBase();
                    log.info("Обновление раздела Trinity --- успешно");

                } catch (Exception ex) {
                    log.error("Не удалось выполнить обновление Trinity",ex);
                    wrapException("6",ex);
                }


                setUpdateVersion(updateOption,6);




    }
    private void update7(ProgramOptions updateOption)
    {
        log.info("ОБНОВЛЕНИЕ 7.");

            try {
                Language ru = model.getLanguage("ru");

                //при обновлении 6 забыта установка поля ownerSystem!! В методе update6 исправлено, но для тех у кого оно уже стояло нужен этот код.
                //также был установлен не верный язык(пользовательский вместо ru)
                Section section = model.findAllSectionByTag("TRINITY");
                if (section != null) {
                    Strings nameStringTrin = section.getName();
                    LocalizedString enName = model.getLocalString(nameStringTrin, model.getDefaultLanguage());
                    if (enName == null) model.addString(nameStringTrin, "Trinity", model.getDefaultLanguage());
                    LocalizedString localString;
                    for (Complex complex : model.findAllComplexBySection(section)) {
                        localString = model.getLocalString(complex.getName(), ru);
                        if (localString == null) {
                            localString = model.getLocalString(complex.getName(), model.getUserLanguage());
                        }
                        if (localString != null) {
                            localString.setLanguage(ru);
                            model.updateLocalString(localString);
                        }
                        localString = model.getLocalString(complex.getDescription(), ru);
                        if (localString == null) {
                            localString = model.getLocalString(complex.getDescription(), model.getUserLanguage());
                        }
                        if (localString != null) {
                            localString.setLanguage(ru);
                            model.updateLocalString(localString);
                        }

                        complex.setOwnerSystem(true);
                        model.updateComplex(complex);
                        for (Program program : model.findAllProgramByComplex(complex)) {
                            localString = model.getLocalString(program.getName(), ru);
                            if (localString == null) {
                                localString = model.getLocalString(program.getName(), model.getUserLanguage());
                            }
                            if (localString != null) {
                                localString.setLanguage(ru);
                                model.updateLocalString(localString);
                            }

                            localString = model.getLocalString(program.getDescription(), ru);
                            if (localString == null) {
                                localString = model.getLocalString(program.getDescription(), model.getUserLanguage());
                            }
                            if (localString != null) {
                                localString.setLanguage(ru);
                                model.updateLocalString(localString);
                            }
                            if (!program.isOwnerSystem()) {
                                program.setOwnerSystem(true);
                                model.updateProgram(program);
                            }

                        }


                    }
                }
                String[] fileUUIDs = new String[]{
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
                String[] fixedUUID = new String[]{
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

                Map<String, String> transMap = new HashMap<>();


                //замена на точные старые UUID, соответствуют файлам перевода
                int j = 0;
                for (Complex complex : model.findAllComplexBySection(section)) {
                    complex.setUuid(fileUUIDs[j++]);
                    model.updateComplex(complex);
                    for (Program program : model.findAllProgramByComplex(complex)) {
                        program.setUuid(fileUUIDs[j++]);
                        model.updateProgram(program);
                    }
                }

                //замена UUID в TrinityBase
                j = 0;
                section.setUuid("e10ffe0a-1064-4c02-ad32-c3cf15ea958b");
                getModel().updateSection(section);

                //сначала все программы, потом комплексы не иначе, тк это порядок обработки файла перевода!!
                for (Complex complex : model.findAllComplexBySection(section)) {
                    for (Program program : model.findAllProgramByComplex(complex)) {
                        transMap.put(program.getUuid(), fixedUUID[j]);
                        program.setUuid(fixedUUID[j++]);
                        model.updateProgram(program);
                        //System.out.println("Program" + program.getId());
                    }
                }

                for (Complex complex : model.findAllComplexBySection(section)) {
                    transMap.put(complex.getUuid(), fixedUUID[j]);
                    complex.setUuid(fixedUUID[j++]);
                    model.updateComplex(complex);
                    // System.out.println("complex" + complex.getId());

                }


                setTranslate("/updates/update7/trinity_fr_trans.xml", "fr", transMap);
                setTranslate("/updates/update7/trinity_it_trans.xml", "it", transMap);
                setTranslate("/updates/update7/trinity_en_trans.xml", "en", transMap);
                setTranslate("/updates/update7/trinity_el_trans.xml", "el", transMap);


                rootSectionNames("La Nuova base delle  frequenze", "La Vecchia base delle frequenze", "it");
                rootSectionNames("Base de nouvelles fréquences", "Base de fréquences anciennes", "fr");
                rootSectionNames("Neue Frequenzen Basis", "Alte Frequenzen Basis", "de");
                rootSectionNames("Βάση νέων συχνοτήτων", "Βάση παλαιών συχνοτήτων", "el");


                //заменить частоты в программах
                // a6e99500-ac4a-486a-98e6-d215dab68afd  2;12;26;26.5;66;75.5;94;95.5
                //  8d8a4c44-1e35-4ec9-a653-ec46a7b72804  6.3;6.5;23.5;60.5;61.5;63;64.5;67
                //  8634ffa9-371b-499f-b64f-00f2ea043fee  1550;880;802;800;787;727;672;444
                //  f81443c3-1ad8-4f8a-9a73-d57fdf691839 332;698;721;732;749;752;942;991.5;1026.2;3212;4412

                Program p1 = getModel().findProgram("a6e99500-ac4a-486a-98e6-d215dab68afd");
                Program p2 = getModel().findProgram("8d8a4c44-1e35-4ec9-a653-ec46a7b72804");
                Program p3 = getModel().findProgram("8634ffa9-371b-499f-b64f-00f2ea043fee");
                Program p4 = getModel().findProgram("f81443c3-1ad8-4f8a-9a73-d57fdf691839");
                Program p5 = getModel().findProgram("160ca4f8-ca82-4b7e-8dbe-311d993bf7af");

                if (p1 == null || p2 == null || p3 == null || p4 == null || p5 == null) {
                    log.error("не найдены программы для обновления частот");
                } else {
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

                setLangNameForSection("6a5d84b9-97b0-415b-9c69-ac1dca3dcad3", "Autorenkomplexe", "de");
                setLangNameForSection("c1ff40dd-0e9d-413e-a9d9-d613eeeb8f2d", "General Programme", "de");
                setLangNameForSection("15cf155a-cc24-49ed-98f1-a6c3fdec3539",
                        "Frequenzen der chemischen Elemente",
                        "de");
                setLangNameForSection("d1a0b290-ccee-4fd9-b635-6861b8a508a7", "Vorbeugende komplexe", "de");
                setLangNameForSection("600886d6-5c2b-4a0b-b974-d151f1125c42", "Sätze von Programmen", "de");
                setLangNameForSection("205967cf-02fa-4e96-ac00-a19e5dd2a3fb", "Antiparasitäre", "de");
                setLangNameForSection("db31c890-3500-4dee-84f3-8c49a7518112", "Benutzerbasis", "de");

                setUpdateVersion(updateOption, 7);

            }catch (Exception e){
                wrapException("7",e);
            }


    }
    private void update8(ProgramOptions updateOption)
    {
        log.info("ОБНОВЛЕНИЕ 8.");

                try {
                    Program p5 = getModel().findProgram("160ca4f8-ca82-4b7e-8dbe-311d993bf7af");

                    if (p5 == null) {
                        log.error("не найдены программы для обновления частот");
                    } else {
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
                    setLangNameForSection("6a5d84b9-97b0-415b-9c69-ac1dca3dcad3", "Autorenkomplexe", "de");
                    setLangNameForSection("c1ff40dd-0e9d-413e-a9d9-d613eeeb8f2d", "General Programme", "de");
                    setLangNameForSection("15cf155a-cc24-49ed-98f1-a6c3fdec3539",
                            "Frequenzen der chemischen Elemente",
                            "de");
                    setLangNameForSection("d1a0b290-ccee-4fd9-b635-6861b8a508a7", "Vorbeugende komplexe", "de");
                    setLangNameForSection("600886d6-5c2b-4a0b-b974-d151f1125c42", "Sätze von Programmen", "de");
                    setLangNameForSection("205967cf-02fa-4e96-ac00-a19e5dd2a3fb", "Antiparasitäre", "de");
                    setLangNameForSection("db31c890-3500-4dee-84f3-8c49a7518112", "Benutzerbasis", "de");

                    setUpdateVersion(updateOption, 8);

                }catch (Exception ex){
                    wrapException("8",ex);
                }


}

    private void update9(ProgramOptions updateOption) {
        log.info("ОБНОВЛЕНИЕ 9.");

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


                }catch (Exception ex){
                    log.error("Вероятно предыдущие обновления меняющие UUID прошли некорректно",ex);
                    wrapException("9",ex);
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

    }
    private void update10(ProgramOptions updateOption)
    {
        log.info("ОБНОВЛЕНИЕ 10.");

        try {

            setUpdateVersion(updateOption, 10);

        }catch (Exception ex){
            wrapException("10",ex);
        }


    }
    private void update11(ProgramOptions updateOption)
    {
        log.info("ОБНОВЛЕНИЕ 11.");

        try {

            setUpdateVersion(updateOption, 11);

        }catch (Exception ex){
            wrapException("11",ex);
        }


    }

    private void changeDDL(){
        try
        {
            log.info("Проверка наличия столбца MULTYFREQ  в THERAPYPROGRAM ");
            emf.createEntityManager().createNativeQuery("SELECT MULTYFREQ FROM THERAPYPROGRAM LIMIT 1").getResultList();
            log.info("Столбец  MULTYFREQ  найден.");
        }catch (Exception e){
            log.error("Поиск столбца", e);
            log.info("Столбец  MULTYFREQ не найден.");
            log.info("Создается  столбец MULTYFREQ  в THERAPYPROGRAM ");
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            try{
                em.createNativeQuery("ALTER TABLE THERAPYPROGRAM ADD MULTYFREQ BOOLEAN(1) DEFAULT 1").executeUpdate();
                em.getTransaction().commit();
                log.info("Столбец  MULTYFREQ создан.");


                log.info("Столбец  MULTYFREQ обновлен.");



            }catch (Exception ex){
                log.error("ошибка обновления ALTER TABLE THERAPYPROGRAM ADD MULTYFREQ BOOLEAN(1) DEFAULT 1",ex);
                wrapException("updateDDL",e);
            }finally {
                if(em!=null) em.close();
            }


        }
        reopenPersistentContext();


        boolean tpPosFinded=false;
        boolean profPosFinded=false;
        boolean mFreqFinded=false;
        try
        {
            log.info("Проверка наличия столбца MULLTYFREQ  в THERAPYCOMPLEX ");
            emf.createEntityManager().createNativeQuery("SELECT MULLTYFREQ FROM THERAPYCOMPLEX LIMIT 1").getResultList();
            log.info("Столбец  MULLTYFREQ  найден.");
            mFreqFinded=true;
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            try{
                em.createNativeQuery("ALTER TABLE THERAPYCOMPLEX DROP `MULLTYFREQ`").executeUpdate();
                em.getTransaction().commit();
                log.info("Столбец  MULLTYFREQ удален.");


            }catch (Exception ex){
                log.error("ошибка обновления ALTER TABLE THERAPYCOMPLEX DROP `MULLTYFREQ`",ex);
                wrapException("6",ex);
            }finally {
                if(em!=null) em.close();
            }

        }catch (Exception e){
        }

        try
        {
            log.info("Проверка наличия столбца POSITION  в THERAPYCOMPLEX ");
            emf.createEntityManager().createNativeQuery("SELECT `POSITION` FROM THERAPYCOMPLEX LIMIT 1").getResultList();
            log.info("Столбец  POSITION  найден.");
            tpPosFinded=true;
        }catch (Exception e){
            log.info("Столбец  POSITION не найден.");
            log.info("Создается  столбец POSITION  в THERAPYPROGRAM ");
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            try{
                em.createNativeQuery("ALTER TABLE THERAPYCOMPLEX ADD `POSITION` BIGINT(19) DEFAULT 1").executeUpdate();
                em.getTransaction().commit();
                log.info("Столбец  POSITION создан.");



            }catch (Exception ex){
                log.error("ошибка обновления ALTER TABLE THERAPYCOMPLEX ADD `POSITION` BIGINT(19) DEFAULT 1",ex);
                wrapException("6",ex);
            }finally {
                if(em!=null) em.close();
            }


        }
        reopenPersistentContext();
        try
        {
            log.info("Проверка наличия столбца POSITION  в PROFILE ");
            emf.createEntityManager().createNativeQuery("SELECT `POSITION` FROM PROFILE LIMIT 1").getResultList();
            log.info("Столбец  POSITION  найден.");
            profPosFinded=true;
        }catch (Exception e){
            log.info("Столбец  POSITION не найден.");
            log.info("Создается  столбец POSITION  в PROFILE ");
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            try{
                em.createNativeQuery("ALTER TABLE PROFILE ADD `POSITION` BIGINT(19) DEFAULT 1").executeUpdate();
                em.getTransaction().commit();
                log.info("Столбец  POSITION создан.");

            }catch (Exception ex){
                log.error("ошибка обновления ALTER TABLE PROFILE ADD `POSITION` BIGINT(19) DEFAULT 1",ex);
                wrapException("6",ex);
            }finally {
                if(em!=null) em.close();
            }


        }

        reopenPersistentContext();
        try
        {
            log.info("Проверка наличия столбца TIMEFORFREQ  в COMPLEX ");
            emf.createEntityManager().createNativeQuery("SELECT `TIMEFORFREQ` FROM COMPLEX LIMIT 1").getResultList();
            log.info("Столбец  TIMEFORFREQ  найден.");

        }catch (Exception e) {
            log.info("Столбец  TIMEFORFREQ не найден.");
            log.info("Создается  столбец TIMEFORFREQ  в COMPLEX ");
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            try {
                em.createNativeQuery("ALTER TABLE COMPLEX ADD `TIMEFORFREQ` INT DEFAULT 0").executeUpdate();
                em.getTransaction().commit();
                log.info("Столбец  TIMEFORFREQ создан.");


            } catch (Exception ex) {
                log.error("ALTER TABLE COMPLEX ADD `TIMEFORFREQ` INT DEFAULT 0", ex);
                wrapException("9",ex);
            } finally {
                if (em != null) em.close();
            }
        }

        reopenPersistentContext();

        try
        {
            log.info("Проверка наличия столбца SRCUUID  в THERAPYCOMPLEX ");
            emf.createEntityManager().createNativeQuery("SELECT `SRCUUID` FROM THERAPYCOMPLEX LIMIT 1").getResultList();
            log.info("Столбец  SRCUUID  найден.");

        }catch (Exception e){
            log.info("Столбец  SRCUUID не найден.");
            log.info("Создается  столбец SRCUUID  в THERAPYPROGRAM ");
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            try{
                em.createNativeQuery("ALTER TABLE THERAPYCOMPLEX ADD `SRCUUID` VARCHAR(255) DEFAULT ''").executeUpdate();
                em.getTransaction().commit();
                log.info("Столбец  SRCUUID создан.");



            }catch (Exception ex){
                log.error("ошибка обновления ALTER TABLE THERAPYCOMPLEX ADD `SRCUUID` VARCHAR(255) DEFAULT ''",ex);
                wrapException("10",ex);
            }finally {
                if(em!=null) em.close();
            }


        }
        reopenPersistentContext();
        try
        {
            log.info("Проверка наличия столбца SRCUUID  в THERAPYPROGRAM ");
            emf.createEntityManager().createNativeQuery("SELECT `SRCUUID` FROM THERAPYPROGRAM LIMIT 1").getResultList();
            log.info("Столбец  SRCUUID  найден.");

        }catch (Exception e){
            log.info("Столбец  SRCUUID не найден.");
            log.info("Создается  столбец SRCUUID  в THERAPYPROGRAM ");
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            try{
                em.createNativeQuery("ALTER TABLE THERAPYPROGRAM ADD `SRCUUID` VARCHAR(255) DEFAULT ''").executeUpdate();
                em.getTransaction().commit();
                log.info("Столбец  SRCUUID создан.");



            }catch (Exception ex){
                log.error("ошибка обновления ALTER TABLE THERAPYPROGRAM ADD `SRCUUID` VARCHAR(255) DEFAULT ''",ex);
                wrapException("10",ex);
            }finally {
                if(em!=null) em.close();
            }
        }
        reopenPersistentContext();

        boolean lastChange = false;
        try
        {
            log.info("Проверка наличия столбца LASTCHANGE  в PROFILE ");
            emf.createEntityManager().createNativeQuery("SELECT `LASTCHANGE` FROM PROFILE LIMIT 1").getResultList();
            log.info("Столбец  LASTCHANGE  найден.");

        }catch (Exception e){
            log.info("Столбец  LASTCHANGE не найден.");
            log.info("Создается  столбец LASTCHANGE  в PROFILE ");
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            try{
                em.createNativeQuery("ALTER TABLE PROFILE ADD `LASTCHANGE` BIGINT(19) DEFAULT 1").executeUpdate();
                em.getTransaction().commit();
                log.info("Столбец  LASTCHANGE создан.");
                lastChange =true;

            }catch (Exception ex){
                log.error("ошибка обновления ALTER TABLE PROFILE ADD `LASTCHANGE` BIGINT(19) DEFAULT 1",ex);
                wrapException("6",ex);
            }finally {
                if(em!=null) em.close();
            }


        }
        reopenPersistentContext();
        boolean timeAddToProfile = false;
        try
        {
            log.info("Проверка наличия столбца TIME  в PROFILE ");
            emf.createEntityManager().createNativeQuery("SELECT `TIME` FROM PROFILE LIMIT 1").getResultList();
            log.info("Столбец  TIME  найден.");

        }catch (Exception e){
            log.info("Столбец  TIME не найден.");
            log.info("Создается  столбец TIME  в PROFILE ");
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            try{
                em.createNativeQuery("ALTER TABLE PROFILE ADD `TIME` BIGINT(19) DEFAULT 0").executeUpdate();
                em.getTransaction().commit();
                log.info("Столбец  TIME создан.");
                timeAddToProfile=true;
            }catch (Exception ex){
                log.error("ошибка обновления ALTER TABLE PROFILE ADD `TIME` BIGINT(19) DEFAULT 1",ex);
                wrapException("14",ex);
            }finally {
                if(em!=null) em.close();
            }


        }


        reopenPersistentContext();
        boolean weightAddToProfile = false;
        try
        {
            log.info("Проверка наличия столбца PROFILEWEIGHT  в PROFILE ");
            emf.createEntityManager().createNativeQuery("SELECT `PROFILEWEIGHT` FROM PROFILE LIMIT 1").getResultList();
            log.info("Столбец  PROFILEWEIGHT  найден.");

        }catch (Exception e){
            log.info("Столбец  PROFILEWEIGHT не найден.");
            log.info("Создается  столбец PROFILEWEIGHT  в PROFILE ");
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            try{
                em.createNativeQuery("ALTER TABLE PROFILE ADD `PROFILEWEIGHT` BIGINT(19) DEFAULT 0").executeUpdate();
                em.getTransaction().commit();
                log.info("Столбец  PROFILEWEIGHT создан.");
                weightAddToProfile=true;
            }catch (Exception ex){
                log.error("ошибка обновления ALTER TABLE PROFILE ADD `PROFILEWEIGHT` BIGINT(19) DEFAULT 1",ex);
                wrapException("14",ex);
            }finally {
                if(em!=null) em.close();
            }


        }

        ///////////apdateactions должны быть в конце иначе будут при дальнейших обновлениях ошибка - типа не найден стоблец, тк модель последняя, а структура базы нет.



        try {
            long pos;
            if(!tpPosFinded) {
                for (TherapyComplex tc : getModel().findAllTherapyComplexes()) {

                    pos = tc.getId();
                    tc.setPosition(pos);
                    getModel().updateTherapyComplex(tc);

                }
                log.info("Столбец  POSITION TherapyComplex обновлен.");
            }
            if(!profPosFinded) {
                for (Profile profile : getModel().findAllProfiles()) {

                    pos = profile.getId();
                    profile.setPosition(pos);
                    getModel().updateProfile(profile);

                }
                log.info("Столбец  POSITION Profile обновлен.");
            }

        }catch (Exception e){
            log.error("ошибка обновления POSITION",e);
            wrapException("6",e);
        }




        reopenPersistentContext();
        try {
            if(lastChange){
                //установить время на текущее со сдвигом в 1 сек согласно позициям
                Calendar cal  = Calendar.getInstance();
                long currentTime = cal.getTimeInMillis();
                List<Profile> allProfiles = getModel().findAllProfiles();
                int allProfilesLen = allProfiles.size();

                for (Profile profile : allProfiles) {
                    profile.setLastChange(currentTime + allProfilesLen*1000);

                    getModel().updateProfile(profile);

                    allProfilesLen--;
                }

            }
        } catch (Exception e) {
            wrapException("6",e);
            log.error("ошибка обновления `LASTCHANGE`, не удалось обновить профили",e);
            e.printStackTrace();
        }




        //обновить все профили, подсчитав их время.
        if(timeAddToProfile){
            try{
                for (Profile profile : model.findAllProfiles()) {
                    model.updateTimeProfile(profile);
                }
            }catch (Exception e){
                log.error("ошибка обновления времени профилей",e);
                wrapException("14",e);
            }


        }

//обновить все профили, подсчитав их время.
        if( weightAddToProfile){
            try{
                for (Profile profile : model.findAllProfiles()) {

                    profile.setProfileWeight(calcProfileFilesWeight(profile));
                    model.updateProfile(profile);
                }
            }catch (Exception e){
                log.error("ошибка обновления размера файлов профилей",e);
                wrapException("14",e);
            }


        }

    }



    public long calcProfileFilesWeight(Profile profile){
        File f = null;
        long summ = 0;
        for (Long v : getModel().getProfileFiles(profile)) {
            f = new File(getDataDir(), v + ".dat");
            if (f.exists()) summ += f.length() ;
        }
        for (String v : getModel().mp3ProgramPathsInProfile(profile)) {

            f = new File(v);
            if (f.exists()) summ += f.length();
        }
        return summ;
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

    private void userSectionName(String newName,String langAbbr) throws Exception {

        Section userSection = model.findSection("db31c890-3500-4dee-84f3-8c49a7518112");
        Strings nameString = userSection.getName();
        LocalizedString lName = model.getLocalString(nameString, model.getLanguage(langAbbr));
        if(lName==null) model.addString(nameString,newName,model.getLanguage(langAbbr));
        else {
            lName.setContent(newName);
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

    private void addPsyhoComplexesToTrinity() throws Exception {
        Section  section = model.findAllSectionByTag("TRINITY");
        if(section == null) throw new Exception("Отсутствует раздел Trinity");

        Complex complex = getModel().findComplex("440f8acf-6156-457f-908f-a4106915bcaa");
        if(complex==null){
            ImportUserBase iUB=new ImportUserBase();

            ResourceUtil ru=new ResourceUtil();
            File trinityBaseFile = ru.saveResource(getTmpDir(),"trinity_new.xmlb",
                    "/updates/update12/new_complexes.xmlb",true);

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
                throw new Exception("Ошибка импорта программ");
            }

        }
/*
        LoadLanguageFiles llf =new LoadLanguageFiles();

        ResourceUtil en=new ResourceUtil();
        File translateFile = en.saveResource(getTmpDir(),"trinity_new_en.xmlb",
                "/updates/update12/translate_en_new_trinity.xml",true);

        if(translateFile==null) throw new Exception("Не удалось создать файл для импорта базы");
       boolean res =  llf.parse(Stream.of(translateFile).collect(Collectors.toList()), getModel());
       if(res==false) throw new Exception("Ошибка импорта переводов программ");

       //не нужно, тк переводы вставятся в обновлении 12.1 а эти переводы не могут встать тк будет несовподение UUID
       */
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


    private static Version starterVersion;

    public static Version getStarterVersion(){
        return starterVersion;
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
        if( AutoUpdater.isIDEStarted()) System.setProperty("starter.version","1.0.0");
        String starter_version = System.getProperty("starter.version");
        if(starter_version==null) starterVersion=new Version(5000,0,0);
        else {
            String[] split = starter_version.split("\\.");
            if(split.length!=3) {
                starterVersion=new Version(5000,0,0);
            }else {
                starterVersion=new Version(Integer.valueOf(split[0]),Integer.valueOf(split[1]),Integer.valueOf(split[2]));
            }
        }

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
