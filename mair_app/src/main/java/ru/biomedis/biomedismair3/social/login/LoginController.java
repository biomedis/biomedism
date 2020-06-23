package ru.biomedis.biomedismair3.social.login;


import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;
import ru.biomedis.biomedismair3.AppController;
import ru.biomedis.biomedismair3.BaseController;
import ru.biomedis.biomedismair3.BlockingAction;

import ru.biomedis.biomedismair3.social.remote_client.BadCredentials;
import ru.biomedis.biomedismair3.social.remote_client.LoginClient;
import ru.biomedis.biomedismair3.social.remote_client.RegistrationClient;
import ru.biomedis.biomedismair3.social.remote_client.ServerProblemException;
import ru.biomedis.biomedismair3.social.remote_client.SocialClient;
import ru.biomedis.biomedismair3.social.remote_client.ValidationError;
import ru.biomedis.biomedismair3.social.remote_client.ValidationErrorProcessor;
import ru.biomedis.biomedismair3.social.remote_client.dto.Credentials;
import ru.biomedis.biomedismair3.social.remote_client.dto.Token;
import ru.biomedis.biomedismair3.social.remote_client.dto.error.ApiError;
import ru.biomedis.biomedismair3.social.remote_client.dto.error.ApiValidationError;
import ru.biomedis.biomedismair3.utils.Other.Result;

@Slf4j
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
  private RegistrationClient registrationClient;

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
    registrationClient = SocialClient.INSTANCE.getRegistrationClient();
    loginClient = client.getLoginClient();
    logIn.disableProperty()
        .bind(emailInput.textProperty().isEmpty().or(passwordInput.textProperty().isEmpty()));

    sendCode.disableProperty()
        .bind(emailInput.textProperty().isEmpty().or(inputCode.textProperty().isEmpty()));

  }



  public void onLoginAction() {
    hideValidationMessages();
    final String e = emailInput.getText().trim();
    final String p = passwordInput.getText().trim();

    Result<Token> result = BlockingAction.actionResult(getControllerWindow(), () -> login(e, p));

    if (result.isError()) {

      if (result.getError() instanceof ServerProblemException) {
       AppController.getProgressAPI().setErrorMessage("Ошибка сервера. Попробуйте позже");
       log.error("",e);
      } else if(result.getError() instanceof ValidationError){
        showValidationMessages(((ValidationError)result.getError()).getErrors());
      }
      else if (result.getError() instanceof BadCredentials) {
       AppController.getProgressAPI().setErrorMessage("Не верный логин или пароль");
      } else if (result.getError() instanceof NeedEmailValidationException){
        prepareValidationEmail(e);
      }  else {
       AppController.getProgressAPI().setErrorMessage("Произошла ошибка. Если ошибка повторится обратитесь к разработчикам.");
       showErrorDialog("Ошибка",
           "Произошла ошибка. Если ошибка повторится обратитесь к разработчикам.",
           "",
           getControllerWindow(),
           Modality.APPLICATION_MODAL);
       log.error("",e);
      }

    } else {
      data.token = result.getValue();
      data.cancel = false;
      closedByLoginAction = true;
      getControllerWindow().close();
    }

  }

  private void prepareValidationEmail(final String email){
    login.setVisible(false);
    confirmation.setVisible(true);
    Result<Void> res = BlockingAction.actionNoResult(getControllerWindow(), () -> registrationClient.sendCode(email));
    if(res.isError()){
      AppController.getProgressAPI().setErrorMessage("Не удалось отправить ко на указанную почту.");
      showErrorDialog("Ошибка","Не удалось отправить ко на указанную почту. Проверьте правильность введенного адреса.",
          "", getControllerWindow(),
          Modality.WINDOW_MODAL);
      log.error("",res.getError());
    }
  }


  private Token login(String email, String password)
      throws ServerProblemException, BadCredentials, NeedEmailValidationException, ValidationError {
    Token token;
    try {
      token = loginClient.getToken(new Credentials(email, password));
      return token;

    } catch (Exception e) {
     if (e instanceof ApiError) {
      ApiError ex = (ApiError) e;
      if (ex.isNeedValidateEmail()) {
        throw new NeedEmailValidationException();
      } else if(ex.isValidationError()){
        throw new ValidationError(ValidationErrorProcessor.process(ex));
      }else if (ex.getStatusCode() == 403) {
        throw new BadCredentials();
      }
      else {
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
    Result<Void> result = BlockingAction.actionNoResult(getControllerWindow(), () -> registrationClient.confirmEmail(email, code));
    if (!result.isError()) {
      AppController.getProgressAPI().setErrorMessage("Email успешно подтвержден");
      login.setVisible(true);
      confirmation.setVisible(false);
      emailInput.setText(email);
      onLoginAction();
    }else {
      if(result.getError() instanceof ApiError){
        ApiError ae = (ApiError) result.getError();
        if(ae.getStatusCode()==400){
          showWarningDialog("Подтверждение почты",
              "Необходимо заполнить оба поля",
              "",
              getControllerWindow(),
              Modality.WINDOW_MODAL);
          return;

        }else if(ae.getStatusCode()==404){

          showWarningDialog("Подтверждение почты",
              "Пользователь с указанным email не найден",
              "",
              getControllerWindow(),
              Modality.WINDOW_MODAL);
          return;
        }
      }

      AppController.getProgressAPI().setErrorMessage("Не удалось подтвердить почту");
      showErrorDialog("Подтверждение почты",
          "Не удалось подтвердить почту",
          "Обратитесь к разработчикам, если ошибка появится снова",
          getControllerWindow(),
          Modality.WINDOW_MODAL);
      log.error("",result.getError());
    }
  }



  public void registration() {
    Optional<String> userEmail = RegistrationController
        .performRegistration((Stage) root.getScene().getWindow());
    if(userEmail.isPresent()){
      AppController.getProgressAPI().setInfoMessage("Успешная регистрация!");
      emailInput.setText(userEmail.get());
    }
  }


  private void showValidationMessages(List<ApiValidationError> errorMessages){
   StringBuilder strb = new StringBuilder();
    errorMessages.forEach(e->{
      switch (e.getField()){
        case "email":
          if(!emailInput.getStyleClass().contains("error_border")){
            emailInput.getStyleClass().add("error_border");
          }
          strb.append("\n").append("Email: "+e.getMessage());
          break;
        case "password":
          if(!passwordInput.getStyleClass().contains("error_border")){
            passwordInput.getStyleClass().add("error_border");
          }
          strb.append("\n").append("Пароль: "+e.getMessage());
          break;
        default:
        showErrorDialog(
            "Ошибка валидации",
            "Неизвестная ошибка валидации",
            e.getField()+". "+e.getMessage(),
            getControllerWindow(),
            Modality.WINDOW_MODAL);
      }
    });

    showWarningDialog(
        "Валидация полей формы",
        "Некоторые поля имеют некорректное содержимое",
        strb.toString(),
        getControllerWindow(),
        Modality.WINDOW_MODAL);
  }

  private void hideValidationMessages(){
    emailInput.getStyleClass().remove("error_border");
    passwordInput.getStyleClass().remove("error_border");

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

  public static Optional<Token> openLoginDialog(Stage context){
    try {
      LoginController.Data res =   BaseController.openDialogUserData(
          context,
          "/fxml/LoginDialog.fxml",
          "Вход",
          false,
          StageStyle.UTILITY,
          0, 0, 0, 0,
          new LoginController.Data()
      );
      if(res.cancel) return Optional.empty();
      else return Optional.of(res.token);

    }catch (Exception e){
      log.error("Ошибка открытия диалога входа",e);
      throw new RuntimeException(e);
    }
  }
}
