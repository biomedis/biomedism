package ru.biomedis.biomedismair3.social.remote_client.dto

import java.util.*

data class MessageInDto(val to: Long, val message: String)

class MessagesState {
    lateinit var newMessagesCount: Map<Long, CountMessage>
    lateinit var editedMessagesCount: Map<Long, Int>
    lateinit var deletedMessages: Map<Long, List<Long>>
}

class MessageDto {
    var id: Long = -1
    var from: Long = -1
    var to: Long = -1
    lateinit var message: String
    lateinit var date: Date
}

 class CountMessage{
     var count: Int = -1
     lateinit var time: Date
 }
