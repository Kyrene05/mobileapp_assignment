package com.example.studify.presentation.nav

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.studify.presentation.admin.AdminDashboardScreen
import com.example.studify.presentation.admin.ShopItemEditScreen
import com.example.studify.presentation.admin.ShopManagementScreen
import com.example.studify.presentation.auth.*
import com.example.studify.presentation.avatar.AvatarCreateScreen
import com.example.studify.presentation.avatar.AvatarProfile
import com.example.studify.presentation.avatar.ShopScreen
import com.example.studify.presentation.home.HomeScreen
import com.example.studify.presentation.home.LevelViewModel
import com.example.studify.presentation.tasks.*
import com.example.studify.presentation.welcome.WelcomeScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await

/* ---------------- Routes ---------------- */

object Routes {
    const val SPLASH = "splash"
    const val WELCOME = "welcome"

    const val AUTH = "auth"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"

    const val HOME = "home"
    const val SHOP = "shop"
    const val TASKS = "tasks"
    const val TASK_RECORD = "task_record"
    const val TASK_FOCUS = "task_focus"
    const val AVATAR = "avatar"

    // Admin
    const val ADMIN_AUTH = "admin_auth"
    const val ADMIN_HOME = "admin_home"
    const val ADMIN_SHOP_MGMT = "admin_shop_mgmt"
    const val ADMIN_SHOP_EDIT = "admin_shop_edit"
}

/* ---------------- Helper ---------------- */

fun NavController.navigateSingleTopTo(route: String) {
    navigate(route) {
        launchSingleTop = true
        restoreState = true
        popUpTo(Routes.HOME) { saveState = true }
    }
}

/* ---------------- NavHost ---------------- */

@Composable
fun AppNavHost() {
    val nav = rememberNavController()
    val levelVm: LevelViewModel = viewModel()

    NavHost(
        navController = nav,
        startDestination = Routes.SPLASH
    ) {

        /* ---------- Splash ---------- */
        composable(Routes.SPLASH) { SplashGate(nav) }

        /* ---------- Welcome ---------- */
        composable(Routes.WELCOME) {
            WelcomeScreen(
                onGoLogin = {
                    nav.navigate(Routes.AUTH) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                },
                onGoAdminLogin = {
                    nav.navigate(Routes.ADMIN_AUTH) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                }
            )
        }

        /* ---------- User Auth ---------- */
        composable(Routes.AUTH) {
            AuthScreen(
                onLoginSuccess = {
                    nav.navigate(Routes.SPLASH) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                },
                onGoRegister = { nav.navigate(Routes.REGISTER) },
                onForgotPassword = { nav.navigate(Routes.FORGOT_PASSWORD) },
                onGoAdminLogin = { nav.navigate(Routes.ADMIN_AUTH) }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    nav.navigate(Routes.AUTH) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onBackToLogin = { nav.popBackStack() }
            )
        }


        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(onBack = { nav.popBackStack() })
        }

        /* ---------- Admin Auth ---------- */
        composable(Routes.ADMIN_AUTH) {
            AdminLoginScreen(
                onAdminLoginSuccess = {
                    nav.navigate(Routes.ADMIN_HOME) {
                        popUpTo(Routes.ADMIN_AUTH) { inclusive = true }
                    }
                },
                onBackToUserLogin = {
                    nav.navigate(Routes.AUTH) {
                        popUpTo(Routes.ADMIN_AUTH) { inclusive = true }
                    }
                },
                onForgotPassword = { nav.navigate(Routes.FORGOT_PASSWORD) }
            )
        }

        /* ---------- Admin Dashboard ---------- */
        composable(Routes.ADMIN_HOME) {
            AdminDashboardScreen(
                onShopManagementClick = {
                    nav.navigate(Routes.ADMIN_SHOP_MGMT)
                },
                onViewReportClick = {},
                onLogoutClick = {
                    FirebaseAuth.getInstance().signOut()
                    nav.navigate(Routes.ADMIN_AUTH) {
                        popUpTo(Routes.ADMIN_HOME) { inclusive = true }
                    }
                }
            )
        }

        /* ---------- Admin: Shop Management (Screen A) ---------- */
        composable(Routes.ADMIN_SHOP_MGMT) {
            ShopManagementScreen(
                onBack = { nav.popBackStack() },

                // ✅ 选中 item -> EDIT
                onEdit = { itemId ->
                    nav.navigate("${Routes.ADMIN_SHOP_EDIT}?itemId=$itemId")
                },

                // ✅ Add New
                onAddNew = {
                    nav.navigate(Routes.ADMIN_SHOP_EDIT)
                }
            )
        }

        /* ---------- Admin: Shop Edit (Screen B) ---------- */
        composable(
            route = "${Routes.ADMIN_SHOP_EDIT}?itemId={itemId}",
            arguments = listOf(
                navArgument("itemId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { entry ->
            val itemId = entry.arguments?.getString("itemId")

            ShopItemEditScreen(
                itemId = itemId,        // null = Add, not null = Edit
                onBack = { nav.popBackStack() },
                onSaved = { nav.popBackStack() },
                onDeleted = { nav.popBackStack() }
            )
        }

        /* ---------- User Flow ---------- */
        composable(Routes.AVATAR) {
            AvatarCreateScreen(
                onNext = {
                    nav.navigate(Routes.HOME) {
                        popUpTo(Routes.AVATAR) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                currentTab = 0,
                onOpenShop = { nav.navigate(Routes.SHOP) },
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    nav.navigate(Routes.AUTH) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onNavHome = { nav.navigate(Routes.HOME) },
                onNavTasks = { nav.navigate(Routes.TASKS) },
                levelVm = levelVm
            )
        }

        composable(Routes.SHOP) {
            ShopScreen(
                levelVm = levelVm,
                onBack = { nav.popBackStack() },
                onSaveDone = { nav.popBackStack() }
            )
        }

        composable(Routes.TASKS) {
            TaskScreen(
                onBack = { nav.popBackStack() },
                onNavHome = { nav.navigate(Routes.HOME) },
                onNavTasks = {},
                onOpenRecord = { task ->
                    nav.navigate("${Routes.TASK_RECORD}/${task.id}")
                }
            )
        }

        composable(
            route = "${Routes.TASK_RECORD}/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.LongType })
        ) { entry ->
            val taskId = entry.arguments!!.getLong("taskId")
            val taskVm: TaskViewModel = viewModel(factory = TaskViewModel.Factory)
            val task = taskVm.tasks.collectAsState().value.firstOrNull { it.id == taskId }

            task?.let {
                TaskRecordScreen(
                    task = it,
                    onBack = { nav.popBackStack() },
                    onStartFocus = { focus ->
                        nav.navigate("${Routes.TASK_FOCUS}/${focus.id}")
                    }
                )
            }
        }

        composable(
            route = "${Routes.TASK_FOCUS}/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.LongType })
        ) { entry ->
            val taskId = entry.arguments!!.getLong("taskId")
            val taskVm: TaskViewModel = viewModel(factory = TaskViewModel.Factory)
            val task = taskVm.tasks.collectAsState().value.firstOrNull { it.id == taskId }

            task?.let {
                TaskFocusScreen(
                    task = it,
                    onBack = { nav.popBackStack() },
                    onFinish = { minutes, exp, success ->
                        if (success) {
                            levelVm.grantSessionReward(exp, exp)
                            taskVm.applyFocusResult(it, minutes)
                        }
                        nav.popBackStack()
                    }
                )
            }
        }
    }
}


/* ---------------- Splash Logic (FIXED) ---------------- */

@Composable
private fun SplashGate(nav: NavController) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        val prefs = context.getSharedPreferences("studify_prefs", Context.MODE_PRIVATE)
        val hasSeenWelcome = prefs.getBoolean("hasSeenWelcome", false)

        if (user == null) {
            nav.navigate(if (hasSeenWelcome) Routes.AUTH else Routes.WELCOME) {
                popUpTo(Routes.SPLASH) { inclusive = true }
                launchSingleTop = true
            }
            prefs.edit().putBoolean("hasSeenWelcome", true).apply()
            return@LaunchedEffect
        }

        // ✅ NEW: role check
        val role = try {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.uid)
                .get(Source.SERVER)
                .await()
                .getString("role")
        } catch (_: Exception) {
            null
        }

        if (role == "admin") {
            nav.navigate(Routes.ADMIN_HOME) {
                popUpTo(Routes.SPLASH) { inclusive = true }
                launchSingleTop = true
            }
            return@LaunchedEffect
        }

        // Normal user flow: avatar gate
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
        Column { Text("") }
    }
}
