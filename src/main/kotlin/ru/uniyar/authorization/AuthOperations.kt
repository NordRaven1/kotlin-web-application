package ru.uniyar.authorization

import ru.uniyar.config.readAuthSaltFromConfiguration
import java.security.MessageDigest
import java.util.HexFormat
import kotlin.system.exitProcess

val commaFormat = HexFormat.of()

fun addUser(
    users: Users,
    username: String,
    password: String,
    role: Permissions,
) {
    val hexPassInStr = formHexPass(password)
    val newUser = User(username, hexPassInStr, role)
    users.add(newUser)
}

fun formHexPass(password: String): String {
    val messageDigest = MessageDigest.getInstance("SHA-256")
    val saltConfig = readAuthSaltFromConfiguration() ?: exitProcess(0)
    val passAndSalt = password + saltConfig.salt
    val hexPass = passAndSalt.toByteArray(Charsets.UTF_8)
    val digest = messageDigest.digest(hexPass)
    return commaFormat.formatHex(digest)
}

fun authUser(
    users: Users,
    username: String,
    password: String,
): Boolean {
    val user = users.findUserByName(username) ?: return false
    return user.password == formHexPass(password)
}

fun formSharedState(
    users: Users,
    userId: String,
): SharedState? {
    val user = users.findUserById(userId) ?: return null
    return SharedState(user.userId, user.userName)
}
