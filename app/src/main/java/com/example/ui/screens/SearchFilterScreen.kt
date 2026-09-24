package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WorkoraBgLight
import com.example.ui.theme.WorkoraBorder
import com.example.ui.theme.WorkoraNavy
import com.example.ui.theme.WorkoraOrange
import com.example.ui.theme.WorkoraTextDark
import com.example.ui.theme.WorkoraTextMuted

@Composable
fun SearchFilterScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onApplyFilters: (category: String, location: String, maxWage: String) -> Unit = { _, _, _ -> }
) {
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedLocation by remember { mutableStateOf("Sector 12") }
    var maxWageInput by remember { mutableStateOf("1000") }

    val categories = listOf("All", "Mason", "Electrician", "Plumber", "Carpenter", "Painter", "Construction Helper")
    val locations = listOf("Sector 12", "Labour Chowk", "Model Town", "Defence Colony", "Civil Lines")
    val wageLimits = listOf("700", "850", "1000", "1500+")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WorkoraBgLight)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // Top Header Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = WorkoraNavy
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = null,
                tint = WorkoraOrange,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Filter Workers & Jobs",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = WorkoraTextDark
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            // 1. Filter by Skill / Category
            Text(
                text = "Skill / Work Category",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = WorkoraTextDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = cat == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 13.sp) },
                        shape = RoundedCornerShape(16.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = WorkoraNavy,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = WorkoraTextDark
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Filter by Location / Area
            Text(
                text = "Location / Area",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = WorkoraTextDark
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                locations.forEach { loc ->
                    val isSelected = loc == selectedLocation
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedLocation = loc },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null,
                        label = { Text(loc, fontSize = 13.sp) },
                        shape = RoundedCornerShape(16.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = WorkoraOrange,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = WorkoraTextDark
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Maximum Wage Filter
            Text(
                text = "Maximum Daily Wage (₹)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = WorkoraTextDark
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = maxWageInput,
                onValueChange = { maxWageInput = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. 1000") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = WorkoraNavy,
                    unfocusedBorderColor = WorkoraBorder
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                wageLimits.forEach { limit ->
                    OutlinedButton(
                        onClick = { maxWageInput = limit.replace("+", "") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("₹$limit", fontSize = 12.sp, color = WorkoraNavy, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Action Buttons (Apply & Reset)
            Button(
                onClick = {
                    onApplyFilters(selectedCategory, selectedLocation, maxWageInput)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WorkoraOrange)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Apply Filters",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    selectedCategory = "All"
                    selectedLocation = "Sector 12"
                    maxWageInput = "1000"
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, WorkoraBorder)
            ) {
                Text(
                    text = "Reset All Filters",
                    fontSize = 14.sp,
                    color = WorkoraTextMuted,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
