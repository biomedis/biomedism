package ru.biomedis.biomedismair3.social.remote_client;

import feign.Param;
import feign.RequestLine;
import java.util.List;
import ru.biomedis.biomedismair3.social.account.AccountSmallView;
import ru.biomedis.biomedismair3.social.account.AccountView;
import ru.biomedis.biomedismair3.social.account.FindData;


public interface AccountClient {

  @RequestLine("PUT /logout_all")
  void clearAllToken();

  @RequestLine("PUT /logout")
  void clearToken(String refreshToken);

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
  List<String> countriesList();


  @RequestLine("GET /country/{country}/cities")
  List<String> citiesList(@Param("country") String country);


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

  @RequestLine("PUT /doctor")
  void setDoctor(boolean doctor);

  @RequestLine("PUT /partner")
  void setPartner(boolean partner);


  @RequestLine("PUT /support/{userId}")
  void setSupport(boolean support, @Param("userId") String userId);


  @RequestLine("PUT /company/{userId}")
  void setCompany(boolean company, @Param("userId") String userId);

  @RequestLine("PUT /depot")
  void setDepot(boolean depot);
}
