package ru.biomedis.biomedismair3.Dialogs;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.StringConverter;
import lombok.extern.slf4j.Slf4j;
import ru.biomedis.biomedismair3.BaseController;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


@Slf4j
public class ProgramDialogController extends BaseController {


    @FXML private Button saveBtn;
    @FXML private Button editBtn;
    @FXML private TextField nameFld;
    @FXML private TextArea descriptionFld;
    @FXML private GridPane rootNode;
    @FXML private Button delBtn;
    @FXML private Button addBtn;
    @FXML private Button addBeforeBtn;
    @FXML private Button addMultyBtn;
    @FXML private ListView<String> freqList;
    @FXML private TextField addField;
    @FXML private TextArea freqsString;




    ResourceBundle res;
    private  ContextMenu addMenu;

    private static  boolean isInputDoubleValid(TextField tf) {
        Boolean b = false;
        if (!(tf.getText() == null || tf.getText().length() == 0)) {
            try
            {
                // Do all the validation you need here such as
                Double d = Double.valueOf(tf.getText());

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
                Double d = Double.valueOf(tf);
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
            freqList.getItems().add( Double.parseDouble(addField.getText())+"" );
            addField.setStyle("");
            addField.setText("");
        }else addField.setStyle("-fx-border-color: crimson");
        addField.requestFocus();
    }


    public void onAddBefore()
    {
        if(freqList.getSelectionModel().getSelectedItem()==null)return;
        if(isInputDoubleValid(addField))
        {
            if(freqList.getSelectionModel().getSelectedItem()==null)
            {

                showInfoDialogNoHeader("Добавление элемента после...","Не выбран элемент в списке",rootNode.getScene().getWindow(),Modality.WINDOW_MODAL);
                return;
            }
            int selectedIndex = freqList.getSelectionModel().getSelectedIndex();

            freqList.getItems().add(selectedIndex,Double.parseDouble(addField.getText())+"" );
            addField.setStyle("");
            addField.setText("");
        }else addField.setStyle("-fx-border-color: crimson");

        addField.requestFocus();
    }


    public void onDelete()
    {
        if(freqList.getSelectionModel().getSelectedItem()==null)return;
        if(freqList.getSelectionModel().getSelectedItem()!=null)
        {
            int selectedIndex = freqList.getSelectionModel().getSelectedIndex();
            freqList.getItems().remove(selectedIndex);
        }

    }

    public void onAddMulty()
    {
        if(freqList.getSelectionModel().getSelectedItem()==null)return;
        if(isInputDoubleValid(addField))
        {

            int selectedIndex = freqList.getSelectionModel().getSelectedIndex();
            freqList.getItems().set(selectedIndex,freqList.getSelectionModel().getSelectedItem()+"+"+Double.parseDouble(addField.getText()));
            freqList.getSelectionModel().select(selectedIndex);
            addField.setStyle("");
            addField.setText("");
        }else addField.setStyle("-fx-border-color: crimson");

        addField.requestFocus();
    }




    @Override
    public void initialize(URL location, ResourceBundle resources) {
        res = resources;

        addBtn.disableProperty().bind(addField.textProperty().isEmpty());


        delBtn.disableProperty().bind(freqList.getSelectionModel().selectedItemProperty().isNull());
        editBtn.disableProperty().bind(freqList.getSelectionModel().selectedItemProperty().isNull());





        addField.setOnKeyReleased(event -> {

            if (event.isShiftDown() && event.getCode() == KeyCode.ENTER) onAddMulty();
            else if (event.isAltDown() && event.getCode() == KeyCode.ENTER) onAddBefore();
            else if (event.getCode() == KeyCode.ENTER) onAdd();

            else {
                addField.setText(addField.getText().replaceAll("[^0-9.]", ""));
                addField.end();
            }

        });


        addField.setOnKeyPressed(event -> {

             if (event.getCode() == KeyCode.E) onEdit();


        });




        nameFld.setOnKeyReleased(event -> {
            String temp = BaseController.replaceSpecial(nameFld.getText(), "[\\\\\"#$%&'/<=>@\\[\\]^_{|}~]");
            if (!temp.equals(nameFld.getText())) {
                nameFld.setText(temp);
                nameFld.end();
            }

        });
        descriptionFld.setOnKeyReleased(event ->
        {
            String temp = BaseController.replaceSpecial(descriptionFld.getText(), "[\\\\\"#$%&'/<=>@\\[\\]^_{|}~]");
            if (!temp.equals(descriptionFld.getText())) {
                descriptionFld.setText(temp);
                descriptionFld.end();
            }
        });


        freqList.setPlaceholder(new Label(resources.getString("ui.program_frequencies")));
        freqList.setOnMouseClicked(event ->
        {

            if (event.getClickCount() == 2) onEdit();


        });
        freqList.setOnKeyPressed(event ->
        {
            if (event.getCode() == KeyCode.DELETE || event.getCode() == KeyCode.BACK_SPACE) onDelete();
            else if(event.isShiftDown() && event.getCode()==KeyCode.ENTER) onAddMulty();
            else  if(event.isAltDown() && event.getCode()==KeyCode.ENTER) onAddBefore();
            else if(event.getCode()==KeyCode.ENTER) onAdd();
            else    if (event.getCode() == KeyCode.E ) onEdit();
        });

        freqList.requestFocus();
        //freqList.setEditable(true);
        //freqList.setCellFactory( TextFieldListCell.forListView());




        MenuItem smi1=new MenuItem(res.getString("app.add"));
        smi1.setOnAction(event -> onAdd());
        KeyCombination kr = new KeyCodeCombination(KeyCode.ENTER);
        smi1.setAccelerator(kr);

        MenuItem smi2=new MenuItem(res.getString("app.add_multy"));
        smi2.setOnAction(event -> onAddMulty());
        KeyCombination kr1 = new KeyCodeCombination(KeyCode.ENTER,KeyCombination.SHIFT_DOWN);
        smi2.setAccelerator(kr1);

        MenuItem smi3=new MenuItem(res.getString("app.add_before"));
        smi3.setOnAction(event -> onAddBefore());
        KeyCombination kr2 = new KeyCodeCombination(KeyCode.ENTER,KeyCombination.ALT_DOWN);
        smi3.setAccelerator(kr2);




        addMenu=new ContextMenu(smi1, smi2, smi3);
        addBtn.setOnAction(event -> {
                    smi1.setDisable(false);
                    smi2.setDisable(false);
                    smi3.setDisable(false);


                    if(freqList.getSelectionModel().getSelectedItem()==null){smi2.setDisable(true); smi3.setDisable(true);}

            addMenu.show(addBtn, Side.BOTTOM, 0, 0);
                }
        );


        freqsString.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (freqsString.isDisable()) return;
                freqsString.setDisable(true);
                String str = addFromString();
                freqsString.setDisable(false);
                freqsString.requestFocus();
                freqsString.setText(str);
                freqsString.end();
                event.consume();
            } else {
                freqsString.setText(freqsString.getText().replaceAll("[^0-9.;\\+]", ""));
                freqsString.end();
            }
        });

    }


    @Override
    protected void onCompletedInitialization() {

    }

    @Override
    protected void onClose(WindowEvent event) {

    }

    @Override
    public void setParams(Object... params) {
        Data sd=  (Data)rootNode.getUserData();
        nameFld.setText(sd.getOldName());
        descriptionFld.setText(sd.getOldDescription());
        if(!sd.getOldFreq().isEmpty()) for (String s : sd.getOldFreq().split(";")) freqList.getItems().add(s);


    }




public void onEdit()
{
    if(freqList.getSelectionModel().getSelectedItem()==null)return;

    int selectedIndex = freqList.getSelectionModel().getSelectedIndex();

    if (freqList.getSelectionModel().getSelectedItem().contains("+")) {
        //сложное редактирование

        MultyFreqEditDialogController.Data td = new MultyFreqEditDialogController.Data(freqList.getSelectionModel().getSelectedItem());
        try {
            openDialogUserData((Stage) rootNode.getScene().getWindow(), "/fxml/MultyFreqEditDialog.fxml", res.getString("app.text.edit_freqs"), false, StageStyle.DECORATED, 0, 0, 0, 0, td);

            if (td.isChanged()) freqList.getItems().set(selectedIndex, td.getNewFreqs());


            td = null;
        } catch (Exception e) {
            td = null;
            log.error("",e);
            showExceptionDialog(res.getString("app.text.edit_freqs_error"), "", "", e, rootNode.getScene().getWindow(), Modality.WINDOW_MODAL);
            return;
        }

    } else {
        //простое  редактирование


        TextInputValidationController.Data td = new TextInputValidationController.Data(freqList.getSelectionModel().getSelectedItem(), ProgramDialogController::isStringDoubleValid);
        try {
            openDialogUserData((Stage) rootNode.getScene().getWindow(), "/fxml/TextInputValidationDialog.fxml", res.getString("app.text.edit_freq"), false, StageStyle.DECORATED, 0, 0, 0, 0, td);

            if (td.isChanged()) freqList.getItems().set(selectedIndex, Double.parseDouble(td.getNewVal())+"");


            td = null;
        } catch (Exception e) {
            td = null;
            log.error("",e);
            showExceptionDialog(res.getString("app.text.edit_freq_error"), "", "", e, rootNode.getScene().getWindow(), Modality.WINDOW_MODAL);
            return;
        }
    }
}

    public void onSave()
    {
        Data sd=  (Data)rootNode.getUserData();
        sd.setNewName(nameFld.getText());
        sd.setNewDescription(descriptionFld.getText());
        sd.setNewFreq(freqList.getItems().stream().collect(Collectors.joining(";")));
        Stage stage = (Stage)rootNode.getScene().getWindow();
        if(nameFld.getText().isEmpty()) {BaseController.showWarningDialog(res.getString("app.msg.name_absent"),res.getString("app.msg.need_name"),"",stage, Modality.WINDOW_MODAL);return;}





        stage.close();

    }

    public static  class Data
    {
        private String oldName="";
        private  String  oldDescription="";
        private String newName="";
        private  String  newDescription="";
        private String oldFreq="";
        private String newFreq="";


        public Data(String oldName, String oldDescription,String oldFreq) {
            this.oldName = oldName;
            this.oldDescription = oldDescription;
            this.oldFreq=oldFreq;

            this.newName = oldName;
            this.newDescription = oldDescription;
            this.newFreq=oldFreq;

        }

        public String getNewName() {
            return newName;
        }

        private void setNewName(String newName) {
            this.newName = newName;
        }

        public String getNewDescription() {
            return newDescription;
        }

        private void setNewDescription(String newDescription) {
            this.newDescription = newDescription;
        }

        public String getOldName() {
            return oldName;
        }

        public String getOldDescription() {
            return oldDescription;
        }

        public String getNewFreq() {
            return newFreq;
        }

        public void setNewFreq(String newFreq) {
            this.newFreq = newFreq;
        }

        public String getOldFreq() {
            return oldFreq;
        }

        public boolean isChanged()
        {
            return !oldDescription.equals(newDescription) || !oldName.equals(newName) || !oldFreq.equals(newFreq);
        }
        public boolean isNameChanged(){return  !oldName.equals(newName);}
        public boolean isDescriptionChanged(){return  !oldDescription.equals(newDescription);}
        public boolean isFreqChanged(){return  !oldFreq.equals(newFreq);}

    }


    public String addFromString() {
        //строка типа 1;2;4+5+6;6
        String text=freqsString.getText().replaceAll("[^0-9.;\\+]", "");

        if(text.charAt(text.length()-1)==';' || text.charAt(text.length()-1)=='+')text=text.substring(0,text.length()-1);

        String[] split = text.replaceAll(",", ".").split(";");
        List<String> res = new ArrayList<>();

        for (int i = 0; i < split.length; i++) {
            res.add(split[i]);
            int index = split[i].indexOf("+");
            if (index == -1) {


                if (!isStringDoubleValid(split[i])) {
                    BaseController.showInfoDialog("Ввод строки частот", "Не верный формат строки.", "Правильный формат: 25.1;35.4;34+56+29", rootNode.getScene().getWindow(), Modality.APPLICATION_MODAL);

                    return text;
                }
            } else {
                String[] split1 = split[i].split("\\+");

                for (int j = 0; j < split1.length; j++)
                    if (!isStringDoubleValid(split1[j])) {
                        BaseController.showInfoDialog("Ввод строки частот", "Не верный формат строки.", "Правильный формат: 25.1;35.4;34+56+29", rootNode.getScene().getWindow(), Modality.APPLICATION_MODAL);
                        freqsString.setText(text);
                        return text;
                    }
            }
        }

        res.forEach(s -> freqList.getItems().add(s));
        freqsString.setText("");

        return "";
    }

}
