package com.wapps1.redcarga.features.requests.presentation.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.wapps1.redcarga.core.ui.theme.*
import com.wapps1.redcarga.features.requests.domain.models.Request
import com.wapps1.redcarga.features.requests.domain.models.RequestItem
import com.wapps1.redcarga.features.requests.presentation.viewmodels.CreateQuoteViewModel
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Box
import compose.icons.fontawesomeicons.solid.ExclamationTriangle
import compose.icons.fontawesomeicons.solid.MapMarkedAlt
import compose.icons.fontawesomeicons.solid.WeightHanging
import kotlinx.coroutines.launch
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
            ItemDetailCard(item = item)
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
private fun ItemDetailCard(item: RequestItem) {
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
                        brush = Brush.horizontalGradient(
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
                                    imageVector = FontAwesomeIcons.Solid.ExclamationTriangle,
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

            // Sección: Dimensiones
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

            // Sección: Peso y Cantidad
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

            // Imágenes
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
                
                // Galería de imágenes
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
            
            // Overlay sutil
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
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
                                    shape = CircleShape,
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

