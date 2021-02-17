package ru.biomedis.biomedismair3.social.link_service

import javafx.stage.DirectoryChooser
import javafx.stage.Modality
import ru.biomedis.biomedismair3.App
import ru.biomedis.biomedismair3.BaseController
import ru.biomedis.biomedismair3.BlockingAction
import ru.biomedis.biomedismair3.DefaultBrowserCaller
import ru.biomedis.biomedismair3.social.remote_client.FilesClient
import ru.biomedis.biomedismair3.social.remote_client.SocialClient
import ru.biomedis.biomedismair3.utils.Other.LoggerDelegate
import ru.biomedis.biomedismair3.utils.Other.Result
import java.nio.file.Files
import java.nio.file.Path

class LinkService {
    companion object {
        private val log by LoggerDelegate()
        private const val PROTECTED_MARKER = "file_protected/"
        private const val PUBLIC_MARKER = "file/"
        private const val BY_LINK_MARKER = "file?key="

        /**
         * Обычные ссылки откроет в браузере, ссылки из программы скачает(определит публичные и защищенные)
         */
        fun useLink(link: String, context: BaseController, fileService: FilesClient) {
            if (link.contains(SocialClient.getApiURL())) downloadFromService(
                link,
                context,
                fileService
            )
            else openInBrowser(link, context, fileService)
        }

        private fun openInBrowser(link: String, context: BaseController, fileService: FilesClient) {
            DefaultBrowserCaller.openInBrowser(link, App.getApp())
        }

        private fun downloadFromService(
            link: String,
            context: BaseController,
            fileService: FilesClient
        ) {
            val chooser = DirectoryChooser()
            chooser.title = "Выбор директории для сохранения файла"
            val dir: Path = chooser.showDialog(context.controllerWindow)?.toPath() ?: return

            val result: Result<Pair<ByteArray, String>> = when {
                link.contains(PROTECTED_MARKER) -> protectedDownload(link, context, fileService)
                link.contains(BY_LINK_MARKER) -> byLinkDownload(link, context, fileService)
                link.contains(PUBLIC_MARKER) -> publicDownload(link, context, fileService)
                else -> {
                    openInBrowser(link, context, fileService)
                    return
                }
            }

            if (result.isError) {
                log.error("", result.error)
                BaseController.showWarningDialog(
                    "Загрузка файла",
                    "",
                    "Загрузка файла не удалась",
                    context.controllerWindow,
                    Modality.WINDOW_MODAL
                )
                return
            }

            val filePath = dir.resolve(result.value.second)
            if (Files.exists(filePath)) {
                val dialog = BaseController.showConfirmationDialog(
                    "Загрузка файла",
                    "",
                    "Файл ${result.value.second} уже существует. Перезаписать?",
                    context.controllerWindow,
                    Modality.WINDOW_MODAL
                )
                if (dialog.isPresent) {
                    if (dialog.get() != BaseController.okButtonType) return
                } else return
            }


            try {
                filePath.toFile().writeBytes(result.value.first)
                BaseController.showInfoDialog(
                    "загрузка файла",
                    "",
                    "Файл успешно сохранен",
                    context.controllerWindow,
                    Modality.WINDOW_MODAL
                )
            } catch (e: Exception) {
                log.error("", e)
                BaseController.showExceptionDialog(
                    "Загрузка файла",
                    "",
                    "Запись файла на диск не удалась",
                    e,
                    context.controllerWindow,
                    Modality.WINDOW_MODAL
                )
                return
            }
        }

        private fun publicDownload(
            link: String,
            context: BaseController,
            fileService: FilesClient
        ): Result<Pair<ByteArray, String>> {
            val index: Int = link.indexOf(PUBLIC_MARKER)
            if (index <= 0) throw Exception("Не верная ссылка на публичный файл")
            val id: Long = link.substring(index + PUBLIC_MARKER.length).toLong()
            return BlockingAction.actionResult(context.controllerWindow) {
                fileService.downloadFilePublic(id).body().asInputStream()
                    .readBytes() to
                        fileService.fileName(id)
            }
        }

        private fun byLinkDownload(
            link: String,
            context: BaseController,
            fileService: FilesClient
        ): Result<Pair<ByteArray, String>> {
            val index: Int = link.indexOf(BY_LINK_MARKER)
            if (index <= 0) throw Exception("Не верная ссылка на файл по ссылке")
            val key: String = link.substring(index + BY_LINK_MARKER.length)
            return BlockingAction.actionResult(context.controllerWindow) {
                fileService.downloadFileByLink(key).body().asInputStream().readBytes() to
                        fileService.fileNameByLink(key)
            }
        }

        private fun protectedDownload(
            link: String,
            context: BaseController,
            fileService: FilesClient
        ): Result<Pair<ByteArray, String>> {
            val index: Int = link.indexOf(PROTECTED_MARKER)
            if (index <= 0) throw Exception("Не верная ссылка на защищенный файл")
            val id: Long = link.substring(index + PROTECTED_MARKER.length).toLong()
            return BlockingAction.actionResult(context.controllerWindow) {
                fileService.downloadFile(id).body().asInputStream()
                    .readBytes() to
                        fileService.fileName(id)
            }

        }

    }
}
