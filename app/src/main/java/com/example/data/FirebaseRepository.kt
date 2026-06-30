package com.example.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

// ─── Firebase DTOs ─────────────────────────────────────────────────────────

data class FirebaseUser(
    val id: Int = 0,
    val name: String = "",
    val role: String = "",
    val phone: String = "",
    val gender: String = "",
    val disabilityType: String? = null,
    val specificNeeds: String? = null,
    val vetted: Boolean = false,
    val idDocumentUri: String? = null,
    val policeClearanceUri: String? = null,
    val rating: Float = 5.0f,
    val totalTrips: Int = 0,
    val volunteerAvailability: String? = null
)

data class FirebaseTrip(
    val id: Int = 0,
    val seekerId: Int = 0,
    val seekerName: String = "",
    val seekerDisabilityType: String? = null,
    val seekerSpecificNeeds: String? = null,
    val volunteerId: Int? = null,
    val volunteerName: String? = null,
    val title: String = "",
    val fromLocation: String = "",
    val toLocation: String = "",
    val dateTime: String = "",
    val status: String = "",
    val helpNeeded: String = "",
    val otp: String = "",
    val ratingToVolunteer: Int? = null,
    val ratingToSeeker: Int? = null,
    val reviewText: String? = null
)

data class FirebaseMessage(
    val id: Int = 0,
    val tripId: Int = 0,
    val senderId: Int = 0,
    val text: String = "",
    val timestamp: Long = 0L
)

// ─── Extension mappers ───────────────────────────────────────────────────────

fun FirebaseUser.toUser() = User(
    id = id, name = name, role = role, phone = phone, gender = gender,
    disabilityType = disabilityType, specificNeeds = specificNeeds,
    isVetted = vetted, idDocumentUri = idDocumentUri,
    policeClearanceUri = policeClearanceUri, rating = rating,
    totalTrips = totalTrips, volunteerAvailability = volunteerAvailability
)

fun User.toFirebaseUser() = FirebaseUser(
    id = if (id == 0) Random.nextInt(1, Int.MAX_VALUE) else id,
    name = name, role = role, phone = phone, gender = gender,
    disabilityType = disabilityType, specificNeeds = specificNeeds,
    vetted = isVetted, idDocumentUri = idDocumentUri,
    policeClearanceUri = policeClearanceUri, rating = rating,
    totalTrips = totalTrips, volunteerAvailability = volunteerAvailability
)

fun FirebaseTrip.toTrip() = Trip(
    id = id, seekerId = seekerId, seekerName = seekerName,
    seekerDisabilityType = seekerDisabilityType, seekerSpecificNeeds = seekerSpecificNeeds,
    volunteerId = volunteerId, volunteerName = volunteerName, title = title,
    fromLocation = fromLocation, toLocation = toLocation, dateTime = dateTime,
    status = status, helpNeeded = helpNeeded, otp = otp,
    ratingToVolunteer = ratingToVolunteer, ratingToSeeker = ratingToSeeker,
    reviewText = reviewText
)

fun Trip.toFirebaseTrip() = FirebaseTrip(
    id = if (id == 0) Random.nextInt(1, Int.MAX_VALUE) else id,
    seekerId = seekerId, seekerName = seekerName,
    seekerDisabilityType = seekerDisabilityType, seekerSpecificNeeds = seekerSpecificNeeds,
    volunteerId = volunteerId, volunteerName = volunteerName, title = title,
    fromLocation = fromLocation, toLocation = toLocation, dateTime = dateTime,
    status = status, helpNeeded = helpNeeded, otp = otp,
    ratingToVolunteer = ratingToVolunteer, ratingToSeeker = ratingToSeeker,
    reviewText = reviewText
)

fun FirebaseMessage.toMessage() = Message(id = id, tripId = tripId, senderId = senderId, text = text, timestamp = timestamp)

fun Message.toFirebaseMessage() = FirebaseMessage(
    id = if (id == 0) Random.nextInt(1, Int.MAX_VALUE) else id,
    tripId = tripId, senderId = senderId, text = text, timestamp = timestamp
)

// ─── Firebase Repository ──────────────────────────────────────────────────────

class FirebaseRepository {
    private val db = FirebaseFirestore.getInstance()
    private val TAG = "FirebaseRepository"

    // ── Users ────────────────────────────────────────────────────────────────

    fun getAllUsersFlow(): Flow<List<User>> = callbackFlow {
        val listener = db.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error fetching users", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val users = snapshot.documents.mapNotNull { it.toObject(FirebaseUser::class.java)?.toUser() }
                    trySend(users)
                }
            }
        awaitClose { listener.remove() }
    }

    fun getUsersByRoleFlow(role: String): Flow<List<User>> = callbackFlow {
        val listener = db.collection("users")
            .whereEqualTo("role", role)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error fetching users by role", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val users = snapshot.documents.mapNotNull { it.toObject(FirebaseUser::class.java)?.toUser() }
                    trySend(users)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun getUserById(id: Int): User? {
        return try {
            val snapshot = db.collection("users").document(id.toString()).get().await()
            snapshot.toObject(FirebaseUser::class.java)?.toUser()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user $id: ${e.message}", e)
            null
        }
    }

    suspend fun insertUser(user: User): Int {
        return try {
            val firebaseUser = user.toFirebaseUser()
            db.collection("users").document(firebaseUser.id.toString()).set(firebaseUser).await()
            firebaseUser.id
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting user: ${e.message}", e)
            -1
        }
    }

    suspend fun updateVettingStatus(userId: Int, vetted: Boolean) {
        try {
            db.collection("users").document(userId.toString())
                .update("vetted", vetted).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating vetting status: ${e.message}", e)
        }
    }

    suspend fun updateRatingAndTrips(userId: Int, rating: Float, totalTrips: Int) {
        try {
            db.collection("users").document(userId.toString())
                .update(
                    mapOf(
                        "rating" to rating,
                        "totalTrips" to totalTrips
                    )
                ).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating rating: ${e.message}", e)
        }
    }

    // ── Trips ────────────────────────────────────────────────────────────────

    fun getAllTripsFlow(): Flow<List<Trip>> = callbackFlow {
        val listener = db.collection("trips")
            .orderBy("id", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error fetching trips", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val trips = snapshot.documents.mapNotNull { it.toObject(FirebaseTrip::class.java)?.toTrip() }
                    trySend(trips)
                }
            }
        awaitClose { listener.remove() }
    }

    fun getPendingTripsFlow(): Flow<List<Trip>> = callbackFlow {
        val listener = db.collection("trips")
            .whereEqualTo("status", "PENDING")
            .orderBy("id", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error fetching pending trips", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val trips = snapshot.documents.mapNotNull { it.toObject(FirebaseTrip::class.java)?.toTrip() }
                    trySend(trips)
                }
            }
        awaitClose { listener.remove() }
    }

    fun getTripsBySeekerFlow(seekerId: Int): Flow<List<Trip>> = callbackFlow {
        val listener = db.collection("trips")
            .whereEqualTo("seekerId", seekerId)
            .orderBy("id", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error fetching seeker trips", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val trips = snapshot.documents.mapNotNull { it.toObject(FirebaseTrip::class.java)?.toTrip() }
                    trySend(trips)
                }
            }
        awaitClose { listener.remove() }
    }

    fun getTripsByVolunteerFlow(volunteerId: Int): Flow<List<Trip>> = callbackFlow {
        val listener = db.collection("trips")
            .whereEqualTo("volunteerId", volunteerId)
            .orderBy("id", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error fetching volunteer trips", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val trips = snapshot.documents.mapNotNull { it.toObject(FirebaseTrip::class.java)?.toTrip() }
                    trySend(trips)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun getTripById(id: Int): Trip? {
        return try {
            val snapshot = db.collection("trips").document(id.toString()).get().await()
            snapshot.toObject(FirebaseTrip::class.java)?.toTrip()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching trip $id: ${e.message}", e)
            null
        }
    }

    suspend fun insertTrip(trip: Trip): Int {
        return try {
            val firebaseTrip = trip.toFirebaseTrip()
            db.collection("trips").document(firebaseTrip.id.toString()).set(firebaseTrip).await()
            firebaseTrip.id
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting trip: ${e.message}", e)
            -1
        }
    }

    suspend fun updateTripStatusAndVolunteer(
        tripId: Int, status: String, volunteerId: Int?, volunteerName: String?
    ) {
        try {
            db.collection("trips").document(tripId.toString())
                .update(
                    mapOf(
                        "status" to status,
                        "volunteerId" to volunteerId,
                        "volunteerName" to volunteerName
                    )
                ).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating trip: ${e.message}", e)
        }
    }

    suspend fun submitTripReview(
        tripId: Int, ratingToV: Int, ratingToS: Int, reviewText: String?
    ) {
        try {
            db.collection("trips").document(tripId.toString())
                .update(
                    mapOf(
                        "ratingToVolunteer" to ratingToV,
                        "ratingToSeeker" to ratingToS,
                        "reviewText" to reviewText,
                        "status" to "COMPLETED"
                    )
                ).await()

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

    fun getMessagesForTripFlow(tripId: Int): Flow<List<Message>> = callbackFlow {
        val listener = db.collection("messages")
            .whereEqualTo("tripId", tripId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error fetching messages", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val messages = snapshot.documents.mapNotNull { it.toObject(FirebaseMessage::class.java)?.toMessage() }
                    trySend(messages)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun insertMessage(message: Message): Int {
        return try {
            val firebaseMessage = message.toFirebaseMessage()
            db.collection("messages").document(firebaseMessage.id.toString()).set(firebaseMessage).await()
            firebaseMessage.id
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting message: ${e.message}", e)
            -1
        }
    }

    // ── Seed initial data if tables are empty ─────────────────────────────────

    suspend fun seedIfEmpty() {
        try {
            val snapshot = db.collection("users").limit(1).get().await()
            if (snapshot.isEmpty) {
                Log.d(TAG, "Tables empty - seeding Firebase with initial data...")
                seedFirebase()
            } else {
                Log.d(TAG, "Tables already have users - skipping seed.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking/seeding: ${e.message}", e)
        }
    }

    private suspend fun seedFirebase() {
        // Seed users
        val users = listOf(
            FirebaseUser(id = 1, name = "أميرة أحمد", role = "SEEKER", phone = "01012345678", gender = "FEMALE", disabilityType = "حركية (كرسي متحرك)", specificNeeds = "أحتاج مساعدة في دفع الكرسي وصعود السلالم، ومرافقة متكررة للكلية.", vetted = true, idDocumentUri = "valid_doc", rating = 4.9f, totalTrips = 8),
            FirebaseUser(id = 2, name = "أحمد محمد", role = "SEEKER", phone = "01123456789", gender = "MALE", disabilityType = "بصرية (كفيف)", specificNeeds = "أحتاج مرافقة في محطة القطار ووصف بصري وإرشاد أثناء الرحلة الطويلة.", vetted = true, idDocumentUri = "valid_doc", rating = 5.0f, totalTrips = 3),
            FirebaseUser(id = 3, name = "آية يوسف", role = "SEEKER", phone = "01234567890", gender = "FEMALE", disabilityType = "سمعية (صم وبكم)", specificNeeds = "أحتاج مرافقة لعيادة الطبيب وتواصل عبر لغة الإشارة أو الشات النصي لكتابة التشخيص.", vetted = true, idDocumentUri = "valid_doc", rating = 4.8f, totalTrips = 5),
            FirebaseUser(id = 4, name = "هبة مصطفى", role = "VOLUNTEER", phone = "01511223344", gender = "FEMALE", vetted = true, idDocumentUri = "heba_id_card.png", policeClearanceUri = "heba_criminal_clearance.png", rating = 4.9f, totalTrips = 12, volunteerAvailability = "الاثنين والأربعاء (طريقي لجامعة القاهرة)"),
            FirebaseUser(id = 5, name = "محمود علي", role = "VOLUNTEER", phone = "01099887766", gender = "MALE", vetted = true, idDocumentUri = "mahmoud_id_card.png", policeClearanceUri = "mahmoud_criminal_clearance.png", rating = 5.0f, totalTrips = 4, volunteerAvailability = "سفر متكرر لأسوان (بالقطار)"),
            FirebaseUser(id = 6, name = "هنا السادات", role = "VOLUNTEER", phone = "01244556677", gender = "FEMALE", vetted = true, idDocumentUri = "hana_id_card.png", policeClearanceUri = "hana_criminal_clearance.png", rating = 4.7f, totalTrips = 6, volunteerAvailability = "الأحد والأربعاء (الأقصر)"),
            FirebaseUser(id = 7, name = "سليم خالد", role = "VOLUNTEER", phone = "01155443322", gender = "MALE", vetted = false, idDocumentUri = "id_card_pending.png", policeClearanceUri = "police_clearance_pending.png", rating = 5.0f, totalTrips = 0, volunteerAvailability = "طوال الأسبوع (المعادي)")
        )
        for (u in users) {
            db.collection("users").document(u.id.toString()).set(u).await()
        }

        // Seed trips
        val trips = listOf(
            FirebaseTrip(id = 1, seekerId = 1, seekerName = "أميرة أحمد", seekerDisabilityType = "حركية (كرسي متحرك)", seekerSpecificNeeds = "دفع الكرسي وصعود المدرجات", title = "مشوار الكلية اليومي - جامعة القاهرة", fromLocation = "المعادي، القاهرة", toLocation = "كلية الآداب، جامعة القاهرة", dateTime = "الإثنين القادم - 08:30 ص", status = "PENDING", helpNeeded = "مرافقة في المواصلات وصعود درجات مبنى الكلية بكرسي حركي.", otp = "5820"),
            FirebaseTrip(id = 2, seekerId = 2, seekerName = "أحمد محمد", seekerDisabilityType = "بصرية (كفيف)", seekerSpecificNeeds = "وصف بصري وإرشاد في المحطة", volunteerId = 5, volunteerName = "محمود علي", title = "سفر بالقطار إلى أسوان", fromLocation = "محطة قطار الجيزة", toLocation = "محطة قطار أسوان", dateTime = "الأربعاء القادم - 10:00 م", status = "ACCEPTED", helpNeeded = "مرافقة وإرشاد في القطار طوال الرحلة ووصف بصري للمعالم عند الحاجة.", otp = "1942"),
            FirebaseTrip(id = 3, seekerId = 3, seekerName = "آية يوسف", seekerDisabilityType = "سمعية (صم وبكم)", seekerSpecificNeeds = "لغة الإشارة أو التواصل بالكتابة", volunteerId = 6, volunteerName = "هنا السادات", title = "زيارة طبيب الأسنان", fromLocation = "شارع التلفزيون، الأقصر", toLocation = "مركز طبيب الأسنان الحديث، الأقصر", dateTime = "الأحد القادم - 06:00 م", status = "COMPLETED", helpNeeded = "مرافقة في العيادة وتسهيل التواصل مع الطبيب وكتابة الملاحظات الطبية.", otp = "3711", ratingToVolunteer = 5, ratingToSeeker = 5, reviewText = "كانت رفقة رائعة جداً ومتفهمة وممتازة بلغة الإشارة!"),
            FirebaseTrip(id = 4, seekerId = 1, seekerName = "أميرة أحمد", seekerDisabilityType = "حركية (كرسي متحرك)", seekerSpecificNeeds = "دفع الكرسي وصعود المدرجات", volunteerId = 4, volunteerName = "هبة مصطفى", title = "زيارة المكتبة المركزية بالجامعة", fromLocation = "المعادي، القاهرة", toLocation = "المكتبة المركزية، جامعة القاهرة", dateTime = "الأسبوع الماضي - 11:00 ص", status = "COMPLETED", helpNeeded = "مساعدة في استعارة الكتب وتحريك الكرسي المتحرك داخل صالات القراءة.", otp = "8891", ratingToVolunteer = 5, ratingToSeeker = 5, reviewText = "هبة إنسانة خلوقة وساعدتني كثيراً في تحضير أوراق البحث الإدارية.")
        )
        for (t in trips) {
            db.collection("trips").document(t.id.toString()).set(t).await()
        }

        // Seed messages
        val messages = listOf(
            FirebaseMessage(id = 1, tripId = 2, senderId = 5, text = "السلام عليكم يا أحمد، أنا محمود رفيقك في رحلة أسوان. لقد تم تأكيد المشوار وجاهز لمساعدتك.", timestamp = System.currentTimeMillis() - 600000),
            FirebaseMessage(id = 2, tripId = 2, senderId = 2, text = "وعليكم السلام يا أخي محمود. سعدت جداً بوجودك معي. سأكون في محطة الجيزة قبل موعد القطار بنصف ساعة إن شاء الله.", timestamp = System.currentTimeMillis() - 300000),
            FirebaseMessage(id = 3, tripId = 2, senderId = 5, text = "ممتاز جداً، سأنتظرك بجوار شباك تذاكر الدرجة الأولى ومعي كارت رفيق للتعريف.", timestamp = System.currentTimeMillis() - 100000)
        )
        for (m in messages) {
            db.collection("messages").document(m.id.toString()).set(m).await()
        }

        Log.d(TAG, "✅ Firebase seeded successfully!")
    }
}
