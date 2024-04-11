package ru.uniyar.web.handlers

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.findSingle
import org.http4k.core.queries
import org.http4k.core.with
import ru.uniyar.authorization.Users
import ru.uniyar.domain.ThemeAndMessages
import ru.uniyar.unsafeDateInFormat
import ru.uniyar.web.models.MessageListPageVM
import ru.uniyar.web.templates.ContextAwareViewRender

class MessageListHandler(
    val users: Users,
    val func: (String) -> ThemeAndMessages?,
    val lens: ContextAwareViewRender,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val themeId =
            lensOrNull(themeIdLens, request)
                ?: return Response(NOT_FOUND).with(lens(request) of errorModel)
        val theme = func(themeId) ?: return Response(NOT_FOUND).with(lens(request) of errorModel)
        val mindate =
            unsafeDateInFormat(
                request.uri.queries().findSingle("mindate"),
                "yyyy-MM-dd'T'HH:mm",
            )
        val maxdate =
            unsafeDateInFormat(
                request.uri.queries().findSingle("maxdate"),
                "yyyy-MM-dd'T'HH:mm",
            )
        val pageNum = request.uri.queries().findSingle("page")?.toIntOrNull() ?: 1
        val paginator = theme.messages.getMessagesPerPage(users, mindate, maxdate, pageNum, request.uri)
        val model = MessageListPageVM(paginator, mindate, maxdate, theme.theme.addPossibility, theme.theme)
        return Response(OK).with(lens(request) of model)
    }
}
