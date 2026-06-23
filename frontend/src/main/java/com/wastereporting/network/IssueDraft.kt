package com.wastereporting.network

object IssueDraft {
    var imageBytes: ByteArray? = null
    var imagesList: List<ByteArray> = emptyList()
    var category: String = ""
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var address: String = ""
    
    fun clear() {
        imageBytes = null
        imagesList = emptyList()
        category = ""
        latitude = 0.0
        longitude = 0.0
        address = ""
    }
}
