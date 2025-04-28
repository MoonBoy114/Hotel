package com.example.hotel.data



data class News(

    val newsId: String = "",
    val title: String = "",
    val subTitle: String = "",
    val content: String = "",
    val imageUrl: String = "",
    val additionalPhotos: List<String> = emptyList()
)




