package ru.uniyar.web.models

import org.http4k.template.ViewModel
import ru.uniyar.domain.AuthorStructure
import ru.uniyar.domain.Reaction

class DeleteReactionDataVM(
    val reaction: AuthorStructure<Reaction>,
    val check: Boolean = false,
) : ViewModel
