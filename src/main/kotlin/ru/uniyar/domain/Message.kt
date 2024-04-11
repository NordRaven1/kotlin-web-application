package ru.uniyar.domain
import ru.uniyar.formTodaysDate
import java.util.UUID

class Message(
    var theme: Theme,
    val author: String,
    val text: String,
    val reactions: List<Reaction> = listOf(),
) {
    private val reactionsList = reactions.toMutableList()
    val listOfReactions: List<Reaction> = reactionsList
    var addDate = formTodaysDate()
    var updateDate = formTodaysDate()
    var id = UUID.randomUUID().toString()
    var revisions = 1

    fun addReaction(reaction: Reaction) {
        reactionsList.add(reaction)
    }

    fun removeReaction(num: Int) {
        reactionsList.removeAt(num)
    }
}
