package com.wapps1.redcarga.core.navigation


sealed class Route(val route: String) {
    
  
   
    data object Welcome : Route("welcome")
   
    data object SignIn : Route("sign_in")
  
    data object SignUp : Route("sign_up")
  
    data object ForgotPassword : Route("forgot_password")
    
    data object Home : Route("home")

    data object Profile : Route("profile")

    data object Settings : Route("settings")
    
    data object Detail : Route("detail/{id}") {
        fun createRoute(id: String) = "detail/$id"
    }
}