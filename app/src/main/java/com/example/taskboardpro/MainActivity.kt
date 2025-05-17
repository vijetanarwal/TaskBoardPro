package com.example.taskboardpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        setContent {
            MaterialTheme {
                var loggedIn by remember { mutableStateOf(false) }
                var currentScreen by remember { mutableStateOf("login") } // login, signup, home, profile, leaderboard
                var selectedProjectId by remember { mutableStateOf<String?>(null) }

                when {
                    !loggedIn && currentScreen == "login" -> {
                        LoginScreen(
                            onLoginSuccess = {
                                loggedIn = true
                                currentScreen = "home" // ✅ Go to Project list after login
                            },
                            onNavigateToSignup = { currentScreen = "signup" }
                        )
                    }

                    !loggedIn && currentScreen == "signup" -> {
                        SignupScreen(
                            onSignupSuccess = {
                                loggedIn = true
                                currentScreen = "home" // ✅ Go to Project list after signup
                            },
                            onNavigateToLogin = { currentScreen = "login" }
                        )
                    }

                    loggedIn -> {
                        Scaffold(
                            topBar = {
                                TopAppBar(
                                    title = { Text("🚀 TaskBoard Pro") },
                                    actions = {
                                        TextButton(onClick = {
                                            currentScreen = if (currentScreen == "profile") "home" else "profile"
                                        }) {
                                            Text(
                                                if (currentScreen == "profile") "Home" else "Profile",
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        TextButton(onClick = {
                                            currentScreen = "leaderboard"
                                        }) {
                                            Text("Leaderboard", color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                )
                            }
                        ) { padding ->
                            Surface(modifier = Modifier.padding(padding)) {
                                when (currentScreen) {
                                    "leaderboard" -> LeaderboardScreen()

                                    "profile" -> ProfileScreen()

                                    "home" -> {
                                        if (selectedProjectId == null) {
                                            ProjectScreen(
                                                onProjectClick = { projectId ->
                                                    selectedProjectId = projectId
                                                }
                                            )
                                        } else {
                                            TaskBoardScreen(
                                                projectId = selectedProjectId!!,
                                                onBackToProjects = {
                                                    selectedProjectId = null
                                                },
                                                onNavigateToLeaderboard = {
                                                    currentScreen = "leaderboard"
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
