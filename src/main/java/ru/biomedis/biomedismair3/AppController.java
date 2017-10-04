package ru.biomedis.biomedismair3;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import ru.biomedis.biomedismair3.Layouts.BiofonTab.BiofonTabController;
import ru.biomedis.biomedismair3.Layouts.BiofonTab.BiofonUIUtil;
import ru.biomedis.biomedismair3.Layouts.LeftPanel.LeftPanelAPI;
import ru.biomedis.biomedismair3.Layouts.LeftPanel.TreeActionListener;
import ru.biomedis.biomedismair3.Layouts.ProgressPanel.ProgressAPI;
import ru.biomedis.biomedismair3.UpdateUtils.FrequenciesBase.*;
import ru.biomedis.biomedismair3.UserUtils.CreateBaseHelper;
import ru.biomedis.biomedismair3.UserUtils.Export.ExportProfile;
import ru.biomedis.biomedismair3.UserUtils.Export.ExportTherapyComplex;
import ru.biomedis.biomedismair3.UserUtils.Export.ExportUserBase;
import ru.biomedis.biomedismair3.UserUtils.Import.ImportProfile;
import ru.biomedis.biomedismair3.UserUtils.Import.ImportTherapyComplex;
import ru.biomedis.biomedismair3.UserUtils.Import.ImportUserBase;
import ru.biomedis.biomedismair3.entity.*;
import ru.biomedis.biomedismair3.utils.Disk.DiskDetector;
import ru.biomedis.biomedismair3.utils.Files.ZIPUtil;
import ru.biomedis.biomedismair3.utils.Text.TextUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static ru.biomedis.biomedismair3.Log.logger;

public class AppController  extends BaseController {

    public static final int MAX_BUNDLES=7;

    @FXML private SplitPane splitOuter;



    @FXML private TabPane therapyTabPane;


    @FXML private AnchorPane leftLayout;
    @FXML private AnchorPane biofonTabContent;
    @FXML private Menu updateBaseMenu;




    private SimpleBooleanProperty connectedDevice =new SimpleBooleanProperty(false);//подключено ли устройство

    private static TabPane _therapyTabPane;

    private  ResourceBundle res;

    private boolean stopGCthread=false;

    private static M2UI m2ui;

    //сборщик мусора. Собирает мусор периодически, тк мы много объектов создаем при построении дерева
    //нужно его отключать вкогда плодим файлы!!!!!
    private Thread gcThreadRunner;


private HBox containerProgress;

    private static  LeftPanelAPI leftAPI;
    private static  BiofonUIUtil biofonUIUtil;
    private static  ProgressAPI progressAPI;


    public static ProgressAPI getProgressAPI() {
        return progressAPI;
    }
    public static M2UI getM2UI() {
        return m2ui;
    }

    public static LeftPanelAPI getLeftAPI() {
        return leftAPI;
    }

    public static BiofonUIUtil getBiofonUIUtil() {
        return biofonUIUtil;
    }


    synchronized   public boolean isStopGCthread() {
        return stopGCthread;
    }

    synchronized  public void setStopGCthread() {
        this.stopGCthread = true;
    }

    public boolean getConnectedDevice() {
        return connectedDevice.get();
    }
    public SimpleBooleanProperty connectedDeviceProperty() {
        return connectedDevice;
    }
    public void setConnectedDevice(boolean connectedDevice) {
        this.connectedDevice.set(connectedDevice);
    }


    @Override
    public void initialize(URL url, ResourceBundle rb) {

        res = rb;

        _therapyTabPane = therapyTabPane;

        updateBaseMenu.setVisible(getApp().isUpdateBaseMenuVisible());

        initVerticalDivider();


        initGCRunner();
        getApp().addCloseApplistener(() -> {
            setStopGCthread();

        });


        leftAPI = initLeftPanel();
        biofonUIUtil = initBiofon();
        progressAPI = initProgressPanel(containerProgress);

        initSectionTreeActionListener();
    }




    private ProgressAPI initProgressPanel(HBox container){

        try{
          return (ProgressAPI) addContentHBox("/fxml/ProgressPane.fxml",container);
        }catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка инициализации панели програесса ",e);
        }

    }


    public void importTherapyComplex(Profile profile, Consumer<Integer> afterAction)
    {
        if(profile==null)return;

//получим путь к файлу.
        File file=null;

        FileChooser fileChooser =new FileChooser();
        fileChooser.setTitle(res.getString("app.title62"));

        fileChooser.setInitialDirectory(new File(getModel().getLastExportPath(System.getProperty("user.home"))));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("xmlc", "*.xmlc"));
        file= fileChooser.showOpenDialog(getApp().getMainWindow());

        if(file==null)return;

        final File fileToSave=file;

        final ImportTherapyComplex imp=new ImportTherapyComplex();
        final ResourceBundle rest=res;
        Task<Integer> task =new Task<Integer>() {
            @Override
            protected Integer call() throws Exception
            {

                imp.setListener(new ImportTherapyComplex.Listener() {
                    @Override
                    public void onStartParse() {
                        updateProgress(10, 100);
                    }

                    @Override
                    public void onEndParse() {
                        updateProgress(30, 100);
                    }

                    @Override
                    public void onStartAnalize() {
                        updateProgress(35, 100);
                    }

                    @Override
                    public void onEndAnalize() {
                        updateProgress(50, 100);
                    }

                    @Override
                    public void onStartImport() {
                        updateProgress(55, 100);
                    }

                    @Override
                    public void onEndImport() {
                        updateProgress(90, 100);
                    }

                    @Override
                    public void onSuccess() {
                        updateProgress(98, 100);
                    }

                    @Override
                    public void onError(boolean fileTypeMissMatch) {
                        imp.setListener(null);

                        if (fileTypeMissMatch) {
                            showErrorDialog(rest.getString("app.title41"), "", rest.getString("app.title42"), getApp().getMainWindow(), Modality.WINDOW_MODAL);
                        }
                        failed();

                    }
                });

                int res= imp.parse(fileToSave,getModel(),profile);

                if(res==0)
                {
                    imp.setListener(null);
                    this.failed();
                    return 0;}
                else {
                    imp.setListener(null);
                    return res;
                }

            }


        };
        task.progressProperty().addListener((observable, oldValue, newValue) -> getProgressAPI().setProgressIndicator(newValue.doubleValue()));
        task.setOnRunning(event1 -> getProgressAPI().setProgressIndicator(0.0, rest.getString("app.title43")));

        task.setOnSucceeded(event ->
        {

            if (task.getValue()!=0)
            {


                getProgressAPI().setProgressIndicator(1.0D, rest.getString("app.title44"));

                afterAction.accept(task.getValue());

            }
            else getProgressAPI().setProgressIndicator(rest.getString("app.title45"));
            getProgressAPI().hideProgressIndicator(true);
        });

        task.setOnFailed(event -> {
            getProgressAPI().setProgressIndicator(rest.getString("app.title45"));
            getProgressAPI(). hideProgressIndicator(true);

        });

        Thread threadTask=new Thread(task);
        threadTask.setDaemon(true);
        getProgressAPI().setProgressIndicator(0.01, rest.getString("app.title46"));
        threadTask.start();
    }



    public void exportTherapyComplexes(List<TherapyComplex> complexes)
    {
        if(complexes.isEmpty()) return;

        final List<TherapyComplex> selectedItems = complexes;

        //получим путь к файлу.
        File file=null;
        String initname;

        initname=selectedItems.get(0).getName();

        FileChooser fileChooser =new FileChooser();
        fileChooser.setTitle(res.getString("app.title37"));
        fileChooser.setInitialFileName(TextUtil.replaceWinPathBadSymbols(initname)+".xmlc");
        fileChooser.setInitialDirectory(new File(getModel().getLastExportPath(System.getProperty("user.home"))));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("xmlc", "*.xmlc"));
        file= fileChooser.showSaveDialog(getApp().getMainWindow());

        if(file==null)return;

        final File fileToSave=file;


        getProgressAPI().setProgressIndicator(res.getString("app.title38"));
        Task<Boolean> task =new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception
            {

                boolean res= ExportTherapyComplex.export(selectedItems, fileToSave, getModel());
                if(res==false) {this.failed();return false;}
                else return true;

            }
        };


        task.setOnRunning(event1 -> getProgressAPI().setProgressIndicator(-1.0, res.getString("app.title34")));
        task.setOnSucceeded(event ->
        {
            if (task.getValue()) getProgressAPI().setProgressIndicator(1.0, res.getString("app.title35"));
            else getProgressAPI().setProgressIndicator(res.getString("app.title39"));
            getProgressAPI().hideProgressIndicator(true);
        });

        task.setOnFailed(event -> {
            getProgressAPI().setProgressIndicator(res.getString("app.title39"));
            getProgressAPI().hideProgressIndicator(true);
        });

        Thread threadTask=new Thread(task);

        threadTask.setDaemon(true);

        threadTask.start();
    }
    public void onExportUserBase()
    {
        leftAPI.exportUserBase();
    }
    private BiofonUIUtil initBiofon(){
        BiofonUIUtil biofonUIUtil;
        try {
            BiofonTabController biofonTabController = initBiofonTabContent();
            biofonTabController.setExportTherapyComplexesFunction(this::exportTherapyComplexes);
            biofonTabController.setImportTherapyComplexFunction(this::importTherapyComplex);
            biofonTabController.setPrintComplexesFunction(this::printComplexes);
            biofonUIUtil = biofonTabController.getBiofonUIUtil();
            containerProgress = biofonTabController.getProgressContainer();
        } catch (Exception e) {
            showExceptionDialog("Ошибка инициализации панели биофона","","",e,getApp().getMainWindow(), Modality.WINDOW_MODAL);
            throw new RuntimeException(e);
        }
        return biofonUIUtil;

    }
    private void initSectionTreeActionListener() {
        leftAPI.setTreeActionListener(new TreeActionListener() {
            @Override
            public void programItemDoubleClicked(TreeItem<INamed> selectedItem) {
                int tabSelectedIndex = therapyTabPane.getSelectionModel().getSelectedIndex();
                doubleClickOnSectionTreeProgramItemAction(selectedItem,tabSelectedIndex);
            }

            @Override
            public void complexItemDoubleClicked(TreeItem<INamed> selectedItem) {
                int tabSelectedIndex = therapyTabPane.getSelectionModel().getSelectedIndex();
                doubleClickOnSectionTreeComplexItemAction(selectedItem,tabSelectedIndex);
            }
        });
    }


    private void addComplexToBiofonTab(TherapyComplex tc)
    {
        biofonUIUtil.addComplex(tc);
    }
    private void addProgramToBiofonTab(TherapyComplex tc,TherapyProgram tp)
    {
        biofonUIUtil.addProgram(tp);
    }

    private void doubleClickOnSectionTreeComplexItemAction(TreeItem<INamed> selectedItem, int tabSelectedIndex) {
        //если выбран биофон вкладка

            //добавляется комплекс в биофон
            Complex c = (Complex) selectedItem.getValue();
            try {
                TherapyComplex th = getModel().createTherapyComplex(getApp().getBiofonProfile(), c, c.getTimeForFreq()==0?180:c.getTimeForFreq(),3,getModel().getInsertComplexLang());

                addComplexToBiofonTab(th);

            } catch (Exception e) {
                logger.error("",e);
                showExceptionDialog("Ошибка создания терапевтического комплекса ", "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);

            }
    }

    private void doubleClickOnSectionTreeProgramItemAction(TreeItem<INamed> selectedItem, int tabSelectedIndex) {
        //проверим язык програмы и язык вставки
        Program p = (Program) selectedItem.getValue();
        String il=getModel().getInsertComplexLang();
        String lp=getModel().getProgramLanguage().getAbbr();

        String name="";
        String oname="";
        String descr="";

        if(il.equals(lp))
        {
            name=p.getNameString();
            descr=p.getDescriptionString();
        }else {
            //вставим имя на языке вставки. oname - на языке который программа
            if(p.isOwnerSystem()) oname=p.getNameString();
            try {
                name = getModel().getString2(p.getName(),getModel().getLanguage(il));
                descr=getModel().getString2(p.getDescription(),getModel().getLanguage(il));
            } catch (Exception e) {
                logger.error("",e);
                showExceptionDialog("Ошибка создания терапевтической программы", "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);
            }
        }


            TherapyComplex selectedTCBiofon = biofonUIUtil.getSelectedComplex();
            if(selectedTCBiofon==null) return;
            try {
                TherapyProgram therapyProgram = getModel().createTherapyProgram(p.getUuid(),selectedTCBiofon, name, descr, p.getFrequencies(),oname);
                addProgramToBiofonTab(selectedTCBiofon,therapyProgram);
            } catch (Exception e) {
                logger.error("",e);
                showExceptionDialog("Ошибка создания терапевтической программы", "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);

            }

    }



    /**
     * Балансирует положение разделителей сплитера для удобства
     */
    private void balanceSpitterDividers()
    {
        SplitPane.Divider divider1 = splitOuter.getDividers().get(0);
        divider1.setPosition(0.25);
    }

    private LeftPanelAPI initLeftPanel() {
        try{
            return  (LeftPanelAPI) replaceContent("/fxml/LeftPanel.fxml",leftLayout);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка инициализации Левой панели",e);
        }
    }



    private BiofonTabController initBiofonTabContent() throws Exception {
        return  (BiofonTabController) replaceContent("/fxml/BiofonTab.fxml",biofonTabContent);
    }







    private void initGCRunner() {
        gcThreadRunner =new Thread(() ->
        {
            try {
                while (true)
                {
                    if(isStopGCthread()) break;
                    Thread.sleep(46000);
                    System.gc();
                    System.out.println("GC");
                }
            } catch (InterruptedException e) {
                logger.error("",e);
            }

        });
        gcThreadRunner.setDaemon(true);
        gcThreadRunner.start();
    }






    private void initVerticalDivider() {
        Platform.runLater(() -> balanceSpitterDividers());

        //будем подстраивать  dividers при изменении размеров контейнера таблиц, при движ ползунков это не работает, при изм размеров окна срабатывает
        splitOuter.widthProperty().addListener((observableValue, oldSceneHeight, newSceneHeight) -> balanceSpitterDividers());
    }





/******************* Пункты меню *****************/

    /**
     * Акшен на кнопку меню найти устройство, открывает диалог ввода метки диска
     */
    public void onFindDevice()
    {

        String retString="";
        String option="";
        try {
             option = getModel().getOption("device.disk.mark");
            retString  = BaseController.showTextInputDialog(res.getString("ui.main.find_device_title_dlg"),"", res.getString("ui.main.find_device_content_dlg"), option , getApp().getMainWindow(), Modality.APPLICATION_MODAL);

        } catch (Exception e) {
            logger.error("",e);

        }
if(!retString.equals(option) && !retString.isEmpty())
{
    try {
        getModel().setOption("device.disk.mark",retString);
        DiskDetector.setNameDiskStore(retString);
        System.out.println(retString);
    } catch (Exception e) {
        logger.error("",e);
    }
}

    }




/************  Обработчики меню Файл **********/



    /**
     * пользователь может не выбрать раздел ни в выборе базы, ни в выборе разделов 2 уровня ни в дереве. Также можно выбрать любой раздел.
     * В нем создастся контейнер
     */
    public void onImportUserBase()
    {
        leftAPI.importUserBase();
    }




    public void  onHelp()
    {
          File manualpath=null;
         manualpath=new File("."+File.separator+"manuals"+File.separator+"manual_"+getModel().getProgramLanguage().getAbbr()+".pdf");
        if(!manualpath.exists())manualpath=new File("."+File.separator+"manuals"+File.separator+"manual_"+getModel().getDefaultLanguage().getAbbr()+".pdf");
        try {

            //openDialogNotModal(getApp().getMainWindow(), "/fxml/webview.fxml", res.getString("app.title72"), true, StageStyle.DECORATED, 0, 0, 0, 0);
            openDialogNotModal(getApp().getMainWindow(), "/fxml/PdfViewer.fxml", res.getString("app.title72"), true, StageStyle.DECORATED, 0, 0, 0, 0,manualpath);
        } catch (IOException e) {
            logger.error("",e);
        }


    }



    public void onLangChoose()
    {

        try {
            openDialog(getApp().getMainWindow(),"/fxml/language_options.fxml",res.getString("app.title114"),false,StageStyle.DECORATED,0,0,0,0);
        } catch (IOException e) {
            logger.error("",e);
        }

    }

    public void printComplexes(List<TherapyComplex> complexes)
    {

        if(complexes ==null)return;
        if(complexes.isEmpty())return;
        try {
            openDialog(
                    getApp().getMainWindow(),
                    "/fxml/PrintContent.fxml",
                    res.getString("app.menu.print_complex"),
                    true,
                    StageStyle.DECORATED,
                    0,0,0,0,
                    complexes.stream().map(value -> value.getId()).collect(Collectors.toList())
                    ,2);
        } catch (IOException e) {
            logger.error("",e);
        }
    }



    /**
     * Импорт терапевтического комплекса в базу частот
     */
    public void   onImportComplexToBase()
    {
        leftAPI.importComplexToBaseFromDir();
    }


    /**
     * Опции кодека из меню
     */
   public void onCodecOptions()
   {
       try {
           String option = getModel().getOption("codec.path");
           String s = showTextInputDialog(res.getString("app.menu.codecpath"), "", "Codec path", option, getApp().getMainWindow(), Modality.WINDOW_MODAL);

           if(!s.equals(option))//сохраним ввод
           {
               getModel().setOption("codec.path",s);
           }

       } catch (Exception e) {
           logger.error("",e);
           showExceptionDialog("Ошибка чтения опции codec.path", "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);
       }
   }


 public void    onLanguageInsertComplexOption(){
     try {
         openDialog(getApp().getMainWindow(),"/fxml/language_insert_complex_option_dlg.fxml",res.getString("app.menu.insert_language"),false,StageStyle.DECORATED,0,0,0,0);
     } catch (IOException e) {
         logger.error("",e);
     }
 }

/******* Обновления базы ****/
    /**
     * Языковые файлы
     */
    public void onCreateLanguageFiles()
    {
        File file=null;
        DirectoryChooser dirChooser =new DirectoryChooser();
        dirChooser.setTitle("Создание  языковых файлов. Выбор директории");
        dirChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        file= dirChooser.showDialog(getApp().getMainWindow());

        if(file==null)return;

        CreateLanguageFiles.export(file,getModel());
        showInfoDialog("Создание  языковых файлов.","","Завершено",getApp().getMainWindow(),Modality.WINDOW_MODAL);

    }
    /**
     * Языковые файлы
     */
    public void onCreateLanguageFilesLatin()
    {
        File file=null;
        DirectoryChooser dirChooser =new DirectoryChooser();
        dirChooser.setTitle("Создание  языковых файлов. Выбор директории");
        dirChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        file= dirChooser.showDialog(getApp().getMainWindow());

        if(file==null)return;

        CreateLanguageFilesCheckLatin.export(file,getModel());
        showInfoDialog("Создание  языковых файлов.","","Завершено",getApp().getMainWindow(),Modality.WINDOW_MODAL);

    }

    public void onloadLanguageFiles()
    {
        List<File> files=null;
        FileChooser fileChooser =new FileChooser();
        fileChooser.setTitle("Загрузка языковых файлов");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("xml", "*.xml"));
        files= fileChooser.showOpenMultipleDialog(getApp().getMainWindow());

        if(files==null)return;

        LoadLanguageFiles ll=new LoadLanguageFiles();
        if( ll.parse(files,getModel())) showInfoDialog("Загрузка языковых файлов","","Все нормально. Результаты в консоли",getApp().getMainWindow(),Modality.WINDOW_MODAL);
        else showErrorDialog("Загрузка языковых файлов","","Есть ошибки. Результаты в консоли",getApp().getMainWindow(),Modality.WINDOW_MODAL);

    }

    /**
     * Файл для правки частот
     */
    public void onCreateUpdateFreqFile()
    {
        File file=null;
        FileChooser fileChooser =new FileChooser();
        fileChooser.setTitle("Создание файла частот");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("freqbase", "*.freqbase"));
        file= fileChooser.showSaveDialog(getApp().getMainWindow());

        if(file==null)return;
       if( CreateFrequenciesFile.export(file,getModel())) showInfoDialog("Создание файла частот","","Файл создан",getApp().getMainWindow(),Modality.WINDOW_MODAL);
        else showErrorDialog("Создание файла частот","","Файл не создан",getApp().getMainWindow(),Modality.WINDOW_MODAL);
    }

    public void onLoadUpdateFreqFile(){
        File file=null;
        FileChooser fileChooser =new FileChooser();
        fileChooser.setTitle("Загрузка файла частот");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("freqbase", "*.freqbase"));
        file= fileChooser.showOpenDialog(getApp().getMainWindow());

        if(file==null)return;
        LoadFrequenciesFile lf=new LoadFrequenciesFile();

        try {
            lf.parse(file,getModel());
            showInfoDialog("Загрузка файла частот","","Файл загружен",getApp().getMainWindow(),Modality.WINDOW_MODAL);
        } catch (Exception e) {
           logger.error("Ошибка загрузки файла частот",e);
            showErrorDialog("Загрузка файла частот","","Файл не загружен",getApp().getMainWindow(),Modality.WINDOW_MODAL);
        }
    }

    /**
     * Создает справочник по базе
     */
    public void onHeplCreate(){
        File file=null;
        DirectoryChooser dirChooser =new DirectoryChooser();
        dirChooser.setTitle("Создание  справочников. Выбор директории");
        dirChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        file= dirChooser.showDialog(getApp().getMainWindow());

        if(file==null)return;
        try {
            CreateBaseHelper.createHelpFiles(file,getModel());
            showInfoDialog("Создание  справочников","","Завершено",getApp().getMainWindow(),Modality.WINDOW_MODAL);
        } catch (Exception e) {
            e.printStackTrace();
            showExceptionDialog("Создание  справочников","","Завершено",e,getApp().getMainWindow(),Modality.WINDOW_MODAL);
        }
    }
/**********/


    /**
     *Создать бекап
     */
    public void onRecoveryCreate(){
        List<Section> collect = leftAPI.getBaseAllItems().stream().filter(section -> "USER".equals(section.getTag())).collect(Collectors.toList());
        Section userSection=null;
        if(collect.isEmpty()) return;
        userSection=collect.get(0);
        collect=null;
        //получим путь к файлу.
        File file=null;

        Calendar cal = Calendar.getInstance();
        getModel().initStringsSection(userSection);
        FileChooser fileChooser =new FileChooser();
       fileChooser.setTitle(res.getString("ui.backup.create_backup"));
        fileChooser.setInitialDirectory(new File(getModel().getLastExportPath(System.getProperty("user.home"))));
        fileChooser.setInitialFileName( cal.get(Calendar.DAY_OF_MONTH)+"_"+(cal.get(Calendar.MONTH)+1)+"_"+cal.get(Calendar.YEAR)+".brecovery");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("brecovery", "*.brecovery"));
        file= fileChooser.showSaveDialog(getApp().getMainWindow());

        if(file==null)return;
        final Section sec=userSection;
        final File zipFile=file;
        getProgressAPI().setProgressBar(0.0, res.getString("ui.backup.create_backup"), "");


        Task<Boolean> task =new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception
            {
                File recoveryDir=new File(getApp().getTmpDir(),"recovery");
                if(!recoveryDir.exists())if(!recoveryDir.mkdir()) return false;

                File baseFile=new File(recoveryDir,"base.xmlb");

                boolean res1= ExportUserBase.export(sec, baseFile, getModel());
                if(res1==false) {this.failed();return false;}

                List<Profile> allProfiles = getApp().getModel().findAllProfiles();
                this.updateProgress(1,allProfiles.size()+2);
                boolean res2=true;


                int cnt=1;
                for (Profile profile : allProfiles) {
                    res2= ExportProfile.export(profile,new File(recoveryDir,profile.getId()+".xmlp"),getModel());
                    if(res2==false) break;
                    this.updateProgress(++cnt,allProfiles.size()+2);
                }
                if(res2==false) {this.failed();return false;}
                boolean res3=true;
                res3 = ZIPUtil.zipFolder(recoveryDir,zipFile);
                return res1 && res2 && res3;
            }
        };

        task.progressProperty().addListener((observable, oldValue, newValue) -> {
            getProgressAPI().setProgressBar(newValue.doubleValue(), res.getString("ui.backup.create_backup"), "");
        });

        task.setOnRunning(event1 -> getProgressAPI().setProgressBar(0.0, res.getString("ui.backup.create_backup"), ""));

        task.setOnSucceeded(event ->
        {
            Waiter.closeLayer();
            if (task.getValue()) {
                getProgressAPI().hideProgressBar(false);
                getProgressAPI().setProgressIndicator(1.0, res.getString("app.title103"));

            } else {
                getProgressAPI().hideProgressBar(false);
                getProgressAPI().setProgressIndicator(res.getString("app.title93"));
            }
            getProgressAPI().hideProgressIndicator(true);
        });

        task.setOnFailed(event -> {
            Waiter.closeLayer();
            getProgressAPI().hideProgressBar(false);
            getProgressAPI().setProgressIndicator(res.getString("app.title93"));
            getProgressAPI().hideProgressIndicator(true);
        });


        Thread threadTask=new Thread(task);
        threadTask.setDaemon(true);
        getProgressAPI().setProgressBar(0.01, res.getString("app.title102"), "");
        Waiter.openLayer(getApp().getMainWindow(),false);
        threadTask.start();
        Waiter.show();
    }




    /**
     * Загрузить бекап
     *
     */
    public void onRecoveryLoad(){
        List<Section> collect = leftAPI.getBaseAllItems().stream().filter(section -> "USER".equals(section.getTag())).collect(Collectors.toList());
        Section userSection=null;
        if(collect.isEmpty()) return;
        userSection=collect.get(0);
        collect=null;

        //получим путь к файлу.
        File file=null;

        getModel().initStringsSection(userSection);
        FileChooser fileChooser =new FileChooser();
        fileChooser.setTitle(res.getString("ui.backup.load_backup"));
        fileChooser.setInitialDirectory(new File(getModel().getLastExportPath(System.getProperty("user.home"))));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("brecovery", "*.brecovery"));
        file= fileChooser.showOpenDialog(getApp().getMainWindow());
        if(file==null)return;

        String add_mode=res.getString("app.ui.add_recovery_data");
        String replace_mode=res.getString("app.ui.replace_recovery_data");

        String mRec = showChoiceDialog("Загрузка резервной копии", "", "Выбирайте ",
                Arrays.asList(add_mode,replace_mode),
                replace_mode,
                getApp().getMainWindow(), Modality.WINDOW_MODAL);

        if(mRec==null) return;
        final boolean replaceMode = mRec.equals(replace_mode);

        final Section sec=userSection;
        final File zipFile=file;

        getProgressAPI().setProgressBar(0.0, res.getString("ui.backup.load_backup"), "");

        Task<Boolean> task =new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception
            {
                getApp().recursiveDeleteTMP();

                File recoveryDir=new File(getApp().getTmpDir(),"recovery");
                if(!recoveryDir.exists())if(!recoveryDir.mkdir()) return false;

                boolean res1 = ZIPUtil.unZip(zipFile,recoveryDir);
                //очистка пользовательской базы и профилей если выбран соответствуюший режим.

                if(replaceMode){
                    getModel().clearUserBaseAndProfiles();

                    Platform.runLater(() -> {
                        //выбрать раздел 0 главный раздел
                        leftAPI.selectBase(0);
                    });

                }

                File profDir=new File(recoveryDir,"profiles");
                if(!profDir.exists())profDir=recoveryDir;//учитывает возможность наличие папки из бекапов старых версий
                boolean res2 =true;
                File[] files = profDir.listFiles((dir, name) -> name.contains(".xmlp"));
                int pCount= files.length;
                this.updateProgress(1,pCount+2);
                int cnt=1;
                for (File f : files)
                {
                    ImportProfile imp=new ImportProfile();
                    imp.setListener(new ImportProfile.Listener() {
                        @Override
                        public void onStartParse() {}

                        @Override
                        public void onEndParse() {}

                        @Override
                        public void onStartAnalize()  {}
                        @Override
                        public void onEndAnalize() {}
                        @Override
                        public void onStartImport() {}

                        @Override
                        public void onEndImport()  {}

                        @Override
                        public void onSuccess()  {}

                        @Override
                        public void onError(boolean fileTypeMissMatch) {

                        }
                    });

                    boolean res= imp.parse(f, getModel());
                    imp.setListener(null);
                    if(res==false)
                    {

                        res2=false;
                        break;
                        }
                    this.updateProgress(++cnt,pCount+2);
                }

                //база

                ImportUserBase imp=new ImportUserBase();

                        imp.setListener(new ImportUserBase.Listener() {
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


                        boolean res3= imp.parse( new File(recoveryDir,"base.xmlb"), getModel(),sec);
                       imp.setListener(null);

                getApp().recursiveDeleteTMP();
                return res1 && res2 && res3;
            }
        };

        task.progressProperty().addListener((observable, oldValue, newValue) -> {
            getProgressAPI().setProgressBar(newValue.doubleValue(), res.getString("ui.backup.load_backup"), "");
        });

        task.setOnRunning(event1 -> getProgressAPI().setProgressBar(0.0, res.getString("ui.backup.load_backup"), ""));


        int count_profiles=replaceMode==true?0:getModel().countProfile();

        task.setOnSucceeded(event ->
        {
            Waiter.closeLayer();
            if (task.getValue()) {
                getProgressAPI().hideProgressBar(false);
                getProgressAPI().setProgressIndicator(1.0, res.getString("app.title103"));
                if(getModel().countProfile()!=0)
                {


                    biofonUIUtil.reloadComplexes();


                    leftAPI.selectBase(0);
                    leftAPI.selectBase(sec);
                }
            } else {
                getProgressAPI().hideProgressBar(false);
                getProgressAPI().setProgressIndicator(res.getString("app.title93"));
            }
            getProgressAPI().hideProgressIndicator(true);
        });

        task.setOnFailed(event -> {
            Waiter.closeLayer();
            getProgressAPI().hideProgressBar(false);
            getProgressAPI().setProgressIndicator(res.getString("app.title93"));
            getProgressAPI().hideProgressIndicator(true);
        });


        Thread threadTask=new Thread(task);
        threadTask.setDaemon(true);
        getProgressAPI().setProgressBar(0.01, res.getString("ui.backup.load_backup"), "");
        Waiter.openLayer(getApp().getMainWindow(),false);

        threadTask.start();
        Waiter.show();
    }

    /**
     *  Нажатие на меню выбора пути к данным в опциях
     */
    public void onPathMenuItemAction()
    {
        try {
            openDialog(getApp().getMainWindow(),"/fxml/DataPathDialog.fxml",res.getString("app.ui.options.data_path"),false,StageStyle.DECORATED,0,0,0,0);
        } catch (IOException e) {
            logger.error("",e);
        }
    }



    @Override
    protected void onCompletedInitialise() {
    }


    public void onReference(){
        try {
            openDialogNotModal(getApp().getMainWindow(),"/fxml/ReferenceBook.fxml",res.getString("app.menu.reference"),true,StageStyle.DECORATED,600,900,0,0,"/styles/Styles.css");
        } catch (IOException e) {
            logger.error("",e);
        }
    }



    @Override
    public void setParams(Object... params)
    {
    }

    /**
     * Уменьшает время отклика Tooltip в мс
     * @param tooltip
     */
    public static void hackTooltipStartTiming(Tooltip tooltip,int startDelay,int hideDelay) {
        try {
            Field fieldBehavior = tooltip.getClass().getDeclaredField("BEHAVIOR");
            fieldBehavior.setAccessible(true);
            Object objBehavior = fieldBehavior.get(tooltip);

            Field fieldTimer = objBehavior.getClass().getDeclaredField("activationTimer");
            fieldTimer.setAccessible(true);
            Timeline objTimer = (Timeline) fieldTimer.get(objBehavior);

            objTimer.getKeyFrames().clear();
            objTimer.getKeyFrames().add(new KeyFrame(new Duration(startDelay)));

            ////

            Field fieldTimer1 = objBehavior.getClass().getDeclaredField("hideTimer");
            fieldTimer1.setAccessible(true);
            Timeline objTimer1 = (Timeline) fieldTimer1.get(objBehavior);

            objTimer1.getKeyFrames().clear();
            objTimer1.getKeyFrames().add(new KeyFrame(new Duration(hideDelay)));


        } catch (Exception e) {
            logger.error("",e);
        }
    }
}
