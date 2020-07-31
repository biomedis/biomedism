package ru.biomedis.biomedismair3.social.admin

import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.transformation.FilteredList
import javafx.fxml.FXML
import javafx.geometry.Pos
import javafx.scene.control.CheckBox
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.stage.Modality
import javafx.stage.WindowEvent
import javafx.util.Callback
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


        this += addCol("ID", CallbackCenteredTextTableCell()) { userSmallView.id }
        this += addCol("Email", CallbackCenteredTextTableCell()) { userSmallView.email }
        this += addCol("Login", CallbackCenteredTextTableCell()) { userSmallView.login }
        this += addCol("Имя", CallbackCenteredTextTableCell()) { userSmallView.name }
        this += addCol("Фамилия", CallbackCenteredTextTableCell()) { userSmallView.surname }
        this += addCol("Skype", CallbackCenteredTextTableCell()) { userSmallView.skype }
        this += addCol("Партнер", CallbackCenteredBooleanTableCell()) { userSmallView.isPartner }
        this += addCol("Доктор", CallbackCenteredBooleanTableCell()) { userSmallView.isDoctor }
        this += addCol("Компания", CallbackCenteredBooleanTableCell()) { userSmallView.isCompany }
        this += addCol("Склад", CallbackCenteredBooleanTableCell()) { userSmallView.isDepot }
        this += addCol("Поддержка", CallbackCenteredBooleanTableCell()) { userSmallView.isSupport }
        this += addCol("Диагност", CallbackCenteredBooleanTableCell()) { userSmallView.isBris }
    }

    /**
     * [name] имя столбца, [maxWidth] макс. ширина столбца, если < 0, то столбец не ограничивается.
     * [field] лямбда возвращает поле, из модели, которое должно отображаться, оно будет обернуто в ObservableObject
     */
    private inline fun <reified T> addCol(name: String, tableCellFactory: Callback<TableColumn<AccountWithRoles, T>, TableCell<AccountWithRoles, T>>, crossinline field: AccountWithRoles.() -> T): TableColumn<AccountWithRoles, T> =
            TableColumn<AccountWithRoles, T>(name).apply {
                setCellValueFactory {
                    ReadOnlyObjectWrapper<T>(it.value.field())
                }
                setCellFactory(tableCellFactory)

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
        if (_data.isEmpty()) refresh()
    }

}


class CallbackCenteredTextTableCell<T> : Callback<TableColumn<AccountWithRoles, T>, TableCell<AccountWithRoles, T>> {
    override fun call(param: TableColumn<AccountWithRoles, T>?): TableCell<AccountWithRoles, T> {
        return CenteredTextTableCell()
    }
}

class CallbackCenteredBooleanTableCell(private val disabled: Boolean = false) : Callback<TableColumn<AccountWithRoles, Boolean>, TableCell<AccountWithRoles, Boolean>> {
    override fun call(param: TableColumn<AccountWithRoles, Boolean>?): TableCell<AccountWithRoles, Boolean> {
        return CenteredBooleanTableCell(disabled)
    }
}

class CenteredTextTableCell<T> : TableCell<AccountWithRoles, T>() {
    init {
        alignment = Pos.CENTER
    }

    override fun updateItem(item: T?, empty: Boolean) {
        super.updateItem(item, empty)
        if (item != null && !empty) text = item.toString()
    }
}

class CenteredBooleanTableCell(private val disabled: Boolean = false) : TableCell<AccountWithRoles, Boolean>() {
    private val checkBox: CheckBox = CheckBox()

    init {
        alignment = Pos.CENTER
        checkBox.isDisable = disabled
    }

    override fun updateItem(item: Boolean?, empty: Boolean) {
        super.updateItem(item, empty)

        if (item != null && !empty) {
            checkBox.isSelected = item
            graphic = checkBox
        } else {
            text = null
            graphic = null
        }
    }
}


