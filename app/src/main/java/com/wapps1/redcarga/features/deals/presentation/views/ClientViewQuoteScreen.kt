package com.wapps1.redcarga.features.deals.presentation.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wapps1.redcarga.features.deals.presentation.viewmodels.ClientDealsViewModel
import com.wapps1.redcarga.features.requests.domain.models.QuoteDetail
import com.wapps1.redcarga.features.requests.presentation.viewmodels.ViewQuoteViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pantalla para que el CLIENTE vea todos los detalles de una cotización.
 * Basada en ViewQuoteScreen pero adaptada al estilo naranja del cliente.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientViewQuoteScreen(
    quoteId: Long,
    onNavigateBack: () -> Unit,
    onOpenChat: () -> Unit = {},
    viewModel: ViewQuoteViewModel = hiltViewModel(),
    dealsViewModel: ClientDealsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val processingAction by dealsViewModel.processingAction.collectAsState()
    val actionMessage by dealsViewModel.actionMessage.collectAsState()

    // Snackbar para mensajes
    val snackbarHostState = remember { SnackbarHostState() }

    // Cargar detalles al entrar
    LaunchedEffect(quoteId) {
        viewModel.loadQuoteDetails(quoteId)
    }

    // Mostrar mensajes de acción
    LaunchedEffect(actionMessage) {
        actionMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = when (message) {
                    is ClientDealsViewModel.ActionMessage.Success -> message.message
                    is ClientDealsViewModel.ActionMessage.Error -> message.message
                },
                duration = androidx.compose.material3.SnackbarDuration.Long
            )
            dealsViewModel.clearActionMessage()
        }
    }

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
        },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Detalle de cotización",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Cotización #$quoteId",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(40.dp)
                            .background(
                                Color.White.copy(alpha = 0.2f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFFF8A65),
                            Color(0xFFFF7043)
                        )
                    )
                )
            )
        },
        containerColor = Color(0xFFFFF3ED)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is ViewQuoteViewModel.UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFFFF6F4E))
                    }
                }

                is ViewQuoteViewModel.UiState.Error -> {
                    ClientViewQuoteErrorContent(
                        message = (uiState as ViewQuoteViewModel.UiState.Error).message,
                        onRetry = { viewModel.loadQuoteDetails(quoteId) }
                    )
                }

                is ViewQuoteViewModel.UiState.Success -> {
                    val quote = (uiState as ViewQuoteViewModel.UiState.Success).quote
                    ClientQuoteDetailContent(
                        quote = quote,
                        isProcessing = processingAction[quote.quoteId] ?: false,
                        onStartNegotiation = { dealsViewModel.startNegotiation(quote.quoteId, quote.requestId) },
                        onReject = { dealsViewModel.rejectQuote(quote.quoteId, quote.requestId) },
                        onChat = onOpenChat
                    )
                }
            }
        }
    }
}

@Composable
private fun ClientQuoteDetailContent(
    quote: QuoteDetail,
    isProcessing: Boolean,
    onStartNegotiation: () -> Unit,
    onReject: () -> Unit,
    onChat: () -> Unit
) {
    val dateFormatter = remember {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Header con monto destacado
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Fondo con gradiente decorativo
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFFF8A65).copy(alpha = 0.15f),
                                        Color(0xFFFFCCBC).copy(alpha = 0.05f)
                                    )
                                ),
                                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Icono grande con fondo
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFFFF8A65),
                                            Color(0xFFFF7043)
                                        )
                                    ),
                                    shape = CircleShape
                                )
                                .border(4.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "💰",
                                fontSize = 36.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Badge de estado
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFFFF8A65).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "COTIZACIÓN RECIBIDA",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFE64A19),
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Monto principal
                        Text(
                            text = "${quote.totalAmount}",
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFE64A19),
                            letterSpacing = (-1).sp
                        )

                        Text(
                            text = quote.currencyCode,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF8A65),
                            letterSpacing = 2.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Monto total propuesto por el proveedor",
                            fontSize = 14.sp,
                            color = Color(0xFF6C757D),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Información general
        item {
            ClientPremiumInfoSection(
                title = "📋 Información de la cotización",
                icon = "📄"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ClientPremiumInfoRow(
                        label = "ID Cotización",
                        value = "#${quote.quoteId}",
                        icon = "🔖",
                        color = Color(0xFF9C27B0)
                    )
                    ClientPremiumInfoRow(
                        label = "ID Solicitud",
                        value = "#${quote.requestId}",
                        icon = "📦",
                        color = Color(0xFF2196F3)
                    )
                    ClientPremiumInfoRow(
                        label = "Estado",
                        value = quote.stateCode,
                        icon = "⚡",
                        color = Color(0xFFFF9800)
                    )
                    ClientPremiumInfoRow(
                        label = "Moneda",
                        value = quote.currencyCode,
                        icon = "💱",
                        color = Color(0xFFFF7043)
                    )
                    ClientPremiumInfoRow(
                        label = "Versión",
                        value = "v${quote.version}",
                        icon = "🔄",
                        color = Color(0xFF607D8B)
                    )
                }
            }
        }

        // Fechas importantes
        item {
            ClientPremiumInfoSection(
                title = "📅 Historial de actividad",
                icon = "⏰"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ClientPremiumInfoRow(
                        label = "Creada",
                        value = dateFormatter.format(Date.from(quote.createdAt)),
                        icon = "✨",
                        color = Color(0xFF00BCD4)
                    )
                    ClientPremiumInfoRow(
                        label = "Última actualización",
                        value = dateFormatter.format(Date.from(quote.updatedAt)),
                        icon = "🔄",
                        color = Color(0xFF009688)
                    )
                }
            }
        }

        // Items cotizados
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFFF8A65),
                                        Color(0xFFFF7043)
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📦",
                            fontSize = 24.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Ítems cotizados",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2C3E50)
                        )
                        Text(
                            text = "${quote.items.size} ítems en total",
                            fontSize = 14.sp,
                            color = Color(0xFF6C757D)
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFF7043).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${quote.items.size}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFF7043),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        items(quote.items) { item ->
            ClientPremiumQuoteItemCard(item = item)
        }

        // ⭐ Botones de acción según el estado de la cotización
        item {
            Spacer(modifier = Modifier.height(12.dp))
            ClientQuoteActionButtons(
                stateCode = quote.stateCode,
                isProcessing = isProcessing,
                onStartNegotiation = onStartNegotiation,
                onReject = onReject,
                onChat = onChat
            )
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun ClientPremiumInfoSection(
    title: String,
    icon: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Color(0xFFFF8A65).copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = icon, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C3E50)
                )
            }
            content()
        }
    }
}

@Composable
private fun ClientPremiumInfoRow(
    label: String,
    value: String,
    icon: String,
    color: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.08f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(color.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = icon, fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF6C757D)
                )
            }
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun ClientPremiumQuoteItemCard(
    item: com.wapps1.redcarga.features.requests.domain.models.QuoteItem
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFB74D),
                                Color(0xFFFF8A65)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📦",
                    fontSize = 28.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Item #${item.quoteItemId}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C3E50)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Request Item: #${item.requestItemId}",
                    fontSize = 13.sp,
                    color = Color(0xFF6C757D)
                )
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFF8A65).copy(alpha = 0.15f)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${item.qty}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFF7043)
                    )
                    Text(
                        text = "unidades",
                        fontSize = 11.sp,
                        color = Color(0xFF6C757D)
                    )
                }
            }
        }
    }
}

@Composable
private fun ClientQuoteActionButtons(
    stateCode: String,
    isProcessing: Boolean,
    onStartNegotiation: () -> Unit,
    onReject: () -> Unit,
    onChat: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Acciones disponibles",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50)
            )

            when (stateCode.uppercase()) {
                "PENDING" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextButton(
                            onClick = onReject,
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
                        ClientGradientActionButton(
                            text = if (isProcessing) "Procesando..." else "Iniciar Trato",
                            onClick = onStartNegotiation,
                            modifier = Modifier.weight(1f),
                            enabled = !isProcessing
                        )
                    }
                }
                "TRATO" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextButton(
                            onClick = onReject,
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
                        ClientGradientActionButton(
                            text = "Chat",
                            onClick = onChat,
                            modifier = Modifier.weight(1f),
                            enabled = !isProcessing
                        )
                    }
                }
                "RECHAZADA" -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFEBEE), RoundedCornerShape(12.dp))
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "HA SIDO RECHAZADO",
                            color = Color(0xFFE53935),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                else -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextButton(
                            onClick = onReject,
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
                        ClientGradientActionButton(
                            text = if (isProcessing) "Procesando..." else "Iniciar Trato",
                            onClick = onStartNegotiation,
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
private fun ClientGradientActionButton(
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
private fun ClientViewQuoteErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "Error al cargar la cotización",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF6F4E),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                color = Color(0xFF8D6E63),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF6F4E)
                )
            ) {
                Text("Reintentar")
            }
        }
    }
}


