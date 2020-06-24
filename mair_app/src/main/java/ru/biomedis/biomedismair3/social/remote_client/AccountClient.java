package ru.biomedis.biomedismair3.social.remote_client;

import feign.RequestLine;

public interface AccountClient {
  @RequestLine("GET /logout_all")
  void clearAllToken();

  @RequestLine("GET /logout")
  void clearToken();
}
