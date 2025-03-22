package com.example.hotel.data


import com.google.firebase.firestore.DocumentId


data class Booking(
    @DocumentId val bookingId: String = "",
    val userId: String = "",
    val roomId: String = "",
    val checkInDate: String = "",
    val checkOutDate: String = "",
    val totalPrice: Double = 0.0,
    val status: String = "Pending"
)

