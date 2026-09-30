package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val WorkoraBlue = Color(0xFF0061FF)
private val TextDark = Color(0xFF0F172A)
private val TextGray = Color(0xFF64748B)
private val BorderGray = Color(0xFFE2E8F0)
private val BgColor = Color(0xFFF8FAFC)
private val ProfileOrange = Color(0xFFFF8C00)

data class WorkerSearchResult(
    val name: String,
    val category: String,
    val rating: String,
    val location: String,
    val wage: String
)

@Composable
fun SearchFilterScreen(
    onBack: () -> Unit = {},
    onWorkerClick: (WorkerSearchResult) -> Unit = {}
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    
    val categories = listOf("All", "Plumber", "Electrician", "Painter", "Mason", "Carpenter")
    
    val allWorkers = listOf(
        WorkerSearchResult("Amit Kumar", "Painter", "4.8", "Raisen (5 km)", "₹800/day"),
        WorkerSearchResult("Rahul Sharma", "Plumber", "4.9", "Silwani (2 km)", "₹900/day"),
        WorkerSearchResult("Vikash Verma", "Electrician", "4.7", "Bhopal (40 km)", "₹1000/day"),
        WorkerSearchResult("Suresh Das", "Mason", "4.5", "Silwani (1 km)", "₹850/day")
    )

    // Functional Search and Filter Logic
    val filteredWorkers = allWorkers.filter { 
        (selectedCategory == "All" || it.category == selectedCategory) &&
        (searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true) || it.category.contains(searchQuery, ignoreCase = true))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Top Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WorkoraBlue)
            }
            Spacer(modifier = Modifier.width(8.dp))
            
            // Functional Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search workers or jobs...", color = TextGray, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextGray) },
                trailingIcon = { 
                    Icon(
                        Icons.Default.FilterList, 
                        contentDescription = "Filter", 
                        tint = WorkoraBlue,
                        modifier = Modifier.clickable { Toast.makeText(context, "Advanced Filters Opened", Toast.LENGTH_SHORT).show() }
                    ) 
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(24.dp),
                maxLines = 1,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WorkoraBlue,
                    unfocusedBorderColor = BorderGray,
                    focusedContainerColor = BgColor,
                    unfocusedContainerColor = BgColor
                )
            )
        }

        // Functional Category Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(categories) { category ->
                val isSelected = selectedCategory == category
                Box(
                    modifier = Modifier
                        .clickable { selectedCategory = category }
                        .background(
                            if (isSelected) WorkoraBlue else BgColor,
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = category,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else TextDark
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        // Search Results List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Search Results (${filteredWorkers.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGray,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            if (filteredWorkers.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 100.dp), contentAlignment = Alignment.Center) {
                        Text(text = "No results found.", color = TextGray, fontSize = 15.sp)
                    }
                }
            }

            items(filteredWorkers) { worker ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onWorkerClick(worker) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, BorderGray)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Profile Avatar
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(Color(0xFFEFF6FF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = WorkoraBlue, modifier = Modifier.size(24.dp))
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = worker.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = ProfileOrange, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = worker.rating, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = worker.category, fontSize = 13.sp, color = WorkoraBlue, fontWeight = FontWeight.Medium)
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextGray, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = worker.location, fontSize = 12.sp, color = TextGray)
                                }
                                Text(text = worker.wage, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
