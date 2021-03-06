package ru.biomedis.biomedismair3.social

import java.io.File

/**
 * Экспорт на сервер файлов программы в выбранную в файловом менеджере папку. Если папка не выбрана, то в корневую
 */
interface BiomedisFilesExportManager {
    fun exportProfile(name: String, profile: String)
    fun exportUserBaseDirectory(name: String, userBase: String)
    fun exportComplex(name: String, complex: String)
    fun exportBackup(file: File);
}
