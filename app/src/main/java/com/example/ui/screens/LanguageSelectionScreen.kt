package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ui.theme.WorkoraTheme

@Composable
fun LanguageSelectionScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onLanguageSelected: (String) -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme
    
    // List of supported languages
    val languages = listOf(
        "English",
        "हिन्दी (Hindi)",
        "मराठी (Marathi)",
        "ગુજરાતી (Gujarati)",
        "தமிழ் (Tamil)",
        "తెలుగు (Telugu)"
    )

    // Default selected language
    var selectedLanguage by rememberSaveable { mutableStateOf("English") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
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
                text = "Select Language",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.onBackground
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Choose your preferred language for Workora",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Language List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(languages) { language ->
                    LanguageCard(
                        language = language,
                        isSelected = language == selectedLanguage,
                        onClick = { selectedLanguage = language }
                    )
                }
            }
        }

        // Save Button
        Box(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            Button(
                onClick = { onLanguageSelected(selectedLanguage) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Continue",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun LanguageCard(
    language: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(12.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (isSelected) colors.primaryContainer else colors.surface)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) colors.primary else colors.outlineVariant,
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = language,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) colors.onPrimaryContainer else colors.onSurface
        )
        
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Selected",
                tint = colors.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LanguageSelectionScreenPreview() {
    WorkoraTheme {
        LanguageSelectionScreen()
    }
}
