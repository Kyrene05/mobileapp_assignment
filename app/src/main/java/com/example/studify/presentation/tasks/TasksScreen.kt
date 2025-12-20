@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.studify.presentation.tasks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.studify.data.task.Task
import com.example.studify.presentation.nav.BottomBar
import com.example.studify.ui.theme.Banana
import com.example.studify.ui.theme.Coffee
import com.example.studify.ui.theme.Cream
import com.example.studify.ui.theme.Paper
import com.example.studify.ui.theme.Stone

@Composable
fun TaskScreen(
    onBack: (() -> Unit)? = null,
    onNavHome: () -> Unit = {},
    onNavTasks: () -> Unit = {},
    onOpenShop: () -> Unit = {},
    onNavProfile: () -> Unit = {},
    onOpenRecord: (Task) -> Unit = {},
    vm: TaskViewModel = viewModel(factory = TaskViewModel.Factory)
) {
    // Task list from ViewModel
    val tasks by vm.tasks.collectAsState()

    var showCreate by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<Task?>(null) }
    var deleteTarget by remember { mutableStateOf<Task?>(null) }

    Scaffold(
        containerColor = Cream,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "< Back",
                        color = Coffee,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .clickable(enabled = onBack != null) {
                                onBack?.invoke()
                            }
                    )
                },
                navigationIcon = {},
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Cream,
                    titleContentColor = Coffee,
                    navigationIconContentColor = Coffee
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreate = true },
                containerColor = Banana,
                contentColor = Coffee,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
        bottomBar = {
            BottomBar(
                selected = 1,
                onSelect = { idx ->
                    when (idx) {
                        0 -> onNavHome()
                        1 -> onNavTasks()   // already here
                        2 -> onOpenShop()
                        3 -> onNavProfile()
                    }
                }
            )
        }
    ) { inner ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            if (tasks.isEmpty()) {
                Text(
                    "Tap + to create your first task",
                    color = Coffee,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 12.dp,
                        top = 12.dp,
                        end = 12.dp,
                        bottom = 84.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(tasks, key = { it.id }) { task ->
                        TaskItemSlim(
                            task = task,
                            onToggle = { vm.toggleDone(task) },
                            onEdit = { editTarget = task },
                            onDelete = { deleteTarget = task },
                            onOpenRecord = { onOpenRecord(task) }
                        )
                    }
                }
            }
        }
    }

    // ➕ Create dialog
    if (showCreate) {
        TaskEditorDialog(
            titleInit = "",
            minutesInit = "",
            onDismiss = { showCreate = false },
            onConfirm = { title, minutes, coins ->
                vm.createTask(title = title, minutes = minutes, coins = coins)
                showCreate = false
            }
        )
    }

    // ✏️ Edit dialog
    editTarget?.let { t ->
        TaskEditorDialog(
            titleInit = t.title,
            minutesInit = t.minutes.toString(),
            onDismiss = { editTarget = null },
            onConfirm = { title, minutes, coins ->
                vm.updateTask(
                    t.copy(
                        title = title,
                        minutes = minutes,
                        coins = coins
                    )
                )
                editTarget = null
            }
        )
    }

    // 🗑 Delete dialog
    deleteTarget?.let { task ->
        DeleteConfirmDialog(
            taskTitle = task.title,
            onDismiss = { deleteTarget = null },
            onConfirm = {
                vm.deleteTask(task)
                deleteTarget = null
            }
        )
    }
}

/* ------------------------ Item / Dialog ------------------------ */

@Composable
private fun TaskItemSlim(
    task: Task,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onOpenRecord: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 88.dp)
            .clickable { onOpenRecord() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Paper
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Coffee
                )

                Spacer(Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${task.minutes} mins",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Coffee.copy(alpha = 0.85f)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "\uD83D\uDCB0 ${task.coins}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Coffee.copy(alpha = 0.85f)
                    )
                }

                // ⭐ New statistics line: only show if there was at least one session
                if (task.focusSessions > 0) {
                    Spacer(Modifier.height(4.dp))

                    val lastStatus = if (task.lastFocusMinutes >= task.minutes) {
                        "Completed full session"
                    } else {
                        "Finished early"
                    }

                    Text(
                        text = "Total: ${task.totalFocusMinutes} mins · ${task.focusSessions} sessions\n" +
                                "Last: ${task.lastFocusMinutes} mins ($lastStatus)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Coffee.copy(alpha = 1.5f)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Coffee)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Coffee)
                }
            }
        }
    }
}

@Composable
private fun TaskEditorDialog(
    titleInit: String,
    minutesInit: String,
    onDismiss: () -> Unit,
    onConfirm: (title: String, minutes: Int, coins: Int) -> Unit
) {
    val minuteOptions = listOf(10, 20, 30, 40, 50, 60)

    fun closestOption(value: Int): Int =
        minuteOptions.minBy { kotlin.math.abs(it - value) }

    var title by remember { mutableStateOf(titleInit) }
    val initialMinutes = minutesInit.toIntOrNull()?.let { closestOption(it) } ?: 25
    var minutes by remember { mutableStateOf(closestOption(initialMinutes)) }

    val coins = minutes * 10
    val canSave = title.isNotBlank()

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            Surface(
                color = Cream,
                shape = RoundedCornerShape(24.dp),
                tonalElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Task name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Coffee,
                            unfocusedBorderColor = Coffee.copy(alpha = 0.8f),
                            cursorColor = Coffee,
                            focusedLabelColor = Coffee,
                            unfocusedLabelColor = Coffee.copy(alpha = 0.8f)
                        )
                    )
                    Spacer(Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Focus time", color = Coffee)
                            Spacer(Modifier.height(8.dp))
                            Surface(
                                color = Paper,
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, Stone.copy(alpha = 0.4f)),
                                modifier = Modifier.size(width = 96.dp, height = 72.dp)
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        text = "$minutes",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Coffee
                                    )
                                    Text(
                                        text = "mins",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Coffee.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Reward", color = Coffee)
                            Spacer(Modifier.height(8.dp))
                            Surface(
                                color = Paper,
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, Stone.copy(alpha = 0.4f)),
                                modifier = Modifier.size(width = 96.dp, height = 72.dp)
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        text = coins.toString(),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Coffee
                                    )
                                    Text(
                                        text = "coins",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Coffee.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Text("Focus length", color = Coffee.copy(alpha = 0.9f))
                    Spacer(Modifier.height(6.dp))

                    Slider(
                        value = minutes.toFloat(),
                        onValueChange = { raw ->
                            val snapped = (raw / 10f).toInt() * 10
                            minutes = closestOption(snapped)
                        },
                        valueRange = 10f..60f,
                        steps = 4,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = Banana,
                            activeTrackColor = Banana,
                            inactiveTrackColor = Banana.copy(alpha = 0.4f),
                            activeTickColor = Coffee,
                            inactiveTickColor = Coffee.copy(alpha = 0.4f)
                        )
                    )

                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("10 mins", color = Stone, style = MaterialTheme.typography.labelSmall)
                        Text("60 mins", color = Stone, style = MaterialTheme.typography.labelSmall)
                    }

                    Spacer(Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = Coffee)
                        }
                        Button(
                            onClick = { onConfirm(title, minutes, coins) },
                            enabled = canSave,
                            colors = ButtonDefaults.buttonColors(containerColor = Banana),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Text("Create", color = Coffee)
                        }
                    }
                }
            }

            Surface(
                color = Banana,
                shape = CircleShape,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .size(64.dp)
                    .offset(y = (-8).dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("\uD83D\uDC31", fontSize = MaterialTheme.typography.titleLarge.fontSize)
                }
            }
        }
    }
}

@Composable
private fun DeleteConfirmDialog(
    taskTitle: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            Surface(
                color = Cream,
                shape = RoundedCornerShape(24.dp),
                tonalElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Delete task?",
                        color = Coffee,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "\"$taskTitle\" will be removed permanently.",
                        color = Coffee.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = Coffee)
                        }
                        Button(
                            onClick = onConfirm,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Banana
                            ),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Text("Delete", color = Coffee)
                        }
                    }
                }
            }

            Surface(
                color = Banana,
                shape = CircleShape,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .size(56.dp)
                    .offset(y = (-8).dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("\uD83D\uDDD1\ufe0f", fontSize = MaterialTheme.typography.titleMedium.fontSize)
                }
            }
        }
    }
}

@Preview(
    name = "TaskScreen – Preview",
    showBackground = true,
    backgroundColor = 0xFFF8E9D2,
    widthDp = 360,
    heightDp = 740
)
@Composable
private fun PreviewTaskScreen() {
    TaskScreen()
}
