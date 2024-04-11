package ru.uniyar.handlers

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.core.UriTemplate
import org.http4k.kotest.haveStatus
import org.http4k.routing.RoutedRequest
import ru.uniyar.domain.Author
import ru.uniyar.domain.Messages
import ru.uniyar.domain.Theme
import ru.uniyar.domain.ThemeAndMessages
import ru.uniyar.domain.Themes
import ru.uniyar.web.handlers.MessageListHandler

class MessageListHandlerTest : FunSpec({

    isolationMode = IsolationMode.InstancePerTest

    val testTheme = Theme("Активный отдых", Author("NordRaven"))
    val testMessages = Messages()
    val testThemes = Themes(listOf(ThemeAndMessages(testTheme, testMessages)))

    test("Should return OK status") {

        val handler = MessageListHandler(testThemes::fetchThemeByNumber, htmlView)
        val request = Request(Method.GET, "/themes/theme/${testTheme.id}")
        val routedRequest = RoutedRequest(request, UriTemplate.from("/themes/theme/{themeId}"))
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.OK),
        )
        result.body.shouldNotBeNull()
    }

    test("invalid theme - should return BAD_REQUEST status") {

        val handler = MessageListHandler(testThemes::fetchThemeByNumber, htmlView)
        val request = Request(Method.GET, "/themes/theme/abc")
        val routedRequest = RoutedRequest(request, UriTemplate.from("/themes/theme/{themeId}"))
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        result.body.shouldNotBeNull()
    }

    test("null theme - should return BAD_REQUEST status") {

        val handler = MessageListHandler(testThemes::fetchThemeByNumber, htmlView)
        val request = Request(Method.GET, "/themes/theme/")
        val routedRequest = RoutedRequest(request, UriTemplate.from("/themes/theme/{themeId}"))
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        result.body.shouldNotBeNull()
    }
})
