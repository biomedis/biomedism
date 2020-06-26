package ru.biomedis.biomedismair3.social.social_panel

import javafx.beans.value.ObservableValue
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.stage.WindowEvent
import ru.biomedis.biomedismair3.AppController
import ru.biomedis.biomedismair3.BaseController
import ru.biomedis.biomedismair3.social.remote_client.NeedAuthByLogin
import ru.biomedis.biomedismair3.social.remote_client.RequestClientException
import ru.biomedis.biomedismair3.social.remote_client.ServerProblemException
import ru.biomedis.biomedismair3.social.remote_client.SocialClient
import ru.biomedis.biomedismair3.utils.Other.LoggerDelegate
import java.net.URL
import java.util.*


class SocialPanelController : BaseController(), SocialPanelAPI {

    @FXML
    private lateinit var messageCounter: Label

    @FXML
    private lateinit var logIn: Button

    private lateinit var res: ResourceBundle

    val log by LoggerDelegate()

    private lateinit var client: SocialClient
    private var isLogin = false

    override fun onCompletedInitialization() {
        try {
            client.initProcessToken()
            showLogout()
        } catch (e: ServerProblemException) {
            log.error("Ошибка обновления аутентификации. Попробуйте позже.", e)
            AppController.getProgressAPI().setErrorMessage("Ошибка обновления аутентификации. Попробуйте позже.")
        } catch (e: RequestClientException) {
            AppController.getProgressAPI().setErrorMessage("Ошибка обработки запроса аутентификации.")
            log.error("Ошибка обработки запроса аутентификации.", e)
        } catch (needAuthByLogin: NeedAuthByLogin) {
        }
    }

    override fun onClose(event: WindowEvent) {}

    override fun setParams(vararg params: Any) {}

    override fun initialize(location: URL, resources: ResourceBundle) {
        res = resources
        client = SocialClient.INSTANCE
        logIn.onAction = EventHandler(this::login)
        client.isAuthProperty.addListener { _: ObservableValue<out Boolean>?, oldValue: Boolean, newValue: Boolean ->
            if (oldValue == newValue) return@addListener
            if (newValue) showLogout() else showLogin()
        }
    }

    //todo - сделать если чел вошел, то при нажатии - меню, с выбором выйти. выйти со всех устройств.
    // На сервере тоже сделать эти эндпоинты - в том месте где авторизация есть или вручную парсим?
    // Проверить при запросе токена у нас куча новых плодится? Что с этим делать? Если рефреш не подошел,
    // то нужно входить иначе новый создавать, тк мы уже вышли( протух рефреш? Есть тут проверка надо ли вводить, новый токен получать по неверному рефрешу?)
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
    }

    override fun showLogout() {
        isLogin = true
        logIn.text = "Выйти"
    }
}
