package ru.biomedis.biomedismair3.social

import javafx.scene.control.Control
import javafx.scene.control.TextField
import javafx.scene.control.TextInputControl
import javafx.stage.Modality
import javafx.stage.Stage
import ru.biomedis.biomedismair3.BaseController
import ru.biomedis.biomedismair3.social.remote_client.dto.error.ApiValidationError

class TextFieldUtil(
        private val requestNameFieldMap:Map<String, Control>,
        private val fieldsNameMap:Map<String, String>,
        private val  controllerWindow: Stage,
        private val titleErrorDialog: String
) {

     fun checkFieldLength(field: TextInputControl, msg: String, minLength: Int, maxLength: Int):Boolean{
        return if( field.text.trim().length < minLength ||  field.text.trim().length > maxLength){
            BaseController.showWarningDialog(
                    titleErrorDialog,
                    "",
                    msg,
                    controllerWindow,
                    Modality.WINDOW_MODAL)
            setErrorField(field)
            false
        }else true
    }

     fun setErrorField(node: Control){
        if (!node.styleClass.contains("error_border")) {
            node.styleClass.add("error_border")
        }
    }

     fun setSuccessField(node: Control){
        if (node.styleClass.contains("error_border")) {
            node.styleClass.remove("error_border")
        }
    }

    fun checkEmailField(emailInput:TextField): Boolean{
        return if (!emailInput.text.trim().matches("^[\\w-_.+]*[\\w-_.]@([\\w]+\\.)+[\\w]+[\\w]$".toRegex())) {
            BaseController.showWarningDialog(
                    "Email",
                    "",
                    "Введен не корректный email!",
                    controllerWindow,
                    Modality.WINDOW_MODAL)
            setErrorField(emailInput)
            false
        }else true
    }


     fun processValidationError(errorMessages: List<ApiValidationError>) {
        val strb = StringBuilder()
        requestNameFieldMap.forEach { u -> setSuccessField(u.value) }

        errorMessages.forEach { e: ApiValidationError ->
            if(e.field in requestNameFieldMap){
                setErrorField(requestNameFieldMap[e.field]!!)
                strb.append(fieldsNameMap[e.field]).append(": ").append(e.message).append("\n")
            }else{
                strb.append(e.field).append(": ").append("Неизвестное поле. ").append(e.message).append("\n")
            }
        }
        BaseController.showWarningDialog(
                "Валидация полей формы",
                "Некоторые поля имеют некорректное содержимое",
                strb.toString(),
                controllerWindow,
                Modality.WINDOW_MODAL)
    }
}
