package ru.biomedis.biomedismair3.social.remote_client.dto.error;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
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

}
