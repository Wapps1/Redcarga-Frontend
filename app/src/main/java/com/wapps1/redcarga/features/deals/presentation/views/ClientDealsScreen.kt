package com.wapps1.redcarga.features.deals.presentation.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wapps1.redcarga.R
import com.wapps1.redcarga.features.deals.presentation.viewmodels.ClientDealsViewModel
import com.wapps1.redcarga.features.requests.domain.models.RequestSummary
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ClientDealsScreen(
    onBack: () -> Unit = {},
    onOpenChat: () -> Unit = {},
    onOpenQuoteDetails: () -> Unit = {},
    viewModel: ClientDealsViewModel = hiltViewModel()
) {
    val tabs = listOf(
        stringResource(R.string.client_deals_tab_all),
        stringResource(R.string.client_deals_tab_in_deal),
        stringResource(R.string.client_deals_tab_in_progress)
    )
    var selectedTabIndex by remember { mutableStateOf(0) }

    // ⭐ Obtener solicitudes reales del ViewModel
    val requests by viewModel.clientRequests.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    // ⭐ Mapear RequestSummary a SolicitudUi
    val dateFormatter = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    }

    val solicitudes = remember(requests) {
        requests.map { request ->
            SolicitudUi(
                requestId = request.requestId,
                titulo = request.requestName,
                dia = dateFormatter.format(Date(request.createdAt.toEpochMilli())),
                origen = request.origin.getFullLocation(),
                destino = request.destination.getFullLocation()
            )
        }
    }

    var expanded by remember { mutableStateOf(false) }
    var selectedSolicitud by remember(solicitudes) {
        mutableStateOf(solicitudes.firstOrNull())
    }
    var search by remember { mutableStateOf("") }

    // ⭐ Actualizar selectedSolicitud cuando cambien las solicitudes
    LaunchedEffect(solicitudes) {
        if (solicitudes.isNotEmpty() && selectedSolicitud == null) {
            selectedSolicitud = solicitudes.first()
        } else if (solicitudes.isEmpty()) {
            selectedSolicitud = null
        } else if (selectedSolicitud != null) {
            // Si la solicitud seleccionada ya no existe, seleccionar la primera
            val stillExists = solicitudes.any { it.requestId == selectedSolicitud!!.requestId }
            if (!stillExists) {
                selectedSolicitud = solicitudes.first()
            }
        }
    }

    // ⭐ Determinar el estado según el tab seleccionado
    val currentState = remember(selectedTabIndex) {
        when (selectedTabIndex) {
            0 -> null // "Todas" - sin filtro
            1 -> "TRATO" // "En Negociación"
            2 -> "TRATO" // "En Proceso" (igual que En Negociación)
            else -> null
        }
    }

    // ⭐ Cargar cotizaciones automáticamente cuando se seleccione una solicitud o cambie el tab
    LaunchedEffect(selectedSolicitud?.requestId, selectedTabIndex) {
        selectedSolicitud?.requestId?.let { requestId ->
            viewModel.loadQuotesForRequest(requestId, currentState)
        }
    }

    // ⭐ Observar cotizaciones del ViewModel
    val quotesByRequestId by viewModel.quotesByRequestId.collectAsState()
    val quotesLoadingState by viewModel.quotesLoadingState.collectAsState()
    val actionMessage by viewModel.actionMessage.collectAsState()
    val processingAction by viewModel.processingAction.collectAsState()

    // ⭐ Snackbar para mensajes
    val snackbarHostState = remember { SnackbarHostState() }

    // ⭐ Mostrar mensajes de acción
    LaunchedEffect(actionMessage) {
        actionMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = when (message) {
                    is ClientDealsViewModel.ActionMessage.Success -> message.message
                    is ClientDealsViewModel.ActionMessage.Error -> message.message
                },
                duration = androidx.compose.material3.SnackbarDuration.Long
            )
            viewModel.clearActionMessage()
        }
    }

    // ⭐ Usar el estado del ViewModel
    val isLoading = uiState is ClientDealsViewModel.UiState.Loading
    val hasError = uiState is ClientDealsViewModel.UiState.Error

    // ⭐ Obtener cotizaciones reales de la solicitud seleccionada con el estado correcto
    val cotizaciones = remember(selectedSolicitud, selectedTabIndex, search, quotesByRequestId, currentState) {
        selectedSolicitud?.let { solicitud ->
            val stateKey = currentState ?: "ALL"
            val quotesKey = "${solicitud.requestId}_$stateKey"
            val quotes = quotesByRequestId[quotesKey] ?: emptyList()
            
            // Mapear QuoteDetail a CotizacionUi
            quotes.map { quote ->
                CotizacionUi(
                    quoteId = quote.quoteId,
                    empresa = "Empresa ${quote.companyId}", // Por ahora usamos el ID, luego se puede obtener el nombre
                    paraSolicitud = solicitud.titulo,
                    rating = 4, // TODO: Obtener rating real cuando esté disponible
                    precio = quote.totalAmount.toInt(),
                    currencyCode = quote.currencyCode,
                    stateCode = quote.stateCode,
                    createdAt = quote.createdAt
                )
            }.filter { it.empresa.contains(search, ignoreCase = true) }
        } ?: emptyList()
    }

    // ⭐ Verificar si se están cargando cotizaciones
    val isLoadingQuotes = selectedSolicitud?.requestId?.let { requestId ->
        val stateKey = currentState ?: "ALL"
        val loadingKey = "${requestId}_$stateKey"
        quotesLoadingState[loadingKey] ?: false
    } ?: false

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = when (actionMessage) {
                        is ClientDealsViewModel.ActionMessage.Success -> Color(0xFF4CAF50)
                        is ClientDealsViewModel.ActionMessage.Error -> Color(0xFFE53935)
                        null -> MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Contenido principal con fondo
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFFFF3ED), Color(0xFFFDF7F5))
                        )
                    )
            ) {
                // Espacio para el header más compacto
                Spacer(modifier = Modifier.height(120.dp))

                // Contenido desplazable
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // ⭐ Selector de solicitud (con datos reales)
                    if (solicitudes.isEmpty()) {
                        // Mostrar mensaje si no hay solicitudes
                        OutlinedCard(
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No tienes solicitudes creadas",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "Crea una solicitud para ver sus cotizaciones",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    } else {
                        Box {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.client_deals_select_request),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                                )
                                androidx.compose.material3.Surface(
                                    tonalElevation = 0.dp,
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFFFFF0EB),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp)
                                        .border(1.dp, Color(0xFFFFD8CC), RoundedCornerShape(14.dp)),
                                    onClick = { if (solicitudes.isNotEmpty()) expanded = true }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(22.dp)
                                                    .background(Color(0xFFFFE0D7), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = selectedSolicitud?.titulo?.take(1) ?: "?",
                                                    color = Color(0xFFFF6F4E),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Spacer(modifier = Modifier.size(8.dp))
                                            Text(
                                                text = selectedSolicitud?.titulo ?: "Selecciona una solicitud",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFFE06442))
                                    }
                                }
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                solicitudes.forEach { item ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(18.dp)
                                                        .background(Color(0xFFFFEDE6), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = item.titulo.take(1),
                                                        fontSize = 10.sp,
                                                        color = Color(0xFFFF6F4E),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                Spacer(modifier = Modifier.size(8.dp))
                                                Column {
                                                    Text(
                                                        text = item.titulo,
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                    Text(
                                                        text = item.dia,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            selectedSolicitud = item
                                            expanded = false
                                        },
                                        trailingIcon = { Icon(Icons.Default.ArrowForward, contentDescription = null) }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // ⭐ Resumen de la solicitud (solo si hay una seleccionada)
                    selectedSolicitud?.let { solicitud ->
                        OutlinedCard(
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Avatar redondo con inicial
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(Color(0xFFFFE0D7), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = solicitud.titulo.take(1),
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFF6F4E)
                                        )
                                    }
                                    Spacer(modifier = Modifier.size(10.dp))
                                    Text(
                                        text = solicitud.titulo,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                KeyValueRow(stringResource(R.string.client_deals_request_day), solicitud.dia)
                                KeyValueRow(stringResource(R.string.client_deals_request_origin), solicitud.origen)
                                KeyValueRow(stringResource(R.string.client_deals_request_destination), solicitud.destino)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ⭐ Solo mostrar búsqueda y cotizaciones si hay una solicitud seleccionada
                    if (selectedSolicitud != null) {
                        ElegantSearchBar(
                            value = search,
                            onValueChange = { search = it },
                            placeholder = stringResource(R.string.client_deals_search_placeholder)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Estados de la UI
                        when {
                            isLoadingQuotes -> {
                                LoadingState()
                            }
                            cotizaciones.isEmpty() -> {
                                EmptyState()
                            }
                            else -> {
                                AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn() + slideInVertically(),
                                    exit = fadeOut() + slideOutVertically()
                                ) {
                                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        items(cotizaciones) { quote ->
                                            CotizacionCard(
                                                cotizacion = quote,
                                                requestId = selectedSolicitud?.requestId ?: 0L,
                                                isProcessing = processingAction[quote.quoteId] ?: false,
                                                onStartNegotiation = { quoteId ->
                                                    selectedSolicitud?.requestId?.let { requestId ->
                                                        viewModel.startNegotiation(quoteId, requestId)
                                                    }
                                                },
                                                onReject = { quoteId ->
                                                    selectedSolicitud?.requestId?.let { requestId ->
                                                        viewModel.rejectQuote(quoteId, requestId)
                                                    }
                                                },
                                                onDetalles = onOpenQuoteDetails,
                                                onChat = onOpenChat
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else if (solicitudes.isNotEmpty()) {
                        // Mensaje para seleccionar una solicitud
                        OutlinedCard(
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Selecciona una solicitud",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Elige una solicitud del selector para ver sus cotizaciones",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Header flotante encima
            CustomDealsHeader(
                tabs = tabs,
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it }
            )
        }
    }
}

@Composable
private fun CustomDealsHeader(
    tabs: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    val gradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFF8A65), Color(0xFFFF7043))
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = gradient,
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            )
            .padding(top = 12.dp, bottom = 16.dp, start = 20.dp, end = 20.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Título más pequeño
            Text(
                text = "Cotizaciones",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            // Tabs tipo pill más compactos
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, label ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .background(
                                color = if (selectedTabIndex == index) Color.White else Color.Transparent,
                                shape = RoundedCornerShape(18.dp)
                            )
                            .border(
                                width = 1.5.dp,
                                color = Color.White,
                                shape = RoundedCornerShape(18.dp)
                            )
                            .clickable { onTabSelected(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (selectedTabIndex == index) Color(0xFFFF8A65) else Color.White,
                            fontWeight = if (selectedTabIndex == index) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = Color(0xFFFF6F4E)
            )
            Text(
                text = stringResource(R.string.client_deals_loading),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ErrorState(onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color(0xFFFF6F4E)
            )
            Text(
                text = stringResource(R.string.client_deals_error_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.client_deals_error_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onRetry,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF6F4E)
                )
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text(stringResource(R.string.client_deals_retry))
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFFE0D7), Color(0xFFFFF0EB))
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = Color(0xFFFF6F4E)
                )
            }
            Text(
                text = stringResource(R.string.client_deals_empty_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.client_deals_empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun KeyValueRow(key: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = key, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Text(text = value, style = MaterialTheme.typography.bodySmall)
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
            .height(46.dp)
            .background(Color.White, RoundedCornerShape(24.dp))
            .border(1.dp, Color(0xFFFFD8CC), RoundedCornerShape(24.dp))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color(0xFFE06442))
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
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
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("×", color = Color(0xFFE06442), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun RatingStars(rating: Int) {
    Row {
        repeat(5) { index ->
            val color = if (index < rating) Color(0xFFFF9800) else MaterialTheme.colorScheme.outline
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun GradientActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val gradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFF8A65), Color(0xFFFF7043))
    )
    Box(
        modifier = modifier
            .background(brush = gradient, shape = RoundedCornerShape(12.dp))
            .then(if (!enabled) Modifier.alpha(0.6f) else Modifier)
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Color.White, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun CotizacionCard(
    cotizacion: CotizacionUi,
    requestId: Long,
    isProcessing: Boolean,
    onStartNegotiation: (Long) -> Unit,
    onReject: (Long) -> Unit,
    onDetalles: () -> Unit,
    onChat: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        // Cinta superior con gradiente suave
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFFFF8A65), Color(0xFFFF7043))
                    ),
                    RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
                )
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                // Avatar de empresa
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color(0xFFFFEDE6), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = cotizacion.empresa.take(1), fontWeight = FontWeight.Bold, color = Color(0xFFFF6F4E))
                }
                Spacer(modifier = Modifier.size(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${cotizacion.empresa} ${stringResource(R.string.client_deals_quote_made_by)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(R.string.client_deals_quote_for, cotizacion.paraSolicitud),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
                // Chip elegante para mostrar el estado real
                TagChip(
                    text = when (cotizacion.stateCode.uppercase()) {
                        "PENDING" -> "PENDIENTE"
                        "TRATO" -> "EN TRATO"
                        "RECHAZADA" -> "RECHAZADA"
                        else -> "PENDIENTE"
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = stringResource(R.string.client_deals_company_rating), style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    RatingStars(rating = cotizacion.rating.coerceIn(0, 5))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = stringResource(R.string.client_deals_price_proposed), style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(2.dp))
                    PricePill(text = "${cotizacion.currencyCode} ${cotizacion.precio}")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ⭐ Mostrar botones según el estado de la cotización
            when (cotizacion.stateCode.uppercase()) {
                "PENDING" -> {
                    // Estado PENDING: Botones Denegar e Iniciar Trato
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Botón Denegar
                        TextButton(
                            onClick = { onReject(cotizacion.quoteId) },
                            enabled = !isProcessing,
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, Color(0xFFE53935), RoundedCornerShape(12.dp))
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color(0xFFE53935),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Denegar",
                                    color = Color(0xFFE53935),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        
                        // Botón Iniciar Trato
                        GradientActionButton(
                            text = if (isProcessing) "Procesando..." else "Iniciar Trato",
                            onClick = { onStartNegotiation(cotizacion.quoteId) },
                            modifier = Modifier.weight(1f),
                            enabled = !isProcessing
                        )
                    }
                }
                "TRATO" -> {
                    // Estado TRATO: Botones Denegar y Chat
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Botón Denegar
                        TextButton(
                            onClick = { onReject(cotizacion.quoteId) },
                            enabled = !isProcessing,
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, Color(0xFFE53935), RoundedCornerShape(12.dp))
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color(0xFFE53935),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Denegar",
                                    color = Color(0xFFE53935),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        
                        // Botón Chat
                        GradientActionButton(
                            text = "Chat",
                            onClick = onChat,
                            modifier = Modifier.weight(1f),
                            enabled = !isProcessing
                        )
                    }
                }
                "RECHAZADA" -> {
                    // Estado RECHAZADA: Solo texto, sin botones
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFEBEE), RoundedCornerShape(12.dp))
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "HA SIDO RECHAZADO",
                            color = Color(0xFFE53935),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                else -> {
                    // Estado desconocido: Mostrar botones por defecto (PENDING)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextButton(
                            onClick = { onReject(cotizacion.quoteId) },
                            enabled = !isProcessing,
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, Color(0xFFE53935), RoundedCornerShape(12.dp))
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color(0xFFE53935),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Denegar",
                                    color = Color(0xFFE53935),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        
                        GradientActionButton(
                            text = if (isProcessing) "Procesando..." else "Iniciar Trato",
                            onClick = { onStartNegotiation(cotizacion.quoteId) },
                            modifier = Modifier.weight(1f),
                            enabled = !isProcessing
                        )
                    }
                }
            }
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
private fun PricePill(text: String) {
    Box(
        modifier = Modifier
            .background(
                brush = Brush.horizontalGradient(listOf(Color(0xFFFF8A65), Color(0xFFFF7043))),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = text, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

private data class SolicitudUi(
    val requestId: Long, // ⭐ ID de la solicitud para futuras consultas
    val titulo: String,
    val dia: String,
    val origen: String,
    val destino: String
)

private data class CotizacionUi(
    val quoteId: Long,
    val empresa: String,
    val paraSolicitud: String,
    val rating: Int,
    val precio: Int,
    val currencyCode: String,
    val stateCode: String,
    val createdAt: java.time.Instant
)