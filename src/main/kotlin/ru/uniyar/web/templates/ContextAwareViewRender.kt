package ru.uniyar.web.templates

import org.http4k.core.Body
import org.http4k.core.ContentType
import org.http4k.core.Request
import org.http4k.lens.BiDiBodyLens
import org.http4k.lens.BiDiBodyLensSpec
import org.http4k.lens.RequestContextLens
import org.http4k.lens.string
import org.http4k.template.ViewModel

data class ContextAwareViewRender(
    private val templateRenderer: ContextAwareTemplateRenderer,
    private val baseBodyLensSpec: BiDiBodyLensSpec<String>,
    private val contextLenses: Map<String, RequestContextLens<*>> = emptyMap(),
) {
    companion object {
        fun withContentType(
            templateRenderer: ContextAwareTemplateRenderer,
            contentType: ContentType,
        ): ContextAwareViewRender =
            ContextAwareViewRender(
                templateRenderer,
                Body.string(contentType),
            )
    }

    fun associateContextLens(
        key: String,
        lens: RequestContextLens<*>,
    ): ContextAwareViewRender {
        val newContextLenses = contextLenses + mapOf(key to lens)
        return this.copy(contextLenses = newContextLenses)
    }

    fun associateContextLenses(lenses: Map<String, RequestContextLens<*>>): ContextAwareViewRender {
        val newContextLenses = contextLenses + lenses
        return this.copy(contextLenses = newContextLenses)
    }

    operator fun invoke(request: Request): BiDiBodyLens<ViewModel> {
        return baseBodyLensSpec.map<ViewModel>(
            {
                throw UnsupportedOperationException("Cannot parse a ViewModel")
            },
            { viewModel: ViewModel ->
                templateRenderer(extractContext(request), viewModel)
            },
        ).toLens()
    }

    private fun extractContext(request: Request): Map<String, Any?> =
        contextLenses.mapValues {
            it.value(request)
        }
}
