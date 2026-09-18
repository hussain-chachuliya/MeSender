package com.mesender.app.domain.util

import java.security.MessageDigest

/**
 * Salted PBKDF2 (via SHA-256) of the PIN, stored as `salt:hash`.
 * Not production-grade crypto but sufficient for a local lock indicator;
 * no secret ever leaves the device.
 */
object PinHasher {
    private const val ITERATIONS = 100_000

    fun hash(pin: String): String {
        val salt = ByteArray(16).also { java.security.SecureRandom().nextBytes(it) }
        val digest = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            .generateSecret(javax.crypto.spec.PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, 256))
            .encoded
        return "${salt.toBase64Local()}:${digest.toBase64Local()}"
    }

    fun verify(pin: String, stored: String): Boolean {
        val parts = stored.split(":")
        if (parts.size != 2) return false
        val salt = parts[0].fromBase64Local()
        val expected = parts[1].fromBase64Local()
        val actual = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            .generateSecret(javax.crypto.spec.PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, 256))
            .encoded
        return MessageDigest.isEqual(expected, actual)
    }

    private fun ByteArray.toBase64Local(): String =
        java.util.Base64.getEncoder().encodeToString(this)

    private fun String.fromBase64Local(): ByteArray =
        java.util.Base64.getDecoder().decode(this)
}