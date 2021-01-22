package ru.biomedis.biomedismair3.social.remote_client.dto

import javafx.beans.property.SimpleStringProperty
import javafx.scene.image.Image
import java.io.ByteArrayInputStream
import java.time.Instant
import java.util.*

interface  IFileItem{
    var id: Long
    var name: String
    val directoryMarker: Boolean
    fun nameProperty(): SimpleStringProperty
}

class FileData: IFileItem {
    override var id: Long=-1
    var createdDate: Date = Date.from(Instant.now())
    var extension: String=""
    var directory: Long?=null
    var type: FileType = FileType.FILE
    var thumbnail: ByteArray? = null
    @Transient var thumbnailImage: Image? = null

    override val directoryMarker: Boolean
        get() = false

    private var _name: SimpleStringProperty = SimpleStringProperty(this, "name", "")
    override  var name: String
        get() = _name.get()
        set(s) = _name.set(s)

    override fun nameProperty() = _name

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
    var parent: Long?=null

    private var _name: SimpleStringProperty = SimpleStringProperty(this, "name", "")
    override  var name: String
        get() = _name.get()
        set(s) = _name.set(s)

    override fun nameProperty() = _name

    override val directoryMarker: Boolean
        get() = true
}

class UserNameDto{var id: Long=-1; var name: String=""}

