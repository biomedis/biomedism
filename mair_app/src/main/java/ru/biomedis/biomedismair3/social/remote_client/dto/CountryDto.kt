package ru.biomedis.biomedismair3.social.remote_client.dto

import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty

class CountryDto {
    var id: Long = -1
    var name: String = ""
}

class CountryView{
    var id: Long = -1
    private var _name: StringProperty = SimpleStringProperty(this, "name", "")
    var name: String
        get() = _name.get()
        set(s) = _name.set(s)

    fun nameProperty() = _name
}
