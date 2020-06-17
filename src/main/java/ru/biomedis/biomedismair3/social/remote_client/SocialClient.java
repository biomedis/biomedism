package ru.biomedis.biomedismair3.social.remote_client;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import feign.Feign;
import feign.Feign.Builder;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.Instant;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;

import ru.biomedis.biomedismair3.social.remote_client.dto.Credentials;
import ru.biomedis.biomedismair3.social.remote_client.dto.Token;
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
  private Consumer<Exception> errorAction;

  public static SocialClient INSTANCE = null;

  private static ObjectMapper mapper = new ObjectMapper()
      .enable(SerializationFeature.INDENT_OUTPUT);

  public static void init(String _apiURL, TokenRepository tokenRepository) {
    apiURL = _apiURL;

    INSTANCE = new SocialClient(tokenRepository);
  }

  private SocialClient(TokenRepository tokenRepository) {
    LoginErrorDecoder loginErrorDecoder = new LoginErrorDecoder();
    loginClient = createFeign(false, loginErrorDecoder)
        .target(LoginClient.class, TextUtil.addPath(apiURL, "/token"));

    tokenHolder = new TokenHolder(loginClient, tokenRepository);

    APIErrorDecoder apiErrorDecoder = new APIErrorDecoder(tokenHolder);

    accountClient = createFeign(true, apiErrorDecoder)
        .target(AccountClient.class, TextUtil.addPath(apiURL, "/api/private/users"));

    backupClient = createFeign(true, apiErrorDecoder)
        .target(BackupClient.class, TextUtil.addPath(apiURL, "/api/private/files"));//

    filesClient = createFeign(true, apiErrorDecoder).target(FilesClient.class,
        TextUtil.addPath(apiURL, "/api/private/files"));// /api/private/files

    registrationClient = createFeign(true, loginErrorDecoder)
        .target(RegistrationClient.class, TextUtil.addPath(apiURL, "/api/public/registration"));//

    contactsClient = createFeign(true, apiErrorDecoder)
        .target(ContactsClient.class, TextUtil.addPath(apiURL, "/api/private/relations"));//
  }


  private Feign.Builder createFeign(boolean interceptor, ErrorDecoder errorDecoder) {
    Builder builder = Feign.builder()
        .decoder(new JacksonDecoder())
        .encoder(new JacksonEncoder());
    if (interceptor) {
      builder.requestInterceptor(new AuthInterceptor(tokenHolder));
    }

    builder.errorDecoder(errorDecoder);
    return builder;
  }

  /**
   * Экшен должен открыть окно ввода данных для логина и вернуть эти данные
   */
  public void setLoginAction(Supplier< Optional<Token>> action) {
    tokenHolder.setLoginAction(action);
  }

  public void setErrorAction(Consumer<Exception> action) {
    tokenHolder.setPreformErrorInfoAction(action);
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

  public LoginClient getLoginClient() {
    return loginClient;
  }

  public String getToken() throws BreakByUserException {
    return tokenHolder.getToken();
  }


  /**
   * Инициирует вход
   * @throws ServerProblemException
   * @throws BreakByUserException
   */
  public void performLogin()
      throws ServerProblemException, BreakByUserException {
    tokenHolder.performLogin();
  }

  /**
   * Выброшенная ошибка это ApiError
   */
  public static class APIErrorDecoder implements ErrorDecoder {

    private TokenHolder tokenHolder;

    public APIErrorDecoder(TokenHolder tokenHolder) {
      this.tokenHolder = tokenHolder;
    }

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
        log.error(e);
        apiError = new ApiError();
        apiError.setStatusCode(response.status());
        apiError.setMessage(res);
      } finally {
        log.error(res);
      }
      //400 это bad request, ретраить моно 1 раз, с попыткой токен получить заново(протух в процессе запроса.)
      //возможно тут процессить токен не надо, это сделает интерцептор. Проверить сработает ли он при ретрае.
      if(response.status() >= 400 && response.status() < 500)
      try {
        //TODO так оставлять нельзя!! Если нужно будет логин ввести,то будет ошибка тк не удастся создать окно из потока.
        //TODO нужно при необходимости войти кинуть окно ошибки и остановить операцию. те восстанавливать токен, если наддо войти,
        // здесь если не верная аутентификация, то явно косяк - левый токен был.нужно прервать все
        //то нужно явно об этом сообщить, прервав операцию
        tokenHolder.processToken();//в случае ошибки инициирует процессинг токена
        return new RetryableException(
            response.status(),
            "Service Unavailable",
            response.request().httpMethod(),
            Date.from(Instant.now()),
            response.request());
      } catch (ServerProblemException | BreakByUserException e) {
        return e;
      }

      // исп errorAction для информировани об ошибках, если их нельзя обработать
      //проверка типа ошибки. Нужен ретрай если токен просрочен - автоматом обновиться при запросе.
      //если требуется логин, те утерян токен - ошибка токена, то  сбрасываем токен в холдере и делаем ретрай - дальше все само произойдет из холдера.
      return apiError;
    }
  }


  public static class LoginErrorDecoder implements ErrorDecoder {


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
        log.error(e);
        apiError = new ApiError();
        apiError.setStatusCode(response.status());
        apiError.setMessage(res);
      } finally {
        log.error(res);
      }

      return apiError;
    }
  }

  static class AuthInterceptor implements RequestInterceptor {

    private final TokenHolder tokenHolder;

    public AuthInterceptor(TokenHolder tokenHolder) {
      this.tokenHolder = tokenHolder;
    }

    @Override
    public void apply(RequestTemplate template) {
      //если токена нет или он косячный, то будет ошибка. Ее отловим в декодере,
      // проведем процесс обновления токена и повторим запрос, при новом запросе тут будет верный токен
      template.header("Authorization", "Bearer " + tokenHolder.getToken());
      //TODO избегать необходимости вводить логин и пароль! Иначе ошибку кидать или как-то еще. Постараться не пустить без токена
    }
  }

}
