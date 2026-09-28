package com.example.ui.screens

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec

object WorkoraSecurityManager {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "WorkoraMasterHardwareKey_v2"
    private const val PREFS_SECRETS = "workora_secure_vault_v2"
    private const val PBKDF2_ITERATIONS = 65536
    private const val HASH_KEY_LENGTH = 256
    private const val SALT_LENGTH = 16

    init {
        ensureHardwareMasterKey()
    }

    private fun ensureHardwareMasterKey() {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
                val spec = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
                keyGen.init(spec)
                keyGen.generateKey()
            }
        } catch (_: Exception) {}
    }

    // Resolves 'Unresolved reference isStrongPassword' in LoginScreen.kt (Point 2)
    fun isStrongPassword(password: String): Boolean {
        return password.length >= 8 && password.any { it.isLetter() } && password.any { it.isDigit() }
    }

    // PBKDF2WithHmacSHA256 Cryptographic Password Hashing (Point 2)
    fun hashPasswordSecure(password: String): String {
        val random = SecureRandom()
        val salt = ByteArray(SALT_LENGTH)
        random.nextBytes(salt)

        val spec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, HASH_KEY_LENGTH)
        val skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = skf.generateSecret(spec).encoded

        val saltB64 = Base64.encodeToString(salt, Base64.NO_WRAP)
        val hashB64 = Base64.encodeToString(hash, Base64.NO_WRAP)
        return "pbkdf2:$saltB64:$hashB64"
    }

    fun verifyPasswordSecure(password: String, storedHashToken: String): Boolean {
        if (!storedHashToken.startsWith("pbkdf2:")) return false
        val parts = storedHashToken.split(":")
        if (parts.size != 3) return false

        val salt = Base64.decode(parts[1], Base64.DEFAULT)
        val expectedHash = Base64.decode(parts[2], Base64.DEFAULT)

        val spec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, HASH_KEY_LENGTH)
        val skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val testHash = skf.generateSecret(spec).encoded

        if (testHash.size != expectedHash.size) return false
        var diff = 0
        for (i in testHash.indices) {
            diff = diff or (testHash[i].toInt() xor expectedHash[i].toInt())
        }
        return diff == 0
    }

    // Hardware-Encrypted Secret Storage (Point 4)
    fun saveEncryptedSecret(context: Context, key: String, plainValue: String) {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            val secretKey = keyStore.getKey(KEY_ALIAS, null) as? SecretKey ?: return

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv
            val cipherBytes = cipher.doFinal(plainValue.toByteArray(Charsets.UTF_8))

            val combined = ByteArray(iv.size + cipherBytes.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(cipherBytes, 0, combined, iv.size, cipherBytes.size)

            val encB64 = Base64.encodeToString(combined, Base64.NO_WRAP)
            context.getSharedPreferences(PREFS_SECRETS, Context.MODE_PRIVATE)
                .edit()
                .putString(key, encB64)
                .apply()
        } catch (_: Exception) {}
    }

    fun readEncryptedSecret(context: Context, key: String, defaultVal: String = ""): String {
        return try {
            val encB64 = context.getSharedPreferences(PREFS_SECRETS, Context.MODE_PRIVATE)
                .getString(key, null) ?: return defaultVal
            val combined = Base64.decode(encB64, Base64.DEFAULT)
            if (combined.size < 12) return defaultVal

            val iv = ByteArray(12)
            System.arraycopy(combined, 0, iv, 0, 12)
            val cipherBytes = ByteArray(combined.size - 12)
            System.arraycopy(combined, 12, cipherBytes, 0, cipherBytes.size)

            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            val secretKey = keyStore.getKey(KEY_ALIAS, null) as? SecretKey ?: return defaultVal

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
            val plainBytes = cipher.doFinal(cipherBytes)
            String(plainBytes, Charsets.UTF_8)
        } catch (_: Exception) {
            defaultVal
        }
    }

    // Rate Limiting & Lockout Engine (Point 2 & 4)
    fun checkLoginBruteForceAllowed(context: Context, tag: String): Pair<Boolean, String?> {
        val prefs = context.getSharedPreferences("workora_ratelimit", Context.MODE_PRIVATE)
        val lockUntil = prefs.getLong("lock_until_$tag", 0L)
        val now = System.currentTimeMillis()
        if (now < lockUntil) {
            val remainMin = ((lockUntil - now) / 60000L) + 1
            return Pair(false, "Too many attempts! Locked out for $remainMin minutes.")
        }
        return Pair(true, null)
    }

    fun recordLoginAttempt(context: Context, tag: String, isSuccess: Boolean) {
        val prefs = context.getSharedPreferences("workora_ratelimit", Context.MODE_PRIVATE)
        if (isSuccess) {
            prefs.edit()
                .remove("fail_count_$tag")
                .remove("lock_until_$tag")
                .apply()
        } else {
            val count = prefs.getInt("fail_count_$tag", 0) + 1
            val editor = prefs.edit()
            editor.putInt("fail_count_$tag", count)
            if (count >= 5) {
                editor.putLong("lock_until_$tag", System.currentTimeMillis() + (15 * 60 * 1000L))
            }
            editor.apply()
        }
    }

    fun sanitizeUserInput(input: String, maxLen: Int = 500): String {
        return input.replace("<", "")
            .replace(">", "")
            .replace("{", "")
            .replace("}", "")
            .replace("\"", "'")
            .take(maxLen)
            .trim()
    }

    fun recordAdminAuditLog(context: Context, adminEmail: String, action: String, target: String) {
        FirebaseManager.logAdminAudit(adminEmail, action, target)
    }
}
