package ru.biomedis.biomedismair3.social.remote_client;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import ru.biomedis.biomedismair3.social.remote_client.dto.Credentials;
import ru.biomedis.biomedismair3.social.remote_client.dto.Token;
import ru.biomedis.biomedismair3.social.remote_client.dto.error.ApiError;

class TokenHolder {

  private LoginClient loginClient;
  private final TokenRepository tokenRepository;
  private Optional<Token> token = Optional.empty();
  private BiFunction<String, String, Optional<Credentials>> inputCredentialAction;
  private ConfirmEmailAction performConfirmEmailAction;
  private Consumer<Exception> preformErrorInfoAction;


  public TokenHolder(LoginClient loginClient, TokenRepository tokenRepository) {
    this.loginClient = loginClient;

    this.tokenRepository = tokenRepository;
  }

  public void setInputCredentialAction(
      BiFunction<String, String, Optional<Credentials>> inputCredentialAction) {
    this.inputCredentialAction = inputCredentialAction;
  }

  public void setPerformConfirmEmailAction(ConfirmEmailAction performConfirmEmailAction) {
    this.performConfirmEmailAction = performConfirmEmailAction;
  }

  public void setPreformErrorInfoAction(
      Consumer<Exception> preformErrorInfoAction) {
    this.preformErrorInfoAction = preformErrorInfoAction;
  }

  /**
   * должен запрашивать автоматически обновление токена.
   */
  //TODO вопрос - при получении нового токена старый на сервере остается в базе? Проблема актуальна, если старый просрали.
  public void processToken() throws BreakByUserException, ServerProblemException {

    if(token.isPresent()) {
       processTokenIfExpired(token.get());
    }else {
      token = tokenRepository.getToken();
      if(token.isPresent()){
         processTokenIfExpired(token.get());
      }else {
         processTokenIfNeedLogin();
      }
    }
  }

  public String getToken(){
    return token.map(Token::getAccessToken).orElse("EMPTY_TOKEN");
  }

  /**
   * Запрашиваетлогин и пароль, пробует получить токен,
   * запросит подтверждение почты если нужно.
   * Будет повторяться если логин кинет BadCredentials
   * @return
   * @throws BreakByUserException
   * @throws ServerProblemException
   */
  private String processTokenIfNeedLogin()
      throws BreakByUserException, ServerProblemException {
    Optional<Credentials> credentials = performInputCredential("","");
    if(!credentials.isPresent()) throw new BreakByUserException("Прервано пользователем");
    boolean retry = true;
   while (retry){
     try {
       token = Optional.of(login(credentials.get()));
       retry = false;
     }catch (BadCredentials e){
       credentials = performInputCredential(credentials.get().getEmail(), credentials.get().getPassword());
       if(!credentials.isPresent()) throw new BreakByUserException("Прервано пользователем");

     }
   }

    return token.get().getAccessToken();
  }


  private String processTokenIfExpired(Token token)
      throws ServerProblemException, BreakByUserException {

    if(token.isExpired()){
      try{
        this.token = Optional.of(loginClient.refreshToken(token.getRefreshToken()));
        tokenRepository.saveToken(token);
        return token.getAccessToken();
      }catch (Exception e){
        if(e instanceof ApiError) {
          ApiError ex = (ApiError) e;
          if(ex.getStatusCode()==400){
            //не верный токен обновления.
            return processTokenIfNeedLogin();
          } else  throw new ServerProblemException(e);
        } else  throw new ServerProblemException(e);

      }
    }else {
      return token.getAccessToken();
    }
  }

  /**
   * Очистит токен, при следующим получении заставит ввести логин и пароль.
   */
  public void resetToken(){
    token = Optional.empty();
    tokenRepository.clearToken();
  }


  private Token login(Credentials credentials)
      throws BreakByUserException, ServerProblemException, BadCredentials {
    Token token;
    try {
      token = loginClient.getToken(credentials);
      tokenRepository.saveToken(token);
      return token;

    }catch (Exception e){
      if(e instanceof ApiError){
        ApiError ex = (ApiError) e;
        if(ex.getNeedValidateEmail()) {
          performConfirmEmail(credentials.getEmail());
          token = login(credentials);
          return token;
        }else if(ex.getStatusCode()==400){
          throw new BadCredentials();
        }else throw new ServerProblemException(e);
      }else throw new ServerProblemException(e);


    }
  }

  public void performLogin() throws ServerProblemException, BreakByUserException {
    processToken();
  }

  private Optional<Credentials> performInputCredential(String email, String pass) {
    if (inputCredentialAction == null) {
      throw new RuntimeException("Необходимо установить экшен получения данных пользователя.");
    }
    return inputCredentialAction.apply(email, pass);//как обработать отказ? Тк нужно будет выйти из запроса
  }

  private void performConfirmEmail(String email) throws BreakByUserException {
    if (performConfirmEmailAction == null) {
      throw new RuntimeException("Необходимо установить экшен для подтверждения почты.");
    }
    performConfirmEmailAction.confirm(email);
  }

  /**
   * Сообщит пользователю об ошибке
   * @param e
   * @return
   */
  private void performErrorInfo(Exception e) {
    if (preformErrorInfoAction == null) {
      throw new RuntimeException("Необходимо установить экшен для подтверждения почты.");
    }
    preformErrorInfoAction.accept(e);
  }


}
