package ru.biomedis.biomedismair3.social.remote_client.dto.error;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApiValidationError extends ApiSubError{
  private String object;
  private String field;
  private Object rejectedValue;
  private String message;
}
