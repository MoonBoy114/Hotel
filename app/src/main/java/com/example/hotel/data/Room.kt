package com.example.hotel.data


import com.google.firebase.firestore.DocumentId

data class Room(
    @DocumentId val roomId: String = "",
    val type: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val capacity: Int = 0,
    val description: String = "",
    val imageUrl: String = ""
)