package ru.biomedis.biomedismair3.social.remote_client.dto;

import java.time.Instant;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Token {
  private String accessToken;
  private String refreshToken;
  private Date expired;

  public boolean isExpired(){
     return Date.from(Instant.now()).after(expired);
  }
}

