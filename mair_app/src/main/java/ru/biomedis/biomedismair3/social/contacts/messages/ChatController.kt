package ru.biomedis.biomedismair3.social.contacts.messages

import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.web.WebView
import javafx.stage.Modality
import javafx.stage.WindowEvent
import org.codefx.libfx.control.webview.WebViewHyperlinkListenerHandle
import ru.biomedis.biomedismair3.BaseController
import ru.biomedis.biomedismair3.social.link_service.LinkService
import ru.biomedis.biomedismair3.social.remote_client.SocialClient
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

    @FXML
    private lateinit var newBtn: Button

    @FXML
    private lateinit var editBtn: Button

    private var hyperlinkListener: WebViewHyperlinkListenerHandle? = null

    override fun onCompletedInitialization() {
        chatService = ChatService(messagesArea, messageEditorArea, contactId, controllerWindow,
            showErrorhandler = { title, msg ->
                showErrorDialog(
                    title,
                    "",
                    msg,
                    controllerWindow,
                    Modality.WINDOW_MODAL
                )
            },
            linkClick = this::onLinkClick
        )

        editBtn.visibleProperty().bind(chatService.editingProperty())
        newBtn.disableProperty().bind(
            chatService.initMessagesLoadedProperty().not()
        )//не дает добавлять сообщения, пока не загрузятся сообщения при открытии чата

//        hyperlinkListener = WebViews.addHyperlinkListener(
//            messagesArea, this::onLinkClick,
//            HyperlinkEvent.EventType.ACTIVATED
//        )


    }

    override fun onClose(event: WindowEvent) {

    }

    override fun setParams(vararg params: Any) {
        if (params.isEmpty()) throw RuntimeException("Не верные параметры. Должен быть параметр ID contact")
        contactId = params[0] as Long
    }

    override fun initialize(location: URL, resources: ResourceBundle) {



    }

    //выполняется при нажатии на ссылку в сообщении
    private fun onLinkClick(href: String) {
        Platform.runLater{
            LinkService.useLink(href, this, SocialClient.INSTANCE.filesClient)
        }
    }

    fun sendMessage() {
        chatService.sendMessage()
    }

    fun editMessage() {
        chatService.editMessage()
    }

    override fun onSelected() {

    }

    override fun onDetach() {
        chatService.removeHandlers()
        chatService.clear()
        hyperlinkListener?.detach()
    }

}
