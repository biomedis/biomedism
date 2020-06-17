package ru.biomedis.biomedismair3.social.login;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.swing.Spring;
import ru.biomedis.biomedismair3.AppController;
import ru.biomedis.biomedismair3.BaseController;
import ru.biomedis.biomedismair3.BlockingAction;
import ru.biomedis.biomedismair3.Log;
import ru.biomedis.biomedismair3.social.remote_client.BadCredentials;
import ru.biomedis.biomedismair3.social.remote_client.LoginClient;
import ru.biomedis.biomedismair3.social.remote_client.ServerProblemException;
import ru.biomedis.biomedismair3.social.remote_client.SocialClient;
import ru.biomedis.biomedismair3.social.remote_client.dto.Credentials;
import ru.biomedis.biomedismair3.social.remote_client.dto.Token;
import ru.biomedis.biomedismair3.social.remote_client.dto.error.ApiError;
import ru.biomedis.biomedismair3.utils.Other.Result;

public class LoginController extends BaseController {

  private ResourceBundle res;


  @FXML
  private Button sendCode;

  @FXML
  private Button logIn;

  @FXML
  private TextField emailInput;

  @FXML
  private TextField passwordInput;

  @FXML
  private StackPane root;

  @FXML
  private VBox confirmation;

  @FXML
  private VBox login;

  @FXML
  private TextField inputCode;

  private SocialClient client;

  private Data data;

  private boolean closedByLoginAction = false;
  private LoginClient loginClient;

  @Override
  protected void onCompletedInitialise() {
    data = (Data) root.getUserData();
    closedByLoginAction = false;
  }

  @Override
  protected void onClose(WindowEvent event) {
   if (!closedByLoginAction) {
    data.cancel = true;
   }
  }

  @Override
  public void setParams(Object... params) {

  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    res = resources;
    client = SocialClient.INSTANCE;
    loginClient = client.getLoginClient();
    logIn.disableProperty()
        .bind(emailInput.textProperty().isEmpty().or(passwordInput.textProperty().isEmpty()));

    sendCode.disableProperty()
        .bind(emailInput.textProperty().isEmpty().or(inputCode.textProperty().isEmpty()));

  }



  public void onLoginAction() {
    final String e = emailInput.getText().trim();
    final String p = passwordInput.getText().trim();

    Result<Token> result = BlockingAction.actionResult(getControllerWindow(), () -> login(e, p));

    if (result.isError()) {

      if (result.getError() instanceof ServerProblemException) {
       AppController.getProgressAPI().setErrorMessage("Ошибка сервера. Попробуйте позже");
       Log.logger.error(e);
      } else if (result.getError() instanceof BadCredentials) {
       AppController.getProgressAPI().setErrorMessage("Не верный логин или пароль");
      } else if (result.getError() instanceof NeedEmailValidationException){
        login.setVisible(false);
        confirmation.setVisible(true);
      } else {
       AppController.getProgressAPI().setErrorMessage("Произошла ошибка. Если ошибка повторится обратитесь к разработчикам.");
       showErrorDialog("Ошибка","Произошла ошибка. Если ошибка повторится обратитесь к разработчикам.","", getControllerWindow(),
           Modality.APPLICATION_MODAL);
       Log.logger.error(e);
      }

    } else {
      data.token = result.getValue();
      data.cancel = false;
      closedByLoginAction = true;
      getControllerWindow().close();
    }

  }


  private Token login(String email, String password)
      throws ServerProblemException, BadCredentials, NeedEmailValidationException {
    Token token;
    try {
      token = loginClient.getToken(new Credentials(email, password));
      return token;

    } catch (Exception e) {
     if (e instanceof ApiError) {
      ApiError ex = (ApiError) e;
      if (ex.isNeedValidateEmail()) {
        throw new NeedEmailValidationException();
      } else if (ex.getStatusCode() == 400) {
       throw new BadCredentials();
      } else {
       throw new ServerProblemException(e);
      }
     } else {
      throw new ServerProblemException(e);
     }


    }
  }

  public void onConfirmEmailAction(){
    final String code = inputCode.getText().trim();
    final String email = emailInput.getText().trim();
    Result<Void> result = BlockingAction.actionNoResult(getControllerWindow(), () -> confirmEmail(email, code));
    if (!result.isError()) {
      login.setVisible(true);
      confirmation.setVisible(false);
      onLoginAction();
    }
  }

  private void confirmEmail(String email, String code) {

  }

  public void registration() {
    Optional<String> userEmail = RegistrationController
        .performRegistration((Stage) root.getScene().getWindow());
    if(userEmail.isPresent()){
      AppController.getProgressAPI().setInfoMessage("Успешная регистрация!");
      emailInput.setText(userEmail.get());
    }
  }


  public static class Data {

    public Token token;
    public boolean cancel;

   public Data() {

   }

    public Data(Token token) {
      this.token = token;
    }
  }

  private static class NeedEmailValidationException extends Exception{}
}
