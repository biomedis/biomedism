package ru.biomedis.biomedismair3.Layouts.BiofonTab;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;
import ru.biomedis.biomedismair3.AppController;
import ru.biomedis.biomedismair3.BaseController;

import ru.biomedis.biomedismair3.entity.Profile;
import ru.biomedis.biomedismair3.entity.TherapyComplex;
import ru.biomedis.biomedismair3.entity.TherapyProgram;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Slf4j
public class BiofonTabController extends BaseController{
    private static final int MAX_BUNDLES = AppController.MAX_BUNDLES;
    @FXML
    ListView<TherapyComplex> biofonCompexesList;
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

    @FXML private Spinner<Integer> bundlesSpinnerBiofon;
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

    @FXML private Label biofonRedInd;
    @FXML private Label biofonBlueInd;
    @FXML private Label biofonGreenInd;
    @FXML private  ProgressIndicator biofonProgressIndicator;
    @FXML private  Button biofonBtnComplex1;
    @FXML private  Button biofonBtnComplex2;
    @FXML private  Button biofonBtnComplex3;

    private ContextMenu biofonComplexesMenu=new ContextMenu();

    private MenuItem biofonPrintMi=new MenuItem();
    private MenuItem biofonImportMi=new MenuItem();
    private MenuItem biofonExportMi=new MenuItem();
    private BiofonUIUtil biofonUIUtil;
    private ResourceBundle res;

    public BiofonUIUtil getBiofonUIUtil() {
        return biofonUIUtil;
    }

    @Override
    protected void onCompletedInitialization() {

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
        biofonUIUtil = initBiofon();
    }

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




        biofonExportMi.setOnAction(event -> exportTherapyComplexes.accept(biofonUIUtil.getSelectedComplexes()) );
        biofonImportMi.setOnAction(event -> {

            importTherapyComplex.accept(getApp().getBiofonProfile(), nums -> {
                if(nums==0) return;
                getModel().getLastTherapyComplexes(nums).forEach(therapyComplex ->  biofonUIUtil.addComplex(therapyComplex));
            });




        });

        biofonPrintMi.setOnAction(event -> printComplexes.accept(biofonUIUtil.getSelectedComplexes()));




        biofonComplexesMenu.getItems().addAll(biofonPrintMi,biofonImportMi,biofonExportMi);


    }

    private Consumer<List<TherapyComplex>> printComplexes;

    public void setPrintComplexesFunction(Consumer<List<TherapyComplex>> func){
        printComplexes = func;
    }


    private Consumer<List<TherapyComplex>> exportTherapyComplexes;

    public void setExportTherapyComplexesFunction(Consumer<List<TherapyComplex>> func){
        exportTherapyComplexes = func;
    }

    private BiConsumer<Profile,Consumer<Integer>> importTherapyComplex;

    public void setImportTherapyComplexFunction(BiConsumer<Profile,Consumer<Integer>> func){
        importTherapyComplex = func;
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
        bundlesSpinnerBiofon.getValueFactory().setValue(val);
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
                    log.error("", var8);
                    showExceptionDialog("Ошибка обновления времени на частоту в терапевтическом комплексе", "", "", var8, getApp().getMainWindow(), Modality.WINDOW_MODAL);
                } finally {
                    this.hideTFSpinnerBTNPanBiofon();
                }

            }
        });





/*******************/

        /** Комбо пачек частот **/


        ObservableList<Integer> bundlesSpinnerDataBiofon = FXCollections.observableArrayList();




        for(int i=2; i<=MAX_BUNDLES; i++)bundlesSpinnerDataBiofon.add(i);
        bundlesSpinnerBiofon.setValueFactory(new SpinnerValueFactory.ListSpinnerValueFactory<Integer>(bundlesSpinnerDataBiofon));
        bundlesSpinnerBiofon.getValueFactory().setValue(2);

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
            if(newValue.getBundlesLength()<2){
                newValue.setBundlesLength(2);
                try {
                    getModel().updateTherapyComplex(newValue);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
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

                    complex.setBundlesLength(bundlesSpinnerBiofon.getValue());
                    this.getModel().updateTherapyComplex(complex);
                    hideBundlesSpinnerBTNPanBiofon();
                }
                biofonUIUtil.viewComplexTime(biofonCompexesList.getSelectionModel().getSelectedItem(),biofonProgramsList.getItems());

            } catch (Exception e) {
                hideBundlesSpinnerBTNPanBiofon();
                log.error("Ошибка установки пачек частот", e);
                showExceptionDialog("Ошибка установки пачек частот", "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);
            }

        });





        spinnerPanBiofon.visibleProperty().bind(biofonCompexesList.getSelectionModel().selectedItemProperty().isNotNull());
        bundlesPanBiofon.visibleProperty().bind(biofonCompexesList.getSelectionModel().selectedItemProperty().isNotNull());

    }

    public SimpleBooleanProperty biofonConnected =new SimpleBooleanProperty(false);

    /**
     * Обработчик подключения биофона
     */
    void onAttachBiofon(){
        biofonRedInd.setVisible(true);
        biofonBlueInd.setVisible(true);
        biofonGreenInd.setVisible(true);
        biofonConnected.set(true);
    }

    /**
     * Обработчик отключения биофона
     */
    void onDetachBiofon(){
        biofonRedInd.setVisible(false);
        biofonBlueInd.setVisible(false);
        biofonGreenInd.setVisible(false);
        biofonConnected.set(false);
    }

    void showBiofonProgressIndicator(){
        biofonProgressIndicator.setVisible(true);
    }
    void hideBiofonProgressIndicator(){
        biofonProgressIndicator.setVisible(false);
    }



    private BiofonUIUtil initBiofon() {

        biofonRedInd.setVisible(false);
        biofonBlueInd.setVisible(false);
        biofonGreenInd.setVisible(false);

        uploadBiofonBtn.setDisable(true);

        BiofonUIUtil  biofonUIUtil=new BiofonUIUtil(res,
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

        return biofonUIUtil;
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
        bComplexMenu.setOnAction(event -> biofonComplexesMenu.show(bComplexMenu, Side.BOTTOM,0,0));

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
}
