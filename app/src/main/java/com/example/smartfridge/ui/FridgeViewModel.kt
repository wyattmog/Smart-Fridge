package com.example.smartfridge.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartfridge.data.RecipeRepository
import com.example.smartfridge.ml.IngredientDetector
import com.example.smartfridge.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface RecipeUiState {
    object Idle : RecipeUiState
    object Loading : RecipeUiState
    data class Success(val recipe: String) : RecipeUiState
    data class Error(val message: String) : RecipeUiState
}

class FridgeViewModel(application: Application) : AndroidViewModel(application) {
    private val recipeRepository = RecipeRepository(RetrofitInstance.api)
    private val ingredientDetector = IngredientDetector(application)

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
        uri?.let {
            val bitmap = it.toBitmap()
            detectIngredients(bitmap)
        }
    }

    private fun detectIngredients(bitmap: Bitmap) {
        viewModelScope.launch {
            try {
                val ingredients = withContext(Dispatchers.Default) {
                    ingredientDetector.detect(bitmap)
                }
                ingredientsText += ", " + ingredients.joinToString(", ")
            } catch (e: Exception) {
                Log.e("FridgeViewModel", "Error detecting ingredients", e)
                _recipeUiState.value = RecipeUiState.Error("Failed to detect ingredients: ${e.message}")
            }
        }
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
                val ingredients = ingredientsText.split(",").map { it.trim() }.joinToString(",")
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

    private fun Uri.toBitmap(): Bitmap {
        val context = getApplication<Application>()
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, this))
        } else {
            MediaStore.Images.Media.getBitmap(context.contentResolver, this)
        }
        return bitmap.copy(Bitmap.Config.ARGB_8888, true)
    }
}
