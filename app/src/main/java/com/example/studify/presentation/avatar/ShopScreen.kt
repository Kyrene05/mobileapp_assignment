@file:OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.example.studify.presentation.avatar

import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.studify.presentation.home.LevelViewModel
import com.example.studify.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- ENUMS & MODELS ---
enum class ShopTab { Shop, Wardrobe }


@Composable
fun ShopScreen(
    levelVm: LevelViewModel,
    onBack: () -> Unit,
    onSaveDone: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val db = remember { FirebaseFirestore.getInstance() }
    val auth = remember { FirebaseAuth.getInstance() }

    var profile by remember { mutableStateOf(AvatarProfile()) }
    var owned by remember { mutableStateOf(setOf<String>()) }
    var accessories by remember { mutableStateOf(setOf<String>()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var tab by remember { mutableStateOf(ShopTab.Shop) }
    var shopItems by remember { mutableStateOf<List<AccessoryItem>>(emptyList()) }

    val progress by levelVm.progress.collectAsState()
    val coins = progress.coins

    var buyTarget by remember { mutableStateOf<AccessoryItem?>(null) }
    var sellTarget by remember { mutableStateOf<AccessoryItem?>(null) }
    var gridClicksEnabled by remember { mutableStateOf(true) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    LaunchedEffect(tab) {
        buyTarget = null
        sellTarget = null
        gridClicksEnabled = false
        delay(80)
        gridClicksEnabled = true
    }

    LaunchedEffect(Unit) {
        loading = true
        AvatarRepository.getUserData()
            .onSuccess { (loadedProfile, _) ->
                val ownedSet = loadedProfile.owned + DEFAULT_OWNED
                val accessorySet = loadedProfile.accessories.filter { it in ownedSet }.toSet()
                owned = ownedSet
                accessories = accessorySet
                profile = loadedProfile
            }
            .onFailure { error = it.message ?: "Failed to load data" }
        loading = false
    }

    DisposableEffect(Unit) {
        val reg = db.collection("shop_items").addSnapshotListener { snap, e ->
            if (e != null) { error = e.message; return@addSnapshotListener }
            shopItems = snap?.documents?.mapNotNull { doc ->
                if (!(doc.getBoolean("available") ?: true)) return@mapNotNull null
                val imageKey = doc.getString("imageKey") ?: return@mapNotNull null
                val resId = resolveDrawableIdByName(context, imageKey)
                if (resId == 0) return@mapNotNull null
                AccessoryItem(
                    id = imageKey,
                    name = doc.getString("name") ?: doc.id,
                    resId = resId,
                    price = (doc.getLong("price") ?: 0L).toInt()
                )
            } ?: emptyList()
        }
        onDispose { reg.remove() }
    }

    Scaffold(
        containerColor = Cream,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("< Back", color = Coffee, modifier = Modifier.clickable { onBack() }) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
            )
        }
    ) { innerPadding ->
        // Use BoxWithConstraints to check orientation
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            val isLandscape = maxWidth > maxHeight

            if (isLandscape) {
                // --- LANDSCAPE: Side-by-Side ---
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // --- LANDSCAPE: Left Column Update ---
                    Column(
                        modifier = Modifier.weight(0.4f).fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top // Change from Center to Top
                    ) {
                        Spacer(Modifier.height(8.dp)) // Add small top margin

                        // Position coins clearly at the top
                        Text(
                            text = "💰 $coins",
                            color = Coffee,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp // Explicit size for visibility
                        )

                        Spacer(Modifier.height(12.dp)) // Vertical gap between coins and avatar

                        AvatarPreview(
                            profile = profile.copy(owned = owned, accessories = accessories.toList()),
                            modifier = Modifier.size(180.dp) // Slightly reduce size to 160.dp for landscape
                        )

                        if (tab == ShopTab.Wardrobe) {
                            Spacer(Modifier.weight(1f)) // Push button to the very bottom
                            Button(
                                onClick = { /* save logic */ },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                                    .height(40.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Banana)
                            ) {
                                Text("SAVE", color = Coffee, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(Modifier.width(16.dp))

                    // Right Column: Tabs + Grid
                    Column(modifier = Modifier.weight(0.6f).fillMaxHeight()) {
                        TabRow(selectedTabIndex = tab.ordinal, containerColor = Cream, contentColor = Coffee, indicator = {}) {
                            Tab(selected = tab == ShopTab.Shop, onClick = { tab = ShopTab.Shop }, text = { Text("Shop") })
                            Tab(selected = tab == ShopTab.Wardrobe, onClick = { tab = ShopTab.Wardrobe }, text = { Text("Wardrobe") })
                        }

                        ShopGrid(
                            gridItems = if (tab == ShopTab.Wardrobe) {
                                (shopItems + ACCESSORIES_BASE.map { AccessoryItem(it.id, it.name, it.resId, 0) })
                                    .distinctBy { it.id }.filter { it.id in owned }
                            } else shopItems,
                            owned = owned,
                            accessories = accessories,
                            coins = coins,
                            tab = tab,
                            enabled = gridClicksEnabled,
                            onItemClick = { item ->
                                if (tab == ShopTab.Wardrobe) {
                                    accessories = if (item.id in accessories) accessories - item.id else accessories + item.id
                                } else if (item.id !in owned) {
                                    buyTarget = item
                                }
                            },
                            onItemLongPress = { item ->
                                if (tab == ShopTab.Wardrobe && item.id in owned && item.id !in DEFAULT_OWNED) sellTarget = item
                            }
                        )
                    }
                }
            } else {
                // --- PORTRAIT: Original Vertical Layout ---
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                        Text("💰 $coins", color = Coffee, fontWeight = FontWeight.Bold)
                    }

                    AvatarPreview(
                        profile = profile.copy(owned = owned, accessories = accessories.toList()),
                        modifier = Modifier.size(200.dp)
                    )

                    TabRow(selectedTabIndex = tab.ordinal, containerColor = Cream, contentColor = Coffee, indicator = {}) {
                        Tab(selected = tab == ShopTab.Shop, onClick = { tab = ShopTab.Shop }, text = { Text("Shop") })
                        Tab(selected = tab == ShopTab.Wardrobe, onClick = { tab = ShopTab.Wardrobe }, text = { Text("Wardrobe") })
                    }

                    val gridItems = if (tab == ShopTab.Wardrobe) {
                        (shopItems + ACCESSORIES_BASE.map { AccessoryItem(it.id, it.name, it.resId, 0) })
                            .distinctBy { it.id }.filter { it.id in owned }
                    } else shopItems

                    Box(modifier = Modifier.weight(1f)) {
                        ShopGrid(gridItems, owned, accessories, coins, tab, gridClicksEnabled,
                            onItemClick = { item ->
                                if (tab == ShopTab.Wardrobe) {
                                    accessories = if (item.id in accessories) accessories - item.id else accessories + item.id
                                } else if (item.id !in owned) {
                                    buyTarget = item
                                }
                            },
                            onItemLongPress = { item ->
                                if (tab == ShopTab.Wardrobe && item.id in owned && item.id !in DEFAULT_OWNED) sellTarget = item
                            }
                        )
                    }

                    if (tab == ShopTab.Wardrobe) {
                        Button(
                            onClick = {
                                scope.launch {
                                    val toSave = profile.copy(owned = owned, accessories = accessories.toList())
                                    AvatarRepository.updateAvatarAndCoins(toSave, coins)
                                        .onSuccess { snackbarHostState.showSnackbar("Outfit saved!"); onSaveDone() }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp).height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Banana)
                        ) { Text("SAVE OUTFIT", color = Coffee, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }

    // --- DIALOGS WITH AUTOMATIC RECORDING ---
    buyTarget?.let { target ->
        BuyDialog(
            title = "Buy \"${target.name}\" ?",
            price = target.price,
            canAfford = coins >= target.price,
            onDismiss = { buyTarget = null },
            onConfirm = {
                val newCoins = coins - target.price
                owned = owned + target.id
                levelVm.updateCoins(newCoins)
                buyTarget = null // Close the buy dialog

                // NEW: Show the success message
                showSuccessDialog = true

                scope.launch {
                    val toSave = profile.copy(owned = owned, accessories = accessories.toList())
                    AvatarRepository.updateAvatarAndCoins(toSave, newCoins)

                    val tx = hashMapOf(
                        "type" to "BUY",
                        "itemName" to target.name,
                        "price" to target.price,
                        "userId" to (auth.currentUser?.uid ?: "unknown"),
                        "createdAt" to com.google.firebase.Timestamp.now()
                    )
                    db.collection("transactions").add(tx)
                }
            }
        )
    }
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            containerColor = Cream,
            title = {
                Text("Purchase Successful!", color = Coffee, fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Your new item is now available in the Wardrobe tab.", color = Coffee)
            },
            confirmButton = {
                TextButton(onClick = { showSuccessDialog = false }) {
                    Text("OK", color = Coffee, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    sellTarget?.let { item ->
        val sellPrice = (item.price / 4).coerceAtLeast(5)
        SellDialog(
            title = "Sell \"${item.name}\" ?",
            price = sellPrice,
            onDismiss = { sellTarget = null },
            onConfirm = {
                val newCoins = coins + sellPrice
                owned = owned - item.id
                accessories = accessories - item.id
                levelVm.updateCoins(newCoins)
                sellTarget = null
                scope.launch {
                    val toSave = profile.copy(owned = owned, accessories = accessories.toList())
                    AvatarRepository.updateAvatarAndCoins(toSave, newCoins)

                    // AUTOMATIC REFUND RECORD
                    val tx = hashMapOf(
                        "type" to "SELL",
                        "itemName" to item.name,
                        "price" to -sellPrice,
                        "userId" to (auth.currentUser?.uid ?: "unknown"),
                        "createdAt" to com.google.firebase.Timestamp.now()
                    )
                    db.collection("transactions").add(tx)
                }
            }
        )
    }
}
@Composable
fun ShopGrid(
    gridItems: List<AccessoryItem>,
    owned: Set<String>,
    accessories: Set<String>,
    coins: Int,
    tab: ShopTab,
    enabled: Boolean,
    onItemClick: (AccessoryItem) -> Unit,
    onItemLongPress: (AccessoryItem) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize().padding(top = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(gridItems, key = { it.id }) { item ->
            AccessoryCard(
                item = item,
                owned = item.id in owned,
                equipped = item.id in accessories,
                canAfford = coins >= item.price,
                tab = tab,
                enabled = enabled,
                onClick = { onItemClick(item) },
                onLongPress = { onItemLongPress(item) }
            )
        }
    }
}

@Composable
fun AccessoryCard(item: AccessoryItem, owned: Boolean, equipped: Boolean, canAfford: Boolean, tab: ShopTab, enabled: Boolean, onClick: () -> Unit, onLongPress: () -> Unit) {
    val bg by animateColorAsState(if (equipped) Banana else Paper)
    Surface(
        color = bg,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (equipped) Coffee else Color.Transparent),
        modifier = Modifier.height(130.dp).combinedClickable(enabled = enabled, onClick = onClick, onLongClick = onLongPress)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(painterResource(item.resId), null, tint = Color.Unspecified, modifier = Modifier.size(50.dp))
            Text(item.name, color = Coffee, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            val label = if (equipped) "Equipped" else if (owned) "Owned" else "💰${item.price}"
            Text(label, color = if (equipped) Coffee else Stone, fontSize = 10.sp)
        }
    }
}

@Composable
fun BuyDialog(title: String, price: Int, canAfford: Boolean, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(color = Cream, shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(20.dp)) {
                Text(title, color = Coffee, fontWeight = FontWeight.Bold, fontSize = 18.sp)

                Text("Price: $price", color = Coffee)

                // Show red warning if user cannot afford the item
                if (!canAfford) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "You don't have enough coins to buy it",
                        color = Color.Red,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cancel button set to Coffee color
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Coffee)
                    }

                    Spacer(Modifier.width(8.dp))

                    Button(
                        onClick = onConfirm,
                        enabled = canAfford, // Button is disabled if coins are insufficient
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Banana,
                            disabledContainerColor = Banana.copy(alpha = 0.5f)
                        )
                    ) {
                        Text("Buy", color = Coffee)
                    }
                }
            }
        }
    }
}
@Composable
fun SellDialog(title: String, price: Int, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(color = Cream, shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(20.dp)) {
                Text(title, color = Coffee, fontWeight = FontWeight.Bold)
                Text("Refund: $price", color = Coffee)
                Row(
                    Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(Banana)
                    ) { Text("Sell", color = Coffee) }
                }
            }
        }
    }
}

fun resolveDrawableIdByName(context: Context, imageKey: String): Int {
    return context.resources.getIdentifier(imageKey, "drawable", context.packageName)
}