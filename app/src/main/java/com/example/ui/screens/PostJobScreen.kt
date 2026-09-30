package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WorkoraTheme

// New Premium Colors
private val WorkoraBlue = Color(0xFF0061FF)
private val TextDark = Color(0xFF0F172A)
private val TextGray = Color(0xFF64748B)
private val BorderGray = Color(0xFFE2E8F0)
private val BgColor = Color.White

@Composable
fun PostJobScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onSubmit: () -> Unit = {} 
) {
    var jobTitle by rememberSaveable { mutableStateOf("") }
    var jobDescription by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("Labour") } // New UI addition
    var date by rememberSaveable { mutableStateOf("") }
    var time by rememberSaveable { mutableStateOf("") }
    var wage by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }

    val categories = listOf("Labour", "Carpenter", "Electrician", "Plumber", "Painter", "Mason", "Cleaner")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = WorkoraBlue,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onBack() }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Post a Job",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark
            )
        }

        // Scrollable Form
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "What kind of work do you need?",
                fontSize = 15.sp,
                color = TextGray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Category Selection (Premium Touch)
            Text(text = "Category", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories.size) { index ->
                    val cat = categories[index]
                    val isSelected = selectedCategory == cat
                    OutlinedButton(
                        onClick = { selectedCategory = cat },
                        modifier = Modifier.height(36.dp),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, if (isSelected) WorkoraBlue else BorderGray),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected) WorkoraBlue else Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else TextGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            JobInputField(
                value = jobTitle,
                onValueChange = { jobTitle = it },
                label = "Job Title (e.g. Wall Plastering)",
                keyboardType = KeyboardType.Text
            )

            JobInputField(
                value = jobDescription,
                onValueChange = { jobDescription = it },
                label = "Job Description & Requirements",
                keyboardType = KeyboardType.Text,
                singleLine = false,
                modifier = Modifier.height(120.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                JobInputField(
                    value = date,
                    onValueChange = { date = it },
                    label = "Date (e.g. Tomorrow)",
                    modifier = Modifier.weight(1f)
                )
                JobInputField(
                    value = time,
                    onValueChange = { time = it },
                    label = "Time (e.g. 9:00 AM)",
                    modifier = Modifier.weight(1f)
                )
            }

            JobInputField(
                value = wage,
                onValueChange = { wage = it.filter { c -> c.isDigit() } }, // Safe number parsing
                label = "Offered Wage (₹ / day)",
                keyboardType = KeyboardType.Number
            )

            JobInputField(
                value = location,
                onValueChange = { location = it },
                label = "Work Location (Address)",
                keyboardType = KeyboardType.Text
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Submit Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WorkoraBlue)
            ) {
                Text(
                    text = "Publish Job",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun JobInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextGray) },
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = ImeAction.Next
        ),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextDark,
            unfocusedTextColor = TextDark,
            focusedBorderColor = WorkoraBlue,
            unfocusedBorderColor = BorderGray,
            focusedLabelColor = WorkoraBlue,
            unfocusedLabelColor = TextGray
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun PostJobScreenPreview() {
    WorkoraTheme {
        PostJobScreen()
    }
}
