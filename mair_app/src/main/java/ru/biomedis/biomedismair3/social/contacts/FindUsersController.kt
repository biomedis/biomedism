package ru.biomedis.biomedismair3.social.contacts

import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.stage.*
import javafx.util.StringConverter
import ru.biomedis.biomedismair3.BaseController
import ru.biomedis.biomedismair3.BlockingAction
import ru.biomedis.biomedismair3.social.remote_client.AccountClient
import ru.biomedis.biomedismair3.social.remote_client.RegistrationClient
import ru.biomedis.biomedismair3.social.remote_client.SocialClient
import ru.biomedis.biomedismair3.social.remote_client.dto.CityDto
import ru.biomedis.biomedismair3.social.remote_client.dto.CountryDto
import ru.biomedis.biomedismair3.utils.Other.LoggerDelegate
import java.net.URL
import java.util.*

class FindUsersController : BaseController() {

    private lateinit var addedUsers: AddedUsers
    @FXML
    private lateinit var foundList: ListView<*>
    @FXML
    private lateinit var firstNameInput: TextField
    @FXML
    private lateinit var lastNameInput: TextField
    @FXML
    private lateinit var skypeInput: TextField
    @FXML
    private lateinit var countryInput: ComboBox<CountryDto>
    @FXML
    private lateinit var cityInput: ComboBox<CityDto>
    @FXML
    private lateinit var aboutInput: TextArea

    @FXML
    private lateinit var doctorInput: CheckBox
    @FXML
    private lateinit var diagnostInput: CheckBox
    @FXML
    private lateinit var depotInput: CheckBox
    @FXML
    private lateinit var partnerInput: CheckBox

    private lateinit  var registrationClient: RegistrationClient
    private lateinit  var accountClient: AccountClient

    override fun onCompletedInitialization() {
        addedUsers = inputDialogData as AddedUsers
        Platform.runLater {
          countryInput.items.addAll(getCountries())
        }
    }

    override fun onClose(event: WindowEvent) {

    }

    override fun setParams(vararg params: Any) {

    }

    override fun initialize(location: URL, resources: ResourceBundle) {
         registrationClient = SocialClient.INSTANCE.registrationClient
         accountClient = SocialClient.INSTANCE.accountClient

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

    private fun getCountries():List<CountryDto>{
        val result = BlockingAction.actionResult<List<CountryDto>>(controllerWindow) {
            registrationClient.countriesList()
        }.map {
            it.sortedBy { country -> country.name }
        }
        return if(result.isError){
            log.error("", result.error)
            showWarningDialog(
                    "Не удалось получить список стран",
                    "",
                    "Проверьте интернет-соединение или попробуйте позже",
                    controllerWindow,
                    Modality.WINDOW_MODAL)
            listOf()
        }else result.value

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
    companion object {
        private val log by LoggerDelegate()

        /**
         * Поиск пользователей.
         * Вернет список добавленных в контакты пользователей
         */
        @JvmStatic
        fun showFindUserDialog(context: Stage): List<AccountSmallView> {
            val users = AddedUsers()
            return try {
                openDialogUserData<AddedUsers>(
                        context,
                        "/fxml/social/FindUsersDialog.fxml",
                        "Поиск пользователей",
                        true,
                        StageStyle.UTILITY,
                        (Screen.getPrimary().bounds.height * 0.9).toInt(), 0, 0, 0,
                        users
                ).users
            } catch (e: Exception) {
                log.error("Ошибка открытия диалога поиска пользователей", e)
                throw RuntimeException(e)
            }
        }
    }

    class AddedUsers {
        val users = mutableListOf<AccountSmallView>()
    }
}
