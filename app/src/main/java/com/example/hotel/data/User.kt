package com.example.hotel.data



data class User(
   val userId: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val passwordHash: String = "",
    val role: String = "Guest"
)



