package ru.uniyar.config

import org.http4k.cloudnative.env.Environment
import org.http4k.cloudnative.env.EnvironmentKey
import org.http4k.lens.int
import ru.uniyar.web.handlers.lensOrNull

data class PortConfig(
    val webPort: Int,
) {
    companion object {
        private val webPortLens = EnvironmentKey.int().required("web.port", "Application web port")

        fun create(environment: Environment): PortConfig? {
            val port = lensOrNull(webPortLens, environment) ?: return null
            return PortConfig(port)
        }
    }
}
