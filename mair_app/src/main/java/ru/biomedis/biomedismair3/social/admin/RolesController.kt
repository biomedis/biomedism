package ru.biomedis.biomedismair3.social.admin

import javafx.fxml.FXML
import javafx.scene.control.ContentDisplay
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.SelectionMode
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.stage.WindowEvent
import javafx.util.Callback
import ru.biomedis.biomedismair3.BaseController
import ru.biomedis.biomedismair3.social.remote_client.Role
import ru.biomedis.biomedismair3.utils.Other.LoggerDelegate
import java.io.IOException
import java.net.URL
import java.util.*


class RolesController : BaseController() {

    @FXML
    private lateinit var rolesList: ListView<Role>
    private var selectedList = mutableListOf<Role>()
    private lateinit var userData: Data
    private var cancel = true

    private val log by LoggerDelegate()

    override fun setParams(vararg params: Any) {
        val p = listOf(*params)
        if (p.isEmpty()) throw RuntimeException("Не верные параметры. Должен быть параметр списка ролей для установки")
        (p[0] as List<*>).forEach { selectedList.add(it as Role) }

    }

    override fun onClose(event: WindowEvent) {
        if(cancel) userData.roles.apply {
            clear()
            addAll(selectedList)
        }
    }

    override fun onCompletedInitialization() {
        userData = inputDialogData as Data
        selectedList.forEach { rolesList.selectionModel.select(it) }
    }

    override fun initialize(location: URL, resources: ResourceBundle) {
        rolesList.selectionModel.selectionMode = SelectionMode.MULTIPLE
        rolesList.cellFactory = RoleCellFactory()
        Role.allRoles().forEach { rolesList.items.add(it) }
    }

    fun onApply() {
        userData.roles.clear()
        rolesList.selectionModel.selectedItems.forEach { userData.roles.add(it) }
        cancel = false
        controllerWindow.close()
    }

    companion object {

        /**
         * Вернет список, актуальный. Он может совпадать с исходным, если небыло изменений
         */
        @Throws(IOException::class)
        @JvmStatic
        fun openRolesDialog(context: Stage, roles: List<Role>): List<Role> {

            val result: Data = openDialogUserData(
                    context,
                    "/fxml/Roles.fxml",
                    "Настройка ролей пользователя",
                    false,
                    StageStyle.UTILITY,
                    0, 0, 0, 0,
                    Data(),
                    roles
            )
            return result.roles
        }
    }

    class Data() {
        var roles: MutableList<Role> = mutableListOf()
    }

    class RoleCellFactory : Callback<ListView<Role?>, ListCell<Role?>> {
        override fun call(param: ListView<Role?>): ListCell<Role?> {
            return RoleCell()
        }
    }

    class RoleCell : ListCell<Role?>() {

        override fun updateItem(item: Role?, empty: Boolean) {
            super.updateItem(item, empty)
            contentDisplay = ContentDisplay.TEXT_ONLY
            text = if (empty || item == null) null  else item.roleName
        }


    }
}
