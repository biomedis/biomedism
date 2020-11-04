package ru.biomedis.biomedismair3.social.social_panel

import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.Hyperlink
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.stage.Modality
import javafx.stage.WindowEvent
import ru.biomedis.biomedismair3.AppController
import ru.biomedis.biomedismair3.BaseController
import ru.biomedis.biomedismair3.BlockingAction
import ru.biomedis.biomedismair3.social.account.AccountController
import ru.biomedis.biomedismair3.social.account.AccountView
import ru.biomedis.biomedismair3.social.remote_client.*
import ru.biomedis.biomedismair3.utils.Other.LoggerDelegate
import ru.biomedis.biomedismair3.utils.Other.Result
import ru.biomedis.biomedismair3.utils.TabHolder
import java.net.URL
import java.util.*


class SocialPanelController : BaseController(), SocialPanelAPI {



    @FXML
    private lateinit var root: HBox

    @FXML
    private lateinit var messageCounter: Label

    @FXML
    private lateinit var logIn: Button


    private lateinit var res: ResourceBundle

    val log by LoggerDelegate()

    private lateinit var client: SocialClient
    private var isLogin = false
    private lateinit var  tokenRepository: TokenRepository

    private lateinit var tabHolder: TabHolder

    override fun onCompletedInitialization() {
        try {
            client.initProcessToken()
        } catch (e: ServerProblemException) {
            log.error("Ошибка обновления аутентификации. Попробуйте позже.", e)
            AppController.getProgressAPI().setErrorMessage("Ошибка обновления аутентификации. Попробуйте позже.")
        } catch (e: RequestClientException) {
            AppController.getProgressAPI().setErrorMessage("Ошибка обработки запроса аутентификации.")
            log.error("Ошибка обработки запроса аутентификации.", e)
        } catch (needAuthByLogin: NeedAuthByLogin) {
        }finally {
            SocialClient.INSTANCE.completeLoginRequestProperty().value = true
        }
    }


    override fun onClose(event: WindowEvent) {}

    override fun setParams(vararg params: Any) {}

    override fun initialize(location: URL, resources: ResourceBundle) {
        res = resources
        client = SocialClient.INSTANCE
        logIn.onAction = EventHandler(this::login)
        tokenRepository = model
        tabHolder = TabHolder(getApp().mainWindow, getAppController().tabPane)

        SocialClient.INSTANCE.isAuthProperty.addListener{
            _,oldV, newV->

            if (oldV == newV) return@addListener
            if (newV) showLogout() else showLogin()
            if(newV){
                tabHolder.addTab("/fxml/social/Contacts.fxml", "Контакты", false)
                tabHolder.addTab("/fxml/social/Registry.fxml", "Справочник", false)
                if(client.isAdmin)  tabHolder.addTab("/fxml/Admin.fxml", "Admin", false)

            }else {
                tabHolder.removeAllTabs()
            }
        }
    }

   private fun login(event: ActionEvent) {
        if (!isLogin) {
            try {
                println("--------------- Состояние входа: "+SocialClient.INSTANCE.isAuthProperty)
                client.performLogin(controllerWindow)
                //если  состяние токена изменилось, то сработает обработчик (выше), который обновит интерфейс
            } catch (e: RequestClientException) {
                AppController.getProgressAPI().setErrorMessage("Не удалось войти в аккаунт. Ошибка обработки запроса аутентификации.")
                log.error("Ошибка обработки запроса аутентификации.", e)
            } catch (e: ServerProblemException) {
                AppController.getProgressAPI().setErrorMessage("Не удалось войти в аккаунт. Ошибка обработки запроса аутентификации.")
                log.error("Ошибка обработки запроса аутентификации.", e)
            }
        } else {
            println("--------------- Состояние входа: "+SocialClient.INSTANCE.isAuthProperty)
            //если  состяние токена изменилось, то сработает обработчик (выше), который обновит интерфейс
            client.performLogout(controllerWindow)
        }
    }

    override fun showLogin() {
        isLogin = false
        logIn.text = "Войти"
        hideUserName()
        println("SHOW LOGIN")
        println("--------------- Состояние входа2: "+SocialClient.INSTANCE.isAuthProperty)
    }



    override fun showLogout() {
        isLogin = true
        logIn.text = "Выйти"
        showUserName()
        println("SHOW LOGOUT")
        println("--------------- Состояние входа2: "+SocialClient.INSTANCE.isAuthProperty)

    }

    override fun setName(name: String) {
        val nameNode = root.children[0]
        if(nameNode is Hyperlink) {
            nameNode.text = name
        }
        tokenRepository.updateTokenName(name)
    }

    private fun onShowProfile(){

        if (!SocialClient.INSTANCE.token.isPresent) {
                showErrorDialog(
                        "Аккаунт",
                        "",
                        "Сессия потеряна, перезапустите программу. Если ошибка повториться обратитесь к разработчикам",
                        controllerWindow,
                        Modality.WINDOW_MODAL
                )
            return
        }

        val result: Result<AccountView> = BlockingAction.actionResult(controllerWindow) {
            SocialClient.INSTANCE.accountClient.getAccount(SocialClient.INSTANCE.token.get().userId)
        }

        if (result.isError) {
            showErrorDialog(
                    "Аккаунт",
                    "",
                    "Не удалось получить данные аккаунта. Перезапустите программу. Если ошибка повториться обратитесь к разработчикам",
                    controllerWindow,
                    Modality.WINDOW_MODAL
            )

        } else {
            AccountController.showAccount(controllerWindow, result.value)
        }


    }

    private fun showUserName(){
        client.token.ifPresent {
            val link  = Hyperlink(it.userName)
            link.onAction = EventHandler { onShowProfile() }
            root.children.add(0, link)
            root.requestLayout()
        }
    }



    private fun hideUserName() {
        val link = root.children[0] as Hyperlink
        link.onAction = null
        root.children.remove(link)
    }

}
