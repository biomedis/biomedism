package ru.biomedis.biomedismair3.social.account

import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.Hyperlink
import javafx.scene.control.TextField
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.stage.WindowEvent
import ru.biomedis.biomedismair3.BaseController
import ru.biomedis.biomedismair3.BlockingAction
import ru.biomedis.biomedismair3.social.remote_client.AccountClient
import ru.biomedis.biomedismair3.social.remote_client.SocialClient
import ru.biomedis.biomedismair3.social.remote_client.dto.error.ApiError
import ru.biomedis.biomedismair3.utils.Other.LoggerDelegate
import java.net.URL
import java.util.*

class ChangeEmailController : BaseController() {
    private val log by LoggerDelegate()
    @FXML
    private lateinit var inputCode: TextField
    @FXML
    private lateinit var inputCodeOld: TextField
    @FXML
    private lateinit var inputEmail: TextField

    @FXML
    private lateinit var codeBox: VBox
    @FXML
    private lateinit var emailBox: VBox

    @FXML
    private lateinit var sendCode: Button
    @FXML
    private lateinit var nextBtn: Button

    @FXML
    private lateinit var resendLink: Hyperlink

    private lateinit var email: String
    private lateinit var res: ResourceBundle

    private val timer: Timer = Timer()

    private val accountClient: AccountClient by lazy { SocialClient.INSTANCE.accountClient }

    private lateinit var data: Data

    override fun setParams(vararg params: Any) {
        if (params.isEmpty()) throw RuntimeException("Не верные параметры. Должен быть параметр email")
        email = params[0] as String
    }

    override fun onClose(event: WindowEvent) {

    }

    override fun onCompletedInitialization() {
        data = inputDialogData as Data
    }

    override fun initialize(location: URL, resources: ResourceBundle) {
        res = resources
        sendCode.disableProperty()
                .bind(inputCode.textProperty().isEmpty.or(inputCodeOld.textProperty().isEmpty))

        nextBtn.disableProperty()
                .bind(inputEmail.textProperty().isEmpty)

    }

    fun onChangeEmailAction() {
        val result = BlockingAction.actionNoResult(controllerWindow) {
            accountClient.changeEmail(inputCode.text, inputCodeOld.text)
        }

        if (result.isError) {
            val err = result.error
            if (err is ApiError) {
                if (err.statusCode == 400) {
                    showWarningDialog(
                            "Изменение email",
                            "Код не принят",
                            "Проверьте правильность ввода кода",
                            controllerWindow,
                            Modality.WINDOW_MODAL
                    )
                    return
                }
            }
            showWarningDialog(
                    "Изменение email",
                    "Код не принят",
                    result.error.message ?: "",
                    controllerWindow,
                    Modality.WINDOW_MODAL)
            return
        }

        data.result = inputEmail.text
        showInfoDialog(
                "Изменение email",
                "Email успешно изменен",
                "Новый email: ${inputEmail.text}",
                controllerWindow,
                Modality.WINDOW_MODAL
        )
        controllerWindow.close()
    }

    private fun sendCode(): Boolean {
        val result = BlockingAction.actionNoResult(controllerWindow) {
            accountClient.sendCodeToChangeEmail(email)
        }

        if (result.isError) {
            val err = result.error
            if (err is ApiError) {
                if (err.statusCode == 406) {
                    showWarningDialog(
                            "Изменение email",
                            "Код смены email не отправлен",
                            "Указанный email уже зарегистрирован в системе",
                            controllerWindow,
                            Modality.WINDOW_MODAL
                    )
                    return false
                }
            }
            showWarningDialog(
                    "Изменение email",
                    "Код смены email не отправлен",
                    result.error.message ?: "",
                    controllerWindow,
                    Modality.WINDOW_MODAL)
            return false
        }
        resendLink.disableProperty().value = true
        timer.schedule(resendShowTask, 20000)
        showInfoDialog(
                "Изменение email",
                "Коды отправлены на два почтовых ящика",
                "Старый email: $email. Новый email: ${inputEmail.text}",
                controllerWindow,
                Modality.WINDOW_MODAL
        )
        return true
    }

    fun onNextAction() {
        if(sendCode()){
            emailBox.visibleProperty().value = false
            codeBox.visibleProperty().value = true
        }
    }

    private val resendShowTask: TimerTask = object : TimerTask() {
        override fun run() {
            resendLink.disableProperty().value = false
        }
    }

    fun onResend() {
        sendCode()
    }


    companion object {
        private val log by LoggerDelegate()

        /**
         * Выполнится подтверждение указанной почты( старая )
         * Ввод новой почты
         * Проверка новой почты, после передачи кода если все ок, то почта будет изменена
         * Почта проверяется на уникальность.
         *
         */
        @JvmStatic
        fun showChangeEmailDialog(context: Stage, email: String): Optional<String> {
            val result: Data = try {
                openDialogUserData(
                        context,
                        "/fxml/ChangeEmailDialog.fxml",
                        "Изменение email",
                        true,
                        StageStyle.UTILITY,
                        0, 0, 0, 0,
                        Data()
                )
            } catch (e: Exception) {
                log.error("Ошибка открытия диалога аккаунта", e)
                throw RuntimeException(e)
            }

            return if (result.result.isEmpty()) Optional.empty()
            else Optional.of(result.result)
        }
    }

    class Data {
        var result: String = ""
    }
}
