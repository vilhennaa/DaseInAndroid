package com.cotovicz.daseinandroid.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cotovicz.daseinandroid.data.remote.models.Comment


@Composable
fun CommentItem(
    comment: Comment,
    commentsByParentId: Map<String?, List<Comment>>,
    currentUserId: String?,
    onReplyClicked: (Comment) -> Unit,
    onEdit: (commentId: String, newText: String) -> Unit,
    onDelete: (commentId: String) -> Unit,
    depth: Int = 0
) {
    val paddingStart = (depth * 16).dp

    val isAuthor = comment.userId == currentUserId
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, start = paddingStart)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = MaterialTheme.shapes.small
                )
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = comment.authorName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = comment.commentText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                TextButton(onClick = { onReplyClicked(comment) }) {
                    Text("Responder")
                }

                Spacer(modifier = Modifier.weight(1f))

                if (isAuthor) {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar Comentário",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Apagar Comentário",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        if (showEditDialog) {
            EditCommentDialog(
                initialText = comment.commentText,
                onDismiss = { showEditDialog = false },
                onSave = { newText ->
                    onEdit(comment.id, newText)
                    showEditDialog = false
                }
            )
        }

        if (showDeleteDialog) {
            DeleteCommentDialog(
                onDismiss = { showDeleteDialog = false },
                onConfirm = {
                    onDelete(comment.id)
                    showDeleteDialog = false
                }
            )
        }

        val replies = commentsByParentId[comment.id]

        replies?.let {
            Column(Modifier.fillMaxWidth()) {
                it.forEach { reply ->
                    CommentItem(
                        comment = reply,
                        commentsByParentId = commentsByParentId,
                        currentUserId = currentUserId,
                        onReplyClicked = onReplyClicked,
                        onEdit = onEdit,
                        onDelete = onDelete,
                        depth = depth + 1
                    )
                }
            }
        }
    }
}

@Composable
private fun EditCommentDialog(
    initialText: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialText) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Comentário") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { onSave(text) }, enabled = text.isNotBlank()) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun DeleteCommentDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Apagar Comentário") },
        text = { Text("Tem a certeza que quer apagar este comentário? Esta ação não pode ser revertida.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text("Apagar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}