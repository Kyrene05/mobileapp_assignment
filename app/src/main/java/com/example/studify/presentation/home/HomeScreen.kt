package com.example.studify.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.studify.R
import com.example.studify.presentation.avatar.AvatarPreview
import com.example.studify.presentation.avatar.AvatarProfile
import com.example.studify.presentation.avatar.AvatarRepository
import com.example.studify.presentation.nav.BottomBar
import com.example.studify.ui.theme.Banana
import com.example.studify.ui.theme.Coffee
import com.example.studify.ui.theme.Cream
import com.example.studify.ui.theme.Stone
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    currentTab: Int,
    onOpenShop: () -> Unit,
    onLogout: () -> Unit,
    onNavHome: () -> Unit,
    onNavTasks: () -> Unit,
    levelVm: LevelViewModel
) {
    // Load latest progress from Firestore once when HomeScreen is first shown
    LaunchedEffect(Unit) {
        levelVm.refreshFromFirebase()
    }

    val progress by levelVm.progress.collectAsState()
    var avatar by remember { mutableStateOf(AvatarProfile(baseColor = "grey")) }
    val scope = rememberCoroutineScope()

    // --------- Top up dialog states ---------
    var showTopUpDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmation by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showTopUpResultDialog by remember { mutableStateOf(false) }

    var selectedPackageId by remember { mutableStateOf<Int?>(null) }
    var selectedPackageLabel by remember { mutableStateOf("") }

    var selectedPaymentMethod by remember { mutableStateOf("Online Banking / FPX") }

    var isInitialLoad by rememberSaveable { mutableStateOf(true) }
    var showLevelUpDialog by rememberSaveable { mutableStateOf(false) }
    var levelReached by rememberSaveable { mutableIntStateOf(progress.level) }
    // Listen for level increases to trigger the dialog
    LaunchedEffect(progress.level) {
        if (isInitialLoad) {
            levelReached = progress.level
            isInitialLoad = false
        } else {
            if (progress.level > levelReached) {
                levelReached = progress.level
                showLevelUpDialog = true
            }
        }
    }
    data class CoinPackage(
        val id: Int,
        val name: String,
        val desc: String
    )

    val coinPackages = remember {
        listOf(
            CoinPackage(1, "Small Pack",  "100 coins  ·  RM3.90"),
            CoinPackage(2, "Medium Pack", "300 coins  ·  RM8.90"),
            CoinPackage(3, "Large Pack",  "600 coins  ·  RM15.90")
        )
    }

    //  avatar
    LaunchedEffect(Unit) {
        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            scope.launch {
                AvatarRepository.load(uid).onSuccess { loaded ->
                    avatar = loaded
                }
            }
        }
    }

    // Level + coins
    val level = progress.level
    val xp = progress.xp
    val nextLevelXp = progress.nextLevelXp
    val coins = progress.coins

    Scaffold(
        containerColor = Cream,
        bottomBar = {
            BottomBar(
                selected = currentTab,
                onSelect = { index ->
                    when (index) {
                        0 -> onNavHome()
                        1 -> onNavTasks()
                        2 -> onOpenShop()
                    }
                }
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isLandscape = maxWidth > maxHeight

            Image(
                painter = painterResource(id = R.drawable.bg_home),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            if (isLandscape) {
                // --- LANDSCAPE: Side-by-Side ---
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left side: Stats
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start
                    ) {
                        LevelBar(level = level, xp = xp, nextLevelXp = nextLevelXp, width = 260.dp)
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("💰 $coins", color = Coffee, style = MaterialTheme.typography.bodyLarge)
                            Spacer(Modifier.width(8.dp))
                            IconButton(onClick = { showTopUpDialog = true }) {
                                Icon(Icons.Filled.Add, "Top up", tint = Coffee)
                            }
                            Spacer(Modifier.width(16.dp))
                            IconButton(onClick = { showLogoutConfirmation = true }) {
                                Icon(Icons.Filled.Logout, "Logout", tint = Coffee)
                            }
                        }
                    }

                    // Right side: Avatar
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        AvatarPreview(profile = avatar, modifier = Modifier.size(220.dp))
                    }
                }
            } else {
                // --- PORTRAIT: Original Vertical Layout ---
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(24.dp))
                    Row(modifier = Modifier.width(260.dp), verticalAlignment = Alignment.CenterVertically) {
                        LevelBar(level = level, xp = xp, nextLevelXp = nextLevelXp, width = 210.dp)
                        Spacer(Modifier.width(20.dp))
                        IconButton(onClick = { showLogoutConfirmation = true }) {
                            Icon(Icons.Filled.Logout, "Logout", tint = Coffee)
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(modifier = Modifier.width(260.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("💰 $coins", color = Coffee, style = MaterialTheme.typography.bodyLarge)
                        IconButton(onClick = { showTopUpDialog = true }) {
                            Icon(Icons.Filled.Add, "Top up", tint = Coffee)
                        }
                    }
                    Spacer(Modifier.height(140.dp))
                    Spacer(Modifier.weight(0.1f)) // Flexible spacing instead of 230.dp
                    Box(modifier = Modifier.weight(2f), contentAlignment = Alignment.Center) {
                        AvatarPreview(profile = avatar, modifier = Modifier.size(260.dp))
                    }
                }
            }
        }
    }
    // ================== Confirmation message ==================
    if (showLogoutConfirmation) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmation = false },
            containerColor = Cream,
            title = { Text("Logout", color = Coffee) },
            text = { Text("Are you sure you want to log out?", color = Stone) },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutConfirmation = false
                    onLogout() // Execute actual logout
                }) {
                    Text("Logout", color = Coffee)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmation = false }) {
                    Text("Cancel", color = Coffee.copy(alpha = 0.6f))
                }
            }
        )
    }
    // ================== Dialog 1：choose package ==================
    if (showTopUpDialog) {
        AlertDialog(
            onDismissRequest = { showTopUpDialog = false },
            containerColor = Cream,
            title = {
                Text(
                    text = "Get more coins",
                    color = Coffee
                )
            },
            text = {
                Column {
                    Text(
                        text = "Choose a coin package:",
                        color = Stone,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(12.dp))

                    coinPackages.forEach { pack ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedPackageId == pack.id,
                                onClick = { selectedPackageId = pack.id },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Coffee
                                )
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(pack.name, color = Coffee)
                                Text(
                                    pack.desc,
                                    color = Stone,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Note: This is a study prototype. No real payment will be processed.",
                        color = Stone.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val chosen = coinPackages.firstOrNull { it.id == selectedPackageId }
                        selectedPackageLabel = if (chosen != null) {
                            "${chosen.name} (${chosen.desc})"
                        } else {
                            ""
                        }
                        showTopUpDialog = false
                        showPaymentDialog = true
                    },
                    enabled = selectedPackageId != null
                ) {
                    Text("Next", color = Coffee)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTopUpDialog = false }) {
                    Text("Cancel", color = Coffee.copy(alpha = 0.6f))
                }
            }
        )
    }

    // ================== Dialog 2 Payment Method ==================
    if (showPaymentDialog) {
        AlertDialog(
            onDismissRequest = { /* force to click Back or Continue */ },
            containerColor = Cream,
            title = {
                Text(
                    text = "Choose payment method",
                    color = Coffee
                )
            },
            text = {
                Column {
                    // 订单摘要
                    if (selectedPackageLabel.isNotBlank()) {
                        Text(
                            text = selectedPackageLabel,
                            color = Coffee,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    Text(
                        text = "Select how you would like to pay:",
                        color = Stone,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(12.dp))

                    val methods = listOf(
                        "Online Banking / FPX",
                        "Credit / Debit Card",
                        "E-Wallet (TNG / GrabPay)"
                    )

                    methods.forEach { method ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedPaymentMethod == method,
                                onClick = { selectedPaymentMethod = method },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Coffee
                                )
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(method, color = Coffee)
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Note: This flow is for UI/UX demo only. No real payment will be made.",
                        color = Stone.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPaymentDialog = false
                        showTopUpResultDialog = true
                    }
                ) {
                    Text("Continue", color = Coffee)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPaymentDialog = false
                        showTopUpDialog = true
                    }
                ) {
                    Text("Back", color = Coffee.copy(alpha = 0.6f))
                }
            }
        )
    }

    // ================== Dialog 3：Demo  ==================
    if (showTopUpResultDialog) {
        AlertDialog(
            onDismissRequest = { showTopUpResultDialog = false },
            containerColor = Cream,
            title = {
                Text(
                    text = "Demo only",
                    color = Coffee
                )
            },
            text = {
                Text(
                    text = buildString {
                        if (selectedPackageLabel.isNotBlank()) {
                            append("You selected: $selectedPackageLabel\n\n")
                        }
                        append("Payment method: $selectedPaymentMethod\n\n")
                        append("This top-up flow is a prototype for UI/UX only.\n")
                        append("In the real app, this step would open a payment screen and add coins to your balance.\n\n")
                        append("Currently, your coins will NOT change and no real payment is made.")
                    },
                    color = Stone
                )
            },
            confirmButton = {
                TextButton(onClick = { showTopUpResultDialog = false }) {
                    Text("OK", color = Coffee)
                }
            }
        )
    }
    // ================== Dialog 4: Level Up Celebration ==================
    if (showLevelUpDialog) {
        AlertDialog(
            onDismissRequest = { showLevelUpDialog = false },
            containerColor = Banana, // Uses themed color for celebration
            title = {
                Text("🎉 LEVEL UP!", color = Coffee, style = MaterialTheme.typography.headlineMedium)
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Congratulations! You've reached Level $levelReached!",
                        color = Coffee,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(16.dp))
                    // Visual representation of the new level
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Coffee,
                        modifier = Modifier.size(80.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("$levelReached", color = Cream, style = MaterialTheme.typography.displaySmall)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLevelUpDialog = false }) {
                    Text("SURE", color = Coffee, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

/**
 * LevelBar shows player progress:
 *   "LV X  |██████----|  currentXp/nextLevelXp"
 */
@Composable
private fun LevelBar(
    level: Int,
    xp: Int,
    nextLevelXp: Int,
    width: Dp
) {
    val safeNext = nextLevelXp.coerceAtLeast(1)
    val progress = (xp.toFloat() / safeNext.toFloat()).coerceIn(0f, 1f)

    Column(modifier = Modifier.requiredWidth(width)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LV $level",
                style = MaterialTheme.typography.labelLarge,
                color = Coffee
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "$xp / $nextLevelXp",
                style = MaterialTheme.typography.labelMedium,
                color = Coffee
            )
        }
        Spacer(Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(8.dp)),
            color = Coffee,
            trackColor = Stone.copy(alpha = 0.35f)
        )
    }
}
