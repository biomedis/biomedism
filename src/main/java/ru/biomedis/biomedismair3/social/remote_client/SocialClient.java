package ru.biomedis.biomedismair3.social.remote_client;


import feign.Feign;
import feign.Response;
import feign.codec.ErrorDecoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import java.io.BufferedReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;

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

 public static  SocialClient INSTANCE = null;

public static void init(String _apiURL){
  apiURL = _apiURL;
  INSTANCE = new SocialClient();

}

  private SocialClient() {
    accountClient = createFeign().target(AccountClient.class, TextUtil.addPath(apiURL, "/api/private/users"));
    backupClient = createFeign().target(BackupClient.class, TextUtil.addPath(apiURL, "/api/private/files"));//
    filesClient = createFeign().target(FilesClient.class, TextUtil.addPath(apiURL, "/api/private/files"));// /api/private/files
    loginClient = createFeign().target(LoginClient.class, TextUtil.addPath(apiURL, "/token"));//
    registrationClient = createFeign().target(RegistrationClient.class, TextUtil.addPath(apiURL, "/api/public/registration"));//
    contactsClient = createFeign().target(ContactsClient.class, TextUtil.addPath(apiURL, "/api/private/relations"));//

  }

  private Feign.Builder createFeign(){
    return Feign.builder()
        .decoder(new JacksonDecoder())
        .encoder(new JacksonEncoder())
        .errorDecoder(new APIErrorDecoder());
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

  public LoginClient getLoginClient() {
    return loginClient;
  }

  public RegistrationClient getRegistrationClient() {
    return registrationClient;
  }

  public ContactsClient getContactsClient() {
    return contactsClient;
  }

  public static class APIErrorDecoder implements ErrorDecoder{

    @Override
    public Exception decode(String s, Response response) {
      String resp = "";
      try(BufferedReader reader = new BufferedReader(response.body().asReader(StandardCharsets.UTF_8))){
        resp = reader.lines().collect(Collectors.joining());
      }catch (Exception e){
        return e;
      }
      String res = response.request().url()+"\n"+response.request().toString()+"\nResponse status:  "+response.status()+"\nResponse: "+resp;
      log.error(res);
      return new Exception("res");
    }
  }
}
