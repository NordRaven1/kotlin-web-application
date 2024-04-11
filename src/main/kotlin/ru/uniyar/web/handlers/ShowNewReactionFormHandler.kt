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
import ru.uniyar.domain.Themes
import ru.uniyar.web.models.NewReactionDataVM
import ru.uniyar.web.templates.ContextAwareViewRender

class ShowNewReactionFormHandler(
    val themes: Themes,
    val webForm: WebForm,
    val lens: ContextAwareViewRender,
    val permissionLens: RequestContextLens<Permissions>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val role = permissionLens(request)
        if (!role.canAddReaction) {
            return Response(FORBIDDEN)
        }
        val notFoundResponse = Response(NOT_FOUND).with(lens(request) of errorModel)
        val themeId = lensOrNull(themeIdLens, request) ?: return notFoundResponse
        val themeAndMessages = themes.fetchThemeByNumber(themeId) ?: return notFoundResponse
        val messageId = lensOrNull(messageIdLens, request) ?: return notFoundResponse
        themeAndMessages.messages.fetchMessageByNumber(messageId) ?: return notFoundResponse
        val model = NewReactionDataVM(webForm, reactionsList)
        return Response(OK).with(lens(request) of model)
    }
}
