package ru.biomedis.biomedismair3.social.contacts

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.scene.control.ListView
import javafx.stage.Modality
import javafx.stage.WindowEvent
import ru.biomedis.biomedismair3.BaseController
import ru.biomedis.biomedismair3.BlockingAction
import ru.biomedis.biomedismair3.social.remote_client.SocialClient
import ru.biomedis.biomedismair3.utils.TabHolder
import ru.biomedis.biomedismair3.social.remote_client.dto.ContactDto
import ru.biomedis.biomedismair3.social.remote_client.dto.SmallContactViewDto
import ru.biomedis.biomedismair3.social.remote_client.dto.error.ApiError
import ru.biomedis.biomedismair3.utils.Other.LoggerDelegate
import ru.biomedis.biomedismair3.utils.Other.Result
import java.net.URL
import java.util.*

class ContactsController: BaseController(), TabHolder.Selected, TabHolder.Detached {
    private val log by LoggerDelegate()
   @FXML private lateinit var contactsList: ListView<SmallContactViewDto>


    private val contacts: ObservableList<SmallContactViewDto> = FXCollections.observableArrayList()

    override fun onCompletedInitialization() {

    }

    override fun onClose(event: WindowEvent) {

    }

    override fun setParams(vararg params: Any) {

    }

    override fun initialize(location: URL, resources: ResourceBundle) {
        contactsList.items = contacts
    }

    /**
     * Открывает диалог поиска пользователей, добавляет выбранного пользователя
     */
    fun findUsers() {
        val usersToContacts: List<AccountSmallView> = FindUsersController.showFindUserDialog(controllerWindow)
        addContacts(usersToContacts)

    }

    private fun addContacts(usersToContacts: List<AccountSmallView>){
        val result: Result<List<ContactDto>> = BlockingAction.actionResult(controllerWindow) {
            SocialClient.INSTANCE.contactsClient.addAllContact(usersToContacts.map { it.id })
        }

        if(result.isError){
            log.error("", result.error)
            if(result.error is ApiError){
                val err = result.error as ApiError
                if(err.statusCode==404){
                    showErrorDialog(
                            "Добавление контактов",
                            "Ошибка добавления контактов",
                            "Один или более добавляемых контактов не найдены",
                            controllerWindow,
                            Modality.WINDOW_MODAL
                    )
                }
            }else   showErrorDialog(
                    "Добавление контактов",
                    "Ошибка добавления контактов",
                    "",
                    controllerWindow,
                    Modality.WINDOW_MODAL
            )
            return
        }
        val contactsMap = result.value.groupBy { it.user }

        contacts.addAll(usersToContacts.map {
            SmallContactViewDto().apply{
                val contact =  contactsMap[it.id]?.get(0)?:throw RuntimeException("Не верное сопоставление id")
                id = contact.id
                userId = contact.user
                contactUserId = contact.contact
                login = it.login
                name = it.name
                surname = it.surname
                isFollowing = contact.following
            }
        })
    }

    /**
     * открывает диалог поиска по логину, добавляет найденного пользователя
     */
    fun addContact() {
        val user: AccountSmallView = AddUserController.showAddUserDialog(controllerWindow) ?: return
        addContacts(listOf(user))
    }

    private fun loadContacts(): List<SmallContactViewDto>{
        val result: Result<List<SmallContactViewDto>> = BlockingAction.actionResult(controllerWindow) {
            SocialClient.INSTANCE.contactsClient.allContacts()
        }
        if(result.isError){
            log.error("",result.error)
            showErrorDialog(
                    "Загрузка контактов",
                    "Ошибка загрузки контактов",
                    "Перезапустите программу и попробуйте позже",
                    controllerWindow,
                    Modality.WINDOW_MODAL
            )
            return listOf()
        }

        return result.value
    }



    private var isContactsLoaded = false

    override fun onSelected() {
        if(!isContactsLoaded){
            contacts.addAll(loadContacts())
            isContactsLoaded = true
        }
    }

    override fun onDetach() {

    }
}
