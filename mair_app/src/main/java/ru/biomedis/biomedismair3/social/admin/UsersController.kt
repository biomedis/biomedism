package ru.biomedis.biomedismair3.social.admin

import javafx.beans.Observable
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.transformation.FilteredList
import javafx.fxml.FXML
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.stage.Modality
import javafx.stage.WindowEvent
import javafx.util.Callback
import javafx.util.StringConverter
import ru.biomedis.biomedismair3.BaseController
import ru.biomedis.biomedismair3.BlockingAction.actionNoResult
import ru.biomedis.biomedismair3.BlockingAction.actionResult
import ru.biomedis.biomedismair3.social.admin.UsersController.ColumnId.BRIS
import ru.biomedis.biomedismair3.social.admin.UsersController.ColumnId.COMPANY
import ru.biomedis.biomedismair3.social.admin.UsersController.ColumnId.DEPOT
import ru.biomedis.biomedismair3.social.admin.UsersController.ColumnId.DOCTOR
import ru.biomedis.biomedismair3.social.admin.UsersController.ColumnId.PARTNER
import ru.biomedis.biomedismair3.social.admin.UsersController.ColumnId.SUPPORT
import ru.biomedis.biomedismair3.social.remote_client.Role
import ru.biomedis.biomedismair3.social.remote_client.SocialClient
import ru.biomedis.biomedismair3.utils.Other.LoggerDelegate
import java.net.URL
import java.util.*
import java.util.function.Predicate
import kotlin.reflect.KMutableProperty0
import ru.biomedis.biomedismair3.utils.Other.Result as ResultAction


class UsersController : BaseController(), Selected {
    @FXML
    private lateinit var table: TableView<AccountWithRoles>

    @FXML
    private lateinit var rolesChoice: ChoiceBox<Role?>

    @FXML
    private lateinit var supportBtn: CheckBox

    @FXML
    private lateinit var doctorBtn: CheckBox

    @FXML
    private lateinit var brisBtn: CheckBox

    @FXML
    private lateinit var companyBtn: CheckBox

    @FXML
    private lateinit var partnerBtn: CheckBox

    @FXML
    private lateinit var depotBtn: CheckBox

    @FXML
    private lateinit var othersBtn: CheckBox

    private val filterCheckboxes = mutableListOf<CheckBox>()
    private val _data = FXCollections.observableArrayList<AccountWithRoles>(extractor())
    private val dataRolesFiltered: FilteredList<AccountWithRoles> = FilteredList(_data)
    private val data: FilteredList<AccountWithRoles> = FilteredList(dataRolesFiltered)

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
        initViewFilterControls()
        table.items = data
        initRolesChoice()
    }


    private fun initRolesChoice() {
        rolesChoice.converter = object : StringConverter<Role?>() {
            override fun toString(item: Role?): String {
                return item?.roleName ?: "Все"
            }

            override fun fromString(itemString: String?): Role {
                throw RuntimeException("Редактирование не планировалось")
            }
        }
        rolesChoice.items.add(null)
        Role.allRoles().forEach { rolesChoice.items.add(it) }
        rolesChoice.selectionModel.select(0)
        rolesChoice.setOnAction { onRolesFilterAction() }
    }

    private fun onRolesFilterAction() {
        data.predicate = DataRolesFilterPredicate()
    }


    private fun initViewFilterControls() {
        fun CheckBox.setup() {
            setOnAction { onChangeFilter() }
            isSelected = true
        }
        filterCheckboxes.apply {
            add(supportBtn)
            add(doctorBtn)
            add(brisBtn)
            add(companyBtn)
            add(partnerBtn)
            add(depotBtn)
            add(othersBtn)
        }.forEach(CheckBox::setup)

        onChangeFilter()
    }

    private fun isRoot(it: AccountWithRoles): Boolean {
        return it.userSmallView.email == "root@root.root"
    }

    private fun onChangeFilter() {
        dataRolesFiltered.predicate = DataFilterPredicate()
    }

    fun refresh() {
        this.loadData()
    }

    private fun createColumns(): List<TableColumn<AccountWithRoles, *>> = mutableListOf<TableColumn<AccountWithRoles, *>>().apply {

        val booleanCell = CallbackCenteredBooleanTableCell(false, this@UsersController::onBooleanEditField)


        this += addCol("ID", ColumnId.ID, CallbackIdTableCell(this@UsersController::onRolesEditField)){ReadOnlyObjectWrapper(userSmallView.id)}
        this += addColList("Статус", ColumnId.ROLES, CallbackRolesTableCell()) { rolesProperty() }
        this += addCol("Email", ColumnId.EMAIL, CallbackCenteredTextTableCell()) { userSmallView.emailProperty() }
        this += addCol("Login", ColumnId.LOGIN, CallbackCenteredTextTableCell()) { userSmallView.loginProperty() }
        this += addCol("Имя", ColumnId.NAME, CallbackCenteredTextTableCell()) { userSmallView.nameProperty() }
        this += addCol("Фамилия", ColumnId.SURNAME, CallbackCenteredTextTableCell()) { userSmallView.surnameProperty() }
        this += addCol("Страна", ColumnId.COUNTRY, CallbackCenteredTextTableCell()) { userSmallView.countryProperty() }
        this += addCol("Город", ColumnId.CITY, CallbackCenteredTextTableCell()) { userSmallView.cityProperty() }
        this += addCol("Skype", ColumnId.SKYPE, CallbackCenteredTextTableCell()) { userSmallView.skypeProperty() }
        this += addCol("Партнер", PARTNER, booleanCell) { userSmallView.partnerProperty() }
        this += addCol("Доктор", DOCTOR, booleanCell) { userSmallView.doctorProperty() }
        this += addCol("Компания", COMPANY, booleanCell) { userSmallView.companyProperty() }
        this += addCol("Склад", DEPOT, booleanCell) { userSmallView.depotProperty() }
        this += addCol("Поддержка", SUPPORT, booleanCell) { userSmallView.supportProperty() }
        this += addCol("Диагност", BRIS, booleanCell) { userSmallView.brisProperty() }
    }

    /**
     * [name] имя столбца, [maxWidth] макс. ширина столбца, если < 0, то столбец не ограничивается.
     * [field] лямбда возвращает поле, из модели, которое должно отображаться, оно будет обернуто в ObservableObject
     */
    private inline fun <reified T> addCol(name: String, fxId: String, tableCellFactory: Callback<TableColumn<AccountWithRoles, T>, TableCell<AccountWithRoles, T>> , crossinline field: AccountWithRoles.() -> ObservableValue<T>): TableColumn<AccountWithRoles, T> =
            TableColumn<AccountWithRoles, T>(name).apply {
                setCellValueFactory {
                    it.value.field()
                }
                cellFactory = tableCellFactory
                id = fxId
            }

    private inline fun <reified T> addColList(name: String,
                                              fxId: String,
                                              tableCellFactory: Callback<TableColumn<AccountWithRoles, ObservableList<T>>, TableCell<AccountWithRoles, ObservableList<T>>> ,
                                              crossinline field: AccountWithRoles.() -> ObservableValue<ObservableList<T>>): TableColumn<AccountWithRoles, ObservableList<T>> =
            TableColumn<AccountWithRoles, ObservableList<T>>(name).apply {
                setCellValueFactory {
                    it.value.field()
                }
                cellFactory = tableCellFactory
                id = fxId
            }

    private fun onRolesEditField(item: AccountWithRoles) {
        val result = RolesController.openRolesDialog(controllerWindow, item.roles)
        val set = item.roles.toSet()

        //списки одинаковы, изменений нет
        if (set == result.toSet()) return
        else {
            item.roles.clear()
            result.forEach { item.roles.add(it) }
            //table.refresh()
        }

    }


    private fun onBooleanEditField(item: AccountWithRoles, fxId: String, value: Boolean): Boolean {

        fun ResultAction<*>.setValue(property: KMutableProperty0<Boolean>): ResultAction<*> {
            if (!this.isError) property.set(value)
            return this
        }

        val client = SocialClient.INSTANCE.accountClient
        val id = item.userSmallView.id

        val uitem = item.userSmallView
        val result: ResultAction<*> = when (fxId) {
            BRIS -> actionNoResult(controllerWindow) { client.setBris(value, id) }.setValue(uitem::isBris)
            COMPANY -> actionNoResult(controllerWindow) { client.setCompany(value, id) }.setValue(uitem::isCompany)
            SUPPORT -> actionNoResult(controllerWindow) { client.setSupport(value, id) }.setValue(uitem::isSupport)
            DOCTOR -> actionNoResult(controllerWindow) { client.setDoctor(value, id) }.setValue(uitem::isDoctor)
            DEPOT -> actionNoResult(controllerWindow) { client.setDepot(value, id) }.setValue(uitem::isDepot)
            PARTNER -> actionNoResult(controllerWindow) { client.setPartner(value, id) }.setValue(uitem::isPartner)
            else -> throw RuntimeException("$fxId has not handler")
        }

        return if (result.isError) {
            showExceptionDialog(
                    "Изменение значений",
                    "",
                    "Неудачно",
                    result.error,
                    controllerWindow,
                    Modality.WINDOW_MODAL)
            log.error("", result.error)
            //table.refresh()
            false
        } else true
    }

    private fun loadData() {
        val result = actionResult(controllerWindow) {
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
        const val ROLES = "ROLES_col"
    }

    /**
     * Принцип работы - если все галочки стоят, то показываются все пользователи.
     * Если галочка Другие не отмечена, то если по остальным нет совпадений по True, то ничего не покажется, иначе покажутся остальные
     * Если все галочки отжаты, то ничего не показывается.
     */
    private inner class DataFilterPredicate : Predicate<AccountWithRoles> {
        override fun test(t: AccountWithRoles): Boolean {

            fun checkUser(t: AccountWithRoles): Boolean {
                return t.userSmallView.isSupport ||
                        t.userSmallView.isDoctor ||
                        t.userSmallView.isPartner ||
                        t.userSmallView.isBris ||
                        t.userSmallView.isDepot ||
                        t.userSmallView.isCompany
            }
            if (isRoot(t)) return false
            val checkFilterAnyBtnSelected =
                    supportBtn.isSelected && t.userSmallView.isSupport ||
                            doctorBtn.isSelected && t.userSmallView.isDoctor ||
                            partnerBtn.isSelected && t.userSmallView.isPartner ||
                            brisBtn.isSelected && t.userSmallView.isBris ||
                            depotBtn.isSelected && t.userSmallView.isDepot ||
                            companyBtn.isSelected && t.userSmallView.isCompany

            return when {
                checkFilterAnyBtnSelected -> return true
                othersBtn.isSelected -> !checkUser(t)
                else -> false
            }

        }

    }

    private inner class DataRolesFilterPredicate : Predicate<AccountWithRoles> {
        override fun test(t: AccountWithRoles): Boolean {
            val selectedRole: Role = rolesChoice.selectionModel.selectedItem ?: return true
            return t.roles.contains(selectedRole)
        }

    }

    /**
     * Позволяет генерировать события изменения списка при изменени указанных свойств объекта
     */
    fun extractor(): Callback<AccountWithRoles, Array<Observable>> {
        return Callback<AccountWithRoles, Array<Observable>> { p: AccountWithRoles ->
            arrayOf(
                    p.userSmallView.brisProperty(),
                    p.userSmallView.companyProperty(),
                    p.userSmallView.supportProperty(),
                    p.userSmallView.partnerProperty(),
                    p.userSmallView.depotProperty(),
                    p.userSmallView.doctorProperty(),
                    p.userSmallView.emailProperty(),
                    p.rolesProperty())
        }
    }

    fun checkAll() {
        filterCheckboxes.forEach { it.isSelected = true }
        onChangeFilter()
    }
}

class CallbackRolesTableCell() : Callback<TableColumn<AccountWithRoles, ObservableList<Role>>, TableCell<AccountWithRoles, ObservableList<Role>>> {
    override fun call(param: TableColumn<AccountWithRoles, ObservableList<Role>>?): TableCell<AccountWithRoles, ObservableList<Role>> {
        return CenteredRoleTableCell()
    }
}

class CallbackIdTableCell(private val onClick: (AccountWithRoles) -> Unit) : Callback<TableColumn<AccountWithRoles, Long>, TableCell<AccountWithRoles, Long>> {
    override fun call(param: TableColumn<AccountWithRoles, Long>?): TableCell<AccountWithRoles, Long> {
        return CenteredIdUserTableCell(onClick)
    }
}

class CallbackCenteredTextTableCell<T> : Callback<TableColumn<AccountWithRoles, T>, TableCell<AccountWithRoles, T>> {
    override fun call(param: TableColumn<AccountWithRoles, T>?): TableCell<AccountWithRoles, T> {
        return CenteredTextTableCell()
    }
}

class CallbackCenteredBooleanTableCell(private val disabled: Boolean = false, private val onChange: (AccountWithRoles, String, Boolean) -> Boolean) : Callback<TableColumn<AccountWithRoles, Boolean>, TableCell<AccountWithRoles, Boolean>> {
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


class CenteredBooleanTableCell(private val disabled: Boolean = false, private val onChange: (AccountWithRoles, String, Boolean) -> Boolean) : TableCell<AccountWithRoles, Boolean>() {
    private val checkBox: CheckBox = CheckBox()

    init {
        alignment = Pos.CENTER
        checkBox.isDisable = disabled
        checkBox.setOnAction {
            if (!onChange(tableRow.item as AccountWithRoles, tableColumn.id, checkBox.isSelected)) {
                checkBox.isSelected != checkBox.isSelected
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

class CenteredRoleTableCell() : TableCell<AccountWithRoles, ObservableList<Role>>() {

    init {
        alignment = Pos.CENTER
    }

    override fun updateItem(item: ObservableList<Role>?, empty: Boolean) {
        super.updateItem(item, empty)

        if (item != null && !empty) {
            when {
                item.contains(Role.ADMIN) -> text = Role.ADMIN.roleName
                item.contains(Role.DELETED) -> text = Role.DELETED.roleName
                item.contains(Role.BANNED) -> text = Role.BANNED.roleName
                item.contains(Role.USER) -> text = Role.USER.roleName
                item.contains(Role.NOT_APPROVED) -> text = Role.NOT_APPROVED.roleName
            }
        } else {
            text = null
            graphic = null
        }

    }
}


class CenteredIdUserTableCell(private val onClick: (AccountWithRoles) -> Unit) : TableCell<AccountWithRoles, Long>() {
    private val box = HBox()
    private val label: Label = Label()
    private val btn: Button = Button()

    init {
        alignment = Pos.CENTER
        label.maxWidth = Double.MAX_VALUE
        box.alignment = Pos.CENTER_LEFT
        HBox.setHgrow(label, Priority.ALWAYS)
        box.spacing = 5.0
        box.maxWidth = Double.MAX_VALUE
        box.children.addAll(label, btn)
        btn.prefHeight = 24.0
        btn.prefWidth = 24.0
        btn.styleClass.addAll("EditSmallBtn")
        btn.setOnAction {
            onClick(tableRow.item as AccountWithRoles)
        }
    }

    override fun updateItem(item: Long?, empty: Boolean) {
        super.updateItem(item, empty)

        if (item != null && !empty) {
            label.text = item.toString()
            graphic = box

        } else {
            text = null
            graphic = null
        }

    }
}
