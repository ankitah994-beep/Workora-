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
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
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
import androidx.compose.material.icons.filled.SwapHoriz
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

// =========================================================================
// GLOBAL APP THEME MANAGER (CONTROLS ENTIRE APP FROM ONE PLACE)
// Default Mode = "System" (Follows Phone's Default System Light/Dark Mode)
// Options = "System" (Default) | "Light" | "Dark"
// =========================================================================
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
        } catch (_: Exception) {
        }

        try {
            val encoded = URLEncoder.encode("$cleanQuery India", "UTF-8")
            val url = URL("https://photon.komoot.io/api/?q=$encoded&limit=8")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("User-Agent", "WorkoraApp/2.0 (Android)")
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
        } catch (_: Exception) {
        }

        if (results.size < 4) {
            try {
                val encoded = URLEncoder.encode(cleanQuery, "UTF-8")
                val url = URL("https://nominatim.openstreetmap.org/search?q=$encoded&countrycodes=in&format=json&addressdetails=1&limit=6")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("User-Agent", "WorkoraMobileApp/2.0 (ankitah994@gmail.com)")
                    setRequestProperty("Accept-Language", "en-IN,en")
                    connectTimeout = 5000
                    readTimeout = 5000
                }
                if (conn.responseCode in 200..299) {
                    val text = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                    val arr = JSONArray(text)
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val display = obj.optString("display_name", "")
                        val cleanDisplay = display.split(",")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() && !it.equals("India", ignoreCase = true) && !it.all { c -> c.isDigit() } }
                            .take(3)
                            .joinToString(", ")
                        if (cleanDisplay.length > 3) {
                            results.add(cleanDisplay)
                        }
                    }
                }
                conn.disconnect()
            } catch (_: Exception) {
            }
        }

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
                } catch (_: Exception) {
                }

                if (detectedAddress.isNullOrBlank()) {
                    val url = URL("https://nominatim.openstreetmap.org/reverse?format=json&lat=$lat&lon=$lon&zoom=14&addressdetails=1")
                    val conn = (url.openConnection() as HttpURLConnection).apply {
                        setRequestProperty("User-Agent", "WorkoraMobileApp/2.0")
                        connectTimeout = 5000
                        readTimeout = 5000
                    }
                    if (conn.responseCode in 200..299) {
                        val resp = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                        val obj = JSONObject(resp)
                        val addr = obj.optJSONObject("address")
                        if (addr != null) {
                            val villageOrCity = addr.optString("city").ifBlank {
                                addr.optString("town").ifBlank {
                                    addr.optString("village").ifBlank {
                                        addr.optString("suburb")
                                    }
                                }
                            }
                            val district = addr.optString("state_district").ifBlank { addr.optString("county") }
                            val state = addr.optString("state")
                            val parts = listOf(villageOrCity, district, state).map { it.trim() }.filter { it.isNotEmpty() }.distinct()
                            if (parts.isNotEmpty()) {
                                detectedAddress = parts.joinToString(", ")
                            }
                        }
                    }
                    conn.disconnect()
                }
            }
        } catch (_: Exception) {
        }

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
    var isGettingGps by remember { mutableStateOf(false) }

    val gpsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            isGettingGps = true
            detectRealGpsLocationAddress(context) { addr ->
                isGettingGps = false
                if (!addr.isNullOrBlank()) {
                    onLocationSelected(addr)
                } else {
                    Toast.makeText(context, "Please enable phone GPS", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

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
                OutlinedButton(
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
                            isGettingGps = true
                            detectRealGpsLocationAddress(context) { addr ->
                                isGettingGps = false
                                if (!addr.isNullOrBlank()) {
                                    onLocationSelected(addr)
                                } else {
                                    Toast.makeText(context, "Turn ON GPS and try again", Toast.LENGTH_SHORT).show()
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
                    },
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.2.dp, deepNavy)
                ) {
                    if (isGettingGps) {
                        CircularProgressIndicator(color = deepNavy, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Detecting GPS Location...", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = deepNavy)
                    } else {
                        Icon(Icons.Default.MyLocation, contentDescription = null, tint = brandOrange, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Use Current Live GPS Location", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = deepNavy)
                    }
                }

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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        CircularProgressIndicator(color = brandOrange, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Searching real locations in India...", fontSize = 12.sp, color = deepNavy, fontWeight = FontWeight.SemiBold)
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
                } else if (queryText.trim().length >= 2 && !isSearching) {
                    Button(
                        onClick = { onLocationSelected(queryText.trim()) },
                        colors = ButtonDefaults.buttonColors(containerColor = brandOrange),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Use '${queryText.trim()}'", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(
                        text = "Type at least 2 letters above to see live village, town & city suggestions.",
                        fontSize = 11.sp,
                        color = textMuted
                    )
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
fun ProfileScreen(
    role: UserRole = UserRole.CUSTOMER,
    userName: String = "Ankit Ahirwar",
    userPhone: String = "+91 6265798340",
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

    // Sync Global Theme State (Default = "System")
    LaunchedEffect(Unit) {
        WorkoraThemeManager.syncFromPrefs(context)
    }
    val appThemeMode = WorkoraThemeManager.currentMode
    val isDark = WorkoraThemeManager.isDark(context)

    val navyColor = Color(0xFF083D91)
    val accentBlue = WorkoraThemeManager.accentBlue(context)
    val orangeColor = Color(0xFFFF8C00)
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
        mutableStateOf(profilePrefs.getString("user_skill", "Mason (राजमिस्त्री)") ?: "Mason (राजमिस्त्री)")
    }
    var savedWage by remember {
        mutableStateOf(profilePrefs.getString("user_rate", "600") ?: "600")
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
    var activeInfoDialogTitle by remember { mutableStateOf<String?>(null) }
    var activeInfoDialogBody by remember { mutableStateOf("") }
    var activePhotoSlot by remember { mutableIntStateOf(0) }

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
                Toast.makeText(context, "Photo Updated ✓", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val loggedEmail = remember {
        (authPrefs.getString("last_logged_in_email", "") ?: "").trim().lowercase()
    }
    val isSuperAdmin = remember(loggedEmail, savedPhone) {
        loggedEmail == "ankitah994@gmail.com" ||
                loggedEmail.contains("ankitah994") ||
                savedPhone.filter { it.isDigit() }.takeLast(10) == "6265798340" ||
                authPrefs.getString("saved_user_role", "") == "ADMIN"
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
                                    text = savedName.take(1).uppercase(),
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
                                    tint = greenColor,
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

            // 🎨 ONE-PLACE GLOBAL APP THEME MODE CARD (System Default / Light / Dark)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                border = BorderStroke(1.dp, borderColor)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tr("🎨 ऐप थीम मोड (App Theme Mode)", "🎨 App Theme Mode", "🎨 App Theme Mode"),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = textDark
                        )
                        Text(
                            text = themeLabel(appThemeMode),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = orangeColor
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "System" to tr("सिस्टम (Default)", "System", "System"),
                            "Light" to tr("लाइट (Light)", "Light", "Light"),
                            "Dark" to tr("डार्क (Dark)", "Dark", "Dark")
                        ).forEach { (modeCode, modeLabel) ->
                            val selected = appThemeMode == modeCode
                            Button(
                                onClick = {
                                    WorkoraThemeManager.setThemeMode(context, modeCode)
                                    Toast.makeText(
                                        context,
                                        "App Theme: ${themeLabel(modeCode)} ✓",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                modifier = Modifier.weight(1f).height(40.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selected) navyColor else subtleBgColor
                                ),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = modeLabel,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) Color.White else textDark
                                )
                            }
                        }
                    }
                }
            }

            // Language Selection Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                border = BorderStroke(1.dp, borderColor)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = tr("🌐 ऐप की भाषा चुनें (App Language)", "🌐 App Language", "🌐 Select App Language"),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textDark
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Hindi" to "हिन्दी", "English" to "English").forEach { (code, label) ->
                            val selected = appLang == code
                            Button(
                                onClick = {
                                    appLang = code
                                    settingsPrefs.edit().putString("app_language", code).apply()
                                    Toast.makeText(
                                        context,
                                        if (code == "Hindi") "ऐप की भाषा हिन्दी कर दी गई है ✓" else "App language set to English ✓",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                modifier = Modifier.weight(1f).height(40.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selected) navyColor else subtleBgColor
                                ),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) Color.White else textDark
                                )
                            }
                        }
                    }
                }
            }

            // Work Proof Photos
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                border = BorderStroke(1.dp, borderColor)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = tr("काम की फ़ोटो (Work Proof Photos)", "Work Proof Photos", "Work Proof Photos"),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textDark
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(
                            1 to workPhoto1Base64,
                            2 to workPhoto2Base64,
                            3 to workPhoto3Base64
                        ).forEach { (slot, base64Data) ->
                            val bmp = remember(base64Data) { decodeBase64ToBitmap(base64Data) }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(86.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(subtleBgColor)
                                    .clickable {
                                        activePhotoSlot = slot
                                        imagePickerLauncher.launch("image/*")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (bmp != null) {
                                    Image(
                                        bitmap = bmp,
                                        contentDescription = "Work Photo $slot",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.AddAPhoto,
                                            contentDescription = null,
                                            tint = orangeColor,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = tr("फ़ोटो $slot", "Photo $slot", "Photo $slot"),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = textMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Availability, Direct Calls, Workora Message Alerts & Distance Radius Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                border = BorderStroke(1.dp, borderColor)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tr("आज काम के लिए उपलब्ध (Availability ON/OFF)", "Availability ON/OFF", "Availability ON/OFF"),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = textDark
                            )
                            Text(
                                text = tr("ग्राहकों को आपकी प्रोफाइल दिखेगी", "Show profile in available workers list", "Show profile in available workers list"),
                                fontSize = 11.sp,
                                color = textMuted
                            )
                        }
                        Switch(
                            checked = availableToday,
                            onCheckedChange = {
                                availableToday = it
                                settingsPrefs.edit().putBoolean("available_today", it).apply()
                            },
                            colors = SwitchDefaults.colors(checkedTrackColor = greenColor)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tr("सीधे फोन कॉल की अनुमति", "Allow Direct Phone Calls", "Allow Direct Phone Calls"),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = textDark
                            )
                            Text(
                                text = tr("ग्राहक सीधे कॉल कर सकेंगे", "Customers can call your mobile number", "Customers can call your mobile number"),
                                fontSize = 11.sp,
                                color = textMuted
                            )
                        }
                        Switch(
                            checked = directCallsEnabled,
                            onCheckedChange = {
                                directCallsEnabled = it
                                settingsPrefs.edit().putBoolean("direct_calls", it).apply()
                            },
                            colors = SwitchDefaults.colors(checkedTrackColor = navyColor)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tr("वर्कओरा मैसेज और जॉब अलर्ट", "Workora Message & Job Alerts", "Workora Message & Job Alerts"),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = textDark
                            )
                            Text(
                                text = tr("नए काम और वर्कओरा मैसेज की सूचना पाएं", "Receive instant Workora Message & job alerts", "Receive instant Workora Message & job alerts"),
                                fontSize = 11.sp,
                                color = textMuted
                            )
                        }
                        Switch(
                            checked = workoraMessageAlertsEnabled,
                            onCheckedChange = {
                                workoraMessageAlertsEnabled = it
                                settingsPrefs.edit().putBoolean("workora_message_alerts", it).apply()
                            },
                            colors = SwitchDefaults.colors(checkedTrackColor = orangeColor)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = tr("काम की दूरी (Work Distance Radius): $workRadiusKm KM", "Work Distance Radius: $workRadiusKm KM", "Work Distance Radius: $workRadiusKm KM"),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = textDark
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(5, 10, 25).forEach { km ->
                            val selected = workRadiusKm == km
                            OutlinedButton(
                                onClick = {
                                    workRadiusKm = km
                                    settingsPrefs.edit().putInt("work_radius_km", km).apply()
                                },
                                modifier = Modifier.weight(1f).height(36.dp),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, if (selected) orangeColor else borderColor),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (selected) orangeColor.copy(alpha = 0.15f) else cardColor
                                ),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = "$km KM",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) orangeColor else textDark
                                )
                            }
                        }
                    }
                }
            }

            // ⚙️ Full Settings Tree (100% Connected to Live Theme & Language)
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
                                    text = tr("⚙️ सेटिंग्स (Settings)", "⚙️ Settings", "⚙️ Settings"),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = textDark
                                )
                                Text(
                                    text = tr(
                                        "अकाउंट, ऐप सेटिंग्स, प्राइवेसी और सुरक्षा",
                                        "Account, App Settings, Privacy & Security, Workora",
                                        "Account, App Settings, Privacy & Security, Workora"
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
                            SettingsCategoryHeader(title = tr("+ अकाउंट (+ Account)", "+ Account", "+ Account"), color = accentBlue)
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
                                valueText = savedPhone,
                                cardColor = cardColor,
                                borderColor = borderColor,
                                textDark = textDark,
                                textMuted = textMuted,
                                onClick = { showEditProfileDialog = true }
                            )
                            SettingsTreeItem(
                                label = tr("🔐 पासवर्ड और सुरक्षा", "🔐 Password & Security", "🔐 Password & Security"),
                                valueText = tr("पासवर्ड बदलें", "Change Password", "Change Password"),
                                cardColor = cardColor,
                                borderColor = borderColor,
                                textDark = textDark,
                                textMuted = textMuted,
                                onClick = { showPasswordDialog = true }
                            )

                            SettingsCategoryHeader(title = tr("+ ऐप सेटिंग्स (+ App Settings)", "+ App Settings", "+ App Settings"), color = accentBlue)
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
                                    Toast.makeText(
                                        context,
                                        if (appLang == "Hindi") "भाषा हिन्दी कर दी गई है ✓" else "Language set to English ✓",
                                        Toast.LENGTH_SHORT
                                    ).show()
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
                                }
                            )
                            SettingsTreeItem(
                                label = tr("🎨 थीम (Theme)", "🎨 Theme", "🎨 Theme"),
                                valueText = themeLabel(appThemeMode),
                                cardColor = cardColor,
                                borderColor = borderColor,
                                textDark = textDark,
                                textMuted = textMuted,
                                onClick = {
                                    val newMode = WorkoraThemeManager.cycleNextThemeMode(context)
                                    Toast.makeText(
                                        context,
                                        "App Theme: ${themeLabel(newMode)} ✓",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                            SettingsTreeItem(
                                label = tr("📍 लोकेशन और क्षेत्र", "📍 Location & Area", "📍 Location & Area"),
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

                            SettingsCategoryHeader(title = tr("+ प्राइवेसी और सुरक्षा", "+ Privacy & Security", "+ Privacy & Security"), color = accentBlue)
                            SettingsTreeItem(
                                label = tr("🔒 प्राइवेसी (Privacy)", "🔒 Privacy", "🔒 Privacy"),
                                valueText = tr("सुरक्षित", "Protected", "Protected"),
                                cardColor = cardColor,
                                borderColor = borderColor,
                                textDark = textDark,
                                textMuted = textMuted,
                                onClick = {
                                    activeInfoDialogTitle = tr("🔒 प्राइवेसी पॉलिसी", "🔒 Privacy Policy", "🔒 Privacy Policy")
                                    activeInfoDialogBody = tr(
                                        "आपका मोबाइल नंबर और प्रोफाइल जानकारी सुरक्षित है और केवल सत्यापित Workora उपयोगकर्ताओं को ही दिखाई देती है।",
                                        "Your phone number and profile details are safe and only visible to verified Workora users.",
                                        "Your phone number and profile details are safe and only visible to verified Workora users."
                                    )
                                }
                            )
                            SettingsTreeItem(
                                label = tr("👁️ मेरी प्रोफाइल कौन देख सकता है", "👁️ Who can see my profile", "👁️ Who can see my profile"),
                                valueText = profileVisibility,
                                cardColor = cardColor,
                                borderColor = borderColor,
                                textDark = textDark,
                                textMuted = textMuted,
                                onClick = {
                                    profileVisibility = if (profileVisibility == "Everyone") "Verified Users" else "Everyone"
                                    settingsPrefs.edit().putString("profile_visibility", profileVisibility).apply()
                                }
                            )
                            SettingsTreeItem(
                                label = tr("📍 लोकेशन प्राइवेसी", "📍 Location Privacy", "📍 Location Privacy"),
                                valueText = locationPrivacy,
                                cardColor = cardColor,
                                borderColor = borderColor,
                                textDark = textDark,
                                textMuted = textMuted,
                                onClick = {
                                    locationPrivacy = if (locationPrivacy == "Area Only") "Exact Location" else "Area Only"
                                    settingsPrefs.edit().putString("location_privacy", locationPrivacy).apply()
                                }
                            )
                            SettingsTreeItem(
                                label = tr("🚫 ब्लॉक किए गए यूज़र", "🚫 Blocked Users", "🚫 Blocked Users"),
                                valueText = tr("0 ब्लॉक", "0 Blocked", "0 Blocked"),
                                cardColor = cardColor,
                                borderColor = borderColor,
                                textDark = textDark,
                                textMuted = textMuted,
                                onClick = {
                                    activeInfoDialogTitle = tr("🚫 ब्लॉक किए गए यूज़र", "🚫 Blocked Users", "🚫 Blocked Users")
                                    activeInfoDialogBody = tr(
                                        "आपने किसी भी यूज़र को ब्लॉक नहीं किया है।",
                                        "You have not blocked any users on Workora.",
                                        "You have not blocked any users on Workora."
                                    )
                                }
                            )
                            SettingsTreeItem(
                                label = tr("🛡️ सुरक्षा (Security)", "🛡️ Security", "🛡️ Security"),
                                valueText = tr("सत्यापित ✓", "OTP Verified ✓", "OTP Verified ✓"),
                                cardColor = cardColor,
                                borderColor = borderColor,
                                textDark = textDark,
                                textMuted = textMuted,
                                onClick = { showPasswordDialog = true }
                            )

                            SettingsCategoryHeader(title = "+ Workora", color = accentBlue)
                            SettingsTreeItem(
                                label = tr("❓ सहायता और सपोर्ट", "❓ Help & Support", "❓ Help & Support"),
                                valueText = "24x7 Support",
                                cardColor = cardColor,
                                borderColor = borderColor,
                                textDark = textDark,
                                textMuted = textMuted,
                                onClick = {
                                    activeInfoDialogTitle = tr("❓ Workora सहायता और सपोर्ट", "❓ Workora Help & Support", "❓ Workora Help & Support")
                                    activeInfoDialogBody = tr(
                                        "काम पर रखने, काम खोजने या Workora मैसेज में किसी भी सहायता के लिए:\n• हेल्पलाइन: +91 6265798340\n• ईमेल: ankitah994@gmail.com\n• सेवा क्षेत्र: $savedLocation और संपूर्ण भारत",
                                        "For any assistance with hiring, jobs, or Workora Message:\n• Helpline: +91 6265798340\n• Email: ankitah994@gmail.com\n• Service Area: $savedLocation & All India",
                                        "For any assistance with hiring, jobs, or Workora Message:\n• Helpline: +91 6265798340\n• Email: ankitah994@gmail.com\n• Service Area: $savedLocation & All India"
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Quick Menu Action Rows
            Card(
                onClick = onSwitchRole,
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
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = accentBlue)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = tr("रोल बदलें (Customer ⇄ Worker)", "Switch Role (Customer ⇄ Worker)", "Switch Role (Customer ⇄ Worker)"),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = textDark
                        )
                    }
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = textMuted)
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
                onClick = {
                    activeInfoDialogTitle = tr("🔔 लाइव नोटिफिकेशन सेंटर", "🔔 Live Notification Center", "🔔 Live Notification Center")
                    activeInfoDialogBody = tr(
                        "🟢 नया काम अलर्ट ($savedLocation):\n• राजमिस्त्री और इलेक्ट्रीशियन की आवश्यकता (₹600/दिन) — अभी उपलब्ध।\n\n💬 Workora मैसेज अपडेट:\n• आपके क्षेत्र ($savedLocation) के सत्यापित ग्राहक और कारीगर ऑनलाइन हैं।\n\n🛡️ सुरक्षा और प्रोफाइल अलर्ट:\n• आपकी प्रोफाइल ($savedName) 100% रियल OTP से सत्यापित और सक्रिय है।",
                        "🟢 New Job Alert ($savedLocation):\n• Mason & Electrician work available (₹600/day).\n\n💬 Workora Message Update:\n• Verified customers and workers in $savedLocation are online.\n\n🛡️ Security & Profile Alert:\n• Your profile ($savedName) is 100% Real-OTP verified and active.",
                        "🟢 New Job Alert ($savedLocation):\n• Mason & Electrician work available (₹600/day).\n\n💬 Workora Message Update:\n• Verified customers and workers in $savedLocation are online.\n\n🛡️ Security & Profile Alert:\n• Your profile ($savedName) is 100% Real-OTP verified and active."
                    )
                }
            )

            if (role == UserRole.CUSTOMER) {
                ProfileMenuActionRow(
                    title = tr("⭐ सेव किए गए कारीगर (Saved Workers)", "⭐ Saved Workers", "⭐ Saved Workers"),
                    subtitle = tr("अपने सेव किए गए कारीगरों को तुरंत देखें", "Quickly access your bookmarked workers", "Quickly access your bookmarked workers"),
                    cardColor = cardColor,
                    borderColor = borderColor,
                    textDark = textDark,
                    textMuted = textMuted,
                    onClick = {
                        activeInfoDialogTitle = tr("⭐ सेव किए गए कारीगर", "⭐ Saved Workers", "⭐ Saved Workers")
                        activeInfoDialogBody = tr(
                            "1. रमेश कुमार — राजमिस्त्री (₹600/दिन • $savedLocation)\n2. सुरेश पटेल — इलेक्ट्रीशियन (₹550/दिन • $savedLocation)\n3. अमित यादव — प्लंबर (₹500/दिन • $savedLocation)",
                            "1. Ramesh Kumar — Mason (₹600/day • $savedLocation)\n2. Suresh Patel — Electrician (₹550/day • $savedLocation)\n3. Amit Yadav — Plumber (₹500/day • $savedLocation)",
                            "1. Ramesh Kumar — Mason (₹600/day • $savedLocation)\n2. Suresh Patel — Electrician (₹550/day • $savedLocation)\n3. Amit Yadav — Plumber (₹500/day • $savedLocation)"
                        )
                    }
                )
            } else {
                ProfileMenuActionRow(
                    title = tr("🔖 सेव किए गए काम (Saved Jobs)", "🔖 Saved Jobs", "🔖 Saved Jobs"),
                    subtitle = tr("सेव या अप्लाई किए गए काम देखें", "View jobs you have bookmarked or applied for", "View jobs you have bookmarked or applied for"),
                    cardColor = cardColor,
                    borderColor = borderColor,
                    textDark = textDark,
                    textMuted = textMuted,
                    onClick = {
                        activeInfoDialogTitle = tr("🔖 सेव किए गए काम", "🔖 Saved Jobs", "🔖 Saved Jobs")
                        activeInfoDialogBody = tr(
                            "1. मकान मरम्मत और प्लास्टर का काम — ₹600/दिन ($savedLocation)\n2. हाउस वायरिंग और पंखा फिटिंग — ₹550/दिन ($savedLocation)",
                            "1. House Repair & Wall Plastering — ₹600/day ($savedLocation)\n2. Complete House Wiring — ₹550/day ($savedLocation)",
                            "1. House Repair & Wall Plastering — ₹600/day ($savedLocation)\n2. Complete House Wiring — ₹550/day ($savedLocation)"
                        )
                    }
                )
            }

            ProfileMenuActionRow(
                title = tr("❓ सहायता और सपोर्ट (Help & Support)", "❓ Help & Support", "❓ Help & Support"),
                subtitle = tr("कॉल सपोर्ट, सवाल-जवाब और Workora मैसेज सहायता", "Call support, FAQs & Workora Message assistance", "Call support, FAQs & Workora Message assistance"),
                cardColor = cardColor,
                borderColor = borderColor,
                textDark = textDark,
                textMuted = textMuted,
                onClick = {
                    activeInfoDialogTitle = tr("❓ सहायता और सपोर्ट", "❓ Help & Support", "❓ Help & Support")
                    activeInfoDialogBody = tr(
                        "Workora पर किसी भी सहायता के लिए:\n\n• हेल्पलाइन नंबर: +91 6265798340\n• सपोर्ट ईमेल: ankitah994@gmail.com\n• कारीगर या ग्राहक से सीधे बात करने के लिए Workora मैसेज का उपयोग करें।",
                        "Need help on Workora?\n\n• Support Phone: +91 6265798340\n• Support Email: ankitah994@gmail.com\n• Use Workora Message to chat directly with workers or customers.",
                        "Need help on Workora?\n\n• Support Phone: +91 6265798340\n• Support Email: ankitah994@gmail.com\n• Use Workora Message to chat directly with workers or customers."
                    )
                }
            )

            ProfileMenuActionRow(
                title = tr("🛡️ रिपोर्ट और सुरक्षा (Report / Safety)", "🛡️ Report / Safety", "🛡️ Report / Safety"),
                subtitle = tr("सुरक्षा नियम और फेक काम/यूज़र की शिकायत करें", "Safety guidelines & report fake jobs or users", "Safety guidelines & report fake jobs or users"),
                cardColor = cardColor,
                borderColor = borderColor,
                textDark = textDark,
                textMuted = textMuted,
                onClick = {
                    activeInfoDialogTitle = tr("🛡️ रिपोर्ट और सुरक्षा", "🛡️ Report / Safety", "🛡️ Report / Safety")
                    activeInfoDialogBody = tr(
                        "• काम पर जाने से पहले कॉल या Workora मैसेज पर पूरी जानकारी ज़रूर कन्फर्म करें।\n• किसी को भी रजिस्ट्रेशन या काम के नाम पर एडवांस पैसे न दें।\n• सुरक्षा हेल्पलाइन: +91 6265798340",
                        "• Always verify work details on call or Workora Message before travelling.\n• Never pay advance registration fees to anyone.\n• Safety Helpline: +91 6265798340.",
                        "• Always verify work details on call or Workora Message before travelling.\n• Never pay advance registration fees to anyone.\n• Safety Helpline: +91 6265798340."
                    )
                }
            )

            // Protected Super Admin Panel Button -> Opens 3-Layer Admin Security Gate
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

            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB42318))
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = tr("लॉग आउट करें (Logout)", "Logout", "Logout"),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
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
                showLiveLocationModal = false
                Toast.makeText(context, "Location updated to $newLoc ✓", Toast.LENGTH_SHORT).show()
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
                    OutlinedTextField(
                        value = editSkill,
                        onValueChange = { editSkill = it },
                        label = { Text("Primary Skill (e.g. Mason, Electrician)") },
                        textStyle = TextStyle(color = textDark, fontSize = 14.sp),
                        leadingIcon = { Icon(Icons.Default.Build, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
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
                        savedName = editName.trim().ifBlank { "Ankit Ahirwar" }
                        savedPhone = editPhone.trim().ifBlank { "+91 6265798340" }
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
                        showEditProfileDialog = false
                        Toast.makeText(context, "Profile Saved Successfully! ✓", Toast.LENGTH_SHORT).show()
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
                        text = "Enter a new password (minimum 8 characters, at least 1 letter & 1 number):",
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
                            Toast.makeText(context, "Password Updated Successfully ✓", Toast.LENGTH_SHORT).show()
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
            },
            dismissButton = {
                if (activeInfoDialogTitle!!.contains("Help") || activeInfoDialogTitle!!.contains("Report") || activeInfoDialogTitle!!.contains("सहायता") || activeInfoDialogTitle!!.contains("रिपोर्ट")) {
                    OutlinedButton(
                        onClick = {
                            try {
                                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:+916265798340")))
                            } catch (_: Exception) {
                            }
                        }
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = orangeColor, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(tr("कॉल करें", "Call Helpline", "Call Helpline"), color = orangeColor, fontWeight = FontWeight.Bold)
                    }
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
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
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
