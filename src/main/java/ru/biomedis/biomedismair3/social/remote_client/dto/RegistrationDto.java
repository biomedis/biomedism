package ru.biomedis.biomedismair3.social.remote_client.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RegistrationDto {

  private String userName = "";

  private String password = "";

  private String email = "";

  private String firstName = "";

  private String lastName = "";

  private String country = "";

  private String city = "";

  private String skype = "";

  private String about = "";

}
