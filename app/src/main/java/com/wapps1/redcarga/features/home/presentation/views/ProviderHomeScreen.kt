package com.wapps1.redcarga.features.home.presentation.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wapps1.redcarga.R
import com.wapps1.redcarga.core.ui.theme.*
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Truck
import compose.icons.fontawesomeicons.solid.Route
import compose.icons.fontawesomeicons.solid.UserTie

/**
 * Pantalla de inicio para Proveedor
 * Muestra resumen de tratos activos, acciones rápidas y resumen de recursos (rutas, conductores, flotas)
 * Diseño mejorado con colores que contrastan hermosamente
 */
@Composable
fun ProviderHomeScreen(
    onNavigateToRoutes: () -> Unit = {},
    onNavigateToDrivers: () -> Unit = {},
    onNavigateToFleet: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(RcColor1) // Fondo suave beige claro
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Saludo de bienvenida
        item {
            Text(
                text = stringResource(R.string.home_welcome),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = RcColor6 // Negro oscuro para contraste
            )
        }

        // Sección: Últimos tratos activos
        item {
            ActiveDealsSection()
        }

        // Sección: Acciones (Administrar Rutas, Conductores, Flotas)
        item {
            ActionsSection(
                onNavigateToRoutes = onNavigateToRoutes,
                onNavigateToDrivers = onNavigateToDrivers,
                onNavigateToFleet = onNavigateToFleet
            )
        }

        // Sección: Tus Rutas
        item {
            YourRoutesSection(onViewRoute = onNavigateToRoutes)
        }

        // Sección: Tus Conductores
        item {
            YourDriversSection(onViewDrivers = onNavigateToDrivers)
        }

        // Sección: Tus Flotas
        item {
            YourFleetSection(onViewFleet = onNavigateToFleet)
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
                text = stringResource(R.string.provider_home_active_deals),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = RcColor6
            )
            Text(
                text = stringResource(R.string.provider_home_see_more),
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
                            text = stringResource(R.string.provider_home_company),
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
 * Sección de acciones (Administrar Rutas, Conductores, Flotas)
 * Botones con colores suaves y elegantes
 */
@Composable
private fun ActionsSection(
    onNavigateToRoutes: () -> Unit,
    onNavigateToDrivers: () -> Unit,
    onNavigateToFleet: () -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.provider_home_actions),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = RcColor6
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ActionButton(
                modifier = Modifier.weight(1f),
                text = stringResource(R.string.provider_home_manage_routes),
                icon = FontAwesomeIcons.Solid.Route,
                color = RcColor7, // Beige suave
                iconColor = RcColor5, // Ícono coral
                onClick = onNavigateToRoutes
            )
            ActionButton(
                modifier = Modifier.weight(1f),
                text = stringResource(R.string.provider_home_manage_drivers),
                icon = FontAwesomeIcons.Solid.UserTie,
                color = RcColor7,
                iconColor = RcColor4,
                onClick = onNavigateToDrivers
            )
            ActionButton(
                modifier = Modifier.weight(1f),
                text = stringResource(R.string.provider_home_manage_fleet),
                icon = FontAwesomeIcons.Solid.Truck,
                color = RcColor7,
                iconColor = RcColor3,
                onClick = onNavigateToFleet
            )
        }
    }
}

/**
 * Botón de acción reutilizable
 * Con íconos coloridos y sombras suaves
 */
@Composable
private fun ActionButton(
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
 * Sección: Tus Rutas
 * Card con fondo blanco y detalles coloridos
 */
@Composable
private fun YourRoutesSection(onViewRoute: () -> Unit) {
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
                text = stringResource(R.string.provider_home_your_routes),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = RcColor6
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Origen
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = RcColor5.copy(alpha = 0.1f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = RcColor5,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.provider_home_route_origin),
                    fontSize = 15.sp,
                    color = RcColor6,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Destino
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = RcColor4.copy(alpha = 0.1f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = RcColor4,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.provider_home_route_destination),
                    fontSize = 15.sp,
                    color = RcColor6,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onViewRoute,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RcColor5
                ),
                shape = RoundedCornerShape(28.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = stringResource(R.string.provider_home_view_route),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * Sección: Tus Conductores
 * Card con info del conductor destacada
 */
@Composable
private fun YourDriversSection(onViewDrivers: () -> Unit) {
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
                text = stringResource(R.string.provider_home_your_drivers),
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
                    Column {
                        Text(
                            text = stringResource(R.string.provider_home_driver_name),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = RcColor6
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.provider_home_driver_dni),
                            fontSize = 13.sp,
                            color = RcColor8,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onViewDrivers,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RcColor4
                ),
                shape = RoundedCornerShape(28.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = stringResource(R.string.provider_home_view_drivers),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * Sección: Tus Flotas
 * Card con info del vehículo destacada
 */
@Composable
private fun YourFleetSection(onViewFleet: () -> Unit) {
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
                text = stringResource(R.string.provider_home_your_fleet),
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
                    Column {
                        Text(
                            text = stringResource(R.string.provider_home_vehicle_name),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = RcColor6
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.provider_home_vehicle_plate),
                            fontSize = 13.sp,
                            color = RcColor8,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onViewFleet,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RcColor3
                ),
                shape = RoundedCornerShape(28.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = stringResource(R.string.provider_home_view_fleet),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

