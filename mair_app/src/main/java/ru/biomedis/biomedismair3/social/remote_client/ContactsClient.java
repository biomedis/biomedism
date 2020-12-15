package ru.biomedis.biomedismair3.social.remote_client;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import ru.biomedis.biomedismair3.social.contacts.UserContact;
import ru.biomedis.biomedismair3.social.remote_client.dto.ContactDto;
import ru.biomedis.biomedismair3.social.remote_client.dto.ContactView;
import ru.biomedis.biomedismair3.social.remote_client.dto.MessageDto;
import ru.biomedis.biomedismair3.social.remote_client.dto.MessageInDto;
import ru.biomedis.biomedismair3.social.remote_client.dto.MessagesState;

public interface ContactsClient {

  @RequestLine("POST /contacts/{id}")
  ContactDto addContact(@Param("id") long id);

  @Headers(value = {"Content-Type: application/json"})
  @RequestLine("POST /contacts")
  List<ContactDto> addAllContact(List<Long> ids);

  @RequestLine("GET /contacts")
  List<UserContact> allContacts();

  @RequestLine("GET /contacts/contact/{id}")
  ContactView getContactInfo(@Param("id") long id);

  @RequestLine("DELETE /contacts/{id}")
  void deleteContact(@Param("id") long id);

  @RequestLine("PUT /contacts/follow/contact/{id}")
  void follow(@Param("id") long contact);

  @RequestLine("PUT /contacts/un_follow/contact/{id}")
  void unFollow(@Param("id") long contact);

  @RequestLine("GET /contacts/followers_count")
  int followersCount();


  /**
   * Получает сооющения от пользователя и свои с индексом менее указанного.
   * @param fromUser контакт, id пользователя
   * @param lastMsg последнее id сообщения или -1 если с начала( с самого последнего, при начальной загрузке чата)
   * @param count ожидаемое количество
   * @return
   */
  @RequestLine("GET /new_messages/from/{from}/last_msg/{last_msg}?count={count}")
  List<MessageDto> getNextMessagesFromUser(@Param("from") long fromUser, @Param("last_msg") long lastMsg, @Param("count") int count);

  /**
   * Список новых сообщений чата
   * @param fromUser от пользователя
   * @return
   */
  @RequestLine("GET /new_messages/all/from/{from}")
  List<MessageDto> allNewMessages(@Param("from") long fromUser);

  /**
   * Все изменения в сообщениях
   * Ключи - это ID пользователя с которым мы общаемся, те нашим контактом(но не сам контакт)
   * @return
   */
  @RequestLine("GET /messages/state")
  MessagesState messagesState();

  @RequestLine("GET /messages/updated/{from_user}")
  List<MessageDto> messagesUpdated(@Param("from_user") long fromUser);


  @RequestLine("GET /new_messages/from/{from}/days/{days}")
  List<MessageDto> getMessagesFromPeriod(@Param("from") long fromUser, @Param("days") int days);

  @Headers(value = {"Content-Type: application/json"})
  @RequestLine("POST /new_message/")
  MessageDto sendMessage(MessageInDto msg);

  @Headers(value = {"Content-Type: application/json"})
  @RequestLine("DELETE /messages")
  void deleteMessages( List<Long> messages);

  @Headers(value = {"Content-Type: application/json"})
  @RequestLine("DELETE /marked_messages")
  void deleteMarkedMessages( List<Long> messages);

  @Headers(value = {"Content-Type: application/json"})
  @RequestLine("PUT /messages/message/{id}")
  void editMessage(@Param("id") long id, String message );
}
