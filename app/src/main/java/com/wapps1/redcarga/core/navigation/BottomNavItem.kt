package com.wapps1.redcarga.core.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.wapps1.redcarga.R
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.*

/**
 * Representa un item de la barra de navegación inferior
 * Usando Font Awesome Icons para mayor variedad y profesionalismo
 */
sealed class BottomNavItem(
    val route: String,
    @StringRes val titleRes: Int,
    val icon: ImageVector
) {
    // Items para Cliente
    data object ClientHome : BottomNavItem("client_home", R.string.nav_home, FontAwesomeIcons.Solid.Home)
    data object ClientQuotes : BottomNavItem("client_quotes", R.string.nav_quotes, FontAwesomeIcons.Solid.FileInvoiceDollar)
    data object ClientRequest : BottomNavItem("client_request", R.string.nav_make_request, FontAwesomeIcons.Solid.BoxOpen)
    data object ClientChat : BottomNavItem("client_chat", R.string.nav_chat, FontAwesomeIcons.Solid.CommentDots)
    data object ClientProfile : BottomNavItem("client_profile", R.string.nav_profile, FontAwesomeIcons.Solid.User)

    // Items para Proveedor
    data object ProviderHome : BottomNavItem("provider_home", R.string.nav_home, FontAwesomeIcons.Solid.Home)
    data object ProviderRequests : BottomNavItem("provider_requests", R.string.nav_requests, FontAwesomeIcons.Solid.Inbox)
    data object ProviderGeo : BottomNavItem("provider_geo", R.string.nav_geolocation, FontAwesomeIcons.Solid.MapMarkedAlt)
    data object ProviderChat : BottomNavItem("provider_chat", R.string.nav_chat, FontAwesomeIcons.Solid.CommentDots)
    data object ProviderProfile : BottomNavItem("provider_profile", R.string.nav_profile, FontAwesomeIcons.Solid.User)

    companion object {
        fun getClientItems() = listOf(
            ClientHome,
            ClientQuotes,
            ClientRequest,
            ClientChat,
            ClientProfile
        )

        fun getProviderItems() = listOf(
            ProviderHome,
            ProviderRequests,
            ProviderGeo,
            ProviderChat,
            ProviderProfile
        )
    }
}

