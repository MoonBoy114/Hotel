package com.example.hotel.data.entity

enum class RoomType(val displayName: String) {
    STANDARD("Стандартный"),
    SUPERIOR("Улучшенный"),
    LUXE("Люкс");

    companion object {
        fun fromDisplayName(displayName: String): RoomType? {
            return entries.find {it.displayName == displayName}
        }
    }
}