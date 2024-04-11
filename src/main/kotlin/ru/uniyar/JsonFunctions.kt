package ru.uniyar

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import ru.uniyar.authorization.User
import ru.uniyar.authorization.Users
import ru.uniyar.domain.ThemeAndMessages
import ru.uniyar.domain.Themes
import java.io.File
import java.nio.file.Paths

private const val THEMES_PATH = "data/data.json"
private const val USERS_PATH = "data/users.json"

fun saveToFileThemes(themes: Themes) {
    try {
        val mapper = jacksonObjectMapper().setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        val writer: ObjectWriter = mapper.writer(DefaultPrettyPrinter())
        writer.writeValue(Paths.get(THEMES_PATH).toFile(), themes.themesList)
    } catch (e: Exception) {
        println("Something went wrong during json serialization")
        println(e.message)
        e.printStackTrace()
    }
}

fun loadFromFileThemes(): List<ThemeAndMessages> {
    return try {
        val jsonString: String = File(THEMES_PATH).readText(Charsets.UTF_8)
        val mapper = jacksonObjectMapper().setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        mapper.readValue(
            jsonString,
            jacksonObjectMapper().typeFactory.constructCollectionType(List::class.java, ThemeAndMessages::class.java),
        )
    } catch (e: Exception) {
        println("Something went wrong during json deserialization")
        println(e.message)
        emptyList()
    }
}

fun saveToFileUsers(users: Users) {
    try {
        val mapper = jacksonObjectMapper().setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        val writer: ObjectWriter = mapper.writer(DefaultPrettyPrinter())
        writer.writeValue(Paths.get(USERS_PATH).toFile(), users.usersList)
    } catch (e: Exception) {
        println("Something went wrong during json serialization")
        println(e.message)
        e.printStackTrace()
    }
}

fun loadFromFileUsers(): List<User> {
    return try {
        val jsonString: String = File(USERS_PATH).readText(Charsets.UTF_8)
        val mapper = jacksonObjectMapper().setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        mapper.readValue(
            jsonString,
            jacksonObjectMapper().typeFactory.constructCollectionType(List::class.java, User::class.java),
        )
    } catch (e: Exception) {
        println("Something went wrong during json deserialization")
        println(e.message)
        emptyList()
    }
}
