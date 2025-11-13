package com.wapps1.redcarga.features.requests.presentation.views

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditLocationAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.wapps1.redcarga.R
import com.wapps1.redcarga.core.ui.theme.RcColor3
import com.wapps1.redcarga.core.ui.theme.RcColor4
import com.wapps1.redcarga.core.ui.theme.RcColor5
import com.wapps1.redcarga.features.fleet.domain.models.geo.Department
import com.wapps1.redcarga.features.fleet.domain.models.geo.Province
import com.wapps1.redcarga.features.requests.presentation.viewmodels.CreateRequestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRequestScreen(
    onBack: () -> Unit = {},
    onNext: () -> Unit = {},
    viewModel: CreateRequestViewModel = hiltViewModel()
) {
    // Estados del ViewModel
    val geoCatalog by viewModel.geoCatalog.collectAsState()
    val requestName by viewModel.requestName.collectAsState()
    val selectedOriginDept by viewModel.selectedOriginDepartment.collectAsState()
    val selectedOriginProv by viewModel.selectedOriginProvince.collectAsState()
    val originDistrict by viewModel.originDistrict.collectAsState()
    val selectedDestDept by viewModel.selectedDestinationDepartment.collectAsState()
    val selectedDestProv by viewModel.selectedDestinationProvince.collectAsState()
    val destinationDistrict by viewModel.destinationDistrict.collectAsState()
    val paymentOnDelivery by viewModel.paymentOnDelivery.collectAsState()
    val items by viewModel.items.collectAsState()

    // Estados para controlar expansión de dropdowns
    var expandedOriginDept by remember { mutableStateOf(false) }
    var expandedOriginProv by remember { mutableStateOf(false) }
    var expandedDestDept by remember { mutableStateOf(false) }
    var expandedDestProv by remember { mutableStateOf(false) }

    // Estado para mostrar el dialog de agregar item
    var showAddItemDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<CreateRequestViewModel.ItemFormData?>(null) }

    // Provincias filtradas
    val originProvinces = remember(selectedOriginDept, geoCatalog) {
        viewModel.getProvincesForDepartment(selectedOriginDept?.code)
    }
    val destinationProvinces = remember(selectedDestDept, geoCatalog) {
        viewModel.getProvincesForDepartment(selectedDestDept?.code)
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
            // Spacer para el header flotante
            item {
                Spacer(modifier = Modifier.height(72.dp))
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Nombre de la solicitud
            item {
                SectionCard(
                    icon = Icons.Filled.EditLocationAlt,
                    title = "Nombre de la solicitud *",
                    accent = RcColor5,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    RTextField(
                        value = requestName,
                        onValueChange = { viewModel.updateRequestName(it) },
                        label = "Ingrese un nombre *",
                        singleLine = true,
                        maxLines = 1,
                        isError = requestName.isBlank()
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Sección Origen
            item {
                SectionCard(
                    icon = Icons.Filled.EditLocationAlt,
                    title = stringResource(R.string.create_request_section_origin) + " *",
                    accent = RcColor5,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Departamento
                        CompactSelector(
                            label = "Departamento *",
                            value = selectedOriginDept?.name ?: "Seleccione",
                            options = geoCatalog?.departments ?: emptyList(),
                            accent = RcColor5,
                            expanded = expandedOriginDept,
                            onExpandedChange = { expandedOriginDept = it },
                            onSelected = { dept ->
                                viewModel.selectOriginDepartment(dept)
                                expandedOriginDept = false
                            },
                            getName = { it.name }
                        )

                        // Provincia (solo si hay departamento seleccionado)
                        if (selectedOriginDept != null) {
                            CompactSelector(
                                label = "Provincia *",
                                value = selectedOriginProv?.name ?: "Seleccione",
                                options = originProvinces,
                                accent = RcColor5,
                                expanded = expandedOriginProv,
                                onExpandedChange = { expandedOriginProv = it },
                                onSelected = { prov ->
                                    viewModel.selectOriginProvince(prov)
                                    expandedOriginProv = false
                                },
                                getName = { it.name }
                            )
                        }

                        // Distrito (solo si hay provincia seleccionada)
                        if (selectedOriginProv != null) {
                            RTextField(
                                value = originDistrict,
                                onValueChange = { viewModel.updateOriginDistrict(it) },
                                label = "Distrito (opcional)",
                                singleLine = true,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(14.dp))
            }

            item {
                SectionCard(
                    icon = Icons.Filled.EditLocationAlt,
                    title = stringResource(R.string.create_request_section_destination) + " *",
                    accent = RcColor4,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        CompactSelector(
                            label = "Departamento *",
                            value = selectedDestDept?.name ?: "Seleccione",
                            options = geoCatalog?.departments ?: emptyList(),
                            accent = RcColor4,
                            expanded = expandedDestDept,
                            onExpandedChange = { expandedDestDept = it },
                            onSelected = { dept ->
                                viewModel.selectDestinationDepartment(dept)
                                expandedDestDept = false
                            },
                            getName = { it.name }
                        )
                        if (selectedDestDept != null) {
                            CompactSelector(
                                label = "Provincia *",
                                value = selectedDestProv?.name ?: "Seleccione",
                                options = destinationProvinces,
                                accent = RcColor4,
                                expanded = expandedDestProv,
                                onExpandedChange = { expandedDestProv = it },
                                onSelected = { prov ->
                                    viewModel.selectDestinationProvince(prov)
                                    expandedDestProv = false
                                },
                                getName = { it.name }
                            )
                        }
                        if (selectedDestProv != null) {
                            RTextField(
                                value = destinationDistrict,
                                onValueChange = { viewModel.updateDestinationDistrict(it) },
                                label = "Distrito (opcional)",
                                singleLine = true,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Sección Payment on Delivery
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.togglePaymentOnDelivery() }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Pago contra entrega",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Switch(
                            checked = paymentOnDelivery,
                            onCheckedChange = { viewModel.setPaymentOnDelivery(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = RcColor5
                            )
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Sección de Artículos
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Inventory2,
                            contentDescription = null,
                            tint = RcColor5
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Artículos *",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    TextButton(
                        onClick = {
                            editingItem = null
                            showAddItemDialog = true
                        }
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Agregar")
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Lista de artículos
            if (items.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items.forEach { item ->
                            ItemCard(
                                item = item,
                                onEdit = {
                                    editingItem = item
                                    showAddItemDialog = true
                                },
                                onDelete = { viewModel.removeItem(item.id) }
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Resumen de artículos
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
                                .padding(12.dp),
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
                                    text = "${viewModel.getTotalItems()}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = RcColor5
                                )
                                Text(
                                    text = "${"%.1f".format(viewModel.getTotalWeight())}kg",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = RcColor5
                                )
                            }
                        }
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
                        Text("Cancelar")
                    }

                    GradientPrimaryButton(
                        text = "Siguiente",
                        onClick = {
                            if (viewModel.isFormValid()) {
                                onNext()
                            } else {
                                // TODO: Mostrar errores de validación
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Header flotante personalizado
        RequestsCustomHeader(onBack = onBack)
    }

    // Dialog para agregar/editar item
    if (showAddItemDialog) {
        AddItemDialog(
            item = editingItem,
            onDismiss = {
                showAddItemDialog = false
                editingItem = null
            },
            onSave = { item ->
                if (editingItem != null) {
                    viewModel.updateItem(item)
                } else {
                    viewModel.addItem(item)
                }
                showAddItemDialog = false
                editingItem = null
            }
        )
    }
}

/* ----------------- Componentes UI reutilizables ----------------- */

@Composable
private fun RequestsCustomHeader(onBack: () -> Unit) {
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
private fun RTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    modifier: Modifier = Modifier.fillMaxWidth(),
    leadingIcon: (@Composable (() -> Unit))? = null,
    isError: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue -> onValueChange(newValue) },
        modifier = modifier,
        label = { Text(label) },
        singleLine = singleLine,
        maxLines = maxLines,
        leadingIcon = leadingIcon,
        isError = isError
    )
}

@Composable
private fun SectionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    accent: Color,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = accent)
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun ItemCard(
    item: CreateRequestViewModel.ItemFormData,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = item.itemName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = RcColor5,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (item.fragile) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFF8A65)
                            ) {
                                Text(
                                    text = "Frágil",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))

                    val dimensions = buildString {
                        if (item.heightCm.isNotBlank()) append("Alto: ${item.heightCm}cm ")
                        if (item.widthCm.isNotBlank()) append("Ancho: ${item.widthCm}cm ")
                        if (item.lengthCm.isNotBlank()) append("Largo: ${item.lengthCm}cm")
                    }
                    if (dimensions.isNotBlank()) {
                        Text(
                            text = dimensions.trim(),
                            style = MaterialTheme.typography.bodySmall,
                            color = RcColor3
                        )
                    }

                    val weight = item.weightKg.toDoubleOrNull() ?: 0.0
                    val quantity = item.quantity.toIntOrNull() ?: 1
                    val totalWeight = weight * quantity

                    Text(
                        text = "Peso: ${item.weightKg}kg · Cantidad: $quantity · Total: ${"%.1f".format(totalWeight)}kg",
                        style = MaterialTheme.typography.bodySmall,
                        color = RcColor3
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "Editar",
                            tint = RcColor5,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Eliminar",
                            tint = Color(0xFFE57373),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Imágenes thumbnail
            if (item.imageUris.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item.imageUris.take(4).forEach { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF5F5F5)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    if (item.imageUris.size > 4) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFFE0B2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+${item.imageUris.size - 4}",
                                color = RcColor5,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GradientPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFF8A65), Color(0xFFFF7043))
    )
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        tonalElevation = 0.dp,
        color = Color.Transparent,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .background(gradient, MaterialTheme.shapes.large)
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = text, color = Color.White, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
    }
}

// Selector genérico con función para obtener el nombre
@Composable
private fun <T> CompactSelector(
    label: String,
    value: String,
    options: List<T>,
    accent: Color,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelected: (T) -> Unit,
    getName: (T) -> String
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = RcColor5
        )
        Surface(
            tonalElevation = 0.dp,
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFFFFF8F5),
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .border(1.dp, accent.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
            onClick = { onExpandedChange(true) }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = accent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = value, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF1B1B1B))
                }
                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = accent)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
            options.forEach { item ->
                DropdownMenuItem(
                    text = { Text(getName(item)) },
                    onClick = {
                        onSelected(item)
                    }
                )
            }
        }
    }
}

// Dialog para agregar/editar artículo
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddItemDialog(
    item: CreateRequestViewModel.ItemFormData?,
    onDismiss: () -> Unit,
    onSave: (CreateRequestViewModel.ItemFormData) -> Unit
) {
    var itemName by remember { mutableStateOf(item?.itemName ?: "") }
    var heightCm by remember { mutableStateOf(item?.heightCm ?: "") }
    var widthCm by remember { mutableStateOf(item?.widthCm ?: "") }
    var lengthCm by remember { mutableStateOf(item?.lengthCm ?: "") }
    var weightKg by remember { mutableStateOf(item?.weightKg ?: "") }
    var quantity by remember { mutableStateOf(item?.quantity ?: "1") }
    var fragile by remember { mutableStateOf(item?.fragile ?: false) }
    var notes by remember { mutableStateOf(item?.notes ?: "") }
    var imageUris by remember { mutableStateOf(item?.imageUris ?: emptyList()) }

    // Launcher para la cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // La imagen ya está guardada en el URI temporal
        }
    }

    // Launcher para seleccionar imágenes de la galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            imageUris = imageUris + it.toString()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (item != null) "Editar Artículo" else "Agregar Artículo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Cerrar",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                item { Spacer(Modifier.height(12.dp)) }

                // Nombre del producto
                item {
                    OutlinedTextField(
                        value = itemName,
                        onValueChange = { itemName = it },
                        label = { Text("Nombre *", fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                }

                item { Spacer(Modifier.height(10.dp)) }

                // Botones de Cámara y Galería
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                galleryLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = RcColor5
                            )
                        ) {
                            Icon(
                                Icons.Filled.Image,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Galería", fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                // TODO: Implementar cámara con URI temporal
                                galleryLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = RcColor5
                            )
                        ) {
                            Icon(
                                Icons.Filled.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Cámara", fontSize = 13.sp)
                        }
                    }
                }

                // Mostrar imágenes seleccionadas
                if (imageUris.isNotEmpty()) {
                    item { Spacer(Modifier.height(8.dp)) }
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(imageUris.size) { index ->
                                Box {
                                    AsyncImage(
                                        model = imageUris[index],
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFF5F5F5)),
                                        contentScale = ContentScale.Crop
                                    )
                                    IconButton(
                                        onClick = {
                                            imageUris = imageUris.filterIndexed { i, _ -> i != index }
                                        },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(20.dp)
                                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                    ) {
                                        Icon(
                                            Icons.Filled.Close,
                                            contentDescription = "Eliminar",
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(10.dp)) }

                // Dimensiones
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = widthCm,
                            onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) widthCm = it },
                            label = { Text("Ancho", fontSize = 12.sp) },
                            suffix = { Text("cm", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = heightCm,
                            onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) heightCm = it },
                            label = { Text("Alto", fontSize = 12.sp) },
                            suffix = { Text("cm", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = lengthCm,
                            onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) lengthCm = it },
                            label = { Text("Largo", fontSize = 12.sp) },
                            suffix = { Text("cm", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }
                }

                item { Spacer(Modifier.height(10.dp)) }

                // Peso y Cantidad
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = weightKg,
                            onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) weightKg = it },
                            label = { Text("Peso *", fontSize = 13.sp) },
                            suffix = { Text("kg", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = quantity,
                            onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d+$"))) quantity = it },
                            label = { Text("Cantidad *", fontSize = 13.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }

                item { Spacer(Modifier.height(8.dp)) }

                // Frágil
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { fragile = !fragile }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Artículo frágil",
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 14.sp
                        )
                        Checkbox(
                            checked = fragile,
                            onCheckedChange = { fragile = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = RcColor5
                            ),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                item { Spacer(Modifier.height(12.dp)) }

                // Botón Listo
                item {
                    GradientPrimaryButton(
                        text = "Listo",
                        onClick = {
                            if (itemName.isNotBlank() && weightKg.isNotBlank() && quantity.isNotBlank()) {
                                val newItem = CreateRequestViewModel.ItemFormData(
                                    id = item?.id ?: java.util.UUID.randomUUID().toString(),
                                    itemName = itemName,
                                    heightCm = heightCm,
                                    widthCm = widthCm,
                                    lengthCm = lengthCm,
                                    weightKg = weightKg,
                                    quantity = quantity,
                                    fragile = fragile,
                                    notes = notes,
                                    imageUris = imageUris
                                )
                                onSave(newItem)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
