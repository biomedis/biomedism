package ru.biomedis.biomedismair3.social.remote_client;

import feign.Param;
import feign.RequestLine;
import java.util.List;
import ru.biomedis.biomedismair3.social.remote_client.dto.ContactDto;
import ru.biomedis.biomedismair3.social.remote_client.dto.ContactView;
import ru.biomedis.biomedismair3.social.remote_client.dto.SmallContactViewDto;

public interface ContactsClient {

  @RequestLine("POST /contacts/{id}")
  ContactDto addContact(@Param("id") long id);

  @RequestLine("POST /contacts")
  List<ContactDto> addAllContact(List<Long> ids);

  @RequestLine("GET /contacts")
  List<SmallContactViewDto> allContacts();

  @RequestLine("GET /contacts/contact/{id}")
  ContactView getContactInfo(@Param("id") long id);

  @RequestLine("DELETE /contacts")
  void deleteContacts(List<Long> ids);
}
