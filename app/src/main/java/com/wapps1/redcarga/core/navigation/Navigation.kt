package com.wapps1.redcarga.core.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun Navigation(){
    val navController = rememberNavController()

    NavHost(navController, startDestination = Route.Login.route) {
        composable (Route.Login.route) {
        }
    }
}

