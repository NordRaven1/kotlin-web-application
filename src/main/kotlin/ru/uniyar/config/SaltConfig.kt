package ru.uniyar.config

import org.http4k.cloudnative.env.Environment
import org.http4k.cloudnative.env.EnvironmentKey
import org.http4k.lens.nonBlankString
import ru.uniyar.web.handlers.lensOrNull

class SaltConfig(
    val salt: String,
) {
    companion object {
        private val saltLens = EnvironmentKey.nonBlankString().required("auth.salt")

        fun create(environment: Environment): SaltConfig? {
            val salt = lensOrNull(saltLens, environment) ?: return null
            return SaltConfig(salt)
        }
    }
}
