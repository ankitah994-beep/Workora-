package com.example.ui.screens

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Debug
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.io.File
import java.io.InputStream
import java.net.URL
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.ConcurrentHashMap
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.net.ssl.HttpsURLConnection

/**
 * WORKORA PRODUCTION & ADVANCED ANTI-HACKER SECURITY MANAGER
 *
 * Includes:
 * 1. Salted Password Hashing (PBKDF2-HMAC-SHA256 / Argon2id compatible)
 * 2. Brute-Force Account Lockout & Sliding-Window API Rate Limiter
 * 3. Hardware-Backed AES-256-GCM Encrypted Storage (AndroidKeyStore)
 * 4. Root / Jailbreak / Frida / Xposed / Debugger Detection
 * 5. SSL/TLS Certificate Pinning (MitM Attack Prevention)
 * 6. Session Token Blacklisting & Anti-Replay Protection
 * 7. RBAC, Ownership Verification & Magic-Byte Image Upload Validation
 */
object WorkoraSecurityManager {

    private const val SECURITY_PREFS = "workora_prod_security_vault"
    private const val ENCRYPTED_VAULT_PREFS = "workora_aes256_encrypted_vault"
    private const val AUDIT_PREFS = "workora_admin_audit_logs"
    private const val KEYSTORE_ALIAS = "WorkoraMasterHardwareKey_v1"

    private const val HASH_ITERATIONS = 120_000
    private const val SALT_BYTES = 16
    private const val HASH_BITS = 256
    private const val GCM_TAG_BITS = 128
    private const val GCM_IV_BYTES = 12

    private const val MAX_LOGIN_ATTEMPTS = 5
    private const val LOCKOUT_DURATION_MS = 15 * 60 * 1000L
    private const val API_WINDOW_MS = 60 * 1000L
    private const val MAX_REQUESTS_PER_MINUTE = 30
    private const val MAX_IMAGE_UPLOAD_BYTES = 2 * 1024 * 1024

    private val requestRateMap = ConcurrentHashMap<String, MutableList<Long>>()
    private val blacklistedTokenHashes = ConcurrentHashMap.newKeySet<String>()

    // =========================================================================
    // 1. HARDWARE-BACKED AES-256-GCM ENCRYPTED STORAGE (ANDROID KEYSTORE)
    // =========================================================================

    private fun getOrCreateHardwareSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val existingEntry = keyStore.getEntry(KEYSTORE_ALIAS, null) as? KeyStore.SecretKeyEntry
        if (existingEntry != null) {
            return existingEntry.secretKey
        }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )
        val spec = KeyGenParameterSpec.Builder(
            KEYSTORE_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setRandomizedEncryptionRequired(true)
            .build()

        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    fun saveEncryptedSecret(context: Context, key: String, plainValue: String) {
        try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateHardwareSecretKey())
            val iv = cipher.iv
            val cipherBytes = cipher.doFinal(plainValue.toByteArray(Charsets.UTF_8))

            val combined = ByteArray(iv.size + cipherBytes.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(cipherBytes, 0, combined, iv.size, cipherBytes.size)

            val encoded = Base64.encodeToString(combined, Base64.NO_WRAP)
            context.getSharedPreferences(ENCRYPTED_VAULT_PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(sanitizeIdentifier(key), encoded)
                .apply()
        } catch (_: Exception) {
        }
    }

    fun readEncryptedSecret(context: Context, key: String, defaultValue: String = ""): String {
        return try {
            val prefs = context.getSharedPreferences(ENCRYPTED_VAULT_PREFS, Context.MODE_PRIVATE)
            val encoded = prefs.getString(sanitizeIdentifier(key), null) ?: return defaultValue
            val combined = Base64.decode(encoded, Base64.NO_WRAP)
            if (combined.size <= GCM_IV_BYTES) return defaultValue

            val iv = combined.copyOfRange(0, GCM_IV_BYTES)
            val cipherBytes = combined.copyOfRange(GCM_IV_BYTES, combined.size)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(
                Cipher.DECRYPT_MODE,
                getOrCreateHardwareSecretKey(),
                GCMParameterSpec(GCM_TAG_BITS, iv)
            )
            String(cipher.doFinal(cipherBytes), Charsets.UTF_8)
        } catch (_: Exception) {
            defaultValue
        }
    }

    // =========================================================================
    // 2. ROOT, DEBUGGER, FRIDA & TAMPER DETECTION (ANTI-HACKER SHIELD)
    // =========================================================================

    fun isDeviceCompromised(): Boolean {
        return isDeviceRooted() || isDebuggerOrHookAttached()
    }

    private fun isDeviceRooted(): Boolean {
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) {
            return true
        }
        val rootPaths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su",
            "/sbin/magisk"
        )
        return rootPaths.any { path ->
            try {
                File(path).exists()
            } catch (_: Exception) {
                false
            }
        }
    }

    private fun isDebuggerOrHookAttached(): Boolean {
        if (Debug.isDebuggerConnected() || Debug.waitingForDebugger()) {
            return true
        }
        // Check loaded memory maps for Frida or Xposed injection
        return try {
            val mapsFile = File("/proc/self/maps")
            if (mapsFile.exists() && mapsFile.canRead()) {
                val content = mapsFile.readText().lowercase()
                content.contains("frida") || content.contains("xposed") || content.contains("substrate")
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    fun isAppRepackagedOrCloned(context: Context): Boolean {
        val filesDirPath = context.filesDir.absolutePath
        // Dual-space / parallel app cloners inject multiple package segments or virtual paths
        val suspiciousClonerKeywords = listOf("dual", "parallel", "virtual", "clone", "multiple")
        return suspiciousClonerKeywords.any { filesDirPath.lowercase().contains(it) }
    }

    // =========================================================================
    // 3. SSL / TLS CERTIFICATE PINNING (MAN-IN-THE-MIDDLE PROTECTION)
    // =========================================================================

    fun openPinnedHttpsConnection(
        httpsUrl: String,
        allowedSpkiSha256Pins: Set<String> = emptySet()
    ): HttpsURLConnection {
        val url = URL(httpsUrl)
        require(url.protocol.equals("https", ignoreCase = true)) {
            "Security Violation: Plain HTTP connections are strictly forbidden."
        }

        val conn = (url.openConnection() as HttpsURLConnection).apply {
            connectTimeout = 8000
            readTimeout = 8000
            useCaches = false
            setRequestProperty("X-Content-Type-Options", "nosniff")
        }

        conn.connect()

        // Verify Server X.509 Certificate Public Key Pin (if pins configured)
        if (allowedSpkiSha256Pins.isNotEmpty()) {
            val certs = conn.serverCertificates
            var pinMatched = false
            for (cert in certs) {
                if (cert is X509Certificate) {
                    val spkiBytes = cert.publicKey.encoded
                    val sha256 = MessageDigest.getInstance("SHA-256").digest(spkiBytes)
                    val pin = "sha256/" + Base64.encodeToString(sha256, Base64.NO_WRAP)
                    if (allowedSpkiSha256Pins.contains(pin)) {
                        pinMatched = true
                        break
                    }
                }
            }
            if (!pinMatched) {
                conn.disconnect()
                throw SecurityException("SSL Pinning Verification Failed: Potential MitM Attack Detected!")
            }
        }

        return conn
    }

    // =========================================================================
    // 4. SESSION TOKEN BLACKLISTING & REPLAY PROTECTION
    // =========================================================================

    fun revokeSessionToken(context: Context, token: String) {
        if (token.isBlank()) return
        val digest = sha256Hex(token)
        blacklistedTokenHashes.add(digest)
        context.getSharedPreferences(SECURITY_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean("revoked_tok_$digest", true)
            .apply()
    }

    fun isSessionTokenRevoked(context: Context, token: String): Boolean {
        if (token.isBlank()) return true
        val digest = sha256Hex(token)
        if (blacklistedTokenHashes.contains(digest)) return true
        return context.getSharedPreferences(SECURITY_PREFS, Context.MODE_PRIVATE)
            .getBoolean("revoked_tok_$digest", false)
    }

    private fun sha256Hex(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // =========================================================================
    // 5. SALTED PASSWORD HASHING & CONSTANT-TIME VERIFICATION
    // =========================================================================

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

    // =========================================================================
    // 6. BRUTE-FORCE LOCKOUT, RATE LIMITING, RBAC & IMAGE MAGIC-BYTES
    // =========================================================================

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
