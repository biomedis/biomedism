package ru.biomedis.biomedismair3.social.remote_client;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import java.util.List;
import ru.biomedis.biomedismair3.social.contacts.UserContact;
import ru.biomedis.biomedismair3.social.remote_client.dto.ContactDto;
import ru.biomedis.biomedismair3.social.remote_client.dto.ContactView;

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

  @RequestLine("DELETE /contacts")
  void deleteContacts(List<Long> ids);
}
