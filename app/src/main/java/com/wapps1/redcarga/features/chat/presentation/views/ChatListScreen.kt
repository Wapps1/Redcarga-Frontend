package com.wapps1.redcarga.features.chat.presentation.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wapps1.redcarga.core.ui.theme.*
import com.wapps1.redcarga.features.chat.domain.models.ChatSummary
import com.wapps1.redcarga.features.chat.presentation.viewmodels.ChatListViewModel
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Comments
import compose.icons.fontawesomeicons.solid.Image
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToChat: (Long) -> Unit = {}, // quoteId
    viewModel: ChatListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Chats",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshChatList() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RcColor5,
                    titleContentColor = White,
                    navigationIconContentColor = White,
                    actionIconContentColor = White
                )
            )
        },
        containerColor = RcColor1
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(RcColor1)
        ) {
            when (uiState) {
                is ChatListViewModel.UiState.Loading -> {
                    LoadingContent()
                }
                is ChatListViewModel.UiState.Error -> {
                    ErrorContent(
                        message = (uiState as ChatListViewModel.UiState.Error).message,
                        onRetry = { viewModel.refreshChatList() }
                    )
                }
                is ChatListViewModel.UiState.Success -> {
                    val chats = (uiState as ChatListViewModel.UiState.Success).chats
                    ChatListContent(
                        chats = chats,
                        onChatClick = { chat -> onNavigateToChat(chat.quoteId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = RcColor5)
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = RcColor5,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Error",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = RcColor6
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            fontSize = 14.sp,
            color = RcColor8,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = RcColor5)
        ) {
            Text("Reintentar")
        }
    }
}

@Composable
private fun ChatListContent(
    chats: List<ChatSummary>,
    onChatClick: (ChatSummary) -> Unit
) {
    if (chats.isEmpty()) {
        EmptyChatsContent()
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(chats) { chat ->
                ChatCard(
                    chat = chat,
                    onClick = { onChatClick(chat) }
                )
            }
        }
    }
}

@Composable
private fun EmptyChatsContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = FontAwesomeIcons.Solid.Comments,
                contentDescription = null,
                tint = RcColor8.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "No tienes chats activos",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = RcColor8
            )
            Text(
                text = "Cuando tengas cotizaciones en negociación,\naparecerán aquí",
                fontSize = 14.sp,
                color = RcColor8.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun ChatCard(
    chat: ChatSummary,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar/Icono
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(12.dp),
                color = RcColor5.copy(alpha = 0.1f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = FontAwesomeIcons.Solid.Comments,
                        contentDescription = null,
                        tint = RcColor5,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Contenido
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Header con quoteId y fecha
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cotización #${chat.quoteId}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = RcColor6
                    )
                    Text(
                        text = formatRelativeTime(chat.getLastActivityDate()),
                        fontSize = 12.sp,
                        color = RcColor8,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Último mensaje
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (chat.lastMessage != null) {
                        if (chat.lastMessage.isImageMessage()) {
                            Icon(
                                imageVector = FontAwesomeIcons.Solid.Image,
                                contentDescription = null,
                                tint = RcColor8.copy(alpha = 0.6f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = chat.getLastMessagePreview(),
                            fontSize = 14.sp,
                            color = RcColor8,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Text(
                            text = "Sin mensajes",
                            fontSize = 14.sp,
                            color = RcColor8.copy(alpha = 0.6f),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }

                // Footer con monto y badge de no leídos
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${chat.currencyCode} ${String.format("%.2f", chat.totalAmount)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = RcColor5
                    )
                    if (chat.hasUnreadMessages()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = RcColor5
                        ) {
                            Text(
                                text = "${chat.unreadCount}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Formatea una fecha como tiempo relativo (hace X minutos/horas/días)
 */
private fun formatRelativeTime(instant: java.time.Instant): String {
    val now = java.time.Instant.now()
    val duration = java.time.Duration.between(instant, now)
    
    return when {
        duration.toMinutes() < 1 -> "Ahora"
        duration.toMinutes() < 60 -> "Hace ${duration.toMinutes()} min"
        duration.toHours() < 24 -> "Hace ${duration.toHours()} h"
        duration.toDays() < 7 -> "Hace ${duration.toDays()} d"
        else -> {
            val date = Date.from(instant)
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
        }
    }
}

