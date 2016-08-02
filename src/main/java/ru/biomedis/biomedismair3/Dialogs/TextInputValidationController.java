package ru.biomedis.biomedismair3.Dialogs;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ru.biomedis.biomedismair3.BaseController;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Predicate;

/**
 * TextInputValidationController.Data td = new TextInputValidationController.Data("", s -> !BaseController.muchSpecials(s),true);
 try {
 openDialogUserData(getApp().getMainWindow(), "/fxml/TextInputValidationDialog.fxml", "Создание профиля", false, StageStyle.DECORATED, 0, 0, 0, 0, td);

 if (td.isChanged())
 {
 Profile profile = getModel().createProfile(td.getNewVal());

 tableProfile.getItems().add(profile);


 }

 td = null;
 } catch (Exception e) {
 td = null;
 e.printStackTrace();
 showExceptionDialog("Ошибка  создания профиля", "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);
 return;
 }
 *
 *
 * Created by Anama on 10.09.2015.
 */
public class TextInputValidationController extends BaseController {


    @FXML private Button btnOk;
    @FXML private Button btnCancel;

    @FXML private TextField input;
    @FXML private VBox rootNode;

    private Predicate<String> inputValidator= s -> true;

    ResourceBundle res;


    public void setInputValidator(Predicate<String> p) {inputValidator=p;}

    @Override
    public void initialize(URL location, ResourceBundle resources) {
res=resources;


        input.setOnKeyReleased(event ->
        {
            Data sd=  (Data)rootNode.getUserData();
            if(sd.isReplace())
            {
                if (!inputValidator.test(input.getText()) || event.getCharacter().equals(" ") )
                {

                    StringBuilder strb=new StringBuilder();
                    for (char c : input.getText().toCharArray()) if(inputValidator.test(String.valueOf(c))) strb.append(c);

                    input.setText(strb.toString());
                    input.end();
                    strb=null;

                }

            }else
            {
                if (!inputValidator.test(input.getText()) || event.getCharacter().equals(" ") ) input.setStyle("-fx-border-color: crimson");
                else input.setStyle("");
            }


        });
    }


    @Override
    public void setParams(Object... params) {
        Data sd=  (Data)rootNode.getUserData();
        input.setText(sd.getOldVal());
        if(sd.getValidator()!=null)inputValidator= sd.getValidator();

    }

    public void onSave()
    {
        Data sd=  (Data)rootNode.getUserData();
        sd.setNewVal(input.getText().trim());

        Stage stage = (Stage)rootNode.getScene().getWindow();

        if(sd.getNewVal().isEmpty())
        {
            if (!inputValidator.test(sd.getNewVal())){input.setStyle("-fx-border-color: crimson"); return;}
            else input.setStyle("");


        }


        stage.close();

    }

    public void onCancel()
    {
        Stage stage = (Stage)rootNode.getScene().getWindow();
        stage.close();
    }

    public static  class Data
    {
        private String oldVal ="";
        private String newVal ="";
        private Predicate<String>  validator;
        private boolean replace=false;


        public Data(String oldVal,Predicate<String>  validator) {
            this.oldVal = oldVal;
            this.validator=validator;
            newVal =oldVal;

        }

        public Data(String oldVal, Predicate<String> validator, boolean replace) {
            this.oldVal = oldVal;
            this.validator = validator;
            this.replace = replace;
            newVal =oldVal;
        }

        private Predicate<String> getValidator(){return validator;
        }
        public String getNewVal() {
            return newVal;
        }

        private void setNewVal(String newVal) {
            this.newVal = newVal;
        }



        public String getOldVal() {
            return oldVal;
        }



        public boolean isChanged(){ return !oldVal.equals(newVal);}

        public boolean isReplace() {
            return replace;
        }

        public void setReplace(boolean replace) {
            this.replace = replace;
        }
    }
}
