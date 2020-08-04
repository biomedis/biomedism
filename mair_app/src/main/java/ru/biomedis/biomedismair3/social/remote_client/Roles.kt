package ru.biomedis.biomedismair3.social.remote_client

import java.util.*

enum class Role(val value: String, val roleName: String ) {
    ADMIN(UserRoles.ADMIN,"Администратор"),
    USER(UserRoles.USER,"Пользователь"),
    NOT_APPROVED(UserRoles.NOT_APPROVED,"Не подтвержден"),
    BANNED(UserRoles.BANNED,"Забанен"),
    DELETED(UserRoles.DELETED,"Удален");

    companion object {
        @JvmStatic
        fun byName(name: String): Optional<Role> {
            for (v in values()) {
                if (v.value == name) {
                    return Optional.of(v)
                }
            }
            return Optional.empty()
        }

        @JvmStatic
        fun byNames(vararg names: String): Set<Role> {
            val namesSet = setOf(*names)
            return values().filter { namesSet.contains(it.value) }.toSet()
        }
        @JvmStatic
        fun setByListNames(names:Iterable<String>): Set<Role> {
            val namesSet = names.toHashSet()
            return values().filter { namesSet.contains(it.value) }.toSet()
        }

        @JvmStatic
        fun listByListNames(names:Iterable<String>): List<Role> {
            val namesSet = names.toHashSet()
            return values().filter { namesSet.contains(it.value) }.distinct().toList()
        }

        @JvmStatic
        fun setByArrayNames(names:Array<String>): Set<Role> {
            val namesSet = names.toHashSet()
            return values().filter { namesSet.contains(it.value) }.toSet()
        }

        @JvmStatic
        fun listByArrayNames(names:Array<String>): List<Role> {
            val namesSet = names.toHashSet()
            return values().filter { namesSet.contains(it.value) }.distinct().toList()
        }

        @JvmStatic
        fun allRoles() = values()
    }
}

object UserRoles {
    const val ADMIN = "ROLE_ADMIN"
    const val USER = "ROLE_USER"
    const val NOT_APPROVED = "ROLE_NOT_APPROVED"
    const val BANNED = "ROLE_BANNED"
    const val DELETED = "ROLE_DELETED"
}
