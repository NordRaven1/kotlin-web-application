package ru.uniyar

import org.http4k.core.Uri
import org.http4k.core.query
import org.http4k.core.removeQuery

class Paginator<T>(
    val elementList: List<T>,
    path: Uri,
    page: Int,
    pageAmount: Int,
) {
    private val amountOfPages =
        if (pageAmount != 0) {
            pageAmount
        } else {
            1
        }
    val targetPage =
        if (page > amountOfPages || page < 1) {
            1
        } else {
            page
        }
    private val uriAndFilters = path.removeQuery("page")
    val previousPageUri = uriAndFilters.query("page", (targetPage - 1).toString())
    val previousPage =
        if (targetPage == 1) {
            "disabled"
        } else {
            ""
        }
    val nextPageUri = uriAndFilters.query("page", (targetPage + 1).toString())
    val nextPage =
        if (targetPage == amountOfPages) {
            "disabled"
        } else {
            ""
        }
    val firstPageUri = uriAndFilters.query("page", "1")
    val lastPageUri = uriAndFilters.query("page", amountOfPages.toString())
}
