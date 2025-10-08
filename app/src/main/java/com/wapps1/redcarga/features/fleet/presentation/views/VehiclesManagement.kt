package com.wapps1.redcarga.features.fleet.presentation.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wapps1.redcarga.core.ui.theme.*
import com.wapps1.redcarga.features.fleet.presentation.viewmodels.VehiclesManagementViewModel

 

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehiclesManagement(
    onNavigateBack: () -> Unit = {}
) {
    val vm: VehiclesManagementViewModel = hiltViewModel()
    val ui by vm.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var showCreate by remember { mutableStateOf(false) }
    var editId by remember { mutableStateOf<Long?>(null) }
    var showFilters by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.bootstrap() }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Vehículos", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = RcColor6) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = RcColor6)
                    }
                },
                actions = {
                    // Botón de filtros con badge
                    val hasActiveFilters = ui.hasActiveFilters
                    BadgedBox(badge = { if (ui.activeFiltersCount > 0) Badge { Text("${ui.activeFiltersCount}") } }) {
                        IconButton(onClick = { showFilters = true }) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                tint = if (hasActiveFilters) RcColor5 else RcColor6
                            )
                        }
                    }
                    IconButton(onClick = { vm.onRefresh() }, enabled = !ui.isRefreshing) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = if (ui.isRefreshing) RcColor8 else RcColor5)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        },
        containerColor = RcColor1
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Tus Vehículos", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = RcColor6)
                Button(
                    onClick = { showCreate = true },
                    colors = ButtonDefaults.buttonColors(containerColor = RcColor5),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.height(38.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Agregar", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = White)
                }
            }

            Spacer(Modifier.height(12.dp))

            if (ui.isRefreshing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = RcColor5, trackColor = RcColor7)
                Spacer(Modifier.height(8.dp))
            }

            if (ui.isInitializing) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = RcColor5)
                }
            } else if (ui.empty) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Sin vehículos", color = RcColor8)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(ui.items, key = { it.id }) { item ->
                        VehicleItemCard(
                            name = item.name,
                            plate = item.plate,
                            active = item.active,
                            onEdit = { editId = item.id },
                            onDelete = { vm.onDelete(item.id) }
                        )
                    }
                }
            }
        }
    }

    if (showCreate) {
        VehicleUpsertDialog(
            title = "Crear Vehículo",
            initial = VehicleFormUi(),
            submitting = ui.isSubmitting,
            onDismiss = { showCreate = false },
            onConfirm = { f -> vm.onCreate(f.name, f.plate, f.active); showCreate = false }
        )
    }

    editId?.let { id ->
        VehicleUpsertDialog(
            title = "Editar Vehículo",
            initial = ui.items.firstOrNull { it.id == id }?.let { VehicleFormUi(it.name, it.plate, it.active) } ?: VehicleFormUi(),
            submitting = ui.isSubmitting,
            onDismiss = { editId = null },
            onConfirm = { f -> vm.onUpdate(id, f.name, f.plate, f.active); editId = null }
        )
    }

    if (showFilters) {
        VehiclesFiltersDialog(
            current = ui.filters,
            onApply = { f -> vm.onFiltersChanged(f); showFilters = false },
            onClear = { vm.onFiltersChanged(VehiclesManagementViewModel.Filters()); showFilters = false },
            onDismiss = { showFilters = false }
        )
    }
}

@Composable
private fun VehicleItemCard(
    name: String,
    plate: String,
    active: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var menu by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = RcColor6)
                    Spacer(Modifier.height(3.dp))
                    Text(plate, fontSize = 11.sp, color = RcColor8, fontWeight = FontWeight.Medium)
                }
                Box {
                    IconButton(onClick = { menu = true }) { Icon(Icons.Default.MoreVert, contentDescription = null, tint = RcColor6) }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(text = { Text("Editar", color = RcColor6) }, onClick = { menu = false; onEdit() })
                        DropdownMenuItem(text = { Text("Eliminar", color = RcColor5, fontWeight = FontWeight.Bold) }, onClick = { menu = false; onDelete() })
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Surface(shape = RoundedCornerShape(10.dp), color = if (active) Color(0xFF4CAF50) else RcColor8.copy(alpha = 0.3f)) {
                    Text(
                        text = if (active) "Activo" else "Inactivo",
                        color = if (active) White else RcColor8,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}

private data class VehicleFormUi(
    val name: String = "",
    val plate: String = "",
    val active: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VehicleUpsertDialog(
    title: String,
    initial: VehicleFormUi,
    submitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (VehicleFormUi) -> Unit
) {
    var form by remember { mutableStateOf(initial) }
    val canSave = remember(form) { form.name.isNotBlank() && form.plate.isNotBlank() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = RcColor6)
                Spacer(Modifier.height(2.dp))
                Box(modifier = Modifier.width(35.dp).height(3.dp), contentAlignment = Alignment.Center) {}
            }
        },
        text = {
            Column(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = form.name,
                    onValueChange = { form = form.copy(name = it) },
                    label = { Text("Nombre", fontWeight = FontWeight.Medium, fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RcColor5,
                        focusedLabelColor = RcColor5,
                        unfocusedBorderColor = RcColor8.copy(alpha = 0.3f)
                    )
                )
                OutlinedTextField(
                    value = form.plate,
                    onValueChange = { form = form.copy(plate = it) },
                    label = { Text("Placa", fontWeight = FontWeight.Medium, fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RcColor5,
                        focusedLabelColor = RcColor5,
                        unfocusedBorderColor = RcColor8.copy(alpha = 0.3f)
                    )
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Activo", fontWeight = FontWeight.Bold, color = RcColor6, fontSize = 13.sp)
                    Switch(
                        checked = form.active,
                        onCheckedChange = { form = form.copy(active = it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = White, checkedTrackColor = RcColor5, uncheckedThumbColor = White, uncheckedTrackColor = RcColor8)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(form) },
                enabled = canSave && !submitting,
                colors = ButtonDefaults.buttonColors(containerColor = RcColor5),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                if (submitting) {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), color = White, strokeWidth = 1.5.dp)
                    Spacer(Modifier.width(6.dp))
                }
                Text("Guardar", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp), modifier = Modifier.height(36.dp)) {
                Text("Cancelar", color = RcColor8, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = White
    )
}

@Composable
private fun VehiclesFiltersDialog(
    current: VehiclesManagementViewModel.Filters,
    onApply: (VehiclesManagementViewModel.Filters) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    var f by remember { mutableStateOf(current) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Filtros de Vehículos", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = RcColor6)
                Spacer(Modifier.height(2.dp))
                Box(modifier = Modifier.width(40.dp).height(3.dp)) {}
            }
        },
        text = {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Estado", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RcColor6)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = f.activeOnly == null,
                        onClick = { f = f.copy(activeOnly = null) },
                        label = { Text("Todos", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = f.activeOnly == true,
                        onClick = { f = f.copy(activeOnly = true) },
                        label = { Text("Activos", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = f.activeOnly == false,
                        onClick = { f = f.copy(activeOnly = false) },
                        label = { Text("Inactivos", fontSize = 11.sp) }
                    )
                }

                OutlinedTextField(
                    value = f.query.orEmpty(),
                    onValueChange = { f = f.copy(query = it) },
                    label = { Text("Buscar por nombre o placa", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                )
            }
        },
        confirmButton = {
            Button(onClick = { onApply(f) }, shape = RoundedCornerShape(8.dp), modifier = Modifier.height(36.dp)) {
                Text("Aplicar", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextButton(onClick = onClear, shape = RoundedCornerShape(8.dp), modifier = Modifier.height(36.dp)) { Text("Limpiar", fontSize = 12.sp) }
                TextButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp), modifier = Modifier.height(36.dp)) { Text("Cancelar", fontSize = 12.sp) }
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = White
    )
}


