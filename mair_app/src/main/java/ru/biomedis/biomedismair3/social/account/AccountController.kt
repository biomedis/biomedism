package ru.biomedis.biomedismair3.social.account

import javafx.application.Platform
import javafx.beans.property.BooleanProperty
import javafx.beans.property.StringProperty
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.input.InputEvent
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
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


    }

    private fun fillFields(value: AccountView) = account.apply {
        aboutInput.text = value.about
        skypeInput.text  = value.skype
        firstNameInput.text = value.name
        lastNameInput.text = value.surname
        nameText.text = value.login
        emailText.text = value.email
        countryInput.text  = value.country
        cityInput.text  = value.city
        depotInput.isSelected = value.depot
        diagnostInput.isSelected = value.bris
        doctorInput.isSelected = value.doctor
        partnerInput.isSelected = value.partner
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


        account = AccountView()

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
            fillFields(result.value)
            account = result.value
            onChangeForm()
        }

    }

    /**
     * ДЛя передачи фокуса следующему контролу
     */
    private fun tabEventFire(sourceEvent: InputEvent, node: Node){
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

    private val eventHandlers: MutableList<Pair<Node,EventHandler<*>>> = mutableListOf()

    private fun textEventHandler(
            control: TextInputControl,
            modelProperty: StringProperty,
            action:  (String)->Unit,
            shiftPressed: Boolean = false
    ):EventHandler<KeyEvent>{
       return  EventHandler { event->
            if(event.code == KeyCode.ENTER && (event.isShiftDown || !shiftPressed)) {
                if(!onChange(modelProperty.value, control.text, action)) {
                    control.text = modelProperty.value
                }
                else {
                    modelProperty.value = control.text
                    tabEventFire(event, control)
                }

                event.consume()
            }
        }

    }



    private  fun <T:InputEvent> booleanEventHandler(
            control: CheckBox,
            modelProperty: BooleanProperty,
            action:  (Boolean)->Unit
    ):EventHandler<T>{
        return EventHandler { event->
            if(event is KeyEvent && !(event.character=="\t" || event.character=="\r") ) {
                   control.isSelected =!modelProperty.value
            }else  if(event is KeyEvent) return@EventHandler

            if(!onChange(modelProperty.value, control.isSelected, action)) {
                control.isSelected = modelProperty.value
            }
            else {
                modelProperty.value = control.isSelected
            }
            event.consume()
        }

    }

    private fun setTextEventFilter( control: TextInputControl,
                                    modelProperty: StringProperty,
                                    shiftPressed: Boolean,
                                    action:  (String)->Unit){

        var eh : EventHandler<KeyEvent> = textEventHandler(control, modelProperty, action, shiftPressed)
        control.addEventFilter(KeyEvent.KEY_PRESSED, eh)
        eventHandlers.add(control to eh)
    }

    private fun setBooleanEventFilter( control: CheckBox,
                                    modelProperty: BooleanProperty,
                                    action:  (Boolean)->Unit){

        var eh : EventHandler<MouseEvent> = booleanEventHandler(control, modelProperty, action)
        control.addEventFilter(MouseEvent.MOUSE_CLICKED, eh)
        var  ehk : EventHandler<KeyEvent> = booleanEventHandler(control, modelProperty, action)
        control.addEventFilter(MouseEvent.MOUSE_CLICKED, eh)
        control.addEventFilter(KeyEvent.KEY_TYPED, ehk)
        eventHandlers.add(control to eh)
        eventHandlers.add(control to ehk)
    }

    private fun onChangeForm() {

        setTextEventFilter(skypeInput, account.skypeProperty(),false) {
            accountClient.setSkype(it)
        }

        setTextEventFilter(firstNameInput, account.nameProperty(),false) {
            accountClient.setFirstName(it)
        }

        setTextEventFilter(lastNameInput, account.surnameProperty(),false) {
            accountClient.setLastName(it)
        }

        setTextEventFilter(cityInput, account.cityProperty(),false) {
            accountClient.setCity(it)
        }

        setTextEventFilter(countryInput, account.countryProperty(),false) {
            accountClient.setCountry(it)
        }

        setTextEventFilter(aboutInput, account.aboutProperty(),true) {
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


    private fun <T> onChange( oldValue: T, newValue: T, action: (T)->Unit): Boolean{
        if(oldValue == newValue) return true
        val result: Result<Void> = BlockingAction.actionNoResult(controllerWindow) { action(newValue) }
        return !checkError(result)
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
