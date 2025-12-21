package com.example.studify.presentation.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
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
    val scrollState = rememberScrollState()
    var expanded by remember { mutableStateOf(false) }
    var selectedImageKey by remember { mutableStateOf(IMAGE_KEY_CHOICES.firstOrNull() ?: "") }
    var name by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    // --- States for Dialogs ---
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showAddConfirmation by remember { mutableStateOf(false) }
    var showUpdateConfirmation by remember { mutableStateOf(false) } // Added for Update
    var successMessage by remember { mutableStateOf<String?>(null) }

    val isEdit = !itemId.isNullOrBlank()
    val primaryLabel = if (isEdit) "UPDATE" else "ADD"

    // ----- UI styles -----
    val fieldShape = RoundedCornerShape(14.dp)
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Paper,
        unfocusedContainerColor = Paper,
        disabledContainerColor = Paper,
        focusedTextColor = Coffee,
        unfocusedTextColor = Coffee,
        focusedBorderColor = Coffee,
        unfocusedBorderColor = Coffee.copy(alpha = 0.35f),
        cursorColor = Coffee
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
                    } else {
                        error = "Item not found."
                    }
                }
                .addOnFailureListener { e -> error = e.message ?: "Failed to load item." }
        }
    }

    // --- Helper function to perform the Firestore save ---
    fun performSaveAction() {
        error = null
        val finalName = name.trim()
        val finalPrice = priceText.toIntOrNull() ?: 0
        val data = hashMapOf(
            "name" to finalName,
            "price" to finalPrice,
            "imageKey" to selectedImageKey,
            "available" to true
        )

        if (isEdit) {
            db.collection("shop_items")
                .document(itemId!!)
                .set(data)
                .addOnSuccessListener { successMessage = "Item updated successfully!" }
                .addOnFailureListener { e -> error = e.message ?: "Failed to update item." }
        } else {
            val newId = selectedImageKey.removePrefix("acc_")
            val docRef = db.collection("shop_items").document(newId)
            docRef.get().addOnSuccessListener { snap ->
                if (snap.exists()) {
                    error = "This item already exists."
                } else {
                    docRef.set(data)
                        .addOnSuccessListener { successMessage = "Item added successfully!" }
                        .addOnFailureListener { e -> error = e.message ?: "Failed to add item." }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream),
                navigationIcon = {
                    Text("< Back", color = Coffee, modifier = Modifier.padding(16.dp).clickable { onBack() })
                }
            )
        },
        containerColor = Cream
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(scrollState).padding(16.dp)
        ) {
            // Dropdown imageKey
            ExposedDropdownMenuBox(
                expanded = if (isEdit) false else expanded,
                onExpandedChange = { if (!isEdit) expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedImageKey, onValueChange = {}, readOnly = true, enabled = !isEdit,
                    shape = fieldShape, colors = fieldColors, modifier = Modifier.menuAnchor().fillMaxWidth(),
                    label = { Text("Image Key") },
                    trailingIcon = { if (!isEdit) ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                )
                if (!isEdit) {
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, containerColor = Cream) {
                        IMAGE_KEY_CHOICES.forEach { key ->
                            DropdownMenuItem(text = { Text(key, color = Coffee) }, onClick = { selectedImageKey = key; expanded = false })
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            OutlinedTextField(value = name, onValueChange = { name = it }, singleLine = true, shape = fieldShape, colors = fieldColors, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(value = priceText, onValueChange = { priceText = it }, singleLine = true, shape = fieldShape, colors = fieldColors, label = { Text("Price") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))

            if (error != null) {
                Text(text = error!!, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
                Spacer(Modifier.height(12.dp))
            }

            // Buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = {
                        if (name.trim().isBlank()) { error = "Name cannot be empty."; return@Button }
                        if (priceText.toIntOrNull() == null) { error = "Price must be a number."; return@Button }

                        if (isEdit) showUpdateConfirmation = true else showAddConfirmation = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Paper, contentColor = Coffee),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).height(56.dp)
                ) {
                    Text(primaryLabel, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                }

                if (isEdit) {
                    Button(
                        onClick = { showDeleteConfirmation = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Paper, contentColor = Coffee),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).height(56.dp)
                    ) {
                        Text("DELETE", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }

    // --- DIALOGS ---

    if (showUpdateConfirmation) {
        AlertDialog(
            onDismissRequest = { showUpdateConfirmation = false },
            containerColor = Cream,
            title = { Text("Confirm Update", color = Coffee, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to save changes to \"$name\"?", color = Coffee) },
            confirmButton = {
                TextButton(onClick = { showUpdateConfirmation = false; performSaveAction() }) {
                    Text("UPDATE", color = Coffee, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpdateConfirmation = false }) { Text("CANCEL", color = Coffee) }
            }
        )
    }

    if (showAddConfirmation) {
        AlertDialog(
            onDismissRequest = { showAddConfirmation = false },
            containerColor = Cream,
            title = { Text("Confirm Add", color = Coffee, fontWeight = FontWeight.Bold) },
            text = { Text("Add \"$name\" to shop for $priceText coins?", color = Coffee) },
            confirmButton = {
                TextButton(onClick = { showAddConfirmation = false; performSaveAction() }) {
                    Text("ADD", color = Coffee, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddConfirmation = false }) { Text("CANCEL", color = Coffee) }
            }
        )
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            containerColor = Cream,
            title = { Text("Confirm Delete", color = Coffee, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete \"$name\"?", color = Coffee) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirmation = false
                    db.collection("shop_items").document(itemId!!).delete()
                        .addOnSuccessListener { successMessage = "Item deleted successfully!" }
                        .addOnFailureListener { e -> error = e.message }
                }) { Text("DELETE", color = Color.Red, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) { Text("CANCEL", color = Coffee) }
            }
        )
    }

    successMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { },
            containerColor = Cream,
            title = { Text("Success!", color = Coffee, fontWeight = FontWeight.Bold) },
            text = { Text(msg, color = Coffee) },
            confirmButton = {
                Button(
                    onClick = {
                        val wasDeleted = msg.contains("deleted")
                        successMessage = null
                        if (wasDeleted) onDeleted() else onSaved()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Coffee)
                ) { Text("OK", color = Cream) }
            }
        )
    }
}
