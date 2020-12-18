package ru.biomedis.biomedismair3.social.contacts.messages

import ru.biomedis.biomedismair3.App
import ru.biomedis.biomedismair3.social.remote_client.ContactsClient
import ru.biomedis.biomedismair3.social.remote_client.dto.CountMessage
import ru.biomedis.biomedismair3.utils.Other.LoggerDelegate
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.function.BiConsumer
import java.util.function.Consumer


class MessagesService(val messageAPI: ContactsClient, val requestPeriod: Long): App.CloseAppListener {

    private val log by LoggerDelegate()
    private val editedHandlers: MutableList<Consumer<Map<Long, Int>>> = mutableListOf()
    private val newHandlers: MutableList<Consumer<Map<Long, CountMessage>>> = mutableListOf()
    private val totalNewHandlers: MutableList<BiConsumer<Int, Map<Long, CountMessage>>> = mutableListOf()
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

    fun addNewMessagesHandler(handler: Consumer<Map<Long, CountMessage>>): Consumer<Map<Long, CountMessage>> {
        newHandlers.add(handler)
        return handler
    }

    fun addTotalCountMessagesHandler(handler: BiConsumer<Int, Map<Long, CountMessage>>): BiConsumer<Int, Map<Long, CountMessage>> {
        totalNewHandlers.add(handler)
        return handler
    }

    fun addDeletedMessagesHandler(handler: Consumer<Map<Long, List<Long>>>): Consumer<Map<Long, List<Long>>> {
        deletedHandler.add(handler)
        return handler
    }

    private fun action() {
       try{

        if(totalNewHandlers.isEmpty() && newHandlers.isEmpty()) return

        val state = messageAPI.messagesState()

        var totalCount = 0
        if(state.newMessagesCount.isNotEmpty()){

            state.newMessagesCount.values.forEach {
                totalCount+=it.count
            }
        }

        totalNewHandlers.forEach {  it.accept(totalCount, state.newMessagesCount) }


        newHandlers.forEach {  it.accept(state.newMessagesCount) }

        editedHandlers.forEach {  it.accept(state.editedMessagesCount) }

           deletedHandler.forEach {
               it.accept(state.deletedMessages)
           }

       }catch (e: Exception){
           log.error("Ошибка в обработчике MessageService", e)
       }
    }

    fun removeEditedMessagesHandler(handler: Consumer<Map<Long, Int>>) {
        editedHandlers.remove(handler)
    }

    fun removeNewMessagesHandler(handler: Consumer<Map<Long, CountMessage>>) {
        newHandlers.remove(handler)
    }

    fun removeTotalCountMessagesHandler(handler: BiConsumer<Int, Map<Long, CountMessage>>) {
        totalNewHandlers.remove(handler)
    }

    fun removeDeletedMessagesHandler(handler: Consumer<Map<Long, List<Long>>>) {
        deletedHandler.remove(handler)
    }

    override fun onClose() {
        stop()
    }
}
