package com.cotovicz.daseinandroid.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.cotovicz.daseinandroid.data.remote.models.Comment
import com.cotovicz.daseinandroid.ui.components.CommentItem
import com.cotovicz.daseinandroid.ui.viewmodel.AuthViewModel
import com.cotovicz.daseinandroid.ui.viewmodel.CreationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    navController: NavController,
    viewModel: CreationViewModel,
    authViewModel: AuthViewModel,
    creationId: String
) {
    val creation by viewModel.getCreationById(creationId).collectAsState(initial = null)
    val comments by viewModel.getCommentsForCreation(creationId).collectAsState(initial = emptyList())
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()

    val commentsByParentId by remember(comments) {
        derivedStateOf {
            comments.groupBy { it.parentId }
        }
    }
    val topLevelComments = commentsByParentId[null] ?: emptyList()

    var newCommentText by rememberSaveable { mutableStateOf("") }
    var replyingTo by remember { mutableStateOf<Comment?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Voltar") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    creation?.let {
                        val isAuthor = currentUser?.uid == it.userId
                        if (isAuthor) {
                            IconButton(onClick = { navController.navigate("edit/${it.id}") }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar")
                            }
                            IconButton(onClick = {
                                viewModel.deleteCreation(it.id)
                                navController.popBackStack()
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Deletar")
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            item {
                creation?.let {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(vertical = 16.dp)
                    ) {
                        Text(
                            text = it.title,
                            style = MaterialTheme.typography.headlineMedium
                        )

                        if (it.imageUrl != null) {
                            AsyncImage(
                                model = it.imageUrl,
                                contentDescription = "Imagem da Criação",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Text(
                            text = it.textContent ?: "",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Por: ${it.authorName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } ?: run {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
            item {
                CommentInputField(
                    text = newCommentText,
                    onTextChange = { newCommentText = it },
                    replyingTo = replyingTo,
                    onCancelReply = { replyingTo = null },
                    onSubmit = {
                        viewModel.addComment(
                            text = newCommentText,
                            creationId = creationId,
                            parentId = replyingTo?.id
                        )
                        newCommentText = ""
                        replyingTo = null
                    }
                )
            }
            item {
                Divider(modifier = Modifier.padding(vertical = 16.dp))
                Text("Comentários", style = MaterialTheme.typography.titleMedium)
            }

            items(topLevelComments) { comment ->
                CommentItem(
                    comment = comment,
                    commentsByParentId = commentsByParentId,
                    currentUserId = currentUser?.uid,
                    onReplyClicked = {
                        replyingTo = it
                    },
                    onEdit = { commentId, newText ->
                        viewModel.updateComment(commentId, newText)
                    },
                    onDelete = { commentId ->
                        viewModel.deleteComment(commentId, creationId)
                    }
                )
            }
        }
    }
}


@Composable
private fun CommentInputField(
    text: String,
    onTextChange: (String) -> Unit,
    replyingTo: Comment?,
    onCancelReply: () -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        if (replyingTo != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.shapes.small
                    )
                    .padding(8.dp)
            ) {
                Text(
                    text = "respondendo ${replyingTo.authorName}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onCancelReply, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, "Cancelar resposta")
                }
            }
        }

        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(
                    if (replyingTo == null) "Adicionar comentário..."
                    else "Responder..."
                )
            },
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

        Button(
            onClick = onSubmit,
            enabled = text.isNotBlank(),
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 8.dp)
        ) {
            Text("Publicar")
        }
    }
}