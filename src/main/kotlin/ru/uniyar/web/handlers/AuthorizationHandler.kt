package ru.uniyar.web.handlers

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.cookie.Cookie
import org.http4k.core.cookie.cookie
import org.http4k.core.with
import ru.uniyar.authorization.JwtTools
import ru.uniyar.authorization.Users
import ru.uniyar.authorization.authUser
import ru.uniyar.web.models.AuthPageVM
import ru.uniyar.web.templates.ContextAwareViewRender
import java.time.Instant
import java.time.temporal.ChronoUnit

class AuthorizationHandler(
    val users: Users,
    val jwtTools: JwtTools,
    val lens: ContextAwareViewRender,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val form = authFormLens(request)
        if (form.errors.isNotEmpty()) {
            val failures = formFailureInfoList(form.errors)
            val model = AuthPageVM(form, failures)
            return Response(Status.BAD_REQUEST).with(lens(request) of model)
        }
        val username = userNameField(form)
        val pass = passField(form)
        if (!authUser(users, username, pass)) {
            val failures = formFailureInfoList(form.errors)
            failures.add("Неверная информация в полях авторизации")
            val model = AuthPageVM(form, failures)
            return Response(Status.BAD_REQUEST).with(lens(request) of model)
        }
        val user = users.findUserByName(username) ?: return Response(Status.OK)
        if (user.role.name == "BANNED") {
            val failures = formFailureInfoList(form.errors)
            failures.add("Данный пользователь находится в чёрном списке")
            val model = AuthPageVM(form, failures)
            return Response(Status.BAD_REQUEST).with(lens(request) of model)
        }
        val token = jwtTools.createJWT(user.userId)
        if (token == null) {
            val failures = formFailureInfoList(form.errors)
            failures.add("Произошла ошибка при авторизации. Попробуйте ещё раз")
            val model = AuthPageVM(form, failures)
            return Response(Status.BAD_REQUEST).with(lens(request) of model)
        }
        val response = Response(Status.FOUND).header("Location", "/")
        return response.cookie(
            Cookie(
                "auth",
                token,
                expires = Instant.now().plus(7, ChronoUnit.DAYS),
                httpOnly = true,
            ),
        )
    }
}
