package ru.biomedis.biomedismair3.social.contacts

import javafx.stage.Screen
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.stage.WindowEvent
import ru.biomedis.biomedismair3.BaseController
import ru.biomedis.biomedismair3.utils.Other.LoggerDelegate
import java.net.URL
import java.util.*

class AddUserController: BaseController() {

    private lateinit var addedUser: AddedUser

    override fun onCompletedInitialization() {
        addedUser = inputDialogData as AddedUser
    }

    override fun onClose(event: WindowEvent) {

    }

    override fun setParams(vararg params: Any) {

    }

    override fun initialize(location: URL, resources: ResourceBundle) {

    }


    companion object{
        private val log by LoggerDelegate()

        /**
         * Поиск пользователей.
         * Вернет список добавленных в контакты пользователей
         */
        @JvmStatic
        fun showAddUserDialog(context: Stage):AccountSmallView? {
            val user  = AddedUser()
            return try {
                openDialogUserData<AddedUser>(
                        context,
                        "/fxml/social/AddUser.fxml",
                        "Поиск пользователей",
                        false,
                        StageStyle.UTILITY,
                        0, 0, 0, 0,
                        user
                ).user
            } catch (e: Exception) {
                log.error("Ошибка открытия диалога добавления пользователя", e)
                throw RuntimeException(e)
            }
        }
    }

    class AddedUser(){
        var user: AccountSmallView? = null
    }
}
