package ru.biomedis.biomedismair3.social.contacts.lenta

import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.fxml.FXML
import javafx.scene.control.ListView
import javafx.stage.Modality
import javafx.stage.WindowEvent
import ru.biomedis.biomedismair3.BaseController
import ru.biomedis.biomedismair3.social.remote_client.SocialClient
import ru.biomedis.biomedismair3.utils.Other.LoggerDelegate
import ru.biomedis.biomedismair3.utils.TabHolder
import java.net.URL
import java.util.*
private const val REQUEST_COUNT_BY_PAGE: Int = 3

class LentaFollowController: BaseController(), TabHolder.Selected, TabHolder.Detached {

    private val log by LoggerDelegate()

    private var firstSelect: Boolean = true
    @FXML
    private lateinit var elementsList: ListView<ShortStory>

    private lateinit var storiesLoader: StoriesLoader
    private val hasDataToLoad: SimpleBooleanProperty = SimpleBooleanProperty(true)

    override fun onCompletedInitialization() {
        storiesLoader = StoriesLoader.forOtherUsed(
                REQUEST_COUNT_BY_PAGE,
                controllerWindow ){
            last: Long, count_: Int ->
            SocialClient.INSTANCE.accountClient.getStoriesFollow(last, count_)
        }
        elementsList.items = storiesLoader.observableList

    }

    override fun onClose(event: WindowEvent) {

    }

    override fun setParams(vararg params: Any) {

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

    override fun onSelected() {
      if(firstSelect) {
          firstSelect= false
          Platform.runLater {
              nextLoadStories()
              elementsList.scrollTo(elementsList.items.lastIndex)

          }
      }
    }

    override fun onDetach() {

    }

}
