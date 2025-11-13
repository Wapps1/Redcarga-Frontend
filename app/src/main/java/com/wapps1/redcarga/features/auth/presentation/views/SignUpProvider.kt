package com.wapps1.redcarga.features.auth.presentation.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import android.content.Intent
import android.net.Uri
import com.wapps1.redcarga.R
import com.wapps1.redcarga.core.ui.components.*
import com.wapps1.redcarga.core.ui.theme.RcColor5
import com.wapps1.redcarga.core.ui.theme.RcColor6
import com.wapps1.redcarga.core.ui.theme.RedcargaTheme
import com.wapps1.redcarga.features.auth.presentation.viewmodels.SignUpProviderViewModel

@Composable
fun SignUpProvider(
    onNavigateToMain: () -> Unit,
    onBackClick: () -> Unit
) {
    val vm: SignUpProviderViewModel = hiltViewModel()
    val ui by vm.ui.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        vm.effect.collect { eff ->
            when (eff) {
                is SignUpProviderViewModel.Effect.OpenUrl -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(eff.url))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
                SignUpProviderViewModel.Effect.NavigateToMain -> onNavigateToMain()
            }
        }
    }

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
                        if (ui.step == 1) onBackClick() else vm.onBack()
                    },
                    modifier = Modifier.align(Alignment.CenterStart)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Step Indicator
            RcStepIndicator(
                currentStep = ui.step,
                totalSteps = 4,
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

                when (ui.step) {
                    1 -> Step1Credentials(
                        email = ui.email,
                        onEmailChange = { vm.updateEmail(it) },
                        username = ui.username,
                        onUsernameChange = { vm.updateUsername(it) },
                        password = ui.password,
                        onPasswordChange = { vm.updatePassword(it) },
                        confirmPassword = ui.confirmPassword,
                        onConfirmPasswordChange = { vm.updateConfirmPassword(it) }
                    )
                    2 -> Step2EmailVerification(
                        email = ui.email,
                        verificationLink = ui.verificationLink,
                        emailVerified = ui.emailVerified,
                        onOpenLink = { vm.onOpenVerificationLink() },
                        onConfirmVerified = { vm.onConfirmEmailVerified() }
                    )
                    3 -> Step3PersonalData(
                        fullName = ui.fullName,
                        onFullNameChange = { vm.updateFullName(it) },
                        phone = ui.phone,
                        onPhoneChange = { vm.updatePhone(it) },
                        birthDate = ui.birthDate,
                        onBirthDateChange = { vm.updateBirthDate(it) },
                        documentType = ui.documentType,
                        onDocumentTypeChange = { vm.updateDocumentType(it) },
                        documentNumber = ui.documentNumber,
                        onDocumentNumberChange = { vm.updateDocumentNumber(it) },
                        ruc = ui.ruc,
                        onRucChange = { vm.updateRuc(it) }
                    )
                    4 -> Step4CompanyData(
                        legalName = ui.legalName,
                        onLegalNameChange = { vm.updateLegalName(it) },
                        commercialName = ui.commercialName,
                        onCommercialNameChange = { vm.updateCommercialName(it) },
                        companyRuc = ui.companyRuc,
                        onCompanyRucChange = { vm.updateCompanyRuc(it) },
                        companyEmail = ui.companyEmail,
                        onCompanyEmailChange = { vm.updateCompanyEmail(it) },
                        companyPhone = ui.companyPhone,
                        onCompanyPhoneChange = { vm.updateCompanyPhone(it) },
                        address = ui.address,
                        onAddressChange = { vm.updateAddress(it) }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            // Botón
            RcButton(
                text = when (ui.step) {
                    1 -> stringResource(R.string.signup_register_button)
                    2 -> if (ui.emailVerified) stringResource(R.string.signup_continue_button) else stringResource(R.string.signup_verify_email_button)
                    3 -> stringResource(R.string.signup_next_button)
                    4 -> stringResource(R.string.signup_finish_button)
                    else -> stringResource(R.string.signup_next_button)
                },
                onClick = {
                    when (ui.step) {
                        1 -> {
                            vm.onRegisterStart()
                        }
                        2 -> {
                            if (!ui.emailVerified) vm.onOpenVerificationLink() else vm.onConfirmEmailVerified()
                        }
                        3 -> {
                            vm.onVerifyPerson()
                        }
                        4 -> {
                            vm.onRegisterCompanyAndLogin()
                        }
                    }
                },
                loading = ui.loading,
                enabled = when (ui.step) {
                    1 -> {
                        ui.email.isNotEmpty() &&
                                ui.username.isNotEmpty() &&
                                ui.password.length >= 8 &&
                                ui.password == ui.confirmPassword &&
                                android.util.Patterns.EMAIL_ADDRESS.matcher(ui.email).matches()
                    }
                    2 -> true
                    3 -> {
                        ui.fullName.isNotEmpty() &&
                                ui.phone.length == 9 &&
                                ui.birthDate.length == 10 &&
                                ui.documentType.isNotEmpty() &&
                                ui.documentNumber.isNotEmpty() &&
                                ui.ruc.isNotEmpty()
                    }
                    4 -> {
                        ui.legalName.isNotEmpty() &&
                                ui.commercialName.isNotEmpty() &&
                                ui.companyRuc.length == 11 &&
                                ui.companyEmail.isNotEmpty() &&
                                ui.companyPhone.length == 9 &&
                                ui.address.isNotEmpty() &&
                                android.util.Patterns.EMAIL_ADDRESS.matcher(ui.companyEmail).matches()
                    }
                    else -> false
                }
            )
        }
    }
}

@Composable
private fun Step1Credentials(
    email: String,
    onEmailChange: (String) -> Unit,
    username: String,
    onUsernameChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit
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
            value = email,
            onValueChange = onEmailChange,
            label = stringResource(R.string.signup_client_email_label),
            leadingIcon = Icons.Default.Email,
            keyboardType = KeyboardType.Email,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        RcTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = stringResource(R.string.signup_client_username_label),
            leadingIcon = Icons.Default.Person,
            keyboardType = KeyboardType.Text,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        RcTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = stringResource(R.string.signup_client_password_label),
            leadingIcon = Icons.Default.Lock,
            isPassword = true,
            keyboardType = KeyboardType.Password,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        RcTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = stringResource(R.string.signup_client_confirm_password_label),
            leadingIcon = Icons.Default.Lock,
            isPassword = true,
            keyboardType = KeyboardType.Password,
            isError = confirmPassword.isNotEmpty() && password != confirmPassword,
            errorMessage = if (confirmPassword.isNotEmpty() && password != confirmPassword)
                stringResource(R.string.validation_passwords_dont_match)
            else null,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun Step2EmailVerification(
    email: String,
    verificationLink: String,
    emailVerified: Boolean,
    onOpenLink: () -> Unit,
    onConfirmVerified: () -> Unit
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

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = email,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = RcColor5,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (emailVerified) {
            // Email verificado - mostrar éxito
            Text(
                text = stringResource(R.string.signup_client_email_verified),
                style = MaterialTheme.typography.bodyLarge,
                color = RcColor5,
                textAlign = TextAlign.Center
            )
        } else {
            // Abrir enlace de verificación
            TextButton(onClick = onOpenLink) {
                Text(
                    text = stringResource(R.string.signup_client_verify_email_button),
                    color = RcColor5,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Confirmar que ya se verificó el correo
            TextButton(onClick = onConfirmVerified) {
                Text(
                    text = stringResource(R.string.signup_continue_button),
                    color = RcColor5,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun Step3PersonalData(
    fullName: String,
    onFullNameChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    birthDate: String,
    onBirthDateChange: (String) -> Unit,
    documentType: String,
    onDocumentTypeChange: (String) -> Unit,
    documentNumber: String,
    onDocumentNumberChange: (String) -> Unit,
    ruc: String,
    onRucChange: (String) -> Unit
) {
    val documentTypes = listOf(
        stringResource(R.string.document_type_dni),
        stringResource(R.string.document_type_passport),
        stringResource(R.string.document_type_ce),
        stringResource(R.string.document_type_ruc)
    )

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
            value = fullName,
            onValueChange = onFullNameChange,
            label = stringResource(R.string.signup_client_full_name_label),
            leadingIcon = Icons.Default.Person,
            keyboardType = KeyboardType.Text,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        RcTextField(
            value = phone,
            onValueChange = { if (it.length <= 9 && it.all { c -> c.isDigit() }) onPhoneChange(it) },
            label = stringResource(R.string.signup_client_phone_label),
            leadingIcon = Icons.Default.Phone,
            keyboardType = KeyboardType.Phone,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        RcDatePickerField(
            value = birthDate,
            onValueChange = onBirthDateChange,
            label = stringResource(R.string.signup_client_birth_date_label),
            leadingIcon = Icons.Default.CalendarToday,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        RcDropdown(
            value = documentType,
            onValueChange = onDocumentTypeChange,
            label = stringResource(R.string.signup_client_document_type_label),
            options = documentTypes,
            leadingIcon = Icons.Default.Badge,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        RcTextField(
            value = documentNumber,
            onValueChange = {
                // Limit based on document type
                val maxLength = when (documentType) {
                    documentTypes[0] -> 8  // DNI
                    documentTypes[3] -> 11 // RUC
                    else -> 20 // Passport, CE
                }
                if (it.length <= maxLength) onDocumentNumberChange(it)
            },
            label = stringResource(R.string.signup_client_document_number_label),
            keyboardType = KeyboardType.Text,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        RcTextField(
            value = ruc,
            onValueChange = { if (it.length <= 11 && it.all { c -> c.isDigit() }) onRucChange(it) },
            label = stringResource(R.string.signup_client_ruc_label),
            keyboardType = KeyboardType.Number,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun Step4CompanyData(
    legalName: String,
    onLegalNameChange: (String) -> Unit,
    commercialName: String,
    onCommercialNameChange: (String) -> Unit,
    companyRuc: String,
    onCompanyRucChange: (String) -> Unit,
    companyEmail: String,
    onCompanyEmailChange: (String) -> Unit,
    companyPhone: String,
    onCompanyPhoneChange: (String) -> Unit,
    address: String,
    onAddressChange: (String) -> Unit
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

        RcTextField(
            value = legalName,
            onValueChange = onLegalNameChange,
            label = stringResource(R.string.signup_provider_legal_name_label),
            leadingIcon = Icons.Default.Business,
            keyboardType = KeyboardType.Text,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        RcTextField(
            value = commercialName,
            onValueChange = onCommercialNameChange,
            label = stringResource(R.string.signup_provider_commercial_name_label),
            leadingIcon = Icons.Default.Business,
            keyboardType = KeyboardType.Text,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        RcTextField(
            value = companyRuc,
            onValueChange = { if (it.length <= 11 && it.all { c -> c.isDigit() }) onCompanyRucChange(it) },
            label = stringResource(R.string.signup_provider_ruc_label),
            leadingIcon = Icons.Default.Badge,
            keyboardType = KeyboardType.Number,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        RcTextField(
            value = companyEmail,
            onValueChange = onCompanyEmailChange,
            label = stringResource(R.string.signup_provider_company_email_label),
            leadingIcon = Icons.Default.Email,
            keyboardType = KeyboardType.Email,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        RcTextField(
            value = companyPhone,
            onValueChange = { if (it.length <= 9 && it.all { c -> c.isDigit() }) onCompanyPhoneChange(it) },
            label = stringResource(R.string.signup_provider_company_phone_label),
            leadingIcon = Icons.Default.Phone,
            keyboardType = KeyboardType.Phone,
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
