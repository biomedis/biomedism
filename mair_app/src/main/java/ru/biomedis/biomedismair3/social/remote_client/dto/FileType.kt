package ru.biomedis.biomedismair3.social.remote_client.dto

import java.util.regex.Pattern

enum class FileType(val type: String) {
    FILE("files"),
    IMAGE("images"),
    BIOMEDIS("biomedis");

    companion object {

        private val IMAGE_PATTERN = Pattern.compile(".+(\\.(jpg)|(png)|(gif))$")
        private val BIOMEDIS_PATTERN = Pattern.compile(".+(\\.(xmlb)|(xmlp)|(xmlc)|(tdump)|(bdump))$")
        /*
        xmlb - файл польз. базы программы MAir
        xmlp - файл профиля  программы MAir
        xmlc  - файл комплекса(в)  программы MAir
        tdump - файл дамп для записи в тринити
        bdump - файл дамп для записи в биофон
         */
        fun fromFileName(fileName: String): FileType = when {
                IMAGE_PATTERN.matcher(fileName).matches() -> IMAGE
                BIOMEDIS_PATTERN.matcher(fileName).matches() -> BIOMEDIS
                else -> FILE
        }

    }

    override fun toString(): String {
        return type
    }


}
