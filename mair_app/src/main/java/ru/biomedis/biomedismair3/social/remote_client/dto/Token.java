package ru.biomedis.biomedismair3.social.remote_client.dto;

import java.time.Instant;
import java.util.Date;

public class Token {
  private String accessToken;
  private String refreshToken;
  private Date expired;
  private String userName;
  private long userId;

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
        '}';
  }
}
