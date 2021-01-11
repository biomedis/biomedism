package ru.biomedis.biomedismair3.social.registry

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.ContentDisplay
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.image.ImageView
import javafx.util.Callback
import ru.biomedis.biomedismair3.social.remote_client.dto.DirectoryData
import ru.biomedis.biomedismair3.social.remote_client.dto.FileData
import ru.biomedis.biomedismair3.social.remote_client.dto.IFileItem
import java.io.IOException

/**
 * Принимает FileData и DirectoryData
 */
class FileCellFactory : Callback<ListView<IFileItem>, ListCell<IFileItem>> {

    override fun call(param: ListView<IFileItem>?): ListCell<IFileItem> {
        return TaskCell()
    }

    class TaskCell : ListCell<IFileItem>() {

        @FXML
        private lateinit var name: Label
        @FXML
        private lateinit var img: ImageView

        private var isDirectory = false;

        private fun loadFXML() {
            try {
                val loader =
                    FXMLLoader(javaClass.getResource("/fxml/social/FileListCell.fxml"))
                loader.setController(this)
                loader.setRoot(this)
                loader.load<Any>()
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }

        override fun updateItem(contact: IFileItem?, empty: Boolean) {
            super.updateItem(contact, empty)

            if (empty || contact == null) {
                text = null
                contentDisplay = ContentDisplay.TEXT_ONLY
                return
            }

            when (item) {
                is DirectoryData -> viewDirectory(item as DirectoryData)
                is FileData -> viewFile(item as FileData)
                else -> throw RuntimeException("Требуется тип FileData или DirectoryData")
            }

            contentDisplay = ContentDisplay.GRAPHIC_ONLY

        }

        private fun viewDirectory(dir: DirectoryData) {
            isDirectory = true
            img.image = DirectoryImage.directoryImage
            name.text = dir.name
        }

        private fun viewFile(file: FileData) {
            isDirectory = false
            img.image = FileImages.selectImage(file)
            name.text = file.name
        }

        init {
            loadFXML()
        }

    }
}
