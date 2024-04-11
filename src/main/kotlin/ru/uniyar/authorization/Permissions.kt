package ru.uniyar.authorization

data class Permissions(
    val name: String,
    var canAddTheme: Boolean = false,
    var canEditTheme: Boolean = false,
    var canDeleteTheme: Boolean = false,
    var canAddMessage: Boolean = false,
    var canEditMessage: Boolean = false,
    var canDeleteMessage: Boolean = false,
    var canAddReaction: Boolean = false,
    var canDeleteReaction: Boolean = false,
    var canChangeStatus: Boolean = false,
)
