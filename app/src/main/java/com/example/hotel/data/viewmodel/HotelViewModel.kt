package com.example.hotel.data.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hotel.data.Booking
import com.example.hotel.data.News
import com.example.hotel.data.Room
import com.example.hotel.data.Service
import com.example.hotel.data.User
import com.example.hotel.data.entity.RoomType
import com.example.hotel.data.repository.HotelRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class HotelViewModel(private val repository: HotelRepository) : ViewModel() {
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

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _isManager = MutableLiveData<Boolean>(false)
    val isManager: LiveData<Boolean> get() = _isManager

    // Авторизация пользователя
    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            try {
                val user = repository.getUserByEmail(email)
                if (user != null && user.passwordHash == password) {
                    _currentUser.value = user
                    loadBookings(user.userId)
                    loadNews()
                    loadRooms()
                    loadServices()
                    checkManagerRole(user.userId)
                } else {
                    _errorMessage.value = "Неверный email или пароль"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    // Регистрация нового пользователя
    fun registerUser(name: String, email: String, phone: String, password: String) {
        viewModelScope.launch {
            try {
                val user = User(
                    name = name,
                    email = email,
                    phone = phone,
                    passwordHash = password,
                    role = "Guest"
                )
                repository.insertUser(user)
                loginUser(email, password)
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
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

    // Выход из аккаунта
    fun logout() {
        _currentUser.value = null
        _bookings.value = emptyList()
        _news.value = emptyList()
        _rooms.value = emptyList()
        _services.value = emptyList()
        _isManager.value = false
    }

    // Методы для работы с новостями
    fun addNews(news: News) {
        viewModelScope.launch {
            try {
                // Валидация
                if (news.title.isBlank() || news.title.length > 50) {
                    _errorMessage.value = "Заголовок должен быть от 1 до 50 символов"
                    return@launch
                }
                if (news.content.isBlank() || news.content.length > 200) {
                    _errorMessage.value = "Описание должно быть от 1 до 200 символов"
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

    fun updateNews(news: News) {
        viewModelScope.launch {
            try {
                // Валидация
                if (news.title.isBlank() || news.title.length > 50) {
                    _errorMessage.value = "Заголовок должен быть от 1 до 50 символов"
                    return@launch
                }
                if (news.content.isBlank() || news.content.length > 200) {
                    _errorMessage.value = "Описание должно быть от 1 до 200 символов"
                    return@launch
                }
                if (news.additionalPhotos.size > 5) {
                    _errorMessage.value = "Максимум 5 дополнительных фотографий"
                    return@launch
                }

                repository.updateNews(
                    newsId = news.newsId,
                    title = news.title,
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

    fun deleteNews(newsId: String) {
        viewModelScope.launch {
            try {
                repository.deleteNews(newsId)
                loadNews()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    suspend fun getNewsById(newsId: String): News? {
        return try {
            repository.getNewsById(newsId)
        } catch (e: Exception) {
            _errorMessage.value = e.message
            null
        }
    }

    fun loadNews() {
        viewModelScope.launch {
            try {
                val newsList = repository.getAllNews()
                _news.value = newsList
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    // Методы для работы с комнатами
    fun loadRooms() {
        viewModelScope.launch {
            try {
                val roomList = repository.getAllRooms()
                _rooms.value = roomList
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun insertRoom(room: Room) {
        viewModelScope.launch {
            try {
                // Валидация
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
                if (room.additionalPhotos.size > 5) {
                    _errorMessage.value = "Максимум 5 дополнительных фотографий"
                    return@launch
                }

                repository.insertRoom(room)
                loadRooms()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun updateRoom(room: Room) {
        viewModelScope.launch {
            try {
                // Валидация
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
                    additionalPhotos = room.additionalPhotos
                )
                loadRooms()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun deleteRoom(roomId: String) {
        viewModelScope.launch {
            try {
                repository.deleteRoom(roomId)
                loadRooms()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    // Методы для работы с акциями
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

    fun insertService(service: Service) {
        viewModelScope.launch {
            try {
                repository.insertService(service)
                loadServices()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun updateService(service: Service) {
        viewModelScope.launch {
            try {
                repository.updateService(
                    serviceId = service.serviceId,
                    name = service.name,
                    description = service.description,
                    imageUrl = service.imageUrl,
                    additionalPhotos = service.additionalPhotos
                )
                loadServices()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

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

    // Методы для работы с бронированиями
    fun addBooking(booking: Booking) {
        viewModelScope.launch {
            try {
                repository.insertBooking(booking)
                _currentUser.value?.userId?.let { loadBookings(it) }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun updateBooking(booking: Booking) {
        viewModelScope.launch {
            try {
                repository.updateBooking(
                    bookingId = booking.bookingId,
                    userId = booking.userId,
                    roomId = booking.roomId,
                    checkInDate = booking.checkInDate,
                    checkOutDate = booking.checkOutDate,
                    totalPrice = booking.totalPrice,
                    status = booking.status
                )
                _currentUser.value?.userId?.let { loadBookings(it) }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun deleteBooking(bookingId: String) {
        viewModelScope.launch {
            try {
                repository.deleteBooking(bookingId)
                _currentUser.value?.userId?.let { loadBookings(it) }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun loadBookings(userId: String) {
        viewModelScope.launch {
            try {
                val bookingList = repository.getBookingsByUser(userId)
                _bookings.value = bookingList
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    // Очистка сообщения об ошибке
    fun clearError() {
        _errorMessage.value = null
    }

    fun setErrorMessage(message: String) {
        _errorMessage.value = message
    }
}