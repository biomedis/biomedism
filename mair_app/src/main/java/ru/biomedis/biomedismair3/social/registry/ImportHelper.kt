package ru.biomedis.biomedismair3.social.registry

import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import javafx.stage.Modality
import javafx.stage.Stage
import ru.biomedis.biomedismair3.App
import ru.biomedis.biomedismair3.AppController
import ru.biomedis.biomedismair3.BaseController
import ru.biomedis.biomedismair3.BlockingAction
import ru.biomedis.biomedismair3.social.remote_client.SocialClient
import ru.biomedis.biomedismair3.social.remote_client.dto.AccessVisibilityType.*
import ru.biomedis.biomedismair3.social.remote_client.dto.FileData
import ru.biomedis.biomedismair3.social.remote_client.dto.IFileItem
import ru.biomedis.biomedismair3.utils.Other.LoggerDelegate
import ru.biomedis.biomedismair3.utils.Other.Result
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

class ImportHelper(val app: App, val res: ResourceBundle) {

    lateinit var controllerWindow: Stage

    private val log by LoggerDelegate()

    fun importBackup(selections: IFileItem) {

        val dir = app.tmpDir.toPath()

        if (!(selections is FileData && selections.extension == "brecovery")) {
            BaseController.showWarningDialog(
                "Импорт файла восстановления",
                "Импорт не удался",
                "Выбран файл не верного типа",
                controllerWindow,
                Modality.WINDOW_MODAL
            )
            return
        }
        app.recursiveDeleteTMP()
        val downloaded = downloadFiles(listOf(selections), dir)
        if (downloaded.isError) {
            BaseController.showWarningDialog(
                "Импорт файла восстановления",
                "Импорт  не удался",
                "Не удалось загрузить файлы с сервера",
                controllerWindow,
                Modality.WINDOW_MODAL
            )
            log.error("", downloaded.error)
            return
        }

        if (downloaded.value.isEmpty()) {
            BaseController.showWarningDialog(
                "Импорт файла восстановления",
                "Импорт не удался",
                "Не удалось загрузить файлы с сервера",
                controllerWindow,
                Modality.WINDOW_MODAL
            )
            log.error("Пустой список загрузки")
            return
        }


        BaseController.getAppController().recoveryLoad(downloaded.value.keys.first())
    }

    fun importFreqBase(selections: List<IFileItem>) {
        val leftAPI = AppController.getLeftAPI();
        val dir = app.tmpDir.toPath()

        val userBaseFiles = selections
            .filterIsInstance<FileData>()
            .filter { it.extension == "xmlb" }

        if (userBaseFiles.isEmpty()) {
            BaseController.showWarningDialog(
                "Импорт пользовательской базы частот",
                "Импорт не удался",
                "В списке выбранных файлов нет файлов пользовательской базы частот",
                controllerWindow,
                Modality.WINDOW_MODAL
            )

            return
        }

        val downloadedComplexes = downloadFiles(userBaseFiles, dir)
        if (downloadedComplexes.isError) {
            BaseController.showWarningDialog(
                "Импорт пользовательской базы частот",
                "Импорт  не удался",
                "Не удалось загрузить файлы с сервера",
                controllerWindow,
                Modality.WINDOW_MODAL
            )
            log.error("", downloadedComplexes.error)
            return
        }

        if (downloadedComplexes.value.isEmpty()) {
            BaseController.showWarningDialog(
                "Импорт пользовательской базы частот",
                "Импорт не удался",
                "Не удалось загрузить файлы с сервера",
                controllerWindow,
                Modality.WINDOW_MODAL
            )
            log.error("Пустой список загрузки")
            return
        }

        val msg = if (downloadedComplexes.value.size > 1) {
            """Будет произведен импорт нескольких файлов базы частот.
            Если произойдет ошибка импорта для файла, то остальные файлы продолжат импортироваться."""
        } else ""

        val msg2 = if ("USER" != leftAPI.selectedBase().tag) {
            """Если вы забыли выбрать раздел в пользовательской базе для импорта, то импорт произойдет в корневой раздел!"""
        } else ""

        if (msg.isNotEmpty() || msg2.isNotEmpty()) BaseController.showInfoDialog(
            "Импорт пользовательской базы частот",
            "",
            msg, controllerWindow, Modality.WINDOW_MODAL
        )

        downloadedComplexes.value.forEach {
            leftAPI.importUserBase(it.key, it.value)
        }
    }

    fun importComplexes(selections: List<IFileItem>) {
        val profileAPI = AppController.getProfileAPI()
        val complexAPI = AppController.getComplexAPI()
        val selectedProfile = profileAPI.selectedProfile()
        if (selectedProfile == null) {
            BaseController.showWarningDialog(
                "Импорт комплексов",
                "Не выбран профиль",
                "Для импорта комплексов необходимо выбрать профиль во вкладке профилей",
                controllerWindow,
                Modality.WINDOW_MODAL
            )
            return
        }
        val dir = app.tmpDir.toPath()
        val complexesFiles = selections
            .filterIsInstance<FileData>()
            .filter { it.extension == "xmlc" }

        if (complexesFiles.isEmpty()) {
            BaseController.showWarningDialog(
                "Импорт комплексов",
                "Импорт комплексов не удался",
                "В списке выбранных файлов нет комплексов",
                controllerWindow,
                Modality.WINDOW_MODAL
            )

            return
        }

        val downloadedComplexes = downloadFiles(complexesFiles, dir)
        if (downloadedComplexes.isError) {
            BaseController.showWarningDialog(
                "Импорт комплексов",
                "Импорт комплексов не удался",
                "Не удалось загрузить файлы с сервера",
                controllerWindow,
                Modality.WINDOW_MODAL
            )
            log.error("", downloadedComplexes.error)
            return
        }

        if (downloadedComplexes.value.isEmpty()) {
            BaseController.showWarningDialog(
                "Импорт комплексов",
                "Импорт комплексов не удался",
                "Не удалось загрузить файлы с сервера",
                controllerWindow,
                Modality.WINDOW_MODAL
            )
            log.error("Пустой список загрузки")
            return
        }

        complexAPI.importTherapyComplex(selectedProfile, downloadedComplexes.value.keys.toList()) {
            downloadedComplexes.value.keys.forEach { Files.delete(it) }
        }


    }

    fun importProfile(selections: List<IFileItem>) {
        val dir = app.tmpDir.toPath()
        val profileFiles = selections
            .filterIsInstance<FileData>()
            .filter { it.extension == "xmlp" }

        if (profileFiles.isEmpty()) {
            BaseController.showWarningDialog(
                "Импорт профилей",
                "Импорт профилей не удался",
                "В списке выбранных файлов нет профилей",
                controllerWindow,
                Modality.WINDOW_MODAL
            )

            return
        }

        val downloadedProfiles = downloadFiles(profileFiles, dir)
        if (downloadedProfiles.isError) {
            BaseController.showWarningDialog(
                "Импорт профилей",
                "Импорт профилей не удался",
                "Не удалось загрузить файлы с сервера",
                controllerWindow,
                Modality.WINDOW_MODAL
            )
            log.error("", downloadedProfiles.error)
            return
        }

        if (downloadedProfiles.value.isEmpty()) {
            BaseController.showWarningDialog(
                "Импорт профилей",
                "Импорт профилей не удался",
                "Не удалось загрузить файлы с сервера",
                controllerWindow,
                Modality.WINDOW_MODAL
            )
            log.error("Пустой список загрузки")
            return
        }

        val profileAPI = AppController.getProfileAPI()

        profileAPI.importProfiles(
            downloadedProfiles.value.keys.toList(),
            downloadedProfiles.value
        ) {
            downloadedProfiles.value.keys.forEach {
                Files.delete(it)       //удаление временных файлов
            }
        }


    }

    fun copyLinks(selections: List<IFileItem>) {

        val selected = selections
        if (selected.isEmpty()) return
        val links = selected
            .asSequence()
            .filterIsInstance<FileData>()
            .filter { it.accessType != PRIVATE }
            .groupBy { it.accessType }
            .map { group ->
                val title = when (group.key) {
                    PUBLIC -> "Общедоступные"
                    PROTECTED -> "Доступные в программе"
                    BY_LINK -> "Доступные по приватным ссылкам"
                    else -> ""
                }

                when (group.key) {
                    PUBLIC -> group.value.map { it.publicLink }
                    PROTECTED -> group.value.map { it.publicLink }
                    BY_LINK -> group.value.map { it.privateLink }
                    else -> listOf()
                }.filter { it.isNotEmpty() }
                    .joinToString("\n", "$title:\n", "\n\n")

            }.filter { it.isNotEmpty() }
            .joinToString("\n")

        if (links.isEmpty()) return
        val clipboard = Clipboard.getSystemClipboard().apply { clear() }
        ClipboardContent().let {
            it.putString(links)
            clipboard.setContent(it)
        }

        BaseController.showInfoDialog(
            "Получение ссылок",
            "Ссылки скопированы в буфер обмена",
            "",
            controllerWindow,
            Modality.WINDOW_MODAL
        )

    }

    /**
     * Скачает файлы в указанную директорию, к именованию файлов применит функцию [nameAction], по умолчанию случайное имя и нормальное расширение
     * Вернет список скачанных файлов
     * Если где-то возникла ошибка, то закачка буде прекращена
     * return пары путь и имя файла на сервере без расширения
     */
    fun downloadFiles(
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
            } catch (e: Exception) {
                paths.forEach { Files.delete(it.key) }
                throw e
            }

            paths
        }
    }

}
