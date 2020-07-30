package ru.biomedis.biomedismair3.social.admin

import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.collections.FXCollections
import javafx.collections.transformation.FilteredList
import javafx.fxml.FXML
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.stage.Modality
import javafx.stage.WindowEvent
import ru.biomedis.biomedismair3.BaseController
import ru.biomedis.biomedismair3.BlockingAction
import ru.biomedis.biomedismair3.social.account.AccountWithRoles
import ru.biomedis.biomedismair3.social.remote_client.SocialClient
import ru.biomedis.biomedismair3.utils.Other.LoggerDelegate
import java.net.URL
import java.util.*


class UsersController : BaseController(), Selected {
    @FXML
    private lateinit var table: TableView<AccountWithRoles>

    private val _data = FXCollections.observableArrayList<AccountWithRoles>()
    private val data: FilteredList<AccountWithRoles> = FilteredList(_data) {
        it.userSmallView.email != "root@root.root"
    }
    private val log by LoggerDelegate()

    override fun setParams(vararg params: Any) {

    }

    override fun onClose(event: WindowEvent) {

    }

    override fun onCompletedInitialization() {

    }

    override fun initialize(location: URL, resources: ResourceBundle) {
        table.columns.addAll(createColumns())
        table.columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
        table.tableMenuButtonVisibleProperty().value = true
        table.items = data
    }

    fun refresh() {
        this.loadData()
    }

    private fun createColumns(): List<TableColumn<AccountWithRoles, *>> = mutableListOf<TableColumn<AccountWithRoles, *>>().apply {
        this += addCol("ID") { userSmallView.id }
        this += addCol("Email") { userSmallView.email }
        this += addCol("Login") { userSmallView.login }
        this += addCol("Имя") { userSmallView.name }
        this += addCol("Фамилия") { userSmallView.surname }
        this += addCol("Skype") { userSmallView.skype }
        this += addCol("Партнер") { userSmallView.isPartner }
        this += addCol("Доктор") { userSmallView.isDoctor }
        this += addCol("Компания") { userSmallView.isCompany }
        this += addCol("Склад") { userSmallView.isDepot }
        this += addCol("Поддержка") { userSmallView.isSupport }
        this += addCol("Диагност") { userSmallView.isBris }
    }


    private inline fun <reified T> addCol(name: String, crossinline field: AccountWithRoles.() -> T): TableColumn<AccountWithRoles, T> =
            TableColumn<AccountWithRoles, T>(name).apply {
                setCellValueFactory {
                    ReadOnlyObjectWrapper<T>(it.value.field())
                }
            }

    private fun loadData() {
        val result = BlockingAction.actionResult(controllerWindow) {
            SocialClient.INSTANCE.accountClient.allUsers()
        }

        if (result.isError) {
            showExceptionDialog("Загрузка пользователей",
                    "",
                    "Не удалось загрузить список пользователей",
                    result.error,
                    controllerWindow,
                    Modality.WINDOW_MODAL)
            log.error("", result.error)
            return
        }

        _data.clear()
        _data.addAll(result.value)
    }

    override fun onSelected() {
        if(_data.isEmpty())refresh()
    }

}
