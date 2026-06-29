package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.Message
import com.example.data.Trip
import com.example.data.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Bilingual Translation Class
class Translation(private val isEnglish: Boolean) {
    fun t(ar: String, en: String): String = if (isEnglish) en else ar
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RafeeqApp(
    viewModel: RafeeqViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isEnglish by viewModel.isEnglish.collectAsState()
    val trans = remember(isEnglish) { Translation(isEnglish) }

    // Navigation and Scaffold
    Scaffold(
        topBar = {
            if (currentScreen != RafeeqScreen.ONBOARDING) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = trans.t("تطبيق رفيق", "Rafeeq App"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = trans.t("خيرُ صُحبة", "Best Companionship"),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (currentScreen == RafeeqScreen.SEEKER_DASHBOARD ||
                                currentScreen == RafeeqScreen.VOLUNTEER_DASHBOARD ||
                                currentScreen == RafeeqScreen.ADMIN_DASHBOARD
                            ) {
                                viewModel.selectUser(null)
                            } else if (currentScreen == RafeeqScreen.CREATE_TRIP || currentScreen == RafeeqScreen.CHAT_SCREEN) {
                                viewModel.setScreen(
                                    if (currentUser?.role == "SEEKER") RafeeqScreen.SEEKER_DASHBOARD
                                    else RafeeqScreen.VOLUNTEER_DASHBOARD
                                )
                            } else if (currentScreen == RafeeqScreen.TRIP_DETAIL) {
                                viewModel.setScreen(
                                    if (currentUser?.role == "SEEKER") RafeeqScreen.SEEKER_DASHBOARD
                                    else RafeeqScreen.VOLUNTEER_DASHBOARD
                                )
                            }
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        // Language Selector
                        IconButton(onClick = { viewModel.toggleLanguage() }) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(Icons.Default.Language, contentDescription = "Language")
                                Text(
                                    text = if (isEnglish) "عربي" else "EN",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        // Reset Db
                        IconButton(onClick = { viewModel.resetDatabase() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reset Database")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                    )
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                RafeeqScreen.ONBOARDING -> OnboardingScreen(viewModel, trans)
                RafeeqScreen.SEEKER_DASHBOARD -> SeekerDashboard(viewModel, trans)
                RafeeqScreen.VOLUNTEER_DASHBOARD -> VolunteerDashboard(viewModel, trans)
                RafeeqScreen.ADMIN_DASHBOARD -> AdminDashboard(viewModel, trans)
                RafeeqScreen.TRIP_DETAIL -> TripDetailsScreen(viewModel, trans)
                RafeeqScreen.CREATE_TRIP -> CreateTripScreen(viewModel, trans)
                RafeeqScreen.CHAT_SCREEN -> ChatScreen(viewModel, trans)
            }
        }
    }
}

// ONBOARDING & LOGIN SCREEN
@Composable
fun OnboardingScreen(viewModel: RafeeqViewModel, trans: Translation) {
    var showRegisterDialog by remember { mutableStateOf(false) }
    var showLoginDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // App Header & Logo
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = trans.t("رفيق - Rafeeq", "Rafeeq - رفيق"),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = trans.t("خيرُ صُحبة", "Best Companionship"),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            // Lang Toggle
            Button(
                onClick = { viewModel.toggleLanguage() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(trans.t("English", "العربية"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Hero Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.img_rafeeq_hero),
                    contentDescription = "Rafeeq Hero",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                            )
                        )
                        .padding(20.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = trans.t("تواصل ملموس وأمان إنساني", "Human Companionship & Security"),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = trans.t(
                                "نربط ذوي الهمم بمتطوعين مؤهلين لمشاوير يومية سعيدة",
                                "Connecting people of determination with trusted volunteers"
                            ),
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Register Button
        Button(
            onClick = { showRegisterDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                trans.t("تسجيل حساب جديد", "Create New Account"),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        // Login Button
        OutlinedButton(
            onClick = { showLoginDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Login, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                trans.t("تسجيل الدخول", "Sign In"),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }

    if (showRegisterDialog) {
        RegistrationDialog(
            trans = trans,
            onDismiss = { showRegisterDialog = false },
            onRegister = { name, role, phone, gender, disType, needs, availability, natId, clearance ->
                viewModel.registerNewUser(name, role, phone, gender, disType, needs, availability, natId, clearance)
                showRegisterDialog = false
            }
        )
    }

    if (showLoginDialog) {
        LoginDialog(
            trans = trans,
            viewModel = viewModel,
            onDismiss = { showLoginDialog = false }
        )
    }
}

@Composable
fun PersonaRow(user: User, trans: Translation, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar Placeholder based on gender
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (user.role == "SEEKER") MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (user.gender == "MALE") Icons.Default.Person else Icons.Default.Face,
                    contentDescription = null,
                    tint = if (user.role == "SEEKER") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = user.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    if (user.role == "VOLUNTEER" && user.isVetted) {
                        Icon(
                            Icons.Default.Verified,
                            contentDescription = "Vetted",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                    } else if (user.role == "VOLUNTEER" && !user.isVetted) {
                        Icon(
                            Icons.Default.HourglassEmpty,
                            contentDescription = "Pending Vetting",
                            tint = Color(0xFFF0AD4E),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                // Description line
                val desc = if (user.role == "SEEKER") {
                    "${user.disabilityType} • ⭐ ${user.rating} (${user.totalTrips} ${trans.t("مشوار", "trips")})"
                } else {
                    "${trans.t("متطوع", "Volunteer")} • ${user.volunteerAvailability ?: ""} • ⭐ ${user.rating}"
                }
                Text(text = desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null)
        }
    }
}

// LOGIN DIALOG
@Composable
fun LoginDialog(
    trans: Translation,
    viewModel: RafeeqViewModel,
    onDismiss: () -> Unit
) {
    val allUsers by viewModel.allUsers.collectAsState()
    var phone by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Icon(
                    Icons.Default.Login,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    text = trans.t("تسجيل الدخول", "Sign In"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = trans.t(
                        "أدخل رقم هاتفك المسجل للدخول إلى حسابك",
                        "Enter your registered phone number to sign in"
                    ),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                // Phone Input
                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        errorMsg = ""
                    },
                    label = { Text(trans.t("رقم الهاتف", "Phone Number")) },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = errorMsg.isNotEmpty()
                )

                if (errorMsg.isNotEmpty()) {
                    Text(
                        text = errorMsg,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }

                // Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(trans.t("إلغاء", "Cancel"))
                    }
                    Button(
                        onClick = {
                            val user = allUsers.find { it.phone == phone.trim() }
                            if (user != null) {
                                viewModel.selectUser(user)
                                onDismiss()
                            } else {
                                errorMsg = trans.t(
                                    "رقم الهاتف غير مسجل. تحقق من الرقم أو سجل حساباً جديداً.",
                                    "Phone number not found. Check the number or register a new account."
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = phone.isNotBlank()
                    ) {
                        Text(trans.t("دخول", "Sign In"), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationDialog(
    trans: Translation,
    onDismiss: () -> Unit,
    onRegister: (String, String, String, String, String?, String?, String?, String?, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("SEEKER") } // "SEEKER" or "VOLUNTEER"
    var phone by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("MALE") }
    
    // Seeker fields
    var disabilityType by remember { mutableStateOf("حركية (كرسي متحرك)") }
    var specificNeeds by remember { mutableStateOf("") }
    
    // Volunteer fields
    var availability by remember { mutableStateOf("") }
    var nationalId by remember { mutableStateOf("uploaded_id.png") }
    var policeClearance by remember { mutableStateOf("uploaded_police_clearance.png") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = trans.t("تسجيل حساب جديد", "Create New Account"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(trans.t("الاسم الكامل", "Full Name")) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(trans.t("رقم الهاتف", "Phone Number")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                // Role Selector
                Text(trans.t("نوع الحساب:", "Account Type:"), fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ElevatedFilterChip(
                        selected = role == "SEEKER",
                        onClick = { role = "SEEKER" },
                        label = { Text(trans.t("طالب دعم (ذوي الهمم)", "Support Seeker")) }
                    )
                    ElevatedFilterChip(
                        selected = role == "VOLUNTEER",
                        onClick = { role = "VOLUNTEER" },
                        label = { Text(trans.t("مقدم دعم (متطوع)", "Volunteer")) }
                    )
                }

                // Gender Selector
                Text(trans.t("الجنس:", "Gender:"), fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ElevatedFilterChip(
                        selected = gender == "MALE",
                        onClick = { gender = "MALE" },
                        label = { Text(trans.t("ذكر", "Male")) }
                    )
                    ElevatedFilterChip(
                        selected = gender == "FEMALE",
                        onClick = { gender = "FEMALE" },
                        label = { Text(trans.t("أنثى", "Female")) }
                    )
                }

                if (role == "SEEKER") {
                    // Disability type
                    Text(trans.t("نوع الإعاقة:", "Disability Type:"), fontWeight = FontWeight.SemiBold)
                    val disabilities = listOf("حركية (كرسي متحرك)", "بصرية (كفيف)", "سمعية (صم وبكم)", "أخرى")
                    var expandedDis by remember { mutableStateOf(false) }
                    
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { expandedDis = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(disabilityType)
                        }
                        DropdownMenu(expanded = expandedDis, onDismissRequest = { expandedDis = false }) {
                            disabilities.forEach { dis ->
                                DropdownMenuItem(
                                    text = { Text(dis) },
                                    onClick = {
                                        disabilityType = dis
                                        expandedDis = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = specificNeeds,
                        onValueChange = { specificNeeds = it },
                        label = { Text(trans.t("الاحتياجات والمساعدة المطلوبة", "Specific Needs/Help")) },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    OutlinedTextField(
                        value = availability,
                        onValueChange = { availability = it },
                        label = { Text(trans.t("أوقات المتاحة والتغطية الجغرافية", "Availability & Geographic Area")) },
                        placeholder = { Text(trans.t("مثال: المعادي - جامعة القاهرة الإثنين والأربعاء", "e.g. Maadi to Cairo Univ - Mon/Wed")) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // SOP mandatory files representation
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = trans.t("متطلبات الأمان الإلزامية (SOP 101):", "Mandatory Security Vetting (SOP 101):"),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp
                            )
                            Text(
                                text = trans.t(
                                    "يجب رفع بطاقة الرقم القومي وصحيفة الحالة الجنائية (فيش وتشبيه) وتفعيل حسابك يدوياً بواسطة الإدارة.",
                                    "You must submit a valid National ID and criminal records check. Your profile will wait for manual admin vetting before you can accept trips."
                                ),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            // Mock Upload State Checkboxes
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(trans.t("تم إرفاق بطاقة الرقم القومي بنجاح", "National ID file attached"), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(trans.t("تم إرفاق الفيش والتشبيه بنجاح (ساري)", "Criminal check attached"), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(trans.t("إلغاء", "Cancel"))
                    }
                    Button(
                        onClick = {
                            if (name.isNotBlank() && phone.isNotBlank()) {
                                onRegister(
                                    name,
                                    role,
                                    phone,
                                    gender,
                                    if (role == "SEEKER") disabilityType else null,
                                    if (role == "SEEKER") specificNeeds else null,
                                    if (role == "VOLUNTEER") availability else null,
                                    if (role == "VOLUNTEER") nationalId else null,
                                    if (role == "VOLUNTEER") policeClearance else null
                                )
                            }
                        },
                        enabled = name.isNotBlank() && phone.isNotBlank()
                    ) {
                        Text(trans.t("إنشاء الحساب", "Sign Up"))
                    }
                }
            }
        }
    }
}


// SEEKER DASHBOARD
@Composable
fun SeekerDashboard(viewModel: RafeeqViewModel, trans: Translation) {
    val seeker by viewModel.currentUser.collectAsState()
    val allTrips by viewModel.allTrips.collectAsState()
    val seekerTrips = remember(allTrips, seeker) {
        allTrips.filter { it.seekerId == seeker?.id }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Seeker Profile Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (seeker?.gender == "MALE") Icons.Default.Person else Icons.Default.Face,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Column {
                    Text(
                        text = seeker?.name ?: "",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "${seeker?.disabilityType}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFF0AD4E),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${seeker?.rating} (${seeker?.totalTrips} ${trans.t("مشوار مكتمل", "completed trips")})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // Action: Create Trip Request
        Button(
            onClick = { viewModel.setScreen(RafeeqScreen.CREATE_TRIP) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(trans.t("طلب رفيق لمشوار جديد", "Request Companion for New Trip"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        // Trips List Title
        Text(
            text = trans.t("مشاويري ورفاقي:", "My Trips & Companions:"),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary
        )

        if (seekerTrips.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.EmojiPeople, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                    Text(trans.t("لا توجد مشاوير مسجلة حالياً.", "No active trips found."), color = MaterialTheme.colorScheme.outline)
                    Text(trans.t("اضغط على الزر أعلاه لطلب رفيق!", "Click the button above to request your first companion!"), fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(seekerTrips) { trip ->
                    TripRow(trip, trans) { viewModel.selectTrip(trip) }
                }
            }
        }
    }
}


// TRIP ROW COMPONENT
@Composable
fun TripRow(trip: Trip, trans: Translation, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = trip.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                StatusBadge(trip.status, trans)
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                Text(
                    text = "${trans.t("من:", "From:")} ${trip.fromLocation} ➔ ${trans.t("إلى:", "To:")} ${trip.toLocation}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.outline)
                Text(
                    text = trip.dateTime,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Companion details if accepted
            if (trip.status != "PENDING" && trip.volunteerName != null) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.EmojiPeople, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                    Text(
                        text = "${trans.t("الرفيق:", "Companion:")} ${trip.volunteerName}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(trans.t("عرض التفاصيل والاتصال ➔", "View & Chat ➔"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            } else {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.HourglassBottom, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFF0AD4E))
                    Text(
                        text = trans.t("قيد المطابقة والبحث عن أفضل صحبة...", "Matching and searching for companions..."),
                        fontSize = 11.sp,
                        color = Color(0xFFF0AD4E)
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String, trans: Translation) {
    val (text, color, containerColor) = when (status) {
        "PENDING" -> Triple(trans.t("قيد الانتظار", "Pending"), Color(0xFFF0AD4E), Color(0xFFFCF8E3))
        "ACCEPTED" -> Triple(trans.t("تم التطابق", "Matched"), MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
        "ACTIVE" -> Triple(trans.t("مشوار نشط", "Active Trip"), MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f))
        "COMPLETED" -> Triple(trans.t("مكتمل السير", "Completed"), MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
        else -> Triple(status, MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.surfaceVariant)
    }

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color)
    ) {
        Text(
            text = text,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}


// CREATE TRIP SCREEN (REQUEST FORM)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTripScreen(viewModel: RafeeqViewModel, trans: Translation) {
    var title by remember { mutableStateOf("") }
    var fromLocation by remember { mutableStateOf("") }
    var toLocation by remember { mutableStateOf("") }
    var dateTime by remember { mutableStateOf("") }
    var helpNeeded by remember { mutableStateOf("") }
    
    var showMapFor by remember { mutableStateOf<String?>(null) } // "FROM" or "TO"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = trans.t("طلب رفيق جديد", "New Companion Errand"),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.primary
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text(trans.t("عنوان المشوار (مثلاً: الذهاب للجامعة أو الطبيب)", "Trip Title / Purpose (e.g., Cairo Univ Lecture)")) },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = fromLocation,
            onValueChange = { fromLocation = it },
            label = { Text(trans.t("نقطة الانطلاق (الموقع الحالي)", "Starting Point (Current Location)")) },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { showMapFor = "FROM" }) {
                    Icon(Icons.Default.Map, contentDescription = "اختر من الخريطة")
                }
            }
        )

        OutlinedTextField(
            value = toLocation,
            onValueChange = { toLocation = it },
            label = { Text(trans.t("الوجهة المقصودة", "Destination Point")) },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { showMapFor = "TO" }) {
                    Icon(Icons.Default.Map, contentDescription = "اختر من الخريطة")
                }
            }
        )

        OutlinedTextField(
            value = dateTime,
            onValueChange = { dateTime = it },
            label = { Text(trans.t("التاريخ والوقت (مثال: الإثنين القادم - 08:30 ص)", "Date & Time (e.g. Next Monday - 08:30 AM)")) },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = helpNeeded,
            onValueChange = { helpNeeded = it },
            label = { Text(trans.t("تفاصيل المساعدة والاحتياجات في هذا المشوار", "Errand description & specific support needed")) },
            placeholder = { Text(trans.t("مثلاً: أحتاج لدفع الكرسي في ممر الجامعة أو مساعدة بالصعود..", "e.g., pushing wheelchair on ramp, reading elevator..")) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (title.isNotBlank() && fromLocation.isNotBlank() && toLocation.isNotBlank()) {
                    viewModel.createTrip(title, fromLocation, toLocation, dateTime, helpNeeded)
                }
            },
            enabled = title.isNotBlank() && fromLocation.isNotBlank() && toLocation.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Icon(Icons.Default.Send, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(trans.t("نشر طلب الرفقة", "Post Companion Errand"), fontWeight = FontWeight.Bold)
        }
    }

    if (showMapFor != null) {
        Dialog(
            onDismissRequest = { showMapFor = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(modifier = Modifier.fillMaxSize()) {
                Column {
                    TopAppBar(
                        title = { Text(if (showMapFor == "FROM") trans.t("حدد نقطة الانطلاق", "Select Starting Point") else trans.t("حدد الوجهة المقصودة", "Select Destination")) },
                        navigationIcon = {
                            IconButton(onClick = { showMapFor = null }) {
                                Icon(Icons.Default.Close, contentDescription = "إغلاق")
                            }
                        }
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        OsmMapScreen(
                            onLocationSelected = { lat, lon ->
                                val locationStr = String.format(java.util.Locale.US, "%.4f, %.4f", lat, lon)
                                if (showMapFor == "FROM") fromLocation = locationStr
                                else toLocation = locationStr
                                showMapFor = null
                            }
                        )
                    }
                }
            }
        }
    }
}


// VOLUNTEER DASHBOARD
@Composable
fun VolunteerDashboard(viewModel: RafeeqViewModel, trans: Translation) {
    val volunteer by viewModel.currentUser.collectAsState()
    val allTrips by viewModel.allTrips.collectAsState()
    val pendingTrips by viewModel.pendingTrips.collectAsState()
    
    val myTrips = remember(allTrips, volunteer) {
        allTrips.filter { it.volunteerId == volunteer?.id }
    }

    var tabIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Volunteer Profile Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(MaterialTheme.colorScheme.secondary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (volunteer?.gender == "MALE") Icons.Default.Person else Icons.Default.Face,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = volunteer?.name ?: "",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        if (volunteer?.isVetted == true) {
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = "Vetted",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Text(
                        text = "${trans.t("الجدول المتاح:", "Availability:")} ${volunteer?.volunteerAvailability ?: ""}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFF0AD4E),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${volunteer?.rating} (${volunteer?.totalTrips} ${trans.t("مشاوير", "trips")})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }

        // SOP Security Check Notification (Vetting)
        if (volunteer?.isVetted == false) {
            Surface(
                color = Color(0xFFFFF9E6),
                border = BorderStroke(1.dp, Color(0xFFF0AD4E)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Warning, contentDescription = "Warning", tint = Color(0xFFF0AD4E), modifier = Modifier.size(32.dp))
                    Text(
                        text = trans.t("الحساب قيد التحقق والتدقيق الأمني (SOP 101)", "Account Awaiting Security Vetting (SOP 101)"),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD9534F),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                    Text(
                        text = trans.t(
                            "مرحباً بك يا ${volunteer?.name}! طبقاً للائحة رفيق الأمنية لحماية ذوي الهمم، لن تتمكن من قبول أي مشاوير إلا بعد قيام مأمور الإدارة بمراجعة فيشك الجنائي وبطاقتك يدوياً.\n\n💡 لتجربة التطبيق، يمكنك تفعيل الحساب فوراً بالدخول إلى 'لوحة تحكم المشرف' من الشاشة الرئيسية واعتماد حسابك بضغطة زر!",
                            "Hello ${volunteer?.name}! Following our strict safety protocol (SOP 101) to protect our seekers, you cannot accept any requests until the admin manually reviews your ID and criminal records check.\n\n💡 To test, go to 'System Admin Dashboard' on the welcome screen and approve your account instantly!"
                        ),
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    
                    Button(
                        onClick = { viewModel.enterAdminMode() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(trans.t("الانتقال للوحة المشرف لتفعيل الحساب ➔", "Go to Admin Panel to Approve Account ➔"))
                    }
                }
            }
        } else {
            // Tab Selector
            TabRow(selectedTabIndex = tabIndex) {
                Tab(
                    selected = tabIndex == 0,
                    onClick = { tabIndex = 0 },
                    text = { Text(trans.t("طلبات متاحة", "Available Requests")) }
                )
                Tab(
                    selected = tabIndex == 1,
                    onClick = { tabIndex = 1 },
                    text = { Text(trans.t("مشاويري (${myTrips.size})", "My Errands (${myTrips.size})")) }
                )
            }

            if (tabIndex == 0) {
                // Pending Trips to Match
                if (pendingTrips.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.VolunteerActivism, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                            Text(trans.t("لا توجد طلبات معلقة حالياً.", "No pending requests available."), color = MaterialTheme.colorScheme.outline)
                            Text(trans.t("شكراً لروحك التطوعية الرائعة!", "Thank you for your volunteer spirit!"), fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(pendingTrips) { trip ->
                            PendingTripCard(trip, trans) { viewModel.selectTrip(trip) }
                        }
                    }
                }
            } else {
                // My accepted/active/completed trips
                if (myTrips.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                            Text(trans.t("لم تقبل أي مشاوير حتى الآن.", "You haven't accepted any trips yet."), color = MaterialTheme.colorScheme.outline)
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(myTrips) { trip ->
                            TripRow(trip, trans) { viewModel.selectTrip(trip) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PendingTripCard(trip: Trip, trans: Translation, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = trip.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                StatusBadge(trip.status, trans)
            }

            Text(
                text = "${trans.t("صاحب الطلب:", "Requester:")} ${trip.seekerName} (${trip.seekerDisabilityType})",
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.secondary
            )

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.outline)
                Text(
                    text = "${trip.fromLocation} ➔ ${trip.toLocation}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.outline)
                Text(
                    text = trip.dateTime,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "${trans.t("الاحتياج:", "Errand assistance:")} ${trip.helpNeeded}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.VolunteerActivism, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(trans.t("تفاصيل المشوار والقبول", "Details & Accompany Request"), fontWeight = FontWeight.Bold)
            }
        }
    }
}


// TRIP DETAILS SCREEN (DETAILED CONTEXT, OTP VALIDATION, SOS, COMPLETED REVIEWS)
@Composable
fun TripDetailsScreen(viewModel: RafeeqViewModel, trans: Translation) {
    val trip by viewModel.selectedTrip.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val isEnglish by viewModel.isEnglish.collectAsState()

    var showOtpDialog by remember { mutableStateOf(false) }
    var showSosDialog by remember { mutableStateOf(false) }
    var showReviewDialog by remember { mutableStateOf(false) }

    val tTrip = trip ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main Title Header
        Text(
            text = tTrip.title,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusBadge(tTrip.status, trans)
            Text(text = tTrip.dateTime, fontSize = 13.sp, color = MaterialTheme.colorScheme.outline)
        }

        // Seeker Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = trans.t("طالب الدعم (ذو الهمة):", "Support Seeker:"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                    Column {
                        Text(text = tTrip.seekerName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(text = "${tTrip.seekerDisabilityType}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (tTrip.seekerSpecificNeeds != null) {
                            Text(text = "${trans.t("الاحتياج الخاص:", "Disability Needs:")} ${tTrip.seekerSpecificNeeds}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Volunteer Card
        if (tTrip.status != "PENDING" && tTrip.volunteerName != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = trans.t("الرفيق المتطوع:", "Companion Volunteer:"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.VolunteerActivism, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        }
                        Column {
                            Text(text = tTrip.volunteerName ?: "", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(text = trans.t("متطوع مؤهل ومعتمد من مبادرة رفيق", "Certified and vetted Rafeeq Volunteer"), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Trip Route Details
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(trans.t("خطة المسار والرفقة:", "Errand & Routing Details:"), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Text(text = "${trans.t("نقطة الانطلاق:", "From Location:")} ${tTrip.fromLocation}", fontSize = 13.sp)
                }
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Flag, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                    Text(text = "${trans.t("الوجهة المقصودة:", "Destination:")} ${tTrip.toLocation}", fontSize = 13.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.HelpOutline, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
                    Text(text = "${trans.t("مواصفات الدعم:", "Support instructions:")} ${tTrip.helpNeeded}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // --- DYNAMIC ACTIONS & LIVETRACKING (TRIP LIFECYCLE) ---
        if (tTrip.status == "PENDING") {
            if (user?.role == "VOLUNTEER") {
                Button(
                    onClick = { viewModel.acceptTripRequest(tTrip) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.Handshake, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(trans.t("موافقة ومرافقة هذا المشوار", "Accept and Accompany Trip"), fontWeight = FontWeight.Bold)
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFCF8E3)),
                    border = BorderStroke(1.dp, Color(0xFFF0AD4E)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = trans.t(
                            "طلبك منشور الآن بنجاح ومتاح لجميع المتطوعين المعتمدين والمؤهلين جغرافياً. ستتلقى إشعاراً فور قبول الرفقة.",
                            "Your companion request is published. Vetted matching volunteers in your area have been notified. We will update you immediately."
                        ),
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = Color(0xFFC09853),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        } else if (tTrip.status == "ACCEPTED") {
            // VERIFICATION SECURITY CODE (OTP) - SOP 203
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = trans.t("الرمز الأمني السري لبدء المشوار (SOP 203)", "Trip Verification Code - OTP (SOP 203)"),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp
                    )
                    
                    if (user?.role == "SEEKER") {
                        Text(
                            text = trans.t(
                                "توجيه أمني: لا تتبادل أرقام الهواتف الشخصية. عند لقاء الرفيق، أعطه هذا الكود السري لبدء المشوار رسمياً في تطبيقه ومشاركة موقعك مع عائلتك.",
                                "Safety Note: Please do not exchange personal phone numbers. When you meet your companion, give them this OTP to verify their identity and start the trip."
                            ),
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text(
                                text = tTrip.otp,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 8.sp,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                            )
                        }
                    } else {
                        Text(
                            text = trans.t(
                                "لقد قبلت المشوار بنجاح! عند مقابلة صديقك ذو الهمة يدوياً، اطلب منه الرمز السري المكون من 4 أرقام وأدخله هنا للتحقق الأمني وبدء المشوار.",
                                "You accepted this companion trip! When you meet your companion face-to-face, ask them for their 4-digit security code (OTP) and enter it to start the trip."
                            ),
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { showOtpDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.LockOpen, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(trans.t("أدخل الرمز السري لبدء المشوار", "Enter Seeker's OTP to Start"), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Open Chat
            Button(
                onClick = { viewModel.setScreen(RafeeqScreen.CHAT_SCREEN) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Default.Chat, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(trans.t("تواصل وغرفة المحادثة الصوتية والكتابية", "Open Interactive Chat Room"), fontWeight = FontWeight.Bold)
            }
        } else if (tTrip.status == "ACTIVE") {
            // ACTIVE TRIP CONTROLS: LIVE TRACKING, SOS, END TRIP
            Card(
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.05f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = trans.t("🔴 المشوار قيد السير والتتبع المباشر حالياً", "🔴 Trip is Currently ACTIVE & Tracked Live"),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp
                    )
                    
                    Text(
                        text = trans.t(
                            "يتم الآن تتبع الموقع الجغرافي بشكل حي ومشاركته تلقائياً مع جهة اتصال الطوارئ (الأهل) لضمان الأمان الأقصى طبقاً للبند (203).",
                            "Live GPS tracking is enabled. Coordinates and companion safety status are shared automatically with family/emergency contact for maximum safety."
                        ),
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    // Visual Map Simulation Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(Color(0xFFE8F0FE), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF4285F4), RoundedCornerShape(12.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Map, contentDescription = null, tint = Color(0xFF4285F4), modifier = Modifier.size(32.dp))
                            Text(trans.t("محاكاة المسار الجغرافي النشط 📍", "Active GPS Routing Simulation 📍"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4285F4))
                            Text(text = "Coordinates: Lat 30.0444 / Lon 31.2357 (Egypt)", fontSize = 9.sp, color = Color.Gray)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // PULSING SOS BUTTON (SOP 401)
                        Button(
                            onClick = { showSosDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = "SOS")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(trans.t("زر الطوارئ SOS", "SOS BUTTON"), fontWeight = FontWeight.Bold)
                        }

                        // End Trip
                        Button(
                            onClick = { viewModel.endTrip(tTrip.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(trans.t("إنهاء المشوار والتقييم", "Complete Trip"), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Chat button during active trip
            Button(
                onClick = { viewModel.setScreen(RafeeqScreen.CHAT_SCREEN) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Default.Chat, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(trans.t("المحادثة الفورية للرفقة", "Chat Room"), fontWeight = FontWeight.Bold)
            }
        } else if (tTrip.status == "COMPLETED") {
            // COMPLETED TRIP: REVIEW SUBMISSION (SOP 204)
            Card(
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(48.dp))
                    Text(
                        text = trans.t("الرحلة تمت بنجاح وبصحبة مباركة!", "Trip Completed Successfully!"),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 15.sp
                    )

                    if (tTrip.ratingToVolunteer != null) {
                        // Show Review details
                        Text(trans.t("تقييم المشوار الساقط:", "Submitted Review Details:"), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(trans.t("تقييم الرفيق المتطوع:", "Volunteer rating:"))
                            repeat(tTrip.ratingToVolunteer!!) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF0AD4E), modifier = Modifier.size(16.dp))
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(trans.t("تقييم صديق الهمة:", "Seeker rating:"))
                            repeat(tTrip.ratingToSeeker!!) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF0AD4E), modifier = Modifier.size(16.dp))
                            }
                        }
                        if (!tTrip.reviewText.isNullOrBlank()) {
                            Text(
                                text = "\"${tTrip.reviewText}\"",
                                fontSize = 13.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Text(
                            text = trans.t(
                                "بناءً على لائحة الأمان والتقييم الثنائي (SOP 204)، يرجى تقييم الرفيق لضمان جودة وأمان خدمات رفيق على أرض الواقع.",
                                "According to our Mutual Rating Policy (SOP 204), please submit a companion review to help us maintain a high level of security and quality."
                            ),
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        
                        Button(
                            onClick = { showReviewDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(trans.t("كتابة وإرسال التقييم الثنائي", "Write Mutual Review"), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // OTP INPUT DIALOG FOR VOLUNTEER
    if (showOtpDialog) {
        var otpText by remember { mutableStateOf("") }
        var isError by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { showOtpDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = trans.t("التحقق من الرمز السري", "Security OTP Verification"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = trans.t(
                            "اطلب من صديقك الرمز المكتوب في شاشته (4 أرقام) لتوثيق اللقاء الجسدي الآمن وبدء الرحلة.",
                            "Enter the 4-digit verification code displayed on the seeker's phone to verify face-to-face meeting."
                        ),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = otpText,
                        onValueChange = { 
                            if (it.length <= 4) {
                                otpText = it
                                isError = false
                            }
                        },
                        label = { Text(trans.t("كود التحقق", "OTP Code")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        isError = isError,
                        modifier = Modifier.width(150.dp)
                    )

                    if (isError) {
                        Text(
                            text = trans.t("الرمز غير صحيح! يرجى التحقق من هاتف صديقك.", "Invalid OTP! Please check the code with your companion."),
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showOtpDialog = false }) {
                            Text(trans.t("إلغاء", "Cancel"))
                        }
                        Button(
                            onClick = {
                                viewModel.startTrip(tTrip.id, otpText) { success ->
                                    if (success) {
                                        showOtpDialog = false
                                    } else {
                                        isError = true
                                    }
                                }
                            },
                            enabled = otpText.length == 4
                        ) {
                            Text(trans.t("تأكيد وبدء المشوار", "Verify & Start"))
                        }
                    }
                }
            }
        }
    }

    // SOS HIGH-PRIORITY PULSING DIALOG (SOP 401)
    if (showSosDialog) {
        SosDialog(trans = trans) { showSosDialog = false }
    }

    // REVIEW AND RATING DIALOG (SOP 204)
    if (showReviewDialog) {
        var ratingToV by remember { mutableIntStateOf(5) }
        var ratingToS by remember { mutableIntStateOf(5) }
        var commentText by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showReviewDialog = false }) {
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.padding(16.dp)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = trans.t("كتابة التقييم والتعليق", "Submit Trip Review"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Rating to volunteer
                    Text(trans.t("تقييمك للرفيق المتطوع (من 1 إلى 5):", "Rate Companion Volunteer (1-5):"), fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (i in 1..5) {
                            IconButton(onClick = { ratingToV = i }) {
                                Icon(
                                    imageVector = if (i <= ratingToV) Icons.Default.Star else Icons.Outlined.StarBorder,
                                    contentDescription = null,
                                    tint = Color(0xFFF0AD4E)
                                )
                            }
                        }
                    }

                    // Rating to seeker
                    Text(trans.t("تقييمك لتمكين وسلوك طالب الدعم:", "Rate Support Seeker (1-5):"), fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (i in 1..5) {
                            IconButton(onClick = { ratingToS = i }) {
                                Icon(
                                    imageVector = if (i <= ratingToS) Icons.Default.Star else Icons.Outlined.StarBorder,
                                    contentDescription = null,
                                    tint = Color(0xFFF0AD4E)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        label = { Text(trans.t("أكتب ملاحظاتك عن رفقة هذا المشوار...", "Write companionship review feedback...")) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showReviewDialog = false }) {
                            Text(trans.t("إلغاء", "Cancel"))
                        }
                        Button(
                            onClick = {
                                viewModel.submitTripReview(tTrip.id, ratingToV, ratingToS, commentText)
                                showReviewDialog = false
                            }
                        ) {
                            Text(trans.t("إرسال التقييم", "Submit"))
                        }
                    }
                }
            }
        }
    }
}


// PULSING SOS FULL SCREEN HIGH-PRIORITY DIALOG (SOP 401)
@Composable
fun SosDialog(trans: Translation, onDismiss: () -> Unit) {
    var countdown by remember { mutableIntStateOf(5) }
    var alertSent by remember { mutableStateOf(false) }

    LaunchedEffect(countdown) {
        if (countdown > 0 && !alertSent) {
            delay(1000)
            countdown--
            if (countdown == 0) {
                alertSent = true
            }
        }
    }

    // Pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, Color.Red, RoundedCornerShape(24.dp)),
            color = Color(0xFF1E0808) // Dark deep red warning background
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Emergency,
                    contentDescription = "Alert",
                    tint = Color.Red,
                    modifier = Modifier.size(64.dp)
                )

                Text(
                    text = trans.t("🚨 إنذار طوارئ نشط - SOS 🚨", "🚨 ACTIVE EMERGENCY - SOS 🚨"),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = Color.Red,
                    textAlign = TextAlign.Center
                )

                if (!alertSent) {
                    Text(
                        text = trans.t(
                            "سيتم الاتصال فوراً بغرفة عمليات رفيق وأقاربك والشرطة وإرسال موقعك الحالي بعد:",
                            "Sending GPS Coordinates & alerting Rafeeq Operations and family in:"
                        ),
                        color = Color.White,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "$countdown",
                        color = Color.Red,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black
                    )
                    
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(trans.t("إلغاء الإنذار (خطر زائف)", "Cancel Alert (False Alarm)"), fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(
                        text = trans.t(
                            "✓ تم إرسال موقعك الدقيق والإنذار لغرفة عمليات رفيق وجهة اتصالك الأمنية ووزارة الداخلية لتوجيه الدعم الفوري.",
                            "✓ Emergency Alert and precise GPS coordinates dispatched to Rafeeq Operations Room, family contacts, and security services immediately."
                        ),
                        color = Color.Green,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )

                    // Immediate emergency call dials representation
                    Surface(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(trans.t("أرقام اتصال سريعة ومباشرة:", "Quick Call Numbers:"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            
                            Button(
                                onClick = {}, // Dial police
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD9534F)),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(trans.t("اتصال بالشرطة (122)", "Call Police (122)"), color = Color.White)
                            }

                            Button(
                                onClick = {}, // Dial ambulance
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0AD4E)),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.LocalHospital, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(trans.t("اتصال بالإسعاف (123)", "Call Ambulance (123)"), color = Color.White)
                            }
                        }
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(trans.t("إغلاق الشاشة", "Close Overlay"))
                    }
                }
            }
        }
    }
}


// BILINGUAL CHAT & ACCESSIBILITY SCREEN
@Composable
fun ChatScreen(viewModel: RafeeqViewModel, trans: Translation) {
    val trip by viewModel.selectedTrip.collectAsState()
    val messages by viewModel.chatMessages.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var messageText by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Chat Header with details
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.Forum, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Column {
                    Text(
                        text = "${trans.t("غرفة مرافقة:", "Errand Companion Room:")} ${trip?.title}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${trans.t("ذو الهمة:", "Seeker:")} ${trip?.seekerName} • ${trans.t("الرفيق:", "Volunteer:")} ${trip?.volunteerName ?: trans.t("لم يتم", "None")}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Accessibility Features Card for different impairments (Ahmed/Aya)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = trans.t("♿ تيسير الوصول والمساعدات الخاصة (SOP 203)", "♿ Accessibility Features (SOP 203)"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                
                // Audio/Visual simulation switches
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Sign Language simulation for Deaf/Aya
                    AssistChip(
                        onClick = { },
                        label = { Text(trans.t("مكالمة لغة إشارة فيديو", "Video Sign Language Call")) },
                        leadingIcon = { Icon(Icons.Default.VideoCameraFront, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    // TTS/Voice control simulation for Blind/Ahmed
                    AssistChip(
                        onClick = { },
                        label = { Text(trans.t("قارئ شاشة مدمج 🔊", "Text To Speech Reader 🔊")) },
                        leadingIcon = { Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }
            }
        }

        // Message List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                val isMe = msg.senderId == currentUser?.id
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Surface(
                        color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = if (isMe) Color.White else MaterialTheme.colorScheme.onSecondaryContainer,
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isMe) 16.dp else 0.dp,
                            bottomEnd = if (isMe) 0.dp else 16.dp
                        ),
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Sender name representation
                            Text(
                                text = if (isMe) trans.t("أنا", "Me") else if (msg.senderId == trip?.seekerId) trip?.seekerName ?: "" else trip?.volunteerName ?: "",
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = if (isMe) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = msg.text, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        // Keyboard inputs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text(trans.t("أكتب رسالتك للرفيق...", "Write your message...")) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp)
            )

            FloatingActionButton(
                onClick = {
                    if (messageText.isNotBlank()) {
                        viewModel.sendChatMessage(messageText)
                        messageText = ""
                    }
                },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}


// ADMIN DASHBOARD (VETTING CONTROL PANEL)
@Composable
fun AdminDashboard(viewModel: RafeeqViewModel, trans: Translation) {
    val users by viewModel.allUsers.collectAsState()
    val volunteers = remember(users) { users.filter { it.role == "VOLUNTEER" } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Surface(
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = trans.t("🛡️ لوحة الإشراف والمراجعة الأمنية (SOP Chapter 1)", "🛡️ Admin Vetting Panel (SOP Chapter 1)"),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 15.sp
                )
                Text(
                    text = trans.t(
                        "حفاظاً على سلامة ذوي الهمم، لن يتمكن أي متطوع من استقبال أو موافقة أي مشوار إلا بعد تفعيل حسابه يدوياً بعد مراجعة مستنداته (الرقم القومي والفيش الجنائي).",
                        "To protect our support seekers, all volunteers must be manually approved by verifying their criminal records check and National ID."
                    ),
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            text = trans.t("قائمة المتطوعين وحالة الاعتماد:", "Volunteers & Vetting Status:"),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(volunteers) { volunteer ->
                AdminVolunteerCard(volunteer, trans) {
                    viewModel.approveVolunteer(volunteer.id)
                }
            }
        }
    }
}

@Composable
fun AdminVolunteerCard(volunteer: User, trans: Translation, onApprove: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = volunteer.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                if (volunteer.isVetted) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(trans.t("معتمد ونشط", "Approved & Active"), color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                } else {
                    Surface(
                        color = Color(0xFFFFF9E6),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFF0AD4E))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF0AD4E), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(trans.t("قيد المراجعة الجنائية", "Awaiting Vetting"), color = Color(0xFFB07A1A), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }

            Text(
                text = "${trans.t("الموبايل:", "Phone:")} ${volunteer.phone} • ${trans.t("المنطقة والجدول:", "Schedule:")} ${volunteer.volunteerAvailability ?: ""}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Documents review row
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(trans.t("المستندات الأمنية المرفوعة:", "Submitted ID & Clearance Files:"), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // National ID
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(volunteer.idDocumentUri ?: "", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }

                        // Criminal Record (فيش وتشبيه)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(volunteer.policeClearanceUri ?: "", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }

            if (!volunteer.isVetted) {
                Button(
                    onClick = onApprove,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(trans.t("اعتماد وتفعيل حساب المتطوع", "Verify and Approve Account"), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Icon helper sizes
fun Modifier.size(size: Int) = this.size(size.dp)
