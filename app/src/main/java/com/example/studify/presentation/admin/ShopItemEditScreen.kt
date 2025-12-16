@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.studify.presentation.admin

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studify.ui.theme.Coffee
import com.example.studify.ui.theme.Cream
import com.example.studify.ui.theme.Paper
import com.example.studify.ui.theme.Stone
import com.google.firebase.firestore.FirebaseFirestore


private val IMAGE_KEY_CHOICES = listOf(
    "acc_cap",
    "acc_crown",
    "acc_shades",
    "acc_love",
    "acc_magichat",
    "acc_gradcap",
    "acc_gojo",
    "acc_beanie",
    "acc_bowtie"
)

@Composable
fun ShopItemEditScreen(
    itemId: String?,           // ✅ null = Add New, not null = Edit existing
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onDeleted: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }

    var loading by remember { mutableStateOf(itemId != null) }
    var error by remember { mutableStateOf<String?>(null) }

    // form states
    var selectedImageKey by remember { mutableStateOf(IMAGE_KEY_CHOICES.first()) }
    var name by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var available by remember { mutableStateOf(true) }

    // dropdown
    var expandDropdown by remember { mutableStateOf(false) }

    // Load existing item if edit mode
    LaunchedEffect(itemId) {
        if (itemId.isNullOrBlank()) return@LaunchedEffect
        loading = true
        error = null
        try {
            val doc = db.collection("shop_items").document(itemId).get().awaitCompat()
            if (!doc.exists()) {
                error = "Item not found."
                loading = false
                return@LaunchedEffect
            }
            name = doc.getString("name") ?: ""
            val p = (doc.getLong("price") ?: 0L).toInt()
            priceText = p.toString()
            available = doc.getBoolean("available") ?: true
            selectedImageKey = doc.getString("imageKey") ?: selectedImageKey
        } catch (e: Exception) {
            error = e.message ?: "Failed to load item."
        } finally {
            loading = false
        }
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

        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Coffee)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 22.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Item",
                color = Coffee,
                fontSize = 44.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(14.dp))

            // ===== Dropdown (ImageKey) =====
            Box {
                Surface(
                    color = Paper,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clickable { expandDropdown = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = selectedImageKey,
                            color = Coffee,
                            fontSize = 18.sp
                        )
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = "Expand",
                            tint = Coffee
                        )
                    }
                }

                DropdownMenu(
                    expanded = expandDropdown,
                    onDismissRequest = { expandDropdown = false },
                    modifier = Modifier.background(Paper)
                ) {
                    IMAGE_KEY_CHOICES.forEach { key ->
                        DropdownMenuItem(
                            text = { Text(key, color = Coffee) },
                            onClick = {
                                selectedImageKey = key
                                expandDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // ===== Name =====
            TextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Item name", color = Stone) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = textFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            )

            Spacer(Modifier.height(14.dp))

            // ===== Price =====
            TextField(
                value = priceText,
                onValueChange = { input ->
                    // 只允许数字
                    priceText = input.filter { it.isDigit() }
                },
                placeholder = { Text("Price", color = Stone) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(14.dp),
                colors = textFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            )

            Spacer(Modifier.height(18.dp))

            // ===== Available toggle =====
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Available",
                    color = Coffee,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = available,
                    onCheckedChange = { available = it }
                )
            }

            Spacer(Modifier.height(26.dp))

            // ===== Buttons: ADD/UPDATE + DELETE =====
            val isEdit = !itemId.isNullOrBlank()
            val primaryLabel = if (isEdit) "UPDATE" else "ADD"

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        error = null
                        val finalName = name.trim()
                        val finalPrice = priceText.toIntOrNull()

                        if (finalName.isBlank()) {
                            error = "Name cannot be empty."
                            return@Button
                        }
                        if (finalPrice == null) {
                            error = "Price must be a number."
                            return@Button
                        }

                        // Save
                        val data = hashMapOf(
                            "name" to finalName,
                            "price" to finalPrice,
                            "available" to available,
                            "imageKey" to selectedImageKey
                        )

                        if (isEdit) {
                            db.collection("shop_items")
                                .document(itemId!!)
                                .set(data)
                                .addOnSuccessListener { onSaved() }
                                .addOnFailureListener { e ->
                                    error = e.message ?: "Failed to update item."
                                }
                        } else {
                            // ✅ 新增：用 imageKey 做 docId 会比较好（可避免重复）
                            // 但如果你想让 firebase 自动 id，可以改成 add(data)
                            val newId = selectedImageKey.removePrefix("acc_") // e.g. cap/crown
                            db.collection("shop_items")
                                .document(newId)
                                .set(data)
                                .addOnSuccessListener { onSaved() }
                                .addOnFailureListener { e ->
                                    error = e.message ?: "Failed to add item."
                                }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Paper,
                        contentColor = Coffee
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Text(primaryLabel, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                }

                Button(
                    onClick = {
                        error = null
                        if (!isEdit) {
                            // 新增模式没得删
                            error = "Nothing to delete (Add mode)."
                            return@Button
                        }
                        db.collection("shop_items")
                            .document(itemId!!)
                            .delete()
                            .addOnSuccessListener { onDeleted() }
                            .addOnFailureListener { e ->
                                error = e.message ?: "Failed to delete item."
                            }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE1E1DB),
                        contentColor = Coffee
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Text("DELETE", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            if (error != null) {
                Spacer(Modifier.height(14.dp))
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

/**
 * M3 TextField color helper (keep your theme look)
 */
@Composable
private fun textFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Paper,
    unfocusedContainerColor = Paper,
    disabledContainerColor = Paper,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    cursorColor = Coffee,
    focusedTextColor = Coffee,
    unfocusedTextColor = Coffee
)

/**
 * Small await helper without adding kotlinx-coroutines-play-services import requirement in this file.
 * If you already use `kotlinx.coroutines.tasks.await`, you can delete this and use await() directly.
 */
private suspend fun <T> com.google.android.gms.tasks.Task<T>.awaitCompat(): T {
    return kotlinx.coroutines.suspendCancellableCoroutine { cont ->
        addOnSuccessListener { cont.resume(it) {} }
        addOnFailureListener { cont.resumeWith(Result.failure(it)) }
    }
}
