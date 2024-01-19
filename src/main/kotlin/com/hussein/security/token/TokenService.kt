package com.hussein.security.token

interface TokenService {
    fun generate(
        config: TokenConfig,
        vararg tokenClaim: TokenClaim
    ):String
}