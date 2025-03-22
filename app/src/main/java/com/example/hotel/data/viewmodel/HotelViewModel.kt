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
import com.example.hotel.data.repository.HotelRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class HotelViewModel(val repository: HotelRepository) : ViewModel() {

    private val _rooms = MutableLiveData<List<Room>>()
    val rooms: LiveData<List<Room>> = _rooms

    private val _news = MutableLiveData<List<News>>()
    val news: LiveData<List<News>> = _news

    private val _services = MutableLiveData<List<Service>>()
    val services: LiveData<List<Service>> = _services

    private val _bookings = MutableLiveData<List<Booking>>()
    val bookings: LiveData<List<Booking>> = _bookings

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        // Realtime-слушатели для коллекций
        FirebaseFirestore.getInstance().collection("rooms")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                snapshot?.let { _rooms.value = it.documents.mapNotNull { doc -> doc.toObject(Room::class.java) } }
            }

        FirebaseFirestore.getInstance().collection("news")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                snapshot?.let { _news.value = it.documents.mapNotNull { doc -> doc.toObject(News::class.java) } }
            }

        FirebaseFirestore.getInstance().collection("services")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                snapshot?.let { _services.value = it.documents.mapNotNull { doc -> doc.toObject(Service::class.java) } }
            }
    }

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            val user = repository.getUserByEmail(email)
            if (user != null && user.passwordHash == password) { // В реальном проекте используйте Firebase Auth
                _currentUser.value = user
                loadBookings(user.userId)
            } else {
                _errorMessage.value = "Invalid email or password"
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _bookings.value = emptyList()
    }

    private fun loadBookings(userId: String) {
        viewModelScope.launch {
            _bookings.value = repository.getBookingsByUser(userId)
        }
        FirebaseFirestore.getInstance().collection("bookings")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                snapshot?.let { _bookings.value = it.documents.mapNotNull { doc -> doc.toObject(Booking::class.java) } }
            }
    }

    // Room
    fun insertRoom(room: Room) {
        viewModelScope.launch {
            _currentUser.value?.let { user ->
                if (repository.isManager(user.userId)) {
                    repository.insertRoom(room)
                } else {
                    _errorMessage.value = "Only managers can insert rooms"
                }
            } ?: run { _errorMessage.value = "User not logged in" }
        }
    }

    fun updateRoom(roomId: String, type: String? = null, name: String? = null, price: Double? = null, capacity: Int? = null, description: String? = null, imageUrl: String? = null) {
        viewModelScope.launch {
            _currentUser.value?.let { user ->
                if (repository.isManager(user.userId)) {
                    repository.updateRoom(roomId, type, name, price, capacity, description, imageUrl)
                } else {
                    _errorMessage.value = "Only managers can update rooms"
                }
            } ?: run { _errorMessage.value = "User not logged in" }
        }
    }

    fun deleteRoom(roomId: String) {
        viewModelScope.launch {
            _currentUser.value?.let { user ->
                if (repository.isManager(user.userId)) {
                    repository.deleteRoom(roomId)
                } else {
                    _errorMessage.value = "Only managers can delete rooms"
                }
            } ?: run { _errorMessage.value = "User not logged in" }
        }
    }

    // News
    fun insertNews(news: News) {
        viewModelScope.launch {
            _currentUser.value?.let { user ->
                if (repository.isManager(user.userId)) {
                    repository.insertNews(news)
                } else {
                    _errorMessage.value = "Only managers can insert news"
                }
            } ?: run { _errorMessage.value = "User not logged in" }
        }
    }

    fun updateNews(newsId: String, title: String? = null, description: String? = null, imageUrl: String? = null) {
        viewModelScope.launch {
            _currentUser.value?.let { user ->
                if (repository.isManager(user.userId)) {
                    repository.updateNews(newsId, title, description, imageUrl)
                } else {
                    _errorMessage.value = "Only managers can update news"
                }
            } ?: run { _errorMessage.value = "User not logged in" }
        }
    }

    fun deleteNews(newsId: String) {
        viewModelScope.launch {
            _currentUser.value?.let { user ->
                if (repository.isManager(user.userId)) {
                    repository.deleteNews(newsId)
                } else {
                    _errorMessage.value = "Only managers can delete news"
                }
            } ?: run { _errorMessage.value = "User not logged in" }
        }
    }

    // Service
    fun insertService(service: Service) {
        viewModelScope.launch {
            _currentUser.value?.let { user ->
                if (repository.isManager(user.userId)) {
                    repository.insertService(service)
                } else {
                    _errorMessage.value = "Only managers can insert services"
                }
            } ?: run { _errorMessage.value = "User not logged in" }
        }
    }

    fun updateService(serviceId: String, name: String? = null, description: String? = null, price: Double? = null, imageUrl: String? = null) {
        viewModelScope.launch {
            _currentUser.value?.let { user ->
                if (repository.isManager(user.userId)) {
                    repository.updateService(serviceId, name, description, price, imageUrl)
                } else {
                    _errorMessage.value = "Only managers can update services"
                }
            } ?: run { _errorMessage.value = "User not logged in" }
        }
    }

    fun deleteService(serviceId: String) {
        viewModelScope.launch {
            _currentUser.value?.let { user ->
                if (repository.isManager(user.userId)) {
                    repository.deleteService(serviceId)
                } else {
                    _errorMessage.value = "Only managers can delete services"
                }
            } ?: run { _errorMessage.value = "User not logged in" }
        }
    }

    // Booking
    fun insertBooking(booking: Booking) {
        viewModelScope.launch {
            _currentUser.value?.let { user ->
                repository.insertBooking(booking.copy(userId = user.userId))
            } ?: run { _errorMessage.value = "User not logged in" }
        }
    }

    fun updateBooking(bookingId: String, userId: String? = null, roomId: String? = null, checkInDate: String? = null, checkOutDate: String? = null, totalPrice: Double? = null, status: String? = null) {
        viewModelScope.launch {
            _currentUser.value?.let {
                repository.updateBooking(bookingId, userId, roomId, checkInDate, checkOutDate, totalPrice, status)
            } ?: run { _errorMessage.value = "User not logged in" }
        }
    }

    fun deleteBooking(bookingId: String) {
        viewModelScope.launch {
            _currentUser.value?.let {
                repository.deleteBooking(bookingId)
            } ?: run { _errorMessage.value = "User not logged in" }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}