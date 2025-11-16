package com.cotovicz.daseinandroid.data.repository

import android.net.Uri
import com.cotovicz.daseinandroid.data.remote.auth.FirebaseAuthManager
import com.cotovicz.daseinandroid.data.remote.models.Comment
import com.cotovicz.daseinandroid.data.remote.models.Creation
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class CreationRepository(
    private val firestore: FirebaseFirestore,
    private val authManager: FirebaseAuthManager
) {

    private val creationsCollection = firestore.collection("creations")
    private val commentsCollection = firestore.collection("comments")
    private val usersCollection = firestore.collection("users")
    private val tagsDocRef = firestore.collection("config").document("tags")
    private val storage = Firebase.storage

    fun getAllCreations(): Flow<List<Creation>> = callbackFlow {
        val listener = creationsCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val creations = snapshot.documents.mapNotNull { doc ->
                        doc.toObject<Creation>()?.copy(id = doc.id)
                    }
                    trySend(creations)
                }
            }
        awaitClose { listener.remove() }
    }

    fun getCreationById(creationId: String): Flow<Creation?> = callbackFlow {
        val docRef = creationsCollection.document(creationId)
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                trySend(snapshot.toObject<Creation>()?.copy(id = snapshot.id))
            } else {
                trySend(null)
            }
        }
        awaitClose { listener.remove() }
    }

    fun getCreationsByUserId(userId: String): Flow<List<Creation>> = callbackFlow {
        val listener = creationsCollection
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val creations = snapshot.documents.mapNotNull { doc ->
                        doc.toObject<Creation>()?.copy(id = doc.id)
                    }
                    trySend(creations)
                }
            }
        awaitClose { listener.remove() }
    }

    private suspend fun getUserDisplayName(uid: String, fallbackEmail: String): String {
        val fallbackName = fallbackEmail.split('@').firstOrNull() ?: fallbackEmail
        return try {
            val profileDoc = usersCollection.document(uid).get().await()
            if (profileDoc.exists()) {
                profileDoc.getString("displayName") ?: fallbackName
            } else {
                fallbackName
            }
        } catch (e: Exception) {
            fallbackName
        }
    }

    suspend fun insertCreation(
        title: String,
        textContent: String?,
        tags: List<String>,
        imageUrl: String?
    ): Result<Unit> = try {
        val currentUser = authManager.getCurrentUserSynchronously()
        if (currentUser == null) {
            Result.failure(Exception("Utilizador não autenticado."))
        } else {
            val authorName = getUserDisplayName(currentUser.uid, currentUser.email ?: "Autor")

            val newCreation = Creation(
                userId = currentUser.uid,
                authorName = authorName,
                title = title,
                textContent = textContent,
                commentCount = 0,
                tags = tags,
                imageUrl = imageUrl
            )
            creationsCollection.add(newCreation).await()
            Result.success(Unit)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateCreation(
        creationId: String,
        title: String,
        textContent: String?,
        tags: List<String>,
        imageUrl: String?
    ): Result<Unit> = try {
        val updates = mapOf(
            "title" to title,
            "textContent" to textContent,
            "tags" to tags,
            "imageUrl" to imageUrl
        )
        creationsCollection.document(creationId).update(updates).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteCreation(creationId: String): Result<Unit> = try {
        creationsCollection.document(creationId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getCommentsForCreation(creationId: String): Flow<List<Comment>> = callbackFlow {
        val listener = commentsCollection
            .whereEqualTo("creationId", creationId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val comments = snapshot.documents.mapNotNull { doc ->
                        doc.toObject<Comment>()?.copy(id = doc.id)
                    }
                    trySend(comments)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun insertComment(
        creationId: String,
        commentText: String,
        parentId: String?
    ): Result<Unit> = try {
        val currentUser = authManager.getCurrentUserSynchronously()
        if (currentUser == null) {
            Result.failure(Exception("Utilizador não autenticado."))
        } else {
            val authorName = getUserDisplayName(currentUser.uid, currentUser.email ?: "Autor")

            val newComment = Comment(
                creationId = creationId,
                userId = currentUser.uid,
                authorName = authorName,
                commentText = commentText,
                parentId = parentId
            )

            val creationRef = creationsCollection.document(creationId)
            val newCommentRef = commentsCollection.document()

            firestore.runBatch { batch ->
                batch.set(newCommentRef, newComment)
                batch.update(creationRef, "commentCount", FieldValue.increment(1))
            }.await()

            Result.success(Unit)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateComment(commentId: String, newText: String): Result<Unit> = try {
        commentsCollection.document(commentId)
            .update("commentText", newText)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteComment(commentId: String, creationId: String): Result<Unit> = try {
        val commentRef = commentsCollection.document(commentId)
        val creationRef = creationsCollection.document(creationId)

        firestore.runBatch { batch ->
            batch.delete(commentRef)
            batch.update(creationRef, "commentCount", FieldValue.increment(-1))
        }.await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getPostsByIdList(postIds: List<String>): Result<List<Creation>> {
        if (postIds.isEmpty()) {
            return Result.success(emptyList())
        }
        return try {
            val chunks = postIds.chunked(30)
            val allCreations = mutableListOf<Creation>()

            for (chunk in chunks) {
                val snapshot = creationsCollection
                    .whereIn(FieldPath.documentId(), chunk)
                    .get()
                    .await()

                val creations = snapshot.documents.mapNotNull { doc ->
                    doc.toObject<Creation>()?.copy(id = doc.id)
                }
                allCreations.addAll(creations)
            }

            allCreations.sortByDescending { it.timestamp }

            Result.success(allCreations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAvailableTags(): Result<List<String>> {
        return try {
            val document = tagsDocRef.get().await()
            if (document.exists()) {
                @Suppress("UNCHECKED_CAST")
                val tags = document.get("availableTags") as? List<String>
                if (tags != null) {
                    Result.success(tags)
                } else {
                    Result.failure(Exception("O campo 'availableTags' não é uma lista de strings ou não foi encontrado."))
                }
            } else {
                Result.failure(Exception("Documento 'config/tags' não encontrado. Crie-o no painel do Firestore."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadImage(userId: String, imageUriString: String): Result<String> {
        return try {
            val uri = Uri.parse(imageUriString)
            val fileName = "images/$userId/${UUID.randomUUID()}.jpg"
            val storageRef = storage.reference.child(fileName)

            storageRef.putFile(uri).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()

            Result.success(downloadUrl)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}