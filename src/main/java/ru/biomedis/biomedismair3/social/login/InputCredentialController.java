package ru.biomedis.biomedismair3.social.login;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.WindowEvent;
import ru.biomedis.biomedismair3.BaseController;
import ru.biomedis.biomedismair3.social.login.EmailConfirmationController.Data;
import ru.biomedis.biomedismair3.social.remote_client.SocialClient;

public class InputCredentialController extends BaseController{

 private ResourceBundle res;

 @FXML
 private Button logIn;

 @FXML
 private TextField email;

 @FXML
 private TextField password;

 @FXML
 private VBox root;

 private SocialClient client;

 private Data data;

 private boolean closedByLoginAction = false;

 @Override
 protected void onCompletedInitialise() {
  data = (Data)root.getUserData();
  closedByLoginAction = false;
 }

 @Override
 protected void onClose(WindowEvent event) {
  if(!closedByLoginAction)data.cancel = true;
 }

 @Override
 public void setParams(Object... params) {


 }

 @Override
 public void initialize(URL location, ResourceBundle resources) {
  res = resources;
  client = SocialClient.INSTANCE;

  logIn.disableProperty().bind(email.textProperty().isEmpty().or(password.textProperty().isEmpty()));

 }

 public void onLoginAction(){
   data.email = email.getText().trim();
   data.password = password.getText().trim();
   data.cancel = false;
   closedByLoginAction = true;
   getControllerWindow().close();
 }

 public static class Data{
  public String email;
  public String password;
  public boolean cancel;

  public Data(String email, String password) {
   this.email = email;
   this.password = password;
  }
 }

}
