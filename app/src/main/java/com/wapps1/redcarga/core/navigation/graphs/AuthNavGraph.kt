package com.wapps1.redcarga.core.navigation.graphs

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.wapps1.redcarga.core.navigation.NavGraph
import com.wapps1.redcarga.core.navigation.Route
import com.wapps1.redcarga.core.session.AuthSessionStore
import com.wapps1.redcarga.features.auth.domain.models.value.Email
import com.wapps1.redcarga.features.auth.domain.models.value.Password
import com.wapps1.redcarga.features.auth.domain.models.value.Platform
import com.wapps1.redcarga.features.auth.presentation.views.ChooseAccountType
import com.wapps1.redcarga.features.auth.presentation.views.SignIn
import com.wapps1.redcarga.features.auth.presentation.views.SignUpClient
import com.wapps1.redcarga.features.auth.presentation.views.SignUpProvider
import com.wapps1.redcarga.features.auth.presentation.views.Welcome


fun NavGraphBuilder.authNavGraph(
    navController: NavHostController,
    store: AuthSessionStore,
    onNavigateToMain: () -> Unit
) {
    navigation(
        startDestination = Route.Welcome.route,
        route = NavGraph.Auth.route
    ) {
        composable(route = Route.Welcome.route) {
            Welcome(
                onCreateAccount = {
                    navController.navigate(Route.ChooseAccountType.route)
                },
                onLogin = {
                    navController.navigate(Route.SignIn.route)
                }
            )
        }

        composable(route = Route.ChooseAccountType.route) {
            ChooseAccountType(
                onClientSelected = {
                    navController.navigate(Route.SignUpClient.route)
                },
                onProviderSelected = {
                    navController.navigate(Route.SignUpProvider.route)
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Route.SignUpClient.route) {
            SignUpClient(
                onNavigateToMain = onNavigateToMain,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(route = Route.SignUpProvider.route) {
            SignUpProvider(
                onNavigateToMain = onNavigateToMain,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(route = Route.SignIn.route) {
            SignIn(
                onNavigateToMain = onNavigateToMain,
                onRegisterClick = { navController.navigate(Route.ChooseAccountType.route) },
                onBackClick = { navController.popBackStack() }
            )
        }


    }
}
