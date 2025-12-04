package com.wapps1.redcarga.features.chat.presentation.views

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.wapps1.redcarga.core.session.UserType
import com.wapps1.redcarga.core.ui.theme.*
import com.wapps1.redcarga.features.chat.domain.models.ChatMessage
import com.wapps1.redcarga.features.chat.presentation.viewmodels.ChatViewModel
import com.wapps1.redcarga.features.requests.domain.models.QuoteDetail
import com.wapps1.redcarga.features.requests.domain.models.QuoteItem
import com.wapps1.redcarga.features.requests.domain.models.Request
import com.wapps1.redcarga.features.requests.domain.models.RequestItem
import com.wapps1.redcarga.features.requests.domain.models.RequestImage
import com.wapps1.redcarga.features.deals.domain.models.ChangeItem
import com.wapps1.redcarga.features.deals.domain.models.ChangeFieldCode
import com.wapps1.redcarga.features.deals.domain.models.ChangeStatus
import com.wapps1.redcarga.features.deals.domain.models.ApplyChangeRequest
import java.math.BigDecimal
import java.math.RoundingMode
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import android.util.Log

/**
 * Enum para las pestañas del chat
 */
enum class ChatTab {
    CHAT,
    INFO
}

/**
 * Formatea un BigDecimal para que siempre muestre al menos un decimal
 * Ejemplos: 15 -> "15.00", 10.5 -> "10.50", 10.567 -> "10.57"
 */
private fun BigDecimal.toFormattedString(): String {
    // Usar setScale + toPlainString para evitar problemas de locale (siempre usa '.')
    return this.setScale(2, RoundingMode.HALF_UP).toPlainString()
}

@Composable
fun ChatScreen(
    quoteId: Long,
    onNavigateBack: () -> Unit = {},
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val messageText by viewModel.messageText.collectAsState()
    val isSendingMessage by viewModel.isSendingMessage.collectAsState()
    val selectedImageUri by viewModel.selectedImageUri.collectAsState()
    val imageCaption by viewModel.imageCaption.collectAsState()
    val imageUploadState by viewModel.imageUploadState.collectAsState()

    // ⭐ QUOTE: Detalles de la cotización asociada al chat
    val quoteDetail by viewModel.quoteDetail.collectAsState()
    
    // ⭐ REQUEST: Detalles de la solicitud asociada al chat
    val requestDetail by viewModel.requestDetail.collectAsState()
    
    // ⭐ USER: Rol del usuario actualmente logueado
    val currentUserType by viewModel.currentUserType.collectAsState()

    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf(ChatTab.CHAT) }

    var showFullscreenImage by remember { mutableStateOf(false) }
    var fullscreenImageUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(quoteId) {
        selectedTab = ChatTab.CHAT
        viewModel.loadChatHistory(quoteId)
    }

    val hasCameraPermission = remember {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    fun createCameraImageUri(): Uri? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/RedCarga")
                }
                context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )
            } else {
                val imageFile = File(
                    context.cacheDir,
                    "camera_images"
                ).apply {
                    if (!exists()) mkdirs()
                }
                val photoFile = File(
                    imageFile,
                    "IMG_${System.currentTimeMillis()}.jpg"
                )
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    photoFile
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("ChatScreen", "Error creando URI para cámara: ${e.message}")
            null
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraImageUri != null) {
            viewModel.selectImage(cameraImageUri!!)
        } else {
            cameraImageUri = null
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = createCameraImageUri()
            if (uri != null) {
                cameraImageUri = uri
                cameraLauncher.launch(uri)
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.selectImage(it)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            ChatHeader(
                quoteId = quoteId,
                selectedTab = selectedTab,
                onBackClick = onNavigateBack,
                onTabSelected = { selectedTab = it }
            )

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            ) {
                when (uiState) {
                    is ChatViewModel.UiState.Loading -> {
                        LoadingContent()
                    }
                    is ChatViewModel.UiState.Error -> {
                        ErrorContent(
                            message = (uiState as ChatViewModel.UiState.Error).message,
                            onRetry = { viewModel.refreshChatHistory(quoteId) }
                        )
                    }
                    is ChatViewModel.UiState.Success -> {
                        val successState = uiState as ChatViewModel.UiState.Success

                        when (selectedTab) {
                            ChatTab.CHAT -> {
                                ChatTabContent(
                                    messages = successState.messages,
                                    currentUserId = successState.currentUserId,
                                    messageText = messageText,
                                    isSendingMessage = isSendingMessage,
                                    selectedImageUri = selectedImageUri,
                                    imageCaption = imageCaption,
                                    imageUploadState = imageUploadState,
                                    onMessageTextChange = { viewModel.updateMessageText(it) },
                                    onSendMessage = { viewModel.sendTextMessage(quoteId) },
                                    onSelectImageFromGallery = {
                                        galleryLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    onTakePhoto = {
                                        if (hasCameraPermission) {
                                            val uri = createCameraImageUri()
                                            if (uri != null) {
                                                cameraImageUri = uri
                                                cameraLauncher.launch(uri)
                                            }
                                        } else {
                                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                        }
                                    },
                                    onClearImage = { viewModel.clearSelectedImage() },
                                    onImageCaptionChange = { viewModel.updateImageCaption(it) },
                                    onSendImage = { viewModel.sendImageMessage(quoteId) },
                                    onImageClick = { imageUrl ->
                                        fullscreenImageUrl = imageUrl
                                        showFullscreenImage = true
                                    },
                                    quoteId = quoteId,
                                    viewModel = viewModel
                                )
                            }
                            ChatTab.INFO -> {
                                InfoTabContent(
                                    quoteId = quoteId,
                                    quoteDetail = quoteDetail,
                                    requestDetail = requestDetail,
                                    currentUserType = currentUserType,
                                    viewModel = viewModel,
                                    onNavigateToChat = { selectedTab = ChatTab.CHAT }
                                )
                            }
                        }
                    }
                }
            }

            if (selectedTab == ChatTab.CHAT && uiState is ChatViewModel.UiState.Success) {
                MessageInputBar(
                    messageText = messageText,
                    isSendingMessage = isSendingMessage,
                    selectedImageUri = selectedImageUri,
                    onMessageTextChange = { viewModel.updateMessageText(it) },
                    onSendMessage = { viewModel.sendTextMessage(quoteId) },
                    onSelectImageFromGallery = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onTakePhoto = {
                        if (hasCameraPermission) {
                            val uri = createCameraImageUri()
                            if (uri != null) {
                                cameraImageUri = uri
                                cameraLauncher.launch(uri)
                            }
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                )
            } else if (selectedTab == ChatTab.INFO) {
                MessageInputBar(
                    messageText = messageText,
                    isSendingMessage = isSendingMessage,
                    selectedImageUri = selectedImageUri,
                    onMessageTextChange = { viewModel.updateMessageText(it) },
                    onSendMessage = { viewModel.sendTextMessage(quoteId) },
                    onSelectImageFromGallery = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onTakePhoto = {
                        if (hasCameraPermission) {
                            val uri = createCameraImageUri()
                            if (uri != null) {
                                cameraImageUri = uri
                                cameraLauncher.launch(uri)
                            }
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                )
            }
        }

        if (showFullscreenImage && fullscreenImageUrl != null) {
            FullscreenImageViewer(
                imageUrl = fullscreenImageUrl!!,
                onDismiss = {
                    showFullscreenImage = false
                    fullscreenImageUrl = null
                }
            )
        }
    }
}

/**
 */
@Composable
private fun ChatHeader(
    quoteId: Long,
    selectedTab: ChatTab,
    onBackClick: () -> Unit,
    onTabSelected: (ChatTab) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(RcColor5, RcColor3)))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                text = "Cotización #$quoteId",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )

            // Menú de tres puntos (mismo tamaño que back para balance)
            IconButton(
                onClick = { /* TODO: Menú de opciones */ },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Más opciones",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Fila de tabs - Diseño consistente y visible
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Tab Chat
            TabButton(
                text = "Chat",
                isSelected = selectedTab == ChatTab.CHAT,
                onClick = { onTabSelected(ChatTab.CHAT) },
                modifier = Modifier.weight(1f)
            )

            // Tab Información
            TabButton(
                text = "Información",
                isSelected = selectedTab == ChatTab.INFO,
                onClick = { onTabSelected(ChatTab.INFO) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Componente reutilizable para los tabs
 */
@Composable
private fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) {
            Color.White.copy(alpha = 0.25f)
        } else {
            Color.Transparent
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * ⭐ CONTENT: Contenido de la tab Chat
 */
@Composable
private fun ChatTabContent(
    messages: List<ChatMessage>,
    currentUserId: Long,
    messageText: String,
    isSendingMessage: Boolean,
    selectedImageUri: Uri?,
    imageCaption: String,
    imageUploadState: ChatViewModel.ImageUploadState,
    onMessageTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onSelectImageFromGallery: () -> Unit,
    onTakePhoto: () -> Unit,
    onClearImage: () -> Unit,
    onImageCaptionChange: (String) -> Unit,
    onSendImage: () -> Unit,
    onImageClick: (String) -> Unit,
    quoteId: Long,
    viewModel: ChatViewModel
) {
    val listState = rememberLazyListState()

    // Scroll automático al final cuando hay nuevos mensajes
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Lista de mensajes
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                // Log detallado al renderizar cada mensaje
                LaunchedEffect(message.messageId) {
                    Log.d("ChatScreen", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    Log.d("ChatScreen", "🎨 [ChatTabContent] Renderizando mensaje")
                    Log.d("ChatScreen", "   messageId=${message.messageId}")
                    Log.d("ChatScreen", "   typeCode=${message.typeCode}")
                    Log.d("ChatScreen", "   systemSubtypeCode=${message.systemSubtypeCode}")
                    Log.d("ChatScreen", "   isChangeAppliedMessage=${message.isChangeAppliedMessage()}")
                    Log.d("ChatScreen", "   isChangeProposedMessage=${message.isChangeProposedMessage()}")
                    Log.d("ChatScreen", "   isChangeAcceptedMessage=${message.isChangeAcceptedMessage()}")
                    Log.d("ChatScreen", "   isChangeRejectedMessage=${message.isChangeRejectedMessage()}")
                    Log.d("ChatScreen", "   isAcceptanceRequestMessage=${message.isAcceptanceRequestMessage()}")
                    Log.d("ChatScreen", "   isAcceptanceConfirmedMessage=${message.isAcceptanceConfirmedMessage()}")
                    Log.d("ChatScreen", "   isAcceptanceRejectedMessage=${message.isAcceptanceRejectedMessage()}")
                    Log.d("ChatScreen", "   isQuoteRejectedMessage=${message.isQuoteRejectedMessage()}")
                    Log.d("ChatScreen", "   change=${if (message.change != null) "✅ Presente (changeId=${message.change.changeId}, statusCode=${message.change.statusCode}, items=${message.change.items.size})" else "❌ null"}")
                    Log.d("ChatScreen", "   acceptanceId=${if (message.acceptanceId != null) "✅ Presente (acceptanceId=${message.acceptanceId})" else "❌ null"}")
                    if (message.isChangeAppliedMessage() && message.change == null) {
                        Log.e("ChatScreen", "   ⚠️⚠️⚠️ ERROR: Es CHANGE_APPLIED pero change es null!")
                        Log.e("ChatScreen", "   info=${message.info?.take(500)}")
                    }
                    if (message.isChangeProposedMessage() && message.change == null) {
                        Log.e("ChatScreen", "   ⚠️⚠️⚠️ ERROR: Es CHANGE_PROPOSED pero change es null!")
                        Log.e("ChatScreen", "   info=${message.info?.take(500)}")
                    }
                    if (message.isAcceptanceRequestMessage() && message.acceptanceId == null) {
                        Log.e("ChatScreen", "   ⚠️⚠️⚠️ ERROR: Es ACCEPTANCE_REQUEST pero acceptanceId es null!")
                        Log.e("ChatScreen", "   info=${message.info?.take(500)}")
                    }
                    Log.d("ChatScreen", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                }
                
                ChatMessageItem(
                    message = message,
                    isFromCurrentUser = message.createdBy == currentUserId,
                    onImageClick = { imageUrl -> onImageClick(imageUrl) },
                    currentUserId = currentUserId,
                    quoteId = quoteId,
                    viewModel = viewModel
                )
            }
        }

        // Preview de imagen seleccionada
        if (selectedImageUri != null) {
            ImagePreviewCard(
                imageUri = selectedImageUri,
                caption = imageCaption,
                isUploading = imageUploadState is ChatViewModel.ImageUploadState.Uploading,
                onCaptionChange = onImageCaptionChange,
                onClear = onClearImage,
                onSend = onSendImage
            )
        }
    }
}


@Composable
private fun InfoTabContent(
    quoteId: Long,
    quoteDetail: QuoteDetail?,
    requestDetail: Request?,
    currentUserType: UserType?,
    viewModel: ChatViewModel,
    onNavigateToChat: () -> Unit
) {
    // Log del rol del usuario cuando se entra a la sección de Información
    LaunchedEffect(Unit) {
        val roleText = when (currentUserType) {
            UserType.CLIENT -> "CLIENT"
            UserType.PROVIDER -> "PROVIDER"
            null -> "NULL (no logueado)"
        }
        Log.d("ChatScreen", "=== INFORMACIÓN TAB ===")
        Log.d("ChatScreen", "Rol del usuario logueado: $roleText")
        Log.d("ChatScreen", "StateCode de la cotización: ${quoteDetail?.stateCode ?: "null"}")
    }
    
    // Estado para mostrar el modal de información de cotización
    var showQuoteDetailDialog by remember { mutableStateOf(false) }
    // Estado para mostrar el modal de edición de carga
    var showEditCargaDialog by remember { mutableStateOf(false) }
    // Estado para mostrar el modal de contraoferta
    var showContraofertaDialog by remember { mutableStateOf(false) }
    // Estado para controlar la carga de datos antes de abrir modales
    var isLoadingDataForQuoteDetail by remember { mutableStateOf(false) }
    var isLoadingDataForEditCarga by remember { mutableStateOf(false) }
    var isLoadingDataForContraoferta by remember { mutableStateOf(false) }
    
    // CoroutineScope para manejar la recarga de datos
    val coroutineScope = rememberCoroutineScope()
    // Determinar qué botones mostrar según el estado y el rol
    val buttonsToShow = remember(quoteDetail?.stateCode, currentUserType) {
        when {
            quoteDetail?.stateCode == "TRATO" && currentUserType == UserType.CLIENT -> {
                // CLIENT + TRATO: Estos 4 botones
                listOf(
                    "Editar carga actual",
                    "Ver información de la cotización",
                    "Rechazar Cotización",
                    "Solicitar Aceptar Trato"
                )
            }
            quoteDetail?.stateCode == "TRATO" && currentUserType == UserType.PROVIDER -> {
                // PROVIDER + TRATO: Estos 5 botones
                listOf(
                    "Editar carga actual",
                    "Hacer Contraoferta",
                    "Ver información de la cotización",
                    "Solicitar Aceptar Trato",
                    "Rechazar Solicitud"
                )
            }
            quoteDetail?.stateCode == "ACEPTADA" && (currentUserType == UserType.CLIENT || currentUserType == UserType.PROVIDER) -> {
                // ⭐ NUEVO: ACEPTADA para CLIENT y PROVIDER
                listOf(
                    "Editar carga actual",
                    "Ver información de la cotización",
                    "Rechazar Cotización"
                )
            }
            else -> emptyList()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(RcColor1),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        if (buttonsToShow.isNotEmpty()) {
            // Sección: Acciones
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Acciones",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = RcColor6
                    )

                    // Mostrar botones en un grid flexible
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        buttonsToShow.chunked(2).forEachIndexed { rowIndex, buttonPair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                buttonPair.forEachIndexed { colIndex, buttonText ->
                                    val buttonIndex = rowIndex * 2 + colIndex
                                    ActionButton(
                                        text = buttonText,
                                        onClick = {
                                            when (buttonText) {
                                                "Ver información de la cotización" -> {
                                                    // ⚠️ Recargar datos ANTES de abrir el modal
                                                    coroutineScope.launch {
                                                        isLoadingDataForQuoteDetail = true
                                                        Log.d("ChatScreen", "🔄 Recargando datos para QuoteDetailDialog...")
                                                        val success = viewModel.reloadQuoteAndRequestData(quoteId)
                                                        isLoadingDataForQuoteDetail = false
                                                        if (success) {
                                                            Log.d("ChatScreen", "✅ Datos recargados, abriendo modal")
                                                            showQuoteDetailDialog = true
                                                        } else {
                                                            Log.e("ChatScreen", "❌ Error al recargar datos, no se abrirá el modal")
                                                        }
                                                    }
                                                }
                                                "Editar carga actual" -> {
                                                    // ⚠️ Recargar datos ANTES de abrir el modal
                                                    coroutineScope.launch {
                                                        isLoadingDataForEditCarga = true
                                                        Log.d("ChatScreen", "🔄 Recargando datos para EditCargaDialog...")
                                                        val success = viewModel.reloadQuoteAndRequestData(quoteId)
                                                        isLoadingDataForEditCarga = false
                                                        if (success) {
                                                            Log.d("ChatScreen", "✅ Datos recargados, abriendo modal")
                                                            showEditCargaDialog = true
                                                        } else {
                                                            Log.e("ChatScreen", "❌ Error al recargar datos, no se abrirá el modal")
                                                        }
                                                    }
                                                }
                                                "Hacer Contraoferta" -> {
                                                    // ⚠️ Validar que esté en estado TRATO
                                                    if (quoteDetail?.stateCode == "TRATO") {
                                                        // ⚠️ Recargar datos ANTES de abrir el modal
                                                        coroutineScope.launch {
                                                            isLoadingDataForContraoferta = true
                                                            Log.d("ChatScreen", "🔄 Recargando datos para ContraofertaDialog...")
                                                            val success = viewModel.reloadQuoteAndRequestData(quoteId)
                                                            isLoadingDataForContraoferta = false
                                                            if (success) {
                                                                Log.d("ChatScreen", "✅ Datos recargados, abriendo modal de contraoferta")
                                                                showContraofertaDialog = true
                                                            } else {
                                                                Log.e("ChatScreen", "❌ Error al recargar datos, no se abrirá el modal")
                                                            }
                                                        }
                                                    } else {
                                                        Log.w("ChatScreen", "⚠️ Contraoferta solo disponible en estado TRATO, estado actual: ${quoteDetail?.stateCode}")
                                                    }
                                                }
                                                "Solicitar Aceptar Trato" -> {
                                                    Log.d("ChatScreen", "═══════════════════════════════════════")
                                                    Log.d("ChatScreen", "🤝 [BOTÓN] Solicitar Aceptar Trato presionado")
                                                    Log.d("ChatScreen", "═══════════════════════════════════════")
                                                    Log.d("ChatScreen", "   QuoteId: $quoteId")
                                                    Log.d("ChatScreen", "   Estado actual: ${quoteDetail?.stateCode}")
                                                    Log.d("ChatScreen", "   Usuario: ${currentUserType}")
                                                    
                                                    // ⚠️ Validar que esté en estado TRATO o EN_ESPERA
                                                    if (quoteDetail?.stateCode == "TRATO" || quoteDetail?.stateCode == "EN_ESPERA") {
                                                        Log.d("ChatScreen", "   ✅ Estado válido, llamando a viewModel.proposeAcceptance()...")
                                                        // Proponer aceptación directamente (sin modal, solo nota opcional)
                                                        viewModel.proposeAcceptance(quoteId, note = null)
                                                        Log.d("ChatScreen", "   ✅ Llamada a proposeAcceptance() completada")
                                                    } else {
                                                        Log.w("ChatScreen", "   ⚠️ Solicitar aceptación solo disponible en estado TRATO o EN_ESPERA")
                                                        Log.w("ChatScreen", "   Estado actual: ${quoteDetail?.stateCode}")
                                                    }
                                                    Log.d("ChatScreen", "═══════════════════════════════════════")
                                                }
                                                "Rechazar Cotización" -> {
                                                    // ⚠️ Validar que esté en estado ACEPTADA
                                                    if (quoteDetail?.stateCode == "ACEPTADA") {
                                                        Log.d("ChatScreen", "═══════════════════════════════════════")
                                                        Log.d("ChatScreen", "❌ [BOTÓN] Rechazar Cotización presionado")
                                                        Log.d("ChatScreen", "═══════════════════════════════════════")
                                                        Log.d("ChatScreen", "   QuoteId: $quoteId")
                                                        Log.d("ChatScreen", "   Estado actual: ${quoteDetail?.stateCode}")
                                                        Log.d("ChatScreen", "   Usuario: ${currentUserType}")
                                                        Log.d("ChatScreen", "   ✅ Estado válido, llamando a viewModel.rejectQuote()...")
                                                        viewModel.rejectQuote(quoteId)
                                                        Log.d("ChatScreen", "   ✅ Llamada a rejectQuote() completada")
                                                        Log.d("ChatScreen", "═══════════════════════════════════════")
                                                    } else {
                                                        Log.w("ChatScreen", "⚠️ Rechazar cotización solo disponible en estado ACEPTADA")
                                                        Log.w("ChatScreen", "   Estado actual: ${quoteDetail?.stateCode}")
                                                    }
                                                }
                                                else -> {
                                                    // TODO: Implementar otras acciones
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        gradient = getGradientForButton(buttonIndex % 4),
                                        enabled = !isLoadingDataForQuoteDetail && !isLoadingDataForEditCarga && !isLoadingDataForContraoferta
                                    )
                                }
                                // Si hay un número impar de botones, agregar un espacio vacío
                                if (buttonPair.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Si no hay botones para mostrar (estado no soportado o datos faltantes)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (quoteDetail == null) {
                            "Cargando información..."
                        } else {
                            "No hay acciones disponibles para este estado"
                        },
                        fontSize = 14.sp,
                        color = RcColor8,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Spacer final
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Modal de información de cotización
    if (showQuoteDetailDialog && quoteDetail != null) {
        QuoteDetailDialog(
            quoteDetail = quoteDetail,
            requestDetail = requestDetail,
            onDismiss = { showQuoteDetailDialog = false }
        )
    }
    
    // Modal de edición de carga
    if (showEditCargaDialog && quoteDetail != null) {
        EditCargaDialog(
            quoteDetail = quoteDetail,
            requestDetail = requestDetail,
            viewModel = viewModel,
            onDismiss = { showEditCargaDialog = false },
            onSuccess = {
                showEditCargaDialog = false
                onNavigateToChat()
            }
        )
    }
    
    // Modal de contraoferta
    if (showContraofertaDialog && quoteDetail != null && quoteDetail.stateCode == "TRATO") {
        ContraofertaDialog(
            quoteId = quoteId,
            quoteDetail = quoteDetail,
            requestDetail = requestDetail,
            viewModel = viewModel,
            onDismiss = { showContraofertaDialog = false },
            onSuccess = {
                showContraofertaDialog = false
                onNavigateToChat()
            }
        )
    }
    
    // ⭐ Diálogos de estado para rejectQuote
    val rejectQuoteState by viewModel.rejectQuoteState.collectAsState()
    when (rejectQuoteState) {
        is ChatViewModel.RejectQuoteState.Success -> {
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(1500)
                viewModel.resetRejectQuoteState()
                onNavigateToChat() // Navegar al chat para ver el mensaje QUOTE_REJECTED
            }
        }
        is ChatViewModel.RejectQuoteState.Error -> {
            val errorState = rejectQuoteState as ChatViewModel.RejectQuoteState.Error
            AlertDialog(
                onDismissRequest = { viewModel.resetRejectQuoteState() },
                title = { Text("Error", fontWeight = FontWeight.Bold) },
                text = { Text(errorState.message) },
                confirmButton = {
                    TextButton(onClick = { viewModel.resetRejectQuoteState() }) {
                        Text("Cerrar")
                    }
                }
            )
        }
        else -> {}
    }
}

/**
 * Obtiene un gradiente para los botones según su índice
 */
@Composable
private fun getGradientForButton(index: Int): Brush {
    return when (index % 4) {
        0 -> Brush.linearGradient(
            colors = listOf(
                Color(0xFFFFE0B2),
                Color(0xFFFFCCBC)
            )
        )
        1 -> Brush.linearGradient(
            colors = listOf(
                Color(0xFFFFCCBC),
                Color(0xFFFFB3BA)
            )
        )
        2 -> Brush.linearGradient(
            colors = listOf(
                Color(0xFFFFB3BA),
                Color(0xFFFF9E9E)
            )
        )
        else -> Brush.linearGradient(
            colors = listOf(
                Color(0xFFFF9E9E),
                Color(0xFFFF8A80)
            )
        )
    }
}

/**
 * ⭐ COMPONENT: Botón de acción con gradiente
 */
@Composable
private fun ActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    gradient: Brush,
    enabled: Boolean = true
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 2.dp,
        color = Color.Transparent,
        enabled = enabled
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (enabled) gradient else Brush.linearGradient(colors = listOf(Color.Gray.copy(alpha = 0.3f), Color.Gray.copy(alpha = 0.2f))))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            if (enabled) {
                Text(
                    text = text,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RcColor6,
                    textAlign = TextAlign.Center
                )
            } else {
                // Mostrar indicador de carga cuando está deshabilitado
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = RcColor5,
                    strokeWidth = 2.dp
                )
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
            textAlign = TextAlign.Center
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

/**
 * ⭐ ACCEPTANCE_REQUEST: Card especial para mensajes de solicitud de aceptación
 * Muestra botones "Aceptar" y "Rechazar" solo para el otro usuario (no el que hizo la solicitud)
 */
@Composable
private fun AcceptanceRequestMessageCard(
    message: ChatMessage,
    currentUserId: Long,
    quoteId: Long,
    viewModel: ChatViewModel
) {
    // Logs al renderizar el card
    LaunchedEffect(message.messageId, message.acceptanceId) {
        Log.d("ChatScreen", "═══════════════════════════════════════")
        Log.d("ChatScreen", "🎨 [AcceptanceRequestMessageCard] Renderizando card")
        Log.d("ChatScreen", "═══════════════════════════════════════")
        Log.d("ChatScreen", "   MessageId: ${message.messageId}")
        Log.d("ChatScreen", "   QuoteId: $quoteId")
        Log.d("ChatScreen", "   AcceptanceId: ${message.acceptanceId}")
        Log.d("ChatScreen", "   CreatedBy: ${message.createdBy}")
        Log.d("ChatScreen", "   CurrentUserId: $currentUserId")
        Log.d("ChatScreen", "   IsFromOtherUser: ${message.createdBy != currentUserId}")
        Log.d("ChatScreen", "   CanRespond: ${message.createdBy != currentUserId && message.acceptanceId != null}")
        Log.d("ChatScreen", "═══════════════════════════════════════")
    }
    
    val acceptanceState by viewModel.acceptanceState.collectAsState()
    val acceptanceId = message.acceptanceId
    
    // Determinar si el mensaje es del otro usuario (mostrar botones solo si es así)
    val isFromOtherUser = message.createdBy != currentUserId
    val canRespond = isFromOtherUser && acceptanceId != null
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                color = RcColor5.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = RcColor5.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Ícono de aceptación
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = RcColor5.copy(alpha = 0.2f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = RcColor5,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Contenido
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Solicitud de aceptación de trato",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = RcColor5
                    )
                    
                    message.body?.takeIf { it.isNotBlank() }?.let { note ->
                        Text(
                            text = note,
                            fontSize = 12.sp,
                            color = RcColor8,
                            lineHeight = 16.sp
                        )
                    } ?: run {
                        Text(
                            text = "Se ha solicitado aceptar esta cotización",
                            fontSize = 12.sp,
                            color = RcColor8,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
            
            // Botones de acción (solo para el otro usuario)
            if (canRespond) {
                HorizontalDivider(color = RcColor8.copy(alpha = 0.2f))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Botón Rechazar
                    Button(
                        onClick = {
                            Log.d("ChatScreen", "═══════════════════════════════════════")
                            Log.d("ChatScreen", "❌ [BOTÓN] Rechazar aceptación presionado")
                            Log.d("ChatScreen", "═══════════════════════════════════════")
                            Log.d("ChatScreen", "   QuoteId: $quoteId")
                            Log.d("ChatScreen", "   AcceptanceId: $acceptanceId")
                            Log.d("ChatScreen", "   MessageId: ${message.messageId}")
                            Log.d("ChatScreen", "   CreatedBy: ${message.createdBy}")
                            Log.d("ChatScreen", "   CurrentUserId: $currentUserId")
                            acceptanceId?.let {
                                Log.d("ChatScreen", "   ✅ AcceptanceId válido, llamando a viewModel.rejectAcceptance()...")
                                viewModel.rejectAcceptance(quoteId, it)
                                Log.d("ChatScreen", "   ✅ Llamada a rejectAcceptance() completada")
                            } ?: run {
                                Log.e("ChatScreen", "   ❌❌❌ ERROR: acceptanceId es null!")
                            }
                            Log.d("ChatScreen", "═══════════════════════════════════════")
                        },
                        modifier = Modifier.weight(1f),
                        enabled = acceptanceState !is ChatViewModel.AcceptanceState.Loading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF5252),
                            contentColor = Color.White,
                            disabledContainerColor = RcColor8.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (acceptanceState is ChatViewModel.AcceptanceState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Rechazar",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    
                    // Botón Aceptar
                    Button(
                        onClick = {
                            Log.d("ChatScreen", "═══════════════════════════════════════")
                            Log.d("ChatScreen", "✅ [BOTÓN] Aceptar trato presionado")
                            Log.d("ChatScreen", "═══════════════════════════════════════")
                            Log.d("ChatScreen", "   QuoteId: $quoteId")
                            Log.d("ChatScreen", "   AcceptanceId: $acceptanceId")
                            Log.d("ChatScreen", "   MessageId: ${message.messageId}")
                            Log.d("ChatScreen", "   CreatedBy: ${message.createdBy}")
                            Log.d("ChatScreen", "   CurrentUserId: $currentUserId")
                            acceptanceId?.let {
                                Log.d("ChatScreen", "   ✅ AcceptanceId válido, llamando a viewModel.confirmAcceptance()...")
                                viewModel.confirmAcceptance(quoteId, it)
                                Log.d("ChatScreen", "   ✅ Llamada a confirmAcceptance() completada")
                            } ?: run {
                                Log.e("ChatScreen", "   ❌❌❌ ERROR: acceptanceId es null!")
                            }
                            Log.d("ChatScreen", "═══════════════════════════════════════")
                        },
                        modifier = Modifier.weight(1f),
                        enabled = acceptanceState !is ChatViewModel.AcceptanceState.Loading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RcColor5,
                            contentColor = Color.White,
                            disabledContainerColor = RcColor8.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (acceptanceState is ChatViewModel.AcceptanceState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Aceptar",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Diálogos de estado para acceptance
    when (acceptanceState) {
        is ChatViewModel.AcceptanceState.Success -> {
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(1500) // Mostrar éxito brevemente
                viewModel.resetAcceptanceState()
            }
        }
        is ChatViewModel.AcceptanceState.Error -> {
            val errorState = acceptanceState as ChatViewModel.AcceptanceState.Error
            AlertDialog(
                onDismissRequest = { viewModel.resetAcceptanceState() },
                title = { Text("Error", fontWeight = FontWeight.Bold) },
                text = { Text(errorState.message) },
                confirmButton = {
                    TextButton(onClick = { viewModel.resetAcceptanceState() }) {
                        Text("Cerrar")
                    }
                }
            )
        }
        else -> {}
    }
}

/**
 * ⭐ ACCEPTANCE_STATUS: Card para mensajes ACCEPTANCE_CONFIRMED y ACCEPTANCE_REJECTED
 */
@Composable
private fun AcceptanceStatusMessageCard(
    message: ChatMessage
) {
    val isConfirmed = message.isAcceptanceConfirmedMessage()
    val iconColor = if (isConfirmed) RcColor3 else Color(0xFFFF5252)
    val backgroundColor = if (isConfirmed) RcColor3.copy(alpha = 0.15f) else Color(0xFFFF5252).copy(alpha = 0.15f)
    val borderColor = if (isConfirmed) RcColor3.copy(alpha = 0.5f) else Color(0xFFFF5252).copy(alpha = 0.5f)
    val title = if (isConfirmed) "Cotización aceptada" else "Solicitud rechazada"
    val description = if (isConfirmed) {
        "La cotización ha sido aceptada exitosamente"
    } else {
        "La solicitud de aceptación ha sido rechazada"
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Ícono
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = iconColor.copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isConfirmed) Icons.Default.CheckCircle else Icons.Default.Close,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Contenido
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = iconColor
                )
                
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = RcColor8,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

/**
 * ⭐ CHANGE_APPLIED: Card especial para mensajes de cambio aplicado
 */
@Composable
private fun ChangeAppliedMessageCard(
    message: ChatMessage
) {
    val change = message.change
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                color = RcColor3.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = RcColor3.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Ícono de edición
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = RcColor3.copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = RcColor3,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Contenido
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Cotización editada",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = RcColor3
                )
                
                if (change != null && change.items.isNotEmpty()) {
                    // Mostrar resumen de cambios
                    val changeDescriptions = change.items.mapNotNull { item ->
                        when (item.fieldCode) {
                            com.wapps1.redcarga.features.deals.domain.models.ChangeFieldCode.QTY -> {
                                "Cantidad modificada"
                            }
                            com.wapps1.redcarga.features.deals.domain.models.ChangeFieldCode.ITEM_REMOVE -> {
                                "Item eliminado"
                            }
                            com.wapps1.redcarga.features.deals.domain.models.ChangeFieldCode.ITEM_ADD -> {
                                "Item agregado"
                            }
                            com.wapps1.redcarga.features.deals.domain.models.ChangeFieldCode.PRICE_TOTAL -> {
                                "Monto total modificado"
                            }
                        }
                    }
                    
                    if (changeDescriptions.isNotEmpty()) {
                        Text(
                            text = changeDescriptions.joinToString(", "),
                            fontSize = 12.sp,
                            color = RcColor8,
                            lineHeight = 16.sp
                        )
                    }
                } else {
                    Text(
                        text = message.body ?: "Cambio aplicado",
                        fontSize = 12.sp,
                        color = RcColor8,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

/**
 * ⭐ CHANGE_PROPOSED: Card especial para mensajes de cambio propuesto
 * Muestra botones "Aceptar" y "Rechazar" solo para el otro usuario (no el que hizo la propuesta)
 */
@Composable
private fun ChangeProposedMessageCard(
    message: ChatMessage,
    currentUserId: Long,
    quoteId: Long,
    viewModel: ChatViewModel
) {
    val change = message.change
    val changeDecisionState by viewModel.changeDecisionState.collectAsState()
    
    // Determinar si el mensaje es del otro usuario (mostrar botones solo si es así)
    val isFromOtherUser = message.createdBy != currentUserId
    val canRespond = isFromOtherUser && 
                     change != null && 
                     change.statusCode == com.wapps1.redcarga.features.deals.domain.models.ChangeStatus.PENDIENTE
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                color = Color(0xFFFF9800).copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFF9800).copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Ícono de propuesta
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = Color(0xFFFF9800).copy(alpha = 0.2f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Contenido
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Cambio propuesto",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF9800)
                    )
                    
                    if (change != null && change.items.isNotEmpty()) {
                        val changeDescriptions = change.items.mapNotNull { item ->
                            when (item.fieldCode) {
                                com.wapps1.redcarga.features.deals.domain.models.ChangeFieldCode.QTY -> {
                                    "Cantidad modificada"
                                }
                                com.wapps1.redcarga.features.deals.domain.models.ChangeFieldCode.ITEM_REMOVE -> {
                                    "Item eliminado"
                                }
                                com.wapps1.redcarga.features.deals.domain.models.ChangeFieldCode.ITEM_ADD -> {
                                    "Item agregado"
                                }
                                com.wapps1.redcarga.features.deals.domain.models.ChangeFieldCode.PRICE_TOTAL -> {
                                    "Monto total modificado"
                                }
                            }
                        }
                        
                        if (changeDescriptions.isNotEmpty()) {
                            Text(
                                text = changeDescriptions.joinToString(", "),
                                fontSize = 12.sp,
                                color = RcColor8,
                                lineHeight = 16.sp
                            )
                        }
                    } else {
                        Text(
                            text = message.body ?: "Cambio propuesto",
                            fontSize = 12.sp,
                            color = RcColor8,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
            
            // Botones de acción (solo para el otro usuario)
            if (canRespond) {
                HorizontalDivider(color = RcColor8.copy(alpha = 0.2f))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Botón Rechazar
                    Button(
                        onClick = {
                            Log.d("ChatScreen", "═══════════════════════════════════════")
                            Log.d("ChatScreen", "❌ [BOTÓN] Rechazar cambio presionado")
                            Log.d("ChatScreen", "═══════════════════════════════════════")
                            Log.d("ChatScreen", "   QuoteId: $quoteId")
                            Log.d("ChatScreen", "   ChangeId: ${change?.changeId}")
                            change?.let {
                                Log.d("ChatScreen", "   ✅ ChangeId válido, llamando a viewModel.decisionChange()...")
                                viewModel.decisionChange(quoteId, it.changeId, accept = false)
                                Log.d("ChatScreen", "   ✅ Llamada a decisionChange() completada")
                            } ?: run {
                                Log.e("ChatScreen", "   ❌❌❌ ERROR: change es null!")
                            }
                            Log.d("ChatScreen", "═══════════════════════════════════════")
                        },
                        modifier = Modifier.weight(1f),
                        enabled = changeDecisionState !is ChatViewModel.ChangeDecisionState.Loading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF5252),
                            contentColor = Color.White,
                            disabledContainerColor = RcColor8.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (changeDecisionState is ChatViewModel.ChangeDecisionState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Rechazar",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    
                    // Botón Aceptar
                    Button(
                        onClick = {
                            Log.d("ChatScreen", "═══════════════════════════════════════")
                            Log.d("ChatScreen", "✅ [BOTÓN] Aceptar cambio presionado")
                            Log.d("ChatScreen", "═══════════════════════════════════════")
                            Log.d("ChatScreen", "   QuoteId: $quoteId")
                            Log.d("ChatScreen", "   ChangeId: ${change?.changeId}")
                            change?.let {
                                Log.d("ChatScreen", "   ✅ ChangeId válido, llamando a viewModel.decisionChange()...")
                                viewModel.decisionChange(quoteId, it.changeId, accept = true)
                                Log.d("ChatScreen", "   ✅ Llamada a decisionChange() completada")
                            } ?: run {
                                Log.e("ChatScreen", "   ❌❌❌ ERROR: change es null!")
                            }
                            Log.d("ChatScreen", "═══════════════════════════════════════")
                        },
                        modifier = Modifier.weight(1f),
                        enabled = changeDecisionState !is ChatViewModel.ChangeDecisionState.Loading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50),
                            contentColor = Color.White,
                            disabledContainerColor = RcColor8.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (changeDecisionState is ChatViewModel.ChangeDecisionState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Aceptar",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Diálogos de estado para change decision
    when (changeDecisionState) {
        is ChatViewModel.ChangeDecisionState.Success -> {
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(1500)
                viewModel.resetChangeDecisionState()
            }
        }
        is ChatViewModel.ChangeDecisionState.Error -> {
            val errorState = changeDecisionState as ChatViewModel.ChangeDecisionState.Error
            AlertDialog(
                onDismissRequest = { viewModel.resetChangeDecisionState() },
                title = { Text("Error", fontWeight = FontWeight.Bold) },
                text = { Text(errorState.message) },
                confirmButton = {
                    TextButton(onClick = { viewModel.resetChangeDecisionState() }) {
                        Text("Cerrar")
                    }
                }
            )
        }
        else -> {}
    }
}

/**
 * ⭐ CHANGE_STATUS: Card para mensajes CHANGE_ACCEPTED y CHANGE_REJECTED
 */
@Composable
private fun ChangeStatusMessageCard(
    message: ChatMessage,
    isAccepted: Boolean
) {
    val change = message.change
    val iconColor = if (isAccepted) Color(0xFF4CAF50) else Color(0xFFFF5252)
    val backgroundColor = if (isAccepted) Color(0xFF4CAF50).copy(alpha = 0.15f) else Color(0xFFFF5252).copy(alpha = 0.15f)
    val borderColor = if (isAccepted) Color(0xFF4CAF50).copy(alpha = 0.5f) else Color(0xFFFF5252).copy(alpha = 0.5f)
    val title = if (isAccepted) "Cambio aceptado" else "Cambio rechazado"
    val description = if (isAccepted) {
        "El cambio propuesto ha sido aceptado y aplicado"
    } else {
        "El cambio propuesto ha sido rechazado"
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Ícono
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = iconColor.copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isAccepted) Icons.Default.CheckCircle else Icons.Default.Close,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Contenido
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = iconColor
                )
                
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = RcColor8,
                    lineHeight = 16.sp
                )
                
                if (change != null && change.items.isNotEmpty()) {
                    val changeDescriptions = change.items.mapNotNull { item ->
                        when (item.fieldCode) {
                            com.wapps1.redcarga.features.deals.domain.models.ChangeFieldCode.QTY -> {
                                "Cantidad modificada"
                            }
                            com.wapps1.redcarga.features.deals.domain.models.ChangeFieldCode.ITEM_REMOVE -> {
                                "Item eliminado"
                            }
                            com.wapps1.redcarga.features.deals.domain.models.ChangeFieldCode.ITEM_ADD -> {
                                "Item agregado"
                            }
                            com.wapps1.redcarga.features.deals.domain.models.ChangeFieldCode.PRICE_TOTAL -> {
                                "Monto total modificado"
                            }
                        }
                    }
                    
                    if (changeDescriptions.isNotEmpty()) {
                        Text(
                            text = changeDescriptions.joinToString(", "),
                            fontSize = 11.sp,
                            color = RcColor8,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * ⭐ QUOTE_REJECTED: Card para mensajes de cotización rechazada
 */
@Composable
private fun QuoteRejectedMessageCard(
    message: ChatMessage
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                color = Color(0xFFFF5252).copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFF5252).copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Ícono
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = Color(0xFFFF5252).copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = Color(0xFFFF5252),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Contenido
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Cotización rechazada",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF5252)
                )
                
                Text(
                    text = message.body ?: "La cotización ha sido rechazada",
                    fontSize = 12.sp,
                    color = RcColor8,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun ChatMessageItem(
    message: ChatMessage,
    isFromCurrentUser: Boolean,
    onImageClick: (String) -> Unit,
    currentUserId: Long,
    quoteId: Long,
    viewModel: ChatViewModel
) {
    // Mensajes CHANGE_APPLIED se muestran centrados
    if (message.isChangeAppliedMessage()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier.widthIn(max = 320.dp)
            ) {
                ChangeAppliedMessageCard(message = message)
                
                // Timestamp centrado
                Text(
                    text = formatMessageTime(message.createdAt),
                    fontSize = 10.sp,
                    color = RcColor8,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }
    
    // Mensajes ACCEPTANCE_REQUEST se muestran centrados con botones condicionales
    if (message.isAcceptanceRequestMessage()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier.widthIn(max = 320.dp)
            ) {
                AcceptanceRequestMessageCard(
                    message = message,
                    currentUserId = currentUserId,
                    quoteId = quoteId,
                    viewModel = viewModel
                )
                
                // Timestamp centrado
                Text(
                    text = formatMessageTime(message.createdAt),
                    fontSize = 10.sp,
                    color = RcColor8,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }
    
    // Mensajes ACCEPTANCE_CONFIRMED y ACCEPTANCE_REJECTED se muestran centrados
    if (message.isAcceptanceConfirmedMessage() || message.isAcceptanceRejectedMessage()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier.widthIn(max = 320.dp)
            ) {
                AcceptanceStatusMessageCard(message = message)
                
                // Timestamp centrado
                Text(
                    text = formatMessageTime(message.createdAt),
                    fontSize = 10.sp,
                    color = RcColor8,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }
    
    // Mensajes CHANGE_PROPOSED se muestran centrados con botones condicionales
    if (message.isChangeProposedMessage()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier.widthIn(max = 320.dp)
            ) {
                ChangeProposedMessageCard(
                    message = message,
                    currentUserId = currentUserId,
                    quoteId = quoteId,
                    viewModel = viewModel
                )
                
                // Timestamp centrado
                Text(
                    text = formatMessageTime(message.createdAt),
                    fontSize = 10.sp,
                    color = RcColor8,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }
    
    // Mensajes CHANGE_ACCEPTED y CHANGE_REJECTED se muestran centrados
    if (message.isChangeAcceptedMessage() || message.isChangeRejectedMessage()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier.widthIn(max = 320.dp)
            ) {
                ChangeStatusMessageCard(
                    message = message,
                    isAccepted = message.isChangeAcceptedMessage()
                )
                
                // Timestamp centrado
                Text(
                    text = formatMessageTime(message.createdAt),
                    fontSize = 10.sp,
                    color = RcColor8,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }
    
    // Mensajes QUOTE_REJECTED se muestran centrados
    if (message.isQuoteRejectedMessage()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier.widthIn(max = 320.dp)
            ) {
                QuoteRejectedMessageCard(message = message)
                
                // Timestamp centrado
                Text(
                    text = formatMessageTime(message.createdAt),
                    fontSize = 10.sp,
                    color = RcColor8,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isFromCurrentUser) {
            Spacer(modifier = Modifier.width(48.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 280.dp),
            horizontalAlignment = if (isFromCurrentUser) Alignment.End else Alignment.Start
        ) {
            // Burbuja del mensaje
            Surface(
                modifier = Modifier.clip(RoundedCornerShape(16.dp)),
                color = if (isFromCurrentUser) RcColor5 else Color.White,
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    when {
                        message.isSystemMessage() -> {
                            // Mensaje del sistema normal
                            Text(
                                text = message.body ?: "",
                                fontSize = 13.sp,
                                color = RcColor8,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                textAlign = TextAlign.Center
                            )
                        }
                        message.isImageMessage() -> {
                            // Mensaje con imagen
                            message.mediaUrl?.let { url ->
                                AsyncImage(
                                    model = url,
                                    contentDescription = "Imagen",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(250.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onImageClick(url) },
                                    contentScale = ContentScale.Crop
                                )
                            }
                            message.body?.takeIf { it.isNotBlank() }?.let { caption ->
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = caption,
                                    fontSize = 14.sp,
                                    color = if (isFromCurrentUser) White else RcColor6
                                )
                            }
                        }
                        else -> {
                            // Mensaje de texto
                            Text(
                                text = message.body ?: "",
                                fontSize = 14.sp,
                                color = if (isFromCurrentUser) White else RcColor6
                            )
                        }
                    }
                }
            }

            // Timestamp
            Text(
                text = formatMessageTime(message.createdAt),
                fontSize = 10.sp,
                color = RcColor8,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }

        if (isFromCurrentUser) {
            Spacer(modifier = Modifier.width(48.dp))
        }
    }
}

@Composable
private fun ImagePreviewCard(
    imageUri: Uri,
    caption: String,
    isUploading: Boolean,
    onCaptionChange: (String) -> Unit,
    onClear: () -> Unit,
    onSend: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Header con botón de cerrar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Vista previa",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RcColor6
                )
                IconButton(
                    onClick = onClear,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = RcColor8,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Imagen preview
            AsyncImage(
                model = imageUri,
                contentDescription = "Preview",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Campo de caption
            OutlinedTextField(
                value = caption,
                onValueChange = onCaptionChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Agregar descripción (opcional)", fontSize = 13.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RcColor5,
                    unfocusedBorderColor = RcColor8.copy(alpha = 0.3f),
                    focusedTextColor = RcColor6,
                    unfocusedTextColor = RcColor6
                ),
                shape = RoundedCornerShape(12.dp),
                maxLines = 2,
                enabled = !isUploading,
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Botón de enviar
            Button(
                onClick = onSend,
                enabled = !isUploading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RcColor5,
                    disabledContainerColor = RcColor8.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enviando...", color = Color.White, fontSize = 14.sp)
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Enviar",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Enviar imagen", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun MessageInputBar(
    messageText: String,
    isSendingMessage: Boolean,
    selectedImageUri: Uri?,
    onMessageTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onSelectImageFromGallery: () -> Unit,
    onTakePhoto: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón de galería
                IconButton(
                    onClick = onSelectImageFromGallery,
                    enabled = !isSendingMessage && selectedImageUri == null,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Galería",
                        tint = if (selectedImageUri == null) RcColor5 else RcColor8.copy(alpha = 0.3f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Botón de cámara
                IconButton(
                    onClick = onTakePhoto,
                    enabled = !isSendingMessage && selectedImageUri == null,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Cámara",
                        tint = if (selectedImageUri == null) RcColor5 else RcColor8.copy(alpha = 0.3f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Campo de texto
                OutlinedTextField(
                    value = messageText,
                    onValueChange = onMessageTextChange,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp, max = 120.dp),
                    placeholder = { Text("Escribe un mensaje...", color = RcColor8, fontSize = 14.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RcColor5,
                        unfocusedBorderColor = RcColor8.copy(alpha = 0.3f),
                        focusedTextColor = RcColor6,
                        unfocusedTextColor = RcColor6
                    ),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4,
                    enabled = !isSendingMessage && selectedImageUri == null,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Botón de enviar
                IconButton(
                    onClick = onSendMessage,
                    enabled = messageText.trim().isNotBlank() && !isSendingMessage && selectedImageUri == null,
                    modifier = Modifier.size(48.dp)
                ) {
                    if (isSendingMessage) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = RcColor5,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Enviar",
                            tint = if (messageText.trim().isNotBlank()) RcColor5 else RcColor8.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun formatMessageTime(timestamp: Instant): String {
    val now = Instant.now()
    val duration = java.time.Duration.between(timestamp, now)

    return when {
        duration.toMinutes() < 1 -> "Ahora"
        duration.toMinutes() < 60 -> "Hace ${duration.toMinutes()} min"
        duration.toHours() < 24 -> "Hace ${duration.toHours()} h"
        duration.toDays() < 7 -> "Hace ${duration.toDays()} d"
        else -> {
            val date = timestamp.atZone(ZoneId.systemDefault())
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(date)
        }
    }
}

/**
 * ⭐ IMAGEN: Visor de imagen a pantalla completa
 */
@Composable
private fun FullscreenImageViewer(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    var showControls by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { showControls = !showControls }
                    )
                }
        ) {
            // Imagen a pantalla completa
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Imagen ampliada",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            // Controles superiores (solo si showControls)
            if (showControls) {
                // Header con botón cerrar
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(48.dp)) // Espacio para balancear
                        IconButton(
                            onClick = onDismiss
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * ⭐ EDIT CARGA: Clase para estado editable de cada item (mutable para Compose)
 */
private class EditableItemState(
    val quoteItemId: Long,
    val originalQty: Int,
    currentQty: Int,
    isDeleted: Boolean = false
) {
    var currentQty by mutableStateOf(currentQty)
    var isDeleted by mutableStateOf(isDeleted)
}

/**
 * ⭐ QUOTE DETAIL: Data class para combinar QuoteItem con RequestItem
 */
private data class QuoteItemDetail(
    val quoteItem: QuoteItem,
    val requestItem: RequestItem?
)

/**
 * ⭐ EDIT CARGA: Función para construir cambios desde items editables
 */
private fun buildChangesFromEditableItems(
    editableItems: List<EditableItemState>
): List<ChangeItem> {
    return editableItems
        .filter { it.isDeleted || it.currentQty != it.originalQty }
        .map { editable ->
            when {
                editable.isDeleted || editable.currentQty == 0 -> {
                    // ITEM_REMOVE
                    ChangeItem(
                        changeItemId = null,
                        fieldCode = ChangeFieldCode.ITEM_REMOVE,
                        targetQuoteItemId = editable.quoteItemId,
                        targetRequestItemId = null,
                        oldValue = editable.originalQty.toString(),
                        newValue = null
                    )
                }
                else -> {
                    // QTY
                    ChangeItem(
                        changeItemId = null,
                        fieldCode = ChangeFieldCode.QTY,
                        targetQuoteItemId = editable.quoteItemId,
                        targetRequestItemId = null,
                        oldValue = editable.originalQty.toString(),
                        newValue = editable.currentQty.toString()
                    )
                }
            }
        }
}

/**
 * ⭐ QUOTE DETAIL: Función helper para combinar datos de QuoteDetail con RequestDetail
 */
private fun combineQuoteWithRequestItems(
    quoteDetail: QuoteDetail,
    requestDetail: Request?
): List<QuoteItemDetail> {
    return quoteDetail.items.map { quoteItem ->
        val requestItem = requestDetail?.items?.find { it.itemId == quoteItem.requestItemId }
        QuoteItemDetail(quoteItem = quoteItem, requestItem = requestItem)
    }
}

/**
 * ⭐ QUOTE DETAIL: Formatear estado de cotización
 */
private fun formatQuoteState(stateCode: String): String {
    return when (stateCode) {
        "PENDIENTE" -> "Pendiente"
        "TRATO" -> "Trato"
        "EN_ESPERA" -> "En espera"
        "ACEPTADA" -> "Aceptada"
        "RECHAZADA" -> "Rechazada"
        "CERRADA" -> "Cerrada"
        "CERRADA_NO_ADJ" -> "Cerrada no adjudicada"
        else -> stateCode
    }
}

/**
 * ⭐ QUOTE DETAIL: Formatear fecha
 */
private fun formatDate(instant: Instant): String {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    return instant.atZone(ZoneId.systemDefault()).format(formatter)
}

/**
 * ⭐ QUOTE DETAIL: Modal principal de información de cotización
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuoteDetailDialog(
    quoteDetail: QuoteDetail,
    requestDetail: Request?,
    onDismiss: () -> Unit
) {
    // Combinar datos de quote con request
    val combinedItems = remember(quoteDetail, requestDetail) {
        combineQuoteWithRequestItems(quoteDetail, requestDetail)
    }

    // Estado para visor de imágenes
    var showImageFullscreen by remember { mutableStateOf(false) }
    var selectedImageIndex by remember { mutableStateOf(0) }
    var selectedImagesList by remember { mutableStateOf<List<String>>(emptyList()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header con gradiente
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(RcColor5, RcColor3)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Información de Cotización",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Contenido scrollable
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(RcColor1),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Sección: Información General
                    item {
                        QuoteInfoSection(quoteDetail = quoteDetail)
                    }

                    // Sección: Items
                    item {
                        Text(
                            text = "Items (${combinedItems.size})",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = RcColor6
                        )
                    }

                    items(combinedItems.size) { index ->
                        val itemDetail = combinedItems[index]
                        QuoteItemCard(
                            itemDetail = itemDetail,
                            index = index + 1,
                            onImageClick = { imageIndex, images ->
                                selectedImageIndex = imageIndex
                                selectedImagesList = images
                                showImageFullscreen = true
                            }
                        )
                    }

                    // Spacer final
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    // Visor de imágenes a pantalla completa
    if (showImageFullscreen && selectedImagesList.isNotEmpty()) {
        QuoteImageFullscreenViewer(
            images = selectedImagesList,
            initialIndex = selectedImageIndex,
            onDismiss = {
                showImageFullscreen = false
                selectedImagesList = emptyList()
            }
        )
    }
}

/**
 * ⭐ EDIT CARGA: Modal principal para editar la carga actual
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditCargaDialog(
    quoteDetail: QuoteDetail,
    requestDetail: Request?,
    viewModel: ChatViewModel,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    // Observar estado de aplicación de cambios
    val applyChangeState by viewModel.applyChangeState.collectAsState()
    // ⭐ Determinar si está en estado ACEPTADA
    val isAceptada = quoteDetail.stateCode == "ACEPTADA"
    // Combinar datos de quote con request
    val combinedItems = remember(quoteDetail, requestDetail) {
        combineQuoteWithRequestItems(quoteDetail, requestDetail)
    }
    
    // Estado editable para cada item
    val editableItems = remember(combinedItems) {
        combinedItems.map { itemDetail ->
            EditableItemState(
                quoteItemId = itemDetail.quoteItem.quoteItemId,
                originalQty = itemDetail.quoteItem.qty,
                currentQty = itemDetail.quoteItem.qty,
                isDeleted = false
            )
        }
    }
    
    // Estado mutable para rastrear cambios
    val editableItemsState = remember { mutableStateListOf(*editableItems.toTypedArray()) }
    
    // Estado para rastrear si hay cambios
    var hasChanges by remember { mutableStateOf(false) }
    
    // Función helper para recalcular hasChanges
    fun updateHasChanges() {
        hasChanges = editableItemsState.any { item ->
            item.isDeleted || item.currentQty != item.originalQty
        }
    }
    
    // Recalcular hasChanges inicialmente
    LaunchedEffect(Unit) {
        updateHasChanges()
    }
    
    // Estado para visor de imágenes
    var showImageFullscreen by remember { mutableStateOf(false) }
    var selectedImageIndex by remember { mutableStateOf(0) }
    var selectedImagesList by remember { mutableStateOf<List<String>>(emptyList()) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = RcColor1),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header con gradiente
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    RcColor5,
                                    RcColor5.copy(alpha = 0.8f)
                                )
                            )
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Editar Carga Actual",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
                
                // Contenido scrollable
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    items(combinedItems.size) { index ->
                        val itemDetail = combinedItems[index]
                        val editableItem = editableItemsState[index]
                        
                        EditableItemCard(
                            itemDetail = itemDetail,
                            editableItem = editableItem,
                            index = index + 1,
                            onQtyChange = { newQty ->
                                editableItem.currentQty = newQty.coerceAtLeast(0)
                                // Si llega a 0, se considera eliminación
                                if (editableItem.currentQty == 0) {
                                    editableItem.isDeleted = true
                                } else {
                                    editableItem.isDeleted = false
                                }
                                // Recalcular hasChanges inmediatamente
                                updateHasChanges()
                            },
                            onDelete = {
                                editableItem.isDeleted = true
                                editableItem.currentQty = 0
                                // Recalcular hasChanges inmediatamente
                                updateHasChanges()
                            },
                            onImageClick = { imageIndex, images ->
                                selectedImageIndex = imageIndex
                                selectedImagesList = images
                                showImageFullscreen = true
                            }
                        )
                    }
                    
                    // Spacer final
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                
                // Footer con botón de confirmar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = RcColor7,
                    shadowElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Indicador de cambios
                        if (hasChanges) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Tienes cambios pendientes",
                                    fontSize = 14.sp,
                                    color = RcColor5,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        
                        // Botón confirmar
                        Button(
                            onClick = {
                                val changes = buildChangesFromEditableItems(editableItemsState)
                                val request = ApplyChangeRequest(items = changes)
                                
                                if (isAceptada) {
                                    // ⭐ ACEPTADA: Crear propuesta (requiere aprobación)
                                    Log.d("ChatScreen", "📝 [ACEPTADA] Creando propuesta de cambio...")
                                    Log.d("ChatScreen", "   QuoteId: ${quoteDetail.quoteId}")
                                    Log.d("ChatScreen", "   Items: ${changes.size}")
                                    viewModel.applyChange(quoteDetail.quoteId, request, quoteDetail.version.toString())
                                } else {
                                    // TRATO: Aplicar cambios inmediatamente
                                    Log.d("ChatScreen", "📝 [TRATO] Aplicando cambios inmediatamente...")
                                    viewModel.applyChange(quoteDetail.quoteId, request, quoteDetail.version.toString())
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = hasChanges && applyChangeState !is ChatViewModel.ApplyChangeState.Loading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (hasChanges) RcColor5 else RcColor8.copy(alpha = 0.5f),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (isAceptada) {
                                    "Proponer cambio de carga"  // ⭐ Texto diferente para ACEPTADA
                                } else {
                                    "Confirmar edición de carga"  // Texto para TRATO
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Visor de imágenes a pantalla completa
    if (showImageFullscreen && selectedImagesList.isNotEmpty()) {
        QuoteImageFullscreenViewer(
            images = selectedImagesList,
            initialIndex = selectedImageIndex,
            onDismiss = {
                showImageFullscreen = false
                selectedImagesList = emptyList()
            }
        )
    }
    
    // Diálogos de estado para aplicación de cambios
    when (applyChangeState) {
        is ChatViewModel.ApplyChangeState.Loading -> {
            ChangeAppliedLoadingDialog()
        }
        is ChatViewModel.ApplyChangeState.Success -> {
            ChangeAppliedSuccessDialog(
                onDismiss = {
                    viewModel.resetApplyChangeState()
                    // Primero cambiar el tab a CHAT
                    onSuccess()
                    // Luego cerrar el diálogo de edición
                    onDismiss()
                }
            )
        }
        is ChatViewModel.ApplyChangeState.Error -> {
            val errorState = applyChangeState as ChatViewModel.ApplyChangeState.Error
            ChangeAppliedErrorDialog(
                message = errorState.message,
                onDismiss = {
                    viewModel.resetApplyChangeState()
                }
            )
        }
        else -> {}
    }
}

/**
 * ⭐ EDIT CARGA: Diálogo de carga
 */
@Composable
private fun ChangeAppliedLoadingDialog() {
    Dialog(onDismissRequest = {}) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = RcColor5,
                    modifier = Modifier.size(56.dp),
                    strokeWidth = 5.dp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Aplicando cambios...",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = RcColor6
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Por favor espera",
                    fontSize = 14.sp,
                    color = RcColor8
                )
            }
        }
    }
}

/**
 * ⭐ EDIT CARGA: Diálogo de éxito
 */
@Composable
private fun ChangeAppliedSuccessDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Ícono de éxito con animación
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(RcColor3.copy(alpha = 0.2f), Color.Transparent)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = RcColor3,
                        modifier = Modifier.size(56.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "¡Carga Editada!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = RcColor6
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Los cambios se han aplicado exitosamente y se ha notificado al otro participante.",
                    fontSize = 14.sp,
                    color = RcColor8,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RcColor5,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Ver Chat",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

/**
 * ⭐ EDIT CARGA: Diálogo de error
 */
@Composable
private fun ChangeAppliedErrorDialog(
    message: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = Color(0xFFFF5252),
                    modifier = Modifier.size(56.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Error al aplicar cambios",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = RcColor6
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = RcColor8,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RcColor5,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Cerrar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

/**
 * ⭐ CONTRAOFERTA: Modal para hacer contraoferta (cambiar monto total)
 * Solo disponible en estado TRATO
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContraofertaDialog(
    quoteId: Long,
    quoteDetail: QuoteDetail,
    requestDetail: Request?,
    viewModel: ChatViewModel,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    // Validar que esté en estado TRATO
    if (quoteDetail.stateCode != "TRATO") {
        Log.w("ChatScreen", "⚠️ ContraofertaDialog: Solo disponible en estado TRATO, estado actual: ${quoteDetail.stateCode}")
        return
    }
    
    // Observar estado de aplicación de cambios
    val applyChangeState by viewModel.applyChangeState.collectAsState()
    
    // Combinar datos de quote con request
    val combinedItems = remember(quoteDetail, requestDetail) {
        combineQuoteWithRequestItems(quoteDetail, requestDetail)
    }
    
    // Estado para el monto editable
    var newTotalAmount by remember { mutableStateOf(quoteDetail.totalAmount.toString()) }
    var amountError by remember { mutableStateOf<String?>(null) }
    
    // Estado para visor de imágenes
    var showImageFullscreen by remember { mutableStateOf(false) }
    var selectedImageIndex by remember { mutableStateOf(0) }
    var selectedImagesList by remember { mutableStateOf<List<String>>(emptyList()) }
    
    // Validar monto
    val isValidAmount = remember(newTotalAmount) {
        try {
            val amount = newTotalAmount.toBigDecimal()
            val currentAmount = quoteDetail.totalAmount.toBigDecimal()
            amount > BigDecimal.ZERO && amount != currentAmount
        } catch (e: Exception) {
            false
        }
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header con gradiente
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(RcColor5, RcColor3)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Hacer Contraoferta",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
                
                // Contenido scrollable
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .background(RcColor1),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Sección: Información General (con monto editable)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Información General",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RcColor6
                                )
                                
                                HorizontalDivider(color = RcColor8.copy(alpha = 0.2f))
                                
                                InfoRow(label = "ID Cotización", value = "#${quoteDetail.quoteId}")
                                InfoRow(label = "ID Solicitud", value = "#${quoteDetail.requestId}")
                                InfoRow(
                                    label = "Estado",
                                    value = formatQuoteState(quoteDetail.stateCode),
                                    valueColor = RcColor5
                                )
                                
                                // ⚠️ MONTO EDITABLE
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Monto Total",
                                            fontSize = 14.sp,
                                            color = RcColor8,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "${quoteDetail.currencyCode} ${String.format("%.2f", quoteDetail.totalAmount)}",
                                            fontSize = 14.sp,
                                            color = RcColor8,
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                        )
                                    }
                                    
                                    OutlinedTextField(
                                        value = newTotalAmount,
                                        onValueChange = { newValue ->
                                            // Validar que solo contenga números y punto decimal
                                            if (newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                                newTotalAmount = newValue
                                                amountError = null
                                                
                                                // Validar que sea mayor a 0
                                                try {
                                                    val amount = newValue.toBigDecimal()
                                                    val currentAmount = quoteDetail.totalAmount.toBigDecimal()
                                                    if (amount <= BigDecimal.ZERO) {
                                                        amountError = "El monto debe ser mayor a 0"
                                                    } else if (amount == currentAmount) {
                                                        amountError = "El monto debe ser diferente al actual"
                                                    }
                                                } catch (e: Exception) {
                                                    if (newValue.isNotBlank()) {
                                                        amountError = "Monto inválido"
                                                    }
                                                }
                                            }
                                        },
                                        label = { Text("Nuevo monto total") },
                                        placeholder = { Text("Ingrese el nuevo monto") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = RcColor5,
                                            unfocusedBorderColor = RcColor8.copy(alpha = 0.5f)
                                        ),
                                        isError = amountError != null,
                                        supportingText = if (amountError != null) {
                                            { Text(text = amountError!!, color = Color(0xFFFF5252)) }
                                        } else {
                                            { Text(text = "Moneda: ${quoteDetail.currencyCode}") }
                                        }
                                    )
                                }
                                
                                InfoRow(label = "Versión", value = "${quoteDetail.version}")
                                InfoRow(label = "Creada", value = formatDate(quoteDetail.createdAt))
                                InfoRow(label = "Actualizada", value = formatDate(quoteDetail.updatedAt))
                            }
                        }
                    }
                    
                    // Sección: Items
                    item {
                        Text(
                            text = "Items (${combinedItems.size})",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = RcColor6
                        )
                    }
                    
                    items(combinedItems.size) { index ->
                        val itemDetail = combinedItems[index]
                        QuoteItemCard(
                            itemDetail = itemDetail,
                            index = index + 1,
                            onImageClick = { imageIndex, images ->
                                selectedImageIndex = imageIndex
                                selectedImagesList = images
                                showImageFullscreen = true
                            }
                        )
                    }
                    
                    // Spacer final
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                
                // Footer con botón "Confirmar contraoferta"
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                // ⚠️ Validar monto antes de proceder
                                try {
                                    val newAmount = newTotalAmount.toBigDecimal()
                                    val currentAmount = quoteDetail.totalAmount.toBigDecimal()
                                    if (newAmount <= BigDecimal.ZERO) {
                                        amountError = "El monto debe ser mayor a 0"
                                        return@Button
                                    }
                                    if (newAmount == currentAmount) {
                                        amountError = "El monto debe ser diferente al actual"
                                        return@Button
                                    }
                                    
                                    // ⚠️ Construir el request para cambiar el monto
                                    // Usar toFormattedString() para formato consistente de decimales (ej: "15.0" en lugar de "15")
                                    val changeItem = com.wapps1.redcarga.features.deals.domain.models.ChangeItem(
                                        changeItemId = null,
                                        fieldCode = com.wapps1.redcarga.features.deals.domain.models.ChangeFieldCode.PRICE_TOTAL,
                                        targetQuoteItemId = null,
                                        targetRequestItemId = null,
                                        oldValue = currentAmount.toFormattedString(),  // Formato consistente: siempre muestra decimales
                                        newValue = newAmount.toFormattedString()  // Formato consistente: siempre muestra decimales
                                    )
                                    
                                    val applyChangeRequest = ApplyChangeRequest(
                                        items = listOf(changeItem)
                                    )
                                    
                                    // ⚠️ LLAMAR AL ENDPOINT REAL
                                    Log.d("ChatScreen", "💰 Aplicando contraoferta: quoteId=$quoteId, nuevoMonto=$newAmount")
                                    viewModel.applyChange(
                                        quoteId = quoteId,
                                        request = applyChangeRequest,
                                        ifMatch = quoteDetail.version.toString()
                                    )
                                    
                                } catch (e: Exception) {
                                    Log.e("ChatScreen", "❌ Error al validar monto: ${e.message}", e)
                                    amountError = "Error al procesar el monto"
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = isValidAmount && amountError == null && applyChangeState !is ChatViewModel.ApplyChangeState.Loading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RcColor5,
                                contentColor = Color.White,
                                disabledContainerColor = RcColor8.copy(alpha = 0.3f),
                                disabledContentColor = Color.White.copy(alpha = 0.6f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Confirmar contraoferta",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        
                        Text(
                            text = "Al confirmar, se cambiará el monto total de la cotización y se notificará al otro participante.",
                            fontSize = 12.sp,
                            color = RcColor8,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
    
    // Visor de imágenes a pantalla completa
    if (showImageFullscreen && selectedImagesList.isNotEmpty()) {
        QuoteImageFullscreenViewer(
            images = selectedImagesList,
            initialIndex = selectedImageIndex,
            onDismiss = {
                showImageFullscreen = false
                selectedImagesList = emptyList()
            }
        )
    }
    
    // Diálogos de estado para aplicación de cambios
    when (applyChangeState) {
        is ChatViewModel.ApplyChangeState.Loading -> {
            ChangeAppliedLoadingDialog()
        }
        is ChatViewModel.ApplyChangeState.Success -> {
            ChangeAppliedSuccessDialog(
                onDismiss = {
                    viewModel.resetApplyChangeState()
                    // Primero cambiar el tab a CHAT
                    onSuccess()
                    // Luego cerrar el diálogo de contraoferta
                    onDismiss()
                }
            )
        }
        is ChatViewModel.ApplyChangeState.Error -> {
            val errorState = applyChangeState as ChatViewModel.ApplyChangeState.Error
            ChangeAppliedErrorDialog(
                message = errorState.message,
                onDismiss = {
                    viewModel.resetApplyChangeState()
                }
            )
        }
        else -> {}
    }
}

/**
 * ⭐ EDIT CARGA: Card editable para cada item
 */
@Composable
private fun EditableItemCard(
    itemDetail: QuoteItemDetail,
    editableItem: EditableItemState,
    index: Int,
    onQtyChange: (Int) -> Unit,
    onDelete: () -> Unit,
    onImageClick: (Int, List<String>) -> Unit
) {
    val requestItem = itemDetail.requestItem
    val quoteItem = itemDetail.quoteItem
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (editableItem.isDeleted) {
                Color(0xFFFFEBEE) // Rojo claro si está eliminado
            } else if (editableItem.currentQty != editableItem.originalQty) {
                Color(0xFFE8F5E9) // Verde claro si está modificado
            } else {
                Color.White
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header con número, nombre y acciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = RcColor5.copy(alpha = 0.2f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = "$index",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = RcColor5
                            )
                        }
                    }
                    Text(
                        text = requestItem?.itemName ?: "Item #${quoteItem.quoteItemId}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = RcColor6
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Badge frágil
                    if (requestItem?.fragile == true) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFF8A65)
                        ) {
                            Text(
                                text = "Frágil",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    // Botón eliminar
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Eliminar item",
                            tint = if (editableItem.isDeleted) Color(0xFFD32F2F) else RcColor8,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            if (requestItem != null) {
                Divider(color = RcColor8.copy(alpha = 0.2f))
                
                // Información del item
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Dimensiones
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Dimensiones:",
                            fontSize = 13.sp,
                            color = RcColor8
                        )
                        Text(
                            text = "${requestItem.widthCm} × ${requestItem.heightCm} × ${requestItem.lengthCm} cm",
                            fontSize = 13.sp,
                            color = RcColor6
                        )
                    }
                    
                    // Peso unitario
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Peso unitario:",
                            fontSize = 13.sp,
                            color = RcColor8
                        )
                        Text(
                            text = "${requestItem.weightKg} kg",
                            fontSize = 13.sp,
                            color = RcColor6
                        )
                    }
                    
                    // Peso total (calculado con cantidad actual)
                    val totalWeight = requestItem.weightKg.multiply(editableItem.currentQty.toBigDecimal())
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Peso total:",
                            fontSize = 13.sp,
                            color = RcColor8
                        )
                        Text(
                            text = "${String.format("%.2f", totalWeight)} kg",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = RcColor5
                        )
                    }
                    
                    // Notas
                    if (requestItem.notes.isNotBlank()) {
                        Divider(color = RcColor8.copy(alpha = 0.2f))
                        Column {
                            Text(
                                text = "Notas:",
                                fontSize = 12.sp,
                                color = RcColor8,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = requestItem.notes,
                                fontSize = 13.sp,
                                color = RcColor6,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                }
                
                // Imágenes
                val sortedImages = requestItem.images.sortedBy { it.imagePosition }
                if (sortedImages.isNotEmpty()) {
                    Divider(color = RcColor8.copy(alpha = 0.2f))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Imágenes (${sortedImages.size})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = RcColor6
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(sortedImages.size) { imgIndex: Int ->
                                val currentImage = sortedImages[imgIndex]
                                QuoteItemImageThumbnail(
                                    imageUrl = currentImage.imageUrl,
                                    imageNumber = imgIndex + 1,
                                    totalImages = sortedImages.size,
                                    onClick = {
                                        val imageUrls = sortedImages.map { it.imageUrl }
                                        onImageClick(imgIndex, imageUrls)
                                    }
                                )
                            }
                        }
                    }
                }
                
                Divider(color = RcColor8.copy(alpha = 0.2f))
                
                // Cantidad editable
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cantidad:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = RcColor6
                        )
                        if (editableItem.currentQty != editableItem.originalQty) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = RcColor5.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "${editableItem.originalQty} → ${editableItem.currentQty}",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = RcColor5
                                )
                            }
                        }
                    }
                    
                    OutlinedTextField(
                        value = editableItem.currentQty.toString(),
                        onValueChange = { newValue ->
                            val qty = newValue.toIntOrNull() ?: 0
                            onQtyChange(qty)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Cantidad de unidades") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RcColor5,
                            unfocusedBorderColor = RcColor8.copy(alpha = 0.5f),
                            focusedLabelColor = RcColor5,
                            unfocusedLabelColor = RcColor8
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    if (editableItem.isDeleted || editableItem.currentQty == 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFEBEE)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    tint = Color(0xFFD32F2F),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Este item será eliminado",
                                    fontSize = 12.sp,
                                    color = Color(0xFFD32F2F),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            } else {
                // Si no se encontró el requestItem
                Text(
                    text = "Información del item no disponible",
                    fontSize = 13.sp,
                    color = RcColor8,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

/**
 * ⭐ QUOTE DETAIL: Sección de información general
 */
@Composable
private fun QuoteInfoSection(quoteDetail: QuoteDetail) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Información General",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = RcColor6
            )

            Divider(color = RcColor8.copy(alpha = 0.2f))

            InfoRow(label = "ID Cotización", value = "#${quoteDetail.quoteId}")
            InfoRow(label = "ID Solicitud", value = "#${quoteDetail.requestId}")
            InfoRow(
                label = "Estado",
                value = formatQuoteState(quoteDetail.stateCode),
                valueColor = RcColor5
            )
            InfoRow(
                label = "Monto Total",
                value = "${quoteDetail.currencyCode} ${String.format("%.2f", quoteDetail.totalAmount)}",
                valueColor = RcColor5,
                valueFontWeight = FontWeight.Bold
            )
            InfoRow(label = "Versión", value = "${quoteDetail.version}")
            InfoRow(label = "Creada", value = formatDate(quoteDetail.createdAt))
            InfoRow(label = "Actualizada", value = formatDate(quoteDetail.updatedAt))
        }
    }
}

/**
 * ⭐ QUOTE DETAIL: Fila de información
 */
@Composable
private fun InfoRow(
    label: String,
    value: String,
    valueColor: Color = RcColor6,
    valueFontWeight: FontWeight = FontWeight.Normal
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = RcColor8,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = valueColor,
            fontWeight = valueFontWeight,
            textAlign = TextAlign.End
        )
    }
}

/**
 * ⭐ QUOTE DETAIL: Card para cada item de la cotización
 */
@Composable
private fun QuoteItemCard(
    itemDetail: QuoteItemDetail,
    index: Int,
    onImageClick: (Int, List<String>) -> Unit
) {
    val requestItem = itemDetail.requestItem
    val quoteItem = itemDetail.quoteItem

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header con número y nombre
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = RcColor5.copy(alpha = 0.2f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = "$index",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = RcColor5
                            )
                        }
                    }
                    Text(
                        text = requestItem?.itemName ?: "Item #${quoteItem.quoteItemId}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = RcColor6
                    )
                }

                // Badge frágil
                if (requestItem?.fragile == true) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFF8A65)
                    ) {
                        Text(
                            text = "Frágil",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (requestItem != null) {
                Divider(color = RcColor8.copy(alpha = 0.2f))

                // Información del item
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Cantidad cotizada vs cantidad original
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Cantidad cotizada:",
                            fontSize = 13.sp,
                            color = RcColor8
                        )
                        Text(
                            text = "${quoteItem.qty} unidades",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = RcColor5
                        )
                    }

                    // Dimensiones
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Dimensiones:",
                            fontSize = 13.sp,
                            color = RcColor8
                        )
                        Text(
                            text = "${requestItem.widthCm} × ${requestItem.heightCm} × ${requestItem.lengthCm} cm",
                            fontSize = 13.sp,
                            color = RcColor6
                        )
                    }

                    // Peso
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Peso unitario:",
                            fontSize = 13.sp,
                            color = RcColor8
                        )
                        Text(
                            text = "${requestItem.weightKg} kg",
                            fontSize = 13.sp,
                            color = RcColor6
                        )
                    }

                    // Peso total
                    val totalWeight = requestItem.weightKg.multiply(quoteItem.qty.toBigDecimal())
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Peso total:",
                            fontSize = 13.sp,
                            color = RcColor8
                        )
                        Text(
                            text = "${String.format("%.2f", totalWeight)} kg",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = RcColor5
                        )
                    }

                    // Notas
                    if (requestItem.notes.isNotBlank()) {
                        Divider(color = RcColor8.copy(alpha = 0.2f))
                        Column {
                            Text(
                                text = "Notas:",
                                fontSize = 12.sp,
                                color = RcColor8,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = requestItem.notes,
                                fontSize = 13.sp,
                                color = RcColor6,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                }

                // Imágenes
                val sortedImages = requestItem.images.sortedBy { it.imagePosition }
                if (sortedImages.isNotEmpty()) {
                    Divider(color = RcColor8.copy(alpha = 0.2f))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Imágenes (${sortedImages.size})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = RcColor6
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(sortedImages.size) { imgIndex: Int ->
                                val currentImage = sortedImages[imgIndex]
                                QuoteItemImageThumbnail(
                                    imageUrl = currentImage.imageUrl,
                                    imageNumber = imgIndex + 1,
                                    totalImages = sortedImages.size,
                                    onClick = {
                                        val imageUrls = sortedImages.map { it.imageUrl }
                                        onImageClick(imgIndex, imageUrls)
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                // Si no se encontró el requestItem
                Text(
                    text = "Información del item no disponible",
                    fontSize = 13.sp,
                    color = RcColor8,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

/**
 * ⭐ QUOTE DETAIL: Thumbnail de imagen del item
 */
@Composable
private fun QuoteItemImageThumbnail(
    imageUrl: String,
    imageNumber: Int,
    totalImages: Int,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 4.dp,
        color = RcColor7
    ) {
        Box {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Imagen $imageNumber de $totalImages",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            // Badge con número
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color.Black.copy(alpha = 0.7f)
            ) {
                Text(
                    text = "$imageNumber",
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * ⭐ QUOTE DETAIL: Visor de imágenes a pantalla completa
 */
@Composable
private fun QuoteImageFullscreenViewer(
    images: List<String>,
    initialIndex: Int,
    onDismiss: () -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { images.size }
    )
    var showControls by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { showControls = !showControls }
                    )
                }
        ) {
            // Pager de imágenes
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { pageIndex ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = images[pageIndex],
                        contentDescription = "Imagen ${pageIndex + 1} de ${images.size}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // Controles superiores (solo si showControls)
            if (showControls) {
                // Header con contador y botón cerrar
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${pagerState.currentPage + 1} / ${images.size}",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                // Indicadores de página en la parte inferior
                if (images.size > 1) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter),
                        color = Color.Black.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Botón anterior
                            if (pagerState.currentPage > 0) {
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Anterior",
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.size(48.dp))
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Indicadores de página
                            repeat(images.size) { index ->
                                val isSelected = index == pagerState.currentPage
                                Surface(
                                    modifier = Modifier
                                        .size(if (isSelected) 10.dp else 6.dp)
                                        .clickable {
                                            scope.launch {
                                                pagerState.animateScrollToPage(index)
                                            }
                                        },
                                    shape = CircleShape,
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f)
                                ) {}
                                if (index < images.size - 1) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Botón siguiente
                            if (pagerState.currentPage < images.size - 1) {
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Siguiente",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .size(28.dp)
                                            .graphicsLayer { rotationZ = 180f }
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.size(48.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
