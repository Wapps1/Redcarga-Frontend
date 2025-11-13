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

@Composable
fun Navigation(
    store: AuthSessionStore,
    navController: NavHostController = rememberNavController()
) {
    val sessionState by store.sessionState.collectAsState()
    val userType by store.currentUserType.collectAsState()

    LaunchedEffect(Unit) { store.bootstrap() }
    LaunchedEffect(sessionState, userType) {
        if (sessionState is SessionState.AppSignedIn && userType != null) {
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
        authNavGraph(navController = navController)
        
        userType?.let { type ->
            mainNavGraph(userType = type)
        }
        }

        if (sessionState is SessionState.FirebaseOnly) {
            CircularProgressIndicator(modifier = androidx.compose.ui.Modifier.align(Alignment.Center))
        }
    }
}