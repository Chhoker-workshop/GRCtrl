package com.vanguard.grc.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Biotech
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Workspaces
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Route constants for the whole platform.
 */
object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val PATHS = "paths"
    const val CURRICULUM = "curriculum"
    const val PRACTICE_LAB = "practice"
    const val TOOLKIT = "toolkit"
    const val EXPLORER = "explorer"
    const val ASSESSMENTS = "assessments"
    const val SIMULATOR = "simulator"
    const val TEMPLATES = "templates"
    const val MENTOR = "mentor"
    const val PROGRESS = "progress"
    const val SETTINGS = "settings"
    const val INTERVIEW = "interview"
    const val PROJECTS = "projects"
    const val ACCELERATOR = "accelerator"
    const val GLOSSARY = "glossary"
    const val SEARCH = "search"
    const val MORE = "more"

    const val MODULE = "module/{moduleId}"
    const val LESSON = "lesson/{lessonId}"
    const val CONTROL = "control/{controlId}"
    const val TOOL = "tool/{toolId}"
    const val FRAMEWORK = "framework/{frameworkId}"
    const val MAPPING = "mapping/{mappingId}"
    const val SCENARIO = "scenario/{scenarioId}"
    const val TEMPLATE = "template/{templateId}"
    const val INTERVIEW_TRACK = "interview/{trackId}"
    const val PROJECT = "project/{projectId}"
    const val GLOSSARY_TERM = "glossary/{termId}"
    const val ACCELERATOR_DAY = "accelerator/{day}"
    const val ASSESSMENT = "assessment/{assessmentId}"

    fun module(id: String) = "module/$id"
    fun lesson(id: String) = "lesson/$id"
    fun control(id: String) = "control/$id"
    fun tool(id: String) = "tool/$id"
    fun framework(id: String) = "framework/$id"
    fun mapping(id: String) = "mapping/$id"
    fun scenario(id: String) = "scenario/$id"
    fun template(id: String) = "template/$id"
    fun interviewTrack(id: String) = "interview/$id"
    fun project(id: String) = "project/$id"
    fun glossaryTerm(id: String) = "glossary/$id"
    fun acceleratorDay(day: Int) = "accelerator/$day"
    fun assessment(id: String) = "assessment/$id"
}

/**
 * A destination in the platform's navigation map.
 */
data class NavDestination(
    val route: String,
    val label: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val group: NavGroup
)

enum class NavGroup(val label: String) {
    LEARN("Learn"),
    PRACTICE("Practice"),
    GOVERN("Govern"),
    TOOLS("Tools"),
    PERSONAL("Personal")
}

/**
 * The twelve primary navigation destinations.
 */
val PRIMARY_DESTINATIONS: List<NavDestination> = listOf(
    NavDestination(
        route = Routes.HOME,
        label = "Home",
        title = "Dashboard",
        description = "Continue learning, today's plan, competency and daily challenge.",
        icon = Icons.Filled.Home,
        group = NavGroup.PERSONAL
    ),
    NavDestination(
        route = Routes.PATHS,
        label = "Paths",
        title = "Learning Paths",
        description = "The 60-day accelerator, extended professional tracks and certification-aligned routes.",
        icon = Icons.Filled.AutoStories,
        group = NavGroup.LEARN
    ),
    NavDestination(
        route = Routes.CURRICULUM,
        label = "Curriculum",
        title = "Curriculum",
        description = "Thirteen knowledge modules, leadership academy and the full control library.",
        icon = Icons.Filled.Insights,
        group = NavGroup.LEARN
    ),
    NavDestination(
        route = Routes.PRACTICE_LAB,
        label = "Practice",
        title = "Practice Lab",
        description = "Question bank, scenario drills and targeted weak-area practice.",
        icon = Icons.Filled.Assignment,
        group = NavGroup.PRACTICE
    ),
    NavDestination(
        route = Routes.TOOLKIT,
        label = "Toolkit",
        title = "GRC Toolkit",
        description = "Thirty practical registers, calculators and assessment instruments.",
        icon = Icons.Filled.GridView,
        group = NavGroup.TOOLS
    ),
    NavDestination(
        route = Routes.EXPLORER,
        label = "Explorer",
        title = "Framework Explorer",
        description = "Framework registry, official sources and cross-framework mapping.",
        icon = Icons.Outlined.AccountTree,
        group = NavGroup.GOVERN
    ),
    NavDestination(
        route = Routes.ASSESSMENTS,
        label = "Assess",
        title = "Assessments",
        description = "Domain examinations, mock exams and competency certification.",
        icon = Icons.Outlined.Assessment,
        group = NavGroup.PRACTICE
    ),
    NavDestination(
        route = Routes.SIMULATOR,
        label = "Simulator",
        title = "GRC Simulator",
        description = "Vanguard Financial Services - live management decisions.",
        icon = Icons.Outlined.SportsEsports,
        group = NavGroup.PRACTICE
    ),
    NavDestination(
        route = Routes.TEMPLATES,
        label = "Templates",
        title = "Documentation Library",
        description = "Editable policies, charters, registers and report templates.",
        icon = Icons.Outlined.Article,
        group = NavGroup.TOOLS
    ),
    NavDestination(
        route = Routes.MENTOR,
        label = "Mentor",
        title = "AI GRC Mentor",
        description = "An enterprise GRC lead in your pocket. Offline by default.",
        icon = Icons.Outlined.Chat,
        group = NavGroup.LEARN
    ),
    NavDestination(
        route = Routes.PROGRESS,
        label = "Progress",
        title = "My Progress",
        description = "Competency, streaks, weak areas, revision schedule and readiness.",
        icon = Icons.Outlined.Checklist,
        group = NavGroup.PERSONAL
    ),
    NavDestination(
        route = Routes.SETTINGS,
        label = "Settings",
        title = "Settings",
        description = "Appearance, accessibility, data management and mentor configuration.",
        icon = Icons.Outlined.Settings,
        group = NavGroup.PERSONAL
    )
)

/** Destinations surfaced directly on the bottom navigation bar. */
val BOTTOM_BAR_DESTINATIONS = listOf(
    Routes.HOME,
    Routes.PATHS,
    Routes.TOOLKIT,
    Routes.ASSESSMENTS,
    Routes.MORE
)

/** Additional top-level entries surfaced on the More hub. */
val SECONDARY_DESTINATIONS: List<NavDestination> = listOf(
    NavDestination(
        route = Routes.CURRICULUM,
        label = "Curriculum",
        title = "Curriculum",
        description = "Thirteen knowledge modules, leadership academy and the full control library.",
        icon = Icons.Filled.Insights,
        group = NavGroup.LEARN
    ),
    NavDestination(
        route = Routes.PRACTICE_LAB,
        label = "Practice Lab",
        title = "Practice Lab",
        description = "Question bank, scenario drills and targeted weak-area practice.",
        icon = Icons.Filled.Assignment,
        group = NavGroup.PRACTICE
    ),
    NavDestination(
        route = Routes.EXPLORER,
        label = "Framework Explorer",
        title = "Framework Explorer",
        description = "Framework registry, official sources and cross-framework mapping.",
        icon = Icons.Outlined.AccountTree,
        group = NavGroup.GOVERN
    ),
    NavDestination(
        route = Routes.SIMULATOR,
        label = "GRC Simulator",
        title = "GRC Simulator",
        description = "Vanguard Financial Services - live management decisions.",
        icon = Icons.Outlined.SportsEsports,
        group = NavGroup.PRACTICE
    ),
    NavDestination(
        route = Routes.TEMPLATES,
        label = "Templates",
        title = "Documentation Library",
        description = "Editable policies, charters, registers and report templates.",
        icon = Icons.Outlined.Article,
        group = NavGroup.TOOLS
    ),
    NavDestination(
        route = Routes.MENTOR,
        label = "AI GRC Mentor",
        title = "AI GRC Mentor",
        description = "An enterprise GRC lead in your pocket. Offline by default.",
        icon = Icons.Outlined.Chat,
        group = NavGroup.LEARN
    ),
    NavDestination(
        route = Routes.INTERVIEW,
        label = "Interview Prep",
        title = "Interview Preparation",
        description = "Twelve role tracks with model answers and mock interviews.",
        icon = Icons.Outlined.Forum,
        group = NavGroup.PRACTICE
    ),
    NavDestination(
        route = Routes.PROJECTS,
        label = "Capstone Projects",
        title = "Practical Capstone Projects",
        description = "Ten enterprise-scale projects with evaluation criteria and model solutions.",
        icon = Icons.Outlined.Workspaces,
        group = NavGroup.LEARN
    ),
    NavDestination(
        route = Routes.ACCELERATOR,
        label = "60-Day Accelerator",
        title = "60-Day GRC Accelerator",
        description = "The intensive 120-minute-a-day programme, day by day.",
        icon = Icons.Outlined.Explore,
        group = NavGroup.LEARN
    ),
    NavDestination(
        route = Routes.GLOSSARY,
        label = "Glossary",
        title = "GRC Glossary",
        description = "Core terminology with framework attribution.",
        icon = Icons.Outlined.Biotech,
        group = NavGroup.LEARN
    ),
    NavDestination(
        route = Routes.PROGRESS,
        label = "My Progress",
        title = "My Progress",
        description = "Competency, streaks, weak areas, revision schedule and readiness.",
        icon = Icons.Outlined.Checklist,
        group = NavGroup.PERSONAL
    ),
    NavDestination(
        route = Routes.SETTINGS,
        label = "Settings",
        title = "Settings",
        description = "Appearance, accessibility, data management and mentor configuration.",
        icon = Icons.Outlined.Settings,
        group = NavGroup.PERSONAL
    )
)
