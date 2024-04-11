package ru.uniyar.web.models

import org.http4k.lens.WebForm
import org.http4k.template.ViewModel
import ru.uniyar.domain.Theme

class EditThemeDataVM(
    val theme: Theme,
    val form: WebForm,
    val failures: List<String> = emptyList(),
) : ViewModel
