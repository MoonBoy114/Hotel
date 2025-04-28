package com.example.hotel.data.repository

import android.util.Log
import com.example.hotel.HotelApp
import com.example.hotel.data.Booking
import com.example.hotel.data.News
import com.example.hotel.data.Room
import com.example.hotel.data.Service
import com.example.hotel.data.User
import io.appwrite.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import io.appwrite.exceptions.AppwriteException
import io.appwrite.services.Databases
import io.appwrite.services.Storage
import java.io.File


class HotelRepository(
    public val databases: Databases = HotelApp.databases,
    public val storage: Storage = HotelApp.storage
) {
    companion object {
        const val DATABASE_ID = "67f62e6000306f467a96"
        const val USERS_COLLECTION_ID = "users"
        const val ROOMS_COLLECTION_ID = "rooms"
        const val NEWS_COLLECTION_ID = "news"
        const val SERVICES_COLLECTION_ID = "services"
        const val BOOKINGS_COLLECTION_ID = "bookings"
        const val IMAGES_BUCKET_ID = "images"
    }

    // Room
    suspend fun getAllRooms(): List<Room> = withContext(Dispatchers.IO) {
        try {
            val response = databases.listDocuments(
                databaseId = DATABASE_ID,
                collectionId = ROOMS_COLLECTION_ID
            )
            response.documents.map { document ->
                Room(
                    roomId = document.id,
                    type = document.data["type"] as String,
                    name = document.data["name"] as String,
                    price = (document.data["price"] as Number).toFloat(),
                    capacity = (document.data["capacity"] as Number).toInt(),
                    description = document.data["description"] as String,
                    imageUrl = document.data["imageUrl"] as String,
                    additionalPhotos = (document.data["additionalPhotos"] as List<*>).map { it as String },
                    isBooked = document.data["isBooked"] as Boolean? ?: false
                )
            }
        } catch (e: AppwriteException) {
            throw Exception("Ошибка получения списка комнат: ${e.message}")
        }
    }

    suspend fun insertRoom(room: Room): String = withContext(Dispatchers.IO) {
        try {
            val response = databases.createDocument(
                databaseId = DATABASE_ID,
                collectionId = ROOMS_COLLECTION_ID,
                documentId = "unique()",
                data = mapOf(
                    "type" to room.type,
                    "name" to room.name,
                    "price" to room.price,
                    "capacity" to room.capacity,
                    "description" to room.description,
                    "imageUrl" to room.imageUrl,
                    "additionalPhotos" to room.additionalPhotos,
                    "isBooked" to room.isBooked
                )
            )
            response.id
        } catch (e: AppwriteException) {
            throw Exception("Ошибка добавления комнаты: ${e.message}")
        }
    }

    suspend fun updateRoom(
        roomId: String,
        type: String? = null,
        name: String? = null,
        price: Float? = null,
        capacity: Int? = null,
        description: String? = null,
        imageUrl: String? = null,
        additionalPhotos: List<String>? = null,
        isBooked: Boolean? = null
    ) = withContext(Dispatchers.IO) {
        try {
            val updates = mutableMapOf<String, Any>()
            type?.let { updates["type"] = it }
            name?.let { updates["name"] = it }
            price?.let { updates["price"] = it }
            capacity?.let { updates["capacity"] = it }
            description?.let { updates["description"] = it }
            imageUrl?.let { updates["imageUrl"] = it }
            additionalPhotos?.let { updates["additionalPhotos"] = it }
            isBooked?.let { updates["isBooked"] = it }
            if (updates.isNotEmpty()) {
                databases.updateDocument(
                    databaseId = DATABASE_ID,
                    collectionId = ROOMS_COLLECTION_ID,
                    documentId = roomId,
                    data = updates
                )
            }
        } catch (e: AppwriteException) {
            throw Exception("Ошибка обновления комнаты: ${e.message}")
        }
    }

    suspend fun deleteRoom(roomId: String) = withContext(Dispatchers.IO) {
        try {
            databases.deleteDocument(
                databaseId = DATABASE_ID,
                collectionId = ROOMS_COLLECTION_ID,
                documentId = roomId
            )
        } catch (e: AppwriteException) {
            throw Exception("Ошибка удаления комнаты: ${e.message}")
        }
    }

    // User
    suspend fun getUserByEmail(email: String): User? = withContext(Dispatchers.IO) {
        try {
            val response = databases.listDocuments(
                databaseId = DATABASE_ID,
                collectionId = USERS_COLLECTION_ID,
                queries = listOf(Query.equal("email", email))
            )
            response.documents.firstOrNull()?.let { document ->
                User(
                    userId = document.id,
                    name = document.data["name"] as String,
                    email = document.data["email"] as String,
                    phone = document.data["phone"] as String,
                    passwordHash = document.data["passwordHash"] as String,
                    role = document.data["role"] as String
                )
            }
        } catch (e: AppwriteException) {
            throw Exception("Ошибка получения пользователя: ${e.message}")
        }
    }

    suspend fun insertUser(user: User): String = withContext(Dispatchers.IO) {
        try {
            val response = databases.createDocument(
                databaseId = DATABASE_ID,
                collectionId = USERS_COLLECTION_ID,
                documentId = "unique()",
                data = mapOf(
                    "name" to user.name,
                    "email" to user.email,
                    "phone" to user.phone,
                    "passwordHash" to user.passwordHash,
                    "role" to user.role
                )
            )
            response.id
        } catch (e: AppwriteException) {
            throw Exception("Ошибка добавления пользователя: ${e.message}")
        }
    }

    suspend fun isManager(userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val document = databases.getDocument(
                databaseId = DATABASE_ID,
                collectionId = USERS_COLLECTION_ID,
                documentId = userId
            )
            (document.data["role"] as String) == "Manager"
        } catch (e: AppwriteException) {
            throw Exception("Ошибка проверки роли пользователя: ${e.message}")
        }
    }

    suspend fun getAllNews(): List<News> = withContext(Dispatchers.IO) {
        try {
            val response = databases.listDocuments(
                databaseId = DATABASE_ID,
                collectionId = NEWS_COLLECTION_ID
            )
            val newsList = response.documents.map { document ->
                News(
                    newsId = document.id,
                    title = document.data["title"] as String,
                    subTitle = document.data["subTitle"] as String,
                    content = document.data["content"] as String,
                    imageUrl = document.data["imageUrl"] as String,
                    additionalPhotos = (document.data["additionalPhotos"] as List<*>).map { it as String }
                )
            }
            Log.d("HotelRepository", "News fetched: $newsList")
            newsList
        } catch (e: AppwriteException) {
            Log.e("HotelRepository", "Failed to fetch news: ${e.message}", e)
            throw Exception("Ошибка получения списка новостей: ${e.message}")
        }
    }

    suspend fun getNewsById(newsId: String): News? = withContext(Dispatchers.IO) {
        try {
            val document = databases.getDocument(
                databaseId = DATABASE_ID,
                collectionId = NEWS_COLLECTION_ID,
                documentId = newsId
            )
            News(
                newsId = document.id,
                title = document.data["title"] as String,
                subTitle = document.data["subTitle"] as String,
                content = document.data["content"] as String,
                imageUrl = document.data["imageUrl"] as String,
                additionalPhotos = (document.data["additionalPhotos"] as List<*>).map { it as String }
            )
        } catch (e: AppwriteException) {
            throw Exception("Ошибка получения новости: ${e.message}")
        }
    }

    suspend fun addNews(news: News): String = withContext(Dispatchers.IO) {
        try {
            val response = databases.createDocument(
                databaseId = DATABASE_ID,
                collectionId = NEWS_COLLECTION_ID,
                documentId = "unique()",
                data = mapOf(
                    "title" to news.title,
                    "subTitle" to news.subTitle,
                    "content" to news.content,
                    "imageUrl" to news.imageUrl,
                    "additionalPhotos" to news.additionalPhotos
                )
            )
            response.id
        } catch (e: AppwriteException) {
            throw Exception("Ошибка добавления новости: ${e.message}")
        }
    }

    suspend fun updateNews(
        newsId: String,
        title: String? = null,
        subTitle: String? = null,
        content: String? = null,
        imageUrl: String? = null,
        additionalPhotos: List<String>? = null
    ) = withContext(Dispatchers.IO) {
        try {
            val updates = mutableMapOf<String, Any>()
            title?.let { updates["title"] = it }
            subTitle?.let { updates["subTitle"] = it }
            content?.let { updates["content"] = it }
            imageUrl?.let { updates["imageUrl"] = it }
            additionalPhotos?.let { updates["additionalPhotos"] = it }
            if (updates.isNotEmpty()) {
                databases.updateDocument(
                    databaseId = DATABASE_ID,
                    collectionId = NEWS_COLLECTION_ID,
                    documentId = newsId,
                    data = updates
                )
            }
        } catch (e: AppwriteException) {
            throw Exception("Ошибка обновления новости: ${e.message}")
        }
    }

    suspend fun deleteNews(newsId: String) {
        try {
            databases.deleteDocument(
                databaseId = DATABASE_ID,
                collectionId = NEWS_COLLECTION_ID,
                documentId = newsId
            )
        } catch (e: AppwriteException) {
            throw e
        }
    }

    // Service
    suspend fun getAllServices(): List<Service> = withContext(Dispatchers.IO) {
        try {
            val response = databases.listDocuments(
                databaseId = DATABASE_ID,
                collectionId = SERVICES_COLLECTION_ID
            )
            response.documents.map { document ->
                Service(
                    serviceId = document.id,
                    name = document.data["name"] as String,
                    subTitle = document.data["subTitle"] as String,
                    description = document.data["description"] as String,
                    imageUrl = document.data["imageUrl"] as String,
                    additionalPhotos = (document.data["additionalPhotos"] as List<*>).map { it as String }
                )
            }
        } catch (e: AppwriteException) {
            throw Exception("Ошибка получения списка акций: ${e.message}")
        }
    }

    suspend fun insertService(service: Service): String = withContext(Dispatchers.IO) {
        try {
            val response = databases.createDocument(
                databaseId = DATABASE_ID,
                collectionId = SERVICES_COLLECTION_ID,
                documentId = "unique()",
                data = mapOf(
                    "name" to service.name,
                    "subTitle" to service.subTitle,
                    "description" to service.description,
                    "imageUrl" to service.imageUrl,
                    "additionalPhotos" to service.additionalPhotos
                )
            )
            response.id
        } catch (e: AppwriteException) {
            throw Exception("Ошибка добавления акции: ${e.message}")
        }
    }

    suspend fun updateService(
        serviceId: String,
        name: String? = null,
        subTitle: String? = null,
        description: String? = null,
        imageUrl: String? = null,
        additionalPhotos: List<String>? = null
    ) = withContext(Dispatchers.IO) {
        try {
            val updates = mutableMapOf<String, Any>()
            name?.let { updates["name"] = it }
            subTitle?.let { updates["subTitle"] = it }
            description?.let { updates["description"] = it }
            imageUrl?.let { updates["imageUrl"] = it }
            additionalPhotos?.let { updates["additionalPhotos"] = it }
            if (updates.isNotEmpty()) {
                databases.updateDocument(
                    databaseId = DATABASE_ID,
                    collectionId = SERVICES_COLLECTION_ID,
                    documentId = serviceId,
                    data = updates
                )
            }
        } catch (e: AppwriteException) {
            throw Exception("Ошибка обновления акции: ${e.message}")
        }
    }

    suspend fun deleteService(serviceId: String) = withContext(Dispatchers.IO) {
        try {
            databases.deleteDocument(
                databaseId = DATABASE_ID,
                collectionId = SERVICES_COLLECTION_ID,
                documentId = serviceId
            )
        } catch (e: AppwriteException) {
            throw Exception("Ошибка удаления акции: ${e.message}")
        }
    }

    // Booking
    suspend fun getBookingsByUser(userId: String): List<Booking> = withContext(Dispatchers.IO) {
        try {
            val response = databases.listDocuments(
                databaseId = DATABASE_ID,
                collectionId = BOOKINGS_COLLECTION_ID,
                queries = listOf(Query.equal("userId", userId))
            )
            response.documents.map { document ->
                Booking(
                    bookingId = document.id,
                    userId = document.data["userId"] as String,
                    roomId = document.data["roomId"] as String,
                    checkInDate = document.data["checkInDate"] as String,
                    checkOutDate = document.data["checkOutDate"] as String,
                    totalPrice = (document.data["totalPrice"] as Number).toDouble()
                )
            }
        } catch (e: AppwriteException) {
            throw Exception("Ошибка получения списка бронирований: ${e.message}")
        }
    }

    suspend fun insertBooking(booking: Booking): String = withContext(Dispatchers.IO) {
        try {
            val response = databases.createDocument(
                databaseId = DATABASE_ID,
                collectionId = BOOKINGS_COLLECTION_ID,
                documentId = "unique()",
                data = mapOf(
                    "userId" to booking.userId,
                    "roomId" to booking.roomId,
                    "checkInDate" to booking.checkInDate,
                    "checkOutDate" to booking.checkOutDate,
                    "totalPrice" to booking.totalPrice
                )
            )
            response.id
        } catch (e: AppwriteException) {
            throw Exception("Ошибка добавления бронирования: ${e.message}")
        }
    }

    suspend fun updateBooking(
        bookingId: String,
        userId: String? = null,
        roomId: String? = null,
        checkInDate: String? = null,
        checkOutDate: String? = null,
        totalPrice: Double? = null
    ) = withContext(Dispatchers.IO) {
        try {
            val updates = mutableMapOf<String, Any>()
            userId?.let { updates["userId"] = it }
            roomId?.let { updates["roomId"] = it }
            checkInDate?.let { updates["checkInDate"] = it }
            checkOutDate?.let { updates["checkOutDate"] = it }
            totalPrice?.let { updates["totalPrice"] = it }
            if (updates.isNotEmpty()) {
                databases.updateDocument(
                    databaseId = DATABASE_ID,
                    collectionId = BOOKINGS_COLLECTION_ID,
                    documentId = bookingId,
                    data = updates
                )
            }
        } catch (e: AppwriteException) {
            throw Exception("Ошибка обновления бронирования: ${e.message}")
        }
    }

    suspend fun deleteBooking(bookingId: String) = withContext(Dispatchers.IO) {
        try {
            databases.deleteDocument(
                databaseId = DATABASE_ID,
                collectionId = BOOKINGS_COLLECTION_ID,
                documentId = bookingId
            )
        } catch (e: AppwriteException) {
            throw Exception("Ошибка удаления бронирования: ${e.message}")
        }
    }

    // Загрузка файла в Storage
    suspend fun uploadFile(file: File): String = withContext(Dispatchers.IO) {
        try {
            val response = storage.createFile(
                bucketId = IMAGES_BUCKET_ID,
                fileId = "unique()",
                file = io.appwrite.models.InputFile.fromFile(file)
            )
            "${HotelApp.client.endpoint}/storage/buckets/$IMAGES_BUCKET_ID/files/${response.id}/view?project=${HotelApp.projectId}"
        } catch (e: AppwriteException) {
            throw Exception("Ошибка загрузки файла: ${e.message}")
        }
    }
}