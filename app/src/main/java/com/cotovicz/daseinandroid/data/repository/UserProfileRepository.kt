package com.cotovicz.daseinandroid.data.repository

import com.cotovicz.daseinandroid.data.remote.models.UserProfile
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await

class UserProfileRepository(
    private val firestore: FirebaseFirestore
) {
    private val usersCollection = firestore.collection("users")

    suspend fun getProfile(uid: String): UserProfile? {
        return try {
            val document = usersCollection.document(uid).get().await()
            document.toObject<UserProfile>()?.copy(uid = document.id)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun createProfile(uid: String, email: String) {
        val defaultDisplayName = email.split('@').firstOrNull() ?: email
        val newProfile = UserProfile(
            uid = uid,
            displayName = defaultDisplayName,
            bio = null,
            savedPostIds = emptyList()
        )
        try {
            usersCollection.document(uid).set(newProfile).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun updateProfile(uid: String, displayName: String, bio: String?): Result<Unit> {
        return try {
            val updates = mapOf(
                "displayName" to displayName,
                "bio" to bio
            )
            usersCollection.document(uid).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun toggleSavePost(uid: String, postId: String, isCurrentlySaved: Boolean): Result<Unit> {
        return try {
            val userProfileRef = usersCollection.document(uid)
            val updateOperation = if (isCurrentlySaved) {
                FieldValue.arrayRemove(postId)
            } else {
                FieldValue.arrayUnion(postId)
            }

            userProfileRef.update("savedPostIds", updateOperation).await()
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}