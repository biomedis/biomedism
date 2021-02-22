package ru.biomedis.biomedismair3.social.registry

import javafx.collections.FXCollections
import javafx.collections.transformation.FilteredList
import javafx.collections.transformation.SortedList
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import javafx.scene.input.MouseButton
import javafx.scene.layout.HBox
import javafx.stage.DirectoryChooser
import javafx.stage.Modality
import javafx.stage.WindowEvent
import ru.biomedis.biomedismair3.BaseController
import ru.biomedis.biomedismair3.BlockingAction
import ru.biomedis.biomedismair3.social.remote_client.SocialClient
import ru.biomedis.biomedismair3.social.remote_client.dto.*
import ru.biomedis.biomedismair3.utils.Other.LoggerDelegate
import ru.biomedis.biomedismair3.utils.Other.Result
import ru.biomedis.biomedismair3.utils.TabHolder
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.function.Predicate
import kotlin.Comparator


class UserFilesViewerController : BaseController(), TabHolder.Selected, TabHolder.Detached {


    private  var contact: Long=0

    private val log by LoggerDelegate()

    @FXML
    lateinit var typeFileFilter: ChoiceBox<String>

    @FXML
    lateinit var pathLine: HBox

    @FXML
    lateinit var container: ListView<IFileItem>


    private var currentDirectory: DirectoryData? = null

    private val items = FXCollections.observableArrayList<IFileItem>()
    private val sortedItems = SortedList(items)
    private val filteredItems = FilteredList(sortedItems)
    private val breadCrumbs = BreadCrumbs(this::loadDirectory)
    private val ctxListMenu = ContextMenu()
    private val fileTypeFilterExtensions = FileTypeByExtension()

    override fun onCompletedInitialization() {

    }

    override fun onClose(event: WindowEvent) {

    }

    override fun setParams(vararg params: Any) {
        if(params.isEmpty()) {
            log.error("Должен быть параметр userID: Long")
            throw RuntimeException("Должен быть параметр userId: Long")
        }
        contact = params[0] as Long
    }

    private class FilterFileTypePredicate( val typeFileFilter: ChoiceBox<String>, val extMap: FileTypeByExtension): Predicate<IFileItem>{
        override fun test(t: IFileItem): Boolean {
            return  if(typeFileFilter.value == "-" || t is DirectoryData)  true
            else {
                val nameType = extMap.map.get((t as FileData).extension)
                if(nameType == null){//файл не из списка
                    typeFileFilter.value == extMap.others//хотели ли отобразить файлы не из списка
                }else {
                    typeFileFilter.value == nameType
                }
            }
        }

    }



    override fun initialize(location: URL?, resources: ResourceBundle?) {

        sortedItems.comparator = Comparator.comparing(IFileItem::directoryMarker).reversed()
            .thenComparing(Comparator.comparing(IFileItem::name))

        filteredItems.predicate = FilterFileTypePredicate(typeFileFilter, fileTypeFilterExtensions)

        container.apply {
            items = filteredItems
            selectionModel.selectionMode = SelectionMode.MULTIPLE
            cellFactory = FileViewerCellFactory( this@UserFilesViewerController::onDownload)
            this.contextMenu = ctxListMenu
            setOnMouseClicked { event ->
                if (event.button === MouseButton.PRIMARY && event.clickCount == 2 && container.selectionModel.selectedItem is DirectoryData) {
                    val dstDir = container.selectionModel.selectedItem as DirectoryData
                    loadDirectory(dstDir)
                    breadCrumbs.destination(dstDir)
                }
            }
        }

        pathLine.children.add(breadCrumbs)
        initContextMenu()
        initFilter()
    }



    private fun initFilter() {

        typeFileFilter.valueProperty().addListener{
                _,n,o->
            filteredItems.predicate = FilterFileTypePredicate(typeFileFilter, fileTypeFilterExtensions)
        }
        typeFileFilter.items.add("-")
        typeFileFilter.items.addAll(fileTypeFilterExtensions.typeNames())
        typeFileFilter.value = typeFileFilter.items.first()
    }


    private fun initContextMenu() {

        val copyLinksItem = MenuItem("Копировать ссылок")

        copyLinksItem.setOnAction { copyLinks() }

        ctxListMenu.items.addAll(
            copyLinksItem
        )
        ctxListMenu.setOnShowing {
            fun disableAll(disable: Boolean) {
                copyLinksItem.isDisable = disable
            }
            disableAll(false)

            val selected = container.selectionModel.selectedItems
            if (selected.isEmpty()) {
                disableAll(true)
                return@setOnShowing
            }

            if (!selected.any { it is FileData }) {

                copyLinksItem.isDisable = true
            }



        }

    }

    private fun copyLinks() {
        val selected = container.selectionModel.selectedItems
        if(selected.isEmpty()) return
        val links = selected
            .asSequence()
            .filterIsInstance<FileData>()
            .filter { it.accessType!=AccessVisibilityType.PRIVATE }
            .groupBy { it.accessType }
            .map { group ->
                val title = when (group.key) {
                    AccessVisibilityType.PUBLIC -> "Общедоступные"
                    AccessVisibilityType.PROTECTED -> "Доступные в программе"
                    AccessVisibilityType.BY_LINK -> "Доступные по приватным ссылкам"
                    else -> ""
                }

                when (group.key) {
                    AccessVisibilityType.PUBLIC -> group.value.map { it.publicLink }
                    AccessVisibilityType.PROTECTED -> group.value.map { it.publicLink }
                    AccessVisibilityType.BY_LINK -> group.value.map { it.privateLink }
                    else -> listOf()
                }.filter { it.isNotEmpty() }
                    .joinToString("\n","$title:\n","\n\n")

        }.filter { it.isNotEmpty() }
            .joinToString("\n")

        if(links.isEmpty()) return
        val clipboard = Clipboard.getSystemClipboard().apply { clear()}
        ClipboardContent().let {
            it.putString(links)
            clipboard.setContent(it)
        }

        showInfoDialog("Получение ссылок","Ссылки скопированы в буфер обмена","", controllerWindow, Modality.WINDOW_MODAL)

    }

    private fun onDownload(file: FileData){
        val chooser = DirectoryChooser()
        chooser.title = "Выбор директории для сохранения файла"
        val dir: Path = chooser.showDialog(controllerWindow)?.toPath() ?: return

        val result: Result<ByteArray> = BlockingAction.actionResult(controllerWindow) {
            val fileClient =  SocialClient.INSTANCE.filesClient
            when (file.accessType) {
                AccessVisibilityType.PUBLIC -> fileClient.downloadFilePublic(file.id)
                AccessVisibilityType.PROTECTED ->fileClient.downloadProtectedFile(file.id)
                else -> throw RuntimeException("Попытка загрузить файл не подходящего типа доступа")
            }.body().asInputStream().readBytes()
        }

        if(result.isError){
            log.error("", result.error)
            showWarningDialog("Загрузка файла","","Загрузка файла не удалась", controllerWindow, Modality.WINDOW_MODAL)
            return
        }

        val filePath = dir.resolve("${file.name}.${file.extension}")
        if(Files.exists(filePath)){
            val dialog = showConfirmationDialog(
                "Загрузка файла",
                "",
                "Файл ${file.name}.${file.extension} уже существует. Перезаписать?",
                controllerWindow,
                Modality.WINDOW_MODAL
            )
            if(dialog.isPresent){
                if(dialog.get() != okButtonType) return
            }else return
        }


           try {
               filePath.toFile().writeBytes(result.value)
               showInfoDialog("загрузка файла","","Файл успешно сохранен",controllerWindow, Modality.WINDOW_MODAL)
           } catch (e: Exception){
               log.error("", e)
                showExceptionDialog(
                    "Загрузка файла",
                    "",
                    "Запись файла на диск не удалась",
                    e,
                    controllerWindow,
                    Modality.WINDOW_MODAL)
               return
           }


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
                    SocialClient.INSTANCE.filesClient.getRootDirectories(contact) to
                            SocialClient.INSTANCE.filesClient.getRootFiles(contact)
                } else SocialClient.INSTANCE.filesClient.getDirectories(dstDir.id, contact) to
                        SocialClient.INSTANCE.filesClient.getFilesAllType(dstDir.id, contact)
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
    private fun onSync() {
        if (!loadDirectory(currentDirectory)) {
            if (currentDirectory != null) loadDirectory()//при ошибке вернемся в корень
        }

        breadCrumbs.clear()
    }
}

