package ru.biomedis.biomedismair3.social.remote_client.dto

import javafx.beans.property.SimpleStringProperty
import javafx.scene.image.Image
import java.io.ByteArrayInputStream
import java.time.Instant
import java.util.*

interface IFileItem {
    var id: Long
    var name: String
    val directoryMarker: Boolean
    var accessType: AccessVisibilityType
    fun nameProperty(): SimpleStringProperty
}

class FileData : IFileItem {
    override var id: Long = -1
    var createdDate: Date = Date.from(Instant.now())
    var extension: String = ""
    var directory: Long? = null
    var type: FileType = FileType.FILE
    var thumbnail: ByteArray? = null
    var fileSize: Float = 0F
    override var accessType: AccessVisibilityType = AccessVisibilityType.PRIVATE
    @Transient
    var thumbnailImage: Image? = null

    override val directoryMarker: Boolean
        get() = false

    private var _name: SimpleStringProperty = SimpleStringProperty(this, "name", "")
    override var name: String
        get() = _name.get()
        set(s) = _name.set(s)

    override fun nameProperty() = _name

    private var _publicLink: SimpleStringProperty = SimpleStringProperty(this, "publicLink", "")
    var publicLink: String
        get() = _publicLink.get()
        set(s) = _publicLink.set(s)

    fun publicLinkProperty() = _publicLink

    private var _privateLink: SimpleStringProperty = SimpleStringProperty(this, "privateLink", "")
    var privateLink: String
        get() = _privateLink.get()
        set(s) = _privateLink.set(s)

    fun privateLinkProperty() = _privateLink

    companion object {
        inline fun fillThumbnailImage(file: FileData) {
            if (file.thumbnail != null) {
                file.thumbnailImage = Image(ByteArrayInputStream(file.thumbnail))
            }
        }
    }
}

class DirectoryData : IFileItem {
    override var id: Long = -1
    var parent: Long? = null
    override var accessType: AccessVisibilityType = AccessVisibilityType.PRIVATE
    private var _name: SimpleStringProperty = SimpleStringProperty(this, "name", "")
    override var name: String
        get() = _name.get()
        set(s) = _name.set(s)

    override fun nameProperty() = _name

    override val directoryMarker: Boolean
        get() = true
}

class UserNameDto {
    var id: Long = -1;
    var name: String = ""
}

enum class AccessVisibilityType {
    PRIVATE,//только для себя, не видны пользователям, не могут быть скачаны
    PUBLIC, //для всех, можно публично давать ссылки в сеть, видны в профиле, могут быть скачаны из чата и ленты
    PROTECTED,//видно в профиле, файлы могут быть получены по ссылке из чата и публикации( будет закачка из программы)
    BY_LINK;//приватны, но можно получать по прямой ссылке - код используется


}

class Links {
    var privateLink: String = ""
    var publicLink: String = ""
}

class StorageDto {
    var id: Long = 0
    var occupiedVolume: Float = 0F
    var availableVolume: Float = 0F
}

