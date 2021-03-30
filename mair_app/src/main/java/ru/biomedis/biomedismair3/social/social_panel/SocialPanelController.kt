package ru.biomedis.biomedismair3.social.social_panel

import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.Hyperlink
import javafx.scene.control.Label
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import javafx.scene.layout.HBox
import javafx.stage.Modality
import javafx.stage.WindowEvent
import ru.biomedis.biomedismair3.AppController
import ru.biomedis.biomedismair3.BaseController
import ru.biomedis.biomedismair3.BlockingAction
import ru.biomedis.biomedismair3.social.account.AccountController
import ru.biomedis.biomedismair3.social.account.AccountView
import ru.biomedis.biomedismair3.social.remote_client.*
import ru.biomedis.biomedismair3.utils.OS.OSValidator
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
                tabHolder.addTab("/fxml/social/Files.fxml", "Файлы", false)
                tabHolder.addTab("/fxml/social/AllLenta.fxml", "Лента", false, fxId = "lenta")
                tabHolder.tabByFxId("lenta")?.let {
                    Platform.runLater {
                        val result = BlockingAction.actionResult(controllerWindow){
                            SocialClient.INSTANCE.accountClient.getNotViewedStoriesCount();
                        }
                        if(result.isError){
                            log.error("", result.error)

                        }else {
                           if(result.value!=0) it.text="${it.text}: ${result.value} новых"
                        }
                    }
                }

                if(client.isAdmin)  tabHolder.addTab("/fxml/Admin.fxml", "Admin", false)

            }else {
                tabHolder.removeAllTabs()
            }
        }

        SocialClient.INSTANCE.addTotalCountMessagesHandler { count, _ ->
            Platform.runLater { messageCounter.text = count.toString() }
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

        if(OSValidator.isMac() && !checkJreVersion()){
            val clipboard = Clipboard.getSystemClipboard().apply { clear()}
            ClipboardContent().let {
                it.putString("http://biomedis.ru/doc/b_mair/jre/mac_os_x_jre.zip")
                clipboard.setContent(it)
            }
            showInfoDialog(res.getString("need_jre_update"),
                    res.getString("need_jre_update"),
                    res.getString("jre_update_instruction")+"\n\n"+res.getString("update_warn"),
                    controllerWindow,
                    Modality.WINDOW_MODAL
            )
        }

        if(OSValidator.isWindows() && !checkJreVersion() && !checkOsWinVersion()){
            showInfoDialog(res.getString("need_jre_update"),
                res.getString("need_jre_update"),
                "Обновите программу запустив установшик с сайта. При запуске установщика выберите -\"Обновить\". Следующие обновления будут автоматическими, как обычно",
                controllerWindow,
                Modality.WINDOW_MODAL
            )
        }
    }

    override fun setName(name: String) {
        val nameNode = root.children[0]
        if(nameNode is Hyperlink) {
            nameNode.text = name
        }
        tokenRepository.updateTokenName(name)
    }

    //true если не нужно обновлять jre
    private fun checkJreVersion(): Boolean {
        val javaVersion = System.getProperty("java.version")
        val split = javaVersion.split(".").toTypedArray()
        val split1 = split[2].split("_").toTypedArray()
        val isBellsoftJre =
            System.getProperty("java.vendor").toLowerCase().contains("BellSoft".toLowerCase())
        //1.8.0_282
        return split[1].toInt() == 8 && split1[0].toInt() == 0 && split1[1].toInt() >= 282 && isBellsoftJre
    }

    private fun checkOsWinVersion(): Boolean {
        val osV: String = OSValidator.osVersion()
        val vArray = osV.split(".").map { s: String -> s.toInt() }
        return if (vArray[0] < 6) false else vArray[0] != 6 || vArray[1] > 1
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
