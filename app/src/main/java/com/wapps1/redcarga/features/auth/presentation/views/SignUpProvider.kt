package com.wapps1.redcarga.features.auth.presentation.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.wapps1.redcarga.core.ui.components.*
import com.wapps1.redcarga.core.ui.theme.RcColor6
import com.wapps1.redcarga.core.ui.theme.RedcargaTheme

@Composable
fun SignUpProvider(
    onSignUpSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    var currentStep by remember { mutableStateOf(1) }
    var ruc by remember { mutableStateOf("") }
    var businessName by remember { mutableStateOf("") }
    var legalDni by remember { mutableStateOf("") }
    var legalName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        RcBackground(Modifier.matchParentSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                RcBackButton(
                    onClick = {
                        if (currentStep == 1) onBackClick()
                        else currentStep--
                    },
                    modifier = Modifier.align(Alignment.CenterStart)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Step Indicator
            RcStepIndicator(
                currentStep = currentStep,
                totalSteps = 5,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Contenido scrolleable
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icono
                Image(
                    painter = painterResource(R.drawable.ic_cargo_truck),
                    contentDescription = null,
                    modifier = Modifier.size(100.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                when (currentStep) {
                    1 -> Step1Company(ruc, { ruc = it }, businessName, { businessName = it })
                    2 -> Step2Representative(legalDni, { legalDni = it }, legalName, { legalName = it })
                    3 -> Step3Contact(phone, { phone = it }, email, { email = it }, address, { address = it })
                    4 -> Step4PIN(pin, { pin = it }, confirmPin, { confirmPin = it })
                    5 -> Step5Verification(phone, verificationCode, { verificationCode = it })
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            // Botón
            RcButton(
                text = if (currentStep == 5) 
                    stringResource(R.string.signup_finish_button) 
                else 
                    stringResource(R.string.signup_next_button),
                onClick = {
                    if (currentStep == 5) {
                        isLoading = true
                        onSignUpSuccess()
                    } else {
                        currentStep++
                    }
                },
                loading = isLoading,
                enabled = when (currentStep) {
                    1 -> ruc.length == 11 && businessName.isNotEmpty()
                    2 -> legalDni.length == 8 && legalName.isNotEmpty()
                    3 -> phone.length == 9 && email.isNotEmpty() && address.isNotEmpty()
                    4 -> pin.length == 6 && pin == confirmPin
                    5 -> verificationCode.length == 6
                    else -> false
                }
            )
        }
    }
}

@Composable
private fun Step1Company(
    ruc: String,
    onRucChange: (String) -> Unit,
    businessName: String,
    onBusinessNameChange: (String) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.signup_provider_step1_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = RcColor6,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.signup_provider_step1_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = RcColor6.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        RcTextField(
            value = ruc,
            onValueChange = { if (it.length <= 11 && it.all { c -> c.isDigit() }) onRucChange(it) },
            label = stringResource(R.string.signup_provider_ruc_label),
            leadingIcon = Icons.Default.Business,
            keyboardType = KeyboardType.Number,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        RcTextField(
            value = businessName,
            onValueChange = onBusinessNameChange,
            label = stringResource(R.string.signup_provider_business_name_label),
            leadingIcon = Icons.Default.Business,
            keyboardType = KeyboardType.Text,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun Step2Representative(
    legalDni: String,
    onLegalDniChange: (String) -> Unit,
    legalName: String,
    onLegalNameChange: (String) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.signup_provider_step2_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = RcColor6,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.signup_provider_step2_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = RcColor6.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        RcTextField(
            value = legalDni,
            onValueChange = { if (it.length <= 8 && it.all { c -> c.isDigit() }) onLegalDniChange(it) },
            label = stringResource(R.string.signup_provider_legal_dni_label),
            leadingIcon = Icons.Default.Badge,
            keyboardType = KeyboardType.Number,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        RcTextField(
            value = legalName,
            onValueChange = onLegalNameChange,
            label = stringResource(R.string.signup_provider_legal_name_label),
            leadingIcon = Icons.Default.Person,
            keyboardType = KeyboardType.Text,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun Step3Contact(
    phone: String,
    onPhoneChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    address: String,
    onAddressChange: (String) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.signup_provider_step3_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = RcColor6,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.signup_provider_step3_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = RcColor6.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        RcTextField(
            value = phone,
            onValueChange = { if (it.length <= 9 && it.all { c -> c.isDigit() }) onPhoneChange(it) },
            label = stringResource(R.string.signup_provider_phone_label),
            leadingIcon = Icons.Default.Phone,
            keyboardType = KeyboardType.Phone,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        RcTextField(
            value = email,
            onValueChange = onEmailChange,
            label = stringResource(R.string.signup_provider_email_label),
            leadingIcon = Icons.Default.Email,
            keyboardType = KeyboardType.Email,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        RcTextField(
            value = address,
            onValueChange = onAddressChange,
            label = stringResource(R.string.signup_provider_address_label),
            leadingIcon = Icons.Default.Home,
            keyboardType = KeyboardType.Text,
            maxLines = 2,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun Step4PIN(
    pin: String,
    onPinChange: (String) -> Unit,
    confirmPin: String,
    onConfirmPinChange: (String) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.signup_provider_step4_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = RcColor6,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.signup_provider_step4_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = RcColor6.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        RcOTPInput(
            value = pin,
            onValueChange = onPinChange,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        RcOTPInput(
            value = confirmPin,
            onValueChange = onConfirmPinChange,
            modifier = Modifier.fillMaxWidth(),
            isError = confirmPin.isNotEmpty() && pin != confirmPin
        )
    }
}

@Composable
private fun Step5Verification(
    phone: String,
    code: String,
    onCodeChange: (String) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.signup_provider_step5_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = RcColor6,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.signup_provider_step5_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = RcColor6.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        RcOTPInput(
            value = code,
            onValueChange = onCodeChange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SignUpProviderPreview() {
    RedcargaTheme(darkTheme = false) {
        SignUpProvider(
            onSignUpSuccess = {},
            onBackClick = {}
        )
    }
}
