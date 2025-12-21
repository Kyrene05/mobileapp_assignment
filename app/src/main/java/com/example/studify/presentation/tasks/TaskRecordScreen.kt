@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.studify.presentation.tasks

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.studify.data.task.Task
import com.example.studify.ui.theme.Banana
import com.example.studify.ui.theme.Coffee
import com.example.studify.ui.theme.Cream
import com.example.studify.ui.theme.Paper
import kotlinx.coroutines.launch


@Composable
fun TaskRecordScreen(
    task: Task,
    onBack: () -> Unit,
    onStartFocus: (Task) -> Unit
) {
    val vm: TaskViewModel = viewModel(factory = TaskViewModel.Factory)
    val currentSession by vm.currentSession.collectAsState()

    var notes by remember(task.id) { mutableStateOf("") }
    var photoUri by remember(task.id) { mutableStateOf<Uri?>(null) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(task.id) {
        vm.loadLatestSession(task.id)
    }


    LaunchedEffect(currentSession?.id) {
        currentSession?.let { s ->
            notes = s.notes
            photoUri = s.photoPath?.let { Uri.parse(it) }
        }
    }

    // Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            photoUri = uri
        }
    }

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
                            .clickable { onBack() }
                    )
                },
                actions = {
                    TextButton(
                        onClick = {
                            vm.saveSession(task, notes, photoUri?.toString())
                            scope.launch {
                                snackbarHostState.showSnackbar("Saved study record.")
                            }
                        }
                    ) {
                        Text("Save", color = Coffee)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Cream,
                    titleContentColor = Coffee,
                    actionIconContentColor = Coffee,
                    navigationIconContentColor = Coffee
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            /* --- Add Photo section--- */
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(Paper, RoundedCornerShape(24.dp))
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    }
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                if (photoUri != null) {
                    AsyncImage(
                        model = photoUri,
                        contentDescription = "Task photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(20.dp))
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            color = Banana,
                            shape = CircleShape,
                            shadowElevation = 2.dp,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "+",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Coffee
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Add photo",
                            color = Coffee.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Coffee
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "💰 ${task.coins} coins per session",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Coffee.copy(alpha = 0.85f)
                )
            }

            Spacer(Modifier.height(16.dp))

            /* --- Notes --- */
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                textStyle = MaterialTheme.typography.bodyMedium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Coffee,
                    unfocusedBorderColor = Coffee.copy(alpha = 0.8f),
                    cursorColor = Coffee,
                    focusedLabelColor = Coffee,
                    unfocusedLabelColor = Coffee.copy(alpha = 0.8f)
                )
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Ready to focus?",
                color = Coffee,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))

            /* --- Swipe bar--- */
            SwipeToStartBar(
                text = "Swipe to start",
                onComplete = { onStartFocus(task) }
            )
        }
    }
}

/* ---------------------- Swipe bar ---------------------- */

@Composable
private fun SwipeToStartBar(
    text: String,
    onComplete: () -> Unit
) {
    val trackColor = Banana.copy(alpha = 0.4f)
    val handleColor = Banana
    val textColor = Coffee

    var progress by remember { mutableStateOf(0f) } // 0f..1f

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(trackColor, RoundedCornerShape(28.dp))
            .padding(horizontal = 4.dp)
    ) {
        val handleSize = 52.dp
        val maxOffset = maxWidth - handleSize

        Box(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { _, dragAmount ->
                        val widthPx = maxWidth.toPx()
                        if (widthPx <= 0f) return@detectHorizontalDragGestures

                        val delta = dragAmount / widthPx
                        progress = (progress + delta).coerceIn(0f, 1f)

                        if (progress >= 0.95f) {
                            onComplete()
                            progress = 0f
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = textColor,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Box(
                modifier = Modifier
                    .size(handleSize)
                    .align(Alignment.CenterStart)
                    .offset(x = maxOffset * progress)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(handleColor, handleColor.copy(alpha = 0.9f)),
                            start = Offset.Zero,
                            end = Offset.Infinite
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .border(
                        BorderStroke(1.dp, Coffee.copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("🐱", color = Coffee, style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}


