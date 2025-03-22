package com.example.hotel.data


import com.example.hotel.data.entity.RoomType
import com.google.firebase.firestore.DocumentId

data class Room(
    @DocumentId
    val roomId: String = "",
    val type: String = RoomType.STANDARD.displayName,
    val name: String = "",
    val price: Double = 0.0,
    val capacity: Int = 1,
    val description: String = "",
    val imageUrl: String = "",
    val additionalPhotos: List<String> = emptyList()
)

