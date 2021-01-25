package ru.biomedis.biomedismair3.social.registry

import feign.form.FormData
import javafx.application.Platform
import javafx.beans.Observable
import javafx.collections.FXCollections
import javafx.collections.transformation.SortedList
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.input.MouseButton
import javafx.scene.layout.HBox
import javafx.stage.FileChooser
import javafx.stage.Modality
import javafx.stage.WindowEvent
import javafx.util.Callback
import ru.biomedis.biomedismair3.BaseController
import ru.biomedis.biomedismair3.BlockingAction
import ru.biomedis.biomedismair3.social.remote_client.SocialClient
import ru.biomedis.biomedismair3.social.remote_client.dto.DirectoryData
import ru.biomedis.biomedismair3.social.remote_client.dto.FileData
import ru.biomedis.biomedismair3.social.remote_client.dto.IFileItem
import ru.biomedis.biomedismair3.utils.Other.LoggerDelegate
import ru.biomedis.biomedismair3.utils.Other.Result
import ru.biomedis.biomedismair3.utils.TabHolder
import java.net.URL
import java.util.*
import kotlin.Comparator


class FilesController : BaseController(), TabHolder.Selected, TabHolder.Detached {

    /**
     * Позволяет генерировать события изменения списка при изменени указанных свойств объекта
     */
    fun extractor(): Callback<IFileItem, Array<Observable>> {
        return Callback<IFileItem, Array<Observable>> { item: IFileItem ->
            arrayOf(
                item.nameProperty()
            )
        }
    }

    private val log by LoggerDelegate()

    @FXML
    lateinit var pathLine: HBox

    @FXML
    lateinit var container: ListView<IFileItem>

    @FXML
    lateinit var newDirBtn: Button

    @FXML
    lateinit var newFileBtn: Button

    private var currentDirectory: DirectoryData? = null

    private val items = FXCollections.observableArrayList<IFileItem>(extractor())
    private val sortedItems = SortedList(items)
    private val breadCrumbs = BreadCrumbs(this::loadDirectory)
    private val ctxListMenu = ContextMenu()

    override fun onCompletedInitialization() {

    }

    override fun onClose(event: WindowEvent) {

    }

    override fun setParams(vararg params: Any) {

    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        sortedItems.comparator = Comparator.comparing(IFileItem::directoryMarker).reversed()
            .thenComparing(Comparator.comparing(IFileItem::name))
        container.apply {
            items = sortedItems
            selectionModel.selectionMode = SelectionMode.MULTIPLE
            cellFactory = FileCellFactory()
            this.contextMenu = ctxListMenu
            setOnMouseClicked { event ->
                if (event.button === MouseButton.PRIMARY && event.clickCount == 2 && container.selectionModel.selectedItem is DirectoryData) {
                    val dstDir = container.selectionModel.selectedItem as DirectoryData
                    loadDirectory(dstDir)
                    breadCrumbs.destination(dstDir)
                }
//                else if (event.button === MouseButton.SECONDARY) {
//                    contextMenu.show(container, event.screenX, event.screenY)
//                }
            }
        }

        pathLine.children.add(breadCrumbs)
        initContextMenu()
    }

    private var cutedItems = mutableListOf<IFileItem>()
    private var cutedFromDirectory: DirectoryData? = null
    private fun initContextMenu() {
        val getLinkItem = MenuItem("Получить ссылку")//для разрешенных для этого типов
        val changeAccessItem = MenuItem("Права доступа")
        val renameItem = MenuItem("Переименовать")
        val cutItem = MenuItem("Вырезать")
        val pasteItem = MenuItem("Вставить")
        val deleteItem = MenuItem("Удалить")
        val clearItem = MenuItem("Очистить директорию")

        cutItem.setOnAction { cutItems() }
        pasteItem.setOnAction { pasteItems() }
        deleteItem.setOnAction { deleteItems() }
        clearItem.setOnAction { clearItem() }
        renameItem.setOnAction { renameItem() }
        changeAccessItem.setOnAction { changeAccessItems() }
        getLinkItem.setOnAction { getLinkItem() }

        ctxListMenu.items.addAll(
            changeAccessItem,
            renameItem,
            SeparatorMenuItem(),
            cutItem,
            pasteItem,
            deleteItem
        )
        ctxListMenu.setOnShowing {
            fun disableAll(disable: Boolean){
                getLinkItem.isDisable = disable
                changeAccessItem .isDisable = disable
                renameItem.isDisable = disable
                cutItem.isDisable = disable
                pasteItem.isDisable = disable
                deleteItem.isDisable = disable
                clearItem .isDisable = disable
            }

            fun checkPaste(){
                if(cutedItems.isNotEmpty()){//режим вырезания
                    pasteItem.isDisable = currentDirectory == cutedFromDirectory//нельзя вставить в туже директорию

                }else  pasteItem.isDisable = true
            }

            disableAll(false)
            //если ничего не выбрано то дизаблим все.
            //если выбрано несколько то недоступно очистить
            //если вырезаем то если выбран элемент из тех, что вырезан - не доступно
            //получить ссылку - доступно если выбранный элемент имеет доступный для этого тип доступа
            val selected = container.selectionModel.selectedItems
            if(selected.isEmpty()) {
                disableAll(true)
                checkPaste()
                return@setOnShowing
            }
            if(selected.size>1) clearItem.isDisable = true
            checkPaste()
            if(!selected.any { it is FileData }) getLinkItem.isDisable = true
            if(selected.size > 1) renameItem.isDisable = true


        }

    }

    private fun getLinkItem() {
        val selected = container.selectionModel.selectedItem
    }

    private fun changeAccessItems() {
        val selected = container.selectionModel.selectedItems.toList()

    }

    private fun renameItem() {
        val selected = container.selectionModel.selectedItem
        val newName = showTextInputDialog("Переименование","Введите новое имя","",selected.name, controllerWindow, Modality.WINDOW_MODAL)
        if(newName.isBlank() || newName==selected.name) return

       val result =  BlockingAction.actionNoResult(controllerWindow){
           if(selected is FileData) SocialClient.INSTANCE.filesClient.renameFile(newName, selected.id)
            else SocialClient.INSTANCE.filesClient.renameDirectory(newName, selected.id)
        }

        if(result.isError){
            log.error("", result.error)
            showWarningDialog("Переименование","Не удалось переименовать элемент","",controllerWindow, Modality.WINDOW_MODAL)
            return
        }

        selected.name = newName
   }

    private fun clearItem() {
        val selected = container.selectionModel.selectedItem


        cutedFromDirectory = null
        cutedItems.clear()

    }

    private fun deleteItems() {
        val selected = container.selectionModel.selectedItems.toList()

        val r = showConfirmationDialog("Удаление файлов","Выбранные файлы и директории будут удалены","Вы уверены?",controllerWindow, Modality.WINDOW_MODAL)
        if( !r.filter{ it==okButtonType}.isPresent) return
        val result = BlockingAction.actionNoResult(controllerWindow){
            val files = selected.filterIsInstance<FileData>().map { it.id }
            val dirs = selected.filterIsInstance<DirectoryData>().map { it.id }
            if(files.isNotEmpty())SocialClient.INSTANCE.filesClient.deleteFiles(files)
            if(dirs.isNotEmpty())SocialClient.INSTANCE.filesClient.deleteDirs(dirs)
        }
        cutedFromDirectory = null
        cutedItems.clear()
        if(result.isError){
            log.error("", result.error)
            showWarningDialog("Удаление","Ошибка удаления","Возможно удалены не все выбранные файлы",controllerWindow, Modality.WINDOW_MODAL)
            Platform.runLater(this::onSync)
            return
        }

        items.removeAll(selected)
    }

    private fun pasteItems() {
       val result =  BlockingAction.actionNoResult(controllerWindow){
                SocialClient.INSTANCE.filesClient.moveFiles(
                    cutedItems.filterIsInstance<FileData>().map { it.id },
                    currentDirectory?.id?:0L
            )
            SocialClient.INSTANCE.filesClient.moveDirectories(
                cutedItems.filterIsInstance<DirectoryData>().map { it.id },
                currentDirectory?.id?:0L
            )
        }
        if(result.isError){
            log.error("", result.error)
            showWarningDialog("Вырезать","Не удалось вырезать файлы", "", controllerWindow, Modality.WINDOW_MODAL)
            return
        }

        items.addAll(cutedItems)
        cutedItems.clear()
        cutedFromDirectory = null
    }

    private fun cutItems() {
        val selected = container.selectionModel.selectedItems.toList()
        cutedItems.clear()
        cutedItems.addAll(selected)
        cutedFromDirectory = currentDirectory
    }

    override fun onSelected() {
        loadDirectory()
    }

    override fun onDetach() {

    }

    private fun fillContainer(directories: List<DirectoryData>, files: List<FileData>) {
        items.clear()
        items.addAll(directories)
        items.addAll(files)
        files.forEach { FileData.fillThumbnailImage(it) }
    }


    private fun loadDirectory(dstDir: DirectoryData? = null): Boolean {

        val result: Result<Pair<List<DirectoryData>, List<FileData>>> =
            BlockingAction.actionResult(controllerWindow) {
                if (dstDir == null) {
                    SocialClient.INSTANCE.filesClient.getRootDirectories() to
                            SocialClient.INSTANCE.filesClient.getRootFiles()
                } else SocialClient.INSTANCE.filesClient.getDirectories(dstDir.id) to
                        SocialClient.INSTANCE.filesClient.getFilesAllType(dstDir.id)
            }

        return if (result.isError) {
            log.error("", result.error)
            showWarningDialog(
                "Загрузка информации о файлах",
                "",
                "Загрузка не удалась",
                controllerWindow,
                Modality.WINDOW_MODAL
            )
            false
        } else {
            changeDirectory(dstDir, result.value.first, result.value.second)
            true
        }
    }

    private fun changeDirectory(
        targetDir: DirectoryData?,
        directories: List<DirectoryData>,
        files: List<FileData>
    ) {
        fillContainer(directories, files)
        currentDirectory = targetDir
    }

    @FXML
    private fun onNewDir() {
        val textInput: String = showTextInputDialog(
            "Создание папки",
            "Введите имя папки",
            "",
            "",
            controllerWindow,
            Modality.WINDOW_MODAL
        ).trim()

        if (textInput.isBlank()) {
            return
        }

        if (textInput.length > 50) {
            showWarningDialog(
                "Создание папки",
                "",
                "Имя папки не должно быть длиннее 50 символов",
                controllerWindow,
                Modality.WINDOW_MODAL
            )
            return
        }

        val result = BlockingAction.actionResult(controllerWindow) {
            if (currentDirectory == null) SocialClient.INSTANCE.filesClient.createInRootDirectory(
                textInput.trim()
            )
            else SocialClient.INSTANCE.filesClient.createDirectory(
                textInput.trim(),
                currentDirectory!!.id
            )
        }

        if (result.isError) {
            log.error("", result.error)
            showWarningDialog(
                "Создание новой папки",
                "",
                "Не удалось создать папку",
                controllerWindow,
                Modality.WINDOW_MODAL
            )
            return
        }

        addDirectoryToContainer(result.value)

    }

    private fun addDirectoryToContainer(dir: DirectoryData) {
        items.add(dir)
    }

    @FXML
    private fun onNewFile() {
        val fileChooser = FileChooser()
        fileChooser.title = "Выбор файлов"
        fileChooser.extensionFilters.addAll(
            FileChooser.ExtensionFilter("Все файлы", "*.*"),
            FileChooser.ExtensionFilter(
                "Архивы",
                "*.rar",
                "*.gzip",
                "*.7z",
                "*.bzip",
                "*.tar",
                "*.gz"
            ),
            FileChooser.ExtensionFilter("Документы", "*.doc", "*.docx", "*.xls", "*.xlsx", "*.pdf"),
            FileChooser.ExtensionFilter("Изображения", "*.jpg", "*.jpeg", "*.png", "*.gif"),
            FileChooser.ExtensionFilter("Biomedis", "*.xmlc", "*.xmlb", "*.xmlp")
        )
        val files = fileChooser.showOpenMultipleDialog(controllerWindow)


        val newFiles = BlockingAction.actionResult(controllerWindow) {
            val result = mutableListOf<FileData>()
            val notLoader = mutableListOf<String>()
            files.forEachIndexed { index, f ->
                try {
                    val file = SocialClient.INSTANCE.uploadFilesClient
                        .uploadFile(
                            FormData("file${index + 1}", f.name, f.readBytes()),
                            currentDirectory?.id ?: 0
                        )
                        FileData.fillThumbnailImage(file)
                    result.add(file)
                } catch (e: Exception) {
                    log.error("Ошибка загрузки файла", e)
                    notLoader.add(f.name)
                }
            }
            result to notLoader
        }

        if (newFiles.value.second.isNotEmpty()) {
            showWarningDialog(
                "Загрузка файлов",
                "Некоторые файлы не загружены",
                newFiles.value.second.joinToString("\n"), controllerWindow,
                Modality.WINDOW_MODAL
            )
        }
        items.addAll(newFiles.value.first)
    }

    @FXML
    private fun onSync() {
        if(!loadDirectory(currentDirectory)){
            if(currentDirectory!=null) loadDirectory()//при ошибке вернемся в корень
        }

        breadCrumbs.clear()
    }


}
