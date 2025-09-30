package com.wapps1.redcarga.core.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.wapps1.redcarga.core.navigation.graphs.authNavGraph
import com.wapps1.redcarga.core.navigation.graphs.mainNavGraph
import com.wapps1.redcarga.core.session.SessionManager
import javax.inject.Inject

@Composable
fun Navigation(
    sessionManager: SessionManager,
    navController: NavHostController = rememberNavController()
) {
    val isAuthenticated by sessionManager.isAuthenticated.collectAsState()
    
    val startGraph = if (isAuthenticated) {
        NavGraph.Main.route
    } else {
        NavGraph.Auth.route
    }
    
    NavHost(
        navController = navController,
        startDestination = startGraph,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        }
    ) {
        authNavGraph(
            navController = navController,
            onNavigateToMain = {
                navController.navigate(NavGraph.Main.route) {
                    popUpTo(NavGraph.Auth.route) {
                        inclusive = true
                    }
                }
            }
        )
        mainNavGraph(
            navController = navController,
            onLogout = {
                sessionManager.logout()
                navController.navigate(NavGraph.Auth.route) {
                    popUpTo(0) {
                        inclusive = true
                    }
                }
            }
        )
    }
}