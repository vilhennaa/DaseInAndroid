package com.cotovicz.daseinandroid.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cotovicz.daseinandroid.data.remote.auth.FirebaseAuthManager
import com.cotovicz.daseinandroid.data.repository.UserProfileRepository

class AuthViewModelFactory(
    private val authManager: FirebaseAuthManager,
    private val profileRepository: UserProfileRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(authManager, profileRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}