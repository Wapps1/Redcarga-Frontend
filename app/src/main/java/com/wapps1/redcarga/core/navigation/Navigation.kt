package com.wapps1.redcarga.core.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import android.util.Log
import com.wapps1.redcarga.core.navigation.graphs.authNavGraph
import com.wapps1.redcarga.core.navigation.graphs.mainNavGraph
import com.wapps1.redcarga.core.session.AuthSessionStore
import com.wapps1.redcarga.core.session.SessionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun Navigation(
    store: AuthSessionStore,
    navController: NavHostController = rememberNavController()
) {
    val sessionState by store.sessionState.collectAsState()
    val userType by store.currentUserType.collectAsState()

    // bootstrap una sola vez
    LaunchedEffect(Unit) { store.bootstrap() }

    // Reaccionar a cambios de sesión y navegar automáticamente a Main
    // Solo cuando ya conocemos el userType (para que el grafo Main esté registrado)
    LaunchedEffect(sessionState, userType) {
        if (sessionState is SessionState.AppSignedIn && userType != null) {
            Log.d("Navigation", "sessionState=${sessionState}")
            Log.d("Navigation", "userType=${userType}")
            Log.d("Navigation", "Navigating to Main graph")
            navController.navigate(NavGraph.Main.route) {
                popUpTo(NavGraph.Auth.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    val startGraph = when {
        sessionState is SessionState.AppSignedIn && userType != null -> NavGraph.Main.route
        else -> NavGraph.Auth.route
    }
    
    Box(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
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
            store = store,
            onNavigateToMain = {
                // no-op: la navegación se hará en LaunchedEffect(sessionState, userType)
            }
        )
        
        // Solo carga mainNavGraph si hay userType válido
        userType?.let { type ->
            mainNavGraph(
                navController = navController,
                userType = type,
                onLogout = {
                    // Limpia y vuelve a Auth
                    CoroutineScope(Dispatchers.Main).launch {
                        store.logout()
                        navController.navigate(NavGraph.Auth.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }
        }

        // Overlay de carga mientras hay FirebaseOnly (auto-login en curso)
        if (sessionState is SessionState.FirebaseOnly) {
            CircularProgressIndicator(modifier = androidx.compose.ui.Modifier.align(Alignment.Center))
        }
    }
}