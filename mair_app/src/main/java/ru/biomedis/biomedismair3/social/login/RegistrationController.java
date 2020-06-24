package ru.biomedis.biomedismair3.social.login;


import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;
import ru.biomedis.biomedismair3.AppController;
import ru.biomedis.biomedismair3.BaseController;
import ru.biomedis.biomedismair3.BlockingAction;
import ru.biomedis.biomedismair3.Layouts.ProgressPanel.ProgressAPI;
import ru.biomedis.biomedismair3.social.remote_client.RegistrationClient;
import ru.biomedis.biomedismair3.social.remote_client.SocialClient;
import ru.biomedis.biomedismair3.social.remote_client.dto.RegistrationDto;
import ru.biomedis.biomedismair3.social.remote_client.dto.error.ApiError;
import ru.biomedis.biomedismair3.utils.Other.Result;

/**
 * Осуществляет регистрацию пользователя в сервисе MAir
 */
@Slf4j
public class RegistrationController extends BaseController {

  @FXML private Button registrationBtn;
  @FXML private  TextField nameInput;
  @FXML private  TextField emailInput;
  @FXML private  TextField passwordInput;
  @FXML private  TextField firstNameInput;
  @FXML private  TextField lastNameInput;
  @FXML private  TextField countryInput;
  @FXML private  TextField cityInput;
  @FXML private  TextField skypeInput;
  @FXML private  TextArea aboutInput;
  @FXML private VBox root;
  private RegistrationClient registrationClient;
  private ResourceBundle res;
  private ProgressAPI progressAPI;

  private boolean closedByAction=false;

  @Override
  protected void onCompletedInitialization() {

  }

  @Override
  protected void onClose(WindowEvent event) {
    if(!closedByAction)root.setUserData(Optional.empty());
  }

  @Override
  public void setParams(Object... params) {

  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    res = resources;
    registrationClient =  SocialClient.INSTANCE.getRegistrationClient();
    registrationBtn.disableProperty().bind(
         nameInput.textProperty().isEmpty()
        .or(emailInput.textProperty().isEmpty())
        .or(passwordInput.textProperty().isEmpty())
        .or(firstNameInput.textProperty().isEmpty())
        .or(lastNameInput.textProperty().isEmpty())
        .or(countryInput.textProperty().isEmpty())
        .or(cityInput.textProperty().isEmpty())
    );

    progressAPI = AppController.getProgressAPI();
  }

  public void onRegistrationAction() {

    RegistrationDto dto = new RegistrationDto();
    dto.setUserName(nameInput.getText().trim());
    dto.setEmail(emailInput.getText().trim());
    dto.setAbout(aboutInput.getText().trim());
    dto.setFirstName(firstNameInput.getText().trim());
    dto.setLastName(lastNameInput.getText().trim());
    dto.setCity(cityInput.getText().trim());
    dto.setCountry(countryInput.getText().trim());
    dto.setSkype(skypeInput.getText().trim());
    dto.setPassword(passwordInput.getText().trim());

    Result<Void> result = BlockingAction.actionNoResult(getControllerWindow(), () -> registration(dto));

    if(!result.isError()){
      root.setUserData(Optional.of(emailInput.getText().trim()));
      closedByAction= true;
      getControllerWindow().close();
      return;
    }

    if(result.getError() instanceof ApiError){
      ApiError e = (ApiError)result.getError();
      if(e.getStatusCode()==406) {
        if(e.getDebugMessage().equals("email")) showWarningDialog("Регистрация",
            "Регистрация не удалась",
            "Аккаунт с указанным email уже существует",
            root.getScene().getWindow(),
            Modality.WINDOW_MODAL);
        else if(e.getDebugMessage().equals("userName"))showWarningDialog("Регистрация",
            "Регистрация не удалась",
            "Аккаунт с указанным именем уже существует",
            root.getScene().getWindow(),
            Modality.WINDOW_MODAL);
        else showWarningDialog("Регистрация",
              "Регистрация не удалась",
              e.getMessage(),
              root.getScene().getWindow(),
              Modality.WINDOW_MODAL);
      }else if(e.isValidationError()){
         processValidationError(e);
      }else {
        log.error("", e);
        showExceptionDialog("Регистрация",
            "Регистрация не удалась",
            "Ошибка на стороне сервера. ",
            e,
            root.getScene().getWindow(),
            Modality.WINDOW_MODAL);

      }
    }else {
      log.error("", result.getError());
      showExceptionDialog("Регистрация",
          "Регистрация не удалась",
          "",
          new Exception(result.getError()),
          root.getScene().getWindow(),
          Modality.WINDOW_MODAL);
    }
  }

  private void processValidationError(ApiError e) {
    //обвести красным поля, выдать сообщение о том какие поля не верные и какие должны быть значения
    //сделать

    e.getSubErrors().forEach(System.out::println);
  }


  private void registration(RegistrationDto dto){
    registrationClient.registration(dto);
  }

  /**
   * Запускает окно регистрации
   * @param context окно в контексте которго запускается окно регистрации
   * @return строка email или пусто если просто закрыли
   */
  public static Optional<String> performRegistration(Stage context){
    try {
      return    BaseController.openDialogUserData(
          context,
          "/fxml/RegistrationDialog.fxml",
          "Регистрация",
          true,
          StageStyle.UTILITY,
          0, 0, 0, 0,
          Optional.empty()
      );

    }catch (Exception e){
      log.error("Ошибка открытия диалога входа",e);
      throw new RuntimeException(e);
    }


  }
}
