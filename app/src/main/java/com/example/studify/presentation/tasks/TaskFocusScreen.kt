@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.studify.presentation.tasks

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.studify.data.task.Task
import com.example.studify.ui.theme.Banana
import com.example.studify.ui.theme.Coffee
import com.example.studify.ui.theme.Cream
import com.example.studify.ui.theme.Stone
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/**
 * Focus screen with countdown timer.
 *
 * onFinish(elapsedMinutes, earnedExp, success):
 *  - elapsedMinutes: real focused minutes in this session
 *  - earnedExp:      XP gained (we use minutes * 10)
 *  - success:        true when user confirms the session
 */
@Composable
fun TaskFocusScreen(
    task: Task,
    onBack: () -> Unit,
    onFinish: (elapsedMinutes: Int, earnedExp: Int, success: Boolean) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    val totalSeconds = task.minutes * 60
    var remainingSeconds by remember(task.id) { mutableStateOf(totalSeconds) }
    var isRunning by remember(task.id) { mutableStateOf(true) }
    var isFinished by remember(task.id) { mutableStateOf(false) }
    var showFinishDialog by remember { mutableStateOf(false) }

    val progress by animateFloatAsState(
        targetValue = 1f - remainingSeconds / totalSeconds.toFloat(),
        animationSpec = tween(durationMillis = 300, easing = LinearEasing),
        label = "progress"
    )

    // Pause when app goes to background, resume when back to foreground.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    if (!isFinished) {
                        isRunning = true
                    }
                }

                Lifecycle.Event.ON_STOP -> {
                    isRunning = false
                }

                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Timer logic
    LaunchedEffect(isRunning, isFinished) {
        if (!isRunning || isFinished) return@LaunchedEffect

        while (remainingSeconds > 0 && isRunning) {
            delay(1000L)
            remainingSeconds--
        }

        // Countdown finished automatically, show dialog to confirm.
        if (remainingSeconds <= 0 && !isFinished) {
            isRunning = false
            isFinished = true
            showFinishDialog = true
        }
    }

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeText = String.format("%02d:%02d", minutes, seconds)
    val statusText = when {
        isFinished -> "Finished"
        !isRunning -> "Pause"
        else -> "Focus"
    }

    val elapsedSeconds = totalSeconds - remainingSeconds
    val actualMinutes = (elapsedSeconds / 60f).roundToInt()
    val gainedExp = actualMinutes * 10
    val gainedCoins = gainedExp

    Scaffold(
        containerColor = Cream,
        contentWindowInsets = WindowInsets(0.dp)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = 55.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    tonalElevation = 4.dp,
                    color = Banana.copy(alpha = 0.95f),
                    modifier = Modifier
                        .fillMaxWidth(0.80f)
                        .heightIn(min = 100.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = Coffee
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Focus ${task.minutes} min gain ${task.coins} coins",
                                style = MaterialTheme.typography.bodySmall,
                                color = Coffee.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "Reward: ${task.minutes * 10} XP",
                                style = MaterialTheme.typography.bodySmall,
                                color = Coffee.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                CircularTimer(
                    progress = progress,
                    timeText = timeText,
                    statusText = statusText,
                    modifier = Modifier.fillMaxWidth(0.75f)
                )

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Stop: pause and show confirm dialog
                    IconButton(
                        onClick = {
                            if (!isFinished) {
                                isRunning = false
                                showFinishDialog = true
                            }
                        }
                    ) {
                        Text("■", color = Coffee)
                    }

                    // Pause / Resume
                    IconButton(onClick = {
                        if (!isFinished) {
                            isRunning = !isRunning
                        }
                    }) {
                        Text(if (isRunning) "Ⅱ" else "▶", color = Coffee)
                    }
                }
            }
        }
    }

    if (showFinishDialog) {
        val sessionEnded = remainingSeconds <= 0

        AlertDialog(
            containerColor = Cream,
            onDismissRequest = { /* not cancellable by outside tap */ },
            title = {
                Text("Session Completed", color = Coffee)
            },
            text = {
                Text(
                    text = "You focused for $actualMinutes min.\n" +
                            "Gained $gainedExp XP and $gainedCoins coins.",
                    color = Stone
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showFinishDialog = false
                        isFinished = true
                        onFinish(actualMinutes, gainedExp, true)
                        onBack()
                    }
                ) {
                    Text("Finish", color = Coffee)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showFinishDialog = false
                        if (!sessionEnded) {
                            // User cancelled, allow them to resume if countdown not finished.
                            isFinished = false
                        }
                    }
                ) {
                    Text("Cancel", color = Coffee.copy(alpha = 0.5f))
                }
            }
        )
    }
}

@Composable
private fun CircularTimer(
    progress: Float,
    timeText: String,
    statusText: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize(0.9f)) {
            val stroke = 18.dp.toPx()
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(stroke / 2, stroke / 2)

            // Background circle
            drawArc(
                color = Stone.copy(alpha = 0.2f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                size = arcSize,
                topLeft = topLeft,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke)
            )

            // Progress arc
            drawArc(
                color = Coffee,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                size = arcSize,
                topLeft = topLeft,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = timeText,
                style = MaterialTheme.typography.headlineLarge,
                color = Coffee
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = statusText,
                style = MaterialTheme.typography.titleMedium,
                color = Coffee
            )
        }
    }
}
