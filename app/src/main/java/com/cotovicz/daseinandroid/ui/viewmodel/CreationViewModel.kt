package com.cotovicz.daseinandroid.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cotovicz.daseinandroid.data.remote.models.Comment
import com.cotovicz.daseinandroid.data.remote.models.Creation
import com.cotovicz.daseinandroid.data.repository.CreationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CreationViewModel(private val repository: CreationRepository) : ViewModel() {

    val allCreations: StateFlow<List<Creation>> = repository.getAllCreations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _savedCreations = MutableStateFlow<List<Creation>>(emptyList())
    val savedCreations: StateFlow<List<Creation>> = _savedCreations

    private val _availableTags = MutableStateFlow<List<String>>(emptyList())
    val availableTags: StateFlow<List<String>> = _availableTags

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedTags = MutableStateFlow<Set<String>>(emptySet())
    val selectedTags: StateFlow<Set<String>> = _selectedTags

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading

    private val _uploadError = MutableStateFlow<String?>(null)
    val uploadError: StateFlow<String?> = _uploadError

    fun clearUploadError() {
        _uploadError.value = null
    }

    val filteredCreations: StateFlow<List<Creation>> = allCreations
        .combine(_searchQuery) { creations, query ->
            if (query.isBlank()) {
                creations
            } else {
                creations.filter { creation ->
                    creation.title.contains(query, ignoreCase = true) ||
                            creation.textContent?.contains(query, ignoreCase = true) == true ||
                            creation.authorName.contains(query, ignoreCase = true)
                }
            }
        }
        .combine(_selectedTags) { creations, tags ->
            if (tags.isEmpty()) {
                creations
            } else {
                creations.filter { creation ->
                    creation.tags.any { tag -> tag in tags }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadAvailableTags()
    }

    private fun loadAvailableTags() {
        viewModelScope.launch {
            val result = repository.getAvailableTags()
            if (result.isSuccess) {
                _availableTags.value = result.getOrNull() ?: emptyList()
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onTagToggled(tag: String) {
        val currentTags = _selectedTags.value.toMutableSet()
        if (tag in currentTags) {
            currentTags.remove(tag)
        } else {
            currentTags.add(tag)
        }
        _selectedTags.value = currentTags
    }

    fun getCreationById(id: String): Flow<Creation?> {
        return repository.getCreationById(id)
    }

    fun getCreationsForUser(userId: String): Flow<List<Creation>> {
        return repository.getCreationsByUserId(userId)
    }

    fun loadSavedCreations(postIds: List<String>) {
        viewModelScope.launch {
            val result = repository.getPostsByIdList(postIds)
            if (result.isSuccess) {
                _savedCreations.value = result.getOrNull() ?: emptyList()
            } else {
                _savedCreations.value = emptyList()
            }
        }
    }

    fun insertCreation(
        title: String,
        description: String,
        tags: List<String>,
        imageUriString: String?,
        currentUserId: String?,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            if (currentUserId == null) {
                _uploadError.value = "Utilizador não está logado."
                onComplete()
                return@launch
            }

            _isUploading.value = true
            _uploadError.value = null
            var finalImageUrl: String? = null

            try {
                if (imageUriString != null) {
                    val uploadResult = repository.uploadImage(currentUserId, imageUriString)
                    if (uploadResult.isSuccess) {
                        finalImageUrl = uploadResult.getOrNull()
                    } else {
                        throw uploadResult.exceptionOrNull() ?: Exception("Falha no upload da imagem.")
                    }
                }

                repository.insertCreation(title, description, tags, finalImageUrl)

            } catch (e: Exception) {
                _uploadError.value = e.message ?: "Erro ao criar post."
            } finally {
                _isUploading.value = false
                onComplete()
            }
        }
    }

    fun updateCreation(
        creationId: String,
        title: String,
        description: String,
        tags: List<String>,
        newImageUriString: String?,
        existingImageUrl: String?,
        imageWasRemoved: Boolean,
        currentUserId: String?,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            if (currentUserId == null) {
                _uploadError.value = "Utilizador não está logado."
                onComplete()
                return@launch
            }

            _isUploading.value = true
            _uploadError.value = null
            var finalImageUrl: String? = existingImageUrl

            try {
                if (newImageUriString != null) {
                    val uploadResult = repository.uploadImage(currentUserId, newImageUriString)
                    if (uploadResult.isSuccess) {
                        finalImageUrl = uploadResult.getOrNull()
                    } else {
                        throw uploadResult.exceptionOrNull() ?: Exception("Falha no upload da nova imagem.")
                    }
                } else if (imageWasRemoved) {
                    finalImageUrl = null
                }

                repository.updateCreation(creationId, title, description, tags, finalImageUrl)

            } catch (e: Exception) {
                _uploadError.value = e.message ?: "Erro ao atualizar post."
            } finally {
                _isUploading.value = false
                onComplete()
            }
        }
    }

    fun deleteCreation(creationId: String) {
        viewModelScope.launch {
            repository.deleteCreation(creationId)
        }
    }

    fun getCommentsForCreation(creationId: String): Flow<List<Comment>> {
        return repository.getCommentsForCreation(creationId)
    }

    fun addComment(text: String, creationId: String, parentId: String?) {
        viewModelScope.launch {
            repository.insertComment(creationId, text, parentId)
        }
    }

    fun updateComment(commentId: String, newText: String) {
        viewModelScope.launch {
            repository.updateComment(commentId, newText)
        }
    }

    fun deleteComment(commentId: String, creationId: String) {
        viewModelScope.launch {
            repository.deleteComment(commentId, creationId)
        }
    }
}