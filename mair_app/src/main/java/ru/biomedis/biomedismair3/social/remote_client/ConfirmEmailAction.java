package ru.biomedis.biomedismair3.social.remote_client;

public interface ConfirmEmailAction {
  void confirm(String email) throws BreakByUserException;
}
