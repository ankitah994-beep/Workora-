package com.example.ui.screens

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object FirebaseManager {

    // Aapka asli Firebase Realtime Database URL
    private const val FIREBASE_DB_URL = "https://workora-d8b51-default-rtdb.firebaseio.com"

    private fun toSafeEmailKey(email: String): String {
        return email.trim().lowercase().replace(".", "_").replace("@", "_at_")
    }

    fun syncUserToFirebase(
        context: Context,
        name: String,
        email: String,
        phone: String,
        password: String,
        role: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val safeEmailKey = toSafeEmailKey(email)
                val url = URL("$FIREBASE_DB_URL/users/$safeEmailKey.json")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "PUT"
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                    connectTimeout = 8000
                    readTimeout = 8000
                }

                val json = JSONObject().apply {
                    put("fullName", name)
                    put("email", email)
                    put("phone", phone)
                    put("password", password)
                    put("role", role)
                    put("updatedAt", System.currentTimeMillis())
                }

                OutputStreamWriter(conn.outputStream).use { writer ->
                    writer.write(json.toString())
                    writer.flush()
                }

                val responseCode = conn.responseCode
                if (responseCode in 200..299) {
                    context.getSharedPreferences("workora_firebase", Context.MODE_PRIVATE)
                        .edit()
                        .putBoolean("cloud_synced", true)
                        .apply()
                }
                conn.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun verifyUserFromFirebase(
        email: String,
        passwordInput: String,
        onResult: (isSuccess: Boolean, name: String?, phone: String?, errorMsg: String?) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val safeEmailKey = toSafeEmailKey(email)
                val url = URL("$FIREBASE_DB_URL/users/$safeEmailKey.json")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 8000
                    readTimeout = 8000
                }

                val responseCode = conn.responseCode
                if (responseCode in 200..299) {
                    val responseText = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                    conn.disconnect()

                    if (responseText.isBlank() || responseText == "null") {
                        withContext(Dispatchers.Main) {
                            onResult(false, null, null, "Account nahi mila! Kripya pehle Sign Up karein.")
                        }
                        return@launch
                    }

                    val json = JSONObject(responseText)
                    val savedPass = json.optString("password", "")
                    val savedName = json.optString("fullName", "Workora User")
                    val savedPhone = json.optString("phone", "")

                    withContext(Dispatchers.Main) {
                        if (savedPass == passwordInput) {
                            onResult(true, savedName, savedPhone, null)
                        } else {
                            onResult(false, null, null, "Galat Password! Kripya sahi password dalein.")
                        }
                    }
                } else {
                    conn.disconnect()
                    withContext(Dispatchers.Main) {
                        onResult(false, null, null, null)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(false, null, null, null)
                }
            }
        }
    }
}
