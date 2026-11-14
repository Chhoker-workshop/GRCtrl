package com.vanguard.grc.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vanguard.grc.ui.GrcViewModel
import com.vanguard.grc.ui.screens.accelerator.AcceleratorDayScreen
import com.vanguard.grc.ui.screens.accelerator.AcceleratorScreen
import com.vanguard.grc.ui.screens.assessment.AssessmentRunnerScreen
import com.vanguard.grc.ui.screens.assessment.AssessmentsScreen
import com.vanguard.grc.ui.screens.auth.LoginScreen
import com.vanguard.grc.ui.screens.control.ControlDetailScreen
import com.vanguard.grc.ui.screens.curriculum.CurriculumScreen
import com.vanguard.grc.ui.screens.curriculum.ModuleScreen
import com.vanguard.grc.ui.screens.explorer.ExplorerScreen
import com.vanguard.grc.ui.screens.explorer.MappingDetailScreen
import com.vanguard.grc.ui.screens.glossary.GlossaryScreen
import com.vanguard.grc.ui.screens.glossary.GlossaryTermScreen
import com.vanguard.grc.ui.screens.home.HomeScreen
import com.vanguard.grc.ui.screens.interview.InterviewSessionScreen
import com.vanguard.grc.ui.screens.interview.InterviewTracksScreen
import com.vanguard.grc.ui.screens.lesson.LessonScreen
import com.vanguard.grc.ui.screens.mentor.MentorScreen
import com.vanguard.grc.ui.screens.more.MoreScreen
import com.vanguard.grc.ui.screens.paths.PathsScreen
import com.vanguard.grc.ui.screens.progress.ProgressScreen
import com.vanguard.grc.ui.screens.projects.ProjectDetailScreen
import com.vanguard.grc.ui.screens.projects.ProjectsScreen
import com.vanguard.grc.ui.screens.search.SearchScreen
import com.vanguard.grc.ui.screens.settings.SettingsScreen
import com.vanguard.grc.ui.screens.simulator.ScenarioScreen
import com.vanguard.grc.ui.screens.simulator.SimulatorScreen
import com.vanguard.grc.ui.screens.templates.TemplateDetailScreen
import com.vanguard.grc.ui.screens.templates.TemplatesScreen
import com.vanguard.grc.ui.screens.toolkit.ToolRunnerScreen
import com.vanguard.grc.ui.screens.toolkit.ToolkitScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrcNavHost(
    navController: NavHostController = rememberNavController(),
    vm: GrcViewModel = viewModel()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    val isDetail = currentRoute?.startsWith("lesson/") == true ||
        currentRoute?.startsWith("module/") == true ||
        currentRoute?.startsWith("control/") == true ||
        currentRoute?.startsWith("tool/") == true ||
        currentRoute?.startsWith("framework/") == true ||
        currentRoute?.startsWith("mapping/") == true ||
        currentRoute?.startsWith("scenario/") == true ||
        currentRoute?.startsWith("template/") == true ||
        currentRoute?.startsWith("project/") == true ||
        currentRoute?.startsWith("glossary/") == true ||
        currentRoute?.startsWith("accelerator/") == true ||
        currentRoute?.startsWith("assessment/") == true ||
        currentRoute == Routes.INTERVIEW_TRACK

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            val title = resolveTitle(currentRoute)
            AnimatedVisibility(
                visible = currentRoute != Routes.LOGIN,
                enter = slideInVertically { -it / 3 },
                exit = slideOutVertically { -it / 3 }
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    navigationIcon = {
                        if (isDetail) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate(Routes.SEARCH) }) {
                            Icon(Icons.Filled.Search, contentDescription = "Search")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    scrollBehavior = scrollBehavior,
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                )
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            val start = if (vm.settings.value.isLoggedIn) Routes.HOME else Routes.LOGIN
            NavHost(
                navController = navController,
                startDestination = start
            ) {
                composable(Routes.LOGIN) {
                    LoginScreen(
                        vm = vm,
                        onLoggedIn = {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.LOGIN) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable(Routes.HOME) {
                    HomeScreen(vm = vm, navController = navController)
                }
                composable(Routes.PATHS) {
                    PathsScreen(vm = vm, navController = navController)
                }
                composable(Routes.CURRICULUM) {
                    CurriculumScreen(vm = vm, navController = navController)
                }
                composable(
                    route = Routes.MODULE,
                    arguments = listOf(navArgument("moduleId") { type = NavType.StringType })
                ) { entry ->
                    ModuleScreen(
                        vm = vm,
                        navController = navController,
                        moduleId = entry.arguments?.getString("moduleId").orEmpty()
                    )
                }
                composable(
                    route = Routes.LESSON,
                    arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
                ) { entry ->
                    LessonScreen(
                        vm = vm,
                        navController = navController,
                        lessonId = entry.arguments?.getString("lessonId").orEmpty()
                    )
                }
                composable(
                    route = Routes.CONTROL,
                    arguments = listOf(navArgument("controlId") { type = NavType.StringType })
                ) { entry ->
                    ControlDetailScreen(
                        vm = vm,
                        navController = navController,
                        controlId = entry.arguments?.getString("controlId").orEmpty()
                    )
                }
                composable(Routes.PRACTICE_LAB) {
                    com.vanguard.grc.ui.screens.practice.PracticeLabScreen(vm = vm, navController = navController)
                }
                composable(Routes.TOOLKIT) {
                    ToolkitScreen(vm = vm, navController = navController)
                }
                composable(
                    route = Routes.TOOL,
                    arguments = listOf(navArgument("toolId") { type = NavType.StringType })
                ) { entry ->
                    ToolRunnerScreen(
                        vm = vm,
                        navController = navController,
                        toolId = entry.arguments?.getString("toolId").orEmpty()
                    )
                }
                composable(Routes.EXPLORER) {
                    ExplorerScreen(vm = vm, navController = navController)
                }
                composable(
                    route = Routes.MAPPING,
                    arguments = listOf(navArgument("mappingId") { type = NavType.StringType })
                ) { entry ->
                    MappingDetailScreen(
                        vm = vm,
                        navController = navController,
                        mappingId = entry.arguments?.getString("mappingId").orEmpty()
                    )
                }
                composable(Routes.ASSESSMENTS) {
                    AssessmentsScreen(vm = vm, navController = navController)
                }
                composable(
                    route = Routes.ASSESSMENT,
                    arguments = listOf(navArgument("assessmentId") { type = NavType.StringType })
                ) { entry ->
                    AssessmentRunnerScreen(
                        vm = vm,
                        navController = navController,
                        assessmentId = entry.arguments?.getString("assessmentId").orEmpty()
                    )
                }
                composable(Routes.SIMULATOR) {
                    SimulatorScreen(vm = vm, navController = navController)
                }
                composable(
                    route = Routes.SCENARIO,
                    arguments = listOf(navArgument("scenarioId") { type = NavType.StringType })
                ) { entry ->
                    ScenarioScreen(
                        vm = vm,
                        navController = navController,
                        scenarioId = entry.arguments?.getString("scenarioId").orEmpty()
                    )
                }
                composable(Routes.TEMPLATES) {
                    TemplatesScreen(vm = vm, navController = navController)
                }
                composable(
                    route = Routes.TEMPLATE,
                    arguments = listOf(navArgument("templateId") { type = NavType.StringType })
                ) { entry ->
                    TemplateDetailScreen(
                        vm = vm,
                        navController = navController,
                        templateId = entry.arguments?.getString("templateId").orEmpty()
                    )
                }
                composable(Routes.MENTOR) {
                    MentorScreen(vm = vm, navController = navController)
                }
                composable(Routes.PROGRESS) {
                    ProgressScreen(vm = vm, navController = navController)
                }
                composable(Routes.SETTINGS) {
                    SettingsScreen(vm = vm)
                }
                composable(Routes.INTERVIEW) {
                    InterviewTracksScreen(vm = vm, navController = navController)
                }
                composable(
                    route = Routes.INTERVIEW_TRACK,
                    arguments = listOf(navArgument("trackId") { type = NavType.StringType })
                ) { entry ->
                    InterviewSessionScreen(
                        vm = vm,
                        navController = navController,
                        trackId = entry.arguments?.getString("trackId").orEmpty()
                    )
                }
                composable(Routes.PROJECTS) {
                    ProjectsScreen(vm = vm, navController = navController)
                }
                composable(
                    route = Routes.PROJECT,
                    arguments = listOf(navArgument("projectId") { type = NavType.StringType })
                ) { entry ->
                    ProjectDetailScreen(
                        vm = vm,
                        navController = navController,
                        projectId = entry.arguments?.getString("projectId").orEmpty()
                    )
                }
                composable(Routes.ACCELERATOR) {
                    AcceleratorScreen(vm = vm, navController = navController)
                }
                composable(
                    route = Routes.ACCELERATOR_DAY,
                    arguments = listOf(navArgument("day") { type = NavType.IntType })
                ) { entry ->
                    AcceleratorDayScreen(
                        vm = vm,
                        navController = navController,
                        day = entry.arguments?.getInt("day") ?: 1
                    )
                }
                composable(Routes.GLOSSARY) {
                    GlossaryScreen(vm = vm, navController = navController)
                }
                composable(
                    route = Routes.GLOSSARY_TERM,
                    arguments = listOf(navArgument("termId") { type = NavType.StringType })
                ) { entry ->
                    GlossaryTermScreen(
                        vm = vm,
                        navController = navController,
                        termId = entry.arguments?.getString("termId").orEmpty()
                    )
                }
                composable(Routes.SEARCH) {
                    SearchScreen(vm = vm, navController = navController)
                }
                composable(Routes.MORE) {
                    MoreScreen(vm = vm, navController = navController)
                }
            }
        }
    }
}

private fun resolveTitle(route: String?): String = when {
    route == null -> "GRC VANGUARD"
    route.startsWith("lesson/") -> "Lesson"
    route.startsWith("module/") -> "Module"
    route.startsWith("control/") -> "Control"
    route.startsWith("tool/") -> "Tool"
    route.startsWith("framework/") -> "Framework"
    route.startsWith("mapping/") -> "Cross-Framework Mapping"
    route.startsWith("scenario/") -> "Scenario"
    route.startsWith("template/") -> "Template"
    route.startsWith("project/") -> "Capstone Project"
    route.startsWith("glossary/") -> "Glossary"
    route.startsWith("accelerator/") -> "Accelerator Day"
    route.startsWith("assessment/") -> "Assessment"
    route.startsWith("interview/") -> "Mock Interview"
    else -> PRIMARY_DESTINATIONS.firstOrNull { it.route == route }?.title
        ?: SECONDARY_DESTINATIONS.firstOrNull { it.route == route }?.title
        ?: "GRC VANGUARD"
}

/** Convenience extension so screens can navigate with a single call. */
fun NavHostController.navigateTo(route: String) {
    navigate(route) { launchSingleTop = true }
}
