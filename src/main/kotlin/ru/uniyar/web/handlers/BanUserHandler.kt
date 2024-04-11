package ru.uniyar.web.handlers

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.FORBIDDEN
import org.http4k.core.Status.Companion.FOUND
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.with
import org.http4k.lens.RequestContextLens
import ru.uniyar.authorization.Permissions
import ru.uniyar.authorization.Users
import ru.uniyar.web.templates.ContextAwareViewRender

class BanUserHandler(
    val users: Users,
    val lens: ContextAwareViewRender,
    val permissionLens: RequestContextLens<Permissions>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val role = permissionLens(request)
        if (role.name != "ADMIN" && role.name != "MODERATOR") return Response(FORBIDDEN)
        val userId =
            lensOrNull(userIdLens, request)
                ?: return Response(NOT_FOUND).with(lens(request) of errorModel)
        val user = users.findUserById(userId) ?: return Response(NOT_FOUND).with(lens(request) of errorModel)
        user.role = rolesList.get(0)
        return Response(FOUND).header(
            "Location",
            "/users",
        )
    }
}
