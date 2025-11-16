package com.cotovicz.daseinandroid.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cotovicz.daseinandroid.data.repository.CreationRepository

class CreationViewModelFactory(
    private val repository: CreationRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreationViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}