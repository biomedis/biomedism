package ru.biomedis.biomedismair3.social.remote_client;

import feign.RequestLine;

public interface AccountClient {
  @RequestLine("PUT /logout_all")
  void clearAllToken();

  @RequestLine("PUT /logout")
  void clearToken(String refreshToken);
}
