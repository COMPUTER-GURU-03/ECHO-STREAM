package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.navigation.toRoute
import com.example.core.navigation.Screen
import com.example.ui.theme.MovieDBTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.features.authentication.presentation.AuthViewModel
import com.example.features.authentication.presentation.AuthUiState
import com.example.features.authentication.presentation.LoginScreen
import com.example.features.authentication.presentation.RegisterScreen
import com.example.features.authentication.presentation.VerificationNoticeScreen
import com.example.features.authentication.presentation.ForgotPasswordScreen
import androidx.compose.runtime.*
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

import com.example.core.ui.glassEffect
import androidx.compose.material.icons.filled.Download

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MovieDBTheme {
                val authViewModel: AuthViewModel = viewModel()
                val authState by authViewModel.uiState.collectAsState()
                
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val isPlayerScreen = currentRoute?.contains("Player", ignoreCase = true) == true
                val isAuthScreen = authState !is AuthUiState.Authenticated
                val showBottomBar = !isAuthScreen && !isPlayerScreen

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            Box(modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 24.dp)
                                .navigationBarsPadding()
                            ) {
                                MovieDBBottomNavigation(
                                    currentRoute = currentRoute,
                                    onNavigate = { screen ->
                                        navController.navigate(screen) {
                                            popUpTo(navController.graph.startDestinationId) {
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
                    NavHost(
                        navController = navController,
                        startDestination = if (authState is AuthUiState.Authenticated) Screen.Home else Screen.Auth,
                        modifier = if (isPlayerScreen) Modifier.fillMaxSize() else Modifier.padding(innerPadding)
                    ) {
                        composable<Screen.Auth> {
                            var authSubScreen by remember { mutableStateOf("login") }
                            var registeredEmail by remember { mutableStateOf("") }

                            BackHandler(enabled = authSubScreen != "login") {
                                authSubScreen = "login"
                            }

                            when (authSubScreen) {
                                "verification" -> {
                                    VerificationNoticeScreen(
                                        email = registeredEmail,
                                        onContinueClick = { authSubScreen = "login" }
                                    )
                                }
                                "register" -> {
                                    RegisterScreen(
                                        onRegisterClick = { email, pass, name -> 
                                            registeredEmail = email
                                            authViewModel.signUp(email, pass, name)
                                        },
                                        onLoginClick = { authSubScreen = "login" },
                                        isLoading = authState is AuthUiState.Loading,
                                        errorMessage = (authState as? AuthUiState.Error)?.message,
                                        onDismissError = { authViewModel.clearError() }
                                    )
                                }
                                "forgot" -> {
                                    ForgotPasswordScreen(
                                        onResetClick = { email -> authViewModel.resetPassword(email) },
                                        onBackClick = { authSubScreen = "login" },
                                        isLoading = authState is AuthUiState.Loading,
                                        errorMessage = (authState as? AuthUiState.Error)?.message,
                                        onDismissError = { authViewModel.clearError() }
                                    )
                                }
                                else -> {
                                    LoginScreen(
                                        onLoginClick = { email, pass -> authViewModel.login(email, pass) },
                                        onRegisterClick = { authSubScreen = "register" },
                                        onForgotPasswordClick = { authSubScreen = "forgot" },
                                        isLoading = authState is AuthUiState.Loading,
                                        errorMessage = (authState as? AuthUiState.Error)?.message,
                                        onDismissError = { authViewModel.clearError() }
                                    )
                                }
                            }
                            
                            LaunchedEffect(authState) {
                                if (authState is AuthUiState.Authenticated) {
                                    navController.navigate(Screen.Home) {
                                        popUpTo(Screen.Auth) { inclusive = true }
                                    }
                                } else if (authState is AuthUiState.NeedsVerification) {
                                    authSubScreen = "verification"
                                }
                            }
                        }
                        composable<Screen.Home> { 
                            val homeViewModel: com.example.features.home.presentation.HomeViewModel = viewModel()
                            com.example.features.home.presentation.HomeScreen(
                                viewModel = homeViewModel,
                                onMovieClick = { movie ->
                                    navController.navigate(Screen.TitleDetails(movie.id, movie.mediaType ?: "movie"))
                                },
                                onPlayProgressClick = { progress ->
                                    navController.navigate(
                                        Screen.Player(
                                            tmdbId = progress.tmdbId,
                                            mediaType = progress.mediaType,
                                            title = progress.title,
                                            posterPath = progress.posterPath
                                        )
                                    )
                                }
                            )
                        }
                        composable<Screen.Discover> { 
                            val discoverViewModel: com.example.features.discovery.presentation.DiscoverViewModel = viewModel()
                            com.example.features.discovery.presentation.DiscoverScreen(
                                viewModel = discoverViewModel,
                                onMovieClick = { movie ->
                                    navController.navigate(Screen.TitleDetails(movie.id, movie.mediaType ?: "movie"))
                                }
                            )
                        }
                        composable<Screen.TitleDetails> { backStackEntry ->
                            val details: Screen.TitleDetails = backStackEntry.toRoute()
                            val detailsViewModel: com.example.features.discovery.presentation.DetailsViewModel = viewModel()
                            com.example.features.discovery.presentation.DetailsScreen(
                                tmdbId = details.tmdbId,
                                mediaType = details.mediaType,
                                viewModel = detailsViewModel,
                                onBackClick = { navController.popBackStack() },
                                onPlayClick = { movie ->
                                    navController.navigate(
                                        Screen.Player(
                                            tmdbId = movie.id,
                                            mediaType = movie.mediaType ?: "movie",
                                            title = movie.displayTitle,
                                            posterPath = movie.posterPath
                                        )
                                    )
                                }
                            )
                        }
                        composable<Screen.Library> { 
                            val libraryViewModel: com.example.features.library.presentation.LibraryViewModel = viewModel()
                            com.example.features.library.presentation.LibraryScreen(
                                viewModel = libraryViewModel,
                                onTitleClick = { id, type ->
                                    navController.navigate(Screen.TitleDetails(id, type))
                                }
                            )
                        }
                        composable<Screen.Downloads> { 
                            com.example.features.downloads.presentation.DownloadsScreen()
                        }
                        composable<Screen.Profile> { 
                            val profileViewModel: com.example.features.profile.presentation.ProfileViewModel = viewModel()
                            com.example.features.profile.presentation.ProfileScreen(
                                viewModel = profileViewModel,
                                onLogoutClick = { authViewModel.logout() }
                            )
                        }
                        composable<Screen.Player> { backStackEntry ->
                            val playerArgs: Screen.Player = backStackEntry.toRoute()
                            val playerViewModel: com.example.features.playback.presentation.PlayerViewModel = viewModel()
                            com.example.features.playback.presentation.PlayerScreen(
                                tmdbId = playerArgs.tmdbId,
                                mediaType = playerArgs.mediaType,
                                title = playerArgs.title,
                                posterPath = playerArgs.posterPath,
                                viewModel = playerViewModel,
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MovieDBBottomNavigation(
    currentRoute: String?,
    onNavigate: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = Color.Transparent,
        tonalElevation = 0.dp,
        modifier = Modifier.glassEffect(24.dp)
    ) {
        val items = listOf(
            NavigationItem("Home", Screen.Home, Icons.Default.Home),
            NavigationItem("Discover", Screen.Discover, Icons.Default.Search),
            NavigationItem("Library", Screen.Library, Icons.AutoMirrored.Filled.LibraryBooks),
            NavigationItem("Downloads", Screen.Downloads, Icons.Default.Download),
            NavigationItem("Profile", Screen.Profile, Icons.Default.Person)
        )

        items.forEach { item ->
            val isSelected = currentRoute?.contains(item.name, ignoreCase = true) == true
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.screen) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = Color.White.copy(alpha = 0.6f),
                    unselectedTextColor = Color.White.copy(alpha = 0.6f),
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

data class NavigationItem(
    val name: String,
    val screen: Screen,
    val icon: ImageVector,
    val label: String = name
)

@androidx.compose.runtime.Composable
fun PlaceholderScreen(name: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "$name Screen Placeholder", color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground)
    }
}
