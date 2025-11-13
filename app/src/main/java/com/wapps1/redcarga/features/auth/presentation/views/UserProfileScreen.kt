package com.wapps1.redcarga.features.auth.presentation.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wapps1.redcarga.core.session.AuthSessionStoreEntryPoint
import com.wapps1.redcarga.core.session.SessionState
import com.wapps1.redcarga.core.session.UserType
import com.wapps1.redcarga.core.ui.theme.*
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val store = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            AuthSessionStoreEntryPoint::class.java
        ).authSessionStore()
    }

    // Datos del store (bloqueo corto para demo hardcodeada)
    val sessionState = runBlocking { store.sessionState.value }
    val userType = runBlocking { store.currentUserType.value }
    val companyId = runBlocking { store.currentCompanyId.value }
    val username = runBlocking { store.currentUsername.value } ?: "Usuario"

    // Hardcode - switches locales
    var notificationsEnabled by remember { mutableStateOf(true) }
    var darkModeEnabled by remember { mutableStateOf(false) }
    var biometricEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFF3ED), Color(0xFFFDF7F5))
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        TopAppBar(
            title = { Text(text = "Perfil") },
            navigationIcon = { Icon(imageVector = Icons.Default.Settings, contentDescription = null) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Encabezado con avatar y nombre
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(3.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color(0xFFFFE0D7), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = username.take(1).uppercase(),
                            color = Color(0xFFFF6F4E),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    Spacer(modifier = Modifier.size(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = username, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            text = when (userType) {
                                UserType.CLIENT -> "Cliente"
                                UserType.PROVIDER -> "Proveedor"
                                null -> "Usuario"
                            },
                            color = RcColor5,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    TextButton(onClick = { /* editar perfil */ }) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.size(6.dp))
                        Text("Editar")
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 6.dp))

                // Datos principales (hardcode + store)
                InfoRow(label = "Usuario", value = username)
                InfoRow(label = "Tipo", value = userType?.name ?: "-")
                InfoRow(label = "Company Id", value = companyId?.toString() ?: "-")
                InfoRow(label = "Estado", value = when (sessionState) {
                    is SessionState.SignedOut -> "SignedOut"
                    is SessionState.FirebaseOnly -> "FirebaseOnly"
                    is SessionState.AppSignedIn -> "AppSignedIn"
                })
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Preferencias
        SectionCard(title = "Preferencias") {
            ToggleRow(
                title = "Notificaciones",
                subtitle = "Recibir avisos de nuevos tratos y mensajes",
                checked = notificationsEnabled,
                onCheckedChange = { notificationsEnabled = it }
            )
            ToggleRow(
                title = "Modo oscuro",
                subtitle = "Ajustar apariencia del sistema",
                checked = darkModeEnabled,
                onCheckedChange = { darkModeEnabled = it }
            )
            ToggleRow(
                title = "Biometría",
                subtitle = "Usar huella para acceder más rápido",
                checked = biometricEnabled,
                onCheckedChange = { biometricEnabled = it }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Configuraciones de cuenta
        SectionCard(title = "Cuenta") {
            ActionRow(title = "Cambiar contraseña")
            ActionRow(title = "Métodos de inicio de sesión")
            ActionRow(title = "Privacidad y seguridad")
            ActionRow(title = "Borrar datos locales")
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Soporte
        SectionCard(title = "Soporte") {
            ActionRow(title = "Centro de ayuda")
            ActionRow(title = "Términos y condiciones")
            ActionRow(title = "Acerca de Red Carga")
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Logout elegante
        Card(
            colors = CardDefaults.cardColors(containerColor = RcColor5),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.size(8.dp))
                Text("Cerrar sesión", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = RcColor6)
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = RcColor8)
        Text(text = value, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ToggleRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Medium)
            Text(text = subtitle, color = RcColor8)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = RcColor5, checkedThumbColor = Color.White)
        )
    }
}

@Composable
private fun ActionRow(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title)
        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = RcColor8)
    }
}


