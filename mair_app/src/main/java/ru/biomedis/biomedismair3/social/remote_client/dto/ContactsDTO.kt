package ru.biomedis.biomedismair3.social.remote_client.dto

import ru.biomedis.biomedismair3.social.account.AccountView

class ContactDto {
    var id: Long = -1
    var user: Long = -1
    var contact: Long = -1
    var following: Boolean = false
}


class SmallContactViewDto {
    var id: Long = -1
    var userId: Long = -1
    var contactUserId: Long = -1
    var login: String = ""
    var name: String = ""
    var surname: String = ""
    var isFollowing: Boolean = false
}


class ContactView{
    var id: Long  = -1
    var userId: Long  = -1
    var isFollowing: Boolean  = false
    lateinit var  contactView: AccountView
}
