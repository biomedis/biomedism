package ru.biomedis.biomedismair3.social.account

import javafx.application.Platform
import javafx.beans.property.BooleanProperty
import javafx.beans.property.StringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.transformation.FilteredList
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.input.InputEvent
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.stage.*
import javafx.util.StringConverter
import ru.biomedis.biomedismair3.AppController
import ru.biomedis.biomedismair3.BaseController
import ru.biomedis.biomedismair3.BlockingAction
import ru.biomedis.biomedismair3.Layouts.ProgressPanel.ProgressAPI
import ru.biomedis.biomedismair3.social.TextFieldUtil
import ru.biomedis.biomedismair3.social.login.RestorePasswordController.Companion.openRestoreDialog
import ru.biomedis.biomedismair3.social.remote_client.AccountClient
import ru.biomedis.biomedismair3.social.remote_client.SocialClient
import ru.biomedis.biomedismair3.social.remote_client.dto.CityDto
import ru.biomedis.biomedismair3.social.remote_client.dto.CountryDto
import ru.biomedis.biomedismair3.social.remote_client.dto.error.ApiError
import ru.biomedis.biomedismair3.social.social_panel.SocialPanelAPI
import ru.biomedis.biomedismair3.utils.Other.LoggerDelegate
import ru.biomedis.biomedismair3.utils.Other.Result
import java.net.URL
import java.util.*

class AccountController : BaseController() {

    private lateinit var textFieldUtil: TextFieldUtil

    @FXML
    private lateinit var showEmailCb: CheckBox

    @FXML
    private lateinit var showRealNameCb: CheckBox

    @FXML
    private lateinit var showSkypeCb: CheckBox


    @FXML
    private lateinit var activeSessionsTab: Tab

    @FXML
    private lateinit var removeSelectedBtn: Button

    @FXML
    private lateinit var removeAllBtn: Button

    @FXML
    private lateinit var sessionsList: ListView<ActiveSession>

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
    private lateinit var countryInput: ComboBox<CountryDto>

    @FXML
    private lateinit var cityInput: ComboBox<CityDto>

    @FXML
    private lateinit var skypeInput: TextField

    @FXML
    private lateinit var aboutInput: TextArea


    @FXML
    private lateinit var root: TabPane
    private lateinit var res: ResourceBundle


    private val log by LoggerDelegate()

    private lateinit var accountClient: AccountClient

    private lateinit var account: AccountView
    private lateinit var progressAPI: ProgressAPI
    private lateinit var socialPanelAPI: SocialPanelAPI


    override fun setParams(vararg params: Any) {
        val accountView: AccountView? = params[0] as? AccountView
        if(accountView == null) {
            log.error("НЕ передан параметр account")
            throw RuntimeException("Следует передать параметр account")

        }
        account = accountView

    }

    override fun onClose(event: WindowEvent) {
        removeEventHandlers()
    }

    private fun removeEventHandlers() {
        eventHandlers.forEach {
            if (it.first is TextInputControl) {
                it.first.removeEventFilter(KeyEvent.KEY_PRESSED, it.second as EventHandler<in KeyEvent>)
            } else {
                it.first.removeEventFilter(MouseEvent.MOUSE_CLICKED, it.second as EventHandler<in MouseEvent>)
            }
        }
        eventHandlers.clear()
    }

    //смена почты отдельной кнопкой? Подтверждени почты в отдельное окно и контроллер(поправить логин)
    //добавить поля по врачам, складам итп
    // для партнерства - выкл просто, а вкл с проверкой
    override fun onCompletedInitialization() {
        fillFields(account)
        Platform.runLater {
            BlockingAction.actionResult<List<CountryDto>>(controllerWindow) {
                SocialClient.INSTANCE.registrationClient.countriesList()
            }.map {
                it.sortedBy { country -> country.name }
            }.action({ e ->
                log.error("", e)
                showWarningDialog(
                        "Не удалось получить список стран",
                        "",
                        "Проверьте интернет-соединение или попробуйте позже",
                        controllerWindow,
                        Modality.WINDOW_MODAL)

                controllerWindow.close()
            }) { countries ->
                countryInput.items.addAll(countries)
                setCityAndCountryFromAccount()
            }

            setCountryAndCityOnChange()
            onChangeForm()
        }

    }

    private fun fillFields(value: AccountView) = account.apply {
        aboutInput.text = value.about
        skypeInput.text = value.skype
        firstNameInput.text = value.name
        lastNameInput.text = value.surname
        nameText.text = value.login
        emailText.text = value.email
        depotInput.isSelected = value.depot
        diagnostInput.isSelected = value.bris
        doctorInput.isSelected = value.doctor
        partnerInput.isSelected = value.partner
        showEmailCb.isSelected = value.showEmail
        showRealNameCb.isSelected = value.showRealName
        showSkypeCb.isSelected = value.showSkype
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

        socialPanelAPI = AppController.getSocialPanelAPI()

        activeSessionsTab.tabPane.selectionModel.selectedItemProperty()
                .addListener { _, oldTab, newTab ->
                    val id = newTab.id ?: ""
                    if (id == activeSessionsTab.id && sessionListSource.isEmpty()) {
                        loadSessions()
                    }
                }
        initTokensList()
        removeSelectedBtn.disableProperty()
                .bind(sessionsList.selectionModel.selectedItemProperty().isNull)

        removeAllBtn.isDisable = true

        countryInput.converter = object : StringConverter<CountryDto>() {
            override fun toString(value: CountryDto?): String = value?.name ?: ""
            override fun fromString(string: String?): CountryDto = throw NotImplementedError()
        }
        cityInput.converter = object : StringConverter<CityDto>() {
            override fun toString(value: CityDto?): String = value?.name ?: ""
            override fun fromString(string: String?): CityDto = throw NotImplementedError()
        }


    }

    private fun getCities(country: Long): List<CityDto> {
        val result = BlockingAction.actionResult<List<CityDto>>(controllerWindow) {
            SocialClient.INSTANCE.registrationClient.citiesList(country)
        }.map {
            it.sortedBy { city -> city.name }
        }

        return if (result.isError) {
            log.error("", result.error)
            showWarningDialog(
                    "Не удалось получить список городов",
                    "",
                    "Проверьте интернет-соединение или попробуйте позже",
                    controllerWindow,
                    Modality.WINDOW_MODAL)
            listOf()

        } else {
            result.value
        }
    }

    private fun loadSessions() {
        val result: Result<List<ActiveSession>> = BlockingAction.actionResult(controllerWindow) {
            accountClient.allTokens()
        }
        if (result.isError) {
            showWarningDialog(
                    "Получение списка сессий",
                    "",
                    "Не удалось получить список сессий, попробуйте позже",
                    controllerWindow,
                    Modality.WINDOW_MODAL)
            log.error("", result.error)
            return
        }

        sessionListSource.clear()
        sessionListSource.addAll(result.value)
        removeAllBtn.isDisable = sessionListObs.isEmpty()
    }

    private val sessionListSource: ObservableList<ActiveSession> = FXCollections.observableArrayList()
    private val sessionListObs: FilteredList<ActiveSession> = FilteredList(sessionListSource) {
        it.id != SocialClient.INSTANCE.token.map { token -> token.id }.orElse(-2)
    }

    private fun initTokensList() {

        sessionsList.selectionModel.selectionMode = SelectionMode.MULTIPLE
        sessionsList.orientation = Orientation.VERTICAL
        sessionsList.placeholder = Label("Нет активных сессий на других устройствах")
        sessionsList.items = sessionListObs
        sessionsList.cellFactory = ActiveSessionCellFactory()
    }

    /**
     * ДЛя передачи фокуса следующему контролу
     */
    private fun tabEventFire(sourceEvent: InputEvent, node: Node) {
        val newEvent = KeyEvent(
                sourceEvent.source,
                sourceEvent.target,
                KeyEvent.KEY_PRESSED,
                "",
                "\t",
                KeyCode.TAB,
                false,
                false,
                false,
                false
        )

        node.fireEvent(newEvent)
    }

    private val eventHandlers: MutableList<Pair<Node, EventHandler<*>>> = mutableListOf()

    private fun textEventHandler(
            control: TextInputControl,
            modelProperty: StringProperty,
            action: (String) -> Unit,
            shiftPressed: Boolean = false
    ): EventHandler<KeyEvent> {
        return EventHandler { event ->
            if (event.code == KeyCode.ENTER && (event.isShiftDown || !shiftPressed)) {
                if (!onChange(modelProperty.value, control.text, action)) {
                    control.text = modelProperty.value
                } else {
                    if (modelProperty.value != control.text) tabEventFire(event, control)
                    modelProperty.value = control.text
                }

                event.consume()
            }
        }

    }


    private fun <T : InputEvent> booleanEventHandler(
            control: CheckBox,
            modelProperty: BooleanProperty,
            action: (Boolean) -> Unit
    ): EventHandler<T> {
        return EventHandler { event ->
            if (event is KeyEvent && !(event.character == "\t" || event.character == "\r")) {
                control.isSelected = !modelProperty.value
            } else if (event is KeyEvent) return@EventHandler

            if (!onChange(modelProperty.value, control.isSelected, action)) {
                control.isSelected = modelProperty.value
            } else {
                modelProperty.value = control.isSelected
                tabEventFire(event, control)
            }
            event.consume()
        }

    }


    private fun setCityAndCountryFromAccount(){
        val index = countryInput.items.indexOfFirst { it.id == account.country.id }
        if (index >= 0) {
            countryInput.selectionModel.select(index)

            cityInput.items.apply {
                clear()
                addAll(getCities(account.country.id))
                val index = indexOfFirst { it.id == account.city.id }
                if (index >= 0) cityInput.selectionModel.select(index)
            }
        }else    {
            countryInput.selectionModel.clearSelection()
            cityInput.selectionModel.clearSelection()
        }
    }


    private fun setCountryAndCityOnChange(){
        cityInput.onAction = cityOnChange
        countryInput.onAction = countryOnChange
    }

    private fun clearCountryAndCityOnChange(){
        cityInput.onAction = null
        countryInput.onAction = null
    }

    private val cityOnChange: EventHandler<ActionEvent> = object : EventHandler<ActionEvent>{
        override fun handle(event: ActionEvent) {
            if (cityInput.selectionModel.selectedItem == null) return

            val result = BlockingAction.actionNoResult(controllerWindow) {
                SocialClient.INSTANCE.accountClient.setCity(cityInput.selectionModel.selectedItem.id)
                SocialClient.INSTANCE.accountClient.setCountry(countryInput.selectionModel.selectedItem.id)
            }

            if (result.isError) {
                clearCountryAndCityOnChange()
                setCityAndCountryFromAccount()
                setCountryAndCityOnChange()
                log.error("", result.error)
                showWarningDialog(
                        "Сохранение страны и города",
                        "",
                        "Не удалось сохранить, повторите позже",
                        controllerWindow,
                        Modality.WINDOW_MODAL
                )

            } else {

                account.city = cityInput.selectionModel.selectedItem
                account.country = countryInput.selectionModel.selectedItem
            }


        }

    }

    private val countryOnChange: EventHandler<ActionEvent> = object : EventHandler<ActionEvent>{
        override fun handle(event: ActionEvent) {
            clearCountryAndCityOnChange()
            if (countryInput.selectionModel.selectedItem != null) {
                cityInput.selectionModel.clearSelection()

                cityInput.items.apply {
                    clear()
                    addAll(getCities(countryInput.selectionModel.selectedItem.id))
                }
            }
            setCountryAndCityOnChange()
        }

    }




    /**
     * [modelProperty] свойство, которое будет обновлено
     * [action] выполнится после нажатия на enter или shift+enter,если [shiftPressed] = true
     */
    private fun setTextEventFilter(control: TextInputControl,
                                   modelProperty: StringProperty,
                                   shiftPressed: Boolean,
                                   action: (String) -> Unit) {

        var eh: EventHandler<KeyEvent> = textEventHandler(control, modelProperty, action, shiftPressed)
        control.addEventFilter(KeyEvent.KEY_PRESSED, eh)
        eventHandlers.add(control to eh)
    }

    private fun setBooleanEventFilter(control: CheckBox,
                                      modelProperty: BooleanProperty,
                                      action: (Boolean) -> Unit) {

        var eh: EventHandler<MouseEvent> = booleanEventHandler(control, modelProperty, action)
        control.addEventFilter(MouseEvent.MOUSE_CLICKED, eh)
        var ehk: EventHandler<KeyEvent> = booleanEventHandler(control, modelProperty, action)
        control.addEventFilter(MouseEvent.MOUSE_CLICKED, eh)
        control.addEventFilter(KeyEvent.KEY_TYPED, ehk)
        eventHandlers.add(control to eh)
        eventHandlers.add(control to ehk)
    }

    private fun onChangeForm() {

        setTextEventFilter(skypeInput, account.skypeProperty(), false) {
            accountClient.setSkype(it)
        }

        setTextEventFilter(firstNameInput, account.nameProperty(), false) {
            accountClient.setFirstName(it)
        }

        setTextEventFilter(lastNameInput, account.surnameProperty(), false) {
            accountClient.setLastName(it)
        }



        setTextEventFilter(aboutInput, account.aboutProperty(), true) {
            accountClient.setAbout(it)
        }

        setBooleanEventFilter(depotInput, account.depotProperty()) {
            accountClient.setDepot(it)
        }

        setBooleanEventFilter(doctorInput, account.doctorProperty()) {
            accountClient.setDoctor(it)
        }

        setBooleanEventFilter(diagnostInput, account.brisProperty()) {
            accountClient.setBris(it)
        }

        setBooleanEventFilter(partnerInput, account.partnerProperty()) {
            accountClient.setPartner(it)
        }

        setBooleanEventFilter(showEmailCb, account.showEmailProperty()) {
            accountClient.setShowEmail(it)
        }

        setBooleanEventFilter(showRealNameCb, account.showRealNameProperty()) {
            accountClient.setRealName(it)
        }

        setBooleanEventFilter(showSkypeCb, account.showSkypeProperty()) {
            accountClient.setShowSkype(it)
        }

    }


    private fun checkError(result: Result<*>): Boolean {
        if (!result.isError) return false

        showErrorDialog(
                "Изменение данных",
                "",
                "Значение поле не сохранено",
                controllerWindow,
                Modality.WINDOW_MODAL
        )
        log.error("", result.error)
        return true
    }


    private fun <T> onChange(oldValue: T, newValue: T, action: (T) -> Unit): Boolean {
        if (oldValue == newValue) return true
        val result: Result<Void> = BlockingAction.actionNoResult(controllerWindow) { action(newValue) }
        return !checkError(result)
    }

    fun onChangePassword() {

        //нельзя сбросить пароль любому человеку, тк сброс будет при подтверждении кода
        val result = BlockingAction.actionNoResult(controllerWindow) {
            SocialClient.INSTANCE.registrationClient.sendResetCode(account.email)
        }
        if (result.isError) {
            processSendCodeError(result)
            return
        }
        openRestoreDialog(controllerWindow, account.email, true)
    }

    private fun processSendCodeError(result: Result<Void>) {
        if (result.error is ApiError) {
            val err = result.error as ApiError
            if (err.statusCode == 404) {
                showWarningDialog(
                        "Отправка кода",
                        "Не удалось отправить код",
                        "Аккаунт с указанным email не существует",
                        controllerWindow,
                        Modality.WINDOW_MODAL)
            } else if (err.statusCode == 400) {
                showWarningDialog(
                        "Отправка кода",
                        "Не удалось отправить код",
                        "Введен не корректный email",
                        controllerWindow,
                        Modality.WINDOW_MODAL)
            }
        } else {
            showWarningDialog(
                    "Отправка кода",
                    "Не удалось отправить код для восстановления",
                    "Попробуйте позже. Если ошибка повторится, обратитесь к разработчикам",
                    controllerWindow,
                    Modality.WINDOW_MODAL)
            log.error("Отправка кода восстановления", result.error)
        }
    }

    fun onChangeEmail() {
        val emailNew = ChangeEmailController.showChangeEmailDialog(controllerWindow, account.email)
        if (!emailNew.isPresent) {
            showErrorDialog(
                    "Изменение email",
                    "",
                    "Email изменен, но значение в поле не обновлено",
                    controllerWindow, Modality.WINDOW_MODAL)
            return
        }

        account.email = emailNew.get()
        emailText.text = account.email


    }

    fun onChangeName() {
        val newName = ChangeNameController.showChangeNameDialog(controllerWindow, account.login)
        if (!newName.isPresent) {
            showErrorDialog(
                    "Изменение имени",
                    "",
                    "Имя изменено, но значение в поле не обновлено",
                    controllerWindow, Modality.WINDOW_MODAL)
            return
        }
        account.login = newName.get()
        nameText.text = account.login
        socialPanelAPI.setName(account.login)
    }

    fun deleteSelectedSessions(actionEvent: ActionEvent) {
        val result = BlockingAction.actionNoResult(controllerWindow) {
            val selected = sessionsList.selectionModel.selectedItems.toList()
            selected.forEach {
                accountClient.deleteToken(it.id)
                sessionListSource.remove(it)
            }
        }
        removeAllBtn.isDisable = sessionListObs.isEmpty()
        if (result.isError) {
            showWarningDialog(
                    "Удаление сессий",
                    "",
                    "Не все сессии удалены, попробуйте позже",
                    controllerWindow,
                    Modality.WINDOW_MODAL)
            log.error("", result.error)
        }
    }

    fun deleteAllSessions(actionEvent: ActionEvent) {
        val result = BlockingAction.actionNoResult(controllerWindow) {
            val forDelete = sessionListObs.toList()//исключаем текущую
            forDelete.forEach {
                accountClient.deleteToken(it.id)
                sessionListSource.remove(it)
            }
        }
        removeAllBtn.isDisable = sessionListObs.isEmpty()
        if (result.isError) {
            showWarningDialog(
                    "Удаление сессий",
                    "",
                    "Не все сессии удалены, попробуйте позже",
                    controllerWindow,
                    Modality.WINDOW_MODAL)
            log.error("", result.error)
        }
    }

    fun refreshAllSessions(actionEvent: ActionEvent) {
        loadSessions()
    }

    companion object {
        private val log by LoggerDelegate()

        @JvmStatic
        fun showAccount(context: Stage, account: AccountView) {

            try {
                openDialogUserData(
                        context,
                        "/fxml/AccountDialog.fxml",
                        "Аккаунт",
                        false,
                        StageStyle.UTILITY,
                        (Screen.getPrimary().bounds.height * 0.9).toInt(), 0, 0, 0,
                        Unit,
                        account
                )
            } catch (e: Exception) {
                log.error("Ошибка открытия диалога аккаунта", e)
                throw RuntimeException(e)
            }
        }
    }

}
