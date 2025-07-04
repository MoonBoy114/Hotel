package com.example.hotel.data.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hotel.HotelApp
import com.example.hotel.data.Booking
import com.example.hotel.data.News
import com.example.hotel.data.Room
import com.example.hotel.data.Service
import com.example.hotel.data.User
import com.example.hotel.data.entity.RoomType
import com.example.hotel.data.repository.HotelRepository
import io.appwrite.Query
import io.appwrite.exceptions.AppwriteException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HotelViewModel(
    private val repository: HotelRepository
) : ViewModel() {
    private val _currentUser = MutableLiveData<User?>(null)
    val currentUser: LiveData<User?> get() = _currentUser

    private val _bookings = MutableLiveData<List<Booking>>(emptyList())
    val bookings: LiveData<List<Booking>> get() = _bookings

    private val _news = MutableLiveData<List<News>>(emptyList())
    val news: LiveData<List<News>> get() = _news

    private val _rooms = MutableLiveData<List<Room>>(emptyList())
    val rooms: LiveData<List<Room>> get() = _rooms

    private val _services = MutableLiveData<List<Service>>(emptyList())
    val services: LiveData<List<Service>> get() = _services

    private val _users = MutableLiveData<List<User>>(emptyList())
    val users: LiveData<List<User>> get() = _users

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _isManager = MutableLiveData<Boolean>(false)
    val isManager: LiveData<Boolean> get() = _isManager

    // Очистка номера телефона
    fun cleanPhoneNumber(phone: String): String {
        val cleaned = phone.replace("[^0-9]".toRegex(), "")
        return if (cleaned.startsWith("8") || cleaned.startsWith("+7")) {
            "7" + cleaned.substring(1)
        } else {
            cleaned
        }
    }

    // Валидация номера телефона
    private fun isValidPhoneNumber(phone: String): Boolean {
        val cleaned = cleanPhoneNumber(phone)
        return cleaned.length == 11 && cleaned.startsWith("7")
    }

    // Форматирование номера для UI
    fun formatPhoneNumber(phone: String): String {
        if (phone.length != 11 || !phone.startsWith("7")) return phone
        val code = phone.substring(0, 1)
        val part1 = phone.substring(1, 4)
        val part2 = phone.substring(4, 7)
        val part3 = phone.substring(7, 9)
        val part4 = phone.substring(9, 11)
        return "+$code ($part1) $part2-$part3-$part4"
    }

    // Вход пользователя
    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            try {
                val user = repository.getUserByEmail(email)
                if (user == null) {
                    _errorMessage.value = "Пользователь с таким email не найден"
                    return@launch
                }
                if (user.passwordHash != password) {
                    _errorMessage.value = "Неверный пароль"
                    return@launch
                }
                _currentUser.value = user
                loadNews()
                loadRoomsAndBookings() // Используем только loadRoomsAndBookings
                loadServices()
                loadUsers()
                checkManagerRole(user.userId)
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка входа: ${e.message}"
            }
        }
    }

    // Обновление данных пользователя
    fun updateUser(user: User) {
        viewModelScope.launch {
            try {
                repository.updateUser(
                    userId = user.userId,
                    name = user.name,
                    email = user.email,
                    phone = user.phone
                )
                _currentUser.value = user
                loadUsers() // Обновляем список пользователей
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка обновления данных: ${e.message}"
            }
        }
    }

    // Регистрация нового пользователя
    fun registerUser(name: String, email: String, phone: String, password: String) {
        viewModelScope.launch {
            try {
                if (!isValidPhoneNumber(phone)) {
                    _errorMessage.value =
                        "Номер телефона должен быть в формате +7XXXXXXXXXX (11 цифр)"
                    return@launch
                }

                val emailResult = isEmailTaken(email)
                emailResult.onSuccess { emailTaken ->
                    if (emailTaken) {
                        _errorMessage.value = "Этот email уже зарегистрирован"
                        return@launch
                    }

                    val phoneResult = isPhoneTaken(phone)
                    phoneResult.onSuccess { phoneTaken ->
                        if (phoneTaken) {
                            _errorMessage.value = "Этот номер телефона уже зарегистрирован"
                            return@launch
                        }

                        val cleanedPhone = cleanPhoneNumber(phone)
                        val user = User(
                            userId = "unique()",
                            name = name,
                            email = email,
                            phone = cleanedPhone,
                            passwordHash = password,
                            role = "Guest"
                        )
                        val userId = repository.insertUser(user)
                        _currentUser.value = user.copy(userId = userId)
                        loadNews()
                        loadRoomsAndBookings()
                        loadServices()
                        loadUsers()
                        checkManagerRole(userId)
                    }.onFailure { e ->
                        _errorMessage.value = "Ошибка проверки телефона: ${e.message}"
                    }
                }.onFailure { e ->
                    _errorMessage.value = "Ошибка проверки email: ${e.message}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка регистрации: ${e.message}"
            }
        }
    }

    // Проверка, занят ли email
    suspend fun isEmailTaken(email: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = repository.databases.listDocuments(
                databaseId = HotelRepository.DATABASE_ID,
                collectionId = HotelRepository.USERS_COLLECTION_ID,
                queries = listOf(Query.equal("email", email))
            )
            Result.success(response.documents.isNotEmpty())
        } catch (e: AppwriteException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Проверка, занят ли телефон
    suspend fun isPhoneTaken(phone: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val cleanedPhone = cleanPhoneNumber(phone)
            val response = repository.databases.listDocuments(
                databaseId = HotelRepository.DATABASE_ID,
                collectionId = HotelRepository.USERS_COLLECTION_ID,
                queries = listOf(Query.equal("phone", cleanedPhone))
            )
            Result.success(response.documents.isNotEmpty())
        } catch (e: AppwriteException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Проверка роли менеджера
    private fun checkManagerRole(userId: String) {
        viewModelScope.launch {
            try {
                val isManager = repository.isManager(userId)
                _isManager.value = isManager
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    // Выход из системы
    fun logout() {
        viewModelScope.launch {
            try {
                _currentUser.value = null
                _bookings.value = emptyList()
                _news.value = emptyList()
                _rooms.value = emptyList()
                _services.value = emptyList()
                _users.value = emptyList()
                _isManager.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка выхода: ${e.message}"
            }
        }
    }

    // Бронирование комнаты
    fun bookRoom(
        room: Room,
        userId: String,
        checkInDate: String,
        checkOutDate: String,
        currentMoney: Float
    ) {
        viewModelScope.launch {
            try {
                Log.d(
                    "HotelViewModel",
                    "bookRoom called with roomId: ${room.roomId}, userId: $userId, checkIn: $checkInDate, checkOut: $checkOutDate, money: $currentMoney"
                )

                if (currentMoney < room.price) {
                    val shortfall = room.price - currentMoney
                    _errorMessage.value = "Недостаточно средств. Не хватает: ${shortfall.toInt()} ₽"
                    Log.e("HotelViewModel", "Insufficient funds. Shortfall: ${shortfall.toInt()} ₽")
                    return@launch
                }

                if (room.isBooked) {
                    _errorMessage.value = "Комната уже забронирована"
                    Log.e("HotelViewModel", "Room ${room.roomId} is already booked")
                    return@launch
                }

                val booking = Booking(
                    bookingId = "unique()",
                    userId = userId,
                    roomId = room.roomId,
                    checkInDate = checkInDate,
                    checkOutDate = checkOutDate,
                    totalPrice = room.price.toDouble()
                )

                Log.d("HotelViewModel", "Inserting booking: $booking")
                repository.insertBooking(booking)

                Log.d("HotelViewModel", "Marking room as booked: ${room.roomId}")
                repository.updateRoom(roomId = room.roomId, isBooked = true)

                Log.d("HotelViewModel", "Booking successful. New balance: ${currentMoney - room.price}")
                loadRoomsAndBookings()
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка бронирования: ${e.message}"
                Log.e("HotelViewModel", "Booking error: ${e.message}", e)
            }
        }
    }

    // Загрузка комнат и бронирований
    fun loadRoomsAndBookings() {
        viewModelScope.launch {
            try {
                Log.d("HotelViewModel", "Starting loadRoomsAndBookings")
                val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                val currentDate = LocalDate.now().format(formatter)
                Log.d("HotelViewModel", "Current date: $currentDate")
                val allBookings = repository.getAllBookings()
                Log.d("HotelViewModel", "All bookings: $allBookings")
                val roomList = repository.getAllRooms()

                roomList.forEach { room ->
                    val booking = allBookings.find { it.roomId == room.roomId }
                    if (booking != null && room.isBooked) {
                        Log.d(
                            "HotelViewModel",
                            "Checking booking for room ${room.roomId}: checkOutDate=${booking.checkOutDate}"
                        )
                        if (currentDate >= booking.checkOutDate) {
                            Log.d(
                                "HotelViewModel",
                                "Booking ${booking.bookingId} expired for room ${room.roomId}. Current date: $currentDate, CheckOutDate: ${booking.checkOutDate}"
                            )
                            repository.updateRoom(roomId = room.roomId, isBooked = false)
                            repository.deleteBooking(booking.bookingId)
                            Log.d(
                                "HotelViewModel",
                                "Booking ${booking.bookingId} deleted. Room ${room.roomId} is now available."
                            )
                        }
                    }
                }

                _rooms.value = repository.getAllRooms()
                Log.d("HotelViewModel", "Updated rooms: ${_rooms.value}")

                val userRole = _currentUser.value?.role ?: "Guest"
                if (userRole == "Manager") {
                    val bookingList = repository.getAllBookings()
                    Log.d("HotelViewModel", "Loaded all bookings for manager: $bookingList")
                    _bookings.value = bookingList
                } else {
                    val userId = _currentUser.value?.userId
                    if (userId != null) {
                        val bookingList = repository.getBookingsByUser(userId)
                        Log.d("HotelViewModel", "Loaded bookings for user $userId: $bookingList")
                        _bookings.value = bookingList
                    } else {
                        _bookings.value = emptyList()
                    }
                }
            } catch (e: Exception) {
                Log.e("HotelViewModel", "Error in loadRoomsAndBookings: ${e.message}", e)
                _errorMessage.value = e.message
            }
        }
    }

    // Добавление новости
    fun addNews(news: News) {
        viewModelScope.launch {
            try {
                if (news.title.isBlank() || news.title.length > 50) {
                    _errorMessage.value = "Заголовок должен быть от 1 до 50 символов"
                    return@launch
                }
                if (news.subTitle.length > 20) {
                    _errorMessage.value = "Подзаголовок должен быть до 20 символов"
                    return@launch
                }
                if (news.content.isBlank() || news.content.length > 200) {
                    _errorMessage.value = "Описание должно быть от 1 до 200 символов"
                    return@launch
                }
                if (news.additionalPhotos.isEmpty()) {
                    _errorMessage.value =
                        "Необходимо добавить хотя бы одну дополнительную фотографию"
                    return@launch
                }
                if (news.additionalPhotos.size > 5) {
                    _errorMessage.value = "Максимум 5 дополнительных фотографий"
                    return@launch
                }

                repository.addNews(news)
                loadNews()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    // Обновление новости
    fun updateNews(news: News) {
        viewModelScope.launch {
            try {
                if (news.title.isBlank() || news.title.length > 50) {
                    _errorMessage.value = "Заголовок должен быть от 1 до 50 символов"
                    return@launch
                }
                if (news.subTitle.length > 20) {
                    _errorMessage.value = "Подзаголовок должен быть до 20 символов"
                    return@launch
                }
                if (news.content.isBlank() || news.content.length > 200) {
                    _errorMessage.value = "Описание должно быть от 1 до 200 символов"
                    return@launch
                }
                if (news.additionalPhotos.isEmpty()) {
                    _errorMessage.value =
                        "Необходимо добавить хотя бы одну дополнительную фотографию"
                    return@launch
                }
                if (news.additionalPhotos.size > 5) {
                    _errorMessage.value = "Максимум 5 дополнительных фотографий"
                    return@launch
                }

                repository.updateNews(
                    newsId = news.newsId,
                    title = news.title,
                    subTitle = news.subTitle,
                    content = news.content,
                    imageUrl = news.imageUrl,
                    additionalPhotos = news.additionalPhotos
                )
                loadNews()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    // Удаление новости
    fun deleteNews(newsId: String) {
        viewModelScope.launch {
            try {
                val news = _news.value?.find { it.newsId == newsId }
                    ?: throw IllegalStateException("Новость с ID $newsId не найдена")
                val mainImageId = news.imageUrl.substringAfter("files/").substringBefore("/view")
                try {
                    HotelApp.storage.deleteFile(bucketId = "images", fileId = mainImageId)
                    Log.d("HotelViewModel", "Основное изображение удалено: $mainImageId")
                } catch (e: AppwriteException) {
                    Log.e("HotelViewModel", "Не удалось удалить основное изображение: ${e.message}")
                    setErrorMessage("Не удалось удалить основное изображение: ${e.message}")
                }
                news.additionalPhotos.forEach { photoUrl ->
                    val photoId = photoUrl.substringAfter("files/").substringBefore("/view")
                    try {
                        HotelApp.storage.deleteFile(bucketId = "images", fileId = photoId)
                        Log.d("HotelViewModel", "Дополнительное изображение удалено: $photoId")
                    } catch (e: AppwriteException) {
                        Log.e(
                            "HotelViewModel",
                            "Не удалось удалить дополнительное изображение: ${e.message}"
                        )
                        setErrorMessage("Не удалось удалить дополнительное изображение: ${e.message}")
                    }
                }
                repository.deleteNews(newsId)
                Log.d("HotelViewModel", "Новость успешно удалена: $newsId")
                loadNews()
            } catch (e: AppwriteException) {
                Log.e("HotelViewModel", "Ошибка удаления новости: ${e.message}, код: ${e.code}")
                setErrorMessage("Ошибка удаления новости: ${e.message}")
            } catch (e: Exception) {
                Log.e("HotelViewModel", "Неожиданная ошибка при удалении новости: ${e.message}")
                setErrorMessage("Неожиданная ошибка: ${e.message}")
            }
        }
    }

    // Получение новости по ID
    suspend fun getNewsById(newsId: String): News? {
        return try {
            repository.getNewsById(newsId)
        } catch (e: Exception) {
            _errorMessage.value = e.message
            null
        }
    }

    // Загрузка новостей
    fun loadNews() {
        viewModelScope.launch {
            try {
                val newsList = repository.getAllNews()
                Log.d("HotelViewModel", "News loaded: $newsList")
                _news.value = newsList
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка загрузки новостей: ${e.message}"
                Log.e("HotelViewModel", "Failed to load news: ${e.message}", e)
            }
        }
    }

    // Загрузка комнат (оставляем для совместимости, но предпочтительно использовать loadRoomsAndBookings)
    fun loadRooms() {
        viewModelScope.launch {
            try {
                Log.d("HotelViewModel", "Starting loadRooms")
                val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                val currentDate = LocalDate.now().format(formatter)
                Log.d("HotelViewModel", "Current date: $currentDate")
                val allBookings = repository.getAllBookings()
                Log.d("HotelViewModel", "All bookings: $allBookings")
                val roomList = repository.getAllRooms()

                roomList.forEach { room ->
                    val booking = allBookings.find { it.roomId == room.roomId }
                    if (booking != null && room.isBooked) {
                        Log.d(
                            "HotelViewModel",
                            "Checking booking for room ${room.roomId}: checkOutDate=${booking.checkOutDate}"
                        )
                        if (currentDate >= booking.checkOutDate) {
                            Log.d(
                                "HotelViewModel",
                                "Booking ${booking.bookingId} expired for room ${room.roomId}. Current date: $currentDate, CheckOutDate: ${booking.checkOutDate}"
                            )
                            repository.updateRoom(roomId = room.roomId, isBooked = false)
                            repository.deleteBooking(booking.bookingId)
                            Log.d(
                                "HotelViewModel",
                                "Booking ${booking.bookingId} deleted. Room ${room.roomId} is now available."
                            )
                        }
                    }
                }

                _rooms.value = repository.getAllRooms()
                Log.d("HotelViewModel", "Updated rooms: ${_rooms.value}")
            } catch (e: Exception) {
                Log.e("HotelViewModel", "Error in loadRooms: ${e.message}", e)
                _errorMessage.value = e.message
            }
        }
    }

    // Добавление комнаты
    fun insertRoom(room: Room) {
        viewModelScope.launch {
            try {
                if (room.name.isBlank() || room.name.length > 20) {
                    _errorMessage.value = "Название комнаты должно быть от 1 до 20 символов"
                    return@launch
                }
                if (room.description.isBlank() || room.description.length > 100) {
                    _errorMessage.value = "Описание комнаты должно быть от 1 до 100 символов"
                    return@launch
                }
                if (room.price < 100 || room.price > 10_000_000) {
                    _errorMessage.value = "Цена за ночь должна быть от 100 до 10,000,000 рублей"
                    return@launch
                }
                if (room.capacity < 1 || room.capacity > 4) {
                    _errorMessage.value = "Вместимость должна быть от 1 до 4 человек"
                    return@launch
                }
                if (RoomType.fromDisplayName(room.type) == null) {
                    _errorMessage.value = "Недопустимый тип комнаты"
                    return@launch
                }
                if (room.additionalPhotos.isEmpty()) {
                    _errorMessage.value =
                        "Необходимо добавить хотя бы одну дополнительную фотографию"
                    return@launch
                }
                if (room.additionalPhotos.size > 5) {
                    _errorMessage.value = "Максимум 5 дополнительных фотографий"
                    return@launch
                }

                repository.insertRoom(room)
                loadRoomsAndBookings()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    // Обновление комнаты
    fun updateRoom(room: Room) {
        viewModelScope.launch {
            try {
                if (room.name.isBlank() || room.name.length > 20) {
                    _errorMessage.value = "Название комнаты должно быть от 1 до 20 символов"
                    return@launch
                }
                if (room.description.isBlank() || room.description.length > 100) {
                    _errorMessage.value = "Описание комнаты должно быть от 1 до 100 символов"
                    return@launch
                }
                if (room.price < 100 || room.price > 10_000_000) {
                    _errorMessage.value = "Цена за ночь должна быть от 100 до 10,000,000 рублей"
                    return@launch
                }
                if (room.capacity < 1 || room.capacity > 4) {
                    _errorMessage.value = "Вместимость должна быть от 1 до 4 человек"
                    return@launch
                }
                if (RoomType.fromDisplayName(room.type) == null) {
                    _errorMessage.value = "Недопустимый тип комнаты"
                    return@launch
                }
                if (room.additionalPhotos.isEmpty()) {
                    _errorMessage.value =
                        "Необходимо добавить хотя бы одну дополнительную фотографию"
                    return@launch
                }
                if (room.additionalPhotos.size > 5) {
                    _errorMessage.value = "Максимум 5 дополнительных фотографий"
                    return@launch
                }

                repository.updateRoom(
                    roomId = room.roomId,
                    type = room.type,
                    name = room.name,
                    price = room.price,
                    capacity = room.capacity,
                    description = room.description,
                    imageUrl = room.imageUrl,
                    additionalPhotos = room.additionalPhotos,
                    isBooked = room.isBooked
                )
                loadRoomsAndBookings()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    // Удаление комнаты
    fun deleteRoom(roomId: String) {
        viewModelScope.launch {
            try {
                val room = _rooms.value?.find { it.roomId == roomId }
                    ?: throw IllegalStateException("Номер с ID $roomId не найден")
                val mainImageId = room.imageUrl.substringAfter("files/").substringBefore("/view")
                try {
                    HotelApp.storage.deleteFile(bucketId = "images", fileId = mainImageId)
                    Log.d("HotelViewModel", "Основное изображение номера удалено: $mainImageId")
                } catch (e: AppwriteException) {
                    Log.e(
                        "HotelViewModel",
                        "Не удалось удалить основное изображение номера: ${e.message}"
                    )
                    setErrorMessage("Не удалось удалить основное изображение номера: ${e.message}")
                }
                room.additionalPhotos.forEach { photoUrl ->
                    val photoId = photoUrl.substringAfter("files/").substringBefore("/view")
                    try {
                        HotelApp.storage.deleteFile(bucketId = "images", fileId = photoId)
                        Log.d(
                            "HotelViewModel",
                            "Дополнительное изображение номера удалено: $photoId"
                        )
                    } catch (e: AppwriteException) {
                        Log.e(
                            "HotelViewModel",
                            "Не удалось удалить дополнительное изображение номера: ${e.message}"
                        )
                        setErrorMessage("Не удалось удалить дополнительное изображение номера: ${e.message}")
                    }
                }
                repository.deleteRoom(roomId)
                Log.d("HotelViewModel", "Номер успешно удалён: $roomId")
                loadRoomsAndBookings()
            } catch (e: AppwriteException) {
                Log.e("HotelViewModel", "Ошибка удаления номера: ${e.message}, код: ${e.code}")
                setErrorMessage("Ошибка удаления номера: ${e.message}")
            } catch (e: Exception) {
                Log.e("HotelViewModel", "Неожиданная ошибка при удалении номера: ${e.message}")
                setErrorMessage("Неожиданная ошибка: ${e.message}")
            }
        }
    }

    // Получение акции по ID
    suspend fun getServiceById(serviceId: String): Service? {
        return try {
            repository.getServiceById(serviceId)
        } catch (e: Exception) {
            _errorMessage.value = "Ошибка загрузки акции: ${e.message}"
            Log.e("HotelViewModel", "Failed to load service with ID $serviceId: ${e.message}", e)
            null
        }
    }

    // Загрузка акций
    fun loadServices() {
        viewModelScope.launch {
            try {
                val serviceList = repository.getAllServices()
                _services.value = serviceList
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    // Добавление акции
    fun insertService(service: Service) {
        viewModelScope.launch {
            try {
                if (service.name.isBlank() || service.name.length > 50) {
                    _errorMessage.value = "Название акции должно быть от 1 до 50 символов"
                    return@launch
                }
                if (service.subTitle.length > 20) {
                    _errorMessage.value = "Подзаголовок акции должен быть до 20 символов"
                    return@launch
                }
                if (service.description.isBlank() || service.description.length > 500) {
                    _errorMessage.value = "Описание акции должно быть от 1 до 500 символов"
                    return@launch
                }

                repository.insertService(service)
                loadServices()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    // Обновление акции
    fun updateService(service: Service) {
        viewModelScope.launch {
            try {
                if (service.name.isBlank() || service.name.length > 50) {
                    _errorMessage.value = "Название акции должно быть от 1 до 50 символов"
                    return@launch
                }
                if (service.subTitle.length > 20) {
                    _errorMessage.value = "Подзаголовок акции должен быть до 20 символов"
                    return@launch
                }
                if (service.description.isBlank() || service.description.length > 500) {
                    _errorMessage.value = "Описание акции должно быть от 1 до 500 символов"
                    return@launch
                }

                repository.updateService(
                    serviceId = service.serviceId,
                    name = service.name,
                    subTitle = service.subTitle,
                    description = service.description,
                    imageUrl = service.imageUrl
                )
                loadServices()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    // Удаление акции
    fun deleteService(serviceId: String) {
        viewModelScope.launch {
            try {
                repository.deleteService(serviceId)
                loadServices()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    // Добавление бронирования
    fun addBooking(booking: Booking) {
        viewModelScope.launch {
            try {
                repository.insertBooking(booking)
                _currentUser.value?.userId?.let { loadBookings(it) }
                loadRoomsAndBookings()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    // Обновление бронирования
    fun updateBooking(booking: Booking) {
        viewModelScope.launch {
            try {
                repository.updateBooking(
                    bookingId = booking.bookingId,
                    userId = booking.userId,
                    roomId = booking.roomId,
                    checkInDate = booking.checkInDate,
                    checkOutDate = booking.checkOutDate,
                    totalPrice = booking.totalPrice
                )
                _currentUser.value?.userId?.let { loadBookings(it) }
                loadRoomsAndBookings()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    // Удаление бронирования
    fun deleteBooking(bookingId: String) {
        viewModelScope.launch {
            try {
                val booking = _bookings.value?.find { it.bookingId == bookingId }
                    ?: throw IllegalStateException("Бронирование с ID $bookingId не найдено")

                repository.deleteBooking(bookingId)
                _currentUser.value?.userId?.let { loadBookings(it) }

                repository.updateRoom(
                    roomId = booking.roomId,
                    isBooked = false
                )
                loadRoomsAndBookings()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    // Загрузка бронирований пользователя
    fun loadBookings(userId: String) {
        viewModelScope.launch {
            try {
                val bookingList = repository.getBookingsByUser(userId)
                Log.d("HotelViewModel", "Loaded bookings for user $userId: $bookingList")
                _bookings.value = bookingList
            } catch (e: Exception) {
                Log.e("HotelViewModel", "Error loading bookings: ${e.message}", e)
                _errorMessage.value = e.message
            }
        }
    }

    // Загрузка всех бронирований
    fun loadAllBookings() {
        viewModelScope.launch {
            try {
                val bookingList = repository.getAllBookings()
                Log.d("HotelViewModel", "Loaded all bookings: $bookingList")
                _bookings.value = bookingList
            } catch (e: Exception) {
                Log.e("HotelViewModel", "Error loading all bookings: ${e.message}", e)
                _errorMessage.value = e.message
            }
        }
    }

    // Загрузка всех пользователей
    fun loadUsers() {
        viewModelScope.launch {
            try {
                val userList = repository.getAllUsers()
                Log.d("HotelViewModel", "Loaded users: $userList")
                _users.value = userList
            } catch (e: Exception) {
                Log.e("HotelViewModel", "Error loading users: ${e.message}", e)
                _errorMessage.value = e.message
            }
        }
    }

    // Очистка сообщения об ошибке
    fun clearError() {
        _errorMessage.value = null
    }

    // Установка сообщения об ошибке
    fun setErrorMessage(message: String) {
        _errorMessage.value = message
    }
}