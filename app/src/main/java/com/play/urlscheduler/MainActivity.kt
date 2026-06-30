package com.play.urlscheduler

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.play.urlscheduler.core.ui.theme.URLRotatorTheme
import com.play.urlscheduler.feature.home.HomeScreen
import com.play.urlscheduler.feature.jobs.CreateEditJobScreen
import com.play.urlscheduler.feature.jobs.JobsScreen
import com.play.urlscheduler.feature.settings.SettingsScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            URLRotatorTheme {
                MainScreen()
            }
        }
    }
}

sealed class Screen(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Screen("home", "Dashboard", Icons.Filled.Home)
    object Jobs : Screen("jobs", "Jobs", Icons.Filled.List)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val items = listOf(Screen.Home, Screen.Jobs, Screen.Settings)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = Screen.Home.route, Modifier.padding(innerPadding)) {
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Jobs.route) { 
                JobsScreen(
                    onNavigateToCreateJob = { navController.navigate("create_edit_job") },
                    onNavigateToEditJob = { jobId -> navController.navigate("create_edit_job?jobId=$jobId") }
                ) 
            }
            composable(
                route = "create_edit_job?jobId={jobId}",
                arguments = listOf(navArgument("jobId") { 
                    type = NavType.StringType 
                    nullable = true 
                    defaultValue = null 
                })
            ) {
                CreateEditJobScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}