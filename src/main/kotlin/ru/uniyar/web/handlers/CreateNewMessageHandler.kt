package ru.uniyar.web.handlers

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.FOUND
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.with
import org.http4k.lens.RequestContextLens
import ru.uniyar.authorization.SharedState
import ru.uniyar.authorization.Users
import ru.uniyar.domain.Message
import ru.uniyar.domain.Themes
import ru.uniyar.web.models.NewMessageDataVM
import ru.uniyar.web.templates.ContextAwareViewRender

class CreateNewMessageHandler(
    val users: Users,
    val themes: Themes,
    val lens: ContextAwareViewRender,
    val sharedStateLens: RequestContextLens<SharedState?>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val themeId =
            lensOrNull(themeIdLens, request)
                ?: return Response(NOT_FOUND).with(lens(request) of errorModel)
        val themeAndMessages =
            themes.fetchThemeByNumber(themeId)
                ?: return Response(NOT_FOUND).with(lens(request) of errorModel)
        val form = messageFormLens(request)
        if (form.errors.isNotEmpty()) {
            val failures = formFailureInfoList(form.errors)
            val model = NewMessageDataVM(form, failures)
            return Response(BAD_REQUEST).with(lens(request) of model)
        }
        val user = sharedStateLens(request) ?: return Response(BAD_REQUEST)
        val authorId = user.userId
        val text = messageTextField(form)
        val newMessage = Message(themeAndMessages.theme, authorId, text)
        themeAndMessages.messages.add(newMessage)
        return Response(FOUND).header(
            "Location",
            "/themes/theme/$themeId/message/${newMessage.id}",
        )
    }
}
