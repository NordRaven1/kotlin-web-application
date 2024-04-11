package ru.uniyar.web.handlers

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.FORBIDDEN
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.lens.RequestContextLens
import ru.uniyar.authorization.Permissions
import ru.uniyar.authorization.Users
import ru.uniyar.web.models.UserListVM
import ru.uniyar.web.templates.ContextAwareViewRender

class UserListHandler(
    val users: Users,
    val lens: ContextAwareViewRender,
    val permissionLens: RequestContextLens<Permissions>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val role = permissionLens(request)
        if (role.name == "ADMIN" || role.name == "MODERATOR") {
            val model = UserListVM(users.usersList.drop(1))
            return Response(OK).with(lens(request) of model)
        }
        return Response(FORBIDDEN)
    }
}
