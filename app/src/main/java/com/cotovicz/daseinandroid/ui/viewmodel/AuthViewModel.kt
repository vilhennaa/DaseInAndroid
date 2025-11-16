package com.cotovicz.daseinandroid.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cotovicz.daseinandroid.data.remote.auth.FirebaseAuthManager
import com.cotovicz.daseinandroid.data.remote.models.UserProfile
import com.cotovicz.daseinandroid.data.repository.UserProfileRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authManager: FirebaseAuthManager,
    private val profileRepository: UserProfileRepository
) : ViewModel() {

    private val _isAuthenticated = MutableStateFlow(authManager.isAuthenticated())
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _currentUser = MutableStateFlow<FirebaseUser?>(authManager.getCurrentUserSynchronously())
    val currentUser: StateFlow<FirebaseUser?> = _currentUser

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile

    init {
        viewModelScope.launch {
            authManager.currentUser.collect { user ->
                _isAuthenticated.value = (user != null)
                _currentUser.value = user
                _errorMessage.value = null

                if (user != null) {
                    var profile = profileRepository.getProfile(user.uid)
                    if (profile == null) {
                        profileRepository.createProfile(user.uid, user.email ?: "")
                        profile = profileRepository.getProfile(user.uid)
                    }
                    _userProfile.value = profile
                } else {
                    _userProfile.value = null
                }
            }
        }
    }

    fun signUp(email: String, password: String) {
        _errorMessage.value = null
        viewModelScope.launch {
            val result = authManager.signUp(email, password)
            if (result.isSuccess) {
                val newUser = result.getOrNull()
                if (newUser != null) {
                    profileRepository.createProfile(newUser.uid, newUser.email ?: "")
                }
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Erro desconhecido ao registrar."
            }
        }
    }

    fun signIn(email: String, password: String) {
        _errorMessage.value = null
        viewModelScope.launch {
            val result = authManager.signIn(email, password)
            if (result.isFailure) {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Erro desconhechido ao fazer login."
            }
        }
    }

    fun updateProfile(displayName: String, bio: String?) {
        viewModelScope.launch {
            val uid = _currentUser.value?.uid
            if (uid != null) {
                val result = profileRepository.updateProfile(uid, displayName, bio)
                if (result.isSuccess) {
                    _userProfile.value = profileRepository.getProfile(uid)
                } else {
                    _errorMessage.value = "Falha ao atualizar o perfil."
                }
            }
        }
    }

    fun toggleSavePost(postId: String) {
        viewModelScope.launch {
            val uid = _currentUser.value?.uid ?: return@launch
            val currentProfile = _userProfile.value ?: return@launch

            val isCurrentlySaved = currentProfile.savedPostIds.contains(postId)
            val result = profileRepository.toggleSavePost(uid, postId, isCurrentlySaved)

            if (result.isSuccess) {
                _userProfile.value = profileRepository.getProfile(uid)
            } else {
                _errorMessage.value = "Falha ao salvar o post."
            }
        }
    }

    fun signOut() {
        authManager.signOut()
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}