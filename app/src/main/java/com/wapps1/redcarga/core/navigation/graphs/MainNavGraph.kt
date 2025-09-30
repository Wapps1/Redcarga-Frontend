package com.wapps1.redcarga.core.navigation.graphs

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.wapps1.redcarga.core.navigation.NavGraph
import com.wapps1.redcarga.core.navigation.Route

/**
 * Grafo de navegación principal (protegido)
 * 
 * Contiene todas las pantallas que requieren autenticación:
 * - Home: Pantalla principal de la app
 * - Profile: Perfil del usuario
 * - Settings: Configuración
 * 
 * IMPORTANTE: Solo se puede acceder a estas pantallas si el usuario está autenticado
 * 
 * @param navController Controlador de navegación
 * @param onLogout Callback para cerrar sesión y volver al grafo de auth
 */
fun NavGraphBuilder.mainNavGraph(
    navController: NavHostController,
    onLogout: () -> Unit
) {
    navigation(
        startDestination = Route.Home.route,
        route = NavGraph.Main.route
    ) {
        // Pantalla Principal (Home)
        composable(route = Route.Home.route) {
            // TODO: Implementar HomeScreen
            // HomeScreen(
            //     onNavigateToProfile = {
            //         navController.navigate(Route.Profile.route)
            //     },
            //     onNavigateToSettings = {
            //         navController.navigate(Route.Settings.route)
            //     }
            // )
        }
        
        // Pantalla de Perfil
        composable(route = Route.Profile.route) {
            // TODO: Implementar ProfileScreen
            // ProfileScreen(
            //     onBackClick = {
            //         navController.popBackStack()
            //     },
            //     onLogout = onLogout
            // )
        }
        
        // Pantalla de Configuración
        composable(route = Route.Settings.route) {
            // TODO: Implementar SettingsScreen
            // SettingsScreen(
            //     onBackClick = {
            //         navController.popBackStack()
            //     },
            //     onLogout = onLogout
            // )
        }
    }
}
