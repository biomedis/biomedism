package ru.biomedis.biomedismair3.social.remote_client;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import java.util.Date;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import ru.biomedis.biomedismair3.social.account.AccountView;
import ru.biomedis.biomedismair3.social.account.ActiveSession;

import ru.biomedis.biomedismair3.social.admin.AccountWithRoles;
import ru.biomedis.biomedismair3.social.contacts.AccountSmallView;
import ru.biomedis.biomedismair3.social.contacts.FindData;
import ru.biomedis.biomedismair3.social.contacts.lenta.PageShortStoryDto;
import ru.biomedis.biomedismair3.social.contacts.lenta.ShortStory;
import ru.biomedis.biomedismair3.social.contacts.lenta.Story;
import ru.biomedis.biomedismair3.social.remote_client.dto.CityDto;
import ru.biomedis.biomedismair3.social.remote_client.dto.CountryDto;


public interface AccountClient {

  @RequestLine("GET /about/{id}")
  String getAbout(@Param("id") long id);

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

  @Headers(value = {"Content-Type: application/json"})
  @RequestLine("POST /find")
  List<AccountSmallView> findUsers(FindData findData);


  @RequestLine("GET /find_by_login/{login}")
  AccountSmallView findUserByLogin(@Param("login")String login);

  @RequestLine("GET /find_by_email/{email}")
  AccountSmallView findUserByEmail(@Param("email")String email);

  @Headers(value = {"Content-Type: application/json"})
  @RequestLine("POST /accounts_info")
  List<AccountSmallView> accountsInfoByIds(List<Long> ids);

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

  @RequestLine("PUT /country/{country}")
  void setCountry(@Param("country") Long country);


  @RequestLine("PUT /city/{city}")
  void setCity(@Param("city") Long city);


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

  @RequestLine("PUT /show_email/{param}")
  void setShowEmail(@Param("param") boolean value);

  @RequestLine("PUT /show_real_name/{param}")
  void setRealName(@Param("param") boolean value);

  @RequestLine("PUT /show_skype/{param}")
  void setShowSkype(@Param("param") boolean value);

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

  @Headers(value = {"Content-Type: application/json"})
  @RequestLine("POST /story")
  Long createStory(Story story);

  @Headers(value = {"Content-Type: application/json"})
  @RequestLine("PUT /story")
  void updateStory(Story story);

  @RequestLine("GET /story/{id}")
  String getStoryContent(@Param("id")Long story);

  @RequestLine("GET /story/user/{user}/all")
  List<ShortStory> getStories(@Param("user")Long user);

  @RequestLine("GET /story/user/{user}/all_by_date?from={from}&to={to}")
  List<ShortStory> getStories(@Param("user")Long user, @Param("from") Date from, @Param("to") Date to);

  @RequestLine("GET /story/all_by_date?from={from}&to={to}")
  List<ShortStory> getStories(@Param("from") Date from, @Param("to") Date to);

  /**
   * Извлекает [count] данных меньше  указанного элемента с [lastStory].
   * @param user
   * @param lastStory
   * @param count
   * @return
   */
  @RequestLine("GET /story/user/{user}/all?last={lastStory}&count={count}")
  PageShortStoryDto getStories(@Param("user")Long user, @Param("lastStory") long lastStory, @Param("count") int count);

  /**
   * Извлекает [count] данных меньше  указанного элемента с [lastStory].
   * @param lastStory
   * @param count
   * @return
   */
  @RequestLine("GET /story/all?last={lastStory}&count={count}")
  PageShortStoryDto getStories(@Param("lastStory") long lastStory, @Param("count") int count);


  @RequestLine("DELETE /story/{id}")
  void deleteStory(@Param("id") long id);

  @RequestLine("GET /story/next/{id}")
  ShortStory getNextStory(@Param("id") long id);

  @RequestLine("GET /story/all_follow?last={lastStory}&count={count}")
  PageShortStoryDto getStoriesFollow(@Param("lastStory") long lastStory, @Param("count") int count);

  //определяется -сколько есть новых, от той что последний раз видели в списке снизу,
  // для этого на бэкенде сохраняется эта последняя история при получении списка историй подписок
  @RequestLine("GET /story/all_follow/not_viewed_count")
  int getNotViewedStoriesCount();
}
