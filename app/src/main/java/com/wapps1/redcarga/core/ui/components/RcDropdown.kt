package com.wapps1.redcarga.core.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.wapps1.redcarga.core.ui.theme.RcColor2
import com.wapps1.redcarga.core.ui.theme.RcColor6

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RcDropdown(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    options: List<String>,
    modifier: Modifier = Modifier,
    displayNames: List<String>? = null,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    // Use displayNames if provided, otherwise use options
    val itemLabels = displayNames ?: options

    // Find the display name for the current value
    val displayValue = if (displayNames != null && options.contains(value)) {
        val index = options.indexOf(value)
        if (index >= 0 && index < displayNames.size) displayNames[index] else value
    } else {
        value
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded },
        modifier = modifier
    ) {
        TextField(
            value = displayValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            leadingIcon = if (leadingIcon != null) {
                { Icon(leadingIcon, contentDescription = null) }
            } else null,
            trailingIcon = {
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null
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
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled)
                .border(
                    width = 2.dp,
                    color = RcColor2.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp)
                )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .exposedDropdownSize()
        ) {
            options.forEachIndexed { index, option ->
                val displayName = if (index < itemLabels.size) itemLabels[index] else option
                DropdownMenuItem(
                    text = { Text(displayName, color = RcColor6) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

