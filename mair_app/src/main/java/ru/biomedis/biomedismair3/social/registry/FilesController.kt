package ru.biomedis.biomedismair3.social.registry

import feign.form.FormData
import javafx.application.Platform
import javafx.beans.Observable
import javafx.beans.property.SimpleObjectProperty
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
import javafx.stage.FileChooser
import javafx.stage.Modality
import javafx.stage.WindowEvent
import javafx.util.Callback
import javafx.util.StringConverter
import ru.biomedis.biomedismair3.AppController
import ru.biomedis.biomedismair3.BaseController
import ru.biomedis.biomedismair3.BlockingAction
import ru.biomedis.biomedismair3.social.BiomedisFilesExportManager
import ru.biomedis.biomedismair3.social.remote_client.SocialClient
import ru.biomedis.biomedismair3.social.remote_client.dto.*
import ru.biomedis.biomedismair3.utils.Other.LoggerDelegate
import ru.biomedis.biomedismair3.utils.Other.Result
import ru.biomedis.biomedismair3.utils.TabHolder
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.function.Predicate
import kotlin.Comparator


class FilesController : BaseController(), TabHolder.Selected, TabHolder.Detached,
    BiomedisFilesExportManager {

    /**
     * Позволяет генерировать события изменения списка при изменени указанных свойств объекта
     */
    fun extractor(): Callback<IFileItem, Array<Observable>> {
        return Callback<IFileItem, Array<Observable>> { item: IFileItem ->
            if(item is FileData) arrayOf(
                item.nameProperty(),
                item.publicLinkProperty(),
                item.privateLinkProperty()
            )else arrayOf(
                item.nameProperty()
            )
        }
    }

    private lateinit var res: ResourceBundle
    private val log by LoggerDelegate()

    @FXML
    lateinit var typeFilter: ChoiceBox<AccessTypeDto>

    @FXML
    lateinit var typeFileFilter: ChoiceBox<String>

    @FXML
    lateinit var pathLine: HBox

    @FXML
    lateinit var container: ListView<IFileItem>

    @FXML
    lateinit var newDirBtn: Button

    @FXML
    lateinit var newFileBtn: Button

    @FXML
    lateinit var storageSummaryText: Label

    @FXML
    lateinit var currentStorageSizeText: Label

    @FXML
    lateinit var  currentStorageValueIndicator: ProgressBar

    private var currentDirectory: DirectoryData? = null

    private val items = FXCollections.observableArrayList<IFileItem>(extractor())
    private val sortedItems = SortedList(items)
    private val filteredItems = FilteredList(sortedItems)
    private val breadCrumbs = BreadCrumbs(this::loadDirectory)
    private val ctxListMenu = ContextMenu()
    private var cutedItems = mutableListOf<IFileItem>()
    private var cutedFromDirectory: DirectoryData? = null
    private val fileTypeFilterExtensions = FileTypeByExtension()

    private val storageValue: SimpleObjectProperty<StorageDto> = SimpleObjectProperty()

   private val directoryAccessList = listOf<AccessTypeDto>(
        AccessTypeDto("Личное", AccessVisibilityType.PRIVATE),
        AccessTypeDto("Доступно пользователям программы и по ссылкам в сети( в тексте сообщений и постов )", AccessVisibilityType.PUBLIC),
        AccessTypeDto("Доступно пользователям программы в том числе в тексте сообщений и ленте", AccessVisibilityType.PROTECTED)  //доступно по ссылкам публично, но не видно в профиле пользователя( для ресурсов, которые указываются в сообщениях и ленте)

    )
    private val fileAccessList = listOf<AccessTypeDto>(
         AccessTypeDto("Доступно по специальным ссылкам",AccessVisibilityType.BY_LINK)//приватны, но можно получать публично по ссылке - код используется
    )

    private val allAccessList = listOf(*directoryAccessList.toTypedArray(), *fileAccessList.toTypedArray())


    override fun onCompletedInitialization() {
        SocialClient.INSTANCE.setBiomedisExportFilesManager(this)
    }

    override fun onClose(event: WindowEvent) {

    }

    override fun setParams(vararg params: Any) {

    }

    private class FilterPredicate(val typeFilter: ChoiceBox<AccessTypeDto>, val fileTypePredicate: FilterFileTypePredicate): Predicate<IFileItem>{
        override fun test(t: IFileItem): Boolean {
          return  if(typeFilter.value.name == "-" ) fileTypePredicate.test(t)
            else  (t.accessType==typeFilter.value.type) && fileTypePredicate.test(t)
        }

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

    private lateinit var fileTypePredicate: FilterFileTypePredicate

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        res = resources?:throw RuntimeException()
        sortedItems.comparator = Comparator.comparing(IFileItem::directoryMarker).reversed()
            .thenComparing(Comparator.comparing(IFileItem::name))

        fileTypePredicate = FilterFileTypePredicate(typeFileFilter, fileTypeFilterExtensions)

        filteredItems.predicate = FilterPredicate(typeFilter, fileTypePredicate)

        container.apply {
            items = filteredItems
            selectionModel.selectionMode = SelectionMode.MULTIPLE
            cellFactory = FileCellFactory(this@FilesController::onGetLink, this@FilesController::onDownload)
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
        initFilter()
        initStorageValueIndicator()
    }

    private fun initStorageValueIndicator() {
        storageValue.addListener { _, _, newValue ->
            currentStorageValueIndicator.progress = newValue.occupiedVolume.toDouble()/newValue.availableVolume.toDouble()
            currentStorageSizeText.text = String.format("%.2f"+res.getString("app.mb"), newValue.occupiedVolume.toDouble())
            storageSummaryText.text = String.format("Доступно %.2f${res.getString("app.mb")}. Всего %.2f${res.getString("app.mb")}.",
                newValue.availableVolume - newValue.occupiedVolume, newValue.availableVolume)
        }
    }


    private fun initFilter() {
        typeFilter.items.add(AccessTypeDto("-", AccessVisibilityType.PRIVATE))
        typeFilter.items.addAll(allAccessList)
        typeFilter.converter = object: StringConverter<AccessTypeDto>() {
            override fun toString(item: AccessTypeDto): String {
                return item.name
            }

            override fun fromString(string: String): AccessTypeDto {
                return typeFilter.items.first { it.name==string }
            }

        }

        typeFilter.value = typeFilter.items.first()
        typeFilter.valueProperty().addListener{
                _,n,o->
            filteredItems.predicate = FilterPredicate(typeFilter, fileTypePredicate)
        }



        typeFileFilter.valueProperty().addListener{
                _,n,o->
            filteredItems.predicate = FilterPredicate(typeFilter, fileTypePredicate)
        }
        typeFileFilter.items.add("-")
        typeFileFilter.items.addAll(fileTypeFilterExtensions.typeNames())
        typeFileFilter.value = typeFileFilter.items.first()
    }


    private fun initContextMenu() {
        val reloadPrivateLinkItem = MenuItem("Изменить приватную ссылку")//для разрешенных для этого типов
        val changeAccessItem = MenuItem("Права доступа")
        val renameItem = MenuItem("Переименовать")
        val cutItem = MenuItem("Вырезать")
        val pasteItem = MenuItem("Вставить")
        val deleteItem = MenuItem("Удалить")
        val copyLinksItem = MenuItem("Копировать ссылки")
        val importProfileItem = MenuItem("Импорт файлов профилей")
        val importComplexesItem = MenuItem("Импорт файлов комплексов")
        val importFreqBaseItem = MenuItem("Импорт файлов базы частот")
        val importBackupItem = MenuItem("Импорт файла резервного сохранения")

        cutItem.setOnAction { cutItems() }
        pasteItem.setOnAction { pasteItems() }
        deleteItem.setOnAction { deleteItems() }
        renameItem.setOnAction { renameItem() }
        changeAccessItem.setOnAction { changeAccessItems() }
        reloadPrivateLinkItem.setOnAction { reloadPrivateLinkItem() }
        copyLinksItem.setOnAction { copyLinks() }
        importProfileItem.setOnAction { importProfile() }
        importComplexesItem.setOnAction { importComplexes() }
        importFreqBaseItem.setOnAction { importFreqBase() }
        importBackupItem.setOnAction { importBackup() }

        ctxListMenu.items.addAll(
            changeAccessItem,
            renameItem,
            reloadPrivateLinkItem,
            copyLinksItem,
            SeparatorMenuItem(),
            cutItem,
            pasteItem,
            deleteItem,
            SeparatorMenuItem(),
            importProfileItem,
            importComplexesItem,
            importFreqBaseItem,
            importBackupItem
        )
        ctxListMenu.setOnShowing {
            fun disableAll(disable: Boolean) {
                reloadPrivateLinkItem.isDisable = disable
                changeAccessItem.isDisable = disable
                renameItem.isDisable = disable
                cutItem.isDisable = disable
                pasteItem.isDisable = disable
                deleteItem.isDisable = disable
                copyLinksItem.isDisable = disable
                importProfileItem.isDisable = disable
                importComplexesItem.isDisable = disable
                importFreqBaseItem.isDisable = disable
                importBackupItem.isDisable = disable
            }

            fun checkPaste() {
                if (cutedItems.isNotEmpty()) {//режим вырезания
                    pasteItem.isDisable =
                        currentDirectory == cutedFromDirectory//нельзя вставить в туже директорию

                } else pasteItem.isDisable = true
            }

            disableAll(false)
            //если ничего не выбрано то дизаблим все.
            //если выбрано несколько то недоступно очистить
            //если вырезаем то если выбран элемент из тех, что вырезан - не доступно
            //получить ссылку - доступно если выбранный элемент имеет доступный для этого тип доступа
            val selected = container.selectionModel.selectedItems
            if (selected.isEmpty()) {
                disableAll(true)
                checkPaste()
                return@setOnShowing
            }

            checkPaste()
            if (!selected.any { it is FileData }) {
                reloadPrivateLinkItem.isDisable = true
                copyLinksItem.isDisable = true
            }
            if (selected.size > 1) renameItem.isDisable = true

            if(selected.size ==1 && selected.first() is FileData) reloadPrivateLinkItem.isDisable = false

            if(selected.isNotEmpty()){
                val biomedisFiles = selected.filterIsInstance<FileData>().filter{it.type == FileType.BIOMEDIS}
                importProfileItem.isDisable =  !biomedisFiles.any { it.extension=="xmlp" }
                importFreqBaseItem.isDisable = !biomedisFiles.any { it.extension=="xmlb" }
                importComplexesItem.isDisable = !biomedisFiles.any { it.extension=="xmlc" }

                importBackupItem.isDisable = !biomedisFiles.any { it.extension=="brecovery" } && biomedisFiles.size > 1

            }

        }

    }

    private fun importBackup() {

        val dir = app.tmpDir.toPath()

        val recoveryFile = container.selectionModel.selectedItem
        if(!(recoveryFile is FileData && recoveryFile.extension=="brecovery")){
            showWarningDialog("Импорт файла восстановления","Импорт не удался","Выбран файл не верного типа",controllerWindow, Modality.WINDOW_MODAL)
            return
        }
        getApp().recursiveDeleteTMP()
        val downloaded = downloadFiles(listOf(recoveryFile), dir)
        if(downloaded.isError){
            showWarningDialog("Импорт файла восстановления","Импорт  не удался","Не удалось загрузить файлы с сервера",controllerWindow, Modality.WINDOW_MODAL)
            log.error("", downloaded.error)
            return
        }

        if(downloaded.value.isEmpty()){
            showWarningDialog("Импорт файла восстановления","Импорт не удался","Не удалось загрузить файлы с сервера",controllerWindow, Modality.WINDOW_MODAL)
            log.error("Пустой список загрузки")
            return
        }


        getAppController().recoveryLoad(downloaded.value.keys.first())
    }

    private fun importFreqBase() {
        val leftAPI = AppController.getLeftAPI();
        val dir = app.tmpDir.toPath()

        val userBaseFiles = container.selectionModel.selectedItems
            .filterIsInstance<FileData>()
            .filter { it.extension=="xmlb" }

        if(userBaseFiles.isEmpty()){
            showWarningDialog("Импорт пользовательской базы частот","Импорт не удался","В списке выбранных файлов нет файлов пользовательской базы частот",controllerWindow, Modality.WINDOW_MODAL)

            return
        }

        val downloadedComplexes = downloadFiles(userBaseFiles, dir)
        if(downloadedComplexes.isError){
            showWarningDialog("Импорт пользовательской базы частот","Импорт  не удался","Не удалось загрузить файлы с сервера",controllerWindow, Modality.WINDOW_MODAL)
            log.error("", downloadedComplexes.error)
            return
        }

        if(downloadedComplexes.value.isEmpty()){
            showWarningDialog("Импорт пользовательской базы частот","Импорт не удался","Не удалось загрузить файлы с сервера",controllerWindow, Modality.WINDOW_MODAL)
            log.error("Пустой список загрузки")
            return
        }

        val msg = if(downloadedComplexes.value.size>1) {
            """Будет произведен импорт нескольких файлов базы частот.
            Если произойдет ошибка импорта для файла, то остальные файлы продолжат импортироваться."""
        }else ""

        val msg2 = if("USER" != leftAPI.selectedBase().tag) {
            """Если вы забыли выбрать раздел в пользовательской базе для импорта, то импорт произойдет в корневой раздел!"""
        }else ""

      if(msg.isNotEmpty() || msg2.isNotEmpty()) showInfoDialog("Импорт пользовательской базы частот",
            "",
           msg,controllerWindow, Modality.WINDOW_MODAL)

        downloadedComplexes.value.forEach {
            leftAPI.importUserBase(it.key, it.value)
        }
    }

    private fun importComplexes() {
        val profileAPI = AppController.getProfileAPI()
        val complexAPI = AppController.getComplexAPI()
        val selectedProfile = profileAPI.selectedProfile()
        if(selectedProfile==null) {
            showWarningDialog("Импорт комплексов","Не выбран профиль","Для импорта комплексов необходимо выбрать профиль во вкладке профилей", controllerWindow, Modality.WINDOW_MODAL)
            return
        }
        val dir = app.tmpDir.toPath()
        val complexesFiles = container.selectionModel.selectedItems
            .filterIsInstance<FileData>()
            .filter { it.extension=="xmlc" }

        if(complexesFiles.isEmpty()){
            showWarningDialog("Импорт комплексов","Импорт комплексов не удался","В списке выбранных файлов нет комплексов",controllerWindow, Modality.WINDOW_MODAL)

            return
        }

        val downloadedComplexes = downloadFiles(complexesFiles, dir)
        if(downloadedComplexes.isError){
            showWarningDialog("Импорт комплексов","Импорт комплексов не удался","Не удалось загрузить файлы с сервера",controllerWindow, Modality.WINDOW_MODAL)
            log.error("", downloadedComplexes.error)
            return
        }

        if(downloadedComplexes.value.isEmpty()){
            showWarningDialog("Импорт комплексов","Импорт комплексов не удался","Не удалось загрузить файлы с сервера",controllerWindow, Modality.WINDOW_MODAL)
            log.error("Пустой список загрузки")
            return
        }

        complexAPI.importTherapyComplex(selectedProfile, downloadedComplexes.value.keys.toList()){
           downloadedComplexes.value.keys.forEach {  Files.delete(it) }
        }


    }

    private fun importProfile() {
        val dir = app.tmpDir.toPath()
        val profileFiles = container.selectionModel.selectedItems
            .filterIsInstance<FileData>()
            .filter { it.extension=="xmlp" }

        if(profileFiles.isEmpty()){
            showWarningDialog("Импорт профилей","Импорт профилей не удался","В списке выбранных файлов нет профилей",controllerWindow, Modality.WINDOW_MODAL)

            return
        }

        val downloadedProfiles = downloadFiles(profileFiles, dir)
        if(downloadedProfiles.isError){
            showWarningDialog("Импорт профилей","Импорт профилей не удался","Не удалось загрузить файлы с сервера",controllerWindow, Modality.WINDOW_MODAL)
            log.error("", downloadedProfiles.error)
            return
        }

        if(downloadedProfiles.value.isEmpty()){
            showWarningDialog("Импорт профилей","Импорт профилей не удался","Не удалось загрузить файлы с сервера",controllerWindow, Modality.WINDOW_MODAL)
            log.error("Пустой список загрузки")
            return
        }

        val profileAPI = AppController.getProfileAPI()

        profileAPI.importProfiles(
            downloadedProfiles.value.keys.toList(),
            downloadedProfiles.value
            ){
            downloadedProfiles.value.keys.forEach{
                Files.delete(it)       //удаление временных файлов
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

    //скопирует в буффер обмена ссылку и сообщит об этом
    private fun onGetLink(link: String, type: AccessVisibilityType){
        val clipboard = Clipboard.getSystemClipboard().apply { clear()}
        ClipboardContent().let {
            it.putString(link)
            clipboard.setContent(it)
       }

        showInfoDialog("Получение ссылки","Ссылка скопирована в буфер обмена","", controllerWindow, Modality.WINDOW_MODAL)
    }


    private fun onDownload(file: FileData){
        val chooser = DirectoryChooser()
        chooser.title = "Выбор директории для сохранения файла"
        val dir: Path = chooser.showDialog(controllerWindow)?.toPath() ?: return

        val result: Result<ByteArray> = BlockingAction.actionResult(controllerWindow) {
            SocialClient.INSTANCE.filesClient.downloadFile(file.id).body().asInputStream().readBytes()
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

    private fun reloadPrivateLinkItem() {
        val selected = container.selectionModel.selectedItems
        if(selected.size!=1) return
        BlockingAction.actionResult<String>(controllerWindow){
            SocialClient.INSTANCE.filesClient.reloadPrivateLink(selected.first().id)
        }.let {
            if(it.isError){
                showWarningDialog("Регенерация ссылки","","Регенерация не удалась", controllerWindow, Modality.WINDOW_MODAL)
            }else {
                (selected.first() as FileData).privateLinkProperty().set(it.value)
                showInfoDialog("Регенерация сслыки","","Ссылка успешно изменена", controllerWindow, Modality.WINDOW_MODAL)
            }
        }
    }

    private fun changeAccessItems() {
        val selected = container.selectionModel.selectedItems.toList()
        if (selected.isEmpty()) return
        val accessType: AccessVisibilityType = when (selected.size) {
            1 -> selected.first().accessType
            else -> AccessVisibilityType.PRIVATE
        }
        val choiceList = mutableListOf<AccessTypeDto>().apply { addAll(directoryAccessList) }
        if (!selected.all { it is DirectoryData }) {
            choiceList.addAll(fileAccessList)
        }

        val defaultChoice = choiceList.first { it.type == accessType }

        val choice = showChoiceDialog(
            "Права доступа к файлам и папкам",
            """Назначение доступа применяется к вложенным папкам и файлам. ${"\n"}Папки не доступны по ссылкам. ${"\n"}
                    Если при смешанном выборе(файлы и папки) будет выбран доступ по ссылке, ${"\n"}
                    то выбранные папки и вложенные файлы получат личный доступ""".trimMargin(),
            "",
            choiceList,
            defaultChoice,
            controllerWindow,
            Modality.WINDOW_MODAL
        )

        val result = BlockingAction.actionResult(controllerWindow) {
            val files = selected.filterIsInstance<FileData>().map { it.id }
            val directories = selected.filterIsInstance<DirectoryData>().map { it.id }

            if (directories.isNotEmpty()) {
                if (choice.type == AccessVisibilityType.BY_LINK) {
                    SocialClient.INSTANCE.filesClient.changeDirAccessType(
                        AccessVisibilityType.PRIVATE.name,
                        directories
                    )
                } else SocialClient.INSTANCE.filesClient.changeDirAccessType(
                    choice.type.name,
                    directories
                )
            }

            if (files.isNotEmpty()) {
                return@actionResult SocialClient.INSTANCE.filesClient.changeFileAccessType(choice.type.name, files)
            }

            return@actionResult emptyMap<Long, Links>()

        }

        if (result.isError) {
            log.error("", result.error)
            showWarningDialog(
                "Изменение прав доступа",
                "Не удалось изменить права доступа",
                "",
                controllerWindow,
                Modality.WINDOW_MODAL
            )
            return
        }

        selected.forEach {
            it.accessType = choice.type
            if(it is FileData){
                it.privateLink = result.value[it.id]?.privateLink?:""
                it.publicLink = result.value[it.id]?.publicLink?:""
            }
        }
    }

    private fun renameItem() {
        val selected = container.selectionModel.selectedItem
        if (selected == null) return
        val newName = showTextInputDialog(
            "Переименование",
            "Введите новое имя",
            "",
            selected.name,
            controllerWindow,
            Modality.WINDOW_MODAL
        )
        if (newName.isBlank() || newName == selected.name) return

        val result = BlockingAction.actionNoResult(controllerWindow) {
            if (selected is FileData) SocialClient.INSTANCE.filesClient.renameFile(
                newName,
                selected.id
            )
            else SocialClient.INSTANCE.filesClient.renameDirectory(newName, selected.id)
        }

        if (result.isError) {
            log.error("", result.error)
            showWarningDialog(
                "Переименование",
                "Не удалось переименовать элемент",
                "",
                controllerWindow,
                Modality.WINDOW_MODAL
            )
            return
        }

        selected.name = newName
    }


    private fun deleteItems() {
        val selected = container.selectionModel.selectedItems.toList()
        if (selected.isEmpty()) return
        val r = showConfirmationDialog(
            "Удаление файлов",
            "Выбранные файлы и директории будут удалены",
            "Вы уверены?",
            controllerWindow,
            Modality.WINDOW_MODAL
        )
        if (!r.filter { it == okButtonType }.isPresent) return
        val result = BlockingAction.actionNoResult(controllerWindow) {
            val files = selected.filterIsInstance<FileData>().map { it.id }
            val dirs = selected.filterIsInstance<DirectoryData>().map { it.id }
            if (files.isNotEmpty()) SocialClient.INSTANCE.filesClient.deleteFiles(files)
            if (dirs.isNotEmpty()) SocialClient.INSTANCE.filesClient.deleteDirs(dirs)
        }
        cutedFromDirectory = null
        cutedItems.clear()
        if (result.isError) {
            log.error("", result.error)
            showWarningDialog(
                "Удаление",
                "Ошибка удаления",
                "Возможно удалены не все выбранные файлы",
                controllerWindow,
                Modality.WINDOW_MODAL
            )
            Platform.runLater(this::onSync)
            return
        }

        items.removeAll(selected)
    }

    private fun pasteItems() {
        val result = BlockingAction.actionNoResult(controllerWindow) {
            SocialClient.INSTANCE.filesClient.moveFiles(
                cutedItems.filterIsInstance<FileData>().map { it.id },
                currentDirectory?.id ?: 0L
            )
            SocialClient.INSTANCE.filesClient.moveDirectories(
                cutedItems.filterIsInstance<DirectoryData>().map { it.id },
                currentDirectory?.id ?: 0L
            )
        }
        if (result.isError) {
            log.error("", result.error)
            showWarningDialog(
                "Вырезать",
                "Не удалось вырезать файлы",
                "",
                controllerWindow,
                Modality.WINDOW_MODAL
            )
            return
        }

        items.addAll(cutedItems)
        cutedItems.clear()
        cutedFromDirectory = null
    }

    private fun cutItems() {
        val selected = container.selectionModel.selectedItems.toList()
        if (selected.isEmpty()) return
        cutedItems.clear()
        cutedItems.addAll(selected)
        cutedFromDirectory = currentDirectory
    }

    override fun onSelected() {
        loadDirectory()
        loadStorageValue()
    }

    override fun onDetach() {

    }

    private fun loadStorageValue(){
        val result: Result<StorageDto> = BlockingAction.actionResult(controllerWindow){
            SocialClient.INSTANCE.filesClient.storageValue()
        }
        if(result.isError){
            log.error("", result.error)
            showWarningDialog(
                "Получение информации о доступном пространстве",
                "",
                "Загрузка не удалась. Попробуйте перезапустить программу.",
                controllerWindow,
                Modality.WINDOW_MODAL
            )
            return
        }

        storageValue.set(result.value)
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
        if (!loadDirectory(currentDirectory)) {
            if (currentDirectory != null) loadDirectory()//при ошибке вернемся в корень
        }
        loadStorageValue()
        breadCrumbs.clear()

    }

    data class AccessTypeDto(val name: String, val type: AccessVisibilityType) {
        override fun toString(): String {
            return name
        }
    }

    override fun exportProfile(name: String, profile: String) {
        uploadFileFromText(name, "xmlp", profile)
    }

    override fun exportUserBaseDirectory(name: String, userBase: String) {
        uploadFileFromText(name, "xmlb", userBase)
    }

    override fun exportComplex(name: String, complex: String) {
        uploadFileFromText(name, "xmlc", complex)
    }

    override fun exportBackup(file: File) {
        uploadFile(file)
    }

    private fun uploadFileFromText(name: String, ext: String, text: String){
        val newFiles = BlockingAction.actionResult(controllerWindow) {
            val result = mutableListOf<FileData>()

            val file = SocialClient.INSTANCE.uploadFilesClient
                .uploadFile(
                    FormData("file", "$name.$ext", text.encodeToByteArray()),
                    currentDirectory?.id ?: 0
                )
            FileData.fillThumbnailImage(file)
            result.add(file)

            result
        }
        if(newFiles.isError) throw RuntimeException(newFiles.error)

        items.addAll(newFiles.value)
    }

    private fun uploadFile(targetFile:File){
        val newFiles = BlockingAction.actionResult(controllerWindow) {
            val result = mutableListOf<FileData>()

            val file = SocialClient.INSTANCE.uploadFilesClient
                .uploadFile(
                    FormData("file", targetFile.name, targetFile.readBytes()),
                    currentDirectory?.id ?: 0
                )
            FileData.fillThumbnailImage(file)
            result.add(file)

            result
        }
        if(newFiles.isError) throw RuntimeException(newFiles.error)

        items.addAll(newFiles.value)
    }

    /**
     * Скачает файлы в указанную директорию, к именованию файлов применит функцию [nameAction], по умолчанию случайное имя и нормальное расширение
     * Вернет список скачанных файлов
     * Если где-то возникла ошибка, то закачка буде прекращена
     * return пары путь и имя файла на сервере без расширения
     */
    private fun downloadFiles(
        files: List<FileData>, toDir: Path,
        nameAction: (FileData) -> String = { "${UUID.randomUUID()}.${it.extension}" }
    ): Result<Map<Path, String>> {
        return BlockingAction.actionResult(controllerWindow) {
            val paths = mutableMapOf<Path, String>()
            try {
                files.forEach {
                    val fBytes =
                        SocialClient.INSTANCE.filesClient.downloadFile(it.id).body().asInputStream()
                            .readBytes()
                    val filePath = toDir.resolve(nameAction(it))
                    filePath.toFile().writeBytes(fBytes)
                    paths[filePath] = it.name
                }
            }catch (e: Exception){
                paths.forEach{ Files.delete(it.key)}
                throw e
            }

            paths
        }
    }
}

