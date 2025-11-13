package com.wapps1.redcarga.features.requests.presentation.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
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
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.wapps1.redcarga.core.ui.theme.RcColor3
import com.wapps1.redcarga.core.ui.theme.RcColor5
import com.wapps1.redcarga.features.requests.presentation.viewmodels.CreateRequestViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RequestSummaryScreen(
    onBack: () -> Unit = {},
    onSubmit: () -> Unit = {},
    viewModel: CreateRequestViewModel = hiltViewModel()
) {
    // Estados del ViewModel observados reactivamente
    val requestName by viewModel.requestName.collectAsState()
    val selectedOriginDept by viewModel.selectedOriginDepartment.collectAsState()
    val selectedOriginProv by viewModel.selectedOriginProvince.collectAsState()
    val originDistrict by viewModel.originDistrict.collectAsState()
    val selectedDestDept by viewModel.selectedDestinationDepartment.collectAsState()
    val selectedDestProv by viewModel.selectedDestinationProvince.collectAsState()
    val destinationDistrict by viewModel.destinationDistrict.collectAsState()
    val paymentOnDelivery by viewModel.paymentOnDelivery.collectAsState()
    val items by viewModel.items.collectAsState()
    val submitState by viewModel.submitState.collectAsState()

    val currentDate = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    }

    // Calcular totales reactivamente
    val totalItems = remember(items) { viewModel.getTotalItems() }
    val totalWeight = remember(items) { viewModel.getTotalWeight() }

    // Manejar estados de envío
    LaunchedEffect(submitState) {
        when (submitState) {
            is CreateRequestViewModel.SubmitState.Success -> {
                // Esperar un poco para que el usuario vea el diálogo de éxito
                kotlinx.coroutines.delay(2000)
                onSubmit()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFFF3ED), Color(0xFFFDF7F5))
                    )
                )
        ) {
            // Spacer para el header
            item {
                Spacer(modifier = Modifier.height(72.dp))
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Título Resumen
            item {
                Text(
                    text = "Resumen",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Card de información general
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SummaryRow(
                            label = "Nombre:",
                            value = requestName.ifBlank { "-" }
                        )
                        SummaryRow(label = "Día:", value = currentDate)

                        val originText = buildString {
                            selectedOriginDept?.let { dept ->
                                selectedOriginProv?.let { prov ->
                                    append("${prov.name}, ${dept.name}")
                                    if (originDistrict.isNotBlank()) {
                                        append(" - $originDistrict")
                                    }
                                }
                            }
                        }.ifBlank { "-" }
                        SummaryRow(label = "Origen:", value = originText)

                        val destinationText = buildString {
                            selectedDestDept?.let { dept ->
                                selectedDestProv?.let { prov ->
                                    append("${prov.name}, ${dept.name}")
                                    if (destinationDistrict.isNotBlank()) {
                                        append(" - $destinationDistrict")
                                    }
                                }
                            }
                        }.ifBlank { "-" }
                        SummaryRow(label = "Destino:", value = destinationText)

                        SummaryRow(
                            label = "Pago contra entrega:",
                            value = if (paymentOnDelivery) "Sí" else "No"
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Card de totales
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8F5))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Total de Artículos:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Peso Total:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "$totalItems",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = RcColor5
                            )
                            Text(
                                text = "${"%.1f".format(totalWeight)}kg",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = RcColor5
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Lista de artículos
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items.forEach { item ->
                        SummaryItemCard(item = item)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Botones de acción
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text("Atrás")
                    }

                    GradientPrimaryButton(
                        text = "Enviar Solicitud",
                        onClick = { viewModel.submitRequest() },
                        modifier = Modifier.weight(1f),
                        enabled = submitState !is CreateRequestViewModel.SubmitState.Loading
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Header
        SummaryHeader(onBack = onBack)

        // Diálogos de estado
        when (submitState) {
            is CreateRequestViewModel.SubmitState.Loading -> {
                LoadingDialog()
            }
            is CreateRequestViewModel.SubmitState.Success -> {
                SuccessDialog(
                    requestId = (submitState as CreateRequestViewModel.SubmitState.Success).requestId,
                    onDismiss = {
                        viewModel.resetSubmitState()
                        onSubmit()
                    }
                )
            }
            is CreateRequestViewModel.SubmitState.Error -> {
                ErrorDialog(
                    message = (submitState as CreateRequestViewModel.SubmitState.Error).message,
                    onDismiss = { viewModel.resetSubmitState() },
                    onRetry = { viewModel.submitRequest() }
                )
            }
            else -> {}
        }
    }
}

@Composable
private fun SummaryHeader(onBack: () -> Unit) {
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
            .padding(top = 16.dp, bottom = 16.dp, start = 20.dp, end = 20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Realizar solicitud",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = RcColor3
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SummaryItemCard(item: CreateRequestViewModel.ItemFormData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header con nombre, badges y cantidad
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.itemName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = RcColor5
                    )
                    if (item.fragile) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFF8A65)
                        ) {
                            Text(
                                text = "Frágil",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                val quantity = item.quantity.toIntOrNull() ?: 1
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFE0B2)
                ) {
                    Text(
                        text = "x$quantity",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = RcColor5,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Dimensiones
            val dimensions = buildString {
                val parts = mutableListOf<String>()
                if (item.widthCm.isNotBlank()) parts.add("Ancho: ${item.widthCm}cm")
                if (item.heightCm.isNotBlank()) parts.add("Alto: ${item.heightCm}cm")
                if (item.lengthCm.isNotBlank()) parts.add("Largo: ${item.lengthCm}cm")
                if (parts.isNotEmpty()) {
                    append(parts.joinToString(" • "))
                }
            }

            if (dimensions.isNotBlank()) {
                Text(
                    text = dimensions,
                    style = MaterialTheme.typography.bodySmall,
                    color = RcColor3
                )
            }

            // Peso y total
            val weight = item.weightKg.toDoubleOrNull() ?: 0.0
            val quantity = item.quantity.toIntOrNull() ?: 1
            val totalWeight = weight * quantity

            Text(
                text = "Peso: ${item.weightKg}kg • Cantidad: $quantity • Total: ${"%.1f".format(totalWeight)}kg",
                style = MaterialTheme.typography.bodySmall,
                color = RcColor3,
                fontWeight = FontWeight.Medium
            )

            // Mostrar número de imágenes si hay
            if (item.imageUris.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "📷 ${item.imageUris.size} imagen${if (item.imageUris.size > 1) "es" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = RcColor5,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun GradientPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val gradient = Brush.horizontalGradient(
        colors = if (enabled) {
            listOf(Color(0xFFFF8A65), Color(0xFFFF7043))
        } else {
            listOf(Color(0xFFBDBDBD), Color(0xFF9E9E9E))
        }
    )
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        tonalElevation = 0.dp,
        color = Color.Transparent,
        modifier = modifier,
        enabled = enabled
    ) {
        Box(
            modifier = Modifier
                .background(gradient, MaterialTheme.shapes.large)
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color.White,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun LoadingDialog() {
    Dialog(onDismissRequest = {}) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(64.dp),
                    color = RcColor5,
                    strokeWidth = 6.dp
                )

                Text(
                    text = "Creando solicitud...",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = RcColor5
                )

                Text(
                    text = "Por favor espera un momento",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RcColor3,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun SuccessDialog(
    requestId: Long,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Ícono de éxito animado
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF4CAF50).copy(alpha = 0.2f),
                                    Color(0xFF4CAF50).copy(alpha = 0.05f)
                                )
                            ),
                            shape = androidx.compose.foundation.shape.CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(56.dp)
                    )
                }

                Text(
                    text = "¡Solicitud Creada!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF1F8E9)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "ID de Solicitud",
                            style = MaterialTheme.typography.bodySmall,
                            color = RcColor3
                        )
                        Text(
                            text = "#$requestId",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = RcColor5
                        )
                    }
                }

                Text(
                    text = "Tu solicitud ha sido registrada exitosamente. Pronto recibirás cotizaciones de nuestros proveedores.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RcColor3,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Ver Cotizaciones",
                        modifier = Modifier.padding(vertical = 8.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorDialog(
    message: String,
    onDismiss: () -> Unit,
    onRetry: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Ícono de error
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFF44336).copy(alpha = 0.2f),
                                    Color(0xFFF44336).copy(alpha = 0.05f)
                                )
                            ),
                            shape = androidx.compose.foundation.shape.CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.ErrorOutline,
                        contentDescription = null,
                        tint = Color(0xFFF44336),
                        modifier = Modifier.size(56.dp)
                    )
                }

                Text(
                    text = "Error al crear solicitud",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC62828)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    )
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFC62828),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Cancelar",
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    Button(
                        onClick = {
                            onDismiss()
                            onRetry()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RcColor5
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Reintentar",
                            modifier = Modifier.padding(vertical = 4.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

