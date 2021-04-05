package ru.biomedis.biomedismair3.social.remote_client;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import ru.biomedis.biomedismair3.BlockingAction;
import ru.biomedis.biomedismair3.social.remote_client.dto.Credentials;
import ru.biomedis.biomedismair3.social.remote_client.dto.Token;
import ru.biomedis.biomedismair3.social.remote_client.dto.error.ApiError;
import ru.biomedis.biomedismair3.utils.Other.Result;

@Slf4j
class TokenHolder {

  private LoginClient loginClient;
  private AccountClient accountClient;
  private final TokenRepository tokenRepository;
  private Optional<Token> token = Optional.empty();
  private Consumer<String > preformErrorInfoAction;


  public TokenHolder(LoginClient loginClient, AccountClient accountClient, TokenRepository tokenRepository) {
    this.loginClient = loginClient;
    this.accountClient  = accountClient;

    this.tokenRepository = tokenRepository;
  }

  public void setPreformErrorInfoAction(
      Consumer<String> preformErrorInfoAction) {
    this.preformErrorInfoAction = preformErrorInfoAction;
  }

  /**
   * должен запрашивать автоматически обновление токена.
   */
  public void processToken() throws ServerProblemException, NeedAuthByLogin, RequestClientException {

    if(token.isPresent()) {
       processTokenIfExpired(token.get());
    }else {
      token = tokenRepository.getToken();
      if(token.isPresent()){
         processTokenIfExpired(token.get());
      }else {
        throw new NeedAuthByLogin();
      }
    }

  }

  public String getAccessToken(){
    return token.map(Token::getAccessToken).orElse("EMPTY_TOKEN");
  }

  public Optional<Token> getToken(){
    return token;
  }

  public boolean hasToken(){
    return token.isPresent();
  }



  private String processTokenIfExpired(Token token)
      throws ServerProblemException, NeedAuthByLogin, RequestClientException {

    if(token.isExpired()){
      log.info("Token expired at "+ token.getExpired().toString());
      try{
        Token newToken = loginClient.refreshToken(token.getRefreshToken());
        if(newToken==null){
          log.info("!!!!!!!! Получен нулевой ответ от сервера loginClient.refreshToken()==null");
//          //токен не стираем в базе, это сделает setToken, если не кинуть тут исключение.
//          //происхождение null тут не понятно. Если это изменение приведет к невозможности входа тк повторный запрос опять тут упадет
//          //то стоит вернуть и искать проблему дальше
//          throw new Exception("Response from server in null. Expected token");
        }
        setToken(Optional.of(newToken));
        return this.token.get().getAccessToken();
      }catch (Exception e){
        if(e instanceof ApiError) {
          ApiError ex = (ApiError) e;
          if(ex.getStatusCode()==400){
            //не верный токен обновления.
            throw new NeedAuthByLogin();
          } else  throw new ServerProblemException(e);
        } else  throw new RequestClientException(e);
      }
    }else {
      return token.getAccessToken();
    }
  }

  protected void setToken(Optional<Token> token){
    this.token = token;
    if(token.isPresent()) tokenRepository.saveToken(token.get());
    else {
      tokenRepository.clearToken();
    }

  }

  /**
   * Очистит токен, при следующим получении заставит ввести логин и пароль.
   */
  public void resetToken(){
    token = Optional.empty();
    tokenRepository.clearToken();
  }



  /**
   * Сообщит пользователю об ошибке
   * @param m
   * @return
   */
  private void performErrorInfo(String m) {
    if (preformErrorInfoAction == null) {
      throw new RuntimeException("Необходимо установить экшен для отображения ошибки.");
    }
    preformErrorInfoAction.accept(m);
  }


  public boolean performLogout(Stage context) {
    if(!token.isPresent()){
      performErrorInfo("Не удалось выйти из системы");
      log.error("Не удалось выйти из системы, тк отсутствует токен!");
      return false;
    }
    Result<Void> res = BlockingAction
        .actionNoResult(context, () -> accountClient.clearToken(token.get().getRefreshToken()));
    if(!res.isError()){
      resetToken();
      return true;
    }else {
      performErrorInfo("Не удалось выйти из системы");
      log.error("Не удалось выйти из системы", res.getError());
      return false;
    }
  }

  public boolean performLogoutFromAll(Stage ctx) {
    Result<Void> res = BlockingAction
        .actionNoResult(ctx, () -> accountClient.clearAllToken());
    if(!res.isError()){
      resetToken();
      return true;
    }else {
      performErrorInfo("Не удалось выйти из системы");
      log.error("Не удалось выйти из системы", res.getError());
      return false;
    }
  }


}
