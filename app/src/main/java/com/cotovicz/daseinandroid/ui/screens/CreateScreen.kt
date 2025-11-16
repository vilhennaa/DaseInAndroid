package com.cotovicz.daseinandroid.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.cotovicz.daseinandroid.ui.viewmodel.AuthViewModel
import com.cotovicz.daseinandroid.ui.viewmodel.CreationViewModel
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateScreen(
    viewModel: CreationViewModel,
    authViewModel: AuthViewModel,
    navController: NavController,
    creationId: String?
) {
    val isEditMode = creationId != null
    val topBarTitle = if (isEditMode) "Editar Ideia" else "Nova Ideia"
    val buttonText = if (isEditMode) "Salvar" else "Publicar"

    var title by rememberSaveable { mutableStateOf("") }
    var textContent by rememberSaveable { mutableStateOf("") }

    val availableTags by viewModel.availableTags.collectAsStateWithLifecycle()
    var selectedTags by remember { mutableStateOf(setOf<String>()) }

    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()

    val isUploading by viewModel.isUploading.collectAsStateWithLifecycle()
    var imageUriString by rememberSaveable { mutableStateOf<String?>(null) }
    var existingImageUrl by rememberSaveable { mutableStateOf<String?>(null) }
    var imageWasRemoved by rememberSaveable { mutableStateOf(false) }

    val currentImageToShow = imageUriString ?: (if (!imageWasRemoved) existingImageUrl else null)

    val isTitleEmpty = title.isBlank()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                imageUriString = uri.toString()
                imageWasRemoved = false
            }
        }
    )

    LaunchedEffect(key1 = creationId) {
        if (isEditMode) {
            viewModel.getCreationById(creationId).first { it != null }?.let { creation ->
                title = creation.title
                textContent = creation.textContent ?: ""
                selectedTags = creation.tags.toSet()
                existingImageUrl = creation.imageUrl
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(topBarTitle) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }, enabled = !isUploading) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cancelar"
                        )
                    }
                },
                actions = {
                    if (isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp).padding(end = 16.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Button(
                            onClick = {
                                if (!isTitleEmpty) {
                                    val tagsList = selectedTags.toList()
                                    val onCompleteCallback: () -> Unit = { navController.popBackStack() }

                                    if (isEditMode) {
                                        viewModel.updateCreation(
                                            creationId = creationId,
                                            title = title,
                                            description = textContent,
                                            tags = tagsList,
                                            newImageUriString = imageUriString,
                                            existingImageUrl = existingImageUrl,
                                            imageWasRemoved = imageWasRemoved,
                                            currentUserId = currentUser?.uid,
                                            onComplete = onCompleteCallback
                                        )
                                    } else {
                                        viewModel.insertCreation(
                                            title = title,
                                            description = textContent,
                                            tags = tagsList,
                                            imageUriString = imageUriString,
                                            currentUserId = currentUser?.uid,
                                            onComplete = onCompleteCallback
                                        )
                                    }
                                }
                            },
                            enabled = !isTitleEmpty,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(buttonText)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isUploading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (currentImageToShow != null) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    AsyncImage(
                        model = currentImageToShow,
                        contentDescription = "Pré-visualização da imagem",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = {
                            imageUriString = null
                            imageWasRemoved = true
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape),
                        enabled = !isUploading
                    ) {
                        Icon(Icons.Default.Close, "Remover Imagem", tint = Color.White)
                    }
                }
            } else {
                OutlinedButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUploading
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text("Adicionar Imagem")
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Título de sua reflexão") },
                isError = isTitleEmpty,
                singleLine = true,
                readOnly = isUploading,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )

            OutlinedTextField(
                value = textContent,
                onValueChange = { textContent = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                label = { Text("Comece a escrever a sua reflexão aqui...") },
                readOnly = isUploading,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )

            Text(
                text = "Selecione as tags relevantes",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp)
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableTags.forEach { tag ->
                    val isSelected = tag in selectedTags
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (!isUploading) {
                                selectedTags = if (isSelected) {
                                    selectedTags - tag
                                } else {
                                    selectedTags + tag
                                }
                            }
                        },
                        label = { Text(tag) },
                        enabled = !isUploading,
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Selecionado",
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        } else {
                            null
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}