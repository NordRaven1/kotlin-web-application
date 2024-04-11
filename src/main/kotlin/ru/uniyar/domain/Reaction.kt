package ru.uniyar.domain
import ru.uniyar.formTodaysDate

data class Reaction(val reactionType: Int, val author: String) {
    val reactionDate = formTodaysDate()
}
