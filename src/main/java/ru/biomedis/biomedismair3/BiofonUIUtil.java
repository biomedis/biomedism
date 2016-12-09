package ru.biomedis.biomedismair3;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import ru.biomedis.biomedismair3.entity.Profile;
import ru.biomedis.biomedismair3.entity.TherapyComplex;
import ru.biomedis.biomedismair3.entity.TherapyProgram;


import java.net.URL;
import java.util.Comparator;
import java.util.ResourceBundle;

/**
 * Created by anama on 09.12.16.
 */
public class BiofonUIUtil {

    private ResourceBundle resource;
    private ModelDataApp mda;
    private Profile biofonProfile;
    private final ListView<TherapyComplex> biofonCompexesList;
    private final ListView<TherapyProgram> biofonProgramsList;

    private Image biofonComplexImage;
    private ImageView biofonComplexImageView;

    private ObservableList<TherapyComplex> biofonComplexes= FXCollections.observableArrayList();
    private SortedList<TherapyComplex> biofonComplexesSorted=new SortedList<>(biofonComplexes);
    private  Comparator<TherapyComplex> comparatorBiofonComplex= Comparator.comparing(TherapyComplex::getName);

    private ObservableList<TherapyProgram> biofonPrograms= FXCollections.observableArrayList();
    private SortedList<TherapyProgram> biofonProgramsSorted=new SortedList<>(biofonPrograms);
    private  Comparator<TherapyProgram> comparatorBiofonProgram= Comparator.comparing(TherapyProgram::getPosition);

    public BiofonUIUtil(ResourceBundle resource, ModelDataApp mda, Profile biofonProfile,ListView<TherapyComplex> biofonCompexesList, ListView<TherapyProgram> biofonProgramsList) {
        this.resource = resource;
        this.mda = mda;
        this.biofonProfile = biofonProfile;
        this.biofonCompexesList = biofonCompexesList;
        this.biofonProgramsList = biofonProgramsList;


    }


    public void init() {

        this.biofonCompexesList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        URL location = getClass().getResource("/images/medical_record.png");
        biofonComplexImage = new Image(location.toExternalForm());
        biofonComplexImageView=new ImageView(biofonComplexImage);




        biofonComplexes.addAll(mda.findAllTherapyComplexByProfile(biofonProfile));
        biofonCompexesList.setCellFactory(param -> new ListCell<TherapyComplex>(){
            private  ImageView imgv;
            @Override
            protected void updateItem(TherapyComplex item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setText(null);
                    setGraphic(null);
                    return;
                } else {
                    this.setText(item.getName());
                    if(imgv==null)imgv=new ImageView(biofonComplexImage);

                    setGraphic(imgv);
                }


            }

        });

        biofonProgramsList.setCellFactory(param -> new ListCell<TherapyProgram>(){
            @Override
            protected void updateItem(TherapyProgram item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setText(null);
                    setGraphic(null);
                    return;
                } else {
                    this.setText(item.getName()+"\n"+item.getFrequencies().replace("+",";"));
                    setGraphic(null);
                }


            }

        });

        biofonComplexesSorted.setComparator(comparatorBiofonComplex);
        biofonCompexesList.setItems(biofonComplexesSorted);


        biofonProgramsList.setItems(biofonProgramsSorted);
        biofonProgramsSorted.setComparator(comparatorBiofonProgram);

        biofonCompexesList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> viewBiofonProgramsOnComplexClick());

    }


    private ObservableList<TherapyComplex> getSelectedComplexes(){
        return biofonCompexesList.getSelectionModel().getSelectedItems();
    }


    public void viewBiofonProgramsOnComplexClick(){
        ObservableList<TherapyComplex> selectedItems = getSelectedComplexes();
        biofonPrograms.clear();
        if(selectedItems.isEmpty())return;

        if(selectedItems.size() == 1)  biofonPrograms.addAll(mda.findTherapyPrograms(selectedItems.get(0)));

    }


    /**
     * Добавить терапевт комплекс в таблицу
     * @param tc
     */
    public void addComplex(TherapyComplex tc){
        biofonComplexes.add(tc);
    }

    /**
     * Добавит программу в таблицу
     * @param tp
     */
    public void addProgram(TherapyProgram tp){
        biofonPrograms.add(tp);
    }

    public void addComplex(){

    }
    public void editComplex(){

    }
    public void delComplex(){

    }
    public void printComplex(){

    }
    public void importComplex(){

    }
    public void exportComplex(){

    }
    public void upProgram(){

    }
    public void downProgram(){

    }
    public void delProgram(){

    }
}
