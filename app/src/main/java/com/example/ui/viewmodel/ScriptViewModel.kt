package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.ScriptEntity
import com.example.data.model.VideoScript
import com.example.data.repository.ScriptRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface GenerationUiState {
    object Idle : GenerationUiState
    object Loading : GenerationUiState
    data class Success(val script: VideoScript) : GenerationUiState
    data class Error(val message: String) : GenerationUiState
}

class ScriptViewModel(private val repository: ScriptRepository) : ViewModel() {

    val savedScripts: StateFlow<List<ScriptEntity>> = repository.allScripts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _generationState = MutableStateFlow<GenerationUiState>(GenerationUiState.Idle)
    val generationState: StateFlow<GenerationUiState> = _generationState.asStateFlow()

    fun generateScript(topic: String, platform: String, tone: String) {
        viewModelScope.launch {
            _generationState.value = GenerationUiState.Loading
            try {
                val result = repository.generateAndSaveScript(topic, platform, tone)
                _generationState.value = GenerationUiState.Success(result)
            } catch (e: Exception) {
                _generationState.value = GenerationUiState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    fun deleteScript(id: Long) {
        viewModelScope.launch {
            repository.deleteScriptById(id)
        }
    }

    fun clearGenerationState() {
        _generationState.value = GenerationUiState.Idle
    }
}

class ScriptViewModelFactory(private val repository: ScriptRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScriptViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScriptViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
