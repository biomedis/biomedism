package ru.biomedis.biomedismair3.social.contacts

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.ContentDisplay
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.layout.FlowPane
import javafx.scene.layout.HBox
import javafx.util.Callback
import java.io.IOException
import java.text.SimpleDateFormat


class FoundUserCellFactory : Callback<ListView<AccountSmallView>, ListCell<AccountSmallView>> {
    override fun call(param: ListView<AccountSmallView>?): ListCell<AccountSmallView> {
        return TaskCell()
    }

    class TaskCell : ListCell<AccountSmallView>() {

        @FXML
        private lateinit var login: Label

        @FXML
        private lateinit var email: Label

        @FXML
        private lateinit var name: Label

        @FXML
        private lateinit var surname: Label

        @FXML
        private lateinit var skype: Label


        @FXML
        private lateinit var support: Label

        @FXML
        private lateinit var doctor: Label

        @FXML
        private lateinit var partner: Label

        @FXML
        private lateinit var depot: Label

        @FXML
        private lateinit var diagnost: Label

        @FXML
        private lateinit var company: Label

        @FXML
        private lateinit var boolContainer: FlowPane


        private fun loadFXML() {
            try {
                val loader = FXMLLoader(javaClass.getResource("/fxml/Social/FoundUserListCell.fxml"))
                loader.setController(this)
                loader.setRoot(this)
                loader.load<Any>()
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }

        override fun updateItem(item: AccountSmallView?, empty: Boolean) {
            super.updateItem(item, empty)
            if (empty || item == null) {
                text = null
                contentDisplay = ContentDisplay.TEXT_ONLY
            } else {
                login.text = item.login
                email.text = item.email
                name.text = item.name
                surname.text = item.surname
                skype.text = if(!item.skype.isEmpty())"Skype: ${item.skype}" else ""

             if(item.support) support.text = "Тех.Поддержка | "
             if(item.company)  company.text = "Представитель компании | "
             if(item.doctor) doctor.text = "БРТ-терапевт | "
             if(item.bris)  diagnost.text = "Диагност | "
             if(item.partner)  partner.text = "Участник партнерской программы | "
             if(item.depot)  depot.text = "Склад"


                contentDisplay = ContentDisplay.GRAPHIC_ONLY
            }
        }

        init {
            loadFXML()
        }
    }
}
