package ru.uniyar.web.models

import org.http4k.lens.WebForm
import org.http4k.template.ViewModel
import ru.uniyar.authorization.Permissions
import ru.uniyar.authorization.User
import ru.uniyar.web.handlers.rolesList

class EditUserDataVM(
    val user: User,
    val form: WebForm,
    val failures: List<String> = emptyList(),
    val roles: List<Permissions> = rolesList,
) : ViewModel
