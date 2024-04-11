package ru.uniyar.authorization

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTCreationException
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import ru.uniyar.formLifeSpan

class JwtTools(private val secret: String, private val issuer: String) {
    private val algorithm: Algorithm = Algorithm.HMAC512(secret)
    private val verifier: JWTVerifier =
        JWT.require(algorithm)
            .withIssuer(issuer)
            .build()

    fun createJWT(userId: String): String? {
        val lifespan = formLifeSpan()
        return try {
            val token =
                JWT.create()
                    .withSubject(userId)
                    .withIssuer(issuer)
                    .withExpiresAt(lifespan)
                    .sign(algorithm)
            token
        } catch (exception: JWTCreationException) {
            null
        }
    }

    fun verifyJWT(token: String): String? {
        return try {
            val decodedJWT: DecodedJWT = verifier.verify(token)
            decodedJWT.subject
        } catch (exception: JWTVerificationException) {
            null
        }
    }
}
