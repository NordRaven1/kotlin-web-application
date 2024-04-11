package ru.uniyar.web.models

import org.http4k.template.ViewModel
import ru.uniyar.Paginator
import ru.uniyar.domain.AuthorStructure
import ru.uniyar.domain.ThemeAndMessages
import java.time.LocalDateTime

class ThemesListPageVM(
    val paginator: Paginator<AuthorStructure<ThemeAndMessages>>,
    val mindate: LocalDateTime?,
    val maxdate: LocalDateTime?,
    val themeSearch: String?,
) : ViewModel
