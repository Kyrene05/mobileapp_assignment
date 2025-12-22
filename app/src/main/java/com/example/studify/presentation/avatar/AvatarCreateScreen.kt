package com.example.studify.presentation.avatar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.example.studify.ui.theme.Cream
import com.example.studify.ui.theme.Coffee
import com.example.studify.ui.theme.Stone
import com.example.studify.ui.theme.Banana
import com.example.studify.ui.theme.Paper

@Composable
fun AvatarCreateScreen(
    onNext: (AvatarProfile) -> Unit,
    initialTab: Int = 0
) {
    var profile by remember { mutableStateOf(AvatarProfile()) }
    var tab by remember { mutableStateOf(initialTab) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // 1. Use Scaffold to handle the Status Bar and Navigation Bar
    Scaffold(
        containerColor = Cream,
        contentWindowInsets = WindowInsets.systemBars // Ensures content stays away from camera notch and home bar
    ) { innerPadding ->
        // 2. Wrap everything in a Column that uses the innerPadding
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // This is the crucial fix for the overlapping bars
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                "Customize your cat",
                color = Coffee,
                style = MaterialTheme.typography.titleLarge
            )

            // 3. BoxWithConstraints allows us to shrink the cat if the screen height is small
            BoxWithConstraints(modifier = Modifier.weight(1f)) {
                val isSmallScreen = maxHeight < 500.dp

                Column {
                    AvatarPreview(
                        profile,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isSmallScreen) 180.dp else 260.dp) // Adjusts size dynamically
                    )

                    Spacer(Modifier.height(8.dp))

                    TabRow(
                        selectedTabIndex = tab,
                        containerColor = Cream,
                        contentColor = Coffee,
                        indicator = {}
                    ) {
                        Tab(
                            selected = tab == 0,
                            onClick = { tab = 0 },
                            text = { Text("Color") },
                            selectedContentColor = Coffee,
                            unselectedContentColor = Stone
                        )
                        Tab(
                            selected = tab == 1,
                            onClick = { tab = 1 },
                            text = { Text("Accessories") },
                            selectedContentColor = Coffee,
                            unselectedContentColor = Stone
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // Content Area (Grid or Color Chips)
                    Box(modifier = Modifier.weight(1f)) {
                        if (tab == 0) {
                            Row(
                                Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                listOf("grey", "pink", "blue").forEach { c ->
                                    FilterChip(
                                        selected = profile.baseColor == c,
                                        onClick = { if (!saving) profile = profile.copy(baseColor = c) },
                                        label = { Text(c.replaceFirstChar { it.uppercase() }) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = Paper,
                                            selectedContainerColor = Banana.copy(alpha = 0.6f)
                                        )
                                    )
                                }
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(bottom = 16.dp)
                            ) {
                                items(ACCESSORIES_BASE) { item ->
                                    val selected = item.id in profile.accessories
                                    AccessoryCell(
                                        resId = item.resId,
                                        name = item.name,
                                        selected = selected,
                                        onClick = {
                                            if (saving) return@AccessoryCell
                                            profile = profile.copy(
                                                accessories = if (selected)
                                                    profile.accessories - item.id
                                                else profile.accessories + item.id
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (error != null) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            // Universal Button at the bottom
            Button(
                onClick = {
                    if (tab == 0) {
                        tab = 1
                    } else {
                        error = null
                        saving = true
                        scope.launch {
                            val result = AvatarRepository.save(profile)
                            saving = false
                            result.onSuccess { onNext(profile) }
                                .onFailure { e -> error = e.message ?: "Save failed." }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(bottom = 8.dp), // Extra margin for the bottom nav bar
                shape = RoundedCornerShape(16.dp),
                enabled = !saving,
                colors = ButtonDefaults.buttonColors(containerColor = Banana)
            ) {
                if (saving) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(20.dp), color = Coffee)
                } else {
                    Text("NEXT", fontWeight = FontWeight.Bold,color=Coffee)
                }
            }
        }
    }
}

@Composable
private fun AccessoryCell(
    resId: Int,
    name: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    Surface(
        color = Paper,
        tonalElevation = if (selected) 2.dp else 0.dp,
        border = if (selected) BorderStroke(2.dp, Coffee) else BorderStroke(1.dp, Stone.copy(0.5f)),
        shape = shape,
        modifier = Modifier
            .clip(shape)
            .clickable { onClick() }
    ) {
        Column(
            Modifier.padding(8.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(resId),
                contentDescription = name,
                tint = Color.Unspecified,
                modifier = Modifier.size(56.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(name, color = Coffee, style = MaterialTheme.typography.labelMedium)
        }
    }
}

/* -------------------- Previews -------------------- */

@Preview(
    name = "Avatar Create – Color tab",
    showBackground = true,
    backgroundColor = 0xFFF8E9D2,
    widthDp = 360, heightDp = 740
)
@Composable
private fun PreviewAvatarCreate_Color() {
    MaterialTheme {
        AvatarCreateScreen(onNext = {}, initialTab = 0)
    }
}

@Preview(
    name = "Avatar Create – Accessories tab",
    showBackground = true,
    backgroundColor = 0xFFF8E9D2,
    widthDp = 360, heightDp = 740
)
@Composable
private fun PreviewAvatarCreate_Accessories() {
    MaterialTheme {
        AvatarCreateScreen(onNext = {}, initialTab = 1)
    }
}
