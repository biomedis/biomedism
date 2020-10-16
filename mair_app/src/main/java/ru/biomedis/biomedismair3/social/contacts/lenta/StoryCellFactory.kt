package ru.biomedis.biomedismair3.social.contacts.lenta

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.util.Callback
import ru.biomedis.biomedismair3.utils.imageFromBase64
import java.io.IOException
import java.text.SimpleDateFormat


class StoryCellFactory private constructor(
        val owner: Boolean,
        val deleteAction: (item: ShortStory) -> Unit = { _ -> Unit },
        val editAction: (item: ShortStory) -> Unit = { _ -> Unit }
) : Callback<ListView<ShortStory>, ListCell<ShortStory>> {

    companion object {
        fun forOwner(deleteAction: (item: ShortStory) -> Unit, editAction: (item: ShortStory) -> Unit): StoryCellFactory {
            return StoryCellFactory(true, editAction, deleteAction)
        }

        fun forOthers(): StoryCellFactory {
            return StoryCellFactory(false)
        }
    }

    override fun call(param: ListView<ShortStory>?): ListCell<ShortStory> {
        return TaskCell(owner, editAction, deleteAction)
    }

    class TaskCell(val owner: Boolean, deleteAction: (item: ShortStory) -> Unit, editAction: (item: ShortStory) -> Unit) : ListCell<ShortStory>() {
        @FXML
        private lateinit var date: Label

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

        private val dateFormat = SimpleDateFormat("dd.mm.yyyy hh:mm")

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
                title.text = item.title
                date.text = dateFormat.format(item.created)
                image.image = imageFromBase64(item.image)

                description.text = item.description

                contentDisplay = ContentDisplay.GRAPHIC_ONLY
            }
        }




        init {
            loadFXML()
            editBtn.setOnAction { editAction(item) }
            deleteBtn.setOnAction { deleteAction(item) }
        }


    }
}
