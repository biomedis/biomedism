package ru.biomedis.biomedismair3.social.remote_client.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import ru.biomedis.biomedismair3.social.remote_client.Role;

public class Token {
  private String accessToken;
  private String refreshToken;
  private Date expired;
  private String userName;
  private long userId;
  private long id;
  @JsonProperty("roles")
  private List<String> rolesNames = new ArrayList<>();

  @JsonIgnore
  private Set<Role> roles = new HashSet<>();

  public Token() {
  }


  public boolean isExpired(){
     return Date.from(Instant.now()).after(expired);
  }

  public String getAccessToken() {
    return this.accessToken;
  }

  public String getRefreshToken() {
    return this.refreshToken;
  }

  public Date getExpired() {
    return this.expired;
  }

  public String getUserName() {
    return this.userName;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public void setExpired(Date expired) {
    this.expired = expired;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }


  public long getUserId() {
    return userId;
  }

  public void setUserId(long userId) {
    this.userId = userId;
  }

  @Override
  public String toString() {
    return "Token{" +
        "accessToken='" + accessToken + '\'' +
        ", refreshToken='" + refreshToken + '\'' +
        ", expired=" + expired +
        ", userName='" + userName + '\'' +
        ", userId=" + userId +
        ", id=" + id +
        ", roles=" + rolesNames +
        '}';
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public List<String> getRolesNames() {
    return rolesNames;
  }

  public void setRolesNames(List<String> rolesNames) {
    this.rolesNames = rolesNames;
    roles.clear();
    roles.addAll(Role.byListNames(rolesNames));
  }

  @JsonIgnore
  public Set<Role> getRoles() {
    if(!rolesNames.isEmpty() && roles.isEmpty()){
      roles.addAll(Role.byListNames(rolesNames));
    }
    return roles;
  }
}

