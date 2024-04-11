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
import ru.uniyar.domain.Reaction
import ru.uniyar.domain.Themes
import ru.uniyar.web.models.DeleteMessageDataVM
import ru.uniyar.web.templates.ContextAwareViewRender

class DeleteMessageHandler(
    val users: Users,
    val themes: Themes,
    val lens: ContextAwareViewRender,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val themeId =
            lensOrNull(themeIdLens, request)
                ?: throw return Response(NOT_FOUND).with(lens(request) of errorModel)
        val themeAndMessages =
            themes.fetchThemeByNumber(themeId)
                ?: return Response(NOT_FOUND).with(lens(request) of errorModel)
        val messageId =
            lensOrNull(messageIdLens, request)
                ?: return Response(NOT_FOUND).with(lens(request) of errorModel)
        val message =
            themeAndMessages.messages.fetchMessageByNumber(messageId)
                ?: return Response(NOT_FOUND).with(lens(request) of errorModel)
        val form = deleteLens(request)
        return if (form.fields["agreement"]?.isNotEmpty() == true) {
            themeAndMessages.messages.removeMessage(messageId)
            Response(FOUND).header("Location", "/themes/theme/$themeId")
        } else {
            val author = users.usersList.first { it.userId == message.author }
            val messageStruct = AuthorStructure(message, author.userName)
            val reactions = mutableListOf<AuthorStructure<Reaction>>()
            for (reaction in message.listOfReactions) {
                val reactionAuthor = users.usersList.first { it.userId == reaction.author }
                reactions.add(AuthorStructure(reaction, reactionAuthor.userName))
            }
            val model = DeleteMessageDataVM(messageStruct, reactions, true)
            Response(BAD_REQUEST).with(lens(request) of model)
        }
    }
}
