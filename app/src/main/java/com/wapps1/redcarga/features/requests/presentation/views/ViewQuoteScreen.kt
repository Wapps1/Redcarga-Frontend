package com.wapps1.redcarga.features.requests.presentation.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wapps1.redcarga.core.ui.theme.*
import com.wapps1.redcarga.features.requests.domain.models.QuoteDetail
import com.wapps1.redcarga.features.requests.presentation.viewmodels.ViewQuoteViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla para ver los detalles de una cotización existente
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewQuoteScreen(
    quoteId: Long,
    onNavigateBack: () -> Unit,
    viewModel: ViewQuoteViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Cargar detalles al entrar
    LaunchedEffect(quoteId) {
        viewModel.loadQuoteDetails(quoteId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            "Mi Cotización",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Cotización #$quoteId",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White.copy(alpha = 0.8f)
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
                            Icons.Default.ArrowBack,
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
                            Color(0xFF4CAF50),
                            Color(0xFF66BB6A),
                            Color(0xFF81C784)
                        )
                    )
                )
            )
        },
        containerColor = Color(0xFFF5F7FA)
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
                        CircularProgressIndicator(color = Color(0xFF4CAF50))
                    }
                }
                is ViewQuoteViewModel.UiState.Error -> {
                    ErrorContent(
                        message = (uiState as ViewQuoteViewModel.UiState.Error).message,
                        onRetry = { viewModel.loadQuoteDetails(quoteId) }
                    )
                }
                is ViewQuoteViewModel.UiState.Success -> {
                    val quote = (uiState as ViewQuoteViewModel.UiState.Success).quote
                    QuoteDetailContent(quote = quote)
                }
            }
        }
    }
}

@Composable
private fun QuoteDetailContent(quote: QuoteDetail) {
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

        // ⭐ NUEVO: Header premium con monto destacado
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
                                        Color(0xFF4CAF50).copy(alpha = 0.15f),
                                        Color(0xFF81C784).copy(alpha = 0.05f)
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
                                            Color(0xFF4CAF50),
                                            Color(0xFF66BB6A)
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
                            color = Color(0xFF4CAF50).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "✓ COTIZACIÓN ENVIADA",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF2E7D32),
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Monto principal
                        Text(
                            text = "${quote.totalAmount}",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF2E7D32),
                            letterSpacing = (-2).sp
                        )
                        
                        Text(
                            text = quote.currencyCode,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF66BB6A),
                            letterSpacing = 2.sp
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Monto Total Cotizado",
                            fontSize = 14.sp,
                            color = Color(0xFF6C757D),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // ⭐ NUEVO: Información General rediseñada
        item {
            PremiumInfoSection(
                title = "📋 Información General",
                icon = "📄"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PremiumInfoRow(
                        label = "ID Cotización",
                        value = "#${quote.quoteId}",
                        icon = "🔖",
                        color = Color(0xFF9C27B0)
                    )
                    PremiumInfoRow(
                        label = "ID Solicitud",
                        value = "#${quote.requestId}",
                        icon = "📦",
                        color = Color(0xFF2196F3)
                    )
                    PremiumInfoRow(
                        label = "Estado",
                        value = quote.stateCode,
                        icon = "⚡",
                        color = Color(0xFF4CAF50)
                    )
                    PremiumInfoRow(
                        label = "Moneda",
                        value = quote.currencyCode,
                        icon = "💱",
                        color = Color(0xFFFF9800)
                    )
                    PremiumInfoRow(
                        label = "Versión",
                        value = "v${quote.version}",
                        icon = "🔄",
                        color = Color(0xFF607D8B)
                    )
                }
            }
        }

        // ⭐ NUEVO: Fechas importantes
        item {
            PremiumInfoSection(
                title = "📅 Historial de Actividad",
                icon = "⏰"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PremiumInfoRow(
                        label = "Creada",
                        value = dateFormatter.format(Date.from(quote.createdAt)),
                        icon = "✨",
                        color = Color(0xFF00BCD4)
                    )
                    PremiumInfoRow(
                        label = "Última Actualización",
                        value = dateFormatter.format(Date.from(quote.updatedAt)),
                        icon = "🔄",
                        color = Color(0xFF009688)
                    )
                }
            }
        }

        // ⭐ NUEVO: Items Cotizados con header elegante
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
                                        Color(0xFF673AB7),
                                        Color(0xFF9C27B0)
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
                            text = "Artículos Cotizados",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2C3E50)
                        )
                        Text(
                            text = "${quote.items.size} items en total",
                            fontSize = 14.sp,
                            color = Color(0xFF6C757D)
                        )
                    }
                }
                
                // Badge con contador
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF9C27B0).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${quote.items.size}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF9C27B0),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        items(quote.items) { item ->
            PremiumQuoteItemCard(item = item)
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

/**
 * ⭐ NUEVO: Sección premium con diseño elegante
 */
@Composable
private fun PremiumInfoSection(
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
                            Color(0xFF4CAF50).copy(alpha = 0.1f),
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

/**
 * ⭐ NUEVO: Fila de información premium con icono y color
 */
@Composable
private fun PremiumInfoRow(
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

/**
 * ⭐ NUEVO: Card premium para items cotizados
 */
@Composable
private fun PremiumQuoteItemCard(
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
            // Icono del item
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFF8A65),
                                Color(0xFFFF7043)
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
            
            // Cantidad destacada
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF4CAF50).copy(alpha = 0.15f)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${item.qty}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF4CAF50)
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
private fun InfoSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
private fun ErrorContent(
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
                text = "Error al cargar",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = RcColor6,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                color = RcColor8,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text("Reintentar")
            }
        }
    }
}

