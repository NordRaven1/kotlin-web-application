package ru.uniyar.web.filters

import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.core.cookie.cookie
import org.http4k.core.with
import org.http4k.lens.RequestContextLens
import ru.uniyar.authorization.JwtTools
import ru.uniyar.authorization.Permissions
import ru.uniyar.authorization.SharedState
import ru.uniyar.authorization.Users
import ru.uniyar.authorization.formSharedState
import ru.uniyar.web.handlers.errorModel
import ru.uniyar.web.templates.ContextAwareViewRender

fun errorFilter(htmlView: ContextAwareViewRender) =
    Filter { next: HttpHandler ->
        { request: Request ->
            val response = next(request)
            if (response.status == Status.OK ||
                response.status == Status.FOUND ||
                response.status == Status.BAD_REQUEST
            ) {
                response
            } else {
                response.with(htmlView(request) of errorModel)
            }
        }
    }

fun addStateFilter(
    users: Users,
    jwtTools: JwtTools,
    sharedStateLens: RequestContextLens<SharedState?>,
) = Filter { next: HttpHandler ->
    {
            request: Request ->
        request
            .cookie("auth")
            ?.let {
                jwtTools
                    .verifyJWT(it.value)
            }?.let {
                formSharedState(users, it)
            }?.let {
                next(request.with(sharedStateLens of it))
            } ?: next(request)
    }
}

fun permissionFilter(
    users: Users,
    sharedStateLens: RequestContextLens<SharedState?>,
    permissionLens: RequestContextLens<Permissions>,
) = Filter { next: HttpHandler ->
    {
            request: Request ->
        val sharedState = sharedStateLens(request)
        val anonRole = Permissions("ANONYMOUS")
        if (sharedState == null) {
            next(request.with(permissionLens of anonRole))
        } else {
            val user = users.findUserByName(sharedState.username)
            if (user == null) {
                next(request.with(permissionLens of anonRole))
            } else {
                val role = user.role
                next(request.with(permissionLens of role))
            }
        }
    }
}
