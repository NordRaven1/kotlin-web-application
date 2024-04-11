package ru.uniyar.domain

import org.http4k.core.Uri
import ru.uniyar.Paginator
import ru.uniyar.authorization.Users
import ru.uniyar.itemsByPageNumber
import ru.uniyar.pageAmount
import ru.uniyar.safeDateInFormat
import java.time.LocalDateTime

class Themes(gotlist: List<ThemeAndMessages>) {
    private val listOfThemes = gotlist.toMutableList()
    val themesList: List<ThemeAndMessages> = listOfThemes

    fun add(themeAndMessages: ThemeAndMessages) {
        listOfThemes.add(themeAndMessages)
    }

    fun fetchThemeByNumber(id: String): ThemeAndMessages? {
        return listOfThemes.find { it.theme.id == id }
    }

    fun replaceTheme(
        id: String,
        newTheme: ThemeAndMessages,
    ) {
        val oldTheme = listOfThemes.first { it.theme.id == id }
        val i = getItemPositionById(oldTheme.theme.id)
        newTheme.theme.id = oldTheme.theme.id
        newTheme.theme.addDate = oldTheme.theme.addDate
        for (message in newTheme.messages.listOfMessage) {
            message.theme = newTheme.theme
        }
        listOfThemes[i] = newTheme
    }

    fun removeTheme(id: String) {
        val deleteTheme = listOfThemes.first { it.theme.id == id }
        val i = getItemPositionById(deleteTheme.theme.id)
        listOfThemes.removeAt(i)
    }

    private fun getItemPositionById(id: String): Int {
        listOfThemes.forEachIndexed { index, item ->
            if (item.theme.id == id) {
                return index
            }
        }
        return -1
    }

    fun themesByUserParameters(
        minD: LocalDateTime?,
        maxD: LocalDateTime?,
        themeSearch: String?,
    ): List<ThemeAndMessages> {
        var filteredList = listOfThemes.toList()
        if (themeSearch != null) {
            filteredList = listOfThemes.filter { it.theme.title.contains(themeSearch, true) }
        }
        if (minD != null) {
            filteredList =
                filteredList.filter {
                    safeDateInFormat(it.theme.addDate).isAfter(minD) ||
                        safeDateInFormat(it.theme.addDate).isEqual(minD)
                }
        }
        if (maxD != null) {
            filteredList =
                filteredList.filter {
                    safeDateInFormat(it.theme.addDate).isBefore(maxD) ||
                        safeDateInFormat(it.theme.addDate).isEqual(maxD)
                }
        }
        return filteredList
    }

    fun getThemesPerPage(
        users: Users,
        themeSearch: String?,
        mindate: LocalDateTime?,
        maxdate: LocalDateTime?,
        pageNum: Int,
        uri: Uri,
    ): Paginator<AuthorStructure<ThemeAndMessages>> {
        val filteredList = themesByUserParameters(mindate, maxdate, themeSearch)
        val pageAmount = pageAmount(filteredList)
        val pagedList = itemsByPageNumber(pageNum, filteredList)
        val themes = mutableListOf<AuthorStructure<ThemeAndMessages>>()
        for (theme in pagedList) {
            val themeAuthor = users.usersList.first { it.userId == theme.theme.author }
            themes.add(AuthorStructure(theme, themeAuthor.userName))
        }
        return Paginator(themes, uri, pageNum, pageAmount)
    }
}
