package ru.biomedis.biomedismair3.social.login;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.WindowEvent;
import ru.biomedis.biomedismair3.BaseController;
import ru.biomedis.biomedismair3.social.remote_client.SocialClient;

public class EmailConfirmationController extends BaseController{

  private ResourceBundle res;

 @FXML
 private Label info;

  @FXML
  private TextField inputCode;

 @FXML
 private VBox root;

 private SocialClient client;

private Data data;

  @Override
  protected void onCompletedInitialise() {
    data = (Data)root.getUserData();
  }

  @Override
  protected void onClose(WindowEvent event) {

  }

  @Override
  public void setParams(Object... params) {


  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    res = resources;
    client = SocialClient.INSTANCE;
   
  }

  public static class Data{
      public String email;
      public boolean closedByUser;

    public Data(String email) {
      this.email = email;
    }
  }

}
