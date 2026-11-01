package com.cs360.weighttracker.utils

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object PasswordHasher {
    /**
     * Hashes a password using SHA-256 from plain text.
     * 
     * @param plainText The plain text string to hash.
     * @return The hashed string.
     */
    @JvmStatic
    fun hash(plainText: String): String {
        val hashString = StringBuilder()
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            // Decode the string to hashed byte array
            val bytes = digest.digest(plainText.toByteArray(StandardCharsets.UTF_8))

            // Iterate through each byte, and convert it to a hex string
            for (b in bytes) {
                val hex = Integer.toHexString(0xff and b.toInt())
                if (hex.length == 1) hashString.append("0")
                hashString.append(hex)
            }
        } catch (e: NoSuchAlgorithmException) {
            // Log not used here to keep the module structurally independent of android
            System.err.println("Invalid algorithm selected for hashing.")
        }

        return hashString.toString()
    }
}
