package ru.uniyar.web.models

import org.http4k.template.ViewModel
import ru.uniyar.domain.AuthorStructure
import ru.uniyar.domain.Message
import ru.uniyar.domain.Reaction

class MessagePageVM(
    val message: AuthorStructure<Message>,
    val reactions: List<AuthorStructure<Reaction>>,
) : ViewModel
