package com.hussein.plugins

import com.hussein.authenticate
import com.hussein.data.models.UserDataSource
import com.hussein.getSecretInfo
import com.hussein.security.salt.HashingService
import com.hussein.security.token.TokenConfig
import com.hussein.security.token.TokenService
import com.hussein.signUp
import com.hussein.singIn
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    userDataSource: UserDataSource,
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig
) {
    routing {
        singIn(userDataSource, hashingService, tokenService, tokenConfig)
        signUp(hashingService,userDataSource)
        authenticate()
        getSecretInfo()
       /* get("/") {
            call.respondText("Hello World!")
        }*/
    }
}
