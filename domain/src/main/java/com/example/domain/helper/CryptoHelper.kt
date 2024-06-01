package com.example.domain.helper

import java.security.MessageDigest

object CryptoHelper {
    private const val SECRET_KEY = 94345L

    fun encrypt(userId: Long): Long {
        return userId xor SECRET_KEY
    }

    fun decrypt(encryptedUserId: Long): Long {
        return encryptedUserId xor SECRET_KEY
    }

    fun hashPassword(password: String): String {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val bytes = messageDigest.digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
