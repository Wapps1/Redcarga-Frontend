package com.wapps1.redcarga.core.navigation.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wapps1.redcarga.core.navigation.BottomNavItem
import com.wapps1.redcarga.core.session.UserType
import com.wapps1.redcarga.features.home.presentation.views.ClientHomeScreen
import com.wapps1.redcarga.features.home.presentation.views.ProviderHomeScreen
import com.wapps1.redcarga.features.requests.presentation.views.ClientRequestsScreen
import com.wapps1.redcarga.features.requests.presentation.views.CreateRequestScreen
import com.wapps1.redcarga.features.deals.presentation.views.ClientDealsScreen
import com.wapps1.redcarga.features.auth.presentation.views.UserProfileScreen
import com.wapps1.redcarga.features.requests.presentation.views.ProviderIncomingRequestsScreen
import com.wapps1.redcarga.features.chat.presentation.views.ChatListScreen
import com.wapps1.redcarga.features.chat.presentation.views.ChatScreen

@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
fun MainScaffold(
    userType: UserType,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    val navItems = when (userType) {
        UserType.CLIENT -> BottomNavItem.getClientItems()
        UserType.PROVIDER -> BottomNavItem.getProviderItems()
    }

    val startDestination = when (userType) {
        UserType.CLIENT -> BottomNavItem.ClientHome.route
        UserType.PROVIDER -> BottomNavItem.ProviderHome.route
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (userType == UserType.CLIENT) {
                ClientBottomNavigationBar(
                    navController = navController,
                    items = navItems
                )
            } else {
                BottomNavigationBar(
                    navController = navController,
                    items = navItems
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        ) {
            if (userType == UserType.CLIENT) {
                composable(BottomNavItem.ClientHome.route) {
                    ClientHomeScreen(
                        onNavigateToRequests = {
                            navController.navigate(BottomNavItem.ClientRequest.route)
                        },
                        onNavigateToCreateRequest = {
                            navController.navigate("client_create_request")
                        }
                    )
                }
                composable(BottomNavItem.ClientQuotes.route) {
                    ClientDealsScreen(
                        onBack = { navController.popBackStack() },
                        onOpenChat = {
                            // Navegar a Chat normalmente, sin modificar el back stack
                            // El bottom bar manejará correctamente el retorno
                            navController.navigate(BottomNavItem.ClientChat.route) {
                                launchSingleTop = true
                            }
                        },
                        onOpenQuoteDetails = { quoteId, _requestId ->
                            navController.navigate("client_view_quote/$quoteId")
                        }
                    )
                }
                composable(BottomNavItem.ClientRequest.route) {
                    ClientRequestsScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onNavigateToCreateRequest = {
                            navController.navigate("client_create_request")
                        }
                    )
                }
                composable(BottomNavItem.ClientChat.route) {
                    ChatListScreen(
                        onNavigateBack = {
                            // No hacer nada, el bottom bar maneja la navegación
                        },
                        onNavigateToChat = { quoteId ->
                            navController.navigate("client_chat/$quoteId")
                        }
                    )
                }

                composable("client_chat/{quoteId}") { backStackEntry ->
                    val quoteId = backStackEntry.arguments?.getString("quoteId")?.toLongOrNull()
                    if (quoteId != null) {
                        ChatScreen(
                            quoteId = quoteId,
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    } else {
                        LaunchedEffect(Unit) {
                            navController.popBackStack()
                        }
                    }
                }
                composable(BottomNavItem.ClientProfile.route) {
                    UserProfileScreen(
                        onLogout = { navController.popBackStack() }
                    )
                }

                composable("client_create_request") {
                    val viewModel: com.wapps1.redcarga.features.requests.presentation.viewmodels.CreateRequestViewModel =
                        hiltViewModel()

                    CreateRequestScreen(
                        viewModel = viewModel,
                        onBack = {
                            navController.popBackStack()
                        },
                        onNext = {
                            navController.navigate("client_request_summary")
                        }
                    )
                }

                composable("client_request_summary") { _ ->
                    val parentEntry = remember {
                        navController.getBackStackEntry("client_create_request")
                    }
                    val viewModel: com.wapps1.redcarga.features.requests.presentation.viewmodels.CreateRequestViewModel =
                        hiltViewModel(parentEntry)

                    com.wapps1.redcarga.features.requests.presentation.views.RequestSummaryScreen(
                        viewModel = viewModel,
                        onBack = {
                            navController.popBackStack()
                        },
                        onSubmit = {
                            // Navegar a ClientDealsScreen después de crear la solicitud
                            navController.navigate(BottomNavItem.ClientQuotes.route) {
                                popUpTo(BottomNavItem.ClientHome.route) {
                                    inclusive = false
                                }
                            }
                        }
                    )
                }

                // ⭐ Pantalla de detalle de cotización para CLIENTE
                composable("client_view_quote/{quoteId}") { backStackEntry ->
                    val quoteId = backStackEntry.arguments?.getString("quoteId")?.toLongOrNull()
                    if (quoteId != null) {
                        com.wapps1.redcarga.features.deals.presentation.views.ClientViewQuoteScreen(
                            quoteId = quoteId,
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            onOpenChat = {
                                navController.navigate(BottomNavItem.ClientChat.route) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    } else {
                        LaunchedEffect(Unit) {
                            navController.popBackStack()
                        }
                    }
                }
            } else {
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
                    ProviderIncomingRequestsScreen(
                        onQuote = { requestId ->
                            navController.navigate("provider_create_quote/$requestId")
                        },
                        onViewQuote = { quoteId -> // ⭐ Nueva navegación para ver cotización
                            navController.navigate("provider_view_quote/$quoteId")
                        },
                        onChat = { quoteId -> // ⭐ Navegación al chat
                            navController.navigate("provider_chat/$quoteId")
                        }
                    )
                }

                composable("provider_create_quote/{requestId}") { backStackEntry ->
                    val requestId = backStackEntry.arguments?.getString("requestId")?.toLongOrNull()
                    if (requestId != null) {
                        com.wapps1.redcarga.features.requests.presentation.views.CreateQuoteScreen(
                            requestId = requestId,
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    } else {
                        // Error: requestId inválido, volver atrás
                        LaunchedEffect(Unit) {
                            navController.popBackStack()
                        }
                    }
                }
                
                // ⭐ Nueva ruta para VER cotización existente
                composable("provider_view_quote/{quoteId}") { backStackEntry ->
                    val quoteId = backStackEntry.arguments?.getString("quoteId")?.toLongOrNull()
                    if (quoteId != null) {
                        com.wapps1.redcarga.features.requests.presentation.views.ViewQuoteScreen(
                            quoteId = quoteId,
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    } else {
                        // Error: quoteId inválido, volver atrás
                        LaunchedEffect(Unit) {
                            navController.popBackStack()
                        }
                    }
                }
                composable(BottomNavItem.ProviderGeo.route) {
                    PlaceholderScreen(title = "Rutas")
                }
                composable(BottomNavItem.ProviderChat.route) {
                    ChatListScreen(
                        onNavigateBack = {
                            // No hacer nada, el bottom bar maneja la navegación
                        },
                        onNavigateToChat = { quoteId ->
                            navController.navigate("provider_chat/$quoteId")
                        }
                    )
                }

                composable("provider_chat/{quoteId}") { backStackEntry ->
                    val quoteId = backStackEntry.arguments?.getString("quoteId")?.toLongOrNull()
                    if (quoteId != null) {
                        ChatScreen(
                            quoteId = quoteId,
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    } else {
                        LaunchedEffect(Unit) {
                            navController.popBackStack()
                        }
                    }
                }
                composable(BottomNavItem.ProviderProfile.route) {
                    UserProfileScreen(
                        onLogout = { navController.popBackStack() }
                    )
                }
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

