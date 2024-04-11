package ru.uniyar.handlers

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.and
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.core.UriTemplate
import org.http4k.core.with
import org.http4k.kotest.haveHeader
import org.http4k.kotest.haveStatus
import org.http4k.lens.WebForm
import org.http4k.routing.RoutedRequest
import ru.uniyar.domain.Author
import ru.uniyar.domain.Message
import ru.uniyar.domain.Messages
import ru.uniyar.domain.Theme
import ru.uniyar.domain.ThemeAndMessages
import ru.uniyar.domain.Themes
import ru.uniyar.web.handlers.CreateNewMessageHandler
import ru.uniyar.web.handlers.DeleteMessageHandler
import ru.uniyar.web.handlers.EditMessageHandler
import ru.uniyar.web.handlers.ShowDeleteMessageFormHandler
import ru.uniyar.web.handlers.ShowEditMessageFormHandler
import ru.uniyar.web.handlers.ShowNewMessageFormHandler
import ru.uniyar.web.handlers.checkField
import ru.uniyar.web.handlers.deleteLens
import ru.uniyar.web.handlers.messageAuthorField
import ru.uniyar.web.handlers.messageFormLens
import ru.uniyar.web.handlers.messageTextField

class MessageHandlersFunctionsTest : FunSpec({

    isolationMode = IsolationMode.InstancePerTest

    val testTheme = Theme("Активный отдых", Author("NordRaven"))
    val testTheme2 = Theme("История", Author("NordRaven"))
    val testMessage = Message(testTheme, Author("NordRaven"), "test", mutableListOf())
    val testMessages = Messages()
    val testMessages2 = Messages()
    val testThemes =
        Themes(
            listOf(
                ThemeAndMessages(testTheme, testMessages),
                ThemeAndMessages(testTheme2, testMessages2),
            ),
        )

    test("show new message form - Should return OK status") {

        val form = WebForm()
        val handler = ShowNewMessageFormHandler(testThemes, form, htmlView)
        val request = Request(Method.GET, "/themes/theme/${testTheme.id}/new")
        val routedRequest =
            RoutedRequest(
                request,
                UriTemplate.from("/themes/theme/{themeId}/new"),
            )
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.OK),
        )
        result.body.shouldNotBeNull()
    }

    test("show new message form with invalid theme id - Should return BAD_REQUEST status") {

        val form = WebForm()
        val handler = ShowNewMessageFormHandler(testThemes, form, htmlView)
        val request = Request(Method.GET, "/themes/theme/abc/new")
        val routedRequest =
            RoutedRequest(
                request,
                UriTemplate.from("/themes/theme/{themeId}/new"),
            )
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        result.body.shouldNotBeNull()
    }

    test("show new message form with null theme id - Should return BAD_REQUEST status") {

        val form = WebForm()
        val handler = ShowNewMessageFormHandler(testThemes, form, htmlView)
        val request = Request(Method.GET, "/themes/theme//new")
        val routedRequest =
            RoutedRequest(
                request,
                UriTemplate.from("/themes/theme/{themeId}/new"),
            )
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        result.body.shouldNotBeNull()
    }

    test("create new message - Should redirect to the /themes/theme/{themeId}/message/{mesId} route") {

        val form = WebForm().with(messageAuthorField of "NordRaven", messageTextField of "Тест")
        val handler = CreateNewMessageHandler(testThemes, htmlView)
        val request =
            Request(
                Method.POST,
                "/themes/theme/${testTheme.id}/new",
            ).with(messageFormLens of form)
        val routedRequest =
            RoutedRequest(
                request,
                UriTemplate.from("/themes/theme/{themeId}/new"),
            )
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.FOUND).and(
                haveHeader(
                    "Location",
                    "/themes/theme/${testTheme.id}/message/${testMessages.listOfMessage[0].id}",
                ),
            ),
        )
        testMessages.listOfMessage.shouldNotBeEmpty()
        testMessages.listOfMessage[0].text.shouldBe("Тест")
        result.body.shouldNotBeNull()
    }

    test("create new message with invalid theme id - Should return BAD_REQUEST status") {

        val form = WebForm().with(messageAuthorField of "NordRaven", messageTextField of "Тест")
        val handler = CreateNewMessageHandler(testThemes, htmlView)
        val request =
            Request(
                Method.POST,
                "/themes/theme/abc/new",
            ).with(messageFormLens of form)
        val routedRequest =
            RoutedRequest(
                request,
                UriTemplate.from("/themes/theme/{themeId}/new"),
            )
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        testMessages.listOfMessage.shouldBeEmpty()
        result.body.shouldNotBeNull()
    }

    test("create new message with null theme id - Should return BAD_REQUEST status") {

        val form = WebForm().with(messageAuthorField of "NordRaven", messageTextField of "Тест")
        val handler = CreateNewMessageHandler(testThemes, htmlView)
        val request =
            Request(
                Method.POST,
                "/themes/theme//new",
            ).with(messageFormLens of form)
        val routedRequest =
            RoutedRequest(
                request,
                UriTemplate.from("/themes/theme/{themeId}/new"),
            )
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        testMessages.listOfMessage.shouldBeEmpty()
        result.body.shouldNotBeNull()
    }

    test("create new message with invalid field - Should return BAD_REQUEST status") {

        val form = WebForm().with(messageAuthorField of "", messageTextField of "Тест")
        val handler = CreateNewMessageHandler(testThemes, htmlView)
        val request =
            Request(
                Method.POST,
                "/themes/theme/${testTheme.id}/new",
            ).with(messageFormLens of form)
        val routedRequest =
            RoutedRequest(
                request,
                UriTemplate.from("/themes/theme/{themeId}/new"),
            )
        val result = handler(routedRequest)
        val invalidForm = messageFormLens(request)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        testMessages.listOfMessage.shouldBeEmpty()
        invalidForm.errors.shouldNotBeEmpty()
        result.body.shouldNotBeNull()
    }

    test("show edit message form - Should return OK status") {

        testMessages.add(testMessage)
        val handler = ShowEditMessageFormHandler(testThemes, htmlView)
        val request = Request(Method.GET, "/themes/theme/${testTheme.id}/message/${testMessage.id}/edit")
        val routedRequest = RoutedRequest(request, UriTemplate.from("/themes/theme/{themeId}/message/{mesId}/edit"))
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.OK),
        )
        result.body.shouldNotBeNull()
    }

    test("show edit message form with invalid theme id - Should return BAD_REQUEST status") {

        testMessages.add(testMessage)
        val handler = ShowEditMessageFormHandler(testThemes, htmlView)
        val request = Request(Method.GET, "/themes/theme/abc/message/${testMessage.id}/edit")
        val routedRequest = RoutedRequest(request, UriTemplate.from("/themes/theme/{themeId}/message/{mesId}/edit"))
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        result.body.shouldNotBeNull()
    }

    test("show edit message form with null theme id - Should return BAD_REQUEST status") {

        testMessages.add(testMessage)
        val handler = ShowEditMessageFormHandler(testThemes, htmlView)
        val request = Request(Method.GET, "/themes/theme//message/${testMessage.id}/edit")
        val routedRequest = RoutedRequest(request, UriTemplate.from("/themes/theme/{themeId}/message/{mesId}/edit"))
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        result.body.shouldNotBeNull()
    }

    test("show edit message form with invalid message id - Should return BAD_REQUEST status") {

        testMessages.add(testMessage)
        val handler = ShowEditMessageFormHandler(testThemes, htmlView)
        val request = Request(Method.GET, "/themes/theme/${testTheme.id}/message/abc/edit")
        val routedRequest = RoutedRequest(request, UriTemplate.from("/themes/theme/{themeId}/message/{mesId}/edit"))
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        result.body.shouldNotBeNull()
    }

    test("show edit message form with null message id - Should return BAD_REQUEST status") {

        testMessages.add(testMessage)
        val handler = ShowEditMessageFormHandler(testThemes, htmlView)
        val request = Request(Method.GET, "/themes/theme/${testTheme.id}/message//edit")
        val routedRequest = RoutedRequest(request, UriTemplate.from("/themes/theme/{themeId}/message/{mesId}/edit"))
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        result.body.shouldNotBeNull()
    }

    test("edit message - Should redirect to the /themes/theme/{themeId}/message/{mesId} route") {

        testMessages.add(testMessage)
        val form =
            WebForm().with(
                messageAuthorField of "NordRaven",
                messageTextField of "Новый текст",
            )
        val handler = EditMessageHandler(testThemes, htmlView)
        val request =
            Request(Method.POST, "/themes/theme/${testTheme.id}/message/${testMessage.id}/edit").with(
                messageFormLens of form,
            )
        val routedRequest = RoutedRequest(request, UriTemplate.from("/themes/theme/{themeId}/message/{mesId}/edit"))
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.FOUND).and(
                haveHeader(
                    "Location",
                    "/themes/theme/${testTheme.id}/message/${testMessage.id}",
                ),
            ),
        )
        testMessages.listOfMessage[0].text.shouldBe("Новый текст")
        result.body.shouldNotBeNull()
    }

    test("edit message with invalid theme id - should return BAD_REQUEST status") {

        testMessages.add(testMessage)
        val form =
            WebForm().with(
                messageAuthorField of "NordRaven",
                messageTextField of "Новый текст",
            )
        val handler = EditMessageHandler(testThemes, htmlView)
        val request =
            Request(Method.POST, "/themes/theme/abc/message/${testMessage.id}/edit").with(
                messageFormLens of form,
            )
        val routedRequest = RoutedRequest(request, UriTemplate.from("/themes/theme/{themeId}/message/{mesId}/edit"))
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        testMessages.listOfMessage[0].text.shouldBe("test")
        result.body.shouldNotBeNull()
    }

    test("edit message with null theme id - should return BAD_REQUEST status") {

        testMessages.add(testMessage)
        val form =
            WebForm().with(
                messageAuthorField of "NordRaven",
                messageTextField of "Новый текст",
            )
        val handler = EditMessageHandler(testThemes, htmlView)
        val request =
            Request(Method.POST, "/themes/theme//message/${testMessage.id}/edit").with(
                messageFormLens of form,
            )
        val routedRequest = RoutedRequest(request, UriTemplate.from("/themes/theme/{themeId}/message/{mesId}/edit"))
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        testMessages.listOfMessage[0].text.shouldBe("test")
        result.body.shouldNotBeNull()
    }

    test("edit message with invalid message id - should return BAD_REQUEST status") {

        testMessages.add(testMessage)
        val form =
            WebForm().with(
                messageAuthorField of "NordRaven",
                messageTextField of "Новый текст",
            )
        val handler = EditMessageHandler(testThemes, htmlView)
        val request =
            Request(Method.POST, "/themes/theme/${testTheme.id}/message/abc/edit").with(
                messageFormLens of form,
            )
        val routedRequest = RoutedRequest(request, UriTemplate.from("/themes/theme/{themeId}/message/{mesId}/edit"))
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        testMessages.listOfMessage[0].text.shouldBe("test")
        result.body.shouldNotBeNull()
    }

    test("edit message with null message id - should return BAD_REQUEST status") {

        testMessages.add(testMessage)
        val form =
            WebForm().with(
                messageAuthorField of "NordRaven",
                messageTextField of "Новый текст",
            )
        val handler = EditMessageHandler(testThemes, htmlView)
        val request =
            Request(Method.POST, "/themes/theme/${testTheme.id}/message//edit").with(
                messageFormLens of form,
            )
        val routedRequest = RoutedRequest(request, UriTemplate.from("/themes/theme/{themeId}/message/{mesId}/edit"))
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        testMessages.listOfMessage[0].text.shouldBe("test")
        result.body.shouldNotBeNull()
    }

    test("edit message with invalid field - should return BAD_REQUEST status") {

        testMessages.add(testMessage)
        val form =
            WebForm().with(
                messageAuthorField of "",
                messageTextField of "Новый текст",
            )
        val handler = EditMessageHandler(testThemes, htmlView)
        val request =
            Request(Method.POST, "/themes/theme/${testTheme.id}/message/abc/edit").with(
                messageFormLens of form,
            )
        val routedRequest = RoutedRequest(request, UriTemplate.from("/themes/theme/{themeId}/message/{mesId}/edit"))
        val result = handler(routedRequest)
        val invalidForm = messageFormLens(request)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        testMessages.listOfMessage[0].text.shouldBe("test")
        invalidForm.errors.shouldNotBeEmpty()
        result.body.shouldNotBeNull()
    }

    test("show delete message form - should return OK status") {

        testMessages.add(testMessage)
        val handler = ShowDeleteMessageFormHandler(testThemes, htmlView)
        val request = Request(Method.GET, "/themes/theme/${testTheme.id}/message/${testMessage.id}/delete")
        val routedRequest = RoutedRequest(request, UriTemplate.from("/themes/theme/{themeId}/message/{mesId}/delete"))
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.OK),
        )
        result.body.shouldNotBeNull()
    }

    test("show delete message form with invalid theme id - should return BAD_REQUEST status") {

        testMessages.add(testMessage)
        val handler = ShowDeleteMessageFormHandler(testThemes, htmlView)
        val request = Request(Method.GET, "/themes/theme/abc/message/${testMessage.id}/delete")
        val routedRequest = RoutedRequest(request, UriTemplate.from("/themes/theme/{themeId}/message/{mesId}/delete"))
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        result.body.shouldNotBeNull()
    }

    test("show delete message form with null theme id - should return BAD_REQUEST status") {

        testMessages.add(testMessage)
        val handler = ShowDeleteMessageFormHandler(testThemes, htmlView)
        val request = Request(Method.GET, "/themes/theme//message/${testMessage.id}/delete")
        val routedRequest = RoutedRequest(request, UriTemplate.from("/themes/theme/{themeId}/message/{mesId}/delete"))
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        result.body.shouldNotBeNull()
    }

    test("show delete message form with invalid message id - should return BAD_REQUEST status") {

        testMessages.add(testMessage)
        val handler = ShowDeleteMessageFormHandler(testThemes, htmlView)
        val request = Request(Method.GET, "/themes/theme/${testTheme.id}/message/abc/delete")
        val routedRequest = RoutedRequest(request, UriTemplate.from("/themes/theme/{themeId}/message/{mesId}/delete"))
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        result.body.shouldNotBeNull()
    }

    test("show delete message form with null message id - should return BAD_REQUEST status") {

        testMessages.add(testMessage)
        val handler = ShowDeleteMessageFormHandler(testThemes, htmlView)
        val request = Request(Method.GET, "/themes/theme/${testTheme.id}/message//delete")
        val routedRequest = RoutedRequest(request, UriTemplate.from("/themes/theme/{themeId}/message/{mesId}/delete"))
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        result.body.shouldNotBeNull()
    }

    test("delete message - Should redirect to the /themes/theme/{themeId} route") {

        testMessages.add(testMessage)
        val form =
            WebForm().with(
                checkField of "on",
            )
        val handler = DeleteMessageHandler(testThemes, htmlView)
        val request =
            Request(Method.POST, "/themes/theme/${testTheme.id}/message/${testMessage.id}/delete").with(
                deleteLens of form,
            )
        val routedRequest =
            RoutedRequest(
                request,
                UriTemplate.from(
                    "/themes/theme/{themeId}/message/{mesId}/delete",
                ),
            )
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.FOUND).and(haveHeader("Location", "/themes/theme/${testTheme.id}")),
        )
        testMessages.listOfMessage.shouldBeEmpty()
        result.body.shouldNotBeNull()
    }

    test("delete message with invalid theme id - should return BAD_REQUEST status") {

        testMessages.add(testMessage)
        val form =
            WebForm().with(
                checkField of "on",
            )
        val handler = DeleteMessageHandler(testThemes, htmlView)
        val request =
            Request(Method.POST, "/themes/theme/abc/message/${testMessage.id}/delete").with(
                deleteLens of form,
            )
        val routedRequest =
            RoutedRequest(
                request,
                UriTemplate.from(
                    "/themes/theme/{themeId}/message/{mesId}/delete",
                ),
            )
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        testMessages.listOfMessage.shouldNotBeEmpty()
        result.body.shouldNotBeNull()
    }

    test("delete message with null theme id - should return BAD_REQUEST status") {

        testMessages.add(testMessage)
        val form =
            WebForm().with(
                checkField of "on",
            )
        val handler = DeleteMessageHandler(testThemes, htmlView)
        val request =
            Request(Method.POST, "/themes/theme//message/${testMessage.id}/delete").with(
                deleteLens of form,
            )
        val routedRequest =
            RoutedRequest(
                request,
                UriTemplate.from(
                    "/themes/theme/{themeId}/message/{mesId}/delete",
                ),
            )
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        testMessages.listOfMessage.shouldNotBeEmpty()
        result.body.shouldNotBeNull()
    }

    test("delete message with invalid message id - should return BAD_REQUEST status") {

        testMessages.add(testMessage)
        val form =
            WebForm().with(
                checkField of "on",
            )
        val handler = DeleteMessageHandler(testThemes, htmlView)
        val request =
            Request(Method.POST, "/themes/theme/${testTheme.id}/message/abc/delete").with(
                deleteLens of form,
            )
        val routedRequest =
            RoutedRequest(
                request,
                UriTemplate.from(
                    "/themes/theme/{themeId}/message/{mesId}/delete",
                ),
            )
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        testMessages.listOfMessage.shouldNotBeEmpty()
        result.body.shouldNotBeNull()
    }

    test("delete message without check field - should return BAD_REQUEST status") {

        testMessages.add(testMessage)
        val form = WebForm()
        val handler = DeleteMessageHandler(testThemes, htmlView)
        val request =
            Request(Method.POST, "/themes/theme/${testTheme.id}/message/${testMessage.id}/delete").with(
                deleteLens of form,
            )
        val routedRequest =
            RoutedRequest(
                request,
                UriTemplate.from(
                    "/themes/theme/{themeId}/message/{mesId}/delete",
                ),
            )
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        testMessages.listOfMessage.shouldNotBeEmpty()
        result.body.shouldNotBeNull()
    }
})
