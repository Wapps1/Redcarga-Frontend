package com.wapps1.redcarga.features.requests.presentation.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header del item
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.itemName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = RcColor6,
                    modifier = Modifier.weight(1f)
                )
                if (item.fragile) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = RcColor5.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = compose.icons.FontAwesomeIcons.Solid.ExclamationTriangle,
                                contentDescription = null,
                                tint = RcColor5,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "FRÁGIL",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = RcColor5
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dimensiones y peso
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Dimensiones (cm)",
                        fontSize = 12.sp,
                        color = RcColor8,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${item.heightCm} × ${item.widthCm} × ${item.lengthCm}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = RcColor6
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Peso",
                        fontSize = 12.sp,
                        color = RcColor8,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${"%.1f".format(item.weightKg.toDouble())} kg",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = RcColor6
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Cantidad",
                        fontSize = 12.sp,
                        color = RcColor8,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${item.quantity} unidades",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = RcColor6
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Peso Total",
                        fontSize = 12.sp,
                        color = RcColor8,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${"%.1f".format(item.totalWeightKg.toDouble())} kg",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = RcColor6
                    )
                }
            }

            // Notas
            if (item.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = RcColor7
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Notas",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = RcColor8
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.notes,
                            fontSize = 13.sp,
                            color = RcColor6
                        )
                    }
                }
            }

            // Imágenes
            if (item.images.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Imágenes (${item.images.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = RcColor8
                )
                Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(item.images.sortedBy { it.imagePosition }) { image ->
                        ItemImageThumbnail(imageUrl = image.imageUrl)
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemImageThumbnail(imageUrl: String) {
    Surface(
        modifier = Modifier
            .size(100.dp),
        shape = RoundedCornerShape(12.dp),
        color = RcColor7
    ) {
        coil.compose.AsyncImage(
            model = imageUrl,
            contentDescription = "Imagen del artículo",
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
    }
}
