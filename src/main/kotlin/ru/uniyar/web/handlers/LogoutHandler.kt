package ru.uniyar.web.handlers

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.cookie.invalidateCookie

class LogoutHandler : HttpHandler {
    override fun invoke(request: Request): Response {
        val response = Response(Status.FOUND).header("Location", "/")
        return response.invalidateCookie("auth")
    }
}
