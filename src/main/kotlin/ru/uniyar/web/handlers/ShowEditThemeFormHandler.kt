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
import ru.uniyar.authorization.SharedState
import ru.uniyar.domain.ThemeAndMessages
import ru.uniyar.web.models.EditThemeDataVM
import ru.uniyar.web.templates.ContextAwareViewRender

class ShowEditThemeFormHandler(
    val func: (String) -> ThemeAndMessages?,
    val lens: ContextAwareViewRender,
    val permissionLens: RequestContextLens<Permissions>,
    val sharedStateLens: RequestContextLens<SharedState?>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val role = permissionLens(request)
        val user = sharedStateLens(request) ?: return Response(FORBIDDEN)
        val themeId =
            lensOrNull(themeIdLens, request)
                ?: return Response(NOT_FOUND).with(lens(request) of errorModel)
        val themeAndMessages =
            func(themeId)
                ?: return Response(NOT_FOUND).with(lens(request) of errorModel)
        val theme = themeAndMessages.theme
        if (role.canEditTheme && (
                theme.author == user.userId || role.name == "ADMIN" ||
                    role.name == "MODERATOR"
            )
        ) {
            val adding = if (theme.addPossibility) "on" else "down"
            val form =
                WebForm().with(
                    themeTitleField of theme.title,
                    themeAddingField of adding,
                )
            val model = EditThemeDataVM(theme, form)
            return Response(OK).with(lens(request) of model)
        }
        return Response(FORBIDDEN)
    }
}
