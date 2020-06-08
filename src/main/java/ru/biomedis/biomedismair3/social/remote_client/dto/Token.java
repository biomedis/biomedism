package ru.biomedis.biomedismair3.social.remote_client.dto;

import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Token {
  private String accessToken;
  private String refreshToken;
  private Date expired;
}

