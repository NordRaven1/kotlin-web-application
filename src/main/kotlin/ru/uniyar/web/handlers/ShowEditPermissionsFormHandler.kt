package ru.uniyar.web.handlers

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.FORBIDDEN
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.lens.RequestContextLens
import org.http4k.lens.WebForm
import ru.uniyar.authorization.Permissions
import ru.uniyar.authorization.Users
import ru.uniyar.web.models.EditPermissionsVM
import ru.uniyar.web.templates.ContextAwareViewRender

class ShowEditPermissionsFormHandler(
    val users: Users,
    val lens: ContextAwareViewRender,
    val permissionLens: RequestContextLens<Permissions>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val role = permissionLens(request)
        if (role.name != "ADMIN") return Response(FORBIDDEN)
        val userId =
            lensOrNull(userIdLens, request)
                ?: return Response(NOT_FOUND).with(lens(request) of errorModel)
        val user = users.findUserById(userId) ?: return Response(NOT_FOUND).with(lens(request) of errorModel)
        val addT = if (user.role.canAddTheme) "on" else "down"
        val editT = if (user.role.canEditTheme) "on" else "down"
        val delT = if (user.role.canDeleteTheme) "on" else "down"
        val addM = if (user.role.canAddMessage) "on" else "down"
        val editM = if (user.role.canEditMessage) "on" else "down"
        val delM = if (user.role.canDeleteMessage) "on" else "down"
        val addR = if (user.role.canAddReaction) "on" else "down"
        val delR = if (user.role.canDeleteReaction) "on" else "down"
        val changeStatus = if (user.role.canChangeStatus) "on" else "down"
        val form =
            WebForm().with(
                userAddThemeField of addT,
                userEditThemeField of editT,
                userDeleteThemeField of delT,
                userAddMessageField of addM,
                userEditMessageField of editM,
                userDeleteMessageField of delM,
                userAddReactionField of addR,
                userDeleteReactionField of delR,
                userChangeStatusField of changeStatus,
            )
        val model = EditPermissionsVM(form)
        return Response(OK).with(lens(request) of model)
    }
}
