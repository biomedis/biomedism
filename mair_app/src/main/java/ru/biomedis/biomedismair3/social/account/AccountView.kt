package ru.biomedis.biomedismair3.social.account


class AccountView {
    var id: Long = -1
    var login = ""
    var email = ""
    var name = ""
    var surname = ""
    var country = ""
    var city = ""
    var skype = ""
    var about = ""
    var depot = false
    var doctor = false
    var partner = false
}

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
    var isPartner: Boolean = false
    var isDoctor: Boolean = false
    var isCompany: Boolean = false
    var isDepot: Boolean = false
    var isSupport: Boolean = false
}
