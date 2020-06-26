package ru.biomedis.biomedismair3.social.social_panel;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;
import ru.biomedis.biomedismair3.AppController;
import ru.biomedis.biomedismair3.BaseController;

import ru.biomedis.biomedismair3.social.remote_client.BreakByUserException;
import ru.biomedis.biomedismair3.social.remote_client.NeedAuthByLogin;
import ru.biomedis.biomedismair3.social.remote_client.RequestClientException;
import ru.biomedis.biomedismair3.social.remote_client.ServerProblemException;
import ru.biomedis.biomedismair3.social.remote_client.SocialClient;

@Slf4j
public class SocialPanelController extends BaseController implements SocialPanelAPI {

  private ResourceBundle res;

 @FXML
 private Button logIn;

 private SocialClient client;

 private boolean isLogin;


  @Override
  protected void onCompletedInitialization() {
    try {
      client.initProcessToken();
      showLogout();
    } catch (ServerProblemException e) {
      log.error("Ошибка обновления аутентификации. Попробуйте позже.", e);
      AppController.getProgressAPI().setErrorMessage("Ошибка обновления аутентификации. Попробуйте позже.");
    } catch (RequestClientException e) {
      AppController.getProgressAPI().setErrorMessage("Ошибка обработки запроса аутентификации.");
      log.error("Ошибка обработки запроса аутентификации.", e);
    } catch (NeedAuthByLogin needAuthByLogin) {

    }
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
    client.isAuthProperty().addListener((observable, oldValue, newValue) -> {
     if(oldValue ==newValue) return;
      if (newValue){
        showLogout();
      }else showLogin();
    });

  }
  //todo - сделать если чел вошел, то при нажатии - меню, с выбором выйти. выйти со всех устройств.
  // На сервере тоже сделать эти эндпоинты - в том месте где авторизация есть или вручную парсим?
  // Проверить при запросе токена у нас куча новых плодится? Что с этим делать? Если рефреш не подошел,
  // то нужно входить иначе новый создавать, тк мы уже вышли( протух рефреш? Есть тут проверка надо ли вводить, новый токен получать по неверному рефрешу?)
  private  void login(ActionEvent event) {
    if(!isLogin){
      try {
        client.performLogin(getControllerWindow());
        //если  состяние токена изменилось, то сработает обработчик (выше), который обновит интерфейс
      } catch (RequestClientException | ServerProblemException e) {
        AppController.getProgressAPI().setErrorMessage("Не удалось войти в аккаунт. Ошибка обработки запроса аутентификации.");
        log.error("Ошибка обработки запроса аутентификации.", e);
      }
    }
    else {
      //если  состяние токена изменилось, то сработает обработчик (выше), который обновит интерфейс
      client.performLogout(getControllerWindow());
    }
  }

  @Override
  public void showLogin(){
    isLogin=false;
    logIn.setText("Войти");
  }


  @Override
  public void showLogout(){
    isLogin=true;
    logIn.setText("Выйти");
  }
}


