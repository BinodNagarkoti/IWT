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
import com.binodnagarkoti.intervalwalktracker.viewmodel.DashboardViewModel
import com.binodnagarkoti.intervalwalktracker.viewmodel.TimeFilter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onStartWorkout: (Int) -> Unit,
    onViewHistory: () -> Unit
) {
    val stats by viewModel.filteredStats.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val targetSets by viewModel.targetSets.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Interval Walk Tracker") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Filter Selection
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                TimeFilter.entries.forEachIndexed { index, filter ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = TimeFilter.entries.size),
                        onClick = { viewModel.setFilter(filter) },
                        selected = selectedFilter == filter
                    ) {
                        Text(filter.name)
                    }
                }
            }

            Text(
                text = "${selectedFilter.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }} Summary",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.height(200.dp)) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    item { ProgressCard(title = "Steps", value = stats.totalSteps.toString()) }
                    item { ProgressCard(title = "Sets Completed", value = stats.completedSets.toString()) }
                    item { ProgressCard(title = "Remaining Sets", value = if (selectedFilter == TimeFilter.DAILY) stats.remainingSets.toString() else "--") }
                    item { ProgressCard(title = "Total Duration", value = stats.totalDuration.toString(), unit = "m") }
                    item { ProgressCard(title = "Fast Walk", value = stats.fastMinutes.toString(), unit = "m") }
                    item { ProgressCard(title = "Slow Walk", value = stats.slowMinutes.toString(), unit = "m") }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "Target Sets: $targetSets (${targetSets * 6} mins)", style = MaterialTheme.typography.bodyLarge)
            Slider(
                value = targetSets.toFloat(),
                onValueChange = { viewModel.setTargetSets(it.toInt()) },
                valueRange = 1f..10f,
                steps = 8
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { onStartWorkout(targetSets) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("START WORKOUT", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onViewHistory,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("VIEW HISTORY")
            }
        }
    }
}
