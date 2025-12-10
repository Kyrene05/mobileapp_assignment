package com.example.studify.presentation.nav

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timer   // ⬅️ 新增：Pomodoro 用计时器图标
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.studify.ui.theme.Banana
import com.example.studify.ui.theme.Coffee
import androidx.compose.material.icons.filled.Store

@Composable
fun BottomBar(
    selected: Int,
    onSelect: (Int) -> Unit
) {
    val itemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = Coffee,
        selectedTextColor = Coffee,
        unselectedIconColor = Coffee.copy(alpha = 0.75f),
        unselectedTextColor = Coffee.copy(alpha = 0.75f),
        indicatorColor = Banana
    )

    NavigationBar(
        containerColor = Banana,
        tonalElevation = 0.dp,
        windowInsets = WindowInsets(0)
    ) {
        // Home
        NavigationBarItem(
            selected = selected == 0,
            onClick = { onSelect(0) },
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text("Home") },
            colors = itemColors
        )

        // Pomodoro
        NavigationBarItem(
            selected = selected == 1,
            onClick = { onSelect(1) },
            icon = { Icon(Icons.Filled.Timer, contentDescription = "Pomodoro") },
            label = { Text("Pomodoro") },
            colors = itemColors
        )

        // Logout
        NavigationBarItem(
            selected = selected == 2,
            onClick = { onSelect(2) },
            icon = { Icon(Icons.Filled.Store, contentDescription = "Shop") },
            label = { Text("Shop") },
            colors = itemColors
        )
    }
}

