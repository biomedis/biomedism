package ru.biomedis.biomedismair3.social.contacts.messages

import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.data.MutableDataSet
import javafx.scene.web.WebEngine
import javafx.scene.web.WebView
import netscape.javascript.JSObject
import java.util.*
import kotlin.random.Random


class ChatService(val messagesArea: WebView, val messageEditorArea: WebView) {
    private lateinit var parser: Parser
    private lateinit var htmlrenderer: HtmlRenderer
    private lateinit var messagesEngine: WebEngine
    private lateinit var messageEditorEngine: WebEngine
    val htmlMessagesPath: String = "/html/chat.html"
    val htmlEditorPath: String = "/html/msg_editor.html"
    val javaConnector: JavaConnector

    init {
        javaConnector = JavaConnector(editAction = this::editMsgAction, deleteAction = this::deleteMsgAction)
        fun initEditor(htmlPath: String, webView: WebView): Pair<WebEngine, JSObject> {
            val engine = webView.engine
            engine.isJavaScriptEnabled = true
            val window: JSObject = engine.executeScript("window") as JSObject
            val html = javaClass.getResourceAsStream(htmlPath).bufferedReader().use { it.readText() }
            engine.loadContent(html)
            return Pair(engine, window)
        }

        fun initMessaging() {
            val initEditor = initEditor(htmlMessagesPath, messagesArea)
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
            messageEditorEngine = initEditor(htmlEditorPath, messageEditorArea).first
        }

        //

        initMessaging()
        initEditing()
        initMarkdownParser()

    }

    private fun editMsgAction(id: Long) {

    }

    private fun deleteMsgAction(id: Long) {

    }

    private fun initMarkdownParser() {
        val options = MutableDataSet()
        this.parser = Parser.builder(options).build()
        this.htmlrenderer = HtmlRenderer.builder(options).build()
    }


    fun clear() {

    }

    /**
     * Установит данные контакта и загрузит сообщения
     */
    fun showContact(contactId: Long) {

    }

    /**
     * Отправит введенное сообщение
     */
    fun sendMessage() {
        var msg = getMessageFromEditor()
        var html = markdownToHtml(msg)
        addMessage(html, Random.nextLong(100))

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

    private fun addMessage(msg: String, msgId: Long) {
        messagesEngine.executeScript("addMessage('${clearContent(msg)}', '$msgId')")
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

    class JavaConnector(val editAction: (Long) -> Unit, val deleteAction: (Long) -> Unit) {
        fun deleteMsg(id: Long) {
            deleteAction(id)
        }

        fun editMsg(id: Long) {
            editAction(id)

        }
    }
}
