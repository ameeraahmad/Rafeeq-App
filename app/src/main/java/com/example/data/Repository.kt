package com.example.data

import kotlinx.coroutines.flow.Flow

class RafeeqRepository(private val db: RafeeqDatabase) {
    private val userDao = db.userDao()
    private val tripDao = db.tripDao()
    private val messageDao = db.messageDao()

    // Users
    val allUsers: Flow<List<User>> = userDao.getAllUsers()
    
    fun getUsersByRole(role: String): Flow<List<User>> = userDao.getUsersByRole(role)
    
    suspend fun getUserById(id: Int): User? = userDao.getUserById(id)
    
    suspend fun insertUser(user: User): Int {
        return userDao.insertUser(user).toInt()
    }
    
    suspend fun updateVettingStatus(userId: Int, vetted: Boolean) {
        userDao.updateVettingStatus(userId, vetted)
    }

    // Trips
    val allTrips: Flow<List<Trip>> = tripDao.getAllTrips()
    val pendingTrips: Flow<List<Trip>> = tripDao.getPendingTrips()

    fun getTripsBySeeker(seekerId: Int): Flow<List<Trip>> = tripDao.getTripsBySeeker(seekerId)
    
    fun getTripsByVolunteer(volunteerId: Int): Flow<List<Trip>> = tripDao.getTripsByVolunteer(volunteerId)
    
    suspend fun getTripById(id: Int): Trip? = tripDao.getTripById(id)

    suspend fun insertTrip(trip: Trip): Int {
        return tripDao.insertTrip(trip).toInt()
    }

    suspend fun acceptTrip(tripId: Int, volunteerId: Int, volunteerName: String) {
        tripDao.updateTripStatusAndVolunteer(tripId, "ACCEPTED", volunteerId, volunteerName)
    }

    suspend fun updateTripStatus(tripId: Int, status: String, volunteerId: Int?, volunteerName: String?) {
        tripDao.updateTripStatusAndVolunteer(tripId, status, volunteerId, volunteerName)
    }

    suspend fun submitTripReview(tripId: Int, ratingToV: Int, ratingToS: Int, reviewText: String?) {
        tripDao.submitReview(tripId, ratingToV, ratingToS, reviewText)
        
        // Optionally update ratings for seeker and volunteer
        val trip = tripDao.getTripById(tripId)
        if (trip != null) {
            // Update Volunteer rating
            trip.volunteerId?.let { vId ->
                val volunteer = userDao.getUserById(vId)
                if (volunteer != null) {
                    val newCount = volunteer.totalTrips + 1
                    val newRating = ((volunteer.rating * volunteer.totalTrips) + ratingToV) / newCount
                    userDao.updateRatingAndTrips(vId, newRating, newCount)
                }
            }
            // Update Seeker rating
            val seeker = userDao.getUserById(trip.seekerId)
            if (seeker != null) {
                val newCount = seeker.totalTrips + 1
                val newRating = ((seeker.rating * seeker.totalTrips) + ratingToS) / newCount
                userDao.updateRatingAndTrips(trip.seekerId, newRating, newCount)
            }
        }
    }

    // Messages / Chat
    fun getMessagesForTrip(tripId: Int): Flow<List<Message>> = messageDao.getMessagesForTrip(tripId)

    suspend fun insertMessage(message: Message): Int {
        return messageDao.insertMessage(message).toInt()
    }
    
    // Dev Helper to reseed
    suspend fun reseedDb() {
        RafeeqDatabase.seedDatabase(db)
    }
}
