package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.User
import com.example.model.Worker
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val CustNavyPrimary = Color(0xFF083D91)
private val CustDarkNavyBtn = Color(0xFF0B2345)
private val CustOrangeAccent = Color(0xFFFF8C00)
private val CustBgLight = Color(0xFFF8FAFC)
private val CustWhite = Color(0xFFFFFFFF)
private val CustMainText = Color(0xFF0B2345)
private val CustSecondaryText = Color(0xFF687280)
private val CustBorder = Color(0xFFE5EAF0)
private val CustSuccessGreen = Color(0xFF16A34A)
private val CustRateBadgeBg = Color(0xFFFEF9C3)
private val CustRateBadgeBorder = Color(0xFFFDE047)
private val CustRateTextOrange = Color(0xFFD97706)

private const val CUST_FB_URL = "https://workora-d8b51-default-rtdb.firebaseio.com"

internal data class CustFeaturedWorkerProfile(
    val id: String,
    val name: String,
    val category: String,
    val rating: Double,
    val jobsDone: Int,
    val area: String,
    val stateName: String,
    val dailyRate: Int,
    val experience: String,
    val phone: String,
    val isVerified: Boolean,
    val shirtColor: Color,
    val hatColor: Color,
    val about: String,
    val profilePhoto: String = "",
    val photo1: String = "",
    val photo2: String = "",
    val photo3: String = ""
)

// =========================================================================
// REAL LIVE LOCATION SUGGESTION ENGINE (Shared across the entire Workora App)
// Combines instant verified Indian locations + Live OpenStreetMap India API
// =========================================================================
internal val VerifiedIndianLocationsDirectory = listOf(
    "Silwani, Raisen, Madhya Pradesh",
    "Main Market, Silwani, Madhya Pradesh",
    "Sector 4, Silwani, Madhya Pradesh",
    "Bhopal Road, Silwani, Madhya Pradesh",
    "Bus Stand, Silwani, Madhya Pradesh",
    "Gram Bamhori, Silwani, Madhya Pradesh",
    "Begamganj, Raisen, Madhya Pradesh",
    "Gairatganj, Raisen, Madhya Pradesh",
    "Udaipura, Raisen, Madhya Pradesh",
    "Bareli, Raisen, Madhya Pradesh",
    "Obedullaganj, Raisen, Madhya Pradesh",
    "Mandideep, Raisen, Madhya Pradesh",
    "Sanchi, Raisen, Madhya Pradesh",
    "Raisen, Madhya Pradesh",
    "Civil Lines, Raisen, Madhya Pradesh",
    "New Colony, Raisen, Madhya Pradesh",
    "Station Road, Raisen, Madhya Pradesh",
    "Bhopal, Madhya Pradesh",
    "MP Nagar, Bhopal, Madhya Pradesh",
    "Kolar Road, Bhopal, Madhya Pradesh",
    "Indrapuri, Bhopal, Madhya Pradesh",
    "Ayodhya Bypass, Bhopal, Madhya Pradesh",
    "Hoshangabad Road, Bhopal, Madhya Pradesh",
    "Indore, Madhya Pradesh",
    "Vijay Nagar, Indore, Madhya Pradesh",
    "Rajwada, Indore, Madhya Pradesh",
    "Bhawarkuan, Indore, Madhya Pradesh",
    "Sagar, Madhya Pradesh",
    "Makronia, Sagar, Madhya Pradesh",
    "Vidisha, Madhya Pradesh",
    "Ganj Basoda, Vidisha, Madhya Pradesh",
    "Sironj, Vidisha, Madhya Pradesh",
    "Jabalpur, Madhya Pradesh",
    "Gwalior, Madhya Pradesh",
    "Ujjain, Madhya Pradesh",
    "Dewas, Madhya Pradesh",
    "Sehore, Madhya Pradesh",
    "Ashta, Sehore, Madhya Pradesh",
    "Narmadapuram (Hoshangabad), Madhya Pradesh",
    "Itarsi, Madhya Pradesh",
    "Pipariya, Madhya Pradesh",
    "Chhindwara, Madhya Pradesh",
    "Rewa, Madhya Pradesh",
    "Satna, Madhya Pradesh",
    "Katni, Madhya Pradesh",
    "Damoh, Madhya Pradesh",
    "Chhatarpur, Madhya Pradesh",
    "Tikamgarh, Madhya Pradesh",
    "Betul, Madhya Pradesh",
    "Khandwa, Madhya Pradesh",
    "Khargone, Madhya Pradesh",
    "Ratlam, Madhya Pradesh",
    "Mandsaur, Madhya Pradesh",
    "Neemuch, Madhya Pradesh",
    "Guna, Madhya Pradesh",
    "Shivpuri, Madhya Pradesh",
    "Morena, Madhya Pradesh",
    "Bhind, Madhya Pradesh",
    "New Delhi, Delhi NCR",
    "Noida, Uttar Pradesh",
    "Lucknow, Uttar Pradesh",
    "Kanpur, Uttar Pradesh",
    "Varanasi, Uttar Pradesh",
    "Agra, Uttar Pradesh",
    "Prayagraj, Uttar Pradesh",
    "Jhansi, Uttar Pradesh",
    "Lalitpur, Uttar Pradesh",
    "Jaipur, Rajasthan",
    "Kota, Rajasthan",
    "Udaipur, Rajasthan",
    "Mumbai, Maharashtra",
    "Pune, Maharashtra",
    "Nagpur, Maharashtra",
    "Ahmedabad, Gujarat",
    "Surat, Gujarat",
    "Patna, Bihar",
    "Raipur, Chhattisgarh",
    "Bilaspur, Chhattisgarh"
)

internal fun fetchLiveLocationSuggestions(
    query: String,
    onResult: (List<String>) -> Unit
) {
    val cleanQuery = query.trim()
    if (cleanQuery.isEmpty()) {
        onResult(VerifiedIndianLocationsDirectory.take(6))
        return
    }

    // 1. Instant local matches so user sees suggestions with 0ms delay
    val localMatches = VerifiedIndianLocationsDirectory.filter { loc ->
        loc.contains(cleanQuery, ignoreCase = true) ||
            cleanQuery.split(" ", ",").filter { it.isNotBlank() }.all { token ->
                loc.contains(token, ignoreCase = true)
            }
    }.take(6)

    onResult(localMatches)

    // 2. Live OpenStreetMap Nominatim Geocoding API for real villages/colonies/cities across India
    if (cleanQuery.length >= 2) {
        Thread {
            val combined = mutableListOf<String>()
            combined.addAll(localMatches)
            try {
                val encoded = URLEncoder.encode("$cleanQuery, India", "UTF-8")
                val url = URL("https://nominatim.openstreetmap.org/search?q=$encoded&format=json&addressdetails=1&limit=6&countrycodes=in")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.setRequestProperty("User-Agent", "WorkoraApp/1.0")
                conn.connectTimeout = 3500
                conn.readTimeout = 3500
                if (conn.responseCode == 200) {
                    val resp = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                    val arr = JSONArray(resp)
                    for (i in 0 until arr.length()) {
                        val obj = arr.optJSONObject(i) ?: continue
                        val displayName = obj.optString("display_name", "")
                        if (displayName.isNotBlank()) {
                            val parts = displayName.split(",")
                                .map { it.trim() }
                                .filter { it.isNotBlank() && !it.equals("India", true) && !it.all { c -> c.isDigit() } }
                            val formatted = parts.take(3).joinToString(", ")
                            if (formatted.isNotBlank() && combined.none { it.equals(formatted, ignoreCase = true) }) {
                                combined.add(formatted)
                            }
                        }
                    }
                }
                conn.disconnect()
            } catch (_: Exception) {
            }
            Handler(Looper.getMainLooper()).post {
                onResult(combined.take(7))
            }
        }.start()
    }
}

@Composable
internal fun LiveLocationAutoCompleteField(
    value: String,
    onValueChange: (String) -> Unit,
    onLocationSelected: (fullLocation: String, areaPart: String, statePart: String) -> Unit = { _, _, _ -> },
    label: String,
    placeholder: String = "शहर, गाँव या एरिया लिखें...",
    modifier: Modifier = Modifier
) {
    var showSuggestions by remember { mutableStateOf(false) }
    val suggestions = remember { mutableStateListOf<String>() }

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = { newText ->
                onValueChange(newText)
                showSuggestions = true
                fetchLiveLocationSuggestions(newText) { res ->
                    suggestions.clear()
                    suggestions.addAll(res)
                }
            },
            label = { Text(label) },
            placeholder = { Text(placeholder, fontSize = 13.sp, color = CustSecondaryText) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = CustOrangeAccent
                )
            },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (value.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                onValueChange("")
                                fetchLiveLocationSuggestions("") { res ->
                                    suggestions.clear()
                                    suggestions.addAll(res)
                                }
                                showSuggestions = true
                            }
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = CustSecondaryText)
                        }
                    }
                    IconButton(
                        onClick = {
                            if (!showSuggestions) {
                                fetchLiveLocationSuggestions(value) { res ->
                                    suggestions.clear()
                                    suggestions.addAll(res)
                                }
                            }
                            showSuggestions = !showSuggestions
                        }
                    ) {
                        Icon(
                            imageVector = if (showSuggestions) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Suggestions",
                            tint = CustNavyPrimary
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        if (showSuggestions && suggestions.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CustWhite),
                border = BorderStroke(1.dp, CustNavyPrimary.copy(alpha = 0.25f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFEFF6FF))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = null,
                                tint = CustNavyPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Live Real Location Suggestions",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CustNavyPrimary
                            )
                        }
                        Text(
                            text = "बंद करें ✕",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CustSecondaryText,
                            modifier = Modifier.clickable { showSuggestions = false }
                        )
                    }

                    suggestions.forEach { suggestion ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val parts = suggestion.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                    val statePart = if (parts.size >= 2) parts.last() else "Madhya Pradesh"
                                    val areaPart = if (parts.size >= 2) parts.dropLast(1).joinToString(", ") else suggestion
                                    onValueChange(suggestion)
                                    onLocationSelected(suggestion, areaPart, statePart)
                                    showSuggestions = false
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Place,
                                contentDescription = null,
                                tint = CustOrangeAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = suggestion,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CustMainText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.NorthWest,
                                contentDescription = null,
                                tint = CustSecondaryText,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        HorizontalDivider(color = CustBorder)
                    }
                }
            }
        }
    }
}

private fun translateCategoryLabel(category: String, isHindi: Boolean): String {
    if (!isHindi) return category
    return when (category.trim().lowercase(Locale.US)) {
        "all" -> "सभी (All)"
        "mason" -> "राजमिस्त्री (Mason)"
        "plumber" -> "प्लंबर (Plumber)"
        "electrician" -> "इलेक्ट्रीशियन (Electrician)"
        "painter" -> "पेंटर (Painter)"
        "carpenter" -> "बढ़ई (Carpenter)"
        "general labour" -> "मजदूर (General Labour)"
        "tile worker" -> "टाइल्स कारीगर (Tile Worker)"
        "cleaner" -> "सफाई कर्मी (Cleaner)"
        "farm worker" -> "कृषि मजदूर (Farm Worker)"
        else -> category
    }
}

private fun matchesWorkerSmartSearch(worker: CustFeaturedWorkerProfile, rawQuery: String): Boolean {
    val q = rawQuery.trim().lowercase(Locale.US)
    if (q.isEmpty()) return true

    val hindiCategory = translateCategoryLabel(worker.category, true).lowercase(Locale.US)
    val extraSynonyms = when (worker.category.trim().lowercase(Locale.US)) {
        "mason" -> "राजमिस्त्री मिस्त्री सिलाई चुनाई प्लास्टर ईंट घर निर्माण mistri rajmistri brick plaster"
        "plumber" -> "प्लंबर नल पाइप फिटिंग टंकी पानी मोटर plumber nal pipe fitting tank"
        "electrician" -> "इलेक्ट्रीशियन बिजली वायरिंग पंखा कूलर लाइट स्विच bijli wiring fan light switch"
        "painter" -> "पेंटर पुट्टी पेंट रंगाई पुताई कलर painter putty paint color"
        "carpenter" -> "बढ़ई फर्नीचर लकड़ी दरवाजा खिड़की पलंग badhai furniture wood door"
        "general labour" -> "मजदूर लेबर हेल्पर लोडिंग अनलोडिंग खुदाई majdur labour helper"
        "tile worker" -> "टाइल्स कारीगर पत्थर मार्बल ग्रेनाइट फर्श tile tiles marble granite floor worker"
        "cleaner" -> "सफाई कर्मी क्लीनर झाड़ू पोछा टैंक सफाई safai cleaner cleaning"
        "farm worker" -> "कृषि मजदूर खेती किसान फसल कटाई kheti farm kisan agriculture"
        else -> ""
    }

    val searchableBlob = buildString {
        append(worker.name.lowercase(Locale.US)).append(" ")
        append(worker.category.lowercase(Locale.US)).append(" ")
        append(hindiCategory).append(" ")
        append(extraSynonyms).append(" ")
        append(worker.area.lowercase(Locale.US)).append(" ")
        append(worker.stateName.lowercase(Locale.US)).append(" ")
        append(worker.phone.lowercase(Locale.US)).append(" ")
        append(worker.dailyRate.toString()).append(" ")
        append(worker.about.lowercase(Locale.US)).append(" ")
        append("worker karigar कारीगर वर्कर")
    }

    if (searchableBlob.contains(q)) return true
    val words = q.split("\\s+".toRegex()).filter { it.isNotBlank() }
    return words.isNotEmpty() && words.all { word -> searchableBlob.contains(word) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <JobT, CatT, TabT> CustomerDashboardScreen(
    currentUser: User? = null,
    searchQuery: String = "",
    onSearchQueryChanged: (String) -> Unit = {},
    workers: List<Worker> = emptyList(),
    jobs: List<JobT> = emptyList(),
    selectedCategory: CatT,
    onCategorySelected: (CatT) -> Unit = {},
    activeTab: TabT,
    onTabSelected: (TabT) -> Unit = {},
    onPostJob: (String, String, String, Int, String, Int, String, String) -> Unit = { _, _, _, _, _, _, _, _ -> },
    onHireWorker: (Worker) -> Unit = {},
    onCompleteJob: (Long) -> Unit = {},
    onSwitchRole: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onOpenFilters: () -> Unit = {},
    toastMessage: String? = null,
    onOpenChat: () -> Unit = {}
) {
    val context = LocalContext.current
    val chatPrefs = remember { context.getSharedPreferences("workora_active_chat", Context.MODE_PRIVATE) }
    val profilePrefs = remember { context.getSharedPreferences("workora_real_profile", Context.MODE_PRIVATE) }

    LaunchedEffect(Unit) {
        AppLanguageManager.init(context)
    }
    val selectedLanguage = AppLanguageManager.currentLanguage
    val isHindi = selectedLanguage.equals("Hindi", ignoreCase = true) || selectedLanguage.contains("हिंदी")

    LaunchedEffect(toastMessage) {
        if (!toastMessage.isNullOrBlank()) {
            Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
        }
    }

    // Active Location State for Home Screen Header & Filtering
    var activeUserLocation by remember {
        mutableStateOf(
            profilePrefs.getString("user_location", "")?.ifBlank { "All Locations" } ?: "All Locations"
        )
    }
    var filterByActiveLocation by remember { mutableStateOf(false) }
    var showLocationPickerDialog by remember { mutableStateOf(false) }

    var localSearchText by remember { mutableStateOf(searchQuery) }
    var selectedSkillFilter by remember { mutableStateOf("All") }
    var sortOption by remember { mutableStateOf("DEFAULT") }
    var showSortFilterModal by remember { mutableStateOf(false) }

    var selectedWorkerForProfile by remember { mutableStateOf<CustFeaturedWorkerProfile?>(null) }
    var workerForHireRequest by remember { mutableStateOf<CustFeaturedWorkerProfile?>(null) }
    var showPostWorkDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }

    val defaultFeaturedWorkers = remember {
        listOf(
            CustFeaturedWorkerProfile(
                id = "w_sunil",
                name = "Sunil Kumar",
                category = "Mason",
                rating = 4.9,
                jobsDone = 25,
                area = "Sector 4, Silwani",
                stateName = "Madhya Pradesh",
                dailyRate = 850,
                experience = "6 years",
                phone = "+91 9826012345",
                isVerified = true,
                shirtColor = Color(0xFF1D4ED8),
                hatColor = Color(0xFFFACC15),
                about = "Experienced Mason specializing in bricklaying, plastering, RCC slab work, and house construction with 6 years of verified local experience."
            ),
            CustFeaturedWorkerProfile(
                id = "w_rajesh",
                name = "Rajesh Sharma",
                category = "Plumber",
                rating = 4.9,
                jobsDone = 32,
                area = "Civil Lines, Raisen",
                stateName = "Madhya Pradesh",
                dailyRate = 900,
                experience = "7 years",
                phone = "+91 9755098765",
                isVerified = true,
                shirtColor = Color(0xFFEA580C),
                hatColor = Color(0xFF2563EB),
                about = "Skilled Plumber for complete bathroom fitting, water tank installation, pipeline leakage repair, and motor fitting."
            ),
            CustFeaturedWorkerProfile(
                id = "w_aslam",
                name = "Mohammad Aslam",
                category = "Electrician",
                rating = 4.8,
                jobsDone = 39,
                area = "Main Market, Silwani",
                stateName = "Madhya Pradesh",
                dailyRate = 950,
                experience = "8 years",
                phone = "+91 9926543210",
                isVerified = true,
                shirtColor = Color(0xFF15803D),
                hatColor = Color(0xFFF97316),
                about = "Certified Electrician experienced in house wiring, switchboard installation, inverter connection, ceiling fan and appliance repair."
            ),
            CustFeaturedWorkerProfile(
                id = "w_ramesh",
                name = "Ramesh Yadav",
                category = "Painter",
                rating = 4.8,
                jobsDone = 21,
                area = "Ward 8, Silwani",
                stateName = "Madhya Pradesh",
                dailyRate = 750,
                experience = "5 years",
                phone = "+91 9425011223",
                isVerified = true,
                shirtColor = Color(0xFF7C3AED),
                hatColor = Color(0xFFFACC15),
                about = "Professional interior and exterior house painter, wall putty, texture painting, and waterproofing specialist."
            ),
            CustFeaturedWorkerProfile(
                id = "w_mahesh",
                name = "Mahesh Vishwakarma",
                category = "Carpenter",
                rating = 4.7,
                jobsDone = 19,
                area = "Bhopal Road, Raisen",
                stateName = "Madhya Pradesh",
                dailyRate = 850,
                experience = "5 years",
                phone = "+91 9893044556",
                isVerified = true,
                shirtColor = Color(0xFF083D91),
                hatColor = Color(0xFFFF8C00),
                about = "Expert Carpenter for doors, windows, modular kitchen cabinets, bed and furniture making & repair."
            ),
            CustFeaturedWorkerProfile(
                id = "w_vikas",
                name = "Vikas Ahirwar",
                category = "General Labour",
                rating = 4.8,
                jobsDone = 28,
                area = "Main Road, Silwani",
                stateName = "Madhya Pradesh",
                dailyRate = 600,
                experience = "4 years",
                phone = "+91 9109077889",
                isVerified = true,
                shirtColor = Color(0xFF16A34A),
                hatColor = Color(0xFFFACC15),
                about = "Hardworking General Labour for construction loading/unloading, site cleaning, digging, and material shifting."
            ),
            CustFeaturedWorkerProfile(
                id = "w_suresh",
                name = "Suresh Sen",
                category = "Tile Worker",
                rating = 4.9,
                jobsDone = 24,
                area = "New Colony, Raisen",
                stateName = "Madhya Pradesh",
                dailyRate = 900,
                experience = "6 years",
                phone = "+91 9630055443",
                isVerified = true,
                shirtColor = Color(0xFF1D4ED8),
                hatColor = Color(0xFFFF8C00),
                about = "Specialist in floor tiles, vitrified tiles, bathroom wall tiles, granite kitchen platform and marble fitting."
            ),
            CustFeaturedWorkerProfile(
                id = "w_dinesh",
                name = "Dinesh Kushwaha",
                category = "Farm Worker",
                rating = 4.7,
                jobsDone = 31,
                area = "Gram Bamhori, Silwani",
                stateName = "Madhya Pradesh",
                dailyRate = 550,
                experience = "7 years",
                phone = "+91 9300122334",
                isVerified = true,
                shirtColor = Color(0xFF15803D),
                hatColor = Color(0xFFFACC15),
                about = "Experienced agricultural & farm worker for harvesting, irrigation, spraying, and crop care."
            ),
            CustFeaturedWorkerProfile(
                id = "w_kamlesh",
                name = "Kamlesh Sahu",
                category = "Cleaner",
                rating = 4.8,
                jobsDone = 18,
                area = "Station Road, Raisen",
                stateName = "Madhya Pradesh",
                dailyRate = 500,
                experience = "3 years",
                phone = "+91 9713066778",
                isVerified = true,
                shirtColor = Color(0xFF0284C7),
                hatColor = Color(0xFFFACC15),
                about = "Full house deep cleaning, water tank cleaning, office cleaning, and post-construction cleanup."
            ),
            CustFeaturedWorkerProfile(
                id = "w_deepak",
                name = "Deepak प्रजापति",
                category = "Mason",
                rating = 4.8,
                jobsDone = 22,
                area = "Bus Stand, Silwani",
                stateName = "Madhya Pradesh",
                dailyRate = 800,
                experience = "5 years",
                phone = "+91 9827033445",
                isVerified = true,
                shirtColor = Color(0xFFEA580C),
                hatColor = Color(0xFFFACC15),
                about = "Skilled Mason for plaster work, boundary wall, elevation design, and house renovation."
            )
        )
    }

    val allWorkersList = remember { mutableStateListOf<CustFeaturedWorkerProfile>().apply { addAll(defaultFeaturedWorkers) } }

    LaunchedEffect(Unit) {
        fetchCustRegisteredWorkersFromFirebase { firebaseWorkers ->
            val merged = mutableListOf<CustFeaturedWorkerProfile>()
            merged.addAll(firebaseWorkers)
            defaultFeaturedWorkers.forEach { def ->
                if (merged.none { it.name.equals(def.name, ignoreCase = true) }) {
                    merged.add(def)
                }
            }
            allWorkersList.clear()
            allWorkersList.addAll(merged)
        }
    }

    val filteredWorkers = allWorkersList
        .filter { w ->
            val matchesCategory = selectedSkillFilter == "All" || w.category.equals(selectedSkillFilter, ignoreCase = true)
            val matchesLocation = if (!filterByActiveLocation || activeUserLocation.equals("All Locations", true) || activeUserLocation.isBlank()) {
                true
            } else {
                val tokens = activeUserLocation.split(",", " ").map { it.trim().lowercase(Locale.US) }.filter { it.length >= 3 }
                tokens.isEmpty() || tokens.any { tk ->
                    w.area.lowercase(Locale.US).contains(tk) || w.stateName.lowercase(Locale.US).contains(tk)
                }
            }
            matchesCategory && matchesLocation && matchesWorkerSmartSearch(w, localSearchText)
        }
        .let { list ->
            when (sortOption) {
                "LOW_RATE" -> list.sortedBy { it.dailyRate }
                "HIGH_RATE" -> list.sortedByDescending { it.dailyRate }
                "HIGH_RATING" -> list.sortedByDescending { it.rating }
                else -> list
            }
        }

    val openDirectLiveChatWithWorker: (CustFeaturedWorkerProfile) -> Unit = { worker ->
        chatPrefs.edit()
            .putString("chat_partner_id", worker.id)
            .putString("chat_partner_name", worker.name)
            .putString("chat_partner_role", worker.category)
            .putString("chat_partner_phone", worker.phone)
            .putString("chat_partner_area", worker.area)
            .apply()
        onOpenChat()
    }

    if (selectedWorkerForProfile != null) {
        val w = selectedWorkerForProfile!!
        CustWorkerFullProfileViewScreen(
            worker = w,
            isHindi = isHindi,
            onBack = { selectedWorkerForProfile = null },
            onCallClick = {
                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${w.phone}"))
                context.startActivity(dialIntent)
            },
            onMessageChatClick = {
                selectedWorkerForProfile = null
                openDirectLiveChatWithWorker(w)
            },
            onHireClick = {
                val target = w
                selectedWorkerForProfile = null
                workerForHireRequest = target
            }
        )
        return
    }

    Scaffold(
        containerColor = CustBgLight,
        bottomBar = {
            Surface(
                color = CustWhite,
                shadowElevation = 12.dp,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(vertical = 10.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CustBottomNavItem(
                        icon = Icons.Default.Menu,
                        label = if (isHindi) "मेनू" else "Menu",
                        isSelected = true,
                        onClick = {
                            selectedSkillFilter = "All"
                            localSearchText = ""
                            filterByActiveLocation = false
                        }
                    )
                    CustBottomNavItem(
                        icon = Icons.Outlined.History,
                        label = if (isHindi) "इतिहास" else "History",
                        isSelected = false,
                        onClick = { showHistoryDialog = true }
                    )
                    CustBottomNavItem(
                        icon = Icons.Default.AddCircle,
                        label = if (isHindi) "काम पोस्ट करें" else "Post Skills",
                        isSelected = false,
                        onClick = { showPostWorkDialog = true }
                    )
                    CustBottomNavItem(
                        icon = Icons.Outlined.Chat,
                        label = if (isHindi) "चैट" else "Chat",
                        isSelected = false,
                        onClick = onOpenChat
                    )
                    CustBottomNavItem(
                        icon = Icons.Outlined.Settings,
                        label = if (isHindi) "सेटिंग्स" else "Settings",
                        isSelected = false,
                        onClick = onOpenProfile
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CustNavyPrimary)
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Workora",
                                color = CustWhite,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = if (isHindi) "खोजें। काम दें। काम पाएं।" else "Find. Hire. Work.",
                                color = Color(0xFFCBD5E1),
                                fontSize = 12.sp
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            IconButton(onClick = onOpenNotifications) {
                                Icon(
                                    imageVector = Icons.Outlined.Notifications,
                                    contentDescription = "Notifications",
                                    tint = CustWhite
                                )
                            }
                            Surface(
                                color = CustOrangeAccent,
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.clickable { onSwitchRole() }
                            ) {
                                Text(
                                    text = if (isHindi) "वर्कर मोड" else "Worker Mode",
                                    color = CustWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // RESTORED TOP HEADER LIVE LOCATION BAR
                    Surface(
                        color = Color(0xFF1E40AF),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF3B82F6)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showLocationPickerDialog = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Location",
                                    tint = CustOrangeAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (activeUserLocation.isBlank() || activeUserLocation == "All Locations") {
                                        if (isHindi) "लोकेशन: सभी क्षेत्र (अपना शहर/गाँव चुनें)" else "Location: All Areas (Tap to set city/area)"
                                    } else {
                                        "📍 $activeUserLocation"
                                    },
                                    color = CustWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isHindi) "बदलें" else "Change",
                                    color = Color(0xFFFDE047),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = Color(0xFFFDE047),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = localSearchText,
                        onValueChange = { newText ->
                            localSearchText = newText
                            if (newText.isNotBlank() && selectedSkillFilter != "All") {
                                selectedSkillFilter = "All"
                            }
                            onSearchQueryChanged(newText)
                        },
                        placeholder = {
                            Text(
                                text = if (isHindi) "कारीगर या शहर खोजें..." else "Search for workers or location...",
                                fontSize = 13.sp,
                                color = CustSecondaryText
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Outlined.Search, contentDescription = null, tint = CustSecondaryText)
                        },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (localSearchText.isNotEmpty()) {
                                    IconButton(onClick = {
                                        localSearchText = ""
                                        onSearchQueryChanged("")
                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = CustSecondaryText)
                                    }
                                }
                                IconButton(onClick = { showSortFilterModal = true }) {
                                    Icon(Icons.Outlined.Tune, contentDescription = "Filter", tint = CustNavyPrimary)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CustWhite,
                            unfocusedContainerColor = CustWhite,
                            focusedBorderColor = CustOrangeAccent,
                            unfocusedBorderColor = CustWhite,
                            focusedTextColor = CustMainText,
                            unfocusedTextColor = CustMainText
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                val categories = listOf(
                    "All", "Mason", "Plumber", "Electrician", "Painter",
                    "Carpenter", "General Labour", "Tile Worker", "Cleaner", "Farm Worker"
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedSkillFilter.equals(cat, ignoreCase = true)
                        Surface(
                            color = if (isSelected) CustNavyPrimary else CustWhite,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, if (isSelected) CustNavyPrimary else CustBorder),
                            modifier = Modifier.clickable {
                                selectedSkillFilter = cat
                                if (localSearchText.isNotBlank()) {
                                    localSearchText = ""
                                    onSearchQueryChanged("")
                                }
                            }
                        ) {
                            Text(
                                text = translateCategoryLabel(cat, isHindi),
                                color = if (isSelected) CustWhite else CustMainText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            item {
                Surface(
                    color = Color(0xFFECFDF5),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF15803D)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = "Verified",
                                tint = CustWhite,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = if (isHindi) {
                                "सत्यापित कारीगर • सीधी कॉल और लाइव चैट • 0% कमीशन"
                            } else {
                                "Background checked • Direct Call & Live Chat • 0% Commission"
                            },
                            color = Color(0xFF166534),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 20.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isHindi) "प्रमुख कारीगर (${filteredWorkers.size})" else "Featured Workers (${filteredWorkers.size})",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CustMainText
                    )
                    Text(
                        text = if (isHindi) "सभी देखें >" else "See All >",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = CustNavyPrimary,
                        modifier = Modifier.clickable {
                            selectedSkillFilter = "All"
                            localSearchText = ""
                            filterByActiveLocation = false
                            sortOption = "DEFAULT"
                            onSearchQueryChanged("")
                        }
                    )
                }
            }

            if (filteredWorkers.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = CustWhite),
                        border = BorderStroke(1.dp, CustBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.SearchOff,
                                contentDescription = null,
                                tint = CustSecondaryText,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isHindi) "इस खोज या लोकेशन के लिए कोई कारीगर नहीं मिला" else "No workers found for this search or location",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = CustMainText
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    localSearchText = ""
                                    selectedSkillFilter = "All"
                                    filterByActiveLocation = false
                                    sortOption = "DEFAULT"
                                    onSearchQueryChanged("")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CustOrangeAccent)
                            ) {
                                Text(if (isHindi) "सभी कारीगर दिखाएं" else "Show All Workers", color = CustWhite, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(filteredWorkers, key = { it.id }) { worker ->
                    CustFeaturedWorkerCardItem(
                        worker = worker,
                        isHindi = isHindi,
                        onCardClick = {
                            selectedWorkerForProfile = worker
                        },
                        onCallNowClick = {
                            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${worker.phone}"))
                            context.startActivity(dialIntent)
                        },
                        onMessageIconClick = {
                            openDirectLiveChatWithWorker(worker)
                        }
                    )
                }
            }
        }
    }

    // =========================================================================
    // LIVE LOCATION PICKER DIALOG WITH REAL AUTO-SUGGESTIONS
    // =========================================================================
    if (showLocationPickerDialog) {
        var tempLocationInput by remember {
            mutableStateOf(if (activeUserLocation == "All Locations") "" else activeUserLocation)
        }

        Dialog(
            onDismissRequest = { showLocationPickerDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CustWhite)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = CustOrangeAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isHindi) "अपनी लोकेशन चुनें (Live Location)" else "Select Your Location",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CustMainText
                            )
                        }
                        IconButton(onClick = { showLocationPickerDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = CustSecondaryText)
                        }
                    }

                    Text(
                        text = if (isHindi) "शहर, तहसील या गाँव का नाम लिखें — नीचे अपने आप असली लोकेशन दिखेंगी:" else "Type your city, tehsil or village — real locations will suggest below:",
                        fontSize = 12.sp,
                        color = CustSecondaryText
                    )

                    LiveLocationAutoCompleteField(
                        value = tempLocationInput,
                        onValueChange = { tempLocationInput = it },
                        onLocationSelected = { fullLoc, areaPart, statePart ->
                            activeUserLocation = fullLoc
                            filterByActiveLocation = true
                            profilePrefs.edit()
                                .putString("user_location", areaPart)
                                .putString("user_state", statePart)
                                .apply()
                            Toast.makeText(context, "Location: $fullLoc ✓", Toast.LENGTH_SHORT).show()
                            showLocationPickerDialog = false
                        },
                        label = if (isHindi) "शहर / गाँव / एरिया खोजें" else "Search City / Village / Area",
                        placeholder = if (isHindi) "जैसे Silwani, Raisen, Bhopal, Indore..." else "e.g. Silwani, Raisen, Bhopal, Indore..."
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                activeUserLocation = "All Locations"
                                filterByActiveLocation = false
                                showLocationPickerDialog = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (isHindi) "सभी क्षेत्र (All)" else "All Locations", color = CustNavyPrimary, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                if (tempLocationInput.isNotBlank()) {
                                    activeUserLocation = tempLocationInput.trim()
                                    filterByActiveLocation = true
                                    profilePrefs.edit().putString("user_location", activeUserLocation).apply()
                                } else {
                                    activeUserLocation = "All Locations"
                                    filterByActiveLocation = false
                                }
                                showLocationPickerDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CustOrangeAccent),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (isHindi) "लोकेशन लागू करें" else "Apply Location", color = CustWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showSortFilterModal) {
        AlertDialog(
            onDismissRequest = { showSortFilterModal = false },
            containerColor = CustWhite,
            title = {
                Text(
                    text = if (isHindi) "कारीगर फ़िल्टर और सॉर्ट करें" else "Filter & Sort Workers",
                    fontWeight = FontWeight.Bold,
                    color = CustMainText
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val options = listOf(
                        "DEFAULT" to (if (isHindi) "डिफ़ॉल्ट (सभी प्रमुख कारीगर)" else "Default (Featured)"),
                        "LOW_RATE" to (if (isHindi) "कम रेट से अधिक रेट (Low to High ₹)" else "Daily Rate: Low to High"),
                        "HIGH_RATE" to (if (isHindi) "अधिक रेट से कम रेट (High to Low ₹)" else "Daily Rate: High to Low"),
                        "HIGH_RATING" to (if (isHindi) "सबसे अच्छी रेटिंग (Top Rated ★)" else "Highest Rating First (★)")
                    )
                    options.forEach { (key, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    sortOption = key
                                    showSortFilterModal = false
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = sortOption == key,
                                onClick = {
                                    sortOption = key
                                    showSortFilterModal = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label, fontSize = 14.sp, color = CustMainText, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        sortOption = "DEFAULT"
                        selectedSkillFilter = "All"
                        localSearchText = ""
                        filterByActiveLocation = false
                        showSortFilterModal = false
                    }
                ) {
                    Text(if (isHindi) "रीसेट करें" else "Reset All", color = CustOrangeAccent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSortFilterModal = false }) {
                    Text(if (isHindi) "बंद करें" else "Close", color = CustSecondaryText)
                }
            }
        )
    }

    // Hire Request Dialog (With Live Location Auto-Suggest & "अपना काम लिखे..")
    if (workerForHireRequest != null) {
        val targetWorker = workerForHireRequest!!
        var workDesc by remember { mutableStateOf("") }
        var workDate by remember {
            mutableStateOf(SimpleDateFormat("d MMM yyyy", Locale.US).format(Date()))
        }
        var workArea by remember {
            mutableStateOf(profilePrefs.getString("user_location", targetWorker.area) ?: targetWorker.area)
        }

        Dialog(
            onDismissRequest = { workerForHireRequest = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CustWhite)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CustNavyPrimary)
                            .padding(horizontal = 18.dp, vertical = 16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { workerForHireRequest = null },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = CustWhite)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isHindi) "बुकिंग रिक्वेस्ट — ${targetWorker.name}" else "Hire Request — ${targetWorker.name}",
                                color = CustWhite,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(if (isHindi) "काम का विवरण *" else "Work Description *", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CustMainText)
                        OutlinedTextField(
                            value = workDesc,
                            onValueChange = { workDesc = it },
                            placeholder = { Text("अपना काम लिखे..", color = CustSecondaryText) },
                            minLines = 3,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(if (isHindi) "तारीख *" else "Date *", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CustMainText)
                        OutlinedTextField(
                            value = workDate,
                            onValueChange = { workDate = it },
                            leadingIcon = { Icon(Icons.Outlined.CalendarToday, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(if (isHindi) "शहर / एरिया (Live Location) *" else "Area / Location *", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CustMainText)
                        LiveLocationAutoCompleteField(
                            value = workArea,
                            onValueChange = { workArea = it },
                            label = if (isHindi) "लोकेशन चुनें या लिखें *" else "Select or Type Location *"
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Button(
                            onClick = {
                                val finalDesc = workDesc.trim().ifEmpty { "${targetWorker.category} work request" }
                                onPostJob(
                                    "Hire ${targetWorker.name}",
                                    targetWorker.category,
                                    finalDesc,
                                    targetWorker.dailyRate,
                                    workArea.trim().ifEmpty { targetWorker.area },
                                    1,
                                    "NORMAL",
                                    workDate
                                )
                                Toast.makeText(
                                    context,
                                    if (isHindi) "${targetWorker.name} को काम की रिक्वेस्ट भेज दी गई! ✓" else "Hire Request Sent to ${targetWorker.name}! ✓",
                                    Toast.LENGTH_SHORT
                                ).show()
                                workerForHireRequest = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CustOrangeAccent),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(if (isHindi) "रिक्वेस्ट भेजें" else "Send Request", color = CustWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Post Work Dialog (With Live Location Auto-Suggest)
    if (showPostWorkDialog) {
        var title by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }
        var rate by remember { mutableStateOf("") }
        var area by remember { mutableStateOf(profilePrefs.getString("user_location", "") ?: "") }

        Dialog(
            onDismissRequest = { showPostWorkDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CustWhite)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (isHindi) "नया काम पोस्ट करें" else "Post New Work Request",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CustMainText
                    )
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(if (isHindi) "काम का नाम *" else "Work Title *") },
                        placeholder = { Text("अपना काम लिखे..", color = CustSecondaryText) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text(if (isHindi) "काम की श्रेणी *" else "Category *") },
                        placeholder = { Text("अपना काम लिखे..", color = CustSecondaryText) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = rate,
                        onValueChange = { rate = it.filter { c -> c.isDigit() } },
                        label = { Text(if (isHindi) "प्रतिदिन रेट (₹) *" else "Daily Rate (₹) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    LiveLocationAutoCompleteField(
                        value = area,
                        onValueChange = { area = it },
                        label = if (isHindi) "शहर / एरिया (Live Location) *" else "Work Area / City *"
                    )
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text(if (isHindi) "काम का विवरण" else "Work Details") },
                        placeholder = { Text("अपना काम लिखे..", color = CustSecondaryText) },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showPostWorkDialog = false }) {
                            Text(if (isHindi) "रद्द करें" else "Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (title.isNotBlank() && area.isNotBlank()) {
                                    onPostJob(
                                        title.trim(),
                                        category.trim().ifEmpty { "General" },
                                        desc.trim(),
                                        rate.toIntOrNull() ?: 600,
                                        area.trim(),
                                        1,
                                        "NORMAL",
                                        "09:00 AM"
                                    )
                                    showPostWorkDialog = false
                                } else {
                                    Toast.makeText(context, if (isHindi) "कृपया काम का नाम और लोकेशन भरें" else "Please fill Work Title and Location", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CustOrangeAccent)
                        ) {
                            Text(if (isHindi) "काम पोस्ट करें" else "Post Work", color = CustWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showHistoryDialog = false },
            containerColor = CustWhite,
            title = { Text(if (isHindi) "मेरी बुकिंग्स और इतिहास" else "My Bookings & Work History", fontWeight = FontWeight.Bold, color = CustMainText) },
            text = {
                Text(
                    text = if (isHindi) {
                        "किसी भी कारीगर के कार्ड पर क्लिक करके उसकी पूरी प्रोफाइल देखें, सीधे कॉल करें, लाइव चैट करें या काम की रिक्वेस्ट भेजें।"
                    } else {
                        "Click on any worker card to view their profile, call them directly, start a live chat, or send a hire request."
                    },
                    color = CustSecondaryText,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showHistoryDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CustNavyPrimary)
                ) {
                    Text(if (isHindi) "ठीक है" else "OK", color = CustWhite)
                }
            }
        )
    }
}

// =========================================================================
// WORKER CARD WITH DEDICATED LOCATION LINE (Never cuts off location!)
// =========================================================================
@Composable
internal fun CustFeaturedWorkerCardItem(
    worker: CustFeaturedWorkerProfile,
    isHindi: Boolean,
    onCardClick: () -> Unit,
    onCallNowClick: () -> Unit,
    onMessageIconClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onCardClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CustWhite),
        border = BorderStroke(1.dp, CustBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                if (worker.profilePhoto.isNotBlank()) {
                    UserProfilePhotoView(base64Photo = worker.profilePhoto, size = 64.dp)
                } else {
                    CustWorkerAvatarCanvas(
                        shirtColor = worker.shirtColor,
                        hatColor = worker.hatColor,
                        size = 64.dp
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = worker.name,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CustMainText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (worker.isVerified) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = Color(0xFF15803D),
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = translateCategoryLabel(worker.category, isHindi),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1D4ED8)
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (worker.rating > 0.0) {
                                "${worker.rating} (${worker.jobsDone} ${if (isHindi) "काम" else "jobs"})"
                            } else {
                                if (isHindi) "नया कारीगर" else "New Worker"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4B5563)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Dedicated full-width Location Row so location is always 100% visible
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = CustOrangeAccent,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = listOf(worker.area, worker.stateName).filter { it.isNotBlank() }.joinToString(", "),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CustSecondaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    color = CustRateBadgeBg,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, CustRateBadgeBorder)
                ) {
                    Text(
                        text = "₹${worker.dailyRate}/${if (isHindi) "दिन" else "day"}",
                        color = CustRateTextOrange,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onCallNowClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CustDarkNavyBtn,
                        contentColor = CustWhite
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call Now",
                        tint = CustWhite,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isHindi) "अभी कॉल करें (Call Now)" else "Call Now",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = CustWhite
                    )
                }

                Surface(
                    color = CustNavyPrimary,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { onMessageIconClick() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Chat,
                            contentDescription = "Live Chat Message",
                            tint = CustWhite,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun CustWorkerFullProfileViewScreen(
    worker: CustFeaturedWorkerProfile,
    isHindi: Boolean,
    onBack: () -> Unit,
    onCallClick: () -> Unit,
    onMessageChatClick: () -> Unit,
    onHireClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CustBgLight)
    ) {
        Surface(
            color = CustNavyPrimary,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = CustWhite
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (isHindi) "कारीगर प्रोफाइल" else "Worker Profile",
                    color = CustWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CustWhite),
                border = BorderStroke(1.dp, CustBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (worker.profilePhoto.isNotBlank()) {
                            UserProfilePhotoView(base64Photo = worker.profilePhoto, size = 76.dp)
                        } else {
                            CustWorkerAvatarCanvas(
                                shirtColor = worker.shirtColor,
                                hatColor = worker.hatColor,
                                size = 76.dp
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = worker.name,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = CustMainText
                                )
                                if (worker.isVerified) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified",
                                        tint = CustSuccessGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = translateCategoryLabel(worker.category, isHindi),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CustNavyPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (worker.rating > 0.0) {
                                        "${worker.rating} (${worker.jobsDone} ${if (isHindi) "रिव्यू" else "reviews"})"
                                    } else {
                                        if (isHindi) "नया कारीगर" else "New Worker"
                                    },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CustSecondaryText
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = CustBorder)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Outlined.WorkOutline, contentDescription = null, tint = CustNavyPrimary, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(worker.experience.ifBlank { "3 years" }, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CustMainText)
                            Text(if (isHindi) "अनुभव" else "Experience", fontSize = 12.sp, color = CustSecondaryText)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = CustNavyPrimary, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(worker.area, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CustMainText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(if (isHindi) "एरिया" else "Area", fontSize = 12.sp, color = CustSecondaryText)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Outlined.CurrencyRupee, contentDescription = null, tint = CustOrangeAccent, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("₹${worker.dailyRate}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = CustOrangeAccent)
                            Text(if (isHindi) "प्रतिदिन रेट" else "Daily Rate", fontSize = 12.sp, color = CustSecondaryText)
                        }
                    }
                }
            }

            val uploadedPhotos = listOf(worker.photo1, worker.photo2, worker.photo3).filter { it.isNotBlank() }
            if (uploadedPhotos.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CustWhite),
                    border = BorderStroke(1.dp, CustBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (isHindi) "काम के फोटो (Work Portfolio)" else "Work Portfolio Photos",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = CustMainText
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            uploadedPhotos.forEach { photoB64 ->
                                val bmp = remember(photoB64) { decodeBase64ToBitmap(photoB64) }
                                if (bmp != null) {
                                    androidx.compose.foundation.Image(
                                        bitmap = bmp,
                                        contentDescription = "Work Photo",
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(96.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CustWhite),
                border = BorderStroke(1.dp, CustBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isHindi) "कारीगर के बारे में" else "About",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = CustMainText
                    )
                    Text(
                        text = worker.about,
                        fontSize = 14.sp,
                        color = CustSecondaryText,
                        lineHeight = 21.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = CustBorder)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(if (isHindi) "मोबाइल नंबर" else "Phone Number", fontSize = 13.sp, color = CustSecondaryText)
                        Text(worker.phone, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CustMainText)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(if (isHindi) "राज्य (State)" else "State", fontSize = 13.sp, color = CustSecondaryText)
                        Text(worker.stateName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = CustMainText)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onCallClick,
                    colors = ButtonDefaults.buttonColors(containerColor = CustDarkNavyBtn),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, tint = CustWhite, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isHindi) "कॉल करें" else "Call Now", color = CustWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                Button(
                    onClick = onMessageChatClick,
                    colors = ButtonDefaults.buttonColors(containerColor = CustNavyPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Icon(Icons.Outlined.Chat, contentDescription = null, tint = CustWhite, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isHindi) "लाइव चैट" else "Live Chat", color = CustWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            Button(
                onClick = onHireClick,
                colors = ButtonDefaults.buttonColors(containerColor = CustOrangeAccent),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(
                    text = if (isHindi) "${worker.name} को काम पर रखें (Hire)" else "Hire ${worker.name}",
                    color = CustWhite,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
internal fun CustWorkerAvatarCanvas(
    shirtColor: Color,
    hatColor: Color,
    size: Dp = 64.dp
) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        drawCircle(
            color = Color(0xFFEFF6FF),
            radius = w * 0.5f,
            center = Offset(w * 0.5f, h * 0.5f)
        )

        drawArc(
            color = shirtColor,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(w * 0.16f, h * 0.62f),
            size = Size(w * 0.68f, h * 0.48f)
        )

        drawCircle(
            color = Color(0xFFFCD34D),
            radius = w * 0.21f,
            center = Offset(w * 0.5f, h * 0.45f)
        )

        drawArc(
            color = hatColor,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(w * 0.24f, h * 0.16f),
            size = Size(w * 0.52f, h * 0.34f)
        )

        drawLine(
            color = hatColor,
            start = Offset(w * 0.20f, h * 0.33f),
            end = Offset(w * 0.80f, h * 0.33f),
            strokeWidth = w * 0.065f,
            cap = StrokeCap.Round
        )
    }
}

@Composable
internal fun CustBottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val tint = if (isSelected) CustOrangeAccent else Color(0xFF475569)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
            color = tint
        )
    }
}

internal fun fetchCustRegisteredWorkersFromFirebase(
    onLoaded: (List<CustFeaturedWorkerProfile>) -> Unit
) {
    Thread {
        val list = mutableListOf<CustFeaturedWorkerProfile>()
        try {
            val conn = URL("$CUST_FB_URL/users.json").openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 5000
            if (conn.responseCode == 200) {
                val resp = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                if (resp.isNotBlank() && resp != "null" && resp.startsWith("{")) {
                    val root = JSONObject(resp)
                    val keys = root.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        val obj = root.optJSONObject(k) ?: continue
                        val roleRaw = obj.optString("role", "")
                        val status = obj.optString("status", "Active")
                        if ((roleRaw.equals("Worker", true) || roleRaw.equals("LABOUR", true)) &&
                            !status.equals("Blocked", true)
                        ) {
                            val name = obj.optString("name", "").trim()
                            val phone = obj.optString("phone", "").trim()
                            val skill = obj.optString("skill", "").trim()
                            val area = obj.optString("location", "").trim()
                            val state = obj.optString("state", "").trim()
                            val exp = obj.optString("experience", "").trim()
                            val rate = obj.optInt("dailyRate", 0)
                            val rating = obj.optDouble("rating", 0.0).let { if (it.isNaN()) 0.0 else it }
                            val jobsCount = obj.optInt("bookingsCount", 0)
                            val bio = obj.optString("bio", "").trim()
                            val profilePhoto = obj.optString("profilePhoto", "")
                            val p1 = obj.optString("photo1", "")
                            val p2 = obj.optString("photo2", "")
                            val p3 = obj.optString("photo3", "")

                            if (name.isNotEmpty() && skill.isNotEmpty() && rate > 0) {
                                list.add(
                                    CustFeaturedWorkerProfile(
                                        id = k,
                                        name = name,
                                        category = skill,
                                        rating = rating,
                                        jobsDone = jobsCount,
                                        area = area.ifEmpty { "Local Area" },
                                        stateName = state.ifEmpty { "Madhya Pradesh" },
                                        dailyRate = rate,
                                        experience = exp.ifEmpty { "1 yr" },
                                        phone = phone.ifEmpty { "+91 9876543210" },
                                        isVerified = true,
                                        shirtColor = Color(0xFF083D91),
                                        hatColor = Color(0xFFFF8C00),
                                        about = bio.ifEmpty { "Verified $skill from $area ($state) with $exp of experience on Workora." },
                                        profilePhoto = profilePhoto,
                                        photo1 = p1,
                                        photo2 = p2,
                                        photo3 = p3
                                    )
                                )
                            }
                        }
                    }
                }
            }
            conn.disconnect()
        } catch (_: Exception) {
        }
        Handler(Looper.getMainLooper()).post {
            onLoaded(list)
        }
    }.start()
}
