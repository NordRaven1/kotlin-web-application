package ru.uniyar.domain

import ru.uniyar.formTodaysDate
import java.util.UUID

class Theme(val title: String, val author: String, val addPossibility: Boolean = true) {
    var addDate = formTodaysDate()
    var id = UUID.randomUUID().toString()
}
