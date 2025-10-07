package com.wapps1.redcarga.core.navigation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import com.wapps1.redcarga.core.ui.components.RcBackground
import com.wapps1.redcarga.core.ui.theme.RcColor6

@Composable
fun ProviderHomeScaffold(
    navController: NavHostController,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = Modifier.fillMaxSize()) {
        RcBackground(Modifier.matchParentSize())
        Text(
            text = "Hola proveedor",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = RcColor6,
            modifier = Modifier.align(Alignment.Center)
        )

        Button(
            onClick = onLogout,
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
        ) {
            Text(text = "Cerrar sesión")
        }
    }
}