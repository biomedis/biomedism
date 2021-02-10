package ru.biomedis.biomedismair3.social.registry

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.util.Callback
import ru.biomedis.biomedismair3.social.remote_client.dto.AccessVisibilityType
import ru.biomedis.biomedismair3.social.remote_client.dto.DirectoryData
import ru.biomedis.biomedismair3.social.remote_client.dto.FileData
import ru.biomedis.biomedismair3.social.remote_client.dto.IFileItem
import java.io.IOException
import java.text.SimpleDateFormat

/**
 * Принимает FileData и DirectoryData
 */
class FileCellFactory(val linkAction:(String, AccessVisibilityType)->Unit ) : Callback<ListView<IFileItem>, ListCell<IFileItem>> {

    override fun call(param: ListView<IFileItem>?): ListCell<IFileItem> {
        return TaskCell(linkAction)
    }

    class TaskCell(val linkAction:(String, AccessVisibilityType)->Unit) : ListCell<IFileItem>() {
        private  val dateFormat = SimpleDateFormat("dd.MM.yyyy hh:mm")
        @FXML
        private lateinit var name: Label
        @FXML
        private lateinit var date: Label
        @FXML
        private lateinit var img: ImageView

        @FXML
        private lateinit var greenLinkBtn: Button

        @FXML
        private lateinit var redLinkBtn: Button

        @FXML
        private lateinit var orangeLinkBtn: Button


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
            img.fitWidth = img.image.width
            img.fitHeight = img.image.height
            date.isVisible= false
        }

        private fun viewFile(file: FileData) {
            isDirectory = false
            val icon = if(file.thumbnailImage!=null)  file.thumbnailImage!!
            else FileImages.selectImage(file)

            img.image = icon
            img.fitWidth = img.image.width
            img.fitHeight = img.image.height

            name.text = "${file.name}.${file.extension}"
            date.text = dateFormat.format(file.createdDate)
            date.isVisible = true

            greenLinkBtn.isVisible = false
            orangeLinkBtn.isVisible = false
            redLinkBtn.isVisible = false

            if(file.accessType==AccessVisibilityType.PUBLIC){
                greenLinkBtn.isVisible = true

            }else  if(file.accessType==AccessVisibilityType.PROTECTED){
                orangeLinkBtn.isVisible = true
            }else if(file.accessType==AccessVisibilityType.BY_LINK){
                redLinkBtn.isVisible = true
            }

        }

        init {
            loadFXML()
            redLinkBtn.setOnAction {
                if(item is FileData) linkAction((item as FileData).privateLink, item.accessType)
            }
            greenLinkBtn.setOnAction {
                if(item is FileData) linkAction((item as FileData).publicLink, item.accessType)
            }
            orangeLinkBtn.setOnAction {
                if(item is FileData) linkAction((item as FileData).publicLink, item.accessType)
            }
        }

    }
}
