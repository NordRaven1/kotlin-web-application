package ru.uniyar.handlers

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.and
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
import ru.uniyar.domain.Messages
import ru.uniyar.domain.Theme
import ru.uniyar.domain.ThemeAndMessages
import ru.uniyar.domain.Themes
import ru.uniyar.web.handlers.CreateNewThemeHandler
import ru.uniyar.web.handlers.DeleteThemeHandler
import ru.uniyar.web.handlers.EditThemeHandler
import ru.uniyar.web.handlers.ShowDeleteThemeFormHandler
import ru.uniyar.web.handlers.ShowEditThemeFormHandler
import ru.uniyar.web.handlers.ShowNewThemeFormHandler
import ru.uniyar.web.handlers.checkField
import ru.uniyar.web.handlers.deleteLens
import ru.uniyar.web.handlers.themeAddingField
import ru.uniyar.web.handlers.themeAuthorField
import ru.uniyar.web.handlers.themeFormLens
import ru.uniyar.web.handlers.themeTitleField

class ThemeHandlersFunctionsTest : FunSpec({

    isolationMode = IsolationMode.InstancePerTest

    val testTheme = Theme("Активный отдых", Author("NordRaven"))
    val testTheme2 = Theme("История", Author("NordRaven"))
    val testMessages = Messages()
    val testMessages2 = Messages()
    val testThemes =
        Themes(
            listOf(
                ThemeAndMessages(testTheme, testMessages),
                ThemeAndMessages(testTheme2, testMessages2),
            ),
        )

    test("show new theme form - Should return OK status") {

        val form = WebForm()
        val handler = ShowNewThemeFormHandler(form, htmlView)
        val request = Request(Method.GET, "/themes/new")
        val result = handler(request)
        result.should(
            haveStatus(Status.OK),
        )
        result.body.shouldNotBeNull()
    }

    test("create new theme - Should redirect to the /themes route") {

        val form =
            WebForm().with(
                themeAuthorField of "NordRaven",
                themeAddingField of "on",
                themeTitleField of "Новая тема",
            )
        val handler = CreateNewThemeHandler(testThemes, htmlView)
        val request = Request(Method.POST, "/themes/new").with(themeFormLens of form)
        val result = handler(request)
        result.should(
            haveStatus(Status.FOUND).and(haveHeader("Location", "/themes")),
        )
        result.body.shouldNotBeNull()
    }

    test("create new theme with invalid field - Should return BAD_REQUEST status") {

        val form =
            WebForm().with(
                themeAuthorField of " ",
                themeAddingField of "on",
                themeTitleField of "Новая тема",
            )
        val handler = CreateNewThemeHandler(testThemes, htmlView)
        val invalidRequest = Request(Method.POST, "/themes/new").with(themeFormLens of form)
        val invalidForm = themeFormLens(invalidRequest)
        val result = handler(invalidRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        invalidForm.errors.shouldNotBeEmpty()
        result.body.shouldNotBeNull()
    }

    test("create new theme with repeated theme - Should return BAD_REQUEST status") {

        val form =
            WebForm().with(
                themeAuthorField of "NordRaven",
                themeAddingField of "on",
                themeTitleField of "Активный отдых",
            )
        val handler = CreateNewThemeHandler(testThemes, htmlView)
        val invalidRequest = Request(Method.POST, "/themes/new").with(themeFormLens of form)
        val result = handler(invalidRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        result.body.shouldNotBeNull()
    }

    test("show edit theme form - Should return OK status") {

        val handler = ShowEditThemeFormHandler(testThemes::fetchThemeByNumber, htmlView)
        val request = Request(Method.GET, "/themes/theme/${testTheme.id}/edit")
        val routedRequest = RoutedRequest(request, UriTemplate.from("/themes/theme/{themeId}/edit"))
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.OK),
        )
        result.body.shouldNotBeNull()
    }

    test("show edit theme form with invalid theme id - Should return BAD_REQUEST status") {

        val handler = ShowEditThemeFormHandler(testThemes::fetchThemeByNumber, htmlView)
        val request = Request(Method.GET, "/themes/theme/abc/edit")
        val routedRequest = RoutedRequest(request, UriTemplate.from("/themes/theme/{themeId}/edit"))
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        result.body.shouldNotBeNull()
    }

    test("show edit theme form with null theme id - Should return BAD_REQUEST status") {

        val handler = ShowEditThemeFormHandler(testThemes::fetchThemeByNumber, htmlView)
        val request = Request(Method.GET, "/themes/theme//edit")
        val routedRequest = RoutedRequest(request, UriTemplate.from("/themes/theme/{themeId}/edit"))
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        result.body.shouldNotBeNull()
    }

    test("edit theme - Should redirect to the /themes/theme/{themeId} route") {

        val form =
            WebForm().with(
                themeAuthorField of "NordRaven",
                themeAddingField of "on",
                themeTitleField of "Новое название",
            )
        val handler = EditThemeHandler(testThemes, htmlView)
        val request = Request(Method.POST, "/themes/theme/${testTheme.id}/edit").with(themeFormLens of form)
        val routedRequest = RoutedRequest(request, UriTemplate.from("/themes/theme/{themeId}/edit"))
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.FOUND).and(haveHeader("Location", "/themes/theme/${testTheme.id}")),
        )
        testThemes.themesList[0].theme.title.shouldBe("Новое название")
        result.body.shouldNotBeNull()
    }

    test("edit theme with invalid theme id - should return BAD_REQUEST status") {

        val form =
            WebForm().with(
                themeAuthorField of "NordRaven",
                themeAddingField of "on",
                themeTitleField of "Новое название",
            )
        val handler = EditThemeHandler(testThemes, htmlView)
        val request = Request(Method.POST, "/themes/theme/abc/edit").with(themeFormLens of form)
        val routedRequest = RoutedRequest(request, UriTemplate.from("/themes/theme/{themeId}/edit"))
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        testThemes.themesList[0].theme.title.shouldBe("Активный отдых")
        result.body.shouldNotBeNull()
    }

    test("edit theme with null theme id - should return BAD_REQUEST status") {

        val form =
            WebForm().with(
                themeAuthorField of "NordRaven",
                themeAddingField of "on",
                themeTitleField of "Новое название",
            )
        val handler = EditThemeHandler(testThemes, htmlView)
        val request = Request(Method.POST, "/themes/theme//edit").with(themeFormLens of form)
        val routedRequest = RoutedRequest(request, UriTemplate.from("/themes/theme/{themeId}/edit"))
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        testThemes.themesList[0].theme.title.shouldBe("Активный отдых")
        result.body.shouldNotBeNull()
    }

    test("edit theme with repeated theme - should return BAD_REQUEST status") {

        val form =
            WebForm().with(
                themeAuthorField of "NordRaven",
                themeAddingField of "on",
                themeTitleField of "История",
            )
        val handler = EditThemeHandler(testThemes, htmlView)
        val request = Request(Method.POST, "/themes/theme/${testTheme.id}/edit").with(themeFormLens of form)
        val routedRequest = RoutedRequest(request, UriTemplate.from("/themes/theme/{themeId}/edit"))
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        testThemes.themesList[0].theme.title.shouldBe("Активный отдых")
        result.body.shouldNotBeNull()
    }

    test("edit theme with invalid field - should return BAD_REQUEST status") {

        val form =
            WebForm().with(
                themeAuthorField of "",
                themeAddingField of "on",
                themeTitleField of "Новое название",
            )
        val handler = EditThemeHandler(testThemes, htmlView)
        val request = Request(Method.POST, "/themes/theme/${testTheme.id}/edit").with(themeFormLens of form)
        val routedRequest = RoutedRequest(request, UriTemplate.from("/themes/theme/{themeId}/edit"))
        val invalidForm = themeFormLens(request)
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        testThemes.themesList[0].theme.title.shouldBe("Активный отдых")
        invalidForm.errors.shouldNotBeEmpty()
        result.body.shouldNotBeNull()
    }

    test("show delete theme form - should return OK status") {

        val handler = ShowDeleteThemeFormHandler(testThemes::fetchThemeByNumber, htmlView)
        val request = Request(Method.GET, "/themes/theme/${testTheme.id}/delete")
        val routedRequest = RoutedRequest(request, UriTemplate.from("/themes/theme/{themeId}/delete"))
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.OK),
        )
        result.body.shouldNotBeNull()
    }

    test("show delete theme form with invalid theme id - should return BAD_REQUEST status") {

        val handler = ShowDeleteThemeFormHandler(testThemes::fetchThemeByNumber, htmlView)
        val request = Request(Method.GET, "/themes/theme/abc/delete")
        val routedRequest = RoutedRequest(request, UriTemplate.from("/themes/theme/{themeId}/delete"))
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        result.body.shouldNotBeNull()
    }

    test("show delete theme form with null theme id - should return BAD_REQUEST status") {

        val handler = ShowDeleteThemeFormHandler(testThemes::fetchThemeByNumber, htmlView)
        val request = Request(Method.GET, "/themes/theme//delete")
        val routedRequest = RoutedRequest(request, UriTemplate.from("/themes/theme/{themeId}/delete"))
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        result.body.shouldNotBeNull()
    }

    test("delete theme - Should redirect to the /themes route") {

        val form =
            WebForm().with(
                checkField of "on",
            )
        val handler = DeleteThemeHandler(testThemes, htmlView)
        val request = Request(Method.POST, "/themes/theme/${testTheme.id}/delete").with(deleteLens of form)
        val routedRequest = RoutedRequest(request, UriTemplate.from("/themes/theme/{themeId}/delete"))
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.FOUND).and(haveHeader("Location", "/themes")),
        )
        testThemes.themesList[0].theme.title.shouldBe("История")
        result.body.shouldNotBeNull()
    }

    test("delete theme with invalid theme id - should return BAD_REQUEST status") {

        val form =
            WebForm().with(
                checkField of "on",
            )
        val handler = DeleteThemeHandler(testThemes, htmlView)
        val request = Request(Method.POST, "/themes/theme/abc/delete").with(deleteLens of form)
        val routedRequest = RoutedRequest(request, UriTemplate.from("/themes/theme/{themeId}/delete"))
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        testThemes.themesList[0].theme.title.shouldBe("Активный отдых")
        result.body.shouldNotBeNull()
    }

    test("delete theme with null theme id - should return BAD_REQUEST status") {

        val form =
            WebForm().with(
                checkField of "on",
            )
        val handler = DeleteThemeHandler(testThemes, htmlView)
        val request = Request(Method.POST, "/themes/theme//delete").with(deleteLens of form)
        val routedRequest = RoutedRequest(request, UriTemplate.from("/themes/theme/{themeId}/delete"))
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        testThemes.themesList[0].theme.title.shouldBe("Активный отдых")
        result.body.shouldNotBeNull()
    }

    test("delete theme without check field - should return BAD_REQUEST status") {

        val form = WebForm()
        val handler = DeleteThemeHandler(testThemes, htmlView)
        val request = Request(Method.POST, "/themes/theme/${testTheme.id}/delete").with(deleteLens of form)
        val routedRequest = RoutedRequest(request, UriTemplate.from("/themes/theme/{themeId}/delete"))
        val result = handler(routedRequest)
        result.should(
            haveStatus(Status.BAD_REQUEST),
        )
        testThemes.themesList[0].theme.title.shouldBe("Активный отдых")
        result.body.shouldNotBeNull()
    }
})
