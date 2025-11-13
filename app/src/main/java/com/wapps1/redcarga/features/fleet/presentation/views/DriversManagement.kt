package com.wapps1.redcarga.features.fleet.presentation.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wapps1.redcarga.core.ui.theme.*
import com.wapps1.redcarga.R
import androidx.hilt.navigation.compose.hiltViewModel
import com.wapps1.redcarga.features.fleet.presentation.viewmodels.DriversManagementViewModel



// Modelo hardcodeado para un conductor
data class DriverUi(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val licenseNumber: String,
    val active: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriversManagement(
    onNavigateBack: () -> Unit = {}
) {
    val vm: DriversManagementViewModel = hiltViewModel()
    val ui by vm.state.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var editDriverId by remember { mutableStateOf<Long?>(null) }
    var showFilters by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { vm.bootstrap() }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbar) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.drivers_title),
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
                    BadgedBox(badge = { if (ui.activeFiltersCount > 0) Badge { Text("${ui.activeFiltersCount}") } }) {
                        IconButton(onClick = { showFilters = true }) {
                            Icon(imageVector = Icons.Default.FilterList, contentDescription = null, tint = if (ui.hasActiveFilters) RcColor5 else RcColor6)
                        }
                    }
                    IconButton(onClick = { vm.onRefresh() }, enabled = !ui.isRefreshing) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = stringResource(R.string.common_refresh), tint = if (ui.isRefreshing) RcColor8 else RcColor5)
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
                    text = stringResource(R.string.drivers_your),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = RcColor6
                )

                Button(
                    onClick = {
                        showCreateDialog = true
                    },
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
                        text = stringResource(R.string.drivers_add_button),
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

            if (ui.empty) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "👤",
                            fontSize = 40.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.drivers_empty),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = RcColor8
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.drivers_empty_subtitle),
                            fontSize = 12.sp,
                            color = RcColor8.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(ui.items, key = { it.id }) { driver ->
                        DriverItemCard(
                            driver = DriverUi(driver.id, driver.firstName, driver.lastName, driver.email, driver.phone, driver.licenseNumber, driver.active),
                            onEdit = { editDriverId = driver.id },
                            onDelete = { vm.onDelete(driver.id) }
                        )
                    }
                }
            }
        }
    }

    // Diálogo de crear
    if (showCreateDialog) {
        DriverUpsertDialog(
            title = stringResource(R.string.drivers_create_title),
            initial = DriverFormUi(),
            onDismiss = { showCreateDialog = false },
            onConfirm = { form ->
                vm.onCreate(form.firstName, form.lastName, form.email, form.phone, form.licenseNumber, form.active)
                showCreateDialog = false
            }
        )
    }

    // Diálogo de editar
    editDriverId?.let { driverId ->
        val driver = ui.items.find { it.id == driverId }
        if (driver != null) {
            DriverUpsertDialog(
                title = stringResource(R.string.drivers_edit_title),
                initial = DriverFormUi(
                    firstName = driver.firstName,
                    lastName = driver.lastName,
                    email = driver.email,
                    phone = driver.phone,
                    licenseNumber = driver.licenseNumber,
                    active = driver.active
                ),
                onDismiss = { editDriverId = null },
                onConfirm = { form ->
                    vm.onUpdate(driverId, form.firstName, form.lastName, form.email, form.phone, form.licenseNumber, form.active)
                    editDriverId = null
                }
            )
        } else {
            editDriverId = null
        }
    }

    if (showFilters) {
        DriversFiltersDialog(
            current = DriversManagementViewModel.Filters(),
            onApply = { f -> vm.onFiltersChanged(f); showFilters = false },
            onClear = { vm.onFiltersChanged(DriversManagementViewModel.Filters()); showFilters = false },
            onDismiss = { showFilters = false }
        )
    }
}

@Composable
private fun DriverItemCard(
    driver: DriverUi,
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
            // Header: Nombre completo y menú
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${driver.firstName} ${driver.lastName}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = RcColor6
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = stringResource(R.string.drivers_license_label, driver.licenseNumber),
                        fontSize = 11.sp,
                        color = RcColor8,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Menú de opciones
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.drivers_menu_options),
                            tint = RcColor6
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.drivers_menu_edit), color = RcColor6, fontWeight = FontWeight.Medium) },
                            onClick = {
                                showMenu = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.drivers_menu_delete), color = RcColor5, fontWeight = FontWeight.Bold) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Información de contacto
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = RcColor7,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Email
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = RcColor5.copy(alpha = 0.2f),
                            modifier = Modifier.size(30.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = RcColor5,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.drivers_card_email_label),
                                fontSize = 10.sp,
                                color = RcColor8,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = driver.email,
                                fontSize = 12.sp,
                                color = RcColor6,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }

                    // Teléfono
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = RcColor4.copy(alpha = 0.2f),
                            modifier = Modifier.size(30.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = RcColor4,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.drivers_card_phone_label),
                                fontSize = 10.sp,
                                color = RcColor8,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = driver.phone,
                                fontSize = 12.sp,
                                color = RcColor6,
                                fontWeight = FontWeight.Normal
                            )
                        }
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
                    color = if (driver.active) Color(0xFF4CAF50) else RcColor8.copy(alpha = 0.3f),
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
                                    if (driver.active) Color.White else RcColor8,
                                    shape = RoundedCornerShape(3.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (driver.active) stringResource(R.string.drivers_active) else stringResource(R.string.drivers_inactive),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (driver.active) White else RcColor8
                        )
                    }
                }
            }
        }
    }
}

// Modelo de formulario
private data class DriverFormUi(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String = "",
    val licenseNumber: String = "",
    val active: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DriverUpsertDialog(
    title: String,
    initial: DriverFormUi,
    onDismiss: () -> Unit,
    onConfirm: (DriverFormUi) -> Unit
) {
    var form by remember { mutableStateOf(initial) }

    val canSave = remember(form) {
        form.firstName.isNotBlank() &&
                form.lastName.isNotBlank() &&
                form.email.isNotBlank() &&
                form.phone.isNotBlank() &&
                form.licenseNumber.isNotBlank()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    title,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 19.sp,
                    color = RcColor6
                )
                Spacer(Modifier.height(3.dp))
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(3.dp)
                        .background(RcColor5, RoundedCornerShape(1.5.dp))
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Información personal
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = RcColor7.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RcColor5.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = RcColor5,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                stringResource(R.string.drivers_section_personal),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = RcColor5
                            )
                        }

                        OutlinedTextField(
                            value = form.firstName,
                            onValueChange = { form = form.copy(firstName = it) },
                            label = { Text(stringResource(R.string.drivers_first_name), fontWeight = FontWeight.Medium, fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RcColor5,
                                focusedLabelColor = RcColor5,
                                unfocusedBorderColor = RcColor8.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                        )

                        OutlinedTextField(
                            value = form.lastName,
                            onValueChange = { form = form.copy(lastName = it) },
                            label = { Text(stringResource(R.string.drivers_last_name), fontWeight = FontWeight.Medium, fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RcColor5,
                                focusedLabelColor = RcColor5,
                                unfocusedBorderColor = RcColor8.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                        )
                    }
                }

                // Información de contacto
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = RcColor7.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RcColor4.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = RcColor4,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                stringResource(R.string.drivers_section_contact),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = RcColor4
                            )
                        }

                        OutlinedTextField(
                            value = form.email,
                            onValueChange = { form = form.copy(email = it) },
                            label = { Text(stringResource(R.string.drivers_email), fontWeight = FontWeight.Medium, fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RcColor5,
                                focusedLabelColor = RcColor5,
                                unfocusedBorderColor = RcColor8.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                        )

                        OutlinedTextField(
                            value = form.phone,
                            onValueChange = { form = form.copy(phone = it) },
                            label = { Text(stringResource(R.string.drivers_phone), fontWeight = FontWeight.Medium, fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RcColor5,
                                focusedLabelColor = RcColor5,
                                unfocusedBorderColor = RcColor8.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                        )
                    }
                }

                // Licencia
                OutlinedTextField(
                    value = form.licenseNumber,
                    onValueChange = { form = form.copy(licenseNumber = it) },
                    label = { Text(stringResource(R.string.drivers_license_number), fontWeight = FontWeight.Medium, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RcColor5,
                        focusedLabelColor = RcColor5,
                        unfocusedBorderColor = RcColor8.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                )

                // Estado con switch elegante
                Surface(
                    shape = RoundedCornerShape(10.dp),
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
                            .padding(horizontal = 12.dp, vertical = 8.dp)
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
                            Spacer(Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.drivers_active_label),
                                fontSize = 13.sp,
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
        },
        confirmButton = {
            Button(
                enabled = canSave,
                onClick = { onConfirm(form) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = RcColor5,
                    disabledContainerColor = RcColor8.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(40.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    stringResource(R.string.drivers_save_button),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(40.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    stringResource(R.string.common_cancel),
                    color = RcColor8,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        },
        shape = RoundedCornerShape(18.dp),
        containerColor = White
    )
}

@Composable
private fun DriversFiltersDialog(
    current: DriversManagementViewModel.Filters,
    onApply: (DriversManagementViewModel.Filters) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    var f by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(stringResource(R.string.drivers_filters_title), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = RcColor6)
                Spacer(Modifier.height(2.dp))
                Box(modifier = Modifier.width(40.dp).height(3.dp)) {}
            }
        },
        text = {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(R.string.drivers_filters_status), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RcColor6)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = f.activeOnly == null, onClick = { f = f.copy(activeOnly = null) }, label = { Text(stringResource(R.string.drivers_filters_all), fontSize = 11.sp) })
                    FilterChip(selected = f.activeOnly == true, onClick = { f = f.copy(activeOnly = true) }, label = { Text(stringResource(R.string.drivers_filters_active), fontSize = 11.sp) })
                    FilterChip(selected = f.activeOnly == false, onClick = { f = f.copy(activeOnly = false) }, label = { Text(stringResource(R.string.drivers_filters_inactive), fontSize = 11.sp) })
                }
                OutlinedTextField(
                    value = f.query.orEmpty(),
                    onValueChange = { f = f.copy(query = it) },
                    label = { Text(stringResource(R.string.drivers_filters_search_hint), fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                )
            }
        },
        confirmButton = {
            Button(onClick = { onApply(f) }, shape = RoundedCornerShape(8.dp), modifier = Modifier.height(36.dp)) { Text(stringResource(R.string.drivers_filters_apply), fontWeight = FontWeight.Bold, fontSize = 12.sp) }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextButton(onClick = onClear, shape = RoundedCornerShape(8.dp), modifier = Modifier.height(36.dp)) { Text(stringResource(R.string.drivers_filters_clear), fontSize = 12.sp) }
                TextButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp), modifier = Modifier.height(36.dp)) { Text(stringResource(R.string.common_cancel), fontSize = 12.sp) }
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = White
    )
}

