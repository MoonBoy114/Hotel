package com.example.hotel.data


import com.google.firebase.firestore.DocumentId

data class Service(
    @DocumentId
    val serviceId: String = "",
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val additionalPhotos: List<String> = emptyList()
)

