package com.wapps1.redcarga.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.wapps1.redcarga.core.ui.theme.RcColor2
import com.wapps1.redcarga.core.ui.theme.RcColor6

/**
 * Campo de texto personalizado para Red Carga
 */
@Composable
fun RcTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    isPassword: Boolean = false,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    maxLines: Int = 1,
    enabled: Boolean = true
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            leadingIcon = if (leadingIcon != null) {
                { Icon(leadingIcon, contentDescription = null) }
            } else null,
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible)
                                Icons.Filled.Visibility
                            else
                                Icons.Filled.VisibilityOff,
                            contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                }
            } else null,
            visualTransformation = if (isPassword && !passwordVisible)
                PasswordVisualTransformation()
            else
                VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            isError = isError,
            maxLines = maxLines,
            enabled = enabled,
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.9f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.9f),
                disabledContainerColor = Color.White.copy(alpha = 0.5f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 2.dp,
                    color = when {
                        isError -> Color.Red
                        else -> RcColor2.copy(alpha = 0.3f)
                    },
                    shape = RoundedCornerShape(16.dp)
                )
        )

        if (isError && errorMessage != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorMessage,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}
