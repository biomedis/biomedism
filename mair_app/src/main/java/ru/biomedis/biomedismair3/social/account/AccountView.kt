package ru.biomedis.biomedismair3.social.account

import javafx.beans.property.*
import java.time.Instant
import java.time.LocalDateTime
import java.util.*


class AccountView {
    var id: Long = -1

    private var _login: StringProperty = SimpleStringProperty(this, "login", "")
    var login: String
        get() = _login.get()
        set(s) = _login.set(s)

    fun loginProperty() = _login

    private var _email: StringProperty = SimpleStringProperty(this, "email", "")
    var email: String
        get() = _email.get()
        set(s) = _email.set(s)

    fun emailProperty() = _email

    private var _name: StringProperty = SimpleStringProperty(this, "name", "")
    var name: String
        get() = _name.get()
        set(s) = _name.set(s)

    fun nameProperty() = _name

    private var _surname: StringProperty = SimpleStringProperty(this, "surname", "")
    var surname: String
        get() = _surname.get()
        set(s) = _surname.set(s)

    fun surnameProperty() = _surname

    private var _country: StringProperty = SimpleStringProperty(this, "country", "")
    var country: String
        get() = _country.get()
        set(s) = _country.set(s)

    fun countryProperty() = _country

    private var _city: StringProperty = SimpleStringProperty(this, "city", "")
    var city: String
        get() = _city.get()
        set(s) = _city.set(s)

    fun cityProperty() = _city

    private var _skype: StringProperty = SimpleStringProperty(this, "skype", "")
    var skype: String
        get() = _skype.get()
        set(s) = _skype.set(s)

    fun skypeProperty() = _skype

    private var _about: StringProperty = SimpleStringProperty(this, "about", "")
    var about: String
        get() = _about.get()
        set(s) = _about.set(s)

    fun aboutProperty() = _about

    private var _depot: BooleanProperty = SimpleBooleanProperty(this, "depot", false)
    var depot: Boolean
        get() = _depot.get()
        set(s) = _depot.set(s)

    fun depotProperty() = _depot

    private var _doctor: BooleanProperty = SimpleBooleanProperty(this, "doctor", false)
    var doctor: Boolean
        get() = _doctor.get()
        set(s) = _doctor.set(s)

    fun doctorProperty() = _doctor

    private var _partner: BooleanProperty = SimpleBooleanProperty(this, "partner", false)
    var partner: Boolean
        get() = _partner.get()
        set(s) = _partner.set(s)

    fun partnerProperty() = _partner

    private var _bris: BooleanProperty = SimpleBooleanProperty(this, "bris", false)
    var bris: Boolean
        get() = _bris.get()
        set(s) = _bris.set(s)

    fun brisProperty() = _bris

    private var _company: BooleanProperty = SimpleBooleanProperty(this, "company", false)
    var company: Boolean
        get() = _company.get()
        set(s) = _company.set(s)

    fun companyProperty() = _company

    private var _support: BooleanProperty = SimpleBooleanProperty(this, "support", false)
    var support: Boolean
        get() = _support.get()
        set(s) = _support.set(s)

    fun supportProperty() = _support
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
    var isPartner: Boolean = false
    var isDoctor: Boolean = false
    var isCompany: Boolean = false
    var isDepot: Boolean = false
    var isSupport: Boolean = false
    var isBris: Boolean = false
}

class ActiveSession {
    var id: Long = -1
    var expired: Date = Date.from(Instant.now())
    var created: Date = Date.from(Instant.now())
    var ip: String = ""
    var os: String = ""
    var browser: String = ""
    var city: String = ""
    var country: String = ""
    var region: String = ""

}
