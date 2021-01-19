package ru.biomedis.biomedismair3.social.remote_client.dto

import javafx.scene.image.Image
import java.io.ByteArrayInputStream
import java.time.Instant
import java.util.*

interface  IFileItem{
    var id: Long
    var name: String
    val directoryMarker: Boolean
}

class FileData: IFileItem {
    override var id: Long=-1
    var createdDate: Date = Date.from(Instant.now())
    override  var name: String=""
    var extension: String=""
    var directory: Long?=null
    var type: FileType = FileType.FILE
    var thumbnail: ByteArray? = null
    @Transient var thumbnailImage: Image? = null

    override val directoryMarker: Boolean
        get() = false

    companion object{
        inline fun fillThumbnailImage(file: FileData){
            if(file.thumbnail!=null){
                file.thumbnailImage =  Image(ByteArrayInputStream(file.thumbnail))
            }
        }
    }
}

class DirectoryData:IFileItem{
    override var id: Long=-1
    override  var name: String=""
    var parent: Long?=null

    override val directoryMarker: Boolean
        get() = true
}

class UserNameDto{var id: Long=-1; var name: String=""}

