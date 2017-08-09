package ru.biomedis.biomedismair3;

import com.sun.javafx.scene.control.skin.TableViewSkin;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.*;
import javafx.util.Duration;
import org.anantacreative.updater.Update.UpdateException;
import org.anantacreative.updater.Update.UpdateTask;
import ru.biomedis.biomedismair3.Layouts.BiofonTab.BiofonTabController;
import ru.biomedis.biomedismair3.Layouts.BiofonTab.BiofonUIUtil;
import ru.biomedis.biomedismair3.Layouts.LeftPanel.LeftPanelAPI;
import ru.biomedis.biomedismair3.Layouts.LeftPanel.NamedTreeItem;
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
import ru.biomedis.biomedismair3.TherapyTabs.TablesCommon;
import ru.biomedis.biomedismair3.UpdateUtils.FrequenciesBase.*;
import ru.biomedis.biomedismair3.UserUtils.CreateBaseHelper;
import ru.biomedis.biomedismair3.UserUtils.Export.ExportProfile;
import ru.biomedis.biomedismair3.UserUtils.Export.ExportTherapyComplex;
import ru.biomedis.biomedismair3.UserUtils.Export.ExportUserBase;
import ru.biomedis.biomedismair3.UserUtils.Import.ImportProfile;
import ru.biomedis.biomedismair3.UserUtils.Import.ImportTherapyComplex;
import ru.biomedis.biomedismair3.UserUtils.Import.ImportUserBase;
import ru.biomedis.biomedismair3.entity.*;
import ru.biomedis.biomedismair3.m2.M2;
import ru.biomedis.biomedismair3.m2.M2BinaryFile;
import ru.biomedis.biomedismair3.utils.Date.DateUtil;
import ru.biomedis.biomedismair3.utils.Disk.DiskDetector;
import ru.biomedis.biomedismair3.utils.Disk.DiskSpaceData;
import ru.biomedis.biomedismair3.utils.Files.*;
import ru.biomedis.biomedismair3.utils.OS.OSValidator;
import ru.biomedis.biomedismair3.utils.Text.TextUtil;
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
import static ru.biomedis.biomedismair3.TherapyTabs.Profile.ProfileController.checkBundlesLength;

public class AppController  extends BaseController {

    public static final int MAX_BUNDLES=7;
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

    private TableViewSkin<?> tableSkin;
    private VirtualFlow<?> virtualFlow;

    private Path devicePath=null;//путь ку устройству или NULL если что-то не так
    private String fsDeviceName="";
    private SimpleBooleanProperty connectedDevice =new SimpleBooleanProperty(false);//подключено ли устройство


    private Image imageDone;
    private Image imageCancel;

    private Image imageSeq;
    private Image imageParallel;

    private Image imageDeviceOff;
    private Image imageDeviceOn;

    private  ResourceBundle res;
    private Tooltip diskSpaceTooltip=new Tooltip();
    private boolean stopGCthread=false;

    private static M2UI m2ui;


    private boolean therapyProgramsCopied=false;

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

    private List<MenuItem> tablesMenuHelper;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        res=rb;
        initNamesTables();

        dataPathMenuItem.setVisible(OSValidator.isWindows());//видимость пункта меню для введения пути к папки данных, только на винде!
        clearTrinityItem.disableProperty().bind(m2Connected.not());
        updateBaseMenu.setVisible(getApp().isUpdateBaseMenuVisible());

        initVerticalDivider();

        diskSpaceBar.setVisible(false);
        diskSpaceBar.setTooltip(diskSpaceTooltip);
        hackTooltipStartTiming(diskSpaceTooltip, 250, 15000);

        initDoneCancelImages();
        initSeqParallelImages();

        initGCRunner();
        getApp().addCloseApplistener(()->{
            setStopGCthread();
            DiskDetector.stopDetectingService();
        } );


        initDeviceMDetection(rb);

        try{
            progressAPI = (ProgressAPI) addContentHBox("/fxml/ProgressPane.fxml",topPane);
        }catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка инициализации панели програесса ",e);
        }


        try {
            leftAPI = initLeftPanel();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка инициализации Левой панели",e);
        }


        initSectionTreeActionListener();

        tablesMenuHelper = initContextMenuHotKeyHolders();

        try {
            profileAPI = initProfileTab();
            initProfileSelectedListener();
        } catch (Exception e) {
            e.printStackTrace();

            throw new RuntimeException("Ошибка инициализации панели профилей",e);
        }


        try {
            complexAPI = initComplexTab();
            initTabComplexNameListener();
        } catch (Exception e) {
            e.printStackTrace();

            throw new RuntimeException("Ошибка инициализации панели комплексов",e);
        }

        try {
            programAPI = initProgramTab();

        } catch (Exception e) {
            e.printStackTrace();

            throw new RuntimeException("Ошибка инициализации панели комплексов",e);
        }

        biofonUIUtil = initBiofon();

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



    private void initProfileSelectedListener() {
        ProfileTable.getInstance().getSelectedItemProperty().addListener((observable, oldValue, newValue) ->
        {
            if (oldValue != newValue) {
                //закроем кнопки спинера времени на частоту
               complexAPI.hideSpinners();

                ComplexTable.getInstance().getAllItems().clear();
                //добавляем через therapyComplexItems иначе не будет работать event на изменение элементов массива и не будут работать галочки мультичастот

                List<TherapyComplex> therapyComplexes = getModel().findTherapyComplexes(newValue);
                try {
                    checkBundlesLength(therapyComplexes);
                } catch (Exception e) {
                    Log.logger.error("",e);
                    showExceptionDialog("Ошибка обновления комплексов","","",e,getApp().getMainWindow(), Modality.WINDOW_MODAL);
                    return;
                }
                ComplexTable.getInstance().getAllItems().addAll(therapyComplexes);


                if(newValue!=null){
                    if(getModel().isNeedGenerateFilesInProfile(newValue)) profileAPI.enableGenerateBtn();
                    else profileAPI.disableGenerateBtn();
                }
            }

        });
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


    /**
     * Балансирует положение разделителей сплитера для удобства
     */
    private void balanceSpitterDividers()
    {
        SplitPane.Divider divider1 = splitOuter.getDividers().get(0);
        divider1.setPosition(0.25);
    }

    private LeftPanelAPI initLeftPanel() throws Exception {
            return  (LeftPanelAPI) replaceContent("/fxml/LeftPanel.fxml",leftLayout);
    }

    private ProgramAPI initProgramTab() throws Exception {
        ProgramController pp = (ProgramController)replaceContent("/fxml/ProgramTab.fxml", programLayout);
        pp.setTherapyTabPane(therapyTabPane);
        pp.setPasteInTables(this::pasteInTables);
        pp.setCutInTables(this::cutInTables);
        pp.setDeleteInTable(this::deleteInTables);
        pp.setCopyInTable(this::copyInTables);
        pp.setTherapyProgramsCopiedFunc(()->therapyProgramsCopied);
        return pp;
    }
    private ComplexAPI initComplexTab() throws Exception {
        ComplexController cc = (ComplexController)replaceContent("/fxml/ComplexTab.fxml", complexLayout);
        cc.setTherapyTabPane(therapyTabPane);
        cc.setDevicePathMethod(()->devicePath);
        cc.setPasteInTables(this::pasteInTables);
        cc.setCutInTables(this::cutInTables);
        cc.setDeleteInTable(this::deleteInTables);
        cc.setCopyInTable(this::copyInTables);
        return cc;
    }

    private ProfileAPI initProfileTab() throws Exception {
        ProfileController pc = (ProfileController)replaceContent("/fxml/ProfileTab.fxml", profileLayout);
        pc.setDevicePathMethod(()->devicePath);
        pc.setTherapyTabPane(therapyTabPane);
        pc.setPasteInTables(this::pasteInTables);
        pc.setCutInTables(this::cutInTables);
        pc.setDeleteInTable(this::deleteInTables);
        pc.setDevicesProperties(m2Ready,connectedDevice,m2Connected);

        return pc;
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

    private void initSeqParallelImages() {
        URL location;
        location = getClass().getResource("/images/seq_16.png");
        imageSeq=new Image(location.toExternalForm());
        location = getClass().getResource("/images/parallel_16.png");
        imageParallel=new Image(location.toExternalForm());
    }

    private void initDoneCancelImages() {
        URL location = getClass().getResource("/images/done.png");
        imageDone=new Image(location.toExternalForm());
        location = getClass().getResource("/images/cancel.png");
        imageCancel=new Image(location.toExternalForm());
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



    private void initNamesTables() {
        baseProfileTabName=res.getString("app.ui.tab1");
        baseComplexTabName=res.getString("app.ui.tab2");
        baseProgramTabName=res.getString("app.ui.tab3");
    }



    private SimpleBooleanProperty m2Ready =new SimpleBooleanProperty(false);
    private SimpleBooleanProperty m2Connected=new SimpleBooleanProperty(false);
    private void initUSBDetectionM2() {
        USBHelper.addPlugEventHandler(M2.productId, M2.vendorId, new PlugDeviceListener() {
            @Override
            public void onAttachDevice() {

                try {
                Thread.sleep(1000);
                    //M2BinaryFile m2BinaryFile = M2.readFromDevice(true);
                    M2BinaryFile m2BinaryFile = new M2BinaryFile();
                    Platform.runLater(() -> {
                                m2ui.setContent(m2BinaryFile);
                                m2Ready.setValue(true);
                            });

                    System.out.println("Устройство Trinity подключено");
                } /*catch (M2.ReadFromDeviceException e) {
                   Platform.runLater(() -> {
                       showExceptionDialog(res.getString("app.ui.reading_device"),res.getString("app.error"),"", e, getApp().getMainWindow(),Modality.WINDOW_MODAL);
                   });


                }*/ catch (InterruptedException e) {
                    e.printStackTrace();
                }finally {
                    Platform.runLater(() ->   m2Connected.set(true));
                    m2Ready.setValue(true);
                    System.out.println("Устройство Trinity подключено");
                }



            }

            @Override
            public void onDetachDevice() {
                System.out.println("Устройство Trinity отключено");
                Platform.runLater(() ->   {
                    m2Connected.set(false);
                    m2Ready.setValue(false);
                    m2ui.cleanView();
                });

            }
        });
    }



    /******* Биофон *****/




    private void addComplexToBiofonTab(TherapyComplex tc)
    {
        biofonUIUtil.addComplex(tc);
    }
    private void addProgramToBiofonTab(TherapyComplex tc,TherapyProgram tp)
    {
        biofonUIUtil.addProgram(tp);
    }







/**********************************************/










            private String getComplexTabName(){
               return therapyTabPane.getTabs().get(1).getText();

            }
    private void setComplexTabName(String val){
         Platform.runLater(() -> therapyTabPane.getTabs().get(1).setText(val));

    }


    private boolean isProfileTabSelected(){
        return 0 == therapyTabPane.getSelectionModel().getSelectedIndex();
    }
    private boolean isComplexesTabSelected(){
        return 1 == therapyTabPane.getSelectionModel().getSelectedIndex();
    }
    private boolean isProgramsTabSelected(){
        return 2 == therapyTabPane.getSelectionModel().getSelectedIndex();
    }






    private List<MenuItem> initContextMenuHotKeyHolders() {
        tablesMenuHelper=new ArrayList<>();
        MenuItem mh1 = new MenuItem(this.res.getString("app.ui.copy"));
        MenuItem mh2 =new MenuItem(this.res.getString("app.ui.paste"));
        MenuItem mh3 =new MenuItem(this.res.getString("app.cut"));
        MenuItem mh4 =new MenuItem(this.res.getString("app.delete"));

        tablesMenuHelper.add(mh1);
        tablesMenuHelper.add(mh2);
        tablesMenuHelper.add(mh3);
        tablesMenuHelper.add(mh4);

        mh1.setAccelerator(KeyCombination.keyCombination("Ctrl+X"));
        mh2.setAccelerator(KeyCombination.keyCombination("Ctrl+C"));
        mh3.setAccelerator(KeyCombination.keyCombination("Ctrl+V"));
        mh4.setAccelerator(KeyCombination.keyCombination("Delete"));

        mh1.setOnAction(e->cutInTables());
        mh1.setOnAction(e->copyInTables());
        mh1.setOnAction(e->pasteInTables());
        mh1.setOnAction(e->deleteInTables());
        return  tablesMenuHelper;
    }



    private void onRemoveProfile()
    {
       profileAPI.removeProfile();

    }
    private void onRemoveComplex()
    {
        complexAPI.removeComplex();

    }

    public void onPrintComplex(){
        complexAPI.printComplex();
    }
    public void onRemovePrograms(){
        programAPI.removePrograms();
    }


    private void deleteInTables() {
        if(isProgramsTabSelected()) onRemovePrograms();
        else if(isComplexesTabSelected()) onRemoveComplex();
        else if(isProfileTabSelected()) onRemoveProfile();
    }

    private void pasteInTables() {
        if(isProgramsTabSelected()) pasteTherapyPrograms();
        else if(isComplexesTabSelected()) pasteTherapyComplexes();
        else if(isProfileTabSelected()) pasteProfile();
    }

    private void copyInTables() {
        if(isProgramsTabSelected()) copySelectedTherapyProgramsToBuffer();
        else if(isComplexesTabSelected()) copySelectedTherapyComplexesToBuffer();

    }

    private void cutInTables() {
        if(isProgramsTabSelected()) cutSelectedTherapyProgramsToBuffer();
        else if(isComplexesTabSelected()) cutSelectedTherapyComplexesToBuffer();
        else if(isProfileTabSelected()) cutProfileToBuffer();
    }

    private void pasteProfile() {
        Profile profile = ProfileTable.getInstance().getSelectedItem();
        if(profile==null) return;
        int dropIndex= ProfileTable.getInstance().getSelectedIndex();

        Clipboard clipboard= Clipboard.getSystemClipboard();
        if(!clipboard.hasContent(ProfileTable.PROFILE_CUT_ITEM_ID)) return;
        if(!clipboard.hasContent(ProfileTable.PROFILE_CUT_ITEM_INDEX)) return;


            Integer ind = (Integer) clipboard.getContent(ProfileTable.PROFILE_CUT_ITEM_INDEX);
            if (ind == null) return;
            else {
                if (dropIndex == ind) return;
                else {
                   Profile movedProfile = ProfileTable.getInstance().getAllItems().get(ind);
                   if(movedProfile==null) {
                       clipboard.clear();
                       return;
                   }
                    ProfileTable.getInstance().getAllItems().remove(movedProfile);
                    ProfileTable.getInstance().getAllItems().add(dropIndex,movedProfile);
                    Profile tmp;
                    try {
                    for (int i=0; i<ProfileTable.getInstance().getAllItems().size();i++){
                        tmp = ProfileTable.getInstance().getAllItems().get(i);
                        tmp.setPosition(i);
                        getModel().updateProfile(tmp);
                    }
                    } catch (Exception e) {
                        e.printStackTrace();
                        clipboard.clear();
                        return;
                    }



                }
            }

        clipboard.clear();
    }


    private void cutProfileToBuffer() {
        Profile profile = ProfileTable.getInstance().getSelectedItem();
        if(profile==null) return;
        Clipboard clipboard=Clipboard.getSystemClipboard();
        clipboard.clear();
        ClipboardContent content = new ClipboardContent();
        content.put(ProfileTable.PROFILE_CUT_ITEM_ID, profile.getId());
        content.put(ProfileTable.PROFILE_CUT_ITEM_INDEX, ProfileTable.getInstance().getSelectedIndex());
        clipboard.setContent(content);
    }




    private void cutSelectedTherapyProgramsToBuffer() {
        if (ProgramTable.getInstance().getSelectedItems().isEmpty()) return;
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.clear();

        content.put(ProgramTable.PROGRAM_CUT_ITEM_INDEX, ProgramTable.getInstance().getSelectedIndexes().toArray(new Integer[0]));
        content.put(ProgramTable.PROGRAM_CUT_ITEM_COMPLEX, ComplexTable.getInstance().getSelectedItem().getId());
        content.put(ProgramTable.PROGRAM_CUT_ITEM_ID, ProgramTable.getInstance().getSelectedItems().stream()
                .map(i->i.getId())
                .collect(Collectors.toList())
                .toArray(new Long[0])
        );
        clipboard.setContent(content);
        therapyProgramsCopied=false;
    }

    private void copySelectedTherapyProgramsToBuffer() {
        if (ProgramTable.getInstance().getSelectedItems().isEmpty()) return;
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.clear();

        content.put(ProgramTable.PROGRAM_COPY_ITEM, ProgramTable.getInstance().getSelectedItems().stream()
               .map(i->i.getId()).collect(Collectors.toList()).toArray(new Long[0]));
        clipboard.setContent(content);
        therapyProgramsCopied=true;
    }

    /**
     * Вырезать комплексы
     */
    private void cutSelectedTherapyComplexesToBuffer() {
        if (ComplexTable.getInstance().getSelectedItems().isEmpty()) return;
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.clear();

        content.put(ComplexTable.COMPLEX_CUT_ITEM_INDEX, ComplexTable.getInstance().getSelectedIndexes().toArray(new Integer[0]));
        content.put(ComplexTable.COMPLEX_CUT_ITEM_PROFILE, ProfileTable.getInstance().getSelectedItem().getId());
        content.put(ComplexTable.COMPLEX_CUT_ITEM_ID, ComplexTable.getInstance().getSelectedItems().stream()
                .map(i->i.getId())
                .collect(Collectors.toList())
                .toArray(new Long[0])
        );
        clipboard.setContent(content);

    }



    private void pasteTherapyComplexes() {
        if(ComplexTable.getInstance().getSelectedItems().size()>1){
            showWarningDialog(res.getString("app.ui.insertion_elements"),res.getString("app.ui.insertion_not_allowed"),res.getString("app.ui.ins_not_av_mess"),getApp().getMainWindow(),Modality.WINDOW_MODAL);
            return;
        }
        Clipboard clipboard=Clipboard.getSystemClipboard();

        if(clipboard.hasContent(ComplexTable.COMPLEX_COPY_ITEM))pasteTherapyComplexesByCopy();
        else pasteTherapyComplexesByCut();
    }

    private void pasteTherapyComplexesByCopy(){
        Profile profile = ProfileTable.getInstance().getSelectedItem();
        if(profile==null) return;
        if (ComplexTable.getInstance().getSelectedItems().size()>1) return;
        Clipboard clipboard = Clipboard.getSystemClipboard();

        if (!clipboard.hasContent(ComplexTable.COMPLEX_COPY_ITEM)) return;
        Long[] ids = (Long[]) clipboard.getContent(ComplexTable.COMPLEX_COPY_ITEM);
        if(ids==null) return;
        if(ids.length==0) return;

        int dropIndex = ComplexTable.getInstance().getSelectedIndex();

        List<TherapyComplex> therapyComplexes =  Arrays.stream(ids)
                .map(i->getModel().findTherapyComplex(i))
                .filter(i->i!=null)
                .collect(Collectors.toList());


        if (ComplexTable.getInstance().getSelectedItems().isEmpty()) {
            //вставка просто в конец таблицы
            try {
                ComplexTable.getInstance().clearSelection();
                for (TherapyComplex tc : therapyComplexes) {

                    TherapyComplex tpn = getModel().copyTherapyComplexToProfile(profile, tc);
                    if(tpn==null) continue;
                    ComplexTable.getInstance().getAllItems().add(tpn);
                    ComplexTable.getInstance().select(tpn);

                }

                profileAPI.updateProfileTime(profile);
            } catch (Exception e1) {
                logger.error(e1);
                showExceptionDialog("Ошибка копирования комплексов","","",e1,getApp().getMainWindow(), Modality.WINDOW_MODAL);

                therapyComplexes.clear();
                clipboard.clear();
                return;
            }
        }else {
            //вставка до выбранного элемента со сдвигом остальных
            List<TherapyComplex> tpl=new ArrayList<>();
            try {
                ComplexTable.getInstance().clearSelection();
                for (TherapyComplex tc : therapyComplexes) {
                    TherapyComplex tcn = getModel().copyTherapyComplexToProfile(profile, tc);
                    if(tcn==null) continue;
                    tpl.add(tcn);
                }
                List<TherapyComplex> tpSlided =  ComplexTable.getInstance().getAllItems().subList(dropIndex,  ComplexTable.getInstance().getAllItems().size());
                long posFirstSlidingElem =  ComplexTable.getInstance().getAllItems().get(dropIndex).getPosition();

                for (TherapyComplex tp : tpSlided) {
                    tp.setPosition(tp.getPosition()+tpl.size());
                    getModel().updateTherapyComplex(tp);
                }
                int cnt=0;
                for (TherapyComplex tp : tpl) {
                    tp.setPosition(posFirstSlidingElem + cnt++);
                    getModel().updateTherapyComplex(tp);
                }

                ComplexTable.getInstance().getAllItems().addAll(dropIndex,tpl);
                for (TherapyComplex tp : tpl) {
                    ComplexTable.getInstance().select(tp);
                }
                profileAPI.updateProfileTime(profile);
            } catch (Exception e1) {
                logger.error(e1);
                showExceptionDialog("Ошибка копирования комплексов","","",e1,getApp().getMainWindow(), Modality.WINDOW_MODAL);

                therapyComplexes.clear();
                clipboard.clear();
                return;
            }

        }


        therapyComplexes.clear();
        clipboard.clear();
    }
    private void pasteTherapyComplexesByCut(){
        //в выбранный индекс вставляется новый элемент а все сдвигаются на 1 индекс  вырезанного индекса

        Clipboard clipboard = Clipboard.getSystemClipboard();

        if (!clipboard.hasContent(ComplexTable.COMPLEX_CUT_ITEM_PROFILE)) return;
        if (!clipboard.hasContent(ComplexTable.COMPLEX_CUT_ITEM_ID)) return;
        if (!clipboard.hasContent(ComplexTable.COMPLEX_CUT_ITEM_INDEX)) return;
        try {
            Profile selectedProfile=ProfileTable.getInstance().getSelectedItem();
            Long idProfile = (Long) clipboard.getContent(ComplexTable.COMPLEX_CUT_ITEM_PROFILE);
            if(idProfile==null)return;

            else if(idProfile.longValue()==selectedProfile.getId().longValue()){
                //вставка в текущем профиле
                if ( ComplexTable.getInstance().getSelectedItems().isEmpty()) return;
                if ( ComplexTable.getInstance().getSelectedItems().size()!=1) return;

                Integer[] indexes = (Integer[]) clipboard.getContent(ComplexTable.COMPLEX_CUT_ITEM_INDEX);
                if(indexes==null) return;
                if(indexes.length==0) return;
                List<Integer> ind = Arrays.stream(indexes).collect(Collectors.toList());
                int dropIndex = ComplexTable.getInstance().getSelectedIndex();

                if(!TablesCommon.isEnablePaste(dropIndex,indexes)) {
                    showWarningDialog(res.getString("app.ui.moving_items"),"",res.getString("app.ui.can_not_move_to_pos"),getApp().getMainWindow(),Modality.WINDOW_MODAL);
                    return;
                }

                List<TherapyComplex> therapyComplexes = ind.stream().map(i->ComplexTable.getInstance().getAllItems().get(i)).collect(Collectors.toList());
                int startIndex=ind.get(0);//первый индекс вырезки
                int lastIndex=ind.get(ind.size()-1);

//элементы всегда будут оказываться выше чем индекс по которому вставляли, те визуально вставляются над выбираемым элементом

                if(dropIndex < startIndex){

                    for (TherapyComplex i : therapyComplexes) {
                        ComplexTable.getInstance().getAllItems().remove(i);
                    }
                    //вставка программ в dropIndex; Изменение их позиции
                    ComplexTable.getInstance().getAllItems().addAll(dropIndex,therapyComplexes);
                }else if(dropIndex > lastIndex){

                    TherapyComplex dropComplex = ComplexTable.getInstance().getAllItems().get(dropIndex);
                    for (TherapyComplex i : therapyComplexes) {
                        ComplexTable.getInstance().getAllItems().remove(i);
                    }
                    dropIndex= ComplexTable.getInstance().getAllItems().indexOf(dropComplex);
                    ComplexTable.getInstance().getAllItems().addAll(dropIndex,therapyComplexes);


                }else return;

                int i=0;
                for (TherapyComplex tp : ComplexTable.getInstance().getAllItems()) {
                    tp.setPosition((long)(i++));
                    getModel().updateTherapyComplex(tp);
                }
                profileAPI.updateProfileTime(selectedProfile);

                therapyComplexes.clear();

            }else {
                //вставка в другом профиле. Нужно вырезать и просто вставить в указанном месте



                Long[] ids = (Long[]) clipboard.getContent(ComplexTable.COMPLEX_CUT_ITEM_ID);

                if(ids==null) return;
                if(ids.length==0) return;
                List<Long> ind = Arrays.stream(ids).collect(Collectors.toList());
                int dropIndex =-1;
                if (ComplexTable.getInstance().getSelectedItem()!=null)dropIndex = ComplexTable.getInstance().getSelectedIndex();
                else if(ComplexTable.getInstance().getAllItems().size()==0) dropIndex=0;

                List<TherapyComplex> movedTP = ind.stream()
                        .map(i->getModel().findTherapyComplex(i))
                        .filter(i->i!=null)
                        .collect(Collectors.toList());

                Profile srcProfile=null;
                if(movedTP.size()>0){
                    Optional<Profile> first = ProfileTable.getInstance().getAllItems().stream().filter(p -> p.getId().longValue() == idProfile.longValue()).findFirst();
                    srcProfile=first.orElse(null);
                }
                //просто вставляем
                if(dropIndex==-1)ComplexTable.getInstance().getAllItems().addAll(movedTP);
                else  ComplexTable.getInstance().getAllItems().addAll(dropIndex,movedTP);
                //теперь все обновляем
                int i=0;
                for (TherapyComplex tp : ComplexTable.getInstance().getAllItems()) {
                    tp.setPosition((long)(i++));
                    tp.setProfile(selectedProfile);
                    getModel().updateTherapyComplex(tp);
                }

                if(movedTP.size()>0){
                   profileAPI.updateProfileTime(selectedProfile);
                    profileAPI.updateProfileTime( srcProfile);

                }
                movedTP.clear();
            }

        } catch (Exception e1) {
            logger.error(e1);
            showExceptionDialog("Ошибка обновления позиции комплексов","","",e1,getApp().getMainWindow(), Modality.WINDOW_MODAL);

            return;
        }finally {
            clipboard.clear();
        }
    }

    private void copySelectedTherapyComplexesToBuffer() {
        if (ComplexTable.getInstance().getSelectedItems().isEmpty()) return;
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.clear();
        content.put(ComplexTable.COMPLEX_COPY_ITEM,
                ComplexTable.getInstance().getSelectedItems().stream().map(i->i.getId()).collect(Collectors.toList()).toArray(new Long[0]));
        clipboard.setContent(content);

    }
    private void pasteTherapyProgramsByCopy(){
        TherapyComplex therapyComplex = ComplexTable.getInstance().getSelectedItem();
        if(therapyComplex==null) return;
        if (ProgramTable.getInstance().getSelectedItems().size()>1) return;
        Clipboard clipboard = Clipboard.getSystemClipboard();

        if (!clipboard.hasContent(ProgramTable.PROGRAM_COPY_ITEM)) return;
        Long[] ids = (Long[]) clipboard.getContent(ProgramTable.PROGRAM_COPY_ITEM);
        if(ids==null) return;
        if(ids.length==0) return;

        int dropIndex = ProgramTable.getInstance().getSelectedIndex();

        List<TherapyProgram> therapyPrograms =  Arrays.stream(ids)
                .map(i->getModel().getTherapyProgram(i))
                .filter(i->i!=null)
                .collect(Collectors.toList());


        if (ProgramTable.getInstance().getSelectedItems().isEmpty()) {
            //вставка просто в конец таблицы
            try {
                ProgramTable.getInstance().clearSelection();
                for (TherapyProgram therapyProgram : therapyPrograms) {

                    TherapyProgram tp = getModel().copyTherapyProgramToComplex(therapyComplex, therapyProgram);
                    if(tp==null) continue;
                    ProgramTable.getInstance().getAllItems().add(tp);
                    ProgramTable.getInstance().select(tp);

                }
                complexAPI.updateComplexTime(therapyComplex,true);
                //updateProfileTime(tableProfile.getSelectionModel().getSelectedItem());
            } catch (Exception e1) {
                logger.error(e1);
                showExceptionDialog("Ошибка копирования программ","","",e1,getApp().getMainWindow(), Modality.WINDOW_MODAL);
                therapyProgramsCopied=false;
                therapyPrograms.clear();
                clipboard.clear();
                return;
            }
        }else {
            //вставка до выбранного элемента со сдвигом остальных
            List<TherapyProgram> tpl=new ArrayList<>();
            try {
                ProgramTable.getInstance().clearSelection();
                for (TherapyProgram therapyProgram : therapyPrograms) {
                    TherapyProgram tp = getModel().copyTherapyProgramToComplex(therapyComplex, therapyProgram);
                    if(tp==null) continue;
                    tpl.add(tp);
                }
                List<TherapyProgram> tpSlided =  ProgramTable.getInstance().getAllItems()
                                                             .subList(dropIndex,  ProgramTable.getInstance().getAllItems().size());
                long posFirstSlidingElem =  ProgramTable.getInstance().getAllItems().get(dropIndex).getPosition();

                for (TherapyProgram tp : tpSlided) {
                    tp.setPosition(tp.getPosition()+tpl.size());
                    getModel().updateTherapyProgram(tp);
                }
                int cnt=0;
                for (TherapyProgram tp : tpl) {
                    tp.setPosition(posFirstSlidingElem + cnt++);
                    getModel().updateTherapyProgram(tp);
                }

                ProgramTable.getInstance().getAllItems().addAll(dropIndex,tpl);
                for (TherapyProgram tp : tpl) {
                    ProgramTable.getInstance().select(tp);
                }


                complexAPI.updateComplexTime(therapyComplex,true);
                //updateProfileTime(tableProfile.getSelectionModel().getSelectedItem());
            } catch (Exception e1) {
                logger.error(e1);
                showExceptionDialog("Ошибка копирования программ","","",e1,getApp().getMainWindow(), Modality.WINDOW_MODAL);
                therapyProgramsCopied=false;
                therapyPrograms.clear();
                clipboard.clear();
                return;
            }

        }

        therapyProgramsCopied=false;
        therapyPrograms.clear();
        clipboard.clear();
    }
    /**
     * Перемещение терапквтических программ через буффер обмена
     */
    private void pasteTherapyProgramsByCut() {
        //в выбранный индекс вставляется новый элемент а все сдвигаются на 1 индекс  вырезанного индекса

        Clipboard clipboard = Clipboard.getSystemClipboard();

        if (!clipboard.hasContent(ProgramTable.PROGRAM_CUT_ITEM_COMPLEX)) return;
        if (!clipboard.hasContent(ProgramTable.PROGRAM_CUT_ITEM_ID)) return;
        if (!clipboard.hasContent(ProgramTable.PROGRAM_CUT_ITEM_INDEX)) return;
        try {
        TherapyComplex selectedComplex=ComplexTable.getInstance().getSelectedItem();
        Long idComplex = (Long) clipboard.getContent(ProgramTable.PROGRAM_CUT_ITEM_COMPLEX);
        if(idComplex==null)return;
        else if(idComplex.longValue()==selectedComplex.getId().longValue()){
            //вставка в текущем комплексе
            if ( ProgramTable.getInstance().getSelectedItems().isEmpty()) return;
            if ( ProgramTable.getInstance().getSelectedItems().size()!=1) return;

            Integer[] indexes = (Integer[]) clipboard.getContent(ProgramTable.PROGRAM_CUT_ITEM_INDEX);
            if(indexes==null) return;
            if(indexes.length==0) return;
            List<Integer> ind = Arrays.stream(indexes).collect(Collectors.toList());
            int dropIndex =  ProgramTable.getInstance().getSelectedIndex();

            if(!TablesCommon.isEnablePaste(dropIndex,indexes)) {
                showWarningDialog(res.getString("app.ui.moving_items"),"",res.getString("app.ui.can_not_move_to_pos"),getApp().getMainWindow(),Modality.WINDOW_MODAL);
                return;
            }

            List<TherapyProgram> therapyPrograms = ind.stream().map(i-> ProgramTable.getInstance().getAllItems().get(i)).collect(Collectors.toList());
            int startIndex=ind.get(0);//первый индекс вырезки
            int lastIndex=ind.get(ind.size()-1);

//элементы всегда будут оказываться выше чем индекс по которому вставляли, те визуально вставляются над выбираемым элементом
            TherapyProgram dropProgram =  ProgramTable.getInstance().getAllItems().get(dropIndex);
                if(dropIndex < startIndex){

                    for (TherapyProgram i : therapyPrograms) {
                        ProgramTable.getInstance().getAllItems().remove(i);
                    }
                    //вставка программ в dropIndex; Изменение их позиции
                    ProgramTable.getInstance().getAllItems().addAll(dropIndex,therapyPrograms);
                }else if(dropIndex > lastIndex){


                    for (TherapyProgram i : therapyPrograms) {
                        ProgramTable.getInstance().getAllItems().remove(i);
                    }
                    dropIndex=  ProgramTable.getInstance().getAllItems().indexOf(dropProgram);
                    ProgramTable.getInstance().getAllItems().addAll(dropIndex,therapyPrograms);


                }else return;

                int i=0;
                for (TherapyProgram tp :  ProgramTable.getInstance().getAllItems()) {
                    tp.setPosition((long)(i++));
                    getModel().updateTherapyProgram(tp);
                }

            complexAPI.updateComplexTime(dropProgram.getTherapyComplex(),false);
            profileAPI.updateProfileTime(ProfileTable.getInstance().getSelectedItem());
            therapyPrograms.clear();

        }else {
            //вставка в другом комплексе. Нужно вырезать и просто вставить в указанном месте



            Long[] ids = (Long[]) clipboard.getContent(ProgramTable.PROGRAM_CUT_ITEM_ID);

            if(ids==null) return;
            if(ids.length==0) return;
            List<Long> ind = Arrays.stream(ids).collect(Collectors.toList());
            int dropIndex =-1;
            if ( ProgramTable.getInstance().getSelectedItem()!=null)dropIndex = ProgramTable.getInstance().getSelectedIndex();
            else if(ProgramTable.getInstance().getAllItems().size()==0) dropIndex=0;

            List<TherapyProgram> movedTP = ind.stream()
                    .map(i->getModel().getTherapyProgram(i))
                    .filter(i->i!=null)
                    .collect(Collectors.toList());
            TherapyComplex srcComplex=null;
            if(movedTP.size() > 0){
                Optional<TherapyComplex> first = ComplexTable.getInstance().getAllItems().stream().filter(p -> p.getId().longValue() == idComplex.longValue()).findFirst();
                //будет найден только если вставка в том же профиле
                srcComplex=first.orElse(null);
            }
            //просто вставляем
           if(dropIndex==-1) ProgramTable.getInstance().getAllItems().addAll(movedTP);
               else   ProgramTable.getInstance().getAllItems().addAll(dropIndex,movedTP);
            //теперь все обновляем
            int i=0;
            for (TherapyProgram tp :  ProgramTable.getInstance().getAllItems()) {
                tp.setPosition((long)(i++));
                tp.setTherapyComplex(selectedComplex);
                getModel().updateTherapyProgram(tp);
            }

            //обновление времени в таблицах
            if(movedTP.size()>0){
                complexAPI.updateComplexTime(selectedComplex,true);
                //если вставка в другом профиле то обновлять не надо
                if(srcComplex!=null) complexAPI.updateComplexTime(srcComplex,true);
                //updateProfileTime(tableProfile.getSelectionModel().getSelectedItem());
            }

            //idComplex
            movedTP.clear();
        }

        } catch (Exception e1) {
            logger.error(e1);
            showExceptionDialog("Ошибка обновления позиции программ","","",e1,getApp().getMainWindow(), Modality.WINDOW_MODAL);

            return;
        }finally {
            clipboard.clear();
        }
    }

    private void pasteTherapyPrograms(){
        if(ProgramTable.getInstance().getSelectedItems().size()>1){
            showWarningDialog(res.getString("app.ui.insertion_elements"),res.getString("app.ui.insertion_not_allowed"),res.getString("app.ui.ins_not_av_mess"),getApp().getMainWindow(),Modality.WINDOW_MODAL);
            return;
        }
        if(therapyProgramsCopied)pasteTherapyProgramsByCopy();
        else pasteTherapyProgramsByCut();
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
        Section start=null;

        List<Section> collect = leftAPI.getBaseAllItems().stream().filter(section -> "USER".equals(section.getTag())).collect(Collectors.toList());
        Section userSection=null;
        if(collect.isEmpty()) return;
        userSection=collect.get(0);
        collect=null;



        //если у не выбран раздел пользовательский
        if(!"USER".equals(leftAPI.selectedBase().getTag()) )
        {
            Optional<ButtonType> buttonType = showConfirmationDialog(res.getString("app.title20"), "", res.getString("app.title21"), getApp().getMainWindow(), Modality.WINDOW_MODAL);

            if(buttonType.isPresent() ? buttonType.get()==okButtonType :false) start= userSection; else return;

        }else
        {
            //выбрана пользовательская база.

            //не выбран раздел в комбобоксе
            if(leftAPI.selectedRootSection().getId()==0)
            {
                Optional<ButtonType> buttonType = showConfirmationDialog(res.getString("app.title22"), "", res.getString("app.title23"), getApp().getMainWindow(), Modality.WINDOW_MODAL);

                if(buttonType.isPresent() ? buttonType.get()==okButtonType :false) start= userSection; else return;

            }else if(leftAPI.selectedSectionTree()==null)
            {
                //выбран раздел в комбобоксе но не выбран в дереве
                start=leftAPI.selectedRootSection();
            }else
            {
                //выбран элемент дерева и выбран раздел

                //если выбран не раздел
                if(!(leftAPI.selectedSectionTreeItem() instanceof Section))
                {

                    showWarningDialog(res.getString("app.title24"),"",res.getString("app.title25"),getApp().getMainWindow(),Modality.WINDOW_MODAL );
                    return;


                }

                start=(Section)leftAPI.selectedSectionTreeItem();//выберем стартовым раздел
            }


        }


        //получим путь к файлу.
        File file=null;

        getModel().initStringsSection(start);
        FileChooser fileChooser =new FileChooser();
        if("USER".equals(start.getTag()))  fileChooser.setTitle(res.getString("app.title26"));
        else fileChooser.setTitle(res.getString("app.title27")+" - " + start.getNameString());
        fileChooser.setInitialDirectory(new File(getModel().getLastExportPath(System.getProperty("user.home"))));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("xmlb", "*.xmlb"));
        fileChooser.setInitialFileName("ubase.xmlb");
        file= fileChooser.showSaveDialog(getApp().getMainWindow());

        if(file==null)return;
        getModel().setLastExportPath(file.getParentFile());

        // запишем файл экспорта







            final Section sec=start;
            final File fileToSave=file;


        getProgressAPI().setProgressIndicator(res.getString("app.title28"));
        Task<Boolean> task =new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception
            {

               boolean res= ExportUserBase.export(sec, fileToSave, getModel());
                if(res==false) {this.failed();return false;}
                else return true;

            }
        };


        task.setOnRunning(event1 -> getProgressAPI().setProgressIndicator(-1.0, res.getString("app.title29")));
        task.setOnSucceeded(event ->
        {
            if (task.getValue()) getProgressAPI().setProgressIndicator(1.0, res.getString("app.title30"));
            else getProgressAPI().setProgressIndicator( res.getString("app.title31"));
            getProgressAPI().hideProgressIndicator(true);
        });

        task.setOnFailed(event -> {
            getProgressAPI().setProgressIndicator(res.getString("app.title31"));
            getProgressAPI().hideProgressIndicator(true);
        });

        Thread threadTask=new Thread(task);

        threadTask.setDaemon(true);

        threadTask.start();







    }

    public void onExportProfile()
    {
        Profile selectedItem = ProfileTable.getInstance().getSelectedItem();
        if(selectedItem == null) return;

        //получим путь к файлу.
        File file=null;


        FileChooser fileChooser =new FileChooser();
        fileChooser.setTitle(res.getString("app.title32")+" - "+selectedItem.getName());

        fileChooser.setInitialDirectory(new File(getModel().getLastExportPath(System.getProperty("user.home"))));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("xmlp", "*.xmlp"));
        fileChooser.setInitialFileName(TextUtil.replaceWinPathBadSymbols(selectedItem.getName())+".xmlp" );
        file= fileChooser.showSaveDialog(getApp().getMainWindow());

        if(file==null)return;
        final Profile prof=selectedItem;
        final File fileToSave=file;


       getModel().setLastExportPath(file.getParentFile());



        getProgressAPI().setProgressIndicator(res.getString("app.title33"));
        Task<Boolean> task =new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception
            {

                boolean res= ExportProfile.export(prof,fileToSave,getModel());
                if(res==false) {this.failed();return false;}
                else return true;

            }
        };


        task.setOnRunning(event1 -> getProgressAPI().setProgressIndicator(-1.0, res.getString("app.title34")));
        task.setOnSucceeded(event ->
        {
            if (task.getValue()) getProgressAPI().setProgressIndicator(1.0, res.getString("app.title35"));
            else getProgressAPI().setProgressIndicator(res.getString("app.title36"));
            getProgressAPI().hideProgressIndicator(true);
        });

        task.setOnFailed(event -> {
            getProgressAPI().setProgressIndicator(res.getString("app.title36"));
            getProgressAPI().hideProgressIndicator(true);
        });

        Thread threadTask=new Thread(task);

        threadTask.setDaemon(true);

        threadTask.start();

        selectedItem=null;
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
        if(complexes.isEmpty()) return;

        final List<TherapyComplex> selectedItems = complexes;

        //получим путь к файлу.
        File file=null;
String initname;

        if(selectedItems.size()>1)initname=ProfileTable.getInstance().getSelectedItem().getName();
        else initname=selectedItems.get(0).getName();

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

    public void onImportProfile()
    {
        
        //получим путь к файлу.
        File file=null;

        FileChooser fileChooser =new FileChooser();
       fileChooser.setTitle(res.getString("app.title40"));

        fileChooser.setInitialDirectory(new File(getModel().getLastExportPath(System.getProperty("user.home"))));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("xmlp", "*.xmlp"));
        file= fileChooser.showOpenDialog(getApp().getMainWindow());

        if(file==null)return;



        final File fileToSave=file;

      final  ImportProfile imp=new ImportProfile();

        Task<Boolean> task =new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception
            {

                imp.setListener(new ImportProfile.Listener() {
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
                            showErrorDialog(res.getString("app.title41"), "", res.getString("app.title42"), getApp().getMainWindow(), Modality.WINDOW_MODAL);
                        }
                        failed();

                    }
                });

                boolean res= imp.parse(fileToSave, getModel());

                if(res==false)
                {
                    imp.setListener(null);
                    this.failed();
                    return false;}
                else {
                    imp.setListener(null);
                    return true;
                }



            }


        };



        task.progressProperty().addListener((observable, oldValue, newValue) -> getProgressAPI().setProgressIndicator(newValue.doubleValue()));
        task.setOnRunning(event1 -> getProgressAPI().setProgressIndicator(0.0, res.getString("app.title43")));

        task.setOnSucceeded(event ->
        {

            if (task.getValue())
            {
                getProgressAPI().setProgressIndicator(1.0, res.getString("app.title44"));
                ProfileTable.getInstance().getAllItems().add(getModel().getLastProfile());
                profileAPI.enableGenerateBtn();


            }
            else getProgressAPI().setProgressIndicator(res.getString("app.title45"));
            getProgressAPI().hideProgressIndicator(true);
        });

        task.setOnFailed(event -> {
            getProgressAPI().setProgressIndicator(res.getString("app.title45"));
            getProgressAPI().hideProgressIndicator(true);



        });



        Thread threadTask=new Thread(task);
        threadTask.setDaemon(true);
        getProgressAPI().setProgressIndicator(0.01, res.getString("app.title46"));
        threadTask.start();






    }

    /**
     * пользователь может не выбрать раздел ни в выборе базы, ни в выборе разделов 2 уровня ни в дереве. Также можно выбрать любой раздел.
     * В нем создастся контейнер
     */
    public void onImportUserBase()
    {

        Section start=null;
        String res="";

        List<Section> collect = leftAPI.getBaseAllItems().stream().filter(section -> "USER".equals(section.getTag())).collect(Collectors.toList());
        Section userSection=null;
        if(collect.isEmpty()) return;
        userSection=collect.get(0);
        collect=null;


        if(!"USER".equals(leftAPI.selectedBase().getTag()) )
        {
            //не выбран пользовательский раздел
             res =  showTextInputDialog(this.res.getString("app.title47"), "", this.res.getString("app.title48"),"", getApp().getMainWindow(), Modality.WINDOW_MODAL);

            if(res==null ? false: !res.isEmpty()) start=userSection;
            else return;


        }else
        {
            if(leftAPI.selectedRootSection().getId()==0)
            {
                //не выбран пользовательский раздел
                 res =  showTextInputDialog(this.res.getString("app.title49"), "", this.res.getString("app.title50"),"", getApp().getMainWindow(), Modality.WINDOW_MODAL);

                if(res==null ? false: !res.isEmpty()) start=userSection;
                else return;

            }else if(leftAPI.selectedSectionTree()==null)
            {
                //выбран раздел в комбобоксе но не выбран в дереве


                res =  showTextInputDialog(this.res.getString("app.title51"), "", this.res.getString("app.title52"), "", getApp().getMainWindow(), Modality.WINDOW_MODAL);

                if(res==null ? false: !res.isEmpty()) start=leftAPI.selectedRootSection();
                else return;

            }else
            {
                //выбран элемент дерева и выбран раздел

                //если выбран не раздел
                if(!(leftAPI.selectedSectionTreeItem() instanceof Section))
                {

                    showWarningDialog(this.res.getString("app.title49"),"",this.res.getString("app.title53"),getApp().getMainWindow(),Modality.WINDOW_MODAL );
                    return;


                }

                res =  showTextInputDialog(this.res.getString("app.title49"), "", this.res.getString("app.title54"), "", getApp().getMainWindow(), Modality.WINDOW_MODAL);

                if(res==null ? false: !res.isEmpty())  start=(Section)leftAPI.selectedRootSection();//выберем стартовым раздел
                else return;

            }



        }

if(res==null ? true: res.isEmpty())
{

    showWarningDialog( this.res.getString("app.title55"),"", this.res.getString("app.title56"),getApp().getMainWindow(),Modality.WINDOW_MODAL);
return;
}

        //теперь можновыбрать файл

        //получим путь к файлу.
        File file=null;
        getModel().initStringsSection(start);
        FileChooser fileChooser =new FileChooser();
        if("USER".equals(start.getTag()))  fileChooser.setTitle(this.res.getString("app.title57"));
        else fileChooser.setTitle(this.res.getString("app.title58")+" - " + start.getNameString()+".  "+this.res.getString("app.title59"));
        fileChooser.setInitialDirectory(new File(getModel().getLastExportPath(System.getProperty("user.home"))));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("xmlb", "*.xmlb"));
        file= fileChooser.showOpenDialog(getApp().getMainWindow());

        if(file==null)return;



        ImportUserBase imp=new ImportUserBase();



        Section container=null;
        final Section startFinal=start;
        try {
            container = getModel().createSection(start, res, "", false, getModel().getUserLanguage());
            container.setDescriptionString("");
            container.setNameString(res);
        } catch (Exception e) {
            logger.error("",e);

            showExceptionDialog("Ошибка создания раздела контейнера для импорта базы","","",e,getApp().getMainWindow(),Modality.WINDOW_MODAL);
        return;

        }

        final Section sec=container;
        final File fileToSave=file;
        final String resName=res;

final ResourceBundle rest=this.res;
        Task<Boolean> task =new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception
            {

                imp.setListener(new ImportUserBase.Listener() {
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


                boolean res= imp.parse( fileToSave, getModel(),sec);
                if(res==false)
                {
                    imp.setListener(null);
                    this.failed();
                    return false;}
                else {
                    imp.setListener(null);
                    return true;
                }



            }


        };


     Section sect=container;
        task.progressProperty().addListener((observable, oldValue, newValue) -> getProgressAPI().setProgressIndicator(newValue.doubleValue()));
        task.setOnRunning(event1 -> getProgressAPI().setProgressIndicator(0.0, rest.getString("app.title43")));

        task.setOnSucceeded(event ->
        {

            if (task.getValue())
            {
                getProgressAPI().setProgressIndicator(1.0, rest.getString("app.title44"));

                //хаполнить структуру дерева и комбо.

                ///вопрос - если выбрана база не пользовательскаяч, нужно во всех случаях проверить что выбрано у нас.!!!!!!!!

                    if(startFinal.getParent()==null && "USER".equals(startFinal.getTag()))
                    {

                        if("USER".equals(leftAPI.selectedBase().getTag()))
                        {
                            //в момент выборы была открыта пользовательская база
                            //если у нас контейнер создан в корне пользовательской базы.
                            leftAPI.getRootSectionAllItems().add(sect);
                            leftAPI.selectRootSection(leftAPI.getRootSectionAllItems().indexOf(sect));
                        }
                        //если не в пользовательской базе то ничего не делаем

                    }
                    else {

                        //если внутри пользовательской базы то меняем, иначе ничего не делаем
                        if ("USER".equals(leftAPI.selectedBase().getTag()))
                        {
                            //иначе контейнер создан в дереве
                            if (leftAPI.selectedSectionTree() == null && leftAPI.selectedRootSection().getId() != 0) {
                                //выбран раздел в комбо но не в дереве
                                leftAPI.addTreeItemToTreeRoot(new NamedTreeItem(sect));

                            } else if (leftAPI.selectedSectionTree()!= null) {
                                //выбран раздел в дереве
                                leftAPI.addTreeItemToSelected(new NamedTreeItem(sect));

                            } else
                                showErrorDialog(rest.getString("app.title60"), rest.getString("app.title61"), "", getApp().getMainWindow(), Modality.WINDOW_MODAL);
                         }
                    }

            }
            else getProgressAPI().setProgressIndicator(rest.getString("app.title45"));
            getProgressAPI().hideProgressIndicator(true);
        });

        task.setOnFailed(event -> {
            getProgressAPI().setProgressIndicator(rest.getString("app.title45"));
            getProgressAPI().hideProgressIndicator(true);

            try {
                getApp().getModel().removeSection(sect);
            } catch (Exception e) {
                logger.error("",e);
            }

        });



        Thread threadTask=new Thread(task);
        threadTask.setDaemon(true);
        getProgressAPI().setProgressIndicator(0.01, rest.getString("app.title46"));
        threadTask.start();










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


    public void onImportProfileFromFolder()
    {

    }
    /************/









public void onAbout()
{
    try {

        openDialog(getApp().getMainWindow(), "/fxml/about.fxml", res.getString("app.title71"), false, StageStyle.UTILITY, 0, 0, 0, 0);
    } catch (IOException e) {
        logger.error("",e);
    }
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


    @FXML private MenuItem readFromTrinityMenu;
    public void onReadProfileFromTrinity(){

        try {
            M2BinaryFile m2BinaryFile = M2.readFromDevice(true);
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
                showExceptionDialog(res.getString("app.ui.reading_device"),res.getString("app.error"),"",e,getApp().getMainWindow(),Modality.WINDOW_MODAL);
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
     * Тестирует директорию на тип профиля. Проводится расширенная проверка
     * Не решает проблему, если у нас смешанные комплексы, старый и новый вариант
     * @param profileDir директория профиля
     * @return true если новый, false если старый
     */
    private boolean testProfileDir(File profileDir) throws Exception {
        //проверим наличие файла profile
        File f=new   File( profileDir,"profile.txt");
        if(f.exists()) return true;

        boolean res=true;
        // до конца рекурсивного цикла
        if (!profileDir.exists())  throw new Exception();
        File textFile=null;

        for (File file : profileDir.listFiles( dir -> dir.isDirectory()))
        {
            //найдем первый попавшийся текстовый файл
            textFile=null;

            for (File file1 : file.listFiles((dir, name) -> name.contains(".txt")))
            {
                textFile=file1;
                break;
            }

            //пустая папка
            if(textFile==null) continue;
            //прочитаем файл

            List<String> progrParam = new ArrayList<String>();
            try( Scanner in = new Scanner(textFile,"UTF-8"))
            {
                while (in.hasNextLine()) progrParam.add(in.nextLine().replace("@",""));
            }catch (Exception e) {res=false;break;}

            if(progrParam.size()<8) {res=false;break;}
            else {res=true;break;}
        }

        return res;
    }

    private boolean loadNewProfile(File dir)
    {
        File profileFile=new File(dir,"profile.txt");
        //стоит учесть факт того, что у нас может быть новый профиль но без файла profile!
        List<String> profileParam = new ArrayList<String>();
        if(profileFile.exists()) {
            try (Scanner in = new Scanner(profileFile, "UTF-8")) {
                while (in.hasNextLine()) profileParam.add(in.nextLine());

            } catch (IOException e) {
                logger.error("", e);
                showExceptionDialog(res.getString("app.title116"), res.getString("app.title106"), res.getString("app.title117"), e, getApp().getMainWindow(), Modality.WINDOW_MODAL);
                return false;
            } catch (Exception e) {
                logger.error("", e);
                showExceptionDialog(res.getString("app.title116"), res.getString("app.title106"), res.getString("app.title117"), e, getApp().getMainWindow(), Modality.WINDOW_MODAL);
                return false;
            }

            if (profileParam.size() != 3) {
                showErrorDialog(res.getString("app.title116"), res.getString("app.title106"), res.getString("app.title117"), getApp().getMainWindow(), Modality.WINDOW_MODAL);
                return false;
            }

        }
        long idProf;
        String uuidP;
        String nameP;
        Profile profile=null;


        if(!profileParam.isEmpty())
        {
            idProf=Long.parseLong(profileParam.get(0));
            uuidP=profileParam.get(1);
            nameP=profileParam.get(2);
        }else {
             idProf=0;
             uuidP=UUID.randomUUID().toString();
             nameP="New profile";

        }

        final String up=uuidP;

       long cnt= ProfileTable.getInstance().getAllItems().stream().filter(itm->itm.getUuid().equals(up)).count();
        if(cnt!=0)
        {
            //имеется уже такой профиль с такой сигнатурой!!
        /*
            Optional<ButtonType> buttonType = showConfirmationDialog(res.getString("app.title119"), res.getString("app.title120"), res.getString("app.title121"), getApp().getMainWindow(), Modality.WINDOW_MODAL);
        if(buttonType.isPresent())
        {
                if(buttonType.get()!=okButtonType) return false;

        }else return false;
    */
            idProf=0;
            uuidP=UUID.randomUUID().toString();
            nameP="New profile";

        }

        //получим список комплексов

        try {
            List<ComplexFileData> complexes = FilesProfileHelper.getComplexes(dir);
            Map<ComplexFileData,Map<Long, ProgramFileData>> data=new LinkedHashMap<>();
            for (ComplexFileData complex : complexes)data.put(complex,FilesProfileHelper.getProgramms(complex.getFile()));


///создадим теперь все если все нормально
            try {
                profile = getModel().createProfile(nameP);
                profile.setUuid(uuidP);//сделаем uuid такой же как и в папке, если это прибор то мы сможем манипулировать профилем после этого!!!
                getModel().updateProfile(profile);
                ProfileTable.getInstance().getAllItems().add(profile);

                TherapyComplex th=null;

                for (Map.Entry<ComplexFileData, Map<Long, ProgramFileData>> complexFileDataMapEntry : data.entrySet())
                {
                    int ind= complexFileDataMapEntry.getKey().getName().indexOf("(");
                    int ind2= complexFileDataMapEntry.getKey().getName().indexOf("-");
                    if(ind==-1) ind= complexFileDataMapEntry.getKey().getName().length()-1;
                    if(ind2==-1) ind2= 0;

                    th=  getModel().createTherapyComplex(complexFileDataMapEntry.getKey().getSrcUUID(),profile, complexFileDataMapEntry.getKey().getName().substring(ind2+1,ind), "", (int) complexFileDataMapEntry.getKey().getTimeForFreq(),complexFileDataMapEntry.getKey().getBundlesLength());



                    for (Map.Entry<Long, ProgramFileData> entry : complexFileDataMapEntry.getValue().entrySet())
                    {
                        if(entry.getValue().isMp3())   getModel().createTherapyProgramMp3(th,entry.getValue().getName(),"",entry.getValue().getFreqs());
                        else getModel().createTherapyProgram(entry.getValue().getSrcUUID(), th,entry.getValue().getName(),"",entry.getValue().getFreqs(),entry.getValue().isMulty());

                    }

                }

               profileAPI.enableGenerateBtn();
            } catch (Exception e) {
                logger.error("",e);
                showExceptionDialog("Ошибка сохраниения данных в базе","","",e,getApp().getMainWindow(),Modality.WINDOW_MODAL);
                return false;
            }




        }

        catch (OldComplexTypeException e) {
            logger.error("",e);
            showErrorDialog(res.getString("app.title116"), res.getString("app.title118"), res.getString("app.title117"), getApp().getMainWindow(), Modality.WINDOW_MODAL);
            return false;
        }
        catch (Exception e)
        {
            logger.error("",e);
            showExceptionDialog("Ошибка создания или обновления комплекса", "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);
            return false;
        }
        Profile pp=profile;
      if(profile!=null)  Platform.runLater(() ->  ProfileTable.getInstance().select(pp));
        return true;
    }


    private boolean loadOldProfile(File dir)
    {
        Profile   profile=null;
        try {
            List<ComplexFileData> complexes = FilesOldProfileHelper.getComplexes(dir);
            Map<ComplexFileData,Map<Long, ProgramFileData>> data=new LinkedHashMap<>();
            for (ComplexFileData complex : complexes)data.put(complex,FilesOldProfileHelper.getProgramms(complex.getFile(),(int)complex.getTimeForFreq()));

            if(complexes.isEmpty()) return false;
///создадим теперь все если все нормально
            try {
                   profile = getModel().createProfile("New profile");
                profile.setUuid("");
                getModel().updateProfile(profile);



                TherapyComplex th=null;

                for (Map.Entry<ComplexFileData, Map<Long, ProgramFileData>> complexFileDataMapEntry : data.entrySet())
                {

                    th=  getModel().createTherapyComplex("",profile, complexFileDataMapEntry.getKey().getName(), "", (int) complexFileDataMapEntry.getKey().getTimeForFreq(),3);



                    for (Map.Entry<Long, ProgramFileData> entry : complexFileDataMapEntry.getValue().entrySet())
                    {
                        getModel().createTherapyProgram("",th,entry.getValue().getName(),"",entry.getValue().getFreqs(),true);

                    }

                }
              profileAPI.enableGenerateBtn();

            } catch (Exception e) {
                logger.error("",e);
                showExceptionDialog("Ошибка сохраниения данных в базе","","",e,getApp().getMainWindow(),Modality.WINDOW_MODAL);
                return  false;
            }




        }
        catch (Exception e)
        {
            logger.error("",e);
            showExceptionDialog(res.getString("app.error"), "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);
            return  false;
        }

        Profile prf=profile;
        if(profile!=null) Platform.runLater(() ->
        {
            ProfileTable.getInstance().getAllItems().add(prf);
            ProfileTable.getInstance().select(prf);

        });
return  true;
    }


    /**
     * Загрузжает профиль из папки
     */
    public void onLoadProfileDir()
    {

        DirectoryChooser dirChooser =new DirectoryChooser();
        dirChooser.setTitle(res.getString("app.menu.read_profile_from_dir"));

        // fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        File dir= dirChooser.showDialog(getApp().getMainWindow());

        if(dir==null)return;


        Task<Boolean> task=null;
        boolean profileType=false;
        try {
            profileType= testProfileDir(dir);
        } catch (Exception e) {
            e.printStackTrace();
            showExceptionDialog("Ошибка чтения выбранной директории","","",e,getApp().getMainWindow(),Modality.WINDOW_MODAL);
            return;
        }
        if(profileType)
        {
            //новый профиль
            task =new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {

                    boolean r= loadNewProfile(dir);
                    if(r==false)failed();
                        return r;
                }
            };

        }else
        {
          //старый профиль или ничего вообще

            task =new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {

                    boolean r=  loadOldProfile(dir);
                    if(r==false)failed();
                    return r;
                }
            };
        }

        Task<Boolean> task2=task;





        task2.setOnRunning(event1 -> getProgressAPI().setProgressBar(0.0, res.getString("app.title102"), ""));

        task2.setOnSucceeded(event ->
        {
            Waiter.closeLayer();
            if (task2.getValue()) {
                getProgressAPI().hideProgressBar(false);
                getProgressAPI().setProgressIndicator(1.0, res.getString("app.title103"));

            } else {
                getProgressAPI().hideProgressBar(false);
                getProgressAPI().setProgressIndicator(res.getString("app.title93"));
            }
            getProgressAPI().hideProgressIndicator(true);
        });

        task2.setOnFailed(event -> {
            Waiter.closeLayer();
            getProgressAPI().hideProgressBar(false);
            getProgressAPI().setProgressIndicator(res.getString("app.title93"));
            getProgressAPI().hideProgressIndicator(true);


        });


        Thread threadTask=new Thread(task2);
        threadTask.setDaemon(true);
        getProgressAPI().setProgressBar(0.01, res.getString("app.title102"), "");


        Waiter.openLayer(getApp().getMainWindow(),false);

        threadTask.start();
        Waiter.show();







    }



    /**
     * Загрузка комплекса их выбранной папки, автоматически определяет старый или новый
     * Применяется в импорте комплекса из папки
     * @param dir
     *
     * @return
     */
    private boolean loadCompex(File dir)
    {

        Map<Long, ProgramFileData> programms= FilesProfileHelper.getProgrammsFromComplexDir(dir);

        try {
            if ( programms!=null) {
                Profile profile =  ProfileTable.getInstance().getSelectedItem();
                if (profile != null) {

                    String name = dir.getName();
                    int ind = name.indexOf('-');
                    if (ind != -1) name = name.substring(ind + 1);

                    ind = name.indexOf('(');
                    if (ind != -1) name = name.substring(0,ind);
                    name= name.trim();

                    if(programms.isEmpty())
                    {



                        getModel().createTherapyComplex("",profile, name, "", 300,3);


                    }else
                    {
                        Long next = programms.keySet().iterator().next();

                        TherapyComplex th = getModel().createTherapyComplex(programms.get(next).getSrcUUIDComplex(),profile, name, "", (int) programms.get(next).getTimeForFreq(),(int) programms.get(next).getBundlesLenght());


                        for (Map.Entry<Long, ProgramFileData> entry : programms.entrySet())
                        {
                            if(entry.getValue().isMp3()) getModel().createTherapyProgramMp3(th,entry.getValue().getName(),"",entry.getValue().getFreqs());
                            else getModel().createTherapyProgram(entry.getValue().getSrcUUID(),th,entry.getValue().getName(),"",entry.getValue().getFreqs(),entry.getValue().isMulty());

                        }
                       profileAPI.enableGenerateBtn();
                    }

                }


            }

        }catch (Exception e){ logger.error("",e);return false;}

        if(programms==null) return false;
        else return true;

    }






    /**
     * Импорт терап.комплексов из папки
     */
    public void onImportComplex()
    {
        DirectoryChooser dirChooser =new DirectoryChooser();
        dirChooser.setTitle(res.getString("app.menu.read_complex_from_dir"));

        // fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        File dir= dirChooser.showDialog(getApp().getMainWindow());

        if(dir==null)return;


        Task<Boolean> task=null;

            //новый профиль
            task =new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {

                    boolean r= loadCompex(dir);
                    if(r==false)failed();
                    return r;
                }
            };



        Task<Boolean> task2=task;





        task2.setOnRunning(event1 -> getProgressAPI().setProgressBar(0.0, res.getString("app.title102"), ""));

        task2.setOnSucceeded(event ->
        {
            Waiter.closeLayer();
            if (task2.getValue()) {
                getProgressAPI().hideProgressBar(false);
                getProgressAPI().setProgressIndicator(1.0, res.getString("app.title103"));

                Profile selectedItem =  ProfileTable.getInstance().getSelectedItem();
                if(selectedItem!=null)   Platform.runLater(() -> {
                    ProfileTable.getInstance().clearSelection();
                    ProfileTable.getInstance().select(selectedItem);
                });


            } else {
                getProgressAPI().hideProgressBar(false);
                getProgressAPI().setProgressIndicator(res.getString("app.title93"));
            }
            getProgressAPI().hideProgressIndicator(true);

        });

        task2.setOnFailed(event -> {
            Waiter.closeLayer();
            getProgressAPI().hideProgressBar(false);
            getProgressAPI().setProgressIndicator(res.getString("app.title93"));
            getProgressAPI().hideProgressIndicator(true);


        });


        Thread threadTask=new Thread(task2);
        threadTask.setDaemon(true);
        getProgressAPI().setProgressBar(0.01, res.getString("app.title102"), "");

        Waiter.openLayer(getApp().getMainWindow(),false);
        threadTask.start();
        Waiter.show();



    }

    /**
     * Импорт терапевтического комплекса в базу частот
     */
    public void   onImportComplexToBase()
    {

        DirectoryChooser dirChooser =new DirectoryChooser();
        dirChooser.setTitle(res.getString("app.menu.read_complex_from_dir"));

        dirChooser.setInitialDirectory(new File(getModel().getLastExportPath(System.getProperty("user.home"))));

        File dir= dirChooser.showDialog(getApp().getMainWindow());

        if(dir==null)return;

    boolean createComplex=true;

        Optional<ButtonType> buttonType = showConfirmationDialog(res.getString("app.ui.import_compl_to_base"), "", res.getString("app.ui.import_compl_to_base_q"), getApp().getMainWindow(), Modality.WINDOW_MODAL);
        if(buttonType.isPresent())
        {
            if(buttonType.get()==okButtonType) createComplex=true;
            else  if(buttonType.get()==noButtonType) createComplex=false;
            else return;
        }else return;



        NamedTreeItem treeSelected = leftAPI.selectedSectionTree();
        if(treeSelected==null) return;
       if(!(treeSelected.getValue() instanceof Section)) return;



        Task<Boolean> task=null;
        boolean createComplex2=createComplex;

        //новый профиль
        task =new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {

                boolean r= leftAPI.loadComplexToBase(dir, treeSelected,createComplex2);
                if(r==false)failed();
                return r;
            }
        };



        Task<Boolean> task2=task;





        task2.setOnRunning(event1 -> getProgressAPI().setProgressBar(0.0, res.getString("app.title102"), ""));

        task2.setOnSucceeded(event ->
        {
            Waiter.closeLayer();
            if (task2.getValue()) {
                getProgressAPI().hideProgressBar(false);
                getProgressAPI().setProgressIndicator(1.0, res.getString("app.title103"));

                Profile selectedItem =  ProfileTable.getInstance().getSelectedItem();
                if(selectedItem!=null)   Platform.runLater(() -> {
                    ProfileTable.getInstance().clearSelection();
                ProfileTable.getInstance().select(selectedItem);
                });


            } else {
                getProgressAPI().hideProgressBar(false);
                getProgressAPI().setProgressIndicator(res.getString("app.title93"));
            }
            getProgressAPI().hideProgressIndicator(true);

        });

        task2.setOnFailed(event -> {
            Waiter.closeLayer();
            getProgressAPI().hideProgressBar(false);
            getProgressAPI().setProgressIndicator(res.getString("app.title93"));
            getProgressAPI().hideProgressIndicator(true);


        });


        Thread threadTask=new Thread(task2);
        threadTask.setDaemon(true);
        getProgressAPI().setProgressBar(0.01, res.getString("app.title102"), "");




        Waiter.openLayer(getApp().getMainWindow(),false);
        threadTask.start();
        Waiter.show();

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



    private void restartProgram() {
/*
 Runtime.getRuntime().addShutdownHook(new Thread() {
    public void run() {
    ((Window) view).setVisible(false);
    Runtime.halt(0);
    }
    });
 */
        //if(AutoUpdater.isIDEStarted()) return;

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


    @Override
    protected void onCompletedInitialise() {
        if(getModel().isAutoUpdateEnable()){
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
    }


    public void onReference(){
        try {
            openDialogNotModal(getApp().getMainWindow(),"/fxml/ReferenceBook.fxml",res.getString("app.menu.reference"),true,StageStyle.DECORATED,600,900,0,0,"/styles/Styles.css");
        } catch (IOException e) {
            logger.error("",e);
        }
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



    /***************************************************/


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







public void onPrintProfile(){
        profileAPI.printProfile();
}


    /**
     * Informs the ListView that one of its items has been modified.
     * listData.get(i).append("!");
     triggerUpdate(listView, listData.get(i), i);
     * @param listView The ListView to trigger.
     * @param newValue The new value of the list item that changed.
     * @param i The index of the list item that changed.
     */
    public static <T> void triggerUpdate(ListView<T> listView, T newValue, int i) {
        EventType<? extends ListView.EditEvent<T>> type = ListView.editCommitEvent();
        Event event = new ListView.EditEvent<>(listView, type, newValue, i);
        listView.fireEvent(event);
    }




    public static ButtonType forceUpdateBtnType =null;
    public static  ButtonType updateBtnType = null;
    /**
     *
     * @param title
     * @param header
     * @param content
     * @return  okButtonType или noButtonType
     */
    public static Optional<ButtonType> showConfirmationFileUpdateDialog(String title,String header,String content,Window owner,Modality modal)
    {
        if(forceUpdateBtnType==null) {
            //инициализация при первом использовании
            forceUpdateBtnType = new ButtonType(getApp().getResources().getString("app.btn.force_update"));
            updateBtnType = new ButtonType(getApp().getResources().getString("app.btn.update"));
        }
        ButtonType cancellButtonType =   new ButtonType(getApp().getResources().getString("app.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        //alert.getDialogPane().getButtonTypes().clear();
        alert.getDialogPane().getButtonTypes().setAll(cancellButtonType, forceUpdateBtnType, updateBtnType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.initOwner(owner);
        alert.initModality(modal);


        Optional<ButtonType> result = alert.showAndWait();
        return result;
    }
}
