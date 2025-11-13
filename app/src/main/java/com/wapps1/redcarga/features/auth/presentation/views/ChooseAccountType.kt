package com.wapps1.redcarga.features.auth.presentation.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
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
import com.wapps1.redcarga.core.ui.theme.RcColor5
import com.wapps1.redcarga.core.ui.theme.RcColor6
import com.wapps1.redcarga.core.ui.theme.RedcargaTheme

@Composable
fun ChooseAccountType(
    onClientSelected: () -> Unit,
    onProviderSelected: () -> Unit,
    onBackClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        RcBackground(Modifier.matchParentSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 48.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
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

            Spacer(modifier = Modifier.weight(0.5f))

            // Título
            Text(
                text = stringResource(R.string.choose_account_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = RcColor6,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Card Cliente
            AccountTypeCard(
                icon = R.drawable.ic_worker_happy,
                title = stringResource(R.string.account_type_client),
                description = stringResource(R.string.account_type_client_desc),
                onClick = onClientSelected
            )

            // Card Proveedor
            AccountTypeCard(
                icon = R.drawable.ic_cargo_truck,
                title = stringResource(R.string.account_type_provider),
                description = stringResource(R.string.account_type_provider_desc),
                onClick = onProviderSelected
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun AccountTypeCard(
    icon: Int,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(200.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = RcColor5.copy(alpha = 0.25f)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.98f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Icono - Usar Image en lugar de Icon para preservar colores
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(100.dp)
            )

            // Contenido de texto
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // Título
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = RcColor6,
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Descripción
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = RcColor6.copy(alpha = 0.7f),
                    textAlign = TextAlign.Start,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                )
            }
        }
    }
}
