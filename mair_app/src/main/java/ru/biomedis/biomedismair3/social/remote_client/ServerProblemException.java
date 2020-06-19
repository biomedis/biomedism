package ru.biomedis.biomedismair3.social.remote_client;

public class ServerProblemException extends Exception{

  public ServerProblemException(String message) {
    super(message);
  }

  public ServerProblemException(String message, Throwable cause) {
    super(message, cause);
  }

  public ServerProblemException(Throwable cause) {
    super(cause);
  }
}
