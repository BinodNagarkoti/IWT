package com.binodnagarkoti.intervalwalktracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.binodnagarkoti.intervalwalktracker.ui.components.AppPrimary
import com.binodnagarkoti.intervalwalktracker.ui.components.BackgroundDark
import com.binodnagarkoti.intervalwalktracker.viewmodel.WorkoutViewModel
import java.util.Locale

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
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Workout Summary",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onReturn) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Share */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
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
            // Hero Section
            Surface(
                color = AppPrimary.copy(alpha = 0.1f),
                shape = CircleShape,
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = AppPrimary,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Great Job!",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "You crushed your interval walk today. You were 15% more active than yesterday.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                ),
                modifier = Modifier.widthIn(max = 300.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Primary Stats Grid
            Row(modifier = Modifier.fillMaxWidth()) {
                SummaryStatCard(
                    title = "Steps",
                    value = String.format(Locale.getDefault(), "%,d", steps),
                    icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                    trend = "+12%",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                SummaryStatCard(
                    title = "Duration",
                    value = String.format(Locale.getDefault(), "%02d:%02d", durationMinutes, 0), // Placeholder for full seconds
                    unit = "Minutes",
                    icon = Icons.Default.Timer,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                SummaryStatCard(
                    title = "Burned",
                    value = "${completedSets * 40}", // Simplified calc
                    unit = "kcal",
                    icon = Icons.Default.LocalFireDepartment,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                SummaryStatCard(
                    title = "Distance",
                    value = String.format(Locale.getDefault(), "%.1f", steps * 0.0008), // Simplified calc
                    unit = "kilometers",
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
            
            IntervalBreakdownCard(fastMins = fastMinutes, slowMinutes = slowMinutes)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Location Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
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
                    Text(text = "Sunset Park Trail", color = Color.White, fontWeight = FontWeight.Medium)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Footer Action
            Button(
                onClick = onReturn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppPrimary)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("View Full History", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SummaryStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    unit: String? = null,
    trend: String? = null
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    title.uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
            
            if (unit != null) {
                Text(
                    unit,
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                )
            }
            
            if (trend != null) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(trend, color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun IntervalBreakdownCard(fastMins: Int, slowMinutes: Int) {
    val total = (fastMins + slowMinutes).toFloat()
    val fastRatio = if (total > 0) fastMins / total else 0.65f // Fallback to design default
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Split Progress Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(modifier = Modifier.fillMaxHeight().weight(fastRatio).background(AppPrimary))
                Box(modifier = Modifier.fillMaxHeight().weight(1f - fastRatio).background(AppPrimary.copy(alpha = 0.3f)))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            BreakdownRow(
                label = "Fast Pace",
                value = "$fastMins mins",
                percentage = "${(fastRatio * 100).toInt()}%",
                subtitle = "Goal Met",
                dotColor = AppPrimary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            BreakdownRow(
                label = "Slow Pace",
                value = "$slowMinutes mins",
                percentage = "${((1f - fastRatio) * 100).toInt()}%",
                subtitle = "Recovery",
                dotColor = AppPrimary.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
fun BreakdownRow(label: String, value: String, percentage: String, subtitle: String, dotColor: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(12.dp).background(dotColor, CircleShape))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(label, fontWeight = FontWeight.Bold)
                Text(value, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)))
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(percentage, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall.copy(color = if (subtitle == "Goal Met") AppPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)))
        }
    }
}
