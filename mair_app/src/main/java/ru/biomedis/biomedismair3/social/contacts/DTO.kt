package ru.biomedis.biomedismair3.social.contacts

import javafx.beans.property.*
import java.time.Instant
import java.time.LocalDateTime
import java.util.*


class FindData {
    var name: String = ""
    var surname: String = ""
    var skype: String = ""
    var about: String = ""
    var isPartner: BooleanFind = BooleanFind()
    var isDoctor: BooleanFind = BooleanFind()
    var isCompany: BooleanFind = BooleanFind()
    var isDepot: BooleanFind = BooleanFind()
    var isSupport: BooleanFind = BooleanFind()
    var isBris: BooleanFind = BooleanFind()
}

class BooleanFind {
    var use: Boolean = false
    var value: Boolean = false
}


class AccountSmallView {
    var id: Long = -1
    var login: String = ""
    var name: String = ""
    var surname: String = ""
    var skype: String = ""
    var email: String = ""
    var city: String = ""
    var country: String = ""
    var isPartner: Boolean = false
    var isDoctor: Boolean = false
    var isCompany: Boolean = false
    var isDepot: Boolean = false
    var isSupport: Boolean = false
    var isBris: Boolean = false
}
