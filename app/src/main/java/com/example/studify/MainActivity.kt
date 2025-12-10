package com.example.studify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.example.studify.presentation.StudifyApp
import com.google.firebase.FirebaseApp   // ⬅️ add this

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Init Firebase to avoid "Default FirebaseApp is not initialized" crash
        FirebaseApp.initializeApp(this)

        // Edge-to-edge (your composables handle insets with padding/imePadding)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent { StudifyApp() }
    }
}
