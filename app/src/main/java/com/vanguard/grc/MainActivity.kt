package com.vanguard.grc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vanguard.grc.data.settings.ThemeMode
import com.vanguard.grc.ui.GrcViewModel
import com.vanguard.grc.ui.navigation.GrcNavHost
import com.vanguard.grc.ui.navigation.Routes
import com.vanguard.grc.ui.theme.GrcVanguardTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val vm: GrcViewModel = viewModel()
            val settings by vm.settings.collectAsStateWithLifecycle()
            val dark = when (settings.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            GrcVanguardTheme(darkTheme = dark, textScale = settings.textScale) {
                MainScaffold(vm)
            }
        }
    }
}

private data class BottomItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val bottomItems = listOf(
    BottomItem(Routes.HOME, "Home", Icons.Filled.Home),
    BottomItem(Routes.PATHS, "Paths", Icons.Filled.AutoStories),
    BottomItem(Routes.TOOLKIT, "Toolkit", Icons.Filled.GridView),
    BottomItem(Routes.ASSESSMENTS, "Assess", Icons.Filled.Assignment),
    BottomItem(Routes.MORE, "More", Icons.Filled.Menu)
)

@Composable
private fun MainScaffold(vm: GrcViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            val route = currentDestination?.route
            if (route != Routes.LOGIN) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest) {
                    bottomItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        androidx.compose.foundation.layout.Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            GrcNavHost(navController = navController, vm = vm)
        }
    }
}
