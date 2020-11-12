package ru.biomedis.biomedismair3.social.contacts.lenta

import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.util.data.MutableDataSet
import javafx.scene.web.WebEngine
import javafx.scene.web.WebView

import com.vladsch.flexmark.util.ast.Node;

import com.vladsch.flexmark.parser.Parser;
import kotlin.random.Random


class MessagesService(val messagesArea: WebView, val messageEditorArea: WebView) {
    private lateinit var parser: Parser
    private lateinit var htmlrenderer: HtmlRenderer
    private  var messagesEngine: WebEngine
    private  var messageEditorEngine: WebEngine
    val htmlMessagesPath: String =  "/html/chat.html"
    val htmlEditorPath: String = "/html/msg_editor.html"

     init {
        fun initEditor(htmlPath: String, webView: WebView): WebEngine {
            val engine = webView.engine
            engine.isJavaScriptEnabled = true
            val html = javaClass.getResourceAsStream(htmlPath).bufferedReader().use { it.readText() }
            engine.loadContent(html)
            return engine
        }
        messagesEngine = initEditor(htmlMessagesPath, messagesArea)
        messageEditorEngine = initEditor(htmlEditorPath, messageEditorArea)

         initMarkdownParser()

    }

    private fun initMarkdownParser(){
        val options = MutableDataSet()
        this.parser = Parser.builder(options).build()
        this.htmlrenderer = HtmlRenderer.builder(options).build()
    }


    fun clear(){

    }
    //TODO сделать стратегию кеширования, чтобы уже загруженные данные  сохранялись в памяти, чтобы при переключении можно было быстро вернуться

    /**
     * Установит данные контакта и загрузит сообщения
     */
    fun showContact(contactId: Long){

    }

    /**
     * Отправит введенное сообщение
     */
    fun sendMessage(){
        var msg = getMessageFromEditor()
        var html = markdownToHtml(msg)
        addMessage(html, 111)

    }


    private fun getMessageFromEditor(): String {
        return messageEditorEngine.executeScript("getMessageFromEditor()") as String
    }

    private fun setEditorContent(mdContend: String){

        messageEditorEngine.executeScript("setContent('${clearContent(mdContend)}')")
    }

    private fun markdownToHtml(html: String): String{
        val document: Node = parser.parse(html)
       return htmlrenderer.render(document)
    }

    private fun addMessage(msg: String, msgId: Long){
        messagesEngine.executeScript("addMessage('${clearContent(msg)}', '$msgId')")
    }

    private fun clearContent(c: String): String{
       // var content = c.replace("'", "\\'")
        var content  = c.replace(System.getProperty("line.separator"), "\\n")
        content = content.replace("\n", "\\n")
        content = content.replace("\r", "\\n")
        return content
    }

}
