package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ui.theme.WorkoraTheme

@Composable
fun PostJobScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onSubmit: () -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme

    var jobTitle by rememberSaveable { mutableStateOf("") }
    var jobDescription by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf("") }
    var time by rememberSaveable { mutableStateOf("") }
    var wage by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Top Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.onBackground)
            }
            Text(
                text = "Post a Job",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.onBackground
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
                text = "Enter Job Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onBackground
            )

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
                onValueChange = { wage = it },
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
        Box(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Post Job Request",
                    style = MaterialTheme.typography.titleMedium
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
    val colors = MaterialTheme.colorScheme

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = ImeAction.Next
        ),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = colors.onBackground,
            unfocusedTextColor = colors.onBackground,
            focusedBorderColor = colors.primary,
            unfocusedBorderColor = colors.outline
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
