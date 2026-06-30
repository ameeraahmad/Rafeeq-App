package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Message
import com.example.data.FirebaseRepository
import com.example.data.Trip
import com.example.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class RafeeqScreen {
    ONBOARDING,
    SEEKER_DASHBOARD,
    VOLUNTEER_DASHBOARD,
    ADMIN_DASHBOARD,
    TRIP_DETAIL,
    CREATE_TRIP,
    CHAT_SCREEN
}

class RafeeqViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FirebaseRepository = FirebaseRepository()

    // Global lists
    val allUsers: StateFlow<List<User>>
    val allTrips: StateFlow<List<Trip>>
    val pendingTrips: StateFlow<List<Trip>>

    // Current app state
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val _currentScreen = MutableStateFlow(RafeeqScreen.ONBOARDING)
    val currentScreen = _currentScreen.asStateFlow()

    private val _selectedTrip = MutableStateFlow<Trip?>(null)
    val selectedTrip = _selectedTrip.asStateFlow()

    private val _isEnglish = MutableStateFlow(false) // Toggle between Arabic and English
    val isEnglish = _isEnglish.asStateFlow()

    // Message lists
    val chatMessages: StateFlow<List<Message>>

    init {
        allUsers = repository.getAllUsersFlow().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allTrips = repository.getAllTripsFlow().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        pendingTrips = repository.getPendingTripsFlow().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
        chatMessages = _selectedTrip
            .flatMapLatest { trip ->
                if (trip != null) {
                    repository.getMessagesForTripFlow(trip.id)
                } else {
                    flowOf(emptyList())
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        // Populate initial data if empty (fallback)
        viewModelScope.launch {
            repository.seedIfEmpty()
        }
    }

    // Language selector toggle
    fun toggleLanguage() {
        _isEnglish.value = !_isEnglish.value
    }

    // Navigation and User Management
    fun setScreen(screen: RafeeqScreen) {
        _currentScreen.value = screen
    }

    fun selectUser(user: User?) {
        _currentUser.value = user
        if (user == null) {
            _currentScreen.value = RafeeqScreen.ONBOARDING
        } else {
            when (user.role) {
                "SEEKER" -> _currentScreen.value = RafeeqScreen.SEEKER_DASHBOARD
                "VOLUNTEER" -> _currentScreen.value = RafeeqScreen.VOLUNTEER_DASHBOARD
            }
        }
    }

    fun enterAdminMode() {
        _currentUser.value = User(id = -1, name = "مدير النظام", role = "ADMIN", phone = "", gender = "MALE", isVetted = true)
        _currentScreen.value = RafeeqScreen.ADMIN_DASHBOARD
    }

    fun selectTrip(trip: Trip) {
        _selectedTrip.value = trip
        _currentScreen.value = RafeeqScreen.TRIP_DETAIL
    }

    // Seeker Action: Create Request
    fun createTrip(
        title: String,
        from: String,
        to: String,
        dateTime: String,
        helpNeeded: String
    ) {
        val seeker = _currentUser.value ?: return
        val otp = String.format("%04d", Random.nextInt(1000, 9999))
        val newTrip = Trip(
            seekerId = seeker.id,
            seekerName = seeker.name,
            seekerDisabilityType = seeker.disabilityType,
            seekerSpecificNeeds = seeker.specificNeeds,
            title = title,
            fromLocation = from,
            toLocation = to,
            dateTime = dateTime,
            status = "PENDING",
            helpNeeded = helpNeeded,
            otp = otp
        )
        viewModelScope.launch {
            val tripId = repository.insertTrip(newTrip)
            // Go back to seeker dashboard
            _currentScreen.value = RafeeqScreen.SEEKER_DASHBOARD
        }
    }

    // Volunteer Action: Accept Companion request
    fun acceptTripRequest(trip: Trip) {
        val volunteer = _currentUser.value ?: return
        if (!volunteer.isVetted) return // Only vetted volunteers can accept

        viewModelScope.launch {
            repository.updateTripStatusAndVolunteer(trip.id, "ACCEPTED", volunteer.id, volunteer.name)
            // Update local selected trip if needed
            val updatedTrip = repository.getTripById(trip.id)
            if (updatedTrip != null) {
                _selectedTrip.value = updatedTrip
            }
        }
    }

    // Start Trip with OTP Verification (by Volunteer)
    fun startTrip(tripId: Int, enteredOtp: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val trip = repository.getTripById(tripId)
            if (trip != null && trip.otp == enteredOtp) {
                repository.updateTripStatusAndVolunteer(tripId, "ACTIVE", trip.volunteerId, trip.volunteerName)
                val updatedTrip = repository.getTripById(tripId)
                _selectedTrip.value = updatedTrip
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }

    // End Trip
    fun endTrip(tripId: Int) {
        viewModelScope.launch {
            val trip = repository.getTripById(tripId)
            if (trip != null) {
                // Change status to completed or wait for review
                repository.updateTripStatusAndVolunteer(tripId, "COMPLETED", trip.volunteerId, trip.volunteerName)
                val updatedTrip = repository.getTripById(tripId)
                _selectedTrip.value = updatedTrip
            }
        }
    }

    // Submit rating and reviews
    fun submitTripReview(tripId: Int, ratingToV: Int, ratingToS: Int, reviewText: String) {
        viewModelScope.launch {
            repository.submitTripReview(tripId, ratingToV, ratingToS, reviewText)
            val updatedTrip = repository.getTripById(tripId)
            _selectedTrip.value = updatedTrip
            
            // Go back to dashboard depending on current role
            val user = _currentUser.value
            if (user != null) {
                when (user.role) {
                    "SEEKER" -> _currentScreen.value = RafeeqScreen.SEEKER_DASHBOARD
                    "VOLUNTEER" -> _currentScreen.value = RafeeqScreen.VOLUNTEER_DASHBOARD
                }
            }
        }
    }

    // Chat Actions
    fun sendChatMessage(text: String) {
        val user = _currentUser.value ?: return
        val trip = _selectedTrip.value ?: return
        val newMessage = Message(
            tripId = trip.id,
            senderId = user.id,
            text = text
        )
        viewModelScope.launch {
            repository.insertMessage(newMessage)
        }
    }

    // Admin Action: Vet Volunteer
    fun approveVolunteer(volunteerId: Int) {
        viewModelScope.launch {
            repository.updateVettingStatus(volunteerId, true)
        }
    }

    // Seeker/Volunteer registration
    fun registerNewUser(
        name: String,
        role: String,
        phone: String,
        gender: String,
        disabilityType: String?,
        specificNeeds: String?,
        availability: String?,
        nationalId: String?,
        policeClearance: String?
    ) {
        val newUser = User(
            name = name,
            role = role,
            phone = phone,
            gender = gender,
            disabilityType = disabilityType,
            specificNeeds = specificNeeds,
            isVetted = role == "SEEKER", // Seekers are vetted instantly for simulation, volunteers need manual vetting
            idDocumentUri = nationalId ?: "id_card.png",
            policeClearanceUri = policeClearance ?: "clearance.png",
            volunteerAvailability = availability
        )
        viewModelScope.launch {
            val id = repository.insertUser(newUser)
            val createdUser = repository.getUserById(id)
            selectUser(createdUser)
        }
    }

    // Reseed DB for development / play
    fun resetDatabase() {
        viewModelScope.launch {
            repository.seedIfEmpty()
            _currentUser.value = null
            _selectedTrip.value = null
            _currentScreen.value = RafeeqScreen.ONBOARDING
        }
    }
}
