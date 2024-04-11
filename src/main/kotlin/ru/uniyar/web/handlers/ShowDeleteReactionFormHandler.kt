package ru.uniyar.web.handlers

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.FORBIDDEN
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.lens.RequestContextLens
import ru.uniyar.authorization.Permissions
import ru.uniyar.authorization.SharedState
import ru.uniyar.authorization.Users
import ru.uniyar.domain.AuthorStructure
import ru.uniyar.domain.Themes
import ru.uniyar.web.models.DeleteReactionDataVM
import ru.uniyar.web.templates.ContextAwareViewRender

class ShowDeleteReactionFormHandler(
    val users: Users,
    val themes: Themes,
    val lens: ContextAwareViewRender,
    val permissionLens: RequestContextLens<Permissions>,
    val sharedStateLens: RequestContextLens<SharedState?>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val role = permissionLens(request)
        val user = sharedStateLens(request) ?: return Response(FORBIDDEN)
        val notFoundResponse = Response(NOT_FOUND).with(lens(request) of errorModel)
        val themeId = lensOrNull(themeIdLens, request) ?: return notFoundResponse
        val themeAndMessages = themes.fetchThemeByNumber(themeId) ?: return notFoundResponse
        val messageId = lensOrNull(messageIdLens, request) ?: return notFoundResponse
        val message = themeAndMessages.messages.fetchMessageByNumber(messageId) ?: return notFoundResponse
        val reactionNum = lensOrNull(reactionNumberLens, request)
        return if (reactionNum == null || reactionNum > message.listOfReactions.lastIndex || reactionNum <= -1) {
            notFoundResponse
        } else {
            val reaction = message.listOfReactions[reactionNum]
            val reactionAuthor = users.usersList.first { it.userId == reaction.author }
            val reactionStruct = AuthorStructure(reaction, reactionAuthor.userName)
            if (role.canDeleteReaction && reaction.author == user.userId) {
                val model = DeleteReactionDataVM(reactionStruct)
                return Response(OK).with(lens(request) of model)
            }
            return Response(FORBIDDEN)
        }
    }
}
