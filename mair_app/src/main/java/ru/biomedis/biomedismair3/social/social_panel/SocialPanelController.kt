package ru.biomedis.biomedismair3.social.social_panel

import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Hyperlink
import javafx.scene.control.Label
import javafx.scene.control.Tab
import javafx.scene.layout.HBox
import javafx.stage.WindowEvent
import ru.biomedis.biomedismair3.AppController
import ru.biomedis.biomedismair3.BaseController
import ru.biomedis.biomedismair3.social.account.AccountController
import ru.biomedis.biomedismair3.social.remote_client.*
import ru.biomedis.biomedismair3.utils.Other.LoggerDelegate
import java.net.URL
import java.util.*


class SocialPanelController : BaseController(), SocialPanelAPI {



    @FXML
    private lateinit var root: HBox

    @FXML
    private lateinit var messageCounter: Label

    @FXML
    private lateinit var logIn: Button

    private val tabs = mutableMapOf<String, Tab>()

    private lateinit var res: ResourceBundle

    val log by LoggerDelegate()

    private lateinit var client: SocialClient
    private var isLogin = false
    private lateinit var  tokenRepository: TokenRepository

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

        SocialClient.INSTANCE.isAuthProperty.addListener{
            _,oldV, newV->
            println("$oldV $newV")
            if (oldV == newV) return@addListener
            if (newV) showLogout() else showLogin()
            if(newV){
                tabs["/fxml/social/Contacts.fxml"]  = addTab("/fxml/social/Contacts.fxml","Контакты")
                tabs["/fxml/social/Registry.fxml"]  =  addTab("/fxml/social/Registry.fxml","Справочник")
                if(client.isAdmin)  tabs["/fxml/Admin.fxml"]  =  addTab("/fxml/Admin.fxml","Admin")
            }else {
                tabs.forEach { (_, t) ->removeTab(t) }
            }
        }
    }

   private fun login(event: ActionEvent) {
        if (!isLogin) {
            try {
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
            //если  состяние токена изменилось, то сработает обработчик (выше), который обновит интерфейс
            client.performLogout(controllerWindow)
        }
    }

    override fun showLogin() {
        isLogin = false
        logIn.text = "Войти"
        hideUserName()
        println("SHOW LOGIN")
    }



    override fun showLogout() {
        isLogin = true
        logIn.text = "Выйти"
        showUserName()
        println("SHOW LOGOUT")

    }

    override fun setName(name: String) {
        val nameNode = root.children[0]
        if(nameNode is Hyperlink) {
            nameNode.text = name
        }
        tokenRepository.updateTokenName(name)
    }

    private fun onShowProfile(){
        AccountController.showAccount(controllerWindow)
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

    private fun addTab(fxml: String, name: String, isClosable: Boolean = false): Tab{
        val tab = Tab(name)

        val node = try {
            loadContent(fxml).key
        } catch (exception: Exception) {
            log.error("", exception)
            throw RuntimeException(exception)
        }
        tab.content = node
        tab.isClosable = isClosable
        Platform.runLater { AppController.getAppController().addTab(tab) }
        return tab
    }

    private fun removeTab(tab: Tab?){
        if(tab!=null){
            Platform.runLater {
                AppController.getAppController().removeTab(tab, true)
            }
        }
    }
}
