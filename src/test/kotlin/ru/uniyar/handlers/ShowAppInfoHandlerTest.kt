package ru.uniyar.handlers

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.kotest.haveStatus
import ru.uniyar.web.handlers.ShowAppInfoHandler

class ShowAppInfoHandlerTest : FunSpec({

    test("Should return OK status") {

        val handler = ShowAppInfoHandler(htmlView)
        val request = Request(Method.GET, "/")
        val result = handler(request)
        result.should(
            haveStatus(Status.OK),
        )
        result.body.shouldNotBeNull()
    }
})
