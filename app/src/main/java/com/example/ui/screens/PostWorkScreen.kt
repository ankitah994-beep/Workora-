package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.Job
import com.example.model.UserAccount
import com.example.ui.components.WorkoraToast
import com.example.ui.theme.WorkoraBgLight
import com.example.ui.theme.WorkoraBorder
import com.example.ui.theme.WorkoraNavy
import com.example.ui.theme.WorkoraNavyDark
import com.example.ui.theme.WorkoraNavySoft
import com.example.ui.theme.WorkoraOrange
import com.example.ui.theme.WorkoraOrangeSoft
import com.example.ui.theme.WorkoraSurface
import com.example.ui.theme.WorkoraTextDark
import com.example.ui.theme.WorkoraTextMuted
import com.example.ui.theme.WorkoraWarning
import com.example.viewmodel.PostWorkViewModel

/**
 * PostWorkScreen composable providing:
 * 1. Top App Bar: Title "Post a Job" with back navigation icon.
 * 2. Form Fields (Material 3 OutlinedTextFields):
 *    - Job Title (e.g., "Need a plumber for pipe repair")
 *    - Category (Mason, Plumber, Electrician, Carpenter)
 *    - Location/Address
 *    - Date & Time requirement
 *    - Description (multiline text field)
 * 3. Bottom Action: Large, full-width primary colored "Submit Job" button.
 * 4. State handled via PostWorkViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostWorkScreen(
    postWorkViewModel: PostWorkViewModel = viewModel(),
    currentUser: UserAccount? = null,
    onBack: () -> Unit,
    onSubmitJob: ((title: String, category: String, location: String, dateTime: String, description: String, rate: Int) -> Unit)? = null,
    onJobSubmittedSuccessfully: ((Job) -> Unit)? = null,
    toastMessage: String? = null,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    val title by postWorkViewModel.title.collectAsStateWithLifecycle()
    val category by postWorkViewModel.category.collectAsStateWithLifecycle()
    val location by postWorkViewModel.location.collectAsStateWithLifecycle()
    val dateTime by postWorkViewModel.dateTime.collectAsStateWithLifecycle()
    val description by postWorkViewModel.description.collectAsStateWithLifecycle()
    val dailyWage by postWorkViewModel.dailyWage.collectAsStateWithLifecycle()
    val isSubmitting by postWorkViewModel.isSubmitting.collectAsStateWithLifecycle()
    val errorMessage by postWorkViewModel.errorMessage.collectAsStateWithLifecycle()
    val successMessage by postWorkViewModel.successMessage.collectAsStateWithLifecycle()

    var localToast by remember { mutableStateOf<String?>(null) }
    var expandedDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser) {
        currentUser?.location?.let { postWorkViewModel.setInitialUserLocation(it) }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = WorkoraBgLight,
        topBar = {
            PostWorkTopAppBar(onBack = onBack)
        },
        bottomBar = {
            // Full-width primary colored "Submit Job" button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                colors = CardDefaults.cardColors(containerColor = WorkoraSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            val rate = dailyWage.toIntOrNull() ?: 800
                            val fallbackUid = currentUser?.id?.takeIf { it > 0 }?.toString() ?: ""
                            postWorkViewModel.submitJob(
                                fallbackCustomerId = fallbackUid,
                                onSuccess = { savedJob ->
                                    localToast = "Job posted successfully!"
                                    onSubmitJob?.invoke(
                                        savedJob.title,
                                        savedJob.category,
                                        savedJob.location,
                                        savedJob.date,
                                        savedJob.description,
                                        rate
                                    )
                                    onJobSubmittedSuccessfully?.invoke(savedJob)
                                },
                                onError = { error ->
                                    localToast = error
                                }
                            )
                        },
                        enabled = !isSubmitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_job_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WorkoraOrange,
                            contentColor = Color.White,
                            disabledContainerColor = WorkoraOrange.copy(alpha = 0.5f),
                            disabledContentColor = Color.White.copy(alpha = 0.8f)
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(22.dp)
                                    .testTag("post_work_loading_indicator"),
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Saving Job...",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Submit Job",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Screen Header Description Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = WorkoraNavySoft),
                border = BorderStroke(1.dp, WorkoraNavy.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(WorkoraNavy, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Work,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Hire Skilled Workers Fast",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = WorkoraNavyDark
                        )
                        Text(
                            text = "Fill in the details below to broadcast your requirement to verified workers.",
                            fontSize = 12.sp,
                            color = WorkoraTextMuted
                        )
                    }
                }
            }

            // Error Message Banner if validation failed
            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                errorMessage?.let { error ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                        border = BorderStroke(1.dp, Color(0xFFEF4444))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = "Error",
                                tint = Color(0xFFB91C1C),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = error,
                                fontSize = 13.sp,
                                color = Color(0xFFB91C1C),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // 1. Job Title Field
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Job Title *",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = WorkoraTextDark
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { postWorkViewModel.setTitle(it) },
                    placeholder = {
                        Text(
                            text = "e.g., Need a plumber for pipe repair",
                            fontSize = 14.sp,
                            color = WorkoraTextMuted
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Work,
                            contentDescription = null,
                            tint = WorkoraNavy,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = WorkoraSurface,
                        unfocusedContainerColor = WorkoraSurface,
                        focusedBorderColor = WorkoraNavy,
                        unfocusedBorderColor = WorkoraBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("job_title_input")
                )
            }

            // 2. Category Selector (Radio Buttons + Chips + Dropdown for maximum usability)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Category *",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = WorkoraTextDark
                )

                // Category Chips Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    postWorkViewModel.categories.forEach { cat ->
                        val isSelected = cat.equals(category, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { postWorkViewModel.setCategory(cat) },
                            label = {
                                Text(
                                    text = cat,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            } else null,
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = WorkoraNavy,
                                selectedLabelColor = Color.White,
                                containerColor = WorkoraSurface,
                                labelColor = WorkoraTextDark
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) WorkoraNavy else WorkoraBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("category_chip_${cat.lowercase()}")
                        )
                    }
                }

                // Exposed Dropdown Menu Box
                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = !expandedDropdown },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Selected Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Handyman,
                                contentDescription = null,
                                tint = WorkoraOrange,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = WorkoraSurface,
                            unfocusedContainerColor = WorkoraSurface,
                            focusedBorderColor = WorkoraNavy,
                            unfocusedBorderColor = WorkoraBorder
                        ),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("category_dropdown")
                    )
                    ExposedDropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        postWorkViewModel.categories.forEach { cat ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        RadioButton(
                                            selected = (cat == category),
                                            onClick = null,
                                            colors = RadioButtonDefaults.colors(selectedColor = WorkoraOrange)
                                        )
                                        Text(text = cat, fontWeight = FontWeight.Medium)
                                    }
                                },
                                onClick = {
                                    postWorkViewModel.setCategory(cat)
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            // 3. Location / Address Field
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Location / Address *",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = WorkoraTextDark
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { postWorkViewModel.setLocation(it) },
                    placeholder = {
                        Text(
                            text = "e.g., Sector 14, Gurugram",
                            fontSize = 14.sp,
                            color = WorkoraTextMuted
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = WorkoraNavy,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = WorkoraSurface,
                        unfocusedContainerColor = WorkoraSurface,
                        focusedBorderColor = WorkoraNavy,
                        unfocusedBorderColor = WorkoraBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("job_location_input")
                )
            }

            // 4. Date & Time Requirement Field
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Date & Time Requirement *",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = WorkoraTextDark
                )
                OutlinedTextField(
                    value = dateTime,
                    onValueChange = { postWorkViewModel.setDateTime(it) },
                    placeholder = {
                        Text(
                            text = "e.g., Today, 9:00 AM or Tomorrow morning",
                            fontSize = 14.sp,
                            color = WorkoraTextMuted
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = WorkoraNavy,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = WorkoraSurface,
                        unfocusedContainerColor = WorkoraSurface,
                        focusedBorderColor = WorkoraNavy,
                        unfocusedBorderColor = WorkoraBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("job_datetime_input")
                )
            }

            // Daily Wage Rate (Offered Compensation)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Offered Daily Wage (₹)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = WorkoraTextDark
                )
                OutlinedTextField(
                    value = dailyWage,
                    onValueChange = { postWorkViewModel.setDailyWage(it) },
                    placeholder = { Text("800") },
                    prefix = { Text("₹ ", fontWeight = FontWeight.Bold, color = WorkoraOrange) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = WorkoraSurface,
                        unfocusedContainerColor = WorkoraSurface,
                        focusedBorderColor = WorkoraNavy,
                        unfocusedBorderColor = WorkoraBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("job_rate_input")
                )
            }

            // 5. Description (Multiline text field)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Description & Details",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = WorkoraTextDark
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { postWorkViewModel.setDescription(it) },
                    placeholder = {
                        Text(
                            text = "Provide extra details like work scope, tools needed, building floor, or special instructions...",
                            fontSize = 14.sp,
                            color = WorkoraTextMuted
                        )
                    },
                    minLines = 4,
                    maxLines = 6,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = WorkoraSurface,
                        unfocusedContainerColor = WorkoraSurface,
                        focusedBorderColor = WorkoraNavy,
                        unfocusedBorderColor = WorkoraBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("job_description_input")
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    (localToast ?: toastMessage)?.let { msg ->
        WorkoraToast(message = msg)
    }
}

/**
 * Top App Bar with title "Post a Job" and back navigation icon.
 */
@Composable
fun PostWorkTopAppBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
        colors = CardDefaults.cardColors(containerColor = WorkoraNavy),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .testTag("post_work_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Post a Job",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.testTag("post_work_title_text")
            )
        }
    }
}
