package com.example.studify.presentation.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studify.ui.theme.Coffee
import com.example.studify.ui.theme.Cream
import com.example.studify.ui.theme.Paper
import com.google.firebase.firestore.FirebaseFirestore

private val IMAGE_KEY_CHOICES = listOf(
    "acc_gojo",
    "acc_cap",
    "acc_crown",
    "acc_shades",
    "acc_beanie",
    "acc_love",
    "acc_gradcap",
    "acc_magichat",
    "acc_bowtie",
    "acc_tie",
    "acc_star"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopItemEditScreen(
    itemId: String?, // Edit: doc.id (e.g. "beanie"). Add: null
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onDeleted: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    var expanded by remember { mutableStateOf(false) }
    var selectedImageKey by remember { mutableStateOf(IMAGE_KEY_CHOICES.firstOrNull() ?: "") }
    var name by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var available by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val isEdit = !itemId.isNullOrBlank()
    val primaryLabel = if (isEdit) "UPDATE" else "ADD"

    // ----- UI styles (force Coffee/Paper/Cream; no purple) -----
    val fieldShape = RoundedCornerShape(14.dp)

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Paper,
        unfocusedContainerColor = Paper,
        disabledContainerColor = Paper,

        focusedTextColor = Coffee,
        unfocusedTextColor = Coffee,
        disabledTextColor = Coffee.copy(alpha = 0.6f),

        focusedBorderColor = Coffee,
        unfocusedBorderColor = Coffee.copy(alpha = 0.35f),
        disabledBorderColor = Coffee.copy(alpha = 0.2f),

        cursorColor = Coffee
    )

    val switchColors = SwitchDefaults.colors(
        checkedTrackColor = Coffee,
        checkedThumbColor = Paper,
        uncheckedTrackColor = Paper,
        uncheckedThumbColor = Coffee
    )

    LaunchedEffect(itemId) {
        if (isEdit) {
            db.collection("shop_items")
                .document(itemId!!)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        selectedImageKey = doc.getString("imageKey") ?: selectedImageKey
                        name = doc.getString("name") ?: ""
                        priceText = (doc.getLong("price") ?: 0L).toString()
                        available = doc.getBoolean("available") ?: true
                    } else {
                        error = "Item not found."
                    }
                }
                .addOnFailureListener { e ->
                    error = e.message ?: "Failed to load item."
                }
        } else {
            selectedImageKey = IMAGE_KEY_CHOICES.firstOrNull() ?: ""
            name = ""
            priceText = ""
            available = true
            error = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Cream,
                    titleContentColor = Coffee,
                    navigationIconContentColor = Coffee
                ),
                navigationIcon = {
                    Text(
                        text = "< Back",
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .clickable { onBack() }
                    )
                }
            )
        },
        containerColor = Cream
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            // ----- Dropdown: imageKey -----
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedImageKey,
                    onValueChange = {},
                    readOnly = true,
                    shape = fieldShape,
                    colors = fieldColors,
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    IMAGE_KEY_CHOICES.forEach { key ->
                        DropdownMenuItem(
                            text = { Text(key, color = Coffee) },
                            onClick = {
                                selectedImageKey = key
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // ----- Name field -----
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                shape = fieldShape,
                colors = fieldColors,
                label = { Text("Name") },          // ✅ show "Name"
                placeholder = { Text("Name") },    // ✅ hint "Name"
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(14.dp))

            // ----- Price field -----
            OutlinedTextField(
                value = priceText,
                onValueChange = { priceText = it },
                singleLine = true,
                shape = fieldShape,
                colors = fieldColors,
                label = { Text("Price") },         // ✅ show "Price"
                placeholder = { Text("Price") },   // ✅ hint "Price"
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(14.dp))

            // ----- Available -----
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Available", color = Coffee)
                Switch(
                    checked = available,
                    onCheckedChange = { available = it },
                    colors = switchColors
                )
            }

            Spacer(Modifier.height(16.dp))

            if (error != null) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(12.dp))
            }

            // ----- Buttons -----
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
                        if (selectedImageKey.isBlank()) {
                            error = "Please select an imageKey."
                            return@Button
                        }

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
                            // Firestore doc.id is WITHOUT "acc_"
                            val newId = selectedImageKey.removePrefix("acc_")
                            val docRef = db.collection("shop_items").document(newId)

                            docRef.get()
                                .addOnSuccessListener { snap ->
                                    if (snap.exists()) {
                                        error = "This item already exists. Please use UPDATE instead."
                                        return@addOnSuccessListener
                                    }

                                    docRef.set(data)
                                        .addOnSuccessListener { onSaved() }
                                        .addOnFailureListener { e ->
                                            error = e.message ?: "Failed to add item."
                                        }
                                }
                                .addOnFailureListener { e ->
                                    error = e.message ?: "Failed to verify item existence."
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

                // DELETE only in edit mode
                if (isEdit) {
                    Button(
                        onClick = {
                            error = null
                            db.collection("shop_items")
                                .document(itemId!!)
                                .delete()
                                .addOnSuccessListener { onDeleted() }
                                .addOnFailureListener { e ->
                                    error = e.message ?: "Failed to delete item."
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
                        Text("DELETE", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}
