package ru.biomedis.biomedismair3.social.contacts

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.*
import javafx.scene.layout.FlowPane
import javafx.scene.layout.VBox
import javafx.util.Callback
import java.io.IOException


class FoundUserCellFactory(
        val findDataContainer: FindUsersController.FindDataContainer?,
        val aboutService:(user:Long)->String) : Callback<ListView<AccountSmallView>, ListCell<AccountSmallView>> {

    override fun call(param: ListView<AccountSmallView>?): ListCell<AccountSmallView> {
        return TaskCell(findDataContainer, aboutService)
    }

    class TaskCell(val findDataContainer: FindUsersController.FindDataContainer? ,
                   val aboutService:(user:Long)->String) : ListCell<AccountSmallView>() {
        @FXML
        private lateinit var  showAboutBtn: Hyperlink

        @FXML
        private lateinit var rootBox: VBox

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
        private lateinit var city: Label

        @FXML
        private lateinit var country: Label

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
                val loader = FXMLLoader(javaClass.getResource("/fxml/social/FoundUserListCell.fxml"))
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
                skype.text = if (!item.skype.isEmpty()) "Skype: ${item.skype}" else ""
                city.text = item.city?.name ?: ""
                country.text = item.country?.name ?: ""

                if (item.support) support.text = "Тех.Поддержка | " else support.text = ""
                if (item.company) company.text = "Представитель компании | " else company.text = ""
                if (item.doctor) doctor.text = "БРТ-терапевт | " else doctor.text = ""
                if (item.bris) diagnost.text = "Диагност | " else diagnost.text = ""
                if (item.partner) partner.text = "Участник партнерской программы | " else partner.text = ""
                if (item.depot) depot.text = "Склад" else depot.text = ""

                showAboutBtn.isVisible = !item.emptyAbout
                //подсветка зеленым, то что искали
               applyFindData()

                contentDisplay = ContentDisplay.GRAPHIC_ONLY
            }
        }

        fun showAbout() {
            val foundTextArea = rootBox.children.lastOrNull { it.id == "about" }
            if(foundTextArea!=null){
                rootBox.children.remove(foundTextArea)
                rootBox.requestLayout()
                return
            }
            val aboutText  = aboutService(item.id)
            if(aboutText.isEmpty()) return

            val textArea = TextArea().apply {
                        id = "about"
                        maxWidth = Double.MAX_VALUE
                        this.isWrapText = true
                        this.isEditable = false
                    }
            textArea.text = aboutText

            rootBox.children.add(textArea)

        }

        init {
            loadFXML()
        }

        private fun applyFindData() {
            if(findDataContainer==null) return
            fun styled(label: Label) {
                label.styleClass.apply {
                    if (!contains("GreenText")) add("GreenText")
                }
            }

            fun deStyled(label: Label) {
                label.styleClass.apply {
                    remove("GreenText")
                }
            }


            fun boolFieldStyle(field: BooleanFind, label: Label) {
                if (field.value) styled(label)
                else deStyled(label)
            }

            fun cityOrCountryFieldStyle(field: Long, label: Label) {
                if (field > 0) styled(label)
                else deStyled(label)
            }

            fun textFieldStyle(field: String, label: Label) {
                if (field.isNotEmpty()) styled(label)
                else deStyled(label)
            }

            findDataContainer.findData.let {
                boolFieldStyle(it.bris, diagnost)
                boolFieldStyle(it.doctor, doctor)
                boolFieldStyle(it.support, support)
                boolFieldStyle(it.company, company)
                boolFieldStyle(it.partner, partner)
                boolFieldStyle(it.depot, depot)

                cityOrCountryFieldStyle(it.city, city)
                cityOrCountryFieldStyle(it.country, country)

                textFieldStyle(it.name, name)
                textFieldStyle(it.surname, surname)
                textFieldStyle(it.skype, skype)

            }
        }
    }
}
