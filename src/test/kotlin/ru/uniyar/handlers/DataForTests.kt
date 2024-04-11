package ru.uniyar.handlers

import org.http4k.core.ContentType
import org.http4k.core.RequestContexts
import org.http4k.lens.RequestContextKey
import org.http4k.lens.RequestContextLens
import ru.uniyar.authorization.Permissions
import ru.uniyar.authorization.SharedState
import ru.uniyar.web.templates.ContextAwarePebbleTemplates
import ru.uniyar.web.templates.ContextAwareViewRender

val renderer = ContextAwarePebbleTemplates().hotReload("src/main/resources")
val htmlV = ContextAwareViewRender.withContentType(renderer, ContentType.TEXT_HTML)
val contexts = RequestContexts()
val sharedStateLens: RequestContextLens<SharedState?> = RequestContextKey.optional(contexts, "sharedState")
val permissionLens: RequestContextLens<Permissions> = RequestContextKey.required(contexts, "permissions")
val htmlView =
    htmlV
        .associateContextLens("sharedState", sharedStateLens)
        .associateContextLens("permissions", permissionLens)
