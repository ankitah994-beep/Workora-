package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.location.LocationManager
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

private const val PROFILE_FIREBASE_URL = "https://workora-d8b51-default-rtdb.firebaseio.com"

object WorkoraThemeManager {
    private const val SETTINGS_PREFS = "workora_app_settings"
    private const val KEY_THEME_MODE = "app_theme_mode"

    var currentMode by mutableStateOf("System")
        private set

    private var isInitialized = false

    fun syncFromPrefs(context: Context): String {
        val prefs = context.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_THEME_MODE, "System") ?: "System"
        val validMode = if (saved in listOf("System", "Light", "Dark")) saved else "System"
        if (!isInitialized || currentMode != validMode) {
            currentMode = validMode
            isInitialized = true
        }
        return currentMode
    }

    fun setThemeMode(context: Context, mode: String) {
        val validMode = if (mode in listOf("System", "Light", "Dark")) mode else "System"
        currentMode = validMode
        isInitialized = true
        context.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME_MODE, validMode)
            .apply()
    }

    fun cycleNextThemeMode(context: Context): String {
        val next = when (currentMode) {
            "System" -> "Light"
            "Light" -> "Dark"
            else -> "System"
        }
        setThemeMode(context, next)
        return next
    }

    @Composable
    fun isDark(context: Context? = null): Boolean {
        if (context != null) {
            syncFromPrefs(context)
        }
        val sysDark = isSystemInDarkTheme()
        return when (currentMode) {
            "Dark" -> true
            "Light" -> false
            else -> sysDark
        }
    }

    @Composable
    fun bgColor(context: Context? = null): Color =
        if (isDark(context)) Color(0xFF0B1120) else Color(0xFFF8FAFC)

    @Composable
    fun surfaceColor(context: Context? = null): Color =
        if (isDark(context)) Color(0xFF1E293B) else Color.White

    @Composable
    fun subtleSurfaceColor(context: Context? = null): Color =
        if (isDark(context)) Color(0xFF0F172A) else Color(0xFFF1F5F9)

    @Composable
    fun textPrimary(context: Context? = null): Color =
        if (isDark(context)) Color(0xFFF8FAFC) else Color(0xFF102A43)

    @Composable
    fun textSecondary(context: Context? = null): Color =
        if (isDark(context)) Color(0xFF94A3B8) else Color(0xFF667085)

    @Composable
    fun borderColor(context: Context? = null): Color =
        if (isDark(context)) Color(0xFF334155) else Color(0xFFE5E7EB)

    @Composable
    fun accentBlue(context: Context? = null): Color =
        if (isDark(context)) Color(0xFF60A5FA) else Color(0xFF083D91)
}

private fun decodeBase64ToBitmap(base64Str: String): ImageBitmap? {
    if (base64Str.isBlank()) return null
    return try {
        val bytes = Base64.decode(base64Str, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    } catch (_: Exception) {
        null
    }
}

private fun encodeUriToBase64(context: Context, uri: Uri): String {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes()
        inputStream?.close()
        if (bytes != null) Base64.encodeToString(bytes, Base64.NO_WRAP) else ""
    } catch (_: Exception) {
        ""
    }
}

@Suppress("DEPRECATION")
suspend fun searchRealLiveLocationsIndia(context: Context, rawQuery: String): List<String> {
    val cleanQuery = rawQuery.trim()
    if (cleanQuery.length < 2) return emptyList()

    return withContext(Dispatchers.IO) {
        val results = LinkedHashSet<String>()

        try {
            if (Geocoder.isPresent()) {
                val geocoder = Geocoder(context, Locale("en", "IN"))
                val addresses = geocoder.getFromLocationName("$cleanQuery, India", 6)
                addresses?.forEach { addr ->
                    val parts = mutableListOf<String>()
                    val locality = addr.locality ?: addr.subLocality ?: addr.featureName
                    val subAdmin = addr.subAdminArea
                    val state = addr.adminArea
                    if (!locality.isNullOrBlank()) parts.add(locality.trim())
                    if (!subAdmin.isNullOrBlank() && !parts.contains(subAdmin.trim())) parts.add(subAdmin.trim())
                    if (!state.isNullOrBlank() && !parts.contains(state.trim())) parts.add(state.trim())
                    val formatted = parts.joinToString(", ")
                    if (formatted.length > 3) {
                        results.add(formatted)
                    }
                }
            }
        } catch (_: Exception) {}

        try {
            val encoded = URLEncoder.encode("$cleanQuery India", "UTF-8")
            val url = URL("https://photon.komoot.io/api/?q=$encoded&limit=8")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("User-Agent", "WorkoraApp/2.4 (Android)")
                connectTimeout = 4500
                readTimeout = 4500
            }
            if (conn.responseCode in 200..299) {
                val jsonText = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                val root = JSONObject(jsonText)
                val features = root.optJSONArray("features")
                if (features != null) {
                    for (i in 0 until features.length()) {
                        val props = features.getJSONObject(i).optJSONObject("properties") ?: continue
                        val country = props.optString("country", "")
                        if (country.isNotBlank() && !country.equals("India", ignoreCase = true) && !country.equals("भारत", ignoreCase = true)) {
                            continue
                        }
                        val name = props.optString("name", "")
                        val city = props.optString("city", "")
                        val county = props.optString("county", "")
                        val district = props.optString("district", "")
                        val state = props.optString("state", "")

                        val parts = mutableListOf<String>()
                        if (name.isNotBlank()) parts.add(name.trim())
                        val mid = city.ifBlank { district.ifBlank { county } }.trim()
                        if (mid.isNotBlank() && !parts.any { it.equals(mid, ignoreCase = true) }) {
                            parts.add(mid)
                        }
                        if (state.isNotBlank() && !parts.any { it.equals(state, ignoreCase = true) }) {
                            parts.add(state.trim())
                        }
                        val combined = parts.joinToString(", ")
                        if (combined.length > 3) {
                            results.add(combined)
                        }
                    }
                }
            }
            conn.disconnect()
        } catch (_: Exception) {}

        results.take(8).toList()
    }
}

@SuppressLint("MissingPermission")
@Suppress("DEPRECATION")
fun detectRealGpsLocationAddress(
    context: Context,
    onResult: (String?) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        var detectedAddress: String? = null
        try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val providers = listOf(
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER
            )
            var bestLoc: android.location.Location? = null
            for (p in providers) {
                val loc = try { lm.getLastKnownLocation(p) } catch (_: Exception) { null }
                if (loc != null && (bestLoc == null || loc.time > bestLoc.time)) {
                    bestLoc = loc
                }
            }

            if (bestLoc != null) {
                val lat = bestLoc.latitude
                val lon = bestLoc.longitude

                try {
                    if (Geocoder.isPresent()) {
                        val geocoder = Geocoder(context, Locale("en", "IN"))
                        val list = geocoder.getFromLocation(lat, lon, 1)
                        if (!list.isNullOrEmpty()) {
                            val a = list[0]
                            val parts = listOfNotNull(
                                a.locality ?: a.subLocality ?: a.featureName,
                                a.subAdminArea,
                                a.adminArea
                            ).map { it.trim() }.filter { it.isNotEmpty() }.distinct()
                            if (parts.isNotEmpty()) {
                                detectedAddress = parts.joinToString(", ")
                            }
                        }
                    }
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {}

        withContext(Dispatchers.Main) {
            onResult(detectedAddress)
        }
    }
}

@Composable
fun LiveLocationAutoCompleteField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Village / City / Area Location",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = WorkoraThemeManager.isDark(context)
    val deepNavy = WorkoraThemeManager.accentBlue(context)
    val brandOrange = Color(0xFFFF8C00)
    val textDark = WorkoraThemeManager.textPrimary(context)
    val cardBg = WorkoraThemeManager.surfaceColor(context)
    val borderCol = WorkoraThemeManager.borderColor(context)

    val liveSuggestions = remember { mutableStateListOf<String>() }
    var isSearching by remember { mutableStateOf(false) }
    var shouldSearchOnTyping by remember { mutableStateOf(false) }
    var isDetectingGps by remember { mutableStateOf(false) }

    val gpsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            isDetectingGps = true
            detectRealGpsLocationAddress(context) { address ->
                isDetectingGps = false
                if (!address.isNullOrBlank()) {
                    shouldSearchOnTyping = false
                    liveSuggestions.clear()
                    onValueChange(address)
                    Toast.makeText(context, "Live GPS Location: $address ✓", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Turn ON Phone GPS & try again", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "Location permission required for GPS", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(value, shouldSearchOnTyping) {
        if (!shouldSearchOnTyping) return@LaunchedEffect
        val query = value.trim()
        if (query.length < 2) {
            liveSuggestions.clear()
            isSearching = false
            return@LaunchedEffect
        }
        isSearching = true
        delay(300)
        val fetched = searchRealLiveLocationsIndia(context, query)
        liveSuggestions.clear()
        liveSuggestions.addAll(fetched)
        isSearching = false
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { newText ->
                shouldSearchOnTyping = true
                onValueChange(newText)
            },
            label = { Text(label) },
            placeholder = { Text("Type any village, tehsil, city or district...") },
            textStyle = TextStyle(color = textDark, fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = brandOrange
                )
            },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (value.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                shouldSearchOnTyping = false
                                liveSuggestions.clear()
                                onValueChange("")
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            val hasFine = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                            val hasCoarse = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasFine || hasCoarse) {
                                isDetectingGps = true
                                detectRealGpsLocationAddress(context) { address ->
                                    isDetectingGps = false
                                    if (!address.isNullOrBlank()) {
                                        shouldSearchOnTyping = false
                                        liveSuggestions.clear()
                                        onValueChange(address)
                                        Toast.makeText(context, "Live GPS: $address ✓", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Please turn ON phone GPS", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                gpsPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        }
                    ) {
                        if (isDetectingGps) {
                            CircularProgressIndicator(
                                color = deepNavy,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Detect Live GPS",
                                tint = deepNavy
                            )
                        }
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = deepNavy,
                unfocusedBorderColor = borderCol,
                focusedContainerColor = cardBg,
                unfocusedContainerColor = cardBg
            )
        )

        if (isSearching) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 6.dp)
            ) {
                CircularProgressIndicator(
                    color = brandOrange,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Searching live locations for '${value.trim()}'...",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = deepNavy
                )
            }
        }

        if (liveSuggestions.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF0F9FF)
                ),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFBAE6FD)),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    liveSuggestions.forEach { suggestion ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    shouldSearchOnTyping = false
                                    liveSuggestions.clear()
                                    onValueChange(suggestion)
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = brandOrange,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = suggestion,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = textDark
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoraLiveLocationModal(
    currentLocation: String,
    onDismiss: () -> Unit,
    onLocationSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val deepNavy = WorkoraThemeManager.accentBlue(context)
    val brandOrange = Color(0xFFFF8C00)
    val textDark = WorkoraThemeManager.textPrimary(context)
    val textMuted = WorkoraThemeManager.textSecondary(context)
    val surfaceCol = WorkoraThemeManager.surfaceColor(context)
    val subtleBg = WorkoraThemeManager.subtleSurfaceColor(context)
    val borderCol = WorkoraThemeManager.borderColor(context)

    var queryText by remember { mutableStateOf("") }
    val liveResults = remember { mutableStateListOf<String>() }
    var isSearching by remember { mutableStateOf(false) }

    LaunchedEffect(queryText) {
        val clean = queryText.trim()
        if (clean.length < 2) {
            liveResults.clear()
            isSearching = false
            return@LaunchedEffect
        }
        isSearching = true
        delay(300)
        val fetched = searchRealLiveLocationsIndia(context, clean)
        liveResults.clear()
        liveResults.addAll(fetched)
        isSearching = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = surfaceCol,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Select Live Location",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = deepNavy
                    )
                    Text(
                        text = "Current: $currentLocation",
                        fontSize = 11.sp,
                        color = textMuted
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = textDark)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = queryText,
                    onValueChange = { queryText = it },
                    placeholder = { Text("Type village, tehsil, city or pin code...") },
                    textStyle = TextStyle(color = textDark, fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = deepNavy)
                    },
                    trailingIcon = {
                        if (queryText.isNotEmpty()) {
                            IconButton(onClick = { queryText = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = textMuted)
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (isSearching) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(color = brandOrange, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Searching locations...", fontSize = 12.sp, color = deepNavy, fontWeight = FontWeight.SemiBold)
                    }
                }

                if (liveResults.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        liveResults.forEach { locName ->
                            Card(
                                onClick = { onLocationSelected(locName) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = subtleBg),
                                border = BorderStroke(1.dp, borderCol)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = brandOrange,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = locName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textDark
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
fun ProfileScreen(
    role: UserRole = UserRole.CUSTOMER,
    userName: String = "Workora User",
    userPhone: String = "",
    userLocation: String = "Silwani, Raisen (MP)",
    onBack: () -> Unit = {},
    onSwitchRole: () -> Unit = {},
    onLogout: () -> Unit = {},
    onOpenChat: () -> Unit = {},
    onOpenAdmin: () -> Unit = {},
    onUpdateProfile: (name: String, phone: String, location: String) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    val authPrefs = remember { context.getSharedPreferences("workora_real_auth", Context.MODE_PRIVATE) }
    val profilePrefs = remember { context.getSharedPreferences("workora_real_profile", Context.MODE_PRIVATE) }
    val settingsPrefs = remember { context.getSharedPreferences("workora_app_settings", Context.MODE_PRIVATE) }

    LaunchedEffect(Unit) {
        WorkoraThemeManager.syncFromPrefs(context)
    }
    val appThemeMode = WorkoraThemeManager.currentMode
    val isDark = WorkoraThemeManager.isDark(context)

    val navyColor = Color(0xFF083D91)
    val accentBlue = WorkoraThemeManager.accentBlue(context)
    val orangeColor = Color(0xFFFF8C00)
    val redDanger = Color(0xFFB42318)
    val bgColor = WorkoraThemeManager.bgColor(context)
    val cardColor = WorkoraThemeManager.surfaceColor(context)
    val subtleBgColor = WorkoraThemeManager.subtleSurfaceColor(context)
    val textDark = WorkoraThemeManager.textPrimary(context)
    val textMuted = WorkoraThemeManager.textSecondary(context)
    val borderColor = WorkoraThemeManager.borderColor(context)
    val greenColor = Color(0xFF22A06B)

    var appLang by remember {
        mutableStateOf(settingsPrefs.getString("app_language", "English") ?: "English")
    }

    fun tr(hi: String, hinglish: String, en: String): String {
        return when (appLang) {
            "Hindi" -> hi
            "Hinglish" -> hinglish
            else -> en
        }
    }

    fun themeLabel(mode: String): String {
        return when (mode) {
            "Light" -> tr("लाइट (Light)", "Light", "Light")
            "Dark" -> tr("डार्क (Dark)", "Dark", "Dark")
            else -> tr("सिस्टम डिफ़ॉल्ट (System)", "System (Default)", "System (Default)")
        }
    }

    var savedName by remember {
        mutableStateOf(profilePrefs.getString("user_name", userName) ?: userName)
    }
    var savedPhone by remember {
        mutableStateOf(profilePrefs.getString("user_phone", userPhone) ?: userPhone)
    }
    var savedLocation by remember {
        mutableStateOf(profilePrefs.getString("user_location", userLocation) ?: userLocation)
    }
    var savedSkill by remember {
        mutableStateOf(profilePrefs.getString("user_skill", "Default") ?: "Mason")
    }
    var savedWage by remember {
        mutableStateOf(profilePrefs.getString("user_rate", "600") ?: "600")
    }

    var kycStatus by remember {
        mutableStateOf(profilePrefs.getString("kyc_status", "NOT_SUBMITTED") ?: "NOT_SUBMITTED")
    }

    var profilePicBase64 by remember {
        mutableStateOf(profilePrefs.getString("profile_photo_base64", "") ?: "")
    }
    var workPhoto1Base64 by remember {
        mutableStateOf(profilePrefs.getString("work_photo_1", "") ?: "")
    }
    var workPhoto2Base64 by remember {
        mutableStateOf(profilePrefs.getString("work_photo_2", "") ?: "")
    }
    var workPhoto3Base64 by remember {
        mutableStateOf(profilePrefs.getString("work_photo_3", "") ?: "")
    }

    var availableToday by remember {
        mutableStateOf(settingsPrefs.getBoolean("available_today", true))
    }
    var directCallsEnabled by remember {
        mutableStateOf(settingsPrefs.getBoolean("direct_calls", true))
    }
    var workoraMessageAlertsEnabled by remember {
        mutableStateOf(settingsPrefs.getBoolean("workora_message_alerts", true))
    }
    var workRadiusKm by remember {
        mutableIntStateOf(settingsPrefs.getInt("work_radius_km", 10))
    }

    var isSettingsExpanded by remember { mutableStateOf(true) }
    var notificationsEnabled by remember {
        mutableStateOf(settingsPrefs.getBoolean("notifications_enabled", true))
    }
    var dataUsageMode by remember {
        mutableStateOf(settingsPrefs.getString("data_usage_mode", "Standard") ?: "Standard")
    }
    var profileVisibility by remember {
        mutableStateOf(settingsPrefs.getString("profile_visibility", "Everyone") ?: "Everyone")
    }
    var locationPrivacy by remember {
        mutableStateOf(settingsPrefs.getString("location_privacy", "Area Only") ?: "Area Only")
    }

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showLiveLocationModal by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showAdminSecurityGate by remember { mutableStateOf(false) }
    var showNotificationsCenterDialog by remember { mutableStateOf(false) }
    var showKycDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var showBlockedUsersDialog by remember { mutableStateOf(false) }
    var showDeleteAccountConfirmDialog by remember { mutableStateOf(false) }
    var activeInfoDialogTitle by remember { mutableStateOf<String?>(null) }
    var activeInfoDialogBody by remember { mutableStateOf("") }
    var activePhotoSlot by remember { mutableIntStateOf(0) }

    val loggedEmail = remember {
        (authPrefs.getString("last_logged_in_email", "") ?: "").trim().lowercase()
    }

    fun syncProfileAndSettingsToFirebase() {
        val cleanPhoneKey = savedPhone.filter { it.isDigit() }.takeLast(10).ifBlank { "0000000000" }
        val wageInt = savedWage.toIntOrNull() ?: 600

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val uConn = (URL("$PROFILE_FIREBASE_URL/users/u_$cleanPhoneKey.json").openConnection() as HttpURLConnection).apply {
                    requestMethod = "PATCH"
                    setRequestProperty("Content-Type", "application/json")
                    connectTimeout = 5000
                    readTimeout = 5000
                    doOutput = true
                }
                val uJson = JSONObject().apply {
                    put("name", savedName)
                    put("phone", savedPhone)
                    put("email", loggedEmail.ifBlank { "${cleanPhoneKey}@workora.in" })
                    put("location", savedLocation)
                    put("skill", savedSkill)
                    put("dailyWage", wageInt)
                    put("role", role.name)
                    put("availableToday", availableToday)
                    put("directCallsEnabled", directCallsEnabled)
                    put("workoraMessageAlerts", workoraMessageAlertsEnabled)
                    put("workRadiusKm", workRadiusKm)
                    put("appLanguage", appLang)
                    put("appThemeMode", WorkoraThemeManager.currentMode)
                    put("profileVisibility", profileVisibility)
                    put("locationPrivacy", locationPrivacy)
                    put("kycStatus", kycStatus)
                    put("updatedAt", System.currentTimeMillis())
                }
                OutputStreamWriter(uConn.outputStream).use { it.write(uJson.toString()) }
                uConn.responseCode
                uConn.disconnect()
            } catch (_: Exception) {}
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val encoded = encodeUriToBase64(context, uri)
            if (encoded.isNotBlank()) {
                when (activePhotoSlot) {
                    0 -> {
                        profilePicBase64 = encoded
                        profilePrefs.edit().putString("profile_photo_base64", encoded).apply()
                    }
                    1 -> {
                        workPhoto1Base64 = encoded
                        profilePrefs.edit().putString("work_photo_1", encoded).apply()
                    }
                    2 -> {
                        workPhoto2Base64 = encoded
                        profilePrefs.edit().putString("work_photo_2", encoded).apply()
                    }
                    3 -> {
                        workPhoto3Base64 = encoded
                        profilePrefs.edit().putString("work_photo_3", encoded).apply()
                    }
                }
                syncProfileAndSettingsToFirebase()
                Toast.makeText(context, "Photo Updated & Synced to Cloud ✓", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Admin check via role flag or cloud authorization (No personal email/phone in code)
    var isSuperAdmin by remember {
        mutableStateOf(authPrefs.getString("saved_user_role", "") == "ADMIN")
    }
    LaunchedEffect(loggedEmail) {
        if (loggedEmail.isNotBlank()) {
            FirebaseManager.checkIfEmailIsAdminOnCloud(loggedEmail) { isAdmin, _ ->
                if (isAdmin) isSuperAdmin = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(if (isDark) Color(0xFF0F172A) else navyColor)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = tr("प्रोफाइल और सेटिंग्स", "Profile & Settings", "Profile & Settings"),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            OutlinedButton(
                onClick = { showEditProfileDialog = true },
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = tr("बदलें", "Edit", "Edit"),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Profile Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                border = BorderStroke(1.dp, borderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val avatarBitmap = remember(profilePicBase64) { decodeBase64ToBitmap(profilePicBase64) }
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(navyColor)
                                .clickable {
                                    activePhotoSlot = 0
                                    imagePickerLauncher.launch("image/*")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (avatarBitmap != null) {
                                Image(
                                    bitmap = avatarBitmap,
                                    contentDescription = "Profile Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = savedName.take(1).uppercase().ifBlank { "W" },
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = savedName,
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = textDark
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = "Verified",
                                    tint = if (kycStatus == "VERIFIED") greenColor else orangeColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = if (role == UserRole.LABOUR) "$savedSkill • ₹$savedWage/day" else tr("ग्राहक अकाउंट (Customer)", "Customer / Hirer Account", "Customer / Hirer Account"),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = orangeColor
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { showLiveLocationModal = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = orangeColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$savedLocation (${tr("बदलें", "Change", "Change")})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = accentBlue
                                )
                            }

                            if (savedPhone.isNotBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = textMuted,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = savedPhone,
                                        fontSize = 12.sp,
                                        color = textMuted
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(subtleBgColor)
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("4.9 ★", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = accentBlue)
                            Text(tr("रेटिंग", "Rating", "Rating"), fontSize = 11.sp, color = textMuted)
                        }
                        Box(modifier = Modifier.width(1.dp).height(28.dp).background(borderColor))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("₹$savedWage", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = accentBlue)
                            Text(tr("दिहाड़ी/दिन", "Daily Wage", "Daily Wage"), fontSize = 11.sp, color = textMuted)
                        }
                        Box(modifier = Modifier.width(1.dp).height(28.dp).background(borderColor))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (role == UserRole.LABOUR) tr("कारीगर", "WORKER", "WORKER") else tr("ग्राहक", "HIRER", "HIRER"),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = orangeColor
                            )
                            Text(tr("सक्रिय मोड", "Active Role", "Active Role"), fontSize = 11.sp, color = textMuted)
                        }
                    }
                }
            }

            // ⚙️ COMPLETE 22-OPTION SETTINGS TREE
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                border = BorderStroke(1.dp, if (isSettingsExpanded) accentBlue else borderColor)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isSettingsExpanded = !isSettingsExpanded }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Settings, contentDescription = null, tint = accentBlue)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = tr("⚙️ सेटिंग्स (Complete Settings Hub)", "⚙️ Settings", "⚙️ Settings"),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = textDark
                                )
                                Text(
                                    text = tr(
                                        "अकाउंट, ऐप सेटिंग्स, प्राइवेसी, KYC, सुरक्षा और Workora",
                                        "Account, App Settings, Privacy, KYC & Workora",
                                        "Account, App Settings, Privacy, KYC & Workora"
                                    ),
                                    fontSize = 11.sp,
                                    color = textMuted
                                )
                            }
                        }
                        Icon(
                            imageVector = if (isSettingsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand Settings",
                            tint = accentBlue
                        )
                    }

                    if (isSettingsExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(subtleBgColor)
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SettingsCategoryHeader(title = tr("1. अकाउंट (ACCOUNT)", "1. ACCOUNT", "1. ACCOUNT"), color = accentBlue)
                            SettingsTreeItem(
                                label = tr("👤 प्रोफाइल (Profile)", "👤 Profile", "👤 Profile"),
                                valueText = savedName,
                                cardColor = cardColor,
                                borderColor = borderColor,
                                textDark = textDark,
                                textMuted = textMuted,
                                onClick = { showEditProfileDialog = true }
                            )
                            SettingsTreeItem(
                                label = tr("📱 मोबाइल नंबर (Phone Number)", "📱 Phone Number", "📱 Phone Number"),
                                valueText = savedPhone.ifBlank { tr("जोड़ें", "Add", "Add") },
                                cardColor = cardColor,
                                borderColor = borderColor,
                                textDark = textDark,
                                textMuted = textMuted,
                                onClick = { showEditProfileDialog = true }
                            )
                            SettingsTreeItem(
                                label = tr("🔐 पासवर्ड और सुरक्षा (Password)", "🔐 Password & Security", "🔐 Password & Security"),
                                valueText = tr("बदलें", "Change", "Change"),
                                cardColor = cardColor,
                                borderColor = borderColor,
                                textDark = textDark,
                                textMuted = textMuted,
                                onClick = { showPasswordDialog = true }
                            )
                            SettingsTreeItem(
                                label = tr("🆔 पहचान और KYC वेरिफिकेशन", "🆔 Identity & KYC Verification", "🆔 Identity & KYC Verification"),
                                valueText = when (kycStatus) {
                                    "VERIFIED" -> "Verified ✓"
                                    "VERIFICATION_PENDING" -> "Pending..."
                                    else -> "Upload ID"
                                },
                                cardColor = cardColor,
                                borderColor = borderColor,
                                textDark = textDark,
                                textMuted = textMuted,
                                onClick = { showKycDialog = true }
                            )

                            SettingsCategoryHeader(title = tr("2. ऐप सेटिंग्स (APP SETTINGS)", "2. APP SETTINGS", "2. APP SETTINGS"), color = accentBlue)
                            SettingsTreeItem(
                                label = tr("🌐 भाषा (Language)", "🌐 Language", "🌐 Language"),
                                valueText = if (appLang == "Hindi") "हिन्दी" else "English",
                                cardColor = cardColor,
                                borderColor = borderColor,
                                textDark = textDark,
                                textMuted = textMuted,
                                onClick = {
                                    appLang = if (appLang == "Hindi") "English" else "Hindi"
                                    settingsPrefs.edit().putString("app_language", appLang).apply()
                                    syncProfileAndSettingsToFirebase()
                                }
                            )
                            SettingsTreeItem(
                                label = tr("🔔 नोटिफिकेशन (Notifications)", "🔔 Notifications", "🔔 Notifications"),
                                valueText = if (notificationsEnabled) tr("चालू (ON)", "ON", "ON") else tr("बंद (OFF)", "OFF", "OFF"),
                                cardColor = cardColor,
                                borderColor = borderColor,
                                textDark = textDark,
                                textMuted = textMuted,
                                onClick = {
                                    notificationsEnabled = !notificationsEnabled
                                    settingsPrefs.edit().putBoolean("notifications_enabled", notificationsEnabled).apply()
                                    syncProfileAndSettingsToFirebase()
                                }
                            )
                            SettingsTreeItem(
                                label = tr("🎨 थीम (Theme Mode)", "🎨 Theme Mode", "🎨 Theme Mode"),
                                valueText = themeLabel(appThemeMode),
                                cardColor = cardColor,
                                borderColor = borderColor,
                                textDark = textDark,
                                textMuted = textMuted,
                                onClick = {
                                    WorkoraThemeManager.cycleNextThemeMode(context)
                                    syncProfileAndSettingsToFirebase()
                                }
                            )
                            SettingsTreeItem(
                                label = tr("📍 लोकेशन और कार्यक्षेत्र", "📍 Location & Area", "📍 Location & Area"),
                                valueText = savedLocation,
                                cardColor = cardColor,
                                borderColor = borderColor,
                                textDark = textDark,
                                textMuted = textMuted,
                                onClick = { showLiveLocationModal = true }
                            )
                            SettingsTreeItem(
                                label = tr("📶 डेटा उपयोग (Data Usage)", "📶 Data Usage", "📶 Data Usage"),
                                valueText = dataUsageMode,
                                cardColor = cardColor,
                                borderColor = borderColor,
                                textDark = textDark,
                                textMuted = textMuted,
                                onClick = {
                                    dataUsageMode = if (dataUsageMode == "Standard") "Data Saver" else "Standard"
                                    settingsPrefs.edit().putString("data_usage_mode", dataUsageMode).apply()
                                }
                            )

                            SettingsCategoryHeader(title = tr("3. प्राइवेसी और सुरक्षा (PRIVACY & SECURITY)", "3. PRIVACY & SECURITY", "3. PRIVACY & SECURITY"), color = accentBlue)
                            SettingsTreeItem(
                                label = tr("🔒 प्राइवेसी पॉलिसी (Privacy Policy)", "🔒 Privacy Policy", "🔒 Privacy Policy"),
                                valueText = tr("सुरक्षित", "Protected", "Protected"),
                                cardColor = cardColor,
                                borderColor = borderColor,
                                textDark = textDark,
                                textMuted = textMuted,
                                onClick = {
                                    activeInfoDialogTitle = "🔒 Privacy Policy"
                                    activeInfoDialogBody = "Workora protects your phone and live location under strict hardware AES-256 encryption. Your information is only visible to verified parties when communication is initiated."
                                }
                            )
                            SettingsTreeItem(
                                label = tr("👁️ प्रोफाइल कौन देख सकता है", "👁️ Who Can See Profile", "👁️ Who Can See Profile"),
                                valueText = profileVisibility,
                                cardColor = cardColor,
                                borderColor = borderColor,
                                textDark = textDark,
                                textMuted = textMuted,
                                onClick = {
                                    profileVisibility = if (profileVisibility == "Everyone") "Verified Users" else "Everyone"
                                    settingsPrefs.edit().putString("profile_visibility", profileVisibility).apply()
                                    syncProfileAndSettingsToFirebase()
                                }
                            )
                            SettingsTreeItem(
                                label = tr("📍 लोकेशन प्राइवेसी (Location Privacy)", "📍 Location Privacy", "📍 Location Privacy"),
                                valueText = locationPrivacy,
                                cardColor = cardColor,
                                borderColor = borderColor,
                                textDark = textDark,
                                textMuted = textMuted,
                                onClick = {
                                    locationPrivacy = if (locationPrivacy == "Area Only") "Exact Location" else "Area Only"
                                    settingsPrefs.edit().putString("location_privacy", locationPrivacy).apply()
                                    syncProfileAndSettingsToFirebase()
                                }
                            )
                            SettingsTreeItem(
                                label = tr("🚫 ब्लॉक किए गए यूज़र (Blocked Users)", "🚫 Blocked Users", "🚫 Blocked Users"),
                                valueText = tr("देखें / अनब्लॉक", "View / Unblock", "View / Unblock"),
                                cardColor = cardColor,
                                borderColor = borderColor,
                                textDark = textDark,
                                textMuted = textMuted,
                                onClick = { showBlockedUsersDialog = true }
                            )
                            SettingsTreeItem(
                                label = tr("🛡️ सुरक्षा शील्ड (PBKDF2 + OTP Shield)", "🛡️ Security Shield", "🛡️ Security Shield"),
                                valueText = "Active ✓",
                                cardColor = cardColor,
                                borderColor = borderColor,
                                textDark = textDark,
                                textMuted = textMuted,
                                onClick = {
                                    activeInfoDialogTitle = "🛡️ Workora Security Architecture"
                                    activeInfoDialogBody = "Your account is secured with 65,536-iteration PBKDF2 cryptographic hashing, hardware AES-256 Keystore protection, and real-time brute-force lockout prevention."
                                }
                            )

                            SettingsCategoryHeader(title = "4. WORKORA", color = accentBlue)
                            SettingsTreeItem(
                                label = tr("❓ सहायता और सपोर्ट (Help & Support)", "❓ Help & Support", "❓ Help & Support"),
                                valueText = "24x7 Support",
                                cardColor = cardColor,
                                borderColor = borderColor,
                                textDark = textDark,
                                textMuted = textMuted,
                                onClick = {
                                    activeInfoDialogTitle = "❓ Workora Help & Support"
                                    activeInfoDialogBody = "For any help with jobs, hiring, or safety:\n• Support Email: support@workora.in\n• Use Workora Live Message for instant support."
                                }
                            )
                            SettingsTreeItem(
                                label = tr("📖 Workora कैसे काम करता है", "📖 How Workora Works", "📖 How Workora Works"),
                                valueText = tr("देखें", "Guide", "Guide"),
                                cardColor = cardColor,
                                borderColor = borderColor,
                                textDark = textDark,
                                textMuted = textMuted,
                                onClick = {
                                    activeInfoDialogTitle = "📖 How Workora Works"
                                    activeInfoDialogBody = "1. Customer posts a job.\n2. Labour applies for the job.\n3. Customer hires the labour.\n4. Both work and confirm completion.\n5. Both rate each other 1-5 stars!"
                                }
                            )
                            SettingsTreeItem(
                                label = tr("⚠️ समस्या की शिकायत करें (Report a Problem)", "⚠️ Report a Problem", "⚠️ Report a Problem"),
                                valueText = tr("रिपोर्ट", "Report", "Report"),
                                cardColor = cardColor,
                                borderColor = borderColor,
                                textDark = textDark,
                                textMuted = textMuted,
                                onClick = { showReportDialog = true }
                            )
                            SettingsTreeItem(
                                label = tr("📄 नियम और शर्तें (Terms & Conditions)", "📄 Terms & Conditions", "📄 Terms & Conditions"),
                                valueText = tr("पढ़ें", "Read", "Read"),
                                cardColor = cardColor,
                                borderColor = borderColor,
                                textDark = textDark,
                                textMuted = textMuted,
                                onClick = {
                                    activeInfoDialogTitle = "📄 Terms & Conditions"
                                    activeInfoDialogBody = "Workora is a direct peer-to-peer connection platform. Users must agree upon daily wages and work terms directly. Advance registration fees or scams are strictly prohibited."
                                }
                            )
                            SettingsTreeItem(
                                label = tr("📲 Workora ऐप शेयर करें (Share App)", "📲 Share Workora", "📲 Share Workora"),
                                valueText = tr("शेयर", "Share", "Share"),
                                cardColor = cardColor,
                                borderColor = borderColor,
                                textDark = textDark,
                                textMuted = textMuted,
                                onClick = {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, "Download Workora App — Find skilled workers and daily jobs near you!")
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share Workora App"))
                                }
                            )
                            SettingsTreeItem(
                                label = tr("⭐ Workora को रेटिंग दें (Rate App)", "⭐ Rate Workora", "⭐ Rate Workora"),
                                valueText = "5.0 ★",
                                cardColor = cardColor,
                                borderColor = borderColor,
                                textDark = textDark,
                                textMuted = textMuted,
                                onClick = { Toast.makeText(context, "Thank you for rating Workora 5 Stars! ★★★★★", Toast.LENGTH_SHORT).show() }
                            )
                            SettingsTreeItem(
    label = tr("ℹ️ Workora के बारे में (About Workora)", "ℹ️ About Workora", "ℹ️ About Workora"),
    valueText = "Info",
    cardColor = cardColor,
    borderColor = borderColor,
    textDark = textDark,
    textMuted = textMuted,
    onClick = {
        activeInfoDialogTitle = "ℹ️ About Workora"
        activeInfoDialogBody = "Workora – Find. Hire. Work.\nWorkora connects customers with trusted local workers. Find the right worker, post jobs, chat, hire, and manage your work—all in one simple app."
    }
)


                            SettingsCategoryHeader(title = tr("5. अकाउंट एक्शन्स (ACCOUNT ACTIONS)", "5. ACCOUNT ACTIONS", "5. ACCOUNT ACTIONS"), color = accentBlue)
                            SettingsTreeItem(
                                label = tr("🔄 रोल बदलें (Customer ⇄ Worker)", "🔄 Switch Role", "🔄 Switch Role"),
                                valueText = role.name,
                                cardColor = cardColor,
                                borderColor = borderColor,
                                textDark = textDark,
                                textMuted = textMuted,
                                onClick = onSwitchRole
                            )
                            SettingsTreeItem(
                                label = tr("🚪 लॉग आउट करें (Logout)", "🚪 Logout", "🚪 Logout"),
                                valueText = tr("बाहर आएं", "Logout", "Logout"),
                                cardColor = cardColor,
                                borderColor = borderColor,
                                textDark = textDark,
                                textMuted = textMuted,
                                onClick = onLogout
                            )
                            SettingsTreeItem(
                                label = tr("🗑️ अकाउंट हमेशा के लिए हटाएं (Delete Account)", "🗑️ Delete Account", "🗑️ Delete Account"),
                                valueText = tr("हटाएं", "Delete", "Delete"),
                                cardColor = cardColor,
                                borderColor = borderColor,
                                textDark = redDanger,
                                textMuted = redDanger,
                                onClick = { showDeleteAccountConfirmDialog = true }
                            )
                        }
                    }
                }
            }

            Card(
                onClick = onOpenChat,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                border = BorderStroke(1.dp, borderColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Chat, contentDescription = null, tint = orangeColor)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = tr("वर्कओरा मैसेज खोलें (Workora Message)", "Open Workora Message", "Open Workora Message"),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = textDark
                        )
                    }
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = textMuted)
                }
            }

            ProfileMenuActionRow(
                title = tr("🔔 नोटिफिकेशन (Notifications)", "🔔 Notifications", "🔔 Notifications"),
                subtitle = if (notificationsEnabled) {
                    tr("Workora मैसेज और जॉब अलर्ट चालू हैं", "Workora Message & job alerts are ON", "Workora Message & job alerts are ON")
                } else {
                    tr("नोटिफिकेशन बंद हैं", "Notifications are muted", "Notifications are muted")
                },
                cardColor = cardColor,
                borderColor = borderColor,
                textDark = textDark,
                textMuted = textMuted,
                onClick = { showNotificationsCenterDialog = true }
            )

            if (isSuperAdmin) {
                Card(
                    onClick = { showAdminSecurityGate = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = navyColor),
                    border = BorderStroke(1.5.dp, orangeColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = orangeColor)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = tr("🔐 सुपर एडमिन पैनल खोलें (PIN + OTP)", "🔐 Open Super Admin Panel (PIN + OTP)", "🔐 Open Super Admin Panel (PIN + OTP)"),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = orangeColor)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (showKycDialog) {
        var docType by remember { mutableStateOf("Aadhaar Card") }
        var docNumber by remember { mutableStateOf("") }
        var docPhotoBase64 by remember { mutableStateOf("") }
        var isUploadingKyc by remember { mutableStateOf(false) }

        val kycDocPicker = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                docPhotoBase64 = encodeUriToBase64(context, uri)
                Toast.makeText(context, "ID Document Photo Loaded ✓", Toast.LENGTH_SHORT).show()
            }
        }

        AlertDialog(
            onDismissRequest = { showKycDialog = false },
            containerColor = cardColor,
            title = {
                Text(
                    text = "🆔 Identity & KYC Verification",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentBlue
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Current Status: $kycStatus",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (kycStatus == "VERIFIED") greenColor else orangeColor
                    )
                    OutlinedTextField(
                        value = docNumber,
                        onValueChange = { docNumber = it },
                        label = { Text("ID Number (Aadhaar/Voter ID)") },
                        textStyle = TextStyle(color = textDark, fontSize = 14.sp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedButton(
                        onClick = { kycDocPicker.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (docPhotoBase64.isNotBlank()) "ID Photo Selected ✓" else "Upload ID Photo")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (docNumber.trim().length >= 4 && docPhotoBase64.isNotBlank()) {
                            isUploadingKyc = true
                            FirebaseManager.submitKycDocument(
                                userPhone = savedPhone,
                                docType = docType,
                                docNumber = docNumber.trim(),
                                docBase64 = docPhotoBase64
                            ) { ok ->
                                isUploadingKyc = false
                                if (ok) {
                                    kycStatus = "VERIFICATION_PENDING"
                                    profilePrefs.edit().putString("kyc_status", "VERIFICATION_PENDING").apply()
                                    showKycDialog = false
                                    Toast.makeText(context, "KYC Submitted! Under Admin Review ✓", Toast.LENGTH_LONG).show()
                                }
                            }
                        } else {
                            Toast.makeText(context, "Please enter ID number and upload photo", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = orangeColor)
                ) {
                    if (isUploadingKyc) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                    } else {
                        Text("Submit KYC")
                    }
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showKycDialog = false }) { Text("Close") }
            }
        )
    }

    if (showReportDialog) {
        val reportReasons = listOf(
            "Fake Profile",
            "Fraud / Money Scam",
            "Abusive Behaviour",
            "Wrong Information",
            "No Show",
            "Harassment",
            "Other"
        )
        var selectedReason by remember { mutableStateOf(reportReasons[0]) }
        var targetPhoneInput by remember { mutableStateOf("") }
        var reportDescription by remember { mutableStateOf("") }
        var isSubmittingReport by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            containerColor = cardColor,
            title = {
                Text(
                    text = "⚠️ Submit Report to Admin",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = redDanger
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = targetPhoneInput,
                        onValueChange = { targetPhoneInput = it },
                        label = { Text("Reported User Phone (Optional)") },
                        textStyle = TextStyle(color = textDark, fontSize = 13.sp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Select Violation Reason:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textDark)
                    reportReasons.forEach { r ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedReason = r }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (selectedReason == r) Icons.Default.CheckCircle else Icons.Default.Info,
                                contentDescription = null,
                                tint = if (selectedReason == r) orangeColor else textMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(r, fontSize = 13.sp, color = textDark, fontWeight = if (selectedReason == r) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                    OutlinedTextField(
                        value = reportDescription,
                        onValueChange = { reportDescription = it },
                        label = { Text("Details of the issue") },
                        textStyle = TextStyle(color = textDark, fontSize = 13.sp),
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isSubmittingReport = true
                        FirebaseManager.submitReport(
                            reporterPhone = savedPhone,
                            reportedPhone = targetPhoneInput.trim().ifBlank { "GENERAL_ISSUE" },
                            reportedName = "User",
                            reason = selectedReason,
                            description = reportDescription.trim()
                        ) { ok ->
                            isSubmittingReport = false
                            showReportDialog = false
                            Toast.makeText(context, if (ok) "Report lodged in Firebase! Admin will review ✓" else "Failed to send report", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = redDanger)
                ) {
                    if (isSubmittingReport) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                    } else {
                        Text("Send Report")
                    }
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showReportDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showBlockedUsersDialog) {
        AlertDialog(
            onDismissRequest = { showBlockedUsersDialog = false },
            containerColor = cardColor,
            title = {
                Text(
                    text = "🚫 Blocked Users Management",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textDark
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "When you block a user, they cannot call you or send you messages on Workora.",
                        fontSize = 12.sp,
                        color = textMuted
                    )
                    Text(
                        text = "Currently 0 users blocked in your list.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = textDark
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showBlockedUsersDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = navyColor)
                ) {
                    Text("OK")
                }
            }
        )
    }

    if (showDeleteAccountConfirmDialog) {
        var isDeleting by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showDeleteAccountConfirmDialog = false },
            containerColor = cardColor,
            title = {
                Text(
                    text = "🗑️ Delete Account Forever?",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = redDanger
                )
            },
            text = {
                Text(
                    text = "⚠️ WARNING: This will permanently anonymize your account and delete your worker profile and active listings. Historical completed work references will be anonymized for security. This cannot be undone.",
                    fontSize = 13.sp,
                    color = textDark,
                    lineHeight = 19.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        isDeleting = true
                        FirebaseManager.deleteAndAnonymizeAccount(savedPhone) { ok ->
                            isDeleting = false
                            showDeleteAccountConfirmDialog = false
                            if (ok) {
                                profilePrefs.edit().clear().apply()
                                authPrefs.edit().clear().apply()
                                Toast.makeText(context, "Account Deleted & Anonymized ✓", Toast.LENGTH_LONG).show()
                                onLogout()
                            } else {
                                Toast.makeText(context, "Failed to delete account", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = redDanger)
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                    } else {
                        Text("Yes, Delete Forever")
                    }
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteAccountConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showNotificationsCenterDialog) {
        WorkoraLiveNotificationCenterDialog(
            userLocation = savedLocation,
            appLang = appLang,
            onDismiss = { showNotificationsCenterDialog = false },
            onOpenChat = onOpenChat
        )
    }

    if (showAdminSecurityGate) {
        WorkoraAdminSecurityGateDialog(
            onDismiss = { showAdminSecurityGate = false },
            onAdminVerifiedSuccess = {
                showAdminSecurityGate = false
                onOpenAdmin()
            }
        )
    }

    if (showLiveLocationModal) {
        WorkoraLiveLocationModal(
            currentLocation = savedLocation,
            onDismiss = { showLiveLocationModal = false },
            onLocationSelected = { newLoc ->
                savedLocation = newLoc
                profilePrefs.edit().putString("user_location", newLoc).apply()
                onUpdateProfile(savedName, savedPhone, newLoc)
                syncProfileAndSettingsToFirebase()
                showLiveLocationModal = false
                Toast.makeText(context, "Location updated & synced ✓", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showEditProfileDialog) {
        var editName by remember { mutableStateOf(savedName) }
        var editPhone by remember { mutableStateOf(savedPhone) }
        var editLocation by remember { mutableStateOf(savedLocation) }
        var editSkill by remember { mutableStateOf(savedSkill) }
        var editWage by remember { mutableStateOf(savedWage) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            containerColor = cardColor,
            title = {
                Text(
                    text = tr("प्रोफाइल जानकारी बदलें", "Edit Profile Details", "Edit Profile Details"),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentBlue
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Name") },
                        textStyle = TextStyle(color = textDark, fontSize = 14.sp),
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Phone Number") },
                        textStyle = TextStyle(color = textDark, fontSize = 14.sp),
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    LiveLocationAutoCompleteField(
                        value = editLocation,
                        onValueChange = { editLocation = it },
                        label = "Location & Area (Live Search)"
                    )
                    val skillOptions = listOf(
    "Default", "Mason", "Electrician", "Plumber",
    "Painter", "Carpenter", "Labour", "Cleaner",
    "Farm Worker", "Tile Worker", "Other"
)

Text(
    text = "Primary Skill (Select Skill)",
    fontSize = 13.sp,
    fontWeight = FontWeight.ExtraBold,
    color = textDark
)

skillOptions.chunked(3).forEach { rowSkills ->
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rowSkills.forEach { skillItem ->
            val isSelected = editSkill.equals(skillItem, ignoreCase = true)
            OutlinedButton(
                onClick = { editSkill = skillItem },
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, if (isSelected) orangeColor else borderColor),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isSelected) orangeColor.copy(alpha = 0.15f) else cardColor
                ),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
            ) {
                Text(
                    text = skillItem,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) orangeColor else textDark,
                    maxLines = 1
                )
            }
        }
    }
}

                    OutlinedTextField(
                        value = editWage,
                        onValueChange = { editWage = it.filter { c -> c.isDigit() } },
                        label = { Text("Daily Wage (₹/day)") },
                        textStyle = TextStyle(color = textDark, fontSize = 14.sp),
                        leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        savedName = editName.trim().ifBlank { "Workora User" }
                        savedPhone = editPhone.trim()
                        savedLocation = editLocation.trim().ifBlank { "Silwani, Raisen (MP)" }
                        savedSkill = editSkill.trim().ifBlank { "Mason" }
                        savedWage = editWage.trim().ifBlank { "600" }

                        profilePrefs.edit().apply {
                            putString("user_name", savedName)
                            putString("user_phone", savedPhone)
                            putString("user_location", savedLocation)
                            putString("user_skill", savedSkill)
                            putString("user_rate", savedWage)
                            apply()
                        }

                        onUpdateProfile(savedName, savedPhone, savedLocation)
                        syncProfileAndSettingsToFirebase()
                        showEditProfileDialog = false
                        Toast.makeText(context, "Profile Saved & Synced! ✓", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = orangeColor)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(tr("सेव करें", "Save Changes", "Save Changes"), color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showEditProfileDialog = false }) {
                    Text(tr("रद्द करें", "Cancel", "Cancel"))
                }
            }
        )
    }

    if (showPasswordDialog) {
        var newPass by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            containerColor = cardColor,
            title = {
                Text("🔐 Password & Security", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = accentBlue)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Enter a new password (minimum 8 characters, at least 1 letter & 1 number). It will be hashed with 65,536 iterations of PBKDF2.",
                        fontSize = 12.sp,
                        color = textMuted
                    )
                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it },
                        label = { Text("New Password") },
                        textStyle = TextStyle(color = textDark, fontSize = 14.sp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clean = newPass.trim()
                        if (clean.length >= 8 && clean.any { it.isLetter() } && clean.any { it.isDigit() }) {
                            val digits = savedPhone.filter { it.isDigit() }.takeLast(10)
                            val hash = WorkoraSecurityManager.hashPasswordSecure(clean)
                            authPrefs.edit().apply {
                                if (loggedEmail.isNotBlank()) putString("user_pass_$loggedEmail", clean)
                                if (digits.isNotBlank()) {
                                    putString("user_pass_$digits", clean)
                                    putString("user_hash_$digits", hash)
                                }
                                apply()
                            }
                            showPasswordDialog = false
                            Toast.makeText(context, "Password Updated Securely (PBKDF2) ✓", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Min 8 chars with 1 letter & 1 number required!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = orangeColor)
                ) {
                    Text("Update Password", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showPasswordDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (activeInfoDialogTitle != null) {
        AlertDialog(
            onDismissRequest = { activeInfoDialogTitle = null },
            containerColor = cardColor,
            title = {
                Text(
                    text = activeInfoDialogTitle!!,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentBlue
                )
            },
            text = {
                Text(
                    text = activeInfoDialogBody,
                    fontSize = 13.sp,
                    color = textDark,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { activeInfoDialogTitle = null },
                    colors = ButtonDefaults.buttonColors(containerColor = navyColor)
                ) {
                    Text(tr("ठीक है (OK)", "OK", "OK"), color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun SettingsCategoryHeader(
    title: String,
    color: Color
) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.ExtraBold,
        color = color,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
    )
}

@Composable
private fun SettingsTreeItem(
    label: String,
    valueText: String,
    cardColor: Color,
    borderColor: Color,
    textDark: Color,
    textMuted: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = textDark
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = valueText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF8C00)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = textMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun ProfileMenuActionRow(
    title: String,
    subtitle: String,
    cardColor: Color,
    borderColor: Color,
    textDark: Color,
    textMuted: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = textMuted
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = textMuted
            )
        }
    }
}
