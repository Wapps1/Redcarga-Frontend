package com.wapps1.redcarga.features.fleet.presentation.views

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DriveEta
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.wapps1.redcarga.core.ui.theme.*
import com.wapps1.redcarga.R
import androidx.hilt.navigation.compose.hiltViewModel
import com.wapps1.redcarga.features.fleet.presentation.viewmodels.DriversManagementViewModel
import kotlinx.coroutines.delay
import android.util.Log

private const val TAG_UI = "DriversManagementUI"



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
    val registrationState by vm.registrationState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var editDriverId by remember { mutableStateOf<Long?>(null) }
    var showFilters by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) { vm.bootstrap() }
    
    // Manejar efectos del ViewModel
    LaunchedEffect(Unit) {
        vm.effects.collect { effect ->
            when (effect) {
                is DriversManagementViewModel.Effect.Message -> {
                    snackbar.showSnackbar(effect.text)
                }
                is DriversManagementViewModel.Effect.OpenUrl -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(effect.url))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
            }
        }
    }

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
                        vm.startDriverRegistration()
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

    // Diálogo de registro completo de conductor
    when (val state = registrationState) {
        is DriversManagementViewModel.RegistrationState.Idle -> { /* No mostrar nada */ }
        else -> {
            DriverRegistrationDialog(
                state = state,
                onDismiss = { vm.cancelDriverRegistration() },
                onStep1Next = { form -> vm.onRegistrationStep1Next(form) },
                onOpenVerificationLink = { link -> vm.onOpenVerificationLink(link) },
                onContinueAfterEmailVerification = { accountId, form -> 
                    Log.d(TAG_UI, "   [UI] Continuando después de verificación de email")
                    vm.continueAfterEmailVerification(accountId, form) 
                },
                onStep2Next = { form -> vm.onRegistrationStep2Next(form) },
                onStep4Next = { form -> vm.onRegistrationStep4Next(form) },
                onSuccess = { vm.cancelDriverRegistration() }
            )
        }
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

// ========== DIÁLOGO DE REGISTRO COMPLETO DE CONDUCTOR ==========

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DriverRegistrationDialog(
    state: DriversManagementViewModel.RegistrationState,
    onDismiss: () -> Unit,
    onStep1Next: (DriversManagementViewModel.DriverRegistrationForm) -> Unit,
    onOpenVerificationLink: (String) -> Unit,
    onContinueAfterEmailVerification: (Long, DriversManagementViewModel.DriverRegistrationForm) -> Unit,
    onStep2Next: (DriversManagementViewModel.DriverRegistrationForm) -> Unit,
    onStep4Next: (DriversManagementViewModel.DriverRegistrationForm) -> Unit,
    onSuccess: () -> Unit
) {
    Log.d(TAG_UI, "═══════════════════════════════════════")
    Log.d(TAG_UI, "🎨 [UI] DriverRegistrationDialog renderizado")
    Log.d(TAG_UI, "═══════════════════════════════════════")
    Log.d(TAG_UI, "   Estado actual: ${state::class.simpleName}")
    
    var form by remember {
        mutableStateOf(
            when (state) {
                is DriversManagementViewModel.RegistrationState.Step1Form -> {
                    Log.d(TAG_UI, "   Inicializando form desde Step1Form")
                    state.form
                }
                is DriversManagementViewModel.RegistrationState.Step1Creating -> {
                    Log.d(TAG_UI, "   Inicializando form desde Step1Creating")
                    state.form
                }
                is DriversManagementViewModel.RegistrationState.Step1EmailVerification -> {
                    Log.d(TAG_UI, "   Inicializando form desde Step1EmailVerification")
                    state.form
                }
                is DriversManagementViewModel.RegistrationState.Step2Form -> {
                    Log.d(TAG_UI, "   Inicializando form desde Step2Form")
                    state.form
                }
                is DriversManagementViewModel.RegistrationState.Step2Verifying -> {
                    Log.d(TAG_UI, "   Inicializando form desde Step2Verifying")
                    state.form
                }
                is DriversManagementViewModel.RegistrationState.Step3Associating -> {
                    Log.d(TAG_UI, "   Inicializando form desde Step3Associating")
                    state.form
                }
                is DriversManagementViewModel.RegistrationState.Step4Form -> {
                    Log.d(TAG_UI, "   Inicializando form desde Step4Form")
                    state.form
                }
                is DriversManagementViewModel.RegistrationState.Step4Creating -> {
                    Log.d(TAG_UI, "   Inicializando form desde Step4Creating")
                    state.form
                }
                else -> {
                    Log.d(TAG_UI, "   Inicializando form vacío (default)")
                    DriversManagementViewModel.DriverRegistrationForm()
                }
            }
        )
    }
    
    val currentStep = when (state) {
        is DriversManagementViewModel.RegistrationState.Step1Form -> 1
        is DriversManagementViewModel.RegistrationState.Step1Creating -> 1
        is DriversManagementViewModel.RegistrationState.Step1EmailVerification -> 1
        is DriversManagementViewModel.RegistrationState.Step2Form -> 2
        is DriversManagementViewModel.RegistrationState.Step2Verifying -> 2
        is DriversManagementViewModel.RegistrationState.Step3Associating -> 3
        is DriversManagementViewModel.RegistrationState.Step4Form -> 4
        is DriversManagementViewModel.RegistrationState.Step4Creating -> 4
        is DriversManagementViewModel.RegistrationState.Success -> 5
        else -> 1
    }
    
    Log.d(TAG_UI, "   Paso actual: $currentStep")
    
    Dialog(
        onDismissRequest = { 
            Log.d(TAG_UI, "   Usuario intentó cerrar diálogo")
            if (state !is DriversManagementViewModel.RegistrationState.Step1Form && 
                state !is DriversManagementViewModel.RegistrationState.Step1Creating) {
                onDismiss()
            } else {
                Log.d(TAG_UI, "   Diálogo no se puede cerrar en este estado")
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Título
                Column {
                    Text(
                        text = "Registrar Nuevo Conductor",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = RcColor6
                    )
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(50.dp)
                            .height(4.dp)
                            .background(RcColor5, RoundedCornerShape(2.dp))
                    )
                }
                
                Spacer(Modifier.height(16.dp))
                
                // Indicador de progreso
                RegistrationProgressIndicator(
                    currentStep = currentStep,
                    totalSteps = 4
                )
                
                Spacer(Modifier.height(20.dp))
                
                // Contenido según el estado
                when (state) {
                    // ✅ NUEVO: Estado para formulario (sin carga)
                    is DriversManagementViewModel.RegistrationState.Step1Form -> {
                        Log.d(TAG_UI, "   Renderizando Step1Form (formulario, sin carga)")
                        RegistrationStep1Content(
                            form = form,
                            onFormChange = { 
                                Log.d(TAG_UI, "   Form cambiado: email=${it.email}, username=${it.username}, passwordLength=${it.password.length}, isValid=${it.isStep1Valid()}")
                                form = it 
                            },
                            onNext = { 
                                Log.d(TAG_UI, "   Usuario presionó 'Continuar' en Step1Form")
                                onStep1Next(form) 
                            },
                            isLoading = false  // ✅ CORREGIDO: false cuando es Step1Form
                        )
                    }
                    // ✅ Estado para cuando está procesando (con carga)
                    is DriversManagementViewModel.RegistrationState.Step1Creating -> {
                        Log.d(TAG_UI, "   Renderizando Step1Creating (procesando, con carga)")
                        RegistrationStep1Content(
                            form = form,
                            onFormChange = { form = it },
                            onNext = { onStep1Next(form) },
                            isLoading = true  // ✅ CORREGIDO: true cuando es Step1Creating
                        )
                    }
                    is DriversManagementViewModel.RegistrationState.Step1EmailVerification -> {
                        Log.d(TAG_UI, "   Renderizando Step1EmailVerification")
                        Log.d(TAG_UI, "   AccountId: ${state.accountId}, Email: ${state.email}")
                        RegistrationEmailVerificationContent(
                            email = state.email,
                            verificationLink = state.verificationLink,
                            onOpenLink = { 
                                Log.d(TAG_UI, "   Usuario presionó 'Abrir Enlace de Verificación'")
                                onOpenVerificationLink(state.verificationLink) 
                            },
                            onContinue = { 
                                Log.d(TAG_UI, "   Usuario presionó 'Continuar con el Registro'")
                                Log.d(TAG_UI, "   Llamando a onContinueAfterEmailVerification con accountId=${state.accountId}")
                                // ✅ CORRECCIÓN: Llamar a continueAfterEmailVerification para mostrar el formulario del paso 2
                                onContinueAfterEmailVerification(state.accountId, state.form)
                            }
                        )
                    }
                    is DriversManagementViewModel.RegistrationState.Step2Form -> {
                        Log.d(TAG_UI, "   Renderizando Step2Form (formulario, sin carga)")
                        RegistrationStep2Content(
                            form = form,
                            onFormChange = { 
                                Log.d(TAG_UI, "   Form cambiado: fullName=${it.fullName}, docType=${it.docTypeCode}, docNumber=${it.docNumber}, isValid=${it.isStep2Valid()}")
                                form = it 
                            },
                            onNext = { 
                                Log.d(TAG_UI, "   Usuario presionó 'Continuar' en Step2Form")
                                onStep2Next(form) 
                            },
                            isLoading = false
                        )
                    }
                    is DriversManagementViewModel.RegistrationState.Step2Verifying -> {
                        Log.d(TAG_UI, "   Renderizando Step2Verifying (procesando)")
                        RegistrationLoadingContent(
                            message = "Verificando identidad...",
                            step = 2
                        )
                    }
                    is DriversManagementViewModel.RegistrationState.Step3Associating -> {
                        Log.d(TAG_UI, "   Renderizando Step3Associating (procesando)")
                        RegistrationLoadingContent(
                            message = "Asociando conductor a la compañía...",
                            step = 3
                        )
                    }
                    is DriversManagementViewModel.RegistrationState.Step4Form -> {
                        Log.d(TAG_UI, "   Renderizando Step4Form (formulario, sin carga)")
                        RegistrationStep4Content(
                            form = form,
                            onFormChange = { 
                                Log.d(TAG_UI, "   Form cambiado: licenseNumber=${it.licenseNumber}, active=${it.active}")
                                form = it 
                            },
                            onNext = { 
                                Log.d(TAG_UI, "   Usuario presionó 'Finalizar Registro' en Step4Form")
                                onStep4Next(form) 
                            },
                            isLoading = false
                        )
                    }
                    is DriversManagementViewModel.RegistrationState.Step4Creating -> {
                        Log.d(TAG_UI, "   Renderizando Step4Creating (procesando)")
                        RegistrationLoadingContent(
                            message = "Registrando conductor en Fleet...",
                            step = 4
                        )
                    }
                    is DriversManagementViewModel.RegistrationState.Success -> {
                        Log.d(TAG_UI, "   Renderizando Success (driverId=${state.driverId})")
                        RegistrationSuccessContent(
                            driverId = state.driverId,
                            onClose = {
                                Log.d(TAG_UI, "   Usuario presionó 'Cerrar' en Success")
                                onSuccess()
                            }
                        )
                    }
                    is DriversManagementViewModel.RegistrationState.Error -> {
                        Log.e(TAG_UI, "   Renderizando Error: ${state.message}")
                        RegistrationErrorContent(
                            message = state.message,
                            onDismiss = {
                                Log.d(TAG_UI, "   Usuario presionó 'Cerrar' en Error")
                                onDismiss()
                            }
                        )
                    }
                    else -> {
                        Log.w(TAG_UI, "   Estado desconocido o Idle, no renderizando contenido")
                    }
                }
            }
        }
    }
}

@Composable
private fun RegistrationProgressIndicator(
    currentStep: Int,
    totalSteps: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { step ->
            val stepNumber = step + 1
            val isCompleted = stepNumber < currentStep
            val isCurrent = stepNumber == currentStep
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Círculo del paso
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = when {
                        isCompleted -> RcColor5
                        isCurrent -> RcColor5.copy(alpha = 0.3f)
                        else -> RcColor7
                    },
                    border = BorderStroke(
                        width = if (isCurrent) 2.dp else 0.dp,
                        color = RcColor5
                    )
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = White,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                text = "$stepNumber",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent) RcColor5 else RcColor8
                            )
                        }
                    }
                }
                
                // Línea conectora
                if (stepNumber < totalSteps) {
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .background(
                                if (isCompleted) RcColor5 else RcColor7,
                                RoundedCornerShape(1.5.dp)
                            )
                    )
                    Spacer(Modifier.width(8.dp))
                }
            }
        }
    }
}

@Composable
private fun RegistrationStep1Content(
    form: DriversManagementViewModel.DriverRegistrationForm,
    onFormChange: (DriversManagementViewModel.DriverRegistrationForm) -> Unit,
    onNext: () -> Unit,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Paso 1: Crear Cuenta Básica",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = RcColor6
        )
        
        Text(
            text = "Ingresa los datos básicos para crear la cuenta del conductor",
            fontSize = 12.sp,
            color = RcColor8
        )
        
        Spacer(Modifier.height(8.dp))
        
        // Email
        OutlinedTextField(
            value = form.email,
            onValueChange = { onFormChange(form.copy(email = it)) },
            label = { Text("Email", fontWeight = FontWeight.Medium, fontSize = 12.sp) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RcColor5,
                focusedLabelColor = RcColor5,
                unfocusedBorderColor = RcColor8.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.Email, null, tint = RcColor5, modifier = Modifier.size(18.dp))
            }
        )
        
        // Username
        OutlinedTextField(
            value = form.username,
            onValueChange = { onFormChange(form.copy(username = it)) },
            label = { Text("Username", fontWeight = FontWeight.Medium, fontSize = 12.sp) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RcColor5,
                focusedLabelColor = RcColor5,
                unfocusedBorderColor = RcColor8.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.Person, null, tint = RcColor5, modifier = Modifier.size(18.dp))
            }
        )
        
        // Password
        var passwordVisible by remember { mutableStateOf(false) }
        OutlinedTextField(
            value = form.password,
            onValueChange = { onFormChange(form.copy(password = it)) },
            label = { Text("Contraseña (mín. 8 caracteres)", fontWeight = FontWeight.Medium, fontSize = 12.sp) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RcColor5,
                focusedLabelColor = RcColor5,
                unfocusedBorderColor = RcColor8.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
            leadingIcon = {
                Icon(Icons.Default.Lock, null, tint = RcColor5, modifier = Modifier.size(18.dp))
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.VerifiedUser else Icons.Default.Lock,
                        null,
                        tint = RcColor8,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        )
        
        Spacer(Modifier.height(8.dp))
        
        // Botón siguiente
        Button(
            onClick = onNext,
            enabled = form.isStep1Valid() && !isLoading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = RcColor5,
                disabledContainerColor = RcColor8.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = White,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                "Continuar",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun RegistrationEmailVerificationContent(
    email: String,
    verificationLink: String,
    onOpenLink: () -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(20.dp))
        
        // Icono
        Surface(
            modifier = Modifier.size(80.dp),
            shape = RoundedCornerShape(40.dp),
            color = RcColor5.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = RcColor5,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        
        Text(
            text = "Verifica tu Email",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = RcColor6
        )
        
        Text(
            text = "Se ha enviado un enlace de verificación a:",
            fontSize = 13.sp,
            color = RcColor8,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = RcColor7,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = email,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = RcColor6,
                modifier = Modifier.padding(12.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
        
        Text(
            text = "Haz clic en el botón para abrir el enlace de verificación en tu navegador",
            fontSize = 12.sp,
            color = RcColor8,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(Modifier.height(8.dp))
        
        // Botón para abrir link
        Button(
            onClick = onOpenLink,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = RcColor5),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.OpenInBrowser,
                contentDescription = null,
                tint = White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Abrir Enlace de Verificación",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
        
        Spacer(Modifier.height(8.dp))
        
        Divider()
        
        Spacer(Modifier.height(8.dp))
        
        Text(
            text = "¿Ya verificaste tu email?",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = RcColor6
        )
        
        OutlinedButton(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = RcColor5
            ),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, RcColor5)
        ) {
            Text(
                "Continuar con el Registro",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegistrationStep2Content(
    form: DriversManagementViewModel.DriverRegistrationForm,
    onFormChange: (DriversManagementViewModel.DriverRegistrationForm) -> Unit,
    onNext: () -> Unit,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Paso 2: Datos Personales",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = RcColor6
        )
        
        Text(
            text = "Ingresa los datos de identidad del conductor",
            fontSize = 12.sp,
            color = RcColor8
        )
        
        Spacer(Modifier.height(8.dp))
        
        // Nombre completo
        OutlinedTextField(
            value = form.fullName,
            onValueChange = { onFormChange(form.copy(fullName = it)) },
            label = { Text("Nombre Completo", fontWeight = FontWeight.Medium, fontSize = 12.sp) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RcColor5,
                focusedLabelColor = RcColor5,
                unfocusedBorderColor = RcColor8.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.Person, null, tint = RcColor5, modifier = Modifier.size(18.dp))
            }
        )
        
        // Tipo de documento
        var docTypeExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = docTypeExpanded,
            onExpandedChange = { docTypeExpanded = it }
        ) {
            OutlinedTextField(
                value = form.docTypeCode,
                onValueChange = {},
                readOnly = true,
                label = { Text("Tipo de Documento", fontWeight = FontWeight.Medium, fontSize = 12.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RcColor5,
                    focusedLabelColor = RcColor5,
                    unfocusedBorderColor = RcColor8.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(10.dp),
                leadingIcon = {
                    Icon(Icons.Default.Badge, null, tint = RcColor5, modifier = Modifier.size(18.dp))
                },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = docTypeExpanded) }
            )
            ExposedDropdownMenu(
                expanded = docTypeExpanded,
                onDismissRequest = { docTypeExpanded = false }
            ) {
                listOf("DNI", "CE", "PAS").forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type) },
                        onClick = {
                            onFormChange(form.copy(docTypeCode = type))
                            docTypeExpanded = false
                        }
                    )
                }
            }
        }
        
        // Número de documento
        OutlinedTextField(
            value = form.docNumber,
            onValueChange = { onFormChange(form.copy(docNumber = it)) },
            label = { Text("Número de Documento", fontWeight = FontWeight.Medium, fontSize = 12.sp) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RcColor5,
                focusedLabelColor = RcColor5,
                unfocusedBorderColor = RcColor8.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(10.dp),
            singleLine = true
        )
        
        // Fecha de nacimiento
        OutlinedTextField(
            value = form.birthDate,
            onValueChange = { onFormChange(form.copy(birthDate = it)) },
            label = { Text("Fecha de Nacimiento (yyyy-MM-dd)", fontWeight = FontWeight.Medium, fontSize = 12.sp) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RcColor5,
                focusedLabelColor = RcColor5,
                unfocusedBorderColor = RcColor8.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            placeholder = { Text("1990-05-15", fontSize = 12.sp, color = RcColor8) },
            leadingIcon = {
                Icon(Icons.Default.CalendarToday, null, tint = RcColor5, modifier = Modifier.size(18.dp))
            }
        )
        
        // Teléfono
        OutlinedTextField(
            value = form.phone,
            onValueChange = { onFormChange(form.copy(phone = it)) },
            label = { Text("Teléfono (+51987654321)", fontWeight = FontWeight.Medium, fontSize = 12.sp) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RcColor5,
                focusedLabelColor = RcColor5,
                unfocusedBorderColor = RcColor8.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.Phone, null, tint = RcColor5, modifier = Modifier.size(18.dp))
            }
        )
        
        // RUC
        OutlinedTextField(
            value = form.ruc,
            onValueChange = { onFormChange(form.copy(ruc = it)) },
            label = { Text("RUC", fontWeight = FontWeight.Medium, fontSize = 12.sp) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RcColor5,
                focusedLabelColor = RcColor5,
                unfocusedBorderColor = RcColor8.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(10.dp),
            singleLine = true
        )
        
        Spacer(Modifier.height(8.dp))
        
        // Botón siguiente
        Button(
            onClick = onNext,
            enabled = form.isStep2Valid() && !isLoading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = RcColor5,
                disabledContainerColor = RcColor8.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = White,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                "Continuar",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun RegistrationStep4Content(
    form: DriversManagementViewModel.DriverRegistrationForm,
    onFormChange: (DriversManagementViewModel.DriverRegistrationForm) -> Unit,
    onNext: () -> Unit,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Paso 4: Información del Conductor",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = RcColor6
        )
        
        Text(
            text = "Ingresa la información específica del conductor",
            fontSize = 12.sp,
            color = RcColor8
        )
        
        Spacer(Modifier.height(8.dp))
        
        // Licencia de conducir
        OutlinedTextField(
            value = form.licenseNumber,
            onValueChange = { onFormChange(form.copy(licenseNumber = it)) },
            label = { Text("Número de Licencia (Opcional)", fontWeight = FontWeight.Medium, fontSize = 12.sp) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RcColor5,
                focusedLabelColor = RcColor5,
                unfocusedBorderColor = RcColor8.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.DriveEta, null, tint = RcColor5, modifier = Modifier.size(18.dp))
            }
        )
        
        // Estado activo
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (form.active) RcColor5.copy(alpha = 0.1f) else RcColor8.copy(alpha = 0.1f),
            border = BorderStroke(
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
                        "Conductor Activo",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = RcColor6
                    )
                }
                Switch(
                    checked = form.active,
                    onCheckedChange = { onFormChange(form.copy(active = it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = White,
                        checkedTrackColor = RcColor5,
                        uncheckedThumbColor = White,
                        uncheckedTrackColor = RcColor8
                    )
                )
            }
        }
        
        Spacer(Modifier.height(8.dp))
        
        // Botón finalizar
        Button(
            onClick = onNext,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = RcColor5),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = White,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                "Finalizar Registro",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun RegistrationLoadingContent(
    message: String,
    step: Int
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(60.dp),
            color = RcColor5,
            strokeWidth = 4.dp
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = message,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = RcColor6
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Paso $step de 4",
            fontSize = 12.sp,
            color = RcColor8
        )
    }
}

@Composable
private fun RegistrationSuccessContent(
    driverId: Long,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = RoundedCornerShape(40.dp),
            color = Color(0xFF4CAF50).copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(50.dp)
                )
            }
        }
        
        Spacer(Modifier.height(20.dp))
        
        Text(
            text = "¡Conductor Registrado!",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = RcColor6
        )
        
        Spacer(Modifier.height(8.dp))
        
        Text(
            text = "El conductor ha sido registrado exitosamente",
            fontSize = 13.sp,
            color = RcColor8,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(Modifier.height(4.dp))
        
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = RcColor7,
            modifier = Modifier.padding(horizontal = 40.dp)
        ) {
            Text(
                text = "ID: $driverId",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = RcColor6,
                modifier = Modifier.padding(8.dp)
            )
        }
        
        Spacer(Modifier.height(30.dp))
        
        Button(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth(0.8f),
            colors = ButtonDefaults.buttonColors(containerColor = RcColor5),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            Text(
                "Cerrar",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun RegistrationErrorContent(
    message: String,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = RoundedCornerShape(40.dp),
            color = RcColor5.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = RcColor5,
                    modifier = Modifier.size(50.dp)
                )
            }
        }
        
        Spacer(Modifier.height(20.dp))
        
        Text(
            text = "Error",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = RcColor6
        )
        
        Spacer(Modifier.height(8.dp))
        
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = RcColor7,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = message,
                fontSize = 13.sp,
                color = RcColor6,
                modifier = Modifier.padding(16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
        
        Spacer(Modifier.height(30.dp))
        
        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(0.8f),
            colors = ButtonDefaults.buttonColors(containerColor = RcColor5),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            Text(
                "Cerrar",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

