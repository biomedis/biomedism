package ru.biomedis.biomedismair3.social.social_panel;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.WindowEvent;
import ru.biomedis.biomedismair3.AppController;
import ru.biomedis.biomedismair3.BaseController;

import ru.biomedis.biomedismair3.social.remote_client.BreakByUserException;
import ru.biomedis.biomedismair3.social.remote_client.ServerProblemException;
import ru.biomedis.biomedismair3.social.remote_client.SocialClient;


public class SocialPanelController extends BaseController implements SocialPanelAPI {

  private ResourceBundle res;

 @FXML
 private Button logIn;

 private SocialClient client;



  @Override
  protected void onCompletedInitialise() {

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
    logIn.setOnAction(this::login);
  }

  private  void login(ActionEvent event) {
    ///client.login("lightway821@gmail.com", "123123");
    try {
      client.performLogin();
    } catch (ServerProblemException e) {
      AppController.getProgressAPI().setErrorMessage(e.getMessage());
    } catch (BreakByUserException e) {

    }
  }
}
