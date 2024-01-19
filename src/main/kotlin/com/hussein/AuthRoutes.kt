package com.hussein

import com.hussein.data.models.User
import com.hussein.data.models.UserDataSource
import com.hussein.data.requests.AuthRequest
import com.hussein.data.responses.AuthResponse
import com.hussein.security.salt.HashingService
import com.hussein.security.salt.SaltedHash
import com.hussein.security.token.TokenClaim
import com.hussein.security.token.TokenConfig
import com.hussein.security.token.TokenService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receiveNullable
import io.ktor.server.request.receiveOrNull
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import org.apache.commons.codec.digest.DigestUtils

fun Route.signUp(
    hashingService: HashingService,
    userDataSource: UserDataSource
){
    post("signup") {
        val request = call.receiveNullable<AuthRequest>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }
        val areFieldBlank = request.username.isBlank() || request.password.isBlank()
        val isPasswordTooShort = request.password.length < 8
        if(areFieldBlank || isPasswordTooShort){
            call.respond(HttpStatusCode.Conflict)
            return@post
        }
        val saltedHash = hashingService.generateSaltedHash(request.password)
        val user = User(
            username = request.username,
            password = saltedHash.hash,
            salt = saltedHash.salt,
        )
        val wasAcknowledge = userDataSource.insertUser(user)
        if(!wasAcknowledge){
            call.respond(HttpStatusCode.Conflict)
            return@post
        }

        //Success
        call.respond(HttpStatusCode.OK)
    }
}

fun Route.singIn(
    userDataSource: UserDataSource,
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig
){
    post("signin") {
        val request = call.receiveOrNull<AuthRequest>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val user = userDataSource.getUserByName(request.username)
        if(user == null){
            call.respond(HttpStatusCode.Conflict,"Incorrect username or password1")
            return@post
        }

        val isValidPassword = hashingService.verify(
            value = request.password,
            saltedHash = SaltedHash(
                hash = user.password,
                salt = user.salt
            )
        )
        if(!isValidPassword) {
            println("Entered hash: ${DigestUtils.sha256Hex("${user.salt}${request.password}")}, Hashed PW: ${user.password}")
            call.respond(HttpStatusCode.Conflict, "Incorrect username or password2")
            return@post
        }

        val token = tokenService.generate(
            config = tokenConfig,
            TokenClaim(
                name = "userId",
                value = user.id.toString()
            )
        )

        call.respond(HttpStatusCode.OK,
            message = AuthResponse(
                token = token
            )
        )

    }

}

fun Route.authenticate(){
    authenticate {
        get("authenticate") {
            call.respond(HttpStatusCode.OK)
        }
    }
}

fun Route.getSecretInfo(){
    authenticate {
        get("secret") {
            val principle = call.principal<JWTPrincipal>()
            val userId = principle?.getClaim("userId", String::class)
            call.respond(HttpStatusCode.OK , "Your userId is $userId")
            /*same as :TokenClaim(
            name = "userId",
            value = user.id.toString()
            )*/
        }
    }
}

