package com.wapps1.redcarga.features.fleet.presentation.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wapps1.redcarga.R
import com.wapps1.redcarga.core.ui.theme.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.wapps1.redcarga.features.fleet.domain.models.routes.RouteType
import com.wapps1.redcarga.features.fleet.presentation.viewmodels.RoutesManagementViewModel
import kotlinx.coroutines.flow.collectLatest

 

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutesManagement(
    onNavigateBack: () -> Unit = {}
) {
    val vm: RoutesManagementViewModel = hiltViewModel()
    val ui by vm.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var showCreateDialog by remember { mutableStateOf(false) }
    var editRouteId by remember { mutableStateOf<Long?>(null) }
    var showOptionsMenu by remember { mutableStateOf(false) }
    var showFiltersDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.bootstrap() }
    LaunchedEffect(Unit) { vm.effects.collectLatest { eff -> if (eff is RoutesManagementViewModel.Effect.Message) snackbar.showSnackbar(eff.text) } }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbar) },
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.routes_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = RcColor6
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                            tint = RcColor6
                        )
                    }
                },
                actions = {
                    // Badge de filtros activos
                    BadgedBox(
                        badge = {
                            if (ui.hasActiveFilters) {
                                Badge(containerColor = RcColor5) {
                                    Text(
                                        text = "${ui.activeFiltersCount}",
                                        color = White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    ) {
                        IconButton(onClick = { showFiltersDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = stringResource(R.string.routes_filters_title),
                                tint = if (ui.hasActiveFilters) RcColor5 else RcColor6
                            )
                        }
                    }
                    
                    IconButton(onClick = { vm.onRefresh() }, enabled = !ui.isRefreshing) {
                        Icon(
                            imageVector = Icons.Default.Refresh, 
                            contentDescription = stringResource(R.string.common_refresh),
                            tint = if (ui.isRefreshing) RcColor8 else RcColor5
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White
                )
            )
        },
        containerColor = RcColor1
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(12.dp)
        ) {
            // Header con título y botón
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.routes_your),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = RcColor6
                )
                
                Button(
                    onClick = { showCreateDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RcColor5
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.height(38.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.routes_add_button),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            // Indicador de refresco
            if (ui.isRefreshing) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = RcColor5,
                    trackColor = RcColor7
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (ui.isInitializing) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = RcColor5)
                }
            } else {
                if (ui.empty) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "📦",
                                fontSize = 40.sp
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.routes_empty),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = RcColor8
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.routes_empty_subtitle),
                                fontSize = 12.sp,
                                color = RcColor8.copy(alpha = 0.7f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(ui.routes, key = { it.id }) { item ->
                            RouteItemCard(
                                type = item.type,
                                origin = buildString {
                                    append(item.originDeptName)
                                    item.originProvName?.let { append(", "); append(it) }
                                },
                                destination = buildString {
                                    append(item.destDeptName)
                                    item.destProvName?.let { append(", "); append(it) }
                                },
                                active = item.active,
                                onEdit = { editRouteId = item.id },
                                onDelete = {vm.onDelete(item.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        RouteUpsertDialog(
            title = stringResource(R.string.routes_create_title),
            initial = UpsertFormUi(),
            onDismiss = { showCreateDialog = false },
            onConfirm = { f ->
                vm.onCreate(
                    type = f.type,
                    originDeptCode = f.originDeptCode,
                    originProvCode = f.originProvCode,
                    destDeptCode = f.destDeptCode,
                    destProvCode = f.destProvCode,
                    active = f.active
                )
                showCreateDialog = false
            }
        )
    }

    editRouteId?.let { rid ->
        RouteEditDialog(routeId = rid, vm = vm, onDismiss = { editRouteId = null })
    }

    if (showFiltersDialog) {
        FiltersDialog(
            vm = vm,
            currentFilters = ui.filters,
            departments = ui.departments,
            provincesByDept = ui.provincesByDept,
            onDismiss = { showFiltersDialog = false }
        )
    }
}

@Composable
private fun RouteItemCard(
    type: RouteType,
    origin: String,
    destination: String,
    active: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header: Tipo de ruta y menú
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Chip de tipo de ruta con colores vibrantes
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (type == RouteType.PP) RcColor4 else RcColor3,
                    shadowElevation = 1.dp
                ) {
                    Text(
                        text = if (type == RouteType.PP) stringResource(R.string.routes_type_pp) else stringResource(R.string.routes_type_dd),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                }

                // Menú de opciones
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.routes_menu_options),
                            tint = RcColor6
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.routes_menu_edit), color = RcColor6, fontWeight = FontWeight.Medium) },
                            onClick = {
                                showMenu = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.routes_menu_delete), color = RcColor5, fontWeight = FontWeight.Bold) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Origen con ícono
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = RcColor7,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = RcColor5.copy(alpha = 0.2f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = "📍", fontSize = 16.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.routes_origin),
                            fontSize = 10.sp,
                            color = RcColor8,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = origin,
                            fontSize = 12.sp,
                            color = RcColor6,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Destino con ícono
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = RcColor7,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = RcColor4.copy(alpha = 0.2f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = "🎯", fontSize = 16.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.routes_destination),
                            fontSize = 10.sp,
                            color = RcColor8,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = destination,
                            fontSize = 12.sp,
                            color = RcColor6,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Estado con diseño mejorado
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (active) Color(0xFF4CAF50) else RcColor8.copy(alpha = 0.3f),
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    if (active) Color.White else RcColor8,
                                    shape = RoundedCornerShape(3.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (active) stringResource(R.string.routes_active) else stringResource(R.string.routes_inactive),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (active) White else RcColor8
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RouteLocationRowHardcoded(
    label: String,
    location: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = location,
            fontSize = 14.sp,
            color = Color.Black,
            fontWeight = FontWeight.Normal
        )
    }
}

// ===== Dialogos de creación/edición simples =====

private data class UpsertFormUi(
    val type: RouteType = RouteType.PP,
    val originDeptCode: String = "",
    val originProvCode: String = "",
    val destDeptCode: String = "",
    val destProvCode: String = "",
    val active: Boolean = true
)

@Composable
private fun RouteUpsertDialog(
    title: String,
    initial: UpsertFormUi,
    onDismiss: () -> Unit,
    onConfirm: (UpsertFormUi) -> Unit
) {
    val vm: RoutesManagementViewModel = hiltViewModel()
    val ui by vm.state.collectAsState()
    var form by remember { mutableStateOf(initial) }
    val isDD = form.type == RouteType.DD

    val originProvList = remember(form.originDeptCode, ui.provincesByDept) {
        ui.provincesByDept[form.originDeptCode].orEmpty()
    }
    val destProvList = remember(form.destDeptCode, ui.provincesByDept) {
        ui.provincesByDept[form.destDeptCode].orEmpty()
    }

    val canSave = remember(form, isDD, ui.geoReady) {
        when {
            !ui.geoReady -> false
            isDD -> form.originDeptCode.isNotBlank() && form.destDeptCode.isNotBlank()
            else -> form.originDeptCode.isNotBlank() && form.destDeptCode.isNotBlank() &&
                    form.originProvCode.isNotBlank() && form.destProvCode.isNotBlank()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Column {
                Text(
                    title, 
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = RcColor6
                )
                Spacer(Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .width(35.dp)
                        .height(3.dp)
                        .background(RcColor5, RoundedCornerShape(1.5.dp))
                )
            }
        },
        text = {
            if (!ui.geoReady) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = RcColor5, strokeWidth = 2.dp, modifier = Modifier.size(30.dp))
                        Spacer(Modifier.height(10.dp))
                        Text(
                            stringResource(R.string.routes_loading_catalog),
                            color = RcColor8,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Tipo con chips elegantes
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            stringResource(R.string.routes_type_label),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = RcColor6,
                            letterSpacing = 0.3.sp
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            FilterChip(
                                selected = form.type == RouteType.DD,
                                onClick = { form = form.copy(type = RouteType.DD, originProvCode = "", destProvCode = "") },
                                label = { 
                                    Text(
                                        stringResource(R.string.routes_type_dd_short),
                                        fontSize = 11.sp,
                                        fontWeight = if (form.type == RouteType.DD) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RcColor3,
                                    selectedLabelColor = White,
                                    containerColor = RcColor7,
                                    labelColor = RcColor6
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = if (form.type == RouteType.DD) RcColor3 else RcColor8.copy(alpha = 0.3f),
                                    borderWidth = 1.dp,
                                    enabled = true,
                                    selected = form.type == RouteType.DD
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = form.type == RouteType.PP,
                                onClick = { form = form.copy(type = RouteType.PP) },
                                label = { 
                                    Text(
                                        stringResource(R.string.routes_type_pp_short),
                                        fontSize = 11.sp,
                                        fontWeight = if (form.type == RouteType.PP) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RcColor4,
                                    selectedLabelColor = White,
                                    containerColor = RcColor7,
                                    labelColor = RcColor6
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = if (form.type == RouteType.PP) RcColor4 else RcColor8.copy(alpha = 0.3f),
                                    borderWidth = 1.dp,
                                    enabled = true,
                                    selected = form.type == RouteType.PP
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Sección Origen con borde
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = RcColor7.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RcColor5.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📍", fontSize = 14.sp)
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    stringResource(R.string.routes_origin_label),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RcColor5
                                )
                            }
                            
                            ElegantSelectField(
                                label = stringResource(R.string.routes_department_label),
                                options = ui.departments.map { it.name to it.code },
                                selectedCode = form.originDeptCode,
                                onSelected = { code -> form = form.copy(originDeptCode = code, originProvCode = "") }
                            )
                            
                            if (!isDD) {
                                ElegantSelectField(
                                    label = stringResource(R.string.routes_province_label),
                                    options = originProvList.map { it.name to it.code },
                                    enabled = form.originDeptCode.isNotBlank(),
                                    selectedCode = form.originProvCode,
                                    onSelected = { code -> form = form.copy(originProvCode = code) },
                                    helperText = if (form.originDeptCode.isBlank()) stringResource(R.string.routes_select_department_first) else null
                                )
                            }
                        }
                    }

                    // Sección Destino con borde
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = RcColor7.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RcColor4.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🎯", fontSize = 14.sp)
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    stringResource(R.string.routes_destination_label),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RcColor4
                                )
                            }
                            
                            ElegantSelectField(
                                label = stringResource(R.string.routes_department_label),
                                options = ui.departments.map { it.name to it.code },
                                selectedCode = form.destDeptCode,
                                onSelected = { code -> form = form.copy(destDeptCode = code, destProvCode = "") }
                            )
                            
                            if (!isDD) {
                                ElegantSelectField(
                                    label = stringResource(R.string.routes_province_label),
                                    options = destProvList.map { it.name to it.code },
                                    enabled = form.destDeptCode.isNotBlank(),
                                    selectedCode = form.destProvCode,
                                    onSelected = { code -> form = form.copy(destProvCode = code) },
                                    helperText = if (form.destDeptCode.isBlank()) stringResource(R.string.routes_select_department_first) else null
                                )
                            }
                        }
                    }

                    // Estado con switch elegante
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (form.active) RcColor5.copy(alpha = 0.1f) else RcColor8.copy(alpha = 0.1f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (form.active) RcColor5 else RcColor8.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(
                                            if (form.active) RcColor5 else RcColor8,
                                            shape = RoundedCornerShape(3.dp)
                                        )
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    stringResource(R.string.routes_active_label),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RcColor6
                                )
                            }
                            Switch(
                                checked = form.active,
                                onCheckedChange = { form = form.copy(active = it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = White,
                                    checkedTrackColor = RcColor5,
                                    uncheckedThumbColor = White,
                                    uncheckedTrackColor = RcColor8
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = canSave && !ui.isSubmitting,
                onClick = { onConfirm(form) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = RcColor5,
                    disabledContainerColor = RcColor8.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                if (ui.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 1.5.dp,
                        color = White
                    )
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    stringResource(R.string.routes_save_button),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    stringResource(R.string.common_cancel),
                    color = RcColor8,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = White
    )
}

@Composable
private fun RouteEditDialog(routeId: Long, vm: RoutesManagementViewModel, onDismiss: () -> Unit) {
    var loading by remember { mutableStateOf(true) }
    var initial by remember { mutableStateOf(UpsertFormUi()) }

    LaunchedEffect(routeId) {
        val cid = vm.state.value.companyId
        if (cid == null) { onDismiss(); return@LaunchedEffect }
        runCatching {
            vm.getRouteForEdit(routeId)
        }.onSuccess { r ->
            if (r != null) {
                initial = UpsertFormUi(
                    type = r.routeType,
                    originDeptCode = r.originDeptCode,
                    originProvCode = r.originProvCode.orEmpty(),
                    destDeptCode = r.destinationDeptCode,
                    destProvCode = r.destinationProvCode.orEmpty(),
                    active = r.active
                )
            }
            loading = false
        }.onFailure {
            loading = false
        }
    }

    if (loading) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    stringResource(R.string.routes_edit_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = RcColor6
                )
            },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = RcColor5)
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.common_close), color = RcColor8, fontWeight = FontWeight.Medium)

                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    } else {
        RouteUpsertDialog(title = stringResource(R.string.routes_edit_title), initial = initial, onDismiss = onDismiss) { f ->
            vm.onUpdate(
                routeId = routeId,
                type = f.type,
                originDeptCode = f.originDeptCode,
                originProvCode = f.originProvCode,
                destDeptCode = f.destDeptCode,
                destProvCode = f.destProvCode,
                active = f.active
            )
            onDismiss()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ElegantSelectField(
    label: String,
    options: List<Pair<String, String>>, // (name to code)
    selectedCode: String,
    onSelected: (String) -> Unit,
    enabled: Boolean = true,
    helperText: String? = null
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = options.firstOrNull { it.second == selectedCode }?.first.orEmpty()

    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            label = { Text(label, fontWeight = FontWeight.Medium, fontSize = 11.sp) },
            readOnly = true,
            enabled = enabled,
            trailingIcon = {
                IconButton(onClick = { if (enabled) expanded = !expanded }) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = if (enabled) RcColor5 else RcColor8.copy(alpha = 0.3f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RcColor5,
                focusedLabelColor = RcColor5,
                unfocusedBorderColor = RcColor8.copy(alpha = 0.3f),
                disabledBorderColor = RcColor8.copy(alpha = 0.2f),
                disabledLabelColor = RcColor8.copy(alpha = 0.5f),
                disabledTextColor = RcColor8.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(8.dp),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
        )
        
        if (helperText != null && !enabled) {
            Text(
                text = helperText,
                fontSize = 10.sp,
                color = RcColor8.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = 3.dp),
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            options.forEach { (name, code) ->
                DropdownMenuItem(
                    text = { 
                        Text(
                            name,
                            color = RcColor6,
                            fontWeight = if (code == selectedCode) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    },
                    onClick = {
                        expanded = false
                        onSelected(code)
                    },
                    colors = MenuDefaults.itemColors(
                        textColor = RcColor6,
                        leadingIconColor = RcColor5
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

// Diálogo de filtros completo
@Composable
private fun FiltersDialog(
    vm: RoutesManagementViewModel,
    currentFilters: RoutesManagementViewModel.RouteFilters,
    departments: List<RoutesManagementViewModel.DeptOption>,
    provincesByDept: Map<String, List<RoutesManagementViewModel.ProvOption>>,
    onDismiss: () -> Unit
) {
    var filters by remember { mutableStateOf(currentFilters) }
    
    val originProvList = remember(filters.originDeptCode, provincesByDept) {
        filters.originDeptCode?.let { provincesByDept[it] }.orEmpty()
    }
    val destProvList = remember(filters.destDeptCode, provincesByDept) {
        filters.destDeptCode?.let { provincesByDept[it] }.orEmpty()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    stringResource(R.string.routes_filters_title),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = RcColor6
                )
                Spacer(Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(3.dp)
                        .background(RcColor4, RoundedCornerShape(1.5.dp))
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Filtro por tipo
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        stringResource(R.string.routes_filters_type),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = RcColor6,
                        letterSpacing = 0.3.sp
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilterChip(
                            selected = filters.type == null,
                            onClick = { filters = filters.copy(type = null) },
                            label = { Text(stringResource(R.string.routes_filters_all), fontSize = 11.sp, fontWeight = if (filters.type == null) FontWeight.Bold else FontWeight.Medium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RcColor6,
                                selectedLabelColor = White,
                                containerColor = RcColor7,
                                labelColor = RcColor6
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = filters.type == RouteType.DD,
                            onClick = { filters = filters.copy(type = RouteType.DD) },
                            label = { Text("DD", fontSize = 11.sp, fontWeight = if (filters.type == RouteType.DD) FontWeight.Bold else FontWeight.Medium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RcColor3,
                                selectedLabelColor = White,
                                containerColor = RcColor7,
                                labelColor = RcColor6
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = filters.type == RouteType.PP,
                            onClick = { filters = filters.copy(type = RouteType.PP) },
                            label = { Text("PP", fontSize = 11.sp, fontWeight = if (filters.type == RouteType.PP) FontWeight.Bold else FontWeight.Medium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RcColor4,
                                selectedLabelColor = White,
                                containerColor = RcColor7,
                                labelColor = RcColor6
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Filtro por estado
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        stringResource(R.string.routes_filters_status),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = RcColor6,
                        letterSpacing = 0.3.sp
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilterChip(
                            selected = filters.activeOnly == null,
                            onClick = { filters = filters.copy(activeOnly = null) },
                            label = { Text(stringResource(R.string.routes_filters_all), fontSize = 11.sp, fontWeight = if (filters.activeOnly == null) FontWeight.Bold else FontWeight.Medium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RcColor6,
                                selectedLabelColor = White,
                                containerColor = RcColor7,
                                labelColor = RcColor6
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = filters.activeOnly == true,
                            onClick = { filters = filters.copy(activeOnly = true) },
                            label = { Text(stringResource(R.string.routes_filters_active), fontSize = 11.sp, fontWeight = if (filters.activeOnly == true) FontWeight.Bold else FontWeight.Medium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF4CAF50),
                                selectedLabelColor = White,
                                containerColor = RcColor7,
                                labelColor = RcColor6
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = filters.activeOnly == false,
                            onClick = { filters = filters.copy(activeOnly = false) },
                            label = { Text(stringResource(R.string.routes_filters_inactive), fontSize = 11.sp, fontWeight = if (filters.activeOnly == false) FontWeight.Bold else FontWeight.Medium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RcColor8,
                                selectedLabelColor = White,
                                containerColor = RcColor7,
                                labelColor = RcColor6
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Filtros por ubicación - Origen
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = RcColor7.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RcColor5.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📍", fontSize = 14.sp)
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    stringResource(R.string.routes_filters_origin),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RcColor5
                                )
                            }
                            if (filters.originDeptCode != null || filters.originProvCode != null) {
                                TextButton(
                                    onClick = { filters = filters.copy(originDeptCode = null, originProvCode = null) },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text(stringResource(R.string.routes_filters_clear_section), fontSize = 10.sp, color = RcColor5, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        
                        ElegantSelectField(
                            label = stringResource(R.string.routes_department_label),
                            options = listOf("" to "") + departments.map { it.name to it.code },
                            selectedCode = filters.originDeptCode ?: "",
                            onSelected = { code -> 
                                filters = if (code.isBlank()) {
                                    filters.copy(originDeptCode = null, originProvCode = null)
                                } else {
                                    filters.copy(originDeptCode = code, originProvCode = null)
                                }
                            }
                        )
                        
                        if (filters.originDeptCode != null) {
                            ElegantSelectField(
                                label = stringResource(R.string.routes_province_optional),
                                options = listOf("" to "") + originProvList.map { it.name to it.code },
                                selectedCode = filters.originProvCode ?: "",
                                onSelected = { code -> 
                                    filters = filters.copy(originProvCode = if (code.isBlank()) null else code)
                                }
                            )
                        }
                    }
                }

                // Filtros por ubicación - Destino
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = RcColor7.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RcColor4.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🎯", fontSize = 14.sp)
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    stringResource(R.string.routes_filters_destination),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RcColor4
                                )
                            }
                            if (filters.destDeptCode != null || filters.destProvCode != null) {
                                TextButton(
                                    onClick = { filters = filters.copy(destDeptCode = null, destProvCode = null) },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text(stringResource(R.string.routes_filters_clear_section), fontSize = 10.sp, color = RcColor4, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        
                        ElegantSelectField(
                            label = stringResource(R.string.routes_department_label),
                            options = listOf("" to "") + departments.map { it.name to it.code },
                            selectedCode = filters.destDeptCode ?: "",
                            onSelected = { code -> 
                                filters = if (code.isBlank()) {
                                    filters.copy(destDeptCode = null, destProvCode = null)
                                } else {
                                    filters.copy(destDeptCode = code, destProvCode = null)
                                }
                            }
                        )
                        
                        if (filters.destDeptCode != null) {
                            ElegantSelectField(
                                label = stringResource(R.string.routes_province_optional),
                                options = listOf("" to "") + destProvList.map { it.name to it.code },
                                selectedCode = filters.destProvCode ?: "",
                                onSelected = { code -> 
                                    filters = filters.copy(destProvCode = if (code.isBlank()) null else code)
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    vm.onFiltersChanged(filters)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = RcColor5),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    stringResource(R.string.routes_filters_apply),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextButton(
                    onClick = {
                        filters = RoutesManagementViewModel.RouteFilters()
                        vm.onFiltersChanged(filters)
                        onDismiss()
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        stringResource(R.string.routes_filters_clear),
                        color = RcColor8,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                TextButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        stringResource(R.string.common_cancel),
                        color = RcColor6,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = White
    )
}
