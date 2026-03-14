package com.binodnagarkoti.intervalwalktracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TimerDisplay(seconds: Int) {
    val mins = seconds / 60
    val secs = seconds % 60
    Text(
        text = String.format("%02d:%02d", mins, secs),
        style = MaterialTheme.typography.displayLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 72.sp
        )
    )
}

@Composable
fun ModeIndicator(isFast: Boolean) {
    Surface(
        color = if (isFast) Color.Red else Color.Blue,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = if (isFast) "FAST WALK" else "SLOW WALK",
            color = Color.White,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Composable
fun StepCounter(steps: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Steps", style = MaterialTheme.typography.labelLarge)
        Text(
            text = steps.toString(),
            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
fun ProgressCard(title: String, value: String, unit: String = "") {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .width(160.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, style = MaterialTheme.typography.labelMedium)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )
                if (unit.isNotEmpty()) {
                    Text(
                        text = " $unit",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
        }
    }
}
