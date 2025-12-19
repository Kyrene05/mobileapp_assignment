@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.studify.presentation.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studify.ui.theme.Coffee
import com.example.studify.ui.theme.Cream
import com.example.studify.ui.theme.Paper
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

// --- DATA CLASSES ---
data class UserRegisterInfo(
    val uid: String,
    val username: String,
    val registerDate: String,
    val rawDate: Date?
)

data class ItemSalesInfo(
    val itemName: String,
    val count: Int,
    val revenue: Int
)

@Composable
fun ReportScreen(onBackClick: () -> Unit) {
    val db = remember { FirebaseFirestore.getInstance() }

    // Data State
    var allUsers by remember { mutableStateOf(listOf<UserRegisterInfo>()) }
    var allTransactions by remember { mutableStateOf(listOf<Triple<String, Int, Date?>>()) }
    var loading by remember { mutableStateOf(false) }

    // Filter State
    val months = listOf("All Time", "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December")
    var expanded by remember { mutableStateOf(false) }
    var selectedMonthIndex by remember { mutableIntStateOf(0) }

    // Helper for Month Filtering
    fun isInSelectedMonth(date: Date?, targetIndex: Int): Boolean {
        if (targetIndex == 0) return true
        if (date == null) return false
        val cal = Calendar.getInstance().apply { time = date }
        return cal.get(Calendar.MONTH) == (targetIndex - 1)
    }

    // Fetch Data from Firebase
    LaunchedEffect(Unit) {
        loading = true
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

            // 1. Fetch Users
            val userSnap = db.collection("users")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get().await()

            allUsers = userSnap.documents.mapNotNull { doc ->
                try {
                    val date = doc.getTimestamp("createdAt")?.toDate()
                    UserRegisterInfo(
                        uid = doc.id,
                        username = doc.getString("username") ?: "Unknown",
                        registerDate = if (date != null) dateFormat.format(date) else "N/A",
                        rawDate = date
                    )
                } catch (e: Exception) { null }
            }

            // 2. Fetch Transactions
            val txSnap = db.collection("transactions").get().await()
            allTransactions = txSnap.documents.mapNotNull { doc ->
                try {
                    val type = doc.getString("type") ?: ""
                    // We only process BUY and SELL types for the report
                    if (type == "BUY" || type == "SELL") {
                        Triple(
                            doc.getString("itemName") ?: "Unknown Item",
                            doc.getLong("price")?.toInt() ?: 0,
                            doc.getTimestamp("createdAt")?.toDate()
                        )
                    } else null
                } catch (e: Exception) { null }
            }
        } catch (e: Exception) {
            // Log error or show snackbar if needed
        } finally {
            loading = false
        }
    }

    // Filter Logic
    val filteredUsers = remember(allUsers, selectedMonthIndex) {
        allUsers.filter { isInSelectedMonth(it.rawDate, selectedMonthIndex) }
    }

    val filteredSales = remember(allTransactions, selectedMonthIndex) {
        val list = allTransactions.filter { isInSelectedMonth(it.third, selectedMonthIndex) }
        if (list.isEmpty()) {
            emptyList()
        } else {
            list.groupBy { it.first }
                .map { (name, items) ->
                    ItemSalesInfo(
                        itemName = name,
                        count = items.size,
                        revenue = items.sumOf { it.second } // SELL price is negative, so this math works
                    )
                }
                .sortedByDescending { it.revenue }
        }
    }

    Scaffold(
        containerColor = Cream,
        topBar = {
            TopAppBar(
                title = { Text("Summary Report", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    // FIX: Use IconButton to prevent the large padding mis-click
                    IconButton(onClick = onBackClick) {
                        Text("<", fontWeight = FontWeight.ExtraBold, color = Coffee, fontSize = 24.sp)
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = Coffee)
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(Paper)
                        ) {
                            months.forEachIndexed { index, month ->
                                DropdownMenuItem(
                                    text = { Text(month, color = Coffee) },
                                    onClick = {
                                        selectedMonthIndex = index
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Cream,
                    titleContentColor = Coffee
                )
            )
        }
    ) { inner ->
        if (loading) {
            Box(Modifier.fillMaxSize().padding(inner), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Coffee)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(inner),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Viewing: ${months[selectedMonthIndex]}",
                        style = MaterialTheme.typography.labelLarge,
                        color = Coffee.copy(alpha = 0.7f)
                    )
                }

                // Financial Summary Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Paper)
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Text("Total Revenue", color = Coffee, style = MaterialTheme.typography.titleSmall)
                            // Total revenue logic handles zero case safely
                            val totalRev = filteredSales.sumOf { it.revenue }
                            Text(
                                "$totalRev Coins",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = Coffee
                            )
                            Spacer(Modifier.height(8.dp))
                            Divider(color = Coffee.copy(alpha = 0.1f))
                            Spacer(Modifier.height(8.dp))
                            Text("Total Sales: ${filteredSales.sumOf { it.count }} items", color = Coffee)
                            Text("New Users: ${filteredUsers.size}", color = Coffee)
                        }
                    }
                }

                item { Text("Sales Performance", color = Coffee, fontWeight = FontWeight.Bold, fontSize = 18.sp) }

                if (filteredSales.isEmpty()) {
                    item { Text("No sales data for this period.", color = Coffee.copy(alpha = 0.5f), fontSize = 14.sp) }
                }

                items(filteredSales) { sale ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Paper)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(sale.itemName, color = Coffee, fontWeight = FontWeight.Bold)
                                Text("${sale.count} units", color = Coffee.copy(alpha = 0.6f), fontSize = 12.sp)
                            }
                            Text("${sale.revenue} Coins", color = Coffee, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }

                item { Text("User Registrations", color = Coffee, fontWeight = FontWeight.Bold, fontSize = 18.sp) }

                items(filteredUsers) { user ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Paper)
                    ) {
                        ListItem(
                            headlineContent = { Text(user.username, color = Coffee, fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text(user.registerDate, color = Coffee.copy(alpha = 0.6f)) },
                            colors = ListItemDefaults.colors(containerColor = Paper)
                        )
                    }
                }
            }
        }
    }
}