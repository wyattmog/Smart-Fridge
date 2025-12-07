package com.example.smartfridge.network

import com.example.smartfridge.data.model.RecipeResponse
import com.example.smartfridge.data.model.SessionResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("/api/sessions/start")
    suspend fun startSession(): SessionResponse

    @GET("/api/recipes/generate")
    suspend fun generateRecipe(
        @Header("x-session-id") sessionId: String,
        @Header("x-session-token") sessionToken: String,
        @Query("ingredients") ingredients: String
    ): RecipeResponse
}
