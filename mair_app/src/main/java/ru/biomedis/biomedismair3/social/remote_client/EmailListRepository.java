package ru.biomedis.biomedismair3.social.remote_client;

import java.util.SortedSet;

public interface EmailListRepository {
      void addEmail(String email);
      void clearList();
      SortedSet<String> getEmails();
}
