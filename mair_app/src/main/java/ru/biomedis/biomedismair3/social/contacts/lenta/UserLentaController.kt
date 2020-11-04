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


class UserLentaController : BaseController() {

    private var storyId: Long = -1
    private val log by LoggerDelegate()

    @FXML
    private lateinit var elementsList: ListView<ShortStory>

    private lateinit var storiesLoader: StoriesLoader
    private val hasDataToLoad: SimpleBooleanProperty = SimpleBooleanProperty(true)

    override fun onCompletedInitialization() {
        storiesLoader = StoriesLoader.forUserUsed(REQUEST_COUNT_BY_PAGE, storyId, controllerWindow)
        elementsList.items = storiesLoader.observableList

        Platform.runLater {
            nextLoadStories()
            elementsList.scrollTo(elementsList.items.lastIndex)

        }

    }

    override fun onClose(event: WindowEvent) {

    }

    override fun setParams(vararg params: Any) {
        if (params.isEmpty()) throw RuntimeException("Не верные параметры. Должен быть параметр ID")
        storyId = params[0] as Long
    }

    override fun initialize(location: URL, resources: ResourceBundle) {
        elementsList.cellFactory = StoryCellFactory.forOthers({nextLoadStories()}) {
            StoryTextController.showStoryDialog(controllerWindow, it) }

    }



    private fun nextLoadStories() {
        try {
            storiesLoader.remove(ShortStory.NEXT_LOAD_ID)
            val firstItem: ShortStory? = if (elementsList.items.isEmpty()) null else elementsList.items[0]

            hasDataToLoad.set(storiesLoader.nextLoad())

            //кнопка подгрузки
            if (hasDataToLoad.get()) {
                storiesLoader.add(ShortStory().apply { id = ShortStory.NEXT_LOAD_ID })
            }

            if (firstItem != null) {
                val index = elementsList.items.lastIndexOf(firstItem) - 1
                if (index > 0) {
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

    companion object {
        private val log by LoggerDelegate()

        /**
         * Поиск пользователей.
         * Вернет список добавленных в контакты пользователей
         */
        @JvmStatic
        fun showLentaDialog(context: Stage, userId: Long) {

            return try {
                openDialogUserData(
                        context,
                        "/fxml/social/UserLenta.fxml",
                        "Лента событий",
                        true,
                        StageStyle.UNIFIED,
                        400, 850, 0, 0,
                        Unit, userId
                )
            } catch (e: Exception) {
                log.error("Ошибка открытия диалога ленты новостей", e)
                throw RuntimeException(e)
            }
        }
    }


}
