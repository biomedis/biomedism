package ru.biomedis.biomedismair3.social.remote_client;

import java.util.function.Consumer;
import java.util.function.Supplier;
import ru.biomedis.biomedismair3.social.remote_client.dto.Credentials;
import ru.biomedis.biomedismair3.social.remote_client.dto.Token;
import ru.biomedis.biomedismair3.social.remote_client.dto.error.ApiError;

class TokenHolder {

  private LoginClient loginClient;
  private final TokenRepository tokenRepository;
  private Token token;
  private Supplier<Credentials> inputCredentialAction;


  public TokenHolder(LoginClient loginClient, TokenRepository tokenRepository) {
    this.loginClient = loginClient;

    this.tokenRepository = tokenRepository;
  }

  public void setInputCredentialAction(Supplier<Credentials> inputCredentialAction) {
    this.inputCredentialAction = inputCredentialAction;
  }

  //должен запрашивать автоматически обновление токена.
  public String getToken() {
    //проверить наличние токена здесь, если нет, то в базе.
    // если в базе нет, то запросить логин, после чего вернуть токен, сохранив его в базе

    //если в базе есть, то получить его, сохранить тут, проверить время экспирации и если нужно запросить новый по рефреш-токену,
    //если все ок, сохранить в базе, здесь и вернуть его.

    //Если есть токен тут, то проверить дату экспирации, и, если нужно, запросить новый по рефреш-токену,
    //если все ок, сохранить в базе, здесь и вернуть его.

    //на сервере при получении ии рефреше настроить возвращаемые ошибки,
    // чтобы их распознать - при не верных креденшеналах, вывести диалог, но его можно закрыть
    //если почта не валидирована, то вывести окно валидации почты

    return "";
  }

  /**
   * Очистит токен, при следующим получении заставит ввести логин и пароль.
   */
  public void resetToken(){
    token = null;
    resetTokenInBase();
  }

  //обработка ошибок
  public void login(String email, String password) {
    token = loginClient.getToken(new Credentials(email, password));
  }

  private Credentials performInputCredential() {
    if (inputCredentialAction == null) {
      throw new RuntimeException("Необходимо установить экшен получения данных пользователя.");
    }
    return inputCredentialAction.get();
  }

  private Token getTokenFromBase(){
    return null;
  }

  private void resetTokenInBase(){

  }
}
