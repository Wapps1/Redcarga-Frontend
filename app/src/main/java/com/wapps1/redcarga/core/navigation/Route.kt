package com.wapps1.redcarga.core.navigation

sealed class Route(val route: String){
    object Login: Route("login")
}