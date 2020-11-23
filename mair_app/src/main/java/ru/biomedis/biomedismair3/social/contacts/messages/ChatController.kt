package ru.biomedis.biomedismair3.social.contacts.messages

import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.web.WebView
import javafx.stage.WindowEvent
import ru.biomedis.biomedismair3.BaseController
import ru.biomedis.biomedismair3.utils.Other.LoggerDelegate
import ru.biomedis.biomedismair3.utils.TabHolder
import java.net.URL
import java.util.*

class ChatController : BaseController(), TabHolder.Selected, TabHolder.Detached {
    private var contactId: Long = -1
    private lateinit var chatService: ChatService

    private val log by LoggerDelegate()

    @FXML
    private lateinit var chatTitle: Label


    @FXML
    private lateinit var messagesArea: WebView


    @FXML
    private lateinit var messageEditorArea: WebView


    override fun onCompletedInitialization() {

    }

    override fun onClose(event: WindowEvent) {

    }

    override fun setParams(vararg params: Any) {
        if (params.isEmpty()) throw RuntimeException("Не верные параметры. Должен быть параметр ID contact")
        contactId = params[0] as Long
    }

    override fun initialize(location: URL, resources: ResourceBundle) {

        chatService = ChatService(messagesArea, messageEditorArea)

    }


    fun sendMessage() {
        chatService.sendMessage()
    }

    override fun onSelected() {

    }

    override fun onDetach() {

    }

}
