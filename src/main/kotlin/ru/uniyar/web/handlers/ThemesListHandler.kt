package ru.uniyar.web.handlers

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Uri
import org.http4k.core.findSingle
import org.http4k.core.queries
import org.http4k.core.with
import ru.uniyar.Paginator
import ru.uniyar.authorization.Users
import ru.uniyar.domain.AuthorStructure
import ru.uniyar.domain.ThemeAndMessages
import ru.uniyar.unsafeDateInFormat
import ru.uniyar.web.models.ThemesListPageVM
import ru.uniyar.web.templates.ContextAwareViewRender
import java.time.LocalDateTime

class ThemesListHandler(
    val users: Users,
    val func: (Users, String?, LocalDateTime?, LocalDateTime?, Int, Uri) -> Paginator<AuthorStructure<ThemeAndMessages>>,
    val lens: ContextAwareViewRender,
) : HttpHandler {
    override fun invoke(request: Request): Response {
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
        val themeSearch = request.uri.queries().findSingle("theme")
        val pageNum = request.uri.queries().findSingle("page")?.toIntOrNull() ?: 1
        val paginator = func(users, themeSearch, mindate, maxdate, pageNum, request.uri)
        val model = ThemesListPageVM(paginator, mindate, maxdate, themeSearch)
        return Response(OK).with(lens(request) of model)
    }
}
