package ru.biomedis.biomedismair3.social.contacts.messages

import com.fasterxml.jackson.databind.ObjectMapper
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.data.MutableDataSet
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.concurrent.Worker
import javafx.scene.web.WebEngine
import javafx.scene.web.WebView
import javafx.stage.Stage
import netscape.javascript.JSObject
import ru.biomedis.biomedismair3.App
import ru.biomedis.biomedismair3.AsyncAction
import ru.biomedis.biomedismair3.BlockingAction
import ru.biomedis.biomedismair3.social.remote_client.SocialClient
import ru.biomedis.biomedismair3.social.remote_client.dto.CountMessage
import ru.biomedis.biomedismair3.social.remote_client.dto.MessageDto
import ru.biomedis.biomedismair3.social.remote_client.dto.MessageInDto
import ru.biomedis.biomedismair3.utils.Other.LoggerDelegate
import java.io.File
import java.util.*
import java.util.function.Consumer

private const val MESSAGES_BATCH_SIZE=15
/**
 * Сервис работы с чатом.
 * Когда окно закрывается нужно вызвать removeHandlers()
 */
class ChatService(
    val messagesArea: WebView,
    val messageEditorArea: WebView,
    val contactUser: Long,
    val context: Stage,
    val showErrorhandler: (title: String, msg: String) -> Unit,
    val linkClick:(String)->Unit
) {
    private lateinit var parser: Parser
    private lateinit var htmlrenderer: HtmlRenderer
    private lateinit var messagesEngine: WebEngine
    private lateinit var messageEditorEngine: WebEngine
    private val htmlMessagesPath: String = "/html/chat.html"
    private val htmlEditorPath: String = "/html/msg_editor.html"
    private val javaConnector: JavaConnector

    private val addEditedMessagesHandler: Consumer<MutableMap<Long, Int>>
    private val addNewMessagesHandler: Consumer<MutableMap<Long, CountMessage>>
    private val addDeletedMessagesHandler: Consumer<MutableMap<Long, MutableList<Long>>>

    private val messagesLoader: MessagesLoader
    private val log by LoggerDelegate()

    private val editingProperty: SimpleBooleanProperty = SimpleBooleanProperty()

    fun editingProperty(): SimpleBooleanProperty = editingProperty

    private val initMessagesLoaded: SimpleBooleanProperty = SimpleBooleanProperty(false)
    fun initMessagesLoadedProperty(): SimpleBooleanProperty = initMessagesLoaded


    init {
        messagesLoader = MessagesLoader(contactUser)
        javaConnector = JavaConnector(
            editAction = this::editMsgAction,
            deleteAction = this::deleteMsgAction,
            loadMessages = {loadMessages(-1,MESSAGES_BATCH_SIZE)},
            linkClick = this::onLinkClick,
            loadMessagesFrom = {fromId->loadMessages(fromId,MESSAGES_BATCH_SIZE)}
        )
        fun initEditor(
            htmlPath: String,
            webView: WebView,
            webDataDirectory: File
        ): Pair<WebEngine, JSObject> {
            val engine = webView.engine
            engine.userDataDirectory =
                webDataDirectory//если не установить отдельные значения, то будет ошибка , баг JRE
            engine.setOnError { log.error(it.message, it.exception) }
            engine.isJavaScriptEnabled = true
            val window: JSObject = engine.executeScript("window") as JSObject
            val html =
                javaClass.getResourceAsStream(htmlPath).bufferedReader().use { it.readText() }
            engine.loadContent(html)
            return Pair(engine, window)
        }

        fun initMessaging() {
            val dir = File(App.getInnerDataDir_(), "messaging_web_dir")
            if (!dir.exists()) dir.mkdirs()

            val initEditor = initEditor(htmlMessagesPath, messagesArea, dir)
            messagesEngine = initEditor.first
            initEditor.second.setMember("javaConnector", javaConnector)
            messagesEngine.documentProperty().addListener { _, oldValue, newValue ->
                if (Objects.nonNull(newValue)) {
                    val window = messagesEngine.executeScript("window") as JSObject
                    window.setMember("javaConnector", javaConnector)
                }
            }

        }

        fun initEditing() {
            val dir = File(App.getInnerDataDir_(), "editing_message_web_dir")
            if (!dir.exists()) dir.mkdirs()
            messageEditorEngine = initEditor(htmlEditorPath, messageEditorArea, dir).first
        }


        //

        initMessaging()
        initEditing()
        initMarkdownParser()

        addEditedMessagesHandler =
            SocialClient.INSTANCE.addEditedMessagesHandler(this::onEditedMessageHandler)
        addNewMessagesHandler =
            SocialClient.INSTANCE.addNewMessagesHandler(this::onNewMessageHandler)
        addDeletedMessagesHandler =
            SocialClient.INSTANCE.addDeletedMessagesHandler(this::onDeletedMessageHandler)


        messagesEngine.setOnError { event ->
            log.error(event.message, event.exception)
        }


        messagesEngine.getLoadWorker().stateProperty().addListener { ov, oldState, newState ->
            if (newState == Worker.State.SCHEDULED) {
                println("state: scheduled")
            } else if (newState == Worker.State.RUNNING) {
                println("state: running")
            } else if (newState == Worker.State.SUCCEEDED) {
                println("state: succeeded")

            }
        }

    }

    private fun onLinkClick(href: String){
        linkClick(href)
    }

    private fun loadMessages(fromId: Long, count: Int) {
        messagesLoader.loadMessages(contactUser, fromId, count).thenAccept {
            if (it.isError) {
                log.error("Ошибка получения обновленных сообщений", it.error)
                showErrorhandler("загрузка сообщений чата", "Не удалось получить сообщения")
            } else {

                Platform.runLater {
                    addMessages(it.value, contactUser, fromId != -1L)
                    initMessagesLoaded.value = true
                }

            }

        }
    }


    fun removeHandlers() {
        SocialClient.INSTANCE.removeDeletedMessagesHandler(addDeletedMessagesHandler)
        SocialClient.INSTANCE.removeNewMessagesHandler(addNewMessagesHandler)
        SocialClient.INSTANCE.removeEditedMessagesHandler(addEditedMessagesHandler)
    }


    private fun onEditedMessageHandler(info: Map<Long, Int>) {
        if (info.containsKey(contactUser)) {
            AsyncAction.actionResult {
                SocialClient.INSTANCE.contactsClient.messagesUpdated(contactUser)
            }.thenAccept {
                if (it.isError) {
                    log.error("Ошибка получения обновленных сообщений", it.error)
                } else {
                    it.value.forEach { msg ->
                        messagesEngine.executeScript("editMessage(${msg.id},${markdownToHtml(msg.message)})")
                    }

                }
            }

        }
    }

    private fun onNewMessageHandler(info: Map<Long, CountMessage>) {
        if (info.containsKey(contactUser)) {
            AsyncAction.actionResult {
                SocialClient.INSTANCE.contactsClient.allNewMessages(contactUser)
            }.thenAccept {
                if (it.isError) {
                    log.error("Ошибка получения новых сообщений", it.error)
                } else {
                    it.value.forEach { msg ->
                        Platform.runLater {
                            if (msg.from == contactUser) addMessageIncomming(markdownToHtml(msg.message), msg.id)
                            else addMessageOutcome(markdownToHtml(msg.message), msg.id)
                        }
                    }

                }
            }

        }
    }

    private fun onDeletedMessageHandler(info: Map<Long, List<Long>>) {
        if (info.containsKey(contactUser)) {
            val arg = info[contactUser]!!.joinToString(",", "[", "]")
            Platform.runLater {
                messagesEngine.executeScript("removeMessage($arg)")
                AsyncAction.actionNoResult {
                    SocialClient.INSTANCE.contactsClient.deleteMarkedMessages(info[contactUser]!!)
                }.thenAccept {
                    if (it.isError) {
                        log.error("", it.error)
                    }
                }

            }
        }

    }

    private var currentEditedMsg: MessageDto? = null

    /**
     * Сигнализирует о желании редактировать это сообщение
     */
    private fun editMsgAction(id: Long) {
        //взять сообщение, отправить в редактор, и по сохранению изменить его на странице
        val msg: MessageDto? = messagesLoader.messageById(id)
        if (msg != null) {
            currentEditedMsg = msg
            setEditorContent(msg.message)
            editingProperty.value = true
        } else {
            log.error("Не удалось найти в списке сообщений нужное сообщение")
            showErrorhandler("Редактирование сообщения", "Не удалось начать редактирование")
        }
    }

    /**
     * Сигнализирует о желании  это сообщение
     */
    private fun deleteMsgAction(id: Long) {
        val result = BlockingAction.actionNoResult(context) {
            SocialClient.INSTANCE.contactsClient.deleteMessages(listOf(id))
        }
        if (result.isError) {
            log.error("", result.error)
            showErrorhandler("Удаление сообщения", "Не удалось удалить сообщение")
        } else {
            messagesLoader.remove(id)
            messagesEngine.executeScript("removeMessage(${id})")
            if (currentEditedMsg != null && currentEditedMsg!!.id == id) {
                currentEditedMsg = null
                setEditorContent("")
                editingProperty.value = false
            }
        }
    }

    private fun initMarkdownParser() {
        val options = MutableDataSet()
        this.parser = Parser.builder(options).build()
        this.htmlrenderer = HtmlRenderer.builder(options).build()
    }


    /**
     * Отправит введенное сообщение
     */
    fun editMessage() {
        var msg = getMessageFromEditor()
        var html = markdownToHtml(msg)
        if (currentEditedMsg == null) {
            showErrorhandler("Редактирование сообщения", "Не удалось редактирование сообщения.")
        } else {
            val result = BlockingAction.actionNoResult(context) {
                SocialClient.INSTANCE.contactsClient.editMessage(currentEditedMsg!!.id, msg)
            }
            if (result.isError) {
                log.error("", result.error)
                showErrorhandler("Редактирование сообщения", "Не удалось сохранить сообщение")
            } else {
                currentEditedMsg!!.message = msg
                editMessage(html, currentEditedMsg!!.id)//свое сообщение правится здесь,
                // а оппонента автоматически в обработчике измененных сообщений onEditedMessageHandler

                //после редактирования режим редактирования закрывается
                currentEditedMsg = null
                editingProperty.value = false
                setEditorContent("")
            }
        }

    }

    fun sendMessage() {
        var msg = getMessageFromEditor()
        var html = markdownToHtml(msg)

        if (msg.trim().isBlank()) {
            return
        }


        val result = BlockingAction.actionResult(context) {
            SocialClient.INSTANCE.contactsClient.sendMessage(MessageInDto(contactUser, msg))

        }
        if (result.isError) {
            log.error("", result.error)
            showErrorhandler("Отправка сообщения", "Не удалось отправить сообщение")
        } else {
            addMessageOutcome(html, result.value.id)
            messagesLoader.addMessage(result.value)
            //при отправк сообщения как нового, редактирование закрывается
            currentEditedMsg = null
            editingProperty.value = false
            setEditorContent("")
        }


    }


    private fun getMessageFromEditor(): String {
        return messageEditorEngine.executeScript("getMessageFromEditor()") as String
    }

    private fun setEditorContent(mdContend: String) {

        messageEditorEngine.executeScript("setContent('${clearContent(mdContend)}')")
    }

    private fun markdownToHtml(html: String): String {
        val document: Node = parser.parse(html)
        return htmlrenderer.render(document)
    }

    private fun editMessage(msg: String, id: Long) {
        messagesEngine.executeScript("editMessage($id,'${clearContent(msg)}')")
    }

    private data class HtmlMessageDto(val msg: String, val msgId: Long, val isOut: Boolean)

    private fun addMessages(messages: List<MessageDto>, contactUser: Long, prepend: Boolean){
        val result = mutableListOf<HtmlMessageDto>()
        messages.forEach {
            result.add(HtmlMessageDto(
                clearContent(markdownToHtml(it.message)),
                it.id,
                it.from != contactUser))
        }
        val mapper = ObjectMapper()
        val json = mapper.writeValueAsString(result)
        val func = if(prepend) "addPrevMessages" else "addMessages"
        val script="""
            var msgs = JSON.parse('$json');
            $func(msgs);
        """.trimIndent()
        messagesEngine.executeScript(script)

    }


    private fun addMessageIncomming(msg: String, msgId: Long, scroll: Boolean = true) {
        messagesEngine.executeScript("addMessageIncoming('${clearContent(msg)}', '$msgId','$scroll')")
    }

    private fun addMessageOutcome(msg: String, msgId: Long, scroll: Boolean = true) {
        messagesEngine.executeScript("addMessageOutcome('${clearContent(msg)}', '$msgId','$scroll')")
    }

    private fun scrollChatToBottom() {
        messagesEngine.executeScript("scrollBottom()")
    }

    /**
     * Очистит строки от некорректных символов, экранирует кавычки иначе строку не передать в JS(кавычки сломают строку итп)
     */
    private fun clearContent(c: String): String {
        var content = c.replace("'", "\\'")
        content = content.replace("\"", "\\\"")
        content = content.replace(System.getProperty("line.separator"), "\\n")
        content = content.replace("\n", "\\n")
        content = content.replace("\r", "\\n")
        return content
    }

    class JavaConnector(
        val editAction: (Long) -> Unit,
        val deleteAction: (Long) -> Unit,
        val loadMessages: () -> Unit,
        val linkClick:(String)->Unit,
        val loadMessagesFrom: (Long) -> Unit
    ) {
        fun deleteMsg(id: Long) {
            deleteAction(id)
        }

        fun editMsg(id: Long) {
            editAction(id)

        }

        fun loadInitMessages() {
            loadMessages()

        }

        fun loadMessages(fromId: Long) {
            loadMessagesFrom(fromId)

        }

        fun onLinkClick(href: String){
            linkClick(href)
        }
    }

    fun clear(){
        messagesLoader.clear()
    }
}
