package ru.uniyar.web.handlers

import org.http4k.core.Body
import org.http4k.lens.Failure
import org.http4k.lens.FormField
import org.http4k.lens.Path
import org.http4k.lens.Validator
import org.http4k.lens.int
import org.http4k.lens.nonBlankString
import org.http4k.lens.webForm
import ru.uniyar.authorization.Permissions
import ru.uniyar.web.models.NotFoundPageVM

val reactionsList =
    listOf(128514, 128545, 128528, 128525, 128557, 128564, 128561, 128533, 128526, 128522, 129324, 129313)

val rolesList =
    listOf(
        Permissions("BANNED"),
        Permissions(
            "AUTH_USER",
            canAddTheme = true, canEditTheme = true, canDeleteTheme = true,
            canAddMessage = true, canEditMessage = true, canDeleteMessage = true,
            canAddReaction = true, canDeleteReaction = true,
        ),
        Permissions(
            "MODERATOR",
            canAddTheme = true, canEditTheme = true, canDeleteTheme = true, canAddMessage = true,
            canEditMessage = true, canDeleteMessage = true, canAddReaction = true, canDeleteReaction = true,
            canChangeStatus = true,
        ),
        Permissions(
            "ADMIN",
            canAddTheme = true, canEditTheme = true, canDeleteTheme = true,
            canAddMessage = true, canEditMessage = true, canDeleteMessage = true, canAddReaction = true,
            canDeleteReaction = true, canChangeStatus = true,
        ),
    )

val themeIdLens = Path.nonBlankString().of("themeId")
val messageIdLens = Path.nonBlankString().of("mesId")
val userIdLens = Path.nonBlankString().of("userId")
val reactionNumberLens = Path.int().of("reactNum")
val checkField = FormField.nonBlankString().optional("agreement", "Согласие")

val themeTitleField = FormField.nonBlankString().required("title", "Название темы")
val themeAddingField =
    FormField.nonBlankString().optional(
        "addPossibility",
        "Возможность добавления сообщений",
    )
val themeFormLens =
    Body.webForm(
        Validator.Feedback,
        themeTitleField,
        themeAddingField,
    ).toLens()

val reactionTypeField = FormField.int().required("reaction", "Реакция")
val reactionFormLens =
    Body.webForm(
        Validator.Feedback,
        reactionTypeField,
    ).toLens()

val messageTextField = FormField.nonBlankString().required("text", "Текст сообщения")
val messageFormLens =
    Body.webForm(
        Validator.Feedback,
        messageTextField,
    ).toLens()

val userNameField = FormField.nonBlankString().required("userName", "Имя пользователя")
val passField = FormField.nonBlankString().required("password", "Пароль 1")
val pass2Field = FormField.nonBlankString().required("repeatPassword", "Пароль 2")
val roleField = FormField.nonBlankString().defaulted("role", "AUTH_USER", "Роль пользователя")
val userFormLens =
    Body.webForm(
        Validator.Feedback,
        userNameField,
        passField,
        pass2Field,
        roleField,
    ).toLens()

val editPasswordFormLens =
    Body.webForm(
        Validator.Feedback,
        passField,
        pass2Field,
    ).toLens()

val authFormLens =
    Body.webForm(
        Validator.Feedback,
        userNameField,
        passField,
    ).toLens()

val userAddThemeField = FormField.nonBlankString().optional("canAddTheme", "Возможность добавлять темы")
val userEditThemeField = FormField.nonBlankString().optional("canEditTheme", "Возможность редактировать темы")
val userDeleteThemeField = FormField.nonBlankString().optional("canDeleteTheme", "Возможность удалять темы")
val userAddMessageField = FormField.nonBlankString().optional("canAddMessage", "Возможность добавлять сообщения")
val userEditMessageField = FormField.nonBlankString().optional("canEditMessage", "Возможность редактировать сообщения")
val userDeleteMessageField = FormField.nonBlankString().optional("canDeleteMessage", "Возможность удалять сообщения")
val userAddReactionField = FormField.nonBlankString().optional("canAddReaction", "Возможность добавлять реакции")
val userDeleteReactionField = FormField.nonBlankString().optional("canDeleteReaction", "Возможность удалять реакции")
val userChangeStatusField = FormField.nonBlankString().optional("canChangeStatus", "Возможность открывать/закрывать темы")
val editPermissionsFormLens =
    Body.webForm(
        Validator.Feedback,
        userAddThemeField,
        userEditThemeField,
        userDeleteThemeField,
        userAddMessageField,
        userEditMessageField,
        userDeleteMessageField,
        userAddReactionField,
        userDeleteReactionField,
        userChangeStatusField,
    ).toLens()

val editRoleFormLens =
    Body.webForm(
        Validator.Feedback,
        roleField,
    ).toLens()

val deleteLens = Body.webForm(Validator.Feedback, checkField).toLens()
val errorModel = NotFoundPageVM()

fun formFailureInfo(failure: Failure): String {
    val recommendation =
        when (failure.type) {
            Failure.Type.Invalid -> "неверный формат"
            Failure.Type.Missing -> "должно быть заполнено"
            else -> "неподдерживаемый формат"
        }
    return "Поле '${failure.meta.description}' - $recommendation"
}

fun formFailureInfoList(failures: List<Failure>): MutableList<String> {
    val recommendations: MutableList<String> = mutableListOf()
    for (failure in failures) {
        recommendations.add(formFailureInfo(failure))
    }
    return recommendations
}
