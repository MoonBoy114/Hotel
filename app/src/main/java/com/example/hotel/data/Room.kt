package com.example.hotel.data


import com.example.hotel.data.entity.RoomType


data class Room(
    val roomId: String = "",
    val type: String = RoomType.STANDARD.displayName,
    val name: String = "",
    val price: Float = 0.0f,
    val capacity: Int = 1,
    val description: String = "",
    val imageUrl: String = "",
    val additionalPhotos: List<String> = emptyList(),
    val isBooked: Boolean = false
)

