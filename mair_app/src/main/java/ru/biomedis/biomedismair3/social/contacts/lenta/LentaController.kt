package ru.biomedis.biomedismair3.social.contacts.lenta


import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.KeyEvent
import javafx.scene.web.HTMLEditor
import javafx.stage.FileChooser
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.stage.WindowEvent
import org.jsoup.Jsoup
import ru.biomedis.biomedismair3.BaseController
import ru.biomedis.biomedismair3.BlockingAction
import ru.biomedis.biomedismair3.social.remote_client.SocialClient
import ru.biomedis.biomedismair3.utils.Other.LoggerDelegate
import ru.biomedis.biomedismair3.utils.Other.Result
import ru.biomedis.biomedismair3.utils.imageViewToBase64
import java.io.File
import java.net.URL
import java.util.*

private const val MAX_DESCR_LENGTH: Int = 400
private const val MAX_TITLE_LENGTH: Int = 120

class LentaController : BaseController() {
    private val log by LoggerDelegate()

    @FXML
    private lateinit var elementsList: ListView<*>

    @FXML
    private lateinit var title: TextField

    @FXML
    private lateinit var image: ImageView

    @FXML
    private lateinit var shortText: TextArea

    @FXML
    private lateinit var htmlEditor: HTMLEditor

    @FXML
    private lateinit var sendBtn: Button

    @FXML
    private lateinit var editBtn: Button

    @FXML
    private lateinit var deleteBtn: Button


    override fun onCompletedInitialization() {

    }

    override fun onClose(event: WindowEvent) {

    }

    override fun setParams(vararg params: Any) {

    }

    override fun initialize(location: URL, resources: ResourceBundle) {
        sendBtn.disableProperty().bind(
                title.textProperty().isEmpty
                        .or(shortText.textProperty().isEmpty)
                        .or(image.imageProperty().isNull))

        initTextFieldsEventConstraints()
    }

    private fun initTextFieldsEventConstraints() {
        fun constraint(input: TextInputControl, maxLength: Int) {
            input.addEventFilter(KeyEvent.KEY_TYPED) { event ->
                if (input.text.length > maxLength) event.consume()
            }
            input.textProperty().addListener { _, _, newValue ->
                if (newValue.length > maxLength) input.text = newValue.substring(0, maxLength)
            }
        }

        constraint(shortText, MAX_DESCR_LENGTH)
        constraint(title, MAX_TITLE_LENGTH)
    }

    fun send() {
        val actionResult: Result<Long> = BlockingAction.actionResult(controllerWindow) {
            SocialClient.INSTANCE.accountClient.createStory(createStory())
        }

    }

    fun edit() {

    }

    fun delete() {

    }


    fun selectimageBtn() {
        val fileChooser = FileChooser()
        fileChooser.title = "Выбор изображения"
        val extFilter = FileChooser.ExtensionFilter("Images:", "*.jpeg", "*.jpg", "*.png")
        fileChooser.extensionFilters.add(extFilter)
        val file: File = fileChooser.showOpenDialog(controllerWindow)
        image.image = Image(file.inputStream(), 100.0, 100.0, true, true)
        image.userData = file.extension.toLowerCase()

    }

    private fun createStory(): Story = Story().also {
        it.image = imageViewToBase64(image, (image.userData as String).equals("png", true))
        it.title = title.text.trim()
        it.description = shortText.text.trim()
        it.content = getHtmlContent()
    }

    private fun getHtmlContent(): String {
        val doc = Jsoup.parse(htmlEditor.htmlText)
        return doc.select("body").html()
    }


    /*
     log.error("Ошибка записи изображения", e)
                    showExceptionDialog("Ошибка записи изображения", "", "", e, controllerWindow, Modality.WINDOW_MODAL)

     */


    companion object {
        private val log by LoggerDelegate()

        /**
         * Поиск пользователей.
         * Вернет список добавленных в контакты пользователей
         */
        @JvmStatic
        fun showLentaDialog(context: Stage) {

            return try {
                openDialogUserData(
                        context,
                        "/fxml/social/Lenta.fxml",
                        "Лента событий",
                        false,
                        StageStyle.UTILITY,
                        0, 0, 0, 0,
                        Unit
                )
            } catch (e: Exception) {
                log.error("Ошибка открытия диалога ленты новостей", e)
                throw RuntimeException(e)
            }
        }
    }


}
