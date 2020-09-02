package ru.biomedis.biomedismair3.social.remote_client;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import java.util.List;
import ru.biomedis.biomedismair3.social.account.AccountView;
import ru.biomedis.biomedismair3.social.account.ActiveSession;

import ru.biomedis.biomedismair3.social.admin.AccountWithRoles;
import ru.biomedis.biomedismair3.social.contacts.AccountSmallView;
import ru.biomedis.biomedismair3.social.contacts.FindData;
import ru.biomedis.biomedismair3.social.remote_client.dto.CityDto;
import ru.biomedis.biomedismair3.social.remote_client.dto.CountryDto;


public interface AccountClient {

  @RequestLine("GET /roles")
  List<String> allRoles();

  @RequestLine("GET /all_users")
  List<AccountWithRoles> allUsers();

  @RequestLine("GET /roles/{id}")
  List<String> userRoles(@Param("id")long id);

  @RequestLine("GET /change_name?name={name}")
  void changeUserName(@Param("name") String name);

  @RequestLine("PUT /logout_all")
  void clearAllToken();

  @RequestLine("PUT /logout")
  void clearToken(String refreshToken);

  @RequestLine("DELETE /delete_token/{id}")
  void deleteToken(@Param("id")long id);

  @RequestLine("GET /{id}")
  AccountView getAccount(@Param("id") long id);


  @RequestLine("POST /find")
  List<AccountSmallView> findUsers(FindData findData);

  @RequestLine("POST /find_by_login")
  AccountSmallView findUserByLogin(String login);

  @RequestLine("POST /find_by_email")
  AccountSmallView findUserByEmail(String email);

  /**
   * Список стран
   */
  @RequestLine("GET /countries")
  List<CountryDto> countriesList();


  @RequestLine("GET /country/{country}/cities")
  List<CityDto> citiesList(@Param("country") long country);

  @Headers(value = {"Content-Type: application/json"})
  @RequestLine("PUT /firstName")
  void setFirstName(String name);

  @RequestLine("PUT /lastName")
  void setLastName(String name);

  @RequestLine("PUT /skype")
  void setSkype(String skype);

  @RequestLine("PUT /country")
  void setCountry(String country);


  @RequestLine("PUT /city")
  void setCity(String city);


  @RequestLine("PUT /about")
  void setAbout(String about);

  @RequestLine("PUT /doctor/{param}")
  void setDoctor(@Param("param") boolean doctor);

  @RequestLine("PUT /partner/{param}")
  void setPartner(@Param("param") boolean partner);

  @RequestLine("PUT /bris/{param}")
  void setBris(@Param("param") boolean bris);

  @RequestLine("PUT /depot/{param}")
  void setDepot(@Param("param") boolean depot);



  @RequestLine("PUT /doctor/{userId}/{param}")
  void setDoctor(@Param("param") boolean doctor, @Param("userId") long userId);

  @RequestLine("PUT /partner/{userId}/{param}")
  void setPartner(@Param("param") boolean partner, @Param("userId") long userId);

  @RequestLine("PUT /bris/{userId}/{param}")
  void setBris(@Param("param") boolean bris, @Param("userId") long userId);

  @RequestLine("PUT /support/{userId}/{param}")
  void setSupport(@Param("param") boolean support, @Param("userId") long userId);


  @RequestLine("PUT /company/{userId}/{param}")
  void setCompany(@Param("param") boolean company, @Param("userId") long userId);

  @Headers(value = {"Content-Type: application/json"})
  @RequestLine("PUT /user/{userId}/roles")
  void setRoles(List<String> roles, @Param("userId") long userId);

  @RequestLine("PUT /depot/{userId}/{param}")
  void setDepot(@Param("param") boolean depot, @Param("userId") long userId);


  @RequestLine("GET /send_code_change_email?email={email}")
  void sendCodeToChangeEmail(@Param("email") String email);

  @RequestLine("GET /change_email?code={code}&old_code={old_code}")
  void changeEmail(@Param("code") String code, @Param("old_code") String codeOld );

  @RequestLine("GET /all_tokens")
  List<ActiveSession> allTokens();
}