package com.binodnagarkoti.intervalwalktracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.binodnagarkoti.intervalwalktracker.timer.TimerState
import com.binodnagarkoti.intervalwalktracker.ui.components.*
import com.binodnagarkoti.intervalwalktracker.viewmodel.WorkoutViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    viewModel: WorkoutViewModel,
    sets: Int,
    fastSeconds: Int = 180,
    slowSeconds: Int = 180,
    onBack: () -> Unit,
    onFinish: () -> Unit
) {
    val timeLeft by viewModel.timeLeft.collectAsState()
    val timerState by viewModel.timerState.collectAsState()
    val currentSet by viewModel.currentSet.collectAsState()
    val steps by viewModel.steps.collectAsState()
    val isSensorAvailable by viewModel.isSensorAvailable.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.setWorkoutConfig(sets, fastSeconds, slowSeconds)
        viewModel.startWorkout()
    }

    LaunchedEffect(timerState) {
        if (timerState == TimerState.COMPLETED) {
            onFinish()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Interval Walk Tracker",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Progress Section
            WorkoutProgressBar(currentSet = currentSet, totalSets = sets)
            
            Spacer(modifier = Modifier.height(32.dp))

            // Mode Badge
            ModeBadge(isFast = timerState == TimerState.FAST || timerState == TimerState.IDLE)

            Spacer(modifier = Modifier.height(24.dp))

            // Circular Timer
            val maxSeconds = if (timerState == TimerState.SLOW) slowSeconds else fastSeconds
            CircularTimer(
                seconds = timeLeft,
                totalSeconds = maxSeconds,
                isFast = timerState == TimerState.FAST || timerState == TimerState.IDLE
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Stats Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WorkoutStatCard(
                    title = "Steps",
                    value = steps.toString(),
                    unit = "total",
                    icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                    modifier = Modifier.weight(1f)
                )
                WorkoutStatCard(
                    title = "Set Time",
                    value = String.format(Locale.getDefault(), "%02d:%02d", timeLeft / 60, timeLeft % 60),
                    unit = "left",
                    icon = Icons.Default.Timer,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Controls
            WorkoutControls(
                isPaused = timerState == TimerState.PAUSED,
                onPauseResume = { viewModel.pauseResume() },
                onStop = { 
                    viewModel.stopWorkout()
                    onFinish()
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Map Placeholder (as seen in Stitch UI)
            MapPlaceholder()
            
            if (!isSensorAvailable) {
                Text(
                    text = "Step sensor not supported on this device",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

@Composable
fun MapPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        // Placeholder for map image/gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                    )
                )
        )
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                tint = AppPrimary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Tracking active walking route...",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Color.LightGray,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}
