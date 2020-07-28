package ru.biomedis.biomedismair3.social.remote_client.dto.error;

import java.util.Objects;

public class ApiValidationError extends ApiSubError{
  private String object;
  private String field;
  private Object rejectedValue;
  private String message;

  public ApiValidationError() {
  }

  public String getObject() {
    return this.object;
  }

  public String getField() {
    return this.field;
  }

  public Object getRejectedValue() {
    return this.rejectedValue;
  }

  public String getMessage() {
    return this.message;
  }

  public void setObject(String object) {
    this.object = object;
  }

  public void setField(String field) {
    this.field = field;
  }

  public void setRejectedValue(Object rejectedValue) {
    this.rejectedValue = rejectedValue;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  @Override
  public String toString() {
    return "ApiValidationError{" +
        "object='" + object + '\'' +
        ", field='" + field + '\'' +
        ", rejectedValue=" + rejectedValue +
        ", message='" + message + '\'' +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiValidationError that = (ApiValidationError) o;
    return Objects.equals(object, that.object) &&
        Objects.equals(field, that.field) &&
        Objects.equals(rejectedValue, that.rejectedValue) &&
        Objects.equals(message, that.message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(object, field, rejectedValue, message);
  }
}
