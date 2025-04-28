package com.example.hotel.data




data class Service(
    val serviceId: String = "",
    val name: String = "",
    val subTitle: String,
    val description: String = "",
    val imageUrl: String = "",
    val additionalPhotos: List<String> = emptyList()
)

