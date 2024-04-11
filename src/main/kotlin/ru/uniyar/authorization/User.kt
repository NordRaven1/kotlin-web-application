package ru.uniyar.authorization

import java.util.UUID

data class User(var userName: String, var password: String, var role: Permissions) {
    var userId = UUID.randomUUID().toString()
}
