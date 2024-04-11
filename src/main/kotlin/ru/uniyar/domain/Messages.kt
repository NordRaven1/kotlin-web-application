package ru.uniyar.domain

import org.http4k.core.Uri
import ru.uniyar.Paginator
import ru.uniyar.authorization.Users
import ru.uniyar.formTodaysDate
import ru.uniyar.itemsByPageNumber
import ru.uniyar.pageAmount
import ru.uniyar.safeDateInFormat
import ru.uniyar.safeDateInMillis
import java.time.LocalDateTime

class Messages {
    private val messagesList = mutableListOf<Message>()
    val listOfMessage: List<Message> = messagesList

    fun add(message: Message) {
        messagesList.add(message)
    }

    fun fetchMessageByNumber(id: String): Message? {
        return messagesList.find { it.id == id }
    }

    fun replaceMessage(
        id: String,
        newMessage: Message,
    ) {
        val oldMessage = messagesList.first { it.id == id }
        val i = getItemPositionById(oldMessage.id)
        newMessage.id = oldMessage.id
        newMessage.addDate = oldMessage.addDate
        newMessage.updateDate = formTodaysDate()
        newMessage.revisions = oldMessage.revisions + 1
        messagesList[i] = newMessage
    }

    fun removeMessage(id: String) {
        val deleteMessage = messagesList.first { it.id == id }
        val i = getItemPositionById(deleteMessage.id)
        messagesList.removeAt(i)
    }

    private fun getItemPositionById(id: String): Int {
        messagesList.forEachIndexed { index, item ->
            if (item.id == id) {
                return index
            }
        }
        return -1
    }

    fun messagesByUserParameters(
        minD: LocalDateTime?,
        maxD: LocalDateTime?,
    ): List<Message> {
        var filteredList =
            messagesList.sortedBy { safeDateInMillis(safeDateInFormat(it.addDate)) }
        if (minD != null) {
            filteredList =
                filteredList.filter {
                    safeDateInFormat(it.addDate).isAfter(minD) ||
                        safeDateInFormat(it.addDate).isEqual(minD)
                }
        }
        if (maxD != null) {
            filteredList =
                filteredList.filter {
                    safeDateInFormat(it.addDate).isBefore(maxD) ||
                        safeDateInFormat(it.addDate).isEqual(maxD)
                }
        }
        return filteredList
    }

    fun getMessagesPerPage(
        users: Users,
        mindate: LocalDateTime?,
        maxdate: LocalDateTime?,
        pageNum: Int,
        uri: Uri,
    ): Paginator<AuthorStructure<Message>> {
        val filteredList = messagesByUserParameters(mindate, maxdate)
        val pageAmount = pageAmount(filteredList)
        val pagedList = itemsByPageNumber(pageNum, filteredList)
        val messages = mutableListOf<AuthorStructure<Message>>()
        for (message in pagedList) {
            val messageAuthor = users.usersList.first { it.userId == message.author }
            messages.add(AuthorStructure(message, messageAuthor.userName))
        }
        return Paginator(messages, uri, pageNum, pageAmount)
    }
}
