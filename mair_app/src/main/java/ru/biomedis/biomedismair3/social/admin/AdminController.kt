package ru.biomedis.biomedismair3.social.admin

import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.stage.WindowEvent
import ru.biomedis.biomedismair3.BaseController
import ru.biomedis.biomedismair3.utils.TabHolder
import ru.biomedis.biomedismair3.utils.Other.LoggerDelegate
import java.lang.RuntimeException
import java.net.URL
import java.util.*

class AdminController : BaseController(), TabHolder.Selected, TabHolder.Detached {

    private val log by LoggerDelegate()
    @FXML
    private lateinit var tabs: TabPane

    private val controllersMap = mutableMapOf<String, TabHolder.Selected>()

    private lateinit var tabHolder: TabHolder



    override fun onClose(event: WindowEvent) {

    }

    override fun setParams(vararg params: Any) {

    }

    override fun onCompletedInitialization() {
        tabHolder = TabHolder(controllerWindow, tabs)

        try{
            tabHolder.addTab( "/fxml/AdminMessages.fxml", "Рассылки",false, "admin_messages")
            tabHolder.addTab( "/fxml/Users.fxml", "Пользователи",false,"users")
        }catch (e: Exception){
            log.error("", e)
            throw e
        }


        tabs.selectionModel.select(0)
    }

    override fun initialize(location: URL, resources: ResourceBundle) {

    }


    override fun onSelected() {

    }

    override fun onDetach() {

    }
}

