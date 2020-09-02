package ru.biomedis.biomedismair3.social.login

import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.stage.WindowEvent
import javafx.util.StringConverter
import lombok.extern.slf4j.Slf4j
import ru.biomedis.biomedismair3.AppController
import ru.biomedis.biomedismair3.BaseController
import ru.biomedis.biomedismair3.BlockingAction
import ru.biomedis.biomedismair3.Layouts.ProgressPanel.ProgressAPI
import ru.biomedis.biomedismair3.social.TextFieldUtil
import ru.biomedis.biomedismair3.social.remote_client.AccountClient
import ru.biomedis.biomedismair3.social.remote_client.RegistrationClient
import ru.biomedis.biomedismair3.social.remote_client.SocialClient
import ru.biomedis.biomedismair3.social.remote_client.ValidationErrorProcessor
import ru.biomedis.biomedismair3.social.remote_client.dto.CityDto
import ru.biomedis.biomedismair3.social.remote_client.dto.CountryDto
import ru.biomedis.biomedismair3.social.remote_client.dto.RegistrationDto
import ru.biomedis.biomedismair3.social.remote_client.dto.error.ApiError
import ru.biomedis.biomedismair3.social.remote_client.dto.error.ApiValidationError
import ru.biomedis.biomedismair3.utils.Other.LoggerDelegate
import ru.biomedis.biomedismair3.utils.Other.Result
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
    private lateinit var countryInput: ComboBox<CountryDto>

    @FXML
    private lateinit var cityInput: ComboBox<CityDto>

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
        data = inputDialogData as Data
       Platform.runLater {
           BlockingAction.actionResult<List<CountryDto>>(controllerWindow) {
               registrationClient.countriesList()
           }.map{
               it.sortedBy { country->country.name }
           }.action({
               e->
               log.error("", e)
               showWarningDialog(
                       "Не удалось получить список стран",
                       "",
                       "Проверьте интернет-соединение или попробуйте позже",
                       controllerWindow,
                       Modality.WINDOW_MODAL)

               controllerWindow.close()
           }){
               countries ->countryInput.items.addAll(countries)
           }
       }


        registrationBtn.disableProperty().bind(
                nameInput.textProperty().isEmpty
                        .or(emailInput.textProperty().isEmpty)
                        .or(passwordInput.textProperty().isEmpty)
                        .or(firstNameInput.textProperty().isEmpty)
                        .or(lastNameInput.textProperty().isEmpty)
                        .or(countryInput.selectionModel.selectedItemProperty().isNull)
                        .or(cityInput.selectionModel.selectedItemProperty().isNull)
        )


    }

    override fun onClose(event: WindowEvent) {
        if (!closedByAction) data.email=null
    }

    override fun setParams(vararg params: Any) {}

    private lateinit var data: Data
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
                        "skype" to skypeInput,
                        "about" to aboutInput
                ),
                mapOf(
                        "userName" to "Имя(псевдоним) пользователя",
                        "password" to "Пароль",
                        "email" to "Email",
                        "firstName" to "Фамилия",
                        "lastName" to "Имя",
                        "skype" to "Skype",
                        "about" to "О себе"),
                controllerWindow,
                "Регистрация"
        )

        countryInput.converter = object : StringConverter<CountryDto>() {
            override fun toString(value: CountryDto?): String =  value?.name?:""
            override fun fromString(string: String?): CountryDto = throw NotImplementedError()
        }
        cityInput.converter = object : StringConverter<CityDto>() {
            override fun toString(value: CityDto?): String =  value?.name?:""
            override fun fromString(string: String?): CityDto = throw NotImplementedError()
        }


        countryInput.selectionModel.selectedItemProperty().addListener{_, oldValue, newValue ->
            if(oldValue===newValue) return@addListener
            cityInput.items.apply {
                clear()
                addAll(getCities(newValue.id))

            }
            cityInput.selectionModel.clearSelection()

        }
    }

   private fun getCities(country: Long): List<CityDto>{
     val result =   BlockingAction.actionResult<List<CityDto>>(controllerWindow) {
           registrationClient.citiesList(country)
       }.map{
           it.sortedBy { city->city.name }
       }

       return if(result.isError){
           log.error("", result.error)
           showWarningDialog(
                   "Не удалось получить список городов",
                   "",
                   "Проверьте интернет-соединение или попробуйте позже",
                   controllerWindow,
                   Modality.WINDOW_MODAL)
           listOf()

       }else{
           result.value
       }
   }


    fun onRegistrationAction() {

        if (!textFieldUtil.checkEmailField(emailInput)) return

        if (!textFieldUtil.checkFieldLength(passwordInput, "Пароль должен быть не менее 5 и не более 255 символов", 5, 255)) return

        if (!textFieldUtil.checkFieldLength(nameInput, "Псевдоним пользователя не должно быть пустым и быть более 255 символов", 1, 255)) return

        if (!textFieldUtil.checkFieldLength(aboutInput, "Описание должно быть не более 16535 символов", 0, 16535)) return

        if (!textFieldUtil.checkFieldLength(firstNameInput, "Имя пользователя не должно быть пустым и быть более 255 символов", 1, 255)) return

        if (!textFieldUtil.checkFieldLength(lastNameInput, "Фамилия пользователя не должна быть пустой и быть более 255 символов", 1, 255)) return

        if (!textFieldUtil.checkFieldLength(skypeInput, "Логин в Skype пользователя не должен быть более 255 символов", 0, 255)) return


        val dto = RegistrationDto()
        dto.userName = nameInput.text.trim()
        dto.email = emailInput.text.trim()
        dto.about = aboutInput.text.trim()
        dto.firstName = firstNameInput.text.trim()
        dto.lastName = lastNameInput.text.trim()
        dto.city = cityInput.selectionModel.selectedItem.id
        dto.country = countryInput.selectionModel.selectedItem.id
        dto.skype = skypeInput.text.trim()
        dto.password = passwordInput.text.trim()
        val result = BlockingAction.actionNoResult(controllerWindow) { registration(dto) }
        if (!result.isError) {
            data.email = emailInput.text.trim()
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
                        Data()
                ).email?.let {
                    Optional.of(it)
                }?: Optional.empty()
            } catch (e: Exception) {
                log.error("Ошибка открытия диалога регистрации", e)
                throw RuntimeException(e)
            }
        }
    }

    class Data{
        var email: String?=null
    }
}
