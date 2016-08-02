package ru.biomedis.biomedismair3.Dialogs;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import ru.biomedis.biomedismair3.BaseController;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static ru.biomedis.biomedismair3.Log.logger;

/**
 * Разбор мультичестот между ; ; те между значениями есть только + +
 * Created by Anama on 10.09.2015.
 */
public class MultyFreqEditDialogController extends BaseController {


    @FXML private Button saveBtn;
    @FXML private Button editBtn;

    @FXML private GridPane rootNode;
    @FXML private Button delBtn;
    @FXML private Button addBtn;
    @FXML private Button addBeforeBtn;
    @FXML private ListView<String> freqList;
    @FXML private TextField addField;



    private ContextMenu addMenu;


    ResourceBundle res;

    private static  boolean isInputDoubleValid(TextField tf) {
        Boolean b = false;
        if (!(tf.getText() == null || tf.getText().length() == 0)) {
            try
            {
                // Do all the validation you need here such as
                Double d = Double.parseDouble(tf.getText());
                if (d > 0.0) b = true;
            } catch (NumberFormatException e)
            {
                b=false;
            }

        }
        return b;
    }
    private static boolean isStringDoubleValid(String tf) {
        Boolean b = false;
        if (!(tf == null || tf.length() == 0)) {
            try
            {
                // Do all the validation you need here such as
                Double d = Double.parseDouble(tf);
                if (d > 0.0) b = true;
            } catch (NumberFormatException e)
            {
                b=false;
            }

        }
        return b;
    }
    public void onAdd()
    {

        if(isInputDoubleValid(addField))
        {
            freqList.getItems().add( Double.parseDouble(addField.getText())+""  );
            addField.setStyle("");
            addField.setText("");
        }else addField.setStyle("-fx-border-color: crimson");
    }


    public void onAddBefore()
    {
        if(freqList.getSelectionModel().getSelectedItem()==null) return;
        if(isInputDoubleValid(addField))
        {
            if(freqList.getSelectionModel().getSelectedItem()==null)
            {

                showInfoDialogNoHeader(res.getString("app.text.add_element_after"),res.getString("app.text.element_not_select"),rootNode.getScene().getWindow(),Modality.WINDOW_MODAL);
                return;
            }
            int selectedIndex = freqList.getSelectionModel().getSelectedIndex();

            freqList.getItems().add(selectedIndex,Double.parseDouble(addField.getText())+""  );
            addField.setStyle("");
            addField.setText("");
        }else addField.setStyle("-fx-border-color: crimson");
    }

    public void onDelete()
    {

        if(freqList.getSelectionModel().getSelectedItem()!=null)
        {
            int selectedIndex = freqList.getSelectionModel().getSelectedIndex();
            freqList.getItems().remove(selectedIndex);
        }

    }



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        res = resources;

        addBtn.disableProperty().bind(addField.textProperty().isEmpty());
        delBtn.disableProperty().bind(freqList.getSelectionModel().selectedItemProperty().isNull());
        editBtn.disableProperty().bind(freqList.getSelectionModel().selectedItemProperty().isNull());



        addField.setOnKeyReleased(event -> {

            if (event.isAltDown() && event.getCode() == KeyCode.ENTER) {
                event.consume();
                onAddBefore();
            } else if (event.getCode() == KeyCode.ENTER) {
                event.consume();
                onAdd();
            } else {
                addField.setText(addField.getText().replaceAll("[^0-9.]", ""));
                addField.end();
            }

        });
        addField.setOnKeyPressed(event -> {

           if (event.getCode() == KeyCode.E) {event.consume();onEdit();}


        });


        freqList.setOnMouseClicked(event ->
        {

            if (event.getClickCount() == 2) onEdit();


        });

        freqList.setOnKeyPressed(event ->
        {
            if (event.getCode() == KeyCode.DELETE || event.getCode() == KeyCode.BACK_SPACE) onDelete();
            else if (event.isAltDown() && event.getCode() == KeyCode.ENTER) onAddBefore();
            else if (event.getCode() == KeyCode.ENTER) onAdd();
            else if (event.getCode() == KeyCode.E) onEdit();
        });

        freqList.requestFocus();





        MenuItem smi1=new MenuItem(res.getString("app.add"));
        smi1.setOnAction(event -> onAdd());
        KeyCombination kr = new KeyCodeCombination(KeyCode.ENTER);
        smi1.setAccelerator(kr);



        MenuItem smi3=new MenuItem(res.getString("app.add_before"));
        smi3.setOnAction(event -> onAddBefore());
        KeyCombination kr2 = new KeyCodeCombination(KeyCode.ENTER,KeyCombination.ALT_DOWN);
        smi3.setAccelerator(kr2);




        addMenu=new ContextMenu(smi1, smi3);
        addBtn.setOnAction(event -> {
                    smi1.setDisable(false);
                    smi3.setDisable(false);


                    if (freqList.getSelectionModel().getSelectedItem() == null) {
                        smi3.setDisable(true);
                    }

                    addMenu.show(addBtn, Side.BOTTOM, 0, 0);
                }
        );




    }


    @Override
    public void setParams(Object... params) {
        Data sd=  (Data)rootNode.getUserData();

        if(!sd.getOldFreqs().isEmpty()) for (String s : sd.getOldFreqs().split("\\+")) freqList.getItems().add(s);


    }

public void onEdit()
{


    if(freqList.getSelectionModel().getSelectedItem()==null) return;

    int selectedIndex = freqList.getSelectionModel().getSelectedIndex();

    TextInputValidationController.Data td = new TextInputValidationController.Data(freqList.getSelectionModel().getSelectedItem(), MultyFreqEditDialogController::isStringDoubleValid);
    try {
        openDialogUserData((Stage) rootNode.getScene().getWindow(), "/fxml/TextInputValidationDialog.fxml", res.getString("app.text.edit_freq"), false, StageStyle.DECORATED, 0, 0, 0, 0, td);

        if (td.isChanged()) freqList.getItems().set(selectedIndex, Double.parseDouble(td.getNewVal())+"");


        td = null;
    } catch (Exception e) {
        td = null;
        logger.error("",e);
        showExceptionDialog(res.getString("app.text.edit_freq_error"), "", "", e, rootNode.getScene().getWindow(), Modality.WINDOW_MODAL);
        return;
    }
}




    public void onSave()
    {
        Data sd=  (Data)rootNode.getUserData();


        sd.setNewFreqs(freqList.getItems().stream().collect(Collectors.joining("+")));
        Stage stage = (Stage)rootNode.getScene().getWindow();
        stage.close();

    }

    public static  class Data
    {
        private String oldFreqs ="";

        private String newFreqs ="";



        public Data(String oldFreqs) {
            this.oldFreqs = oldFreqs;

            this.newFreqs = oldFreqs;


        }

        public String getNewFreqs() {
            return newFreqs;
        }

        private void setNewFreqs(String newFreqs) {
            this.newFreqs = newFreqs;
        }





        public String getOldFreqs() {
            return oldFreqs;
        }


        public boolean isChanged()
        {
            return !oldFreqs.equals(newFreqs);
        }


    }





}
