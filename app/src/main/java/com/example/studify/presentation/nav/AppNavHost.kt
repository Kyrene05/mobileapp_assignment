package com.example.studify.presentation.nav

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.studify.data.task.Task
import com.example.studify.presentation.admin.AdminDashboardScreen
import com.example.studify.presentation.auth.AdminLoginScreen
import com.example.studify.presentation.auth.AuthScreen
import com.example.studify.presentation.auth.RegisterScreen
import com.example.studify.presentation.avatar.AvatarCreateScreen
import com.example.studify.presentation.avatar.AvatarProfile
import com.example.studify.presentation.avatar.ShopScreen
import com.example.studify.presentation.home.HomeScreen
import com.example.studify.presentation.home.LevelViewModel
import com.example.studify.presentation.tasks.TaskFocusScreen
import com.example.studify.presentation.tasks.TaskRecordScreen
import com.example.studify.presentation.tasks.TaskScreen
import com.example.studify.presentation.welcome.WelcomeScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await

object Routes {
    const val SPLASH = "splash"
    const val WELCOME = "welcome"
    const val AUTH = "auth"
    const val REGISTER = "register"
    const val AVATAR = "avatar"
    const val HOME = "home"
    const val SHOP = "shop"
    const val TASKS = "tasks"

    const val TASK_RECORD = "task_record"
    const val TASK_FOCUS = "task_focus"

    // --- Admin routes ---
    const val ADMIN_AUTH = "admin_auth"
    const val ADMIN_HOME = "admin_home"

    const val FORGOT_PASSWORD = "forgot_password"
}

/**
 * Helper for bottom navigation
 */
fun NavController.navigateSingleTopTo(route: String) {
    this.navigate(route) {
        launchSingleTop = true
        restoreState = true
        popUpTo(Routes.HOME) { saveState = true }
    }
}

@Composable
fun AppNavHost() {
    val nav = rememberNavController()
    val levelVm: LevelViewModel = viewModel()

    val backStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val bottomIndex = when (currentRoute) {
        Routes.HOME  -> 0
        Routes.TASKS -> 1
        Routes.SHOP  -> 2
        else         -> 0
    }

    NavHost(
        navController = nav,
        startDestination = Routes.SPLASH
    ) {
        // Splash – decide navigation
        composable(Routes.SPLASH) { SplashGate(nav) }

        // Welcome
        composable(Routes.WELCOME) {
            WelcomeScreen(
                onGoLogin = {
                    nav.navigate(Routes.AUTH) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onGoAdminLogin = {
                    nav.navigate(Routes.ADMIN_AUTH) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // Normal user login
        composable(Routes.AUTH) {
            AuthScreen(
                onLoginSuccess = {
                    nav.navigate(Routes.SPLASH) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onGoRegister = { nav.navigate(Routes.REGISTER) },
                onForgotPassword = {
                    nav.navigate(Routes.FORGOT_PASSWORD)
                }
            )
        }

        // Admin login
        composable(Routes.ADMIN_AUTH) {
            AdminLoginScreen(
                onAdminLoginSuccess = {
                    nav.navigate(Routes.ADMIN_HOME) {
                        popUpTo(Routes.ADMIN_AUTH) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onBackToUserLogin = {
                    nav.navigate(Routes.AUTH) {
                        popUpTo(Routes.ADMIN_AUTH) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onForgotPassword = {
                    nav.navigate(Routes.FORGOT_PASSWORD)
                }
            )
        }

        // Admin Home – your dashboard screen
        composable(Routes.ADMIN_HOME) {
            AdminDashboardScreen(
                onShopManagementClick = {
                    // TODO later: nav.navigate("admin_shop")
                },
                onViewReportClick = {
                    // TODO later: nav.navigate("admin_report")
                },
                onLogoutClick = {
                    FirebaseAuth.getInstance().signOut()
                    nav.navigate(Routes.WELCOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // Register
        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {},
                onBackToLogin = { nav.popBackStack() }
            )
        }

        // Forgot password
        composable(Routes.FORGOT_PASSWORD) {
            com.example.studify.presentation.auth.ForgotPasswordScreen(
                onBack = { nav.popBackStack() }
            )
        }

        // Avatar creation
        composable(Routes.AVATAR) {
            AvatarCreateScreen(
                onNext = { _: AvatarProfile ->
                    nav.navigate(Routes.HOME) {
                        popUpTo(Routes.AVATAR) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // Home
        composable(Routes.HOME) {
            HomeScreen(
                currentTab = bottomIndex,
                onOpenShop = { nav.navigateSingleTopTo(Routes.SHOP) },
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    nav.navigate(Routes.AUTH) {
                        popUpTo(Routes.HOME) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavHome = { nav.navigateSingleTopTo(Routes.HOME) },
                onNavTasks = { nav.navigateSingleTopTo(Routes.TASKS) },
                levelVm = levelVm
            )
        }

        // Shop
        composable(Routes.SHOP) {
            ShopScreen(
                levelVm = levelVm,
                onBack = { nav.popBackStack() },
                onSaveDone = {
                    nav.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }

        // Tasks list
        composable(Routes.TASKS) {
            TaskScreen(
                onBack = { nav.navigateSingleTopTo(Routes.HOME) },
                onNavHome = { nav.navigateSingleTopTo(Routes.HOME) },
                onNavTasks = {},
                onOpenRecord = { task ->
                    nav.navigate("${Routes.TASK_RECORD}/${task.id}")
                }
            )
        }

        // Task record
        composable(
            route = "${Routes.TASK_RECORD}/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.LongType })
        ) { entry ->
            val taskId = entry.arguments!!.getLong("taskId")

            val taskVm: com.example.studify.presentation.tasks.TaskViewModel =
                viewModel(factory = com.example.studify.presentation.tasks.TaskViewModel.Factory)

            val tasks by taskVm.tasks.collectAsState()
            val task: Task? = tasks.firstOrNull { it.id == taskId }

            if (task != null) {
                TaskRecordScreen(
                    task = task,
                    onBack = { nav.popBackStack() },
                    onStartFocus = { focusTask ->
                        nav.navigate("${Routes.TASK_FOCUS}/${focusTask.id}")
                    }
                )
            }
        }

        // Task focus
        composable(
            route = "${Routes.TASK_FOCUS}/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.LongType })
        ) { entry ->
            val taskId = entry.arguments!!.getLong("taskId")

            val taskVm: com.example.studify.presentation.tasks.TaskViewModel =
                viewModel(factory = com.example.studify.presentation.tasks.TaskViewModel.Factory)

            val tasks by taskVm.tasks.collectAsState()
            val task: Task? = tasks.firstOrNull { it.id == taskId }

            if (task != null) {
                TaskFocusScreen(
                    task = task,
                    onBack = { nav.popBackStack() },
                    onFinish = { elapsedMinutes, earnedExp, success ->
                        if (success) {
                            levelVm.grantSessionReward(
                                exp = earnedExp,
                                coinsGain = earnedExp
                            )
                            taskVm.applyFocusResult(task, elapsedMinutes)
                        }
                        nav.popBackStack()
                    }
                )
            }
        }
    }
}

/**
 * Splash navigation logic
 */
@Composable
private fun SplashGate(nav: NavController) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        val prefs = context.getSharedPreferences("studify_prefs", Context.MODE_PRIVATE)
        val hasSeenWelcome = prefs.getBoolean("hasSeenWelcome", false)

        if (user == null) {
            if (!hasSeenWelcome) {
                prefs.edit().putBoolean("hasSeenWelcome", true).apply()
                nav.navigate(Routes.WELCOME) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                    launchSingleTop = true
                }
            } else {
                nav.navigate(Routes.AUTH) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                    launchSingleTop = true
                }
            }
            return@LaunchedEffect
        }

        val hasAvatar = try {
            FirebaseFirestore.getInstance()
                .collection("users").document(user.uid)
                .collection("avatar").document("profile")
                .get(Source.SERVER)
                .await()
                .exists()
        } catch (_: Exception) { false }

        nav.navigate(if (hasAvatar) Routes.HOME else Routes.AVATAR) {
            popUpTo(Routes.SPLASH) { inclusive = true }
            launchSingleTop = true
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        // Empty splash surface while navigation decides
        Column(modifier = Modifier.fillMaxSize()) {
            Text(text = "")
        }
    }
}
