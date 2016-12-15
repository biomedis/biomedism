package ru.biomedis.biomedismair3;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import ru.biomedis.biomedismair3.Dialogs.NameDescroptionDialogController;
import ru.biomedis.biomedismair3.entity.Profile;
import ru.biomedis.biomedismair3.entity.TherapyComplex;
import ru.biomedis.biomedismair3.entity.TherapyProgram;


import java.io.File;
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

    public BiofonUIUtil(ResourceBundle resource, App app, BaseController bc, ModelDataApp mda, Profile biofonProfile,
                        ListView<TherapyComplex> biofonCompexesList, ListView<TherapyProgram> biofonProgramsList,
                        Label complexOName,Label programOName) {
        this.resource = resource;
        this.app = app;
        this.bc = bc;
        this.mda = mda;
        this.biofonProfile = biofonProfile;
        this.biofonCompexesList = biofonCompexesList;
        this.biofonProgramsList = biofonProgramsList;


        this.complexOName = complexOName;
        this.programOName = programOName;
    }

    /**
     * Перезагрузить список комплексов в таблице
     */
    public void reloadComplexes(){

        biofonComplexes.clear();
        biofonComplexes.addAll(mda.findAllTherapyComplexByProfile(biofonProfile));
    }

    public void init() {


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
}
