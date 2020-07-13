package ru.biomedis.biomedismair3.social.account

import javafx.application.Platform
import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.WritableBooleanValue
import javafx.beans.value.WritableStringValue
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
    private lateinit var nameText: Label

    @FXML
    private lateinit var emailText: Label

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



    private val log by LoggerDelegate()

    private lateinit var accountClient: AccountClient

    private lateinit var account:AccountView
    private lateinit var progressAPI: ProgressAPI

    override fun setParams(vararg params: Any?) {

    }

    override fun onClose(event: WindowEvent) {
        unBindForm()
        removeOnChangeForm()
    }
    //смена почты отдельной кнопкой? Подтверждени почты в отдельное окно и контроллер(поправить логин)
    //добавить поля по врачам, складам итп
    // для партнерства - выкл просто, а вкл с проверкой
    override fun onCompletedInitialization() {
        changeBtn.disableProperty().bind(
                firstNameInput.textProperty().isEmpty
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

            val result: Result<AccountView> = BlockingAction.actionResult(controllerWindow) {
                accountClient.getAccount(SocialClient.INSTANCE.token.get().userId)
            }

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
                fillAccount(result.value)
                account = result.value
                onChangeForm()
            }



        }
    }

    private fun fillAccount(value: AccountView) = account.apply {
        about = value.about
        skype = value.skype
        surname = value.surname
        name = value.name
        login = value.login
        email = value.email
        country = value.country
        city = value.city
        depot = value.depot
        bris = value.bris
        doctor = value.doctor
        partner = value.partner
    }



    override fun initialize(location: URL, resources: ResourceBundle) {
       res = resources
        accountClient = SocialClient.INSTANCE.accountClient
        progressAPI = AppController.getProgressAPI()
        textFieldUtil = TextFieldUtil(
                mapOf(

                        "firstName" to firstNameInput,
                        "lastName" to lastNameInput,
                        "country" to countryInput,
                        "city" to cityInput,
                        "skype" to skypeInput,
                        "about" to aboutInput
                ),
                mapOf(

                        "firstName" to "Фамилия",
                        "lastName" to "Имя",
                        "country" to "Страна",
                        "city" to "Город",
                        "skype" to "Skype",
                        "about" to "О себе"),
                controllerWindow,
                "редактирование профиля"
        )


        bindForm()

    }

    private fun bindForm() {
        skypeInput.textProperty().bindBidirectional(account.aboutProperty())
        aboutInput.textProperty().bindBidirectional(account.aboutProperty())
        firstNameInput.textProperty().bindBidirectional(account.nameProperty())
        lastNameInput.textProperty().bindBidirectional(account.surnameProperty())
        cityInput.textProperty().bindBidirectional(account.cityProperty())
        countryInput.textProperty().bindBidirectional(account.countryProperty())
        depotInput.selectedProperty().bindBidirectional(account.depotProperty())
        doctorInput.selectedProperty().bindBidirectional(account.doctorProperty())
        diagnostInput.selectedProperty().bindBidirectional(account.brisProperty())
        partnerInput.selectedProperty().bindBidirectional(account.brisProperty())

    }

    private val skypeOnChangeListener = ChangeListener<String> {
        _, oldValue, newValue -> textOnChange(account.skypeProperty(),oldValue, newValue){ accountClient.setSkype(it) }
    }

    private val aboutOnChangeListener = ChangeListener<String> {
        _, oldValue, newValue -> textOnChange(account.aboutProperty(), oldValue, newValue) { accountClient.setAbout(it)}
    }

    private val firstNameOnChangeListener = ChangeListener<String> {
        _, oldValue, newValue -> textOnChange(account.nameProperty(), oldValue, newValue)  { accountClient.setFirstName(it)}
    }

    private val lastNameOnChangeListener = ChangeListener<String> {
        _, oldValue, newValue -> textOnChange(account.surnameProperty(), oldValue, newValue) { accountClient.setLastName(it)}
    }

    private val cityOnChangeListener = ChangeListener<String> {
        _, oldValue, newValue -> textOnChange(account.cityProperty(), oldValue, newValue) { accountClient.setCity(it)}
    }

    private val countryOnChangeListener = ChangeListener<String> {
        _, oldValue, newValue -> textOnChange(account.countryProperty(), oldValue, newValue)  { accountClient.setCountry(it)}
    }

    private val depotOnChangeListener = ChangeListener<Boolean> {
        _, oldValue, newValue -> boolOnChange( account.depotProperty(),oldValue, newValue)  { accountClient.setDepot(it)}
    }

    private val diagnostOnChangeListener = ChangeListener<Boolean> {
        _, oldValue, newValue -> boolOnChange(account.brisProperty(), oldValue, newValue)  { accountClient.setBris(it)}
    }

    private val partnerOnChangeListener = ChangeListener<Boolean> {
        _, oldValue, newValue -> boolOnChange(account.partnerProperty(), oldValue, newValue)  { accountClient.setPartner(it)}
    }

    private val doctorOnChangeListener = ChangeListener<Boolean> {
        _, oldValue, newValue -> boolOnChange( account.doctorProperty(),oldValue, newValue)  { accountClient.setDoctor(it)}
    }

    private fun onChangeForm() {
        skypeInput.textProperty().addListener(skypeOnChangeListener)
        aboutInput.textProperty().addListener(aboutOnChangeListener)
        firstNameInput.textProperty().addListener(firstNameOnChangeListener)
        lastNameInput.textProperty().addListener(lastNameOnChangeListener)
        cityInput.textProperty().addListener(cityOnChangeListener)
        countryInput.textProperty().addListener(countryOnChangeListener)
        depotInput.selectedProperty().addListener(depotOnChangeListener)
        doctorInput.selectedProperty().addListener(doctorOnChangeListener)
        diagnostInput.selectedProperty().addListener(diagnostOnChangeListener)
        partnerInput.selectedProperty().addListener(partnerOnChangeListener)

    }

    private fun removeOnChangeForm() {
        skypeInput.textProperty().removeListener(skypeOnChangeListener)
        aboutInput.textProperty().removeListener(aboutOnChangeListener)
        firstNameInput.textProperty().removeListener(firstNameOnChangeListener)
        lastNameInput.textProperty().removeListener(lastNameOnChangeListener)
        cityInput.textProperty().removeListener(cityOnChangeListener)
        countryInput.textProperty().removeListener(countryOnChangeListener)
        depotInput.selectedProperty().removeListener(depotOnChangeListener)
        doctorInput.selectedProperty().removeListener(doctorOnChangeListener)
        diagnostInput.selectedProperty().removeListener(diagnostOnChangeListener)
        partnerInput.selectedProperty().removeListener(partnerOnChangeListener)

    }

    private fun checkError(result: Result<*>): Boolean{
       if(!result.isError) return false

        showErrorDialog(
                "Изменение данных",
                "",
                "Значение поле не сохранено",
                controllerWindow,
                Modality.WINDOW_MODAL
        )
        log.error("",result.error)
        return true
    }

    private var canceledStringProperty: WritableStringValue? = null
    private var canceledBoolProperty: WritableBooleanValue? = null

    private fun textOnChange(prop: StringProperty, oldValue: String, newValue: String, action: (String)->Unit){
        if(oldValue == newValue) return
        if(canceledStringProperty === prop) {
            canceledStringProperty = null
            return
        }

        val result: Result<Void> = BlockingAction.actionNoResult(controllerWindow) { action(newValue) }
        if(checkError(result)){
            canceledStringProperty = prop
           Platform.runLater { prop.value = oldValue }
        }
    }



    private fun boolOnChange(prop: BooleanProperty, oldValue: Boolean, newValue: Boolean, action: (Boolean)->Unit){
        if(oldValue == newValue) return
        if(canceledBoolProperty === prop) {
            canceledBoolProperty = null
            return
        }

        val result: Result<Void> = BlockingAction.actionNoResult(controllerWindow) { action(newValue) }
        if(checkError(result)){
            canceledBoolProperty = prop
            Platform.runLater { prop.value = oldValue }
        }
    }


    private fun unBindForm() {
        skypeInput.textProperty().unbind()
        aboutInput.textProperty().unbind()
        firstNameInput.textProperty().unbind()
        lastNameInput.textProperty().unbind()
        cityInput.textProperty().unbind()
        countryInput.textProperty().unbind()
        depotInput.selectedProperty().unbind()
        doctorInput.selectedProperty().unbind()
        diagnostInput.selectedProperty().unbind()
        partnerInput.selectedProperty().unbind()

    }


    fun onChangePassword() {
        //нельзя сбросить пароль любому человеку, тк сброс будет при подтверждении кода
        openRestoreDialog(controllerWindow, account.email, true)
    }

    fun onChangeEmail() {
       val emailNew =  ChangeEmailController.showChangeEmailDialog(controllerWindow, account.email)
        if(!emailNew.isPresent) return

        account.email = emailNew.get()


    }

    fun onChangeName() {
       val newName = ChangeNameController.showChangeNameDialog(controllerWindow, account.name)
        if(!newName.isPresent) return
        account.name = newName.get()
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
