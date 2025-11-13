package com.wapps1.redcarga.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wapps1.redcarga.core.ui.theme.RcColor2
import com.wapps1.redcarga.core.ui.theme.RcColor5
import com.wapps1.redcarga.core.ui.theme.RcColor6

/**
 * Input para códigos OTP/PIN de 6 dígitos
 * Diseño moderno con cajas separadas
 */
@Composable
fun RcOTPInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    length: Int = 6,
    isError: Boolean = false
) {
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(value, TextRange(value.length)))
    }

    LaunchedEffect(value) {
        if (value != textFieldValue.text) {
            textFieldValue = TextFieldValue(value, TextRange(value.length))
        }
    }

    BasicTextField(
        value = textFieldValue,
        onValueChange = { newValue ->
            if (newValue.text.length <= length && newValue.text.all { it.isDigit() }) {
                textFieldValue = newValue.copy(selection = TextRange(newValue.text.length))
                onValueChange(newValue.text)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        decorationBox = {
            Row(
                modifier = modifier,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(length) { index ->
                    val char = value.getOrNull(index)
                    val isFocused = value.length == index
                    
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.95f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(
                                width = 2.dp,
                                color = when {
                                    isError -> Color.Red
                                    isFocused -> RcColor5
                                    char != null -> RcColor2
                                    else -> RcColor2.copy(alpha = 0.3f)
                                },
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char?.toString() ?: "",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = RcColor6,
                            textAlign = TextAlign.Center,
                            fontSize = 24.sp
                        )
                    }
                }
            }
        }
    )
}
