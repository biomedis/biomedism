package ru.biomedis.biomedismair3.social.remote_client.dto

import java.time.Instant
import java.util.*

class FileData {
    var id: Long=-1
    var createdDate: Date = Date.from(Instant.now())
    var name: String=""
    var extension: String=""
    var directory: Long?=null
    var type: FileType = FileType.FILE
}

class DirectoryData{
    var id: Long=-1
    var name: String=""
    var parent: Long?=null
}

class UserNameDto{var id: Long=-1; var name: String=""}

