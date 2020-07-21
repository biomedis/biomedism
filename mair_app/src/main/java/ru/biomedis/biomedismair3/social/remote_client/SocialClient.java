package ru.biomedis.biomedismair3.social.remote_client;


import static java.util.concurrent.TimeUnit.SECONDS;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.Feign;
import feign.Feign.Builder;
import feign.Logger.Level;
import feign.Request.Options;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.Response;
import feign.RetryableException;
import feign.Retryer;
import feign.Retryer.Default;
import feign.codec.ErrorDecoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.okhttp.OkHttpClient;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j2;
import ru.biomedis.biomedismair3.App;
import ru.biomedis.biomedismair3.social.login.LoginController;
import ru.biomedis.biomedismair3.social.remote_client.dto.Token;
import ru.biomedis.biomedismair3.social.remote_client.dto.error.ApiError;
import ru.biomedis.biomedismair3.utils.OS.OSValidator;
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
  private final SimpleBooleanProperty isAuth = new SimpleBooleanProperty(false);

  private  TokenHolder tokenHolder = null;
  private Consumer<String> errorAction;

  public static SocialClient INSTANCE = null;

  private static ObjectMapper mapper = new ObjectMapper()
      .enable(SerializationFeature.INDENT_OUTPUT);


  public static void init(String _apiURL, TokenRepository tokenRepository) {
    apiURL = _apiURL;

    INSTANCE = new SocialClient(tokenRepository);
  }

  public boolean isIsAuth() {
    return isAuth.get();
  }

  /**
   * Можно использовать для отключения включения функций в зависимости от вошел ли пользователь или
   * нет
   */
  public SimpleBooleanProperty isAuthProperty() {
    return isAuth;
  }

  private SocialClient(TokenRepository tokenRepository) {
    Supplier<String> tokenProvider =  () -> tokenHolder==null?"":tokenHolder.getAccessToken();

    LoginErrorDecoder loginErrorDecoder = new LoginErrorDecoder();
    loginClient = createFeign(false, loginErrorDecoder, tokenProvider)
        .target(LoginClient.class, TextUtil.addPath(apiURL, "/token"));

    APIErrorDecoder apiErrorDecoder = new APIErrorDecoder();

    accountClient = createFeign(true, apiErrorDecoder, tokenProvider)
        .target(AccountClient.class, TextUtil.addPath(apiURL, "/api/private/users"));

    backupClient = createFeign(true, apiErrorDecoder, tokenProvider)
        .target(BackupClient.class, TextUtil.addPath(apiURL, "/api/private/files"));//

    filesClient = createFeign(true, apiErrorDecoder, tokenProvider).target(FilesClient.class,
        TextUtil.addPath(apiURL, "/api/private/files"));// /api/private/files

    registrationClient = createFeign(false, loginErrorDecoder, tokenProvider)
        .target(RegistrationClient.class, TextUtil.addPath(apiURL, "/api/public/registration"));//

    contactsClient = createFeign(true, apiErrorDecoder, tokenProvider)
        .target(ContactsClient.class, TextUtil.addPath(apiURL, "/api/private/relations"));//

    tokenHolder = new TokenHolder(loginClient, accountClient,  tokenRepository);
  }


  private Feign.Builder createFeign(boolean interceptor, ErrorDecoder errorDecoder, Supplier<String> tokenProvider) {
    ObjectMapper mapper = new ObjectMapper();
    JavaTimeModule timeModule = new JavaTimeModule();
    mapper.registerModule(timeModule);
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    mapper.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false);

    Builder builder = Feign.builder()
        .retryer(new Default(100, SECONDS.toMillis(1), 2))
        .client(new OkHttpClient())
        .options(new Options(15, TimeUnit.SECONDS, 30, TimeUnit.SECONDS, false))
        .logger(new CustomFeignRequestLogging())
        .logLevel(Level.FULL)
        .decoder(new JacksonDecoder(mapper))
        .encoder(new JacksonEncoder(mapper));
    if (interceptor) {
      builder.requestInterceptor(new AuthInterceptor(tokenProvider));
    }else {
      builder.requestInterceptor(new UserAgentInterceptor());
    }

    builder.errorDecoder(errorDecoder);
    return builder;
  }


  public void setErrorAction(Consumer<String> action) {
    tokenHolder.setPreformErrorInfoAction(action);
    errorAction = action;
  }

  /**
   * Можно инициализировать получение токена из базы или обновления с сервера.
   */
  public void initProcessToken()
      throws ServerProblemException, RequestClientException, NeedAuthByLogin {
    try {
      tokenHolder.processToken();
      isAuth.set(tokenHolder.hasToken());
    } catch (NeedAuthByLogin e) {
      isAuth.set(false);
      throw e;
    }
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

  public LoginClient getLoginClient() {
    return loginClient;
  }

  public String getAccessToken(){
    return tokenHolder.getAccessToken();
  }

  public Optional<Token> getToken()  {
    return tokenHolder.getToken();
  }



  /**
   * Инициирует вход
   */
  public boolean performLogin(Stage ctx)  throws RequestClientException, ServerProblemException{
    try {
      initProcessToken();
      return true;
    } catch (NeedAuthByLogin e) {
      Optional<Token> token = LoginController.openLoginDialog(ctx);
      tokenHolder.setToken(token);
      isAuth.set(tokenHolder.hasToken());
      return token.isPresent();
    } catch (ServerProblemException  | RequestClientException e) {
      isAuth.set(false);
      log.error("", e);
      throw e;
    }
  }

  /**
   * Инициализирует выход и очистку токена
   */
  public boolean performLogout(Stage ctx) {
    boolean res = tokenHolder.performLogout(ctx);
    if (res) {
      isAuth.set(false);
    }
    return res;
  }

  /**
   * Выход со всех устройств, удалит все токены пользователя на сервере
   */
  public boolean performLogoutFromAll(Stage ctx) {
    boolean res = tokenHolder.performLogoutFromAll(ctx);
    if (res) {
      isAuth.set(false);
    }
    return res;
  }

  /**
   * Выброшенная ошибка это ApiError
   */
  private class APIErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String s, Response response) {
      String resp = "";
      try (BufferedReader reader = new BufferedReader(
          response.body().asReader(StandardCharsets.UTF_8))) {
        resp = reader.lines().collect(Collectors.joining());
      } catch (Exception e) {
        return e;
      }
      ApiError apiError = null;
      String res =
          response.request().url() + "\n" + response.request().toString() + "\nResponse status:  "
              + response.status() + "\nResponse: " + resp;
      try {
        apiError = mapper.readValue(resp, ApiError.class);
      } catch (Exception e) {
        log.error("Ошибка парсинга объекта ошибки",e);
        apiError = new ApiError();
        apiError.setStatusCode(response.status());
        apiError.setMessage(res);
      } finally {
        log.error(res);
      }

      if (apiError.isTokenError()) {
        try {
          //вопрос - при BAD_TOKEN нам по сути нужно получить новый, можно сначала рефрешнуть токен, это нужно тут учесть как-то

          initProcessToken();//в случае ошибки инициирует процессинг токена
          //повтор запроса, если успешно обновлен токен
          return new RetryableException(
              response.status(),
              "Token is invalid",
              response.request().httpMethod(),
              Date.from(Instant.now()),
              response.request());


        } catch (NeedAuthByLogin e) {
          //получать токен из декодера нельзя будет ошибка. поэтому только информирование о необходимости войти
          return e;
        } catch (Exception e) {
          log.error("", e);
          return e;
        }
      }
      return apiError;
    }
  }


  private static class LoginErrorDecoder implements ErrorDecoder {


    @Override
    public Exception decode(String s, Response response) {
      String resp = "";
      try (BufferedReader reader = new BufferedReader(
          response.body().asReader(StandardCharsets.UTF_8))) {
        resp = reader.lines().collect(Collectors.joining());
      } catch (Exception e) {
        log.error("", e);
        return e;
      }
      ApiError apiError = null;
      String res =
          response.request().url() + "\n" + response.request().toString() + "\nResponse status:  "
              + response.status() + "\nResponse: " + resp;
      try {
        apiError = mapper.readValue(resp, ApiError.class);
      } catch (Exception e) {
        log.error(e);
        apiError = new ApiError();
        apiError.setStatusCode(response.status());
        apiError.setMessage(res);
      }

      return apiError;
    }
  }

  private static class AuthInterceptor implements RequestInterceptor {

    private final Supplier<String> tokenProvider;

    public AuthInterceptor(Supplier<String> tokenProvider) {
      this.tokenProvider = tokenProvider;
    }

    @Override
    public void apply(RequestTemplate template) {
      //может вернуть пустой токен, если пользователь не аутентифицирован( нет валидного токена в базе, не удалось обновить токен)
      //при неверном токене, в декодере ошибки, будет проведена попытка обновить или извлечь из базы
      template.removeHeader("Authorization");
      template.removeHeader("User-Agent");
      template.header("Authorization", "Bearer " + tokenProvider.get());
      template.header("User-Agent", "("+OSValidator.osAlt()+") "+"BiomedisMAir/"+ App.getAppVersion());

    }
  }

  private static class UserAgentInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
      template.header("User-Agent", "("+OSValidator.osAlt()+") "+"BiomedisMAir/"+ App.getAppVersion());
    }
  }

}
