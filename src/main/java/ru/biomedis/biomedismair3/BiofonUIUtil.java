package ru.biomedis.biomedismair3;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.Event;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import ru.biomedis.biomedismair3.Biofon.Biofon;
import ru.biomedis.biomedismair3.Biofon.BiofonBinaryFile;
import ru.biomedis.biomedismair3.Biofon.BiofonComplex;
import ru.biomedis.biomedismair3.Biofon.BiofonProgram;
import ru.biomedis.biomedismair3.Dialogs.NameDescroptionDialogController;
import ru.biomedis.biomedismair3.entity.Profile;
import ru.biomedis.biomedismair3.entity.TherapyComplex;
import ru.biomedis.biomedismair3.entity.TherapyProgram;
import ru.biomedis.biomedismair3.utils.Date.DateUtil;
import ru.biomedis.biomedismair3.utils.USB.PlugDeviceListener;
import ru.biomedis.biomedismair3.utils.USB.USBHelper;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static ru.biomedis.biomedismair3.AppController.checkBundlesLength;
import static ru.biomedis.biomedismair3.Log.logger;

/**
 * Created by anama on 09.12.16.
 */
public class BiofonUIUtil {

    private ResourceBundle resource;
    private App app;
    private BaseController bc;
    private ModelDataApp mda;
    private Profile biofonProfile;
    private final ListView<TherapyComplex> biofonCompexesList;
    private final ListView<TherapyProgram> biofonProgramsList;
    private final Label complexOName;
    private final Label programOName;
    private final Runnable onAttach;
    private final Runnable onDetach;
    private final Label tFInfo;
    private final Label bundlesInfo;
    private Label countProgramsBiofonInfo;
    private Label complexTimeBiofon;
    private final Button loadIndicator;

    private Image biofonComplexImage;
    private ImageView biofonComplexImageView;

    private ObservableList<TherapyComplex> biofonComplexes = FXCollections.observableArrayList(param -> new Observable[]{param.nameProperty()});
    private SortedList<TherapyComplex> biofonComplexesSorted = new SortedList<>(biofonComplexes);
    private Comparator<TherapyComplex> comparatorBiofonComplexByName = Comparator.comparing(TherapyComplex::getName);
    private Comparator<TherapyComplex> comparatorBiofonComplexByTime = Comparator.comparing(TherapyComplex::getId);

    private ObservableList<TherapyProgram> biofonPrograms = FXCollections.observableArrayList(param -> new Observable[]{param.positionProperty()});
    private SortedList<TherapyProgram> biofonProgramsSorted = new SortedList<>(biofonPrograms);
    private Comparator<TherapyProgram> comparatorBiofonProgram = Comparator.comparing(TherapyProgram::getPosition);




    private Tooltip tooltipComplex = new Tooltip();
    private Button bDeviceComplex1;
    private Button bDeviceComplex2;
    private Button bDeviceComplex3;
    private Button uploadBiofonBtn;

    private ContextMenu deviceBtnMenu1=new ContextMenu();
    private ContextMenu deviceBtnMenu2=new ContextMenu();
    private ContextMenu deviceBtnMenu3=new ContextMenu();
    private static final int PAUSE_BETWEEN_PROGRAM=5;

    public BiofonUIUtil(ResourceBundle resource, App app, BaseController bc, ModelDataApp mda, Profile biofonProfile,
                        ListView<TherapyComplex> biofonCompexesList, ListView<TherapyProgram> biofonProgramsList,
                        Label complexOName,Label programOName,
                        Runnable onAttach, Runnable onDetach,
                        Label tFInfo, Label bundlesInfo,Label countProgramsBiofonInfo,Label complexTimeBiofon, Button loadIndicator) {
        this.resource = resource;
        this.app = app;
        this.bc = bc;
        this.mda = mda;
        this.biofonProfile = biofonProfile;
        this.biofonCompexesList = biofonCompexesList;
        this.biofonProgramsList = biofonProgramsList;


        this.complexOName = complexOName;
        this.programOName = programOName;
        this.onAttach = onAttach;
        this.onDetach = onDetach;
        this.tFInfo = tFInfo;
        this.bundlesInfo = bundlesInfo;
        this.countProgramsBiofonInfo = countProgramsBiofonInfo;
        this.complexTimeBiofon = complexTimeBiofon;
        this.loadIndicator = loadIndicator;
    }

    /**
     * Перезагрузить список комплексов в таблице
     */
    public void reloadComplexes(){

        biofonComplexes.clear();
        List<TherapyComplex> allTherapyComplexByProfile = mda.findAllTherapyComplexByProfile(biofonProfile);
        try {
            checkBundlesLength(biofonComplexes);
            biofonComplexes.addAll(allTherapyComplexByProfile);
        } catch (Exception e) {
            Log.logger.error("",e);
            bc.showExceptionDialog("Ошибка обновления комплексов","","",e,bc.getControllerWindow(),Modality.WINDOW_MODAL);
            return;
        }

    }





    private SimpleObjectProperty<TherapyComplex> bComplex1=new SimpleObjectProperty<>();
    private SimpleObjectProperty<TherapyComplex> bComplex2=new SimpleObjectProperty<>();
    private SimpleObjectProperty<TherapyComplex> bComplex3=new SimpleObjectProperty<>();


    static final DataFormat COMPLEXES__DATA_FORMAT = new DataFormat("biofon/complex");
    private Image dragImage;

    private void setInfo(TherapyComplex tc){
        if(tc==null) {hideInfo();return;}
        tFInfo.getParent().setVisible(true);
        bundlesInfo.getParent().setVisible(true);
        tFInfo.setText(tc.getTimeForFrequency()/60+"");
        bundlesInfo.setText(tc.getBundlesLength()==1?"-":String.valueOf(tc.getBundlesLength()));
    }

    private void hideInfo(){
        tFInfo.getParent().setVisible(false);
        bundlesInfo.getParent().setVisible(false);

    }
    private  void viewComplexPrograms(TherapyComplex tc) {

            if(tc ==null) return;

            biofonPrograms.clear();
            if(tc.getId().intValue()==0){

                if(tc == bComplex1.get()) biofonPrograms.addAll(bReadedTherapyPrograms.get(0));
                else  if(tc == bComplex2.get()) biofonPrograms.addAll(bReadedTherapyPrograms.get(1));
                else if(tc == bComplex3.get()) biofonPrograms.addAll(bReadedTherapyPrograms.get(2));


            }else  biofonPrograms.addAll(mda.findTherapyPrograms(tc));

        viewComplexTime(tc,biofonPrograms);

            if(!tc.getOname().isEmpty()) complexOName.setText(tc.getOname());
            else complexOName.setText("");
            programOName.setText("");

        //viewProgramCount();



    }

    public  void viewComplexTime(TherapyComplex tc,List<TherapyProgram> tpl){

            complexTimeBiofon.setText(DateUtil.convertSecondsToHMmSs(calcComplexTime(biofonPrograms,
                    tc.getBundlesLength(),
                    PAUSE_BETWEEN_PROGRAM,
                    tc.getTimeForFrequency())));
    }

    private int calcComplexTime(List<TherapyProgram> tpl,int bundlesLength,int pauseBetweenProgramm,int timeForFreq){
        // количество программ

        int resTimeSec=0;
        for (TherapyProgram tp : tpl) {
            if(bundlesLength!=1){
                resTimeSec += splitFreqsByBundles(tp.parseFreqs(), bundlesLength).size()*(timeForFreq + pauseBetweenProgramm);

            }else {
                resTimeSec +=(timeForFreq+pauseBetweenProgramm);
            }

        }
        if(resTimeSec==0) return 0;
        else  return resTimeSec-pauseBetweenProgramm;
    }

    private void viewProgramCount() {

        countProgramsBiofonInfo.setText(String.valueOf(biofonPrograms.size()));
        if(biofonPrograms.size()> BiofonComplex.MAX_PROGRAM_COUNT_IN_COMPLEX) countProgramsBiofonInfo.setStyle("-fx-background-color: red;");
        else countProgramsBiofonInfo.setStyle("");
    }

    private void on3ComplexButtonClick(Event e){

        if(e.getTarget() instanceof Button) {
            Button btn = (Button) e.getTarget();
            programOName.setText("");
            biofonCompexesList.getSelectionModel().clearSelection();

            if (btn.getId().equals(bDeviceComplex1.getId())) {
                viewComplexPrograms(bComplex1.get());
                setInfo(bComplex1.get());

            } else if (btn.getId().equals(bDeviceComplex2.getId())) {
                viewComplexPrograms(bComplex2.get());
                setInfo(bComplex2.get());
            } else if (btn.getId().equals(bDeviceComplex3.getId())) {
                viewComplexPrograms(bComplex3.get());
                setInfo(bComplex3.get());
            }

        }
    }

    public void init3ComplexesButtons(Button bDeviceComplexBtn1,Button bDeviceComplexBtn2,Button bDeviceComplexBtn3){
        URL dragImgUrl = getClass().getResource("/images/medical_record.png");
        dragImage = new Image(dragImgUrl.toExternalForm());

        this.bDeviceComplex1 = bDeviceComplexBtn1;
        this.bDeviceComplex2 = bDeviceComplexBtn2;
        this.bDeviceComplex3 = bDeviceComplexBtn3;

        this.bDeviceComplex1.setWrapText(true);
        this.bDeviceComplex2.setWrapText(true);
        this.bDeviceComplex3.setWrapText(true);

        this.bDeviceComplex1.setOnAction(this::on3ComplexButtonClick);
        this.bDeviceComplex2.setOnAction(this::on3ComplexButtonClick);
        this.bDeviceComplex3.setOnAction(this::on3ComplexButtonClick);

        initComplexButtons(true);



        biofonCompexesList.setOnDragDetected(event -> {
            int selectedCount = biofonCompexesList.getSelectionModel().getSelectedIndices().size();
            if (selectedCount == 0) {
                event.consume();
                return;
            }
            // Initiate a drag-and-drop gesture
            Dragboard dragboard = biofonCompexesList.startDragAndDrop(TransferMode.COPY);
            // Put the the selected items to the dragboard
            TherapyComplex selectedItems = biofonCompexesList.getSelectionModel().getSelectedItem();
            ClipboardContent content = new ClipboardContent();
            content.put(COMPLEXES__DATA_FORMAT, selectedItems.getId());
            dragboard.setContent(content);
            dragboard.setDragView(dragImage);
            event.consume();



        });



        bDeviceComplex1.setOnDragOver(this::onDragOverComplexesBtn);
        bDeviceComplex2.setOnDragOver(this::onDragOverComplexesBtn);
        bDeviceComplex3.setOnDragOver(this::onDragOverComplexesBtn);

        bDeviceComplex1.setOnDragDropped(this::onDragDroppedComplexesBtn);
        bDeviceComplex2.setOnDragDropped(this::onDragDroppedComplexesBtn);
        bDeviceComplex3.setOnDragDropped(this::onDragDroppedComplexesBtn);


        initContextMenuDeviceButtons();


    }
    private void btnActionDev(MenuItem mi) {

        Integer indexBtn = (Integer) mi.getUserData();
        if (indexBtn.intValue() == 1) BiofonUIUtil.this.toEditComplex(bComplex1.get(), indexBtn.intValue());
        else if (indexBtn.intValue() == 2)
            BiofonUIUtil.this.toEditComplex(bComplex2.get(), indexBtn.intValue());
        else if (indexBtn.intValue() == 3)
            BiofonUIUtil.this.toEditComplex(bComplex3.get(), indexBtn.intValue());
    }
    private void initContextMenuDeviceButtons() {




        MenuItem mi1=new MenuItem(resource.getString("app.edit"));
        MenuItem mi2=new MenuItem(resource.getString("app.edit"));
        MenuItem mi3=new MenuItem(resource.getString("app.edit"));


        mi1.setOnAction(e->btnActionDev(mi1));
        mi2.setOnAction(e->btnActionDev(mi2));
        mi3.setOnAction(e->btnActionDev(mi3));
        deviceBtnMenu1.getItems().addAll(mi1);
       deviceBtnMenu2.getItems().addAll(mi2);
        deviceBtnMenu3.getItems().addAll(mi3);

        bDeviceComplex1.setContextMenu(deviceBtnMenu1);
        bDeviceComplex2.setContextMenu(deviceBtnMenu2);
        bDeviceComplex3.setContextMenu(deviceBtnMenu3);

        deviceBtnMenu1.setOnShowing(event -> {

            if(bComplex1==null || bComplex1.getValue().getId().intValue()!=0) {
                mi1.setDisable(true);
            }
            else {
                mi1.setUserData(1);
                mi1.setDisable(false);

            }
        });
        deviceBtnMenu2.setOnShowing(event -> {

            if(bComplex2==null || bComplex2.getValue().getId().intValue()!=0) {
                mi2.setDisable(true);
            }
            else {
                mi2.setUserData(2);
                mi2.setDisable(false);

            }
        });
        deviceBtnMenu3.setOnShowing(event -> {

            if(bComplex3==null || bComplex3.getValue().getId().intValue()!=0) {
                mi3.setDisable(true);
            }
            else {
                mi3.setUserData(3);
                mi3.setDisable(false);

            }
        });

    }

    /**
     * Редактировать комплекс из кнопки устройства.
     * Перенесет его в базу с программами. Можно будет отредактировать
     *
     */
    private void toEditComplex(TherapyComplex therapyComplex,int indbtn) {

        try {
            TherapyComplex tc = mda.createTherapyComplex(app.getBiofonProfile(),
                    therapyComplex.getName(),
                    "",
                    therapyComplex.getTimeForFrequency(),

                    therapyComplex.getBundlesLength());

            List<TherapyProgram> programList = bReadedTherapyPrograms.get(indbtn-1);
            for (TherapyProgram tp : programList) mda.createTherapyProgram(tc,tp.getName(),tp.getDescription(),tp.getFrequencies());


            if(indbtn==1)bComplex1.get().setId(tc.getId());
            else if(indbtn==2)bComplex2.get().setId(tc.getId());
            else if(indbtn==3)bComplex3.get().setId(tc.getId());

            biofonComplexes.add(tc);
            biofonCompexesList.getSelectionModel().select(tc);
            biofonCompexesList.getFocusModel().focus( biofonCompexesList.getSelectionModel().getSelectedIndex());
            biofonCompexesList.scrollTo(tc);



        } catch (Exception e) {
           Log.logger.error("Ошибка создание комплекса или программ в базе",e);
           bc.showExceptionDialog("Редактирование комплекса с устройства","Ошибка","",e,app.getMainWindow(),Modality.WINDOW_MODAL);
        }


    }


    private void onDragDroppedComplexesBtn(DragEvent e){

        if(e.getGestureTarget() instanceof Button){
            Button btn =(Button)e.getGestureTarget();
            if(!btn.getStyleClass().contains("OverBorder")){
                btn.getStyleClass().remove("OverBorder");
            }

            boolean dragCompleted = false;
            // Transfer the data to the target
            Dragboard dragboard = e.getDragboard();

            if(dragboard.hasContent(COMPLEXES__DATA_FORMAT)) {
                Long draggedComplexID = (Long) dragboard.getContent(COMPLEXES__DATA_FORMAT);


                TherapyComplex draggedComplex = mda.findTherapyComplex(draggedComplexID);

                if(draggedComplex.getTimeForFrequency()/60>BiofonComplex.MAX_TIME_BY_FREQ) draggedComplex.setTimeForFrequency(BiofonComplex.MAX_TIME_BY_FREQ);
                if(draggedComplex!=null){


                    btn.setText(draggedComplex.getName());
                    btn.getStyleClass().remove("GrayBackground");

                    if(btn.getId().equals(bDeviceComplex1.getId())) {
                        if( !btn.getStyleClass().contains("RedBackground"))btn.getStyleClass().addAll("RedBackground");

                        bComplex1.setValue(draggedComplex);
                    }
                    else  if(btn.getId().equals(bDeviceComplex2.getId())) {
                        if( !btn.getStyleClass().contains("GreenBackground")) btn.getStyleClass().addAll("GreenBackground");
                        bComplex2.setValue(draggedComplex);
                    }
                    else  if(btn.getId().equals(bDeviceComplex3.getId())) {
                        if( !btn.getStyleClass().contains("BlueBackground")) btn.getStyleClass().addAll("BlueBackground");
                        bComplex3.setValue(draggedComplex);
                    }

                    dragCompleted = true;
                }


            }

            e.setDropCompleted(dragCompleted);
            e.consume();



        }

    }

    private void onDragOverComplexesBtn(DragEvent e){
        // If drag board has an ITEM_LIST and it is not being dragged
        // over itself, we accept the MOVE transfer mode
                Dragboard dragboard = e.getDragboard();
                if (e.getGestureSource() == biofonCompexesList && dragboard.hasContent(COMPLEXES__DATA_FORMAT)) {
                    e.acceptTransferModes(TransferMode.COPY);
                    if(e.getGestureTarget() instanceof Button){
                        Button btn =(Button)e.getGestureTarget();
                        if(!btn.getStyleClass().contains("OverBorder")){
                            btn.getStyleClass().add("OverBorder");
                        }

                    }
                }
        e.consume();
    }


    private void loadIndicatorCalc(){
        //-
    }


    private Tooltip loadIndicatorTooltip=new Tooltip();

private enum LoadIndicatorType{RED,GREEN}
    private void showLoadIndicator(LoadIndicatorType t){
        if(t==LoadIndicatorType.RED){
            if(!loadIndicator.getStyleClass().contains("RedBackground")) {
                loadIndicator.getStyleClass().add("RedBackground");
            }
            if(loadIndicator.getStyleClass().contains("GreenBackground")) {
                loadIndicator.getStyleClass().remove("GreenBackground");
            }
        }else {
            if(!loadIndicator.getStyleClass().contains("GreenBackground")) {
                loadIndicator.getStyleClass().add("GreenBackground");
            }
            if(loadIndicator.getStyleClass().contains("RedBackground")) {
                loadIndicator.getStyleClass().remove("RedBackground");
            }
        }
        loadIndicator.setVisible(true);
    }
    private void hideLoadIndicator(){
        loadIndicator.setVisible(false);
    }
    public void init() {
        loadIndicator.setVisible(false);
        loadIndicator.setTooltip(loadIndicatorTooltip);
        loadIndicatorTooltip.setOnShowing(event -> {
            if(loadIndicator.getStyleClass().contains("GreenBackground")){
                loadIndicatorTooltip.setText("Все хорошо");
            }else {
                loadIndicatorTooltip.setText("Слишком большой файл");
            }
        });
        initUSBDetection();

        hideInfo();


        biofonPrograms.addListener((ListChangeListener<TherapyProgram>) c -> {
            viewProgramCount();
            if(!biofonPrograms.isEmpty())viewComplexTime(biofonPrograms.get(0).getTherapyComplex(),biofonPrograms);

        });

        countProgramsBiofonInfo.getParent().visibleProperty().bind(new BooleanBinding() {
            {
                super.bind(biofonPrograms);
            }
            @Override
            protected boolean computeValue() {

                return biofonPrograms.size()!=0;

            }
        });

        complexTimeBiofon.getParent().visibleProperty().bind( countProgramsBiofonInfo.getParent().visibleProperty());


        biofonCompexesList.setPlaceholder(new Label(resource.getString("app.table.complex_placeholder")));
        biofonProgramsList.setPlaceholder(new Label(resource.getString("app.table.programm_placeholder")));


        this.biofonCompexesList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        URL location = getClass().getResource("/images/medical_record.png");
        biofonComplexImage = new Image(location.toExternalForm());
        biofonComplexImageView = new ImageView(biofonComplexImage);


       // biofonComplexes.addAll(mda.findAllTherapyComplexByProfile(biofonProfile));
        List<TherapyComplex> allTherapyComplexByProfile = mda.findAllTherapyComplexByProfile(biofonProfile);
        try {
            checkBundlesLength(biofonComplexes);
            biofonComplexes.addAll(allTherapyComplexByProfile);
        } catch (Exception e) {
            Log.logger.error("",e);
            bc.showExceptionDialog("Ошибка обновления комплексов","","",e,bc.getControllerWindow(),Modality.WINDOW_MODAL);
            return;
        }

        biofonCompexesList.setCellFactory(param -> new ListCell<TherapyComplex>() {
            private ImageView imgv;

            @Override
            protected void updateItem(TherapyComplex item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setText(null);
                    setGraphic(null);
                    return;
                } else {
                    this.setText(item.getName());
                    if (imgv == null) imgv = new ImageView(biofonComplexImage);

                    setGraphic(imgv);
                }


            }

        });

        biofonProgramsList.setCellFactory(param -> new ListCell<TherapyProgram>() {
            private Text name;
            private Text freqs;
            private VBox vbox;

            @Override
            protected void updateItem(TherapyProgram item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setText(null);
                    setGraphic(null);
                    return;
                } else {
                    if(vbox==null){
                        name  = new Text(item.getName());
                        name.setFont(Font.font(null, FontWeight.BOLD, 12));
                        freqs = new Text(item.getFrequencies().replace(";","; ").replace("+","; "));
                        freqs.setWrappingWidth(getListView().getWidth()-30); // Setting the wrapping width to the Text
                        freqs.wrappingWidthProperty().bind(getListView().widthProperty().subtract(30));
                        vbox=new VBox();
                        vbox.getChildren().addAll(name,freqs);
                        vbox.setSpacing(4);


                    }else {
                        name.setText(item.getName());
                        freqs.setText(item.getFrequencies().replace(";","; ").replace("+","; "));
                    }

                    setGraphic(vbox);
                }


            }

        });

        biofonComplexesSorted.setComparator(comparatorBiofonComplexByTime);
        biofonCompexesList.setItems(biofonComplexesSorted);


        biofonProgramsList.setItems(biofonProgramsSorted);
        biofonProgramsSorted.setComparator(comparatorBiofonProgram);

        biofonCompexesList.getSelectionModel()
                          .selectedItemProperty()
                          .addListener((observable, oldValue, newValue) -> viewBiofonProgramsOnComplexClick());


        biofonCompexesList.setOnMouseClicked(event -> {
            tooltipComplex.hide();
            if (event.getClickCount() == 2) {
                TherapyComplex selectedItem = biofonCompexesList.getSelectionModel().getSelectedItem();
                if (selectedItem == null) return;

                if (selectedItem.getDescription().isEmpty()) return;
                tooltipComplex.setText(selectedItem.getDescription());

                tooltipComplex.show(biofonCompexesList, event.getScreenX(), event.getScreenY());
            }
        });


        //program Oname
        biofonProgramsList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue==null) return;
            if(!newValue.getOname().isEmpty()) programOName.setText(newValue.getOname());
            else programOName.setText("");

        });

        tooltipComplex.setHideOnEscape(true);
        tooltipComplex.setAutoHide(true);


    }


    private void initComplexButtons(boolean disable){
        bDeviceComplex1.setDisable(disable);
        bDeviceComplex1.setText(resource.getString("app.empty"));
        if(!bDeviceComplex1.getStyleClass().contains("GrayBackground"))bDeviceComplex1.getStyleClass().add("GrayBackground");
        bDeviceComplex1.setText(resource.getString("app.empty"));

        bDeviceComplex2.setDisable(disable);
        bDeviceComplex2.setText(resource.getString("app.empty"));
        if(!bDeviceComplex2.getStyleClass().contains("GrayBackground"))bDeviceComplex2.getStyleClass().add("GrayBackground");
        bDeviceComplex2.setText(resource.getString("app.empty"));

        bDeviceComplex3.setDisable(disable);
        bDeviceComplex3.setText(resource.getString("app.empty"));
        if(!bDeviceComplex3.getStyleClass().contains("GrayBackground"))bDeviceComplex3.getStyleClass().add("GrayBackground");
        bDeviceComplex3.setText(resource.getString("app.empty"));
    }



    private void initUSBDetection() {
        USBHelper.addPlugEventHandler(Biofon.productId, Biofon.vendorId, new PlugDeviceListener() {
            @Override
            public void onAttachDevice() {
                System.out.println("Устройство Biofon подключено");

                Platform.runLater(() -> initComplexButtons(false));

                bComplex1.setValue(null);
                bComplex2.setValue(null);
                bComplex3.setValue(null);


               onAttach.run();

                    onLoad();

            }

            @Override
            public void onDetachDevice() {

                System.out.println("Устройство Biofon отключено");

               Platform.runLater(() -> initComplexButtons(true));
                Platform.runLater(() ->{
                    if(biofonCompexesList.getSelectionModel().getSelectedItem()==null) biofonPrograms.clear();
                    hideInfo();
                });
                bComplex1.setValue(null);
                bComplex2.setValue(null);
                bComplex3.setValue(null);

                onDetach.run();
            }
        });
    }


    public ObservableList<TherapyComplex> getSelectedComplexes() {
        return biofonCompexesList.getSelectionModel().getSelectedItems();
    }

        //еще ест обработчик в appController, описан ниже настроек кнопок времени на частоту
    public void viewBiofonProgramsOnComplexClick() {

        hideInfo();
        programOName.setText("");
        ObservableList<TherapyComplex> selectedItems = getSelectedComplexes();
        biofonPrograms.clear();
        if (selectedItems.isEmpty()) return;

        if (selectedItems.size() == 1) {
            biofonPrograms.addAll(mda.findTherapyPrograms(selectedItems.get(0)));
            if(!selectedItems.get(0).getOname().isEmpty()) complexOName.setText(selectedItems.get(0).getOname());
            else complexOName.setText("");
        }

        //viewProgramCount();

    }

    public void complexesToBiofon(List<TherapyComplex> tcs) {
        try {
        for (TherapyComplex tc : tcs) {

            int timeForFrequency = tc.getTimeForFrequency();
            tc.setTimeForFrequency((int)Math.round(timeForFrequency/60.0)*60);
            TherapyComplex therapyComplex = mda.copyTherapyComplexToProfile(app.getBiofonProfile(), tc);
            tc.setTimeForFrequency(timeForFrequency);
            addComplex(therapyComplex);


        }
        } catch (Exception e) {
            BaseController.showExceptionDialog("Ошибка копирования  комплексов", "", "", e, app.getMainWindow(),
                    Modality.WINDOW_MODAL);
            return;
        }

    }


    private SimpleBooleanProperty disableUploadBtn=new SimpleBooleanProperty(true);

    public void initUpload(Button uploadBiofonBtn) {
        this.uploadBiofonBtn = uploadBiofonBtn;
       // this.uploadBiofonBtn.setDisable(true);

        this.uploadBiofonBtn.disableProperty().bind(bComplex1.isNull().or(bComplex2.isNull()).or(bComplex3.isNull()).and(disableUploadBtn));

        this.uploadBiofonBtn.setOnAction(this::onUpload);

    }




    private void onUpload(Event e){

            if(bComplex1.get() ==null || bComplex2.get()==null || bComplex3.get()==null) return;

        disableUploadBtn.set(true);

        try {
            BiofonBinaryFile file = createFile(bComplex1.get(), bComplex2.get(), bComplex3.get());
            if(file==null) throw new Exception("Отсутствуют комплексы");

            Biofon.writeToDevice(file);
            bc.showInfoDialog(resource.getString("app.ui.record_to_device"), resource.getString("app.ui.complexes_successfully_written"),"",
                    app.getMainWindow(),Modality.WINDOW_MODAL);
        } catch (BiofonBinaryFile.MaxBytesBoundException e1) {

            bc.showWarningDialog(resource.getString("app.ui.record_to_device"),
                    resource.getString("app.ui.mani_data_in_complexes"),resource.getString("app.ui.reduce_programs"),
                    app.getMainWindow(),Modality.WINDOW_MODAL);

        } catch (BiofonComplex.ZeroCountProgramBoundException e1) {

            bc.showWarningDialog(resource.getString("app.ui.record_to_device"),resource.getString("app.ui.must_have_program"),"",
                    app.getMainWindow(),Modality.WINDOW_MODAL);

        } catch (Biofon.WriteToDeviceException e1) {
            Log.logger.error("Ошибка записи в биофон",e1);
            bc.showExceptionDialog(resource.getString("app.ui.record_to_device"),resource.getString("app.ui.write_error"),"",e1,
                    app.getMainWindow(),Modality.WINDOW_MODAL);
        } catch (BiofonComplex.MaxPauseBoundException e1) {
            bc.showWarningDialog(resource.getString("app.ui.record_to_device"),resource.getString("app.ui.to_long_pause"),"",
                    app.getMainWindow(),Modality.WINDOW_MODAL);
        } catch (BiofonComplex.MaxTimeByFreqBoundException e1) {
            bc.showWarningDialog(resource.getString("app.ui.record_to_device"),resource.getString("app.ui.time_for_freq_many"),resource.getString("app.ui.time.must_not_exceed"),
                    app.getMainWindow(),Modality.WINDOW_MODAL);
        } catch (BiofonComplex.MaxCountProgramBoundException e1) {
            bc.showWarningDialog(resource.getString("app.ui.record_to_device"),resource.getString("app.ui.exceed_numbers_program"),
                    resource.getString("app.ui.must_less_255_progs"),
                    app.getMainWindow(),Modality.WINDOW_MODAL);
        } catch (BiofonProgram.MaxProgramIDValueBoundException e1) {
            bc.showWarningDialog(resource.getString("app.ui.record_to_device"),"Достигнут предел числа идентификаторов программ.",
                    "Обратитесь к разработчикам",
                    app.getMainWindow(),Modality.WINDOW_MODAL);
        } catch (BiofonProgram.MinFrequenciesBoundException e1) {
            bc.showWarningDialog(resource.getString("app.ui.record_to_device"),resource.getString("app.ui.have_not_freqs"),
                    "",
                    app.getMainWindow(),Modality.WINDOW_MODAL);
        } catch (BiofonProgram.MaxFrequenciesBoundException e1) {
            bc.showWarningDialog(resource.getString("app.ui.record_to_device"),resource.getString("app.ui.more_freqs"),
                    resource.getString("app.ui.mores_freqs_255"),
                    app.getMainWindow(),Modality.WINDOW_MODAL);
        } catch (Exception e1) {
            bc.showExceptionDialog(resource.getString("app.ui.record_to_device"),resource.getString("app.ui.does_not_contain_freqs"),
                    "",e1,
                    app.getMainWindow(),Modality.WINDOW_MODAL);
        }finally {
            disableUploadBtn.set(false);
        }
    }



    private int calcBundlesLength(  BiofonComplex biofonComplex) throws Exception {

        int index=biofonComplex.getPrograms().get(0).getProgramID();

        Map<Integer,Integer> lengthMap=new HashMap<>();

        for (BiofonProgram biofonProgram : biofonComplex.getPrograms()){
            int pID=biofonProgram.getProgramID();
            if(lengthMap.containsKey(pID))lengthMap.put(pID,lengthMap.get(pID)+1);
            else lengthMap.put(pID,1);
        }


        final int bCount  = lengthMap.entrySet().stream()
                                     .mapToInt(i -> i.getValue().intValue())
                                     .max()
                                     .orElseThrow(() -> new Exception("Не найдены программы"));


        int pID = lengthMap.entrySet()
                                     .stream()
                                     .filter(i -> i.getValue().intValue() == bCount)
                                     .limit(1)
                                     .mapToInt(i -> i.getKey())
                                     .findFirst()
                                     .orElseThrow(() -> new Exception("Не найдены программы"));




        if(bCount==1) return 1;
        int fCount = biofonComplex.getPrograms().stream()
                     .filter(i->i.getProgramID()==pID)
                     .mapToInt(i->i.getCountFrequencies())
                     .sum();

        return (int)Math.floor((double)fCount/(double)bCount);
    }

    private List<TherapyProgram> calcProgramsList(BiofonComplex bc,TherapyComplex tc){
        List<TherapyProgram> res=new ArrayList<>();
        Map<Integer,List<BiofonProgram>> compileList=new LinkedHashMap<>();
        for (BiofonProgram biofonProgram : bc.getPrograms()){

            if(!compileList.containsKey(biofonProgram.getProgramID())) compileList.put(biofonProgram.getProgramID(),new ArrayList<>());
            compileList.get(biofonProgram.getProgramID()).add(biofonProgram);

        }


        long position=0;
        for (Map.Entry<Integer, List<BiofonProgram>> entry : compileList.entrySet()) {

            TherapyProgram tp=new TherapyProgram();
            tp.setId(0L);
            tp.setPosition(position);



            int pID=entry.getKey();
            TherapyProgram therapyProgram = mda.getTherapyProgram(pID);
            if(therapyProgram==null){

                tp.setName(resource.getString("ui.program")+"-"+position);
                StringBuilder strb=new StringBuilder();
                for (BiofonProgram bp : entry.getValue()) {

                    strb.append(bp.getFrequencies().stream().map(i->i.toString()).collect(Collectors.joining("; ")));
                    if(entry.getValue().indexOf(bp) != entry.getValue().size()-1)strb.append("; ");

                }

                tp.setFrequencies(strb.toString());

            }
            else {
                tp.setName(therapyProgram.getName());

                tp.setFrequencies(therapyProgram.getFrequencies());
                tp.setId(therapyProgram.getId());
            }
            tp.setTherapyComplex(tc);


            res.add(tp);

            position++;
        }
        return res;
    }



    private List<List<TherapyProgram>> bReadedTherapyPrograms =new ArrayList<>();

    private void onLoad() {
        try {
            BiofonBinaryFile file = Biofon.readFromDevice(true);

            TherapyComplex [] tcArray=new TherapyComplex[3];
            if(bReadedTherapyPrograms.isEmpty()){
                bReadedTherapyPrograms.add(new ArrayList<>());
                bReadedTherapyPrograms.add(new ArrayList<>());
                bReadedTherapyPrograms.add(new ArrayList<>());
            }else {
                bReadedTherapyPrograms.get(0).clear();
                bReadedTherapyPrograms.get(1).clear();
                bReadedTherapyPrograms.get(2).clear();
            }


            int ind=0;
            for (BiofonComplex biofonComplex : file.getComplexesList()) {

                tcArray[ind] = new TherapyComplex();
                tcArray[ind].setName(resource.getString("ui.complex")+"-"+(ind+1));
                tcArray[ind].setTimeForFrequency(biofonComplex.getTimeByFrequency()*60);
                tcArray[ind].setBundlesLength(calcBundlesLength(biofonComplex));
                tcArray[ind].setId(0L);
                tcArray[ind].setProfile(null);
                tcArray[ind].setDescription("");
                tcArray[ind].setOname("");

                bReadedTherapyPrograms.get(ind).addAll(calcProgramsList(biofonComplex,tcArray[ind]));

                ind++;
            }

            bComplex1.setValue(tcArray[0]);
            bComplex2.setValue(tcArray[1]);
            bComplex3.setValue(tcArray[2]);
Platform.runLater(() -> {
            if (tcArray[0] != null){
                bDeviceComplex1.setText(tcArray[0].getName());
                if( !bDeviceComplex1.getStyleClass().contains("RedBackground")) bDeviceComplex1.getStyleClass().addAll("RedBackground");
            }
            else bDeviceComplex1.setText(resource.getString("app.empty"));

            if (tcArray[1] != null) {
                bDeviceComplex2.setText(tcArray[1].getName());
                if( !bDeviceComplex2.getStyleClass().contains("GreenBackground")) bDeviceComplex2.getStyleClass().addAll("GreenBackground");
            }
            else bDeviceComplex2.setText(resource.getString("app.empty"));

            if (tcArray[2] != null) {
                bDeviceComplex3.setText(tcArray[2].getName());
                if( !bDeviceComplex3.getStyleClass().contains("BlueBackground")) bDeviceComplex3.getStyleClass().addAll("BlueBackground");
            }
            else bDeviceComplex3.setText(resource.getString("app.empty"));

        } );

            //установить на кнопках названия
            //доработать обработчик нажатия на кнопки, чтобы учесть если id комплекса =0 то брать программы не из базы а из списков

        } catch (Biofon.ReadFromDeviceException e) {
            Log.logger.error("Ошибка в процессе считывания прибора",e);
            Platform.runLater(() ->bc.showExceptionDialog(resource.getString("app.ui.reading_device"),resource.getString("app.error"),
                    "",e,
                    app.getMainWindow(),Modality.WINDOW_MODAL));
        } catch (Exception e) {
            Log.logger.error("Ошибка в процессе анализа содержимого прибора",e);
            Platform.runLater(() ->bc.showExceptionDialog(resource.getString("app.ui.reading_device"),resource.getString("app.ui.error_processing"),
                    "",e,
                    app.getMainWindow(),Modality.WINDOW_MODAL) );

        }
    }


    public enum SortType {TIME, NAME}

    public void changeComplexesSortType(SortType type) {
        if (type == SortType.TIME) biofonComplexesSorted.setComparator(comparatorBiofonComplexByTime);
        else biofonComplexesSorted.setComparator(comparatorBiofonComplexByName);
    }

    /**
     * Добавить терапевт комплекс в таблицу уже созданный
     *
     * @param tc
     */
    public void addComplex(TherapyComplex tc) {
        biofonComplexes.add(tc);

        if(tc.getTimeForFrequency()/60> BiofonComplex.MAX_TIME_BY_FREQ) {
            tc.setTimeForFrequency(255);
            try {
                mda.updateTherapyComplex(tc);
            } catch (Exception e) {
                Log.logger.error("",e);
            }
        }else {
            int tm = tc.getTimeForFrequency() / 60;
            if(tm*60!=tc.getTimeForFrequency()){
                tc.setTimeForFrequency(tm*60);
                try {
                    mda.updateTherapyComplex(tc);
                } catch (Exception e) {
                    Log.logger.error("",e);
                }
            }
        }
    }

    /**
     * Добавит программу в таблицу
     *
     * @param tp
     */
    public void addProgram(TherapyProgram tp) {
        biofonPrograms.add(tp);
    }


    /**
     * Диалог добавления нового комплекса
     */
    public void addComplex() {


        //выведем диалог ввода данных


        NameDescroptionDialogController.Data data = null;
        try {
            data = BaseController.openDialogUserData(app.getMainWindow(),
                    "/fxml/SectionDialogCreate.fxml",
                    resource.getString("app.title68"),
                    false,
                    StageStyle.DECORATED,
                    0,
                    0,
                    0,
                    0,
                    new NameDescroptionDialogController.Data("", ""));


        } catch (IOException e) {
            logger.error("", e);
            data = null;
        }

        if (data == null) {
            BaseController.showErrorDialog("Ошибка создания комплекса", "", "", app.getMainWindow(),
                    Modality.WINDOW_MODAL);
            return;
        }


        //проверим полученные данные из диалога, создали ли имя
        if (data.isNameChanged()) {

            try {


                TherapyComplex therapyComplex = mda.createTherapyComplex(app.getBiofonProfile(),
                        data.getNewName(),
                        data.getNewDescription(),
                        300, 1);

                biofonComplexes.add(therapyComplex);

                int i = biofonCompexesList.getItems().indexOf(therapyComplex);
                biofonCompexesList.requestFocus();
                biofonCompexesList.getSelectionModel().clearSelection();
                biofonCompexesList.getSelectionModel().select(i);
                biofonCompexesList.scrollTo(i);
                biofonCompexesList.getFocusModel().focus(i);
            } catch (Exception e) {
                logger.error("", e);
                bc.showExceptionDialog("Ошибка создания терапевтического комплекса",
                        "",
                        "",
                        e,
                        app.getMainWindow(),
                        Modality.WINDOW_MODAL);

            }


        }


    }

    public void editComplex() {

        //выведем диалог ввода данных
        if (getSelectedComplexes() == null) return;
        if (getSelectedComplexes().isEmpty()) return;

        TherapyComplex selectedItem = biofonCompexesList.getSelectionModel().getSelectedItem();


        NameDescroptionDialogController.Data data = null;
        try {
            data = BaseController.openDialogUserData(app.getMainWindow(),
                    "/fxml/SectionDialog.fxml",
                    resource.getString("app.title68"),
                    false,
                    StageStyle.DECORATED,
                    0,
                    0,
                    0,
                    0,
                    new NameDescroptionDialogController.Data(
                            selectedItem.getName()
                            , selectedItem.getDescription()));


        } catch (IOException e) {
            logger.error("", e);
            data = null;
        }

        if (data == null) {
            BaseController.showErrorDialog("Ошибка редактирования комплекса", "", "", app.getMainWindow(),
                    Modality.WINDOW_MODAL);
            return;
        }


        //проверим полученные данные из диалога, создали ли имя
        if (data.isNameChanged()) {

            try {

                selectedItem.setName(data.getNewName());
                selectedItem.setDescription(data.getNewDescription());

                mda.updateTherapyComplex(selectedItem);


            } catch (Exception e) {
                logger.error("", e);
                bc.showExceptionDialog("Ошибка редактирования терапевтического комплекса",
                        "",
                        "",
                        e,
                        app.getMainWindow(),
                        Modality.WINDOW_MODAL);

            }


        }


    }

    public void delComplex() {

        if (getSelectedComplexes() == null) return;
        if (getSelectedComplexes().isEmpty()) return;
        ObservableList<TherapyComplex> selectedItems = getSelectedComplexes();


        if (!selectedItems.isEmpty()) {
            Optional buttonType = bc.showConfirmationDialog(
                    resource.getString("app.title69"),
                    "",
                    resource.getString("app.title70"),
                    app.getMainWindow(),
                    Modality.WINDOW_MODAL
            );
            if (buttonType.isPresent() && buttonType.get() == bc.okButtonType) {
                try {
                    Iterator<TherapyComplex> e = selectedItems.iterator();


                    while (e.hasNext()) {
                        mda.removeTherapyComplex(e.next());
                    }


                    List<TherapyComplex> e1 = selectedItems.stream().collect(Collectors.toList());
                    Iterator<TherapyComplex> iterator1 = e1.iterator();


                    while (iterator1.hasNext()) {
                        biofonComplexes.remove(iterator1.next());
                    }


                    selectedItems = null;
                    biofonCompexesList.getSelectionModel().clearSelection();
                    biofonProgramsList.getSelectionModel().clearSelection();
                    biofonPrograms.clear();


                } catch (Exception var9) {
                    Log.logger.error("", var9);
                    bc.showExceptionDialog("Ошибка удаления комплексов",
                            "",
                            "",
                            var9,
                            app.getMainWindow(),
                            Modality.WINDOW_MODAL);
                }
            }

            selectedItems = null;
        }

    }
private void changePositionProgram(boolean up){


    TherapyProgram selectedItem = biofonProgramsList.getSelectionModel().getSelectedItem();
    Long selectedItemPosition = selectedItem.getPosition();

    int ind1 = biofonProgramsSorted.indexOf(selectedItem);
    TherapyProgram item2 = biofonProgramsSorted.get(ind1 + (up?-1:1));
    Long item2Position = item2.getPosition();

        selectedItem.setPosition(item2Position);
        item2.setPosition(selectedItemPosition);

        try {

        mda.updateTherapyProgram(selectedItem);
        mda.updateTherapyProgram(item2);
        biofonProgramsList.requestFocus();
        biofonProgramsList.scrollTo(selectedItem);

    } catch (Exception e) {
        bc.showExceptionDialog("Ошибка перемещения программы",
                "",
                "",
                e,
                app.getMainWindow(),
                Modality.WINDOW_MODAL);
        selectedItem.setPosition(selectedItemPosition);
        item2.setPosition(item2Position);
    }

}


    public void upProgram() {

        changePositionProgram(true);
    }


    public void downProgram() {
        changePositionProgram(false );

    }


    public void delProgram() {

        TherapyProgram selectedItem = biofonProgramsList.getSelectionModel().getSelectedItem();

        if (selectedItem == null) return;


        Optional<ButtonType> buttonType = bc.showConfirmationDialog(
                resource.getString("app.title66"),
                "", resource.getString("app.title67"),
                app.getMainWindow(),
                Modality.WINDOW_MODAL);

        if (buttonType.isPresent() ? buttonType.get() == bc.okButtonType : false) {
            try {

                mda.removeTherapyProgram(selectedItem);

                biofonPrograms.remove(selectedItem);

            } catch (Exception e) {
                logger.error("", e);

                bc.showExceptionDialog("Ошибка удаления программы",
                        "",
                        "",
                        e,
                        app.getMainWindow(),
                        Modality.WINDOW_MODAL);

            }


        }
    }


    private List<List<Double>> splitFreqsByBundles(List<Double> fl,int bundlesLength){
        List<List<Double>> res=new ArrayList<>();
        if(bundlesLength >1)
        {
            //пачки частот
            int bundlesCount=(int)Math.ceil((float)fl.size()/(float)bundlesLength);
            int cEnd=0;
            int cEndT=0;
            for(int i=0;i<bundlesCount;i++)
            {
                cEndT = (i+1)*bundlesLength;
                if(cEndT<=fl.size())cEnd=cEndT;
                else cEnd=fl.size();
                //разбиваем по пачкам

                res.add(fl.subList(i*bundlesLength,cEnd));
            }


        }else  res.add(fl);
        return res;
    }

    /**
     * Создает файл из трех комплексов
     * @param tc1
     * @param tc2
     * @param tc3
     * @return
     */
    private  BiofonBinaryFile createFile(TherapyComplex tc1, TherapyComplex tc2, TherapyComplex tc3) throws BiofonComplex.MaxTimeByFreqBoundException, BiofonComplex.MaxPauseBoundException, BiofonProgram.MaxProgramIDValueBoundException, BiofonProgram.MaxFrequenciesBoundException, BiofonProgram.MinFrequenciesBoundException, BiofonComplex.MaxCountProgramBoundException {

        if(bComplex1.get() ==null || bComplex2.get()==null || bComplex3.get()==null) return null;

        BiofonComplex bc1=null;
        BiofonComplex bc2=null;
        BiofonComplex bc3=null;

             bc1=new BiofonComplex(PAUSE_BETWEEN_PROGRAM, tc1.getTimeForFrequency()/60);
             bc2=new BiofonComplex(PAUSE_BETWEEN_PROGRAM, tc2.getTimeForFrequency()/60);
             bc3=new BiofonComplex(PAUSE_BETWEEN_PROGRAM, tc3.getTimeForFrequency()/60);

        List<TherapyProgram> therapyPrograms;

             if(tc1.getId().intValue()==0)therapyPrograms=bReadedTherapyPrograms.get(0);
           else  therapyPrograms = mda.findTherapyPrograms(tc1);

        for (TherapyProgram therapyProgram : therapyPrograms) {

            if(tc1.getBundlesLength()==1){
                bc1.addProgram(new BiofonProgram(therapyProgram.parseFreqs(),therapyProgram.getId().intValue()));
            }else {
                for (List<Double> fl : splitFreqsByBundles(therapyProgram.parseFreqs(), tc1.getBundlesLength())) {
                    bc1.addProgram(new BiofonProgram(fl,therapyProgram.getId().intValue()));
                }
            }


        }

        if(tc2.getId().intValue()==0)therapyPrograms=bReadedTherapyPrograms.get(1);
        else  therapyPrograms = mda.findTherapyPrograms(tc2);

        for (TherapyProgram therapyProgram : therapyPrograms) {


            if(tc2.getBundlesLength()==1){
                bc2.addProgram(new BiofonProgram(therapyProgram.parseFreqs(),therapyProgram.getId().intValue()));
            }else {
                for (List<Double> fl : splitFreqsByBundles(therapyProgram.parseFreqs(), tc2.getBundlesLength())) {
                    bc2.addProgram(new BiofonProgram(fl,therapyProgram.getId().intValue()));
                }
            }

        }

        if(tc3.getId().intValue()==0)therapyPrograms=bReadedTherapyPrograms.get(2);
        else  therapyPrograms = mda.findTherapyPrograms(tc3);

        for (TherapyProgram therapyProgram : therapyPrograms) {


            if(tc3.getBundlesLength()==1){
                bc3.addProgram(new BiofonProgram(therapyProgram.parseFreqs(),therapyProgram.getId().intValue()));
            }else {
                for (List<Double> fl : splitFreqsByBundles(therapyProgram.parseFreqs(), tc3.getBundlesLength())) {
                    bc3.addProgram(new BiofonProgram(fl,therapyProgram.getId().intValue()));
                }
            }

        }

       return  new BiofonBinaryFile(bc1,bc2,bc3);
    }
}
