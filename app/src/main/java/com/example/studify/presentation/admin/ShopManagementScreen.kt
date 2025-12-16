@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.studify.presentation.admin

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studify.ui.theme.Coffee
import com.example.studify.ui.theme.Cream
import com.example.studify.ui.theme.Paper
import com.example.studify.ui.theme.Stone
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

data class ShopItem(
    val id: String = "",
    val name: String = "",
    val price: Int = 0,
    val available: Boolean = true,
    val imageKey: String = ""
)

@Composable
fun ShopManagementScreen(
    onBack: () -> Unit,
    onEdit: (itemId: String) -> Unit,
    onAddNew: () -> Unit, // ✅ + Add New
) {
    val context = LocalContext.current

    var items by remember { mutableStateOf<List<ShopItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedId by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        val reg: ListenerRegistration =
            FirebaseFirestore.getInstance()
                .collection("shop_items")
                .addSnapshotListener { snap, e ->
                    if (e != null) {
                        loading = false
                        error = e.message ?: "Failed to load items"
                        return@addSnapshotListener
                    }

                    val newItems = snap?.documents?.map { doc ->
                        ShopItem(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            price = (doc.getLong("price") ?: 0L).toInt(),
                            available = doc.getBoolean("available") ?: true,
                            imageKey = doc.getString("imageKey") ?: ""
                        )
                    }?.sortedBy { it.name.lowercase() } ?: emptyList()

                    items = newItems
                    loading = false
                    error = null

                    // If selected item is removed, clear selection
                    val cur = selectedId
                    if (cur != null && newItems.none { it.id == cur }) {
                        selectedId = null
                    }
                }

        onDispose { reg.remove() }
    }

    Scaffold(
        containerColor = Cream,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    Text(
                        text = "< Back",
                        color = Coffee,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .clickable { onBack() }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {

            if (loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Coffee)
                }
                return@Column
            }

            if (error != null) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(8.dp))
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                itemsIndexed(items) { index, item ->
                    ShopItemRow(
                        index = index + 1,
                        item = item,
                        selected = item.id == selectedId,
                        onClick = { selectedId = item.id },
                        context = context
                    )
                }
            }

            // ✅ Bottom buttons: + Add New & EDIT
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Button(
                    onClick = {
                        error = null
                        onAddNew()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Paper,
                        contentColor = Coffee
                    ),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("+ Add New", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        val id = selectedId
                        if (id == null) {
                            error = "Please select an item first."
                        } else {
                            error = null
                            onEdit(id)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Paper,
                        contentColor = Coffee
                    ),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("EDIT", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
private fun ShopItemRow(
    index: Int,
    item: ShopItem,
    selected: Boolean,
    onClick: () -> Unit,
    context: Context
) {
    val borderColor = if (selected) Coffee else Color.Transparent
    val statusText = if (item.available) "Available" else "Unavailable"

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp)
            .clickable { onClick() },
        color = Paper,
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(2.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$index.",
                color = Coffee,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.width(40.dp)
            )

            Spacer(Modifier.width(10.dp))

            val resId = remember(item.imageKey) {
                resolveDrawableIdByName(context, item.imageKey)
            }

            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFEFE7DD)),
                contentAlignment = Alignment.Center
            ) {
                if (resId != 0) {
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = item.name,
                        modifier = Modifier.size(70.dp)
                    )
                } else {
                    Text("No\nImage", color = Stone, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.width(18.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Name : ${item.name}",
                    color = Coffee,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Price: 💰 ${item.price}",
                    color = Coffee,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Status : $statusText",
                    color = Coffee,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private fun resolveDrawableIdByName(context: Context, imageKey: String): Int {
    if (imageKey.isBlank()) return 0
    return context.resources.getIdentifier(imageKey, "drawable", context.packageName)
}
