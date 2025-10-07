package com.wapps1.redcarga.core.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.wapps1.redcarga.core.ui.theme.RcColor2
import com.wapps1.redcarga.core.ui.theme.RcColor5
import com.wapps1.redcarga.core.ui.theme.RcColor6
import java.text.SimpleDateFormat
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RcDatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = Icons.Default.CalendarToday,
    enabled: Boolean = true
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) {
                showDatePicker = true
            }
        }
    }

    Column(modifier = modifier) {
        TextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            leadingIcon = if (leadingIcon != null) {
                { Icon(leadingIcon, contentDescription = null) }
            } else null,
            trailingIcon = {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = "Seleccionar fecha"
                )
            },
            enabled = enabled,
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.9f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.9f),
                disabledContainerColor = Color.White.copy(alpha = 0.5f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            interactionSource = interactionSource,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 2.dp,
                    color = RcColor2.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp)
                )
        )

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val date = Date(millis)
                                val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                formatter.timeZone = TimeZone.getTimeZone("UTC")
                                val formattedDate = formatter.format(date)
                                onValueChange(formattedDate)
                            }
                            showDatePicker = false
                        }
                    ) {
                        Text("Aceptar", color = RcColor5)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancelar", color = RcColor6.copy(alpha = 0.7f))
                    }
                },
                colors = DatePickerDefaults.colors(
                    containerColor = Color.White
                )
            ) {
                DatePicker(
                    state = datePickerState,
                    colors = DatePickerDefaults.colors(
                        containerColor = Color.White,
                        selectedDayContainerColor = RcColor5,
                        selectedDayContentColor = Color.White,
                        todayContentColor = RcColor5,
                        todayDateBorderColor = RcColor5,
                        dayContentColor = RcColor6,
                        selectedYearContainerColor = RcColor5,
                        selectedYearContentColor = Color.White,
                        currentYearContentColor = RcColor5,
                        yearContentColor = RcColor6
                    )
                )
            }
        }
    }
}

