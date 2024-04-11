package ru.uniyar

import kotlin.math.ceil

private val pageLimit = 6

fun <T> pageAmount(list: List<T>): Int {
    val res = list.size.toDouble() / pageLimit
    return ceil(res).toInt()
}

fun <T> itemsByPageNumber(
    pageNumber: Int,
    list: List<T>,
): List<T> {
    var page = pageNumber
    if (page > pageAmount(list) || page < 1) {
        page = 1
    }
    val lowerBound = (page - 1) * pageLimit
    val upperBound = page * pageLimit - 1
    return list.filterIndexed { index, _ -> (index >= lowerBound) && (index <= upperBound) }
}
