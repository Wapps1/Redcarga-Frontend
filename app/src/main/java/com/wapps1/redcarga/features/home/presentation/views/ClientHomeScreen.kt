package com.wapps1.redcarga.features.home.presentation.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wapps1.redcarga.R
import com.wapps1.redcarga.core.session.AuthSessionStore
import com.wapps1.redcarga.core.session.AuthSessionStoreEntryPoint
import com.wapps1.redcarga.core.ui.theme.*
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.FileInvoiceDollar
import compose.icons.fontawesomeicons.solid.Handshake
import compose.icons.fontawesomeicons.solid.Truck
import compose.icons.fontawesomeicons.solid.MapMarkedAlt
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch


@Composable
fun ClientHomeScreen(
    onNavigateToRequests: () -> Unit = {},
    onNavigateToCreateRequest: () -> Unit = {}
) {
    val context = LocalContext.current
    val authStore = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            AuthSessionStoreEntryPoint::class.java
        ).authSessionStore()
    }
    val scope = rememberCoroutineScope()
    
    // Observar el username del usuario
    val username by authStore.currentUsername.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(RcColor1)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header con saludo y botón de logout
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.home_greeting_client, username ?: "Usuario"),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = RcColor6
                )

                // Botón de logout compacto y elegante
                Surface(
                    onClick = {
                        scope.launch {
                            authStore.logout()
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = RcColor5,
                    shadowElevation = 2.dp,
                    modifier = Modifier.height(40.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = null,
                            tint = White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.common_logout),
                            color = White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Sección: Últimos tratos activos
        item {
            ActiveDealsSection()
        }

        // Sección: Acciones principales
        item {
            ClientActionsSection(
                onNavigateToCreateRequest = onNavigateToCreateRequest
            )
        }

        // Sección: Tus Solicitudes
        item {
            YourRequestsSection(
                onNavigateToRequests = onNavigateToRequests
            )
        }

        // Sección: Tus Cotizaciones
        item {
            YourQuotesSection()
        }

        // Sección: Tus Tratos
        item {
            YourDealsSection()
        }
    }
}

/**
 * Sección de tratos activos con cards horizontales
 * Colores vibrantes con gradientes sutiles
 */
@Composable
private fun ActiveDealsSection() {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.client_home_active_deals),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = RcColor6
            )
            Text(
                text = stringResource(R.string.client_home_see_more),
                fontSize = 13.sp,
                color = RcColor5,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            repeat(3) { index ->
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = when (index) {
                        0 -> RcColor2 // Salmón claro
                        1 -> RcColor4 // Coral vibrante
                        else -> RcColor3 // Rosa suave
                    },
                    shadowElevation = 4.dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.client_home_deal_title),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Sección de acciones principales para clientes
 * Botones con colores suaves y elegantes
 */
@Composable
private fun ClientActionsSection(
    onNavigateToCreateRequest: () -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.client_home_actions),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = RcColor6
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ClientActionButton(
                modifier = Modifier.weight(1f),
                text = stringResource(R.string.client_home_make_request),
                icon = FontAwesomeIcons.Solid.FileInvoiceDollar,
                color = RcColor7,
                iconColor = RcColor5,
                onClick = onNavigateToCreateRequest
            )
            ClientActionButton(
                modifier = Modifier.weight(1f),
                text = stringResource(R.string.client_home_view_quotes),
                icon = FontAwesomeIcons.Solid.Handshake,
                color = RcColor7,
                iconColor = RcColor4,
                onClick = { /* TODO: Navigate to quotes */ }
            )
            ClientActionButton(
                modifier = Modifier.weight(1f),
                text = stringResource(R.string.client_home_track_shipment),
                icon = FontAwesomeIcons.Solid.MapMarkedAlt,
                color = RcColor7,
                iconColor = RcColor3,
                onClick = { /* TODO: Navigate to tracking */ }
            )
        }
    }
}

/**
 * Botón de acción para clientes
 */
@Composable
private fun ClientActionButton(
    modifier: Modifier = Modifier,
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        color = color,
        onClick = onClick,
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = iconColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = RcColor6,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}

/**
 * Sección: Tus Solicitudes
 * Card con fondo blanco y detalles coloridos
 */
@Composable
private fun YourRequestsSection(
    onNavigateToRequests: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = stringResource(R.string.client_home_your_requests),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = RcColor6
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Solicitud de ejemplo
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = RcColor7,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.client_home_request_title),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = RcColor6
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.client_home_request_id),
                            fontSize = 13.sp,
                            color = RcColor8,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Estado de la solicitud
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = RcColor5.copy(alpha = 0.1f),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.client_home_request_status),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = RcColor5,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onNavigateToRequests,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RcColor5
                ),
                shape = RoundedCornerShape(28.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = stringResource(R.string.client_home_view_requests),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * Sección: Tus Cotizaciones
 * Card con info de cotizaciones destacada
 */
@Composable
private fun YourQuotesSection() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = stringResource(R.string.client_home_your_quotes),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = RcColor6
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = RcColor7,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.client_home_quote_provider),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = RcColor6
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.client_home_quote_price),
                            fontSize = 13.sp,
                            color = RcColor8,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Precio destacado
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = RcColor4.copy(alpha = 0.1f),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.client_home_quote_amount),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = RcColor4,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = { /* TODO: Navigate to quotes */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RcColor4
                ),
                shape = RoundedCornerShape(28.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = stringResource(R.string.client_home_view_quotes),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * Sección: Tus Tratos
 * Card con info del trato destacada
 */
@Composable
private fun YourDealsSection() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = stringResource(R.string.client_home_your_deals),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = RcColor6
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = RcColor7,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.client_home_deal_provider),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = RcColor6
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.client_home_deal_route),
                            fontSize = 13.sp,
                            color = RcColor8,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Estado del trato
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = RcColor3.copy(alpha = 0.1f),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.client_home_deal_status),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = RcColor3,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = { /* TODO: Navigate to deals */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RcColor3
                ),
                shape = RoundedCornerShape(28.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = stringResource(R.string.client_home_view_deals),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

