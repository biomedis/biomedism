package ru.biomedis.biomedismair3.social.remote_client;

import static feign.Logger.Level.HEADERS;

import feign.Logger;
import feign.Request;
import feign.Response;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomFeignRequestLogging  extends Logger {
  @Override
  protected void logRequest(String configKey, Level logLevel, Request request) {

    if (logLevel.ordinal() >= HEADERS.ordinal()) {
      super.logRequest(configKey, logLevel, request);
    } else {
      int bodyLength = 0;
      if (request.body() != null) {
        bodyLength = request.body().length;
      }
      log(configKey, "---> %s %s HTTP/1.1 (%s-byte body) ", request.httpMethod().name(), request.url(), bodyLength);
    }
  }

  @Override
  protected Response logAndRebufferResponse(String configKey, Level logLevel, Response response, long elapsedTime)
      throws IOException {
    if (logLevel.ordinal() >= HEADERS.ordinal()) {
      super.logAndRebufferResponse(configKey, logLevel, response, elapsedTime);
    } else {
      int status = response.status();
      Request request = response.request();
      log(configKey, "<--- %s %s HTTP/1.1 %s (%sms) ", request.httpMethod().name(), request.url(), status, elapsedTime);
    }
    return response;
  }


  @Override
  protected void log(String configKey, String format, Object... args) {
    log.debug(format(configKey, format, args));
  }

  protected String format(String configKey, String format, Object... args) {
    return String.format(methodTag(configKey) + format, args);
  }
}
