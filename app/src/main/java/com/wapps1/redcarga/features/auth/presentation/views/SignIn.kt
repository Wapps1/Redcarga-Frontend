package com.wapps1.redcarga.features.auth.presentation.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wapps1.redcarga.R
import com.wapps1.redcarga.core.ui.components.RcBackButton
import com.wapps1.redcarga.core.ui.components.RcBackground
import com.wapps1.redcarga.core.ui.components.RcButton
import com.wapps1.redcarga.core.ui.components.RcTextField
import com.wapps1.redcarga.core.ui.theme.RcColor5
import com.wapps1.redcarga.core.ui.theme.RcColor6
import com.wapps1.redcarga.core.ui.theme.RedcargaTheme

@Composable
fun SignIn(
    onSignInSuccess: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var emailOrPhone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo con blur
        RcBackground(Modifier.matchParentSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Botón atrás
            Box(modifier = Modifier.fillMaxWidth()) {
                RcBackButton(
                    onClick = onBackClick,
                    modifier = Modifier.align(Alignment.TopStart)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Logo
            Image(
                painter = painterResource(R.drawable.ic_agent_welcome_sign),
                contentDescription = null,
                modifier = Modifier.size(120.dp)
            )

            // Título
            Text(
                text = stringResource(R.string.auth_login_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = RcColor6,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo Email/Teléfono
            RcTextField(
                value = emailOrPhone,
                onValueChange = { emailOrPhone = it },
                label = stringResource(R.string.signin_email_or_phone),
                leadingIcon = Icons.Default.Email,
                keyboardType = KeyboardType.Email,
                modifier = Modifier.fillMaxWidth()
            )

            // Campo Password/PIN
            RcTextField(
                value = password,
                onValueChange = { password = it },
                label = stringResource(R.string.signin_password),
                leadingIcon = Icons.Default.Lock,
                isPassword = true,
                keyboardType = KeyboardType.Password,
                modifier = Modifier.fillMaxWidth()
            )

            // Link "Olvidé contraseña"
            Text(
                text = stringResource(R.string.auth_forgot_password),
                style = MaterialTheme.typography.bodyMedium,
                color = RcColor5,
                modifier = Modifier
                    .clickable(onClick = onForgotPasswordClick)
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Botón Iniciar Sesión
            RcButton(
                text = stringResource(R.string.signin_button),
                onClick = {
                    isLoading = true
                    // TODO: Implementar lógica de login
                    // Por ahora, simular éxito
                    onSignInSuccess()
                },
                loading = isLoading,
                enabled = emailOrPhone.isNotEmpty() && password.isNotEmpty()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Divisor "o"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = 1.dp,
                    color = RcColor6.copy(alpha = 0.3f)
                )
                Text(
                    text = "o",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RcColor6.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = 1.dp,
                    color = RcColor6.copy(alpha = 0.3f)
                )
            }

            // Link registro
            Text(
                text = stringResource(R.string.signin_register_link),
                style = MaterialTheme.typography.bodyMedium,
                color = RcColor5,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clickable(onClick = onRegisterClick)
                    .padding(8.dp)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SignInPreview() {
    RedcargaTheme(darkTheme = false) {
        SignIn(
            onSignInSuccess = {},
            onForgotPasswordClick = {},
            onRegisterClick = {},
            onBackClick = {}
        )
    }
}
