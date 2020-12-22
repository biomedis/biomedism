package ru.biomedis.biomedismair3.social.contacts.lenta


import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.KeyEvent
import javafx.scene.web.WebEngine
import javafx.scene.web.WebView
import javafx.stage.*
import ru.biomedis.biomedismair3.BaseController
import ru.biomedis.biomedismair3.BlockingAction
import ru.biomedis.biomedismair3.social.remote_client.SocialClient
import ru.biomedis.biomedismair3.utils.Other.LoggerDelegate
import ru.biomedis.biomedismair3.utils.Other.Result
import ru.biomedis.biomedismair3.utils.imageFromBase64
import ru.biomedis.biomedismair3.utils.imageViewToBase64
import java.io.File
import java.net.URL
import java.util.*


private const val MAX_DESCR_LENGTH: Int = 400
private const val MAX_TITLE_LENGTH: Int = 120
private const val REQUEST_COUNT_BY_PAGE: Int = 3


class LentaController : BaseController() {


    private var editorInited: Boolean = false

    private val log by LoggerDelegate()

    @FXML
    private lateinit var elementsList: ListView<ShortStory>

    @FXML
    private lateinit var title: TextField

    @FXML
    private lateinit var image: ImageView

    @FXML
    private lateinit var shortText: TextArea

    @FXML
    private lateinit var editor: WebView

    @FXML
    private lateinit var sendBtn: Button

    @FXML
    private lateinit var editBtn: Button

    @FXML
    private lateinit var listPane: TitledPane

    @FXML
    private lateinit var editPane: TitledPane

    @FXML
    private lateinit var accordion: Accordion

    private lateinit var storiesLoader: StoriesLoader

    private val hasDataToLoad: SimpleBooleanProperty = SimpleBooleanProperty(true)

    private lateinit var engine: WebEngine

    private var editedStory: ShortStory?=null

    override fun onCompletedInitialization() {
        storiesLoader = StoriesLoader.selfUsed(REQUEST_COUNT_BY_PAGE, controllerWindow)
        elementsList.items = storiesLoader.observableList

        Platform.runLater {
            nextLoadStories()
            elementsList.scrollTo(elementsList.items.lastIndex)
            if (elementsList.items.size == 0) {
                accordion.expandedPane = editPane

            } else accordion.expandedPane = listPane
            Platform.runLater {
                initEditor()
            }
        }

    }

    override fun onClose(event: WindowEvent) {

    }

    override fun setParams(vararg params: Any) {

    }

    private var lastElementForLoading: ShortStory?=null

    private val editedProperty: SimpleBooleanProperty = SimpleBooleanProperty(false)

    override fun initialize(location: URL, resources: ResourceBundle) {
        elementsList.cellFactory = StoryCellFactory.forOwner(
                this::deleteAction,
                this::editAction,
                this::newLoad,
                this::showStoryText
        )

        sendBtn.disableProperty().bind(
                title.textProperty().isEmpty
                        .or(shortText.textProperty().isEmpty)
                        .or(image.imageProperty().isNull))

        initTextFieldsEventConstraints()

        accordion.expandedPaneProperty().addListener { _, _, newValue ->
            if(newValue==editPane && !editorInited) Platform.runLater {  initEditor() }
        }

        editBtn.visibleProperty().bind(editedProperty)

    }

    private fun showStoryText(item: ShortStory) {
        StoryTextController.showStoryDialog(controllerWindow, item)
    }

    private fun newLoad(){
        nextLoadStories()
    }
    private fun deleteAction(item: ShortStory){
        val result = showConfirmationDialog("Удаление публикации", item.title, "Публикация будет удалена.", controllerWindow, Modality.WINDOW_MODAL)
        if (result.isPresent && result.get() == okButtonType){
            try {
                storiesLoader.remove(item)
            }catch (e: StoriesLoader.DeleteStoryException){
                showErrorDialog("Удаление публикации", "", "Удаление не удалось", controllerWindow, Modality.WINDOW_MODAL)
            }
        }
    }

    private fun editAction(item: ShortStory){

        val result = BlockingAction.actionResult(controllerWindow){
            SocialClient.INSTANCE.accountClient.getStoryContent(item.id)
        }

        if(result.isError){
            log.error("Не удалось загрузить контент публикации", result.error)
            showErrorDialog("Редактирование публикации","","Не удалось загрузить контент публикации",controllerWindow, Modality.WINDOW_MODAL)
            return
        }
        editedStory = item
        accordion.expandedPane = editPane
        title.text = item.title
        image.image = imageFromBase64(item.image)
        shortText.text = item.description
        setEditorContent(result.value)
        editedProperty.value = true
    }


    private fun initEditor() {
        engine = editor.engine
        engine.isJavaScriptEnabled = true
        val html = javaClass.getResourceAsStream("/html/editor.html").bufferedReader().use { it.readText() }
        engine.loadContent(html)
        editorInited = true
    }


    private fun nextLoadStories() {
        try {
            storiesLoader.remove(ShortStory.NEXT_LOAD_ID)
            val firstItem: ShortStory? = if(elementsList.items.isEmpty()) null else elementsList.items[0]

            hasDataToLoad.set(storiesLoader.nextLoad())

            //кнопка подгрузки
            if(hasDataToLoad.get()){
                storiesLoader.add(ShortStory().apply { id = ShortStory.NEXT_LOAD_ID })
            }

            if(firstItem!=null){
                val index = elementsList.items.lastIndexOf(firstItem)-1
                if(index>0) {
                    elementsList.apply {
                        scrollTo(index)//скролл к первому добавленному
                        focusModel.focus(index)
                    }
                }
            }

        } catch (e: Exception) {
            Platform.runLater {
                log.error("", e)
                showErrorDialog(
                        "Загрузка публикаций",
                        "",
                        "Загрузка не удалась не удалась",
                        controllerWindow,
                        Modality.WINDOW_MODAL
                )
            }
        }
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

    /**
     * Установить контент
     * @param content контент
     * @param actionListener листнер событий
     */
    fun setEditorContent(content: String) {
        var content = content
        content = content.replace("'", "\\'")
        content = content.replace(System.getProperty("line.separator"), "\\n")
        content = content.replace("\n", "\\n")
        content = content.replace("\r", "\\n")
        engine.executeScript("setContent('$content')")
    }

    /**
     * Получить контент
     * @return
     */
    fun getEditorContent(): String {
        return engine.executeScript("getContent()") as String
    }

     fun clearForm() {
        title.text = ""
        shortText.text = ""
        image.image = null
        setEditorContent("")
         editedStory = null
         editedProperty.value = false
    }

//    fun send() {
//        if(editedStory!=null) updateStorySave()
//        else newStorySave()
//    }

   @FXML  private fun newStorySave(){
        val story = createStory()
        val actionResult: Result<Long> = BlockingAction.actionResult(controllerWindow) {
            SocialClient.INSTANCE.accountClient.createStory(story)
        }
        if (actionResult.isError) {
            log.error("", actionResult.error)
            showErrorDialog(
                    "Новая публикация",
                    "",
                    "Публикация не удалась",
                    controllerWindow,
                    Modality.WINDOW_MODAL
            )
            return
        }
        story.id = actionResult.value
        val shortStory = story.toShortStory()

        storiesLoader.add(shortStory)
        clearForm()
        accordion.expandedPane = listPane
        elementsList.scrollTo(elementsList.items.size - 1)
        elementsList.selectionModel.select(shortStory)

    }

    @FXML  private fun updateStorySave(){
        val story = createStory()
        story.id = editedStory!!.id

        val result = BlockingAction.actionNoResult(controllerWindow) {
            SocialClient.INSTANCE.accountClient.updateStory(story)
        }

        if (result.isError) {
            log.error("", result.error)
            showErrorDialog(
                    "Обновление публикации",
                    "",
                    "Сохранение не удалось",
                    controllerWindow,
                    Modality.WINDOW_MODAL
            )
            return
        }

        val item = editedStory!!
        item.apply {
            title = story.title
            description = story.description
            image = story.image
        }
        clearForm()
        accordion.expandedPane = listPane
        storiesLoader.updateStory(item)
        elementsList.scrollTo(item)
        elementsList.selectionModel.select(item)


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
        it.image = imageViewToBase64(image, ((image.userData?:"png") as String).equals("png", true))
        it.title = title.text.trim()
        it.description = shortText.text.trim()
        it.content = getMarkDownContent()
    }

    private fun getMarkDownContent(): String {
        return getEditorContent()
    }


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
                        true,
                        StageStyle.UNIFIED,
                        400, 850, 0, 0,
                        Unit
                )
            } catch (e: Exception) {
                log.error("Ошибка открытия диалога ленты новостей", e)
                throw RuntimeException(e)
            }
        }
    }


}
