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
import androidx.compose.runtime.LaunchedEffect
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
    //check admin claim when this screen enters
    LaunchedEffect(Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.getIdToken(true)
            ?.addOnSuccessListener { result ->
                val isAdmin = result.claims["admin"] == true
                Log.d("ADMIN", "admin claim = $isAdmin")
            }
            ?.addOnFailureListener { e ->
                Log.e("ADMIN", "token refresh failed", e)
            }
    }

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

                IconButton(onClick = onLogoutClick) {
                    Icon(
                        imageVector = Icons.Outlined.Logout,
                        contentDescription = "Logout",
                        tint = textColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))

            DashboardButtonCard(
                label = "Shop Item Management",
                onClick = onShopManagementClick,
                cardColor = cardColor,
                textColor = textColor
            )

            Spacer(modifier = Modifier.height(40.dp))

            DashboardButtonCard(
                label = "View Summary Report",
                onClick = onViewReportClick,
                cardColor = cardColor,
                textColor = textColor
            )
        }
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
