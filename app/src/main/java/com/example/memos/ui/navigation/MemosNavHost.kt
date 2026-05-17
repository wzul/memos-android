package com.example.memos.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.memos.ui.screens.login.LoginScreen
import com.example.memos.ui.screens.login.LoginViewModel
import com.example.memos.ui.screens.memolist.MemoListScreen
import com.example.memos.ui.screens.memoedit.MemoEditScreen
import com.example.memos.ui.screens.settings.SettingsScreen

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object MemoList : Screen("memos")
    data object MemoEdit : Screen("edit/{name}")
    data object Settings : Screen("settings")

    fun createRoute(vararg args: String): String {
        var route = this.route
        args.forEach { route = route.replaceFirst("{name}", Uri.encode(it)) }
        return route
    }
}

@Composable
fun MemosNavHost(
    navController: NavHostController = rememberNavController()
) {
    val loginViewModel: LoginViewModel = hiltViewModel()
    val isLoggedIn = loginViewModel.isLoggedIn

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) Screen.MemoList.route else Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.MemoList.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.MemoList.route) {
            MemoListScreen(
                onNavigateToEdit = { name ->
                    navController.navigate(Screen.MemoEdit.createRoute(name ?: "new"))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        composable(
            route = Screen.MemoEdit.route,
            arguments = listOf(
                navArgument("name") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name")
            val memoName = if (name == "new" || name.isNullOrBlank()) null else name
            MemoEditScreen(
                memoName = memoName,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onSignOut = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
