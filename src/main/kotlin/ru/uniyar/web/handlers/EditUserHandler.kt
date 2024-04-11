package ru.uniyar.web.handlers

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.FOUND
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.with
import ru.uniyar.authorization.Users
import ru.uniyar.web.models.EditUserDataVM
import ru.uniyar.web.templates.ContextAwareViewRender

class EditUserHandler(
    val users: Users,
    val lens: ContextAwareViewRender,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val userId =
            lensOrNull(userIdLens, request)
                ?: return Response(NOT_FOUND).with(lens(request) of errorModel)
        val user = users.findUserById(userId) ?: return Response(NOT_FOUND).with(lens(request) of errorModel)
        val form = editRoleFormLens(request)
        if (form.errors.isNotEmpty()) {
            val failures = formFailureInfoList(form.errors)
            val model = EditUserDataVM(user, form, failures)
            return Response(BAD_REQUEST).with(lens(request) of model)
        }
        val role = roleField(form)
        val newPermissions = rolesList.find { it.name == role } ?: rolesList.get(1)
        users.editUser(userId, user.copy(role = newPermissions))
        return Response(FOUND).header("Location", "/users")
    }
}
