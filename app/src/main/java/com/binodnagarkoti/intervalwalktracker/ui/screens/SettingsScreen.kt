package com.binodnagarkoti.intervalwalktracker.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.binodnagarkoti.intervalwalktracker.ui.components.AppPrimary
import com.binodnagarkoti.intervalwalktracker.viewmodel.DashboardViewModel
import com.binodnagarkoti.intervalwalktracker.viewmodel.SettingsViewModel
import com.binodnagarkoti.intervalwalktracker.viewmodel.UnitSystem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: DashboardViewModel,
    settingsViewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val appVersion = "1.1.4"
    val targetSets by viewModel.targetSets.collectAsState()
    
    val fastVal by viewModel.fastIntervalValue.collectAsState()
    val fastUnit by viewModel.fastIntervalUnit.collectAsState()
    val slowVal by viewModel.slowIntervalValue.collectAsState()
    val slowUnit by viewModel.slowIntervalUnit.collectAsState()
    
    val isDarkTheme by settingsViewModel.isDarkTheme.collectAsState()
    val isAudioFeedbackEnabled by settingsViewModel.isAudioFeedbackEnabled.collectAsState()
    val isVibrationEnabled by settingsViewModel.isVibrationEnabled.collectAsState()
    val unitSystem by settingsViewModel.unitSystem.collectAsState()
    val exportStatus by settingsViewModel.exportStatus.collectAsState()
    
    var showVersionDialog by remember { mutableStateOf(false) }
    var showUnitDialog by remember { mutableStateOf(false) }

    // CSV Export Launcher
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
        onResult = { uri ->
            uri?.let { settingsViewModel.exportDatabaseToCsv(it) }
        }
    )

    // CSV Import Launcher - Improved with OpenDocument for better file selection
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let { settingsViewModel.importCsvToDatabase(it) }
        }
    )

    // Handle Export/Import Status Toasts
    LaunchedEffect(exportStatus) {
        exportStatus?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            settingsViewModel.clearStatus()
        }
    }

    if (showVersionDialog) {
        AlertDialog(
            onDismissRequest = { showVersionDialog = false },
            title = { Text("Version Info - v$appVersion", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("New Features:", fontWeight = FontWeight.Bold, color = AppPrimary)
                    BulletPoint("Data Portability: Export your walk sessions to CSV and import them back.")
                    BulletPoint("Improved File Selection: Better support for finding CSV files during import.")
                    BulletPoint("Configurable Intervals: Set durations in seconds or minutes.")
                }
            },
            confirmButton = {
                TextButton(onClick = { showVersionDialog = false }) {
                    Text("Got it", color = AppPrimary)
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    if (showUnitDialog) {
        AlertDialog(
            onDismissRequest = { showUnitDialog = false },
            title = { Text("Select Unit System", fontWeight = FontWeight.Bold) },
            text = {
                Column(Modifier.selectableGroup()) {
                    UnitSystem.entries.forEach { system ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .selectable(
                                    selected = (system == unitSystem),
                                    onClick = { 
                                        settingsViewModel.setUnitSystem(system)
                                        showUnitDialog = false
                                    },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (system == unitSystem),
                                onClick = null,
                                colors = RadioButtonDefaults.colors(selectedColor = AppPrimary)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(text = system.label, style = MaterialTheme.typography.bodyLarge)
                                Text(text = system.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showUnitDialog = false }) {
                    Text("Cancel", color = AppPrimary)
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text(
                text = "Workout Configuration",
                style = MaterialTheme.typography.labelLarge,
                color = AppPrimary,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Configuration for Sets
            ConfigCard(
                title = "Total Sets",
                value = "$targetSets Sets",
                icon = Icons.Default.SettingsSuggest,
                description = "Adjust the number of sets for your workout session."
            ) {
                Slider(
                    value = targetSets.toFloat(),
                    onValueChange = { viewModel.setTargetSets(it.toInt()) },
                    valueRange = 1f..10f,
                    steps = 8,
                    colors = SliderDefaults.colors(thumbColor = AppPrimary, activeTrackColor = AppPrimary)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Fast Interval Config
            IntervalConfigCard(
                title = "Fast Walk Duration",
                value = fastVal,
                unit = fastUnit,
                onValueChange = { viewModel.setFastInterval(it, fastUnit) },
                onUnitChange = { viewModel.setFastInterval(fastVal, it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Slow Interval Config
            IntervalConfigCard(
                title = "Slow Walk Duration",
                value = slowVal,
                unit = slowUnit,
                onValueChange = { viewModel.setSlowInterval(it, slowUnit) },
                onUnitChange = { viewModel.setSlowInterval(slowVal, it) }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Data Management",
                style = MaterialTheme.typography.labelLarge,
                color = AppPrimary,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            SettingsItem(
                title = "Export to CSV",
                subtitle = "Save workout history to a file",
                icon = Icons.Default.FileDownload,
                onClick = { exportLauncher.launch("walk_sessions_${System.currentTimeMillis()}.csv") }
            )

            SettingsItem(
                title = "Import from CSV",
                subtitle = "Restore workout history from a file",
                icon = Icons.Default.FileUpload,
                onClick = { 
                    // Using a broader set of MIME types to ensure CSV files are selectable
                    importLauncher.launch(arrayOf(
                        "text/csv", 
                        "text/comma-separated-values", 
                        "application/csv", 
                        "application/vnd.ms-excel",
                        "text/plain",
                        "*/*" 
                    )) 
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "General",
                style = MaterialTheme.typography.labelLarge,
                color = AppPrimary,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SettingsItem(
                title = "Dark Theme",
                subtitle = if (isDarkTheme) "Enabled" else "Disabled",
                icon = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                trailing = { 
                    Switch(
                        checked = isDarkTheme, 
                        onCheckedChange = { settingsViewModel.toggleTheme() }
                    ) 
                }
            )

            SettingsItem(
                title = "Units",
                subtitle = "${unitSystem.label} (${unitSystem.description})",
                icon = Icons.Default.Straighten,
                onClick = { showUnitDialog = true }
            )
            
            SettingsItem(
                title = "Audio Feedback",
                subtitle = if (isAudioFeedbackEnabled) "Enabled" else "Disabled",
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                trailing = { 
                    Switch(
                        checked = isAudioFeedbackEnabled, 
                        onCheckedChange = { settingsViewModel.toggleAudioFeedback() }
                    ) 
                }
            )

            SettingsItem(
                title = "Vibration Alerts",
                subtitle = if (isVibrationEnabled) "Enabled" else "Disabled",
                icon = Icons.Default.Vibration,
                trailing = { 
                    Switch(
                        checked = isVibrationEnabled, 
                        onCheckedChange = { settingsViewModel.toggleVibration() }
                    ) 
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Support & About",
                style = MaterialTheme.typography.labelLarge,
                color = AppPrimary,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            SettingsItem(
                title = "Help & Support",
                subtitle = "Contact us or view FAQs",
                icon = Icons.AutoMirrored.Filled.HelpOutline,
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/BinodNagarkoti/IWT/issues"))
                    context.startActivity(intent)
                }
            )

            SettingsItem(
                title = "App Version",
                subtitle = appVersion,
                icon = Icons.Default.Info,
                onClick = { showVersionDialog = true }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Update Button
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("https://github.com/BinodNagarkoti/IWT/releases")
                        setPackage("com.android.vending")
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/BinodNagarkoti/IWT/releases"))
                        context.startActivity(browserIntent)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppPrimary)
            ) {
                Icon(Icons.Default.SystemUpdate, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Check for Updates", fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ConfigCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = AppPrimary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(title, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = value,
                    fontWeight = FontWeight.Black,
                    color = AppPrimary,
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun IntervalConfigCard(
    title: String,
    value: Int,
    unit: String,
    onValueChange: (Int) -> Unit,
    onUnitChange: (String) -> Unit
) {
    ConfigCard(
        title = title,
        value = "$value $unit",
        icon = Icons.Default.Timer,
        description = "Set the duration for this interval type."
    ) {
        Column {
            Slider(
                value = value.toFloat(),
                onValueChange = { onValueChange(it.toInt()) },
                valueRange = if (unit == "seconds") 5f..60f else 1f..10f,
                steps = if (unit == "seconds") 10 else 8,
                colors = SliderDefaults.colors(thumbColor = AppPrimary, activeTrackColor = AppPrimary)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text("Unit: ", style = MaterialTheme.typography.labelMedium)
                AssistChip(
                    onClick = { onUnitChange("seconds") },
                    label = { Text("Seconds") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (unit == "seconds") AppPrimary.copy(alpha = 0.2f) else Color.Transparent
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                AssistChip(
                    onClick = { onUnitChange("minutes") },
                    label = { Text("Minutes") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (unit == "minutes") AppPrimary.copy(alpha = 0.2f) else Color.Transparent
                    )
                )
            }
        }
    }
}

@Composable
fun BulletPoint(text: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text("• ", fontWeight = FontWeight.Black, color = AppPrimary)
        Text(text, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it },
        color = Color.Transparent
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            trailing?.invoke()
        }
    }
}
