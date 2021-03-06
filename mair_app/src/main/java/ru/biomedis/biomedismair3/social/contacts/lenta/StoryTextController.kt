package ru.biomedis.biomedismair3.social.contacts.lenta


import javafx.application.Platform
import javafx.concurrent.Worker
import javafx.fxml.FXML
import javafx.scene.web.WebEngine
import javafx.scene.web.WebView
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.stage.WindowEvent
import netscape.javascript.JSObject
import ru.biomedis.biomedismair3.BaseController
import ru.biomedis.biomedismair3.BlockingAction
import ru.biomedis.biomedismair3.social.link_service.LinkService
import ru.biomedis.biomedismair3.social.remote_client.SocialClient
import ru.biomedis.biomedismair3.utils.Other.LoggerDelegate
import java.net.URL
import java.util.*


class StoryTextController : BaseController() {



    private val log by LoggerDelegate()


    @FXML
    private lateinit var editor: WebView


    private lateinit var engine: WebEngine

    private  var storyId: Long = -1

    private val javaConnector: JavaConnector

    init{
        javaConnector = JavaConnector{ onLinkClick(it)}
    }

    override fun onCompletedInitialization() {


    }

    override fun onClose(event: WindowEvent) {

    }

    override fun setParams(vararg params: Any) {
        if (params.isEmpty()) throw RuntimeException("Не верные параметры. Должен быть параметр ID")
        storyId = params[0] as Long
    }



    override fun initialize(location: URL, resources: ResourceBundle) {
        initEditor()
        engine.loadWorker.stateProperty().addListener { observable, oldValue, newValue ->
            if (newValue == Worker.State.SUCCEEDED) {
                if(storyId!=-1L) setEditorContent()
            }
        }

    }



    private fun initEditor() {

        engine = editor.engine
        engine.setOnError { log.error(it.message, it.exception) }
        engine.isJavaScriptEnabled = true
        val window: JSObject = engine.executeScript("window") as JSObject
        val html = javaClass.getResourceAsStream("/html/markdown_viewer.html").bufferedReader().use { it.readText() }
        engine.loadContent(html)

        window.setMember("javaConnector", javaConnector)
        engine.documentProperty().addListener { _, oldValue, newValue ->
            if (Objects.nonNull(newValue)) {
                val w = engine.executeScript("window") as JSObject
                w.setMember("javaConnector", javaConnector)
            }
        }

    }

    private fun onLinkClick(href: String) {
        Platform.runLater{
            LinkService.useLink(href, this, SocialClient.INSTANCE.filesClient)
        }
    }


    /**
     * Установить контент
     * @param content контент
     * @param actionListener листнер событий
     */
    fun setEditorContent() {
       val result =  BlockingAction.actionResult(controllerWindow){
            SocialClient.INSTANCE.accountClient.getStoryContent(storyId)
        }
        if(result.isError){
            log.error("", result.error)
            showErrorDialog(
                    "Загрузка публикации",
                    "",
                    "Загрузка не удалась",
                    controllerWindow,
                    Modality.WINDOW_MODAL
            )
            return
        }

        var content = result.value
        content = content.replace("'", "\\'")
        content = content.replace(System.getProperty("line.separator"), "\\n")
        content = content.replace("\n", "\\n")
        content = content.replace("\r", "\\n")
        engine.executeScript("initEditor('$content')")
    }

    companion object {
        private val log by LoggerDelegate()

        /**
         * Поиск пользователей.
         * Вернет список добавленных в контакты пользователей
         */
        @JvmStatic
        fun showStoryDialog(context: Stage, item: ShortStory) {

            return try {
                openDialogUserData(
                        context,
                        "/fxml/social/StoryText.fxml",
                        item.title,
                        true,
                        StageStyle.UNIFIED,
                        400, 850, 0, 0,
                        Unit,
                        item.id
                )
            } catch (e: Exception) {
                log.error("Ошибка открытия диалога публикации", e)
                throw RuntimeException(e)
            }
        }
    }

    class JavaConnector(
        val linkClick:(String)->Unit
    ) {

        fun onLinkClick(href: String){
            linkClick(href)
        }
    }
}
