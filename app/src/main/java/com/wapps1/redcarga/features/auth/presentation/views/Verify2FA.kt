package com.wapps1.redcarga.features.auth.presentation.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wapps1.redcarga.R
import com.wapps1.redcarga.core.ui.components.RcBackButton
import com.wapps1.redcarga.core.ui.components.RcBackground
import com.wapps1.redcarga.core.ui.components.RcButton
import com.wapps1.redcarga.core.ui.components.RcOTPInput
import com.wapps1.redcarga.core.ui.theme.RcColor5
import com.wapps1.redcarga.core.ui.theme.RcColor6
import com.wapps1.redcarga.core.ui.theme.RedcargaTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
fun Verify2FA(
    phoneNumber: String = "+51 999 999 999",
    onVerifySuccess: () -> Unit,
    onResendCode: () -> Unit,
    onBackClick: () -> Unit
) {
    var otpCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var countdown by remember { mutableStateOf(60) }
    var canResend by remember { mutableStateOf(false) }

    // Countdown timer
    LaunchedEffect(Unit) {
        while (countdown > 0) {
            delay(1.seconds)
            countdown--
        }
        canResend = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        RcBackground(Modifier.matchParentSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header con botón atrás
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                RcBackButton(
                    onClick = onBackClick,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
            }

            Spacer(modifier = Modifier.weight(0.3f))

            // Icono
            Image(
                painter = painterResource(R.drawable.ic_agent_welcome_sign),
                contentDescription = null,
                modifier = Modifier.size(140.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Título
            Text(
                text = stringResource(R.string.verify_2fa_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = RcColor6,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Subtítulo
            Text(
                text = stringResource(R.string.verify_2fa_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = RcColor6.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Número de teléfono
            Text(
                text = phoneNumber,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = RcColor5,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Input OTP
            RcOTPInput(
                value = otpCode,
                onValueChange = { otpCode = it },
                modifier = Modifier.fillMaxWidth(),
                length = 6
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Botón Verificar
            RcButton(
                text = stringResource(R.string.verify_2fa_verify_button),
                onClick = {
                    isLoading = true
                    // TODO: Implementar verificación
                    onVerifySuccess()
                },
                loading = isLoading,
                enabled = otpCode.length == 6
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Texto "¿No recibiste el código?"
            Text(
                text = stringResource(R.string.verify_2fa_didnt_receive),
                style = MaterialTheme.typography.bodyMedium,
                color = RcColor6.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Botón Reenviar
            TextButton(
                onClick = {
                    if (canResend) {
                        onResendCode()
                        countdown = 60
                        canResend = false
                    }
                },
                enabled = canResend
            ) {
                Text(
                    text = if (canResend) {
                        stringResource(R.string.verify_2fa_resend)
                    } else {
                        stringResource(R.string.verify_2fa_resend_in, formatTime(countdown))
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (canResend) RcColor5 else RcColor6.copy(alpha = 0.4f)
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

private fun formatTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun Verify2FAPreview() {
    RedcargaTheme(darkTheme = false) {
        Verify2FA(
            onVerifySuccess = {},
            onResendCode = {},
            onBackClick = {}
        )
    }
}
