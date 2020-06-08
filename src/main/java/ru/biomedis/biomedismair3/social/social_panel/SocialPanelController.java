package ru.biomedis.biomedismair3.social.social_panel;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import ru.biomedis.biomedismair3.BaseController;
import ru.biomedis.biomedismair3.social.remote_client.LoginClient;
import ru.biomedis.biomedismair3.social.remote_client.SocialClient;
import ru.biomedis.biomedismair3.social.remote_client.dto.Credentials;
import ru.biomedis.biomedismair3.social.remote_client.dto.Token;

public class SocialPanelController extends BaseController implements SocialPanelAPI {

  private ResourceBundle res;

 @FXML
 private Button logIn;

 private LoginClient client;



  @Override
  protected void onCompletedInitialise() {

  }

  @Override
  public void setParams(Object... params) {

  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    res = resources;
    client = SocialClient.INSTANCE.getLoginClient();
    logIn.setOnAction(this::login);
  }

  private  void login(ActionEvent event) {
    Token token = client.getToken(new Credentials("lightway821@gmail.com", "123123"));
    System.out.println(token);
  }
}
