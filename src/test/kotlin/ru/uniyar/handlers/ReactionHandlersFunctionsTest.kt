package ru.uniyar.handlers

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.and
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
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
import ru.uniyar.domain.Reaction
import ru.uniyar.domain.Theme
import ru.uniyar.domain.ThemeAndMessages
import ru.uniyar.domain.Themes
import ru.uniyar.web.handlers.CreateNewReactionHandler
import ru.uniyar.web.handlers.DeleteReactionHandler
import ru.uniyar.web.handlers.ShowDeleteReactionFormHandler
import ru.uniyar.web.handlers.ShowNewReactionFormHandler
import ru.uniyar.web.handlers.checkField
import ru.uniyar.web.handlers.deleteLens
import ru.uniyar.web.handlers.reactionAuthorField
import ru.uniyar.web.handlers.reactionFormLens
import ru.uniyar.web.handlers.reactionTypeField

class ReactionHandlersFunctionsTest : FunSpec({

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

    test("show new reaction form - Should return OK status") {

        testMessages.add(testMessage)
        val form = WebForm()
        val handler = ShowNewReactionFormHandler(testThemes, form, htmlView)
        val request = Request(Method.GET, "/themes/theme/${testTheme.id}/message/${testMessage.id}/newReaction")
        val routedRequest =
            RoutedRequest(
                request,
                UriTemplate.from("/themes/theme/{themeId}/message/{mesId}/newReaction"),
            )
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.OK),
        )
        result.body.shouldNotBeNull()
    }

    test("show new reaction form with invalid theme id - Should return BAD_REQUEST status") {

        testMessages.add(testMessage)
        val form = WebForm()
        val handler = ShowNewReactionFormHandler(testThemes, form, htmlView)
        val request = Request(Method.GET, "/themes/theme/abc/message/${testMessage.id}/newReaction")
        val routedRequest =
            RoutedRequest(
                request,
                UriTemplate.from("/themes/theme/{themeId}/message/{mesId}/newReaction"),
            )
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        result.body.shouldNotBeNull()
    }

    test("show new reaction form with null theme id - Should return BAD_REQUEST status") {

        testMessages.add(testMessage)
        val form = WebForm()
        val handler = ShowNewReactionFormHandler(testThemes, form, htmlView)
        val request = Request(Method.GET, "/themes/theme//message/${testMessage.id}/newReaction")
        val routedRequest =
            RoutedRequest(
                request,
                UriTemplate.from("/themes/theme/{themeId}/message/{mesId}/newReaction"),
            )
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        result.body.shouldNotBeNull()
    }

    test("show new reaction form with invalid message id - Should return BAD_REQUEST status") {

        testMessages.add(testMessage)
        val form = WebForm()
        val handler = ShowNewReactionFormHandler(testThemes, form, htmlView)
        val request = Request(Method.GET, "/themes/theme/${testTheme.id}/message/abc/newReaction")
        val routedRequest =
            RoutedRequest(
                request,
                UriTemplate.from("/themes/theme/{themeId}/message/{mesId}/newReaction"),
            )
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        result.body.shouldNotBeNull()
    }

    test("show new reaction form with null message id - Should return BAD_REQUEST status") {

        testMessages.add(testMessage)
        val form = WebForm()
        val handler = ShowNewReactionFormHandler(testThemes, form, htmlView)
        val request = Request(Method.GET, "/themes/theme/${testTheme.id}/message//newReaction")
        val routedRequest =
            RoutedRequest(
                request,
                UriTemplate.from("/themes/theme/{themeId}/message/{mesId}/newReaction"),
            )
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        result.body.shouldNotBeNull()
    }

    test("create new reaction - Should redirect to the /themes/theme/{themeId}/message/{mesId} route") {

        testMessages.add(testMessage)
        val form = WebForm().with(reactionAuthorField of "NordRaven", reactionTypeField of 128514)
        val handler = CreateNewReactionHandler(testThemes, htmlView)
        val request =
            Request(
                Method.POST,
                "/themes/theme/${testTheme.id}/message/${testMessage.id}/newReaction",
            ).with(reactionFormLens of form)
        val routedRequest =
            RoutedRequest(
                request,
                UriTemplate.from("/themes/theme/{themeId}/message/{mesId}/newReaction"),
            )
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.FOUND).and(
                haveHeader(
                    "Location",
                    "/themes/theme/${testTheme.id}/message/${testMessage.id}",
                ),
            ),
        )
        testMessage.listOfReactions.shouldNotBeEmpty()
        result.body.shouldNotBeNull()
    }

    test("create new reaction with invalid theme id -  Should return BAD_REQUEST status") {

        testMessages.add(testMessage)
        val form = WebForm().with(reactionAuthorField of "NordRaven", reactionTypeField of 128514)
        val handler = CreateNewReactionHandler(testThemes, htmlView)
        val request =
            Request(
                Method.POST,
                "/themes/theme/abc/message/${testMessage.id}/newReaction",
            ).with(reactionFormLens of form)
        val routedRequest =
            RoutedRequest(
                request,
                UriTemplate.from("/themes/theme/{themeId}/message/{mesId}/newReaction"),
            )
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        testMessage.listOfReactions.shouldBeEmpty()
        result.body.shouldNotBeNull()
    }

    test("create new reaction with null theme id -  Should return BAD_REQUEST status") {

        testMessages.add(testMessage)
        val form = WebForm().with(reactionAuthorField of "NordRaven", reactionTypeField of 128514)
        val handler = CreateNewReactionHandler(testThemes, htmlView)
        val request =
            Request(
                Method.POST,
                "/themes/theme//message/${testMessage.id}/newReaction",
            ).with(reactionFormLens of form)
        val routedRequest =
            RoutedRequest(
                request,
                UriTemplate.from("/themes/theme/{themeId}/message/{mesId}/newReaction"),
            )
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        testMessage.listOfReactions.shouldBeEmpty()
        result.body.shouldNotBeNull()
    }

    test("create new reaction with invalid message id -  Should return BAD_REQUEST status") {

        testMessages.add(testMessage)
        val form = WebForm().with(reactionAuthorField of "NordRaven", reactionTypeField of 128514)
        val handler = CreateNewReactionHandler(testThemes, htmlView)
        val request =
            Request(
                Method.POST,
                "/themes/theme/${testTheme.id}/message/abc/newReaction",
            ).with(reactionFormLens of form)
        val routedRequest =
            RoutedRequest(
                request,
                UriTemplate.from("/themes/theme/{themeId}/message/{mesId}/newReaction"),
            )
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        testMessage.listOfReactions.shouldBeEmpty()
        result.body.shouldNotBeNull()
    }

    test("create new reaction with null message id -  Should return BAD_REQUEST status") {

        testMessages.add(testMessage)
        val form = WebForm().with(reactionAuthorField of "NordRaven", reactionTypeField of 128514)
        val handler = CreateNewReactionHandler(testThemes, htmlView)
        val request =
            Request(
                Method.POST,
                "/themes/theme/${testTheme.id}/message//newReaction",
            ).with(reactionFormLens of form)
        val routedRequest =
            RoutedRequest(
                request,
                UriTemplate.from("/themes/theme/{themeId}/message/{mesId}/newReaction"),
            )
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        testMessage.listOfReactions.shouldBeEmpty()
        result.body.shouldNotBeNull()
    }

    test("create new reaction with invalid field -  Should return BAD_REQUEST status") {

        testMessages.add(testMessage)
        val form = WebForm().with(reactionAuthorField of " ", reactionTypeField of 128514)
        val handler = CreateNewReactionHandler(testThemes, htmlView)
        val request =
            Request(
                Method.POST,
                "/themes/theme/${testTheme.id}/message/${testMessage.id}/newReaction",
            ).with(reactionFormLens of form)
        val routedRequest =
            RoutedRequest(
                request,
                UriTemplate.from("/themes/theme/{themeId}/message/{mesId}/newReaction"),
            )
        val invalidForm = reactionFormLens(request)
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        invalidForm.errors.shouldNotBeEmpty()
        testMessage.listOfReactions.shouldBeEmpty()
        result.body.shouldNotBeNull()
    }

    test("show delete reaction form - should return OK status") {

        testMessage.listOfReactions.add(Reaction(128514, Author("NordRaven")))
        testMessages.add(testMessage)
        val handler = ShowDeleteReactionFormHandler(testThemes, htmlView)
        val request = Request(Method.GET, "/themes/theme/${testTheme.id}/message/${testMessage.id}/deleteReaction/0")
        val routedRequest =
            RoutedRequest(
                request,
                UriTemplate.from(
                    "/themes/theme/{themeId}/message/{mesId}/deleteReaction/{reactNum}",
                ),
            )
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.OK),
        )
        result.body.shouldNotBeNull()
    }

    test("show delete reaction form with theme message id - should return BAD_REQUEST status") {

        testMessage.listOfReactions.add(Reaction(128514, Author("NordRaven")))
        testMessages.add(testMessage)
        val handler = ShowDeleteReactionFormHandler(testThemes, htmlView)
        val request = Request(Method.GET, "/themes/theme/abc/message/${testMessage.id}/deleteReaction/0")
        val routedRequest =
            RoutedRequest(
                request,
                UriTemplate.from(
                    "/themes/theme/{themeId}/message/{mesId}/deleteReaction/{reactNum}",
                ),
            )
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        result.body.shouldNotBeNull()
    }

    test("show delete reaction form with null theme id - should return BAD_REQUEST status") {

        testMessage.listOfReactions.add(Reaction(128514, Author("NordRaven")))
        testMessages.add(testMessage)
        val handler = ShowDeleteReactionFormHandler(testThemes, htmlView)
        val request = Request(Method.GET, "/themes/theme//message/${testMessage.id}/deleteReaction/0")
        val routedRequest =
            RoutedRequest(
                request,
                UriTemplate.from(
                    "/themes/theme/{themeId}/message/{mesId}/deleteReaction/{reactNum}",
                ),
            )
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        result.body.shouldNotBeNull()
    }

    test("show delete reaction form with invalid message id - should return BAD_REQUEST status") {

        testMessage.listOfReactions.add(Reaction(128514, Author("NordRaven")))
        testMessages.add(testMessage)
        val handler = ShowDeleteReactionFormHandler(testThemes, htmlView)
        val request =
            Request(
                Method.GET,
                "/themes/theme/${testTheme.id}/message/abc/deleteReaction/0",
            )
        val routedRequest =
            RoutedRequest(
                request,
                UriTemplate.from(
                    "/themes/theme/{themeId}/message/{mesId}/deleteReaction/{reactNum}",
                ),
            )
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        result.body.shouldNotBeNull()
    }

    test("show delete reaction form with null message id - should return BAD_REQUEST status") {

        testMessage.listOfReactions.add(Reaction(128514, Author("NordRaven")))
        testMessages.add(testMessage)
        val handler = ShowDeleteReactionFormHandler(testThemes, htmlView)
        val request =
            Request(
                Method.GET,
                "/themes/theme/${testTheme.id}/message//deleteReaction/0",
            )
        val routedRequest =
            RoutedRequest(
                request,
                UriTemplate.from(
                    "/themes/theme/{themeId}/message/{mesId}/deleteReaction/{reactNum}",
                ),
            )
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        result.body.shouldNotBeNull()
    }

    test("show delete reaction form with invalid reaction number - should return BAD_REQUEST status") {

        testMessage.listOfReactions.add(Reaction(128514, Author("NordRaven")))
        testMessages.add(testMessage)
        val handler = ShowDeleteReactionFormHandler(testThemes, htmlView)
        val request =
            Request(
                Method.GET,
                "/themes/theme/${testTheme.id}/message/${testMessage.id}/deleteReaction/fddf",
            )
        val routedRequest =
            RoutedRequest(
                request,
                UriTemplate.from(
                    "/themes/theme/{themeId}/message/{mesId}/deleteReaction/{reactNum}",
                ),
            )
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        result.body.shouldNotBeNull()
    }

    test("delete reaction - Should redirect to the /themes/theme/{themeId}/message/{mesId} route") {

        testMessage.listOfReactions.add(Reaction(128514, Author("NordRaven")))
        testMessages.add(testMessage)
        val form =
            WebForm().with(
                checkField of "on",
            )
        val handler = DeleteReactionHandler(testThemes, htmlView)
        val request =
            Request(
                Method.POST,
                "/themes/theme/${testTheme.id}/message/${testMessage.id}/deleteReaction/0",
            ).with(deleteLens of form)
        val routedRequest =
            RoutedRequest(
                request,
                UriTemplate.from("/themes/theme/{themeId}/message/{mesId}/deleteReaction/{reactNum}"),
            )
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.FOUND).and(
                haveHeader(
                    "Location",
                    "/themes/theme/${testTheme.id}/message/${testMessage.id}",
                ),
            ),
        )
        testMessage.listOfReactions.shouldBeEmpty()
        result.body.shouldNotBeNull()
    }

    test("delete reaction with invalid theme id - should return BAD_REQUEST status") {

        testMessage.listOfReactions.add(Reaction(128514, Author("NordRaven")))
        testMessages.add(testMessage)
        val form =
            WebForm().with(
                checkField of "on",
            )
        val handler = DeleteReactionHandler(testThemes, htmlView)
        val request =
            Request(
                Method.POST,
                "/themes/theme/abc/message/${testMessage.id}/deleteReaction/0",
            ).with(deleteLens of form)
        val routedRequest =
            RoutedRequest(
                request,
                UriTemplate.from("/themes/theme/{themeId}/message/{mesId}/deleteReaction/{reactNum}"),
            )
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        testMessage.listOfReactions.shouldNotBeEmpty()
        result.body.shouldNotBeNull()
    }

    test("delete reaction with null theme id - should return BAD_REQUEST status") {

        testMessage.listOfReactions.add(Reaction(128514, Author("NordRaven")))
        testMessages.add(testMessage)
        val form =
            WebForm().with(
                checkField of "on",
            )
        val handler = DeleteReactionHandler(testThemes, htmlView)
        val request =
            Request(
                Method.POST,
                "/themes/theme//message/${testMessage.id}/deleteReaction/0",
            ).with(deleteLens of form)
        val routedRequest =
            RoutedRequest(
                request,
                UriTemplate.from("/themes/theme/{themeId}/message/{mesId}/deleteReaction/{reactNum}"),
            )
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        testMessage.listOfReactions.shouldNotBeEmpty()
        result.body.shouldNotBeNull()
    }

    test("delete reaction with invalid message id - should return BAD_REQUEST status") {

        testMessage.listOfReactions.add(Reaction(128514, Author("NordRaven")))
        testMessages.add(testMessage)
        val form =
            WebForm().with(
                checkField of "on",
            )
        val handler = DeleteReactionHandler(testThemes, htmlView)
        val request =
            Request(
                Method.POST,
                "/themes/theme/${testTheme.id}/message/abc/deleteReaction/0",
            ).with(deleteLens of form)
        val routedRequest =
            RoutedRequest(
                request,
                UriTemplate.from("/themes/theme/{themeId}/message/{mesId}/deleteReaction/{reactNum}"),
            )
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        testMessage.listOfReactions.shouldNotBeEmpty()
        result.body.shouldNotBeNull()
    }

    test("delete reaction with null message id - should return BAD_REQUEST status") {

        testMessage.listOfReactions.add(Reaction(128514, Author("NordRaven")))
        testMessages.add(testMessage)
        val form =
            WebForm().with(
                checkField of "on",
            )
        val handler = DeleteReactionHandler(testThemes, htmlView)
        val request =
            Request(
                Method.POST,
                "/themes/theme/${testTheme.id}/message//deleteReaction/0",
            ).with(deleteLens of form)
        val routedRequest =
            RoutedRequest(
                request,
                UriTemplate.from("/themes/theme/{themeId}/message/{mesId}/deleteReaction/{reactNum}"),
            )
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        testMessage.listOfReactions.shouldNotBeEmpty()
        result.body.shouldNotBeNull()
    }

    test("delete reaction with invalid reaction number - should return BAD_REQUEST status") {

        testMessage.listOfReactions.add(Reaction(128514, Author("NordRaven")))
        testMessages.add(testMessage)
        val form =
            WebForm().with(
                checkField of "on",
            )
        val handler = DeleteReactionHandler(testThemes, htmlView)
        val request =
            Request(
                Method.POST,
                "/themes/theme/${testTheme.id}/message/${testMessage.id}/deleteReaction/fgf",
            ).with(deleteLens of form)
        val routedRequest =
            RoutedRequest(
                request,
                UriTemplate.from("/themes/theme/{themeId}/message/{mesId}/deleteReaction/{reactNum}"),
            )
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        testMessage.listOfReactions.shouldNotBeEmpty()
        result.body.shouldNotBeNull()
    }

    test("delete reaction without check field - should return BAD_REQUEST status") {

        testMessage.listOfReactions.add(Reaction(128514, Author("NordRaven")))
        testMessages.add(testMessage)
        val form = WebForm()
        val handler = DeleteReactionHandler(testThemes, htmlView)
        val request =
            Request(
                Method.POST,
                "/themes/theme/${testTheme.id}/message/${testMessage.id}/deleteReaction/0",
            ).with(deleteLens of form)
        val routedRequest =
            RoutedRequest(
                request,
                UriTemplate.from("/themes/theme/{themeId}/message/{mesId}/deleteReaction/{reactNum}"),
            )
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        testMessage.listOfReactions.shouldNotBeEmpty()
        result.body.shouldNotBeNull()
    }
})
