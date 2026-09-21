package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserRole
import com.example.ui.components.RoleCard
import com.example.ui.components.WorkoraHelmetLogo
import com.example.ui.components.WorkoraToast
import com.example.ui.theme.WorkoraBgLight
import com.example.ui.theme.WorkoraNavy
import com.example.ui.theme.WorkoraOrange
import com.example.ui.theme.WorkoraTextDark
import com.example.ui.theme.WorkoraTextMuted

@Composable
fun AccountSelectScreen(
    onSelectRole: (UserRole) -> Unit,
    toastMessage: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WorkoraBgLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Mini Helmet Logo
                WorkoraHelmetLogo(
                    size = 50.dp,
                    showHalo = false
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Mini App Title
                Text(
                    text = "WORKORA",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = WorkoraNavy,
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Mini Tagline
                Text(
                    text = "FIND. HIRE. WORK.",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = WorkoraOrange,
                    letterSpacing = 1.2.sp
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Selection Header
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "What do you want to do?",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = WorkoraTextDark,
                        lineHeight = 30.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Select how you want to use the app",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = WorkoraTextMuted
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Cards Container
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // Option 1: I Want to Hire (Customer)
                    RoleCard(
                        role = UserRole.CUSTOMER,
                        onClick = { onSelectRole(UserRole.CUSTOMER) }
                    )

                    // Option 2: I Want to Work (Labour)
                    RoleCard(
                        role = UserRole.LABOUR,
                        onClick = { onSelectRole(UserRole.LABOUR) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Floating Toast Notification at bottom
        WorkoraToast(
            message = toastMessage,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
        )
    }
}
