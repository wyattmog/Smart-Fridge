package com.example.smartfridge.ui

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartfridge.data.RecipeRepository
import com.example.smartfridge.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface RecipeUiState {
    object Idle : RecipeUiState
    object Loading : RecipeUiState
    data class Success(val recipe: String) : RecipeUiState
    data class Error(val message: String) : RecipeUiState
}

class FridgeViewModel : ViewModel() {
    private val recipeRepository = RecipeRepository(RetrofitInstance.api)

    var ingredientsText by mutableStateOf("")
        private set

    var selectedImageUri by mutableStateOf<Uri?>(null)
        private set

    private var sessionId by mutableStateOf<String?>(null)
    private var sessionToken by mutableStateOf<String?>(null)

    private val _recipeUiState = MutableStateFlow<RecipeUiState>(RecipeUiState.Idle)
    val recipeUiState = _recipeUiState.asStateFlow()

    init {
        startSession()
    }

    private fun startSession() {
        viewModelScope.launch {
            try {
                val session = recipeRepository.startSession()
                sessionId = session.sessionId
                sessionToken = session.sessionToken
            } catch (e: Exception) {
                Log.e("FridgeViewModel", "Failed to start session", e)
                _recipeUiState.value = RecipeUiState.Error("Failed to start session: ${e.message}")
            }
        }
    }

    fun onIngredientsTextChanged(newText: String) {
        ingredientsText = newText
    }

    fun onImageSelected(uri: Uri?) {
        selectedImageUri = uri
    }

    fun getRecipe() {
        if (ingredientsText.isBlank()) {
            _recipeUiState.value = RecipeUiState.Error("Please enter ingredients.")
            return
        }

        if (sessionId == null || sessionToken == null) {
            _recipeUiState.value = RecipeUiState.Error("Session not initialized. Please try again.")
            return
        }

        viewModelScope.launch {
            _recipeUiState.value = RecipeUiState.Loading
            try {
                val ingredients = ingredientsText.split(",").joinToString(",") { it.trim() }
                val recipe = recipeRepository.getRecipe(sessionId!!, sessionToken!!, ingredients)
                _recipeUiState.value = RecipeUiState.Success(recipe)
            } catch (e: Exception) {
                Log.e("FridgeViewModel", "Error getting recipe", e)
                _recipeUiState.value = RecipeUiState.Error("${e.message}")
            }
        }
    }
    
    fun resetUiState() {
        _recipeUiState.value = RecipeUiState.Idle
    }
}
