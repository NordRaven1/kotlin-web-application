package ru.uniyar.web.models

import org.http4k.template.ViewModel
import ru.uniyar.authorization.User

class UserListVM(
    val users: List<User>,
) : ViewModel
