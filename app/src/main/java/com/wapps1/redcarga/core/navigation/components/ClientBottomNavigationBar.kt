package com.wapps1.redcarga.core.navigation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.wapps1.redcarga.core.navigation.BottomNavItem
import com.wapps1.redcarga.core.ui.theme.RcColor5
import com.wapps1.redcarga.core.ui.theme.RcColor8

/**
 * Barra de navegación inferior para clientes con botón flotante central
 */
@Composable
fun ClientBottomNavigationBar(
    navController: NavHostController,
    items: List<BottomNavItem>,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        // Barra de navegación
        NavigationBar(
            containerColor = Color.White,
            contentColor = RcColor8,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            items.forEachIndexed { index, item ->
                // El ítem central (índice 2) se deja vacío para el FAB
                if (index == 2) {
                    // Spacer para mantener el espacio del botón flotante
                    Spacer(modifier = Modifier.weight(1f))
                } else {
                    val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true

                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = stringResource(item.titleRes),
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(item.titleRes),
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                maxLines = 1
                            )
                        },
                        selected = isSelected,
                        onClick = {
                            // Si ya estás en esta ruta, no hacer nada
                            if (isSelected) {
                                return@NavigationBarItem
                            }
                            
                            // Intentar hacer pop hasta la ruta destino si está en el back stack
                            val popped = navController.popBackStack(item.route, inclusive = false)
                            
                            // Si no se pudo hacer pop (la ruta no está en el back stack), navegar normalmente
                            if (!popped) {
                                navController.navigate(item.route) {
                                    // Limpiar el back stack hasta el start destination
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = RcColor5,
                            selectedTextColor = RcColor5,
                            unselectedIconColor = RcColor8,
                            unselectedTextColor = RcColor8,
                            indicatorColor = RcColor5.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }

        // Botón flotante central (3er ítem - Solicitar)
        val centerItem = items[2] // ClientRequest
        val isCenterSelected = currentDestination?.hierarchy?.any { it.route == centerItem.route } == true
        
        FloatingActionButton(
            onClick = {
                navController.navigate(centerItem.route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-28).dp)
                .size(56.dp),
            shape = CircleShape,
            containerColor = RcColor5,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 8.dp,
                pressedElevation = 12.dp,
                hoveredElevation = 10.dp
            )
        ) {
            Icon(
                imageVector = centerItem.icon,
                contentDescription = stringResource(centerItem.titleRes),
                modifier = Modifier.size(28.dp),
                tint = Color.White
            )
        }
    }
}

