package com.example.hotel.data

import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId val userId: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val passwordHash: String = "",
    val role: String = "Guest"
)



