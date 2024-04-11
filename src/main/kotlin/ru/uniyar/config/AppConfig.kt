package ru.uniyar.config

import org.http4k.cloudnative.env.Environment

val appEnv =
    Environment.JVM_PROPERTIES overrides
        Environment.ENV

fun readWebPortFromConfiguration(): PortConfig? {
    return PortConfig.create(appEnv)
}

fun readAuthSaltFromConfiguration(): SaltConfig? {
    return SaltConfig.create(appEnv)
}

fun readJwtSecretFromConfiguration(): SecretConfig? {
    return SecretConfig.create(appEnv)
}
