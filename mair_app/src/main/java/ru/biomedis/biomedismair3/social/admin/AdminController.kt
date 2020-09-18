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

class AdminController : BaseController() {

    private val log by LoggerDelegate()
    @FXML
    private lateinit var tabs: TabPane

    private val controllersMap = mutableMapOf<String, TabHolder.Selected>()

    override fun setParams(vararg params: Any?) {

    }

    override fun onClose(event: WindowEvent) {

    }

    override fun onCompletedInitialization() {

    }

    override fun initialize(location: URL, resources: ResourceBundle) {
        tabs.selectionModel.selectedItemProperty().addListener {
            _, oldValue, newValue ->
            if(oldValue==newValue) return@addListener
            controllersMap[newValue.id]?.onSelected()
        }
        try{
            addTab("Рассылки", "/fxml/AdminMessages.fxml", "admin_messages")
            addTab("Пользователи", "/fxml/Users.fxml", "users")
        }catch (e: Exception){
            log.error("", e)
            throw e
        }


        tabs.selectionModel.select(0)
    }

    private fun addTab(tabName: String, fxml: String, fxId: String,  vararg arg: Any): Pair<Node, TabHolder.Selected>{
        val result = loadContent(fxml, arg)
        if(result.value !is TabHolder.Selected) throw RuntimeException("Контроллер должен реализовывать интерфейс Selected")
        Tab(tabName).apply {
            content = result.key
            isClosable = false
            id=fxId
        }.also {
            tabs.tabs.add(it)
        }
        controllersMap[fxId] = result.value as TabHolder.Selected
        return result.key to controllersMap[fxId]!!
    }
}

