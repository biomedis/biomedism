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

    private lateinit var reqMap:Map<String, Control>

    private lateinit var fieldsNameMap:Map<String, String>

    @FXML
    private lateinit var root: VBox
    private lateinit var registrationClient: RegistrationClient
    private lateinit var res: ResourceBundle
    private lateinit var progressAPI: ProgressAPI
    private  var closedByAction = false

    private val log by LoggerDelegate()

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

        fieldsNameMap = mapOf(
        "userName" to "Имя(псевдоним) пользователя",
        "password" to "Пароль",
        "email" to "Email",
        "firstName" to "Фамилия",
        "lastName" to "Имя",
        "country" to "Страна",
        "city" to "Город",
        "skype" to "Skype",
        "about" to "О себе"
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
        reqMap = mapOf(
                "userName" to nameInput,
                "password" to passwordInput,
                "email" to emailInput,
                "firstName" to firstNameInput,
                "lastName" to lastNameInput,
                "country" to countryInput,
                "city" to cityInput,
                "skype" to skypeInput,
                "about" to aboutInput
        )
    }

    private fun checkFieldLength(field: TextInputControl, msg: String, minLength: Int, maxLength: Int):Boolean{
        return if( nameInput.text.trim().length < minLength ||  nameInput.text.trim().length > maxLength){
            showWarningDialog(
                    "Регистрация",
                    "",
                    msg,
                    controllerWindow,
                    Modality.WINDOW_MODAL)
            setErrorField(field)
            false
        }else true
    }


    fun onRegistrationAction() {

        if (!emailInput.text.trim().matches("^[\\w-_.+]*[\\w-_.]@([\\w]+\\.)+[\\w]+[\\w]$".toRegex())) {
            showWarningDialog(
                    "Регистрация",
                    "",
                    "Введен не корректный email!",
                    controllerWindow,
                    Modality.WINDOW_MODAL)
            setErrorField(emailInput)
            return
        }
        if(!checkFieldLength(passwordInput, "Пароль должен быть не менее 5 и не более 255 символов", 5, 255)) return

        if(!checkFieldLength(nameInput, "Псевдоним пользователя не должно быть пустым и быть более 255 символов", 1, 255)) return

        if(!checkFieldLength(aboutInput, "Описание должно быть не более 16535 символов", 0, 16535)) return

        if(!checkFieldLength(firstNameInput, "Имя пользователя не должно быть пустым и быть более 255 символов", 1, 255)) return

        if(!checkFieldLength(lastNameInput, "Фамилия пользователя не должна быть пустой и быть более 255 символов", 1, 255)) return

        if(!checkFieldLength(countryInput, "Страна пользователя не должно быть пустой и быть более 255 символов", 1, 255)) return

        if(!checkFieldLength(cityInput, "Город пользователя не должен быть пустым и быть более 255 символов", 1, 255)) return

        if(!checkFieldLength(skypeInput, "Логин в Skype пользователя не должен быть более 255 символов", 0, 255)) return


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
                    processValidationError(ValidationErrorProcessor.process(e))
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


    private fun setErrorField(node: Control){
        if (!node.styleClass.contains("error_border")) {
            node.styleClass.add("error_border")
        }
    }

    private fun setSuccessField(node: Control){
        if (node.styleClass.contains("error_border")) {
            node.styleClass.remove("error_border")
        }
    }

    private fun hideValidationMessages() =  root.children.forEach {
        it.styleClass.remove("error_border")
    }


    private fun processValidationError(errorMessages: List<ApiValidationError>) {
        val strb = StringBuilder()
        reqMap.forEach { u -> setSuccessField(u.value) }

        errorMessages.forEach { e: ApiValidationError ->
            if(e.field in reqMap){
                setErrorField(reqMap[e.field]!!)
                strb.append(fieldsNameMap[e.field]).append(": ").append(e.message).append("\n")
            }else{
                strb.append(e.field).append(": ").append("Неизвестное поле. ").append(e.message).append("\n")
            }
        }
        showWarningDialog(
                "Валидация полей формы",
                "Некоторые поля имеют некорректное содержимое",
                strb.toString(),
                controllerWindow,
                Modality.WINDOW_MODAL)
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
                log.error("Ошибка открытия диалога входа", e)
                throw RuntimeException(e)
            }
        }
    }
}
