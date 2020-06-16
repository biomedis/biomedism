package ru.biomedis.biomedismair3.social.remote_client.dto.error;


import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApiError extends RuntimeException {

  private int statusCode = 0;

  private  LocalDateTime timestamp;

  private  String message;

  private String debugMessage;

  private  List<ApiSubError> subErrors;

  private boolean validationError;

  private TokenErrorType tokenErrorType;

  private boolean tokenError;

  private boolean needValidateEmail;

}
