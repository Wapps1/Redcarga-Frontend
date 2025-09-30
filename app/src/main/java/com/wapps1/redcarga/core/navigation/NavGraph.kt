package com.wapps1.redcarga.core.navigation


sealed class NavGraph(val route: String) {
   
    data object Auth : NavGraph("auth_graph")
    
  
    data object Main : NavGraph("main_graph")
}
