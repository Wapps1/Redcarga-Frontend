package com.wapps1.redcarga.features.requests.presentation.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.wapps1.redcarga.R
import com.wapps1.redcarga.core.ui.theme.*
import com.wapps1.redcarga.features.requests.domain.models.RequestSummary
import com.wapps1.redcarga.features.requests.presentation.viewmodels.ClientRequestsViewModel
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Box
import compose.icons.fontawesomeicons.solid.CalendarAlt
import compose.icons.fontawesomeicons.solid.ExclamationTriangle
import compose.icons.fontawesomeicons.solid.MapMarkedAlt
import compose.icons.fontawesomeicons.solid.WeightHanging
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ClientRequestsScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToCreateRequest: () -> Unit = {},
    viewModel: ClientRequestsViewModel = hiltViewModel()
) {
    // Observar solicitudes del ViewModel
    val requests by viewModel.clientRequests.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val detailState by viewModel.detailState.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateRequest,
                containerColor = RcColor5,
                contentColor = White,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.client_requests_create_new),
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        containerColor = RcColor1
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
                    .background(RcColor1)
            ) {
                // Espacio para el header
                Spacer(modifier = Modifier.height(80.dp))

                // Manejar estados de la UI
                when (uiState) {
                    is ClientRequestsViewModel.UiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = RcColor5)
                        }
                    }
                    is ClientRequestsViewModel.UiState.Error -> {
                        ErrorStateContent(
                            message = (uiState as ClientRequestsViewModel.UiState.Error).message,
                            onRetry = { viewModel.refreshRequests() }
                        )
                    }
                    is ClientRequestsViewModel.UiState.Success -> {
                        // Lista de contenido
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Header con estadísticas
                            item {
                                RequestStatsCard(
                                    total = viewModel.getTotalRequests(),
                                    active = viewModel.getActiveRequests(),
                                    completed = viewModel.getCompletedRequests()
                                )
                            }

                            // Lista de solicitudes
                            items(requests) { request ->
                                RequestCard(
                                    request = request,
                                    onViewDetails = { viewModel.loadRequestDetails(request.requestId) }
                                )
                            }

                            // Empty state si no hay solicitudes
                            if (requests.isEmpty()) {
                                item {
                                    EmptyRequestsCard()
                                }
                            }

                            // Espacio extra al final
                            item {
                                Spacer(modifier = Modifier.height(80.dp))
                            }
                        }
                    }
                }
            }

            // Header flotante encima
            RequestsCustomHeader()
        }

        // Modal de detalles
        if (detailState !is ClientRequestsViewModel.DetailState.Idle) {
            RequestDetailModal(
                detailState = detailState,
                onDismiss = { viewModel.closeDetails() }
            )
        }
    }
}

/**
 * Header custom con diseño degradado (sin tabs)
 */
@Composable
private fun RequestsCustomHeader() {
    val gradient = androidx.compose.ui.graphics.Brush.horizontalGradient(
        colors = listOf(Color(0xFFFF8A65), Color(0xFFFF7043))
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = gradient,
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            )
            .padding(top = 16.dp, bottom = 16.dp, start = 20.dp, end = 20.dp)
    ) {
        // Solo título, sin tabs
        Text(
            text = "Mis Solicitudes",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Card con estadísticas de solicitudes
 */
@Composable
private fun RequestStatsCard(
    total: Int,
    active: Int,
    completed: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = stringResource(R.string.client_requests_stats_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = RcColor6
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    title = stringResource(R.string.client_requests_total),
                    value = total.toString(),
                    color = RcColor4
                )
                StatItem(
                    title = stringResource(R.string.client_requests_active),
                    value = active.toString(),
                    color = RcColor5
                )
                StatItem(
                    title = stringResource(R.string.client_requests_completed),
                    value = completed.toString(),
                    color = RcColor3
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    title: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = RcColor8,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Card individual de solicitud
 */
@Composable
private fun RequestCard(
    request: RequestSummary,
    onViewDetails: () -> Unit
) {
    val dateFormatter = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Header con título y estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = request.requestName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = RcColor6,
                    modifier = Modifier.weight(1f)
                )

                RequestStatusChip(status = request.status.name)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Ruta
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = RcColor5.copy(alpha = 0.1f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.MapMarkedAlt,
                            contentDescription = null,
                            tint = RcColor5,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = request.getRouteDescription(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = RcColor6
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Información adicional
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Peso
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = FontAwesomeIcons.Solid.WeightHanging,
                        contentDescription = null,
                        tint = RcColor4,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${"%.1f".format(request.totalWeightKg.toDouble())} kg",
                        fontSize = 13.sp,
                        color = RcColor8,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Items
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = FontAwesomeIcons.Solid.Box,
                        contentDescription = null,
                        tint = RcColor3,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${request.itemsCount} items",
                        fontSize = 13.sp,
                        color = RcColor8,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Fecha
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = FontAwesomeIcons.Solid.CalendarAlt,
                        contentDescription = null,
                        tint = RcColor2,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = dateFormatter.format(Date(request.createdAt.toEpochMilli())),
                        fontSize = 13.sp,
                        color = RcColor8,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botón "Ver más"
            Button(
                onClick = onViewDetails,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RcColor5
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Ver más",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

/**
 * Chip de estado de solicitud
 */
@Composable
private fun RequestStatusChip(status: String) {
    val (backgroundColor, textColor) = when (status.lowercase()) {
        "open" -> RcColor5.copy(alpha = 0.1f) to RcColor5
        "in_progress" -> RcColor4.copy(alpha = 0.1f) to RcColor4
        "completed" -> RcColor3.copy(alpha = 0.1f) to RcColor3
        "cancelled" -> RcColor8.copy(alpha = 0.1f) to RcColor8
        else -> RcColor7 to RcColor6
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Text(
            text = status.replace("_", " ").uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/**
 * Card para estado vacío
 */
@Composable
private fun EmptyRequestsCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = FontAwesomeIcons.Solid.Box,
                contentDescription = null,
                tint = RcColor8,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.client_requests_empty_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = RcColor6,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.client_requests_empty_subtitle),
                fontSize = 14.sp,
                color = RcColor8,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

/**
 * Contenido para estado de error
 */
@Composable
private fun ErrorStateContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 3.dp
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = RcColor5,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Error al cargar",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = RcColor6,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = RcColor8,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RcColor5
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reintentar")
                }
            }
        }
    }
}

/**
 * Modal de detalles completo de una solicitud
 */
@Composable
public fun RequestDetailModal(
    detailState: ClientRequestsViewModel.DetailState,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(24.dp),
            color = RcColor1
        ) {
            when (detailState) {
                is ClientRequestsViewModel.DetailState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = RcColor5)
                    }
                }
                is ClientRequestsViewModel.DetailState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = "Error",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = RcColor5
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = detailState.message,
                                fontSize = 14.sp,
                                color = RcColor8,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = onDismiss) {
                                Text("Cerrar")
                            }
                        }
                    }
                }
                is ClientRequestsViewModel.DetailState.Success -> {
                    RequestDetailContent(
                        request = detailState.request,
                        onDismiss = onDismiss
                    )
                }
                else -> {}
            }
        }
    }
}

/**
 * Contenido del modal de detalles
 */
@Composable
private fun RequestDetailContent(
    request: com.wapps1.redcarga.features.requests.domain.models.Request,
    onDismiss: () -> Unit
) {
    val dateFormatter = remember {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Header del modal
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = listOf(Color(0xFFFF8A65), Color(0xFFFF7043))
                    ),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Detalles de Solicitud",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = request.requestName,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color.White
                    )
                }
            }
        }

        // Contenido desplazable
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Información general
            item {
                DetailSection(title = "Información General") {
                    DetailInfoRow("Estado", request.status.name.replace("_", " "))
                    DetailInfoRow("Creado", dateFormatter.format(Date(request.createdAt.toEpochMilli())))
                    DetailInfoRow("Actualizado", dateFormatter.format(Date(request.updatedAt.toEpochMilli())))
                    if (request.closedAt != null) {
                        DetailInfoRow("Cerrado", dateFormatter.format(Date(request.closedAt.toEpochMilli())))
                    }
                    DetailInfoRow("Solicitante", request.requesterNameSnapshot)
                    DetailInfoRow("Documento", request.requesterDocNumber)
                }
            }

            // Ubicaciones
            item {
                DetailSection(title = "Origen y Destino") {
                    LocationCard(
                        title = "Origen",
                        ubigeo = request.origin,
                        icon = FontAwesomeIcons.Solid.MapMarkedAlt
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LocationCard(
                        title = "Destino",
                        ubigeo = request.destination,
                        icon = FontAwesomeIcons.Solid.MapMarkedAlt
                    )
                }
            }

            // Resumen de envío
            item {
                DetailSection(title = "Resumen del Envío") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        SummaryItem(
                            label = "Items",
                            value = request.itemsCount.toString(),
                            icon = FontAwesomeIcons.Solid.Box,
                            color = RcColor3
                        )
                        SummaryItem(
                            label = "Peso Total",
                            value = "${"%.1f".format(request.totalWeightKg.toDouble())} kg",
                            icon = FontAwesomeIcons.Solid.WeightHanging,
                            color = RcColor4
                        )
                        SummaryItem(
                            label = "Frágiles",
                            value = request.getFragileItemsCount().toString(),
                            icon = compose.icons.FontAwesomeIcons.Solid.ExclamationTriangle,
                            color = RcColor5
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailInfoRow("Pago contra entrega", if (request.paymentOnDelivery) "Sí" else "No")
                }
            }

            // Items (Artículos)
            item {
                Text(
                    text = "Artículos",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = RcColor6,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(request.items.sortedBy { it.position }) { item ->
                ItemDetailCard(item = item)
            }

            // Espacio final
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = RcColor6,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
private fun DetailInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = RcColor8,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = RcColor6,
            modifier = Modifier.weight(0.6f),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun LocationCard(
    title: String,
    ubigeo: com.wapps1.redcarga.features.requests.domain.models.UbigeoSnapshot,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = RcColor5.copy(alpha = 0.05f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = RcColor5.copy(alpha = 0.15f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = RcColor5,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = RcColor8,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = ubigeo.getFullLocation(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RcColor6
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = color.copy(alpha = 0.1f),
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = RcColor6
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = RcColor8
        )
    }
}

@Composable
private fun ItemDetailCard(
    item: com.wapps1.redcarga.features.requests.domain.models.RequestItem
) {
    var showImageFullscreen by remember { mutableStateOf(false) }
    var selectedImageIndex by remember { mutableStateOf(0) }
    val sortedImages = remember(item.images) {
        item.images.sortedBy { it.imagePosition }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header del item con gradiente
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                            colors = listOf(
                                RcColor5.copy(alpha = 0.1f),
                                RcColor4.copy(alpha = 0.05f)
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.itemName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = RcColor6
                        )
                        if (item.itemId != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "ID: #${item.itemId} • Posición: ${item.position}",
                                fontSize = 11.sp,
                                color = RcColor8,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    if (item.fragile) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = RcColor5
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = compose.icons.FontAwesomeIcons.Solid.ExclamationTriangle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "FRÁGIL",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sección: Dimensiones (simplificada)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = RcColor7,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Dimensiones",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = RcColor6,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Alto:",
                            fontSize = 13.sp,
                            color = RcColor8
                        )
                        Text(
                            text = "${"%.1f".format(item.heightCm.toDouble())} cm",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = RcColor6
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Ancho:",
                            fontSize = 13.sp,
                            color = RcColor8
                        )
                        Text(
                            text = "${"%.1f".format(item.widthCm.toDouble())} cm",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = RcColor6
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Largo:",
                            fontSize = 13.sp,
                            color = RcColor8
                        )
                        Text(
                            text = "${"%.1f".format(item.lengthCm.toDouble())} cm",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = RcColor6
                        )
                    }
                    Divider(color = RcColor8.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Volumen unitario:",
                            fontSize = 13.sp,
                            color = RcColor8
                        )
                        Text(
                            text = "${"%.2f".format(item.getVolume().toDouble())} cm³",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = RcColor6
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Volumen total:",
                            fontSize = 13.sp,
                            color = RcColor8
                        )
                        Text(
                            text = "${"%.2f".format(item.getTotalVolume().toDouble())} cm³",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = RcColor6
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sección: Peso y Cantidad (simplificada)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = RcColor7,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Peso y Cantidad",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = RcColor6,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Peso unitario:",
                            fontSize = 13.sp,
                            color = RcColor8
                        )
                        Text(
                            text = "${"%.1f".format(item.weightKg.toDouble())} kg",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = RcColor6
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Cantidad:",
                            fontSize = 13.sp,
                            color = RcColor8
                        )
                        Text(
                            text = "${item.quantity}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = RcColor6
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Peso total:",
                            fontSize = 13.sp,
                            color = RcColor8
                        )
                        Text(
                            text = "${"%.1f".format(item.totalWeightKg.toDouble())} kg",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = RcColor6
                        )
                    }
                }
            }

            // Notas
            if (item.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = RcColor7,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Notas",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = RcColor6
                        )
                        Text(
                            text = item.notes,
                            fontSize = 13.sp,
                            color = RcColor6,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Imágenes - Sección mejorada
            if (sortedImages.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Título de imágenes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Imágenes (${sortedImages.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = RcColor6
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Galería de imágenes mejorada
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(sortedImages.size) { index ->
                        ItemImageThumbnail(
                            imageUrl = sortedImages[index].imageUrl,
                            imageNumber = index + 1,
                            totalImages = sortedImages.size,
                            onClick = {
                                selectedImageIndex = index
                                showImageFullscreen = true
                            }
                        )
                    }
                }
            }
        }
    }

    // Visor de imágenes a pantalla completa
    if (showImageFullscreen && sortedImages.isNotEmpty()) {
        FullscreenImageViewer(
            images = sortedImages.map { it.imageUrl },
            initialIndex = selectedImageIndex,
            onDismiss = { showImageFullscreen = false }
        )
    }
}

@Composable
private fun ItemImageThumbnail(
    imageUrl: String,
    imageNumber: Int,
    totalImages: Int,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(120.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 4.dp,
        color = RcColor7
    ) {
        Box {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Imagen $imageNumber de $totalImages",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
            
            // Badge con número en la esquina superior derecha
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp),
                shape = RoundedCornerShape(10.dp),
                color = Color.Black.copy(alpha = 0.7f)
            ) {
                Text(
                    text = "$imageNumber",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Overlay sutil al hacer hover (efecto visual)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.1f)
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )
        }
    }
}

@Composable
private fun FullscreenImageViewer(
    images: List<String>,
    initialIndex: Int,
    onDismiss: () -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { images.size }
    )
    var showControls by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { showControls = !showControls }
                    )
                }
        ) {
            // Pager de imágenes
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { pageIndex ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = images[pageIndex],
                        contentDescription = "Imagen ${pageIndex + 1} de ${images.size}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // Controles superiores (solo si showControls)
            if (showControls) {
                // Header con contador y botón cerrar
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${pagerState.currentPage + 1} / ${images.size}",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                // Indicadores de página en la parte inferior
                if (images.size > 1) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter),
                        color = Color.Black.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Botón anterior
                            if (pagerState.currentPage > 0) {
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Anterior",
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.size(48.dp))
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Indicadores de página
                            repeat(images.size) { index ->
                                val isSelected = index == pagerState.currentPage
                                Surface(
                                    modifier = Modifier
                                        .size(if (isSelected) 10.dp else 6.dp)
                                        .clickable {
                                            scope.launch {
                                                pagerState.animateScrollToPage(index)
                                            }
                                        },
                                    shape = androidx.compose.foundation.shape.CircleShape,
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f)
                                ) {}
                                if (index < images.size - 1) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Botón siguiente
                            if (pagerState.currentPage < images.size - 1) {
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Siguiente",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .size(28.dp)
                                            .graphicsLayer { scaleX = -1f } // Invertir horizontalmente
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.size(48.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
