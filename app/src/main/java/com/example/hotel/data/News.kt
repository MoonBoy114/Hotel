package com.example.hotel.data

import com.google.firebase.firestore.DocumentId

data class News(
    @DocumentId val newsId: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String = ""
)