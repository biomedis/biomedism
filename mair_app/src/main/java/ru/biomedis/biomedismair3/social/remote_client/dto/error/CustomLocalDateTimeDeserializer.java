package ru.biomedis.biomedismair3.social.remote_client.dto.error;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CustomLocalDateTimeDeserializer extends StdDeserializer<LocalDateTime> {
  private static DateTimeFormatter formatter
      = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

  public CustomLocalDateTimeDeserializer() {
    this(null);
  }

  public CustomLocalDateTimeDeserializer(Class<?> vc) {
    super(vc);
  }


//18-06-2020 03:42:16

  @Override
  public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
    String date = p.getText();
    try {
      return LocalDateTime.parse(date , formatter);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
