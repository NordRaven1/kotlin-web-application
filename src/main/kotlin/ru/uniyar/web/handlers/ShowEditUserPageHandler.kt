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
import ru.uniyar.web.models.EditUserDataVM
import ru.uniyar.web.templates.ContextAwareViewRender

class ShowEditUserPageHandler(
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
        val userRole = user.role.name
        val form = WebForm().with(roleField of userRole)
        val model = EditUserDataVM(user, form)
        return Response(OK).with(lens(request) of model)
    }
}
