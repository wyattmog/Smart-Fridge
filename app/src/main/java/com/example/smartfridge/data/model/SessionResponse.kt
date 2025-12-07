package com.example.smartfridge.data.model

import com.squareup.moshi.Json

data class SessionResponse(
    @Json(name = "sessionId")
    val sessionId: String,
    @Json(name = "token")
    val sessionToken: String
)
