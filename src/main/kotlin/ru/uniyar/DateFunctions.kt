package ru.uniyar

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Date
import java.util.concurrent.TimeUnit

fun safeDateInMillis(date: LocalDateTime): Long {
    return date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

fun unsafeDateInFormat(
    stringDate: String?,
    pattern: String = "dd MMM yyyy, HH:mm",
): LocalDateTime? {
    return if (stringDate == null || stringDate == "") {
        null
    } else {
        try {
            val formater = DateTimeFormatter.ofPattern(pattern)
            LocalDateTime.parse(stringDate, formater)
        } catch (e: DateTimeParseException) {
            null
        }
    }
}

fun safeDateInFormat(
    stringDate: String,
    pattern: String = "dd MMM yyyy, HH:mm",
): LocalDateTime {
    val formater = DateTimeFormatter.ofPattern(pattern)
    return LocalDateTime.parse(stringDate, formater)
}

fun formTodaysDate(): String {
    val formater = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
    val curNow = safeDateInMillis(LocalDateTime.now())
    val curToday = Instant.ofEpochMilli(curNow).atZone(ZoneId.systemDefault()).toLocalDateTime()
    return curToday.format(formater)
}

fun formLifeSpan(): Date {
    val now = Date().time
    val aDay = TimeUnit.DAYS.toMillis(1)
    return Date(now + aDay * 7)
}
