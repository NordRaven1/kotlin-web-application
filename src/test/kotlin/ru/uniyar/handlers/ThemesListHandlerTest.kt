package ru.uniyar.handlers

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.kotest.haveStatus
import ru.uniyar.domain.Author
import ru.uniyar.domain.Messages
import ru.uniyar.domain.Theme
import ru.uniyar.domain.ThemeAndMessages
import ru.uniyar.domain.Themes
import ru.uniyar.web.handlers.ThemesListHandler

class ThemesListHandlerTest : FunSpec({

    isolationMode = IsolationMode.InstancePerTest

    val testTheme = Theme("Активный отдых", Author("NordRaven"))
    val testMessages = Messages()
    val testThemes = Themes(listOf(ThemeAndMessages(testTheme, testMessages)))

    test("Should return OK status") {

        val handler = ThemesListHandler(testThemes::getThemesPerPage, htmlView)
        val request = Request(Method.GET, "/themes")
        val result = handler(request)
        result.should(
            haveStatus(Status.OK),
        )
        result.body.shouldNotBeNull()
    }
})
