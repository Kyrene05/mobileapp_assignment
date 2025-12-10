package com.example.studify.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
    var showTopUpDialog by remember { mutableStateOf(false) }          // 选套餐
    var showPaymentDialog by remember { mutableStateOf(false) }        // 选付款方式
    var showTopUpResultDialog by remember { mutableStateOf(false) }    // Demo 提示

    var selectedPackageId by remember { mutableStateOf<Int?>(null) }
    var selectedPackageLabel by remember { mutableStateOf("") }

    var selectedPaymentMethod by remember { mutableStateOf("Online Banking / FPX") }

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

    // 加载 avatar
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 背景
            Image(
                painter = painterResource(id = R.drawable.bg_home),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            val barWidth = 260.dp
            val iconSize = 30.dp
            val gap = 20.dp
            val barUsable = barWidth - iconSize - gap

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(24.dp))

                // 顶部：Level + Logout
                Row(
                    modifier = Modifier.requiredWidth(barWidth),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LevelBar(
                        level = level,
                        xp = xp,
                        nextLevelXp = nextLevelXp,
                        width = barUsable
                    )
                    Spacer(Modifier.width(gap))
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Filled.Logout,
                            contentDescription = "Logout",
                            tint = Coffee
                        )
                    }
                }

                // Coins + 「+」
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.requiredWidth(barWidth),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(Modifier.width(2.dp))

                    Text(
                        text = "\uD83D\uDCB0 $coins",
                        color = Coffee,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            selectedPackageId = null
                            selectedPaymentMethod = "Online Banking / FPX"
                            showTopUpDialog = true
                        },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Top up coins",
                            tint = Coffee
                        )
                    }
                }

                // Avatar
                Spacer(Modifier.height(230.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    AvatarPreview(
                        profile = avatar,
                        modifier = Modifier.size(260.dp)
                    )
                }
            }
        }
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
