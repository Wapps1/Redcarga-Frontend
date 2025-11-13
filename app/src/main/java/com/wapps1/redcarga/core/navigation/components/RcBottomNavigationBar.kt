package com.wapps1.redcarga.core.navigation.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.wapps1.redcarga.core.ui.theme.RcColor5
import com.wapps1.redcarga.core.ui.theme.RcColor6

data class NavItem(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon
)

@Composable
fun RcBottomNavigationBar(
    items: List<NavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = Color.White,
        contentColor = RcColor6
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedIndex == index,
                onClick = { onItemSelected(index) },
                icon = {
                    Icon(
                        imageVector = if (selectedIndex == index) item.selectedIcon else item.icon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = RcColor5,
                    selectedTextColor = RcColor5,
                    unselectedIconColor = RcColor6.copy(alpha = 0.6f),
                    unselectedTextColor = RcColor6.copy(alpha = 0.6f),
                    indicatorColor = RcColor5.copy(alpha = 0.1f)
                )
            )
        }
    }
}

