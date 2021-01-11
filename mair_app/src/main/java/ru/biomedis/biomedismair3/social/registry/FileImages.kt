package ru.biomedis.biomedismair3.social.registry

import javafx.scene.image.Image
import ru.biomedis.biomedismair3.social.remote_client.dto.FileData

class FileImages {

    companion object {
        private val fileImage = ImageHolder("/images/file.png")
        private val archImage = ImageHolder("/images/zip.png")
        private val docImage = ImageHolder("/images/doc.png")
        private val exelImage = ImageHolder("/images/exel.png")
        private val pdfImage = ImageHolder("/images/pdf.png")

         fun selectImage(data: FileData): Image = when (data.extension) {
            "zip", "rar", "gzip", "7z", "bzip", "tar", "gz" -> archImage.image
            "doc", "docx" -> docImage.image
            "xls", "xlsx" -> exelImage.image
            "pdf" -> pdfImage.image
            else -> fileImage.image
        }

    }
}


class DirectoryImage {

    companion object {
         val directoryImage = ImageHolder("/images/directory.png").image
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
