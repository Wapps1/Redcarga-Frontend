package com.wapps1.redcarga.features.requests.presentation.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
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
import com.wapps1.redcarga.R
import com.wapps1.redcarga.core.ui.theme.*
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Box
import compose.icons.fontawesomeicons.solid.CalendarAlt
import compose.icons.fontawesomeicons.solid.MapMarkedAlt
import compose.icons.fontawesomeicons.solid.WeightHanging

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientRequestsScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToCreateRequest: () -> Unit = {}
) {
    var showFilters by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("all") }
    
    // Obtener solicitudes hardcodeadas una sola vez
    val hardcodedRequests = getHardcodedRequests()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.client_requests_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = RcColor6
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                            tint = RcColor6
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showFilters = true }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = stringResource(R.string.common_filters),
                            tint = RcColor5
                        )
                    }
                    IconButton(onClick = { /* TODO: Refresh */ }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.common_refresh),
                            tint = RcColor4
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RcColor1,
                    titleContentColor = RcColor6
                )
            )
        },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header con estadísticas
            item {
                RequestStatsCard()
            }
            
            // Lista de solicitudes
            items(hardcodedRequests) { request ->
                RequestCard(
                    request = request,
                    onClick = { /* TODO: Navigate to detail */ }
                )
            }
            
            // Empty state si no hay solicitudes
            if (hardcodedRequests.isEmpty()) {
                item {
                    EmptyRequestsCard()
                }
            }
        }
    }
    
    // Dialog de filtros
    if (showFilters) {
        RequestFiltersDialog(
            selectedFilter = selectedFilter,
            onFilterSelected = { filter ->
                selectedFilter = filter
                showFilters = false
            },
            onDismiss = { showFilters = false }
        )
    }
}

/**
 * Card con estadísticas de solicitudes
 */
@Composable
private fun RequestStatsCard() {
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
                    value = "12",
                    color = RcColor4
                )
                StatItem(
                    title = stringResource(R.string.client_requests_active),
                    value = "3",
                    color = RcColor5
                )
                StatItem(
                    title = stringResource(R.string.client_requests_completed),
                    value = "9",
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
    request: HardcodedRequest,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        onClick = onClick,
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
                    text = request.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = RcColor6,
                    modifier = Modifier.weight(1f)
                )
                
                RequestStatusChip(status = request.status)
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
                    text = request.route,
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
                        text = request.weight,
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
                        text = request.items,
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
                        text = request.date,
                        fontSize = 13.sp,
                        color = RcColor8,
                        fontWeight = FontWeight.Medium
                    )
                }
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
 * Dialog de filtros
 */
@Composable
private fun RequestFiltersDialog(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.client_requests_filters_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = RcColor6
            )
        },
        text = {
            Column {
                val filters = listOf(
                    "all" to stringResource(R.string.client_requests_filter_all),
                    "open" to stringResource(R.string.client_requests_filter_open),
                    "in_progress" to stringResource(R.string.client_requests_filter_in_progress),
                    "completed" to stringResource(R.string.client_requests_filter_completed),
                    "cancelled" to stringResource(R.string.client_requests_filter_cancelled)
                )
                
                filters.forEach { (value, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedFilter == value,
                            onClick = { onFilterSelected(value) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = RcColor5,
                                unselectedColor = RcColor8
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            fontSize = 14.sp,
                            color = RcColor6
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = RcColor5
                )
            ) {
                Text(
                    text = stringResource(R.string.common_apply),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

/**
 * Data class para solicitudes hardcodeadas
 */
data class HardcodedRequest(
    val id: String,
    val name: String,
    val status: String,
    val route: String,
    val weight: String,
    val items: String,
    val date: String
)

/**
 * Función para obtener solicitudes hardcodeadas
 */
@Composable
private fun getHardcodedRequests(): List<HardcodedRequest> = listOf(
    HardcodedRequest(
        id = "1",
        name = stringResource(R.string.sample_request_1_name),
        status = "open",
        route = stringResource(R.string.sample_request_1_route),
        weight = stringResource(R.string.sample_request_1_weight),
        items = stringResource(R.string.sample_request_1_items),
        date = stringResource(R.string.sample_request_1_date)
    ),
    HardcodedRequest(
        id = "2",
        name = stringResource(R.string.sample_request_2_name),
        status = "in_progress",
        route = stringResource(R.string.sample_request_2_route),
        weight = stringResource(R.string.sample_request_2_weight),
        items = stringResource(R.string.sample_request_2_items),
        date = stringResource(R.string.sample_request_2_date)
    ),
    HardcodedRequest(
        id = "3",
        name = stringResource(R.string.sample_request_3_name),
        status = "completed",
        route = stringResource(R.string.sample_request_3_route),
        weight = stringResource(R.string.sample_request_3_weight),
        items = stringResource(R.string.sample_request_3_items),
        date = stringResource(R.string.sample_request_3_date)
    ),
    HardcodedRequest(
        id = "4",
        name = stringResource(R.string.sample_request_4_name),
        status = "open",
        route = stringResource(R.string.sample_request_4_route),
        weight = stringResource(R.string.sample_request_4_weight),
        items = stringResource(R.string.sample_request_4_items),
        date = stringResource(R.string.sample_request_4_date)
    ),
    HardcodedRequest(
        id = "5",
        name = stringResource(R.string.sample_request_5_name),
        status = "cancelled",
        route = stringResource(R.string.sample_request_5_route),
        weight = stringResource(R.string.sample_request_5_weight),
        items = stringResource(R.string.sample_request_5_items),
        date = stringResource(R.string.sample_request_5_date)
    )
)
