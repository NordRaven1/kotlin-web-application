package ru.uniyar.web.models

import org.http4k.lens.WebForm
import org.http4k.template.ViewModel

class NewMessageDataVM(
    val form: WebForm,
    val failures: List<String> = emptyList(),
) : ViewModel
