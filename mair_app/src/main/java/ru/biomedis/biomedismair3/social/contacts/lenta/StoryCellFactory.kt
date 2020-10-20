package ru.biomedis.biomedismair3.social.contacts.lenta

import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.util.Callback
import ru.biomedis.biomedismair3.utils.imageFromBase64
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class StoryCellFactory private constructor(
        val owner: Boolean,
        val deleteAction: (item: ShortStory) -> Unit = { _ -> Unit },
        val editAction: (item: ShortStory) -> Unit = { _ -> Unit },
        val needLoadEvent: ()-> Unit
) : Callback<ListView<ShortStory>, ListCell<ShortStory>> {

    companion object {
        fun forOwner(
                deleteAction: (item: ShortStory) -> Unit,
                editAction: (item: ShortStory) -> Unit,
                needLoadEvent: ()-> Unit
        ): StoryCellFactory {
            return StoryCellFactory(true, editAction, deleteAction, needLoadEvent)
        }

        fun forOthers( needLoadEvent: ()-> Unit): StoryCellFactory {
            return StoryCellFactory(false,  needLoadEvent = needLoadEvent)
        }
    }

    override fun call(param: ListView<ShortStory>?): ListCell<ShortStory> {
        return TaskCell(owner, editAction, deleteAction, needLoadEvent)
    }

    class TaskCell(
            val owner: Boolean,
            val deleteAction: (item: ShortStory) -> Unit,
            val editAction: (item: ShortStory) -> Unit,
            val needLoadEvent: ()-> Unit
    ) : ListCell<ShortStory>() {
        @FXML
        private lateinit var date: Label

        @FXML
        private lateinit var loadBtn: Button

        @FXML
        private lateinit var loadBox: HBox

        @FXML
        private lateinit var title: Label

        @FXML
        private lateinit var editBtn: Button

        @FXML
        private lateinit var image: ImageView

        @FXML
        private lateinit var deleteBtn: Button

        @FXML
        private lateinit var description: Label

        @FXML
        private lateinit var fullContent: Hyperlink

        private val dateFormat = SimpleDateFormat("dd.MM.yyyy hh:mm")

        private fun loadFXML() {
            try {
                val loader = FXMLLoader(javaClass.getResource("/fxml/Social/StoryListCell.fxml"))
                loader.setController(this)
                loader.setRoot(this)
                loader.load<Any>()
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }

        override fun updateItem(item: ShortStory?, empty: Boolean) {
            super.updateItem(item, empty)
            if (!owner) {
                editBtn.isVisible = false
                deleteBtn.isVisible = false
            }

            if (empty || item == null) {
                text = null
                contentDisplay = ContentDisplay.TEXT_ONLY
            } else {
                if(item.id==ShortStory.NEXT_LOAD_ID){
                    loadBox.isVisible = true
                }else {
                    loadBox.isVisible = false
                    title.text = item.title
                    date.text = dateFormat.format(item.created)
                    image.image = imageFromBase64(item.image)
                    description.text = item.description
                    contentDisplay = ContentDisplay.GRAPHIC_ONLY
                }


            }
        }



        init {
            dateFormat.timeZone = Calendar.getInstance().timeZone
            loadFXML()
            editBtn.setOnAction { editAction(item) }
            deleteBtn.setOnAction { deleteAction(item) }
            loadBtn.setOnAction { Platform.runLater(needLoadEvent) }

        }


    }
}
