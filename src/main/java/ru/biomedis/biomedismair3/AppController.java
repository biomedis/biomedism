package ru.biomedis.biomedismair3;

import com.mpatric.mp3agic.Mp3File;
import com.sun.javafx.scene.control.skin.TableViewSkin;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.*;
import javafx.util.Duration;
import org.anantacreative.updater.Update.UpdateException;
import org.anantacreative.updater.Update.UpdateTask;
import ru.biomedis.biomedismair3.Converters.SectionConverter;
import ru.biomedis.biomedismair3.DBImport.NewDBImport;
import ru.biomedis.biomedismair3.Dialogs.NameDescroptionDialogController;
import ru.biomedis.biomedismair3.Dialogs.ProgramDialogController;
import ru.biomedis.biomedismair3.Dialogs.SearchProfile;
import ru.biomedis.biomedismair3.Dialogs.TextInputValidationController;
import ru.biomedis.biomedismair3.TherapyTabs.Complex.ComplexTable;
import ru.biomedis.biomedismair3.TherapyTabs.Profile.ProfileTable;
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
import ru.biomedis.biomedismair3.m2.*;
import ru.biomedis.biomedismair3.utils.Audio.MP3Encoder;
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
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static ru.biomedis.biomedismair3.Log.logger;

public class AppController  extends BaseController {
    
    private static int MAX_BUNDLES=7;
    @FXML private ImageView deviceIcon;//иконка устройства
    @FXML private ComboBox<Section> baseCombo;//первый уровень разделов( типа выбор базы)
    @FXML private ComboBox<Section> sectionCombo;//второй уровень разделов
    @FXML private Button btnUploadm;//закачать на прибор
    @FXML private TreeView<INamed> sectionTree;//дерево разделов
    @FXML private HBox userActionPane;//панель пользовательских действий
    @FXML private ProgressBar diskSpaceBar;//прогресс бар занятого места на диске прибора
    @FXML private Button createUserBtn;//создание в пользовательской базе
    @FXML private Button editUserBtn;//редакт. в пользовательской базе
    @FXML private Button delUserBtn;//удал. в пользовательской базе
    @FXML private TextArea programInfo;
    @FXML private TextArea programDescription;

    @FXML private VBox progress1Pane;
    @FXML private VBox progress2Pane;
    @FXML private VBox  progress3Pane;
    @FXML private Label  messageText;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Label progressIndicatorLabel;
    @FXML private ProgressBar progressAction;
    @FXML private Label textInfo;
    @FXML private Label textActionInfo;

    @FXML private TableView<Profile> tableProfile;
    @FXML private TableView<TherapyComplex> tableComplex;
    @FXML private TableView<TherapyProgram> tableProgram;
    @FXML private Button btnDeleteProfile;
    @FXML private Button  generationComplexesBtn;
    @FXML private Button  btnCreateTherapy;
    @FXML private Button btnDeleteTherapy;

    @FXML private Button  btnDeleteProgram;
    @FXML private Button  btnUpProgram;
    @FXML private Button  btnDownProgram;
    @FXML private Spinner<Double> timeToFreqSpinner;

    @FXML private HBox spinnerPan;
    @FXML private VBox spinnerBtnPan;
    @FXML private Button  btnOkSpinner;
    @FXML private Button  btnCancelSpinner;

    @FXML private MenuItem menuExportProfile;
    @FXML private    MenuItem    menuExportTherapyComplex;
    @FXML private    MenuItem  menuImportTherapyComplex;

    @FXML private Button btnGenerate;
    @FXML private MenuItem menuDelGeneratedFiles;


    @FXML private TextField searchPatternField;
    @FXML private Button searchBtn;

   @FXML private SplitPane splitOuter;

    @FXML private MenuItem printProfileMenu;
    @FXML private MenuItem printComplexMenu;
    @FXML private MenuItem menuImportComplex;
    @FXML private MenuItem   menuImportComplexToBase;
    @FXML private MenuItem   dataPathMenuItem;
    @FXML TabPane therapyTabPane;

    @FXML Button   uploadComplexesBtn;
    private ContextMenu uploadComplexesMenu=new ContextMenu();

    @FXML
    private Tab tab1;
    @FXML
    private Tab tab2;
    @FXML
    private Tab tab3;

    @FXML
    private Tab tab4;

    @FXML
    private Tab tab5;


    @FXML private HBox bundlesPan;
    @FXML private Spinner<String> bundlesSpinner;

    //@FXML  private ObservableList<String>  bundlesSpinnerData;
    @FXML  private VBox bundlesBtnPan;
    @FXML private Button btnOkBundles;
    @FXML private Button btnCancelBundles;






    @FXML private Menu updateBaseMenu;
    @FXML private Button searchReturn;
    @FXML private MenuItem installUpdatesMItm;
    @FXML private MenuItem  checkForUpdatesMItm;
    private @FXML MenuItem clearTrinityItem;
    private TableViewSkin<?> tableSkin;
    private VirtualFlow<?> virtualFlow;

    private ContextMenu deleteMenu=new ContextMenu();
    private Path devicePath=null;//путь ку устройству или NULL если что-то не так
    private String fsDeviceName="";

    private ContextMenu searchMenu=new ContextMenu();
    private SearchState searchState=new SearchState();

    private ContextMenu uploadMenu=new ContextMenu();


    private SimpleBooleanProperty connectedDevice =new SimpleBooleanProperty(false);//подключено ли устройство


    private Image imageDone;
    private Image imageCancel;

    private Image imageSeq;
    private Image imageParallel;

    private Image imageDeviceOff;
    private Image imageDeviceOn;




    //экстрактор для событий обновления комбобокса
    //private Callback<Section ,Observable [] > extractor = param -> new Observable[]{param.nameStringProperty(),param.desriptionStringProperty()};
    private List<Section> sectionsBase=new ArrayList<>();//основные разделы

    private  ResourceBundle res;
    private Tooltip diskSpaceTooltip=new Tooltip();
    private Tooltip searchTooltip=new Tooltip();


    private NamedTreeItem rootItem=new NamedTreeItem();//корень дерева разделов(всегда есть, мы в нем мменяем только дочерние элементы)
    private boolean stopGCthread=false;

    private final DataFormat PROGRAM_DRAG_ITEM=new DataFormat("biomedis/programitem");


    private M2UI m2ui;

    private List<TherapyComplex> therapyComplexesClipboard=new ArrayList<>();
    private boolean therapyProgramsCopied=false;

    public boolean getConnectedDevice() {
        return connectedDevice.get();
    }

    public SimpleBooleanProperty connectedDeviceProperty() {
        return connectedDevice;
    }

    public void setConnectedDevice(boolean connectedDevice) {
        this.connectedDevice.set(connectedDevice);
    }

    //сборщик мусора. Собирает мусор периодически, тк мы много объектов создаем при построении дерева
    //нужно его отключать вкогда плодим файлы!!!!!
    private Thread gcThreadRunner;

    synchronized   public boolean isStopGCthread() {
        return stopGCthread;
    }

    synchronized  public void setStopGCthread() {
        this.stopGCthread = true;
    }





    private SimpleBooleanProperty checkUppload=new SimpleBooleanProperty(false);
    /**
     * Изменяет состояние кнопки загрузки в прибор. Стоит проверить при изменении состояния устройства и изменеии состояния кнопки загрузки
     */
        private void checkUpploadBtn()
        {
                //свойство заставит сработать проверку доступности кнопки btnUpload.disableProperty().bind(new BooleanBinding()
            checkUppload.set(!checkUppload.get());

        }


    public void onSearchReturn(){
        clearSearch(true,true);

    }

    /**
     *  возврат к состоянию дерева как до поиска, возврат состояния других элементов
     * @param restoreState восстановить старое дерево до поиска?
     */
    private void clearSearch(boolean restoreState,boolean resetSearchField)
    {
        if(searchState.isSearch())
        {
           //произведем подчистку, иначе
            if(restoreState)
            {
                //восстановление дерева как до поиска

                fillTree(sectionCombo.getValue());


            }
        }
        if(resetSearchField)searchPatternField.setText("");
        //вернем в открытое состояние если у нас выбрана родительская база
        String tag = baseCombo.getSelectionModel().getSelectedItem().getTag();
        if (tag != null ? tag.equals("USER") : false) userActionPane.setDisable(false);
        else userActionPane.setDisable(true);

        searchState.clear();
    }

    /**
     * Балансирует положение разделителей сплитера для удобства
     */
    private void balanceSpitterDividers()
    {

       // double summ=   splitOuter.getItems().stream().mapToDouble(node ->(node instanceof Parent )? ((Parent)node).minWidth(-1):  node.getBoundsInParent().getWidth()).sum();
      /*  double summ =
                ((splitOuter.getItems().get(0) instanceof Parent )? ((Parent)splitOuter.getItems().get(0)).minWidth(-1):  splitOuter.getItems().get(0).getBoundsInParent().getWidth())+ ((splitOuter.getItems().get(1) instanceof Parent )? ((Parent)splitOuter.getItems().get(1)).minWidth(-1):  splitOuter.getItems().get(1).getBoundsInParent().getWidth());

        summ+=15;
        double w=  splitOuter.getWidth();
        SplitPane.Divider divider1 = splitOuter.getDividers().get(0);
       if(divider1.getPosition()<=summ/w) divider1.setPosition(summ / w);
        */
        SplitPane.Divider divider1 = splitOuter.getDividers().get(0);
         divider1.setPosition(0.25);




    }
    private String baseComplexTabName;
    private String baseProgramTabName;
    private String baseProfileTabName;



    @Override
    public void initialize(URL url, ResourceBundle rb) {

        res=rb;
        initNamesTables();
        initSearchUI();

        dataPathMenuItem.setVisible(OSValidator.isWindows());//видимость пункта меню для введения пути к папки данных, только на винде!
        clearTrinityItem.disableProperty().bind(m2Connected.not());
        updateBaseMenu.setVisible(getApp().isUpdateBaseMenuVisible());

        btnGenerate.setDisable(true);


        //настройка подписей в табах
        initTabs();

        /** Контекстное меню загрузки в прибор **/
        initUploadMenuBtn();
        initVerticalDivider();

        /** Конопки меню верхнего ***/
        menuDelGeneratedFiles.disableProperty().bind(tableProfile.getSelectionModel().selectedItemProperty().isNull());

        printProfileMenu.disableProperty().bind(tableProfile.getSelectionModel().selectedItemProperty().isNull());
        printComplexMenu.disableProperty().bind(tableComplex.getSelectionModel().selectedItemProperty().isNull());
        menuImportComplex.disableProperty().bind(tableProfile.getSelectionModel().selectedItemProperty().isNull());
        menuExportProfile.disableProperty().bind(tableProfile.getSelectionModel().selectedItemProperty().isNull());
        menuExportTherapyComplex.disableProperty().bind(tableComplex.getSelectionModel().selectedItemProperty().isNull());
        menuImportTherapyComplex.disableProperty().bind(tableProfile.getSelectionModel().selectedItemProperty().isNull());
        /***************/

        baseInitSpinnerTimeForFreq();
        baseInitBundlesSpinner();


        progress1Pane.setVisible(false);
        progress2Pane.setVisible(false);
        progress3Pane.setVisible(false);


        userActionPane.setDisable(true);
        diskSpaceBar.setVisible(false);
        diskSpaceBar.setTooltip(diskSpaceTooltip);
        hackTooltipStartTiming(diskSpaceTooltip, 250, 15000);


        initDeleteFromUserBaseMenu(rb);

        initDoneCancelImages();
        initSeqParallelImages();

        initGCRunner();
        getApp().addCloseApplistener(()->{
            setStopGCthread();
            DiskDetector.stopDetectingService();
        } );


        initDeviceMDetection(rb);


        programInfo.setEditable(false);
        programInfo.setWrapText(true);
        programDescription.setEditable(false);
        programDescription.setWrapText(true);


        initBaseCombo();
        initSectionCombo();
        //настроим дерево разделов
        initSectionTree();

        initTables();
        initProgramSearch();
        initBiofon();
        initUSBDetectionM2();

        initTrinityReadingMenuItemDisabledPolicy();
        initM2UI();

        initSpinnersPaneVisibilityPolicy();
        /** кнопки  таблиц **/
        initTablesButtonVisibilityPolicy();

        initMenuItemImportComplexToBase();
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

    private void initTablesButtonVisibilityPolicy() {
        btnDeleteProfile.disableProperty().bind(tableProfile.getSelectionModel().selectedItemProperty().isNull());
        btnCreateTherapy.disableProperty().bind(tableProfile.getSelectionModel().selectedItemProperty().isNull());
        btnDeleteTherapy.disableProperty().bind(tableComplex.getSelectionModel().selectedItemProperty().isNull());


        btnDeleteProgram.disableProperty().bind(tableProgram.getSelectionModel().selectedItemProperty().isNull());


        btnUpProgram.disableProperty().bind(new BooleanBinding() {
            {bind(tableProgram.getSelectionModel().selectedItemProperty());}
            @Override
            protected boolean computeValue() {
                if(tableProgram.getSelectionModel().getSelectedIndices().size()==0)return true;
                 if(tableProgram.getSelectionModel().getSelectedIndices().size()>1) return true;
                 if(tableProgram.getSelectionModel().getSelectedIndex()==0) return true;//верхний элемент
                 return false;
            }
        });

        btnDownProgram.disableProperty().bind(new BooleanBinding() {
            {
                //заставит этот биндинг обновляться при изменении свойства selectedIndexProperty
              super.bind(tableProgram.getSelectionModel().selectedItemProperty());

            }
             @Override
             protected boolean computeValue()
             {

                 if(tableProgram.getSelectionModel().getSelectedIndices().size()==0)return true;
                 if(tableProgram.getSelectionModel().getSelectedIndices().size()>1) return true;

                if( tableProgram.getSelectionModel().getSelectedIndex() == tableProgram.getItems().size()-1) return true;
                return false;
             }
         });
    }

    private void initSpinnersPaneVisibilityPolicy() {
        spinnerPan.visibleProperty().bind(tableComplex.getSelectionModel().selectedItemProperty().isNotNull());
        bundlesPan.visibleProperty().bind(tableComplex.getSelectionModel().selectedItemProperty().isNotNull());
    }

    private void initMenuItemImportComplexToBase() {
        menuImportComplexToBase.disableProperty().bind(new BooleanBinding() {

            {
                super.bind(sectionTree.getSelectionModel().selectedIndexProperty());
            }
            @Override
            protected boolean computeValue()
            {


                if (baseCombo.getSelectionModel().getSelectedItem().getTag() != null ? baseCombo.getSelectionModel().getSelectedItem().getTag().equals("USER") : false) {

                    if (sectionTree.getSelectionModel().getSelectedItem() == null) return true;
                    if (sectionTree.getSelectionModel().getSelectedItem().getValue() instanceof Complex) return false;
                    else if (sectionTree.getSelectionModel().getSelectedItem().getValue() instanceof Section)
                        return false;
                    else return true;
                }else  return true;
            }
        });
    }

    private void initSectionTree() {
        sectionTree.setShowRoot(false);
        sectionTree.setRoot(rootItem);
        rootItem.setExpanded(true);

        sectionTree.setCellFactory(param -> new SectionTreeCell());
        sectionTree.setOnMouseClicked(this::sectionTreeClickAction);
    }

    private void sectionTreeClickAction(MouseEvent event) {
        //по одиночному клику

        TreeItem<INamed> selectedItem = sectionTree.getSelectionModel().getSelectedItem();
        if (selectedItem == null) return;

        singleClickOnSectionTreeAction(selectedItem);
        if (event.getClickCount() == 2)doubleClickOnSectionTreeAction(selectedItem);
    }

    private void singleClickOnSectionTreeAction(TreeItem<INamed> selectedItem) {
        if (selectedItem.getValue() instanceof Program)
        {
            singleClickOnSectionTreeProgramItemAction(selectedItem);
        } else  if (selectedItem.getValue() instanceof Section)
        {
            singleClickOnSectionTreeSectionItemAction(selectedItem);
        } else  if (selectedItem.getValue() instanceof Complex)
        {
            singleClickOnSectionTreeComplexItemAction(selectedItem);
        }
    }

    private void doubleClickOnSectionTreeAction(TreeItem<INamed> selectedItem) {
        int tabSelectedIndex = therapyTabPane.getSelectionModel().getSelectedIndex();
        //перенос программы в текущий комплекс  в такблицу справа.
        if (selectedItem.getValue() instanceof Program)
        {
            doubleClickOnSectionTreeProgramItemAction(selectedItem, tabSelectedIndex);
        } else if (selectedItem.getValue() instanceof Complex) {
            doubleClickOnSectionTreeComplexItemAction(selectedItem, tabSelectedIndex);
        }
    }

    private void doubleClickOnSectionTreeComplexItemAction(TreeItem<INamed> selectedItem, int tabSelectedIndex) {
        //если выбран биофон вкладка
        if(tabSelectedIndex==3){
            //добавляется комплекс в биофон
            Complex c = (Complex) selectedItem.getValue();
            try {
                TherapyComplex th = getModel().createTherapyComplex(getApp().getBiofonProfile(), c, c.getTimeForFreq()==0?180:c.getTimeForFreq(),3,getInsertComplexLang());

                addComplexToBiofonTab(th);

            } catch (Exception e) {
                logger.error("",e);
                showExceptionDialog("Ошибка создания терапевтического комплекса ", "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);

            }


        }else
        if (tableProfile.getSelectionModel().getSelectedItem() != null)//добавление комплекса в профиль
        {

            Complex c = (Complex) selectedItem.getValue();

            try {
                TherapyComplex th = getModel().createTherapyComplex(tableProfile.getSelectionModel().getSelectedItem(), c, c.getTimeForFreq()==0?180:c.getTimeForFreq(),3,getInsertComplexLang());

                //therapyComplexItems.clear();
                //therapyComplexItems содержит отслеживаемый список, элементы которого добавляются в таблицу. Его не нужно очищать

                tableComplex.getItems().add(th);
                tableComplex.getSelectionModel().clearSelection();
                therapyTabPane.getSelectionModel().select(1);//выберем таб с комплексами
                tableComplex.getSelectionModel().select(tableComplex.getItems().size() - 1);
                updateProfileTime(tableProfile.getSelectionModel().getSelectedItem());

                    //если есть программы  к перенесенном комплексе то можно разрешить генерацию
                if(getModel().countTherapyPrograms(th)>0) btnGenerate.setDisable(false);
                else btnGenerate.setDisable(true);
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
        String il=getInsertComplexLang();
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
            TherapyComplex selectedTCBiofon = biofonCompexesList.getSelectionModel().getSelectedItem();
            if(selectedTCBiofon==null) return;
            try {
                TherapyProgram therapyProgram = getModel().createTherapyProgram(p.getUuid(),selectedTCBiofon, name, descr, p.getFrequencies(),oname);
                addProgramToBiofonTab(selectedTCBiofon,therapyProgram);
            } catch (Exception e) {
                logger.error("",e);
                showExceptionDialog("Ошибка создания терапевтической программы", "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);

            }



        }else
        if (tableComplex.getSelectionModel().getSelectedItem() != null) {
            //если выбран комплекс в таблице комплексов

            try {


                TherapyProgram therapyProgram = getModel().createTherapyProgram(p.getUuid(),tableComplex.getSelectionModel().getSelectedItem(), name, descr, p.getFrequencies(),oname);
                tableProgram.getItems().add(therapyProgram);
                updateComplexTime(tableComplex.getSelectionModel().getSelectedItem(), false);
                therapyTabPane.getSelectionModel().select(2);//выберем таб с программами


                Platform.runLater(() -> {
                    updateComplexTime(tableComplex.getSelectionModel().getSelectedItem(),true);
                    tableProgram.getSelectionModel().clearSelection();
                    tableProgram.requestFocus();
                    tableProgram.getSelectionModel().select(tableProgram.getItems().size() - 1);
                    tableProgram.getFocusModel().focus(tableProgram.getItems().size() - 1);
                    tableProgram.scrollTo(tableProgram.getItems().size() - 1);
                });

                btnGenerate.setDisable(false);
                therapyProgram = null;
            } catch (Exception e) {

                logger.error("",e);
                showExceptionDialog("Ошибка создания терапевтической программы", "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);
            }
            p = null;

        }
    }



    private void singleClickOnSectionTreeComplexItemAction(TreeItem<INamed> selectedItem) {
        String pathtext="";

        if(((Complex) selectedItem.getValue()).getSection()!=null)
        {
            Section tS=  ((Complex) selectedItem.getValue()).getSection();
            getModel().initStringsSection(tS);
            pathtext=tS.getNameString();
            if(tS.getParent()!=null)
            {
                Section tS1= tS.getParent();
                getModel().initStringsSection(tS1);
                pathtext=tS1.getNameString()+" -> "+pathtext;
            }
        }
        if(!pathtext.isEmpty()) pathtext+=" -> "+selectedItem.getValue().getNameString(); else pathtext=selectedItem.getValue().getNameString();
        programDescription.setText(pathtext+"\n"+((IDescriptioned) selectedItem.getValue()).getDescriptionString());
        programInfo.setText("");
        createUserBtn.setDisable(false);
    }

    private void singleClickOnSectionTreeSectionItemAction(TreeItem<INamed> selectedItem) {
        String pathtext="";
        if(((Section) selectedItem.getValue()).getParent()!=null)
        {
            Section tS=  ((Section) selectedItem.getValue()).getParent();
            getModel().initStringsSection(tS);
            pathtext=tS.getNameString();

            if(tS.getParent()!=null)
            {
                Section tS1= tS.getParent();
                getModel().initStringsSection(tS1);
                pathtext=tS1.getNameString()+" -> "+pathtext;
            }
        }
        if(!pathtext.isEmpty()) pathtext+=" -> "+selectedItem.getValue().getNameString(); else pathtext=selectedItem.getValue().getNameString();
        programDescription.setText(pathtext+"\n"+((IDescriptioned) selectedItem.getValue()).getDescriptionString());
        programInfo.setText("");
        createUserBtn.setDisable(false);
    }

    private void singleClickOnSectionTreeProgramItemAction(TreeItem<INamed> selectedItem) {
        String pathtext="";
        if(((Program) selectedItem.getValue()).getComplex()!=null)
        {
            Complex tC=  ((Program) selectedItem.getValue()).getComplex();
            getModel().initStringsComplex(tC);
            pathtext=tC.getNameString();

            if(tC.getSection()!=null)
            {
                Section tS= tC.getSection();
                getModel().initStringsSection(tS);
                pathtext=tS.getNameString()+" -> "+pathtext;
            }
        }else if(((Program) selectedItem.getValue()).getSection()!=null)
        {
            Section tS=  ((Program) selectedItem.getValue()).getSection();
            getModel().initStringsSection(tS);
            pathtext=tS.getNameString();

            if(tS.getParent()!=null)
            {
                Section tS1= tS.getParent();
                getModel().initStringsSection(tS1);
                pathtext=tS1.getNameString()+" -> "+pathtext;
            }
        }

        if(!pathtext.isEmpty()) pathtext+=" -> "+selectedItem.getValue().getNameString(); else pathtext=selectedItem.getValue().getNameString();
        programDescription.setText(pathtext+"\n"+((Program) selectedItem.getValue()).getDescriptionString());
        programInfo.setText(((Program) selectedItem.getValue()).getFrequencies().replace(";", ";  "));
        createUserBtn.setDisable(true);
    }

    private void initBaseCombo() {
        List<Section> allRootSection;// разделы старая и новая база
        allRootSection = getModel().findAllRootSection();// разделы разных баз(старая и новая)
        getModel().initStringsSection(allRootSection);
        baseCombo.setConverter(new SectionConverter(getModel().getProgramLanguage().getAbbr()));
        baseCombo.getItems().addAll(allRootSection);
        baseCombo.setVisibleRowCount(5);

        //  sectionCombo.setPlaceholder(new Label(rb.getString("ui.main.empty_list")));
        //выбор базы
        baseCombo.setOnAction(event ->
        {
            programDescription.setText("");
            programInfo.setText("");
            clearSearch(false,false);//очистка состояния поиска
            sectionTree.setShowRoot(false);
            //переключение панели кнопок действий для пользовательского раздела
            String tag = baseCombo.getSelectionModel().getSelectedItem().getTag();
            if (tag != null ? tag.equals("USER") : false) userActionPane.setDisable(false);
            else userActionPane.setDisable(true);

            fillSectionsSelectedBase();

            //если список подразделов пуст, то попробуем заполнить дерево из корня, те из выбранной базы. Для тринити сейчас так
            if(sectionsBase.size()<=1 && !tag.equals("USER")){
                clearSearch(false,false);//очистка состояния поиска
                Section selectedItem = baseCombo.getSelectionModel().getSelectedItem();
                programDescription.setText("");
                programInfo.setText("");
                fillTree(selectedItem);//очистит и заполнит дерево, родительский раздел передается как параметр
            }


        });

        //откроем первую базу
        baseCombo.getSelectionModel().select(0);
        baseCombo.fireEvent(new ActionEvent());//создадим эвент для baseCombo.setOnAction и заполним комбобок тем самым
    }

    private void initSectionCombo() {
        sectionCombo.setConverter(new SectionConverter(getModel().getProgramLanguage().getAbbr()));//конвертер секции в строку
        sectionCombo.setVisibleRowCount(10);

        //выбор рездела. Заполнение дерева после выбора раздела
        sectionCombo.setOnAction(event ->
        {
            clearSearch(false,false);//очистка состояния поиска
            INamed value = rootItem.getValue();
            Section selectedItem = sectionCombo.getSelectionModel().getSelectedItem();
            programDescription.setText("");
            programInfo.setText("");
            fillTree(sectionCombo.getSelectionModel().getSelectedItem());//очистит и заполнит дерево, родительский раздел передается как параметр
        });

        sectionCombo.getSelectionModel().select(1);
        sectionCombo.getOnAction().handle(new ActionEvent());
    }

    private void fillSectionsSelectedBase() {
        sectionsBase.clear();
        sectionsBase.add(new Section());//пустой элемент вставим для выбора он с ID =0
        sectionsBase.addAll(getModel().findAllSectionByParent(baseCombo.getSelectionModel().getSelectedItem()));
        getModel().initStringsSection(sectionsBase);
        //очистка и заполение комбобокса разделов 2 уровня согласно выбранному 1 разделу
        sectionCombo.getItems().clear();
        sectionCombo.getItems().addAll(sectionsBase);
        rootItem.setValue(null);
        if(baseCombo.getSelectionModel().getSelectedIndex()<=1)sectionCombo.getSelectionModel().select(1);
        else sectionCombo.getSelectionModel().select(0);//автоматически очистит дерево, тк сработает sectionCombo.setOnAction(event....
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

    private void initDeleteFromUserBaseMenu(ResourceBundle rb) {
        MenuItem mi1=new MenuItem(rb.getString("app.delete"));
        MenuItem mi2=new MenuItem(rb.getString("app.clear"));

        mi1.setOnAction(event2 -> onDeleteItm());
        mi2.setOnAction(event2 -> onClearItm());

        deleteMenu.getItems().addAll(mi1, mi2);

        delUserBtn.setOnAction(event3 ->
        {

            if (sectionTree.getSelectionModel().getSelectedItem() == null )
            {
                if(sectionCombo.getSelectionModel().getSelectedItem().getId()==0)return;
                else
                {
                    //это для выбранного меню разделов в комбо для секций
                    mi1.setDisable(false);
                    mi2.setDisable(true);

                    deleteMenu.show(delUserBtn, Side.BOTTOM, 0, 0);
                    return;

                }
            }
            mi1.setDisable(false);
            mi2.setDisable(false);


            if (sectionTree.getSelectionModel().getSelectedItem().getValue() instanceof Section)
            {
                //можно удалять разделы если в них все пустые разделы, но есть програмы и комплексы
                long count = sectionTree.getSelectionModel().getSelectedItem().getChildren().stream().filter(itm -> (itm.getValue() instanceof Section && !itm.getChildren().isEmpty())).count();
                if(count!=0)  mi1.setDisable(true);

               // if (!sectionTree.getSelectionModel().getSelectedItem().getChildren().isEmpty())
            } else if (sectionTree.getSelectionModel().getSelectedItem().getValue() instanceof Program)
                mi2.setDisable(true);


            deleteMenu.show(delUserBtn, Side.BOTTOM, 0, 0);
        });
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

    private void initSearchUI() {
        searchReturn.setDisable(true);
        searchReturn.disableProperty().bind(searchState.searchedProperty().not());
        initSearchContextMenu();
    }

    private void initSearchContextMenu() {
        MenuItem smi1=new MenuItem(res.getString("app.text.search_in_dep"));
        MenuItem smi2=new MenuItem(res.getString("app.text.search_in_cbase"));
        MenuItem smi3=new MenuItem(res.getString("app.text.search_in_allbase"));
        SeparatorMenuItem spmi=new SeparatorMenuItem();
        MenuItem smi4=new MenuItem(res.getString("app.back"));

        smi1.setOnAction(event2 ->  fillTreeFind(new FindFilter(SearchActionType.IN_SELECTED_DEP,searchPatternField.getText())));
        smi2.setOnAction(event2 ->  fillTreeFind(new FindFilter(SearchActionType.IN_SELECTED_BASE, searchPatternField.getText())));
        smi3.setOnAction(event2 ->  fillTreeFind(new FindFilter(SearchActionType.IN_ALL_BASE,searchPatternField.getText())));
        smi4.setOnAction(event2 ->    clearSearch(true,true));


        searchMenu.getItems().addAll(smi3, smi2, smi1, spmi, smi4);
        searchBtn.setOnAction(event1 ->
        {
            //покажем пункты меню в зависимости от выбранных элементов базы и режима поиска!
            if (!searchState.isSearch()) smi4.setDisable(true);
            else smi4.setDisable(false);

            if (sectionCombo.getValue().getId().longValue() == 0) smi1.setDisable(true);
            else smi1.setDisable(false);


            //используем searchState объект
            if(!searchMenu.isShowing())searchMenu.show(searchBtn, Side.BOTTOM, 0, 0);
            else searchMenu.hide();
        });

        //нажатие на ввод вызовет поиск по всей базе
        searchPatternField.setOnAction(event1 ->
        {
            fillTreeFind(new FindFilter(SearchActionType.IN_ALL_BASE, searchPatternField.getText()));
            /*
              //нажатие на ввод вызовет поиск по выбранному разделу или базе
            if (sectionCombo.getValue().getId().longValue() == 0) fillTreeFind(new FindFilter(SearchActionType.IN_SELECTED_BASE, searchPatternField.getText()));
            else fillTreeFind(new FindFilter(SearchActionType.IN_SELECTED_DEP, searchPatternField.getText()));
            */
        });
/*
        searchTooltip.setText("Происк производится по нажатию кнопки 'Найти'\nили по нажатию кнопки 'Enter' на клавиатуре\n( произойдет поиск в выбранном разделе или базе если раздел не выбран).");
        searchPatternField.setTooltip(searchTooltip);
        hackTooltipStartTiming(searchTooltip, 250, 15000);
*/
    }

    private void baseInitBundlesSpinner() {
        bundlesPan.setVisible(false);
        bundlesBtnPan.setVisible(false);
        URL okUrl = getClass().getResource("/images/ok.png");
        URL cancelUrl = getClass().getResource("/images/cancel.png");
        btnOkBundles.setGraphic(new ImageView(new Image(okUrl.toExternalForm())));
        btnCancelBundles.setGraphic(new ImageView(new Image(cancelUrl.toExternalForm())));
    }

    private void baseInitSpinnerTimeForFreq() {
        timeToFreqSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.5,10.0,3.0,0.5));
        timeToFreqSpinner.setEditable(false);
        spinnerPan.setVisible(false);
        spinnerBtnPan.setVisible(false);
        URL okUrl = getClass().getResource("/images/ok.png");
        URL cancelUrl = getClass().getResource("/images/cancel.png");
        btnOkSpinner.setGraphic(new ImageView(new Image(okUrl.toExternalForm())));
        btnCancelSpinner.setGraphic(new ImageView(new Image(cancelUrl.toExternalForm())));
    }

    private void initUploadToDirDisabledPolicy(MenuItem btnUploadDir) {
        btnUploadDir.disableProperty().bind(new BooleanBinding() {
            {
                super.bind(tableProfile.getSelectionModel().selectedItemProperty(),checkUppload, btnGenerate.disabledProperty());//подключение устройств или выключение вызывает проверку computeValue() также если появиться необходимость генерации или мы переключаем профиль(если вдруг выбор пустой стал)
            }

            @Override
            protected boolean computeValue() {

                boolean res=false;
                if(btnGenerate.isDisable())  if(tableProfile.getSelectionModel().getSelectedItem()!=null)res = !getModel().isNeedGenerateFilesInProfile(tableProfile.getSelectionModel().getSelectedItem());


                //если устройство подключено, выбран профиль и не активна кнопка генерации, то можно совершить загрузку в устройство
                return !(res && tableProfile.getSelectionModel().getSelectedItem() != null);

            }
        });
    }

    private void initButtonUploadDisabledPolicy(MenuItem btnUpload) {
        btnUpload.disableProperty().bind(new BooleanBinding() {
            {
                super.bind(tableProfile.getSelectionModel().selectedItemProperty(),connectedDeviceProperty(),checkUppload, btnGenerate.disabledProperty());//подключение устройств или выключение вызывает проверку computeValue() также если появиться необходимость генерации или мы переключаем профиль(если вдруг выбор пустой стал)
            }

            @Override
            protected boolean computeValue() {

                boolean res=false;
             if(btnGenerate.isDisable())  if(tableProfile.getSelectionModel().getSelectedItem()!=null)res = !getModel().isNeedGenerateFilesInProfile(tableProfile.getSelectionModel().getSelectedItem());




                    //если устройство подключено, выбран профиль и не активна кнопка генерации, то можно совершить загрузку в устройство
                return !(res && tableProfile.getSelectionModel().getSelectedItem() != null && getConnectedDevice());

            }
        });
    }

    private void initVerticalDivider() {
        Platform.runLater(() -> balanceSpitterDividers());

        //будем подстраивать  dividers при изменении размеров контейнера таблиц, при движ ползунков это не работает, при изм размеров окна срабатывает
        splitOuter.widthProperty().addListener((observableValue, oldSceneHeight, newSceneHeight) -> balanceSpitterDividers());
    }

    private void initUploadMenuBtn() {

        MenuItem btnUploadDir=new MenuItem(res.getString("app.upload_to_dir"));
        btnUploadDir.setDisable(true);
        btnUploadDir.setOnAction(event -> uploadInDir());
        initUploadToDirDisabledPolicy(btnUploadDir);

        MenuItem btnUploadM2=new MenuItem(res.getString("app.ui.record_on_trinity"));
        btnUploadM2.setOnAction(event ->  uploadM2(tableProfile.getSelectionModel().getSelectedItem()));
        btnUploadM2.disableProperty().bind(m2Ready.and(tableProfile.getSelectionModel().selectedItemProperty().isNotNull()).not());

        MenuItem btnUpload=new MenuItem(res.getString("app.uppload"));
        btnUpload.setDisable(true);
        btnUpload.setOnAction(event -> onUploadProfile());
        initButtonUploadDisabledPolicy(btnUpload);

        uploadMenu.getItems().addAll(btnUploadDir,btnUpload, btnUploadM2);
        btnUploadm.setOnAction(event4 ->
        {
            if(!uploadMenu.isShowing()) uploadMenu.show(btnUploadm, Side.BOTTOM, 0, 0);
            else uploadMenu.hide();

        });



    }

    private void initTabs() {
        ObservableList<Tab> tabs = therapyTabPane.getTabs();
        tabs.get(1).disableProperty().bind(tableProfile.getSelectionModel().selectedItemProperty().isNull());
        tabs.get(2).disableProperty().bind(tableComplex.getSelectionModel().selectedItemProperty().isNull());


        //при переключении на вкладку профилей проверяем можно ли грузить файлы
        tabs.get(0).setOnSelectionChanged(e -> {if(tabs.get(0).isSelected()) checkUpploadBtn();});

        tabs.get(0).textProperty().bind(new StringBinding() {
            {
                //указывается через , список свойств изменения которых приведут к срабатыванию этого
                super.bind(tableProfile.getSelectionModel().selectedItemProperty());
            }
            @Override
            protected String computeValue() {
                if(tableProfile.getSelectionModel().getSelectedItem()!=null)
                {
                   return  baseProfileTabName+" ("+tableProfile.getSelectionModel().getSelectedItem().getName()+")";
                }else return baseProfileTabName;
            }
        });

        tableComplex.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

            if(newValue !=null)
            {
                String s = DateUtil.convertSecondsToHMmSs(AppController.this.getModel().getTimeTherapyComplex(newValue));
                setComplexTabName(baseComplexTabName+" ("+ newValue.getName() +") +("+s+")");


            }else setComplexTabName(baseComplexTabName);

        });

        tabs.get(2).textProperty().bind(new StringBinding() {
            {
                //указывается через , список свойств изменения которых приведут к срабатыванию этого
                super.bind(tableProgram.getSelectionModel().selectedItemProperty());
            }
            @Override
            protected String computeValue() {
                if(tableProgram.getSelectionModel().getSelectedItem()!=null)
                {
                    return  baseProgramTabName+" ("+tableProgram.getSelectionModel().getSelectedItem().getName()+")";
                }else return baseProgramTabName;
            }
        });

    }



    private void initNamesTables() {
        baseProfileTabName=res.getString("app.ui.tab1");
        baseComplexTabName=res.getString("app.ui.tab2");
        baseProgramTabName=res.getString("app.ui.tab3");
    }


    @FXML private AnchorPane tab5_content;
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
    private void uploadM2(Profile profile) {

        //проверка установленных пачек частот и если есть отличные от 3, то нужно указать, на это
        long  cnt=getModel().findAllTherapyComplexByProfile(profile).stream().filter(c->c.getBundlesLength()!=M2Complex.BUNDLES_LENGTH).count();
        if(cnt!=0){
            showWarningDialog(
                    res.getString("app.ui.record_on_trinity"),
                    res.getString("app.ui.attention"),
                    res.getString("app.m2.message"),
                    getApp().getMainWindow(),Modality.WINDOW_MODAL);
        }

        Platform.runLater(() ->   {
            m2Connected.set(false);
            m2Ready.setValue(false);
        });

        Task task = new Task() {
            protected Boolean call() {
                boolean res1=false;
                M2BinaryFile m2BinaryFile=null;
                try {
                    System.out.println("Запись на прибор");

                    m2BinaryFile = M2.uploadProfile(profile,true);

                    try {
                        Thread.sleep(1000);//таймаут дает время прибору подумать после записи
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                   // M2BinaryFile m2BinaryFile = M2.readFromDevice(true);
                    M2BinaryFile bf=m2BinaryFile;
                    Platform.runLater(() ->m2ui.setContent(bf));


                    res1=true;
                } catch (M2Complex.MaxTimeByFreqBoundException e) {
                    Platform.runLater(() -> showExceptionDialog( res.getString("app.ui.record_on_trinity"),
                            res.getString("app.error"),e.getMessage(),e, getApp().getMainWindow(),Modality.WINDOW_MODAL));
                } catch (M2Complex.MaxPauseBoundException e) {
                    Platform.runLater(() -> showExceptionDialog( res.getString("app.ui.record_on_trinity"),res.getString("app.error"),e.getMessage(),e, getApp().getMainWindow(),Modality.WINDOW_MODAL));
                } catch (M2Program.ZeroValueFreqException e) {
                    Platform.runLater(() -> showExceptionDialog( res.getString("app.ui.record_on_trinity"),res.getString("app.error"),res.getString("zerofreqval"),e, getApp().getMainWindow(),Modality.WINDOW_MODAL));
                } catch (M2Program.MaxProgramIDValueBoundException e) {
                    Platform.runLater(() ->  showExceptionDialog( res.getString("app.ui.record_on_trinity"),res.getString("app.error"),e.getMessage(),e, getApp().getMainWindow(),Modality.WINDOW_MODAL));
                } catch (M2Program.MinFrequenciesBoundException e) {
                    Platform.runLater(() -> showExceptionDialog( res.getString("app.ui.record_on_trinity"),res.getString("app.error"),res.getString("mustbefreqs"),e, getApp().getMainWindow(),Modality.WINDOW_MODAL));
                } catch (M2Complex.MaxCountProgramBoundException e) {
                    Platform.runLater(() ->  showExceptionDialog( res.getString("app.ui.record_on_trinity"),res.getString("app.error"),res.getString("moreprograms"),e, getApp().getMainWindow(),Modality.WINDOW_MODAL));
                } catch (M2BinaryFile.MaxBytesBoundException e) {
                    Platform.runLater(() -> showExceptionDialog( res.getString("app.ui.record_on_trinity"),res.getString("app.error"),res.getString("trinity.maxboundsize"),e, getApp().getMainWindow(),Modality.WINDOW_MODAL));
                } catch (M2Complex.ZeroCountProgramBoundException e) {
                    Platform.runLater(() -> showErrorDialog( res.getString("app.ui.record_on_trinity"),res.getString("app.error"),res.getString("app.ui.must_have_program"), getApp().getMainWindow(),Modality.WINDOW_MODAL));
                } catch (LanguageDevice.NoLangDeviceSupported e) {
                    Platform.runLater(() -> showErrorDialog( res.getString("app.ui.record_on_trinity"),res.getString("app.error"),res.getString("app.ui.lang_not_support"), getApp().getMainWindow(),Modality.WINDOW_MODAL));
                } catch (M2.WriteToDeviceException e) {
                    Platform.runLater(() -> showExceptionDialog( res.getString("app.ui.record_on_trinity"),res.getString("app.error"),e.getMessage(),e, getApp().getMainWindow(),Modality.WINDOW_MODAL));
                } /*catch (M2.ReadFromDeviceException e) {
                    Platform.runLater(() -> {
                        showExceptionDialog(res.getString("app.ui.reading_device"),res.getString("app.error"),"",e,getApp().getMainWindow(),Modality.WINDOW_MODAL);
                    });
                }*/catch (Exception e){
                    Platform.runLater(() ->  showExceptionDialog( res.getString("app.ui.record_on_trinity"),res.getString("app.error"),e.getMessage(),e, getApp().getMainWindow(),Modality.WINDOW_MODAL));

                }finally {
                    Platform.runLater(() ->   {
                        m2Connected.set(true);
                        m2Ready.setValue(true);
                    });
                }
                return res1;
            }
        };

        task.setOnScheduled((event) -> {
            Waiter.openLayer(getApp().getMainWindow(), true);
        });
        task.setOnFailed(ev -> {
            Waiter.closeLayer();
            Platform.runLater(() ->  showErrorDialog( res.getString("app.ui.record_on_trinity"),"" , res.getString("app.error"), getApp().getMainWindow(),Modality.WINDOW_MODAL));

        });
        task.setOnSucceeded(ev -> {
            Waiter.closeLayer();
            if(((Boolean)task.getValue()).booleanValue()) {
                showInfoDialog(res.getString("app.ui.uploadM2"), "",res.getString("app.ui.upload_ok"), getApp().getMainWindow(), Modality.WINDOW_MODAL);
            }

        });
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();



    }


    /******* Биофон *****/



    @FXML ListView<TherapyComplex> biofonCompexesList;
    @FXML ListView<TherapyProgram> biofonProgramsList;

    @FXML private Button bComplexAdd;
    @FXML private Button bComplexMenu;
    @FXML private Button bComplexDel;
    @FXML private Button bComplexEdit;

    @FXML private Button bProgramUp;
    @FXML private Button bProgramDown;
    @FXML private Button bProgramDel;

    @FXML private Button uploadBiofonBtn;


    @FXML private Spinner<Integer> timeToFreqSpinnerBiofon;

    @FXML private HBox spinnerPanBiofon;
    @FXML private VBox spinnerBtnPanBiofon;
    @FXML private Button  btnOkSpinnerBiofon;
    @FXML private Button  btnCancelSpinnerBiofon;

    @FXML private Button bComplexSort;

    @FXML private Spinner<String> bundlesSpinnerBiofon;
    @FXML  private VBox bundlesBtnPanBiofon;
    @FXML private Button btnOkBundlesBiofon;
    @FXML private Button btnCancelBundlesBiofon;


    @FXML private HBox bundlesPanBiofon;
    @FXML private Label biofonInsLangComplex;
    @FXML private Label biofonInsLangProgram;


    @FXML private Label  tToFBiofonInfo;
    @FXML private Label bundlesBiofonInfo;
    @FXML private Label countProgramsBiofonInfo;
    @FXML private Label  complexTimeBiofon;

    @FXML private Button loadIndicator;

   // @FXML  private ObservableList<String>  bundlesSpinnerDataBiofon;

    private BiofonUIUtil biofonUIUtil;
    private ContextMenu biofonComplexesMenu=new ContextMenu();

    private void addComplexToBiofonTab(TherapyComplex tc)
    {
        biofonUIUtil.addComplex(tc);
    }
    private void addProgramToBiofonTab(TherapyComplex tc,TherapyProgram tp)
    {
        biofonUIUtil.addProgram(tp);
    }


    private MenuItem biofonPrintMi=new MenuItem();
    private MenuItem biofonImportMi=new MenuItem();
    private MenuItem biofonExportMi=new MenuItem();

    private void initContextMenuComplexes(){

        URL imgb = getClass().getResource("/images/print.png");
        biofonPrintMi.setGraphic(new ImageView(new Image(imgb.toExternalForm())));

        imgb = getClass().getResource("/images/import.png");
        biofonImportMi.setGraphic(new ImageView(new Image(imgb.toExternalForm())));

        imgb = getClass().getResource("/images/export.png");
        biofonExportMi.setGraphic(new ImageView(new Image(imgb.toExternalForm())));





        biofonPrintMi.setText(res.getString("app.ui.printing_complexes"));


        biofonImportMi.setText(res.getString("app.ui.import_complexes"));

        biofonExportMi.setText(res.getString("app.ui.export_complexes"));




        biofonExportMi.setOnAction(event -> exportTherapyComplexes(biofonUIUtil.getSelectedComplexes()) );
        biofonImportMi.setOnAction(event -> {

            importTherapyComplex(getApp().getBiofonProfile(),nums -> {
                if(nums==0) return;
              getModel().getLastTherapyComplexes(nums).forEach(therapyComplex ->  biofonUIUtil.addComplex(therapyComplex));
            });




        });

        biofonPrintMi.setOnAction(event -> printComplexes(biofonUIUtil.getSelectedComplexes()));




        biofonComplexesMenu.getItems().addAll(biofonPrintMi,biofonImportMi,biofonExportMi);


    }


    private void hideTFSpinnerBTNPanBiofon(int val)
    {
        timeToFreqSpinnerBiofon.getValueFactory().setValue(val);
        spinnerBtnPanBiofon.setVisible(false);

    }
    private void hideTFSpinnerBTNPanBiofon()
    {

        spinnerBtnPanBiofon.setVisible(false);

    }

    private void hideBundlesSpinnerBTNPanBiofon(int val)
    {
        bundlesSpinnerBiofon.getValueFactory().setValue(String.valueOf(val));
        bundlesBtnPanBiofon.setVisible(false);

    }

    private void hideBundlesSpinnerBTNPanBiofon()
    {

        bundlesBtnPanBiofon.setVisible(false);

    }



    private void initBiofonSpinners(){






        /** Спиннер внемя на частоту **/

        timeToFreqSpinnerBiofon.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 7, 3, 1));
        timeToFreqSpinnerBiofon.setEditable(true);
        spinnerPanBiofon.setVisible(false);
        spinnerBtnPanBiofon.setVisible(false);


        URL okUrl = getClass().getResource("/images/ok.png");
        URL cancelUrl = getClass().getResource("/images/cancel.png");
        btnOkSpinnerBiofon.setGraphic(new ImageView(new Image(okUrl.toExternalForm())));
        btnCancelSpinnerBiofon.setGraphic(new ImageView(new Image(cancelUrl.toExternalForm())));




        //показывает кнопки при изменениях спинера
        timeToFreqSpinnerBiofon.valueProperty().addListener((observable, oldValue, newValue) -> {if(oldValue!=newValue) spinnerBtnPanBiofon.setVisible(true);});
        //кнопка отмены
        btnCancelSpinnerBiofon.setOnAction(event ->hideTFSpinnerBTNPanBiofon(biofonCompexesList.getSelectionModel().getSelectedItem().getTimeForFrequency()/60) );
        //принять изменения времени
        btnOkSpinnerBiofon.setOnAction(event ->
        {

            ObservableList<TherapyComplex> selectedComplex = biofonCompexesList.getSelectionModel().getSelectedItems();
            if(!biofonCompexesList.getSelectionModel().getSelectedItems().isEmpty()) {
                List<TherapyComplex> items = new ArrayList<>(selectedComplex);

                try {


                    for(TherapyComplex item:items) {


                        item.setTimeForFrequency(this.timeToFreqSpinnerBiofon.getValue()*60);
                        this.getModel().updateTherapyComplex(item);

                    }
                    biofonUIUtil.viewComplexTime(biofonCompexesList.getSelectionModel().getSelectedItem(),biofonProgramsList.getItems());

                } catch (Exception var8) {
                    this.hideTFSpinnerBTNPanBiofon(biofonCompexesList.getSelectionModel().getSelectedItem().getTimeForFrequency().intValue()/60);
                    Log.logger.error("", var8);
                    showExceptionDialog("Ошибка обновления времени на частоту в терапевтическом комплексе", "", "", var8, getApp().getMainWindow(), Modality.WINDOW_MODAL);
                } finally {
                    this.hideTFSpinnerBTNPanBiofon();
                }

            }
        });





/*******************/

        /** Комбо пачек частот **/


        ObservableList<String> bundlesSpinnerDataBiofon = FXCollections.observableArrayList();




        for(int i=2; i<=MAX_BUNDLES; i++)bundlesSpinnerDataBiofon.add(String.valueOf(i));
        bundlesSpinnerBiofon.setValueFactory(new SpinnerValueFactory.ListSpinnerValueFactory<String>(bundlesSpinnerDataBiofon));
        bundlesSpinnerBiofon.getValueFactory().setValue("2");

        btnOkBundlesBiofon.setGraphic(new ImageView(new Image(okUrl.toExternalForm())));
        btnCancelBundlesBiofon.setGraphic(new ImageView(new Image(cancelUrl.toExternalForm())));

        //показывает кнопки при изменениях спинера
        bundlesSpinnerBiofon.valueProperty().addListener((observable, oldValue, newValue) -> {if(oldValue!=newValue) bundlesBtnPanBiofon.setVisible(true);});
        //кнопка отмены
        btnCancelBundlesBiofon.setOnAction(event ->hideBundlesSpinnerBTNPanBiofon(biofonCompexesList.getSelectionModel().getSelectedItem().getBundlesLength()) );

/*******************/

        //обработчик нажатия на комплекс. Есть еще такой в BiofonUtils
        biofonCompexesList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

            if( biofonCompexesList.getSelectionModel().getSelectedItems().size() > 0)bundlesSpinnerBiofon.setDisable(false);
            else bundlesSpinnerBiofon.setDisable(true);


            if(newValue==null) {
                hideTFSpinnerBTNPanBiofon();
                hideBundlesSpinnerBTNPanBiofon();
                return;
            }

            hideTFSpinnerBTNPanBiofon(newValue.getTimeForFrequency()/60);
            hideBundlesSpinnerBTNPanBiofon(newValue.getBundlesLength()<2?2:newValue.getBundlesLength());
        });



        //принять изменения времени
        btnOkBundlesBiofon.setOnAction(event ->
        {


            ObservableList<TherapyComplex> selectedItems = biofonCompexesList.getSelectionModel().getSelectedItems();
            if(selectedItems ==null) return;

            if(biofonCompexesList.getSelectionModel().getSelectedItems().isEmpty()) { hideBundlesSpinnerBTNPanBiofon();return;}


            try {
                for (TherapyComplex complex : selectedItems) {

                    complex.setBundlesLength(Integer.parseInt(bundlesSpinnerBiofon.getValue()));
                    this.getModel().updateTherapyComplex(complex);
                    hideBundlesSpinnerBTNPanBiofon();
                }
                biofonUIUtil.viewComplexTime(biofonCompexesList.getSelectionModel().getSelectedItem(),biofonProgramsList.getItems());

            } catch (Exception e) {
                hideBundlesSpinnerBTNPanBiofon();
               Log.logger.error("Ошибка установки пачек частот", e);
                showExceptionDialog("Ошибка установки пачек частот", "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);
            }

        });





        spinnerPanBiofon.visibleProperty().bind(biofonCompexesList.getSelectionModel().selectedItemProperty().isNotNull());
        bundlesPanBiofon.visibleProperty().bind(biofonCompexesList.getSelectionModel().selectedItemProperty().isNotNull());

    }

    /**
     * Копирует комплексы в профиль биофона
     */
    private void  complexesToBiofon(List<TherapyComplex> tcs){

        biofonUIUtil.complexesToBiofon( tcs);

    }


    @FXML private Label biofonRedInd;
    @FXML private Label biofonBlueInd;
    @FXML private Label biofonGreenInd;
    @FXML private  ProgressIndicator biofonProgressIndicator;
    @FXML private  Button biofonBtnComplex1;
    @FXML private  Button biofonBtnComplex2;
    @FXML private  Button biofonBtnComplex3;

    /**
     * Обработчик подключения биофона
     */
    void onAttachBiofon(){
        biofonRedInd.setVisible(true);
        biofonBlueInd.setVisible(true);
        biofonGreenInd.setVisible(true);
    }

    /**
     * Обработчик отключения биофона
     */
    void onDetachBiofon(){
        biofonRedInd.setVisible(false);
        biofonBlueInd.setVisible(false);
        biofonGreenInd.setVisible(false);
    }

    void showBiofonProgressIndicator(){
        biofonProgressIndicator.setVisible(true);
    }
    void hideBiofonProgressIndicator(){
        biofonProgressIndicator.setVisible(false);
    }



    private void initBiofon() {

        biofonRedInd.setVisible(false);
        biofonBlueInd.setVisible(false);
        biofonGreenInd.setVisible(false);

        uploadBiofonBtn.setDisable(true);

        biofonUIUtil=new BiofonUIUtil(res,
                getApp(),this,
                getModel(),
                getApp().getBiofonProfile(),
                biofonCompexesList,
                biofonProgramsList,
                biofonInsLangComplex,
                biofonInsLangProgram,
                this::onAttachBiofon,
                this::onDetachBiofon,
                tToFBiofonInfo,
                bundlesBiofonInfo,
                countProgramsBiofonInfo,
                complexTimeBiofon,
                loadIndicator
        );

        biofonUIUtil.init();
        biofonUIUtil.init3ComplexesButtons(biofonBtnComplex1,biofonBtnComplex2,biofonBtnComplex3);
        biofonUIUtil.initUpload(uploadBiofonBtn);

        initContextMenuComplexes();

        initBiofonButtons();
        initBiofonSpinners();

        biofonInsLangComplex.setText("");
        biofonInsLangProgram.setText("");


    }



    private void initBiofonButtons(){
        //bComplexAdd;

        bProgramDown.disableProperty().bind(new BooleanBinding() {
            {super.bind(biofonProgramsList.getSelectionModel().selectedIndexProperty());}
            @Override
            protected boolean computeValue() {

               if(biofonProgramsList.getSelectionModel().getSelectedItem()==null) return true;
               if(biofonProgramsList.getSelectionModel().getSelectedIndex() == biofonProgramsList.getItems().size()-1)return true;
               return false;
            }
        });
        bProgramUp.disableProperty().bind(new BooleanBinding() {
            {super.bind(biofonProgramsList.getSelectionModel().selectedIndexProperty());}
            @Override
            protected boolean computeValue() {

                if(biofonProgramsList.getSelectionModel().getSelectedItem()==null) return true;
                if(biofonProgramsList.getSelectionModel().getSelectedIndex() == 0)return true;
                return false;
            }
        });

        bComplexEdit.disableProperty().bind(biofonCompexesList.getSelectionModel().selectedItemProperty().isNull());

        biofonPrintMi.disableProperty().bind(biofonCompexesList.getSelectionModel().selectedItemProperty().isNull());
        bComplexDel.disableProperty().bind(biofonCompexesList.getSelectionModel().selectedItemProperty().isNull());
        biofonExportMi.disableProperty().bind(biofonCompexesList.getSelectionModel().selectedItemProperty().isNull());
        //bProgramUp.disableProperty().bind(biofonProgramsList.getSelectionModel().selectedItemProperty().isNull());
        //bProgramDown.disableProperty().bind(biofonProgramsList.getSelectionModel().selectedItemProperty().isNull());
        bProgramDel.disableProperty().bind(biofonProgramsList.getSelectionModel().selectedItemProperty().isNull());

        bComplexAdd.setOnAction(event -> biofonUIUtil.addComplex());
        bComplexMenu.setOnAction(event -> biofonComplexesMenu.show(bComplexMenu,Side.BOTTOM,0,0));

        bComplexEdit.setOnAction(event -> biofonUIUtil.editComplex());
        bComplexDel.setOnAction(event -> biofonUIUtil.delComplex());

        bProgramUp.setOnAction(event -> biofonUIUtil.upProgram());
        bProgramDown.setOnAction(event -> biofonUIUtil.downProgram());
        bProgramDel.setOnAction(event -> biofonUIUtil.delProgram());

        bComplexSort.getStyleClass().addAll("GenericBtn","SortTimeBtn");
        bComplexSort.setOnAction(event -> {

            if(bComplexSort.getStyleClass().contains("SortTimeBtn")){
                bComplexSort.getStyleClass().remove("SortTimeBtn");
                bComplexSort.getStyleClass().add("SortAlphBtn");
                biofonUIUtil.changeComplexesSortType(BiofonUIUtil.SortType.NAME);
            }else {
                bComplexSort.getStyleClass().remove("SortAlphBtn");
                bComplexSort.getStyleClass().add("SortTimeBtn");
                biofonUIUtil.changeComplexesSortType(BiofonUIUtil.SortType.TIME);
            }

        });

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
    private List<MenuItem> tablesMenuHelper;

    private ProfileTable profileTable;
    private ComplexTable complexTable;
    private ProgramTable programTable;
    private void initTables()
    {
        tablesMenuHelper = initContextMenuHotKeyHolders();


        /*** Профили  ****/
        profileTable = initProfileTable();
        profileTable.initProfileContextMenu( this::onPrintProfile,  this::cutInTables,  this::pasteInTables, this::deleteInTables);
        initProfileSelectedListener();


        /*** Комплексы  ****/
         complexTable = initComplexesTable();
        initGenerateComplexesButton();
        complexTable.initComplexesContextMenu(() -> devicePath!=null,
                this::onPrintComplex,
                this::cutInTables,
                this::deleteInTables,
                this::copyTherapyComplexToBase,
                this::generateComplexes,
                this::uploadComplexesToDir,
                this::uploadComplexesToM,
                this::copyInTables,
                this::pasteInTables,
                this::complexesToBiofon,
                ()->{
                    boolean res=true;
                    if(sectionTree.getSelectionModel().getSelectedItem() != null) {
                        if(((TreeItem)sectionTree.getSelectionModel().getSelectedItem()).getValue() instanceof Section) {
                            String tag1 = ((Section)baseCombo.getSelectionModel().getSelectedItem()).getTag();
                            if(tag1 != null && tag1.equals("USER")) res = false;
                        }
                    }
                    return res;

                });
        initUploadComplexesContextMenu();
        initComplexSelectedListener();
        initComplexSpinnerTimeForFreq();
        initComplexBundlesLength();
        //установка имени таба комплексов с учетом времени
        initTabComplexNameListener();

        /*** Программы  ****/
         programTable = initProgramsTable();
        programTable.initProgramsTableContextMenu(this::copyTherapyProgramToBase,
                this::editMP3ProgramPath,
                this::cutInTables,
                this::copyInTables,
                this::pasteInTables,
                this::deleteInTables,
                ()->therapyProgramsCopied,
                ()-> {
                    boolean res = true;
                    if (sectionTree.getSelectionModel().getSelectedItem() != null) {
                        INamed value = sectionTree.getSelectionModel().getSelectedItem().getValue();
                        if (value instanceof Section || value instanceof Complex) {
                            String tag = ((Section) baseCombo.getSelectionModel().getSelectedItem()).getTag();
                            if (tag != null && tag.equals("USER")) return false;
                        }
                    }
                    return res;
                });


        initDeleteAndSwitchTableKeys();
        initSwitchTabByDoubleClick();

    }

    private void initComplexBundlesLength() {
        ObservableList<String> bundlesSpinnerData = FXCollections.observableArrayList();
        for(int i=2; i<=MAX_BUNDLES; i++)bundlesSpinnerData.add(String.valueOf(i));
        // Value factory.

        bundlesSpinner.setValueFactory(new SpinnerValueFactory.ListSpinnerValueFactory<String>(bundlesSpinnerData));
        bundlesSpinner.getValueFactory().setValue("2");

        //показывает кнопки при изменениях спинера
        bundlesSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {if(oldValue!=newValue) bundlesBtnPan.setVisible(true);});
        //кнопка отмены
        btnCancelBundles.setOnAction(event ->hideBundlesSpinnerBTNPan(tableComplex.getSelectionModel().getSelectedItem().getBundlesLength()) );


        //принять изменения пачек частот
        btnOkBundles.setOnAction(event ->
        {

            if(!this.tableComplex.getSelectionModel().getSelectedItems().isEmpty()) {
                List<TherapyComplex> items = new ArrayList<>(this.tableComplex.getSelectionModel().getSelectedItems());

                try {


                    for(TherapyComplex item:items) {


                        item.setBundlesLength(Integer.parseInt(bundlesSpinner.getValue()));
                        item.setChanged(true);
                        this.getModel().updateTherapyComplex(item);
                        this.btnGenerate.setDisable(false);                   }

                    this.updateComplexsTime(items, true);
                } catch (Exception var8) {
                    this.hideBundlesSpinnerBTNPan(this.tableComplex.getSelectionModel().getSelectedItem().getBundlesLength());
                    Log.logger.error("", var8);
                    showExceptionDialog("Ошибка обновления времени в терапевтическом комплексе", "", "", var8, getApp().getMainWindow(), Modality.WINDOW_MODAL);
                } finally {
                    this.hideTFSpinnerBTNPan();
                }

            }

        });
    }

    private void initComplexSpinnerTimeForFreq() {
        //показывает кнопки при изменениях спинера
        timeToFreqSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(oldValue!=newValue) {
                spinnerBtnPan.setVisible(true);
            }
            //коррекция значения не кратного 30сек
            int newTime=(int)(newValue*60);
            if(newTime%30!=0){
                TherapyComplex sCompl = tableComplex.getSelectionModel().getSelectedItem();
                if(sCompl!=null) {
                    int st=0;
                    if( newTime % 30 >15)st=newTime + (30-newTime % 30);
                    else st=newTime - newTime % 30;
                    sCompl.setTimeForFrequency(st);
                    try {
                        this.getModel().updateTherapyComplex(sCompl);
                        timeToFreqSpinner.getValueFactory().setValue(st / 60.0);
                    } catch (Exception e) {
                      logger.error("",e);
                    }

                }
            }
        });
        //кнопка отмены
        btnCancelSpinner.setOnAction(event ->hideTFSpinnerBTNPan(tableComplex.getSelectionModel().getSelectedItem().getTimeForFrequency()) );
        //принять изменения времени
        btnOkSpinner.setOnAction(event ->
        {


            if(!this.tableComplex.getSelectionModel().getSelectedItems().isEmpty()) {
                List<TherapyComplex> items = new ArrayList<>(this.tableComplex.getSelectionModel().getSelectedItems());

                try {


                   for(TherapyComplex item:items) {


                        item.setTimeForFrequency((int)(this.timeToFreqSpinner.getValue()*60));
                        item.setChanged(true);
                        this.getModel().updateTherapyComplex(item);
                        this.btnGenerate.setDisable(false);
                    }

                    this.updateComplexsTime(items, true);
                } catch (Exception var8) {
                    this.hideTFSpinnerBTNPan(this.tableComplex.getSelectionModel().getSelectedItem().getTimeForFrequency().intValue());
                    Log.logger.error("", var8);
                    showExceptionDialog("Ошибка обновления времени в терапевтическом комплексе", "", "", var8, getApp().getMainWindow(), Modality.WINDOW_MODAL);
                } finally {
                    this.hideTFSpinnerBTNPan();
                }

            }
        });
    }

    private void initComplexSelectedListener() {
        tableComplex.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {



                if( tableComplex.getSelectionModel().getSelectedItems().size()>0)bundlesSpinner.setDisable(false);
                else bundlesSpinner.setDisable(true);

            if (oldValue != newValue) {

                //закроем кнопки спинера времени на частоту, при переключении компелекса
                if(newValue!=null) {
                    Platform.runLater(() -> {
                        if(newValue!=null){
                            hideTFSpinnerBTNPan(newValue.getTimeForFrequency());
                            if(tableComplex.getSelectionModel().getSelectedItem()==null) hideBundlesSpinnerBTNPan();
                            else  hideBundlesSpinnerBTNPan(tableComplex.getSelectionModel().getSelectedItem().getBundlesLength());
                        }else {
                            hideTFSpinnerBTNPan();
                            hideBundlesSpinnerBTNPan();
                        }


                    });

                }
                else  {
                    hideTFSpinnerBTNPan();
                    hideBundlesSpinnerBTNPan();

                }

                tableProgram.getItems().clear();

                tableProgram.getItems().addAll(getModel().findTherapyPrograms(newValue));


            }


        });
    }

    private void initProfileSelectedListener() {
        tableProfile.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
        {


            if (oldValue != newValue) {
                //закроем кнопки спинера времени на частоту
                hideTFSpinnerBTNPan();
                hideBundlesSpinnerBTNPan();

                tableComplex.getItems().clear();
                //добавляем через therapyComplexItems иначе не будет работать event на изменение элементов массива и не будут работать галочки мультичастот

                List<TherapyComplex> therapyComplexes = getModel().findTherapyComplexes(newValue);
                try {
                    checkBundlesLength(therapyComplexes);
                } catch (Exception e) {
                    Log.logger.error("",e);
                   showExceptionDialog("Ошибка обновления комплексов","","",e,getApp().getMainWindow(), Modality.WINDOW_MODAL);
                   return;
                }

                tableComplex.getItems().addAll(therapyComplexes);


                if(newValue!=null){

                    btnGenerate.setDisable(!getModel().isNeedGenerateFilesInProfile(newValue));

                }
            }

        });
    }

    private void initSwitchTabByDoubleClick() {
        tableProfile.setOnMouseClicked(event -> {
            if(event.getClickCount()==2) {
                event.consume();
                //int selectedIndex = tableProfile.getSelectionModel().getSelectedIndex();
               // tableProfile.getSelectionModel().clearSelection();
                //tableProfile.getSelectionModel().select(selectedIndex);
                if(tableProfile.getSelectionModel().getSelectedItem()!=null) therapyTabPane.getSelectionModel().select(1);

            }
        });

        tableComplex.setOnMouseClicked(event -> {
            if(event.getClickCount()==2) {
                event.consume();

                if(tableProfile.getSelectionModel().getSelectedItem()!=null)therapyTabPane.getSelectionModel().select(2);

            }
        });
    }



    private void initDeleteAndSwitchTableKeys() {
        tableProfile.setOnKeyReleased(event ->
        {
            if(event.getCode()== KeyCode.DELETE) onRemoveProfile();
            else
            if(event.getCode()==KeyCode.RIGHT && !tab2.isDisable()){
                  if(ProfileTable.getInstance().isTextEdited()) return;
                therapyTabPane.getSelectionModel().select(1);
                tableComplex.requestFocus();
                if(tableComplex.getItems().size()!=0){
                    tableComplex.getFocusModel().focus(tableComplex.getSelectionModel().getSelectedIndex());

                }
            }
        });

        this.tableComplex.setOnKeyReleased((e) -> {
            if(e.getCode() == KeyCode.DELETE) {
                this.onRemoveComplex();
            }else

            if(e.getCode() == KeyCode.A && e.isControlDown()) {
                if(ComplexTable.getInstance().isTextEdited()) return;
                this.tableComplex.getSelectionModel().selectAll();
            }else

            if(e.getCode()==KeyCode.RIGHT  && !tab3.isDisable()) {
                if(ComplexTable.getInstance().isTextEdited()) return;
                therapyTabPane.getSelectionModel().select(2);
                tableProgram.requestFocus();
                if(tableProgram.getItems().size()!=0){
                    tableProgram.getFocusModel().focus(tableProgram.getSelectionModel().getSelectedIndex());

                }
            }else
            if(e.getCode()==KeyCode.LEFT  && !tab1.isDisable()) {
                if(ComplexTable.getInstance().isTextEdited()) return;
                therapyTabPane.getSelectionModel().select(0);
                tableProfile.requestFocus();
                if(tableProfile.getItems().size()!=0){
                    tableProfile.getFocusModel().focus(tableProgram.getSelectionModel().getSelectedIndex());

            }

        }});

        tableProgram.setOnKeyReleased(e ->{
            //if(e.getCode()==KeyCode.DELETE) onRemovePrograms();
             if(e.getCode()==KeyCode.LEFT && !tab2.isDisable()) {
                therapyTabPane.getSelectionModel().select(1);
                tableComplex.requestFocus();
                if(tableComplex.getItems().size()!=0){
                    tableComplex.getFocusModel().focus(tableComplex.getSelectionModel().getSelectedIndex());
                }
            }


        });
    }

    private ProgramTable initProgramsTable() {
        return ProgramTable.init(tableProgram,res,imageCancel,imageDone,imageSeq,imageParallel,(needUpdateProfileTime) -> {
            updateComplexTime(ComplexTable.getInstance().getSelectedItem(),true);
            if(needUpdateProfileTime)updateProfileTime(ProfileTable.getInstance().getSelectedItem());
        });
    }

    private void initGenerateComplexesButton() {
        generationComplexesBtn.disableProperty().bind(new BooleanBinding() {
            {super.bind(tableComplex.getSelectionModel().selectedItemProperty());}
            @Override
            protected boolean computeValue() {
                boolean res=true;
                for (TherapyComplex therapyComplex : tableComplex.getSelectionModel().getSelectedItems()) {
                    if( therapyComplex.isChanged() || getModel().hasNeedGenerateProgramInComplex(therapyComplex) ){
                       res=false;
                       break;
                    }
                }

                return res;

            }
        });
        generationComplexesBtn.setOnAction(e->generateComplexes());
    }

    private ComplexTable initComplexesTable() {
       return ComplexTable.init(tableComplex,res,imageCancel,imageDone);
    }



    private ProfileTable initProfileTable() {
        return ProfileTable.init(tableProfile, res);
    }

    private void initTabComplexNameListener() {
        complexTable.textComplexTimeProperty().addListener((observable, oldValue, newValue) -> {

            String[] strings = newValue.split("#");
             if(strings.length!=0)
             {
                 TherapyComplex selectedItem = tableComplex.getSelectionModel().getSelectedItem();
                 if(selectedItem==null) return;


                 long idC= Long.parseLong(strings[1]);
                 if(idC!=selectedItem.getId().longValue())return;//если изменения не в выбраном комплексе, то и считать не надо
                 setComplexTabName(baseComplexTabName+" ("+ selectedItem.getName() +") +("+strings[0]+")") ;
             }

        });
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

    private void initUploadComplexesContextMenu() {
        MenuItem mic3_ = new MenuItem(this.res.getString("app.upload_to_dir"));
        MenuItem mic5_ = new MenuItem(this.res.getString("app.upload_to_biomedism"));
        mic3_.setOnAction(event -> uploadComplexesToDir());
        mic5_.setOnAction(event -> uploadComplexesToM());
        uploadComplexesMenu.setOnShowing(event -> {
            mic3_.setDisable(false);
            mic5_.setDisable(false);
            if(this.tableComplex.getSelectionModel().getSelectedItems().isEmpty()) {

                mic3_.setDisable(true);
                mic5_.setDisable(true);
            } else{
                Iterator tag = this.tableComplex.getSelectionModel().getSelectedItems().iterator();

                while(tag.hasNext()) {
                    TherapyComplex therapyComplex = (TherapyComplex)tag.next();


                    if( therapyComplex.isChanged() || this.getModel().hasNeedGenerateProgramInComplex(therapyComplex) ) {
                        mic3_.setDisable(true);
                        mic5_.setDisable(true);
                        break;
                    }
                }
                if(devicePath!=null && !mic5_.isDisable() )   mic5_.setDisable(false);
                else  mic5_.setDisable(true);
            }
        });


        uploadComplexesMenu.getItems().addAll(new MenuItem[]{ mic3_, mic5_});
        uploadComplexesBtn.setOnAction(event ->
                {
                    if(!uploadComplexesMenu.isShowing()) uploadComplexesMenu.show(uploadComplexesBtn, Side.BOTTOM, 0, 0);
                    else uploadComplexesMenu.hide();

                }
        );
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
        Profile profile = tableProfile.getSelectionModel().getSelectedItem();
        if(profile==null) return;
        int dropIndex=tableProfile.getSelectionModel().getSelectedIndex();

        Clipboard clipboard= Clipboard.getSystemClipboard();
        if(!clipboard.hasContent(ProfileTable.PROFILE_CUT_ITEM_ID)) return;
        if(!clipboard.hasContent(ProfileTable.PROFILE_CUT_ITEM_INDEX)) return;


            Integer ind = (Integer) clipboard.getContent(ProfileTable.PROFILE_CUT_ITEM_INDEX);
            if (ind == null) return;
            else {
                if (dropIndex == ind) return;
                else {
                   Profile movedProfile = tableProfile.getItems().get(ind);
                   if(movedProfile==null) {
                       clipboard.clear();
                       return;
                   }
                    tableProfile.getItems().remove(movedProfile);
                    tableProfile.getItems().add(dropIndex,movedProfile);
                    Profile tmp;
                    try {
                    for (int i=0; i<tableProfile.getItems().size();i++){
                        tmp = tableProfile.getItems().get(i);
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
        Profile profile = tableProfile.getSelectionModel().getSelectedItem();
        if(profile==null) return;
        Clipboard clipboard=Clipboard.getSystemClipboard();
        clipboard.clear();
        ClipboardContent content = new ClipboardContent();
        content.put(ProfileTable.PROFILE_CUT_ITEM_ID, profile.getId());
        content.put(ProfileTable.PROFILE_CUT_ITEM_INDEX, tableProfile.getSelectionModel().getSelectedIndex());
        clipboard.setContent(content);
    }




    private void cutSelectedTherapyProgramsToBuffer() {
        if (tableProgram.getSelectionModel().getSelectedItems().isEmpty()) return;
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.clear();

        content.put(ProgramTable.PROGRAM_CUT_ITEM_INDEX, tableProgram.getSelectionModel().getSelectedIndices().toArray(new Integer[0]));
        content.put(ProgramTable.PROGRAM_CUT_ITEM_COMPLEX, tableComplex.getSelectionModel().getSelectedItem().getId());
        content.put(ProgramTable.PROGRAM_CUT_ITEM_ID, tableProgram.getSelectionModel().getSelectedItems().stream()
                .map(i->i.getId())
                .collect(Collectors.toList())
                .toArray(new Long[0])
        );
        clipboard.setContent(content);
        therapyProgramsCopied=false;
    }

    private void copySelectedTherapyProgramsToBuffer() {
        if (tableProgram.getSelectionModel().getSelectedItems().isEmpty()) return;
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.clear();

        content.put(ProgramTable.PROGRAM_COPY_ITEM, tableProgram.getSelectionModel().getSelectedItems().stream()
               .map(i->i.getId()).collect(Collectors.toList()).toArray(new Long[0]));
        clipboard.setContent(content);
        therapyProgramsCopied=true;
    }

    /**
     * Вырезать комплексы
     */
    private void cutSelectedTherapyComplexesToBuffer() {
        if (tableComplex.getSelectionModel().getSelectedItems().isEmpty()) return;
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.clear();

        content.put(ComplexTable.COMPLEX_CUT_ITEM_INDEX, tableComplex.getSelectionModel().getSelectedIndices().toArray(new Integer[0]));
        content.put(ComplexTable.COMPLEX_CUT_ITEM_PROFILE, tableProfile.getSelectionModel().getSelectedItem().getId());
        content.put(ComplexTable.COMPLEX_CUT_ITEM_ID, tableComplex.getSelectionModel().getSelectedItems().stream()
                .map(i->i.getId())
                .collect(Collectors.toList())
                .toArray(new Long[0])
        );
        clipboard.setContent(content);

    }



    private void pasteTherapyComplexes() {
        if(tableComplex.getSelectionModel().getSelectedItems().size()>1){
            showWarningDialog(res.getString("app.ui.insertion_elements"),res.getString("app.ui.insertion_not_allowed"),res.getString("app.ui.ins_not_av_mess"),getApp().getMainWindow(),Modality.WINDOW_MODAL);
            return;
        }
        Clipboard clipboard=Clipboard.getSystemClipboard();

        if(clipboard.hasContent(ComplexTable.COMPLEX_COPY_ITEM))pasteTherapyComplexesByCopy();
        else pasteTherapyComplexesByCut();
    }

    private void pasteTherapyComplexesByCopy(){
        Profile profile = tableProfile.getSelectionModel().getSelectedItem();
        if(profile==null) return;
        if (tableComplex.getSelectionModel().getSelectedItems().size()>1) return;
        Clipboard clipboard = Clipboard.getSystemClipboard();

        if (!clipboard.hasContent(ComplexTable.COMPLEX_COPY_ITEM)) return;
        Long[] ids = (Long[]) clipboard.getContent(ComplexTable.COMPLEX_COPY_ITEM);
        if(ids==null) return;
        if(ids.length==0) return;

        int dropIndex = tableComplex.getSelectionModel().getSelectedIndex();

        List<TherapyComplex> therapyComplexes =  Arrays.stream(ids)
                .map(i->getModel().findTherapyComplex(i))
                .filter(i->i!=null)
                .collect(Collectors.toList());


        if (tableComplex.getSelectionModel().getSelectedItems().isEmpty()) {
            //вставка просто в конец таблицы
            try {
                tableComplex.getSelectionModel().clearSelection();
                for (TherapyComplex tc : therapyComplexes) {

                    TherapyComplex tpn = getModel().copyTherapyComplexToProfile(profile, tc);
                    if(tpn==null) continue;
                    tableComplex.getItems().add(tpn);
                    tableComplex.getSelectionModel().select(tpn);

                }

                updateProfileTime(profile);
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
                tableComplex.getSelectionModel().clearSelection();
                for (TherapyComplex tc : therapyComplexes) {
                    TherapyComplex tcn = getModel().copyTherapyComplexToProfile(profile, tc);
                    if(tcn==null) continue;
                    tpl.add(tcn);
                }
                List<TherapyComplex> tpSlided = tableComplex.getItems().subList(dropIndex, tableComplex.getItems().size());
                long posFirstSlidingElem = tableComplex.getItems().get(dropIndex).getPosition();

                for (TherapyComplex tp : tpSlided) {
                    tp.setPosition(tp.getPosition()+tpl.size());
                    getModel().updateTherapyComplex(tp);
                }
                int cnt=0;
                for (TherapyComplex tp : tpl) {
                    tp.setPosition(posFirstSlidingElem + cnt++);
                    getModel().updateTherapyComplex(tp);
                }

                tableComplex.getItems().addAll(dropIndex,tpl);
                for (TherapyComplex tp : tpl) {
                    tableComplex.getSelectionModel().select(tp);
                }
                updateProfileTime(profile);
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
            Profile selectedProfile=tableProfile.getSelectionModel().getSelectedItem();
            Long idProfile = (Long) clipboard.getContent(ComplexTable.COMPLEX_CUT_ITEM_PROFILE);
            if(idProfile==null)return;

            else if(idProfile.longValue()==selectedProfile.getId().longValue()){
                //вставка в текущем профиле
                if (tableComplex.getSelectionModel().getSelectedItems().isEmpty()) return;
                if (tableComplex.getSelectionModel().getSelectedItems().size()!=1) return;

                Integer[] indexes = (Integer[]) clipboard.getContent(ComplexTable.COMPLEX_CUT_ITEM_INDEX);
                if(indexes==null) return;
                if(indexes.length==0) return;
                List<Integer> ind = Arrays.stream(indexes).collect(Collectors.toList());
                int dropIndex = tableComplex.getSelectionModel().getSelectedIndex();

                if(!TablesCommon.isEnablePaste(dropIndex,indexes)) {
                    showWarningDialog(res.getString("app.ui.moving_items"),"",res.getString("app.ui.can_not_move_to_pos"),getApp().getMainWindow(),Modality.WINDOW_MODAL);
                    return;
                }

                List<TherapyComplex> therapyComplexes = ind.stream().map(i->tableComplex.getItems().get(i)).collect(Collectors.toList());
                int startIndex=ind.get(0);//первый индекс вырезки
                int lastIndex=ind.get(ind.size()-1);

//элементы всегда будут оказываться выше чем индекс по которому вставляли, те визуально вставляются над выбираемым элементом

                if(dropIndex < startIndex){

                    for (TherapyComplex i : therapyComplexes) {
                        tableComplex.getItems().remove(i);
                    }
                    //вставка программ в dropIndex; Изменение их позиции
                    tableComplex.getItems().addAll(dropIndex,therapyComplexes);
                }else if(dropIndex > lastIndex){

                    TherapyComplex dropComplex = tableComplex.getItems().get(dropIndex);
                    for (TherapyComplex i : therapyComplexes) {
                        tableComplex.getItems().remove(i);
                    }
                    dropIndex= tableComplex.getItems().indexOf(dropComplex);
                    tableComplex.getItems().addAll(dropIndex,therapyComplexes);


                }else return;

                int i=0;
                for (TherapyComplex tp : tableComplex.getItems()) {
                    tp.setPosition((long)(i++));
                    getModel().updateTherapyComplex(tp);
                }
                updateProfileTime(selectedProfile);

                therapyComplexes.clear();

            }else {
                //вставка в другом профиле. Нужно вырезать и просто вставить в указанном месте



                Long[] ids = (Long[]) clipboard.getContent(ComplexTable.COMPLEX_CUT_ITEM_ID);

                if(ids==null) return;
                if(ids.length==0) return;
                List<Long> ind = Arrays.stream(ids).collect(Collectors.toList());
                int dropIndex =-1;
                if (tableComplex.getSelectionModel().getSelectedItem()!=null)dropIndex = tableComplex.getSelectionModel().getSelectedIndex();
                else if(tableComplex.getItems().size()==0) dropIndex=0;

                List<TherapyComplex> movedTP = ind.stream()
                        .map(i->getModel().findTherapyComplex(i))
                        .filter(i->i!=null)
                        .collect(Collectors.toList());

                Profile srcProfile=null;
                if(movedTP.size()>0){
                    Optional<Profile> first = tableProfile.getItems().stream().filter(p -> p.getId().longValue() == idProfile.longValue()).findFirst();
                    srcProfile=first.orElse(null);
                }
                //просто вставляем
                if(dropIndex==-1)tableComplex.getItems().addAll(movedTP);
                else  tableComplex.getItems().addAll(dropIndex,movedTP);
                //теперь все обновляем
                int i=0;
                for (TherapyComplex tp : tableComplex.getItems()) {
                    tp.setPosition((long)(i++));
                    tp.setProfile(selectedProfile);
                    getModel().updateTherapyComplex(tp);
                }

                if(movedTP.size()>0){
                    updateProfileTime(selectedProfile);
                    updateProfileTime( srcProfile);

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
        if (tableComplex.getSelectionModel().getSelectedItems().isEmpty()) return;
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.clear();
        content.put(ComplexTable.COMPLEX_COPY_ITEM, tableComplex.getSelectionModel()
                .getSelectedItems().stream().map(i->i.getId()).collect(Collectors.toList()).toArray(new Long[0]));
        clipboard.setContent(content);

    }
    private void pasteTherapyProgramsByCopy(){
        TherapyComplex therapyComplex = tableComplex.getSelectionModel().getSelectedItem();
        if(therapyComplex==null) return;
        if (tableProgram.getSelectionModel().getSelectedItems().size()>1) return;
        Clipboard clipboard = Clipboard.getSystemClipboard();

        if (!clipboard.hasContent(ProgramTable.PROGRAM_COPY_ITEM)) return;
        Long[] ids = (Long[]) clipboard.getContent(ProgramTable.PROGRAM_COPY_ITEM);
        if(ids==null) return;
        if(ids.length==0) return;

        int dropIndex = tableProgram.getSelectionModel().getSelectedIndex();

        List<TherapyProgram> therapyPrograms =  Arrays.stream(ids)
                .map(i->getModel().getTherapyProgram(i))
                .filter(i->i!=null)
                .collect(Collectors.toList());


        if (tableProgram.getSelectionModel().getSelectedItems().isEmpty()) {
            //вставка просто в конец таблицы
            try {
                tableProgram.getSelectionModel().clearSelection();
                for (TherapyProgram therapyProgram : therapyPrograms) {

                    TherapyProgram tp = getModel().copyTherapyProgramToComplex(therapyComplex, therapyProgram);
                    if(tp==null) continue;
                    tableProgram.getItems().add(tp);
                    tableProgram.getSelectionModel().select(tp);

                }
                updateComplexTime(therapyComplex,true);
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
                tableProgram.getSelectionModel().clearSelection();
                for (TherapyProgram therapyProgram : therapyPrograms) {
                    TherapyProgram tp = getModel().copyTherapyProgramToComplex(therapyComplex, therapyProgram);
                    if(tp==null) continue;
                    tpl.add(tp);
                }
                List<TherapyProgram> tpSlided = tableProgram.getItems().subList(dropIndex, tableProgram.getItems().size());
                long posFirstSlidingElem = tableProgram.getItems().get(dropIndex).getPosition();

                for (TherapyProgram tp : tpSlided) {
                    tp.setPosition(tp.getPosition()+tpl.size());
                    getModel().updateTherapyProgram(tp);
                }
                int cnt=0;
                for (TherapyProgram tp : tpl) {
                    tp.setPosition(posFirstSlidingElem + cnt++);
                    getModel().updateTherapyProgram(tp);
                }

                tableProgram.getItems().addAll(dropIndex,tpl);
                for (TherapyProgram tp : tpl) {
                    tableProgram.getSelectionModel().select(tp);
                }


                updateComplexTime(therapyComplex,true);
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
        TherapyComplex selectedComplex=tableComplex.getSelectionModel().getSelectedItem();
        Long idComplex = (Long) clipboard.getContent(ProgramTable.PROGRAM_CUT_ITEM_COMPLEX);
        if(idComplex==null)return;
        else if(idComplex.longValue()==selectedComplex.getId().longValue()){
            //вставка в текущем комплексе
            if (tableProgram.getSelectionModel().getSelectedItems().isEmpty()) return;
            if (tableProgram.getSelectionModel().getSelectedItems().size()!=1) return;

            Integer[] indexes = (Integer[]) clipboard.getContent(ProgramTable.PROGRAM_CUT_ITEM_INDEX);
            if(indexes==null) return;
            if(indexes.length==0) return;
            List<Integer> ind = Arrays.stream(indexes).collect(Collectors.toList());
            int dropIndex = tableProgram.getSelectionModel().getSelectedIndex();

            if(!TablesCommon.isEnablePaste(dropIndex,indexes)) {
                showWarningDialog(res.getString("app.ui.moving_items"),"",res.getString("app.ui.can_not_move_to_pos"),getApp().getMainWindow(),Modality.WINDOW_MODAL);
                return;
            }

            List<TherapyProgram> therapyPrograms = ind.stream().map(i->tableProgram.getItems().get(i)).collect(Collectors.toList());
            int startIndex=ind.get(0);//первый индекс вырезки
            int lastIndex=ind.get(ind.size()-1);

//элементы всегда будут оказываться выше чем индекс по которому вставляли, те визуально вставляются над выбираемым элементом
            TherapyProgram dropProgram = tableProgram.getItems().get(dropIndex);
                if(dropIndex < startIndex){

                    for (TherapyProgram i : therapyPrograms) {
                        tableProgram.getItems().remove(i);
                    }
                    //вставка программ в dropIndex; Изменение их позиции
                    tableProgram.getItems().addAll(dropIndex,therapyPrograms);
                }else if(dropIndex > lastIndex){


                    for (TherapyProgram i : therapyPrograms) {
                        tableProgram.getItems().remove(i);
                    }
                    dropIndex= tableProgram.getItems().indexOf(dropProgram);
                    tableProgram.getItems().addAll(dropIndex,therapyPrograms);


                }else return;

                int i=0;
                for (TherapyProgram tp : tableProgram.getItems()) {
                    tp.setPosition((long)(i++));
                    getModel().updateTherapyProgram(tp);
                }

            updateComplexTime(dropProgram.getTherapyComplex(),false);
            updateProfileTime(tableProfile.getSelectionModel().getSelectedItem());
            therapyPrograms.clear();

        }else {
            //вставка в другом комплексе. Нужно вырезать и просто вставить в указанном месте



            Long[] ids = (Long[]) clipboard.getContent(ProgramTable.PROGRAM_CUT_ITEM_ID);

            if(ids==null) return;
            if(ids.length==0) return;
            List<Long> ind = Arrays.stream(ids).collect(Collectors.toList());
            int dropIndex =-1;
            if (tableProgram.getSelectionModel().getSelectedItem()!=null)dropIndex = tableProgram.getSelectionModel().getSelectedIndex();
            else if(tableProgram.getItems().size()==0) dropIndex=0;

            List<TherapyProgram> movedTP = ind.stream()
                    .map(i->getModel().getTherapyProgram(i))
                    .filter(i->i!=null)
                    .collect(Collectors.toList());
            TherapyComplex srcComplex=null;
            if(movedTP.size() > 0){
                Optional<TherapyComplex> first = tableComplex.getItems().stream().filter(p -> p.getId().longValue() == idComplex.longValue()).findFirst();
                //будет найден только если вставка в том же профиле
                srcComplex=first.orElse(null);
            }
            //просто вставляем
           if(dropIndex==-1)tableProgram.getItems().addAll(movedTP);
               else  tableProgram.getItems().addAll(dropIndex,movedTP);
            //теперь все обновляем
            int i=0;
            for (TherapyProgram tp : tableProgram.getItems()) {
                tp.setPosition((long)(i++));
                tp.setTherapyComplex(selectedComplex);
                getModel().updateTherapyProgram(tp);
            }

            //обновление времени в таблицах
            if(movedTP.size()>0){
                updateComplexTime(selectedComplex,true);
                //если вставка в другом профиле то обновлять не надо
                if(srcComplex!=null)updateComplexTime(srcComplex,true);
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
        if(tableProgram.getSelectionModel().getSelectedItems().size()>1){
            showWarningDialog(res.getString("app.ui.insertion_elements"),res.getString("app.ui.insertion_not_allowed"),res.getString("app.ui.ins_not_av_mess"),getApp().getMainWindow(),Modality.WINDOW_MODAL);
            return;
        }
        if(therapyProgramsCopied)pasteTherapyProgramsByCopy();
        else pasteTherapyProgramsByCut();
    }

    /**
     * Проверяет максимальное значение пачек частот в комплексах, если превышено ставит максимальное
     * @param complexList
     * @throws Exception
     */
    public static void checkBundlesLength(List<TherapyComplex> complexList) throws Exception {

        List<TherapyComplex> edited = complexList.stream().filter(c -> c.getBundlesLength() > MAX_BUNDLES).collect(Collectors.toList());

        for (TherapyComplex tc : edited) {
                tc.setBundlesLength(MAX_BUNDLES);
                App.getStaticModel().updateTherapyComplex(tc);

        }


    }



    private void editMP3ProgramPath()
    {

        TherapyProgram item = tableProgram.getSelectionModel().getSelectedItem();
        if(item==null) return;
        if(!item.isMp3()) return;

        File tf=new File(item.getFrequencies());


        FileChooser fileChooser =new FileChooser();

        fileChooser.setTitle(res.getString("app.ui.program_by_mp3"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("mp3", "*.mp3"));

            //если такой файл уже есть то установим путь относительно него
         if( tf.exists()) fileChooser.setInitialDirectory(tf.getParentFile());
         else fileChooser.setInitialDirectory( new File(getModel().getMp3Path(System.getProperty("user.home"))));
        File mp3 = fileChooser.showOpenDialog(getApp().getMainWindow());

        if(mp3==null)return;

        item.setName(TextUtil.digitText(mp3.getName().substring(0,mp3.getName().length()-4)));
        item.setFrequencies(mp3.getAbsolutePath());
        int i = tableProgram.getItems().indexOf(item);
        item.setChanged(false);

        getModel().setMp3Path(mp3.getParentFile());
        try
        {

            getModel().updateTherapyProgram(item);
        } catch (Exception e)
        {
            logger.error("Ошибка обновления TherapyProgram",e);
            return;
        }

        tableProgram.getItems().remove(i);
        tableProgram.getItems().add(i,item);
        updateComplexTime(tableComplex.getSelectionModel().getSelectedItem(),true);
        tableProgram.getSelectionModel().select(i);



    }

    /**
     * Копирование текущей программы в пользовательскую базу
     */
    private void copyTherapyProgramToBase()
    {

        ObservableList<TherapyProgram> selectedItems = tableProgram.getSelectionModel().getSelectedItems();
        NamedTreeItem treeItem = (NamedTreeItem) sectionTree.getSelectionModel().getSelectedItem();
        if(selectedItems.isEmpty()|| treeItem==null) return;

        List<TherapyProgram> src=selectedItems.stream().filter(i->!i.isMp3()).collect(Collectors.toList());


        List<Program> tpl=new ArrayList<>();
        try {

            if(treeItem.getValue() instanceof Section) {
                for (TherapyProgram therapyProgram : src) {
                    Program p = getModel().createProgram(therapyProgram.getName(), therapyProgram.getDescription(), therapyProgram.getFrequencies(),(Section)treeItem.getValue(), false, getModel().getUserLanguage());
                    tpl.add(p);
                }


            }
            else if(treeItem.getValue() instanceof Complex){
                for (TherapyProgram therapyProgram : src){

                Program p = getModel().createProgram(therapyProgram.getName(), therapyProgram.getDescription(), therapyProgram.getFrequencies(),(Complex)treeItem.getValue(), false, getModel().getUserLanguage());
                tpl.add(p);
                }
            }
            else throw new Exception();


            if (!treeItem.isLeaf())  {
                getModel().initStringsProgram(tpl);
                treeItem.getChildren().addAll(tpl.stream().map(NamedTreeItem::new).collect(Collectors.toList()));
            }


            boolean isleaf=treeItem.isLeaf();
            if (treeItem.isLeaf())treeItem.setLeafNode(false);
            if (!treeItem.isExpanded()) treeItem.setExpanded(true);
          //s  if (treeItem != null) sectionTree.getSelectionModel().select(treeItem.getChildren().get(treeItem.getChildren().size() - 1));//выделим

        } catch (Exception e) {
            logger.error("",e);
            showExceptionDialog("Ошибка переноса программы в базу","","",e,getApp().getMainWindow(),Modality.WINDOW_MODAL);
        }






    }

    /**
     * Копирует выделенный комплекс с пользов базу, должен быть выделен раздел
     */
    private void copyTherapyComplexToBase()
    {
        ObservableList<TherapyComplex> therapyComplexes = this.tableComplex.getSelectionModel().getSelectedItems();
        NamedTreeItem treeItem = (NamedTreeItem)this.sectionTree.getSelectionModel().getSelectedItem();
        if(!therapyComplexes.isEmpty() && treeItem != null) {
            Complex complex = null;

            try {
                Iterator<TherapyComplex> e = therapyComplexes.iterator();

                while(e.hasNext()) {
                    TherapyComplex itm = (TherapyComplex)e.next();
                    complex = this.getModel().createComplex(itm.getName(), itm.getDescription(), (Section)treeItem.getValue(), false, this.getModel().getUserLanguage());
                    this.getModel().initStringsComplex(complex);
                    Iterator<TherapyProgram> it = this.getModel().findTherapyPrograms(itm.getId()).stream().filter((s) ->!s.isMp3()).collect(Collectors.toList()).iterator();

                    while(it.hasNext()) {
                        TherapyProgram tp = (TherapyProgram)it.next();
                        this.getModel().createProgram(tp.getName(), tp.getDescription(), tp.getFrequencies(), complex, false, this.getModel().getUserLanguage());
                    }

                    if(treeItem.isLeaf()) {
                        treeItem.setLeafNode(false);
                    } else {
                        treeItem.getChildren().add(new NamedTreeItem(complex));
                    }

                    if(!treeItem.isExpanded()) {
                        treeItem.setExpanded(true);
                    }
                }
            } catch (Exception var8) {
                Log.logger.error("", var8);
                showExceptionDialog("Ошибка переноса комплекса  в базу", "", "", var8, getApp().getMainWindow(), Modality.WINDOW_MODAL);
            }

        }



    }

    /**
     * After adding elements to the list, remember to call loadVirtualFlow before calling scrollTo,
     * so the VirtualFlow gets updated and doesn't throw an exception. можно вызывать в начале драга
     * перемотает таблицу на экран выше
     */
    private void scrollProgrammJumpDown()
    {
        int first = virtualFlow.getFirstVisibleCell().getIndex();
        int last = virtualFlow.getLastVisibleCell().getIndex();
        if(last== tableProgram.getItems().size()-1) return;//если виден последний интекс
        int delta=last-first;
        if(last+delta>=tableProgram.getItems().size()-1)tableProgram.scrollTo(tableProgram.getItems().size()-1);
        else tableProgram.scrollTo(last+delta);

    }
    private void scrollProgrammJumpUp()
    {
        int first = virtualFlow.getFirstVisibleCell().getIndex();
        int last = virtualFlow.getLastVisibleCell().getIndex();
        if(first==0) return;//если виден первый
        int delta=last-first;
        if(first-delta<=0)tableProgram.scrollTo(0);
        else tableProgram.scrollTo(first-delta);

    }

    /**
     * Scrolls the table until the given index is visible
     * After adding elements to the list, remember to call loadVirtualFlow before calling scrollTo,
     * so the VirtualFlow gets updated and doesn't throw an exception.
     * @param index to be shown
     */
    private void scrollTo(int index){
        int first = virtualFlow.getFirstVisibleCell().getIndex();
        int last = virtualFlow.getLastVisibleCell().getIndex();
        if (index <= first){
            while (index <= first && virtualFlow.adjustPixels(-1) < 0){
                first = virtualFlow.getFirstVisibleCell().getIndex();
            }
        } else {
            while (index >= last && virtualFlow.adjustPixels(1) > 0){
                last = virtualFlow.getLastVisibleCell().getIndex();
            }
        }
    }
    private void loadVirtualFlowTableProgramm(){
        tableSkin = (TableViewSkin<?>) tableProgram.getSkin();
        virtualFlow = (VirtualFlow<?>) tableSkin.getChildren().get(1);
    }

    private void hideTFSpinnerBTNPan(int val)
    {
        timeToFreqSpinner.getValueFactory().setValue(val/60.0);
        spinnerBtnPan.setVisible(false);



    }
    private void hideTFSpinnerBTNPan()
    {

        spinnerBtnPan.setVisible(false);

    }



    private void hideBundlesSpinnerBTNPan(int val)
    {
        bundlesSpinner.getValueFactory().setValue(String.valueOf(val));
        bundlesBtnPan.setVisible(false);

    }

    private void hideBundlesSpinnerBTNPan()
    {

        bundlesBtnPan.setVisible(false);

    }


    /**
     * перерсчтет времени на профиль. Профиль инстанс из таблицы.
     * @param p
     */
    private void updateProfileTime(Profile p) {
        p.setTime(p.getTime() + 1);
    }

    /**
     * Обновит время комплекса, профиля и программ если указанно reloadPrograms
     * @param c терапевтический комплекс - инстанс из таблицы!!
     * @param reloadPrograms обновить время программ или нет, если нет то просто изменится время комплекса
     */
    private void updateComplexTime(TherapyComplex c, boolean reloadPrograms)
    {
        if(!reloadPrograms)
        {
            c.setTime(c.getTime()+1);
            updateProfileTime(tableProfile.getSelectionModel().getSelectedItem());
            return;
        }

        int i = tableComplex.getItems().indexOf(c);
        if(i==-1){
            System.out.println("null передан в updateComplexTime");
         return;
        }
        tableComplex.getItems().set(i, null);
        tableComplex.getItems().set(i, c);
        tableComplex.getSelectionModel().select(i);
        updateProfileTime(tableProfile.getSelectionModel().getSelectedItem());


    }


    private void updateComplexsTime(List<TherapyComplex> c, boolean reloadPrograms) {
        if(!reloadPrograms) {
            c.forEach(i ->i.setTime(i.getTime() + 1L));
            this.updateProfileTime(this.tableProfile.getSelectionModel().getSelectedItem());
            this.tableComplex.getSelectionModel().clearSelection();
        } else {
            Iterator<TherapyComplex> iterator = c.iterator();
            TherapyComplex therapyComplex;

            while(iterator.hasNext()) {
                therapyComplex = iterator.next();
                int i = this.tableComplex.getItems().indexOf(therapyComplex);
                this.tableComplex.getItems().set(i, null);
                this.tableComplex.getItems().set(i, therapyComplex);

            }
            this.tableComplex.getSelectionModel().clearSelection();

            for (TherapyComplex complex : c) {
                int i = this.tableComplex.getItems().indexOf(complex);
                this.tableComplex.getSelectionModel().select(i);
            }


            this.updateProfileTime(this.tableProfile.getSelectionModel().getSelectedItem());
        }
    }





    /**
     * Заполнит дерево разделов
     * @param containerNode  Section  тот который хранит дерево элементов в базе. те начало отсчета выборки
     *
     */
    private void fillTree(Section containerNode)
    {

        if (containerNode==null || rootItem==null) {  /*System.out.println("FILL -containerNode==null || rootItem==null");*/return;}

        clearTree();
//если выбрали пустой элемент списка(фейковый) то не станем заполнять ничего
        if(containerNode.getId()==0) {
            sectionTree.setShowRoot(false);
            editUserBtn.setDisable(true);
            delUserBtn.setDisable(true);
            return;
        }
        editUserBtn.setDisable(false);
        delUserBtn.setDisable(false);

        //TimeMesure tm=new TimeMesure("Инициализация списка ");

       // tm.start();

        rootItem.setValue(containerNode);

        //загрузим разделы(все вложенные элементы будут грузиться автоматически, благодаря NamedTreeItem, который сам подгрузит доччерние элементы)
        List<Section> allSectionByParent = getModel().findAllSectionByParent(containerNode);
        getApp().getModel().initStringsSection(allSectionByParent);

        String lang=getModel().getProgramLanguage().getAbbr();
        if(!allSectionByParent.isEmpty())
        {
             lang = App.getStaticModel().getSmartLang(allSectionByParent.get(0).getName());
        }



        sectionTree.setShowRoot(true);
        allSectionByParent.stream().sorted(App.getStaticModel().getComparator(lang)).forEach(section -> rootItem.getChildren().add(new NamedTreeItem(section)));
//загрузим прогрмы и комплексы корня.
        List<Complex> allComplexBySection = getModel().findAllComplexBySection(containerNode);
        List<Program> allProgramBySection = getModel().findAllProgramBySection(containerNode);

        if(!allComplexBySection.isEmpty())
        {
            getApp().getModel().initStringsComplex(allComplexBySection);//строки инициализируются тк у нас многоязыковое приложение. Тут инициализируются строки выбранной локали или в первую очередь пользовательские
            lang = App.getStaticModel().getSmartLang(allComplexBySection.get(0).getName());

            allComplexBySection.stream().sorted(App.getStaticModel().getComparator(lang)).forEach(complex -> rootItem.getChildren().add(new NamedTreeItem(complex)));
        }

        if(!allProgramBySection.isEmpty())
        {
            getApp().getModel().initStringsProgram(allProgramBySection);
            lang = App.getStaticModel().getSmartLang(allProgramBySection.get(0).getName());
            allProgramBySection.stream().sorted(App.getStaticModel().getComparator(lang)).forEach(program -> rootItem.getChildren().add(new NamedTreeItem(program)));
        }

        allSectionByParent.clear();allSectionByParent=null;
        allComplexBySection.clear();allComplexBySection=null;
        allProgramBySection.clear();allProgramBySection=null;

      //  tm.stop();



    }



//очистка дерева( корень остается всегда)
    private void clearTree()
    {
        rootItem.setValue(null);
        rootItem.getChildren().forEach(this::removeRecursively);

        rootItem.getChildren().clear();

    }

    enum SearchActionType {IN_SELECTED_DEP,IN_SELECTED_BASE,IN_ALL_BASE};
    class FindFilter
    {

        String searchPattern;//строка которую ищем
        SearchActionType actionType;


        public FindFilter( SearchActionType actionType, String searchPattern) {
            this.actionType = actionType;
            this.searchPattern = searchPattern;
        }

        public SearchActionType getActionType() {
            return actionType;
        }







        public String getSearchPattern() {
            return searchPattern;
        }


    }



    private void fillTreeFind(FindFilter ff)
    {

        if(ff.searchPattern.length()<=2) { showInfoDialog(res.getString("app.search"),res.getString("app.search_1"),"",getApp().getMainWindow(),Modality.WINDOW_MODAL);return;}
        //необходимо сохранить раздел который открыт. Также заблокировать возможность удалять и добавлять и редактировать(стоит установить например старую базу по умолчанию) а может ничего менять не надо!!! Просто учесть режим поиска.
        //любой выбор в списке разделов  или баз отключает режим поиска!
        //нужно учесть состояние поиска чтобы во время него не искать где попало. Те после поиска мы можем продолжать искать исходя из старой ситуации.
        //стоит сделать так чтобы нажатие на ввод искало по текцщему выбору , а меню его меняло бы.

        //сохраним
        // searchState.setRoot(baseCombo.getValue());
        //searchState.setRoot2(sectionCombo.getValue().getId().longValue() == 0 ? null : sectionCombo.getValue());
        searchState.setSearch(true);
        searchState.setSearchText(ff.searchPattern);
        userActionPane.setDisable(true);

        List<Section> sections=null;
        List<Complex>  complexes=null;
        List<Program>  programs=null;
        switch (ff.actionType)
        {
            case IN_ALL_BASE:

                sections = getModel().searchSectionInAllBase(ff.searchPattern, getModel().getProgramLanguage());//поиск пользовательских данных в любом случае произойдет
                 complexes= getModel().searchComplexInAllBase(ff.searchPattern, getModel().getProgramLanguage());//поиск пользовательских данных в любом случае произойдет
                 programs= getModel().searchProgramInAllBase(ff.searchPattern, getModel().getProgramLanguage());//поиск пользовательских данных в любом случае произойдет
                break;
            case IN_SELECTED_BASE:
                sections = findSectionIn(ff.searchPattern, baseCombo.getValue());//поиск пользовательских данных в любом случае произойдет
                complexes = findComplexIn(ff.searchPattern, baseCombo.getValue());//поиск пользовательских данных в любом случае произойдет
                programs = findProgramIn(ff.searchPattern, baseCombo.getValue());//поиск пользовательских данных в любом случае произойдет
                break;
            case IN_SELECTED_DEP:
                sections = findSectionIn(ff.searchPattern,sectionCombo.getValue().getId().longValue() == 0 ? null : sectionCombo.getValue());//поиск пользовательских данных в любом случае произойдет
                complexes = findComplexIn(ff.searchPattern, sectionCombo.getValue().getId().longValue() == 0 ? null : sectionCombo.getValue());//поиск пользовательских данных в любом случае произойдет
                programs = findProgramIn(ff.searchPattern,sectionCombo.getValue().getId().longValue() == 0 ? null : sectionCombo.getValue());//поиск пользовательских данных в любом случае произойдет
                break;

        }
        if(sections.isEmpty() && complexes.isEmpty() && programs.isEmpty()){
            showInfoDialog(res.getString("app.search_res"),res.getString("app.search_res_1"),"",
                    getApp().getMainWindow(),Modality.WINDOW_MODAL);

            //clearSearch(false,false);

            return;
        }

        clearTree();

        //заполним дерево данными
        Section section = new Section();
        section.setNameString("Результаты поиска");
        rootItem.setValue(section);

        getApp().getModel().initStringsSection(sections);

        sectionTree.setShowRoot(true);

        if(!sections.isEmpty())
        {
            //отфильтруем первые 2 уровня базы, чтобы их не искать и заполним дерево разделами
            sections.stream().filter(itm -> itm.getParent() != null && itm.getParent() != null ? itm.getParent().getParent() != null : false).forEach(itm -> rootItem.getChildren().add(new NamedTreeItem(itm)));

        }

        if(!complexes.isEmpty())
        {
            getApp().getModel().initStringsComplex(complexes);//строки инициализируются тк у нас многоязыковое приложение. Тут инициализируются строки выбранной локали или в первую очередь пользовательские
            complexes.forEach(complex -> rootItem.getChildren().add(new NamedTreeItem(complex)));
        }

        if(!programs.isEmpty())
        {
            getApp().getModel().initStringsProgram(programs);
            programs.forEach(program -> rootItem.getChildren().add(new NamedTreeItem(program)));
        }

        sections.clear();sections=null;
        complexes.clear();complexes=null;
        programs.clear();programs=null;

    //возможно стоит отфильтровывать программы которые родительские найденным разделам?? + возможно стоит отображать для программ комплексы и их разделы? но они автоматом загрузят все содержимое!! мо в конструкторе NamedTreeItem отключать это??
    }

    List<Section> findSectionIn(String text,Section sec)
    {
        if(sec==null) return new ArrayList<>();

        List<Section> sections =getModel().searchSectionInParent(text,getModel().getProgramLanguage(),sec);
        getModel().findAllSectionByParent(sec).forEach(section -> sections.addAll(findSectionIn(text, section)));
        return sections;
    }

    List<Complex> findComplexIn(String text,Section sec)
    {
        if(sec==null) return new ArrayList<>();

        List<Complex> complexes = getModel().searchComplexInParent(text, getModel().getProgramLanguage(), sec);

        getModel().findAllSectionByParent(sec).forEach(section -> complexes.addAll(findComplexIn(text, section)));
        return complexes;
    }
    List<Program> findProgramIn(String text,Section sec)
    {
        if(sec==null) return new ArrayList<>();

        List<Program> programms = getModel().searchProgramInParent(text, getModel().getProgramLanguage(), sec);
        getModel().findAllSectionByParent(sec).forEach(section -> programms.addAll(findProgramIn(text, section)));
        getModel().findAllComplexBySection(sec).forEach(complex ->  programms.addAll(getModel().searchProgramInComplex(text, getModel().getProgramLanguage(), complex)));
        return programms;
    }


    /**
     * Указынный элемент не удаляется, его если надо нужно удалить вручную из родительского контейнера, также не обнуляется его значение Value
     * рекурсивная очистка ссылок дерева, удаляются все дочерние элементы, начиная от указанного элемента(очищаются дочерние и их Value, также указанный элемент)
     * @param item
     */
    private void removeRecursively(TreeItem<INamed>  item) {

        if (!item.getChildren().isEmpty())
        {
            item.getChildren().forEach(itm -> {
                itm.setGraphic(null);//очистим ссылку на изображение
                itm.setValue(null);//очистим ссылку на Entity иначе утечка памяти
                removeRecursively(itm);
            });
            item.getChildren().clear();

        }

    }




    /**
     * Указынный элемент не удаляется, его если надо нужно удалить вручную из родительского контейнера, также не обнуляется его значение Value
     * рекурсивная очистка ссылок дерева, удаляются все дочерние элементы, начиная от указанного элемента(очищаются дочерние и их Value, также указанный элемент)
     * @param item
     */
    private void removeRecursively(TreeItem<INamed>  item, Predicate< TreeItem<INamed> > filter) {




        if (!item.getChildren().isEmpty())
        {
            item.getChildren().stream().filter(filter).forEach(itm -> {
                itm.setGraphic(null);//очистим ссылку на изображение
                itm.setValue(null);//очистим ссылку на Entity иначе утечка памяти
                removeRecursively(itm);
            });

            Iterator<TreeItem<INamed>> itr = item.getChildren().iterator();
            while(itr.hasNext()) if(filter.test(itr.next())) itr.remove();
            itr=null;



        }

    }
    /**
     * полное удаление элементов, включая тот что мы указали
     * @param item
     */
    private void clearTree(TreeItem<INamed>  item)
    {
        if(item==null) return;

        removeRecursively(item);
        item.setGraphic(null);//очистим ссылку на изображение
        item.setValue(null);//очистим ссылку на Entity иначе утечка памяти

        if(item.getParent()!=null)item.getParent().getChildren().remove(item);

    }

    /**
     * полное удаление элементов, включая тот что мы указали, предикат отфильтрует нужные дочерние
     * @param item
     * @param filter предикат, который отфильтрует элементы которые мы хотим удалить и очистить
     */
    private void clearTree(TreeItem<INamed>  item, Predicate<NamedTreeItem> filter)
    {
        if(item==null) return;

        removeRecursively(item);
        item.setGraphic(null);//очистим ссылку на изображение
        item.setValue(null);//очистим ссылку на Entity иначе утечка памяти

        if(item.getParent()!=null)item.getParent().getChildren().remove(item);

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


        btnGenerate.setDisable(true);

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


        task.setOnRunning(event1 -> setProgressIndicator(-1.0, res.getString("app.serv_4")));
        task.setOnSucceeded(event ->
        {
            setProgressIndicator(1.0, res.getString("app.serv_5"));

            hideProgressIndicator(true);
            btnGenerate.setDisable(false);
        });

        task.setOnFailed(event -> {
            setProgressIndicator(res.getString("app.serv_6"));
            hideProgressIndicator(true);
            btnGenerate.setDisable(false);
        });

        Thread threadTask=new Thread(task);

        threadTask.setDaemon(true);

        threadTask.start();


    }



    private void createSection(Section selectedSection,TreeItem<INamed> selectedItem)throws Exception
    {
        Section parent = null;
        if(selectedSection==null && selectedItem==null)parent = getModel().findAllSectionByTag("USER");
        else  if(selectedSection!=null && selectedItem==null)parent=selectedSection;
        else if(selectedSection!=null && selectedItem!=null)
        {
            if(selectedItem.getValue()==null)throw new Exception("selectedItem.getValue() == null");
            if(!(selectedItem.getValue() instanceof Section)) return;
            parent=(Section)selectedItem.getValue();
        }else return;


    //выведем диалог ввода данных
        NameDescroptionDialogController.Data data =null;
        try {
            data = BaseController.openDialogUserData(getApp().getMainWindow(), "/fxml/SectionDialog.fxml", res.getString("app.title1"), false,
                    StageStyle.DECORATED, 0, 0, 0, 0, new NameDescroptionDialogController.Data("",""));


        } catch (IOException e) {
            logger.error("",e);
            data =null;
        }

        if(data ==null){BaseController.showErrorDialog("Ошибка создания раздела", "", "",  getApp().getMainWindow(),Modality.WINDOW_MODAL);return;}




        //проверим полученные данные из диалога, создали ли имя
        if( data.isNameChanged())
        {


            Section createdSection=null;
            try {
                createdSection= getModel().createSection(parent, data.getNewName(), data.getNewDescription(),false,getModel().getUserLanguage());

                createdSection.setNameString(data.getNewName());
                createdSection.setDescriptionString(data.getNewDescription());

               if(parent.getTag()!=null ? parent.getTag().equals("USER"): false)
               {
                   //добавим в комбобокс
                   sectionCombo.getItems().add(createdSection);
                   sectionCombo.getSelectionModel().selectLast();
               }
                else if( selectedItem==null)
               {
                   rootItem.getChildren().add(new NamedTreeItem(createdSection));//вставим в корен дерева, если не выбран элемент в дереве
                   sectionTree.getSelectionModel().select(rootItem.getChildren().get(rootItem.getChildren().size() - 1));//выделим добавленный пункт
                }
                  else {
                   if(selectedItem.isLeaf())((NamedTreeItem)selectedItem).setLeafNode(false);//установим что он теперь не лист., автоматом подгрузятся дочернии, поэтому не надо вставлять их тут
                  else  selectedItem.getChildren().add(new NamedTreeItem(createdSection));//добавим в дерево, если унас уже есть дочернии в ветке
                   if(!selectedItem.isExpanded()) selectedItem.setExpanded(true);
                   if(selectedItem!=null)sectionTree.getSelectionModel().select(selectedItem.getChildren().get(selectedItem.getChildren().size()-1));//выделим добавленный пункт
               }



            } catch (Exception e) {
                data=null;
                logger.error("",e);
                BaseController.showExceptionDialog("Ошибка создания раздела", "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);
            }

        }

        data =null;//очистка ссылки


    }


    private void createComplex(  TreeItem<INamed> selectedItem) throws Exception
    {
        if(selectedItem==null) return;
        if(selectedItem.getValue()==null)throw new Exception("selectedItem.getValue() == null");

        if(selectedItem.getValue() instanceof Section)
        {

            //выведем диалог ввода данных
            NameDescroptionDialogController.Data data =null;
            try {
                data = BaseController.openDialogUserData(getApp().getMainWindow(), "/fxml/SectionDialog.fxml", res.getString("app.title2"), false,
                        StageStyle.DECORATED, 0, 0, 0, 0, new NameDescroptionDialogController.Data("",""));


            } catch (IOException e) {
                logger.error("",e);
                data =null;
            }

            if(data ==null){BaseController.showErrorDialog("Ошибка создания комплекса", "", "",  getApp().getMainWindow(),Modality.WINDOW_MODAL);return;}

            //создаем комплекс
            if( data.isNameChanged() )
            {
                Complex complex=null;

                try {
                    complex= getModel().createComplex(data.getNewName(),data.getNewDescription(),(Section)selectedItem.getValue(),false,getModel().getUserLanguage());
                    complex.setNameString(data.getNewName());
                    complex.setDescriptionString(data.getNewDescription());

                    if(selectedItem.isLeaf())((NamedTreeItem)selectedItem).setLeafNode(false);//установим что он теперь не лист., автоматом подгрузятся дочернии, поэтому не надо вставлять их тут
                    else  selectedItem.getChildren().add(new NamedTreeItem(complex));//добавим в дерево, если унас уже есть дочернии в ветке
                    if(!selectedItem.isExpanded()) selectedItem.setExpanded(true);
                     sectionTree.getSelectionModel().select(selectedItem.getChildren().get(selectedItem.getChildren().size()-1));//выделим добавленный пункт


                } catch (Exception e) {
                    data=null;
                    logger.error("",e);
                    BaseController.showExceptionDialog("Ошибка создания комплекса", "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);return;
                }


            }




data=null;
        }

    }

    private void createProgram(  TreeItem<INamed> selectedItem) throws Exception {

        if(selectedItem==null) return;
        if(selectedItem.getValue()==null)throw new Exception("selectedItem.getValue() == null");

        if(selectedItem.getValue() instanceof Section || selectedItem.getValue() instanceof Complex )
        {

            //выведем диалог ввода данных
            ProgramDialogController.Data data =null;
            try {
                data = BaseController.openDialogUserData(getApp().getMainWindow(), "/fxml/ProgramDialog.fxml", res.getString("app.title3"), false,
                        StageStyle.DECORATED, 0, 0, 0, 0, new ProgramDialogController.Data("","",""));


            } catch (IOException e) {
                logger.error("",e);
                data =null;
            }

            if(data ==null){BaseController.showErrorDialog("Ошибка создания програмы", "", "",  getApp().getMainWindow(),Modality.WINDOW_MODAL);return;}

            //создаем комплекс
            if(data.isNameChanged())
            {
                Program program=null;

                try {
                    if(selectedItem.getValue() instanceof Section) program= getModel().createProgram(data.getNewName(),data.getNewDescription(),data.getNewFreq(),(Section)selectedItem.getValue(),false,getModel().getUserLanguage());
                            else program= getModel().createProgram(data.getNewName(),data.getNewDescription()
                            ,data.getNewFreq(),(Complex)selectedItem.getValue(),false,getModel().getUserLanguage());


                    program.setNameString(data.getNewName());
                    program.setDescriptionString(data.getNewDescription());

                    if(selectedItem.isLeaf())((NamedTreeItem)selectedItem).setLeafNode(false);//установим что он теперь не лист., автоматом подгрузятся дочернии, поэтому не надо вставлять их тут
                    else  selectedItem.getChildren().add(new NamedTreeItem(program));//добавим в дерево, если унас уже есть дочернии в ветке
                    if(!selectedItem.isExpanded()) selectedItem.setExpanded(true);



                } catch (Exception e) {
                    data=null;
                    logger.error("",e);
                    BaseController.showExceptionDialog("Ошибка создания комплекса", "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);return;
                }


            }




            data=null;
        }

    }

    public void onCreateUserBtn()
    {

        TreeItem<INamed> selectedItem=null;
        Section selectedSection =null;


        //если у нас выбран пустой пункт, то мы должны иметь selectedItem ==null и selectedSection ==null иначе они должны иметь значения, но  selectedItem может быть null
        selectedSection = sectionCombo.getSelectionModel().getSelectedItem();
        if(selectedSection.getId()!=0) selectedItem = sectionTree.getSelectionModel().getSelectedItem();
        else selectedSection =null;




        if(!baseCombo.getSelectionModel().getSelectedItem().getTag().equals("USER")) return;//чтобы случайно активированная кнопка не позволила создать раздел, проверим какая бааза выбрана(корневой раздел Section)


        try {


      //создание корневого раздела если у нас юзерская база и не выбран ее раздел
        if(selectedSection==null)createSection(null,null);
        else //если выбран раздел, но не выбран подраздел в дереве разделов, создадим раздел уже в дереве
        if(selectedSection!=null && selectedItem==null) createSection(selectedSection,null);



        else if(selectedSection!=null && selectedItem!=null)
        {
            //создание подраздела в подразделе дерева или комплекса
            if(selectedItem.getValue() instanceof Section)
            {

                //дадим пользователю выбор
                String choice = BaseController.showChoiceDialog(res.getString("ui.msg.create_section_or_complex"), "", res.getString("ui.msg.section_or_ui_content"), Arrays.asList(res.getString("ui.section"), res.getString("ui.complex"),res.getString("ui.program")),res.getString("ui.section"), getApp().getMainWindow(), Modality.WINDOW_MODAL);
                if(choice==null) return;

                if(choice.equals(res.getString("ui.section"))) createSection(selectedSection, selectedItem);//создание раздела
                else if(choice.equals(res.getString("ui.complex"))) createComplex(selectedItem);//создание компелекса
                else  createProgram(selectedItem);//создание программы в разделе
            }
            else   if(selectedItem.getValue() instanceof Complex) createProgram(selectedItem);


        }
        }catch (Exception e)
        {
            logger.error("",e);
            BaseController.showExceptionDialog("Ошибка создания элемента", "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);return;
        }

    }



    public void onEditUserBtn()
    {
        NamedTreeItem selectedTreeItem = (NamedTreeItem)sectionTree.getSelectionModel().getSelectedItem();
        Section selectedComboItem = sectionCombo.getSelectionModel().getSelectedItem();


        if( selectedComboItem.getId()==0) return;//выбран пустой элемент в комбобоксе
         else
         //выбран элемент дерева
        if(selectedTreeItem!=null)
        {
            if(selectedTreeItem.getValue() instanceof Section)
            {
                //выведем диалог ввода данных
                NameDescroptionDialogController.Data data = new NameDescroptionDialogController.Data(selectedTreeItem.getValue().getNameString(),((Section)selectedTreeItem.getValue()).getDescriptionString());


                try {
                    data = BaseController.openDialogUserData(getApp().getMainWindow(), "/fxml/SectionDialog.fxml", res.getString("app.title4"), false,
                            StageStyle.DECORATED, 0, 0, 0, 0, data);


                } catch (IOException e) {
                    logger.error("",e);
                    data =null;
                }

                if(data ==null){ BaseController.showErrorDialog("Ошибка редакетирования раздела", " data==null", "",  getApp().getMainWindow(),Modality.WINDOW_MODAL);return;}


                if(data.isChanged())
                {

                    try {

                        selectedTreeItem.getValue().setNameString(data.getNewName());
                        ((Section)selectedTreeItem.getValue()).setDescriptionString(data.getNewDescription());

                        LocalizedString localStringName = getModel().getLocalString( ((Section) selectedTreeItem.getValue()).getName(), getModel().getUserLanguage());
                        localStringName.setContent(data.getNewName());
                        getModel().updateLocalString(localStringName);

                        LocalizedString localStringDesc = getModel().getLocalString(((Section) selectedTreeItem.getValue()).getDescription(), getModel().getUserLanguage());
                        localStringDesc.setContent(data.getNewDescription());
                        getModel().updateLocalString(localStringDesc);

                        if(selectedTreeItem.getParent()==null)//мы выбрали корень дерева. Изменим элемент и отображение в комбо и в дереве иначе только в дереве
                        {
                            int i = sectionCombo.getSelectionModel().getSelectedIndex();
                            ObservableList<Section> items = sectionCombo.getItems();
                            sectionCombo.setItems(null);
                            sectionCombo.setItems(items);
                            sectionCombo.getSelectionModel().select(i);
                            items=null;

                            INamed value = selectedTreeItem.getValue();
                            selectedTreeItem.setValue(null);
                            selectedTreeItem.setValue(value);
                            value=null;

                        }else
                        {
                            INamed value = selectedTreeItem.getValue();
                            selectedTreeItem.setValue(null);
                            selectedTreeItem.setValue(value);
                            value=null;

                        }

                        programDescription.setText(data.getNewDescription());
                        programInfo.setText("");
                    data=null;
                    } catch (Exception e) {
                        data=null;
                        logger.error("",e);
                        BaseController.showExceptionDialog("Ошибка редакетирования раздела", "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);
                        return;
                    }



                }


            }else
            if(selectedTreeItem.getValue() instanceof Complex)
            {
                //выведем диалог ввода данных
                NameDescroptionDialogController.Data data = new NameDescroptionDialogController.Data(selectedTreeItem.getValue().getNameString(),((Complex)selectedTreeItem.getValue()).getDescriptionString());


                try {
                    data = BaseController.openDialogUserData(getApp().getMainWindow(), "/fxml/SectionDialog.fxml", res.getString("app.title5"), false,
                            StageStyle.DECORATED, 0, 0, 0, 0, data);


                } catch (IOException e) {
                    logger.error("",e);
                    data =null;
                }

                if(data ==null){ BaseController.showErrorDialog("Ошибка редакетирования комплекса", " data==null", "",  getApp().getMainWindow(),Modality.WINDOW_MODAL);return;}



                if(data.isChanged())
                {

                    try {

                        selectedTreeItem.getValue().setNameString(data.getNewName());
                        ((Complex)selectedTreeItem.getValue()).setDescriptionString(data.getNewDescription());

                        LocalizedString localStringName = getModel().getLocalString(((Complex) selectedTreeItem.getValue()).getName(), getModel().getUserLanguage());
                        localStringName.setContent(data.getNewName());
                        getModel().updateLocalString(localStringName);

                        LocalizedString localStringDesc = getModel().getLocalString(((Complex) selectedTreeItem.getValue()).getDescription(), getModel().getUserLanguage());
                        localStringDesc.setContent(data.getNewDescription());
                        getModel().updateLocalString(localStringDesc);


                        INamed value = selectedTreeItem.getValue();
                        selectedTreeItem.setValue(null);
                            selectedTreeItem.setValue(value);
                        value=null;


                        programDescription.setText(data.getNewDescription());
                        programInfo.setText("");

                            data=null;


                    } catch (Exception e) {
                        data=null;
                        logger.error("",e);
                        BaseController.showExceptionDialog("Ошибка редакетирования комплекса", "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);
                        return;
                    }



                }


            }else
            if(selectedTreeItem.getValue() instanceof Program)
            {

                ProgramDialogController.Data data = new ProgramDialogController.Data(selectedTreeItem.getValue().getNameString(),((Program)selectedTreeItem.getValue()).getDescriptionString(),((Program)selectedTreeItem.getValue()).getFrequencies());


                try {
                    data = BaseController.openDialogUserData(getApp().getMainWindow(), "/fxml/ProgramDialog.fxml", res.getString("app.title6"), false,
                            StageStyle.DECORATED, 0, 0, 0, 0, data);


                } catch (IOException e) {
                    logger.error("",e);
                    data =null;
                }

                if(data ==null){ BaseController.showErrorDialog("Ошибка редакетирования програмы", " data==null", "",  getApp().getMainWindow(),Modality.WINDOW_MODAL);return;}



                if(data.isChanged())
                {

                    try {

                        selectedTreeItem.getValue().setNameString(data.getNewName());
                        ((Program)selectedTreeItem.getValue()).setDescriptionString(data.getNewDescription());
                        ((Program)selectedTreeItem.getValue()).setFrequencies(data.getNewFreq());

                        LocalizedString localStringName = getModel().getLocalString( ((Program) selectedTreeItem.getValue()).getName(), getModel().getUserLanguage());
                        localStringName.setContent(data.getNewName());
                        getModel().updateLocalString(localStringName);

                        LocalizedString localStringDesc = getModel().getLocalString(((Program) selectedTreeItem.getValue()).getDescription(), getModel().getUserLanguage());
                        localStringDesc.setContent(data.getNewDescription());
                        getModel().updateLocalString(localStringDesc);

                        if(data.isFreqChanged())getModel().updateProgram((Program)selectedTreeItem.getValue());//если изменились частоты то сохраним прогрму иначе только строки


                        INamed value = selectedTreeItem.getValue();
                        selectedTreeItem.setValue(null);
                        selectedTreeItem.setValue(value);
                        value=null;


                        programDescription.setText(data.getNewDescription());
                        programInfo.setText(data.getNewFreq().replace(";",";  "));
                        data=null;

                    } catch (Exception e) {
                        data=null;
                        logger.error("",e);
                        BaseController.showExceptionDialog("Ошибка редакетирования програмы", "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);
                        return;
                    }



                }






            }
        }else
        {
            //выбран раздел 2 уровня в комбобокс, но не в дереве

            //выведем диалог ввода данных
            NameDescroptionDialogController.Data data = new NameDescroptionDialogController.Data(selectedComboItem.getNameString(),selectedComboItem.getDescriptionString());


            try {
                data = BaseController.openDialogUserData(getApp().getMainWindow(), "/fxml/SectionDialog.fxml", res.getString("app.title7"), false,
                        StageStyle.DECORATED, 0, 0, 0, 0, data);


            } catch (IOException e) {
                logger.error("",e);
                data =null;
            }

            if(data ==null){BaseController.showErrorDialog("Ошибка редакетирования раздела", "", "",  getApp().getMainWindow(),Modality.WINDOW_MODAL);return;}


            if(data.isChanged() && data.isNameChanged())
            {
                try {

                    selectedComboItem.setNameString(data.getNewName());
                    selectedComboItem.setDescriptionString(data.getNewDescription());

                    LocalizedString localStringName = getModel().getLocalString(selectedComboItem.getName(), getModel().getUserLanguage());
                    localStringName.setContent(data.getNewName());
                    getModel().updateLocalString(localStringName);

                    LocalizedString localStringDesc = getModel().getLocalString(selectedComboItem.getDescription(), getModel().getUserLanguage());
                    localStringDesc.setContent(data.getNewDescription());
                    getModel().updateLocalString(localStringDesc);


                    int i = sectionCombo.getSelectionModel().getSelectedIndex();
                    ObservableList<Section> items = sectionCombo.getItems();
                    sectionCombo.setItems(null);
                    sectionCombo.setItems(items);
                    sectionCombo.getSelectionModel().select(i);
                    items=null;
                    INamed value = rootItem.getValue();
                    rootItem.setValue(null);
                    rootItem.setValue(value);
                    value=null;




data=null;
                } catch (Exception e) {
                    data=null;
                    logger.error("",e);
                    BaseController.showExceptionDialog("Ошибка редакетирования раздела", "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);
                    return;
                }


            }

        }


    }

    public void onDeleteItm()
    {

        TreeItem<INamed> itemSelected=sectionTree.getSelectionModel().getSelectedItem();
        Section comboSelected=sectionCombo.getSelectionModel().getSelectedItem();

        if (itemSelected == null )
        {
            if(comboSelected.getId()==0)return;
            else
            {
                //это для выбранного меню разделов в комбо для секций. Если только оно выбрано

                if(getModel().countSectionChildren(comboSelected)!=0)
                {
                  showInfoDialogNoHeader(res.getString("app.title8"),res.getString("app.title9"),getApp().getMainWindow(),Modality.WINDOW_MODAL);
                    return;
                }

                try {
                    getModel().removeSection(comboSelected);
                    sectionCombo.getItems().remove(comboSelected);
                    sectionCombo.getSelectionModel().select(0);

                } catch (Exception e) {
                    logger.error("",e);
                    showExceptionDialog("Ошибка удаления раздела", "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);
                }


                return;
            }
        }







        if(itemSelected==null ? true :itemSelected.getValue()==null) return ;

        if(itemSelected.getValue() instanceof Program)
        {

            Optional<ButtonType> buttonType = showConfirmationDialog(res.getString("app.title10"), "", res.getString("app.title11"), getApp().getMainWindow(), Modality.WINDOW_MODAL);

            if(buttonType.isPresent() ? buttonType.get()==okButtonType: false)
            {
                try {
                    getModel().removeProgram((Program) itemSelected.getValue());
                    clearTree(itemSelected);

                } catch (Exception e) {
                    logger.error("",e);
                    showExceptionDialog("Ошибка удаления програмы","","",e,getApp().getMainWindow(),Modality.WINDOW_MODAL);
                }

            }


        }else if(itemSelected.getValue() instanceof Complex)
        {

            Optional<ButtonType> buttonType = showConfirmationDialog(res.getString("app.title12"), "", res.getString("app.title13"), getApp().getMainWindow(), Modality.WINDOW_MODAL);

            if(buttonType.isPresent() ? buttonType.get()==okButtonType: false)
            {
                try {
                    getModel().removeComplex((Complex) itemSelected.getValue());

                    clearTree(itemSelected);


                } catch (Exception e) {
                    logger.error("",e);
                    showExceptionDialog("Ошибка удаления комплекса","","",e,getApp().getMainWindow(),Modality.WINDOW_MODAL);
                }

            }


        }else  if(itemSelected.getValue() instanceof Section)
        {

            //если есть непустые разделы то не станем удалять
            long count = itemSelected.getChildren().stream().filter(itm -> (itm.getValue() instanceof Section && !itm.getChildren().isEmpty())).count();
            if(count!=0) return;
            // if(!itemSelected.getChildren().isEmpty()) return;

            //здесь у нас пустой раздел. Мы можем его просто удалить

            Optional<ButtonType> buttonType = showConfirmationDialog(res.getString("app.title14"), "", res.getString("app.title15"), getApp().getMainWindow(), Modality.WINDOW_MODAL);

            if(buttonType.isPresent() ? buttonType.get()==okButtonType: false)
            {





                try {

                    //удалим пустые разделы и комплексы с программами
                    for (TreeItem<INamed> item : itemSelected.getChildren()) {

                        if(item.getValue() instanceof Program) getModel().removeProgram((Program)item.getValue());
                        else if(item.getValue() instanceof Complex)  getModel().removeComplex((Complex)item.getValue());
                        else if(item.getValue() instanceof Section && item.getChildren().isEmpty()) getModel().removeSection((Section)item.getValue());
                    }


                    getModel().removeSection((Section) itemSelected.getValue());
                    if(itemSelected.getParent()==null)
                    {
                        //это корень. Удалим из комбо, активируем пустой элемент. дерево замо очистится
                        sectionCombo.getItems().remove((Section) itemSelected.getValue());
                        sectionCombo.getSelectionModel().select(0);

                    }else  clearTree(itemSelected);//если удалили элемент дерева, то очистим нижлежайшие ветви




                } catch (Exception e) {
                    logger.error("",e);
                    showExceptionDialog("Ошибка удаления раздела","","",e,getApp().getMainWindow(),Modality.WINDOW_MODAL);
                }

            }


        }

    }

    public void onClearItm()
    {
        TreeItem<INamed> itemSelected=sectionTree.getSelectionModel().getSelectedItem();



        if(itemSelected==null ? true :itemSelected.getValue()==null) return ;

        if(itemSelected.getValue() instanceof Section)
        {
            Optional<ButtonType> buttonType = showConfirmationDialog(res.getString("app.title16"), "", res.getString("app.title17"), getApp().getMainWindow(), Modality.WINDOW_MODAL);

            if(buttonType.isPresent() ? buttonType.get()==okButtonType: false)
            {
                try {
                    getModel().clearSection((Section) itemSelected.getValue());

                    removeRecursively(itemSelected, item -> !(item.getValue() instanceof Section));//очистим все кроме разделов



                } catch (Exception e) {
                    logger.error("",e);
                    showExceptionDialog("Ошибка удаления раздела","","",e,getApp().getMainWindow(),Modality.WINDOW_MODAL);
                }

            }

        }else  if(itemSelected.getValue() instanceof Complex)
        {
            Optional<ButtonType> buttonType = showConfirmationDialog(res.getString("app.title18"), "", res.getString("app.title19"), getApp().getMainWindow(), Modality.WINDOW_MODAL);

            if(buttonType.isPresent() ? buttonType.get()==okButtonType: false)
            {
                try {

                    for (TreeItem<INamed> item : itemSelected.getChildren())
                    {
                        getModel().removeProgram((Program) item.getValue());

                    }
                    removeRecursively(itemSelected);



                } catch (Exception e) {
                    logger.error("",e);
                    showExceptionDialog("Ошибка очистки комплекса", "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);
                }

            }

        }


    }


/************  Обработчики меню Файл **********/


    public void onExportUserBase()
    {
        Section start=null;

        List<Section> collect = baseCombo.getItems().stream().filter(section -> "USER".equals(section.getTag())).collect(Collectors.toList());
        Section userSection=null;
        if(collect.isEmpty()) return;
        userSection=collect.get(0);
        collect=null;



        //если у не выбран раздел пользовательский
        if(!"USER".equals(baseCombo.getSelectionModel().getSelectedItem().getTag()) )
        {
            Optional<ButtonType> buttonType = showConfirmationDialog(res.getString("app.title20"), "", res.getString("app.title21"), getApp().getMainWindow(), Modality.WINDOW_MODAL);

            if(buttonType.isPresent() ? buttonType.get()==okButtonType :false) start= userSection; else return;

        }else
        {
            //выбрана пользовательская база.

            //не выбран раздел в комбобоксе
            if(sectionCombo.getSelectionModel().getSelectedItem().getId()==0)
            {
                Optional<ButtonType> buttonType = showConfirmationDialog(res.getString("app.title22"), "", res.getString("app.title23"), getApp().getMainWindow(), Modality.WINDOW_MODAL);

                if(buttonType.isPresent() ? buttonType.get()==okButtonType :false) start= userSection; else return;

            }else if(sectionTree.getSelectionModel().getSelectedItem()==null)
            {
                //выбран раздел в комбобоксе но не выбран в дереве



                start=sectionCombo.getSelectionModel().getSelectedItem();
            }else
            {
                //выбран элемент дерева и выбран раздел

                //если выбран не раздел
                if(!(sectionTree.getSelectionModel().getSelectedItem().getValue() instanceof Section))
                {

                    showWarningDialog(res.getString("app.title24"),"",res.getString("app.title25"),getApp().getMainWindow(),Modality.WINDOW_MODAL );
                    return;


                }

                start=(Section)sectionTree.getSelectionModel().getSelectedItem().getValue();//выберем стартовым раздел
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


        setProgressIndicator(res.getString("app.title28"));
        Task<Boolean> task =new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception
            {

               boolean res= ExportUserBase.export(sec, fileToSave, getModel());
                if(res==false) {this.failed();return false;}
                else return true;

            }
        };


        task.setOnRunning(event1 -> setProgressIndicator(-1.0, res.getString("app.title29")));
        task.setOnSucceeded(event ->
        {
            if (task.getValue()) setProgressIndicator(1.0, res.getString("app.title30"));
            else setProgressIndicator( res.getString("app.title31"));
            hideProgressIndicator(true);
        });

        task.setOnFailed(event -> {
            setProgressIndicator(res.getString("app.title31"));
            hideProgressIndicator(true);
        });

        Thread threadTask=new Thread(task);

        threadTask.setDaemon(true);

        threadTask.start();







    }

    public void onExportProfile()
    {

        if(tableProfile.getSelectionModel().getSelectedItem()==null) return;

        Profile selectedItem = tableProfile.getSelectionModel().getSelectedItem();

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



        setProgressIndicator(res.getString("app.title33"));
        Task<Boolean> task =new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception
            {

                boolean res= ExportProfile.export(prof,fileToSave,getModel());
                if(res==false) {this.failed();return false;}
                else return true;

            }
        };


        task.setOnRunning(event1 -> setProgressIndicator(-1.0, res.getString("app.title34")));
        task.setOnSucceeded(event ->
        {
            if (task.getValue()) setProgressIndicator(1.0, res.getString("app.title35"));
            else setProgressIndicator(res.getString("app.title36"));
            hideProgressIndicator(true);
        });

        task.setOnFailed(event -> {
            setProgressIndicator(res.getString("app.title36"));
            hideProgressIndicator(true);
        });

        Thread threadTask=new Thread(task);

        threadTask.setDaemon(true);

        threadTask.start();

        selectedItem=null;
    }



    public void onExportTherapyComplex()
    {
        if(tableComplex.getSelectionModel().getSelectedItems().isEmpty()) return;

       final ObservableList<TherapyComplex> selectedItems = tableComplex.getSelectionModel().getSelectedItems();
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

        if(selectedItems.size()>1)initname=tableProfile.getSelectionModel().getSelectedItem().getName();
        else initname=selectedItems.get(0).getName();

        FileChooser fileChooser =new FileChooser();
        fileChooser.setTitle(res.getString("app.title37"));
        fileChooser.setInitialFileName(TextUtil.replaceWinPathBadSymbols(initname)+".xmlc");
        fileChooser.setInitialDirectory(new File(getModel().getLastExportPath(System.getProperty("user.home"))));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("xmlc", "*.xmlc"));
        file= fileChooser.showSaveDialog(getApp().getMainWindow());

        if(file==null)return;

        final File fileToSave=file;


        setProgressIndicator(res.getString("app.title38"));
        Task<Boolean> task =new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception
            {

                boolean res= ExportTherapyComplex.export(selectedItems, fileToSave, getModel());
                if(res==false) {this.failed();return false;}
                else return true;

            }
        };


        task.setOnRunning(event1 -> setProgressIndicator(-1.0, res.getString("app.title34")));
        task.setOnSucceeded(event ->
        {
            if (task.getValue()) setProgressIndicator(1.0, res.getString("app.title35"));
            else setProgressIndicator(res.getString("app.title39"));
            hideProgressIndicator(true);
        });

        task.setOnFailed(event -> {
            setProgressIndicator(res.getString("app.title39"));
            hideProgressIndicator(true);
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



        task.progressProperty().addListener((observable, oldValue, newValue) -> setProgressIndicator(newValue.doubleValue()));
        task.setOnRunning(event1 -> setProgressIndicator(0.0, res.getString("app.title43")));

        task.setOnSucceeded(event ->
        {

            if (task.getValue())
            {
                setProgressIndicator(1.0, res.getString("app.title44"));

               tableProfile.getItems().add(getModel().getLastProfile());
                btnGenerate.setDisable(false);

            }
            else setProgressIndicator(res.getString("app.title45"));
            hideProgressIndicator(true);
        });

        task.setOnFailed(event -> {
            setProgressIndicator(res.getString("app.title45"));
            hideProgressIndicator(true);



        });



        Thread threadTask=new Thread(task);
        threadTask.setDaemon(true);
        setProgressIndicator(0.01, res.getString("app.title46"));
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

        List<Section> collect = baseCombo.getItems().stream().filter(section -> "USER".equals(section.getTag())).collect(Collectors.toList());
        Section userSection=null;
        if(collect.isEmpty()) return;
        userSection=collect.get(0);
        collect=null;


        if(!"USER".equals(baseCombo.getSelectionModel().getSelectedItem().getTag()) )
        {
            //не выбран пользовательский раздел
             res =  showTextInputDialog(this.res.getString("app.title47"), "", this.res.getString("app.title48"),"", getApp().getMainWindow(), Modality.WINDOW_MODAL);

            if(res==null ? false: !res.isEmpty()) start=userSection;
            else return;


        }else
        {
            if(sectionCombo.getSelectionModel().getSelectedItem().getId()==0)
            {
                //не выбран пользовательский раздел
                 res =  showTextInputDialog(this.res.getString("app.title49"), "", this.res.getString("app.title50"),"", getApp().getMainWindow(), Modality.WINDOW_MODAL);

                if(res==null ? false: !res.isEmpty()) start=userSection;
                else return;

            }else if(sectionTree.getSelectionModel().getSelectedItem()==null)
            {
                //выбран раздел в комбобоксе но не выбран в дереве


                res =  showTextInputDialog(this.res.getString("app.title51"), "", this.res.getString("app.title52"), "", getApp().getMainWindow(), Modality.WINDOW_MODAL);

                if(res==null ? false: !res.isEmpty()) start=sectionCombo.getSelectionModel().getSelectedItem();
                else return;

            }else
            {
                //выбран элемент дерева и выбран раздел

                //если выбран не раздел
                if(!(sectionTree.getSelectionModel().getSelectedItem().getValue() instanceof Section))
                {

                    showWarningDialog(this.res.getString("app.title49"),"",this.res.getString("app.title53"),getApp().getMainWindow(),Modality.WINDOW_MODAL );
                    return;


                }

                res =  showTextInputDialog(this.res.getString("app.title49"), "", this.res.getString("app.title54"), "", getApp().getMainWindow(), Modality.WINDOW_MODAL);

                if(res==null ? false: !res.isEmpty())  start=(Section)sectionTree.getSelectionModel().getSelectedItem().getValue();//выберем стартовым раздел
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
        task.progressProperty().addListener((observable, oldValue, newValue) -> setProgressIndicator(newValue.doubleValue()));
        task.setOnRunning(event1 -> setProgressIndicator(0.0, rest.getString("app.title43")));

        task.setOnSucceeded(event ->
        {

            if (task.getValue())
            {
                setProgressIndicator(1.0, rest.getString("app.title44"));

                //хаполнить структуру дерева и комбо.

                ///вопрос - если выбрана база не пользовательскаяч, нужно во всех случаях проверить что выбрано у нас.!!!!!!!!

                    if(startFinal.getParent()==null && "USER".equals(startFinal.getTag()))
                    {

                        if("USER".equals(baseCombo.getSelectionModel().getSelectedItem().getTag()))
                        {
                            //в момент выборы была открыта пользовательская база
                            //если у нас контейнер создан в корне пользовательской базы.
                            sectionCombo.getItems().add(sect);
                            sectionCombo.getSelectionModel().select(sectionCombo.getItems().indexOf(sect));
                        }
                        //если не в пользовательской базе то ничего не делаем

                    }
                    else {

                        //если внутри пользовательской базы то меняем, иначе ничего не делаем
                        if ("USER".equals(baseCombo.getSelectionModel().getSelectedItem().getTag()))
                        {
                            //иначе контейнер создан в дереве
                            if (sectionTree.getSelectionModel().getSelectedItem() == null && sectionCombo.getSelectionModel().getSelectedItem().getId() != 0) {
                                //выбран раздел в комбо но не в дереве

                                rootItem.getChildren().add(new NamedTreeItem(sect));

                            } else if (sectionTree.getSelectionModel().getSelectedItem() != null) {
                                //выбран раздел в дереве

                                sectionTree.getSelectionModel().getSelectedItem().getChildren().add(new NamedTreeItem(sect));
                            } else
                                showErrorDialog(rest.getString("app.title60"), rest.getString("app.title61"), "", getApp().getMainWindow(), Modality.WINDOW_MODAL);
                         }
                    }

            }
            else setProgressIndicator(rest.getString("app.title45"));
            hideProgressIndicator(true);
        });

        task.setOnFailed(event -> {
            setProgressIndicator(rest.getString("app.title45"));
            hideProgressIndicator(true);

            try {
                getApp().getModel().removeSection(sect);
            } catch (Exception e) {
                logger.error("",e);
            }

        });



        Thread threadTask=new Thread(task);
        threadTask.setDaemon(true);
        setProgressIndicator(0.01, rest.getString("app.title46"));
        threadTask.start();










    }

    public void onImportTherapyComplex()
    {
        Profile profile = tableProfile.getSelectionModel().getSelectedItem();
        importTherapyComplex(profile,nums -> {

            this.tableProfile.getSelectionModel().select(profile);

            List<TherapyComplex> lastTherapyComplexes = this.getModel().getLastTherapyComplexes(nums);
            if(!lastTherapyComplexes.isEmpty())
            {

                this.tableComplex.getItems().addAll(lastTherapyComplexes);

            }

            this.btnGenerate.setDisable(false);
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



        task.progressProperty().addListener((observable, oldValue, newValue) -> setProgressIndicator(newValue.doubleValue()));
        task.setOnRunning(event1 -> setProgressIndicator(0.0, rest.getString("app.title43")));

        task.setOnSucceeded(event ->
        {

            if (task.getValue()!=0)
            {


                this.setProgressIndicator(1.0D, rest.getString("app.title44"));

                afterAction.accept(task.getValue());

            }
            else setProgressIndicator(rest.getString("app.title45"));
            hideProgressIndicator(true);
        });

        task.setOnFailed(event -> {
            setProgressIndicator(rest.getString("app.title45"));
            hideProgressIndicator(true);



        });



        Thread threadTask=new Thread(task);
        threadTask.setDaemon(true);
        setProgressIndicator(0.01, rest.getString("app.title46"));
        threadTask.start();



    }


    public void onImportProfileFromFolder()
    {

    }
    /************/


    /**
     * Отобразит строку информации
     * @param message
     */
    public void setInfoMessage(String message)
{
    progress1Pane.setVisible(false);
    progress2Pane.setVisible(false);
    progress3Pane.setVisible(true);

    messageText.setText(message);

    FadeTransition fadeTransition;
    fadeTransition = new FadeTransition(Duration.seconds(6), progress3Pane);
    fadeTransition.setFromValue(1);
    fadeTransition.setDelay(Duration.seconds(3));
    fadeTransition.setToValue(0.01);
    fadeTransition.setOnFinished(event -> {
        progress3Pane.setVisible(false);
        progress3Pane.setOpacity(1.0);
        Platform.runLater(() -> fadeTransition.setOnFinished(null));
    });
    fadeTransition.play();

}

    /**
     *
     * @param value 0 - 1.0
     * @param textAction ниже textInfo
     * @param textInfo вверху
     */
  public void setProgressBar(double value,String textAction,String textInfo)
  {
      progress1Pane.setVisible(false);
      progress2Pane.setVisible(true);
      progress3Pane.setVisible(false);

      if(value>1.0)value=1.0;
      progressAction.setProgress(value);
      textActionInfo.setText(textAction);
      this.textInfo.setText(textInfo);

      if(value==1.0)
      {
          progressIndicatorLabel.setText("");
          Text doneText = (Text) progressIndicator.lookup(".percentage");
          if(doneText!=null)doneText.setText(textAction);
      }




  }


    public void hideProgressBar(boolean animation)
    {
        if(animation)
        {

            FadeTransition fadeTransition;
            fadeTransition = new FadeTransition(Duration.seconds(4), progress2Pane);
            fadeTransition.setFromValue(1);
            fadeTransition.setDelay(Duration.seconds(3));
            fadeTransition.setToValue(0.01);
            fadeTransition.setOnFinished(event -> {
                progress2Pane.setVisible(false);
                progress2Pane.setOpacity(1.0);
                Platform.runLater(() -> fadeTransition.setOnFinished(null) );
            });
            fadeTransition.play();

        }else progress2Pane.setVisible(false);




    }




    /**
     * Установит значение прогресса и текст, сделает все видимым
     * @param value
     * @param text
     */
    public void setProgressIndicator(double value,String text)
    {

        progress2Pane.setVisible(false);
        progress3Pane.setVisible(false);
        progress1Pane.setVisible(true);
        if(value>1.0)value=1.0;
        progressIndicator.setProgress(value);
        progressIndicatorLabel.setText(text);
        if(value==1.0)
        {
            progressIndicatorLabel.setText("");
            Text doneText = (Text) progressIndicator.lookup(".percentage");
            doneText.setText(text);
        }

    }
    public void setProgressIndicator(double value)
    {
        System.out.println(value);

        progress2Pane.setVisible(false);
        progress3Pane.setVisible(false);
        progress1Pane.setVisible(true);
        if(value>1.0)value=1.0;

      progressIndicator.setProgress(value);


        if (value==1.0)
        {
            progressIndicatorLabel.setText("");
            Text doneText = (Text) progressIndicator.lookup(".percentage");
            doneText.setText(progressIndicatorLabel.getText());
        }

    }
    /**
     * Установит неопределенное значение прогресса и текст. Все сделает видимым
     * @param text
     */
    public void setProgressIndicator(String text)
    {
        progressIndicator.setVisible(true);
        progress2Pane.setVisible(false);
        progress3Pane.setVisible(false);
        progress1Pane.setVisible(true);
        progressIndicator.setProgress(-0.5);
        progressIndicatorLabel.setText(text);

    }

    /**
     * Скрывает круговой индикатор прогресса
     */
    public void hideProgressIndicator(boolean animation)
    {
     if(animation)
     {

         FadeTransition fadeTransition;
         fadeTransition = new FadeTransition(Duration.seconds(4), progress1Pane);
         fadeTransition.setFromValue(1);
         fadeTransition.setDelay(Duration.seconds(3));
         fadeTransition.setToValue(0.01);
         fadeTransition.setOnFinished(event -> {
             progress1Pane.setVisible(false);
             progress1Pane.setOpacity(1.0);
             Platform.runLater(() -> fadeTransition.setOnFinished(null));
         });
         fadeTransition.play();

     }else progress1Pane.setVisible(false);




    }




    public void onCreateProfile()
    {
        TextInputValidationController.Data td = new TextInputValidationController.Data("", s -> !BaseController.muchSpecials(s),true);
        try {
            openDialogUserData(getApp().getMainWindow(), "/fxml/TextInputValidationDialogCreate.fxml", res.getString("app.title63"), false, StageStyle.DECORATED, 0, 0, 0, 0, td);

            if (td.isChanged())
            {
                Profile profile = getModel().createProfile(td.getNewVal());

                tableProfile.getItems().add(profile);

                int i = tableProfile.getItems().indexOf(profile);
                tableProfile.requestFocus();
                tableProfile.getSelectionModel().select(i);
                tableProfile.scrollTo(i);
                tableProfile.getFocusModel().focus(i);

            }




            td = null;
        } catch (Exception e) {
            td = null;
            logger.error("",e);
            showExceptionDialog("Ошибка  создания профиля", "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);
            return;
        }



    }

    public void onRemoveProfile()
    {
        Profile selectedItem = tableProfile.getSelectionModel().getSelectedItem();

        if(selectedItem==null) return;


        Optional<ButtonType> buttonType = showConfirmationDialog(res.getString("app.title64"), "", res.getString("app.title65"), getApp().getMainWindow(), Modality.WINDOW_MODAL);

        if(buttonType.isPresent() ? buttonType.get()==okButtonType: false)
        {
            try {



                List<Long> profileFiles = getModel().getProfileFiles(selectedItem);
               File temp=null;
                for (Long file : profileFiles)
                {
                 temp=new File(getApp().getDataDir(),file+".dat");
                    if(temp.exists())temp.delete();
                }

                getModel().removeProfile(selectedItem);
                tableProfile.getItems().remove(selectedItem);
                selectedItem=null;


            } catch (Exception e) {
                selectedItem=null;
                logger.error("",e);
                showExceptionDialog("Ошибка удаления профиля","","",e,getApp().getMainWindow(),Modality.WINDOW_MODAL);
            }

        }

    }
    public void onLoadProfile()
    {


        NewDBImport im=new NewDBImport(getModel());

        im.execute();
        /*
      if(tableComplex.getSelectionModel().getSelectedItem().isMulltyFreq())  System.out.println("трю");
        else System.out.println("элку");
*/
    }


    public void onUpProgram()
    {
        int i = tableProgram.getSelectionModel().getSelectedIndex();
        TherapyProgram tp1=tableProgram.getItems().get(i - 1);
        TherapyProgram tp2=tableProgram.getItems().get(i);
        tableProgram.getItems().set(i-1,tp2);
        tableProgram.getItems().set(i, tp1);

        long pos =  tp1.getPosition();
        tp1.setPosition(tp2.getPosition());
        tp2.setPosition(pos);
        try {
            getModel().updateTherapyProgram(tp1);
            getModel().updateTherapyProgram(tp2);
        } catch (Exception e) {
            logger.error("",e);
        }


        tp1=null;
        tp2=null;



    }

    public void onDownProgram()
    {
        int i = tableProgram.getSelectionModel().getSelectedIndex();
        TherapyProgram tp1=tableProgram.getItems().get(i + 1);
        TherapyProgram tp2=tableProgram.getItems().get(i);
        tableProgram.getItems().set(i+1,tp2);
        tableProgram.getItems().set(i,tp1);

        long pos =  tp1.getPosition();
        tp1.setPosition(tp2.getPosition());
        tp2.setPosition(pos);

        try {
            getModel().updateTherapyProgram(tp1);
            getModel().updateTherapyProgram(tp2);
        } catch (Exception e) {
            logger.error("",e);
        }
        tp1=null;
        tp2=null;

    }

    /**
     * Удаление программи из таблиц терапевтических программ
     */
    public void onRemovePrograms()
    {

        List<TherapyProgram> selectedItems = tableProgram.getSelectionModel().getSelectedItems().stream().collect(Collectors.toList());
        if(selectedItems.isEmpty())return;

        Optional<ButtonType> buttonType = showConfirmationDialog(res.getString("app.title66"), "", res.getString("app.title67"), getApp().getMainWindow(), Modality.WINDOW_MODAL);
        TherapyComplex therapyComplex = tableComplex.getSelectionModel().getSelectedItem();
        if(buttonType.isPresent() ? buttonType.get()==okButtonType: false)
        {
            try {
                for (TherapyProgram p : selectedItems) {
                    getModel().removeTherapyProgram(p);
                    File   temp=new File(getApp().getDataDir(),p.getId()+".dat");
                    if(temp.exists())temp.delete();
                    tableProgram.getItems().remove(p);

                }

                selectedItems.clear();

              updateComplexTime(therapyComplex, true);
                tableProgram.getSelectionModel().clearSelection();

            } catch (Exception e) {
                logger.error("",e);

                showExceptionDialog("Ошибка удаления программы","","",e,getApp().getMainWindow(),Modality.WINDOW_MODAL);

            }


        }





    }

    public void onCreateComplex()
    {

        if(tableProfile.getSelectionModel().getSelectedItem()==null) return;
        //выведем диалог ввода данных
        NameDescroptionDialogController.Data data =null;
        try {
            data = BaseController.openDialogUserData(getApp().getMainWindow(), "/fxml/SectionDialogCreate.fxml", res.getString("app.title68"), false,
                    StageStyle.DECORATED, 0, 0, 0, 0, new NameDescroptionDialogController.Data("",""));


        } catch (IOException e) {
            logger.error("",e);
            data =null;
        }

        if(data ==null){BaseController.showErrorDialog("Ошибка создания комплекса", "", "",  getApp().getMainWindow(),Modality.WINDOW_MODAL);return;}




        //проверим полученные данные из диалога, создали ли имя
        if( data.isNameChanged())
        {

            try {
                TherapyComplex therapyComplex = getModel().createTherapyComplex("", tableProfile.getSelectionModel().getSelectedItem(), data.getNewName(), data.getNewDescription(), 300,3);

                tableComplex.getItems().add(therapyComplex);

                int i = tableComplex.getItems().indexOf(therapyComplex);
                tableComplex.requestFocus();
                tableComplex.getSelectionModel().clearSelection();
                tableComplex.getSelectionModel().select(i);
                tableComplex.scrollTo(i);
                tableComplex.getFocusModel().focus(i);
            } catch (Exception e) {
                logger.error("",e);
                showExceptionDialog("Ошибка создания терапевтического комплекса","","",e,getApp().getMainWindow(),Modality.WINDOW_MODAL);
            }


        }



    }
    public void onRemoveComplex()
    {
        List<TherapyComplex> selectedItems = this.tableComplex.getSelectionModel().getSelectedItems().stream().collect(Collectors.toList());

        if(!selectedItems.isEmpty()) {
            Optional buttonType = showConfirmationDialog(this.res.getString("app.title69"), "", this.res.getString("app.title70"), getApp().getMainWindow(), Modality.WINDOW_MODAL);
            if(buttonType.isPresent() && buttonType.get() == okButtonType) {
                try {
                    Iterator<TherapyComplex> e = selectedItems.iterator();

                    TherapyComplex next;
                    while(e.hasNext()) {
                        next = (TherapyComplex)e.next();
                        List<Long> profileFiles = this.getModel().getTherapyComplexFiles(next);
                        File temp = null;
                        Iterator<Long> next2 = profileFiles.iterator();

                        while(next2.hasNext()) {
                            Long file = (Long)next2.next();
                            temp = new File(getApp().getDataDir(), file + ".dat");
                            if(temp.exists()) {
                                temp.delete();
                            }
                        }

                        this.getModel().removeTherapyComplex(next);
                    }

                    List<TherapyComplex> e1 = selectedItems.stream().collect(Collectors.toList());
                    Iterator<TherapyComplex> iterator1 = e1.iterator();


                    while(iterator1.hasNext()) {
                        this.tableComplex.getItems().remove(iterator1.next());
                    }

                    next = null;
                    selectedItems = null;
                    this.tableProgram.getItems().clear();
                    this.tableComplex.getSelectionModel().clearSelection();
                    this.updateProfileTime((Profile)this.tableProfile.getSelectionModel().getSelectedItem());
                    this.tableComplex.getSelectionModel().clearSelection();
                } catch (Exception var9) {
                    Log.logger.error("", var9);
                    showExceptionDialog("Ошибка удаления комплексов", "", "", var9, getApp().getMainWindow(), Modality.WINDOW_MODAL);
                }
            }

            selectedItems = null;
        }

    }

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



    class RemoveProfileFiles
    {
        private UpdateHandler updateHandler;
        private FailHandler failHandler;
        private boolean cancell=false;

        public  boolean isCancell() {
            return cancell;
        }

        public synchronized void cancel() {
            this.cancell=true;
        }

        private void callFail(String msg){if(failHandler!=null)failHandler.onFail(msg);}
        private void callUpdate(int progress,int total){if(updateHandler!=null)updateHandler.onUpdate(progress,total);}


        public FailHandler getFailHandler() {
            return failHandler;
        }

        public void setFailHandler(FailHandler failHandler) {
            this.failHandler = failHandler;
        }

        public UpdateHandler getUpdateHandler() {
            return updateHandler;
        }

        public void setUpdateHandler(UpdateHandler updateHandler) {
            this.updateHandler = updateHandler;
        }


        public boolean remove()
        {
            boolean ret=true;

            cancell=false;

                    File f = null;
                    List<TherapyProgram> allTherapyProgram = getModel().getAllTherapyProgram(tableProfile.getSelectionModel().getSelectedItem()).
                            stream().filter(therapyProgram -> !therapyProgram.isMp3()).collect(Collectors.toList());
                    int total=allTherapyProgram.size();
                    int progress=1;
                    for (TherapyProgram itm : allTherapyProgram)
                    {
                        if(isCancell()){
                            ret=false;
                            break;
                        }

                        f = new File(getApp().getDataDir(), itm.getId() + ".dat");
                        if (f.exists()) f.delete();
                        itm.setChanged(true);
                        try
                        {
                            getModel().updateTherapyProgram(itm);
                        } catch (Exception e)
                        {
                            logger.error("",e);
                            showExceptionDialog("Ошибка обновления данных терапевтических программ.", "Очистка произошла с ошибкой", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);
                            ret=false;
                            callFail("Очистка произошла с ошибкой");
                           break;
                        }
                        callUpdate(progress++,total);
                    }




            return ret;
        }

    }


    public void onRemoveProfileFiles()
    {
        if (tableProfile.getSelectionModel().getSelectedItem() == null) return;

        Optional<ButtonType> buttonType = showConfirmationDialog(res.getString("app.title73"), res.getString("app.title74"), res.getString("app.title75"), getApp().getMainWindow(), Modality.WINDOW_MODAL);

        if (buttonType.isPresent())
        {
            if (buttonType.get() == okButtonType)
            {
                RemoveProfileFiles rp=new RemoveProfileFiles();

                Task<Boolean> task=new Task<Boolean>() {
                    @Override
                    protected Boolean call() throws Exception {

                        rp.setUpdateHandler((progress, total) -> updateProgress(progress, total));
                        rp.setFailHandler(failMessage -> failed());
                        return rp.remove();

                    }
                };


                CalcLayer.openLayer(() -> Platform.runLater(() ->
                {
                    //отмена генерации. Что успело нагенериться то успело
                    //скрытие произойдет по окончании работы  ниже
                    CalcLayer.setInfoLayer(res.getString("app.title76"));
                    rp.cancel();

                }), getApp().getMainWindow(),false);



                task.setOnScheduled(event1 -> setProgressBar(0.0, res.getString("app.title77"), res.getString("app.title78")));

                task.setOnFailed(event ->
                                Platform.runLater(() ->
                                {
                                    CalcLayer.closeLayer();
                                    setProgressBar(100.0, res.getString("app.title79"), res.getString("app.title78"));

                                    if (tableProfile.getSelectionModel().getSelectedItem() != null)
                                    {
                                        Profile p = tableProfile.getSelectionModel().getSelectedItem();
                                        int i = tableProfile.getItems().indexOf(tableProfile.getSelectionModel().getSelectedItem());
                                        tableProfile.getItems().set(i, null);
                                        tableProfile.getItems().set(i, p);
                                        Platform.runLater(() -> tableProfile.getSelectionModel().select(i));
                                        p = null;
                                    }


                                    hideProgressBar(true);

                                    rp.setFailHandler(null);
                                    rp.setUpdateHandler(null);





                                })
                );
                task.setOnSucceeded(event ->
                                Platform.runLater(() ->
                                {

                                    if (task.getValue())
                                        setProgressBar(100.0, res.getString("app.title80"), res.getString("app.title78"));
                                    else {
                                        if (rp.isCancell()) setProgressBar(100.0,  res.getString("app.cancel"), res.getString("app.title78"));
                                        else setProgressBar(0.0,  res.getString("app.error"), res.getString("app.title78"));
                                    }


                                    if (tableProfile.getSelectionModel().getSelectedItem() != null)
                                    {
                                        Profile p = tableProfile.getSelectionModel().getSelectedItem();
                                        int i = tableProfile.getItems().indexOf(tableProfile.getSelectionModel().getSelectedItem());
                                        tableProfile.getItems().set(i, null);
                                        tableProfile.getItems().set(i, p);
                                        Platform.runLater(() -> tableProfile.getSelectionModel().select(i));
                                        p = null;
                                    }


                                    CalcLayer.closeLayer();
                                    hideProgressBar(true);

                                    rp.setFailHandler(null);
                                    rp.setUpdateHandler(null);



                                })
                );

                task.progressProperty().addListener((observable, oldValue, newValue) ->
                {

                    Platform.runLater(() -> setProgressBar(newValue.doubleValue(), res.getString("app.title81"), res.getString("app.title78")));

                });
                Thread thread=new Thread(task);
                thread.setDaemon(true);
                thread.start();


               CalcLayer.showLayer();


            }
        }


    }

    public void  onGenerate()
    {



        if(m2Ready.get()){
            showWarningDialog(res.getString("app.ui.attention"),"",res.getString("trinity.warn"),getApp().getMainWindow(),Modality.WINDOW_MODAL);
        }
        if(tableProfile.getSelectionModel().getSelectedItem()==null){btnGenerate.setDisable(true);return;}
        if(!getModel().isNeedGenerateFilesInProfile(tableProfile.getSelectionModel().getSelectedItem())) {btnGenerate.setDisable(true);return;}

        try {

            //в начале мы снимим пометки с комплексов и пометим все программы требующие изменений. Далее кодек будет уже брать программы и по мере кодирования
            //снимать с них пометки. В конце просто обновиться список программ в таблице

            for (TherapyProgram therapyProgram : getModel().findNeedGenerateList((tableProfile.getSelectionModel().getSelectedItem())) )
            {
                    if(therapyProgram.isMp3()) continue;

                therapyProgram.setChanged(true);//установим для всех программ(особенно тех чей комплекс помечен изменением) статус изменения
                getModel().updateTherapyProgram(therapyProgram);
            }



            //в том комплексе что выбран в в таблице программ пометим все как имзмененные. Чтобы не исчезла иконка
            if( tableComplex.getSelectionModel().getSelectedItem()!=null)
            {

                tableProgram.getItems().forEach(itm->{
                    if(tableComplex.getSelectionModel().getSelectedItem().isChanged())
                    {
                        if(!itm.isMp3())
                        {
                            itm.setChanged(true);
                            itm.getTherapyComplex().setChanged(true);
                        }
                    }


                });


            }
            //снимим пометки с комплексов об изменеии. Теперь важны лишь программы

            //тк генерить можно только выбрав профиль, то все комплексы в таблице можно просматривать
            for (TherapyComplex therapyComplex : tableComplex.getItems())
            {
                if(therapyComplex.isChanged())
                {
                    therapyComplex.setChanged(false);
                    getModel().updateTherapyComplex(therapyComplex);
                }

            }






            btnGenerate.setDisable(true);
        } catch (Exception e) {
            logger.error("",e);
            showExceptionDialog(res.getString("app.title82"), "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);
        }




        try
        {

            MP3Encoder encoder=new MP3Encoder(tableProfile.getSelectionModel().getSelectedItem(), MP3Encoder.CODEC_TYPE.EXTERNAL_CODEC,44100);
            Thread thread =new Thread(encoder);



            CalcLayer.openLayer(() -> Platform.runLater(() ->
            {
                //отмена генерации. Что успело нагенериться то успело
                //скрытие произойдет по окончании работы  ниже
                CalcLayer.setInfoLayer(res.getString("app.title76"));
                encoder.stopEncode();

            }), getApp().getMainWindow(),false);






            // обработка окончания кодирования каждой програмы. Может быть по отмене! isCanceled. Само окннчание по отмене мы специально не ловим. Просто убираем ненужное с экрана
            encoder.setActionListener((id, isCanceled) -> {
            //здесь изменим параметры в базе. Если была отмена, то не будем менять для этой програмы
                if(!isCanceled)
                {
                    //просто перезапишим Changed поле. По окончаюю всей обработки,обновим таблицу програм, если она активна и все. Комплексы уже подправлены ранее
                    TherapyProgram tp = getModel().getTherapyProgram(id);
                    tp.setChanged(false);
                    tp.setUuid(UUID.randomUUID().toString());//при загрузки файлов это читается из базы а не таблицы. можно не беспокоится об обновлении бинов таблицы

                    try {
                        getModel().updateTherapyProgram(tp);
                    } catch (Exception e) {
                        logger.error("",e);
                    }
                    tp=null;
                }


            });





            encoder.setOnScheduled(event1 -> setProgressBar(0.0,res.getString("app.title83"),res.getString("app.title84")));

            encoder.setOnFailed(event ->
                Platform.runLater(() ->
                {
                    CalcLayer.closeLayer();
                    setProgressBar(100.0, res.getString("app.error"), res.getString("app.title84"));
                    hideProgressBar(true);

                    encoder.removeActionListener();

                    //обновим таблицу программ если у нас выбран комлекс
                    if(tableComplex.getSelectionModel().getSelectedItem()!=null)
                    {
                        TherapyComplex p=tableComplex.getSelectionModel().getSelectedItem();
                        int i = tableComplex.getItems().indexOf(tableComplex.getSelectionModel().getSelectedItem());
                        tableComplex.getItems().set(i, null);
                        tableComplex.getItems().set(i, p);
                        tableComplex.getSelectionModel().select(i);
                        p = null;
                    }


                })
            );
            encoder.setOnSucceeded(event ->
                Platform.runLater(() ->
                {
                    if(encoder.isStop())
                    {
                        CalcLayer.closeLayer();
                        setProgressBar(0.0, res.getString("app.cancel"), res.getString("app.title84"));
                        hideProgressBar(true);
                        encoder.removeActionListener();
                        return;
                    }//если была остановка от кнопки в диалоге

                    if(encoder.getValue())setProgressBar(100.0, res.getString("app.title85"), res.getString("app.title84"));
                    else  setProgressBar(100.0, res.getString("app.cancel"), res.getString("app.title84"));
                    CalcLayer.closeLayer();
                    hideProgressBar(true);
                    //System.out.println("COMPLETE");
                    encoder.removeActionListener();

                    //обновим таблицу программ если у нас выбран комлекс
                    if(tableComplex.getSelectionModel().getSelectedItem()!=null)
                    {
                        TherapyComplex p=tableComplex.getSelectionModel().getSelectedItem();
                        int i = tableComplex.getItems().indexOf(tableComplex.getSelectionModel().getSelectedItem());
                        tableComplex.getItems().set(i, null);
                        tableComplex.getItems().set(i, p);
                        tableComplex.getSelectionModel().select(i);
                        p = null;
                    }

                })
            );

            encoder.progressProperty().addListener((observable, oldValue, newValue) ->
            {
                //System.out.println(newValue);
                Platform.runLater(() -> setProgressBar(newValue.doubleValue(),encoder.getCurrentName() /*res.getString("app.title83")*/, res.getString("app.title84")));

            });


            thread.start();


            CalcLayer.showLayer();


        } catch (Exception e) {
            logger.error("",e);
            showExceptionDialog(res.getString("app.title86"), "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);
        }



        tableProfile.getSelectionModel().getSelectedItem().setProfileWeight(tableProfile.getSelectionModel().getSelectedItem().getProfileWeight()+1);//пересчет веса инициализируем
        checkUpploadBtn();





    }


class ForceCopyProfile
{
    private UpdateHandler updateHandler;
    private FailHandler failHandler;

    private boolean cancel=false;

    public synchronized void cancel()
    {
        cancel=true;

    }

    public boolean isCancel() {
        return cancel;
    }

    private void callFail(String msg){if(failHandler!=null)failHandler.onFail(msg);}
    private void callUpdate(int progress,int total){if(updateHandler!=null)updateHandler.onUpdate(progress,total);}


    public FailHandler getFailHandler() {
        return failHandler;
    }

    public void setFailHandler(FailHandler failHandler) {
        this.failHandler = failHandler;
    }

    public UpdateHandler getUpdateHandler() {
        return updateHandler;
    }

    public void setUpdateHandler(UpdateHandler updateHandler) {
        this.updateHandler = updateHandler;
    }

    private boolean forceCopyProfile(File profile,boolean inDir) throws Exception
    {
        File prof =null;
        cancel = false;
        if (tableProfile.getSelectionModel().getSelectedItem() == null)
        {
            callFail("Не выбран профиль!");
            return false;
        }
        int total = (int) getModel().countTherapyPrograms(tableProfile.getSelectionModel().getSelectedItem()) + 2;
        int progress = 1;

        if (!inDir)
        {
            //здесь стирание всего и запись нового

            //на всякий проверим наличие lib файлов

            File f2 = new File(devicePath.toFile(), "ASDKDD.LIB");
        boolean flg = true;
        if (!f2.exists()) flg = false;
        else flg = true;

        if (flg == false)
        {
            f2 = new File(devicePath.toFile(), "asdkdd.lib");
            if (!f2.exists()) flg = false;
            else flg = true;

        }

        if (flg == false)
        {

            Platform.runLater(() -> showWarningDialog(res.getString("app.title87"), res.getString("app.title88"), res.getString("app.title89"), getApp().getMainWindow(), Modality.WINDOW_MODAL));


            callFail("Ошибка идентификации диска прибора.");
            return false;

        }


        //теперь все сотрем нафиг
        if (FilesProfileHelper.recursiveDelete(devicePath.toFile(), "asdkdd.lib"))
        {
            Platform.runLater(() -> showErrorDialog(res.getString("app.title87"), "", res.getString("app.title90"), getApp().getMainWindow(), Modality.WINDOW_MODAL));

            callFail("Ошибка удаления файлов");
            return false;

        }

            prof=profile;

    }else
        {
            prof=new File(profile,"profile.txt");
        }
            callUpdate(progress++,total);


        if(cancel)return false;

        Map<Long,String> cMap=new LinkedHashMap<>();
        Map<Long,Integer> cMap2=new LinkedHashMap<>();


        TableColumn<TherapyComplex, ?> timeTableColumn = tableComplex.getColumns().get(3);//с учетом что время это 4 колонка!!!


        //TODO:   ///  нельзя использовать в именах  \ / ? : * " > < |  +=[]:;«,./?'пробел'     Нужно это выфильтровывать
        //генерируем список названий папкок для комплексов.
        tableComplex.getItems().forEach(itm ->
                {

                    cMap.put(itm.getId(), TextUtil.replaceWinPathBadSymbols(itm.getName()) + " (" + DateUtil.replaceTime(timeTableColumn.getCellObservableValue(itm).getValue().toString(),res) + ")");
                    cMap2.put(itm.getId(), itm.getTimeForFrequency());
                }
        );



        //

        if(cancel)return false;

//запись файла профиля
        try(PrintWriter writer = new PrintWriter(prof,"UTF-8")) {




            writer.println(tableProfile.getSelectionModel().getSelectedItem().getId().toString());//id профиля
           // writer.write("\n");

            writer.println(tableProfile.getSelectionModel().getSelectedItem().getUuid());//uuid профиля. Чтобы не перепутать с профилем записанном на другой программе
           // writer.write("\n");
/*
            for (Map.Entry<Long, String> entry : cMap.entrySet())
            {
                writer.write(entry.getKey()+"@"+entry.getValue()+"#");

            }
            writer.write("\n");
*/
            writer.print(tableProfile.getSelectionModel().getSelectedItem().getName());//название профиля
        }catch (IOException e)
        {
            logger.error("",e);
            Platform.runLater(() -> showExceptionDialog(res.getString("app.title87"), "", res.getString("app.title91"), e, getApp().getMainWindow(), Modality.WINDOW_MODAL));

            callFail("Ошибка записи файлов профиля.");
            return false;
        }
        catch (Exception e) {
            logger.error("",e);
            Platform.runLater(() -> showExceptionDialog(res.getString("app.title87"), "", res.getString("app.title92"), e, getApp().getMainWindow(), Modality.WINDOW_MODAL));

            callFail("Ошибка записи файлов профиля.");
            return false;
        }
        callUpdate(progress++,total);


        File bDir=null;
        if(inDir)bDir=profile;
        else bDir=devicePath.toFile();

        //запишем файлы и папки
        int cnt=0;
        File tempFile=null;
        if(cancel)return false;
        try
        {
            for (Map.Entry<Long, String> entry : cMap.entrySet())
            {
                if(cancel)return false;
                // URI outputURI = new URI(("file:///"+devicePath. output.replaceAll(" ", "%20")));
                //File outputFile = new File(outputURI);
                tempFile= new File(bDir,(++cnt)+"-"+entry.getValue());//папка комплекса записываемого


                //System.out.print("Создание папки комплекса: " + tempFile.getAbsolutePath() + "...");
               // System.out.println("OK");

                FilesProfileHelper.copyDir(tempFile);


                int count2=0;
                TherapyComplex therapyComplex=null;
                for (TherapyProgram therapyProgram : getModel().findTherapyPrograms(entry.getKey()))
                {
                    if (cancel) return false;
                    therapyComplex = getModel().findTherapyComplex(entry.getKey());

                    if (!therapyProgram.isMp3())
                    {
                        //если программа не MP3
                        String timeP = DateUtil.convertSecondsToHMmSs(therapyComplex.getTimeForFrequency());


                    String nameFile = (++count2) + "-" + TextUtil.replaceWinPathBadSymbols(therapyProgram.getName()) + " (" + DateUtil.replaceTime(timeP,res) + ")";

                    // System.out.print("Создание программы: " + nameFile + ".bss...");

                    FilesProfileHelper.copyBSS(new File(getApp().getDataDir(), therapyProgram.getId() + ".dat"),
                            new File(tempFile, nameFile + ".bss"));
                    //System.out.println("OK");
                    // System.out.print("Создание текста программы: " + nameFile + ".txt...");
                    FilesProfileHelper.copyTxt(therapyProgram.getFrequencies(), cMap2.get(entry.getKey()), therapyProgram.getId().longValue(),
                            therapyProgram.getUuid(), entry.getKey(),therapyComplex.getBundlesLength(), TextUtil.replaceWinPathBadSymbols(therapyProgram.getName()),
                            false, new File(tempFile, nameFile + ".txt"), therapyProgram.isMultyFreq(),therapyProgram.getSrcUUID(),therapyComplex.getSrcUUID());
                    // System.out.println("OK");
                }else
                    {
                        //если программа MP3

                        Mp3File mp3file = null;
                        try {
                            mp3file = new Mp3File(therapyProgram.getFrequencies());
                        } catch (Exception e) {mp3file=null;}

                        if(mp3file!=null)
                        {
                            //если файл существует
                            String timeP =DateUtil.convertSecondsToHMmSs(mp3file.getLengthInSeconds());

                            String nameFile = (++count2) + "-" + TextUtil.replaceWinPathBadSymbols(therapyProgram.getName()) + " (" + DateUtil.replaceTime(timeP,res) + ")";

                            FilesProfileHelper.copyBSS(new File(therapyProgram.getFrequencies()),new File(tempFile, nameFile + ".bss"));

                            FilesProfileHelper.copyTxt(therapyProgram.getFrequencies(), cMap2.get(entry.getKey()), therapyProgram.getId().longValue(),
                                    therapyProgram.getUuid(), entry.getKey(),therapyComplex.getBundlesLength(), TextUtil.replaceWinPathBadSymbols(therapyProgram.getName()),
                                    true, new File(tempFile, nameFile + ".txt"), therapyProgram.isMultyFreq(),therapyProgram.getSrcUUID(),therapyComplex.getSrcUUID());




                        }
                    }


                    callUpdate(progress++, total);

                }

                //System.out.println("COMPLETED");
            }
        }catch (IOException e)
        {

            logger.error("",e);
            Platform.runLater(() -> showWarningDialog(res.getString("app.title87"), "", res.getString("app.title115"), getApp().getMainWindow(), Modality.WINDOW_MODAL));
            callFail("Ошибка копирования файлов.");
            return false;
        }
        catch (Exception e)
        {
            logger.error("",e);
            Platform.runLater(() -> showExceptionDialog(res.getString("app.title87"), "", res.getString("app.title93"), e, getApp().getMainWindow(), Modality.WINDOW_MODAL));
            callFail("Ошибка копирования файлов.");
            return false;
        }


        return true;
    }




}
     interface UpdateHandler
    {
        public void onUpdate(int progress,int total);

    }
    interface FailHandler
    {
        public void onFail(String failMessage);

    }


    /**
     * Загрузка профиля в выбранную папку. Папка должна быть пустая
     */
    public void uploadInDir()
    {
        if(tableProfile.getSelectionModel().getSelectedItem()==null) return;

        if(getModel().isNeedGenerateFilesInProfile(tableProfile.getSelectionModel().getSelectedItem()))
        {
            showWarningDialog(res.getString("app.title87"), "", res.getString("app.title97"), getApp().getMainWindow(), Modality.WINDOW_MODAL);
            return;
        }

        //проверим нет ли мп3 с неверными путями
        List<TherapyProgram> failedMp3=new ArrayList<>();
        long count = getModel().mp3ProgramInProfile(tableProfile.getSelectionModel().getSelectedItem()).stream().filter(itm ->
        {
            File ff=new File(itm.getFrequencies());//в частотах прописан путь к файлу
            if(!ff.exists()) { failedMp3.add(itm); return true;}
            else return false;
        }).count();

        //напишим в окне список путей невверных
        if(count>0) {
            String failed = failedMp3.stream().map(tt -> tt.getFrequencies()).collect(Collectors.joining("\n"));
            updateProfileTime(tableProfile.getSelectionModel().getSelectedItem());
            showWarningDialog(res.getString("app.title87"),failed,res.getString("app.ui.mp3_not_avaliable"),getApp().getMainWindow(),Modality.WINDOW_MODAL);

            return;
        }


        DirectoryChooser dirChooser =new DirectoryChooser();
        dirChooser.setTitle(res.getString("app.upload_to_dir"));
        dirChooser.setInitialDirectory(new File(getModel().getLastSavedFilesPath(System.getProperty("user.home"))));

        File dir= dirChooser.showDialog(getApp().getMainWindow());
        if(dir==null)return;
        if(!dir.exists()) {
            showWarningDialog(res.getString("app.upload_to_dir"),  res.getString("app.ui.dir_not_exist"), "", getApp().getMainWindow(), Modality.WINDOW_MODAL);
            return;}

            getModel().setLastSavedFilesPath(dir.getParentFile());

        int cnt = dir.list().length;
        if(cnt!=0)
        {
            showWarningDialog(res.getString("app.upload_to_dir"),  res.getString("app.ui.dir_not_empty"), res.getString("app.ui_dir_not_empty2"), getApp().getMainWindow(), Modality.WINDOW_MODAL);
            return;}

        ForceCopyProfile fcp=new ForceCopyProfile();



        Task<Boolean> task =new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                fcp.setUpdateHandler((progress, total) -> updateProgress(progress, total));
                fcp.setFailHandler(failMessage -> failed());
                boolean r=false;
                try
                {
                    r=  fcp.forceCopyProfile(dir,true);
                }catch (Exception e)
                {
                    logger.error("",e);
                }
                return r;

            }
        };



        CalcLayer.openLayer(() -> Platform.runLater(() ->
        {
            //отмена генерации. Что успело нагенериться то успело
            //скрытие произойдет по окончании работы  ниже
            CalcLayer.setInfoLayer(res.getString("app.title76"));
            fcp.cancel();

        }), getApp().getMainWindow(),false);


        task.progressProperty().addListener((observable, oldValue, newValue) -> setProgressBar(newValue.doubleValue(),res.getString("app.title101"),""));
        task.setOnRunning(event1 -> setProgressBar(0.0, res.getString("app.title102"),""));

        task.setOnSucceeded(event ->
        {
            CalcLayer.closeLayer();

            if (task.getValue()) {
                hideProgressBar(false);
                setProgressIndicator(1.0, res.getString("app.title103"));

            } else {

                hideProgressBar(false);
                if(fcp.isCancel()) setProgressIndicator(res.getString("app.title104"));
                else setProgressIndicator(res.getString("app.title93"));
            }
            hideProgressIndicator(true);
        });

        task.setOnFailed(event -> {
            CalcLayer.closeLayer();
            hideProgressBar(false);
            setProgressIndicator(res.getString("app.title93"));
            hideProgressIndicator(true);


        });


        Thread threadTask=new Thread(task);
        threadTask.setDaemon(true);
        setProgressBar(0.01, res.getString("app.title102"), "");

        threadTask.start();
        CalcLayer.showLayer();






    }
    /**
     * Загрузка профиля в папку или на устройство
     *
     */
    public void onUploadProfile()
    {
        //проверим что действительно все файлы сгенерированны.
        //проверим что прибор подключен

        if(!getConnectedDevice()){
            showWarningDialog(res.getString("app.title87"), "", res.getString("app.title96"), getApp().getMainWindow(), Modality.WINDOW_MODAL);
            return;
        }
        if(tableProfile.getSelectionModel().getSelectedItem()==null) return;

        if(getModel().isNeedGenerateFilesInProfile(tableProfile.getSelectionModel().getSelectedItem()))
        {
            showWarningDialog(res.getString("app.title87"), "", res.getString("app.title97"), getApp().getMainWindow(), Modality.WINDOW_MODAL);
            return;
        }

        if(devicePath==null )
        {
            showErrorDialog(res.getString("app.title87"),"",res.getString("app.title98"),getApp().getMainWindow(),Modality.WINDOW_MODAL);
            return;
        }

        //проверим нет ли мп3 с неверными путями
        List<TherapyProgram> failedMp3=new ArrayList<>();
        long count = getModel().mp3ProgramInProfile(tableProfile.getSelectionModel().getSelectedItem()).stream().filter(itm ->
        {
            File ff=new File(itm.getFrequencies());//в частотах прописан путь к файлу
            if(!ff.exists()) { failedMp3.add(itm); return true;}
            else return false;
        }).count();

        //напишим в окне список путей невверных
        if(count>0) {
            String failed = failedMp3.stream().map(tt -> tt.getFrequencies()).collect(Collectors.joining("\n"));
            updateProfileTime(tableProfile.getSelectionModel().getSelectedItem());
            showWarningDialog(res.getString("app.title87"),failed,res.getString("app.ui.mp3_not_avaliable"),getApp().getMainWindow(),Modality.WINDOW_MODAL);

            return;
        }

        File f=new   File( devicePath.toFile(),"profile.txt");
            Optional<ButtonType> buttonType = showConfirmationDialog(res.getString("app.uppload_"), res.getString("app.title99"), res.getString("app.title100"), getApp().getMainWindow(), Modality.WINDOW_MODAL);

            if(buttonType.isPresent())
            {
                if(buttonType.get()==okButtonType)
                {
                    //здесь стирание всего и запись нового
                    ForceCopyProfile fcp=new ForceCopyProfile();



                    Task<Boolean> task =new Task<Boolean>() {
                        @Override
                        protected Boolean call() throws Exception {
                            fcp.setUpdateHandler((progress, total) -> updateProgress(progress, total));
                            fcp.setFailHandler(failMessage -> failed());
                            boolean r=false;
                            try
                            {
                                r=  fcp.forceCopyProfile(f,false);
                            }catch (Exception e)
                            {
                                logger.error("",e);
                            }
                            return r;

                        }
                    };



                    CalcLayer.openLayer(() -> Platform.runLater(() ->
                    {
                        //отмена генерации. Что успело нагенериться то успело
                        //скрытие произойдет по окончании работы  ниже
                        CalcLayer.setInfoLayer(res.getString("app.title76"));
                        fcp.cancel();

                    }), getApp().getMainWindow(),false);


                    task.progressProperty().addListener((observable, oldValue, newValue) -> setProgressBar(newValue.doubleValue(),res.getString("app.title101"),""));
                    task.setOnRunning(event1 -> setProgressBar(0.0, res.getString("app.title102"),""));

                    task.setOnSucceeded(event ->
                    {
                        CalcLayer.closeLayer();

                        if (task.getValue()) {
                            hideProgressBar(false);
                            setProgressIndicator(1.0, res.getString("app.title103"));

                        } else {

                            hideProgressBar(false);
                            if(fcp.isCancel()) setProgressIndicator(res.getString("app.title104"));
                            else setProgressIndicator(res.getString("app.title93"));
                        }
                        hideProgressIndicator(true);
                    });

                    task.setOnFailed(event -> {
                        CalcLayer.closeLayer();
                        hideProgressBar(false);
                        setProgressIndicator(res.getString("app.title93"));
                        hideProgressIndicator(true);


                    });


                    Thread threadTask=new Thread(task);
                    threadTask.setDaemon(true);
                    setProgressBar(0.01, res.getString("app.title102"), "");

                    threadTask.start();
                    CalcLayer.showLayer();







                }else return;

            }else
            {

                showErrorDialog(res.getString("app.title87"),"","Ошибка выбора",getApp().getMainWindow(),Modality.WINDOW_MODAL);
                return;
            }





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

            tableProfile.getItems().add(profile);
            tableProfile.getSelectionModel().select(profile);
            tableProfile.scrollTo(profile);
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


  /* public void onUploadProfile()
   {
        //проверим что действительно все файлы сгенерированны.
       //проверим что прибор подключен

       if(!getConnectedDevice()){
           showWarningDialog(res.getString("app.title87"), "", res.getString("app.title96"), getApp().getMainWindow(), Modality.WINDOW_MODAL);
           return;
       }
      if(tableProfile.getSelectionModel().getSelectedItem()==null) return;

       if(getModel().isNeedGenerateFilesInProfile(tableProfile.getSelectionModel().getSelectedItem()))
       {
           showWarningDialog(res.getString("app.title87"), "", res.getString("app.title97"), getApp().getMainWindow(), Modality.WINDOW_MODAL);
           return;
       }

       if(devicePath==null )
       {
           showErrorDialog(res.getString("app.title87"),"",res.getString("app.title98"),getApp().getMainWindow(),Modality.WINDOW_MODAL);
           return;
       }

       //проверим нет ли мп3 с неверными путями
       List<TherapyProgram> failedMp3=new ArrayList<>();
       long count = getModel().mp3ProgramInProfile(tableProfile.getSelectionModel().getSelectedItem()).stream().filter(itm ->
       {
           File ff=new File(itm.getFrequencies());//в частотах прописан путь к файлу
           if(!ff.exists()) { failedMp3.add(itm); return true;}
               else return false;
       }).count();

            //напишим в окне список путей невверных
       if(count>0) {
           String failed = failedMp3.stream().map(tt -> tt.getFrequencies()).collect(Collectors.joining("\n"));
           updateProfileTime(tableProfile.getSelectionModel().getSelectedItem());
           showWarningDialog(res.getString("app.title87"),failed,res.getString("app.ui.mp3_not_avaliable"),getApp().getMainWindow(),Modality.WINDOW_MODAL);

           return;
       }


       //проверим наличие файла profile.txt

         File f=new   File( devicePath.toFile(),"profile.txt");


       if(!f.exists())
       {
           //загрузка на чистый прибор или профилем со страрой программы!!

           Optional<ButtonType> buttonType = showConfirmationDialog(res.getString("app.title87"), res.getString("app.title99"), res.getString("app.title100"), getApp().getMainWindow(), Modality.WINDOW_MODAL);

           if(buttonType.isPresent())
           {
            if(buttonType.get()==okButtonType)
            {
            //здесь стирание всего и запись нового
                ForceCopyProfile fcp=new ForceCopyProfile();



                Task<Boolean> task =new Task<Boolean>() {
                    @Override
                    protected Boolean call() throws Exception {
                        fcp.setUpdateHandler((progress, total) -> updateProgress(progress, total));
                        fcp.setFailHandler(failMessage -> failed());
                        boolean r=false;
                        try
                        {
                          r=  fcp.forceCopyProfile(f,false);
                        }catch (Exception e)
                        {
                            logger.error("",e);
                        }
                       return r;

                    }
                };



                CalcLayer.openLayer(() -> Platform.runLater(() ->
                    {
                        //отмена генерации. Что успело нагенериться то успело
                        //скрытие произойдет по окончании работы  ниже
                        CalcLayer.setInfoLayer(res.getString("app.title76"));
                        fcp.cancel();

                    }), getApp().getMainWindow(),false);


                task.progressProperty().addListener((observable, oldValue, newValue) -> setProgressBar(newValue.doubleValue(),res.getString("app.title101"),""));
                task.setOnRunning(event1 -> setProgressBar(0.0, res.getString("app.title102"),""));

                task.setOnSucceeded(event ->
                {
                    CalcLayer.closeLayer();

                    if (task.getValue()) {
                        hideProgressBar(false);
                        setProgressIndicator(1.0, res.getString("app.title103"));

                    } else {

                        hideProgressBar(false);
                        if(fcp.isCancel()) setProgressIndicator(res.getString("app.title104"));
                        else setProgressIndicator(res.getString("app.title93"));
                    }
                    hideProgressIndicator(true);
                });

                task.setOnFailed(event -> {
                    CalcLayer.closeLayer();
                    hideProgressBar(false);
                    setProgressIndicator(res.getString("app.title93"));
                    hideProgressIndicator(true);


                });


                Thread threadTask=new Thread(task);
                threadTask.setDaemon(true);
                setProgressBar(0.01, res.getString("app.title102"), "");

                threadTask.start();
                CalcLayer.showLayer();







            }else return;

           }else
           {

               showErrorDialog(res.getString("app.title87"),"","Ошибка выбора",getApp().getMainWindow(),Modality.WINDOW_MODAL);
               return;
           }

       }else
       {



           //профиль есть и определен файл. нужно его прочитать и понять какой у нас профиль!
           List<String> profileParam = new ArrayList<String>();
            try( Scanner in = new Scanner(f,"UTF-8"))
            {
                while (in.hasNextLine()){

                    profileParam.add(in.nextLine());
                    System.out.println(profileParam.get(profileParam.size()-1));
                }

            }
            catch (IOException e)
            {
                logger.error("",e);
                showExceptionDialog(res.getString("app.title105"), res.getString("app.title106"), res.getString("app.title107"), e, getApp().getMainWindow(), Modality.WINDOW_MODAL);
                return;
            }
            catch (Exception e)
            {
                logger.error("",e);
                showExceptionDialog(res.getString("app.title105"), res.getString("app.title106"), res.getString("app.title107"), e, getApp().getMainWindow(), Modality.WINDOW_MODAL);
                return;
            }

           if(profileParam.size()!=3)
           {
               showErrorDialog(res.getString("app.title105"),res.getString("app.title106"), res.getString("app.title107"),getApp().getMainWindow(),Modality.WINDOW_MODAL);
               return;
           }

            long idProf=Long.parseLong(profileParam.get(0));
           String uuidP=profileParam.get(1);


           String nameP=profileParam.get(2);


           Profile prof=tableProfile.getSelectionModel().getSelectedItem();

           if(prof.getId().longValue()==idProf && prof.getUuid().equals(uuidP))
           {
               //у нас тот же профиль что мы пытаемся записать
               Optional<ButtonType> buttonType = showConfirmationFileUpdateDialog(res.getString("app.title87"), res.getString("app.title108"), res.getString("app.title109"), getApp().getMainWindow(), Modality.WINDOW_MODAL);

               if(buttonType.isPresent())
               {
                   if (buttonType.get() == forceUpdateBtnType)
                   {
                        //стирание профиля и запись заного
                       ForceCopyProfile fcp=new ForceCopyProfile();



                       Task<Boolean> task =new Task<Boolean>() {
                           @Override
                           protected Boolean call() throws Exception {
                               fcp.setUpdateHandler((progress, total) -> updateProgress(progress,total));
                               fcp.setFailHandler(failMessage -> failed());
                               boolean r=false;
                               try
                               {
                                  r= fcp.forceCopyProfile(f,false);
                               }catch (Exception e)
                               {
                                   logger.error("",e);
                               }
                               return r;

                           }
                       };

                       CalcLayer.openLayer(() -> Platform.runLater(() ->
                       {
                           //отмена генерации. Что успело нагенериться то успело
                           //скрытие произойдет по окончании работы  ниже
                           CalcLayer.setInfoLayer(res.getString("app.title76"));
                           fcp.cancel();

                       }), getApp().getMainWindow(),false);


                       task.progressProperty().addListener((observable, oldValue, newValue) -> setProgressBar(newValue.doubleValue(), res.getString("app.title101"), ""));
                       task.setOnRunning(event1 -> setProgressBar(0.0, res.getString("app.title102"), ""));

                       task.setOnSucceeded(event ->
                       {
                          CalcLayer.closeLayer();
                           if (task.getValue()) {
                               hideProgressBar(false);
                               setProgressIndicator(1.0, res.getString("app.title103"));

                           } else {
                               hideProgressBar(false);
                               if (fcp.isCancel()) setProgressIndicator(res.getString("app.title104"));
                               else setProgressIndicator(res.getString("app.title93"));
                           }
                           hideProgressIndicator(true);
                       });

                       task.setOnFailed(event -> {
                           CalcLayer.closeLayer();
                           hideProgressBar(false);
                           setProgressIndicator(res.getString("app.title93"));
                           hideProgressIndicator(true);


                       });


                       Thread threadTask=new Thread(task);
                       threadTask.setDaemon(true);
                       setProgressBar(0.01, res.getString("app.title102"), "");




                       threadTask.start();
                       CalcLayer.showLayer();



                   }else if(buttonType.get() == updateBtnType)
                   {

    ////обновление


                       ForceCopyProfile fcp=new ForceCopyProfile();

                       Task<Boolean> task =new Task<Boolean>() {
                           @Override
                           protected Boolean call() throws Exception {
                               fcp.setFailHandler(failMessage -> failed());

                               boolean r=false;
                               try
                               {
                                   r= fcp.updateFiles(f,prof,nameP);
                               }catch (Exception e)
                               {
                                   logger.error("",e);
                               }
                               return r;


                           }
                       };




                       task.setOnScheduled(event1 -> setProgressIndicator(-1.0, res.getString("app.title102")));
                       task.setOnRunning(event1 -> setProgressIndicator(-1.0, res.getString("app.title101")));

                       task.setOnSucceeded(event ->
                       {
                           CalcLayer.closeLayer();
                           if (task.getValue())
                           {
                               setProgressIndicator(1.0, res.getString("app.title103"));

                           }
                           else {

                               if(fcp.isCancel()) setProgressIndicator(res.getString("app.title104"));
                               else setProgressIndicator(res.getString("app.title93"));
                           }

                           hideProgressIndicator(true);

                       });

                       task.setOnFailed(event -> {
                           CalcLayer.closeLayer();
                           setProgressIndicator(res.getString("app.title93"));
                           hideProgressIndicator(true);



                       });



                       Thread threadTask=new Thread(task);
                       threadTask.setDaemon(true);
                       setProgressIndicator(-1, res.getString("app.title102"));

                       CalcLayer.openLayer(() -> Platform.runLater(() ->
                       {
                           //отмена генерации. Что успело нагенериться то успело
                           //скрытие произойдет по окончании работы  ниже
                           CalcLayer.setInfoLayer(res.getString("app.title76"));

                           fcp.cancel();
                       }), getApp().getMainWindow(),false);





                       threadTask.start();
                       CalcLayer.showLayer();




                   }else return;


               }

           }else
           {
               //у нас другой профиль
               Optional<ButtonType> buttonType = showConfirmationDialog(res.getString("app.title87"), res.getString("app.title111") + " ( " + nameP + " ) " + res.getString("app.title112"), res.getString("app.title113"), getApp().getMainWindow(), Modality.WINDOW_MODAL);

               if(buttonType.isPresent())
               {
                   if (buttonType.get() == okButtonType)
                   {
                       //стирание профиля и запись заного
                       ForceCopyProfile fcp=new ForceCopyProfile();



                       Task<Boolean> task =new Task<Boolean>() {
                           @Override
                           protected Boolean call() throws Exception {
                               fcp.setUpdateHandler((progress, total) -> updateProgress(progress, total));
                               fcp.setFailHandler(failMessage -> failed());

                               boolean r=false;
                               try
                               {
                                   r= fcp.forceCopyProfile(f,false);
                               }catch (Exception e)
                               {
                                   logger.error("",e);
                               }
                               return r;

                           }
                       };




                       task.progressProperty().addListener((observable, oldValue, newValue) -> setProgressBar(newValue.doubleValue(),res.getString("app.title101"),""));
                       task.setOnRunning(event1 -> setProgressBar(0.0, res.getString("app.title102"),""));

                       task.setOnSucceeded(event ->
                       {
                          CalcLayer.closeLayer();
                           if (task.getValue()) {
                               hideProgressBar(false);
                               setProgressIndicator(1.0, res.getString("app.title103"));

                           } else {
                               hideProgressBar(false);
                              if(fcp.isCancel())setProgressIndicator(res.getString("app.title104"));
                              else setProgressIndicator(res.getString("app.title93"));
                           }
                           hideProgressIndicator(true);
                       });

                       task.setOnFailed(event -> {
                          CalcLayer.closeLayer();
                           hideProgressBar(false);
                           setProgressIndicator(res.getString("app.title93"));
                           hideProgressIndicator(true);


                       });


                       Thread threadTask=new Thread(task);
                       threadTask.setDaemon(true);
                       setProgressBar(0.01, res.getString("app.title102"), "");



                       CalcLayer.openLayer(() -> Platform.runLater(() ->
                       {
                           //отмена генерации. Что успело нагенериться то успело
                           //скрытие произойдет по окончании работы  ниже
                           CalcLayer.setInfoLayer(res.getString("app.title76"));

                           fcp.cancel();
                       }), getApp().getMainWindow(),false);

                       threadTask.start();

                       CalcLayer.showLayer();


                   }else return;

               }

           }



       }



   }
   */



    public void onLangChoose()
    {

        try {
            openDialog(getApp().getMainWindow(),"/fxml/language_options.fxml",res.getString("app.title114"),false,StageStyle.DECORATED,0,0,0,0);
        } catch (IOException e) {
            logger.error("",e);
        }

    }




    public void onPrintComplex()
    {
        if(tableComplex.getSelectionModel().getSelectedItem()==null)return;

        try {
            openDialog(getApp().getMainWindow(),"/fxml/PrintContent.fxml",res.getString("app.menu.print_complex"),true,StageStyle.DECORATED,0,0,0,0,tableComplex.getSelectionModel().getSelectedItem().getId(),0);


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


    public void onPrintProfile()
    {
        if(tableProfile.getSelectionModel().getSelectedItem()==null)return;

        try {
            openDialog(getApp().getMainWindow(),"/fxml/PrintContent.fxml",res.getString("app.menu.print_profile"),true,StageStyle.DECORATED,0,0,0,0,tableProfile.getSelectionModel().getSelectedItem().getId(),1);


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

       long cnt= tableProfile.getItems().stream().filter(itm->itm.getUuid().equals(up)).count();
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
                tableProfile.getItems().add(profile);

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

                btnGenerate.setDisable(false);
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
      if(profile!=null)  Platform.runLater(() -> tableProfile.getSelectionModel().select(pp));
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
                btnGenerate.setDisable(false);

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
            tableProfile.getItems().add(prf);
            tableProfile.getSelectionModel().select(prf);
            tableProfile.getSelectionModel().select(prf);
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





        task2.setOnRunning(event1 -> setProgressBar(0.0, res.getString("app.title102"), ""));

        task2.setOnSucceeded(event ->
        {
            Waiter.closeLayer();
            if (task2.getValue()) {
                hideProgressBar(false);
                setProgressIndicator(1.0, res.getString("app.title103"));

            } else {
                hideProgressBar(false);
               setProgressIndicator(res.getString("app.title93"));
            }
            hideProgressIndicator(true);
        });

        task2.setOnFailed(event -> {
            Waiter.closeLayer();
            hideProgressBar(false);
            setProgressIndicator(res.getString("app.title93"));
            hideProgressIndicator(true);


        });


        Thread threadTask=new Thread(task2);
        threadTask.setDaemon(true);
        setProgressBar(0.01, res.getString("app.title102"), "");


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
                Profile profile = tableProfile.getSelectionModel().getSelectedItem();
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
                        btnGenerate.setDisable(false);
                    }

                }


            }

        }catch (Exception e){ logger.error("",e);return false;}

        if(programms==null) return false;
        else return true;

    }


    /**
     * Вставка  комплекса в пользовательскую базу из папки
     * @param dir
     * @param treeItem выбранный элемент дерева - должен быть разделом или
     *                 @param createComplex создать комплекс и вставить в него програмы или просто вставить в раздел
     * @return
     */
    private boolean loadComplexToBase(File dir,NamedTreeItem treeItem,boolean createComplex)
    {
        Map<Long, ProgramFileData> programms= FilesProfileHelper.getProgrammsFromComplexDir(dir);

        try {
            if (programms!=null)
            {


                    String name = dir.getName();
                    int ind = name.indexOf('-');
                    if (ind != -1) name = name.substring(ind + 1);

                    ind = name.indexOf('(');
                    if (ind != -1) name = name.substring(0,ind);
                    name= name.trim();

                    if(programms.isEmpty())
                    {
                     return false;

                    }else
                    {

                        if(createComplex)
                        {
                            Complex complex = getModel().createComplex(name, "", (Section) treeItem.getValue(), false, getModel().getUserLanguage());
                            getModel().initStringsComplex(complex);

                            for (Map.Entry<Long, ProgramFileData> entry : programms.entrySet()) {

                                if(entry.getValue().isMp3())continue;//пропустим мп3
                                getModel().createProgram(entry.getValue().getName(), "", entry.getValue().getFreqs(), complex, false, getModel().getUserLanguage());

                            }


                            if (treeItem.isLeaf())treeItem.setLeafNode(false);//установим что он теперь не лист., автоматом подгрузятся дочернии, поэтому не надо ставлять их тут
                            else
                                treeItem.getChildren().add(new NamedTreeItem(complex));//добавим в дерево, если унас уже есть дочернии в ветке
                            if (!treeItem.isExpanded()) treeItem.setExpanded(true);
                            if (treeItem != null)
                                sectionTree.getSelectionModel().select(treeItem.getChildren().get(treeItem.getChildren().size() - 1));//выделим
                        }else
                        {


                            Program p;
                            for (Map.Entry<Long, ProgramFileData> entry : programms.entrySet())
                            {
                                if(entry.getValue().isMp3())continue;//пропустим мп3
                                 p =  getModel().createProgram(entry.getValue().getName(), "", entry.getValue().getFreqs(),
                                        (Section)treeItem.getValue(), false, getModel().getUserLanguage());

                                if (!treeItem.isLeaf())  {getModel().initStringsProgram(p); treeItem.getChildren().add(new NamedTreeItem(p));}

                            }
                            boolean isleaf=treeItem.isLeaf();
                            if (treeItem.isLeaf())treeItem.setLeafNode(false);
                            if (!treeItem.isExpanded()) treeItem.setExpanded(true);
                          //  if (treeItem != null && !isleaf) sectionTree.getSelectionModel().select(treeItem.getChildren().get(treeItem.getChildren().size() - 1));//выделим
                        }
                        // добавленный пункт
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





        task2.setOnRunning(event1 -> setProgressBar(0.0, res.getString("app.title102"), ""));

        task2.setOnSucceeded(event ->
        {
            Waiter.closeLayer();
            if (task2.getValue()) {
                hideProgressBar(false);
                setProgressIndicator(1.0, res.getString("app.title103"));

                Profile selectedItem = tableProfile.getSelectionModel().getSelectedItem();
                if(selectedItem!=null)   Platform.runLater(() -> {tableProfile.getSelectionModel().clearSelection();tableProfile.getSelectionModel().select(selectedItem);});


            } else {
                hideProgressBar(false);
                setProgressIndicator(res.getString("app.title93"));
            }
            hideProgressIndicator(true);

        });

        task2.setOnFailed(event -> {
            Waiter.closeLayer();
            hideProgressBar(false);
            setProgressIndicator(res.getString("app.title93"));
            hideProgressIndicator(true);


        });


        Thread threadTask=new Thread(task2);
        threadTask.setDaemon(true);
        setProgressBar(0.01, res.getString("app.title102"), "");

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



        NamedTreeItem treeSelected = (NamedTreeItem)sectionTree.getSelectionModel().getSelectedItem();
        if(treeSelected==null) return;
       if(!(treeSelected.getValue() instanceof Section)) return;



        Task<Boolean> task=null;
        boolean createComplex2=createComplex;

        //новый профиль
        task =new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {

                boolean r= loadComplexToBase(dir, treeSelected,createComplex2);
                if(r==false)failed();
                return r;
            }
        };



        Task<Boolean> task2=task;





        task2.setOnRunning(event1 -> setProgressBar(0.0, res.getString("app.title102"), ""));

        task2.setOnSucceeded(event ->
        {
            Waiter.closeLayer();
            if (task2.getValue()) {
                hideProgressBar(false);
                setProgressIndicator(1.0, res.getString("app.title103"));

                Profile selectedItem = tableProfile.getSelectionModel().getSelectedItem();
                if(selectedItem!=null)   Platform.runLater(() -> {tableProfile.getSelectionModel().clearSelection();tableProfile.getSelectionModel().select(selectedItem);});


            } else {
                hideProgressBar(false);
                setProgressIndicator(res.getString("app.title93"));
            }
            hideProgressIndicator(true);

        });

        task2.setOnFailed(event -> {
            Waiter.closeLayer();
            hideProgressBar(false);
            setProgressIndicator(res.getString("app.title93"));
            hideProgressIndicator(true);


        });


        Thread threadTask=new Thread(task2);
        threadTask.setDaemon(true);
        setProgressBar(0.01, res.getString("app.title102"), "");




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

    /**
     * Поиск по профилям
     */
    public void onSearchProfile()
    {
        //возврат ID выбранного профиля
        SearchProfile.Data res=null;
        try {
          res=  BaseController.openDialogUserData(getApp().getMainWindow(), "/fxml/Search_profile.fxml", this.res.getString("search_in_profile"), false, StageStyle.UTILITY, 0, 0, 0, 0, new SearchProfile.Data(0));
        } catch (IOException e) {
            logger.error("",e);
        }

        if(res!=null)
        {

           if(res.isChanged())
           {
               long resf=res.getNewVal();
               List<Profile> collect = tableProfile.getItems().stream().filter(profile -> profile.getId().longValue() == resf).collect(Collectors.toList());
               if(!collect.isEmpty())
               {

                   int i = tableProfile.getItems().indexOf(collect.get(0));
                   Platform.runLater(() -> {
                       therapyTabPane.getSelectionModel().select(0);//выберем таб с профилями
                       tableProfile.requestFocus();
                       tableProfile.getSelectionModel().select(i);
                       tableProfile.getFocusModel().focus(i);
                       tableProfile.scrollTo(i);
                   });
               }
           }
        }

    }

    /**
     * Добавить MP3 в выбранный комплекс
     */
    public void onAddMP3()
    {

        FileChooser fileChooser =new FileChooser();

        fileChooser.setTitle(res.getString("app.ui.program_by_mp3"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("mp3", "*.mp3"));
         fileChooser.setInitialDirectory(new File(getModel().getMp3Path(System.getProperty("user.home"))));

        List<File> files = fileChooser.showOpenMultipleDialog(getApp().getMainWindow());



        if(files==null)return;
        if(files.size()==0) return;
        getModel().setMp3Path(files.get(0).getParentFile());
        TherapyComplex complex = tableComplex.getSelectionModel().getSelectedItem();
        if(complex==null) return;

        try {

            List<TherapyProgram> tpl=new ArrayList<>();
            for (File mp3 : files)
            {
                tpl.add(getModel().createTherapyProgramMp3(complex, TextUtil.digitText(mp3.getName().substring(0,mp3.getName().length()-4)), "", mp3.getAbsolutePath())) ;

            }



          if(tpl.isEmpty()) return;
            tableProgram.getItems().addAll(tpl);
            int i = tableProgram.getItems().size()-1;
            tableProgram.scrollTo(i);

            updateComplexTime(tableComplex.getSelectionModel().getSelectedItem(), true);
            tableProfile.getSelectionModel().getSelectedItem().setProfileWeight(tableProfile.getSelectionModel().getSelectedItem().getProfileWeight()+1);





        } catch (Exception e) {
            logger.error("",e);
            showExceptionDialog("Ошибка создания программы", "","",e,getApp().getMainWindow(),Modality.WINDOW_MODAL);
        }
    }

    private void uploadComplexesToDir(){
        uploadComplexes(null);
    }
    private void uploadComplexesToM(){

        if(devicePath!=null){
            uploadComplexes(devicePath.toFile());
        }

    }

    private void uploadComplexes(File dst) {
       final ObservableList<TherapyComplex> selectedItems = this.tableComplex.getSelectionModel().getSelectedItems();

        if(!selectedItems.isEmpty()) {
            List<TherapyProgram> failedMp3 = new ArrayList<>();
            Iterator<TherapyComplex> iterator = selectedItems.iterator();

            TherapyComplex selectedItem;

            //проверка есть ли все mp3 файлы
            while(iterator.hasNext()) {
                 selectedItem = iterator.next();
                failedMp3.addAll(this.getModel().mp3ProgramInComplex(selectedItem).stream().filter((i) -> {
                    File ff = new File(i.getFrequencies());
                    return !ff.exists();
                }).collect(Collectors.toList()));
            }

            int cnt = failedMp3.size();
            if(cnt > 0)
            {
                //список файлов с не верными путями
                String dirChooser1 = failedMp3.stream().map(tt ->tt.getFrequencies()).collect(Collectors.joining("\n"));

                this.updateProfileTime(this.tableProfile.getSelectionModel().getSelectedItem());
                showWarningDialog(this.res.getString("app.title87"), dirChooser1, this.res.getString("app.ui.mp3_not_avaliable"), getApp().getMainWindow(), Modality.WINDOW_MODAL);
            } else
            {
                File dirT;
                if(dst==null ) {

                    //нужно найти есть ли в выбранной папке комплексы(соответствующие директории), если есть то дописать с верной нумерацией
                    DirectoryChooser dirChooser = new DirectoryChooser();
                    dirChooser.setInitialDirectory(new File(getModel().getLastSavedFilesPath(System.getProperty("user.home"))));
                    dirChooser.setTitle(this.res.getString("app.upload_to_dir"));
                    dirT = dirChooser.showDialog(getApp().getMainWindow());
                }else {
                    if(!dst.exists()){
                        DirectoryChooser dirChooser = new DirectoryChooser();
                        dirChooser.setInitialDirectory(new File(getModel().getLastSavedFilesPath(System.getProperty("user.home"))));
                        dirChooser.setTitle(this.res.getString("app.upload_to_dir"));
                        dirT = dirChooser.showDialog(getApp().getMainWindow());
                    }else dirT=dst;
                }
                final File dir=dirT;
                if(dir != null)
                {
                    if(!dir.exists()) {
                        showWarningDialog(res.getString("app.upload_to_dir"),  res.getString("app.ui.dir_not_exist"), "", getApp().getMainWindow(), Modality.WINDOW_MODAL);
                        return;}

                        getModel().setLastSavedFilesPath(dir.getParentFile());

                    try {
                        //максимальное значение нумерации комплексов в папке.
                         int e = 0;
                        OptionalInt max = Arrays.asList(dir.listFiles((i) -> {
                            return i.isDirectory() && !i.getName().equalsIgnoreCase("System Volume Information") && TextUtil.match(i.getName(), "^([0-9]+)-.*$");
                        })).stream().mapToInt(i -> Integer.parseInt(i.getName().split("-")[0])).max();
                        if(max.isPresent()) {
                            e = max.getAsInt();
                        }
                        final int ee=e;
                        Task task = new Task() {
                            protected Boolean call() throws Exception {
                                Map<Long,String> cMap=new LinkedHashMap<>();
                                Map<Long,Integer> cMap2=new LinkedHashMap<>();

                                TableColumn<TherapyComplex, ?> timeTableColumn = (TableColumn)AppController.this.tableComplex.getColumns().get(3);
                                selectedItems.forEach((itm) -> {
                                    cMap.put(itm.getId(), TextUtil.replaceWinPathBadSymbols(itm.getName()) + " (" + DateUtil.replaceTime(timeTableColumn.getCellObservableValue(itm).getValue().toString(),res) + ")");
                                    cMap2.put(itm.getId(), itm.getTimeForFrequency());
                                });
                                File bDir = dir;
                                int cnt = ee;
                                File tempFile = null;

                                try {
                                    Iterator<Map.Entry<Long, String>> it = cMap.entrySet().iterator();
                                    Map.Entry<Long, String> entry;
                                    while(it.hasNext()) {
                                         entry = it.next();
                                        StringBuilder strb = new StringBuilder();
                                        ++cnt;
                                        tempFile = new File(bDir, strb.append(cnt).append("-").append(entry.getValue()).toString());
                                        FilesProfileHelper.copyDir(tempFile);
                                        int count2 = 0;
                                        TherapyComplex therapyComplex;
                                        Iterator<TherapyProgram> it2 = AppController.this.getModel().findTherapyPrograms(entry.getKey()).iterator();
                                        TherapyProgram therapyProgram;



                                        while(it2.hasNext()) {
                                             therapyProgram = it2.next();
                                             therapyComplex = AppController.this.getModel().findTherapyComplex(entry.getKey());

                                            String mp3file;
                                            String timeP;

                                            StringBuilder strb2;

                                            if(!therapyProgram.isMp3()) {


                                                mp3file = DateUtil.convertSecondsToHMmSs(ProgramTable.calcTherapyProgramTime(therapyProgram));
                                                strb2 = new StringBuilder();
                                                ++count2;
                                                timeP = strb2.append(count2).append("-").append(TextUtil.replaceWinPathBadSymbols(therapyProgram.getName())).append(" (").append(DateUtil.replaceTime(mp3file,res)).append(")").toString();
                                                FilesProfileHelper.copyBSS(new File(BaseController.getApp().getDataDir(), therapyProgram.getId() + ".dat"), new File(tempFile, timeP + ".bss"));
                                                FilesProfileHelper.copyTxt(therapyProgram.getFrequencies(),
                                                        (cMap2.get(entry.getKey())).intValue(),
                                                        therapyProgram.getId().longValue(),
                                                        therapyProgram.getUuid(),
                                                        (entry.getKey()).longValue(),

                                                        therapyComplex.getBundlesLength(),
                                                        TextUtil.replaceWinPathBadSymbols(therapyProgram.getName()),
                                                        false,
                                                        new File(tempFile, timeP + ".txt"),
                                                        therapyProgram.isMultyFreq(),
                                                        therapyProgram.getSrcUUID(), therapyComplex.getSrcUUID());
                                            } else {
                                                mp3file = null;

                                                Mp3File mp3;
                                                try {
                                                    mp3 = new Mp3File(therapyProgram.getFrequencies());
                                                } catch (Exception var16) {
                                                    mp3 = null;
                                                }

                                                if(mp3 != null) {
                                                    timeP = DateUtil.convertSecondsToHMmSs(mp3.getLengthInSeconds());
                                                    strb2 = new StringBuilder();
                                                    ++count2;
                                                    String nameFile = strb2.append(count2).append("-").append(TextUtil.replaceWinPathBadSymbols(therapyProgram.getName())).append(" (").append(DateUtil.replaceTime(timeP,res)).append(")").toString();
                                                    FilesProfileHelper.copyBSS(new File(therapyProgram.getFrequencies()), new File(tempFile, nameFile + ".bss"));
                                                    FilesProfileHelper.copyTxt(therapyProgram.getFrequencies(),
                                                            (cMap2.get(entry.getKey())).intValue(),
                                                            therapyProgram.getId().longValue(),
                                                            therapyProgram.getUuid(),
                                                            (entry.getKey()).longValue(),

                                                            therapyComplex.getBundlesLength(),
                                                            TextUtil.replaceWinPathBadSymbols(therapyProgram.getName()),
                                                            true,
                                                            new File(tempFile, nameFile + ".txt"),
                                                            therapyProgram.isMultyFreq(),
                                                            therapyProgram.getSrcUUID(), therapyComplex.getSrcUUID());
                                                }
                                            }
                                        }
                                    }

                                    return true;
                                } catch (IOException ex1) {
                                    Log.logger.error("", ex1);
                                    Platform.runLater(() -> {
                                        BaseController.showWarningDialog(AppController.this.res.getString("app.title87"), "", AppController.this.res.getString("app.title115"), BaseController.getApp().getMainWindow(), Modality.WINDOW_MODAL);
                                    });
                                    return false;
                                } catch (Exception var18) {
                                    Log.logger.error("", var18);
                                    Platform.runLater(() -> {
                                        BaseController.showExceptionDialog(AppController.this.res.getString("app.title87"), "", AppController.this.res.getString("app.title93"), var18, BaseController.getApp().getMainWindow(), Modality.WINDOW_MODAL);
                                    });
                                    return false;
                                }
                            }
                        };
                        task.setOnScheduled((event) -> {
                            Waiter.openLayer(getApp().getMainWindow(), true);
                        });
                        task.setOnFailed(ev -> {
                            Waiter.closeLayer();
                            showErrorDialog("Ошибка записи файлов", "", "", getApp().getMainWindow(), Modality.WINDOW_MODAL);
                        });
                        task.setOnSucceeded(ev -> {
                            Waiter.closeLayer();
                            if(((Boolean)task.getValue()).booleanValue()) {
                                showInfoDialog(res.getString("app.ui.upload_compexes_in_dir"), "",res.getString("app.ui.upload_ok"), getApp().getMainWindow(), Modality.WINDOW_MODAL);
                            } else {
                                showErrorDialog("Ошибка записи файлов", "", "", getApp().getMainWindow(), Modality.WINDOW_MODAL);
                            }

                        });
                        Thread thread = new Thread(task);
                        thread.setDaemon(true);
                        thread.start();
                    } catch (Exception ex3) {
                        Log.logger.error("", ex3);
                        showExceptionDialog("Ошибка записи файлов", "", "", ex3, getApp().getMainWindow(), Modality.WINDOW_MODAL);
                    }
                }
            }
        }
    }

    private void generateComplexes() {
        ObservableList<TherapyComplex> selectedItems = this.tableComplex.getSelectionModel().getSelectedItems();
        if(!selectedItems.isEmpty())
        {
            int[] selectedIndexes = tableComplex.getSelectionModel().getSelectedIndices().stream().mapToInt(i -> i).toArray();
            ArrayList<TherapyComplex> toGenerate = new ArrayList<>();
            toGenerate.addAll(selectedItems.stream().filter((i) -> i.isChanged()?true:this.getModel().hasNeedGenerateProgramInComplex(i)).collect(Collectors.toList()));


            ArrayList<TherapyProgram> toGenProgramm = new ArrayList<>();

            try {
                Iterator<TherapyComplex> it = toGenerate.iterator();
                TherapyComplex nex;

                while(it.hasNext()) {
                    nex =it.next();
                    Iterator<TherapyProgram> it2 = this.getModel().findNeedGenerateList(nex).iterator();
                    TherapyProgram p;

                    while(it2.hasNext())
                    {
                        p =it2.next();
                        if(!p.isMp3())
                        {
                            toGenProgramm.add(p);
                            if(!p.isChanged())
                            {
                                p.setChanged(true);
                                this.getModel().updateTherapyProgram(p);
                            }
                        }
                    }

                    if(nex.isChanged()) {
                        nex.setChanged(false);
                        this.getModel().updateTherapyComplex(nex);
                    }
                }
            } catch (Exception e) {
                Log.logger.error("", e);
                showExceptionDialog(this.res.getString("app.title82"), "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);
            }

            if(!toGenProgramm.isEmpty()) {
                try {
                    MP3Encoder encoder = new MP3Encoder(toGenProgramm, MP3Encoder.CODEC_TYPE.EXTERNAL_CODEC, 44100);
                    Thread thread = new Thread(encoder);
                    CalcLayer.openLayer(() -> {
                        Platform.runLater(() -> {
                            CalcLayer.setInfoLayer(this.res.getString("app.title76"));
                            encoder.stopEncode();
                        });
                    }, getApp().getMainWindow(), false);
                    encoder.setActionListener((id, isCanceled) -> {
                        if(!isCanceled) {
                            TherapyProgram tp = this.getModel().getTherapyProgram(id);
                            tp.setChanged(false);
                            tp.setUuid(UUID.randomUUID().toString());

                            try {
                                this.getModel().updateTherapyProgram(tp);
                            } catch (Exception var6) {
                                Log.logger.error("", var6);
                            }

                            tp = null;
                        }

                    });
                    encoder.setOnScheduled(ev ->  this.setProgressBar(0.0D, this.res.getString("app.title83"), this.res.getString("app.title84")));

                    encoder.setOnFailed(event ->
                        Platform.runLater(() -> {
                            CalcLayer.closeLayer();
                            this.setProgressBar(100.0D, this.res.getString("app.error"), this.res.getString("app.title84"));
                            this.hideProgressBar(true);
                            encoder.removeActionListener();
                            tableComplex.requestFocus();
                            tableComplex.getSelectionModel().clearSelection();
                            for (int selectedIndex : selectedIndexes) {
                                this.tableComplex.getSelectionModel().select(selectedIndex);
                            }
                        }));
                    encoder.setOnSucceeded((event) -> {
                        Platform.runLater(() -> {
                            if(encoder.isStop()) {
                                CalcLayer.closeLayer();
                                this.setProgressBar(0.0D, this.res.getString("app.cancel"), this.res.getString("app.title84"));
                                this.hideProgressBar(true);
                                encoder.removeActionListener();
                            } else {
                                if(encoder.getValue().booleanValue()) {
                                    this.setProgressBar(100.0D, this.res.getString("app.title85"), this.res.getString("app.title84"));
                                } else {
                                    this.setProgressBar(100.0D, this.res.getString("app.cancel"), this.res.getString("app.title84"));
                                }

                                System.out.println("Манипуляция сгенерированными комплексами");
                                Profile profile = this.tableProfile.getSelectionModel().getSelectedItem();
                                this.tableProfile.getSelectionModel().clearSelection();
                                this.tableProfile.getSelectionModel().select(profile);
                                this.tableProfile.getSelectionModel().getSelectedItem().setProfileWeight(this.tableProfile.getSelectionModel().getSelectedItem().getProfileWeight() + 1);
                                this.checkUpploadBtn();


                                CalcLayer.closeLayer();
                                this.hideProgressBar(true);
                                encoder.removeActionListener();

                                Platform.runLater(() -> {
                                    tableComplex.requestFocus();
                                    tableComplex.getSelectionModel().clearSelection();
                                    for (int selectedIndex : selectedIndexes) {
                                        this.tableComplex.getSelectionModel().select(selectedIndex);
                                    }

                                });

                            }
                        });
                    });
                    encoder.progressProperty().addListener((observable, oldValue, newValue) -> {
                        Platform.runLater(() -> {
                            this.setProgressBar(newValue.doubleValue(), encoder.getCurrentName(), this.res.getString("app.title84"));
                        });
                    });
                    thread.start();
                    CalcLayer.showLayer();
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        tableComplex.requestFocus();
                        tableComplex.getSelectionModel().clearSelection();
                        for (int selectedIndex : selectedIndexes) {
                            this.tableComplex.getSelectionModel().select(selectedIndex);
                        }

                    });
                    Log.logger.error("", e);
                    showExceptionDialog(this.res.getString("app.title86"), "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);
                }


            }
        }
    }

 public void    onLanguageInsertComplexOption(){



     try {
         openDialog(getApp().getMainWindow(),"/fxml/language_insert_complex_option_dlg.fxml",res.getString("app.menu.insert_language"),false,StageStyle.DECORATED,0,0,0,0);
     } catch (IOException e) {
         logger.error("",e);
     }
 }

    /**
     * Абривиатура языка вставки комплекса
     * @return Пустое значение, если неудачно
     */
   public  String getInsertComplexLang(){
        try {
            return  getModel().getOption("app.lang_insert_complex");
        } catch (Exception e) {
            logger.error("Ошибка получения языка вставки комплекса",e);
        }
       return "";
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



        List<Section> collect = baseCombo.getItems().stream().filter(section -> "USER".equals(section.getTag())).collect(Collectors.toList());
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

        setProgressBar(0.0, res.getString("ui.backup.create_backup"), "");


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
            setProgressBar(newValue.doubleValue(), res.getString("ui.backup.create_backup"), "");
        });


        task.setOnRunning(event1 -> setProgressBar(0.0, res.getString("ui.backup.create_backup"), ""));

        task.setOnSucceeded(event ->
        {
            Waiter.closeLayer();
            if (task.getValue()) {
                hideProgressBar(false);
                setProgressIndicator(1.0, res.getString("app.title103"));

            } else {
                hideProgressBar(false);
                setProgressIndicator(res.getString("app.title93"));
            }
            hideProgressIndicator(true);


        });

        task.setOnFailed(event -> {
            Waiter.closeLayer();
            hideProgressBar(false);
            setProgressIndicator(res.getString("app.title93"));
            hideProgressIndicator(true);


        });


        Thread threadTask=new Thread(task);
        threadTask.setDaemon(true);
        setProgressBar(0.01, res.getString("app.title102"), "");


        Waiter.openLayer(getApp().getMainWindow(),false);

        threadTask.start();
        Waiter.show();





    }




    /**
     * Загрузить бекап
     *
     */
    public void onRecoveryLoad(){


        List<Section> collect = baseCombo.getItems().stream().filter(section -> "USER".equals(section.getTag())).collect(Collectors.toList());
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

        setProgressBar(0.0, res.getString("ui.backup.load_backup"), "");



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
                        tableProfile.getItems().clear();
                        //выбрать раздел 0 главный раздел
                        baseCombo.getSelectionModel().select(0);
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
            setProgressBar(newValue.doubleValue(), res.getString("ui.backup.load_backup"), "");
        });


        task.setOnRunning(event1 -> setProgressBar(0.0, res.getString("ui.backup.load_backup"), ""));


        int count_profiles=replaceMode==true?0:getModel().countProfile();

        task.setOnSucceeded(event ->
        {
            Waiter.closeLayer();
            if (task.getValue()) {
                hideProgressBar(false);
                setProgressIndicator(1.0, res.getString("app.title103"));
                if(getModel().countProfile()!=0)
                {
                    if(count_profiles==0){
                        tableProfile.getItems().addAll(getModel().findAllProfiles().stream()
                                                                 .filter(i->!i.getName().equals(App.BIOFON_PROFILE_NAME))
                        .collect(Collectors.toList()));


                    }
                    else if(count_profiles<getModel().countProfile()){
                        tableProfile.getItems().addAll( getModel().findAllProfiles().subList(count_profiles,getModel().countProfile()).stream()
                                                                  .filter(i->!i.getName().equals(App.BIOFON_PROFILE_NAME))
                                                                  .collect(Collectors.toList()));
                    }

                    biofonUIUtil.reloadComplexes();


                    int i = tableProfile.getItems().size()-1;
                    tableProfile.requestFocus();
                    tableProfile.getSelectionModel().select(i);
                    tableProfile.scrollTo(i);
                    tableProfile.getFocusModel().focus(i);

                    baseCombo.getSelectionModel().select(0);
                    baseCombo.getSelectionModel().select(sec);

                }


            } else {
                hideProgressBar(false);
                setProgressIndicator(res.getString("app.title93"));
            }
            hideProgressIndicator(true);


        });

        task.setOnFailed(event -> {
            Waiter.closeLayer();
            hideProgressBar(false);
            setProgressIndicator(res.getString("app.title93"));
            hideProgressIndicator(true);


        });


        Thread threadTask=new Thread(task);
        threadTask.setDaemon(true);
        setProgressBar(0.01, res.getString("ui.backup.load_backup"), "");


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

    @FXML private Button searchReturnBtnPrograms;
    @FXML private Button searchBtnProgram;

    @FXML private TextField  nameProgramSearch;
    @FXML private TextField freqProgramSearch;
    private SimpleBooleanProperty programSearch =new SimpleBooleanProperty(false);

    private void cleanSearchedProgram(){
        TherapyProgram tp;
        for(int i=0;i< tableProgram.getItems().size();i++) {
            tp = tableProgram.getItems().get(i);
            if(tp.isMatchedFreqs() || tp.isMatchedAnyName()){
                tp.cleanSearch();
                tableProgram.getItems().set(i,null);
                tableProgram.getItems().set(i,tp);
            }
        }
    }
    private void initProgramSearch(){

        searchReturnBtnPrograms.disableProperty().bind(programSearch.not());
        searchBtnProgram.disableProperty().bind(nameProgramSearch.textProperty().isEmpty().and(freqProgramSearch.textProperty().isEmpty()));
        programSearch.bind(nameProgramSearch.textProperty().isNotEmpty().or(freqProgramSearch.textProperty().isNotEmpty()));

        programSearch.addListener((observable, oldValue, newValue) -> {
           if(newValue==false)cleanSearchedProgram();
        });

        Predicate<String> characterFilter= c-> TextUtil.match(c,"[0-9. ]");


        freqProgramSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            //3.6 + 4 .9;7;.0;46.0;70.0  -;70.5;;;++72.5;95.0
            String val=
                     newValue.replaceAll("[^0-9\\.;\\+ ]","")
                    .replace(";"," ").replace("+"," ")
                    .replaceAll("\\s+"," ")
                            .replaceAll("\\s+\\.",".");
            if(!val.equals(newValue)) freqProgramSearch.setText(val);

        });
        freqProgramSearch.addEventFilter(KeyEvent.KEY_TYPED, event ->
        {
            if(!characterFilter.test( event.getCharacter())) {event.consume();}
        });
        nameProgramSearch.setOnAction(event ->onSearchProgram());
        freqProgramSearch.setOnAction(event ->onSearchProgram());
    }
    /*
    Поиск по тер. программам в таблице
     */


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
    /**
     * Сброс режима поиска
     * programSearch зависит от содержимого полей поиска
     */
    public void onSearchReturnPrograms(){
        nameProgramSearch.setText("");
        freqProgramSearch.setText("");
        tableProgram.getSelectionModel().clearSelection();
        tableProgram.scrollTo(0);
    }

    public void onSearchProgram(){

        String name = nameProgramSearch.getText().trim();
        String freqs = freqProgramSearch.getText().trim();
        if(name.length()<=2 && freqs.length()==0) { showInfoDialog(res.getString("app.search"),res.getString("app.search_1"),"",getApp().getMainWindow(),Modality.WINDOW_MODAL);return;}

        tableProgram.getSelectionModel().clearSelection();

        freqs=freqs.replaceAll("\\s+"," ").replaceAll("\\s+\\.",".");//исключим посторонние дырки в паттерне
        freqProgramSearch.setText(freqs);

        cleanSearchedProgram();

        boolean first=true;

        TherapyProgram tp;
        for(int i=0;i< tableProgram.getItems().size();i++){
            tp = tableProgram.getItems().get(i);
            boolean searchRes=false;
            if(!name.isEmpty() && freqs.isEmpty())searchRes = tp.searchNames(name);
            else if(!name.isEmpty() && !freqs.isEmpty()) {

                searchRes = tp.searchFreqs(freqs) & tp.searchNames(name);
                if(!searchRes){
                    if(tp.isMatchedAnyName())tp.cleanSearch();
                    else if(tp.isMatchedFreqs())tp.cleanSearch();
                }

            }
            else if(name.isEmpty() && !freqs.isEmpty()) searchRes = tp.searchFreqs(freqs);

            if(searchRes){
                tableProgram.getItems().set(i,null);
                tableProgram.getItems().set(i,tp);

                if(first){
                    tableProgram.scrollTo(i);
                    first=false;
                }
            }

        }




        if(!first){

            for(int i=0;i< tableProgram.getItems().size();i++) {
                tp = tableProgram.getItems().get(i);

                boolean searchRes=false;

                if(!name.isEmpty() && freqs.isEmpty())  searchRes = tp.isMatchedAnyName();
                else if(!name.isEmpty() && !freqs.isEmpty())searchRes = tp.isMatchedFreqs() && tp.isMatchedAnyName();
                else if(name.isEmpty() && !freqs.isEmpty())searchRes = tp.isMatchedFreqs();

                if(searchRes)tableProgram.getSelectionModel().select(i);
            }

            tableProgram.requestFocus();
        }else  {
            showInfoDialog(res.getString("app.search_res"),res.getString("app.search_res_1"),"",
                    getApp().getMainWindow(),Modality.WINDOW_MODAL);
            return;

        }



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
