package com.binodnagarkoti.intervalwalktracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.binodnagarkoti.intervalwalktracker.data.database.WalkSession
import com.binodnagarkoti.intervalwalktracker.ui.components.AppPrimary
import com.binodnagarkoti.intervalwalktracker.ui.components.BackgroundDark
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDetailScreen(
    session: WalkSession,
    onBack: () -> Unit
) {
    val sdfDate = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault())
    val sdfTime = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val dateString = sdfDate.format(Date(session.date))
    val timeString = sdfTime.format(Date(session.date))

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Session Details", fontWeight = FontWeight.Bold) },
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
            // Header Info
            Text(
                text = dateString,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Started at $timeString",
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Main Stats Card (like in Summary)
            Row(modifier = Modifier.fillMaxWidth()) {
                SummaryStatCard(
                    title = "Total Steps",
                    value = String.format(Locale.getDefault(), "%,d", session.steps),
                    icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                SummaryStatCard(
                    title = "Duration",
                    value = "${session.durationMinutes}:00",
                    unit = "Minutes",
                    icon = Icons.Default.Timer,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                SummaryStatCard(
                    title = "Energy",
                    value = "${session.completedSets * 40}",
                    unit = "kcal",
                    icon = Icons.Default.LocalFireDepartment,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                SummaryStatCard(
                    title = "Distance",
                    value = String.format(Locale.getDefault(), "%.2f", session.steps * 0.0008),
                    unit = "km",
                    icon = Icons.Default.Hiking,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Interval Breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Analytics, contentDescription = null, tint = AppPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Interval Breakdown",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            IntervalBreakdownCard(fastMins = session.fastMinutes, slowMinutes = session.slowMinutes)

            Spacer(modifier = Modifier.height(32.dp))

            // Map Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, BackgroundDark.copy(alpha = 0.8f))
                            )
                        )
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = AppPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Recorded Route Path", color = Color.White, fontWeight = FontWeight.Medium)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Delete Action (Placeholder for now)
            OutlinedButton(
                onClick = { /* TODO: Implement delete */ },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete This Session")
            }
        }
    }
}
