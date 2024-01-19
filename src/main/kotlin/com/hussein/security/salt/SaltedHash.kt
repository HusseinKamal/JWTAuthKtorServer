package com.hussein.security.salt

data class SaltedHash(
    val hash :String,
    val salt: String
)
