package com.binodnagarkoti.intervalwalktracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.binodnagarkoti.intervalwalktracker.ui.components.ProgressCard
import com.binodnagarkoti.intervalwalktracker.viewmodel.WorkoutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    viewModel: WorkoutViewModel,
    onReturn: () -> Unit
) {
    val steps by viewModel.summarySteps.collectAsState()
    val completedSets by viewModel.summaryCompletedSets.collectAsState()
    val totalSets by viewModel.totalSets.collectAsState()
    val durationMinutes by viewModel.summaryDurationMinutes.collectAsState()
    val fastMinutes by viewModel.summaryFastMinutes.collectAsState()
    val slowMinutes by viewModel.summarySlowMinutes.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Workout Summary") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Great Job!",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Box(modifier = Modifier.weight(1f)) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    item { ProgressCard(title = "Total Steps", value = steps.toString()) }
                    item { ProgressCard(title = "Sets Completed", value = "$completedSets / $totalSets") }
                    item { ProgressCard(title = "Total Duration", value = durationMinutes.toString(), unit = "m") }
                    item { ProgressCard(title = "Fast Walk", value = fastMinutes.toString(), unit = "m") }
                    item { ProgressCard(title = "Slow Walk", value = slowMinutes.toString(), unit = "m") }
                }
            }
            
            Button(
                onClick = onReturn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("DONE", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
