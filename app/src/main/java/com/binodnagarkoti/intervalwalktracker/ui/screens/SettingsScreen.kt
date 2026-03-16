package com.binodnagarkoti.intervalwalktracker.ui.screens

import android.content.Intent
import android.net.Uri
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
    val appVersion = "1.1.1"
    val targetSets by viewModel.targetSets.collectAsState()
    val isDarkTheme by settingsViewModel.isDarkTheme.collectAsState()
    val isAudioFeedbackEnabled by settingsViewModel.isAudioFeedbackEnabled.collectAsState()
    val isVibrationEnabled by settingsViewModel.isVibrationEnabled.collectAsState()
    val unitSystem by settingsViewModel.unitSystem.collectAsState()
    
    var showVersionDialog by remember { mutableStateOf(false) }
    var showUnitDialog by remember { mutableStateOf(false) }

    if (showVersionDialog) {
        AlertDialog(
            onDismissRequest = { showVersionDialog = false },
            title = { Text("Version Info - v$appVersion", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("New Features:", fontWeight = FontWeight.Bold, color = AppPrimary)
                    BulletPoint("Unit System: Switch between Metric (km) and Imperial (miles).")
                    BulletPoint("Edge-to-Edge: Modernized UI with full screen support.")
                    BulletPoint("Functional Settings: Toggles for Audio and Vibration are now active.")
                    BulletPoint("Quick Settings: Added gear icon to dashboard for faster access.")
                    BulletPoint("Stability: Enhanced Hilt dependency injection for better performance.")
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text("Bugs Fixed:", fontWeight = FontWeight.Bold, color = AppPrimary)
                    BulletPoint("Navigation: Resolved 'onNavigateToFeature' crash in AppNavigation.")
                    BulletPoint("Deprecation Cleanup: Migrated to latest compilerOptions and UI APIs.")
                    BulletPoint("Theme Engine: Fixed status bar color glitches in dark/light mode.")
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
                            Icon(Icons.Default.SettingsSuggest, contentDescription = null, tint = AppPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Total Sets", fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "$targetSets Sets",
                            fontWeight = FontWeight.Black,
                            color = AppPrimary,
                            fontSize = 18.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Adjust the number of 6-minute intervals for your workout session.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Slider(
                        value = targetSets.toFloat(),
                        onValueChange = { viewModel.setTargetSets(it.toInt()) },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = AppPrimary,
                            activeTrackColor = AppPrimary,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("1 Set", style = MaterialTheme.typography.labelSmall)
                        Text("10 Sets", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

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
