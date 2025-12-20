package com.example.studify.presentation.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.studify.ui.theme.Banana
import com.example.studify.ui.theme.Coffee
import com.example.studify.ui.theme.Cream
import android.util.Log
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GetTokenResult


@Composable
fun AdminDashboardScreen(
    onShopManagementClick: () -> Unit,
    onViewReportClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    var showLogoutConfirmation by remember { mutableStateOf(false) }

    // 1. Create the scroll state
    val scrollState = rememberScrollState()

    val backgroundColor = Cream
    val cardColor = Banana
    val textColor = Coffee

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                // 2. Add verticalScroll here so it can move in landscape
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top row: Dashboard + logout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dashboard",
                    style = MaterialTheme.typography.headlineMedium,
                    color = textColor
                )

                IconButton(onClick = { showLogoutConfirmation = true }) {
                    Icon(
                        imageVector = Icons.Outlined.Logout,
                        contentDescription = "Logout",
                        tint = textColor
                    )
                }
            }

            // 3. Reduce this height slightly for better landscape fit
            Spacer(modifier = Modifier.height(40.dp))

            DashboardButtonCard(
                label = "Shop Item Management",
                onClick = onShopManagementClick,
                cardColor = cardColor,
                textColor = textColor
            )

            Spacer(modifier = Modifier.height(24.dp))

            DashboardButtonCard(
                label = "View Summary Report",
                onClick = onViewReportClick,
                cardColor = cardColor,
                textColor = textColor
            )

            // Add a little bottom spacer so the last button isn't stuck to the edge
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
    // Confirmation Dialog logic
    if (showLogoutConfirmation) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmation = false },
            containerColor = Cream,
            title = { Text("Admin Logout", color = textColor) },
            text = { Text("Are you sure you want to exit the admin dashboard?", color = textColor) },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutConfirmation = false
                    onLogoutClick()
                }) { Text("Logout", color = textColor) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmation = false }) {
                    Text("Cancel", color = textColor.copy(alpha = 0.6f))
                }
            }
        )
    }
}

@Composable
private fun DashboardButtonCard(
    label: String,
    onClick: () -> Unit,
    cardColor: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color
) {
    Surface(
        shape = RoundedCornerShape(32.dp),
        color = cardColor,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = textColor
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AdminDashboardPreview() {
    AdminDashboardScreen(
        onShopManagementClick = {},
        onViewReportClick = {},
        onLogoutClick = {}
    )
}
