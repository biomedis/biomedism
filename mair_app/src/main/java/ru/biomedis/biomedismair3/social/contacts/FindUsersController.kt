package ru.biomedis.biomedismair3.social.contacts

import javafx.stage.Screen
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.stage.WindowEvent
import ru.biomedis.biomedismair3.BaseController
import ru.biomedis.biomedismair3.utils.Other.LoggerDelegate
import java.net.URL
import java.util.*

class FindUsersController: BaseController() {

    private lateinit var addedUsers: AddedUsers

    override fun onCompletedInitialization() {
        addedUsers = inputDialogData as AddedUsers
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
        fun showFindUserDialog(context: Stage):List<AccountSmallView> {
            val users  = AddedUsers()
            return try {
                openDialogUserData<AddedUsers>(
                        context,
                        "/fxml/social/FindUsersDialog.fxml",
                        "Поиск пользователей",
                        true,
                        StageStyle.UTILITY,
                        (Screen.getPrimary().bounds.height*0.9).toInt(), 0, 0, 0,
                        users
                ).users
            } catch (e: Exception) {
                log.error("Ошибка открытия диалога поиска пользователей", e)
                throw RuntimeException(e)
            }
        }
    }

    class AddedUsers{
        val users = mutableListOf<AccountSmallView>()
    }
}
