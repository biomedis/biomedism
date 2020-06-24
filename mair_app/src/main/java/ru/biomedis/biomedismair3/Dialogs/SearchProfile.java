package ru.biomedis.biomedismair3.Dialogs;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import ru.biomedis.biomedismair3.App;
import ru.biomedis.biomedismair3.BaseController;
import ru.biomedis.biomedismair3.entity.Profile;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Поиск по профилям диалог
 * Created by Anama on 17.02.2016.
 */
public class SearchProfile extends BaseController {

    @FXML    GridPane root;
    @FXML    ListView<Profile> searchResult;
    @FXML    TextField searchPattern;
    @FXML    Button search;
    @FXML    Button setProfile;

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
    public void initialize(URL location, ResourceBundle resources)
    {
        search.disableProperty().bind(searchPattern.textProperty().isEmpty());
        setProfile.disableProperty().bind(searchResult.getSelectionModel().selectedItemProperty().isNull());

        searchResult.setCellFactory(new Callback<ListView<Profile>, ListCell<Profile>>() {
            @Override
            public ListCell<Profile> call(ListView<Profile> param) {
                return new ListCell<Profile>(){
                    @Override
                    protected void updateItem(Profile item, boolean empty) {
                        super.updateItem(item, empty);

                        if (item == null || empty) {
                            setText(null);
                            setGraphic(null);
                            return;
                        } else {
                            this.setText(item.getName());
                            setGraphic(null);
                        }


                    }
                };
            }
        });
        searchResult.setOnMouseClicked(event -> {if(event.getClickCount()==2) onSearch();});


    }

    public void onSetProfile()
    {
        if(searchResult.getSelectionModel().getSelectedItem()!=null)
        {
            Data data=(Data)root.getUserData();

            data.setNewVal(searchResult.getSelectionModel().getSelectedItem().getId());

            Stage stage = (Stage)root.getScene().getWindow();
            stage.close();

        }
    }

    public void onSearch()
    {
        searchResult.getItems().clear();
        searchResult.getItems().addAll(getModel().searchProfile(searchPattern.getText()).stream().filter(i->!i.getName().equals(
                App.BIOFON_PROFILE_NAME)).collect(Collectors.toList())) ;

    }

    public static  class Data
    {
        private long oldVal =0;
        private long newVal =0;



        public Data(long oldVal) {
            this.oldVal = oldVal;

            newVal =oldVal;

        }



        public long getNewVal() {
            return newVal;
        }

        private void setNewVal(long newVal) {
            this.newVal = newVal;
        }



        public long getOldVal() {
            return oldVal;
        }



        public boolean isChanged(){ return oldVal!=newVal;}


    }
}
