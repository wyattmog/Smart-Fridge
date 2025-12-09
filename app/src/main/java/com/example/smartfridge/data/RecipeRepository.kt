package com.example.smartfridge.data

import com.example.smartfridge.data.model.SessionResponse
import com.example.smartfridge.network.ApiService

class RecipeRepository(private val apiService: ApiService) {

    suspend fun startSession(): SessionResponse {
        return apiService.startSession()
    }

    suspend fun getRecipe(sessionId: String, sessionToken: String, ingredients: String): String {
        val response = apiService.generateRecipe(sessionId, sessionToken, ingredients)
        return response.recipe
    }


}
