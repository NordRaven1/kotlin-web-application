package ru.uniyar.web.handlers

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.FOUND
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.with
import ru.uniyar.authorization.Users
import ru.uniyar.web.models.EditPermissionsVM
import ru.uniyar.web.templates.ContextAwareViewRender

class EditPermissionsHandler(
    val users: Users,
    val lens: ContextAwareViewRender,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val userId =
            lensOrNull(userIdLens, request)
                ?: return Response(NOT_FOUND).with(lens(request) of errorModel)
        val user = users.findUserById(userId) ?: return Response(NOT_FOUND).with(lens(request) of errorModel)
        val form = editPermissionsFormLens(request)
        if (form.errors.isNotEmpty()) {
            val failures = formFailureInfoList(form.errors)
            val model = EditPermissionsVM(form, failures)
            return Response(BAD_REQUEST).with(lens(request) of model)
        }
        val addT = userAddThemeField(form) != null
        val editT = userEditThemeField(form) != null
        val delT = userDeleteThemeField(form) != null
        val addM = userAddMessageField(form) != null
        val editM = userEditMessageField(form) != null
        val delM = userDeleteMessageField(form) != null
        val addR = userAddReactionField(form) != null
        val delR = userDeleteReactionField(form) != null
        val changeStatus = userChangeStatusField(form) != null
        val roleName = user.role.name
        val newPermissions = user.role.copy(roleName, addT, editT, delT, addM, editM, delM, addR, delR, changeStatus)
        users.editUser(userId, user.copy(role = newPermissions))
        return Response(FOUND).header("Location", "/users")
    }
}
