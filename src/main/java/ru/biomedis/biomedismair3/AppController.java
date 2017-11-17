package ru.biomedis.biomedismair3;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.anantacreative.updater.Update.UpdateException;
import org.anantacreative.updater.Update.UpdateTask;
import org.hid4java.HidDevice;
import ru.biomedis.biomedismair3.Layouts.BiofonTab.BiofonTabController;
import ru.biomedis.biomedismair3.Layouts.BiofonTab.BiofonUIUtil;
import ru.biomedis.biomedismair3.Layouts.LeftPanel.LeftPanelAPI;
import ru.biomedis.biomedismair3.Layouts.LeftPanel.TreeActionListener;
import ru.biomedis.biomedismair3.Layouts.ProgressPanel.ProgressAPI;
import ru.biomedis.biomedismair3.TherapyTabs.Complex.ComplexAPI;
import ru.biomedis.biomedismair3.TherapyTabs.Complex.ComplexController;
import ru.biomedis.biomedismair3.TherapyTabs.Complex.ComplexTable;
import ru.biomedis.biomedismair3.TherapyTabs.Profile.ProfileAPI;
import ru.biomedis.biomedismair3.TherapyTabs.Profile.ProfileController;
import ru.biomedis.biomedismair3.TherapyTabs.Profile.ProfileTable;
import ru.biomedis.biomedismair3.TherapyTabs.Programs.ProgramAPI;
import ru.biomedis.biomedismair3.TherapyTabs.Programs.ProgramController;
import ru.biomedis.biomedismair3.TherapyTabs.Programs.ProgramTable;
import ru.biomedis.biomedismair3.UpdateUtils.FrequenciesBase.*;
import ru.biomedis.biomedismair3.UserUtils.CreateBaseHelper;
import ru.biomedis.biomedismair3.UserUtils.Export.ExportProfile;
import ru.biomedis.biomedismair3.UserUtils.Export.ExportUserBase;
import ru.biomedis.biomedismair3.UserUtils.Import.ImportProfile;
import ru.biomedis.biomedismair3.UserUtils.Import.ImportUserBase;
import ru.biomedis.biomedismair3.entity.*;
import ru.biomedis.biomedismair3.m2.M2;
import ru.biomedis.biomedismair3.m2.M2BinaryFile;
import ru.biomedis.biomedismair3.utils.Date.DateUtil;
import ru.biomedis.biomedismair3.utils.Disk.DiskDetector;
import ru.biomedis.biomedismair3.utils.Disk.DiskSpaceData;
import ru.biomedis.biomedismair3.utils.Files.FilesProfileHelper;
import ru.biomedis.biomedismair3.utils.Files.ZIPUtil;
import ru.biomedis.biomedismair3.utils.OS.OSValidator;
import ru.biomedis.biomedismair3.utils.USB.PlugDeviceListener;
import ru.biomedis.biomedismair3.utils.USB.USBHelper;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static ru.biomedis.biomedismair3.Log.logger;

public class AppController  extends BaseController {

    public static final int MAX_BUNDLES=15;
    @FXML private ImageView deviceIcon;//иконка устройства
    @FXML private ProgressBar diskSpaceBar;//прогресс бар занятого места на диске прибора
    @FXML private MenuItem menuExportProfile;
    @FXML private MenuItem    menuExportTherapyComplex;
    @FXML private MenuItem  menuImportTherapyComplex;
    @FXML private MenuItem menuDelGeneratedFiles;
    @FXML private SplitPane splitOuter;

    @FXML private MenuItem printProfileMenu;
    @FXML private MenuItem printComplexMenu;
    @FXML private MenuItem menuImportComplex;
    @FXML private MenuItem   menuImportComplexToBase;
    @FXML private MenuItem   dataPathMenuItem;
    @FXML private TabPane therapyTabPane;
    @FXML private Tab tab5;

    @FXML private AnchorPane leftLayout;
    @FXML private AnchorPane biofonTabContent;
    @FXML private AnchorPane profileLayout;
    @FXML private AnchorPane tab5_content;
    @FXML private AnchorPane complexLayout;
    @FXML private AnchorPane programLayout;
    @FXML private Menu updateBaseMenu;

    private @FXML MenuItem clearTrinityItem;
    @FXML private  Menu menuImport;
    @FXML private  HBox topPane;
    @FXML private MenuItem readFromTrinityMenu;

    private Path devicePath=null;//путь ку устройству или NULL если что-то не так
    private String fsDeviceName="";
    private SimpleBooleanProperty connectedDevice =new SimpleBooleanProperty(false);//подключено ли устройство

    private static TabPane _therapyTabPane;

    private Image imageDeviceOff;
    private Image imageDeviceOn;

    private  ResourceBundle res;
    private Tooltip diskSpaceTooltip=new Tooltip();
    private boolean stopGCthread=false;

    private static M2UI m2ui;

    //сборщик мусора. Собирает мусор периодически, тк мы много объектов создаем при построении дерева
    //нужно его отключать вкогда плодим файлы!!!!!
    private Thread gcThreadRunner;



    private String baseComplexTabName;
    private String baseProgramTabName;
    private String baseProfileTabName;

    private static  LeftPanelAPI leftAPI;
    private static  BiofonUIUtil biofonUIUtil;
    private static  ProfileAPI profileAPI;
    private static  ComplexAPI complexAPI;
    private static  ProgressAPI progressAPI;
    private static  ProgramAPI programAPI;

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

    public static ProfileAPI getProfileAPI() {
        return profileAPI;
    }

    public static ComplexAPI getComplexAPI() {
        return complexAPI;
    }

    public static ProgramAPI getProgramAPI() {
        return programAPI;
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
        initNamesTables();
        _therapyTabPane = therapyTabPane;
        dataPathMenuItem.setVisible(OSValidator.isWindows());//видимость пункта меню для введения пути к папки данных, только на винде!
        clearTrinityItem.disableProperty().bind(m2Connected.not());
        updateBaseMenu.setVisible(getApp().isUpdateBaseMenuVisible());

        initVerticalDivider();

        diskSpaceBar.setVisible(false);
        diskSpaceBar.setTooltip(diskSpaceTooltip);
        hackTooltipStartTiming(diskSpaceTooltip, 250, 15000);

        initGCRunner();
        getApp().addCloseApplistener(() -> {
            setStopGCthread();
            DiskDetector.stopDetectingService();
        });

        initDeviceMDetection(rb);

        progressAPI = initProgressPanel();
        leftAPI = initLeftPanel();
        profileAPI = initProfileTab();
        complexAPI = initComplexTab();
        programAPI = initProgramTab();
        biofonUIUtil = initBiofon();

        initTabComplexNameListener();
        initSectionTreeActionListener();

        initUSBDetectionM2();
        initTrinityReadingMenuItemDisabledPolicy();
        initM2UI();

        initMenuImport();

        //настройка подписей в табах
        initTabs();

        /** Конопки меню верхнего ***/
        menuDelGeneratedFiles.disableProperty().bind(ProfileTable.getInstance().getSelectedItemProperty().isNull());

        printProfileMenu.disableProperty().bind(ProfileTable.getInstance().getSelectedItemProperty().isNull());
        printComplexMenu.disableProperty().bind(ComplexTable.getInstance().getSelectedItemProperty().isNull());
        menuImportComplex.disableProperty().bind(ProfileTable.getInstance().getSelectedItemProperty().isNull());
        menuExportProfile.disableProperty().bind(ProfileTable.getInstance().getSelectedItemProperty().isNull());
        menuExportTherapyComplex.disableProperty().bind(ComplexTable.getInstance().getSelectedItemProperty().isNull());
        menuImportTherapyComplex.disableProperty().bind(ProfileTable.getInstance().getSelectedItemProperty().isNull());
        /***************/

    }




    private ProgressAPI initProgressPanel(){

        try{
          return (ProgressAPI) addContentHBox("/fxml/ProgressPane.fxml",topPane);
        }catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка инициализации панели програесса ",e);
        }

    }


    private BiofonUIUtil initBiofon(){
        BiofonUIUtil biofonUIUtil;
        try {
            BiofonTabController biofonTabController = initBiofonTabContent();
            biofonTabController.setExportTherapyComplexesFunction(this::exportTherapyComplexes);
            biofonTabController.setImportTherapyComplexFunction(this::importTherapyComplex);
            biofonTabController.setPrintComplexesFunction(this::printComplexes);
            biofonUIUtil = biofonTabController.getBiofonUIUtil();

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
        if(tabSelectedIndex==3){
            //добавляется комплекс в биофон
            Complex c = (Complex) selectedItem.getValue();
            try {
                TherapyComplex th = getModel().createTherapyComplex(getApp().getBiofonProfile(), c, c.getTimeForFreq()==0?180:c.getTimeForFreq(),3,getModel().getInsertComplexLang());

                addComplexToBiofonTab(th);

            } catch (Exception e) {
                logger.error("",e);
                showExceptionDialog("Ошибка создания терапевтического комплекса ", "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);

            }


        }else
        if (ProfileTable.getInstance().getSelectedItem() != null)//добавление комплекса в профиль
        {

            Complex c = (Complex) selectedItem.getValue();

            try {
                TherapyComplex th = getModel().createTherapyComplex(ProfileTable.getInstance().getSelectedItem(), c, c.getTimeForFreq()==0?180:c.getTimeForFreq(),3,getModel().getInsertComplexLang());

                //therapyComplexItems.clear();
                //therapyComplexItems содержит отслеживаемый список, элементы которого добавляются в таблицу. Его не нужно очищать

                ComplexTable.getInstance().getAllItems().add(th);
                ComplexTable.getInstance().clearSelection();
                therapyTabPane.getSelectionModel().select(1);//выберем таб с комплексами
                ComplexTable.getInstance().select(ComplexTable.getInstance().getAllItems().size() - 1);
                profileAPI.updateProfileTime(ProfileTable.getInstance().getSelectedItem());

                //если есть программы  к перенесенном комплексе то можно разрешить генерацию
                if(getModel().countTherapyPrograms(th)>0)profileAPI.enableGenerateBtn();
                else profileAPI.disableGenerateBtn();
                th = null;
            } catch (Exception e) {
                logger.error("",e);
                showExceptionDialog("Ошибка создания терапевтического комплекса ", "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);
            }
            c = null;
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

        if(tabSelectedIndex==3){
            TherapyComplex selectedTCBiofon = biofonUIUtil.getSelectedComplex();
            if(selectedTCBiofon==null) return;
            try {
                TherapyProgram therapyProgram = getModel().createTherapyProgram(p.getUuid(),selectedTCBiofon, name, descr, p.getFrequencies(),oname);
                addProgramToBiofonTab(selectedTCBiofon,therapyProgram);
            } catch (Exception e) {
                logger.error("",e);
                showExceptionDialog("Ошибка создания терапевтической программы", "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);

            }

        }else
        if (ComplexTable.getInstance().getSelectedItem() != null) {
            //если выбран комплекс в таблице комплексов

            try {
                TherapyProgram therapyProgram = getModel().createTherapyProgram(p.getUuid(),ComplexTable.getInstance().getSelectedItem(), name, descr, p.getFrequencies(),oname);
                ProgramTable.getInstance().getAllItems().add(therapyProgram);
                complexAPI.updateComplexTime(ComplexTable.getInstance().getSelectedItem(), false);
                therapyTabPane.getSelectionModel().select(2);//выберем таб с программами


                Platform.runLater(() -> {
                    complexAPI.updateComplexTime(ComplexTable.getInstance().getSelectedItem(),true);
                    ProgramTable.getInstance().clearSelection();
                    ProgramTable.getInstance().requestFocus();
                    ProgramTable.getInstance().select( ProgramTable.getInstance().getAllItems().size() - 1);
                    ProgramTable.getInstance().setItemFocus(ProgramTable.getInstance().getAllItems().size() - 1);
                    ProgramTable.getInstance().scrollTo(ProgramTable.getInstance().getAllItems().size() - 1);
                });

                profileAPI.enableGenerateBtn();
                therapyProgram = null;
            } catch (Exception e) {

                logger.error("",e);
                showExceptionDialog("Ошибка создания терапевтической программы", "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);
            }
            p = null;
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

    private ProgramAPI initProgramTab(){
        try {

        ProgramController pp = (ProgramController)replaceContent("/fxml/ProgramTab.fxml", programLayout);
        pp.setTherapyTabPane(therapyTabPane);
        return pp;
        } catch (Exception e) {
            e.printStackTrace();

            throw new RuntimeException("Ошибка инициализации панели комплексов",e);
        }
    }
    private ComplexAPI initComplexTab()  {
       try{
        ComplexController cc = (ComplexController)replaceContent("/fxml/ComplexTab.fxml", complexLayout);
        cc.setTherapyTabPane(therapyTabPane);
        cc.setDevicePathMethod(()->devicePath);
        return cc;
       } catch (Exception e) {
           e.printStackTrace();

           throw new RuntimeException("Ошибка инициализации панели комплексов",e);
       }
    }

    private ProfileAPI initProfileTab() {
       try {
           ProfileController pc = (ProfileController) replaceContent("/fxml/ProfileTab.fxml", profileLayout);
           pc.setDevicePathMethod(() -> devicePath);
           pc.setTherapyTabPane(therapyTabPane);
           pc.setDevicesProperties(m2Ready, connectedDevice, m2Connected);

           return pc;
       } catch (Exception e) {
           e.printStackTrace();

           throw new RuntimeException("Ошибка инициализации панели профилей",e);
       }

    }

    private BiofonTabController initBiofonTabContent() throws Exception {
        return  (BiofonTabController) replaceContent("/fxml/BiofonTab.fxml",biofonTabContent);
    }

    private void initTrinityReadingMenuItemDisabledPolicy() {
        readFromTrinityMenu.disableProperty().bind(m2Ready.not());
    }

    private void initM2UI() {
        tab5.disableProperty().bind(m2Ready.not());
        try {
            m2ui=(M2UI)replaceContent("/fxml/M2UI.fxml",tab5_content);

        } catch (Exception e) {
            showExceptionDialog("Ошибка инициализации M2UI","","",e,getApp().getMainWindow(), Modality.WINDOW_MODAL);
        }
    }


    private void initMenuImport() {
        menuImport.setOnShowing(event -> {
            menuImportComplexToBase.setDisable(!(leftAPI.isInUserBaseComplexSelected() || leftAPI.isInUserBaseSectionSelected()));
        });
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



    private void initDeviceMDetection(ResourceBundle rb) {
        DropShadow borderGlow;
        borderGlow= new DropShadow();
        borderGlow.setOffsetY(0f);
        borderGlow.setOffsetX(0f);
        borderGlow.setColor(Color.GREEN);
        borderGlow.setWidth(20);
        borderGlow.setHeight(20);


        //при закрытии приложения мы закроем сервис детектирования диска

        URL location;
        location = getClass().getResource("/images/DeviceOff.png");
        imageDeviceOff=new Image(location.toExternalForm());

        location = getClass().getResource("/images/DeviceOn.png");
        imageDeviceOn=new Image(location.toExternalForm());
        deviceIcon.setImage(imageDeviceOff);
        deviceIcon.setEffect(null);
        try {
            DiskDetector.waitForDeviceNotifying(4,getModel().getOption("device.disk.mark"),(state, fs)->{

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if(state)
                        {

                            //вытащим путь к устройству, в перыый раз или когда он будет отличаться.
                           if(fsDeviceName.isEmpty()){
                               fsDeviceName=fs.name();
                               try {
                                   devicePath=DiskDetector.getRootPath(fs);
                               } catch (Exception e) {
                                   devicePath=null;
                                   logger.error("",e);
                               }
                           }
                            else if(!fsDeviceName.equals(fs.name()))
                           {
                               fsDeviceName=fs.name();
                               try {
                                   devicePath=DiskDetector.getRootPath(fs);
                               } catch (Exception e) {
                                   devicePath=null;
                                   logger.error("",e);
                               }

                           }

                            connectedDevice.setValue(true);

                           // checkUpploadBtn();
                            deviceIcon.setImage(imageDeviceOn);
                            deviceIcon.setEffect(borderGlow);

                            DiskSpaceData diskSpace = DiskDetector.getDiskSpace(fs);
                            double progr = (double)diskSpace.getUsed(DiskSpaceData.SizeDiskType.MEGA) /(double) diskSpace.getTotal(DiskSpaceData.SizeDiskType.MEGA);
                            if(progr>1.0)progr=1.0;

                            diskSpaceBar.setProgress(progr);
                            if(!diskSpaceBar.isVisible()) diskSpaceBar.setVisible(true);

                            StringBuilder strb=new StringBuilder();



                            strb.append(rb.getString("ui.main.avaliable_space"));
                            strb.append(" ");
                            strb.append(diskSpace.getAvaliable(DiskSpaceData.SizeDiskType.MEGA));
                            strb.append(rb.getString("app.mb"));
                            strb.append("\n");

                            strb.append(rb.getString("ui.main.used_space"));
                            strb.append(" ");
                            strb.append(diskSpace.getUsed(DiskSpaceData.SizeDiskType.MEGA));
                            strb.append(rb.getString("app.mb"));
                            strb.append("\n");

                            strb.append(rb.getString("ui.main.total_space"));
                            strb.append(" ");
                            strb.append(diskSpace.getTotal(DiskSpaceData.SizeDiskType.MEGA));
                            strb.append(rb.getString("app.mb"));





                            diskSpaceTooltip.setText(strb.toString());

                            strb=null;
                            diskSpace=null;




                        }
                        else {
                            connectedDevice.setValue(false);

                            fsDeviceName="";
                            devicePath=null;


                            deviceIcon.setImage(imageDeviceOff);
                            deviceIcon.setEffect(null);
                            if(diskSpaceBar.isVisible()) diskSpaceBar.setVisible(false);

                        }
                    }
                });


            });
        } catch (Exception e) {
            logger.error("",e);
        }
    }


    private void initVerticalDivider() {
        Platform.runLater(() -> balanceSpitterDividers());

        //будем подстраивать  dividers при изменении размеров контейнера таблиц, при движ ползунков это не работает, при изм размеров окна срабатывает
        splitOuter.widthProperty().addListener((observableValue, oldSceneHeight, newSceneHeight) -> balanceSpitterDividers());
    }

    private void initTabComplexNameListener() {
        ComplexTable.getInstance().textComplexTimeProperty().addListener((observable, oldValue, newValue) -> {

            String[] strings = newValue.split("#");
            if(strings.length!=0)
            {
                TherapyComplex selectedItem = ComplexTable.getInstance().getSelectedItem();
                if(selectedItem==null) return;


                long idC= Long.parseLong(strings[1]);
                if(idC!=selectedItem.getId().longValue())return;//если изменения не в выбраном комплексе, то и считать не надо
                setComplexTabName(baseComplexTabName+" ("+ selectedItem.getName() +") +("+strings[0]+")") ;
            }

        });
    }


    private void setComplexTabName(String val){
        Platform.runLater(() -> therapyTabPane.getTabs().get(1).setText(val));
    }

    private void initNamesTables() {
        baseProfileTabName=res.getString("app.ui.tab1");
        baseComplexTabName=res.getString("app.ui.tab2");
        baseProgramTabName=res.getString("app.ui.tab3");
    }

    private void initTabs() {
        ObservableList<Tab> tabs = therapyTabPane.getTabs();
        tabs.get(1).disableProperty().bind(ProfileTable.getInstance().getSelectedItemProperty().isNull());
        tabs.get(2).disableProperty().bind(ComplexTable.getInstance().getSelectedItemProperty().isNull());


        //при переключении на вкладку профилей проверяем можно ли грузить файлы
        tabs.get(0).setOnSelectionChanged(e -> {if(tabs.get(0).isSelected()) profileAPI.checkUpploadBtn();});

        tabs.get(0).textProperty().bind(new StringBinding() {
            {
                //указывается через , список свойств изменения которых приведут к срабатыванию этого
                super.bind(ProfileTable.getInstance().getSelectedItemProperty());
            }
            @Override
            protected String computeValue() {
                if(ProfileTable.getInstance().getSelectedItem()!=null)
                {
                   return  baseProfileTabName+" ("+ProfileTable.getInstance().getSelectedItem().getName()+")";
                }else return baseProfileTabName;
            }
        });

        ComplexTable.getInstance().getSelectedItemProperty().addListener((observable, oldValue, newValue) -> {

            if(newValue !=null)
            {
                String s = DateUtil.convertSecondsToHMmSs(AppController.this.getModel().getTimeTherapyComplex(newValue));
                setComplexTabName(baseComplexTabName+" ("+ newValue.getName() +") +("+s+")");


            }else setComplexTabName(baseComplexTabName);

        });

        tabs.get(2).textProperty().bind(new StringBinding() {
            {
                //указывается через , список свойств изменения которых приведут к срабатыванию этого
                super.bind(ProgramTable.getInstance().getSelectedItemProperty());
            }
            @Override
            protected String computeValue() {
                if(ProgramTable.getInstance().getSelectedItem()!=null)
                {
                    return  baseProgramTabName+" ("+ProgramTable.getInstance().getSelectedItem().getName()+")";
                }else return baseProgramTabName;
            }
        });

    }

    private static   class ReadingWatchDog{
        private int timeSec;
        private boolean stop=false;
        private Runnable action;
        private Thread thread;


        private ReadingWatchDog(int timeSec,Runnable action) {
            this.timeSec = timeSec;
            this.action = action;
        }

        public static ReadingWatchDog start(int timeSec,Runnable action){
            ReadingWatchDog wd = new ReadingWatchDog(timeSec,action);
            wd.readingWatchDog();
            return wd;
        }

        private void readingWatchDog(){
            Thread thread=new Thread(()->{
                try {
                    Thread.sleep(timeSec*1000);
                    if(!stop){
                        action.run();
                    }

                } catch (InterruptedException e) {
                    //e.printStackTrace();
                }
            });
            this.thread = thread;
            thread.setDaemon(true);
            thread.start();

        }

        public void stop(){
            stop=true;
            thread.interrupt();
        }
    }


    private SimpleBooleanProperty m2Ready =new SimpleBooleanProperty(false);
    private SimpleBooleanProperty m2Connected=new SimpleBooleanProperty(false);

    private void initUSBDetectionM2() {
        USBHelper.addPlugEventHandler(M2.productId, M2.vendorId, new PlugDeviceListener() {
            @Override
            public void onAttachDevice(HidDevice device)  {
                readingWatchDog =null;
                Task<Void> task= new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        trinityAttachLogic();
                        return null;
                    }
                };
                Thread thread=new Thread(task);
                thread.setDaemon(true);
                thread.start();




/*
                Platform.runLater(() -> {
                    showExceptionDialog(res.getString("app.ui.reading_device"),res.getString("app.error"),"", e, getApp().getMainWindow(),Modality.WINDOW_MODAL);
                });
                Platform.runLater(() -> {
                    showExceptionDialog(res.getString("app.ui.reading_device"),res.getString("app.error"),res.getString("trinity_should"), e2, getApp().getMainWindow(),Modality.WINDOW_MODAL);
                });

                Platform.runLater(() -> {
                    m2ui.setContent(new M2BinaryFile());
                    m2Ready.setValue(true);
                });
*/
            }

            @Override
            public void onDetachDevice(HidDevice device) {
                System.out.println("Устройство Trinity отключено");
                if( readingWatchDog !=null)  readingWatchDog.stop();
                Platform.runLater(() ->   {
                    m2Connected.set(false);
                    m2Ready.setValue(false);
                    m2ui.cleanView();
                });

            }

            @Override
            public void onFailure(USBHelper.USBException e)
            {
                Log.logger.error(e);
                showExceptionDialog("USB подключение","Ошибка!","",e,getApp().getMainWindow(),Modality.WINDOW_MODAL);

            }
        });
    }
    private ReadingWatchDog readingWatchDog;
     private void trinityAttachLogic() {
        boolean badConnect=false;
        boolean readingError=false;
        ReadingWatchDog wd=null;
        try {
        Thread.sleep(3000);
        //если запись зависнет, то сработает это  сообщение.
            wd =  ReadingWatchDog.start(15,() -> {
               Platform.runLater(() -> {
                   showErrorDialog(res.getString("app.ui.reading_device"),res.getString("app.error"),res.getString("trinity_should2"), getApp().getMainWindow(), Modality.WINDOW_MODAL);
               });
           });

            M2.readDeviceName(true);
            wd.stop();//остановка WatchDog, чтобы сообщение не всплыло

        } catch (M2.WriteToDeviceException e){
            //если эта ошибка, то устройство вообще не корректно вставлено
            //ошибка покажется через WatchDog
            badConnect = true;
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
            wd.stop();//остановка WatchDog, чтобы сообщение не всплыло
            readingError=true;

        }finally {
            //если есть какая-то из этих ошибок, то дальше скорее всего устройство не будет доступно для работы
            if(badConnect || readingError) return;
            else System.out.println("Устройство Trinity подключено");

        }

        M2BinaryFile m2BinaryFile = new M2BinaryFile();
        try {

             m2BinaryFile = M2.readFromDevice(true);

        } catch (M2.WriteToDeviceException e) {
            e.printStackTrace();
            badConnect =true;

        }
        catch (Exception e) {
            e.printStackTrace();
            readingError =true;

        } finally {

            if(badConnect){
                Platform.runLater(() -> {
                    showErrorDialog(res.getString("app.ui.reading_device"),res.getString("app.error"),res.getString("trinity_should2"), getApp().getMainWindow(),Modality.WINDOW_MODAL);
                });
                return;
            }


            if(readingError){
                readingWatchDog = ReadingWatchDog.start(20,() ->Platform.runLater(() ->   {
                    m2Connected.set(true);
                    m2Ready.setValue(true);
                    m2ui.setContent(new M2BinaryFile());
                    readFromTrinityMenu.setVisible(false);
                }));

            }else {
                M2BinaryFile m2bf= m2BinaryFile;
                Platform.runLater(() ->   {
                    m2Connected.set(true);
                    m2Ready.setValue(true);
                    m2ui.setContent(m2bf);
                    readFromTrinityMenu.setVisible(true);
                });
            }
        }
    }

    /**********************************************/





    public void onPrintComplex(){
        complexAPI.printComplex();
    }


    public static boolean isProfileTabSelected(){
        return 0 == _therapyTabPane.getSelectionModel().getSelectedIndex();
    }
    public static  boolean isComplexesTabSelected(){
        return 1 == _therapyTabPane.getSelectionModel().getSelectedIndex();
    }
    public static boolean isProgramsTabSelected(){
        return 2 == _therapyTabPane.getSelectionModel().getSelectedIndex();
    }

    public static  void deleteInTables() {
        System.out.println("deleteInTables");
        if(isProgramsTabSelected())  programAPI.removePrograms();
        else if(isComplexesTabSelected()) complexAPI.removeComplex();
        else if(isProfileTabSelected())   profileAPI.removeProfile();
    }

    public static  void pasteInTables() {
        System.out.println("pasteInTables");
        if(isProgramsTabSelected()) programAPI.pasteTherapyPrograms();
        else if(isComplexesTabSelected()) complexAPI.pasteTherapyComplexes();
        else if(isProfileTabSelected()) profileAPI.pasteProfile();
    }

    public static  void copyInTables() {
        System.out.println("copyInTables");
        if(isProgramsTabSelected()) programAPI.copySelectedTherapyProgramsToBuffer();
        else if(isComplexesTabSelected()) complexAPI.copySelectedTherapyComplexesToBuffer();

    }

    public  static void cutInTables() {
        System.out.println("cutInTables");
        if(isProgramsTabSelected()) programAPI.cutSelectedTherapyProgramsToBuffer();
        else if(isComplexesTabSelected()) complexAPI.cutSelectedTherapyComplexesToBuffer();
        else if(isProfileTabSelected()) profileAPI.cutProfileToBuffer();
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


    public void onClearDevice()
    {
        //очищение файлов lib для пересканирования
if(!getConnectedDevice())return;

        profileAPI.disableGenerateBtn();


        Optional<ButtonType> buttonType = showConfirmationDialog(res.getString("app.serv_1"), res.getString("app.serv_2"), res.getString("app.serv_3"), getApp().getMainWindow(), Modality.WINDOW_MODAL);

        if(buttonType.isPresent())
        {
            if(buttonType.get()!=okButtonType)return;


        }else return;


        Task<Void> task =new Task<Void>() {
            @Override
            protected Void call() throws Exception
            {

                FilesProfileHelper.recursiveLibsDelete(devicePath.toFile());
                Thread.sleep(2000);
                return null;
            }
        };


        task.setOnRunning(event1 -> getProgressAPI().setProgressIndicator(-1.0, res.getString("app.serv_4")));
        task.setOnSucceeded(event ->
        {
            getProgressAPI().setProgressIndicator(1.0, res.getString("app.serv_5"));

            getProgressAPI().hideProgressIndicator(true);
            profileAPI.enableGenerateBtn();

        });

        task.setOnFailed(event -> {
            getProgressAPI().setProgressIndicator(res.getString("app.serv_6"));
            getProgressAPI().hideProgressIndicator(true);
            profileAPI.enableGenerateBtn();
        });

        Thread threadTask=new Thread(task);

        threadTask.setDaemon(true);

        threadTask.start();


    }

/************  Обработчики меню Файл **********/


    public void onExportUserBase()
    {
        leftAPI.exportUserBase();
    }

    public void onExportProfile()
    {
        profileAPI.exportProfile();
    }



    public void onExportTherapyComplex()
    {
        if(ComplexTable.getInstance().getSelectedItems().isEmpty()) return;

       final ObservableList<TherapyComplex> selectedItems = ComplexTable.getInstance().getSelectedItems();
        exportTherapyComplexes(selectedItems);

    }

    /**
     * Экспорт выбранных комплексов в файл
     * @param complexes
     */
    public void exportTherapyComplexes(List<TherapyComplex> complexes)
    {
        complexAPI.exportTherapyComplexes(complexes);
    }

    public void onImportProfile()
    {
         profileAPI.importProfile();
    }

    /**
     * пользователь может не выбрать раздел ни в выборе базы, ни в выборе разделов 2 уровня ни в дереве. Также можно выбрать любой раздел.
     * В нем создастся контейнер
     */
    public void onImportUserBase()
    {
        leftAPI.importUserBase();
    }

    public void onImportTherapyComplex()
    {
        Profile profile = ProfileTable.getInstance().getSelectedItem();
        importTherapyComplex(profile,nums -> {
            ProfileTable.getInstance().select(profile);

            List<TherapyComplex> lastTherapyComplexes = this.getModel().getLastTherapyComplexes(nums);
            if(!lastTherapyComplexes.isEmpty())
            {

                ComplexTable.getInstance().getAllItems().addAll(lastTherapyComplexes);

            }
            profileAPI.enableGenerateBtn();

        });
    }

    /**
     * Импорт комплексов из файла
     * @param profile
     * @param afterAction выполняется после всего, в случае успеха. Передается параметр ему кол-во импортированных комплексов
     */
    public void importTherapyComplex(Profile profile, Consumer<Integer> afterAction)
    {
        complexAPI.importTherapyComplex(profile,afterAction);
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


    public void onRemoveProfileFiles()
    {
       profileAPI.removeProfileFiles();

    }



    public void onReadProfileFromTrinity(){

        try {
            M2BinaryFile m2BinaryFile =null;
           try {
               m2BinaryFile = M2.readFromDevice(true);
           }catch (M2.ReadFromDeviceException e){
               try {
                   m2BinaryFile = M2.readFromDevice(true);
               }catch (M2.ReadFromDeviceException e1){
                   try {
                       m2BinaryFile = M2.readFromDevice(true);
                   }catch (M2.ReadFromDeviceException e2){
                       throw e2;
                   }
               }
           }
            List<TherapyComplex> tcs=new ArrayList<>();
            Map<Long,List<TherapyProgram>> programsCache=new HashMap<>();
            m2ui.parseFile(m2BinaryFile,tcs,programsCache);


            Calendar cal = Calendar.getInstance();
            Profile profile = getModel().createProfile("Trinity_" + cal.get(Calendar.DAY_OF_MONTH)+"_"+(cal.get(Calendar.MONTH)+1)+"_"+cal.get(Calendar.YEAR));
            for (TherapyComplex tc : tcs) {
                TherapyComplex tcn=getModel().createTherapyComplex("",profile,tc.getName(),tc.getDescription(),tc.getTimeForFrequency(),tc.getBundlesLength());
                if(tcn==null) break;
                for (TherapyProgram tp : programsCache.get(tc.getId())) {
                    getModel().createTherapyProgram("",tcn,tp.getName(),"",tp.getFrequencies());
                }
            }



            Platform.runLater(() -> {

                ProfileTable.getInstance().getAllItems().add(profile);
                ProfileTable.getInstance().select(profile);
                ProfileTable.getInstance().scrollTo(profile);
            therapyTabPane.getSelectionModel().select(0);

            });

        } catch (M2.ReadFromDeviceException e) {
            Platform.runLater(() -> {
                showExceptionDialog(res.getString("app.ui.reading_device"),res.getString("app.error"),res.getString("trinity_should"), e, getApp().getMainWindow(),Modality.WINDOW_MODAL);

            });

        } catch (Exception e) {
         logger.error("Ошибка парсинга времени",e);
            Platform.runLater(() -> {
                showExceptionDialog(res.getString("app.ui.reading_device"),res.getString("app.title116"),"",e,getApp().getMainWindow(),Modality.WINDOW_MODAL);
            });
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
     * Загрузжает профиль из папки
     */
    public void onLoadProfileDir()
    {
        profileAPI.loadProfileDir();
    }

    /**
     * Импорт терап.комплексов из папки
     */
    public void onImportComplex()
    {
        complexAPI.importComplexFromDir();
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
                        //удалить из таблицы профилей профили, комплесы и програмы
                        ProfileTable.getInstance().getAllItems().clear();
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
                    if(count_profiles==0){
                        ProfileTable.getInstance().getAllItems().addAll(getModel().findAllProfiles().stream()
                                                                 .filter(i->!i.getName().equals(App.BIOFON_PROFILE_NAME))
                        .collect(Collectors.toList()));
                    }
                    else if(count_profiles<getModel().countProfile()){
                        ProfileTable.getInstance().getAllItems().addAll( getModel().findAllProfiles().subList(count_profiles,getModel().countProfile()).stream()
                                                                  .filter(i->!i.getName().equals(App.BIOFON_PROFILE_NAME))
                                                                  .collect(Collectors.toList()));
                    }

                    biofonUIUtil.reloadComplexes();

                    int i =  ProfileTable.getInstance().getAllItems().size()-1;
                    ProfileTable.getInstance().requestFocus();
                    ProfileTable.getInstance().select(i);
                    ProfileTable.getInstance().scrollTo(i);
                    ProfileTable.getInstance().setItemFocus(i);

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

    public void onClearTrinity(){
        if(!m2Connected.get()) return;

        Optional<ButtonType> buttonType = showConfirmationDialog(res.getString("app.menu.clear_trinity"), "", res.getString("app.clear_trinity_ask"), getApp().getMainWindow(), Modality.WINDOW_MODAL);
        if(buttonType.get()!=okButtonType) return;

        Task task = new Task() {
            protected Boolean call() {
                try {
                    M2.clearDevice(true);
                    return true;
                } catch (M2.WriteToDeviceException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        };

        task.setOnScheduled((event) -> {
            Waiter.openLayer(getApp().getMainWindow(), true);
        });
        task.setOnFailed(ev -> {
            Waiter.closeLayer();
            showErrorDialog(res.getString("app.ui.write_error"), "", "", getApp().getMainWindow(), Modality.WINDOW_MODAL);
        });

        task.setOnSucceeded(ev -> {
                    Waiter.closeLayer();
                    if (((Boolean) task.getValue()).booleanValue()) {
                        showInfoDialog(res.getString("app.success"),res.getString("app.success"),"",getApp().getMainWindow(),Modality.WINDOW_MODAL);

                    } else
                        showErrorDialog(res.getString("app.ui.write_error"), res.getString("app.error"), "",  getApp().getMainWindow(), Modality.WINDOW_MODAL);
                }
        );
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    protected void onCompletedInitialise() {
        if(getModel().isAutoUpdateEnable()){
            System.out.println("Current starter version = "+App.getStarterVersion());
            AutoUpdater.getAutoUpdater().startUpdater(App.getStarterVersion(), new AutoUpdater.Listener() {
                @Override
                public void taskCompleted() {
                    try {
                        Platform.runLater(() -> Waiter.openLayer(getApp().getMainWindow(),true));
                        AutoUpdater.getAutoUpdater().performUpdateTask(new UpdateTask.UpdateListener() {
                            @Override
                            public void progress(int i) {

                            }

                            @Override
                            public void completed() {
                                Platform.runLater(() ->  Waiter.closeLayer());
                                System.out.println("Обновлен Starter");
                            }

                            @Override
                            public void error(UpdateException e) {
                               e.printStackTrace();
                                Log.logger.error("Ошибка обновления starter",e);
                                Platform.runLater(() ->  Waiter.closeLayer());
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.logger.error("",e);
                    }

                }

                @Override
                public void error(Exception e) {
                    e.printStackTrace();
                    Log.logger.error("",e);
                }

                @Override
                public void completeFile(String name) {

                }

                @Override
                public void currentFileProgress(float val) {

                }

                @Override
                public void nextFileStartDownloading(String name) {

                }

                @Override
                public void totalProgress(float val) {

                }
            });
        }


        try {
            USBHelper.startHotPlugListener();
        } catch (USBHelper.USBException e) {
            Log.logger.error(e);
            showExceptionDialog("Детектирование USB подключений","Ошибка!","",e,getApp().getMainWindow(),Modality.WINDOW_MODAL);
        }
    }


    public void onReference(){
        try {
            openDialogNotModal(getApp().getMainWindow(),"/fxml/ReferenceBook.fxml",res.getString("app.menu.reference"),true,StageStyle.DECORATED,600,900,0,0,"/styles/Styles.css");
        } catch (IOException e) {
            logger.error("",e);
        }
    }

    public void onPrintProfile(){
        profileAPI.printProfile();
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
