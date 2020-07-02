package ru.biomedis.biomedismair3.social.account

import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.stage.WindowEvent
import ru.biomedis.biomedismair3.AppController
import ru.biomedis.biomedismair3.BaseController
import ru.biomedis.biomedismair3.BlockingAction
import ru.biomedis.biomedismair3.Layouts.ProgressPanel.ProgressAPI
import ru.biomedis.biomedismair3.social.TextFieldUtil
import ru.biomedis.biomedismair3.social.login.RestorePasswordController.Companion.openRestoreDialog
import ru.biomedis.biomedismair3.social.remote_client.AccountClient
import ru.biomedis.biomedismair3.social.remote_client.SocialClient
import ru.biomedis.biomedismair3.utils.Other.LoggerDelegate
import ru.biomedis.biomedismair3.utils.Other.Result
import java.net.URL
import java.util.*

class AccountController: BaseController(){

    private lateinit var textFieldUtil: TextFieldUtil

    @FXML
    private lateinit var changeBtn: Button

    @FXML
    private lateinit var nameInput: TextField

    @FXML
    private lateinit var doctorInput: CheckBox


    @FXML
    private lateinit var depotInput: CheckBox

    @FXML
    private lateinit var diagnostInput: CheckBox

    @FXML
    private lateinit var partnerInput: CheckBox

    @FXML
    private lateinit var firstNameInput: TextField

    @FXML
    private lateinit var lastNameInput: TextField

    @FXML
    private lateinit var countryInput: TextField

    @FXML
    private lateinit var cityInput: TextField

    @FXML
    private lateinit var skypeInput: TextField

    @FXML
    private lateinit var aboutInput: TextArea


    @FXML
    private lateinit var root: TabPane
    private lateinit var res: ResourceBundle
    private lateinit var progressAPI: ProgressAPI


    private val log by LoggerDelegate()

    private lateinit var accountClient: AccountClient

    private lateinit var account:AccountView


    override fun setParams(vararg params: Any?) {

    }

    override fun onClose(event: WindowEvent) {

    }
    //смена почты отдельной кнопкой? Подтверждени почты в отдельное окно и контроллер(поправить логин)
    //добавить поля по врачам, складам итп
    // для партнерства - выкл просто, а вкл с проверкой
    override fun onCompletedInitialization() {
        changeBtn.disableProperty().bind(
                nameInput.textProperty().isEmpty

                        .or(firstNameInput.textProperty().isEmpty)
                        .or(lastNameInput.textProperty().isEmpty)
                        .or(countryInput.textProperty().isEmpty)
                        .or(cityInput.textProperty().isEmpty)
        )

        if(!SocialClient.INSTANCE.token.isPresent){
            Platform.runLater{
                showErrorDialog(
                        "Аккаунт",
                        "",
                        "Сессия потеряна, перезапустите программу. Если ошибка повториться обратитесь к разработчикам",
                        controllerWindow,
                        Modality.WINDOW_MODAL
                )
                controllerWindow.close()
            }
        }

        Platform.runLater {

            val result: Result<AccountView> = BlockingAction.actionResult(controllerWindow, BlockingAction.Action1 {
                accountClient.getAccount(SocialClient.INSTANCE.token.get().userId)
            })

            if(result.isError){
                showErrorDialog(
                        "Аккаунт",
                        "",
                        "Не удалось получить данные аккаунта. Перезапустите программу. Если ошибка повториться обратитесь к разработчикам",
                        controllerWindow,
                        Modality.WINDOW_MODAL
                )
                controllerWindow.close()
            }else {
                account = result.value
            }



        }
    }

    override fun initialize(location: URL, resources: ResourceBundle) {
       res = resources
        accountClient = SocialClient.INSTANCE.accountClient
        progressAPI = AppController.getProgressAPI()
        textFieldUtil = TextFieldUtil(
                mapOf(
                        "userName" to nameInput,
                        "firstName" to firstNameInput,
                        "lastName" to lastNameInput,
                        "country" to countryInput,
                        "city" to cityInput,
                        "skype" to skypeInput,
                        "about" to aboutInput
                ),
                mapOf(
                        "userName" to "Имя(псевдоним) пользователя",
                        "firstName" to "Фамилия",
                        "lastName" to "Имя",
                        "country" to "Страна",
                        "city" to "Город",
                        "skype" to "Skype",
                        "about" to "О себе"),
                controllerWindow,
                "редактирование профиля"
        )
    }



    fun onAccountChange(){

    }

    fun onChangePassword(actionEvent: ActionEvent) {
        //нельзя сбросить пароль любому человеку, тк сброс будет при подтверждении кода
        openRestoreDialog(controllerWindow, account.email, true)
    }

    fun onChangeEmail(actionEvent: ActionEvent) {

    }

    fun onChangeName(actionEvent: ActionEvent) {

    }

    companion object{
        private val log by LoggerDelegate()
        @JvmStatic
        fun showAccount(context: Stage) {
             try {
                openDialogUserData<Unit>(
                        context,
                        "/fxml/AccountDialog.fxml",
                        "Аккаунт",
                        true,
                        StageStyle.UTILITY,
                        0, 0, 0, 0,
                        Unit
                )
            } catch (e: Exception) {
                log.error("Ошибка открытия диалога аккаунта", e)
                throw RuntimeException(e)
            }
        }
    }

}
