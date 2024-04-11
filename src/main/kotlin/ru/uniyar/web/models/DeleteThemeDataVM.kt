package ru.uniyar.web.models

import org.http4k.template.ViewModel
import ru.uniyar.domain.AuthorStructure
import ru.uniyar.domain.ThemeAndMessages

class DeleteThemeDataVM(
    val themeAndMessages: AuthorStructure<ThemeAndMessages>,
    val check: Boolean = false,
) : ViewModel
