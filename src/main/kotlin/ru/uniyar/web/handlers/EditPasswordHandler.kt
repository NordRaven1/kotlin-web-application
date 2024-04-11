package ru.uniyar.web.handlers

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.FOUND
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.with
import ru.uniyar.authorization.Users
import ru.uniyar.authorization.formHexPass
import ru.uniyar.web.models.EditUserPasswordVM
import ru.uniyar.web.templates.ContextAwareViewRender

class EditPasswordHandler(
    val users: Users,
    val lens: ContextAwareViewRender,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val userId =
            lensOrNull(userIdLens, request)
                ?: return Response(NOT_FOUND).with(lens(request) of errorModel)
        val user = users.findUserById(userId) ?: return Response(NOT_FOUND).with(lens(request) of errorModel)
        val form = editPasswordFormLens(request)
        if (form.errors.isNotEmpty()) {
            val failures = formFailureInfoList(form.errors)
            val model = EditUserPasswordVM(form, failures)
            return Response(BAD_REQUEST).with(lens(request) of model)
        }
        val pass1 = passField(form)
        val pass2 = pass2Field(form)
        if (pass1 != pass2) {
            val failures = formFailureInfoList(form.errors)
            failures.add("Пароли не сходятся")
            val model = EditUserPasswordVM(form, failures)
            return Response(BAD_REQUEST).with(lens(request) of model)
        }
        val newPassword = formHexPass(pass1)
        users.editUser(userId, user.copy(password = newPassword))
        return Response(FOUND).header("Location", "/users")
    }
}
