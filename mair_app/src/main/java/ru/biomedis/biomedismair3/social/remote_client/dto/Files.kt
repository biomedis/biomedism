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
    var accessType: AccessVisibilityType
    fun nameProperty(): SimpleStringProperty
    }

class FileData : IFileItem {
    override var id: Long=-1
    var createdDate: Date = Date.from(Instant.now())
    var extension: String=""
    var directory: Long?=null
    var type: FileType = FileType.FILE
    var thumbnail: ByteArray? = null
    override var accessType: AccessVisibilityType = AccessVisibilityType.PRIVATE
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
    override var accessType: AccessVisibilityType = AccessVisibilityType.PRIVATE
    private var _name: SimpleStringProperty = SimpleStringProperty(this, "name", "")
    override  var name: String
        get() = _name.get()
        set(s) = _name.set(s)

    override fun nameProperty() = _name

    override val directoryMarker: Boolean
        get() = true
}

class UserNameDto{var id: Long=-1; var name: String=""}

enum class AccessVisibilityType {
    PRIVATE,//только для себя
    PUBLIC, //для всех, можно публично давать ссылки в сеть
    PROTECTED,//доступно по ссылкам публично, но не видно в профиле пользователя( для ресурсов, которые указываются в сообщениях и ленте)
    BY_LINK,//приватны, но можно получать публично по ссылке - код используется
    REGISTERED;//доступно для публично для пользователей сервиса
}
