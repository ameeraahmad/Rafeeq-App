package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val role: String, // "SEEKER" or "VOLUNTEER"
    val phone: String,
    val gender: String, // "MALE" or "FEMALE"
    val disabilityType: String? = null, // e.g., "حركية", "بصرية", "سمعية"
    val specificNeeds: String? = null,   // e.g., "دفع الكرسي المتحرك", "وصف بصري", "لغة الإشارة"
    val isVetted: Boolean = false,      // For volunteers, admin manual review status
    val idDocumentUri: String? = null,  // Base64 or mock URI for National ID
    val policeClearanceUri: String? = null, // Base64 or mock URI for "فيش وتشبيه"
    val rating: Float = 5.0f,
    val totalTrips: Int = 0,
    val volunteerAvailability: String? = null // e.g. "الاثنين والأربعاء", "الأحد والأربعاء", "كل الأيام"
) : Serializable

@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val seekerId: Int,
    val seekerName: String,
    val seekerDisabilityType: String?,
    val seekerSpecificNeeds: String?,
    val volunteerId: Int? = null,
    val volunteerName: String? = null,
    val title: String,
    val fromLocation: String,
    val toLocation: String,
    val dateTime: String,
    val status: String, // "PENDING", "ACCEPTED", "ACTIVE", "COMPLETED", "CANCELLED"
    val helpNeeded: String,
    val otp: String, // 4-digit code (e.g., "4382")
    val ratingToVolunteer: Int? = null,
    val ratingToSeeker: Int? = null,
    val reviewText: String? = null
) : Serializable

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tripId: Int,
    val senderId: Int,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable
