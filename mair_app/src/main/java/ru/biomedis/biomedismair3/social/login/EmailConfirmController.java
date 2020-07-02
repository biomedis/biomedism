package ru.biomedis.biomedismair3.social.login;


import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;
import ru.biomedis.biomedismair3.AppController;
import ru.biomedis.biomedismair3.BaseController;
import ru.biomedis.biomedismair3.BlockingAction;
import ru.biomedis.biomedismair3.social.remote_client.LoginClient;
import ru.biomedis.biomedismair3.social.remote_client.RegistrationClient;
import ru.biomedis.biomedismair3.social.remote_client.SocialClient;
import ru.biomedis.biomedismair3.social.remote_client.dto.error.ApiError;
import ru.biomedis.biomedismair3.utils.Other.Result;

@Slf4j
public class EmailConfirmController extends BaseController {

  private ResourceBundle res;

  @FXML
  private Button sendCode;

  @FXML
  private TextField inputCode;

  private SocialClient client;

  private Data data;

  private boolean closedByLoginAction = false;
  private LoginClient loginClient;
  private RegistrationClient registrationClient;
  private String email;
  private boolean needSendCode;

  @Override
  protected void onCompletedInitialization() {
    data = (Data) getInputDialogData();
    data.result=false;

    if(needSendCode){
      Result<Void> res = BlockingAction.actionNoResult(getControllerWindow(), () -> registrationClient.sendCode(email));
      if(res.isError()){
        AppController.getProgressAPI().setErrorMessage("Не удалось отправить ко на указанную почту.");
        showErrorDialog("Ошибка","Не удалось отправить код на почту аккаунта. "+email,
            "", getControllerWindow(),
            Modality.WINDOW_MODAL);
        log.error("",res.getError());
        getControllerWindow().close();
      }
    }
  }

  @Override
  protected void onClose(WindowEvent event) {
  }

  @Override
  public void setParams(Object... params) {
    if(params.length==0) throw new RuntimeException("Не верные параметры. Должен быть параметр email");
    email = (String)params[0];
    needSendCode = (Boolean) params[1];
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    res = resources;
    client = SocialClient.INSTANCE;
    registrationClient = SocialClient.INSTANCE.getRegistrationClient();
    loginClient = client.getLoginClient();

    sendCode.disableProperty().bind(inputCode.textProperty().isEmpty());
  }



  public void onConfirmEmailAction(){
    final String code = inputCode.getText().trim();

    Result<Void> result = BlockingAction.actionNoResult(getControllerWindow(), () -> registrationClient.confirmEmail(email.trim(), code));
    if (!result.isError()) {
      AppController.getProgressAPI().setErrorMessage("Email успешно подтвержден");
      data.result = true;
      getControllerWindow().close();
    }else {
      if(result.getError() instanceof ApiError){
        ApiError ae = (ApiError) result.getError();
        if(ae.getStatusCode()==400){
          showWarningDialog("Подтверждение почты",
              "Не верный код.",
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


  /**
   * Повторная отправка email
   *
   */
  public void onResend() {
    Result<Void> result = BlockingAction.actionNoResult(getControllerWindow(),
        () -> registrationClient.sendCode(email.trim()));
    if(!result.isError()){
      showInfoDialog(
          "Отправка кода",
          "Код успешно отправлен на указанную почту",
          "",
          getControllerWindow(),
          Modality.WINDOW_MODAL);
    }else {
      processSendCodeError(result);
    }
  }



  private void processSendCodeError(Result<Void> result){
    if(result.getError() instanceof ApiError){
      ApiError err = (ApiError)result.getError();
      if(err.getStatusCode()==404) {
        showWarningDialog(
            "Отправка кода",
            "Не удалось отправить код",
            "Аккаунт с указанным email не существует",
            getControllerWindow(),
            Modality.WINDOW_MODAL);

      }else if(err.getStatusCode()==400) {
        showWarningDialog(
            "Отправка кода",
            "Не удалось отправить код",
            "Введен не корректный email",
            getControllerWindow(),
            Modality.WINDOW_MODAL);

      }
    }else {
      showWarningDialog(
          "Отправка кода",
          "Не удалось отправить код для восстановления",
          "Попробуйте позже. Если ошибка повторится, обратитесь к разработчикам",
          getControllerWindow(),
          Modality.WINDOW_MODAL);
      log.error("Отправка кода восстановления",result.getError());
    }
  }

  public static class Data {
    public boolean result;

   public Data() {

   }
  }

  /**
   * Открывает диалог подтверждения переданной почты. Это должна быть почта зарегистрированого пользователя. Код отправляется контроллером при открытии
   * @param context
   * @param email
   * @return true если успешно
   */
  public static boolean openConfirmationEmailDialog(Stage context, String email){
      return openConfirmationEmailDialog( context,  email,  true);
  }

  /**
   * Открывает диалог подтверждения переданной почты. Это должна быть почта зарегистрированого пользователя
   * @param context
   * @param email
   * @param needSendCode  нужно ли отправлять код. В случаи регистрации, код не надо отправлять, тк его отправит контроллер регистрации
   * @return true если успешно
   */
  public static boolean openConfirmationEmailDialog(Stage context, String email, boolean needSendCode){
    try {
      Data res =   BaseController.openDialogUserData(
          context,
          "/fxml/EmailConfirmDialog.fxml",
          "Подтверждение email",
          false,
          StageStyle.UTILITY,
          0, 0, 0, 0,
          new Data(),
          email,
          Boolean.valueOf(needSendCode)
      );

     return res.result;

    }catch (Exception e){
      log.error("Ошибка открытия диалога подтверждения почты",e);
      throw new RuntimeException(e);
    }
  }
}
