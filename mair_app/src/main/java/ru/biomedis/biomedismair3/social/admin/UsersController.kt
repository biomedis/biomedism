package ru.biomedis.biomedismair3.social.admin

import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
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

        val booleanCell = CallbackCenteredBooleanTableCell(false, this@UsersController::onBooleanEditField)

        this += addCol("ID", ColumnId.ID, CallbackCenteredTextTableCell()) { userSmallView.id }
        this += addCol("Email", ColumnId.EMAIL, CallbackCenteredTextTableCell()) { userSmallView.email }
        this += addCol("Login", ColumnId.LOGIN, CallbackCenteredTextTableCell()) { userSmallView.login }
        this += addCol("Имя", ColumnId.NAME, CallbackCenteredTextTableCell()) { userSmallView.name }
        this += addCol("Фамилия", ColumnId.SURNAME, CallbackCenteredTextTableCell()) { userSmallView.surname }
        this += addCol("Skype", ColumnId.SKYPE, CallbackCenteredTextTableCell()) { userSmallView.skype }
        this += addCol("Партнер", ColumnId.PARTNER, booleanCell) { userSmallView.isPartner }
        this += addCol("Доктор", ColumnId.DOCTOR, booleanCell) { userSmallView.isDoctor }
        this += addCol("Компания", ColumnId.COMPANY, booleanCell) { userSmallView.isCompany }
        this += addCol("Склад", ColumnId.DEPOT, booleanCell) { userSmallView.isDepot }
        this += addCol("Поддержка", ColumnId.SUPPORT, booleanCell) { userSmallView.isSupport }
        this += addCol("Диагност", ColumnId.BRIS, booleanCell) { userSmallView.isBris }
    }

    /**
     * [name] имя столбца, [maxWidth] макс. ширина столбца, если < 0, то столбец не ограничивается.
     * [field] лямбда возвращает поле, из модели, которое должно отображаться, оно будет обернуто в ObservableObject
     */
    private inline fun <reified T> addCol(name: String, fxId: String, tableCellFactory: Callback<TableColumn<AccountWithRoles, T>, TableCell<AccountWithRoles, T>>, crossinline field: AccountWithRoles.() -> T): TableColumn<AccountWithRoles, T> =
            TableColumn<AccountWithRoles, T>(name).apply {
                setCellValueFactory {
                    ReadOnlyObjectWrapper<T>(it.value.field())
                }
                cellFactory = tableCellFactory
                id = fxId
            }


    private fun onBooleanEditField(item: AccountWithRoles, fxId: String, value: Boolean): Boolean{
        val client = SocialClient.INSTANCE.accountClient
        val id = item.userSmallView.id

        val result = when (fxId) {
            ColumnId.BRIS -> BlockingAction.actionNoResult(controllerWindow) { client.setBris(value, id) }
            ColumnId.COMPANY -> BlockingAction.actionNoResult(controllerWindow) { client.setCompany(value, id) }
            ColumnId.SUPPORT -> BlockingAction.actionNoResult(controllerWindow) { client.setSupport(value, id) }
            ColumnId.DOCTOR -> BlockingAction.actionNoResult(controllerWindow) { client.setDoctor(value, id) }
            ColumnId.DEPOT -> BlockingAction.actionNoResult(controllerWindow) { client.setDepot(value, id) }
            ColumnId.PARTNER -> BlockingAction.actionNoResult(controllerWindow) { client.setPartner(value, id) }
            else -> throw RuntimeException("$fxId has not handler")
        }

        if(result.isError){
            showExceptionDialog("Изменение значений","","Неудачно",result.error,controllerWindow, Modality.WINDOW_MODAL)
            log.error("",result.error)
            return false
        }


        TODO("Сделать на сервере эндпоинты админа для изменения булеанов")
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

    private object ColumnId {
        const val ID = "ID_col"
        const val EMAIL = "EMAIL_col"
        const val NAME = "NAME_col"
        const val SURNAME = "SURNAME_col"
        const val LOGIN = "LOGIN_col"
        const val CITY = "CITY_col"
        const val COUNTRY = "COUNTRY_col"
        const val SKYPE = "SKYPE_col"
        const val BRIS = "BRIS_col"
        const val PARTNER = "PARTNER_col"
        const val COMPANY = "COMPANY_col"
        const val DEPOT = "DEPOT_col"
        const val DOCTOR = "DOCTOR_col"
        const val SUPPORT = "SUPPORT_col"
    }
}


class CallbackCenteredTextTableCell<T> : Callback<TableColumn<AccountWithRoles, T>, TableCell<AccountWithRoles, T>> {
    override fun call(param: TableColumn<AccountWithRoles, T>?): TableCell<AccountWithRoles, T> {
        return CenteredTextTableCell()
    }
}

class CallbackCenteredBooleanTableCell(private val disabled: Boolean = false, private val onChange: (AccountWithRoles, String, Boolean)->Boolean) : Callback<TableColumn<AccountWithRoles, Boolean>, TableCell<AccountWithRoles, Boolean>> {
    override fun call(param: TableColumn<AccountWithRoles, Boolean>?): TableCell<AccountWithRoles, Boolean> {
        return CenteredBooleanTableCell(disabled, onChange)
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

class CenteredBooleanTableCell(private val disabled: Boolean = false, private val onChange: (AccountWithRoles, String, Boolean)->Boolean) : TableCell<AccountWithRoles, Boolean>() {
    private val checkBox: CheckBox = CheckBox()

    init {
        alignment = Pos.CENTER
        checkBox.isDisable = disabled
        checkBox.setOnAction {
            if(!onChange(tableRow.item as AccountWithRoles, tableColumn.id, checkBox.isSelected)){
                checkBox.isSelected!=checkBox.isSelected
            }
        }
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


