package com.binodnagarkoti.intervalwalktracker

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.binodnagarkoti.intervalwalktracker.service.WorkoutService
import com.binodnagarkoti.intervalwalktracker.ui.navigation.AppNavigation
import com.binodnagarkoti.intervalwalktracker.ui.navigation.Screen
import com.binodnagarkoti.intervalwalktracker.ui.theme.IntervalWalkTrackerTheme
import com.binodnagarkoti.intervalwalktracker.viewmodel.DashboardViewModel
import com.binodnagarkoti.intervalwalktracker.viewmodel.SettingsViewModel
import com.binodnagarkoti.intervalwalktracker.viewmodel.WorkoutViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var isBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            isBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val activityRecognitionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions[Manifest.permission.ACTIVITY_RECOGNITION] ?: false
        } else true

        val notificationsGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions[Manifest.permission.POST_NOTIFICATIONS] ?: false
        } else true

        if (!activityRecognitionGranted) {
            Toast.makeText(
                this,
                "Step tracking requires Activity Recognition permission.",
                Toast.LENGTH_LONG
            ).show()
        }

        if (!notificationsGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(
                this,
                "Notifications are recommended to track workouts in the background.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkPermissions()

        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val isDarkTheme by settingsViewModel.isDarkTheme.collectAsState()

            IntervalWalkTrackerTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                val showBottomBar = when (currentDestination?.route) {
                    Screen.Dashboard.route, 
                    Screen.History.route, 
                    "coming_soon/Training Plans", 
                    Screen.Settings.route -> true
                    else -> false
                }

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 8.dp
                            ) {
                                NavigationBarItem(
                                    selected = currentDestination?.hierarchy?.any { it.route == Screen.Dashboard.route } == true,
                                    onClick = {
                                        navController.navigate(Screen.Dashboard.route) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                    label = { Text("Home", fontSize = 10.sp) }
                                )
                                NavigationBarItem(
                                    selected = currentDestination?.hierarchy?.any { it.route == Screen.History.route } == true,
                                    onClick = {
                                        navController.navigate(Screen.History.route) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                                    label = { Text("History", fontSize = 10.sp) }
                                )
                                NavigationBarItem(
                                    selected = currentDestination?.route == "coming_soon/Training Plans",
                                    onClick = {
                                        navController.navigate(Screen.ComingSoon.createRoute("Training Plans")) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(Icons.AutoMirrored.Filled.EventNote, contentDescription = "Plans") },
                                    label = { Text("Plans", fontSize = 10.sp) }
                                )
                                NavigationBarItem(
                                    selected = currentDestination?.hierarchy?.any { it.route == Screen.Settings.route } == true,
                                    onClick = {
                                        navController.navigate(Screen.Settings.route) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                    label = { Text("Settings", fontSize = 10.sp) }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        val dashboardViewModel: DashboardViewModel = hiltViewModel()
                        val workoutViewModel: WorkoutViewModel = hiltViewModel()

                        AppNavigation(
                            navController = navController,
                            dashboardViewModel = dashboardViewModel,
                            workoutViewModel = workoutViewModel,
                            settingsViewModel = settingsViewModel
                        )
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this, WorkoutService::class.java).also { intent ->
            bindService(intent, connection, BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }

    private fun checkPermissions() {
        val permissions = mutableListOf<String>()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACTIVITY_RECOGNITION)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val toRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (toRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(toRequest.toTypedArray())
        }
    }
}
