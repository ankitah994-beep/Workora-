package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ui.theme.WorkoraTheme

@Composable
fun WorkerDetailScreen(
    modifier: Modifier = Modifier,
    name: String = "Amit Sharma",
    bio: String = "Hardworking and reliable worker with 5 years of experience in house construction, painting, and basic electrical work. Always on time.",
    rating: String = "4.8",
    completedJobs: String = "42",
    skills: List<String> = listOf("Construction", "Painting", "Plumbing", "Electrical"),
    onBack: () -> Unit = {},
    onContact: () -> Unit = {}
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
                text = "Worker Profile",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.onBackground
            )
        }

        // Profile Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Profile Picture Placeholder
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(colors.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.size(50.dp),
                    tint = colors.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Name
            Text(
                text = name,
                style = MaterialTheme.typography.headlineMedium,
                color = colors.onBackground,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard(title = "Rating", value = "$rating ⭐️")
                StatCard(title = "Jobs Done", value = completedJobs)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Skills Section
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Top Skills", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colors.onBackground)
                Spacer(modifier = Modifier.height(12.dp))
                
                // Horizontal scrollable row for skills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    skills.forEach { skill ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = colors.secondaryContainer
                        ) {
                            Text(
                                text = skill,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = colors.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bio Section
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("About", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colors.onBackground)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = bio, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            }
        }

        // Contact Button
        Box(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            Button(
                onClick = onContact,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Contact / Hire Worker", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String) {
    val colors = MaterialTheme.colorScheme
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = colors.primary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = title, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
    }
}

@Preview(showBackground = true)
@Composable
private fun WorkerDetailScreenPreview() {
    WorkoraTheme {
        WorkerDetailScreen()
    }
}
