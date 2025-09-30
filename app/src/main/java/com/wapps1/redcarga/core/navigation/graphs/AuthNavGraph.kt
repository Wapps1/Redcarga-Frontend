package com.wapps1.redcarga.core.navigation.graphs

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.wapps1.redcarga.core.navigation.NavGraph
import com.wapps1.redcarga.core.navigation.Route
import com.wapps1.redcarga.features.auth.presentation.views.Welcome

/**
 * Grafo de navegación para flujo de autenticación
 * 
 * Contiene todas las pantallas públicas que no requieren autenticación:
 * - Welcome: Pantalla inicial de bienvenida
 * - SignIn: Inicio de sesión
 * - SignUp: Registro de nueva cuenta
 * - ForgotPassword: Recuperación de contraseña
 * 
 * @param navController Controlador de navegación
 * @param onNavigateToMain Callback para navegar al grafo principal tras login exitoso
 */
fun NavGraphBuilder.authNavGraph(
    navController: NavHostController,
    onNavigateToMain: () -> Unit
) {
    navigation(
        startDestination = Route.Welcome.route,
        route = NavGraph.Auth.route
    ) {
        // Pantalla de Bienvenida
        composable(route = Route.Welcome.route) {
            Welcome(
                onCreateAccount = {
                    navController.navigate(Route.SignUp.route)
                },
                onLogin = {
                    navController.navigate(Route.SignIn.route)
                }
            )
        }
        
        // Pantalla de Inicio de Sesión
        composable(route = Route.SignIn.route) {
            // TODO: Implementar SignInScreen
            // SignInScreen(
            //     onSignInSuccess = onNavigateToMain,
            //     onNavigateToSignUp = {
            //         navController.navigate(Route.SignUp.route)
            //     },
            //     onNavigateToForgotPassword = {
            //         navController.navigate(Route.ForgotPassword.route)
            //     },
            //     onBackClick = {
            //         navController.popBackStack()
            //     }
            // )
        }
        
        // Pantalla de Registro
        composable(route = Route.SignUp.route) {
            // TODO: Implementar SignUpScreen
            // SignUpScreen(
            //     onSignUpSuccess = onNavigateToMain,
            //     onNavigateToSignIn = {
            //         navController.navigate(Route.SignIn.route)
            //     },
            //     onBackClick = {
            //         navController.popBackStack()
            //     }
            // )
        }
        
        // Pantalla de Recuperación de Contraseña
        composable(route = Route.ForgotPassword.route) {
            // TODO: Implementar ForgotPasswordScreen
            // ForgotPasswordScreen(
            //     onPasswordResetSent = {
            //         navController.popBackStack()
            //     },
            //     onBackClick = {
            //         navController.popBackStack()
            //     }
            // )
        }
    }
}
