package com.binodnagarkoti.intervalwalktracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.binodnagarkoti.intervalwalktracker.ui.components.ModeIndicator
import com.binodnagarkoti.intervalwalktracker.ui.components.StepCounter
import com.binodnagarkoti.intervalwalktracker.ui.components.TimerDisplay
import com.binodnagarkoti.intervalwalktracker.timer.TimerState
import com.binodnagarkoti.intervalwalktracker.viewmodel.WorkoutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    viewModel: WorkoutViewModel,
    sets: Int,
    onFinish: () -> Unit
) {
    val timeLeft by viewModel.timeLeft.collectAsState()
    val timerState by viewModel.timerState.collectAsState()
    val currentSet by viewModel.currentSet.collectAsState()
    val steps by viewModel.steps.collectAsState()
    val alertMessage by viewModel.alertMessage.collectAsState()
    val isSensorAvailable by viewModel.isSensorAvailable.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.startWorkout(sets)
    }

    LaunchedEffect(timerState) {
        if (timerState == TimerState.COMPLETED) {
            onFinish()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Workout - Set $currentSet / $sets") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!isSensorAvailable) {
                Text(
                    text = "Step sensor not supported on this device",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            ModeIndicator(isFast = timerState == TimerState.FAST)
            
            alertMessage?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            TimerDisplay(seconds = timeLeft)
            
            Spacer(modifier = Modifier.height(48.dp))
            
            StepCounter(steps = steps)
            
            Spacer(modifier = Modifier.height(64.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { viewModel.pauseResume() },
                    modifier = Modifier.weight(1f).padding(8.dp)
                ) {
                    Text(if (timerState == TimerState.PAUSED) "RESUME" else "PAUSE")
                }
                
                OutlinedButton(
                    onClick = { 
                        viewModel.stopWorkout()
                        onFinish()
                    },
                    modifier = Modifier.weight(1f).padding(8.dp)
                ) {
                    Text("STOP")
                }
            }
        }
    }
}
