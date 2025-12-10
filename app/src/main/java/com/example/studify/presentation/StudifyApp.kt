package com.example.studify.presentation


import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.example.studify.presentation.nav.AppNavHost

@Composable
fun StudifyApp() {
    MaterialTheme { AppNavHost() }
}
