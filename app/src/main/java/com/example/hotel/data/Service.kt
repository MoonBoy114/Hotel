package com.example.hotel.data


import com.google.firebase.firestore.DocumentId

data class Service(
    @DocumentId val serviceId: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val imageUrl: String = ""
)