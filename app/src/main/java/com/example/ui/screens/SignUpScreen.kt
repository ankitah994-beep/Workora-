package com.example.ui.screens

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val SignUpNavyPrimary = Color(0xFF083D91)
private val SignUpOrangeAccent = Color(0xFFFF8C00)
private val SignUpBgLight = Color(0xFFF8FAFC)
private val SignUpWhite = Color(0xFFFFFFFF)
private val SignUpMainText = Color(0xFF0B2345)
private val SignUpSecondaryText = Color(0xFF687280)
private val SignUpBorderColor = Color(0xFFE5EAF0)
private val SignUpDangerRed = Color(0xFFDC2626)

@Composable
fun SignUpScreen(
    onSignUp: (String, String, String, String) -> Unit = { _, _, _, _ -> },
    onNavigateToLogin: () -> Unit = {}
) {
    val context = LocalContext.current
    val authPrefs = remember { context.getSharedPreferences("workora_real_auth", Context.MODE_PRIVATE) }
    val profilePrefs = remember { context.getSharedPreferences("workora_real_profile", Context.MODE_PRIVATE) }

    LaunchedEffect(Unit) {
        AppLanguageManager.init(context)
    }
    val selectedLanguage = AppLanguageManager.currentLanguage
    val isHindi = selectedLanguage.equals("Hindi", ignoreCase = true) || selectedLanguage.contains("हिंदी")

    var selectedRole by remember {
        val initial = authPrefs.getString("saved_user_role", "CUSTOMER") ?: "CUSTOMER"
        mutableStateOf(if (initial.equals("LABOUR", true) || initial.equals("Worker", true)) "LABOUR" else "CUSTOMER")
    }
    val isWorkerRole = selectedRole == "LABOUR"

    // All Registration Fields Start Empty (No Automatic Dummy Data)
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var emailAddress by remember { mutableStateOf("") }
    var stateName by remember { mutableStateOf("") }
    var areaLocation by remember { mutableStateOf("") }
    var fullAddress by remember { mutableStateOf("") }

    // Worker / Skill Fields
    var workerCategory by remember { mutableStateOf("") }
    var workerExperienceYears by remember { mutableStateOf("") }
    var workerDailyRate by remember { mutableStateOf("") }
    var userBio by remember { mutableStateOf("") }

    // Profile Photo + 3 Sample Work Photos Upload State
    var profilePhotoBase64 by remember { mutableStateOf("") }
    var photo1Base64 by remember { mutableStateOf("") }
    var photo2Base64 by remember { mutableStateOf("") }
    var photo3Base64 by remember { mutableStateOf("") }

    // 0 = Profile Photo, 1 = Photo 1, 2 = Photo 2, 3 = Photo 3
    var activeUploadSlot by remember { mutableStateOf(0) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val encoded = encodeImageUriToBase64(context, uri)
            if (!encoded.isNullOrBlank()) {
                when (activeUploadSlot) {
                    0 -> profilePhotoBase64 = encoded
                    1 -> photo1Base64 = encoded
                    2 -> photo2Base64 = encoded
                    3 -> photo3Base64 = encoded
                }
                Toast.makeText(
                    context,
                    if (isHindi) "फोटो अपलोड हो गई ✓" else "Photo uploaded ✓",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isRegistering by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val availableCategories = listOf(
        "Mason", "General Labour", "Painter", "Electrician",
        "Plumber", "Carpenter", "Cleaner", "Farm Worker", "Tile Worker"
    )

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = SignUpMainText,
        unfocusedTextColor = SignUpMainText,
        focusedBorderColor = SignUpNavyPrimary,
        unfocusedBorderColor = SignUpBorderColor,
        focusedContainerColor = SignUpBgLight,
        unfocusedContainerColor = SignUpBgLight
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SignUpBgLight)
    ) {
        SignUpBottomWaveCanvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .align(Alignment.BottomCenter)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            SignUpScreenHelmetLogo(size = 68.dp)

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Workora",
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SignUpNavyPrimary,
                textAlign = TextAlign.Center
            )

            Text(
                text = if (isHindi) "खोजें। काम दें। काम पाएं।" else "Find. Hire. Work.",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = SignUpMainText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Login | Register Tab Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToLogin() },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isHindi) "लॉगिन (Login)" else "Login",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = SignUpSecondaryText,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(SignUpBorderColor)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isHindi) "रजिस्टर (Register)" else "Register",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SignUpMainText,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(SignUpNavyPrimary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Complete Registration Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SignUpWhite),
                border = BorderStroke(1.dp, SignUpBorderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (isHindi) "अकाउंट का प्रकार चुनें *" else "Select Account Type *",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SignUpMainText
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            color = if (!isWorkerRole) SignUpNavyPrimary else SignUpBgLight,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (!isWorkerRole) SignUpNavyPrimary else SignUpBorderColor),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedRole = "CUSTOMER"
                                    authPrefs.edit().putString("saved_user_role", "CUSTOMER").apply()
                                    errorMessage = null
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = null,
                                    tint = if (!isWorkerRole) SignUpWhite else SignUpMainText,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isHindi) "ग्राहक (Customer)" else "Customer (Hire)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!isWorkerRole) SignUpWhite else SignUpMainText
                                )
                            }
                        }

                        Surface(
                            color = if (isWorkerRole) SignUpOrangeAccent else SignUpBgLight,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (isWorkerRole) SignUpOrangeAccent else SignUpBorderColor),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedRole = "LABOUR"
                                    authPrefs.edit().putString("saved_user_role", "LABOUR").apply()
                                    errorMessage = null
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Construction,
                                    contentDescription = null,
                                    tint = if (isWorkerRole) SignUpWhite else SignUpMainText,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isHindi) "कारीगर (Worker)" else "Worker (Work)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isWorkerRole) SignUpWhite else SignUpMainText
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = SignUpBorderColor)

                    // =====================================================
                    // PROFILE PHOTO + 3 SAMPLE PHOTOS UPLOAD ("इमेज डाले")
                    // =====================================================
                    Text(
                        text = if (isHindi) "प्रोफाइल फोटो और काम की 3 सैंपल फोटो" else "Profile Photo & 3 Work Sample Photos",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SignUpNavyPrimary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.BottomEnd,
                            modifier = Modifier.clickable {
                                activeUploadSlot = 0
                                imagePickerLauncher.launch("image/*")
                            }
                        ) {
                            UserProfilePhotoView(
                                base64Photo = profilePhotoBase64,
                                size = 72.dp
                            )
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(SignUpOrangeAccent)
                                    .border(2.dp, SignUpWhite, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Upload Profile Photo",
                                    tint = SignUpWhite,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isHindi) "आपकी प्रोफाइल फोटो" else "Your Profile Photo",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SignUpMainText
                            )
                            Text(
                                text = if (isHindi) "गैलरी से अपनी साफ फोटो चुनें" else "Select a clear photo from your gallery",
                                fontSize = 11.sp,
                                color = SignUpSecondaryText
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedButton(
                                onClick = {
                                    activeUploadSlot = 0
                                    imagePickerLauncher.launch("image/*")
                                },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, SignUpNavyPrimary),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AddAPhoto,
                                    contentDescription = null,
                                    tint = SignUpNavyPrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isHindi) "प्रोफाइल फोटो चुनें" else "Choose Profile Photo",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SignUpNavyPrimary
                                )
                            }
                        }
                    }

                    Text(
                        text = if (isHindi) "काम की सैंपल फोटो डालने के लिए नीचे बॉक्स पर टैप करें:" else "Tap the boxes below to add 3 sample photos:",
                        fontSize = 12.sp,
                        color = SignUpSecondaryText
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ThreePhotoUploadBox(
                            label = if (isHindi) "फोटो 1" else "Photo 1",
                            base64Data = photo1Base64,
                            onPickClick = {
                                activeUploadSlot = 1
                                imagePickerLauncher.launch("image/*")
                            },
                            onRemoveClick = { photo1Base64 = "" },
                            modifier = Modifier.weight(1f)
                        )
                        ThreePhotoUploadBox(
                            label = if (isHindi) "फोटो 2" else "Photo 2",
                            base64Data = photo2Base64,
                            onPickClick = {
                                activeUploadSlot = 2
                                imagePickerLauncher.launch("image/*")
                            },
                            onRemoveClick = { photo2Base64 = "" },
                            modifier = Modifier.weight(1f)
                        )
                        ThreePhotoUploadBox(
                            label = if (isHindi) "फोटो 3" else "Photo 3",
                            base64Data = photo3Base64,
                            onPickClick = {
                                activeUploadSlot = 3
                                imagePickerLauncher.launch("image/*")
                            },
                            onRemoveClick = { photo3Base64 = "" },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HorizontalDivider(color = SignUpBorderColor)

                    // =====================================================
                    // PERSONAL & LIVE LOCATION DETAILS
                    // =====================================================
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            errorMessage = null
                        },
                        label = { Text(if (isHindi) "पूरा नाम (Full Name) *" else "Full Name *") },
                        placeholder = { Text(if (isHindi) "अपना पूरा नाम लिखें" else "Enter your full name", color = SignUpSecondaryText, fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Person, contentDescription = null, tint = SignUpSecondaryText, modifier = Modifier.size(20.dp))
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { input ->
                            phoneNumber = input.filter { it.isDigit() || it == '+' }.take(13)
                            errorMessage = null
                        },
                        label = { Text(if (isHindi) "मोबाइल नंबर (Phone Number) *" else "Phone Number *") },
                        placeholder = { Text(if (isHindi) "10 अंकों का मोबाइल नंबर" else "Enter 10-digit mobile number", color = SignUpSecondaryText, fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Phone, contentDescription = null, tint = SignUpSecondaryText, modifier = Modifier.size(20.dp))
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = emailAddress,
                        onValueChange = {
                            emailAddress = it
                            errorMessage = null
                        },
                        label = { Text(if (isHindi) "ईमेल पता (Email Address) *" else "Email Address *") },
                        placeholder = { Text(if (isHindi) "अपना ईमेल पता लिखें" else "Enter your email address", color = SignUpSecondaryText, fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Email, contentDescription = null, tint = SignUpSecondaryText, modifier = Modifier.size(20.dp))
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // LIVE REAL LOCATION AUTO-SUGGESTION FIELD FOR CITY / AREA & STATE
                    LiveLocationAutoCompleteField(
                        value = areaLocation,
                        onValueChange = {
                            areaLocation = it
                            errorMessage = null
                        },
                        onLocationSelected = { _, areaPart, statePart ->
                            areaLocation = areaPart
                            stateName = statePart
                            errorMessage = null
                        },
                        label = if (isHindi) "शहर / गाँव / एरिया (Live Location) *" else "City / Area / Village (Live Location) *",
                        placeholder = if (isHindi) "शहर या गाँव लिखें (नीचे असली लोकेशन दिखेंगी)" else "Type city or village for live suggestions"
                    )

                    OutlinedTextField(
                        value = stateName,
                        onValueChange = {
                            stateName = it
                            errorMessage = null
                        },
                        label = { Text(if (isHindi) "राज्य (State) *" else "State (राज्य) *") },
                        placeholder = { Text(if (isHindi) "अपने राज्य का नाम" else "Enter your state", color = SignUpSecondaryText, fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Map, contentDescription = null, tint = SignUpSecondaryText, modifier = Modifier.size(20.dp))
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = fullAddress,
                        onValueChange = {
                            fullAddress = it
                            errorMessage = null
                        },
                        label = { Text(if (isHindi) "पूरा पता / लैंडमार्क (Full Address)" else "Full Address / Landmark") },
                        placeholder = { Text(if (isHindi) "वार्ड, कॉलोनी या नजदीकी स्थान" else "Ward, colony or nearby landmark", color = SignUpSecondaryText, fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Home, contentDescription = null, tint = SignUpSecondaryText, modifier = Modifier.size(20.dp))
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // =====================================================
                    // WORKER SPECIFIC MANDATORY DETAILS ("अपना काम लिखे..")
                    // =====================================================
                    if (isWorkerRole) {
                        HorizontalDivider(color = SignUpBorderColor)

                        Text(
                            text = if (isHindi) "कारीगर के काम की जानकारी *" else "Worker Work Details (काम की जानकारी) *",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SignUpNavyPrimary
                        )

                        Text(
                            text = if (isHindi) "अपनी काम की श्रेणी चुनें या नीचे लिखें:" else "Tap to select your Category / Skill or type below:",
                            fontSize = 11.sp,
                            color = SignUpSecondaryText
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            availableCategories.forEach { cat ->
                                val isSelected = workerCategory.equals(cat, ignoreCase = true)
                                Surface(
                                    color = if (isSelected) SignUpNavyPrimary else SignUpBgLight,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, if (isSelected) SignUpNavyPrimary else SignUpBorderColor),
                                    modifier = Modifier.clickable {
                                        workerCategory = cat
                                        errorMessage = null
                                    }
                                ) {
                                    Text(
                                        text = cat,
                                        color = if (isSelected) SignUpWhite else SignUpMainText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = workerCategory,
                            onValueChange = {
                                workerCategory = it
                                errorMessage = null
                            },
                            label = { Text(if (isHindi) "काम की श्रेणी (Work Category / Skill) *" else "Work Category / Skill (काम की श्रेणी) *") },
                            placeholder = { Text("अपना काम लिखे..", color = SignUpSecondaryText, fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(Icons.Outlined.Construction, contentDescription = null, tint = SignUpSecondaryText, modifier = Modifier.size(20.dp))
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = workerExperienceYears,
                                onValueChange = { input ->
                                    workerExperienceYears = input.filter { it.isDigit() }.take(2)
                                    errorMessage = null
                                },
                                label = { Text(if (isHindi) "अनुभव (वर्षों में) *" else "Experience (Yrs) *") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = fieldColors,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = workerDailyRate,
                                onValueChange = { input ->
                                    workerDailyRate = input.filter { it.isDigit() }.take(5)
                                    errorMessage = null
                                },
                                label = { Text(if (isHindi) "प्रतिदिन रेट (₹) *" else "Daily Rate (₹) *") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = fieldColors,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = userBio,
                            onValueChange = {
                                userBio = it
                                errorMessage = null
                            },
                            label = { Text(if (isHindi) "काम का विवरण (Work Description)" else "Work Description / Bio") },
                            placeholder = { Text("अपना काम लिखे..", color = SignUpSecondaryText, fontSize = 13.sp) },
                            minLines = 2,
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Password & Confirm Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        label = { Text(if (isHindi) "पासवर्ड बनाएं (Create Password) *" else "Create Password *") },
                        placeholder = { Text(if (isHindi) "कम से कम 6 अक्षर" else "Minimum 6 characters", color = SignUpSecondaryText, fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Lock, contentDescription = null, tint = SignUpSecondaryText, modifier = Modifier.size(20.dp))
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    contentDescription = "Toggle Password",
                                    tint = SignUpSecondaryText,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            errorMessage = null
                        },
                        label = { Text(if (isHindi) "पासवर्ड कन्फर्म करें (Confirm Password) *" else "Confirm Password *") },
                        placeholder = { Text(if (isHindi) "पासवर्ड दोबारा लिखें" else "Re-enter your password", color = SignUpSecondaryText, fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Lock, contentDescription = null, tint = SignUpSecondaryText, modifier = Modifier.size(20.dp))
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (!errorMessage.isNullOrBlank()) {
                        Text(
                            text = errorMessage!!,
                            color = SignUpDangerRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            val cleanName = fullName.trim()
                            val cleanPhone = phoneNumber.trim()
                            val digitsInPhone = cleanPhone.filter { it.isDigit() }
                            val cleanEmail = emailAddress.trim().lowercase(Locale.US)
                            val cleanArea = areaLocation.trim()
                            val cleanState = stateName.trim().ifEmpty { "Madhya Pradesh" }
                            val cleanAddress = fullAddress.trim()
                            val cleanCategory = workerCategory.trim()
                            val cleanExp = workerExperienceYears.trim()
                            val cleanRate = workerDailyRate.trim()
                            val cleanBio = userBio.trim()
                            val cleanPass = password.trim()
                            val cleanConfirm = confirmPassword.trim()

                            when {
                                cleanName.isEmpty() -> {
                                    errorMessage = "कृपया अपना पूरा नाम (Full Name) भरें।"
                                }
                                digitsInPhone.length < 10 -> {
                                    errorMessage = "कृपया सही 10 अंकों का मोबाइल नंबर (Phone Number) भरें।"
                                }
                                cleanEmail.isEmpty() || !cleanEmail.contains("@") -> {
                                    errorMessage = "कृपया अपना सही ईमेल पता (Email Address) भरें।"
                                }
                                cleanArea.isEmpty() -> {
                                    errorMessage = "कृपया अपने शहर / गाँव / एरिया (City / Area) का नाम भरें।"
                                }
                                isWorkerRole && cleanCategory.isEmpty() -> {
                                    errorMessage = "कृपया अपने काम की श्रेणी (Work Category / Skill) चुनें या लिखें।"
                                }
                                isWorkerRole && cleanExp.isEmpty() -> {
                                    errorMessage = "कृपया अपना काम का अनुभव (Experience in Years) भरें।"
                                }
                                isWorkerRole && (cleanRate.isEmpty() || (cleanRate.toIntOrNull() ?: 0) <= 0) -> {
                                    errorMessage = "कृपया अपनी प्रतिदिन की मजदूरी (Daily Rate ₹) भरें।"
                                }
                                cleanPass.length < 6 -> {
                                    errorMessage = "पासवर्ड कम से कम 6 अक्षरों का होना चाहिए।"
                                }
                                cleanPass != cleanConfirm -> {
                                    errorMessage = "दोनों पासवर्ड एक समान नहीं हैं (Passwords do not match)।"
                                }
                                else -> {
                                    isRegistering = true
                                    val formattedExp = if (isWorkerRole) "$cleanExp yrs" else ""
                                    val parsedRate = if (isWorkerRole) (cleanRate.toIntOrNull() ?: 0) else 0
                                    val finalSkill = if (isWorkerRole) cleanCategory else "Customer"

                                    profilePrefs.edit()
                                        .putString("user_name", cleanName)
                                        .putString("user_phone", cleanPhone)
                                        .putString("user_state", cleanState)
                                        .putString("user_location", cleanArea)
                                        .putString("user_address", cleanAddress)
                                        .putString("user_skill", finalSkill)
                                        .putString("user_experience", formattedExp)
                                        .putString("user_rate", if (parsedRate > 0) parsedRate.toString() else "")
                                        .putString("user_bio", cleanBio)
                                        .putString("user_profile_photo", profilePhotoBase64)
                                        .putString("user_photo_1", photo1Base64)
                                        .putString("user_photo_2", photo2Base64)
                                        .putString("user_photo_3", photo3Base64)
                                        .apply()

                                    authPrefs.edit()
                                        .putBoolean("is_logged_in", true)
                                        .putString("last_logged_in_email", cleanEmail)
                                        .putString("saved_user_role", selectedRole)
                                        .putString("saved_password_$cleanEmail", cleanPass)
                                        .apply()

                                    registerNewUserWithPhotosToFirebase(
                                        name = cleanName,
                                        phone = cleanPhone,
                                        email = cleanEmail,
                                        state = cleanState,
                                        area = cleanArea,
                                        address = cleanAddress,
                                        role = selectedRole,
                                        skill = finalSkill,
                                        experience = formattedExp,
                                        dailyRate = parsedRate,
                                        bio = cleanBio,
                                        profilePhoto = profilePhotoBase64,
                                        photo1 = photo1Base64,
                                        photo2 = photo2Base64,
                                        photo3 = photo3Base64,
                                        password = cleanPass
                                    ) {
                                        isRegistering = false
                                        Toast.makeText(
                                            context,
                                            if (isHindi) "रजिस्ट्रेशन सफलतापूर्वक पूरा हुआ! ✓" else "Registration Successful! ✓",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        onSignUp(cleanName, cleanEmail, cleanPhone, cleanPass)
                                    }
                                }
                            }
                        },
                        enabled = !isRegistering,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SignUpOrangeAccent,
                            contentColor = SignUpWhite
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        if (isRegistering) {
                            CircularProgressIndicator(
                                color = SignUpWhite,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isHindi) "आपकी जानकारी सेव हो रही है..." else "Saving Your Details...",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = if (isHindi) "रजिस्ट्रेशन पूरा करें (Complete Registration)" else "Complete Registration",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isHindi) "पहले से अकाउंट है? " else "Already have an account? ",
                            fontSize = 13.sp,
                            color = SignUpSecondaryText
                        )
                        Text(
                            text = if (isHindi) "लॉगिन करें" else "Login",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SignUpOrangeAccent,
                            modifier = Modifier.clickable { onNavigateToLogin() }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(170.dp))
        }
    }
}

@Composable
private fun SignUpScreenHelmetLogo(size: Dp = 72.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        drawArc(
            color = SignUpOrangeAccent,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(w * 0.10f, h * 0.18f),
            size = Size(w * 0.80f, h * 0.82f)
        )

        drawRoundRect(
            color = SignUpOrangeAccent,
            topLeft = Offset(w * 0.43f, h * 0.11f),
            size = Size(w * 0.14f, h * 0.15f),
            cornerRadius = CornerRadius(w * 0.04f, w * 0.04f)
        )

        drawRoundRect(
            color = SignUpBgLight,
            topLeft = Offset(w * 0.37f, h * 0.22f),
            size = Size(w * 0.045f, h * 0.22f),
            cornerRadius = CornerRadius(4f, 4f)
        )
        drawRoundRect(
            color = SignUpBgLight,
            topLeft = Offset(w * 0.585f, h * 0.22f),
            size = Size(w * 0.045f, h * 0.22f),
            cornerRadius = CornerRadius(4f, 4f)
        )

        drawRoundRect(
            color = SignUpOrangeAccent,
            topLeft = Offset(w * 0.03f, h * 0.56f),
            size = Size(w * 0.94f, h * 0.11f),
            cornerRadius = CornerRadius(w * 0.06f, w * 0.06f)
        )
    }
}

@Composable
private fun SignUpBottomWaveCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val orangeWavePath = Path().apply {
            moveTo(w * 0.34f, h * 0.66f)
            cubicTo(
                w * 0.58f, h * 0.60f,
                w * 0.78f, h * 0.16f,
                w, h * 0.24f
            )
            lineTo(w, h)
            lineTo(w * 0.34f, h)
            close()
        }
        drawPath(
            path = orangeWavePath,
            color = SignUpOrangeAccent
        )

        val navyWavePath = Path().apply {
            moveTo(0f, h * 0.36f)
            cubicTo(
                w * 0.28f, h * 0.08f,
                w * 0.55f, h * 0.82f,
                w, h * 0.40f
            )
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = navyWavePath,
            color = SignUpNavyPrimary
        )
    }
}

private fun formatSignUpSafeKey(identifier: String): String {
    return identifier.trim().lowercase(Locale.US)
        .replace(".", "_")
        .replace("@", "_at_")
        .replace("+", "")
        .replace(" ", "")
}

private fun registerNewUserWithPhotosToFirebase(
    name: String,
    phone: String,
    email: String,
    state: String,
    area: String,
    address: String,
    role: String,
    skill: String,
    experience: String,
    dailyRate: Int,
    bio: String,
    profilePhoto: String,
    photo1: String,
    photo2: String,
    photo3: String,
    password: String,
    onDone: () -> Unit
) {
    Thread {
        try {
            val key = formatSignUpSafeKey(email)
            val url = URL("https://workora-d8b51-default-rtdb.firebaseio.com/users/$key.json")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "PUT"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            val payload = JSONObject().apply {
                put("name", name)
                put("phone", phone)
                put("email", email)
                put("state", state)
                put("location", area)
                put("address", address)
                put("role", if (role.equals("LABOUR", true)) "Worker" else "Customer")
                put("skill", skill)
                put("experience", experience)
                put("dailyRate", dailyRate)
                put("bio", bio)
                put("profilePhoto", profilePhoto)
                put("photo1", photo1)
                put("photo2", photo2)
                put("photo3", photo3)
                put("rating", 0.0)
                put("bookingsCount", 0)
                put("password", password)
                put("status", "Active")
                put("availability", "Available")
                put("joined", SimpleDateFormat("d MMM yyyy", Locale.US).format(Date()))
            }
            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
            conn.responseCode
            conn.disconnect()
        } catch (_: Exception) {
        }
        Handler(Looper.getMainLooper()).post {
            onDone()
        }
    }.start()
}
