package ru.uniyar.web.handlers

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import ru.uniyar.authorization.Users
import ru.uniyar.domain.AuthorStructure
import ru.uniyar.domain.Reaction
import ru.uniyar.domain.Themes
import ru.uniyar.web.models.MessagePageVM
import ru.uniyar.web.templates.ContextAwareViewRender

class ShowMessageHandler(
    val users: Users,
    val themesList: Themes,
    val lens: ContextAwareViewRender,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val notFoundResponse = Response(NOT_FOUND).with(lens(request) of errorModel)
        val themeId = lensOrNull(themeIdLens, request) ?: return notFoundResponse
        val theme = themesList.fetchThemeByNumber(themeId) ?: return notFoundResponse
        val messageId = lensOrNull(messageIdLens, request) ?: return notFoundResponse
        val message = theme.messages.fetchMessageByNumber(messageId) ?: return notFoundResponse
        val author = users.usersList.first { it.userId == message.author }
        val messageStruct = AuthorStructure(message, author.userName)
        val reactions = mutableListOf<AuthorStructure<Reaction>>()
        for (reaction in message.listOfReactions) {
            val reactionAuthor = users.usersList.first { it.userId == reaction.author }
            reactions.add(AuthorStructure(reaction, reactionAuthor.userName))
        }
        val model = MessagePageVM(messageStruct, reactions)
        return Response(OK).with(lens(request) of model)
    }
}
