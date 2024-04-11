package ru.uniyar.web.models

import org.http4k.template.ViewModel
import ru.uniyar.Paginator
import ru.uniyar.domain.AuthorStructure
import ru.uniyar.domain.Message
import ru.uniyar.domain.Theme
import java.time.LocalDateTime

class MessageListPageVM(
    val paginator: Paginator<AuthorStructure<Message>>,
    val mindate: LocalDateTime?,
    val maxdate: LocalDateTime?,
    val addingPossibility: Boolean,
    val theme: Theme,
) : ViewModel
