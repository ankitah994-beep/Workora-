package com.example.ui.components

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.WorkoraNavy
import com.example.ui.theme.WorkoraOrange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun WorkoraHelmetLogo(
    size: Dp = 84.dp,
    showHalo: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val brandPrefs = remember { context.getSharedPreferences("workora_app_branding", Context.MODE_PRIVATE) }

    var customLogoBitmap by remember {
        val savedBase64 = brandPrefs.getString("logo_base64", "") ?: ""
        val bmp = if (savedBase64.isNotBlank()) {
            try {
                val bytes = Base64.decode(savedBase64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        } else null
        mutableStateOf<ImageBitmap?>(bmp)
    }

    // Live Sync with Firebase Admin Panel Branding
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val conn = (URL("https://workora-d8b51-default-rtdb.firebaseio.com/app_branding.json").openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 6000
                    readTimeout = 6000
                }
                if (conn.responseCode in 200..299) {
                    val text = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                    if (text.isNotBlank() && text != "null") {
                        val obj = JSONObject(text)
                        val cloudLogo = obj.optString("logoBase64", "")
                        val cloudName = obj.optString("appName", "WORKORA")
                        val cloudTagline = obj.optString("appTagline", "FIND. HIRE. WORK.")
                        val cloudHeading = obj.optString("welcomeHeading", "What do you want to do?")
                        val cloudBanner = obj.optString("bannerText", "")

                        brandPrefs.edit()
                            .putString("logo_base64", cloudLogo)
                            .putString("app_name", cloudName)
                            .putString("app_tagline", cloudTagline)
                            .putString("welcome_heading", cloudHeading)
                            .putString("banner_text", cloudBanner)
                            .apply()

                        if (cloudLogo.isNotBlank()) {
                            val bytes = Base64.decode(cloudLogo, Base64.DEFAULT)
                            val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                            withContext(Dispatchers.Main) {
                                customLogoBitmap = decoded
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                customLogoBitmap = null
                            }
                        }
                    }
                }
                conn.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Agar Admin Panel se naya Logo lagaya gaya hai, toh har screen par wahi dikhao
    if (customLogoBitmap != null) {
        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(Color.White)
                .border(2.dp, WorkoraOrange, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = customLogoBitmap!!,
                contentDescription = "Workora Custom Logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        return
    }

    // Default Workora Helmet Logo
    Box(
        modifier = modifier.size(if (showHalo) size * 1.25f else size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val center = Offset(w / 2f, h / 2f)

            if (showHalo) {
                drawCircle(
                    color = WorkoraOrange.copy(alpha = 0.12f),
                    radius = w * 0.48f,
                    center = center
                )
                drawCircle(
                    color = WorkoraOrange.copy(alpha = 0.22f),
                    radius = w * 0.38f,
                    center = center
                )
            }

            val domeRadius = if (showHalo) w * 0.28f else w * 0.40f
            val domeCenterY = h * 0.54f

            val domePath = Path().apply {
                moveTo(center.x - domeRadius, domeCenterY)
                cubicTo(
                    center.x - domeRadius, domeCenterY - domeRadius * 1.15f,
                    center.x + domeRadius, domeCenterY - domeRadius * 1.15f,
                    center.x + domeRadius, domeCenterY
                )
                close()
            }
            drawPath(path = domePath, color = WorkoraOrange)

            drawRoundRect(
                color = WorkoraNavy,
                topLeft = Offset(center.x - domeRadius * 0.22f, domeCenterY - domeRadius * 0.98f),
                size = Size(domeRadius * 0.44f, domeRadius * 0.55f),
                cornerRadius = CornerRadius(8f, 8f)
            )

            drawRoundRect(
                color = WorkoraOrange,
                topLeft = Offset(center.x - domeRadius * 1.18f, domeCenterY - domeRadius * 0.05f),
                size = Size(domeRadius * 2.36f, domeRadius * 0.24f),
                cornerRadius = CornerRadius(12f, 12f)
            )
        }
    }
}
