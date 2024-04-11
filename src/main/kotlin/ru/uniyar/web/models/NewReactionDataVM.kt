package ru.uniyar.web.models

import org.http4k.lens.WebForm
import org.http4k.template.ViewModel

class NewReactionDataVM(
    val form: WebForm,
    val reactions: List<Int>,
    val failures: List<String> = emptyList(),
) : ViewModel
