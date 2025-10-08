package com.wapps1.redcarga.core.navigation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wapps1.redcarga.core.navigation.BottomNavItem
import com.wapps1.redcarga.core.session.UserType
import com.wapps1.redcarga.features.home.presentation.views.ClientHomeScreen
import com.wapps1.redcarga.features.home.presentation.views.ProviderHomeScreen

/**
 * Scaffold principal con barra de navegación inferior persistente
 * Se adapta según el tipo de usuario (Cliente/Proveedor)
 */
@Composable
fun MainScaffold(
    userType: UserType,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    
    // Determinar items de navegación según tipo de usuario
    val navItems = when (userType) {
        UserType.CLIENT -> BottomNavItem.getClientItems()
        UserType.PROVIDER -> BottomNavItem.getProviderItems()
    }
    
    // Ruta inicial según tipo de usuario
    val startDestination = when (userType) {
        UserType.CLIENT -> BottomNavItem.ClientHome.route
        UserType.PROVIDER -> BottomNavItem.ProviderHome.route
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                items = navItems
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        ) {
            if (userType == UserType.CLIENT) {
                // Rutas para Cliente
                composable(BottomNavItem.ClientHome.route) {
                    ClientHomeScreen()
                }
                composable(BottomNavItem.ClientQuotes.route) {
                    PlaceholderScreen(title = "Cotizaciones")
                }
                composable(BottomNavItem.ClientRequest.route) {
                    PlaceholderScreen(title = "Hacer Solicitud")
                }
                composable(BottomNavItem.ClientChat.route) {
                    PlaceholderScreen(title = "Chat")
                }
                composable(BottomNavItem.ClientProfile.route) {
                    PlaceholderScreen(title = "Perfil")
                }
            } else {
                // Rutas para Proveedor
                composable(BottomNavItem.ProviderHome.route) {
                    ProviderHomeScreen(
                        onNavigateToRoutes = {
                            navController.navigate("routes_management")
                        },
                        onNavigateToDrivers = {
                            navController.navigate("drivers_management")
                        },
                        onNavigateToFleet = {
                            navController.navigate("vehicles_management")
                        }
                    )
                }
                composable(BottomNavItem.ProviderRequests.route) {
                    PlaceholderScreen(title = "Solicitudes")
                }
                composable(BottomNavItem.ProviderGeo.route) {
                    PlaceholderScreen(title = "Rutas")
                }
                composable(BottomNavItem.ProviderChat.route) {
                    PlaceholderScreen(title = "Chat")
                }
                composable(BottomNavItem.ProviderProfile.route) {
                    PlaceholderScreen(title = "Perfil")
                }
                
                // Rutas adicionales para Proveedor (fuera del bottom nav)
                composable("routes_management") {
                    com.wapps1.redcarga.features.fleet.presentation.views.RoutesManagement(
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
                composable("drivers_management") {
                    com.wapps1.redcarga.features.fleet.presentation.views.DriversManagement(
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
                composable("vehicles_management") {
                    com.wapps1.redcarga.features.fleet.presentation.views.VehiclesManagement(
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}

