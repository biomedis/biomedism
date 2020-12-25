package ru.biomedis.biomedismair3.social.registry

import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.VBox
import ru.biomedis.biomedismair3.social.remote_client.dto.DirectoryData
import ru.biomedis.biomedismair3.social.remote_client.dto.FileData

class FileControl(val data: FileData) : VBox(3.0) {

    init {
        val imgView = ImageView(selectImage(data))
        val name = Label("${data.name}.${data.extension}")
        children.addAll(imgView, name)
    }

    @FXML
    fun initialize() {
        println("!!!!!!!!!!!!!!!!!!")
    }


    companion object {
        private val fileImage = ImageHolder("/images/file.png")
        private val archImage = ImageHolder("/images/zip.png")
        private val docImage = ImageHolder("/images/doc.png")
        private val exelImage = ImageHolder("/images/exel.png")
        private val pdfImage = ImageHolder("/images/pdf.png")

        private fun selectImage(data: FileData): Image = when (data.extension) {
            "zip", "rar", "gzip", "7z", "bzip", "tar", "gz" -> archImage.image
            "doc", "docx" -> docImage.image
            "xls", "xlsx" -> exelImage.image
            "pdf" -> pdfImage.image
            else -> fileImage.image
        }

    }
}


class DirectoryControl(val data: DirectoryData) : VBox(3.0) {

    init {
        val imgView = ImageView(directoryImage.image)
        val name = Label(data.name)
        children.addAll(imgView, name)
    }

    @FXML
    fun initialize() {
        println("!!!!!!!!!!!!!!!!!!")
    }


    companion object {
        private val directoryImage = ImageHolder("/images/directory.png")
    }
}


internal class ImageHolder(private val path: String) {
    private var _fileImage: Image? = null
    val image: Image
        get() {
            if (_fileImage == null) {
                synchronized(this) {
                    if (_fileImage != null) return _fileImage as Image
                    val resource = this.javaClass.getResource(path)
                    _fileImage = Image(resource.toExternalForm())
                }
            }
            return _fileImage as Image
        }

}
