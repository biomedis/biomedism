package ru.biomedis.biomedismair3.social.contacts

import ru.biomedis.biomedismair3.social.remote_client.dto.CityDto
import ru.biomedis.biomedismair3.social.remote_client.dto.CountryDto


class FindData {
    var name: String = ""
    var surname: String = ""
    var skype: String = ""
    var about: String = ""
    var city: Long = -1
    var country: Long = -1
    var partner: BooleanFind = BooleanFind()
    var doctor: BooleanFind = BooleanFind()
    var company: BooleanFind = BooleanFind()
    var depot: BooleanFind = BooleanFind()
    var support: BooleanFind = BooleanFind()
    var bris: BooleanFind = BooleanFind()
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
    var city: CityDto? = null
    var country: CountryDto? = null
    var partner: Boolean = false
    var doctor: Boolean = false
    var company: Boolean = false
    var depot: Boolean = false
    var support: Boolean = false
    var bris: Boolean = false
    var emptyAbout: Boolean = false
}
