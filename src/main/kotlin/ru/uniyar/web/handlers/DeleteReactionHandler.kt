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
import ru.uniyar.web.models.DeleteReactionDataVM
import ru.uniyar.web.templates.ContextAwareViewRender

class DeleteReactionHandler(
    val users: Users,
    val themes: Themes,
    val lens: ContextAwareViewRender,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val notFoundResponse = Response(NOT_FOUND).with(lens(request) of errorModel)
        val themeId = lensOrNull(themeIdLens, request) ?: return notFoundResponse
        val themeAndMessages = themes.fetchThemeByNumber(themeId) ?: return notFoundResponse
        val messageId = lensOrNull(messageIdLens, request) ?: return notFoundResponse
        val message = themeAndMessages.messages.fetchMessageByNumber(messageId) ?: return notFoundResponse
        val reactuinNum = lensOrNull(reactionNumberLens, request)
        if (reactuinNum == null || reactuinNum > message.listOfReactions.lastIndex || reactuinNum <= -1) {
            return notFoundResponse
        }
        val form = deleteLens(request)
        return if (form.fields["agreement"]?.isNotEmpty() == true) {
            message.removeReaction(reactuinNum)
            Response(FOUND).header(
                "Location",
                "/themes/theme/$themeId/message/$messageId",
            )
        } else {
            val reaction = message.listOfReactions[reactuinNum]
            val reactionAuthor = users.usersList.first { it.userId == reaction.author }
            val reactionStruct = AuthorStructure(reaction, reactionAuthor.userName)
            val model = DeleteReactionDataVM(reactionStruct, true)
            Response(BAD_REQUEST).with(lens(request) of model)
        }
    }
}
