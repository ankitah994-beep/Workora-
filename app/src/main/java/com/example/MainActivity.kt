package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.User
import com.example.model.UserRole
import com.example.model.UserRole.EMPLOYER
import com.example.model.UserRole.WORKER
import com.example.ui.theme.MyApplicationTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        WorkoraMainApp()
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoraMainApp() {
  val context = LocalContext.current
  var selectedRole: UserRole by remember<MutableState<UserRole>> {
    mutableStateOf<UserRole>(UserRole.WORKER)
  }
  var currentUser: User? by remember<MutableState<User?>> {
    mutableStateOf<User?>(null)
  }

  if (currentUser == null) {
    AuthScreen(
      selectedRole = selectedRole,
      onRoleChanged = { role: UserRole ->
        selectedRole = role
      },
      onBackToRoleSelection = {
        Toast.makeText(context, "Role Selection", Toast.LENGTH_SHORT).show()
      },
      onLoginSuccess = { user: User ->
        currentUser = user
      },
      toastMessage = { message: String ->
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
      }
    )
  } else {
    val user: User = currentUser!!
    Scaffold(
      topBar = {
        TopAppBar(
          title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Engineering,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("Workora Dashboard", color = Color.White, fontWeight = FontWeight.Bold)
            }
          },
          actions = {
            IconButton(onClick = { currentUser = null }) {
              Icon(
                imageVector = Icons.Default.Logout,
                contentDescription = "Sign Out",
                tint = Color.White
              )
            }
          },
          colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF1D4ED8)
          )
        )
      }
    ) { innerPadding ->
      Surface(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding),
        color = Color(0xFFF1F5F9)
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
          ) {
            Column(
              modifier = Modifier.padding(20.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Box(
                modifier = Modifier
                  .size(64.dp)
                  .clip(CircleShape)
                  .background(Color(0xFFDBEAFE)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Person,
                  contentDescription = null,
                  tint = Color(0xFF1D4ED8),
                  modifier = Modifier.size(36.dp)
                )
              }

              Spacer(modifier = Modifier.height(12.dp))

              Text(
                text = user.name,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
              )

              Text(
                text = "Role: ${user.role.name}",
                fontSize = 14.sp,
                color = Color(0xFF1D4ED8),
                fontWeight = FontWeight.SemiBold
              )

              Spacer(modifier = Modifier.height(16.dp))
              HorizontalDivider(color = Color(0xFFE2E8F0))
              Spacer(modifier = Modifier.height(16.dp))

              UserPropertyRow(label = "User ID", value = user.id)
              UserPropertyRow(label = "Email", value = user.email)
              UserPropertyRow(label = "Phone", value = user.phoneNumber)
              UserPropertyRow(
                label = "Created At",
                value = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(user.createdAt))
              )
            }
          }

          Spacer(modifier = Modifier.height(24.dp))

          Button(
            onClick = { currentUser = null },
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFFDC2626)
            )
          ) {
            Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign Out / Switch Account")
          }
        }
      }
    }
  }
}

@Composable
private fun UserPropertyRow(label: String, value: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(text = label, fontSize = 13.sp, color = Color(0xFF64748B))
    Text(
      text = value,
      fontSize = 13.sp,
      fontWeight = FontWeight.Medium,
      color = Color(0xFF0F172A)
    )
  }
}

@Preview(showBackground = true)
@Composable
fun WorkoraMainAppPreview() {
  MyApplicationTheme {
    WorkoraMainApp()
  }
}
