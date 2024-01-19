package com.hussein

import com.hussein.data.models.MongoUserDataSource
import com.hussein.data.models.User
import com.hussein.plugins.*
import com.hussein.security.salt.SHA256HashingService
import com.hussein.security.token.JwtTokenService
import com.hussein.security.token.TokenConfig
import io.ktor.server.application.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.litote.kmongo.KMongo
import org.litote.kmongo.coroutine.coroutine

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    //husseinkamal —> username mongoDB
    //ktor-server-app —> password mongoDB
    //Databae name : ktor-auth

    //Check Edit Configuration menu : MONGO_PW=ktor-server-app
    val mongoPassword = System.getenv("MONGO_PW") // add user password  in MongoDB

    val dbName = "ktor-auth" // Choose any name you need
    val db =org.litote.kmongo.reactivestreams.KMongo.createClient(
        connectionString = "mongodb+srv://husseinkamal:$mongoPassword@cluster0.e9whvcz.mongodb.net/$dbName?retryWrites=true&w=majority"
    ).coroutine.getDatabase(dbName)


   val userDataSource = MongoUserDataSource(db)
    //Create user for testing and go to MongoDB to check if added or not in DataBase->Collections
    /* GlobalScope.launch {
        val user = User(
            username = "test",
            password = "test-password",
            salt = "salt"
        )
        userDataSource.insertUser(user)
    }*/
    val tokenService  = JwtTokenService()
    val tokenConfig = TokenConfig(
        issuer = environment.config.property("jwt.issuer").getString(),
        audience = environment.config.property("jwt.audience").getString(),
        expiresIn = 356L  * 1000L * 60L * 60L *24L,//For one year token still alive
        secret = System.getenv("JWT_SECRET") //Check Edit Configuration menu : JWT_SECRET=jwt-secret
    )
    val hashingService = SHA256HashingService()

    configureSerialization()
    configureMonitoring()
    configureSecurity(tokenConfig)
    configureRouting(userDataSource,hashingService,tokenService, tokenConfig)
}
