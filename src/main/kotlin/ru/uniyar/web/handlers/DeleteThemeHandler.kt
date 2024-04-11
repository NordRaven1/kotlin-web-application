package ru.uniyar.web.handlers

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.FOUND
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.with
import ru.uniyar.authorization.Users
import ru.uniyar.domain.AuthorStructure
import ru.uniyar.domain.Themes
import ru.uniyar.web.models.DeleteThemeDataVM
import ru.uniyar.web.templates.ContextAwareViewRender

class DeleteThemeHandler(
    val users: Users,
    val themes: Themes,
    val lens: ContextAwareViewRender,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val themeId =
            lensOrNull(themeIdLens, request)
                ?: return Response(NOT_FOUND).with(lens(request) of errorModel)
        val theme =
            themes.fetchThemeByNumber(themeId)
                ?: return Response(NOT_FOUND).with(lens(request) of errorModel)
        val form = deleteLens(request)
        return if (form.fields["agreement"]?.isNotEmpty() == true) {
            themes.removeTheme(themeId)
            Response(FOUND).header("Location", "/themes")
        } else {
            val themeAuthor = users.usersList.first { it.userId == theme.theme.author }
            val themeStruct = AuthorStructure(theme, themeAuthor.userName)
            val model = DeleteThemeDataVM(themeStruct, true)
            Response(BAD_REQUEST).with(lens(request) of model)
        }
    }
}
