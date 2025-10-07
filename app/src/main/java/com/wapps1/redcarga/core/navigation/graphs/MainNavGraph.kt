package com.wapps1.redcarga.core.navigation.graphs

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.wapps1.redcarga.core.navigation.NavGraph
import com.wapps1.redcarga.core.navigation.Route
import com.wapps1.redcarga.core.navigation.components.ClientHomeScaffold
import com.wapps1.redcarga.core.navigation.components.ProviderHomeScaffold
import com.wapps1.redcarga.core.session.UserType

fun NavGraphBuilder.mainNavGraph(
    navController: NavHostController,
    userType: UserType,
    onLogout: () -> Unit
) {
    navigation(
        startDestination = Route.HomeScaffold.route,
        route = NavGraph.Main.route
    ) {
        composable(route = Route.HomeScaffold.route) {
            when (userType) {
                UserType.CLIENT -> ClientHomeScaffold(navController, onLogout)
                UserType.PROVIDER -> ProviderHomeScaffold(navController, onLogout)
            }
        }
    }
}
