package ru.biomedis.biomedismair3.social.remote_client.dto.error;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class ApiError extends RuntimeException {


  private String status;

  private int statusCode = 0;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
  @JsonDeserialize(using = CustomLocalDateTimeDeserializer.class)
  private  LocalDateTime timestamp;

  private  String message;

  private String debugMessage;

  private  List<ApiSubError> subErrors;

  private boolean validationError;

  private TokenErrorType tokenErrorType;

  private boolean tokenError;

  private boolean needValidateEmail;

  public ApiError() {
  }

  public String getStatus() {
    return this.status;
  }

  public int getStatusCode() {
    return this.statusCode;
  }

  public LocalDateTime getTimestamp() {
    return this.timestamp;
  }

  public String getMessage() {
    return this.message;
  }

  public String getDebugMessage() {
    return this.debugMessage;
  }

  public List<ApiSubError> getSubErrors() {
    return this.subErrors;
  }

  public boolean isValidationError() {
    return this.validationError;
  }

  public TokenErrorType getTokenErrorType() {
    return this.tokenErrorType;
  }

  @JsonProperty("tokenError")
  public boolean isTokenError() {
    return this.tokenError;
  }

  public boolean isNeedValidateEmail() {
    return this.needValidateEmail;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public void setDebugMessage(String debugMessage) {
    this.debugMessage = debugMessage;
  }

  public void setSubErrors(List<ApiSubError> subErrors) {
    this.subErrors = subErrors;
  }

  public void setValidationError(boolean validationError) {
    this.validationError = validationError;
  }

  public void setTokenErrorType(TokenErrorType tokenErrorType) {
    this.tokenErrorType = tokenErrorType;
  }

  public void setTokenError(boolean tokenError) {
    this.tokenError = tokenError;
  }

  public void setNeedValidateEmail(boolean needValidateEmail) {
    this.needValidateEmail = needValidateEmail;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiError apiError = (ApiError) o;
    return statusCode == apiError.statusCode &&
        validationError == apiError.validationError &&
        tokenError == apiError.tokenError &&
        needValidateEmail == apiError.needValidateEmail &&
        Objects.equals(status, apiError.status) &&
        Objects.equals(timestamp, apiError.timestamp) &&
        Objects.equals(message, apiError.message) &&
        Objects.equals(debugMessage, apiError.debugMessage) &&
        Objects.equals(subErrors, apiError.subErrors) &&
        tokenErrorType == apiError.tokenErrorType;
  }

  @Override
  public int hashCode() {
    return Objects
        .hash(status, statusCode, timestamp, message, debugMessage, subErrors, validationError,
            tokenErrorType, tokenError, needValidateEmail);
  }

  @Override
  public String toString() {
    return "ApiError{" +
        "status='" + status + '\'' +
        ", statusCode=" + statusCode +
        ", timestamp=" + timestamp +
        ", message='" + message + '\'' +
        ", debugMessage='" + debugMessage + '\'' +
        ", subErrors=" + subErrors +
        ", validationError=" + validationError +
        ", tokenErrorType=" + tokenErrorType +
        ", tokenError=" + tokenError +
        ", needValidateEmail=" + needValidateEmail +
        '}';
  }
}
