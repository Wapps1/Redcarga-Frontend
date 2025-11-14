package com.wapps1.redcarga.features.requests.presentation.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.wapps1.redcarga.core.ui.theme.*
import com.wapps1.redcarga.features.requests.domain.models.Request
import com.wapps1.redcarga.features.requests.domain.models.RequestItem
import com.wapps1.redcarga.features.requests.presentation.viewmodels.CreateQuoteViewModel
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Box
import compose.icons.fontawesomeicons.solid.MapMarkedAlt
import compose.icons.fontawesomeicons.solid.WeightHanging
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateQuoteScreen(
    requestId: Long,
    onNavigateBack: () -> Unit,
    viewModel: CreateQuoteViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val submitState by viewModel.submitState.collectAsState()
    val totalAmount by viewModel.totalAmount.collectAsState()

    // Cargar detalles al iniciar
    LaunchedEffect(requestId) {
        viewModel.loadRequestDetails(requestId)
    }

    // Manejar éxito de envío
    LaunchedEffect(submitState) {
        if (submitState is CreateQuoteViewModel.SubmitState.Success) {
            kotlinx.coroutines.delay(2500) // Mostrar diálogo de éxito
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Cotización", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RcColor5,
                    titleContentColor = White,
                    navigationIconContentColor = White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(RcColor1)
        ) {
            when (uiState) {
                is CreateQuoteViewModel.UiState.Loading -> {
                    LoadingContent()
                }
                is CreateQuoteViewModel.UiState.Error -> {
                    ErrorContent(
                        message = (uiState as CreateQuoteViewModel.UiState.Error).message,
                        onRetry = { viewModel.loadRequestDetails(requestId) }
                    )
                }
                is CreateQuoteViewModel.UiState.Success -> {
                    val request = (uiState as CreateQuoteViewModel.UiState.Success).request
                    QuoteFormContent(
                        request = request,
                        totalAmount = totalAmount,
                        onTotalAmountChange = { viewModel.updateTotalAmount(it) },
                        onSubmit = { viewModel.submitQuote() },
                        submitState = submitState
                    )
                }
                else -> {}
            }
        }

        // Diálogos de estado
        when (submitState) {
            is CreateQuoteViewModel.SubmitState.Loading -> {
                QuoteLoadingDialog()
            }
            is CreateQuoteViewModel.SubmitState.Success -> {
                QuoteSuccessDialog(
                    quoteId = (submitState as CreateQuoteViewModel.SubmitState.Success).quoteId,
                    onDismiss = { viewModel.resetSubmitState() }
                )
            }
            is CreateQuoteViewModel.SubmitState.Error -> {
                QuoteErrorDialog(
                    message = (submitState as CreateQuoteViewModel.SubmitState.Error).message,
                    onDismiss = { viewModel.resetSubmitState() }
                )
            }
            else -> {}
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = RcColor5)
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = RcColor5,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Error",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = RcColor6
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            fontSize = 14.sp,
            color = RcColor8,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = RcColor5)
        ) {
            Text("Reintentar")
        }
    }
}

@Composable
private fun QuoteFormContent(
    request: Request,
    totalAmount: BigDecimal,
    onTotalAmountChange: (BigDecimal) -> Unit,
    onSubmit: () -> Unit,
    submitState: CreateQuoteViewModel.SubmitState
) {
    var totalAmountText by remember { mutableStateOf(totalAmount.toString()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header con info de la solicitud
        item {
            RequestInfoCard(request = request)
        }

        // Resumen de items
        item {
            Text(
                text = "Items de la Solicitud",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = RcColor6,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        items(request.items.sortedBy { it.position }) { item ->
            RequestItemCard(item = item)
        }

        // Campo de precio total
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tu Cotización",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = RcColor6,
                modifier = Modifier.padding(vertical = 8.dp)
            )

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
                        text = "Precio Total",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = RcColor8
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = totalAmountText,
                        onValueChange = { newValue ->
                            totalAmountText = newValue
                            newValue.toBigDecimalOrNull()?.let { onTotalAmountChange(it) }
                        },
                        label = { Text("Ingresa el precio total") },
                        leadingIcon = {
                            Text(
                                text = "S/",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = RcColor5
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RcColor5,
                            focusedLabelColor = RcColor5,
                            cursorColor = RcColor5
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Incluye todos los costos: transporte, seguro, manejo, etc.",
                        fontSize = 12.sp,
                        color = RcColor8.copy(alpha = 0.7f),
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Botón enviar
        item {
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = submitState !is CreateQuoteViewModel.SubmitState.Loading && totalAmount > BigDecimal.ZERO,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RcColor5,
                    disabledContainerColor = RcColor8
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Enviar Cotización",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun RequestInfoCard(request: Request) {
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
                text = request.requestName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = RcColor6
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Origen -> Destino
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = RcColor5.copy(alpha = 0.1f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.MapMarkedAlt,
                            contentDescription = null,
                            tint = RcColor5,
                            modifier = Modifier.size(18.dp)
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

            Spacer(modifier = Modifier.height(12.dp))

            // Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatChip(
                    icon = FontAwesomeIcons.Solid.Box,
                    value = "${request.itemsCount}",
                    label = "Items",
                    color = RcColor3
                )
                StatChip(
                    icon = FontAwesomeIcons.Solid.WeightHanging,
                    value = "${"%.1f".format(request.totalWeightKg.toDouble())} kg",
                    label = "Peso Total",
                    color = RcColor4
                )
            }
        }
    }
}

@Composable
private fun StatChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = color.copy(alpha = 0.1f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = RcColor6
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = RcColor8
        )
    }
}

@Composable
private fun RequestItemCard(item: RequestItem) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = RcColor7
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar del item
            Surface(
                shape = CircleShape,
                color = RcColor5.copy(alpha = 0.15f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = FontAwesomeIcons.Solid.Box,
                        contentDescription = null,
                        tint = RcColor5,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.itemName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RcColor6
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${item.quantity} und × ${item.weightKg} kg = ${item.totalWeightKg} kg",
                    fontSize = 13.sp,
                    color = RcColor8
                )
            }
        }
    }
}

// ==================== DIÁLOGOS ====================

@Composable
private fun QuoteLoadingDialog() {
    Dialog(onDismissRequest = {}) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = RcColor5,
                    modifier = Modifier.size(56.dp),
                    strokeWidth = 5.dp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Enviando cotización...",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = RcColor6
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Por favor espera",
                    fontSize = 14.sp,
                    color = RcColor8
                )
            }
        }
    }
}

@Composable
private fun QuoteSuccessDialog(
    quoteId: Long,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Ícono de éxito con animación
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(RcColor3.copy(alpha = 0.2f), Color.Transparent)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = RcColor3,
                        modifier = Modifier.size(56.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "¡Cotización Enviada!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = RcColor6
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Tu cotización #$quoteId ha sido enviada exitosamente al cliente.",
                    fontSize = 14.sp,
                    color = RcColor8,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = RcColor5),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Entendido",
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuoteErrorDialog(
    message: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(RcColor5.copy(alpha = 0.2f), Color.Transparent)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = RcColor5,
                        modifier = Modifier.size(56.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Error al Enviar",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = RcColor6
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = RcColor8,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = RcColor5),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Entendido",
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

