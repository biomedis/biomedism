package ru.biomedis.biomedismair3.social.account

import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.Hyperlink
import javafx.scene.control.TextField
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

class ChangeNameController : BaseController() {
    private val log by LoggerDelegate()
    @FXML
    private lateinit var inputName: TextField

    @FXML
    private lateinit var actionBtn: Button

    private lateinit var name: String
    private lateinit var res: ResourceBundle

    private val accountClient: AccountClient by lazy { SocialClient.INSTANCE.accountClient }

    private lateinit var data: Data

    override fun setParams(vararg params: Any) {
        if (params.isEmpty()) throw RuntimeException("Не верные параметры. Должен быть параметр name")
        name = params[0] as String
    }

    override fun onClose(event: WindowEvent) {

    }

    override fun onCompletedInitialization() {
        data = inputDialogData as Data
    }

    override fun initialize(location: URL, resources: ResourceBundle) {
        res = resources
        actionBtn.disableProperty()
                .bind(inputName.textProperty().isEmpty)

    }

    fun onAction() {
        val result = BlockingAction.actionNoResult(controllerWindow) {
            accountClient.changeUserName(inputName.text)
        }

        if (result.isError) {
            val err = result.error
            if (err is ApiError) {
                if (err.statusCode == 406) {
                    showWarningDialog(
                            "Изменение имени",
                            "Имя не изменено",
                            "Указанное имя уже существует в системе.",
                            controllerWindow,
                            Modality.WINDOW_MODAL
                    )
                    return
                }
            }
            log.error("", err)
            showWarningDialog(
                    "Изменение имени",
                    "Имя не изменено",
                    result.error.message ?: "",
                    controllerWindow,
                    Modality.WINDOW_MODAL)
            return
        }

        data.result = inputName.text
        showInfoDialog(
                "Изменение имени",
                "Имя успешно изменено",
                "Новое имя: ${inputName.text}",
                controllerWindow,
                Modality.WINDOW_MODAL
        )
        controllerWindow.close()
    }


    companion object {
        private val log by LoggerDelegate()


        @JvmStatic
        fun showChangeNameDialog(context: Stage, name: String): Optional<String> {
            val result: Data = try {
                openDialogUserData(
                        context,
                        "/fxml/ChangeNameDialog.fxml",
                        "Изменение имени",
                        true,
                        StageStyle.UTILITY,
                        0, 0, 0, 0,
                        Data()
                )
            } catch (e: Exception) {
                log.error("Ошибка открытия диалога изменения имени", e)
                throw RuntimeException(e)
            }

            return if (result.result == name) Optional.empty()
            else Optional.of(result.result)
        }
    }

    class Data {
        var result: String = ""
    }
}
