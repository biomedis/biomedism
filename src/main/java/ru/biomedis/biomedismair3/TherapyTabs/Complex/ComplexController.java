package ru.biomedis.biomedismair3.TherapyTabs.Complex;

import com.mpatric.mp3agic.Mp3File;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import ru.biomedis.biomedismair3.*;
import ru.biomedis.biomedismair3.Dialogs.NameDescroptionDialogController;
import ru.biomedis.biomedismair3.Layouts.LeftPanel.LeftPanelAPI;
import ru.biomedis.biomedismair3.Layouts.LeftPanel.NamedTreeItem;
import ru.biomedis.biomedismair3.Layouts.ProgressPanel.ProgressAPI;
import ru.biomedis.biomedismair3.TherapyTabs.Profile.ProfileAPI;
import ru.biomedis.biomedismair3.TherapyTabs.Profile.ProfileTable;
import ru.biomedis.biomedismair3.TherapyTabs.Programs.ProgramTable;
import ru.biomedis.biomedismair3.entity.*;
import ru.biomedis.biomedismair3.utils.Audio.MP3Encoder;
import ru.biomedis.biomedismair3.utils.Date.DateUtil;
import ru.biomedis.biomedismair3.utils.Files.FilesProfileHelper;
import ru.biomedis.biomedismair3.utils.Text.TextUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
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

    private ComplexTable complexTable;
    private List<TherapyComplex> therapyComplexesClipboard=new ArrayList<>();
    private LeftPanelAPI leftAPI;
    private  ProfileAPI profileAPI;
    private ResourceBundle res;
    private Image imageDone;
    private Image imageCancel;
    private TabPane therapyTabPane;

    @Override
    protected void onCompletedInitialise() {

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
                cutInTables,
                deleteInTables,
                this::copyTherapyComplexToBase,
                this::generateComplexes,
                this::uploadComplexesToDir,
                this::uploadComplexesToM,
                copyInTables,
                pasteInTables,
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
        initUploadComplexesContextMenu();
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
            if(e.getCode() == KeyCode.DELETE) {
                this.onRemoveComplex();
            }else

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

    Runnable cutInTables;
    public void setCutInTables(Runnable f){
        cutInTables =f;
    }
    Runnable pasteInTables;
    public void setPasteInTables(Runnable f){
        pasteInTables = f;
    }
    Runnable deleteInTables;
    public void setDeleteInTable(Runnable f){
        deleteInTables =f;
    }

    Runnable copyInTables;
    public void setCopyInTable(Runnable f){
        copyInTables =f;
    }

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
                        profileAPI.enableGenerateBtn();
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
                        profileAPI.enableGenerateBtn();
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
                if(getDevicePath()!=null && !mic5_.isDisable() )   mic5_.setDisable(false);
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
                    complex = this.getModel().createComplex(itm.getName(), itm.getDescription(), (Section)treeItem.getValue(), false, this.getModel().getUserLanguage());
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
                TherapyComplex therapyComplex = getModel().createTherapyComplex("", ProfileTable.getInstance().getSelectedItem(), data.getNewName(), data.getNewDescription(), 300,3);

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
                                ProfileTable.getInstance().getSelectedItem().setProfileWeight( ProfileTable.getInstance().getSelectedItem().getProfileWeight() + 1);
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
}
