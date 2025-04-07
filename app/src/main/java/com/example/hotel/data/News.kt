package com.example.hotel.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class News(
    @DocumentId
    val newsId: String = "",
    val title: String = "",
    val subTitle: String = "",
    val content: String = "",
    val imageUrl: String = "",
    val additionalPhotos: List<String> = emptyList()
)




