package ru.biomedis.biomedismair3.social.admin

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import ru.biomedis.biomedismair3.social.remote_client.Role

class AccountSmallViewObserved {
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

    private var _depot: BooleanProperty = SimpleBooleanProperty(this, "depot", false)
    var isDepot: Boolean
        get() = _depot.get()
        set(s) = _depot.set(s)

    fun depotProperty() = _depot

    private var _doctor: BooleanProperty = SimpleBooleanProperty(this, "doctor", false)
    var isDoctor: Boolean
        get() = _doctor.get()
        set(s) = _doctor.set(s)

    fun doctorProperty() = _doctor

    private var _partner: BooleanProperty = SimpleBooleanProperty(this, "partner", false)
    var isPartner: Boolean
        get() = _partner.get()
        set(s) = _partner.set(s)

    fun partnerProperty() = _partner

    private var _bris: BooleanProperty = SimpleBooleanProperty(this, "bris", false)
    var isBris: Boolean
        get() = _bris.get()
        set(s) = _bris.set(s)

    fun brisProperty() = _bris

    private var _company: BooleanProperty = SimpleBooleanProperty(this, "company", false)
    var isCompany: Boolean
        get() = _company.get()
        set(s) = _company.set(s)

    fun companyProperty() = _company

    private var _support: BooleanProperty = SimpleBooleanProperty(this, "support", false)
    var isSupport: Boolean
        get() = _support.get()
        set(s) = _support.set(s)

    fun supportProperty() = _support
}

class AccountWithRoles {
    lateinit var userSmallView: AccountSmallViewObserved

    @JsonDeserialize(using = CustomRoleDeserializer::class)
    lateinit var roles: List<Role>
}

class CustomRoleDeserializer: StdDeserializer<List<Role>> {
    constructor():this(null as Class<*>?)
    constructor(vc: Class<*>?) : super(vc)
    constructor(valueType: JavaType?) : super(valueType)
    constructor(src: StdDeserializer<*>?) : super(src)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): List<Role> {
        val values: Array<String> = p.readValueAs(Array<String>::class.java)
       return Role.listByArrayNames(values)
    }

}
