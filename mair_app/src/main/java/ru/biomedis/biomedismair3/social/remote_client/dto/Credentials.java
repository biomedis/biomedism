package ru.biomedis.biomedismair3.social.remote_client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Credentials {
  private String email;
  private String password;
}
