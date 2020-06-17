package ru.biomedis.biomedismair3.social.remote_client;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import ru.biomedis.biomedismair3.social.remote_client.dto.RegistrationDto;


public interface RegistrationClient {

  /**
   * Осуществляет регистрацию
   * @param data данные клиента
   */
  @RequestLine("POST /")
  @Headers("Content-Type: application/json")
  void registration(RegistrationDto data);

  /**
   * Отправляет код подтверждения на почту
   * @param email
   */
  @RequestLine("GET /send_code")
  void sendCode(@Param("email") String email);

  /**
   * Подтвердить почту по ранее высланому коду
   * @param email
   * @param code
   */
  @RequestLine("GET /confirm_email")
  void confirmEmail(@Param("email") String email, @Param("code") String code);

  /**
   * Сбросить пароль и отправить проверочный код на почту
   * @param email
   */
  @RequestLine("GET /send_reset_code")
  void sendResetCode(@Param("email") String email);


  /**
   * Задать новый пароль по коду  сброса
   * @param email
   * @param code
   * @param password
   */
  @RequestLine("PUT /new_password")
  void setNewPassword(@Param("email") String email, @Param("code") String code, @Param("password") String password);
}
