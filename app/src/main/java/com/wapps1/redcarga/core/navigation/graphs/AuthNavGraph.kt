package com.wapps1.redcarga.core.navigation.graphs

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.wapps1.redcarga.core.navigation.NavGraph
import com.wapps1.redcarga.core.navigation.Route
import com.wapps1.redcarga.features.auth.presentation.views.ChooseAccountType
import com.wapps1.redcarga.features.auth.presentation.views.ForgotPassword
import com.wapps1.redcarga.features.auth.presentation.views.SignIn
import com.wapps1.redcarga.features.auth.presentation.views.SignUpClient
import com.wapps1.redcarga.features.auth.presentation.views.SignUpProvider
import com.wapps1.redcarga.features.auth.presentation.views.Verify2FA
import com.wapps1.redcarga.features.auth.presentation.views.Welcome


fun NavGraphBuilder.authNavGraph(
    navController: NavHostController,
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
                    navController.navigate("sign_up_client")
                },
                onProviderSelected = {
                    navController.navigate("sign_up_provider")
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(route = "sign_up_client") {
            SignUpClient(
                onSignUpSuccess = onNavigateToMain,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(route = "sign_up_provider") {
            SignUpProvider(
                onSignUpSuccess = onNavigateToMain,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Route.SignIn.route) {
            SignIn(
                onSignInSuccess = onNavigateToMain,
                onForgotPasswordClick = {
                    navController.navigate(Route.ForgotPassword.route)
                },
                onRegisterClick = {
                    navController.navigate(Route.ChooseAccountType.route)
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

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

        composable(route = Route.ForgotPassword.route) {
            ForgotPassword(
                onPasswordResetSuccess = {
                    navController.popBackStack()
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
