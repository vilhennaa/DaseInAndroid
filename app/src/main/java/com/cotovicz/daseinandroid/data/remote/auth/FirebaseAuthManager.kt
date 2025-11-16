package com.cotovicz.daseinandroid.data.remote.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthManager {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
        }
    }

    suspend fun signUp(email: String, password: String): Result<FirebaseUser> {
        return try {
            val userCredential = auth.createUserWithEmailAndPassword(email, password).await()
            userCredential.user?.let { Result.success(it) }
                ?: Result.failure(Exception("Usuário não retornado após o registro."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return try {
            val userCredential = auth.signInWithEmailAndPassword(email, password).await()
            userCredential.user?.let { Result.success(it) }
                ?: Result.failure(Exception("Usuário não retornado após o login."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }

    fun getCurrentUserSynchronously(): FirebaseUser? {
        return auth.currentUser
    }

    fun isAuthenticated(): Boolean {
        return auth.currentUser != null
    }
}