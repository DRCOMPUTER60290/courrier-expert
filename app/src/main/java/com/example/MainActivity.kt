package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FolderSpecial
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.local.AppDatabase
import com.example.data.repository.LetterRepository
import com.example.ui.components.AdMobBanner
import com.example.ui.screens.AiCreatorScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LetterEditorScreen
import com.example.ui.screens.MyLettersScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.CourrierExpertTheme
import com.example.ui.viewmodel.LetterViewModel
import com.example.ui.viewmodel.LetterViewModelFactory
import com.google.android.gms.ads.MobileAds

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : Screen("home", "Modèles", Icons.Filled.Description, Icons.Outlined.Description)
    object MyLetters : Screen("my_letters", "Mes Courriers", Icons.Filled.FolderSpecial, Icons.Outlined.FolderSpecial)
    object AiCreator : Screen("ai_creator", "Créateur IA", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome)
    object Profile : Screen("profile", "Mon Profil", Icons.Filled.Person, Icons.Outlined.Person)
    object Editor : Screen("editor", "Éditeur", Icons.Filled.Description, Icons.Outlined.Description)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Google Mobile Ads SDK
        MobileAds.initialize(this) {}

        setContent {
            val context = LocalContext.current
            val database = remember(context) { AppDatabase.getDatabase(context) }
            val repository = remember(database) { LetterRepository(database.letterDao()) }
            val letterViewModel: LetterViewModel = viewModel(
                factory = LetterViewModelFactory(repository)
            )
            val isDarkTheme by letterViewModel.isDarkTheme.collectAsStateWithLifecycle()

            CourrierExpertTheme(darkTheme = isDarkTheme) {
                CourrierExpertApp(letterViewModel = letterViewModel)
            }
        }
    }
}

@Composable
fun CourrierExpertApp(letterViewModel: LetterViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        Screen.Home,
        Screen.MyLetters,
        Screen.AiCreator,
        Screen.Profile
    )

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Column(modifier = Modifier.navigationBarsPadding()) {
                AdMobBanner()
                if (showBottomBar) {
                    NavigationBar(
                        modifier = Modifier.testTag("main_navigation_bar")
                    ) {
                        bottomNavItems.forEach { screen ->
                            val isSelected = currentRoute == screen.route
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                        contentDescription = screen.title
                                    )
                                },
                                label = {
                                    Text(
                                        text = screen.title,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                selected = isSelected,
                                onClick = {
                                    if (currentRoute != screen.route) {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                modifier = Modifier.testTag("nav_item_${screen.route}")
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = letterViewModel,
                    onTemplateSelected = { template ->
                        letterViewModel.loadTemplateForEditing(template)
                        navController.navigate(Screen.Editor.route)
                    },
                    onNavigateToAiCreator = {
                        navController.navigate(Screen.AiCreator.route)
                    }
                )
            }

            composable(Screen.MyLetters.route) {
                MyLettersScreen(
                    viewModel = letterViewModel,
                    onEditLetter = { letter ->
                        letterViewModel.loadLetterForEditing(letter)
                        navController.navigate(Screen.Editor.route)
                    },
                    onNavigateToNewLetter = {
                        val emptyTemplate = com.example.data.model.LetterTemplate(
                            id = "custom_new",
                            title = "Courrier personnalisé",
                            category = com.example.data.model.LetterTemplate.Category.LOGEMENT,
                            shortDescription = "",
                            defaultSubject = "",
                            defaultBody = ""
                        )
                        letterViewModel.loadTemplateForEditing(emptyTemplate)
                        navController.navigate(Screen.Editor.route)
                    }
                )
            }

            composable(Screen.AiCreator.route) {
                AiCreatorScreen(
                    viewModel = letterViewModel,
                    onGeneratedSuccessfully = {
                        navController.navigate(Screen.Editor.route)
                    }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(viewModel = letterViewModel)
            }

            composable(Screen.Editor.route) {
                LetterEditorScreen(
                    viewModel = letterViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
