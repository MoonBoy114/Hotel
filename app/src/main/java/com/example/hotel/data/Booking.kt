package com.example.hotel.data

data class Booking(
    val bookingId: String = "",
    val userId: String = "",
    val roomId: String = "",
    val checkInDate: String = "",
    val checkOutDate: String = "",
    val totalPrice: Double = 0.0,
)

