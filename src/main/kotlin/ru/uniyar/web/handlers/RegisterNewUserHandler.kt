package ru.uniyar.web.handlers

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.FOUND
import org.http4k.core.with
import ru.uniyar.authorization.Users
import ru.uniyar.authorization.addUser
import ru.uniyar.web.models.NewUserPageVM
import ru.uniyar.web.templates.ContextAwareViewRender

class RegisterNewUserHandler(
    val users: Users,
    val lens: ContextAwareViewRender,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val form = userFormLens(request)
        if (form.errors.isNotEmpty()) {
            val failures = formFailureInfoList(form.errors)
            val model = NewUserPageVM(form, failures)
            return Response(BAD_REQUEST).with(lens(request) of model)
        }
        val name = userNameField(form)
        val pass1 = passField(form)
        val pass2 = pass2Field(form)
        val roleName = roleField(form)
        val role = rolesList.find { it.name == roleName } ?: rolesList.get(1)
        if (pass1 != pass2) {
            val failures = formFailureInfoList(form.errors)
            failures.add("Пароли не сходятся")
            val model = NewUserPageVM(form, failures)
            return Response(BAD_REQUEST).with(lens(request) of model)
        }
        if (users.findUserByName(name) != null) {
            val failures = formFailureInfoList(form.errors)
            failures.add("Невозможно зарегистрировать пользователя с таким именем")
            val model = NewUserPageVM(form, failures)
            return Response(BAD_REQUEST).with(lens(request) of model)
        }
        addUser(users, name, pass1, role)
        return Response(FOUND).header("Location", "/")
    }
}
