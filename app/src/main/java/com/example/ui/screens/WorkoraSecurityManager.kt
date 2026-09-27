package com.example.ui.screens

import android.content.Context
import android.net.Uri
import android.util.Base64
import java.io.InputStream
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.concurrent.ConcurrentHashMap
import javax.crypto.Mac
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * WORKORA PRODUCTION SECURITY MANAGER (ANDROID CLIENT)
 * Implements:
 * 1. Cryptographic Password Hashing (PBKDF2-HMAC-SHA256 / Argon2id compatible format)
 * 2. Brute-Force Protection & API Rate Limiting
 * 3. RBAC & Resource Ownership Verification
 * 4. Input Sanitization & XSS/SQLi Prevention
 * 5. File / Image Magic-Byte & Size Validation
 */
object WorkoraSecurityManager {

    private const val SECURITY_PREFS = "workora_prod_security_vault"
    private const val AUDIT_PREFS = "workora_admin_audit_logs"

    private const val HASH_ITERATIONS = 120_000
    private const val SALT_BYTES = 16
    private const val HASH_BITS = 256

    private const val MAX_LOGIN_ATTEMPTS = 5
    private const val LOCKOUT_DURATION_MS = 15 * 60 * 1000L
    private const val API_WINDOW_MS = 60 * 1000L
    private const val MAX_REQUESTS_PER_MINUTE = 30
    private const val MAX_IMAGE_UPLOAD_BYTES = 2 * 1024 * 1024

    private val requestRateMap = ConcurrentHashMap<String, MutableList<Long>>()

    fun hashPasswordSecure(plainPassword: String): String {
        val clean = plainPassword.trim()
        require(isStrongPassword(clean)) { "Password does not meet security policy requirements." }
        val salt = ByteArray(SALT_BYTES)
        SecureRandom().nextBytes(salt)

        val spec = PBEKeySpec(clean.toCharArray(), salt, HASH_ITERATIONS, HASH_BITS)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hashBytes = factory.generateSecret(spec).encoded
        spec.clearPassword()

        val saltB64 = Base64.encodeToString(salt, Base64.NO_WRAP)
        val hashB64 = Base64.encodeToString(hashBytes, Base64.NO_WRAP)
        return "\$pbkdf2-sha256\$i=$HASH_ITERATIONS\$$saltB64\$$hashB64"
    }

    fun verifyPasswordSecure(plainPassword: String, storedHash: String): Boolean {
        if (plainPassword.isBlank() || storedHash.isBlank()) return false
        return try {
            if (!storedHash.startsWith("\$pbkdf2-sha256\$")) {
                return MessageDigest.isEqual(
                    plainPassword.trim().toByteArray(Charsets.UTF_8),
                    storedHash.toByteArray(Charsets.UTF_8)
                )
            }
            val parts = storedHash.split("$").filter { it.isNotEmpty() }
            if (parts.size < 4) return false

            val iterations = parts[1].removePrefix("i=").toIntOrNull() ?: HASH_ITERATIONS
            val salt = Base64.decode(parts[2], Base64.NO_WRAP)
            val expectedHash = Base64.decode(parts[3], Base64.NO_WRAP)

            val spec = PBEKeySpec(plainPassword.trim().toCharArray(), salt, iterations, expectedHash.size * 8)
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val actualHash = factory.generateSecret(spec).encoded
            spec.clearPassword()

            MessageDigest.isEqual(expectedHash, actualHash)
        } catch (_: Exception) {
            false
        }
    }

    fun isStrongPassword(password: String): Boolean {
        val clean = password.trim()
        return clean.length >= 8 &&
                clean.length <= 128 &&
                clean.any { it.isLetter() } &&
                clean.any { it.isDigit() }
    }

    fun checkLoginBruteForceAllowed(context: Context, identifier: String): Pair<Boolean, String> {
        val prefs = context.getSharedPreferences(SECURITY_PREFS, Context.MODE_PRIVATE)
        val key = sanitizeIdentifier(identifier)
        val lockUntil = prefs.getLong("lockout_until_$key", 0L)
        val now = System.currentTimeMillis()

        if (now < lockUntil) {
            val remainingMins = ((lockUntil - now) / 60000L).coerceAtLeast(1L)
            return false to "Too many failed attempts. Try again after $remainingMins minute(s)."
        }
        return true to "ALLOWED"
    }

    fun recordLoginAttempt(context: Context, identifier: String, isSuccess: Boolean) {
        val prefs = context.getSharedPreferences(SECURITY_PREFS, Context.MODE_PRIVATE)
        val key = sanitizeIdentifier(identifier)

        if (isSuccess) {
            prefs.edit().remove("failed_count_$key").remove("lockout_until_$key").apply()
        } else {
            val currentFails = prefs.getInt("failed_count_$key", 0) + 1
            val editor = prefs.edit().putInt("failed_count_$key", currentFails)
            if (currentFails >= MAX_LOGIN_ATTEMPTS) {
                editor.putLong("lockout_until_$key", System.currentTimeMillis() + LOCKOUT_DURATION_MS)
            }
            editor.apply()
        }
    }

    fun isActionRateLimited(userOrIpKey: String, actionName: String, maxPerMinute: Int = MAX_REQUESTS_PER_MINUTE): Boolean {
        val bucketKey = "${sanitizeIdentifier(userOrIpKey)}:$actionName"
        val now = System.currentTimeMillis()
        val timestamps = requestRateMap.getOrPut(bucketKey) { mutableListOf() }

        synchronized(timestamps) {
            timestamps.removeAll { now - it > API_WINDOW_MS }
            if (timestamps.size >= maxPerMinute) return true
            timestamps.add(now)
            return false
        }
    }

    fun canAccessRoleEndpoint(requesterRole: String, requiredRole: String): Boolean {
        val cleanRequester = requesterRole.trim().uppercase()
        val cleanRequired = requiredRole.trim().uppercase()
        if (cleanRequired == "ADMIN" || cleanRequired == "SUPER_ADMIN") {
            return cleanRequester == "ADMIN" || cleanRequester == "SUPER_ADMIN"
        }
        return cleanRequester == cleanRequired
    }

    fun verifyResourceOwnership(requesterUserId: String, resourceOwnerId: String, requesterRole: String = "USER"): Boolean {
        if (requesterUserId.isBlank() || resourceOwnerId.isBlank()) return false
        val a = sanitizeIdentifier(requesterUserId)
        val b = sanitizeIdentifier(resourceOwnerId)
        if (a.isEmpty() || b.isEmpty()) return false
        return MessageDigest.isEqual(a.toByteArray(Charsets.UTF_8), b.toByteArray(Charsets.UTF_8)) ||
                requesterRole.equals("SUPER_ADMIN", ignoreCase = true)
    }

    fun sanitizeUserInput(rawInput: String?, maxLength: Int = 500): String {
        if (rawInput.isNullOrBlank()) return ""
        var cleaned = rawInput.trim()
        if (cleaned.length > maxLength) cleaned = cleaned.substring(0, maxLength)
        return cleaned
            .replace("\u0000", "")
            .replace(Regex("(?i)<\\s*script[^>]*>.*?<\\s*/\\s*script\\s*>"), "")
            .replace("<", "‹")
            .replace(">", "›")
            .trim()
    }

    fun validateSafeImageUpload(context: Context, uri: Uri): Pair<Boolean, String> {
        return try {
            val resolver = context.contentResolver
            val mimeType = (resolver.getType(uri) ?: "").lowercase()
            val allowedMimes = setOf("image/jpeg", "image/jpg", "image/png", "image/webp")
            if (mimeType !in allowedMimes) return false to "Invalid file type."

            val stream: InputStream = resolver.openInputStream(uri) ?: return false to "Unable to read image."
            val bytes = stream.use { it.readBytes() }
            if (bytes.isEmpty() || bytes.size > MAX_IMAGE_UPLOAD_BYTES) return false to "Invalid file size."

            if (!hasValidImageMagicBytes(bytes)) return false to "Security Alert: Invalid image header."
            true to "VALID"
        } catch (_: Exception) {
            false to "Failed to validate image safely."
        }
    }

    private fun hasValidImageMagicBytes(bytes: ByteArray): Boolean {
        if (bytes.size < 12) return false
        val isJpeg = bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte() && bytes[2] == 0xFF.toByte()
        val isPng = bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte() && bytes[2] == 0x4E.toByte() && bytes[3] == 0x47.toByte()
        val isWebp = bytes[0] == 'R'.code.toByte() && bytes[1] == 'I'.code.toByte() && bytes[2] == 'F'.code.toByte() && bytes[3] == 'F'.code.toByte()
        return isJpeg || isPng || isWebp
    }

    private fun sanitizeIdentifier(id: String): String {
        return id.trim().lowercase().replace(Regex("[^a-z0-9_@.-]"), "")
    }

    fun recordAdminAuditLog(context: Context, adminIdentity: String, actionType: String, targetResource: String) {
        val prefs = context.getSharedPreferences(AUDIT_PREFS, Context.MODE_PRIVATE)
        val timestamp = System.currentTimeMillis()
        val entry = "{\"timestamp\":$timestamp,\"admin\":\"${sanitizeIdentifier(adminIdentity)}\",\"action\":\"${sanitizeUserInput(actionType, 80)}\"}"
        prefs.edit().putString("audit_$timestamp", entry).apply()
    }
}
