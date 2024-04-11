package ru.uniyar.web.handlers

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import ru.uniyar.web.models.MainPageVM
import ru.uniyar.web.templates.ContextAwareViewRender

class ShowAppInfoHandler(val lens: ContextAwareViewRender) : HttpHandler {
    override fun invoke(request: Request): Response {
        val model = MainPageVM()
        return Response(OK).with(lens(request) of model)
    }
}
