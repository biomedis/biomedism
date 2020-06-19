package ru.biomedis.biomedismair3.social.remote_client;

import java.util.Optional;
import ru.biomedis.biomedismair3.social.remote_client.dto.Token;

public interface TokenRepository {
  Optional<Token> getToken();
  void saveToken(Token token);
  void clearToken();
}
