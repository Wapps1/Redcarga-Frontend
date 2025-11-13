package com.wapps1.redcarga.core.navigation

import androidx.navigation.NavHostController


fun NavHostController.navigateAndClearBackStack(route: String) {
    navigate(route) {
        popUpTo(0) {
            inclusive = true
        }
    }
}

fun NavHostController.navigateAndPopUpTo(
    route: String,
    popUpToRoute: String,
    inclusive: Boolean = false
) {
    navigate(route) {
        popUpTo(popUpToRoute) {
            this.inclusive = inclusive
        }
    }
}

fun NavHostController.navigateSingleTop(route: String) {
    navigate(route) {
        launchSingleTop = true
    }
}
