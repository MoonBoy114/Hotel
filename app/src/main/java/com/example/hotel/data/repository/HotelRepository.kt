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
        return firestore.collection("rooms")
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject(Room::class.java) }
    }

    suspend fun insertRoom(room: Room): String {
        val docRef = firestore.collection("rooms").document()
        val newRoom = room.copy(roomId = docRef.id)
        docRef.set(newRoom).await()
        return docRef.id
    }

    suspend fun updateRoom(
        roomId: String,
        type: String? = null,
        name: String? = null,
        price: Double? = null,
        capacity: Int? = null,
        description: String? = null,
        imageUrl: String? = null
    ) {
        val updates = mutableMapOf<String, Any>()
        type?.let { updates["type"] = it }
        name?.let { updates["name"] = it }
        price?.let { updates["price"] = it }
        capacity?.let { updates["capacity"] = it }
        description?.let { updates["description"] = it }
        imageUrl?.let { updates["imageUrl"] = it }
        if (updates.isNotEmpty()) {
            firestore.collection("rooms").document(roomId).update(updates).await()
        }
    }

    suspend fun deleteRoom(roomId: String) {
        firestore.collection("rooms").document(roomId).delete().await()
    }

    // User
    suspend fun getUserByEmail(email: String): User? {
        return firestore.collection("users")
            .whereEqualTo("email", email)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?.toObject(User::class.java)
    }

    suspend fun insertUser(user: User): String {
        val docRef = firestore.collection("users").document()
        val newUser = user.copy(userId = docRef.id)
        docRef.set(newUser).await()
        return docRef.id
    }

    suspend fun isManager(userId: String): Boolean {
        val user = firestore.collection("users").document(userId).get().await().toObject(User::class.java)
        return user?.role == "Manager"
    }

    // News
    suspend fun getAllNews(): List<News> {
        return firestore.collection("news")
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject(News::class.java) }
    }

    suspend fun insertNews(news: News): String {
        val docRef = firestore.collection("news").document()
        val newNews = news.copy(newsId = docRef.id)
        docRef.set(newNews).await()
        return docRef.id
    }

    suspend fun updateNews(
        newsId: String,
        title: String? = null,
        description: String? = null,
        imageUrl: String? = null
    ) {
        val updates = mutableMapOf<String, Any>()
        title?.let { updates["title"] = it }
        description?.let { updates["description"] = it }
        imageUrl?.let { updates["imageUrl"] = it }
        if (updates.isNotEmpty()) {
            firestore.collection("news").document(newsId).update(updates).await()
        }
    }

    suspend fun deleteNews(newsId: String) {
        firestore.collection("news").document(newsId).delete().await()
    }

    // Service
    suspend fun getAllServices(): List<Service> {
        return firestore.collection("services")
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject(Service::class.java) }
    }

    suspend fun insertService(service: Service): String {
        val docRef = firestore.collection("services").document()
        val newService = service.copy(serviceId = docRef.id)
        docRef.set(newService).await()
        return docRef.id
    }

    suspend fun updateService(
        serviceId: String,
        name: String? = null,
        description: String? = null,
        price: Double? = null,
        imageUrl: String? = null
    ) {
        val updates = mutableMapOf<String, Any>()
        name?.let { updates["name"] = it }
        description?.let { updates["description"] = it }
        price?.let { updates["price"] = it }
        imageUrl?.let { updates["imageUrl"] = it }
        if (updates.isNotEmpty()) {
            firestore.collection("services").document(serviceId).update(updates).await()
        }
    }

    suspend fun deleteService(serviceId: String) {
        firestore.collection("services").document(serviceId).delete().await()
    }

    // Booking
    suspend fun getBookingsByUser(userId: String): List<Booking> {
        return firestore.collection("bookings")
            .whereEqualTo("userId", userId)
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject(Booking::class.java) }
    }

    suspend fun insertBooking(booking: Booking): String {
        val docRef = firestore.collection("bookings").document()
        val newBooking = booking.copy(bookingId = docRef.id)
        docRef.set(newBooking).await()
        return docRef.id
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
    }

    suspend fun deleteBooking(bookingId: String) {
        firestore.collection("bookings").document(bookingId).delete().await()
    }
}