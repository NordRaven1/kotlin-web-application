package ru.uniyar

import org.http4k.core.ContentType
import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.NoOp
import org.http4k.core.RequestContexts
import org.http4k.core.then
import org.http4k.filter.ServerFilters
import org.http4k.lens.RequestContextKey
import org.http4k.lens.RequestContextLens
import org.http4k.lens.WebForm
import org.http4k.routing.ResourceLoader
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.routing.static
import org.http4k.server.Netty
import org.http4k.server.asServer
import ru.uniyar.authorization.JwtTools
import ru.uniyar.authorization.Permissions
import ru.uniyar.authorization.SharedState
import ru.uniyar.authorization.Users
import ru.uniyar.config.readAuthSaltFromConfiguration
import ru.uniyar.config.readJwtSecretFromConfiguration
import ru.uniyar.config.readWebPortFromConfiguration
import ru.uniyar.domain.Themes
import ru.uniyar.web.filters.addStateFilter
import ru.uniyar.web.filters.errorFilter
import ru.uniyar.web.filters.permissionFilter
import ru.uniyar.web.handlers.AuthorizationHandler
import ru.uniyar.web.handlers.BanUserHandler
import ru.uniyar.web.handlers.CreateNewMessageHandler
import ru.uniyar.web.handlers.CreateNewReactionHandler
import ru.uniyar.web.handlers.CreateNewThemeHandler
import ru.uniyar.web.handlers.DeleteMessageHandler
import ru.uniyar.web.handlers.DeleteReactionHandler
import ru.uniyar.web.handlers.DeleteThemeHandler
import ru.uniyar.web.handlers.EditMessageHandler
import ru.uniyar.web.handlers.EditPasswordHandler
import ru.uniyar.web.handlers.EditPermissionsHandler
import ru.uniyar.web.handlers.EditThemeHandler
import ru.uniyar.web.handlers.EditUserHandler
import ru.uniyar.web.handlers.LogoutHandler
import ru.uniyar.web.handlers.MessageListHandler
import ru.uniyar.web.handlers.RegisterNewUserHandler
import ru.uniyar.web.handlers.ShowAppInfoHandler
import ru.uniyar.web.handlers.ShowAuthPageHandler
import ru.uniyar.web.handlers.ShowDeleteMessageFormHandler
import ru.uniyar.web.handlers.ShowDeleteReactionFormHandler
import ru.uniyar.web.handlers.ShowDeleteThemeFormHandler
import ru.uniyar.web.handlers.ShowEditMessageFormHandler
import ru.uniyar.web.handlers.ShowEditPasswordHandler
import ru.uniyar.web.handlers.ShowEditPermissionsFormHandler
import ru.uniyar.web.handlers.ShowEditThemeFormHandler
import ru.uniyar.web.handlers.ShowEditUserPageHandler
import ru.uniyar.web.handlers.ShowMessageHandler
import ru.uniyar.web.handlers.ShowNewMessageFormHandler
import ru.uniyar.web.handlers.ShowNewReactionFormHandler
import ru.uniyar.web.handlers.ShowNewThemeFormHandler
import ru.uniyar.web.handlers.ShowRegistrationPageHandler
import ru.uniyar.web.handlers.ThemesListHandler
import ru.uniyar.web.handlers.UserListHandler
import ru.uniyar.web.templates.ContextAwarePebbleTemplates
import ru.uniyar.web.templates.ContextAwareViewRender
import kotlin.concurrent.thread
import kotlin.system.exitProcess

fun router(
    lens: ContextAwareViewRender,
    themes: Themes,
    users: Users,
    jwtTools: JwtTools,
    permissionLens: RequestContextLens<Permissions>,
    sharedStateLens: RequestContextLens<SharedState?>,
) = routes(
    "/" bind GET to ShowAppInfoHandler(lens),
    "/login" bind GET to ShowAuthPageHandler(WebForm(), lens, permissionLens),
    "/login" bind POST to AuthorizationHandler(users, jwtTools, lens),
    "/logout" bind GET to LogoutHandler(),
    "/users/new" bind GET to ShowRegistrationPageHandler(WebForm(), lens, permissionLens),
    "/users/new" bind POST to RegisterNewUserHandler(users, lens),
    "/users/{userId}" bind GET to ShowEditUserPageHandler(users, lens, permissionLens),
    "/users/{userId}" bind POST to EditUserHandler(users, lens),
    "/users/{userId}/permissions" bind GET to ShowEditPermissionsFormHandler(users, lens, permissionLens),
    "/users/{userId}/permissions" bind POST to EditPermissionsHandler(users, lens),
    "/users/{userId}/password" bind GET to ShowEditPasswordHandler(users, WebForm(), lens, permissionLens),
    "/users/{userId}/password" bind POST to EditPasswordHandler(users, lens),
    "/users/{userId}/ban" bind GET to BanUserHandler(users, lens, permissionLens),
    "/users" bind GET to UserListHandler(users, lens, permissionLens),
    "/themes/new" bind GET to ShowNewThemeFormHandler(WebForm(), lens, permissionLens),
    "/themes/new" bind POST to CreateNewThemeHandler(users, themes, lens, sharedStateLens),
    "/themes" bind GET to ThemesListHandler(users, themes::getThemesPerPage, lens),
    "/themes/theme/{themeId}/new" bind GET to ShowNewMessageFormHandler(themes, WebForm(), lens, permissionLens),
    "/themes/theme/{themeId}/new" bind POST to CreateNewMessageHandler(users, themes, lens, sharedStateLens),
    "/themes/theme/{themeId}/edit" bind GET to
        ShowEditThemeFormHandler(themes::fetchThemeByNumber, lens, permissionLens, sharedStateLens),
    "/themes/theme/{themeId}/edit" bind POST to EditThemeHandler(users, themes, lens),
    "/themes/theme/{themeId}/delete" bind GET to
        ShowDeleteThemeFormHandler(users, themes::fetchThemeByNumber, lens, permissionLens, sharedStateLens),
    "/themes/theme/{themeId}/delete" bind POST to DeleteThemeHandler(users, themes, lens),
    "/themes/theme/{themeId}" bind GET to MessageListHandler(users, themes::fetchThemeByNumber, lens),
    "/themes/theme/{themeId}/message/{mesId}/newReaction" bind GET to
        ShowNewReactionFormHandler(themes, WebForm(), lens, permissionLens),
    "/themes/theme/{themeId}/message/{mesId}/newReaction" bind POST to
        CreateNewReactionHandler(users, themes, lens, sharedStateLens),
    "/themes/theme/{themeId}/message/{mesId}/delete" bind GET to
        ShowDeleteMessageFormHandler(users, themes, lens, permissionLens, sharedStateLens),
    "/themes/theme/{themeId}/message/{mesId}/delete" bind POST to DeleteMessageHandler(users, themes, lens),
    "/themes/theme/{themeId}/message/{mesId}/edit" bind GET to
        ShowEditMessageFormHandler(themes, lens, permissionLens, sharedStateLens),
    "/themes/theme/{themeId}/message/{mesId}/edit" bind POST to
        EditMessageHandler(users, themes, lens),
    "/themes/theme/{themeId}/message/{mesId}/deleteReaction/{reactNum}"
        bind GET to ShowDeleteReactionFormHandler(users, themes, lens, permissionLens, sharedStateLens),
    "/themes/theme/{themeId}/message/{mesId}/deleteReaction/{reactNum}"
        bind POST to DeleteReactionHandler(users, themes, lens),
    "/themes/theme/{themeId}/message/{mesId}" bind GET to ShowMessageHandler(users, themes, lens),
    static(ResourceLoader.Classpath("/ru/uniyar/public")),
)

fun main() {
    val portConfig = readWebPortFromConfiguration()
    val saltConfig = readAuthSaltFromConfiguration()
    val secretConfig = readJwtSecretFromConfiguration()
    if (portConfig == null || saltConfig == null || secretConfig == null) {
        println("!!!!!!\nProgram needs a specified webport/salt/secret in settings\n!!!!!!")
        exitProcess(0)
    }

    val themes = Themes(loadFromFileThemes())
    val users = Users(loadFromFileUsers())

    val renderer = ContextAwarePebbleTemplates().hotReload("src/main/resources")
    val htmlView = ContextAwareViewRender.withContentType(renderer, ContentType.TEXT_HTML)

    val jwtTools = JwtTools(secretConfig.secret, "ru.example")
    val contexts = RequestContexts()

    val sharedStateLens: RequestContextLens<SharedState?> = RequestContextKey.optional(contexts, "sharedState")
    val permissionLens: RequestContextLens<Permissions> = RequestContextKey.required(contexts, "permissions")

    val htmlViewWithContext =
        htmlView
            .associateContextLens("sharedState", sharedStateLens)
            .associateContextLens("permissions", permissionLens)

    val app =
        ServerFilters.InitialiseRequestContext(contexts)
            .then(addStateFilter(users, jwtTools, sharedStateLens))
            .then(permissionFilter(users, sharedStateLens, permissionLens))
            .then(errorFilter(htmlViewWithContext))
            .then(router(htmlViewWithContext, themes, users, jwtTools, permissionLens, sharedStateLens))

    val printingApp: HttpHandler = Filter.NoOp.then(app)
    val server = printingApp.asServer(Netty(portConfig.webPort)).start()

    println("Server started on http://localhost:" + server.port())

    val hook =
        thread(start = false) {
            saveToFileThemes(themes)
            saveToFileUsers(users)
        }
    Runtime.getRuntime().addShutdownHook(hook)
    println("Press enter to stop the application.")
    readln()
    server.stop()
}
