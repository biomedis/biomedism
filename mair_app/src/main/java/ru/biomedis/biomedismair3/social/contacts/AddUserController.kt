package ru.biomedis.biomedismair3.social.contacts

import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ListView
import javafx.scene.control.SelectionMode
import javafx.scene.control.TextField
import javafx.scene.input.KeyEvent
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.stage.WindowEvent
import ru.biomedis.biomedismair3.BaseController
import ru.biomedis.biomedismair3.BlockingAction
import ru.biomedis.biomedismair3.social.remote_client.SocialClient
import ru.biomedis.biomedismair3.social.remote_client.dto.error.ApiError
import ru.biomedis.biomedismair3.utils.Other.LoggerDelegate
import java.net.URL
import java.util.*

class AddUserController: BaseController() {
    private val log by LoggerDelegate()
    private lateinit var addedUser: AddedUser

    @FXML lateinit var searchBtn: Button
    @FXML lateinit var addBtn: Button
    @FXML lateinit var foundUserList: ListView<AccountSmallView>
    @FXML lateinit var loginInput: TextField

    private val inputRegex: Regex = "\\w".toRegex()
    private val inputClearingRegex: Regex = "[^\\w]".toRegex()

    private var foundUser: AccountSmallView?=null


    override fun onCompletedInitialization() {
        addedUser = inputDialogData as AddedUser
    }

    override fun onClose(event: WindowEvent) {

    }

    override fun setParams(vararg params: Any) {

    }

    override fun initialize(location: URL, resources: ResourceBundle) {
        searchBtn.disableProperty().bind(loginInput.textProperty().isEmpty)
        addBtn.disableProperty().bind(foundUserList.selectionModel.selectedItemProperty().isNull)
        initFoundList()
        loginInput.addEventFilter(KeyEvent.KEY_TYPED){
            if(it.isShiftDown || it.isControlDown) {
                clearTextField()
                return@addEventFilter
            }
            if(!it.character.matches(inputRegex)) it.consume()
        }
    }

    private fun clearTextField(){
        if(inputClearingRegex.containsMatchIn(loginInput.text))loginInput.text = loginInput.text.replace(inputClearingRegex,"")

    }

    private fun initFoundList() {
        foundUserList.apply {
            cellFactory = FoundUserCellFactory(null) {
                val result = BlockingAction.actionResult(controllerWindow) {
                    SocialClient.INSTANCE.accountClient.getAbout(it)
                }
                if (result.isError) {
                    showWarningDialog(
                            "Загрузка данных о пользователе",
                            "Загрузка данных не удалась",
                            "Перезапустите программу или попробуйте позже",
                            controllerWindow,
                            Modality.WINDOW_MODAL
                    )
                    log.error("", result.error)

                }
                if (!result.isError) result.value else ""
            }
            selectionModel.selectionMode = SelectionMode.SINGLE
        }
    }

    fun search() {
        clearTextField()
        if(loginInput.text.trim().isEmpty()) return
        val result = BlockingAction.actionResult(controllerWindow) {
            SocialClient.INSTANCE.accountClient.findUserByLogin(loginInput.text.trim())
        }
        if (result.isError) {
            foundUserList.items.clear()
            foundUser = null
            if(result.error is ApiError){
             val apiError = result.error as ApiError
                if(apiError.statusCode==404){
                    showInfoDialog(
                            "Поиск пользователя по псевдониму",
                            "Пользователь не найден",
                            "",
                            controllerWindow,
                            Modality.WINDOW_MODAL
                    )
                }

                return
            }
            showWarningDialog(
                    "Загрузка данных о пользователе",
                    "Загрузка данных не удалась",
                    "Перезапустите программу или попробуйте позже",
                    controllerWindow,
                    Modality.WINDOW_MODAL
            )
            log.error("", result.error)

            return
        }
        foundUserList.items.clear()
        foundUserList.items.add(result.value)
        foundUser = result.value
    }

    fun addUser() {
        addedUser.user = foundUser
        controllerWindow.close()
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
