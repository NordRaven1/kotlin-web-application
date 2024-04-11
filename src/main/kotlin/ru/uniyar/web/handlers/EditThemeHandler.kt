package ru.uniyar.web.handlers

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.FOUND
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.with
import ru.uniyar.authorization.Users
import ru.uniyar.domain.Theme
import ru.uniyar.domain.ThemeAndMessages
import ru.uniyar.domain.Themes
import ru.uniyar.web.models.EditThemeDataVM
import ru.uniyar.web.templates.ContextAwareViewRender

class EditThemeHandler(
    val users: Users,
    val themes: Themes,
    val lens: ContextAwareViewRender,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val themeId =
            lensOrNull(themeIdLens, request)
                ?: return Response(NOT_FOUND).with(lens(request) of errorModel)
        val themeAndMessages =
            themes.fetchThemeByNumber(themeId)
                ?: return Response(NOT_FOUND).with(lens(request) of errorModel)
        val form = themeFormLens(request)
        if (form.errors.isNotEmpty()) {
            val failures = formFailureInfoList(form.errors)
            val model = EditThemeDataVM(themeAndMessages.theme, form, failures)
            return Response(BAD_REQUEST).with(lens(request) of model)
        }
        val title = themeTitleField(form)
        val themeCheck =
            themes.themesList.find { themes ->
                themes.theme.title.replace(" ", "")
                    .equals(title.replace(" ", ""), true)
            }
        if (themeCheck == null || themeAndMessages.theme.title == title) {
            val addingPossibility = themeAddingField(form)
            val adding = addingPossibility != null
            val newTheme = Theme(title, themeAndMessages.theme.author, adding)
            val newThemeAndMessages = ThemeAndMessages(newTheme, themeAndMessages.messages)
            themes.replaceTheme(themeId, newThemeAndMessages)
            return Response(FOUND).header("Location", "/themes/theme/$themeId")
        } else {
            val failures = formFailureInfoList(form.errors)
            failures.add("Такая тема уже существует!")
            val model = EditThemeDataVM(themeAndMessages.theme, form, failures)
            return Response(BAD_REQUEST).with(lens(request) of model)
        }
    }
}
