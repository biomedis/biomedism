package ru.biomedis.biomedismair3;

import com.mpatric.mp3agic.Mp3File;
import com.sun.javafx.scene.control.skin.TableViewSkin;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.*;
import javafx.util.Duration;
import javafx.util.StringConverter;
import ru.biomedis.biomedismair3.CellFactories.TextAreaTableCell;
import ru.biomedis.biomedismair3.Converters.SectionConverter;
import ru.biomedis.biomedismair3.DBImport.NewDBImport;
import ru.biomedis.biomedismair3.Dialogs.NameDescroptionDialogController;
import ru.biomedis.biomedismair3.Dialogs.ProgramDialogController;
import ru.biomedis.biomedismair3.Dialogs.SearchProfile;
import ru.biomedis.biomedismair3.Dialogs.TextInputValidationController;
import ru.biomedis.biomedismair3.UpdateUtils.FrequenciesBase.CreateFrequenciesFile;
import ru.biomedis.biomedismair3.UpdateUtils.FrequenciesBase.CreateLanguageFiles;
import ru.biomedis.biomedismair3.UpdateUtils.FrequenciesBase.LoadFrequenciesFile;
import ru.biomedis.biomedismair3.UpdateUtils.FrequenciesBase.LoadLanguageFiles;
import ru.biomedis.biomedismair3.UserUtils.Export.ExportProfile;
import ru.biomedis.biomedismair3.UserUtils.Export.ExportTherapyComplex;
import ru.biomedis.biomedismair3.UserUtils.Export.ExportUserBase;
import ru.biomedis.biomedismair3.UserUtils.Import.ImportProfile;
import ru.biomedis.biomedismair3.UserUtils.Import.ImportTherapyComplex;
import ru.biomedis.biomedismair3.UserUtils.Import.ImportUserBase;
import ru.biomedis.biomedismair3.entity.*;
import ru.biomedis.biomedismair3.utils.Audio.MP3Encoder;
import ru.biomedis.biomedismair3.utils.Date.DateUtil;
import ru.biomedis.biomedismair3.utils.Disk.DiskDetector;
import ru.biomedis.biomedismair3.utils.Disk.DiskSpaceData;
import ru.biomedis.biomedismair3.utils.Files.*;
import ru.biomedis.biomedismair3.utils.OS.OSValidator;
import ru.biomedis.biomedismair3.utils.Text.TextUtil;

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
import java.util.stream.Stream;

import static ru.biomedis.biomedismair3.Log.logger;

public class AppController  extends BaseController {
    

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

    @FXML private Button  btnCreateTherapy;
    @FXML private Button btnDeleteTherapy;

    @FXML private Button  btnDeleteProgram;
    @FXML private Button  btnUpProgram;
    @FXML private Button  btnDownProgram;
    @FXML private Spinner<Integer> timeToFreqSpinner;

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


    @FXML
    private Tab tab1;
    @FXML
    private Tab tab2;
    @FXML
    private Tab tab3;

    @FXML
    private Tab tab4;



    @FXML private HBox bundlesPan;
    @FXML private Spinner<String> bundlesSpinner;

    @FXML  private ObservableList<String>  bundlesSpinnerData;
    @FXML  private VBox bundlesBtnPan;
    @FXML private Button btnOkBundles;
    @FXML private Button btnCancelBundles;


    @FXML private HBox onameBoxProgram;
    @FXML private HBox onameBoxComplex;

    @FXML private Label onameProgram;
    @FXML private Label onameComplex;
    @FXML private Menu updateBaseMenu;
    @FXML private Button searchReturn;

    private TableViewSkin<?> tableSkin;
    private VirtualFlow<?> virtualFlow;

    private ContextMenu deleteMenu=new ContextMenu();
    private Path devicePath=null;//путь ку устройству или NULL если что-то не так
    private String fsDeviceName="";

    private ContextMenu searchMenu=new ContextMenu();
    private SearchState searchState=new SearchState();

    private ContextMenu  programmMenu=new ContextMenu();
    private ContextMenu  complexesMenu=new ContextMenu();
    private ContextMenu uploadMenu=new ContextMenu();
    private  MenuItem btnUpload;
    MenuItem btnUploadDir;

    private SimpleBooleanProperty connectedDevice =new SimpleBooleanProperty(false);//подключено ли устройство


    private Image imageDone;
    private Image imageCancel;

    private Image imageDeviceOff;
    private Image imageDeviceOn;
    private List<Section> allRootSection;// разделы старая и новая база

    //экстрактор для событий обновления комбобокса
    //private Callback<Section ,Observable [] > extractor = param -> new Observable[]{param.nameStringProperty(),param.desriptionStringProperty()};
    private List<Section> sectionsBase=new ArrayList<>();//основные разделы

    private  ResourceBundle res;
    private Tooltip diskSpaceTooltip=new Tooltip();
    private Tooltip searchTooltip=new Tooltip();


    private NamedTreeItem rootItem=new NamedTreeItem();//корень дерева разделов(всегда есть, мы в нем мменяем только дочерние элементы)
    private boolean stopGCthread=false;

    private final DataFormat PROGRAM_DRAG_ITEM=new DataFormat("biomedis/programitem");
    private final DataFormat PROGRAM_CUT_ITEM=new DataFormat("biomedis/cut_programitem");

    private DropShadow borderGlow;

    //создаем коллекцию с экстрактором, чтобы получать уведомления об изменениях
    private final ObservableList<TherapyComplex> therapyComplexItems =  FXCollections.observableArrayList(param ->  new Observable[]{param.mulltyFreqProperty()});

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
    private Thread gcThreadRunner =new Thread(() ->
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
     * Установка текста над табл комплексов
     * @param name
     */
    private void setOnameComplex(String name){
        onameComplex.setText(name);
    }
    /**
     * Установка текста над табл программ
     * @param name
     */
    private void setOnameProgram(String name){
        onameProgram.setText(name);
    }
    /**
     * Статус поиска по базе
     * Раздел открытый до поиска чиатается из ComboBox тк любое его изменение отменит поиск
     */
    class SearchState
    {
       private   boolean search=false;
        private String searchText="";

        private ReadOnlyBooleanWrapper searched=new ReadOnlyBooleanWrapper(false);




        public ReadOnlyBooleanProperty searchedProperty() {
            return searched.getReadOnlyProperty();
        }



        public boolean isSearch() {
            return this.search;
        }

        public void setSearch(boolean search) {
            this.search = search;
            searched.set(search);
        }

        public String getSearchText() {
            return this.searchText;
        }

        public void setSearchText(String searchText) {
            this.searchText = searchText;
        }

        public void clear(){

           setSearch(false);
           setSearchText("");

        }
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
        double summ =
                ((splitOuter.getItems().get(0) instanceof Parent )? ((Parent)splitOuter.getItems().get(0)).minWidth(-1):  splitOuter.getItems().get(0).getBoundsInParent().getWidth())+ ((splitOuter.getItems().get(1) instanceof Parent )? ((Parent)splitOuter.getItems().get(1)).minWidth(-1):  splitOuter.getItems().get(1).getBoundsInParent().getWidth());

        summ+=15;
        double w=  splitOuter.getWidth();
        SplitPane.Divider divider1 = splitOuter.getDividers().get(0);
        if(divider1.getPosition()<=summ/w) divider1.setPosition(summ / w);





    }
    private String baseComplexTabName;
    private String baseProgramTabName;
    private String baseProfileTabName;



    @Override
    public void initialize(URL url, ResourceBundle rb) {

        res=rb;
        baseProfileTabName=res.getString("app.ui.tab1");
        baseComplexTabName=res.getString("app.ui.tab2");
        baseProgramTabName=res.getString("app.ui.tab3");


        searchReturn.setDisable(true);
        searchReturn.disableProperty().bind(searchState.searchedProperty().not());

        dataPathMenuItem.setVisible(OSValidator.isWindows());//видимость пункта меню для введения пути к папки данных, только на винде!

        //if (!searchState.isSearch()) smi4.setDisable(true);
        //else smi4.setDisable(false);

        updateBaseMenu.setVisible(getApp().isUpdateBaseMenuVisible());


        onameComplex.textProperty().bind(new StringBinding() {
            {
                bind(tableComplex.getSelectionModel().selectedItemProperty());
            }
            @Override
            protected String computeValue() {
               if (tableComplex.getSelectionModel().getSelectedItem()==null) return "";
                return tableComplex.getSelectionModel().getSelectedItem().getOname();
            }
        });

        onameProgram.textProperty().bind(new StringBinding() {
            {
                bind(tableProgram.getSelectionModel().selectedItemProperty());
            }
            @Override
            protected String computeValue() {
                if (tableProgram.getSelectionModel().getSelectedItem()==null) return "";
                return tableProgram.getSelectionModel().getSelectedItem().getOname();
            }
        });


        //настройка подписей в табах
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

        /*
        tabs.get(1).textProperty().bind(new StringBinding() {
            {
                //указывается через , список свойств изменения которых приведут к срабатыванию этого
                super.bind(tableComplex.getSelectionModel().selectedItemProperty());
            }
            @Override
            protected String computeValue() {
                TherapyComplex selected = tableComplex.getSelectionModel().getSelectedItem();
                if(selected !=null)
                {
                    String s = DateUtil.convertSecondsToHMmSs(AppController.this.getModel().getTimeTherapyComplex(selected));
                    return  baseComplexTabName+" ("+ selected.getName() +") +("+s+")";


                }else{

                    return baseComplexTabName;
                }
            }
        });
*/
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




        /** Контекстное меню загрузки в прибор **/

        btnUploadDir=new MenuItem(res.getString("app.upload_to_dir"));
        btnUpload=new MenuItem(res.getString("app.uppload"));
        btnUpload.setOnAction(event -> onUploadProfile());
        btnUploadDir.setOnAction(event -> uploadInDir());
        uploadMenu.getItems().addAll(btnUploadDir,btnUpload);
        btnUploadm.setOnAction(event4 ->
        {
            if(!uploadMenu.isShowing()) uploadMenu.show(btnUploadm, Side.BOTTOM, 0, 0);
            else uploadMenu.hide();

        }
        );

        /*********/

        menuDelGeneratedFiles.disableProperty().bind(tableProfile.getSelectionModel().selectedItemProperty().isNull());
        btnUpload.setDisable(true);
        btnUploadDir.setDisable(true);
        btnGenerate.setDisable(true);


        printProfileMenu.disableProperty().bind(tableProfile.getSelectionModel().selectedItemProperty().isNull());
        printComplexMenu.disableProperty().bind(tableComplex.getSelectionModel().selectedItemProperty().isNull());
        menuImportComplex.disableProperty().bind(tableProfile.getSelectionModel().selectedItemProperty().isNull());
        //меню импорта комплекса из папки в пользовательскую базу

        //splitOuter.setStyle("-fx-box-border: transparent;");
        Platform.runLater(() -> balanceSpitterDividers());

        //будем подстраивать  dividers при изменении размеров контейнера таблиц, при движ ползунков это не работает, при изм размеров окна срабатывает
        splitOuter.widthProperty().addListener((observableValue, oldSceneHeight, newSceneHeight) -> balanceSpitterDividers());



        //btnGenerate.disableProperty().addListener((observable, oldValue, newValue) -> checkUpploadBtn());

/*
        btnGenerate.disableProperty().bind(new BooleanBinding()
        {
            {
                super.bind(tableComplex.itemsProperty());
            }
            @Override
            protected boolean computeValue()
            {
                return false;
            }
        });
*/

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





        /** Конопки меню верхнего ***/
        menuExportProfile.disableProperty().bind(tableProfile.getSelectionModel().selectedItemProperty().isNull());
        menuExportTherapyComplex.disableProperty().bind(tableComplex.getSelectionModel().selectedItemProperty().isNull());
        menuImportTherapyComplex.disableProperty().bind(tableProfile.getSelectionModel().selectedItemProperty().isNull());
        /***************/

/** Спиннер внемя на частоту **/

        timeToFreqSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 1000, 180, 10));
        timeToFreqSpinner.setEditable(true);
        spinnerPan.setVisible(false);
        spinnerBtnPan.setVisible(false);


        URL okUrl = getClass().getResource("/images/ok.png");
        URL cancelUrl = getClass().getResource("/images/cancel.png");
        btnOkSpinner.setGraphic(new ImageView(new Image(okUrl.toExternalForm())));
        btnCancelSpinner.setGraphic(new ImageView(new Image(cancelUrl.toExternalForm())));

/*******************/

        /** Спиннер пачек частот **/


        bundlesPan.setVisible(false);
        bundlesBtnPan.setVisible(false);

        btnOkBundles.setGraphic(new ImageView(new Image(okUrl.toExternalForm())));
        btnCancelBundles.setGraphic(new ImageView(new Image(cancelUrl.toExternalForm())));



/*******************/


        progress1Pane.setVisible(false);
        progress2Pane.setVisible(false);
        progress3Pane.setVisible(false);

        gcThreadRunner.setDaemon(true);
        gcThreadRunner.start();

        userActionPane.setDisable(true);
        diskSpaceBar.setVisible(false);
        diskSpaceBar.setTooltip(diskSpaceTooltip);
        hackTooltipStartTiming(diskSpaceTooltip, 250, 15000);




/**** Контекстное меню поиска **/

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





/*******/

/***************** Удаление из дерева ************************/
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


        /****/


        //btnDownload.setText(rb.getString("ui.main.load_from_device"));
        borderGlow= new DropShadow();
        borderGlow.setOffsetY(0f);
        borderGlow.setOffsetX(0f);
        borderGlow.setColor(Color.GREEN);
        borderGlow.setWidth(20);
        borderGlow.setHeight(20);

        URL location = getClass().getResource("/images/done.png");
        imageDone=new Image(location.toExternalForm());
         location = getClass().getResource("/images/cancel.png");
        imageCancel=new Image(location.toExternalForm());

        //при закрытии приложения мы закроем сервис детектирования диска
        getApp().addCloseApplistener(()->{setStopGCthread(); DiskDetector.stopDetectingService();} );

        location = getClass().getResource("/images/DeviceOff.png");
        imageDeviceOff=new Image(location.toExternalForm());

        location = getClass().getResource("/images/DeviceOn.png");
        imageDeviceOn=new Image(location.toExternalForm());
        deviceIcon.setImage(imageDeviceOff);
        deviceIcon.setEffect(null);
        try {
            DiskDetector.waitForDeviceNotifying(4,getModel().getOption("device.disk.mark"),(state,fs)->{

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


//заполнение комбобокс
        allRootSection = getModel().findAllRootSection();// разделы разных баз(старая и новая)
        getModel().initStringsSection(allRootSection);
        baseCombo.setConverter(new SectionConverter(getModel().getProgramLanguage().getAbbr()));
        baseCombo.getItems().addAll(allRootSection);
        baseCombo.setVisibleRowCount(5);


        sectionCombo.setConverter(new SectionConverter(getModel().getProgramLanguage().getAbbr()));//конвертер секции в строку
        sectionCombo.setVisibleRowCount(10);

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

            sectionsBase.clear();
            sectionsBase.add(new Section());//пустой элемент вставим для выбора он с ID =0
            sectionsBase.addAll(getModel().findAllSectionByParent(baseCombo.getSelectionModel().getSelectedItem()));
            getModel().initStringsSection(sectionsBase);
            //очистка и заполение комбобокса разделов 2 уровня согласно выбранному 1 разделу
            sectionCombo.getItems().clear();
            sectionCombo.getItems().addAll(sectionsBase);
            rootItem.setValue(null);
            sectionCombo.getSelectionModel().select(0);//автоматически очистит дерево, тк сработает sectionCombo.setOnAction(event....

        });

        //откроем первую базу
        baseCombo.getSelectionModel().select(0);
        baseCombo.fireEvent(new ActionEvent());//создадим эвент для baseCombo.setOnAction и заполним комбобок тем самым



        programInfo.setEditable(false);
        programInfo.setWrapText(true);
        programDescription.setEditable(false);
        programDescription.setWrapText(true);

//настроим дерево разделов
        sectionTree.setShowRoot(false);
        sectionTree.setRoot(rootItem);
        rootItem.setExpanded(true);



        sectionTree.setOnMouseClicked(event -> {




            //по одиночному клику

            TreeItem<INamed> selectedItem = sectionTree.getSelectionModel().getSelectedItem();


            if (selectedItem == null) return;
            if (selectedItem.getValue() instanceof Program)
            {
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
            } else  if (selectedItem.getValue() instanceof Section)
            {
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
            } else  if (selectedItem.getValue() instanceof Complex)
            {
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



            if (event.getClickCount() == 2)
            {
                int tabSelectedIndex = therapyTabPane.getSelectionModel().getSelectedIndex();
                //перенос программы в текущий комплекс  в такблицу справа.
                if (selectedItem.getValue() instanceof Program)
                {


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
                        oname=p.getNameString();
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
                            TherapyProgram therapyProgram = getModel().createTherapyProgram(selectedTCBiofon, name, descr, p.getFrequencies(),oname);
                            addProgramToBiofonTab(selectedTCBiofon,therapyProgram);
                        } catch (Exception e) {
                            logger.error("",e);
                            showExceptionDialog("Ошибка создания терапевтической программы", "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);

                        }



                    }else
                    if (tableComplex.getSelectionModel().getSelectedItem() != null) {
                        //если выбран комплекс в таблице комплексов

                        try {


                            TherapyProgram therapyProgram = getModel().createTherapyProgram(tableComplex.getSelectionModel().getSelectedItem(), name, descr, p.getFrequencies(),oname);
                            tableProgram.getItems().add(therapyProgram);
                            updateComplexTime(tableComplex.getSelectionModel().getSelectedItem(), false);
                            therapyTabPane.getSelectionModel().select(2);//выберем таб с программами


                            Platform.runLater(() -> {
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

                } else if (selectedItem.getValue() instanceof Complex) {

                    //если выбран биофон вкладка
                    if(tabSelectedIndex==3){
                        //добавляется комплекс в биофон
                        Complex c = (Complex) selectedItem.getValue();
                        try {
                            TherapyComplex th = getModel().createTherapyComplex(getApp().getBiofonProfile(), c, 180, true,1,getInsertComplexLang());

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
                            TherapyComplex th = getModel().createTherapyComplex(tableProfile.getSelectionModel().getSelectedItem(), c, 300, true,1,getInsertComplexLang());

                            //therapyComplexItems.clear();
                            //therapyComplexItems содержит отслеживаемый список, элементы которого добавляются в таблицу. Его не нужно очищать
                            therapyComplexItems.add(th);
                            tableComplex.getItems().addAll(therapyComplexItems.get(therapyComplexItems.size()-1));
                            therapyTabPane.getSelectionModel().select(2);//выберем таб с программами
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
                }

        });

        sectionTree.setCellFactory(param -> new TreeCell<INamed>() {
            @Override
            protected void updateItem(INamed item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    this.setText(null);
                    this.setGraphic(null);
                } else {
                    if (getTreeItem().getValue() == null) {
                        this.setText(null);
                        this.setGraphic(null);
                        return;
                    }
                    this.setText(getTreeItem().getValue().getNameString());//имя из названия INamed
                    this.setGraphic(getTreeItem().getGraphic());//иконку мы устанавливали для элементов в NamedTreeItem согласно типу содержимого
                }
            }

        });



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

initTables();
initBiofon();


        /** кнопки  таблиц **/

             btnDeleteProfile.disableProperty().bind(tableProfile.getSelectionModel().selectedItemProperty().isNull());
             btnCreateTherapy.disableProperty().bind(tableProfile.getSelectionModel().selectedItemProperty().isNull());
             btnDeleteTherapy.disableProperty().bind(tableComplex.getSelectionModel().selectedItemProperty().isNull());

           spinnerPan.visibleProperty().bind(tableComplex.getSelectionModel().selectedItemProperty().isNotNull());
           bundlesPan.visibleProperty().bind(tableComplex.getSelectionModel().selectedItemProperty().isNotNull());


         btnDeleteProgram.disableProperty().bind(tableProgram.getSelectionModel().selectedItemProperty().isNull());
         btnUpProgram.disableProperty().bind(tableProgram.getSelectionModel().selectedItemProperty().isNull().or(tableProgram.getSelectionModel().selectedIndexProperty().isEqualTo(0)));
        btnDownProgram.disableProperty().bind(new BooleanBinding() {
            {
                //заставит этот биндинг обновляться при изменении свойства selectedIndexProperty
              super.bind(tableProgram.getSelectionModel().selectedIndexProperty());

            }
             @Override
             protected boolean computeValue()
             {

                 if(tableProgram.getSelectionModel().getSelectedItem()==null) return true;

                if( tableProgram.getSelectionModel().getSelectedIndex() == tableProgram.getItems().size()-1) return true;
                 else return false;
             }
         });

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
        /********************/
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

    @FXML  private ObservableList<String>  bundlesSpinnerDataBiofon;

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





        biofonPrintMi.setText(" Печать комплексов");


        biofonImportMi.setText(" Импорт комплексов");

        biofonExportMi.setText(" Экспорт комплексов");




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
        bundlesSpinnerBiofon.getValueFactory().setValue(val==1 ? "-" : String.valueOf(val));
        bundlesBtnPanBiofon.setVisible(false);

    }

    private void hideBundlesSpinnerBTNPanBiofon()
    {

        bundlesBtnPanBiofon.setVisible(false);

    }



    private void initBiofonSpinners(){






        /** Спиннер внемя на частоту **/

        timeToFreqSpinnerBiofon.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 1000, 180, 10));
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
        btnCancelSpinnerBiofon.setOnAction(event ->hideTFSpinnerBTNPanBiofon(tableComplex.getSelectionModel().getSelectedItem().getTimeForFrequency()) );
        //принять изменения времени
        btnOkSpinnerBiofon.setOnAction(event ->
        {

            ObservableList<TherapyComplex> selectedComplex = biofonCompexesList.getSelectionModel().getSelectedItems();
            if(!biofonCompexesList.getSelectionModel().getSelectedItems().isEmpty()) {
                List<TherapyComplex> items = new ArrayList<>(selectedComplex);

                try {


                    for(TherapyComplex item:items) {


                        item.setTimeForFrequency(this.timeToFreqSpinnerBiofon.getValue());
                        this.getModel().updateTherapyComplex(item);

                    }


                } catch (Exception var8) {
                    this.hideTFSpinnerBTNPanBiofon(biofonCompexesList.getSelectionModel().getSelectedItem().getTimeForFrequency().intValue());
                    Log.logger.error("", var8);
                    showExceptionDialog("Ошибка обновления времени на частоту в терапевтическом комплексе", "", "", var8, getApp().getMainWindow(), Modality.WINDOW_MODAL);
                } finally {
                    this.hideTFSpinnerBTNPanBiofon();
                }

            }
        });





/*******************/

        /** Комбо пачек частот **/



        bundlesSpinnerDataBiofon.add("-");
        for(int i=2; i<20; i++)bundlesSpinnerDataBiofon.add(String.valueOf(i));
        bundlesSpinnerBiofon.getValueFactory().setValue("-");

        btnOkBundlesBiofon.setGraphic(new ImageView(new Image(okUrl.toExternalForm())));
        btnCancelBundlesBiofon.setGraphic(new ImageView(new Image(cancelUrl.toExternalForm())));

        //показывает кнопки при изменениях спинера
        bundlesSpinnerBiofon.valueProperty().addListener((observable, oldValue, newValue) -> {if(oldValue!=newValue) bundlesBtnPanBiofon.setVisible(true);});
        //кнопка отмены
        btnCancelBundlesBiofon.setOnAction(event ->hideBundlesSpinnerBTNPanBiofon(biofonCompexesList.getSelectionModel().getSelectedItem().getBundlesLength()) );

/*******************/

        //обработчик нажатия на комплекс. Есть еще такой в BiofonUtils
        biofonCompexesList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

            if( biofonCompexesList.getSelectionModel().getSelectedItems().size()==1){

                if(biofonCompexesList.getSelectionModel().getSelectedItem().isMulltyFreq())bundlesSpinnerBiofon.setDisable(false);
                else bundlesSpinnerBiofon.setDisable(true);

            }else bundlesSpinnerBiofon.setDisable(false);


            if(newValue==null) {
                hideTFSpinnerBTNPanBiofon();
                hideBundlesSpinnerBTNPanBiofon();
                return;
            }
            hideTFSpinnerBTNPanBiofon(newValue.getTimeForFrequency());
            hideBundlesSpinnerBTNPanBiofon(newValue.getBundlesLength());
        });



        //принять изменения времени
        btnOkBundlesBiofon.setOnAction(event ->
        {


            ObservableList<TherapyComplex> selectedItems = biofonCompexesList.getSelectionModel().getSelectedItems();
            if(selectedItems ==null) return;




            try {
                for (TherapyComplex complex : selectedItems) {

                    complex.setBundlesLength(bundlesSpinnerBiofon.getValue().equals("-")?1:Integer.parseInt(bundlesSpinnerBiofon.getValue()));
                    this.getModel().updateTherapyComplex(complex);
                    hideBundlesSpinnerBTNPanBiofon();
                }
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



        biofonUIUtil=new BiofonUIUtil(res,
                getApp(),this,
                getModel(),
                getApp().getBiofonProfile(),
                biofonCompexesList,
                biofonProgramsList,
                biofonInsLangComplex,
                biofonInsLangProgram,
                this::onAttachBiofon,
                this::onDetachBiofon
        );

        biofonUIUtil.init();
        biofonUIUtil.init3ComplexesButtons(biofonBtnComplex1,biofonBtnComplex2,biofonBtnComplex3);

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

/*
        Tooltip t = new Tooltip("Добавить пустой комплекс");
        Tooltip.install(bComplexAdd, t);


        t = new Tooltip("Редактировать имя комплекса");
        Tooltip.install(bComplexEdit, t);

        t = new Tooltip("Экспортировать комплексы");
        Tooltip.install(biofonExportMi, t);

        t = new Tooltip("Импортировать комплексы");
        Tooltip.install(bComplexImport, t);

        t = new Tooltip("Печать комплексов");
        Tooltip.install(bComplexPrint, t);

        t = new Tooltip("Удалить комплексы");
        Tooltip.install(bComplexDel, t);

        t = new Tooltip("Вверх");
        Tooltip.install(bProgramUp, t);

        t = new Tooltip("Вниз");
        Tooltip.install(bProgramDown, t);

        t = new Tooltip("Удалить программу");
        Tooltip.install(bProgramDel, t);
        */

    }


/**********************************************/



    //хранит время комплекса в строковом представлении и через # id комплекса. Используется в выводе в табе комплекса времени и его обновления
private SimpleStringProperty textComplexTime=new SimpleStringProperty();





            private String getComplexTabName(){
               return therapyTabPane.getTabs().get(1).getText();

            }
    private void setComplexTabName(String val){
         Platform.runLater(() -> therapyTabPane.getTabs().get(1).setText(val));

    }
    private void initTables()
    {

        //установка имени таба комплексов с учетом времени
        textComplexTime.addListener((observable, oldValue, newValue) -> {

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



        //значения времени(хранятся в соответств. классах сущностей) устанавливаются при инициализации данных. После обновляются при изменении времени, удалении и добавлении програм и комплексов.

        /*** Профили  ****/
        //номер по порядку
        TableColumn<Profile,Number> numProfileCol =new TableColumn<>("№");
        numProfileCol.setCellValueFactory(param -> new SimpleIntegerProperty(param.getTableView().getItems().indexOf(param.getValue())+1));

        //имя профиля
        TableColumn<Profile,String> nameCol=new TableColumn<>(res.getString("app.table.profile_name"));
        nameCol.cellValueFactoryProperty().setValue(new PropertyValueFactory<Profile, String>("name"));
        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        nameCol.setOnEditCommit(event ->
        {

            if (!event.getNewValue().equals(event.getOldValue())) {

                String s = replaceSpecial(event.getNewValue());
                if (s.length() == 0) {
                    event.getRowValue().setName(event.getOldValue());
                    Profile p = event.getRowValue();
                    int i = tableProfile.getItems().indexOf(event.getRowValue());
                    tableProfile.getItems().set(i, null);
                    tableProfile.getItems().set(i, p);
                    p = null;
                    tableProfile.getSelectionModel().select(i);
                    return;
                }
                event.getRowValue().setName(s);
                try {
                    getModel().updateProfile(event.getRowValue());
                    Profile p = event.getRowValue();
                    int i = tableProfile.getItems().indexOf(event.getRowValue());
                    tableProfile.getItems().set(i, null);
                    tableProfile.getItems().set(i, p);
                    tableProfile.getSelectionModel().select(i);
                    p = null;

                } catch (Exception e) {
                    logger.error("",e);
                }


            }
        });



        //общая длительность, зависит от количества комплексов, програм их частот и мультичастотного режима, также времени на частоту
        TableColumn<Profile,String> timeCol=new TableColumn<>(res.getString("app.table.delay"));
        timeCol.setCellValueFactory(param -> {
            SimpleStringProperty property = new SimpleStringProperty();
            property.bind(new StringBinding() {
                {
                    super.bind(param.getValue().timeProperty());//используем фейковое свойство, для инициализации расчета
                }

                @Override
                protected String computeValue() {
                    return DateUtil.convertSecondsToHMmSs(AppController.this.getModel().getTimeProfile(param.getValue()));
                }
            });
            return property;
        });


        TableColumn<Profile,String> weightCol=new TableColumn<>(res.getString("app.table.file_size"));
        weightCol.setCellValueFactory(param -> {
            SimpleStringProperty property = new SimpleStringProperty();
            property.bind(new StringBinding() {
                {
                    super.bind(param.getValue().profileWeightProperty());//используем фейковое свойство, для инициализации расчета
                }

                @Override
                protected String computeValue() {

                    File f = null;
                    double summ = 0;
                    for (Long v : getModel().getProfileFiles(param.getValue())) {

                        f = new File(getApp().getDataDir(), v + ".dat");
                        if (f.exists()) summ += f.length() / 1048576;
                    }

                    return Math.ceil(summ) + " Mb";
                }
            });
            return property;
        });


        tableProfile.getColumns().addAll(numProfileCol, nameCol, timeCol, weightCol);
        tableProfile.placeholderProperty().setValue(new Label(res.getString("app.table.profile_not_avaliable")));
        tableProfile.setEditable(true);

        tableProfile.getItems().addAll(getModel().findAllProfiles()
                                                 .stream()
                                                 .filter(i->!i.getName().equals(App.BIOFON_PROFILE_NAME))
                                                 .collect(Collectors.toList()));


        numProfileCol.prefWidthProperty().bind(tableProfile.widthProperty().multiply(0.1));
        nameCol.prefWidthProperty().bind(tableProfile.widthProperty().multiply(0.50));
        timeCol.prefWidthProperty().bind(tableProfile.widthProperty().multiply(0.25));
        weightCol.prefWidthProperty().bind(tableProfile.widthProperty().multiply(0.15));

        weightCol.setEditable(false);
        numProfileCol.setEditable(false);
        nameCol.setEditable(true);
        timeCol.setEditable(false);

        numProfileCol.setSortable(false);
        nameCol.setSortable(false);
        timeCol.setSortable(false);
        weightCol.setSortable(false);




    /**********/

        /*** Комплексы  ****/
        //номер по порядку
        TableColumn<TherapyComplex,Number> numComplexCol =new TableColumn<>("№");
        numComplexCol.setCellValueFactory(param -> new SimpleIntegerProperty(param.getTableView().getItems().indexOf(param.getValue()) + 1));


        //имя
        TableColumn<TherapyComplex,String> nameColTC=new TableColumn<>(res.getString("app.table.name_complex"));
        nameColTC.cellValueFactoryProperty().setValue(new PropertyValueFactory<TherapyComplex, String>("name"));
        nameColTC.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColTC.setOnEditCommit(event ->
        {

            if (!event.getNewValue().equals(event.getOldValue())) {

                String s = replaceSpecial(event.getNewValue());
                if (s.length() == 0) {
                    event.getRowValue().setName(event.getOldValue());
                    TherapyComplex p = event.getRowValue();
                    int i = tableComplex.getItems().indexOf(event.getRowValue());
                    tableComplex.getItems().set(i, null);
                    tableComplex.getItems().set(i, p);
                    tableComplex.getSelectionModel().select(i);
                    p = null;
                    return;
                }
                event.getRowValue().setName(s);
                try {
                    getModel().updateTherapyComplex(event.getRowValue());
                    TherapyComplex p = event.getRowValue();
                    int i = tableComplex.getItems().indexOf(event.getRowValue());
                    tableComplex.getItems().set(i, null);
                    tableComplex.getItems().set(i, p);
                    tableComplex.getSelectionModel().select(i);
                    p = null;

                } catch (Exception e) {
                    logger.error("",e);
                }


            }
        });

        //описание
        TableColumn<TherapyComplex,String> descColTC=new TableColumn<>(res.getString("app.table.complex_descr"));
        descColTC.cellValueFactoryProperty().setValue(new PropertyValueFactory<TherapyComplex, String>("description"));
        descColTC.setCellFactory(TextAreaTableCell.forTableColumn());
        descColTC.setOnEditCommit(event ->
        {

            if (!event.getNewValue().equals(event.getOldValue())) {

                String s = replaceSpecial(event.getNewValue());
                if (s.length() == 0) {
                    event.getRowValue().setDescription(event.getOldValue());
                    TherapyComplex p = event.getRowValue();
                    int i = tableComplex.getItems().indexOf(event.getRowValue());
                    tableComplex.getItems().set(i, null);
                    tableComplex.getItems().set(i, p);
                    p = null;
                    return;
                }
                event.getRowValue().setDescription(s);
                try {
                    getModel().updateTherapyComplex(event.getRowValue());
                    TherapyComplex p = event.getRowValue();
                    int i = tableComplex.getItems().indexOf(event.getRowValue());
                    tableComplex.getItems().set(i, null);
                    tableComplex.getItems().set(i, p);
                    p = null;

                } catch (Exception e) {
                    logger.error("",e);
                }


            }
        });

        //общая длительность, зависит от количества програм их частот и мультичастотного режима, также времени на частоту
        TableColumn<TherapyComplex,String> timeColTC=new TableColumn<>(res.getString("app.table.delay"));
      //  timeColTC.setCellValueFactory(param -> new SimpleStringProperty(DateUtil.convertSecondsToHMmSs(getModel().getTimeTherapyComplex(param.getValue()))));
       //пересчет индуцируется при изменении свойства time




        timeColTC.setCellValueFactory(param -> {
            SimpleStringProperty property = new SimpleStringProperty();
            property.bind(new StringBinding() {
                {
                    super.bind(param.getValue().timeProperty());
                }

                @Override
                protected String computeValue() {
                    String s = DateUtil.convertSecondsToHMmSs(AppController.this.getModel().getTimeTherapyComplex(param.getValue()));
                    textComplexTime.setValue(s+"#"+param.getValue().getId().longValue());
                    return s;
                }
            });
            return property;
        });





        TableColumn<TherapyComplex,Boolean> mulltyCol=new TableColumn<>(res.getString("app.table.mulltyfreqs"));
        mulltyCol.cellValueFactoryProperty().setValue(new PropertyValueFactory<TherapyComplex,Boolean>("mulltyFreq"));
        mulltyCol.setCellFactory(CheckBoxTableCell.<TherapyComplex>forTableColumn(mulltyCol));

        //обработака редактирования галочки мультичастот
        //нужно учесть, что  придется регенерировать все файлы прогрмм комплекса. Те мы должны тут очистить файлы комплекса для регенерации
        //слушаем изменения в списке комплексов и его полях
        therapyComplexItems.addListener((ListChangeListener<TherapyComplex>) c -> {
            try {
                while (c.next()) {
                    if (c.wasUpdated()) {
                        //обновленные элементы сохраним(цикл - если у нас множественные изменения)
                        for (int i = c.getFrom(); i < c.getTo(); i++)

                            c.getList().get(c.getFrom()).setChanged(true);
                        getModel().updateTherapyComplex(c.getList().get(c.getFrom()));
                        btnGenerate.setDisable(false);

                        //тут мы должны пресчитать все длительности!!!!!!!!!!!!!!!!
                        //пункт изменения у нас выбран пользователем
                        if (tableComplex.getSelectionModel().getSelectedItem() == c.getList().get(c.getFrom()))
                            updateComplexTime(c.getList().get(c.getFrom()), true);
                        else updateComplexTime(c.getList().get(c.getFrom()), false);

                    }
                }


            } catch (Exception e) {

                logger.error("",e);
            }

        });



        this.tableComplex.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tableComplex.getColumns().addAll(numComplexCol, nameColTC, descColTC, timeColTC, mulltyCol);
        tableComplex.placeholderProperty().setValue(new Label(res.getString("app.table.complex_placeholder")));
        tableComplex.setEditable(true);



        numComplexCol.prefWidthProperty().bind(tableComplex.widthProperty().multiply(0.1));
        nameColTC.prefWidthProperty().bind(tableComplex.widthProperty().multiply(0.325));
        descColTC.prefWidthProperty().bind(tableComplex.widthProperty().multiply(0.325));
        timeColTC.prefWidthProperty().bind(tableComplex.widthProperty().multiply(0.1));
        mulltyCol.prefWidthProperty().bind(tableComplex.widthProperty().multiply(0.15));

        numComplexCol.setSortable(false);
        nameColTC.setSortable(false);
        descColTC.setSortable(false);
        timeColTC.setSortable(false);
        mulltyCol.setSortable(false);

        mulltyCol.setEditable(true);


        MenuItem mic1 = new MenuItem(this.res.getString("app.to_user_base"));
        MenuItem mic2 = new MenuItem(this.res.getString("app.ui_comlexes_generation"));
        MenuItem mic3 = new MenuItem(this.res.getString("app.upload_to_dir"));
        MenuItem mic4 =new MenuItem(this.res.getString("app.to_biofon"));

        mic1.setOnAction((event2) -> {
            this.copyTherapyComplexToBase();
        });

        mic2.setOnAction((event2) -> {
            this.generateComplexes();
        });
        mic3.setOnAction((event2) -> {
            this.uploadComplexes();
        });
        mic4.setOnAction(event -> complexesToBiofon(tableComplex.getSelectionModel().getSelectedItems()));
        this.complexesMenu.getItems().addAll(new MenuItem[]{mic1, mic2, mic3,mic4});
        this.tableComplex.setContextMenu(this.complexesMenu);
        this.complexesMenu.setOnShowing((event1) -> {
            mic1.setDisable(false);
            mic2.setDisable(true);
            mic3.setDisable(false);
            mic4.setDisable(false);
            if(this.tableComplex.getSelectionModel().getSelectedItems().isEmpty()) {
                mic1.setDisable(true);
                mic2.setDisable(true);
                mic3.setDisable(true);
                mic4.setDisable(true);
            } else {
                Iterator tag = this.tableComplex.getSelectionModel().getSelectedItems().iterator();

                while(tag.hasNext()) {
                    TherapyComplex therapyComplex = (TherapyComplex)tag.next();
                    if( therapyComplex.isChanged() || this.getModel().hasNeedGenerateProgramInComplex(therapyComplex) ) {
                        mic2.setDisable(false);
                        mic3.setDisable(true);
                        break;
                    }
                }

                if(this.sectionTree.getSelectionModel().getSelectedItem() != null) {
                    if(((TreeItem)this.sectionTree.getSelectionModel().getSelectedItem()).getValue() instanceof Section) {
                        String tag1 = ((Section)this.baseCombo.getSelectionModel().getSelectedItem()).getTag();
                        if(tag1 != null && tag1.equals("USER")) {
                            mic1.setDisable(false);
                        } else {
                            mic1.setDisable(true);
                        }
                    } else {
                        mic1.setDisable(true);
                    }
                } else {
                    mic1.setDisable(true);
                }

            }
        });



        /**********/



        /*** Программы  ****/

        tableProgram.setFixedCellSize(Region.USE_COMPUTED_SIZE);
        //номер по порядку
        TableColumn<TherapyProgram,Number> numProgCol =new TableColumn<>("№");
        numProgCol.setCellValueFactory(param -> new SimpleIntegerProperty(param.getTableView().getItems().indexOf(param.getValue()) + 1));


        //имя
        TableColumn<TherapyProgram,String> nameColTP=new TableColumn<>(res.getString("app.table.program_name"));
        nameColTP.cellValueFactoryProperty().setValue(new PropertyValueFactory<TherapyProgram, String>("name"));

        //частоты
        TableColumn<TherapyProgram,String> descColTP=new TableColumn<>(res.getString("app.table.freqs"));
        descColTP.cellValueFactoryProperty().setValue(param -> new SimpleStringProperty(param.getValue().getFrequencies().replace(";", ";  ")));
        descColTP.setCellFactory(param1 -> {

            TableCell<TherapyProgram, String> cell = new TableCell<TherapyProgram, String>() {
                private Text text;

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    this.setText(null);
                    this.setGraphic(null);
                    if (!empty) {
                        text = new Text(item.toString());
                        text.setWrappingWidth((getTableColumn().getWidth())); // Setting the wrapping width to the Text
                        text.wrappingWidthProperty().bind(getTableColumn().widthProperty());
                        setGraphic(text);
                    }
                }
            };


            return cell;
        });


        //общая длительность, зависит от количества  частот и мультичастотного режима, также времени на частоту и пачек частот
        TableColumn<TherapyProgram,String> timeColTP=new TableColumn<>(res.getString("app.table.delay"));
        timeColTP.setCellValueFactory(param ->
        {
            if(param.getValue().isMp3())
            {

                Mp3File mp3file = null;
                try {
                    mp3file = new Mp3File(param.getValue().getFrequencies());
                } catch (Exception e)
                {

                    mp3file=null;
                }

                if(mp3file!=null)   return new SimpleStringProperty(DateUtil.convertSecondsToHMmSs(mp3file.getLengthInSeconds()));
                else return new SimpleStringProperty(DateUtil.convertSecondsToHMmSs(0));

            }else
            {
                final TherapyComplex selectedComplex = tableComplex.getSelectionModel().getSelectedItem();
                int freqBundlesCount=1;//сколько пачек получем из частот программы
                if( selectedComplex.isMulltyFreq() &&  selectedComplex.getBundlesLength()>=2){
                    int numFreqsForce = param.getValue().getNumFreqsForce();
                    freqBundlesCount=(int)Math.ceil((float)numFreqsForce/(float)selectedComplex.getBundlesLength());
                }



                return new SimpleStringProperty(DateUtil.convertSecondsToHMmSs(
                        selectedComplex.isMulltyFreq() ? selectedComplex.getTimeForFrequency()*freqBundlesCount : param.getValue().getNumFreqs() * selectedComplex.getTimeForFrequency()));
            }
        });


        TableColumn<TherapyProgram,Boolean> fileCol=new TableColumn<>(res.getString("app.table.file"));
        fileCol.setCellValueFactory(param -> {
            SimpleBooleanProperty property = new SimpleBooleanProperty();
            property.bind(Bindings.or(param.getValue().changedProperty(), tableComplex.getSelectionModel().getSelectedItem().changedProperty()));
            return property;
        });

        fileCol.setCellFactory(col ->
                {
                    TableCell<TherapyProgram, Boolean> cell = new TableCell<TherapyProgram, Boolean>() {
                        @Override
                        protected void updateItem(Boolean item, boolean empty) {

                            super.updateItem(item, empty);
                            this.setText(null);
                            this.setGraphic(null);


                            ImageView iv;
                            if( this.getUserData()!=null)
                            {
                                iv=(ImageView)this.getUserData();
                            }else iv=new ImageView(imageCancel);


                            if (!empty) {
                                if (this.getTableRow().getItem() == null) {setText(""); return;}

                                File f;
                                if(((TherapyProgram) this.getTableRow().getItem()).isMp3())
                                {

                                    //в любом случае проверим наличие файла
                                    f = new File(((TherapyProgram) this.getTableRow().getItem()).getFrequencies());


                                    if (f.exists())
                                    {
                                        setText(Math.ceil((double) f.length() / 1048576.0) + " Mb");
                                        iv.setImage(imageDone);
                                        setGraphic(iv);}
                                    else
                                    {
                                        setText("");
                                        iv.setImage(imageCancel);
                                        setGraphic(iv);

                                        //если установленно что не требуется генерация, а файла нет, то изменим флаг генерации и иконку
                                        if(((TherapyProgram) this.getTableRow().getItem()).isChanged()==false)
                                        {
                                            ((TherapyProgram) this.getTableRow().getItem()).setChanged(true);
                                            try {
                                                getModel().updateTherapyProgram(((TherapyProgram) this.getTableRow().getItem()));
                                            } catch (Exception e) {
                                                logger.error("",e);
                                            }
                                        }
                                    }
                                }else
                                {
                                    if (item) {
                                        iv.setImage(imageCancel);
                                        setGraphic(iv);
                                        setText("");
                                    } else
                                    {
                                        long id = ((TherapyProgram) this.getTableRow().getItem()).getId();
                                        f = new File(getApp().getDataDir(), id + ".dat");


                                        if (f.exists())
                                        {
                                            setText(Math.ceil((double) f.length() / 1048576.0) + " Mb");
                                            iv.setImage(imageDone);
                                            setGraphic(iv);
                                        }
                                        else{ setText("");    iv.setImage(imageCancel); setGraphic(iv);}



                                    }

                                }

                            }
                        }
                    };

                    return cell;
                }
        );



        tableProgram.getColumns().addAll(numProgCol, nameColTP, descColTP, timeColTP, fileCol);
        tableProgram.placeholderProperty().setValue(new Label(res.getString("app.table.programm_placeholder")));


        numProgCol.prefWidthProperty().bind(tableProgram.widthProperty().multiply(0.1));
        nameColTP.prefWidthProperty().bind(tableProgram.widthProperty().multiply(0.3));
        descColTP.prefWidthProperty().bind(tableProgram.widthProperty().multiply(0.3));
        timeColTP.prefWidthProperty().bind(tableProgram.widthProperty().multiply(0.15));
        fileCol.prefWidthProperty().bind(tableProgram.widthProperty().multiply(0.15));

        numProgCol.setSortable(false);
        nameColTP.setSortable(false);
        descColTP.setSortable(false);
        timeColTP.setSortable(false);
        fileCol.setSortable(false);

        tableProfile.setOnKeyReleased(event ->
        {
            if(event.getCode()==KeyCode.DELETE) onRemoveProfile();
            if(event.getCode()==KeyCode.RIGHT){
                therapyTabPane.getSelectionModel().select(1);
                tableComplex.requestFocus();
                if(tableComplex.getItems().size()!=0){tableComplex.getFocusModel().focus(0);tableComplex.getSelectionModel().select(0);}
            }
        });

        this.tableComplex.setOnKeyReleased((e) -> {
            if(e.getCode() == KeyCode.DELETE) {
                this.onRemoveComplex();
            }

            if(e.getCode() == KeyCode.A && e.isControlDown()) {
                this.tableComplex.getSelectionModel().selectAll();
            }

            if(e.getCode()==KeyCode.RIGHT) {
                therapyTabPane.getSelectionModel().select(2);
                tableProgram.requestFocus();
                if(tableProgram.getItems().size()!=0){tableProgram.getFocusModel().focus(0);tableProgram.getSelectionModel().select(0);}
            }
            if(e.getCode()==KeyCode.LEFT) {
                therapyTabPane.getSelectionModel().select(0);
                tableProfile.requestFocus();
                if(tableProfile.getItems().size()!=0){tableProfile.getFocusModel().focus(0);tableProfile.getSelectionModel().select(0);}
            }

        });
        tableProgram.setOnKeyReleased(e ->{
            if(e.getCode()==KeyCode.DELETE) onRemoveProgram();
            if(e.getCode()==KeyCode.LEFT) {
                therapyTabPane.getSelectionModel().select(1);
                tableComplex.requestFocus();
                if(tableComplex.getItems().size()!=0){tableComplex.getFocusModel().focus(0);tableComplex.getSelectionModel().select(0);}
            }

        });
//контекстное меню для программ - вырезать и вставить, зависит от выбрано или нет

        MenuItem mi1=new MenuItem(res.getString("app.cut"));
        MenuItem mi2=new MenuItem(res.getString("app.paste"));
        MenuItem mi3=new SeparatorMenuItem();
        MenuItem mi4=new MenuItem(res.getString("app.to_user_base"));
        MenuItem mi5=new SeparatorMenuItem();
        MenuItem mi6=new MenuItem(res.getString("app.ui.edit_file_path"));



        mi4.setOnAction(event2 -> copyTherapyProgramToBase());
        mi6.setOnAction(event2 -> editMP3ProgramPath());

        mi1.setOnAction(e ->
        {
            if (tableProgram.getSelectionModel().getSelectedItem() == null) return;
            Clipboard clipboard = Clipboard.getSystemClipboard();


                ClipboardContent content = new ClipboardContent();
                content.put(PROGRAM_CUT_ITEM, tableProgram.getSelectionModel().getSelectedIndex());
                clipboard.setContent(content);

        });

        mi2.setOnAction(e ->
        {

            //в выбранный индекс вставляется новый элемент а все сдвигаются на 1 индекс  вырезанного индекса
            if (tableProgram.getSelectionModel().getSelectedItem() == null) return;
            Clipboard clipboard = Clipboard.getSystemClipboard();

            if (!clipboard.hasContent(PROGRAM_CUT_ITEM)) return;
            int ind = (Integer) clipboard.getContent(PROGRAM_CUT_ITEM);
            int dropIndex = tableProgram.getSelectionModel().getSelectedIndex();
            TherapyProgram therapyProgram = tableProgram.getItems().get(ind);
            if(ind!=dropIndex)
            {
    //элементы всегда будут оказываться выше чем индекс по которому вставляли, те визуально вставляются над выбираемым элементом
                try {
                    //обновление базы
                    if(dropIndex>ind) {

                        //если перетащили выше исходного положения, то нужно обновить позиции от новой позиции до исходной увеличив на 1




                        tableProgram.getItems().add(dropIndex, therapyProgram);
                        tableProgram.getSelectionModel().select(dropIndex);
                        tableProgram.getItems().remove(ind);

                        therapyProgram.setPosition(tableProgram.getItems().get(dropIndex).getPosition());//здесь dropIndex указ уже на след элемент после вставки
                        getModel().updateTherapyProgram(therapyProgram);


                        for(int i=dropIndex;i<tableProgram.getItems().size();i++)
                        {
                            tableProgram.getItems().get(i).setPosition(tableProgram.getItems().get(i).getPosition()+1);
                            getModel().updateTherapyProgram( tableProgram.getItems().get(i));

                        }

                    }else
                    {
                        //элемент будет на месте вставки а все сдвинется ниже

                        tableProgram.getItems().remove(ind);
                        tableProgram.getItems().add(dropIndex, therapyProgram);
                        tableProgram.getSelectionModel().select(dropIndex);


                        therapyProgram.setPosition(tableProgram.getItems().get(dropIndex+1).getPosition());//dropIndex это точный индекс вставки
                        getModel().updateTherapyProgram(therapyProgram);

                        for(int i=dropIndex+1;i<tableProgram.getItems().size();i++)
                        {
                            tableProgram.getItems().get(i).setPosition(tableProgram.getItems().get(i).getPosition()+1);
                            getModel().updateTherapyProgram( tableProgram.getItems().get(i));

                        }

                    }

                } catch (Exception e1) {
                    logger.error(e1);
                    showExceptionDialog("Ошибка обновления позиции программы","","",e1,getApp().getMainWindow(),Modality.WINDOW_MODAL);
                    therapyProgram = null;
                    clipboard.clear();
                    return;
                }






            }
            therapyProgram = null;
            clipboard.clear();

        });

        programmMenu.getItems().addAll(mi2, mi1,mi3,mi4,mi5,mi6);

        tableProgram.setContextMenu(programmMenu);
        this.programmMenu.setOnShowing((event1) -> {
            if(this.tableProgram.getSelectionModel().getSelectedItem() == null) {
                mi2.setDisable(true);
                mi1.setDisable(true);
                mi3.setDisable(true);
                mi4.setDisable(true);
                mi5.setDisable(true);
                mi6.setDisable(true);
            } else {
                mi2.setDisable(true);
                mi1.setDisable(true);
                mi3.setDisable(true);
                mi4.setDisable(true);
                mi5.setDisable(true);
                mi6.setDisable(true);
                Clipboard clipboard = Clipboard.getSystemClipboard();
                if(!clipboard.hasContent(this.PROGRAM_CUT_ITEM)) {
                    mi2.setDisable(true);
                    mi1.setDisable(false);
                } else {
                    mi2.setDisable(false);
                    mi1.setDisable(false);
                }

                if(this.tableProgram.getSelectionModel().getSelectedItem() == null) {
                    mi4.setDisable(true);
                }

                if(this.sectionTree.getSelectionModel().getSelectedItem() != null) {
                    INamed value = this.sectionTree.getSelectionModel().getSelectedItem().getValue();
                    if( value instanceof Section || value instanceof Complex) {
                        String tag = ((Section)this.baseCombo.getSelectionModel().getSelectedItem()).getTag();
                        if(tag != null && tag.equals("USER")) {
                            mi4.setDisable(false);
                        } else {
                            mi4.setDisable(true);
                        }
                    } else {
                        mi4.setDisable(true);
                    }

                    if(((TherapyProgram)this.tableProgram.getSelectionModel().getSelectedItem()).isMp3()) {
                        mi4.setDisable(true);
                        mi6.setDisable(false);
                    }
                } else {
                    mi4.setDisable(true);
                }

            }
        });





        /**********/





        tableProfile.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
        {


            if (oldValue != newValue) {
                //закроем кнопки спинера времени на частоту
                hideTFSpinnerBTNPan();
                hideBundlesSpinnerBTNPan();

                tableComplex.getItems().clear();
                //добавляем через therapyComplexItems иначе не будет работать event на изменение элементов массива и не будут работать галочки мультичастот
                therapyComplexItems.clear();
                therapyComplexItems.addAll(getModel().findTherapyComplexes(newValue));
                tableComplex.getItems().addAll(therapyComplexItems);


                if(newValue!=null){

                    btnGenerate.setDisable(!getModel().isNeedGenerateFilesInProfile(newValue));

                }
            }

        });



        tableComplex.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {



                if( tableComplex.getSelectionModel().getSelectedItems().size()==1){

                    if(tableComplex.getSelectionModel().getSelectedItem().isMulltyFreq())bundlesSpinner.setDisable(false);
                    else bundlesSpinner.setDisable(true);

                }else bundlesSpinner.setDisable(false);

            if (oldValue != newValue) {

                //закроем кнопки спинера времени на частоту, при переключении компелекса
                if(newValue!=null) {
                    Platform.runLater(() -> {
                        hideTFSpinnerBTNPan(newValue.getTimeForFrequency());
                        hideBundlesSpinnerBTNPan(tableComplex.getSelectionModel().getSelectedItem().getBundlesLength());
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

        /*
tableProgram.getSelectionModel().selectedItemProperty().addListener((observable1, oldValue1, newValue1) ->
{
    if(spinnerBtnPan.isVisible()) {
        //закроем кнопки спинера времени на частоту, при переключении на программу

      if(tableComplex.getSelectionModel().getSelectedItem()!=null)  {
          hideTFSpinnerBTNPan(tableComplex.getSelectionModel().getSelectedItem().getTimeForFrequency());
          //hideBundlesSpinnerBTNPan(tableComplex.getSelectionModel().getSelectedItem().getBundlesLength());
      }
        else { hideTFSpinnerBTNPan();}
    }
});
*/

        /***** Спиннер времени на частоту ****/

        //показывает кнопки при изменениях спинера
        timeToFreqSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {if(oldValue!=newValue) spinnerBtnPan.setVisible(true);});
        //кнопка отмены
        btnCancelSpinner.setOnAction(event ->hideTFSpinnerBTNPan(tableComplex.getSelectionModel().getSelectedItem().getTimeForFrequency()) );
        //принять изменения времени
        btnOkSpinner.setOnAction(event ->
        {


            if(!this.tableComplex.getSelectionModel().getSelectedItems().isEmpty()) {
                List<TherapyComplex> items = new ArrayList<>(this.tableComplex.getSelectionModel().getSelectedItems());

                try {


                   for(TherapyComplex item:items) {


                        item.setTimeForFrequency(this.timeToFreqSpinner.getValue());
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


        /***************/

        /** Комбо пачек частот **/

        bundlesSpinnerData.add("-");
        for(int i=2; i<20; i++)bundlesSpinnerData.add(String.valueOf(i));
        bundlesSpinner.getValueFactory().setValue("-");

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


                        item.setBundlesLength(bundlesSpinner.getValue().equals("-")?1:Integer.parseInt(bundlesSpinner.getValue()));
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


        /***************/





        //помогает узнать видимые строки
     //   loadVirtualFlowTableProgramm();

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
        File mp3 = fileChooser.showOpenDialog(getApp().getMainWindow());

        if(mp3==null)return;

        item.setName(TextUtil.digitText(mp3.getName().substring(0,mp3.getName().length()-4)));
        item.setFrequencies(mp3.getAbsolutePath());
        int i = tableProgram.getItems().indexOf(item);

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
        tableProgram.getSelectionModel().select(i);


    }

    /**
     * Копирование текущей программы в пользовательскую базу
     */
    private void copyTherapyProgramToBase()
    {

        TherapyProgram therapyProgram = tableProgram.getSelectionModel().getSelectedItem();
        NamedTreeItem treeItem = (NamedTreeItem) sectionTree.getSelectionModel().getSelectedItem();
        if(therapyProgram==null || treeItem==null) return;
        if(therapyProgram.isMp3())return;


        Program p= null;
        try {

            if(treeItem.getValue() instanceof Section) p = getModel().createProgram(therapyProgram.getName(), therapyProgram.getDescription(), therapyProgram.getFrequencies(),(Section)treeItem.getValue(), false, getModel().getUserLanguage());
            else if(treeItem.getValue() instanceof Complex) p = getModel().createProgram(therapyProgram.getName(), therapyProgram.getDescription(), therapyProgram.getFrequencies(),(Complex)treeItem.getValue(), false, getModel().getUserLanguage());
            else throw new Exception();


            if (!treeItem.isLeaf())  {getModel().initStringsProgram(p); treeItem.getChildren().add(new NamedTreeItem(p));}


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
        timeToFreqSpinner.getValueFactory().setValue(val);
        spinnerBtnPan.setVisible(false);

    }
    private void hideTFSpinnerBTNPan()
    {

        spinnerBtnPan.setVisible(false);

    }



    private void hideBundlesSpinnerBTNPan(int val)
    {
        bundlesSpinner.getValueFactory().setValue(val==1 ? "-" : String.valueOf(val));
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
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("xmlb", "*.xmlb"));
        file= fileChooser.showSaveDialog(getApp().getMainWindow());

        if(file==null)return;


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

        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("xmlp", "*.xmlp"));
        file= fileChooser.showSaveDialog(getApp().getMainWindow());

        if(file==null)return;
        final Profile prof=selectedItem;
        final File fileToSave=file;



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


        FileChooser fileChooser =new FileChooser();
        fileChooser.setTitle(res.getString("app.title37"));

        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
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

        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
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
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
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
            //this.therapyComplexItems.clear();
            int nextIndex = this.therapyComplexItems.size();
            List<TherapyComplex> lastTherapyComplexes = this.getModel().getLastTherapyComplexes(nums);
            if(!lastTherapyComplexes.isEmpty())
            {
                this.therapyComplexItems.addAll(lastTherapyComplexes);
                this.tableComplex.getItems().addAll(this.therapyComplexItems.subList(nextIndex,this.therapyComplexItems.size()));
                //this.therapyComplexItems.clear();
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

        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
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
private void setInfoMessage(String message)
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
  private void setProgressBar(double value,String textAction,String textInfo)
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


    private void hideProgressBar(boolean animation)
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
    private void setProgressIndicator(double value,String text)
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
    private void setProgressIndicator(double value)
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
    private void setProgressIndicator(String text)
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
    private void hideProgressIndicator(boolean animation)
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
    public void onRemoveProgram()
    {

        if(tableProgram.getSelectionModel().getSelectedItem()==null)return;

       TherapyProgram p = tableProgram.getSelectionModel().getSelectedItem();

        Optional<ButtonType> buttonType = showConfirmationDialog(res.getString("app.title66"), "", res.getString("app.title67"), getApp().getMainWindow(), Modality.WINDOW_MODAL);

        if(buttonType.isPresent() ? buttonType.get()==okButtonType: false)
        {
            try {
                getModel().removeTherapyProgram(p);

                File   temp=new File(getApp().getDataDir(),p.getId()+".dat");
                if(temp.exists())temp.delete();

                tableProgram.getItems().remove(p);







              updateComplexTime(tableComplex.getSelectionModel().getSelectedItem(), false);

            } catch (Exception e) {
                logger.error("",e);

                showExceptionDialog("Ошибка удаления программы","","",e,getApp().getMainWindow(),Modality.WINDOW_MODAL);

            }

            p=null;
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
                TherapyComplex therapyComplex = getModel().createTherapyComplex(tableProfile.getSelectionModel().getSelectedItem(), data.getNewName(), data.getNewDescription(), 300, true,1);
                //therapyComplexItems.clear();
                therapyComplexItems.add(therapyComplex);
                tableComplex.getItems().add(therapyComplexItems.get(therapyComplexItems.size()-1));

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
        ObservableList<TherapyComplex> selectedItems = this.tableComplex.getSelectionModel().getSelectedItems();

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


    private String replaceTime(String time)
    {
        StringBuilder strb=new StringBuilder();


        String[] split = time.split(":");
        if(split.length==3)
        {
            strb.append(split[0]); strb.append(res.getString("app.hour"));strb.append(" ");
            strb.append(split[1]); strb.append(res.getString("app.minute"));strb.append(" ");
            strb.append(split[2]); strb.append(res.getString("app.secunde"));
            return strb.toString();
        }else return time.replace(":","_");







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

                    cMap.put(itm.getId(), TextUtil.replaceWinPathBadSymbols(itm.getName()) + " (" + replaceTime(timeTableColumn.getCellObservableValue(itm).getValue().toString()) + ")");
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
                        String timeP = DateUtil.convertSecondsToHMmSs(
                                therapyComplex.isMulltyFreq() ? therapyComplex.getTimeForFrequency() : therapyProgram.getNumFreqs() * therapyComplex.getTimeForFrequency());


                    String nameFile = (++count2) + "-" + TextUtil.replaceWinPathBadSymbols(therapyProgram.getName()) + " (" + replaceTime(timeP) + ")";

                    // System.out.print("Создание программы: " + nameFile + ".bss...");

                    FilesProfileHelper.copyBSS(new File(getApp().getDataDir(), therapyProgram.getId() + ".dat"),
                            new File(tempFile, nameFile + ".bss"));
                    //System.out.println("OK");
                    // System.out.print("Создание текста программы: " + nameFile + ".txt...");
                    FilesProfileHelper.copyTxt(therapyProgram.getFrequencies(), cMap2.get(entry.getKey()), therapyProgram.getId().longValue(),
                            therapyProgram.getUuid(), entry.getKey(), therapyComplex.isMulltyFreq(),therapyComplex.getBundlesLength(), TextUtil.replaceWinPathBadSymbols(therapyProgram.getName()),false, new File(tempFile, nameFile + ".txt"));
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

                            String nameFile = (++count2) + "-" + TextUtil.replaceWinPathBadSymbols(therapyProgram.getName()) + " (" + replaceTime(timeP) + ")";

                            FilesProfileHelper.copyBSS(new File(therapyProgram.getFrequencies()),new File(tempFile, nameFile + ".bss"));

                            FilesProfileHelper.copyTxt(therapyProgram.getFrequencies(), cMap2.get(entry.getKey()), therapyProgram.getId().longValue(),
                                    therapyProgram.getUuid(), entry.getKey(), therapyComplex.isMulltyFreq(),therapyComplex.getBundlesLength(), TextUtil.replaceWinPathBadSymbols(therapyProgram.getName()),true, new File(tempFile, nameFile + ".txt"));




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



    /**
     *
     * @param f файл профиля устройства
     * @param prof текущий профиль выбранный
     * @param nameP имя профиля из устройства
     */
    private boolean updateFiles(File f,Profile prof,String nameP)
    {
        //анализ изменений и их перезапись
        //Профиль - фиксируем изменение названия. Можно его переписать в файле.
        //Комплексы - сравним список id папок комплексов с ID  из базы. Можно потереть все папки id которых не соответствует базе.
        //далее проход стандартный по записи. Перезаписываем названия всех папок- вдруг изменились названия или время, если такого компл нет - создаем.
        //программы создаем если нет, если есть то сравниваем их UUID. Если изменлися то перезаписываем
        cancel=false;
        //обновим файл профиля
        if(!nameP.equals(prof.getName()))
        {

            try(PrintWriter writer = new PrintWriter(f,"UTF-8")) {




                writer.println(tableProfile.getSelectionModel().getSelectedItem().getId().toString());//id профиля
                //writer.write("\n");

                writer.println(tableProfile.getSelectionModel().getSelectedItem().getUuid());//uuid профиля. Чтобы не перепутать с профилем записанном на другой программе
                //writer.write("\n");

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

        }
        if(cancel)return false;

        // получим список комплексов и программ с устройства с их именами итп
        List<ComplexFileData> tDComplexes = null;
        try {
            tDComplexes = FilesProfileHelper.getComplexes(devicePath.toFile());
        } catch (OldComplexTypeException e) {
            logger.error("",e);
            Platform.runLater(() -> showWarningDialog(res.getString("app.title87"), "", res.getString("app.title94") + " - " + e.getComplexName() + " " + res.getString("app.title95"), getApp().getMainWindow(), Modality.WINDOW_MODAL));
            callFail("Обнаружен комплекс - " + e.getComplexName()+" старого формата.");
            return false;
        }

        //найдем комплексы которых нет в базе и потрем их
        List<Integer> allTherapyComplexID = getModel().getAllTherapyComplexID(prof);


        for (ListIterator<ComplexFileData> i = tDComplexes.listIterator(); i.hasNext();){
            ComplexFileData tDComplex = i.next();
            if(!allTherapyComplexID.contains(tDComplex.getId()))
            {
                FilesProfileHelper.recursiveDelete(tDComplex.getFile());//удалим директорию которой нет в базе, в том числе и те что не содержат текстовых описаний
                i.remove();
            }
        }
        if(cancel)return false;
        // переименуем все комплексы
        Map<Long,String> cMap=new LinkedHashMap<>();
        Map<Long,Integer> cMap2=new LinkedHashMap<>();


        TableColumn<TherapyComplex, ?> timeTableColumn = tableComplex.getColumns().get(3);//с учетом что время это 4 колонка!!!


        //TODO:   ///  нельзя использовать в именах  \ / ? : * " > < |  +=[]:;«,./?'пробел'     Нужно это выфильтровывать
        //генерируем список названий папкок для комплексов.
        tableComplex.getItems().forEach(itm ->
                {

                    cMap.put(itm.getId(), TextUtil.replaceWinPathBadSymbols(itm.getName()) + " (" + replaceTime(timeTableColumn.getCellObservableValue(itm).getValue().toString()) + ")");
                    cMap2.put(itm.getId(), itm.getTimeForFrequency());
                }
        );


        if(cancel)return false;
        List<Long> hasComplexesOnDevice=new ArrayList<>();
        // переименуем все комплексы на новые
        int cnt=0;
        for (ComplexFileData tDComplex : tDComplexes)
        {

            File nf= new File(devicePath.toFile(),(++cnt)+"-"+cMap.get(tDComplex.getId()));
            nf=new File(nf.toURI());//пробелы!!
            tDComplex.getFile().renameTo(nf);
            tDComplex.setFile(nf);
            tDComplex.setName(cnt + "-" + cMap.get(tDComplex.getId()));

            hasComplexesOnDevice.add(tDComplex.getId());

        }


        if(cancel)return false;


//создадим те папки которых нет
        try
        {


            File tempFile=null;

            cnt=hasComplexesOnDevice.size();
            TherapyComplex therapyComplex=null;
            for (Map.Entry<Long, String> entry : cMap.entrySet())
            {
                if(hasComplexesOnDevice.contains(entry.getKey()))continue;
                therapyComplex=getModel().findTherapyComplex(entry.getKey());
                if( getModel().countTherapyPrograms(entry.getKey(),true)==0 ) continue;//не записываем пустые директории
                tempFile= new File(devicePath.toFile(),(++cnt)+"-"+entry.getValue());

                FilesProfileHelper.copyDir(tempFile);
                tDComplexes.add(new ComplexFileData(entry.getKey(),cnt+"-"+entry.getValue(),cMap2.get(entry.getKey()),therapyComplex.isMulltyFreq(),therapyComplex.getBundlesLength(),tempFile));

            }
        }catch (Exception e)
        {
            logger.error("",e);
            Platform.runLater(() -> showExceptionDialog(res.getString("app.title87"), "", res.getString("app.title93"), e, getApp().getMainWindow(), Modality.WINDOW_MODAL));
            callFail("Ошибка копирования файлов. ");
            return false;
        }

        //пройдем по всем папкам комплексов и обработаем программы! В tDComplexes есть все комплексы и они точно синхронизированны с базой(это не пустые компелексы)
        List<TherapyProgram> programList =null;
        Map<Long,ProgramFileData> diskProgramms=null;
        for (ComplexFileData tDComplex : tDComplexes) {
            programList = getModel().findTherapyPrograms(tDComplex.getId());
            try {
                diskProgramms = FilesProfileHelper.getProgramms(tDComplex.getFile());
            } catch (OldComplexTypeException e) {
                logger.error("",e);
                Platform.runLater(() -> showWarningDialog(res.getString("app.title87"), "", res.getString("app.title94") + " - " + e.getComplexName() + " " + res.getString("app.title95"), getApp().getMainWindow(), Modality.WINDOW_MODAL));
                callFail("Обнаружен комплекс - " + e.getComplexName() + " старого формата.");
                return false;
            }

            //переименуем все файлы в папке для удобства. Все файлы переименованны по ID. ЧТо гарантирует отсутствие коллизий
            for (Map.Entry<Long, ProgramFileData> entry : diskProgramms.entrySet()) {

                if (entry.getValue().getBssFile() != null) {
                    File nf = new File(tDComplex.getFile(), entry.getKey() + ".bss");
                    nf = new File(nf.toURI());//пробелы!!
                    entry.getValue().getBssFile().renameTo(nf);
                    entry.getValue().setBssFile(nf);
                }

                File nf = new File(tDComplex.getFile(), entry.getKey() + ".txt");
                nf = new File(nf.toURI());//пробелы!!
                entry.getValue().getTxtFile().renameTo(nf);
                entry.getValue().setTxtFile(nf);


            }

            cnt = 0;

            try
            {
                //пройдем по программам в базе. И комируем на прибор или просто переименовываем файлы на приборе
                for (TherapyProgram program : programList) {

                    //если есть программа на приборе уже
                    if (diskProgramms.containsKey(program.getId()))
                    {

                        //проверим изменение.
                        if (!program.getUuid().equals(diskProgramms.get(program.getId()).getUuid()))
                        {
                            //есть изменения. Нужно перезаписать программу с ПК.
                            if(diskProgramms.get(program.getId()).getBssFile()!=null)diskProgramms.get(program.getId()).getBssFile().delete();
                            diskProgramms.get(program.getId()).getTxtFile().delete();



                            TherapyComplex therapyComplex = getModel().findTherapyComplex(tDComplex.getId());
                            if(!program.isMp3())
                            {
                                String timeP = DateUtil.convertSecondsToHMmSs(
                                        therapyComplex.isMulltyFreq() ? therapyComplex.getTimeForFrequency() : program.getNumFreqs() * therapyComplex.getTimeForFrequency());


                                String nameFile = (++cnt) + "-" + TextUtil.replaceWinPathBadSymbols(program.getName()) + " (" + replaceTime(timeP) + ").bss";
                                String nameFile1 = cnt + "-" + TextUtil.replaceWinPathBadSymbols(program.getName()) + ".txt";
                                //  System.out.print("Создание программы: " + nameFile + "...");

                                FilesProfileHelper.copyBSS(new File(getApp().getDataDir(), program.getId() + ".dat"),
                                        new File(tDComplex.getFile(), nameFile));
                                // System.out.println("OK");
                                //  System.out.print("Создание текста программы: " + nameFile1 + "...");
                                FilesProfileHelper.copyTxt(program.getFrequencies(), therapyComplex.getTimeForFrequency(), program.getId().longValue(),
                                        program.getUuid(), tDComplex.getId(), therapyComplex.isMulltyFreq(),therapyComplex.getBundlesLength(), TextUtil.replaceWinPathBadSymbols(program.getName()),
                                        false, new File(tDComplex.getFile(), nameFile1));
                                //System.out.println("OK");
                            }else
                            {
                                //если программа MP3

                                Mp3File mp3file = null;
                                try {
                                    mp3file = new Mp3File(program.getFrequencies());
                                } catch (Exception e) {mp3file=null;}

                                if(mp3file!=null)
                                {
                                    //если файл существует
                                    String timeP =DateUtil.convertSecondsToHMmSs(mp3file.getLengthInSeconds());

                                    String nameFile = (++cnt) + "-" + TextUtil.replaceWinPathBadSymbols(program.getName()) + " (" + replaceTime(timeP) + ").bss";
                                    String nameFile1 = cnt + "-" + TextUtil.replaceWinPathBadSymbols(program.getName()) + ".txt";

                                    FilesProfileHelper.copyBSS(new File(program.getFrequencies()),new File(tDComplex.getFile(), nameFile + ".bss"));

                                    FilesProfileHelper.copyTxt(program.getFrequencies(), therapyComplex.getTimeForFrequency(), program.getId().longValue(),
                                            program.getUuid(), tDComplex.getId(), therapyComplex.isMulltyFreq(),therapyComplex.getBundlesLength(), TextUtil.replaceWinPathBadSymbols(program.getName()),
                                            true, new File(tDComplex.getFile(), nameFile1));




                                }

                            }

                        } else
                        {
                            //просто переименуем согласно счетчику.
                            TherapyComplex therapyComplex = getModel().findTherapyComplex(tDComplex.getId());

                            if(!program.isMp3())
                            {
                                String timeP = DateUtil.convertSecondsToHMmSs(
                                        therapyComplex.isMulltyFreq() ? therapyComplex.getTimeForFrequency() : program.getNumFreqs() * therapyComplex.getTimeForFrequency());


                                String nameFile = (++cnt) + "-" + TextUtil.replaceWinPathBadSymbols(program.getName()) + " (" + replaceTime(timeP) + ").bss";
                                String nameFile1 = cnt + "-" + TextUtil.replaceWinPathBadSymbols(program.getName()) + ".txt";
                                // System.out.print("Переименование программы: " + nameFile + "...");


                                File nf = new File(tDComplex.getFile(), nameFile);
                                nf = new File(nf.toURI());//пробелы!!
                                if (diskProgramms.get(program.getId()).getBssFile() != null) diskProgramms.get(program.getId()).getBssFile().renameTo(nf);
                                else FilesProfileHelper.copyBSS(new File(getApp().getDataDir(), program.getId() + ".dat"),
                                            new File(tDComplex.getFile(), nameFile));//если файла программы нет то его создадим.


                                //  System.out.println("OK");
                                //System.out.print("Переименование текста программы: " + nameFile + "...");

                                nf = new File(tDComplex.getFile(), nameFile1);
                                nf = new File(nf.toURI());//пробелы!!
                                diskProgramms.get(program.getId()).getTxtFile().renameTo(nf);
                                //  System.out.println("OK");
                            }else
                            {
                                Mp3File mp3file = null;
                                try {
                                    mp3file = new Mp3File(program.getFrequencies());
                                } catch (Exception e) {mp3file=null;}

                                if(mp3file!=null)
                                {
                                    //если файл существует
                                    String timeP =DateUtil.convertSecondsToHMmSs(mp3file.getLengthInSeconds());

                                    String nameFile = (++cnt) + "-" + TextUtil.replaceWinPathBadSymbols(program.getName()) + " (" + replaceTime(timeP) + ").bss";
                                    String nameFile1 = cnt + "-" + TextUtil.replaceWinPathBadSymbols(program.getName()) + ".txt";

                                    File nf = new File(tDComplex.getFile(), nameFile);
                                    nf = new File(nf.toURI());//пробелы!!
                                    if (diskProgramms.get(program.getId()).getBssFile() != null) diskProgramms.get(program.getId()).getBssFile().renameTo(nf);
                                    else  FilesProfileHelper.copyBSS(new File(program.getFrequencies()),
                                            new File(tDComplex.getFile(), nameFile + ".bss"));//если файла программы нет то его создадим.


                                    nf = new File(tDComplex.getFile(), nameFile1);
                                    nf = new File(nf.toURI());//пробелы!!
                                    diskProgramms.get(program.getId()).getTxtFile().renameTo(nf);
                                }
                            }
                        }
                    }
                    else
                    {
                        //программы вообще нет. Запишем с ПК

                        TherapyComplex therapyComplex = getModel().findTherapyComplex(tDComplex.getId());

                        if(!program.isMp3())
                        {
                            String timeP = DateUtil.convertSecondsToHMmSs(
                                    therapyComplex.isMulltyFreq() ? therapyComplex.getTimeForFrequency() : program.getNumFreqs() * therapyComplex.getTimeForFrequency());


                            String nameFile = (++cnt) + "-" + TextUtil.replaceWinPathBadSymbols(program.getName()) + " (" + replaceTime(timeP) + ").bss";
                            String nameFile1 = cnt + "-" + TextUtil.replaceWinPathBadSymbols(program.getName()) + ".txt";
                            // System.out.print("Создание программы: " + nameFile + "...");

                            FilesProfileHelper.copyBSS(new File(getApp().getDataDir(), program.getId() + ".dat"),
                                    new File(tDComplex.getFile(), nameFile));
                            //System.out.println("OK");
                            // System.out.print("Создание текста программы: " + nameFile1 + "...");
                            FilesProfileHelper.copyTxt(program.getFrequencies(), therapyComplex.getTimeForFrequency(), program.getId().longValue(),
                                    program.getUuid(), tDComplex.getId(), therapyComplex.isMulltyFreq(),therapyComplex.getBundlesLength(), TextUtil.replaceWinPathBadSymbols(program.getName()), false, new File(tDComplex.getFile(), nameFile1));
                            // System.out.println("OK");
                        }else
                        {
                            Mp3File mp3file = null;
                            try {
                                mp3file = new Mp3File(program.getFrequencies());
                            } catch (Exception e) {mp3file=null;}

                            if(mp3file!=null)
                            {
                                //если файл существует
                                String timeP =DateUtil.convertSecondsToHMmSs(mp3file.getLengthInSeconds());

                                String nameFile = (++cnt) + "-" + TextUtil.replaceWinPathBadSymbols(program.getName()) + " (" + replaceTime(timeP) + ").bss";
                                String nameFile1 = cnt + "-" + TextUtil.replaceWinPathBadSymbols(program.getName()) + ".txt";

                                FilesProfileHelper.copyBSS(new File(program.getFrequencies()),new File(tDComplex.getFile(), nameFile + ".bss"));

                                FilesProfileHelper.copyTxt(program.getFrequencies(), therapyComplex.getTimeForFrequency(), program.getId().longValue(),
                                        program.getUuid(), tDComplex.getId(), therapyComplex.isMulltyFreq(),therapyComplex.getBundlesLength(), TextUtil.replaceWinPathBadSymbols(program.getName()),
                                        true, new File(tDComplex.getFile(), nameFile1));




                            }
                        }
                    }


                }
            }catch (Exception e)
            {
                logger.error("",e);
                Platform.runLater(() -> showExceptionDialog(res.getString("app.title87"), "", res.getString("app.title93"), e, getApp().getMainWindow(), Modality.WINDOW_MODAL));
                callFail("Ошибка копирования файлов");
                return false;
            }

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

        // fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        File dir= dirChooser.showDialog(getApp().getMainWindow());
        if(dir==null)return;
        if(!dir.exists()) {
            showWarningDialog(res.getString("app.upload_to_dir"),  res.getString("app.ui.dir_not_exist"), "", getApp().getMainWindow(), Modality.WINDOW_MODAL);
            return;}

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

/*
            Map<Long,String> pCMap=new LinkedHashMap<>();
           for (String s : profileParam.get(2).split("#"))
           {

                   String[] split = s.split("@");
                   pCMap.put(Long.parseLong(split[0]),split[1]);


           }

*/
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

            if(progrParam.size()!=8) {res=false;break;}
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

                    th=  getModel().createTherapyComplex(profile, complexFileDataMapEntry.getKey().getName().substring(ind2+1,ind), "", (int) complexFileDataMapEntry.getKey().getTimeForFreq(), complexFileDataMapEntry.getKey().isMullty(),complexFileDataMapEntry.getKey().getBundlesLength());



                    for (Map.Entry<Long, ProgramFileData> entry : complexFileDataMapEntry.getValue().entrySet())
                    {
                        if(entry.getValue().isMp3())   getModel().createTherapyProgramMp3(th,entry.getValue().getName(),"",entry.getValue().getFreqs());
                        else getModel().createTherapyProgram(th,entry.getValue().getName(),"",entry.getValue().getFreqs());

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

                    th=  getModel().createTherapyComplex(profile, complexFileDataMapEntry.getKey().getName(), "", (int) complexFileDataMapEntry.getKey().getTimeForFreq(), complexFileDataMapEntry.getKey().isMullty(),1);



                    for (Map.Entry<Long, ProgramFileData> entry : complexFileDataMapEntry.getValue().entrySet())
                    {
                        getModel().createTherapyProgram(th,entry.getValue().getName(),"",entry.getValue().getFreqs());

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



                        getModel().createTherapyComplex(profile, name, "", 300, true,1);


                    }else
                    {
                        Long next = programms.keySet().iterator().next();

                        TherapyComplex th = getModel().createTherapyComplex(profile, name, "", (int) programms.get(next).getTimeForFreq(), true,(int) programms.get(next).getBundlesLenght());


                        for (Map.Entry<Long, ProgramFileData> entry : programms.entrySet())
                        {
                            if(entry.getValue().isMp3()) getModel().createTherapyProgramMp3(th,entry.getValue().getName(),"",entry.getValue().getFreqs());
                            else getModel().createTherapyProgram(th,entry.getValue().getName(),"",entry.getValue().getFreqs());

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

        // fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

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
        // fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        List<File> files = fileChooser.showOpenMultipleDialog(getApp().getMainWindow());



        if(files==null)return;
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

            updateComplexTime(tableComplex.getSelectionModel().getSelectedItem(), false);





        } catch (Exception e) {
            logger.error("",e);
            showExceptionDialog("Ошибка создания программы", "","",e,getApp().getMainWindow(),Modality.WINDOW_MODAL);
        }
    }



    private void uploadComplexes() {
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
                //нужно найти есть ли в выбранной папке комплексы(соответствующие директории), если есть то дописать с верной нумерацией
                DirectoryChooser dirChooser = new DirectoryChooser();
                dirChooser.setTitle(this.res.getString("app.upload_to_dir"));
                final File dir = dirChooser.showDialog(getApp().getMainWindow());
                if(dir != null)
                {
                    if(!dir.exists()) {
                        showWarningDialog(res.getString("app.upload_to_dir"),  res.getString("app.ui.dir_not_exist"), "", getApp().getMainWindow(), Modality.WINDOW_MODAL);
                        return;}


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
                                    cMap.put(itm.getId(), TextUtil.replaceWinPathBadSymbols(itm.getName()) + " (" + AppController.this.replaceTime(timeTableColumn.getCellObservableValue(itm).getValue().toString()) + ")");
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
                                                mp3file = DateUtil.convertSecondsToHMmSs(therapyComplex.isMulltyFreq()?(long)therapyComplex.getTimeForFrequency().intValue():(long)(therapyProgram.getNumFreqs() * therapyComplex.getTimeForFrequency().intValue()));
                                                strb2 = new StringBuilder();
                                                ++count2;
                                                timeP = strb2.append(count2).append("-").append(TextUtil.replaceWinPathBadSymbols(therapyProgram.getName())).append(" (").append(AppController.this.replaceTime(mp3file)).append(")").toString();
                                                FilesProfileHelper.copyBSS(new File(BaseController.getApp().getDataDir(), therapyProgram.getId() + ".dat"), new File(tempFile, timeP + ".bss"));
                                                FilesProfileHelper.copyTxt(therapyProgram.getFrequencies(), (cMap2.get(entry.getKey())).intValue(), therapyProgram.getId().longValue(), therapyProgram.getUuid(), (entry.getKey()).longValue(), therapyComplex.isMulltyFreq(),therapyComplex.getBundlesLength(), TextUtil.replaceWinPathBadSymbols(therapyProgram.getName()), false, new File(tempFile, timeP + ".txt"));
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
                                                    String nameFile = strb2.append(count2).append("-").append(TextUtil.replaceWinPathBadSymbols(therapyProgram.getName())).append(" (").append(AppController.this.replaceTime(timeP)).append(")").toString();
                                                    FilesProfileHelper.copyBSS(new File(therapyProgram.getFrequencies()), new File(tempFile, nameFile + ".bss"));
                                                    FilesProfileHelper.copyTxt(therapyProgram.getFrequencies(), (cMap2.get(entry.getKey())).intValue(), therapyProgram.getId().longValue(), therapyProgram.getUuid(), (entry.getKey()).longValue(), therapyComplex.isMulltyFreq(),therapyComplex.getBundlesLength(), TextUtil.replaceWinPathBadSymbols(therapyProgram.getName()), true, new File(tempFile, nameFile + ".txt"));
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
            ArrayList<TherapyComplex> toGenerate = new ArrayList<>();
            toGenerate.addAll(selectedItems.stream().filter((i) -> i.isChanged()?true:this.getModel().hasNeedGenerateProgramInComplex(i)).collect(Collectors.toList()));
            this.tableComplex.getSelectionModel().clearSelection();
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

                                CalcLayer.closeLayer();
                                this.hideProgressBar(true);
                                encoder.removeActionListener();
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
                    Log.logger.error("", e);
                    showExceptionDialog(this.res.getString("app.title86"), "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);
                }

                System.out.println("Манипуляция сгенерированными комплексами");
                Profile profile = this.tableProfile.getSelectionModel().getSelectedItem();
                this.tableProfile.getSelectionModel().clearSelection();
                this.tableProfile.getSelectionModel().select(profile);
                this.tableProfile.getSelectionModel().getSelectedItem().setProfileWeight(this.tableProfile.getSelectionModel().getSelectedItem().getProfileWeight() + 1);
                this.checkUpploadBtn();
                this.therapyTabPane.getSelectionModel().select(this.tab2);
                toGenerate.forEach(i -> this.tableComplex.getSelectionModel().select(i));
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

        getModel().initStringsSection(userSection);
        FileChooser fileChooser =new FileChooser();
       fileChooser.setTitle(res.getString("ui.backup.create_backup"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
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
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
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
