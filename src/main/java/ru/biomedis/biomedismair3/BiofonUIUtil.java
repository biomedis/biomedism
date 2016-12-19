package ru.biomedis.biomedismair3;

import javafx.beans.Observable;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
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
import ru.biomedis.biomedismair3.utils.USB.PlugDeviceListener;
import ru.biomedis.biomedismair3.utils.USB.USBHelper;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

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

    public BiofonUIUtil(ResourceBundle resource, App app, BaseController bc, ModelDataApp mda, Profile biofonProfile,
                        ListView<TherapyComplex> biofonCompexesList, ListView<TherapyProgram> biofonProgramsList,
                        Label complexOName,Label programOName,
                        Runnable onAttach, Runnable onDetach) {
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
    }

    /**
     * Перезагрузить список комплексов в таблице
     */
    public void reloadComplexes(){

        biofonComplexes.clear();
        biofonComplexes.addAll(mda.findAllTherapyComplexByProfile(biofonProfile));
    }





    private SimpleObjectProperty<TherapyComplex> bComplex1=new SimpleObjectProperty<>();
    private SimpleObjectProperty<TherapyComplex> bComplex2=new SimpleObjectProperty<>();
    private SimpleObjectProperty<TherapyComplex> bComplex3=new SimpleObjectProperty<>();


    static final DataFormat COMPLEXES__DATA_FORMAT = new DataFormat("biofon/complex");
    private Image dragImage;

    private  void viewComplexPrograms(TherapyComplex tc) {

            if(tc ==null) return;

            biofonPrograms.clear();
            biofonPrograms.addAll(mda.findTherapyPrograms(tc));
            if(!tc.getOname().isEmpty()) complexOName.setText(tc.getOname());
            else complexOName.setText("");


    }
    private void on3ComplexButtonClick(Event e){

        if(e.getTarget() instanceof Button) {
            Button btn = (Button) e.getTarget();

            biofonCompexesList.getSelectionModel().clearSelection();

            if (btn.getId().equals(bDeviceComplex1.getId())) {
                viewComplexPrograms(bComplex1.get());

            } else if (btn.getId().equals(bDeviceComplex2.getId())) {
                viewComplexPrograms(bComplex2.get());
            } else if (btn.getId().equals(bDeviceComplex3.getId())) {
                viewComplexPrograms(bComplex3.get());
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

                if(draggedComplex!=null){


                    btn.setText(draggedComplex.getName());
                    btn.getStyleClass().remove("GrayBackground");

                    if(btn.getId().equals(bDeviceComplex1.getId())) {
                        btn.getStyleClass().addAll("RedBackground");
                        bComplex1.setValue(draggedComplex);
                    }
                    else  if(btn.getId().equals(bDeviceComplex2.getId())) {
                        btn.getStyleClass().addAll("GreenBackground");
                        bComplex2.setValue(draggedComplex);
                    }
                    else  if(btn.getId().equals(bDeviceComplex3.getId())) {
                        btn.getStyleClass().addAll("BlueBackground");
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


    public void init() {

        initUSBDetection();



        biofonCompexesList.setPlaceholder(new Label(resource.getString("app.table.complex_placeholder")));
        biofonProgramsList.setPlaceholder(new Label(resource.getString("app.table.programm_placeholder")));


        this.biofonCompexesList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        URL location = getClass().getResource("/images/medical_record.png");
        biofonComplexImage = new Image(location.toExternalForm());
        biofonComplexImageView = new ImageView(biofonComplexImage);


        biofonComplexes.addAll(mda.findAllTherapyComplexByProfile(biofonProfile));
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
                        freqs = new Text(item.getFrequencies().replace("+", "; "));
                        freqs.setWrappingWidth(getListView().getWidth()); // Setting the wrapping width to the Text
                        freqs.wrappingWidthProperty().bind(getListView().widthProperty());
                        vbox=new VBox();
                        vbox.getChildren().addAll(name,freqs);
                        vbox.setSpacing(4);


                    }else {
                        name.setText(item.getName());
                        freqs.setText(item.getFrequencies().replace("+", "; "));
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

                initComplexButtons(false);

                bComplex1.setValue(null);
                bComplex2.setValue(null);
                bComplex3.setValue(null);


               onAttach.run();
               onLoad();
            }

            @Override
            public void onDetachDevice() {

                System.out.println("Устройство Biofon отключено");

                initComplexButtons(true);

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


    public void viewBiofonProgramsOnComplexClick() {


        ObservableList<TherapyComplex> selectedItems = getSelectedComplexes();
        biofonPrograms.clear();
        if (selectedItems.isEmpty()) return;

        if (selectedItems.size() == 1) {
            biofonPrograms.addAll(mda.findTherapyPrograms(selectedItems.get(0)));
            if(!selectedItems.get(0).getOname().isEmpty()) complexOName.setText(selectedItems.get(0).getOname());
            else complexOName.setText("");
        }

    }

    public void complexesToBiofon(List<TherapyComplex> tcs) {
        try {
        for (TherapyComplex tc : tcs) {

            TherapyComplex therapyComplex = mda.copyTherapyComplexToProfile(app.getBiofonProfile(), tc, true);
            addComplex(therapyComplex);


        }
        } catch (Exception e) {
            BaseController.showExceptionDialog("Ошибка копирования  комплексов", "", "", e, app.getMainWindow(),
                    Modality.WINDOW_MODAL);
            return;
        }

    }


    public void initUpload(Button uploadBiofonBtn) {
        this.uploadBiofonBtn = uploadBiofonBtn;
       // this.uploadBiofonBtn.setDisable(true);

        this.uploadBiofonBtn.disableProperty().bind(bComplex1.isNull().or(bComplex2.isNull()).or(bComplex3.isNull()));

        this.uploadBiofonBtn.setOnAction(this::onUpload);

    }




    private void onUpload(Event e){
            if(bComplex1.get() ==null || bComplex2.get()==null || bComplex3.get()==null) return;



        try {
            BiofonBinaryFile file = createFile(bComplex1.get(), bComplex2.get(), bComplex3.get());
            Biofon.writeToDevice(file);
            bc.showInfoDialog("Запись в прибор","Комплексы успешно записаны в прибор","",
                    app.getMainWindow(),Modality.WINDOW_MODAL);
        } catch (BiofonBinaryFile.MaxBytesBoundException e1) {

            bc.showWarningDialog("Запись в прибор","Слишком большой объем комплексов","Необходимо уменьшить колличество программ\n или \nувеличить размер пачек частот для комплексов, если они используются",
                    app.getMainWindow(),Modality.WINDOW_MODAL);

        } catch (BiofonComplex.ZeroCountProgramBoundException e1) {

            bc.showWarningDialog("Запись в прибор","Комплексы должны содержать хотябы одну программу","",
                    app.getMainWindow(),Modality.WINDOW_MODAL);

        } catch (Biofon.WriteToDeviceException e1) {
            Log.logger.error("Ошибка записи в биофон",e1);
            bc.showExceptionDialog("Запись в прибор","Комплексы должны содержать хотябы одну программу","",e1,
                    app.getMainWindow(),Modality.WINDOW_MODAL);
        } catch (Exception e1) {
            Log.logger.error("",e1);
           return;
        }
    }

    //TODO преобразовать в комплексы, активизировать кнопки или указать что прибор пустой!
    private void onLoad(){
        try {
            BiofonBinaryFile file = Biofon.readFromDevice(false);



        } catch (Biofon.ReadFromDeviceException e) {
            e.printStackTrace();
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
                        300, true, 1);

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


    public void upProgram() {

        TherapyProgram selectedItem = biofonProgramsList.getSelectionModel().getSelectedItem();
        Long selectedItemPosition = selectedItem.getPosition();

        int ind1 = biofonProgramsSorted.indexOf(selectedItem);
        TherapyProgram item2 = biofonProgramsSorted.get(ind1 - 1);
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


    public void downProgram() {
        TherapyProgram selectedItem = biofonProgramsList.getSelectionModel().getSelectedItem();
        Long selectedItemPosition = selectedItem.getPosition();

        int ind1 = biofonProgramsSorted.indexOf(selectedItem);
        TherapyProgram item2 = biofonProgramsSorted.get(ind1 + 1);
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
    /**
     * Создает файл из трех комплексов
     * @param tc1
     * @param tc2
     * @param tc3
     * @return
     */
    public  BiofonBinaryFile createFile(TherapyComplex tc1, TherapyComplex tc2, TherapyComplex tc3) throws Exception {

        if(bComplex1.get() ==null || bComplex2.get()==null || bComplex3.get()==null) throw new Exception("Отстутствуют комплексы");

        BiofonComplex bc1=null;
        BiofonComplex bc2=null;
        BiofonComplex bc3=null;
        try {
             bc1=new BiofonComplex(5, tc1.getTimeForFrequency());
             bc1=new BiofonComplex(5, tc2.getTimeForFrequency());
             bc1=new BiofonComplex(5, tc3.getTimeForFrequency());


        } catch (BiofonComplex.MaxPauseBoundException e) {
            bc.showErrorDialog("Запись в прибор","Превышено максимальное значение паузы между программами",
                    "Значение должно быть не более 255",app.getMainWindow(),Modality.WINDOW_MODAL);
            throw new Exception();
        } catch (BiofonComplex.MaxTimeByFreqBoundException e) {
           bc.showErrorDialog("Запись в прибор","Превышено максимальное значение времени на частоту",
                   "Значение должно быть не более 255",app.getMainWindow(),Modality.WINDOW_MODAL);
           throw new Exception();

        }

        for (TherapyProgram therapyProgram : mda.findTherapyPrograms(tc1)) {


            bc1.addProgram(new BiofonProgram(therapyProgram.parseFrqeqs(),therapyProgram.getId().intValue()));

        }

        for (TherapyProgram therapyProgram : mda.findTherapyPrograms(tc2)) {


            bc1.addProgram(new BiofonProgram(therapyProgram.parseFrqeqs(),therapyProgram.getId().intValue()));

        }
        for (TherapyProgram therapyProgram : mda.findTherapyPrograms(tc3)) {


            bc1.addProgram(new BiofonProgram(therapyProgram.parseFrqeqs(),therapyProgram.getId().intValue()));

        }

       return  new BiofonBinaryFile(bc1,bc2,bc3);
    }
}
