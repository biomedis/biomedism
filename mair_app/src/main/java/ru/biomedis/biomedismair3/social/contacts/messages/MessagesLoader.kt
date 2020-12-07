package ru.biomedis.biomedismair3.social.contacts.messages

import ru.biomedis.biomedismair3.AsyncAction
import ru.biomedis.biomedismair3.social.remote_client.SocialClient
import ru.biomedis.biomedismair3.social.remote_client.dto.MessageDto
import ru.biomedis.biomedismair3.utils.Other.Result
import java.util.concurrent.CompletableFuture

class MessagesLoader(val contactUser: Long) {

    private val messages: MutableMap<Long, MessageDto> = mutableMapOf()

    fun messageById(id: Long): MessageDto? {
        return messages[id]
    }

    fun loadMessages(contactUser: Long): CompletableFuture<Result<MutableList<MessageDto>>> =  AsyncAction.actionResult {
            SocialClient.INSTANCE.contactsClient.getNextMessagesFromUser(contactUser, -1, 20)
        }.thenApply {
            if(!it.isError) {
                synchronized(messages){
                    it.value.forEach {
                            msg->
                        messages[msg.id]=msg
                    }
                }
            }
        it
    }

    fun remove(id: Long) {
        messages.remove(id)
    }

    fun addMessage(msg: MessageDto){
        messages[msg.id] = msg
    }
}
