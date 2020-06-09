package ru.biomedis.biomedismair3.social.remote_client;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;
import feign.Feign;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.Response;
import feign.codec.ErrorDecoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;

import ru.biomedis.biomedismair3.social.remote_client.dto.Credentials;
import ru.biomedis.biomedismair3.social.remote_client.dto.error.ApiError;
import ru.biomedis.biomedismair3.utils.Text.TextUtil;

@Log4j2
public class SocialClient {
 private final AccountClient accountClient;
 private final BackupClient backupClient;
 private final FilesClient filesClient;
 private final LoginClient loginClient;
 private final RegistrationClient registrationClient;
 private final ContactsClient contactsClient;
 private static String apiURL;

  private final TokenHolder tokenHolder;
  private Consumer<ApiError> errorAction;

 public static  SocialClient INSTANCE = null;

 private static ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

public static void init(String _apiURL, TokenRepository tokenRepository){
  apiURL = _apiURL;

  INSTANCE = new SocialClient(tokenRepository);
}

  private SocialClient(TokenRepository tokenRepository) {

    accountClient = createFeign(true).target(AccountClient.class, TextUtil.addPath(apiURL, "/api/private/users"));
    backupClient = createFeign(true).target(BackupClient.class, TextUtil.addPath(apiURL, "/api/private/files"));//
    filesClient = createFeign(true).target(FilesClient.class, TextUtil.addPath(apiURL, "/api/private/files"));// /api/private/files

    registrationClient = createFeign(true).target(RegistrationClient.class, TextUtil.addPath(apiURL, "/api/public/registration"));//
    contactsClient = createFeign(true).target(ContactsClient.class, TextUtil.addPath(apiURL, "/api/private/relations"));//

    loginClient = createFeign(false)
        .target(LoginClient.class, TextUtil.addPath(apiURL, "/token"));//

    tokenHolder = new TokenHolder(loginClient, tokenRepository);
  }



  private Feign.Builder createFeign(boolean interceptor){
    return Feign.builder()
        .decoder(new JacksonDecoder())
        .encoder(new JacksonEncoder())
        .requestInterceptor(new AuthInterceptor(tokenHolder))
        .errorDecoder(new APIErrorDecoder(tokenHolder));
  }

  /**
   * Экшен должен открыть окно ввода данных для логина и вернуть эти данные
   * @param inputCredentialAction
   */
  public void setInputCredentialAction(Supplier<Credentials> inputCredentialAction){
      tokenHolder.setInputCredentialAction(inputCredentialAction);
  }

  public void setErrorAction(Consumer<ApiError> action){
    errorAction = action;
  }

  public AccountClient getAccountClient() {
    return accountClient;
  }

  public BackupClient getBackupClient() {
    return backupClient;
  }

  public FilesClient getFilesClient() {
    return filesClient;
  }

  public RegistrationClient getRegistrationClient() {
    return registrationClient;
  }

  public ContactsClient getContactsClient() {
    return contactsClient;
  }


  public String getToken(){
    return tokenHolder.getToken();
  }


  public void login( String email, String password){
    tokenHolder.login(email, password);
  }

  /**
   * Выброшенная ошибка это ApiError
   */
  public static class APIErrorDecoder implements ErrorDecoder{
    private TokenHolder tokenHolder;

    public APIErrorDecoder(TokenHolder tokenHolder) {
      this.tokenHolder = tokenHolder;
    }

    @Override
    public Exception decode(String s, Response response) {
      String resp = "";
      try(BufferedReader reader = new BufferedReader(response.body().asReader(StandardCharsets.UTF_8))){
        resp = reader.lines().collect(Collectors.joining());
      }catch (Exception e){
        return e;
      }
      ApiError apiError = null;
      String res = response.request().url()+"\n"+response.request().toString()+"\nResponse status:  "+response.status()+"\nResponse: "+resp;
      try {
        apiError =  mapper.readValue(resp, ApiError.class);
      }catch (Exception e){
        log.error(e);
        apiError = new ApiError();
        apiError.setStatusCode(response.status());
        apiError.setMessage(res);
      }finally {
        log.error(res);
      }
      // исп errorAction для информировани об ошибках, если их нельзя обработать
      //проверка типа ошибки. Нужен ретрай если токен просрочен - автоматом обновиться при запросе.
      //если требуется логин, те утерян токен - ошибка токена, то  сбрасываем токен в холдере и делаем ретрай - дальше все само произойдет из холдера.
      return apiError;
    }
  }

  static class AuthInterceptor implements RequestInterceptor {
    private TokenHolder tokenHolder;

    public AuthInterceptor(TokenHolder tokenHolder) {
      this.tokenHolder = tokenHolder;
    }

    @Override public void apply(RequestTemplate template) {
      template.header("Authorization", "Bearer "+tokenHolder.getToken());
    }
  }

}
