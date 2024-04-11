package ru.uniyar.config

import org.http4k.cloudnative.env.Environment
import org.http4k.cloudnative.env.EnvironmentKey
import org.http4k.lens.nonBlankString
import ru.uniyar.web.handlers.lensOrNull

class SecretConfig(
    val secret: String,
) {
    companion object {
        val secretLens = EnvironmentKey.nonBlankString().required("jwt.secret")

        fun create(environment: Environment): SecretConfig? {
            val secret = lensOrNull(secretLens, environment) ?: return null
            return SecretConfig(secret)
        }
    }
}
