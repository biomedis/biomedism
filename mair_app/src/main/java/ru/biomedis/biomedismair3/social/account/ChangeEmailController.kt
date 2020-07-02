package ru.biomedis.biomedismair3.social.account

import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.TextField
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.stage.WindowEvent
import ru.biomedis.biomedismair3.BaseController
import ru.biomedis.biomedismair3.social.login.EmailConfirmController
import ru.biomedis.biomedismair3.utils.Other.LoggerDelegate
import java.net.URL
import java.util.*

class ChangeEmailController:BaseController(){
    private val log by LoggerDelegate()
    @FXML private lateinit var inputCode: TextField
    private lateinit var email: String
    private lateinit var res: ResourceBundle;

    override fun setParams(vararg params: Any) {
        if (params.isEmpty()) throw RuntimeException("Не верные параметры. Должен быть параметр email")
        email = params[0] as String
    }

    override fun onClose(event: WindowEvent) {

    }

    override fun onCompletedInitialization() {


    }

    override fun initialize(location: URL, resources: ResourceBundle) {
        res = resources
    }

    fun onChangeEmailAction() {
        if(!EmailConfirmController.openConfirmationEmailDialog(controllerWindow, email)) return
        //проверить уникальность почты.
        //инициировать передачу кода смены email, сохранить код и новую почту в отдельной таблице для смены почты
        //отправить код, проверить на сервере совпадение кода для юзера, взять почту и заменить в основной.


        /*
        На сервере - уникальность почты, выслать код для новой почты, проверить код новой почты и заменить ее
         */
    }


    companion object{
        private val log by LoggerDelegate()

        /**
         * Выполнится подтверждение указанной почты( старая )
         * Ввод новой почты
         * Проверка новой почты, после передачи кода если все ок, то почта будет изменена
         * Почта проверяется на уникальность.
         *
         */
        @JvmStatic
        fun showChangeEmailDialog(context: Stage, email: String): Optional<String> {
            val result: Data =   try {
                openDialogUserData(
                        context,
                        "/fxml/ChangeEmailDialog.fxml",
                        "Изменение email",
                        true,
                        StageStyle.UTILITY,
                        0, 0, 0, 0,
                        Data()
                )
            } catch (e: Exception) {
                log.error("Ошибка открытия диалога аккаунта", e)
                throw RuntimeException(e)
            }

            return if (result.result.isEmpty()) Optional.empty()
            else Optional.of(result.result)
        }
    }

    class Data{
        var result: String=""
    }
}
