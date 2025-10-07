package com.wapps1.redcarga.core.navigation


sealed class Route(val route: String) {

    // ============================================
    // AUTH GRAPH - Rutas Públicas
    // ============================================

    data object Welcome : Route("welcome")

    data object ChooseAccountType : Route("choose_account_type")

    data object SignIn : Route("sign_in")

    data object SignUpClient : Route("sign_up_client")
    
    data object SignUpProvider : Route("sign_up_provider")

    // ============================================
    // MAIN GRAPH - Rutas Protegidas
    // ============================================
    
    data object HomeScaffold : Route("home_scaffold")

    // CLIENT ROUTES
    data object ClientRequests : Route("client_requests")
    
    data object ClientQuotes : Route("client_quotes")
    
    data object ClientDeals : Route("client_deals")
    
    data object ClientTemplates : Route("client_templates")
    
    data object ClientTracking : Route("client_tracking")
    
    data object ClientProfile : Route("client_profile")

    // PROVIDER ROUTES
    data object ProviderFleet : Route("provider_fleet")
    
    data object ProviderRoutes : Route("provider_routes")
    
    data object ProviderRequests : Route("provider_requests")
    
    data object ProviderDeals : Route("provider_deals")
    
    data object ProviderTracking : Route("provider_tracking")
    
    data object ProviderProfile : Route("provider_profile")
}