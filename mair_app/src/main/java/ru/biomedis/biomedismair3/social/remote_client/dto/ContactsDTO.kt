package ru.biomedis.biomedismair3.social.remote_client.dto

import javafx.beans.property.SimpleObjectProperty
import ru.biomedis.biomedismair3.social.account.AccountView
import java.time.Instant
import java.util.*

class ContactDto {
    var id: Long = -1
    var user: Long = -1
    var contact: Long = -1
    var created: Date =  Date.from(Instant.now())
    var following: Boolean = false

    private var _lastMessageDate: SimpleObjectProperty<Date> = SimpleObjectProperty<Date>(this, "lastMessageDate", null)
    var lastMessageDate: Date?
        get() = _lastMessageDate.get()
        set(s) = _lastMessageDate.set(s)

    fun lastMessageDateProperty() = _lastMessageDate
}


class SmallContactViewDto {
    var id: Long = -1
    var userId: Long = -1
    var contactUserId: Long = -1
    var login: String = ""
    var name: String = ""
    var surname: String = ""
    var isFollowing: Boolean = false
    var created: Date =  Date.from(Instant.now())
}


class ContactView{
    var id: Long  = -1
    var userId: Long  = -1
    var isFollowing: Boolean  = false
    var created: Date = Date.from(Instant.now())
    lateinit var  contactView: AccountView
}
