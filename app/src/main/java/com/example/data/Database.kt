package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Int): User?

    @Query("SELECT * FROM users WHERE role = :role")
    fun getUsersByRole(role: String): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Query("UPDATE users SET isVetted = :vetted WHERE id = :id")
    suspend fun updateVettingStatus(id: Int, vetted: Boolean)

    @Query("UPDATE users SET rating = :rating, totalTrips = :totalTrips WHERE id = :id")
    suspend fun updateRatingAndTrips(id: Int, rating: Float, totalTrips: Int)
}

@Dao
interface TripDao {
    @Query("SELECT * FROM trips ORDER BY id DESC")
    fun getAllTrips(): Flow<List<Trip>>

    @Query("SELECT * FROM trips WHERE id = :id")
    suspend fun getTripById(id: Int): Trip?

    @Query("SELECT * FROM trips WHERE seekerId = :seekerId ORDER BY id DESC")
    fun getTripsBySeeker(seekerId: Int): Flow<List<Trip>>

    @Query("SELECT * FROM trips WHERE volunteerId = :volunteerId ORDER BY id DESC")
    fun getTripsByVolunteer(volunteerId: Int): Flow<List<Trip>>

    @Query("SELECT * FROM trips WHERE status = 'PENDING' ORDER BY id DESC")
    fun getPendingTrips(): Flow<List<Trip>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: Trip): Long

    @Query("UPDATE trips SET status = :status, volunteerId = :volunteerId, volunteerName = :volunteerName WHERE id = :id")
    suspend fun updateTripStatusAndVolunteer(id: Int, status: String, volunteerId: Int?, volunteerName: String?)

    @Query("UPDATE trips SET ratingToVolunteer = :ratingToV, ratingToSeeker = :ratingToS, reviewText = :review, status = 'COMPLETED' WHERE id = :id")
    suspend fun submitReview(id: Int, ratingToV: Int, ratingToS: Int, review: String?)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE tripId = :tripId ORDER BY timestamp ASC")
    fun getMessagesForTrip(tripId: Int): Flow<List<Message>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message): Long
}

@Database(entities = [User::class, Trip::class, Message::class], version = 1, exportSchema = false)
abstract class RafeeqDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun tripDao(): TripDao
    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile
        private var INSTANCE: RafeeqDatabase? = null

        fun getDatabase(context: Context): RafeeqDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RafeeqDatabase::class.java,
                    "rafeeq_database"
                )
                .addCallback(DatabaseCallback(context))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Seed data in a background coroutine
                CoroutineScope(Dispatchers.IO).launch {
                    val database = getDatabase(context)
                    seedDatabase(database)
                }
            }
        }

        suspend fun seedDatabase(database: RafeeqDatabase) {
            val userDao = database.userDao()
            val tripDao = database.tripDao()
            val messageDao = database.messageDao()

            // 1. Seed Support Seekers (ذوي الهمم)
            val amiraId = userDao.insertUser(
                User(
                    id = 1,
                    name = "أميرة أحمد",
                    role = "SEEKER",
                    phone = "01012345678",
                    gender = "FEMALE",
                    disabilityType = "حركية (كرسي متحرك)",
                    specificNeeds = "أحتاج مساعدة في دفع الكرسي وصعود السلالم، ومرافقة متكررة للكلية.",
                    isVetted = true,
                    idDocumentUri = "valid_doc",
                    rating = 4.9f,
                    totalTrips = 8
                )
            ).toInt()

            val ahmedId = userDao.insertUser(
                User(
                    id = 2,
                    name = "أحمد محمد",
                    role = "SEEKER",
                    phone = "01123456789",
                    gender = "MALE",
                    disabilityType = "بصرية (كفيف)",
                    specificNeeds = "أحتاج مرافقة في محطة القطار ووصف بصري وإرشاد أثناء الرحلة الطويلة.",
                    isVetted = true,
                    idDocumentUri = "valid_doc",
                    rating = 5.0f,
                    totalTrips = 3
                )
            ).toInt()

            val ayaId = userDao.insertUser(
                User(
                    id = 3,
                    name = "آية يوسف",
                    role = "SEEKER",
                    phone = "01234567890",
                    gender = "FEMALE",
                    disabilityType = "سمعية (صم وبكم)",
                    specificNeeds = "أحتاج مرافقة لعيادة الطبيب وتواصل عبر لغة الإشارة أو الشات النصي لكتابة التشخيص.",
                    isVetted = true,
                    idDocumentUri = "valid_doc",
                    rating = 4.8f,
                    totalTrips = 5
                )
            ).toInt()

            // 2. Seed Volunteers (المتطوعين)
            val hebaId = userDao.insertUser(
                User(
                    id = 4,
                    name = "هبة مصطفى",
                    role = "VOLUNTEER",
                    phone = "01511223344",
                    gender = "FEMALE",
                    isVetted = true, // Approved
                    idDocumentUri = "heba_id_card.png",
                    policeClearanceUri = "heba_criminal_clearance.png",
                    rating = 4.9f,
                    totalTrips = 12,
                    volunteerAvailability = "الاثنين والأربعاء (طريقي لجامعة القاهرة)"
                )
            ).toInt()

            val mahmoudId = userDao.insertUser(
                User(
                    id = 5,
                    name = "محمود علي",
                    role = "VOLUNTEER",
                    phone = "01099887766",
                    gender = "MALE",
                    isVetted = true, // Approved
                    idDocumentUri = "mahmoud_id_card.png",
                    policeClearanceUri = "mahmoud_criminal_clearance.png",
                    rating = 5.0f,
                    totalTrips = 4,
                    volunteerAvailability = "سفر متكرر لأسوان (بالقطار)"
                )
            ).toInt()

            val hanaId = userDao.insertUser(
                User(
                    id = 6,
                    name = "هنا السادات",
                    role = "VOLUNTEER",
                    phone = "01244556677",
                    gender = "FEMALE",
                    isVetted = true, // Approved
                    idDocumentUri = "hana_id_card.png",
                    policeClearanceUri = "hana_criminal_clearance.png",
                    rating = 4.7f,
                    totalTrips = 6,
                    volunteerAvailability = "الأحد والأربعاء (الأقصر)"
                )
            ).toInt()

            // A new volunteer waiting for admin vetting
            userDao.insertUser(
                User(
                    id = 7,
                    name = "سليم خالد",
                    role = "VOLUNTEER",
                    phone = "01155443322",
                    gender = "MALE",
                    isVetted = false, // PENDING VETTING
                    idDocumentUri = "id_card_pending.png",
                    policeClearanceUri = "police_clearance_pending.png",
                    rating = 5.0f,
                    totalTrips = 0,
                    volunteerAvailability = "طوال الأسبوع (المعادي)"
                )
            )

            // 3. Seed Trips (المشاوير / الرحلات)
            // Trip 1: Amira needs to go to Cairo University
            tripDao.insertTrip(
                Trip(
                    id = 1,
                    seekerId = amiraId,
                    seekerName = "أميرة أحمد",
                    seekerDisabilityType = "حركية (كرسي متحرك)",
                    seekerSpecificNeeds = "دفع الكرسي وصعود المدرجات",
                    title = "مشوار الكلية اليومي - جامعة القاهرة",
                    fromLocation = "المعادي، القاهرة",
                    toLocation = "كلية الآداب، جامعة القاهرة",
                    dateTime = "الإثنين القادم - 08:30 ص",
                    status = "PENDING",
                    helpNeeded = "مرافقة في المواصلات وصعود درجات مبنى الكلية بكرسي حركي.",
                    otp = "5820"
                )
            )

            // Trip 2: Ahmed travels to Aswan
            tripDao.insertTrip(
                Trip(
                    id = 2,
                    seekerId = ahmedId,
                    seekerName = "أحمد محمد",
                    seekerDisabilityType = "بصرية (كفيف)",
                    seekerSpecificNeeds = "وصف بصري وإرشاد في المحطة",
                    title = "سفر بالقطار إلى أسوان",
                    fromLocation = "محطة قطار الجيزة",
                    toLocation = "محطة قطار أسوان",
                    dateTime = "الأربعاء القادم - 10:00 م",
                    status = "ACCEPTED",
                    volunteerId = mahmoudId,
                    volunteerName = "محمود علي",
                    helpNeeded = "مرافقة وإرشاد في القطار طوال الرحلة ووصف بصري للمعالم عند الحاجة.",
                    otp = "1942"
                )
            )

            // Trip 3: Aya visits the doctor
            tripDao.insertTrip(
                Trip(
                    id = 3,
                    seekerId = ayaId,
                    seekerName = "آية يوسف",
                    seekerDisabilityType = "سمعية (صم وبكم)",
                    seekerSpecificNeeds = "لغة الإشارة أو التواصل بالكتابة",
                    title = "زيارة طبيب الأسنان",
                    fromLocation = "شارع التلفزيون، الأقصر",
                    toLocation = "مركز طبيب الأسنان الحديث، الأقصر",
                    dateTime = "الأحد القادم - 06:00 م",
                    status = "COMPLETED",
                    volunteerId = hanaId,
                    volunteerName = "هنا السادات",
                    helpNeeded = "مرافقة في العيادة وتسهيل التواصل مع الطبيب وكتابة الملاحظات الطبية.",
                    otp = "3711",
                    ratingToVolunteer = 5,
                    ratingToSeeker = 5,
                    reviewText = "كانت رفقة رائعة جداً ومتفهمة وممتازة بلغة الإشارة!"
                )
            )

            // Trip 4: Amira completed trip with Heba
            tripDao.insertTrip(
                Trip(
                    id = 4,
                    seekerId = amiraId,
                    seekerName = "أميرة أحمد",
                    seekerDisabilityType = "حركية (كرسي متحرك)",
                    seekerSpecificNeeds = "دفع الكرسي وصعود المدرجات",
                    title = "زيارة المكتبة المركزية بالجامعة",
                    fromLocation = "المعادي، القاهرة",
                    toLocation = "المكتبة المركزية، جامعة القاهرة",
                    dateTime = "الأسبوع الماضي - 11:00 ص",
                    status = "COMPLETED",
                    volunteerId = hebaId,
                    volunteerName = "هبة مصطفى",
                    helpNeeded = "مساعدة في استعارة الكتب وتحريك الكرسي المتحرك داخل صالات القراءة.",
                    otp = "8891",
                    ratingToVolunteer = 5,
                    ratingToSeeker = 5,
                    reviewText = "هبة إنسانة خلوقة وساعدتني كثيراً في تحضير أوراق البحث الإدارية."
                )
            )

            // 4. Seed Chat Messages for Trip 2 (Ahmed & Mahmoud)
            messageDao.insertMessage(
                Message(
                    tripId = 2,
                    senderId = mahmoudId,
                    text = "السلام عليكم يا أحمد، أنا محمود رفيقك في رحلة أسوان. لقد تم تأكيد المشوار وجاهز لمساعدتك.",
                    timestamp = System.currentTimeMillis() - 600000
                )
            )
            messageDao.insertMessage(
                Message(
                    tripId = 2,
                    senderId = ahmedId,
                    text = "وعليكم السلام يا أخي محمود. سعدت جداً بوجودك معي. سأكون في محطة الجيزة قبل موعد القطار بنصف ساعة إن شاء الله.",
                    timestamp = System.currentTimeMillis() - 300000
                )
            )
            messageDao.insertMessage(
                Message(
                    tripId = 2,
                    senderId = mahmoudId,
                    text = "ممتاز جداً، سأنتظرك بجوار شباك تذاكر الدرجة الأولى ومعي كارت رفيق للتعريف.",
                    timestamp = System.currentTimeMillis() - 100000
                )
            )
        }
    }
}
