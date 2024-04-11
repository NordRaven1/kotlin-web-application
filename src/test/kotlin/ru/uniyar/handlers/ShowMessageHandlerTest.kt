package ru.uniyar.handlers

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.OK
import org.http4k.core.UriTemplate
import org.http4k.kotest.haveStatus
import org.http4k.routing.RoutedRequest
import ru.uniyar.domain.Author
import ru.uniyar.domain.Message
import ru.uniyar.domain.Messages
import ru.uniyar.domain.Theme
import ru.uniyar.domain.ThemeAndMessages
import ru.uniyar.domain.Themes
import ru.uniyar.web.handlers.ShowMessageHandler

class ShowMessageHandlerTest : FunSpec({

    isolationMode = IsolationMode.InstancePerTest

    val testTheme = Theme("Активный отдых", Author("NordRaven"))
    val testMessage = Message(testTheme, Author("NordRaven"), "test", mutableListOf())
    val testMessages = Messages()
    val testThemes = Themes(listOf(ThemeAndMessages(testTheme, testMessages)))

    test("Should return OK status") {

        testMessages.add(testMessage)
        val handler = ShowMessageHandler(testThemes, htmlView)
        val request = Request(GET, "/themes/theme/${testTheme.id}/message/${testMessage.id}")
        val routedRequest = RoutedRequest(request, UriTemplate.from("/themes/theme/{themeId}/message/{mesId}"))
        val result = handler(routedRequest)
        result.should(
            haveStatus(OK),
        )
        result.body.shouldNotBeNull()
    }

    test("invalid theme - should return BAD_REQUEST status") {

        val handler = ShowMessageHandler(testThemes, htmlView)
        val request = Request(GET, "/themes/theme/abc/message/${testMessage.id}")
        val routedRequest = RoutedRequest(request, UriTemplate.from("/themes/theme/{themeId}/message/{mesId}"))
        val result = handler(routedRequest)
        result.should(
            haveStatus(BAD_REQUEST),
        )
        result.body.shouldNotBeNull()
    }

    test("null theme - should return BAD_REQUEST status") {

        val handler = ShowMessageHandler(testThemes, htmlView)
        val request = Request(GET, "/themes/theme//message/${testMessage.id}")
        val routedRequest = RoutedRequest(request, UriTemplate.from("/themes/theme/{themeId}/message/{mesId}"))
        val result = handler(routedRequest)
        result.should(
            haveStatus(BAD_REQUEST),
        )
        result.body.shouldNotBeNull()
    }

    test("invalid message - should return BAD_REQUEST status") {

        val handler = ShowMessageHandler(testThemes, htmlView)
        val request = Request(GET, "/themes/theme/${testTheme.id}/message/abc")
        val routedRequest = RoutedRequest(request, UriTemplate.from("/themes/theme/{themeId}/message/{mesId}"))
        val result = handler(routedRequest)
        result.should(
            haveStatus(BAD_REQUEST),
        )
        result.body.shouldNotBeNull()
    }

    test("null message - should return BAD_REQUEST status") {

        val handler = ShowMessageHandler(testThemes, htmlView)
        val request = Request(GET, "/themes/theme/${testTheme.id}/message/")
        val routedRequest = RoutedRequest(request, UriTemplate.from("/themes/theme/{themeId}/message/{mesId}"))
        val result = handler(routedRequest)
        result.should(
            haveStatus(BAD_REQUEST),
        )
        result.body.shouldNotBeNull()
    }
})
