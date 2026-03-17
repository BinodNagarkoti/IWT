package com.binodnagarkoti.intervalwalktracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.binodnagarkoti.intervalwalktracker.ui.components.AppPrimary
import com.binodnagarkoti.intervalwalktracker.viewmodel.AggregatedSession
import com.binodnagarkoti.intervalwalktracker.viewmodel.DashboardViewModel
import com.binodnagarkoti.intervalwalktracker.viewmodel.TimeFilter
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: DashboardViewModel,
    onBack: () -> Unit,
    onViewDetail: (Int) -> Unit
) {
    val sessions by viewModel.aggregatedSessions.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val availableYears by viewModel.availableYears.collectAsState()

    var showFilterSheet by remember { mutableStateOf(false) }

    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    "Filter Sessions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Year Selection
                Text("Year", style = MaterialTheme.typography.labelLarge, color = AppPrimary)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedYear == null,
                        onClick = { viewModel.setYearFilter(null) },
                        label = { Text("All") }
                    )
                    availableYears.forEach { year ->
                        FilterChip(
                            selected = selectedYear == year,
                            onClick = { viewModel.setYearFilter(year) },
                            label = { Text(year.toString()) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Month Selection
                Text("Month", style = MaterialTheme.typography.labelLarge, color = AppPrimary)
                Box(modifier = Modifier.fillMaxWidth()) {
                    var expanded by remember { mutableStateOf(false) }
                    val months = DateFormatSymbols().months
                    
                    OutlinedCard(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(if (selectedMonth == null) "All Months" else months[selectedMonth!!])
                            Icon(Icons.Default.MoreVert, contentDescription = null)
                        }
                    }

                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(
                            text = { Text("All Months") },
                            onClick = { 
                                viewModel.setMonthFilter(null)
                                expanded = false 
                            }
                        )
                        months.filter { it.isNotEmpty() }.forEachIndexed { index, name ->
                            DropdownMenuItem(
                                text = { Text(name) },
                                onClick = { 
                                    viewModel.setMonthFilter(index)
                                    expanded = false 
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { showFilterSheet = false },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Apply Filters")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Activity History",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (selectedFilter == TimeFilter.DAILY) {
                        IconButton(onClick = { showFilterSheet = true }) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filter",
                                tint = if (selectedYear != null || selectedMonth != null) AppPrimary else LocalContentColor.current
                            )
                        }
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ScrollableTabRow(
                selectedTabIndex = TimeFilter.entries.indexOf(selectedFilter),
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = AppPrimary,
                edgePadding = 16.dp,
                divider = {}
            ) {
                TimeFilter.entries.forEach { filter ->
                    Tab(
                        selected = selectedFilter == filter,
                        onClick = { viewModel.setFilter(filter) },
                        text = { 
                            Text(
                                filter.name.lowercase().replaceFirstChar { it.uppercase() },
                                fontWeight = if (selectedFilter == filter) FontWeight.Bold else FontWeight.Medium
                            ) 
                        }
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            if (sessions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No sessions found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (selectedYear != null || selectedMonth != null) {
                            TextButton(onClick = { 
                                viewModel.setYearFilter(null)
                                viewModel.setMonthFilter(null)
                            }) {
                                Text("Clear filters", color = AppPrimary)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (selectedFilter == TimeFilter.DAILY) "DETAILED SESSIONS" else "AGGREGATED SESSIONS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            )
                            if (selectedFilter == TimeFilter.DAILY && (selectedYear != null || selectedMonth != null)) {
                                Surface(
                                    color = AppPrimary.copy(alpha = 0.1f),
                                    shape = CircleShape
                                ) {
                                    val monthName = selectedMonth?.let { DateFormatSymbols().months[it] } ?: ""
                                    val filterText = listOfNotNull(monthName, selectedYear?.toString()).joinToString(" ")
                                    Text(
                                        text = filterText,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(color = AppPrimary, fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }
                    items(sessions) { session ->
                        HistorySessionCard(
                            session = session, 
                            selectedFilter = selectedFilter,
                            onClick = { 
                                if (!session.isAggregated) {
                                    onViewDetail(session.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistorySessionCard(
    session: AggregatedSession, 
    selectedFilter: TimeFilter,
    onClick: () -> Unit
) {
    val sdfDateBadge = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val dateBadgeString = sdfDateBadge.format(Date(session.date))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f))
                            )
                        )
                )
                
                // Date Badge (Show on Daily tab as requested, or Aggregate Badge on other tabs)
                if (selectedFilter == TimeFilter.DAILY) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = CircleShape
                    ) {
                        Text(
                            text = dateBadgeString,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 9.sp
                            )
                        )
                    }
                } else if (session.isAggregated) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                        color = AppPrimary,
                        shape = CircleShape
                    ) {
                        Text(
                            text = "${session.sessionCount} SESSIONS",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp
                            )
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = if (selectedFilter == TimeFilter.DAILY) "Session at ${session.label}" else session.label, 
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (selectedFilter == TimeFilter.DAILY) "Individual Walk" else "Aggregated Summary",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                        )
                    }
                    if (!session.isAggregated) {
                        IconButton(onClick = onClick) {
                            Icon(Icons.Default.MoreVert, contentDescription = "View Details", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MiniStatItem(label = "Steps", value = String.format(Locale.getDefault(), "%,d", session.steps), modifier = Modifier.weight(1f))
                    MiniStatItem(label = "Sets", value = "${session.completedSets}/${session.totalSets}", modifier = Modifier.weight(1f))
                    MiniStatItem(label = "Time", value = "${session.durationMinutes}m", modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun MiniStatItem(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = AppPrimary)
            )
        }
    }
}
