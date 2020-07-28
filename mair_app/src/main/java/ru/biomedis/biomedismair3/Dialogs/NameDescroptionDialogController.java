package ru.biomedis.biomedismair3.Dialogs;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import ru.biomedis.biomedismair3.BaseController;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Anama on 10.09.2015.
 */
public class NameDescroptionDialogController extends BaseController {


    @FXML private Button saveBtn;
    @FXML private TextField nameFld;
    @FXML private TextArea descriptionFld;
    @FXML private GridPane rootNode;
    ResourceBundle res;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
res=resources;

       nameFld.setOnKeyReleased(event ->{
           String temp= BaseController.replaceSpecial(nameFld.getText(),"[\\\\\"#$%&'/<=>@\\[\\]^_{|}~]");
           if(!temp.equals(nameFld.getText()))
           {
               nameFld.setText(temp);
               nameFld.end();
           }
       }  );
        descriptionFld.setOnKeyReleased(event ->{
            String temp= BaseController.replaceSpecial(descriptionFld.getText(),"[\\\\\"#$%&'/<=>@\\[\\]^_{|}~]");
            if(!temp.equals(descriptionFld.getText()))
            {
                descriptionFld.setText(temp);
                descriptionFld.end();
            }
        } );


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
    }

    public void onSave()
    {
        Data sd=  (Data)rootNode.getUserData();
        sd.setNewName(BaseController.replaceSpecial(nameFld.getText()));
        sd.setNewDescription(BaseController.replaceSpecial(descriptionFld.getText()));
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


        public Data(String oldName, String oldDescription) {
            this.oldName = oldName;
            this.oldDescription = oldDescription;
            this.newDescription=oldDescription;
            this.newName=oldName;
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

        public boolean isChanged()
        {
           return !oldDescription.equals(newDescription) || !oldName.equals(newName);
        }
        public boolean isNameChanged(){return  !oldName.equals(newName);}
        public boolean isDescriptionChanged(){return  !oldDescription.equals(newDescription);}
    }
}
