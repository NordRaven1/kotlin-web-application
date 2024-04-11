package ru.uniyar.web.handlers

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.FOUND
import org.http4k.core.with
import org.http4k.lens.RequestContextLens
import ru.uniyar.authorization.SharedState
import ru.uniyar.authorization.Users
import ru.uniyar.domain.Messages
import ru.uniyar.domain.Theme
import ru.uniyar.domain.ThemeAndMessages
import ru.uniyar.domain.Themes
import ru.uniyar.web.models.NewThemeDataVM
import ru.uniyar.web.templates.ContextAwareViewRender

class CreateNewThemeHandler(
    val users: Users,
    val themes: Themes,
    val lens: ContextAwareViewRender,
    val sharedStateLens: RequestContextLens<SharedState?>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val form = themeFormLens(request)
        if (form.errors.isNotEmpty()) {
            val failures = formFailureInfoList(form.errors)
            val model = NewThemeDataVM(form, failures)
            return Response(BAD_REQUEST).with(lens(request) of model)
        }
        val title = themeTitleField(form)
        val themeCheck =
            themes.themesList.find { themes ->
                themes.theme.title.replace(" ", "")
                    .equals(title.replace(" ", ""), true)
            }
        if (themeCheck != null) {
            val failures = formFailureInfoList(form.errors)
            failures.add("Такая тема уже существует!")
            val model = NewThemeDataVM(form, failures)
            return Response(BAD_REQUEST).with(lens(request) of model)
        }
        val user = sharedStateLens(request) ?: return Response(BAD_REQUEST)
        val authorId = user.userId
        val newTheme = Theme(title, authorId)
        val newThemeAndMessages = ThemeAndMessages(newTheme, Messages())
        themes.add(newThemeAndMessages)
        return Response(FOUND).header("Location", "/themes")
    }
}
