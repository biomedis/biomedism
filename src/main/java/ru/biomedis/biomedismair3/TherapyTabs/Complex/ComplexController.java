package ru.biomedis.biomedismair3.TherapyTabs.Complex;

import com.mpatric.mp3agic.Mp3File;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import ru.biomedis.biomedismair3.*;
import ru.biomedis.biomedismair3.Dialogs.NameDescroptionDialogController;
import ru.biomedis.biomedismair3.Layouts.LeftPanel.LeftPanelAPI;
import ru.biomedis.biomedismair3.Layouts.LeftPanel.NamedTreeItem;
import ru.biomedis.biomedismair3.Layouts.ProgressPanel.ProgressAPI;
import ru.biomedis.biomedismair3.TherapyTabs.Profile.ProfileAPI;
import ru.biomedis.biomedismair3.TherapyTabs.Profile.ProfileTable;
import ru.biomedis.biomedismair3.TherapyTabs.Programs.ProgramTable;
import ru.biomedis.biomedismair3.TherapyTabs.TablesCommon;
import ru.biomedis.biomedismair3.UserUtils.Export.ExportTherapyComplex;
import ru.biomedis.biomedismair3.UserUtils.Import.ImportTherapyComplex;
import ru.biomedis.biomedismair3.entity.*;
import ru.biomedis.biomedismair3.utils.Audio.MP3Encoder;
import ru.biomedis.biomedismair3.utils.Date.DateUtil;
import ru.biomedis.biomedismair3.utils.Files.FilesProfileHelper;
import ru.biomedis.biomedismair3.utils.Files.ProgramFileData;
import ru.biomedis.biomedismair3.utils.Text.TextUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static ru.biomedis.biomedismair3.Log.logger;

public class ComplexController extends BaseController implements ComplexAPI{
    private static int MAX_BUNDLES = AppController.MAX_BUNDLES;
    @FXML    private TableView<TherapyComplex> tableComplex;
    @FXML private Button generationComplexesBtn;
    @FXML private Button  btnCreateTherapy;
    @FXML private Button btnDeleteTherapy;
    @FXML private Spinner<Double> timeToFreqSpinner;
    @FXML private Button  btnReadTherapy;
    @FXML private HBox spinnerPan;
    @FXML private VBox spinnerBtnPan;
    @FXML private Button  btnOkSpinner;
    @FXML private Button  btnCancelSpinner;


    @FXML private HBox bundlesPan;
    @FXML private Spinner<String> bundlesSpinner;

    //@FXML  private ObservableList<String>  bundlesSpinnerData;
    @FXML  private VBox bundlesBtnPan;
    @FXML private Button btnOkBundles;
    @FXML private Button btnCancelBundles;
    @FXML Button   uploadComplexesBtn;

    private ContextMenu uploadComplexesMenu=new ContextMenu();
    private ContextMenu readMenu = new ContextMenu();

    private ComplexTable complexTable;
    private List<TherapyComplex> therapyComplexesClipboard=new ArrayList<>();
    private LeftPanelAPI leftAPI;
    private  ProfileAPI profileAPI;
    private ResourceBundle res;
    private Image imageDone;
    private Image imageCancel;
    private TabPane therapyTabPane;

    private SimpleBooleanProperty m2Ready;//trinity
    private SimpleBooleanProperty m2Connected;//trinity
    private SimpleBooleanProperty connectedDevice;//biomedism


    public void setDevicesProperties(SimpleBooleanProperty m2Ready, SimpleBooleanProperty connectedDeviceProperty, SimpleBooleanProperty m2Connected){
        if( this.m2Ready!=null) return;
        this.connectedDevice =connectedDeviceProperty;
        this.m2Connected=m2Connected;
        this.m2Ready=m2Ready;
        initUploadComplexesContextMenu();
        initReadMenu();
    }

    @Override
    protected void onCompletedInitialise() {

    }

    @Override
    protected void onClose(WindowEvent event) {

    }

    @Override
    public void setParams(Object... params) {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        res = resources;
        leftAPI = getLeftAPI();
        profileAPI = getProfileAPI();
        initDoneCancelImages();

        btnCreateTherapy.disableProperty().bind(ProfileTable.getInstance().getSelectedItemProperty().isNull());
        btnDeleteTherapy.disableProperty().bind(tableComplex.getSelectionModel().selectedItemProperty().isNull());

        initSpinnersPaneVisibilityPolicy();
        baseInitSpinnerTimeForFreq();
        baseInitBundlesSpinner();

        complexTable = initComplexesTable();
        initGenerateComplexesButton();
        complexTable.initComplexesContextMenu(() -> getDevicePath()!=null,
                this::onPrintComplex,
                AppController::cutInTables,
                AppController::deleteInTables,
                this::copyTherapyComplexToBase,
                this::generateComplexes,
                this::uploadComplexesToDir,
                this::uploadComplexesToM,
                AppController::copyInTables,
                AppController::pasteInTables,
                AppController::pasteInTables_after,
                this::complexesToBiofon,
                ()->{
                    boolean res=true;
                    if(leftAPI.selectedSectionTree() != null) {
                        INamed value = leftAPI.selectedSectionTreeItem();
                        if(value instanceof Section) {
                            return ((Section) value).isOwnerSystem();
                        }
                    }
                    return res;

                });


        initComplexSelectedListener();
        initComplexSpinnerTimeForFreq();
        initComplexBundlesLength();

        tableComplex.setOnMouseClicked(event -> {
            if(event.getClickCount()==2) {
                event.consume();

                if(ProfileTable.getInstance().getSelectedItem()!=null)therapyTabPane.getSelectionModel().select(2);

            }
        });

        this.tableComplex.setOnKeyReleased((e) -> {
           // if(e.getCode() == KeyCode.DELETE) {
            //    this.onRemoveComplex();
            //}else

            if(e.getCode() == KeyCode.A && e.isControlDown()) {
                if(ComplexTable.getInstance().isTextEdited()) return;
                this.tableComplex.getSelectionModel().selectAll();
            }else

            if(e.getCode()==KeyCode.RIGHT  && !therapyTabPane.getTabs().get(2).isDisable()) {
                if(ComplexTable.getInstance().isTextEdited()) return;
                therapyTabPane.getSelectionModel().select(2);
                ProgramTable.getInstance().requestFocus();
                if( ProgramTable.getInstance().getAllItems().size()!=0){
                    ProgramTable.getInstance().setItemFocus( ProgramTable.getInstance().getSelectedIndex());

                }
            }else
            if(e.getCode()==KeyCode.LEFT  && !therapyTabPane.getTabs().get(0).isDisable()) {
                if(ComplexTable.getInstance().isTextEdited()) return;
                therapyTabPane.getSelectionModel().select(0);
                ProfileTable.getInstance().requestFocus();
                if(ProfileTable.getInstance().getAllItems().size()!=0){
                    ProfileTable.getInstance().setItemFocus(ProfileTable.getInstance().getSelectedIndex());
                }

            }});


    }
    private Supplier<Path> devicePathFunc;
    public void setDevicePathMethod(Supplier<Path> f)
    {
        devicePathFunc =f;
    }
    private Path getDevicePath(){return devicePathFunc.get();}

    public void setTherapyTabPane( TabPane pane){ therapyTabPane=pane;}



    private LeftPanelAPI getLeftAPI(){
       return AppController.getLeftAPI();
    }

    private ProfileAPI getProfileAPI(){return AppController.getProfileAPI();}

    private ProgressAPI getProgressAPI(){return AppController.getProgressAPI();}

    private void initSpinnersPaneVisibilityPolicy() {
        spinnerPan.visibleProperty().bind(tableComplex.getSelectionModel().selectedItemProperty().isNotNull());
        bundlesPan.visibleProperty().bind(tableComplex.getSelectionModel().selectedItemProperty().isNotNull());
    }

    private void baseInitBundlesSpinner() {
        //bundlesPan.setVisible(false);
        bundlesBtnPan.setVisible(false);
        URL okUrl = getClass().getResource("/images/ok.png");
        URL cancelUrl = getClass().getResource("/images/cancel.png");
        btnOkBundles.setGraphic(new ImageView(new Image(okUrl.toExternalForm())));
        btnCancelBundles.setGraphic(new ImageView(new Image(cancelUrl.toExternalForm())));
    }

    private void baseInitSpinnerTimeForFreq() {
        timeToFreqSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.5,10.0,3.0,0.5));
        timeToFreqSpinner.setEditable(false);
       // spinnerPan.setVisible(false);
        spinnerBtnPan.setVisible(false);
        URL okUrl = getClass().getResource("/images/ok.png");
        URL cancelUrl = getClass().getResource("/images/cancel.png");
        btnOkSpinner.setGraphic(new ImageView(new Image(okUrl.toExternalForm())));
        btnCancelSpinner.setGraphic(new ImageView(new Image(cancelUrl.toExternalForm())));
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
                        getModel().updateTherapyComplex(item);

                    }
                    updateComplexsTime(items, true);
                } catch (Exception var8) {
                    hideBundlesSpinnerBTNPan(this.tableComplex.getSelectionModel().getSelectedItem().getBundlesLength());
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

                ProgramTable.getInstance().getAllItems().clear();

                ProgramTable.getInstance().getAllItems().addAll(getModel().findTherapyPrograms(newValue));


            }


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

    private void initDoneCancelImages() {
        URL location = getClass().getResource("/images/done.png");
        imageDone=new Image(location.toExternalForm());
        location = getClass().getResource("/images/cancel.png");
        imageCancel=new Image(location.toExternalForm());
    }


    private void setComplexTabName(String val){
        Platform.runLater(() -> therapyTabPane.getTabs().get(1).setText(val));

    }
    private void initUploadComplexesContextMenu() {
        MenuItem toDirMI = new MenuItem(this.res.getString("app.upload_to_dir"));
        MenuItem toMMI = new MenuItem(this.res.getString("app.upload_to_biomedism"));
        MenuItem toFileMI = new MenuItem(this.res.getString("app.export_to_file"));

        toDirMI.setOnAction(event -> uploadComplexesToDir());
        toMMI.setOnAction(event -> uploadComplexesToM());
        toFileMI.setOnAction(event -> exportTherapyComplexes(complexTable.getSelectedItems()));

        uploadComplexesMenu.setOnShowing(event -> {
            toDirMI.setDisable(false);
            toMMI.setDisable(false);
            if(this.tableComplex.getSelectionModel().getSelectedItems().isEmpty()) {

                toDirMI.setDisable(true);
                toMMI.setDisable(true);
            } else{
                Iterator tag = this.tableComplex.getSelectionModel().getSelectedItems().iterator();

                while(tag.hasNext()) {
                    TherapyComplex therapyComplex = (TherapyComplex)tag.next();


                    if( therapyComplex.isChanged() || this.getModel().hasNeedGenerateProgramInComplex(therapyComplex) ) {
                        toDirMI.setDisable(true);
                        toMMI.setDisable(true);
                        break;
                    }
                }
                if(getDevicePath()!=null && !toMMI.isDisable() )   toMMI.setDisable(false);
                else  toMMI.setDisable(true);
            }
        });


        uploadComplexesMenu.getItems().addAll(new MenuItem[]{ toMMI, toDirMI,toFileMI });
        uploadComplexesBtn.setOnAction(event ->
                {
                    if(!uploadComplexesMenu.isShowing()) uploadComplexesMenu.show(uploadComplexesBtn, Side.BOTTOM, 0, 0);
                    else uploadComplexesMenu.hide();

                }
        );
    }


    private void updateComplexsTime(List<TherapyComplex> c, boolean reloadPrograms) {
        if(!reloadPrograms) {
            c.forEach(i ->i.setTime(i.getTime() + 1L));
            profileAPI.updateProfileTime(ProfileTable.getInstance().getSelectedItem());
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


            profileAPI.updateProfileTime(ProfileTable.getInstance().getSelectedItem());
        }
    }


    /**
     * Копирует выделенный комплекс с пользов базу, должен быть выделен раздел
     */
    private void copyTherapyComplexToBase()
    {
        ObservableList<TherapyComplex> therapyComplexes = this.tableComplex.getSelectionModel().getSelectedItems();
        NamedTreeItem treeItem = leftAPI.selectedSectionTree();
        if(!therapyComplexes.isEmpty() && treeItem != null) {
            Complex complex = null;

            try {
                Iterator<TherapyComplex> e = therapyComplexes.iterator();

                while(e.hasNext()) {
                    TherapyComplex itm = (TherapyComplex)e.next();
                    complex = this.getModel().createComplex(itm.getName(), itm.getDescription(), (Section)treeItem.getValue(), false, this.getModel().getUserLanguage(), itm.getTimeForFrequency());
                    this.getModel().initStringsComplex(complex);
                    Iterator<TherapyProgram> it = this.getModel().findTherapyPrograms(itm.getId()).stream().filter((s) ->!s.isMp3()).collect(
                            Collectors.toList()).iterator();

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


    public void onCreateComplex()
    {

        if(ProfileTable.getInstance().getSelectedItem()==null) return;
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
                TherapyComplex therapyComplex = getModel().createTherapyComplex("", ProfileTable.getInstance().getSelectedItem(), data.getNewName(), data.getNewDescription(), 180,3);

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


    private void initReadMenu(){
        MenuItem downM=new MenuItem(res.getString("app.menu.import_from_m_device"));
        downM.setDisable(true);
        downM.setOnAction(event ->  importComplexesFromBiomedisM());
        downM.disableProperty().bind(connectedDevice.not());

        MenuItem downDir=new MenuItem(res.getString("app.import_from_dir"));
        downDir.setDisable(false);
        downDir.setOnAction(event -> importComplexFromDir());

        MenuItem importFromFile = new MenuItem(res.getString("app.from_file"));
        importFromFile.setDisable(false);
        importFromFile.setOnAction(event -> App.getAppController().onImportTherapyComplex());

        readMenu.getItems().addAll(downM, downDir, importFromFile);

    }

    public void onReadTherapyComplexes(){
        if(!readMenu.isShowing()) readMenu.show(btnReadTherapy, Side.BOTTOM, 0, 0);
        else readMenu.hide();
    }

    private void uploadComplexesToDir(){
        uploadComplexes(null);
    }
    private void uploadComplexesToM(){

        if(getDevicePath()!=null){
            uploadComplexes(getDevicePath().toFile());
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

                profileAPI.updateProfileTime( ProfileTable.getInstance().getSelectedItem());
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
                            return i.isDirectory() && !i.getName().equalsIgnoreCase("System Volume Information") && TextUtil
                                    .match(i.getName(), "^([0-9]+)-.*$");
                        })).stream().mapToInt(i -> Integer.parseInt(i.getName().split("-")[0])).max();
                        if(max.isPresent()) {
                            e = max.getAsInt();
                        }
                        final int ee=e;
                        Task task = new Task() {
                            protected Boolean call() throws Exception {
                                Map<Long,String> cMap=new LinkedHashMap<>();
                                Map<Long,Integer> cMap2=new LinkedHashMap<>();

                                TableColumn<TherapyComplex, ?> timeTableColumn = (TableColumn)tableComplex.getColumns().get(3);
                                selectedItems.forEach((itm) -> {
                                    cMap.put(itm.getId(), TextUtil.replaceWinPathBadSymbols(itm.getName()) + " (" + DateUtil
                                            .replaceTime(timeTableColumn.getCellObservableValue(itm).getValue().toString(),res) + ")");
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
                                        Iterator<TherapyProgram> it2 = getModel().findTherapyPrograms(entry.getKey()).iterator();
                                        TherapyProgram therapyProgram;



                                        while(it2.hasNext()) {
                                            therapyProgram = it2.next();
                                            therapyComplex = getModel().findTherapyComplex(entry.getKey());

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
                                        BaseController.showWarningDialog(res.getString("app.title87"), "", res.getString("app.title115"), BaseController.getApp().getMainWindow(), Modality.WINDOW_MODAL);
                                    });
                                    return false;
                                } catch (Exception var18) {
                                    Log.logger.error("", var18);
                                    Platform.runLater(() -> {
                                        BaseController.showExceptionDialog(res.getString("app.title87"), "", res.getString("app.title93"), var18, BaseController.getApp().getMainWindow(), Modality.WINDOW_MODAL);
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
                    encoder.setOnScheduled(ev ->  getProgressAPI().setProgressBar(0.0D, this.res.getString("app.title83"), this.res.getString("app.title84")));

                    encoder.setOnFailed(event ->
                            Platform.runLater(() -> {
                                CalcLayer.closeLayer();
                                getProgressAPI().setProgressBar(100.0D, this.res.getString("app.error"), this.res.getString("app.title84"));
                                getProgressAPI().hideProgressBar(true);
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
                                getProgressAPI().setProgressBar(0.0D, this.res.getString("app.cancel"), this.res.getString("app.title84"));
                                getProgressAPI().hideProgressBar(true);
                                encoder.removeActionListener();
                            } else {
                                if(encoder.getValue().booleanValue()) {
                                    getProgressAPI().setProgressBar(100.0D, this.res.getString("app.title85"), this.res.getString("app.title84"));
                                } else {
                                    getProgressAPI().setProgressBar(100.0D, this.res.getString("app.cancel"), this.res.getString("app.title84"));
                                }

                                System.out.println("Манипуляция сгенерированными комплексами");
                                Profile profile =  ProfileTable.getInstance().getSelectedItem();
                                ProfileTable.getInstance().clearSelection();
                                ProfileTable.getInstance().select(profile);
                                //ProfileTable.getInstance().getSelectedItem().setProfileWeight( ProfileTable.getInstance().getSelectedItem().getProfileWeight() + 1);
                                profileAPI.updateProfileWeight(ProfileTable.getInstance().getSelectedItem());
                                profileAPI.checkUpploadBtn();


                                CalcLayer.closeLayer();
                                getProgressAPI().hideProgressBar(true);
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
                            getProgressAPI().setProgressBar(newValue.doubleValue(), encoder.getCurrentName(), this.res.getString("app.title84"));
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
    public void onRemoveComplex()
    {
       removeComplex();

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

    /**
     * Копирует комплексы в профиль биофона
     */
    private void  complexesToBiofon(List<TherapyComplex> tcs){
        AppController.getBiofonUIUtil().complexesToBiofon( tcs);
    }


    /*** API ***/
    @Override
    public void hideSpinners() {
        hideTFSpinnerBTNPan();
        hideBundlesSpinnerBTNPan();
    }

    @Override
    public void removeComplex() {
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

                    ProgramTable.getInstance().getAllItems().clear();
                    tableComplex.getSelectionModel().clearSelection();
                    profileAPI.updateProfileTime((Profile)ProfileTable.getInstance().getSelectedItem());
                    this.tableComplex.getSelectionModel().clearSelection();
                } catch (Exception var9) {
                    Log.logger.error("", var9);
                    showExceptionDialog("Ошибка удаления комплексов", "", "", var9, getApp().getMainWindow(), Modality.WINDOW_MODAL);
                }
            }

            selectedItems = null;
        }
    }

    @Override
    public void printComplex() {
        onPrintComplex();
    }

    /**
     * Обновит время комплекса, профиля и программ если указанно reloadPrograms
     * @param c терапевтический комплекс - инстанс из таблицы!!
     * @param reloadPrograms обновить время программ или нет, если нет то просто изменится время комплекса
     */
    @Override
    public void updateComplexTime(TherapyComplex c, boolean reloadPrograms)
    {
        if(!reloadPrograms)
        {
            c.setTime(c.getTime()+1);
            profileAPI.updateProfileTime(ProfileTable.getInstance().getSelectedItem());
            return;
        }

        int i = ComplexTable.getInstance().getAllItems().indexOf(c);
        if(i==-1){
            System.out.println("null передан в updateComplexTime");
            return;
        }
        ComplexTable.getInstance().getAllItems().set(i, null);
        ComplexTable.getInstance().getAllItems().set(i, c);
        ComplexTable.getInstance().select(i);
        profileAPI.updateProfileTime(ProfileTable.getInstance().getSelectedItem());


    }


    @Override
    public  void pasteTherapyComplexes() {
        if(ComplexTable.getInstance().getSelectedItems().size()>1){
            showWarningDialog(res.getString("app.ui.insertion_elements"),res.getString("app.ui.insertion_not_allowed"),res.getString("app.ui.ins_not_av_mess"),getApp().getMainWindow(),Modality.WINDOW_MODAL);
            return;
        }
        Clipboard clipboard=Clipboard.getSystemClipboard();

        if(clipboard.hasContent(ComplexTable.COMPLEX_COPY_ITEM))pasteTherapyComplexesByCopy();
        else pasteTherapyComplexesByCut();
    }

    @Override
    public void pasteTherapyComplexes_after() {
        ComplexTable.getInstance().clearSelection();
        pasteTherapyComplexes();
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
        System.out.println("pasteTherapyComplexesByCut ");
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
                //if ( ComplexTable.getInstance().getSelectedItems().isEmpty()) return;
                if(!ComplexTable.getInstance().getSelectedItems().isEmpty())if ( ComplexTable.getInstance().getSelectedItems().size()!=1) return;

                Integer[] indexes = (Integer[]) clipboard.getContent(ComplexTable.COMPLEX_CUT_ITEM_INDEX);
                if(indexes==null) return;
                if(indexes.length==0) return;
                List<Integer> ind = Arrays.stream(indexes).collect(Collectors.toList());
                int dropIndex = 0;
                if(ComplexTable.getInstance().getSelectedItems().size() ==  0){
                    dropIndex = ComplexTable.getInstance().getAllItems().size()-1;
                    if(dropIndex < 0) dropIndex = 0;
                }
                else dropIndex = ComplexTable.getInstance().getSelectedIndex();

                if(!TablesCommon.isEnablePaste(dropIndex,indexes)) {
                    showWarningDialog(res.getString("app.ui.moving_items"),"",res.getString("app.ui.can_not_move_to_pos"),getApp().getMainWindow(),Modality.WINDOW_MODAL);
                    return;
                }

                List<TherapyComplex> therapyComplexes = ind.stream().map(i->ComplexTable.getInstance().getAllItems().get(i)).collect(Collectors.toList());
                int startIndex=ind.get(0);//первый индекс вырезки
                int lastIndex=ind.get(ind.size()-1);

//элементы всегда будут оказываться выше чем индекс по которому вставляли, те визуально вставляются над выбираемым элементом
                if(ComplexTable.getInstance().getSelectedItems().size() == 0){

                    for (TherapyComplex i : therapyComplexes) {
                        ComplexTable.getInstance().getAllItems().remove(i);
                    }
                    ComplexTable.getInstance().getAllItems().addAll(therapyComplexes);

                }
                else if(dropIndex < startIndex){

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
                if (ComplexTable.getInstance().getSelectedItems().size() > 0 )dropIndex = ComplexTable.getInstance().getSelectedIndex();
                else if(ComplexTable.getInstance().getAllItems().size()==0) dropIndex=0;
                else if(ComplexTable.getInstance().getSelectedItems().size() ==0) dropIndex = -1;

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


    @Override
    public  void copySelectedTherapyComplexesToBuffer() {
        if (ComplexTable.getInstance().getSelectedItems().isEmpty()) return;
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.clear();
        content.put(ComplexTable.COMPLEX_COPY_ITEM,
                ComplexTable.getInstance().getSelectedItems().stream().map(i->i.getId()).collect(Collectors.toList()).toArray(new Long[0]));
        clipboard.setContent(content);

    }

    /**
     * Вырезать комплексы
     */
    @Override
    public void cutSelectedTherapyComplexesToBuffer() {
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

    @Override
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

    /**
     * Импорт комплексов из файла
     * @param profile
     * @param afterAction выполняется после всего, в случае успеха. Передается параметр ему кол-во импортированных комплексов
     */
    @Override
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


    private void importComplexesFromBiomedisM(){
        DirectoryChooser dirChooser =new DirectoryChooser();
        dirChooser.setTitle(res.getString("app.menu.read_complex_from_dir"));
        dirChooser.setInitialDirectory( getDevicePath().toFile());
        File dir= dirChooser.showDialog(getApp().getMainWindow());
        if(dir==null)return;
        importComplexFromDir(dir);
    }

    private void importComplexFromDir(File dir){

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
     * Импорт терап.комплексов из папки
     */
    @Override
    public void importComplexFromDir()
    {
        DirectoryChooser dirChooser =new DirectoryChooser();
        dirChooser.setTitle(res.getString("app.menu.read_complex_from_dir"));
        File dir= dirChooser.showDialog(getApp().getMainWindow());
        if(dir==null)return;

        importComplexFromDir(dir);
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

                    }

                }
            }

        }catch (Exception e){ logger.error("",e);return false;}

        if(programms==null) return false;
        else return true;
    }


}
