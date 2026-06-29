package com.example.data

import android.util.Log
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.PostgresAction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ─── Supabase DTOs (Data Transfer Objects) ───────────────────────────────────
// These mirror the Supabase table schema. Column names use snake_case as stored in Supabase.

@Serializable
data class SupabaseUser(
    val id: Int = 0,
    val name: String,
    val role: String,
    val phone: String,
    val gender: String,
    @SerialName("disability_type") val disabilityType: String? = null,
    @SerialName("specific_needs") val specificNeeds: String? = null,
    @SerialName("is_vetted") val isVetted: Boolean = false,
    @SerialName("id_document_uri") val idDocumentUri: String? = null,
    @SerialName("police_clearance_uri") val policeClearanceUri: String? = null,
    val rating: Float = 5.0f,
    @SerialName("total_trips") val totalTrips: Int = 0,
    @SerialName("volunteer_availability") val volunteerAvailability: String? = null
)

@Serializable
data class SupabaseTrip(
    val id: Int = 0,
    @SerialName("seeker_id") val seekerId: Int,
    @SerialName("seeker_name") val seekerName: String,
    @SerialName("seeker_disability_type") val seekerDisabilityType: String?,
    @SerialName("seeker_specific_needs") val seekerSpecificNeeds: String?,
    @SerialName("volunteer_id") val volunteerId: Int? = null,
    @SerialName("volunteer_name") val volunteerName: String? = null,
    val title: String,
    @SerialName("from_location") val fromLocation: String,
    @SerialName("to_location") val toLocation: String,
    @SerialName("date_time") val dateTime: String,
    val status: String,
    @SerialName("help_needed") val helpNeeded: String,
    val otp: String,
    @SerialName("rating_to_volunteer") val ratingToVolunteer: Int? = null,
    @SerialName("rating_to_seeker") val ratingToSeeker: Int? = null,
    @SerialName("review_text") val reviewText: String? = null
)

@Serializable
data class SupabaseMessage(
    val id: Int = 0,
    @SerialName("trip_id") val tripId: Int,
    @SerialName("sender_id") val senderId: Int,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

// ─── Extension mappers: Supabase DTO ↔ Room/App model ────────────────────────

fun SupabaseUser.toUser() = User(
    id = id, name = name, role = role, phone = phone, gender = gender,
    disabilityType = disabilityType, specificNeeds = specificNeeds,
    isVetted = isVetted, idDocumentUri = idDocumentUri,
    policeClearanceUri = policeClearanceUri, rating = rating,
    totalTrips = totalTrips, volunteerAvailability = volunteerAvailability
)

fun User.toSupabaseUser() = SupabaseUser(
    id = id, name = name, role = role, phone = phone, gender = gender,
    disabilityType = disabilityType, specificNeeds = specificNeeds,
    isVetted = isVetted, idDocumentUri = idDocumentUri,
    policeClearanceUri = policeClearanceUri, rating = rating,
    totalTrips = totalTrips, volunteerAvailability = volunteerAvailability
)

fun SupabaseTrip.toTrip() = Trip(
    id = id, seekerId = seekerId, seekerName = seekerName,
    seekerDisabilityType = seekerDisabilityType, seekerSpecificNeeds = seekerSpecificNeeds,
    volunteerId = volunteerId, volunteerName = volunteerName, title = title,
    fromLocation = fromLocation, toLocation = toLocation, dateTime = dateTime,
    status = status, helpNeeded = helpNeeded, otp = otp,
    ratingToVolunteer = ratingToVolunteer, ratingToSeeker = ratingToSeeker,
    reviewText = reviewText
)

fun Trip.toSupabaseTrip() = SupabaseTrip(
    id = id, seekerId = seekerId, seekerName = seekerName,
    seekerDisabilityType = seekerDisabilityType, seekerSpecificNeeds = seekerSpecificNeeds,
    volunteerId = volunteerId, volunteerName = volunteerName, title = title,
    fromLocation = fromLocation, toLocation = toLocation, dateTime = dateTime,
    status = status, helpNeeded = helpNeeded, otp = otp,
    ratingToVolunteer = ratingToVolunteer, ratingToSeeker = ratingToSeeker,
    reviewText = reviewText
)

fun SupabaseMessage.toMessage() = Message(id = id, tripId = tripId, senderId = senderId, text = text, timestamp = timestamp)
fun Message.toSupabaseMessage() = SupabaseMessage(id = id, tripId = tripId, senderId = senderId, text = text, timestamp = timestamp)

// ─── Supabase Repository ──────────────────────────────────────────────────────

class SupabaseRepository {
    private val client = SupabaseClientProvider.client
    private val TAG = "SupabaseRepository"

    // ── Users ────────────────────────────────────────────────────────────────

    fun getAllUsersFlow(): Flow<List<User>> = flow {
        while (true) {
            try {
                val users = client.from("users")
                    .select()
                    .decodeList<SupabaseUser>()
                emit(users.map { it.toUser() })
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching users: ${e.message}", e)
                emit(emptyList())
            }
            kotlinx.coroutines.delay(5000) // Poll every 5 seconds
        }
    }

    fun getUsersByRoleFlow(role: String): Flow<List<User>> = flow {
        while (true) {
            try {
                val users = client.from("users")
                    .select { filter { eq("role", role) } }
                    .decodeList<SupabaseUser>()
                emit(users.map { it.toUser() })
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching users by role: ${e.message}", e)
                emit(emptyList())
            }
            kotlinx.coroutines.delay(5000)
        }
    }

    suspend fun getUserById(id: Int): User? {
        return try {
            client.from("users")
                .select { filter { eq("id", id) } }
                .decodeSingleOrNull<SupabaseUser>()
                ?.toUser()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user $id: ${e.message}", e)
            null
        }
    }

    suspend fun insertUser(user: User): Int {
        return try {
            val result = client.from("users")
                .insert(user.toSupabaseUser()) { select() }
                .decodeSingle<SupabaseUser>()
            result.id
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting user: ${e.message}", e)
            -1
        }
    }

    suspend fun updateVettingStatus(userId: Int, vetted: Boolean) {
        try {
            client.from("users")
                .update({ set("is_vetted", vetted) }) { filter { eq("id", userId) } }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating vetting status: ${e.message}", e)
        }
    }

    suspend fun updateRatingAndTrips(userId: Int, rating: Float, totalTrips: Int) {
        try {
            client.from("users")
                .update({
                    set("rating", rating)
                    set("total_trips", totalTrips)
                }) { filter { eq("id", userId) } }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating rating: ${e.message}", e)
        }
    }

    // ── Trips ────────────────────────────────────────────────────────────────

    fun getAllTripsFlow(): Flow<List<Trip>> = flow {
        while (true) {
            try {
                val trips = client.from("trips")
                    .select { order("id", Order.DESCENDING) }
                    .decodeList<SupabaseTrip>()
                emit(trips.map { it.toTrip() })
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching trips: ${e.message}", e)
                emit(emptyList())
            }
            kotlinx.coroutines.delay(3000) // Poll every 3 seconds for trips (more real-time)
        }
    }

    fun getPendingTripsFlow(): Flow<List<Trip>> = flow {
        while (true) {
            try {
                val trips = client.from("trips")
                    .select {
                        filter { eq("status", "PENDING") }
                        order("id", Order.DESCENDING)
                    }
                    .decodeList<SupabaseTrip>()
                emit(trips.map { it.toTrip() })
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching pending trips: ${e.message}", e)
                emit(emptyList())
            }
            kotlinx.coroutines.delay(3000)
        }
    }

    fun getTripsBySeekerFlow(seekerId: Int): Flow<List<Trip>> = flow {
        while (true) {
            try {
                val trips = client.from("trips")
                    .select {
                        filter { eq("seeker_id", seekerId) }
                        order("id", Order.DESCENDING)
                    }
                    .decodeList<SupabaseTrip>()
                emit(trips.map { it.toTrip() })
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching seeker trips: ${e.message}", e)
                emit(emptyList())
            }
            kotlinx.coroutines.delay(3000)
        }
    }

    fun getTripsByVolunteerFlow(volunteerId: Int): Flow<List<Trip>> = flow {
        while (true) {
            try {
                val trips = client.from("trips")
                    .select {
                        filter { eq("volunteer_id", volunteerId) }
                        order("id", Order.DESCENDING)
                    }
                    .decodeList<SupabaseTrip>()
                emit(trips.map { it.toTrip() })
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching volunteer trips: ${e.message}", e)
                emit(emptyList())
            }
            kotlinx.coroutines.delay(3000)
        }
    }

    suspend fun getTripById(id: Int): Trip? {
        return try {
            client.from("trips")
                .select { filter { eq("id", id) } }
                .decodeSingleOrNull<SupabaseTrip>()
                ?.toTrip()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching trip $id: ${e.message}", e)
            null
        }
    }

    suspend fun insertTrip(trip: Trip): Int {
        return try {
            val result = client.from("trips")
                .insert(trip.toSupabaseTrip()) { select() }
                .decodeSingle<SupabaseTrip>()
            result.id
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting trip: ${e.message}", e)
            -1
        }
    }

    suspend fun updateTripStatusAndVolunteer(
        tripId: Int, status: String, volunteerId: Int?, volunteerName: String?
    ) {
        try {
            client.from("trips")
                .update({
                    set("status", status)
                    set("volunteer_id", volunteerId)
                    set("volunteer_name", volunteerName)
                }) { filter { eq("id", tripId) } }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating trip: ${e.message}", e)
        }
    }

    suspend fun submitTripReview(
        tripId: Int, ratingToV: Int, ratingToS: Int, reviewText: String?
    ) {
        try {
            client.from("trips")
                .update({
                    set("rating_to_volunteer", ratingToV)
                    set("rating_to_seeker", ratingToS)
                    set("review_text", reviewText)
                    set("status", "COMPLETED")
                }) { filter { eq("id", tripId) } }

            // Update volunteer and seeker ratings
            val trip = getTripById(tripId)
            if (trip != null) {
                trip.volunteerId?.let { vId ->
                    val volunteer = getUserById(vId)
                    if (volunteer != null) {
                        val newCount = volunteer.totalTrips + 1
                        val newRating = ((volunteer.rating * volunteer.totalTrips) + ratingToV) / newCount
                        updateRatingAndTrips(vId, newRating, newCount)
                    }
                }
                val seeker = getUserById(trip.seekerId)
                if (seeker != null) {
                    val newCount = seeker.totalTrips + 1
                    val newRating = ((seeker.rating * seeker.totalTrips) + ratingToS) / newCount
                    updateRatingAndTrips(trip.seekerId, newRating, newCount)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error submitting review: ${e.message}", e)
        }
    }

    // ── Messages ─────────────────────────────────────────────────────────────

    fun getMessagesForTripFlow(tripId: Int): Flow<List<Message>> = flow {
        while (true) {
            try {
                val messages = client.from("messages")
                    .select {
                        filter { eq("trip_id", tripId) }
                        order("timestamp", Order.ASCENDING)
                    }
                    .decodeList<SupabaseMessage>()
                emit(messages.map { it.toMessage() })
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching messages: ${e.message}", e)
                emit(emptyList())
            }
            kotlinx.coroutines.delay(2000) // Poll every 2 seconds for chat messages
        }
    }

    suspend fun insertMessage(message: Message): Int {
        return try {
            val result = client.from("messages")
                .insert(message.toSupabaseMessage()) { select() }
                .decodeSingle<SupabaseMessage>()
            result.id
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting message: ${e.message}", e)
            -1
        }
    }

    // ── Seed initial data if tables are empty ─────────────────────────────────

    suspend fun seedIfEmpty() {
        try {
            val userCount = client.from("users").select().decodeList<SupabaseUser>().size
            if (userCount == 0) {
                Log.d(TAG, "Tables empty - seeding Supabase with initial data...")
                seedSupabase()
            } else {
                Log.d(TAG, "Tables already have $userCount users - skipping seed.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking/seeding: ${e.message}", e)
        }
    }

    private suspend fun seedSupabase() {
        // Seed users
        val users = listOf(
            SupabaseUser(id = 1, name = "أميرة أحمد", role = "SEEKER", phone = "01012345678", gender = "FEMALE", disabilityType = "حركية (كرسي متحرك)", specificNeeds = "أحتاج مساعدة في دفع الكرسي وصعود السلالم، ومرافقة متكررة للكلية.", isVetted = true, idDocumentUri = "valid_doc", rating = 4.9f, totalTrips = 8),
            SupabaseUser(id = 2, name = "أحمد محمد", role = "SEEKER", phone = "01123456789", gender = "MALE", disabilityType = "بصرية (كفيف)", specificNeeds = "أحتاج مرافقة في محطة القطار ووصف بصري وإرشاد أثناء الرحلة الطويلة.", isVetted = true, idDocumentUri = "valid_doc", rating = 5.0f, totalTrips = 3),
            SupabaseUser(id = 3, name = "آية يوسف", role = "SEEKER", phone = "01234567890", gender = "FEMALE", disabilityType = "سمعية (صم وبكم)", specificNeeds = "أحتاج مرافقة لعيادة الطبيب وتواصل عبر لغة الإشارة أو الشات النصي لكتابة التشخيص.", isVetted = true, idDocumentUri = "valid_doc", rating = 4.8f, totalTrips = 5),
            SupabaseUser(id = 4, name = "هبة مصطفى", role = "VOLUNTEER", phone = "01511223344", gender = "FEMALE", isVetted = true, idDocumentUri = "heba_id_card.png", policeClearanceUri = "heba_criminal_clearance.png", rating = 4.9f, totalTrips = 12, volunteerAvailability = "الاثنين والأربعاء (طريقي لجامعة القاهرة)"),
            SupabaseUser(id = 5, name = "محمود علي", role = "VOLUNTEER", phone = "01099887766", gender = "MALE", isVetted = true, idDocumentUri = "mahmoud_id_card.png", policeClearanceUri = "mahmoud_criminal_clearance.png", rating = 5.0f, totalTrips = 4, volunteerAvailability = "سفر متكرر لأسوان (بالقطار)"),
            SupabaseUser(id = 6, name = "هنا السادات", role = "VOLUNTEER", phone = "01244556677", gender = "FEMALE", isVetted = true, idDocumentUri = "hana_id_card.png", policeClearanceUri = "hana_criminal_clearance.png", rating = 4.7f, totalTrips = 6, volunteerAvailability = "الأحد والأربعاء (الأقصر)"),
            SupabaseUser(id = 7, name = "سليم خالد", role = "VOLUNTEER", phone = "01155443322", gender = "MALE", isVetted = false, idDocumentUri = "id_card_pending.png", policeClearanceUri = "police_clearance_pending.png", rating = 5.0f, totalTrips = 0, volunteerAvailability = "طوال الأسبوع (المعادي)")
        )
        client.from("users").insert(users)

        // Seed trips
        val trips = listOf(
            SupabaseTrip(id = 1, seekerId = 1, seekerName = "أميرة أحمد", seekerDisabilityType = "حركية (كرسي متحرك)", seekerSpecificNeeds = "دفع الكرسي وصعود المدرجات", title = "مشوار الكلية اليومي - جامعة القاهرة", fromLocation = "المعادي، القاهرة", toLocation = "كلية الآداب، جامعة القاهرة", dateTime = "الإثنين القادم - 08:30 ص", status = "PENDING", helpNeeded = "مرافقة في المواصلات وصعود درجات مبنى الكلية بكرسي حركي.", otp = "5820"),
            SupabaseTrip(id = 2, seekerId = 2, seekerName = "أحمد محمد", seekerDisabilityType = "بصرية (كفيف)", seekerSpecificNeeds = "وصف بصري وإرشاد في المحطة", volunteerId = 5, volunteerName = "محمود علي", title = "سفر بالقطار إلى أسوان", fromLocation = "محطة قطار الجيزة", toLocation = "محطة قطار أسوان", dateTime = "الأربعاء القادم - 10:00 م", status = "ACCEPTED", helpNeeded = "مرافقة وإرشاد في القطار طوال الرحلة ووصف بصري للمعالم عند الحاجة.", otp = "1942"),
            SupabaseTrip(id = 3, seekerId = 3, seekerName = "آية يوسف", seekerDisabilityType = "سمعية (صم وبكم)", seekerSpecificNeeds = "لغة الإشارة أو التواصل بالكتابة", volunteerId = 6, volunteerName = "هنا السادات", title = "زيارة طبيب الأسنان", fromLocation = "شارع التلفزيون، الأقصر", toLocation = "مركز طبيب الأسنان الحديث، الأقصر", dateTime = "الأحد القادم - 06:00 م", status = "COMPLETED", helpNeeded = "مرافقة في العيادة وتسهيل التواصل مع الطبيب وكتابة الملاحظات الطبية.", otp = "3711", ratingToVolunteer = 5, ratingToSeeker = 5, reviewText = "كانت رفقة رائعة جداً ومتفهمة وممتازة بلغة الإشارة!"),
            SupabaseTrip(id = 4, seekerId = 1, seekerName = "أميرة أحمد", seekerDisabilityType = "حركية (كرسي متحرك)", seekerSpecificNeeds = "دفع الكرسي وصعود المدرجات", volunteerId = 4, volunteerName = "هبة مصطفى", title = "زيارة المكتبة المركزية بالجامعة", fromLocation = "المعادي، القاهرة", toLocation = "المكتبة المركزية، جامعة القاهرة", dateTime = "الأسبوع الماضي - 11:00 ص", status = "COMPLETED", helpNeeded = "مساعدة في استعارة الكتب وتحريك الكرسي المتحرك داخل صالات القراءة.", otp = "8891", ratingToVolunteer = 5, ratingToSeeker = 5, reviewText = "هبة إنسانة خلوقة وساعدتني كثيراً في تحضير أوراق البحث الإدارية.")
        )
        client.from("trips").insert(trips)

        // Seed messages
        val messages = listOf(
            SupabaseMessage(tripId = 2, senderId = 5, text = "السلام عليكم يا أحمد، أنا محمود رفيقك في رحلة أسوان. لقد تم تأكيد المشوار وجاهز لمساعدتك.", timestamp = System.currentTimeMillis() - 600000),
            SupabaseMessage(tripId = 2, senderId = 2, text = "وعليكم السلام يا أخي محمود. سعدت جداً بوجودك معي. سأكون في محطة الجيزة قبل موعد القطار بنصف ساعة إن شاء الله.", timestamp = System.currentTimeMillis() - 300000),
            SupabaseMessage(tripId = 2, senderId = 5, text = "ممتاز جداً، سأنتظرك بجوار شباك تذاكر الدرجة الأولى ومعي كارت رفيق للتعريف.", timestamp = System.currentTimeMillis() - 100000)
        )
        client.from("messages").insert(messages)

        Log.d(TAG, "✅ Supabase seeded successfully!")
    }
}
