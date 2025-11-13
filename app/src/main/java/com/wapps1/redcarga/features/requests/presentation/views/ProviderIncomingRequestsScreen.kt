package com.wapps1.redcarga.features.requests.presentation.views

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderIncomingRequestsScreen(
    onQuote: (Long) -> Unit = {},
    viewModel: com.wapps1.redcarga.features.requests.presentation.viewmodels.ProviderIncomingRequestsViewModel = hiltViewModel()
) {
    // Observar datos del ViewModel
    val requests by viewModel.incomingRequests.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val detailState by viewModel.detailState.collectAsState()
    
    val tabs = listOf("Todas", "Abiertas", "En Proceso")
    var selectedTabIndex by remember { mutableStateOf(0) }
    var search by remember { mutableStateOf("") }
    
    // Filtrar requests basado en tab y búsqueda
    val filteredRequests = remember(requests, selectedTabIndex, search) {
        val filtered = when (selectedTabIndex) {
            0 -> requests // Todas
            1 -> requests.filter { it.isOpen() } // Abiertas
            2 -> requests.filter { !it.isOpen() } // En Proceso
            else -> requests
        }
        filtered.filter { 
            it.requesterName.contains(search, true) || 
            it.getRouteDescription().contains(search, true)
        }
    }
    
    val isLoading = uiState is com.wapps1.redcarga.features.requests.presentation.viewmodels.ProviderIncomingRequestsViewModel.UiState.Loading
    val hasError = uiState is com.wapps1.redcarga.features.requests.presentation.viewmodels.ProviderIncomingRequestsViewModel.UiState.Error

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Solicitudes", style = MaterialTheme.typography.titleLarge) },
                scrollBehavior = rememberTopAppBarState().let { null }
            )
        }
    ) { paddingValues ->
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
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, label ->
                    Tab(selected = selectedTabIndex == index, onClick = { selectedTabIndex = index }, text = { Text(label) })
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
                                ProviderRequestCard(
                                    request = req,
                                    onQuote = { onQuote(req.requestId) },
                                    onViewDetails = { viewModel.loadRequestDetails(req.requestId) },
                                    onDelete = { viewModel.deleteRequest(req.requestId) }
                                )
                            }
                        }
                    }
                }
            }
        }
        
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
    onDelete: () -> Unit
) {
    val dateFormatter = remember {
        java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
    }
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        // Cinta superior
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(
                    Brush.horizontalGradient(listOf(Color(0xFFFF8A65), Color(0xFFFF7043))),
                    RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
                )
        )

        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(32.dp).background(Color(0xFFFFEDE6), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = request.requesterName.take(1), fontWeight = FontWeight.Bold, color = Color(0xFFFF6F4E))
                }
                Spacer(modifier = Modifier.size(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Solicitud de ${request.requesterName}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(text = request.getRouteDescription(), style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                }
                TagChip(text = request.status.name)
            }

            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Items: ${request.totalQuantity}", style = MaterialTheme.typography.bodySmall)
                    Text("Ruta: #${request.matchedRouteId}", style = MaterialTheme.typography.bodySmall)
                }
                Text("Fecha: ${dateFormatter.format(java.util.Date.from(request.createdAt))}", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(
                    onClick = onViewDetails,
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Color(0xFFDDDDDD), RoundedCornerShape(10.dp))
                ) { Text("Ver más", style = MaterialTheme.typography.labelMedium) }
                GradientActionButton(text = "Cotizar", onClick = onQuote, modifier = Modifier.weight(1f), textStyle = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

// ---------- Utilidades de UI (copiadas/ajustadas del estilo de ClientDealsScreen) ----------

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


