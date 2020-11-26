package ru.biomedis.biomedismair3.social.contacts

import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.*
import javafx.scene.layout.FlowPane
import javafx.scene.layout.VBox
import javafx.util.Callback
import ru.biomedis.biomedismair3.social.remote_client.SocialClient
import java.io.IOException
import java.util.function.Consumer


class ContactUserCellFactory(
        val aboutService: (user: Long) -> String,
        val followService: (contact: Long, follow: Boolean) -> Boolean,
        val deleteService:(contact: UserContact)->Unit,
        val showStoriesAction: (contact: Long) -> Unit

) : Callback<ListView<UserContact>, ListCell<UserContact>> {

    override fun call(param: ListView<UserContact>?): ListCell<UserContact> {
        return TaskCell(aboutService, followService, deleteService, showStoriesAction)
    }

    class TaskCell(
            val aboutService: (user: Long) -> String,
            val followService: (contact: Long, follow: Boolean) -> Boolean,
            val deleteService:(contact: UserContact)->Unit,
            val showStoriesAction: (contact: Long) -> Unit
    ) : ListCell<UserContact>() {

        @FXML
        private lateinit var followCheckBox: CheckBox

        @FXML
        private lateinit var showAboutBtn: Hyperlink

        @FXML
        private lateinit var deleteBtn: Button

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

        @FXML
        private lateinit var showStoriesBtn: Hyperlink

        @FXML
        private lateinit var messageCounter: Label

        private fun loadFXML() {
            try {
                val loader = FXMLLoader(javaClass.getResource("/fxml/Social/ContactUserListCell.fxml"))
                loader.setController(this)
                loader.setRoot(this)
                loader.load<Any>()
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }

        override fun updateItem(contact: UserContact?, empty: Boolean) {
            super.updateItem(contact, empty)

            if (empty || contact == null) {
                text = null
                contentDisplay = ContentDisplay.TEXT_ONLY
                return
            }
            val account = contact.account
            login.text = account.login
            email.text = account.email
            name.text = account.name
            surname.text = account.surname
            skype.text = if (!account.skype.isEmpty()) "Skype: ${account.skype}" else ""
            city.text = account.city?.name ?: ""
            country.text = account.country?.name ?: ""

            if (account.support) support.text = "Тех.Поддержка | " else support.text = ""
            if (account.company) company.text = "Представитель компании | " else company.text = ""
            if (account.doctor) doctor.text = "БРТ-терапевт | " else doctor.text = ""
            if (account.bris) diagnost.text = "Диагност | " else diagnost.text = ""
            if (account.partner) partner.text = "Участник партнерской программы | " else partner.text = ""
            if (account.depot) depot.text = "Склад" else depot.text = ""
            followCheckBox.isSelected = contact.contact.following
            showAboutBtn.isVisible = !account.emptyAbout

            contentDisplay = ContentDisplay.GRAPHIC_ONLY

        }

        fun onFollowAction() {
            val prev = !followCheckBox.isSelected
            if (!followService(item.contact.id, followCheckBox.isSelected)) {
                followCheckBox.isSelected = prev
            }
        }

        fun delete() {
            deleteService(item)
        }

        fun showAbout() {
            val foundTextArea = rootBox.children.lastOrNull { it.id == "about" }
            if (foundTextArea != null) {
                rootBox.children.remove(foundTextArea)
                rootBox.requestLayout()
                return
            }
            val aboutText = aboutService(item.account.id)
            if (aboutText.isEmpty()) return

            val textArea = TextArea().apply {
                id = "about"
                maxWidth = Double.MAX_VALUE

                this.isWrapText = true
                this.isEditable = false
                this.prefColumnCount = 0


            }
            textArea.text = aboutText

            rootBox.children.add(textArea)

        }

        val addNewMessagesHandler: Consumer<MutableMap<Long, Int>>

        private fun onNewMessageHandler(info: Map<Long,Int>){
           var count =0
            if(info.containsKey(item.contact.id)){
                count = info.getValue(item.contact.id)
            }
            Platform.runLater {

                messageCounter.text= count.toString()
               if(count==0){
                   messageCounter.styleClass.add("ContactMessageCountEmpty")
                   messageCounter.styleClass.remove("ContactMessageCount")
               }
                else {
                    messageCounter.styleClass.add("ContactMessageCount")
                   messageCounter.styleClass.remove("ContactMessageCountEmpty")
               }
            }
        }

        init {
            loadFXML()
            showStoriesBtn.setOnAction { Platform.runLater { showStoriesAction(item.account.id) } }
            addNewMessagesHandler =  SocialClient.INSTANCE.addNewMessagesHandler(this::onNewMessageHandler)
        }

    }
}
