package com.example.hotel.data.repository

import com.example.hotel.data.Booking
import com.example.hotel.data.News
import com.example.hotel.data.Room
import com.example.hotel.data.Service
import com.example.hotel.data.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class HotelRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    init {
        // Включение офлайн-поддержки
        firestore.firestoreSettings = com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
    }

    // Room
    suspend fun getAllRooms(): List<Room> {
        return try {
            firestore.collection("rooms")
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(Room::class.java) }
        } catch (e: Exception) {
            throw Exception("Ошибка получения списка комнат: ${e.message}")
        }
    }

    suspend fun insertRoom(room: Room): String {
        return try {
            val docRef = firestore.collection("rooms").document()
            val newRoom = room.copy(roomId = docRef.id)
            docRef.set(newRoom).await()
            docRef.id
        } catch (e: Exception) {
            throw Exception("Ошибка добавления комнаты: ${e.message}")
        }
    }

    suspend fun updateRoom(
        roomId: String,
        type: String? = null,
        name: String? = null,
        price: Double? = null,
        capacity: Int? = null,
        description: String? = null,
        imageUrl: String? = null,
        additionalPhotos: List<String>? = null
    ) {
        try {
            val updates = mutableMapOf<String, Any>()
            type?.let { updates["type"] = it }
            name?.let { updates["name"] = it }
            price?.let { updates["price"] = it }
            capacity?.let { updates["capacity"] = it }
            description?.let { updates["description"] = it }
            imageUrl?.let { updates["imageUrl"] = it }
            additionalPhotos?.let { updates["additionalPhotos"] = it }
            if (updates.isNotEmpty()) {
                firestore.collection("rooms").document(roomId).update(updates).await()
            }
        } catch (e: Exception) {
            throw Exception("Ошибка обновления комнаты: ${e.message}")
        }
    }

    suspend fun deleteRoom(roomId: String) {
        try {
            firestore.collection("rooms").document(roomId).delete().await()
        } catch (e: Exception) {
            throw Exception("Ошибка удаления комнаты: ${e.message}")
        }
    }

    // User
    suspend fun getUserByEmail(email: String): User? {
        return try {
            firestore.collection("users")
                .whereEqualTo("email", email)
                .get()
                .await()
                .documents
                .firstOrNull()
                ?.toObject(User::class.java)
        } catch (e: Exception) {
            throw Exception("Ошибка получения пользователя: ${e.message}")
        }
    }

    suspend fun insertUser(user: User): String {
        return try {
            val docRef = firestore.collection("users").document()
            val newUser = user.copy(userId = docRef.id)
            docRef.set(newUser).await()
            docRef.id
        } catch (e: Exception) {
            throw Exception("Ошибка добавления пользователя: ${e.message}")
        }
    }

    suspend fun isManager(userId: String): Boolean {
        return try {
            val user = firestore.collection("users").document(userId).get().await().toObject(User::class.java)
            user?.role == "Manager"
        } catch (e: Exception) {
            throw Exception("Ошибка проверки роли пользователя: ${e.message}")
        }
    }

    // News
    suspend fun getAllNews(): List<News> {
        return try {
            firestore.collection("news")
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(News::class.java) }
        } catch (e: Exception) {
            throw Exception("Ошибка получения списка новостей: ${e.message}")
        }
    }

    suspend fun getNewsById(newsId: String): News? {
        return try {
            firestore.collection("news")
                .document(newsId)
                .get()
                .await()
                .toObject(News::class.java)
        } catch (e: Exception) {
            throw Exception("Ошибка получения новости: ${e.message}")
        }
    }

    suspend fun addNews(news: News): String {
        return try {
            val docRef = firestore.collection("news").document()
            val newNews = news.copy(newsId = docRef.id)
            docRef.set(newNews).await()
            docRef.id
        } catch (e: Exception) {
            throw Exception("Ошибка добавления новости: ${e.message}")
        }
    }

    suspend fun updateNews(
        newsId: String,
        title: String? = null,
        subTitle: String? = null, // Добавляем subTitle
        content: String? = null,
        imageUrl: String? = null,
        additionalPhotos: List<String>? = null
    ) {
        try {
            val updates = mutableMapOf<String, Any>()
            title?.let { updates["title"] = it }
            subTitle?.let { updates["subTitle"] = it } // Поддержка subTitle
            content?.let { updates["content"] = it }
            imageUrl?.let { updates["imageUrl"] = it }
            additionalPhotos?.let { updates["additionalPhotos"] = it }
            if (updates.isNotEmpty()) {
                firestore.collection("news").document(newsId).update(updates).await()
            }
        } catch (e: Exception) {
            throw Exception("Ошибка обновления новости: ${e.message}")
        }
    }

    suspend fun deleteNews(newsId: String) {
        try {
            firestore.collection("news").document(newsId).delete().await()
        } catch (e: Exception) {
            throw Exception("Ошибка удаления новости: ${e.message}")
        }
    }

    // Service
    suspend fun getAllServices(): List<Service> {
        return try {
            firestore.collection("services")
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(Service::class.java) }
        } catch (e: Exception) {
            throw Exception("Ошибка получения списка акций: ${e.message}")
        }
    }

    suspend fun insertService(service: Service): String {
        return try {
            val docRef = firestore.collection("services").document()
            val newService = service.copy(serviceId = docRef.id)
            docRef.set(newService).await()
            docRef.id
        } catch (e: Exception) {
            throw Exception("Ошибка добавления акции: ${e.message}")
        }
    }

    suspend fun updateService(
        serviceId: String,
        name: String? = null,
        description: String? = null,
        imageUrl: String? = null,
        additionalPhotos: List<String>? = null
    ) {
        try {
            val updates = mutableMapOf<String, Any>()
            name?.let { updates["name"] = it }
            description?.let { updates["description"] = it }
            imageUrl?.let { updates["imageUrl"] = it }
            additionalPhotos?.let { updates["additionalPhotos"] = it }
            if (updates.isNotEmpty()) {
                firestore.collection("services").document(serviceId).update(updates).await()
            }
        } catch (e: Exception) {
            throw Exception("Ошибка обновления акции: ${e.message}")
        }
    }

    suspend fun deleteService(serviceId: String) {
        try {
            firestore.collection("services").document(serviceId).delete().await()
        } catch (e: Exception) {
            throw Exception("Ошибка удаления акции: ${e.message}")
        }
    }

    // Booking
    suspend fun getBookingsByUser(userId: String): List<Booking> {
        return try {
            firestore.collection("bookings")
                .whereEqualTo("userId", userId)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(Booking::class.java) }
        } catch (e: Exception) {
            throw Exception("Ошибка получения списка бронирований: ${e.message}")
        }
    }

    suspend fun insertBooking(booking: Booking): String {
        return try {
            val docRef = firestore.collection("bookings").document()
            val newBooking = booking.copy(bookingId = docRef.id)
            docRef.set(newBooking).await()
            docRef.id
        } catch (e: Exception) {
            throw Exception("Ошибка добавления бронирования: ${e.message}")
        }
    }

    suspend fun updateBooking(
        bookingId: String,
        userId: String? = null,
        roomId: String? = null,
        checkInDate: String? = null,
        checkOutDate: String? = null,
        totalPrice: Double? = null,
        status: String? = null
    ) {
        try {
            val updates = mutableMapOf<String, Any>()
            userId?.let { updates["userId"] = it }
            roomId?.let { updates["roomId"] = it }
            checkInDate?.let { updates["checkInDate"] = it }
            checkOutDate?.let { updates["checkOutDate"] = it }
            totalPrice?.let { updates["totalPrice"] = it }
            status?.let { updates["status"] = it }
            if (updates.isNotEmpty()) {
                firestore.collection("bookings").document(bookingId).update(updates).await()
            }
        } catch (e: Exception) {
            throw Exception("Ошибка обновления бронирования: ${e.message}")
        }
    }

    suspend fun deleteBooking(bookingId: String) {
        try {
            firestore.collection("bookings").document(bookingId).delete().await()
        } catch (e: Exception) {
            throw Exception("Ошибка удаления бронирования: ${e.message}")
        }
    }
}