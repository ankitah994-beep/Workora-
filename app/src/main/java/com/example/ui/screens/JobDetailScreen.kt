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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ui.theme.WorkoraTheme

@Composable
fun JobDetailScreen(
    modifier: Modifier = Modifier,
    title: String = "Construction Helper Needed",
    description: String = "Looking for an experienced helper for a 5-day construction project. Must have basic tools and be punctual. Payment will be processed daily.",
    location: String = "Silwani, MP",
    payRate: String = "₹500 / day",
    category: String = "Construction",
    postedBy: String = "Ramesh Kumar",
    date: String = "Today",
    isUrgent: Boolean = true,
    hasApplied: Boolean = false,
    isLoading: Boolean = false,
    onBack: () -> Unit = {},
    onApply: () -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme

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
                text = "Job Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.onBackground
            )
        }

        // Scrollable Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            if (isUrgent) {
                Surface(
                    color = colors.errorContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "URGENT",
                        color = colors.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = colors.onBackground,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Info Chips Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoChip(icon = Icons.Default.LocationOn, text = location, modifier = Modifier.weight(1f))
                InfoChip(icon = Icons.Default.Person, text = category, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(20.dp))

            // Pay Rate Card
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.primaryContainer),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Pay Rate", style = MaterialTheme.typography.titleMedium, color = colors.onPrimaryContainer)
                    Text(payRate, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = colors.primary)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            // Description
            Text("Job Description", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colors.onBackground)
            Spacer(modifier = Modifier.height(8.dp))
            Text(description, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)

            Spacer(modifier = Modifier.height(24.dp))
            
            // Employer Info
            Text("Employer Info", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colors.onBackground)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Posted by: $postedBy", style = MaterialTheme.typography.bodyMedium, color = colors.onSurface)
            Text("Posted on: $date", style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Apply Button
        Box(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            Button(
                onClick = onApply,
                enabled = !hasApplied && !isLoading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = colors.onPrimary, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        text = if (hasApplied) "Already Applied" else "Apply for Job",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoChip(icon: ImageVector, text: String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .background(colors.surfaceVariant, RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
    }
}

@Preview(showBackground = true)
@Composable
private fun JobDetailScreenPreview() {
    WorkoraTheme {
        JobDetailScreen()
    }
}
