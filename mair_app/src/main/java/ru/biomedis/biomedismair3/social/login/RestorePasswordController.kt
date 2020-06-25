package ru.biomedis.biomedismair3.social.login

import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.stage.WindowEvent
import ru.biomedis.biomedismair3.AppController
import ru.biomedis.biomedismair3.BaseController
import ru.biomedis.biomedismair3.BlockingAction
import ru.biomedis.biomedismair3.social.remote_client.RegistrationClient
import ru.biomedis.biomedismair3.social.remote_client.SocialClient
import ru.biomedis.biomedismair3.social.remote_client.dto.error.ApiError
import ru.biomedis.biomedismair3.utils.Other.LoggerDelegate
import java.net.URL
import java.util.*


class RestorePasswordController : BaseController() {

    @FXML
    private lateinit var root: VBox
    @FXML
    private lateinit var emailInput: TextField
    @FXML
    private lateinit var codeInput: TextField
    @FXML
    private lateinit var passwordInput: TextField
    @FXML
    private lateinit var updatePasswordBtn: Button

    private lateinit var registrationClient: RegistrationClient



    override fun setParams(vararg params: Any) {

    }

    override fun onClose(event: WindowEvent) {
    }

    override fun onCompletedInitialization() {
        emailInput.text = root.userData as String
    }

    override fun initialize(location: URL, resources: ResourceBundle) {
        updatePasswordBtn.disableProperty().bind(
                emailInput.textProperty().isEmpty.or(codeInput.textProperty().isEmpty)
                .or(passwordInput.textProperty().isEmpty))

        registrationClient = SocialClient.INSTANCE.registrationClient
    }

    fun onNewPassword() {

        val result = BlockingAction.actionNoResult(controllerWindow) {
            registrationClient.setNewPassword(emailInput.text.trim(), codeInput.text.trim(), passwordInput.text.trim())
        }

        if(!result.isError) {
            AppController.getProgressAPI().setInfoMessage("Пароль успешно обновлен")
            root.userData = emailInput.text.trim()
            controllerWindow.close()
            return
        }

        when(val err = result.error){
            is ApiError ->{
                log.error("Установка пароля", err)
                showErrorDialog(
                        "Установка пароля",
                        "",
                        "Пароль не  установлен, произошла ошибка. Попробуйте позже.",
                        controllerWindow,
                        Modality.WINDOW_MODAL
                )
            }
            else -> {
                showErrorDialog(
                        "Установка пароля",
                        "",
                        "Пароль возможно не  установлен, произошла ошибка",
                        controllerWindow,
                        Modality.WINDOW_MODAL
                )
                log.error("Установка пароля", err)
            }

        }


    }

    fun onResend(actionEvent: ActionEvent) {
        val result = BlockingAction.actionNoResult(controllerWindow ) {
            registrationClient.sendCode(emailInput.text.trim())
        }
        if (!result.isError) {
            showWarningDialog(
                    "Отправка кода",
                    "Код успешно отправлен на указанную почту",
                    "",
                    controllerWindow,
                    Modality.WINDOW_MODAL)
        }else {
            showWarningDialog(
                    "Отправка кода",
                    "Не удалось отправить код для восстановления",
                    "Попробуйте позже. Если ошибка повторится, обратитесь к разработчикам",
                    controllerWindow,
                    Modality.WINDOW_MODAL)
            log.error("Отправка кода восстановления", result.error)

        }
    }


    companion object {
        private val log by LoggerDelegate()

        @JvmStatic
        fun openRestoreDialog(context: Stage, email: String): String = try {
            openDialogUserData(
                    context,
                    "/fxml/RestorePasswordDialog.fxml",
                    "Вход",
                    false,
                    StageStyle.UTILITY,
                    0, 0, 0, 0,
                    email
            )
        } catch (e: Exception) {
            log.error("", e)
            throw RuntimeException(e)
        }
    }

}
