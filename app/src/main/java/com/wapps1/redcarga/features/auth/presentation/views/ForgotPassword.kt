package com.wapps1.redcarga.features.auth.presentation.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.wapps1.redcarga.core.ui.components.RcOTPInput
import com.wapps1.redcarga.core.ui.components.RcTextField
import com.wapps1.redcarga.core.ui.theme.RcColor5
import com.wapps1.redcarga.core.ui.theme.RcColor6
import com.wapps1.redcarga.core.ui.theme.RedcargaTheme

enum class ForgotPasswordStep {
    ENTER_EMAIL,
    ENTER_CODE,
    NEW_PASSWORD
}

@Composable
fun ForgotPassword(
    onPasswordResetSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    var currentStep by remember { mutableStateOf(ForgotPasswordStep.ENTER_EMAIL) }
    var emailOrPhone by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

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
                    onClick = {
                        if (currentStep == ForgotPasswordStep.ENTER_EMAIL) {
                            onBackClick()
                        } else {
                            currentStep = when (currentStep) {
                                ForgotPasswordStep.ENTER_CODE -> ForgotPasswordStep.ENTER_EMAIL
                                ForgotPasswordStep.NEW_PASSWORD -> ForgotPasswordStep.ENTER_CODE
                                else -> currentStep
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterStart)
                )
            }

            Spacer(modifier = Modifier.weight(0.3f))

            // Icono
            Image(
                painter = painterResource(R.drawable.ic_mascot_alert),
                contentDescription = null,
                modifier = Modifier.size(140.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Título
            Text(
                text = stringResource(R.string.forgot_password_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = RcColor6,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Contenido según el paso
            when (currentStep) {
                ForgotPasswordStep.ENTER_EMAIL -> {
                    EnterEmailStep(
                        emailOrPhone = emailOrPhone,
                        onEmailOrPhoneChange = { emailOrPhone = it },
                        isLoading = isLoading,
                        onSendCode = {
                            isLoading = true
                            // TODO: Enviar código
                            currentStep = ForgotPasswordStep.ENTER_CODE
                            isLoading = false
                        }
                    )
                }
                ForgotPasswordStep.ENTER_CODE -> {
                    EnterCodeStep(
                        emailOrPhone = emailOrPhone,
                        verificationCode = verificationCode,
                        onCodeChange = { verificationCode = it },
                        isLoading = isLoading,
                        onVerifyCode = {
                            isLoading = true
                            // TODO: Verificar código
                            currentStep = ForgotPasswordStep.NEW_PASSWORD
                            isLoading = false
                        },
                        onResendCode = {
                            // TODO: Reenviar código
                        }
                    )
                }
                ForgotPasswordStep.NEW_PASSWORD -> {
                    NewPasswordStep(
                        newPassword = newPassword,
                        onNewPasswordChange = { newPassword = it },
                        confirmPassword = confirmPassword,
                        onConfirmPasswordChange = { confirmPassword = it },
                        isLoading = isLoading,
                        onResetPassword = {
                            isLoading = true
                            // TODO: Restablecer contraseña
                            onPasswordResetSuccess()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun EnterEmailStep(
    emailOrPhone: String,
    onEmailOrPhoneChange: (String) -> Unit,
    isLoading: Boolean,
    onSendCode: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Subtítulo
        Text(
            text = stringResource(R.string.forgot_password_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = RcColor6.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Campo Email/Teléfono
        RcTextField(
            value = emailOrPhone,
            onValueChange = onEmailOrPhoneChange,
            label = stringResource(R.string.forgot_password_email_label),
            leadingIcon = Icons.Default.Email,
            keyboardType = KeyboardType.Email,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Botón Enviar Código
        RcButton(
            text = stringResource(R.string.forgot_password_send_code),
            onClick = onSendCode,
            loading = isLoading,
            enabled = emailOrPhone.isNotEmpty()
        )
    }
}

@Composable
private fun EnterCodeStep(
    emailOrPhone: String,
    verificationCode: String,
    onCodeChange: (String) -> Unit,
    isLoading: Boolean,
    onVerifyCode: () -> Unit,
    onResendCode: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Mensaje de código enviado
        Text(
            text = stringResource(R.string.forgot_password_code_sent, emailOrPhone),
            style = MaterialTheme.typography.bodyLarge,
            color = RcColor6.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Input OTP
        RcOTPInput(
            value = verificationCode,
            onValueChange = onCodeChange,
            modifier = Modifier.fillMaxWidth(),
            length = 6
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Botón Continuar
        RcButton(
            text = stringResource(R.string.common_continue),
            onClick = onVerifyCode,
            loading = isLoading,
            enabled = verificationCode.length == 6
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botón Reenviar
        TextButton(onClick = onResendCode) {
            Text(
                text = stringResource(R.string.verify_2fa_resend),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = RcColor5
            )
        }
    }
}

@Composable
private fun NewPasswordStep(
    newPassword: String,
    onNewPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    isLoading: Boolean,
    onResetPassword: () -> Unit
) {
    val passwordsMatch = newPassword == confirmPassword && newPassword.isNotEmpty()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Subtítulo
        Text(
            text = "Crea una nueva contraseña segura",
            style = MaterialTheme.typography.bodyLarge,
            color = RcColor6.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Campo Nueva Contraseña
        RcTextField(
            value = newPassword,
            onValueChange = onNewPasswordChange,
            label = stringResource(R.string.forgot_password_new_password),
            leadingIcon = Icons.Default.Lock,
            isPassword = true,
            keyboardType = KeyboardType.Password,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo Confirmar Contraseña
        RcTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = stringResource(R.string.forgot_password_confirm_new_password),
            leadingIcon = Icons.Default.Lock,
            isPassword = true,
            keyboardType = KeyboardType.Password,
            isError = confirmPassword.isNotEmpty() && !passwordsMatch,
            errorMessage = if (confirmPassword.isNotEmpty() && !passwordsMatch) {
                stringResource(R.string.validation_passwords_dont_match)
            } else null,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Botón Restablecer
        RcButton(
            text = stringResource(R.string.forgot_password_reset_button),
            onClick = onResetPassword,
            loading = isLoading,
            enabled = passwordsMatch && newPassword.length >= 6
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ForgotPasswordPreview() {
    RedcargaTheme(darkTheme = false) {
        ForgotPassword(
            onPasswordResetSuccess = {},
            onBackClick = {}
        )
    }
}
