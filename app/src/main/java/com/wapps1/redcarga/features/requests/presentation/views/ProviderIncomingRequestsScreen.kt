package com.wapps1.redcarga.features.requests.presentation.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderIncomingRequestsScreen(
    onQuote: (Long) -> Unit = {}, // Para crear cotización (requestId)
    onViewQuote: (Long) -> Unit = {}, // ⭐ Para ver cotización existente (quoteId)
    onChat: (Long) -> Unit = {}, // ⭐ Para ir al chat (quoteId)
    viewModel: com.wapps1.redcarga.features.requests.presentation.viewmodels.ProviderIncomingRequestsViewModel = hiltViewModel()
) {
    // Observar datos del ViewModel
    val requests by viewModel.incomingRequests.collectAsState()
    val quotedRequestIds by viewModel.quotedRequestIds.collectAsState() // ⭐ IDs de solicitudes cotizadas
    val acceptedQuotesState by viewModel.acceptedQuotesState.collectAsState() // ⭐ Mapa requestId -> stateCode
    val uiState by viewModel.uiState.collectAsState()
    val detailState by viewModel.detailState.collectAsState()
    val newRequestNotification by viewModel.newRequestNotification.collectAsState()
    val lastNewRequestId by viewModel.lastNewRequestId.collectAsState()
    val refreshErrorAfterNotification by viewModel.refreshErrorAfterNotification.collectAsState() // ⭐ Error de refresh después de WebSocket

    val tabs = listOf("Todas", "Abiertas", "Cotizadas", "Cotizaciones aceptadas")
    var selectedTabIndex by remember { mutableStateOf(0) }
    var search by remember { mutableStateOf("") }

    // ⭐ Filtrar requests basado en tab y búsqueda (NUEVO FILTRADO CON COTIZACIONES)
    val filteredRequests = remember(requests, quotedRequestIds, acceptedQuotesState, selectedTabIndex, search) {
        val acceptedRequestIds = acceptedQuotesState.keys
        val filtered = when (selectedTabIndex) {
            0 -> requests // Todas (sin importar qué)
            1 -> requests.filter { it.requestId !in quotedRequestIds } // ⭐ Abiertas (NO cotizadas)
            2 -> requests.filter { 
                it.requestId in quotedRequestIds && it.requestId !in acceptedRequestIds 
            } // ⭐ Cotizadas (YA cotizadas pero NO aceptadas)
            3 -> requests.filter { it.requestId in acceptedRequestIds } // ⭐ Cotizaciones aceptadas (TRATO, ACEPTADA, CERRADA)
            else -> requests
        }
        filtered.filter {
            it.requesterName.contains(search, true) ||
                    it.getRouteDescription().contains(search, true)
        }
    }

    val isLoading = uiState is com.wapps1.redcarga.features.requests.presentation.viewmodels.ProviderIncomingRequestsViewModel.UiState.Loading
    val hasError = uiState is com.wapps1.redcarga.features.requests.presentation.viewmodels.ProviderIncomingRequestsViewModel.UiState.Error

    // ⭐ MEJORADO: Snackbar para mostrar errores de refresh después de notificación WebSocket
    val snackbarHostState = remember { SnackbarHostState() }
    
    // ⭐ MEJORADO: Mostrar error de refresh después de notificación WebSocket
    LaunchedEffect(refreshErrorAfterNotification) {
        refreshErrorAfterNotification?.let { errorMsg ->
            snackbarHostState.showSnackbar(
                message = errorMsg,
                duration = androidx.compose.material3.SnackbarDuration.Long
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Solicitudes", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    // ⭐ Botón de refresh elegante
                    androidx.compose.material3.IconButton(
                        onClick = { viewModel.refreshAllData() },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refrescar",
                            tint = Color(0xFFFF8A65)
                        )
                    }
                },
                scrollBehavior = rememberTopAppBarState().let { null }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Color(0xFFE53935), // Rojo para errores
                    contentColor = Color.White
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(listOf(Color(0xFFFFF3ED), Color(0xFFFDF7F5)))
                    )
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))
                
                // ⭐ LazyRow para tabs que ocupan el 100% del espacio disponible
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    items(tabs.size) { index ->
                        val label = tabs[index]
                        val isSelected = selectedTabIndex == index
                        
                        Box(
                            modifier = Modifier
                                .fillParentMaxWidth(1f / tabs.size)
                                .height(48.dp)
                                .clickable { selectedTabIndex = index }
                                .background(
                                    color = if (isSelected) Color(0xFFFF8A65) else Color.Transparent,
                                    shape = when (index) {
                                        0 -> RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp, topEnd = 0.dp, bottomEnd = 0.dp)
                                        tabs.size - 1 -> RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = 12.dp, bottomEnd = 12.dp)
                                        else -> RoundedCornerShape(0.dp)
                                    }
                                )
                                .then(
                                    if (!isSelected && index < tabs.size - 1) {
                                        Modifier.border(1.dp, Color(0xFFE0E0E0).copy(alpha = 0.3f), RoundedCornerShape(0.dp))
                                    } else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF6C757D),
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                ElegantSearchBar(value = search, onValueChange = { search = it }, placeholder = "Buscar solicitudes")

                Spacer(modifier = Modifier.height(8.dp))

                when {
                    isLoading -> ProviderLoading()
                    hasError -> ProviderError { viewModel.refreshRequests() }
                    filteredRequests.isEmpty() -> ProviderEmpty()
                    else -> {
                        AnimatedVisibility(visible = true, enter = fadeIn() + slideInVertically(), exit = fadeOut() + slideOutVertically()) {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(filteredRequests) { req ->
                                    val isNew = req.requestId == lastNewRequestId // ⭐ Determinar si es nueva
                                    val isQuoted = req.requestId in quotedRequestIds // ⭐ Determinar si está cotizada
                                    val isAccepted = req.requestId in acceptedQuotesState.keys // ⭐ Determinar si está aceptada
                                    val quoteState = acceptedQuotesState[req.requestId] // ⭐ Estado de la cotización
                                    ProviderRequestCard(
                                        request = req,
                                        onQuote = {
                                            // ⭐ Si está cotizada, obtener el quoteId y ver cotización
                                            // Si NO está cotizada, ir a crear cotización
                                            if (isQuoted) {
                                                val quoteId = viewModel.getQuoteIdForRequest(req.requestId)
                                                quoteId?.let { onViewQuote(it) }
                                            } else {
                                                onQuote(req.requestId)
                                            }
                                        },
                                        onViewDetails = { viewModel.loadRequestDetails(req.requestId) },
                                        onDelete = { viewModel.deleteRequest(req.requestId) },
                                        onChat = {
                                            // ⭐ Ir al chat solo si el estado es TRATO
                                            val quoteId = viewModel.getQuoteIdForRequest(req.requestId)
                                            quoteId?.let { onChat(it) }
                                        },
                                        isNew = isNew, // ⭐ Pasar el estado de nueva
                                        isQuoted = isQuoted, // ⭐ Pasar el estado de cotizada
                                        isAccepted = isAccepted, // ⭐ Pasar el estado de aceptada
                                        quoteState = quoteState // ⭐ Pasar el estado de la cotización
                                    )
                                }
                            }
                        }
                    }
                }
            } // ⭐ Cerrar el Column

            // ⭐ NUEVO: Banner flotante para notificaciones
            AnimatedVisibility(
                visible = newRequestNotification != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
            ) {
                NewRequestBanner(
                    message = newRequestNotification ?: "",
                    onDismiss = { viewModel.dismissNotification() }
                )
            }
        } // ⭐ Cerrar el Box

        // Modal de detalles
        if (detailState !is com.wapps1.redcarga.features.requests.presentation.viewmodels.ProviderIncomingRequestsViewModel.DetailState.Idle) {
            com.wapps1.redcarga.features.requests.presentation.views.RequestDetailModal(
                detailState = when (detailState) {
                    is com.wapps1.redcarga.features.requests.presentation.viewmodels.ProviderIncomingRequestsViewModel.DetailState.Loading ->
                        com.wapps1.redcarga.features.requests.presentation.viewmodels.ClientRequestsViewModel.DetailState.Loading
                    is com.wapps1.redcarga.features.requests.presentation.viewmodels.ProviderIncomingRequestsViewModel.DetailState.Success ->
                        com.wapps1.redcarga.features.requests.presentation.viewmodels.ClientRequestsViewModel.DetailState.Success(
                            (detailState as com.wapps1.redcarga.features.requests.presentation.viewmodels.ProviderIncomingRequestsViewModel.DetailState.Success).request
                        )
                    is com.wapps1.redcarga.features.requests.presentation.viewmodels.ProviderIncomingRequestsViewModel.DetailState.Error ->
                        com.wapps1.redcarga.features.requests.presentation.viewmodels.ClientRequestsViewModel.DetailState.Error(
                            (detailState as com.wapps1.redcarga.features.requests.presentation.viewmodels.ProviderIncomingRequestsViewModel.DetailState.Error).message
                        )
                    else -> com.wapps1.redcarga.features.requests.presentation.viewmodels.ClientRequestsViewModel.DetailState.Idle
                },
                onDismiss = { viewModel.closeDetails() }
            )
        }
    }
}

@Composable
private fun ProviderLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Color(0xFFFF6F4E))
    }
}

@Composable
private fun ProviderError(onRetry: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) {
        Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = Color(0xFFFF6F4E), modifier = Modifier.size(56.dp))
        Spacer(Modifier.height(8.dp))
        Text("No se pudieron cargar las solicitudes")
        Spacer(Modifier.height(8.dp))
        Button(onClick = onRetry) { Row { Icon(Icons.Filled.Refresh, null); Spacer(Modifier.size(6.dp)); Text("Reintentar") } }
    }
}

@Composable
private fun ProviderEmpty() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) {
        Text("Sin solicitudes por ahora")
        Text("Cuando los clientes publiquen, las verás aquí", color = Color.DarkGray.copy(alpha = 0.6f))
    }
}

@Composable
private fun ProviderRequestCard(
    request: com.wapps1.redcarga.features.requests.domain.models.IncomingRequestSummary,
    onQuote: () -> Unit,
    onViewDetails: () -> Unit,
    onDelete: () -> Unit,
    onChat: () -> Unit = {}, // ⭐ NUEVO: callback para ir al chat
    isNew: Boolean = false,
    isQuoted: Boolean = false, // ⭐ NUEVO: indica si ya se cotizó
    isAccepted: Boolean = false, // ⭐ NUEVO: indica si está aceptada
    quoteState: String? = null // ⭐ NUEVO: estado de la cotización (TRATO, ACEPTADA, CERRADA)
) {
    val dateFormatter = remember {
        java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
    }

    // Animación de brillo para solicitudes nuevas
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Card(
        elevation = CardDefaults.cardElevation(
            defaultElevation = when {
                isNew -> 16.dp
                isQuoted -> 8.dp
                else -> 4.dp
            }
        ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .then(
                when {
                    isNew -> {
                        Modifier.border(
                            width = 3.dp,
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFFFC107).copy(alpha = alpha),
                                    Color(0xFFFFD54F).copy(alpha = alpha)
                                )
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                    }
                    isQuoted -> {
                        Modifier.border(
                            width = 2.dp,
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF4CAF50), Color(0xFF66BB6A))
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                    }
                    else -> Modifier
                }
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ⭐ NUEVO: Cinta superior elegante con gradiente
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(
                        brush = when {
                            isNew -> Brush.horizontalGradient(
                                listOf(Color(0xFFFFC107), Color(0xFFFFD54F), Color(0xFFFFC107))
                            )
                            isQuoted -> Brush.horizontalGradient(
                                listOf(Color(0xFF4CAF50), Color(0xFF66BB6A), Color(0xFF81C784))
                            )
                            else -> Brush.horizontalGradient(
                                listOf(Color(0xFFFF8A65), Color(0xFFFF7043))
                            )
                        },
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
            )

            // ⭐ CONTENIDO PRINCIPAL REDISEÑADO
            Column(modifier = Modifier.padding(20.dp)) {
                // Header con avatar y nombre
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar circular elegante
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                brush = when {
                                    isNew -> Brush.radialGradient(
                                        colors = listOf(Color(0xFFFFC107), Color(0xFFFFD54F))
                                    )
                                    isQuoted -> Brush.radialGradient(
                                        colors = listOf(Color(0xFF4CAF50), Color(0xFF66BB6A))
                                    )
                                    else -> Brush.radialGradient(
                                        colors = listOf(Color(0xFFFF8A65), Color(0xFFFF7043))
                                    )
                                },
                                shape = CircleShape
                            )
                            .border(3.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = request.requesterName.take(1).uppercase(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Info del solicitante
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = request.requesterName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2C3E50)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Badge de estado
                            Box(
                                modifier = Modifier
                                    .background(
                                        brush = when {
                                            isAccepted -> Brush.horizontalGradient(
                                                listOf(Color(0xFF2196F3), Color(0xFF42A5F5))
                                            )
                                            isQuoted -> Brush.horizontalGradient(
                                                listOf(Color(0xFF4CAF50), Color(0xFF66BB6A))
                                            )
                                            isNew -> Brush.horizontalGradient(
                                                listOf(Color(0xFFFFC107), Color(0xFFFFD54F))
                                            )
                                            else -> Brush.horizontalGradient(
                                                listOf(Color(0xFFFF8A65), Color(0xFFFF7043))
                                            )
                                        },
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = when {
                                        isAccepted -> "ACEPTADA ✓"
                                        isQuoted -> "COTIZADA ✓"
                                        isNew -> "NUEVA ⭐"
                                        else -> request.status.name
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            
                            // ⭐ Badge de estado de cotización (solo si está aceptada)
                            if (isAccepted && quoteState != null) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = when (quoteState) {
                                                "TRATO" -> Color(0xFFFF9800)
                                                "ACEPTADA" -> Color(0xFF4CAF50)
                                                "CERRADA" -> Color(0xFF9E9E9E)
                                                else -> Color(0xFF757575)
                                            },
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = quoteState,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Ruta con iconos elegantes
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF8F9FA)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Icono de ubicación origen
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    Color(0xFF3498DB).copy(alpha = 0.1f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📍", fontSize = 20.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = request.getRouteDescription(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF2C3E50),
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Info cards compactas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Items
                    MiniInfoCard(
                        icon = "📦",
                        label = "Items",
                        value = "${request.totalQuantity}",
                        modifier = Modifier.weight(1f),
                        color = Color(0xFF9C27B0)
                    )

                    // Ruta ID
                    MiniInfoCard(
                        icon = "🛣️",
                        label = "Ruta",
                        value = "#${request.matchedRouteId}",
                        modifier = Modifier.weight(1f),
                        color = Color(0xFF2196F3)
                    )

                    // Fecha
                    MiniInfoCard(
                        icon = "📅",
                        label = "Fecha",
                        value = dateFormatter.format(java.util.Date.from(request.createdAt)),
                        modifier = Modifier.weight(1f),
                        color = Color(0xFFFF9800)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ⭐ BOTONES DE ACCIÓN REDISEÑADOS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Botón "Ver Detalles" con diseño elegante
                    OutlinedButton(
                        onClick = onViewDetails,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color(0xFF6C757D)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            2.dp,
                            Color(0xFFE0E0E0)
                        )
                    ) {
                        Text(
                            "Ver Detalles",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Botón principal condicional
                    if (isAccepted && (quoteState == "TRATO" || quoteState == "ACEPTADA")) {
                        // ⭐ Botón "Ir al Chat" - Si está aceptada y en estado TRATO o ACEPTADA
                        Button(
                            onClick = onChat,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFF2196F3),
                                                Color(0xFF42A5F5)
                                            )
                                        ),
                                        shape = RoundedCornerShape(14.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        "💬",
                                        fontSize = 18.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Ir al Chat",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    } else if (isQuoted) {
                        // Botón "Ver Cotización" - Diseño verde premium
                        Button(
                            onClick = onQuote,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFF4CAF50),
                                                Color(0xFF66BB6A)
                                            )
                                        ),
                                        shape = RoundedCornerShape(14.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        "💰",
                                        fontSize = 18.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Ver Cotización",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    } else {
                        // Botón "Cotizar" - Diseño naranja premium
                        Button(
                            onClick = onQuote,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFFFF8A65),
                                                Color(0xFFFF7043)
                                            )
                                        ),
                                        shape = RoundedCornerShape(14.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        "✍️",
                                        fontSize = 18.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Cotizar",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ElegantSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(Color.White, RoundedCornerShape(18.dp))
            .border(1.dp, Color(0xFFFFD8CC), RoundedCornerShape(18.dp))
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color(0xFFE06442), modifier = Modifier.size(18.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = Color(0xFF1B1B1B).copy(alpha = 0.45f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            AnimatedVisibility(visible = value.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
                TextButton(
                    onClick = { onValueChange("") },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("×", color = Color(0xFFE06442), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Mini card para mostrar información compacta con icono
 */
@Composable
private fun MiniInfoCard(
    icon: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    color: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = icon,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color(0xFF6C757D)
            )
        }
    }
}

@Composable
private fun TagChip(text: String) {
    Box(
        modifier = Modifier
            .background(Color(0xFFFFF0EB), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text = text, color = Color(0xFFFF6F4E), fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun GradientActionButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, textStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium) {
    val gradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFF8A65), Color(0xFFFF7043))
    )
    Box(
        modifier = modifier
            .background(brush = gradient, shape = RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Color.White, fontWeight = FontWeight.SemiBold, style = textStyle)
    }
}

/**
 * Banner flotante hermoso que aparece cuando llega una nueva solicitud
 */
@Composable
private fun NewRequestBanner(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .padding(top = 12.dp, start = 8.dp, end = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4CAF50) // Verde vibrante
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF4CAF50), Color(0xFF66BB6A))
                    )
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Ícono animado
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = message,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Toca para ver",
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            androidx.compose.material3.IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(32.dp)
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Cerrar",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}


