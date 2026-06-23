package com.wastereporting.network

import kotlinx.serialization.Serializable

@Serializable
data class UploadResponse(
    val file_url: String
)
