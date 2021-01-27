package ru.biomedis.biomedismair3.social.registry

import javafx.scene.image.Image
import ru.biomedis.biomedismair3.social.remote_client.dto.FileData

class FileTypeByExtension{
    val others = "Другие"
    val map = mapOf(
        "zip" to "Архивы",
        "rar" to "Архивы",
        "gzip" to "Архивы",
        "7z" to "Архивы",
        "bzip" to "Архивы",
        "tar" to "Архивы",
        "gz" to "Архивы",
        "doc" to "Документы",
        "docx" to "Документы",
        "xls" to "Электронные таблицы",
        "xlsx" to "Электронные таблицы",
        "pdf" to "Документы",
        "ppt" to "Презентации",
        "pptx" to "Презентации",
        "mp3" to "Аудио",
        "mp4" to "Видео",
        "jpg" to "Картинки",
        "jpeg" to "Картинки",
        "gif" to "Картинки",
        "png" to "Картинки"
    )

    fun typeNames(): List<String>{
        return map.values.distinct().toMutableList().apply {
            add(others)

        }
    }
}

class FileImages {

    companion object {
        private val fileImage = ImageHolder("/images/file.png")
        private val archImage = ImageHolder("/images/zip.png")
        private val docImage = ImageHolder("/images/doc.png")
        private val exelImage = ImageHolder("/images/exel.png")
        private val pdfImage = ImageHolder("/images/pdf.png")
        private val imageImage = ImageHolder("/images/image.png")

         fun selectImage(data: FileData): Image = when (data.extension) {
            "zip", "rar", "gzip", "7z", "bzip", "tar", "gz" -> archImage.image
            "doc", "docx" -> docImage.image
            "xls", "xlsx" -> exelImage.image
            "pdf" -> pdfImage.image
             "jpg","jpeg","gif","png"-> imageImage.image
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
