package ru.biomedis.biomedismair3.social.contacts

import javafx.fxml.FXML
import javafx.scene.control.TabPane
import javafx.stage.WindowEvent
import ru.biomedis.biomedismair3.BaseController
import ru.biomedis.biomedismair3.utils.Other.LoggerDelegate
import ru.biomedis.biomedismair3.utils.TabHolder
import java.net.URL
import java.util.*

class ContactsContentController : BaseController(), TabHolder.Selected, TabHolder.Detached {

    private val log by LoggerDelegate()

    @FXML
    private lateinit var root: TabPane


    private lateinit var tabHolder: TabHolder
    private lateinit var contact: UserContact

    override fun onCompletedInitialization() {
        tabHolder = TabHolder(controllerWindow, root)
        openChat(contact)
        openFiles(contact)
    }

    override fun onClose(event: WindowEvent) {

    }

    override fun setParams(vararg params: Any) {
        if(params.isEmpty()) {
            log.error("Должен быть параметр UserContact")
            throw RuntimeException("Должен быть параметр UserContact")
        }
        contact = params[0] as UserContact
    }

    override fun initialize(location: URL, resources: ResourceBundle) {


    }

    private fun openChat(contact: UserContact) {
        val fxId= "chat_${contact.contact.id}"
        var chat = tabHolder.tabByFxId(fxId)
        if (chat == null) {
            tabHolder.addTab(
                "/fxml/social/Chat.fxml",
                contact.account.login,
                false,
                "Чат",
                contact.contact.contact
            )
            chat = tabHolder.tabByFxId(fxId)
        }

        root.selectionModel.select(chat)
    }
    private fun openFiles(contact: UserContact) {
        val fxId= "files_${contact.contact.id}"
        var files = tabHolder.tabByFxId(fxId)
        if (files == null) {
            tabHolder.addTab(
                "/fxml/social/FilesViewer.fxml",
                "Файлы",
                false,
                contact.contact.id.toString(),
                contact.contact.contact
            )

        }
    }



    override fun onSelected() {

    }

    override fun onDetach() {
        tabHolder.removeAllTabs()
    }


}
