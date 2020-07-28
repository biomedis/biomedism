package ru.biomedis.biomedismair3.social.login

import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.stage.WindowEvent
import lombok.extern.slf4j.Slf4j
import ru.biomedis.biomedismair3.AppController
import ru.biomedis.biomedismair3.BaseController
import ru.biomedis.biomedismair3.BlockingAction
import ru.biomedis.biomedismair3.Layouts.ProgressPanel.ProgressAPI
import ru.biomedis.biomedismair3.social.TextFieldUtil
import ru.biomedis.biomedismair3.social.remote_client.RegistrationClient
import ru.biomedis.biomedismair3.social.remote_client.SocialClient
import ru.biomedis.biomedismair3.social.remote_client.ValidationErrorProcessor
import ru.biomedis.biomedismair3.social.remote_client.dto.RegistrationDto
import ru.biomedis.biomedismair3.social.remote_client.dto.error.ApiError
import ru.biomedis.biomedismair3.social.remote_client.dto.error.ApiValidationError
import ru.biomedis.biomedismair3.utils.Other.LoggerDelegate
import java.net.URL
import java.util.*

/**
 * Осуществляет регистрацию пользователя в сервисе MAir
 */
@Slf4j
class RegistrationController : BaseController() {
    @FXML
    private lateinit var registrationBtn: Button

    @FXML
    private lateinit var nameInput: TextField

    @FXML
    private lateinit var emailInput: TextField

    @FXML
    private lateinit var passwordInput: TextField

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

    //private lateinit var reqMap:Map<String, Control>

    // private lateinit var fieldsNameMap:Map<String, String>

    @FXML
    private lateinit var root: VBox
    private lateinit var registrationClient: RegistrationClient
    private lateinit var res: ResourceBundle
    private lateinit var progressAPI: ProgressAPI
    private var closedByAction = false

    private val log by LoggerDelegate()

    private lateinit var textFieldUtil: TextFieldUtil

    override fun onCompletedInitialization() {
        registrationBtn.disableProperty().bind(
                nameInput.textProperty().isEmpty
                        .or(emailInput.textProperty().isEmpty)
                        .or(passwordInput.textProperty().isEmpty)
                        .or(firstNameInput.textProperty().isEmpty)
                        .or(lastNameInput.textProperty().isEmpty)
                        .or(countryInput.textProperty().isEmpty)
                        .or(cityInput.textProperty().isEmpty)
        )


    }

    override fun onClose(event: WindowEvent) {
        if (!closedByAction) root.userData = Optional.empty<Any>()
    }

    override fun setParams(vararg params: Any) {}

    override fun initialize(location: URL, resources: ResourceBundle) {
        res = resources
        registrationClient = SocialClient.INSTANCE.registrationClient
        progressAPI = AppController.getProgressAPI()

        textFieldUtil = TextFieldUtil(
                mapOf(
                        "userName" to nameInput,
                        "password" to passwordInput,
                        "email" to emailInput,
                        "firstName" to firstNameInput,
                        "lastName" to lastNameInput,
                        "country" to countryInput,
                        "city" to cityInput,
                        "skype" to skypeInput,
                        "about" to aboutInput
                ),
                mapOf(
                        "userName" to "Имя(псевдоним) пользователя",
                        "password" to "Пароль",
                        "email" to "Email",
                        "firstName" to "Фамилия",
                        "lastName" to "Имя",
                        "country" to "Страна",
                        "city" to "Город",
                        "skype" to "Skype",
                        "about" to "О себе"),
                controllerWindow,
                "Регистрация"
        )


    }




    fun onRegistrationAction() {

        if (!textFieldUtil.checkEmailField(emailInput)) return

        if (!textFieldUtil.checkFieldLength(passwordInput, "Пароль должен быть не менее 5 и не более 255 символов", 5, 255)) return

        if (!textFieldUtil.checkFieldLength(nameInput, "Псевдоним пользователя не должно быть пустым и быть более 255 символов", 1, 255)) return

        if (!textFieldUtil.checkFieldLength(aboutInput, "Описание должно быть не более 16535 символов", 0, 16535)) return

        if (!textFieldUtil.checkFieldLength(firstNameInput, "Имя пользователя не должно быть пустым и быть более 255 символов", 1, 255)) return

        if (!textFieldUtil.checkFieldLength(lastNameInput, "Фамилия пользователя не должна быть пустой и быть более 255 символов", 1, 255)) return

        if (!textFieldUtil.checkFieldLength(countryInput, "Страна пользователя не должно быть пустой и быть более 255 символов", 1, 255)) return

        if (!textFieldUtil.checkFieldLength(cityInput, "Город пользователя не должен быть пустым и быть более 255 символов", 1, 255)) return

        if (!textFieldUtil.checkFieldLength(skypeInput, "Логин в Skype пользователя не должен быть более 255 символов", 0, 255)) return


        val dto = RegistrationDto()
        dto.userName = nameInput.text.trim()
        dto.email = emailInput.text.trim()
        dto.about = aboutInput.text.trim()
        dto.firstName = firstNameInput.text.trim()
        dto.lastName = lastNameInput.text.trim()
        dto.city = cityInput.text.trim()
        dto.country = countryInput.text.trim()
        dto.skype = skypeInput.text.trim()
        dto.password = passwordInput.text.trim()
        val result = BlockingAction.actionNoResult(controllerWindow) { registration(dto) }
        if (!result.isError) {
            root.userData = Optional.of(emailInput.text.trim())
            closedByAction = true
            controllerWindow.close()
            return
        }

        if (result.error is ApiError) {
            val e = result.error as ApiError
            when {
                e.statusCode == 406 -> {
                    when (e.debugMessage) {
                        "email" -> showWarningDialog("Регистрация",
                                "Регистрация не удалась",
                                "Аккаунт с указанным email уже существует",
                                root.scene.window,
                                Modality.WINDOW_MODAL)
                        "userName" -> showWarningDialog("Регистрация",
                                "Регистрация не удалась",
                                "Аккаунт с указанным именем уже существует",
                                root.scene.window,
                                Modality.WINDOW_MODAL)
                        else -> {
                            showWarningDialog("Регистрация",
                                    "Регистрация не удалась",
                                    e.message!!,
                                    root.scene.window,
                                    Modality.WINDOW_MODAL)
                            log.error("Регистрация не удалась", e.message)
                        }
                    }
                }
                e.isValidationError -> {
                    textFieldUtil.processValidationError(ValidationErrorProcessor.process(e))
                }

                else -> {
                    log.error("", e)
                    showExceptionDialog("Регистрация",
                            "Регистрация не удалась",
                            "Ошибка на стороне сервера. ",
                            e,
                            root.scene.window,
                            Modality.WINDOW_MODAL)
                }
            }
        } else {
            log.error("", result.error)
            showExceptionDialog("Регистрация",
                    "Регистрация не удалась",
                    "",
                    Exception(result.error),
                    root.scene.window,
                    Modality.WINDOW_MODAL)
        }
    }




    private fun hideValidationMessages() = root.children.forEach {
        it.styleClass.remove("error_border")
    }

    private fun registration(dto: RegistrationDto) {
        registrationClient.registration(dto)
    }

    companion object {
        private val log by LoggerDelegate()

        /**
         * Запускает окно регистрации
         * @param context окно в контексте которго запускается окно регистрации
         * @return строка email или пусто если просто закрыли
         */
        @JvmStatic
        fun performRegistration(context: Stage): Optional<String> {
            return try {
                openDialogUserData(
                        context,
                        "/fxml/RegistrationDialog.fxml",
                        "Регистрация",
                        true,
                        StageStyle.UTILITY,
                        0, 0, 0, 0,
                        Optional.empty()
                )
            } catch (e: Exception) {
                log.error("Ошибка открытия диалога регистрации", e)
                throw RuntimeException(e)
            }
        }
    }
}
