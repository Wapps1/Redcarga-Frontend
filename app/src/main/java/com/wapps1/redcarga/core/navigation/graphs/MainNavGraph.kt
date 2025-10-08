package com.wapps1.redcarga.core.navigation.graphs

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.wapps1.redcarga.core.navigation.NavGraph
import com.wapps1.redcarga.core.navigation.Route
import com.wapps1.redcarga.core.navigation.components.MainScaffold
import com.wapps1.redcarga.core.session.UserType

fun NavGraphBuilder.mainNavGraph(
    userType: UserType
) {
    navigation(
        startDestination = Route.HomeScaffold.route,
        route = NavGraph.Main.route
    ) {
        composable(route = Route.HomeScaffold.route) {
            MainScaffold(userType = userType)
        }
    }
}
