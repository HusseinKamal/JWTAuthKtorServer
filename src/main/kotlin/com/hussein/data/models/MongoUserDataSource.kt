package com.hussein.data.models

import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

class MongoUserDataSource(
    db : CoroutineDatabase
) : UserDataSource {

    val users = db.getCollection<User>()
    override suspend fun getUserByName(username: String): User? {
        return users.findOne(User::username eq username)
    }

    override suspend fun insertUser(user: User): Boolean {
        return users.insertOne(user).wasAcknowledged()
    }
}