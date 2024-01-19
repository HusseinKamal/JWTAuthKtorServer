package com.hussein.data.models

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

interface UserDataSource {
    suspend fun getUserByName(username:String) : User?
    suspend fun insertUser(user: User):Boolean

}
