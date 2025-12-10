@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.studify.presentation.avatar

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.ui.unit.sp
import com.example.studify.presentation.home.LevelViewModel
import com.example.studify.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class ShopTab { Shop, Wardrobe }

@Composable
fun ShopScreen(
    levelVm: LevelViewModel,
    onBack: () -> Unit,
    onSaveDone: () -> Unit
) {
    var profile by remember { mutableStateOf(AvatarProfile()) }
    var owned by remember { mutableStateOf(setOf<String>()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var tab by remember { mutableStateOf(ShopTab.Shop) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val progress by levelVm.progress.collectAsState()
    val coins = progress.coins

    var buyTarget by remember { mutableStateOf<AccessoryItem?>(null) }
    var sellTarget by remember { mutableStateOf<AccessoryItem?>(null) }

    var gridClicksEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(tab) {
        buyTarget = null
        sellTarget = null
        gridClicksEnabled = false
        delay(80)
        gridClicksEnabled = true
    }


    LaunchedEffect(Unit) {
        AvatarRepository.getUserData()
            .onSuccess { (loadedProfile, _userCoinsFromAvatar) ->
                val finalOwned = loadedProfile.owned + DEFAULT_OWNED
                profile = loadedProfile.copy(owned = finalOwned)
                owned = finalOwned
            }
            .onFailure { error = it.message }
        loading = false
    }

    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Coffee)
        }
        return
    }

    Scaffold(
        containerColor = Cream,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "< Back",
                        color = Coffee,
                        modifier = Modifier.clickable { onBack() }
                    )
                },
                navigationIcon = {},
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Cream,
                    titleContentColor = Coffee
                )
            )
        }
    ) { innerPadding ->

        Surface(color = Cream, modifier = Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Coins row
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "💰 $coins",
                            color = Coffee,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    // Avatar preview
                    AvatarPreview(profile, Modifier.size(220.dp))
                    Spacer(Modifier.height(12.dp))

                    // Tabs
                    TabRow(
                        selectedTabIndex = tab.ordinal,
                        containerColor = Cream,
                        contentColor = Coffee,
                        indicator = {}
                    ) {
                        Tab(
                            selected = tab == ShopTab.Shop,
                            onClick = {
                                tab = ShopTab.Shop
                            },
                            selectedContentColor = Coffee,
                            unselectedContentColor = Stone,
                            text = { Text("Shop") }
                        )
                        Tab(
                            selected = tab == ShopTab.Wardrobe,
                            onClick = {
                                tab = ShopTab.Wardrobe
                            },
                            selectedContentColor = Coffee,
                            unselectedContentColor = Stone,
                            text = { Text("Wardrobe") }
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    if (tab == ShopTab.Wardrobe) {
                        Text(
                            text = "Tip: Long press an item to sell it.",
                            color = Stone,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(Modifier.height(4.dp))
                    }

                    val shopCatalog = ACCESSORIES
                    val wardrobeCatalog = remember {
                        (ACCESSORIES + ACCESSORIES_BASE.map {
                            AccessoryItem(
                                id = it.id,
                                name = it.name,
                                resId = it.resId,
                                price = 0
                            )
                        }).distinctBy { it.id }
                    }

                    val gridItems =
                        if (tab == ShopTab.Wardrobe) wardrobeCatalog.filter { it.id in owned }
                        else shopCatalog

                    // Items grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        items(gridItems, key = { it.id }) { item ->
                            val isOwned = item.id in owned
                            val equipped = item.id in profile.accessories
                            val canAfford = coins >= item.price

                            AccessoryCard(
                                item = item,
                                owned = isOwned,
                                equipped = equipped,
                                canAfford = canAfford,
                                tab = tab,
                                enabled = gridClicksEnabled,
                                onClick = {
                                    when (tab) {
                                        ShopTab.Wardrobe -> {
                                            profile = profile.copy(
                                                accessories = if (equipped)
                                                    profile.accessories - item.id
                                                else
                                                    profile.accessories + item.id
                                            )
                                        }

                                        ShopTab.Shop -> {
                                            if (!isOwned) {
                                                buyTarget = item
                                            }
                                        }
                                    }
                                },
                                onLongPress = {
                                    if (tab == ShopTab.Wardrobe && isOwned && item.id !in DEFAULT_OWNED) {
                                        sellTarget = item
                                    }
                                }
                            )
                        }
                    }

                    // Wardrobe bottom save button
                    if (tab == ShopTab.Wardrobe) {
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    AvatarRepository
                                        .updateAvatarAndCoins(
                                            profile.copy(owned = owned),
                                            coins
                                        )
                                        .onSuccess {
                                            snackbarHostState.showSnackbar("Outfit saved!")
                                            onSaveDone()
                                        }
                                        .onFailure { error = it.message }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Banana)
                        ) { Text("SAVE", color = Coffee) }
                    }

                    if (error != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(error!!, color = Color.Red)
                    }
                }
            }
        }
    }

    // ===== Buy Dialog =====
    val target = buyTarget
    if (target != null) {
        val canAfford = coins >= target.price
        BuyDialog(
            title = "Buy \"${target.name}\" ?",
            price = target.price,
            canAfford = canAfford,
            onDismiss = { buyTarget = null },
            onConfirm = {
                val newCoins = coins - target.price
                val newOwned = owned + target.id
                val newProfile = profile.copy(
                    owned = newOwned
                )

                owned = newOwned
                profile = newProfile
                buyTarget = null

                levelVm.updateCoins(newCoins)


                scope.launch {
                    AvatarRepository.updateAvatarAndCoins(newProfile, newCoins)
                }
            }
        )
    }

    // ===== Sell Dialog（长按）=====
    val sellItem = sellTarget
    if (sellItem != null) {
        // 简单设计：卖出价格 = 原价的一半，至少 5
        val basePrice = sellItem.price
        val sellPrice = (basePrice / 4).coerceAtLeast(5)

        SellDialog(
            title = "Sell \"${sellItem.name}\" ?",
            price = sellPrice,
            onDismiss = { sellTarget = null },
            onConfirm = {
                val newCoins = coins + sellPrice
                val newOwned = owned - sellItem.id
                val newAccessories = profile.accessories - sellItem.id
                val newProfile = profile.copy(
                    owned = newOwned,
                    accessories = newAccessories
                )

                owned = newOwned
                profile = newProfile
                sellTarget = null

                levelVm.updateCoins(newCoins)

                scope.launch {
                    AvatarRepository.updateAvatarAndCoins(newProfile, newCoins)
                }
            }
        )
    }
}

/* ---------- Buy Dialog（Cream 色系） ---------- */
@Composable
private fun BuyDialog(
    title: String,
    price: Int,
    canAfford: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = Cream,
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 2.dp,
            border = BorderStroke(1.dp, Coffee.copy(alpha = 0.15f))
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(title, color = Coffee, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(10.dp))
                Text("Price: $price coins", color = Coffee)

                if (!canAfford) {
                    Spacer(Modifier.height(6.dp))
                    Text("Not enough coins.", color = Color(0xFFCC3333))
                }

                Spacer(Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Coffee)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = onConfirm,
                        enabled = canAfford,
                        colors = ButtonDefaults.buttonColors(containerColor = Banana)
                    ) {
                        Text("Buy", color = Coffee)
                    }
                }
            }
        }
    }
}

/* ---------- Sell Dialog（色系保持一致） ---------- */
@Composable
private fun SellDialog(
    title: String,
    price: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = Cream,
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 2.dp,
            border = BorderStroke(1.dp, Coffee.copy(alpha = 0.15f))
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(title, color = Coffee, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(10.dp))
                Text("You will get $price coins.", color = Coffee)

                Spacer(Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Coffee)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = Banana)
                    ) {
                        Text("Sell", color = Coffee)
                    }
                }
            }
        }
    }
}

/* ---------- Accessory Card：支持点击 + 长按 ---------- */
@Composable
private fun AccessoryCard(
    item: AccessoryItem,
    owned: Boolean,
    equipped: Boolean,
    canAfford: Boolean,
    tab: ShopTab,
    enabled: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    val bg by animateColorAsState(
        if (equipped) Banana else Paper,
        label = "card-bg"
    )

    val borderColor = when {
        equipped -> Coffee
        owned -> Stone
        !canAfford && tab == ShopTab.Shop -> Stone.copy(alpha = 0.4f)
        else -> Stone
    }

    val labelColor = if (equipped) Coffee else Stone

    Surface(
        color = bg,
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = if (equipped) 1.5.dp else 0.dp,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .widthIn(min = 96.dp)
            .height(140.dp)
            .combinedClickable(
                enabled = enabled,
                onClick = onClick,
                onLongClick = onLongPress
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                painterResource(item.resId),
                contentDescription = item.name,
                tint = Color.Unspecified,
                modifier = Modifier.size(56.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(item.name, color = Coffee, style = MaterialTheme.typography.labelMedium)

            val label = when {
                equipped -> "Equipped"
                owned -> "Owned"
                else -> "💰${item.price}"
            }

            if (tab == ShopTab.Shop || owned) {
                Text(label, color = labelColor, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

/* ---------- 简单 Preview（不连 ViewModel，只看 UI） ---------- */
@Preview(
    name = "Shop – Preview UI only",
    showBackground = true,
    backgroundColor = 0xFFF8E9D2,
    widthDp = 360,
    heightDp = 740
)
@Composable
private fun Preview_Shop_Tab_Shop() {
    // 只是 UI 预览用，不会用到 levelVm
    Surface(color = Cream, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("< Back", fontSize = 22.sp, color = Coffee)
        }
    }
}
