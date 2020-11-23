package ru.biomedis.biomedismair3.utils

import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.fxml.JavaFXBuilderFactory
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.stage.Stage
import javafx.util.Pair
import ru.biomedis.biomedismair3.BaseController
import ru.biomedis.biomedismair3.utils.Other.LoggerDelegate
import java.util.*

/**
 * Класс позволяет добавлять табы в TabPane, получать события выбора этого таба и отсоединения в контроллере таба.
 * Контроллер должен реализовывать интерфейс Selected И Detached
 */
class TabHolder(private val context: Stage, private val tabPane: TabPane){
    private val log by LoggerDelegate()
    private val controllersMap = mutableMapOf<String, Selected>()
    private val tabs = mutableMapOf<String, Tab>()

    init {
        tabPane.selectionModel.selectedItemProperty().addListener {
            _, oldValue, newValue ->
            if(oldValue==newValue) return@addListener
            controllersMap[newValue.id]?.onSelected()
        }
    }

    fun tabByFxId(fxId: String): Tab? = tabs[fxId]

     fun addTab(fxml: String, name: String, isClosable: Boolean, fxId: String = UUID.randomUUID().toString(), vararg params: Any): Selected {
        val loaded = try {
            val result: Pair<Node, BaseController> = loadContent(fxml, context, *params)
            if(result.value !is Selected || result.value !is Detached) throw java.lang.RuntimeException("Контроллер должен реализовывать интерфейс Selected И Detached")
            else  result
        } catch (exception: Exception) {
            log.error("", exception)
            throw RuntimeException(exception)
        }
        val tab = Tab(name).apply {
            content = loaded.key
            this.isClosable = isClosable
            id = fxId
           setOnClosed {
               removeTab(fxId)
           }
        }

        controllersMap[fxId] = loaded.value as Selected
        tabs[fxId] = tab
        Platform.runLater { tabPane.tabs.add(tab) }
        return controllersMap[fxId]!!
    }

    /**
     * Удаляет таб, что добавлен этим холдером
     */
    fun removeTab(fxId: String){
        tabs[fxId]?.let {
            Platform.runLater {
                tabPane.tabs.remove(it)
            }
            (controllersMap[fxId] as Detached).apply { onDetach() }
            tabs.remove(fxId)
            controllersMap.remove(fxId)
        }

    }


    /**
     * Удаляет табы, что добавлены этим холдером
     */
    fun removeAllTabs(){
        tabs.keys.toSet().forEach(this::removeTab)
    }

    fun disableTab(fxId: String){
        tabs[fxId]?.let {
           it.isDisable = true
        }
    }

    fun enableTab(fxId: String){
        tabs[fxId]?.let {
            it.isDisable = false
        }
    }

    @Throws(Exception::class)
  private  fun loadContent(fxml: String, context: Stage, vararg params: Any): Pair<Node, BaseController> {
        val location = BaseController.getApp().javaClass.getResource(fxml)
        val fxmlLoader = FXMLLoader(location, BaseController.getApp().strings)
        fxmlLoader.builderFactory = JavaFXBuilderFactory()
        val root = fxmlLoader.load<Parent>()
        val controller: BaseController = (fxmlLoader.getController<Any>() as BaseController?)?.apply {
            setWindow(context)
            setParams(*params) //до открытия окна в show можно устанавливать любые параметры
            onInitialized()
        }?:throw RuntimeException("Controller must be not null")

        return Pair(root, controller)
    }

    interface Selected{
        fun onSelected()
    }

    interface Detached{
        fun onDetach()
    }
}
