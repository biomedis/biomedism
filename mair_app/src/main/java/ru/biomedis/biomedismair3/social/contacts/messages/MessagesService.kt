package ru.biomedis.biomedismair3.social.contacts.messages

import ru.biomedis.biomedismair3.social.remote_client.ContactsClient
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.function.Consumer


class MessagesService(val messageAPI: ContactsClient, val requestPeriod: Long) {

    private val editedHandlers: MutableList<Consumer<Map<Long, Int>>> = mutableListOf()
    private val newHandlers: MutableList<Consumer<Map<Long, Int>>> = mutableListOf()
    private val totalNewHandlers: MutableList<Consumer<Int>> = mutableListOf()
    private val  deletedHandler: MutableList<Consumer<Map<Long, List<Long>>>> = mutableListOf()

    private var scheduledExecutorService: ScheduledExecutorService = Executors.newScheduledThreadPool(2)
    private var scheduleWithFixedDelay: ScheduledFuture<*>?=null
    fun start(): Boolean {
        if (scheduleWithFixedDelay != null) return false
         scheduleWithFixedDelay = scheduledExecutorService.scheduleWithFixedDelay(this::action, 1, requestPeriod, TimeUnit.SECONDS);
        return true
    }

    fun stop() {
        scheduleWithFixedDelay?.cancel(true)
        scheduleWithFixedDelay=null
    }

    fun addEditedMessagesHandler(handler: Consumer<Map<Long, Int>>):Consumer<Map<Long, Int>> {
         editedHandlers.add(handler)
        return handler
    }

    fun addNewMessagesHandler(handler: Consumer<Map<Long, Int>>): Consumer<Map<Long, Int>> {
        newHandlers.add(handler)
        return handler
    }

    fun addTotalCountMessagesHandler(handler: Consumer<Int>): Consumer<Int> {
        totalNewHandlers.add(handler)
        return handler
    }

    fun addDeletedMessagesHandler(handler: Consumer<Map<Long, List<Long>>>): Consumer<Map<Long, List<Long>>> {
        deletedHandler.add(handler)
        return handler
    }

    private fun action() {
        println("MessagesService")
        val state = messageAPI.messagesState()

        var totalCount = 0
        if(state.newMessagesCount.isNotEmpty()){

             totalCount = state.newMessagesCount.values.reduce { acc, entry ->
                acc + entry
            }
        }
        totalNewHandlers.forEach {  it.accept(totalCount) }


        newHandlers.forEach {  it.accept(state.newMessagesCount) }
        editedHandlers.forEach {  it.accept(state.editedMessagesCount) }
    }

    fun removeEditedMessagesHandler(handler: Consumer<Map<Long, Int>>) {
        editedHandlers.remove(handler)
    }

    fun removeNewMessagesHandler(handler: Consumer<Map<Long, Int>>) {
        newHandlers.remove(handler)
    }

    fun removeTotalCountMessagesHandler(handler: Consumer<Int>) {
        totalNewHandlers.remove(handler)
    }

    fun removeDeletedMessagesHandler(handler: Consumer<Map<Long, List<Long>>>) {
        deletedHandler.remove(handler)
    }
}
