package com.wapps1.redcarga.features.requests.presentation.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wapps1.redcarga.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRequestScreen(
    onBack: () -> Unit = {},
    onSaved: () -> Unit = {}
) {
    var originDistrict by rememberSaveable { mutableStateOf("") }
    var destinationDistrict by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var itemsList by rememberSaveable { mutableStateOf(listOf<String>()) }
    
    // Obtener strings una sola vez
    val defaultItem1 = stringResource(R.string.create_request_default_item, 1)
    val defaultItem2 = stringResource(R.string.create_request_default_item, 2)
    val newItemTemplate = stringResource(R.string.create_request_new_item, 0) // Template sin número
    
    // Inicializar items por defecto
    LaunchedEffect(Unit) {
        if (itemsList.isEmpty()) {
            itemsList = listOf(defaultItem1, defaultItem2)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.create_request_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.create_request_back_description)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val newItemNumber = itemsList.size + 1
                        val newItemText = newItemTemplate.replace("%1\$d", newItemNumber.toString())
                        itemsList = itemsList + newItemText
                    }) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = stringResource(R.string.create_request_add_item_description))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionTitle(stringResource(R.string.create_request_section_origin))
                RTextField(
                    value = originDistrict,
                    onValueChange = { newText -> originDistrict = newText },
                    label = stringResource(R.string.create_request_origin_district_label)
                )
            }

            item {
                SectionTitle(stringResource(R.string.create_request_section_destination))
                RTextField(
                    value = destinationDistrict,
                    onValueChange = { newText -> destinationDistrict = newText },
                    label = stringResource(R.string.create_request_destination_district_label)
                )
            }

            item {
                SectionTitle(stringResource(R.string.create_request_section_notes))
                RTextField(
                    value = notes,
                    onValueChange = { newText -> notes = newText },
                    label = stringResource(R.string.create_request_notes_label),
                    singleLine = false,
                    maxLines = 4
                )
            }

            item { SectionTitle(stringResource(R.string.create_request_section_items)) }

            items(itemsList) { row ->
                RequestItemRow(
                    title = row,
                    onRemove = { itemsList = itemsList.filterNot { it == row } }
                )
            }

            item {
                Spacer(Modifier.height(8.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onSaved() }
                ) { Text(stringResource(R.string.create_request_save_button)) }
            }
        }
    }
}

/* ----------------- Componentes UI reutilizables ----------------- */

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun RTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue -> onValueChange(newValue) },
        modifier = modifier,
        label = { Text(label) },
        singleLine = singleLine,
        maxLines = maxLines
    )
}

@Composable
private fun RequestItemRow(
    title: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            TextButton(onClick = onRemove) { Text(stringResource(R.string.create_request_remove_item)) }
        }
    }
}
