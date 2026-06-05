package com.chiko0085.testgym.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
// Mengimpor resource secara manual (pastikan file gambar ada di src/commonMain/composeResources/drawable/)
import youthgym.shared.generated.resources.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chiko0085.testgym.model.Member
import com.chiko0085.testgym.model.Reservation
import com.chiko0085.testgym.Trainer
import com.chiko0085.testgym.openWebLink
import com.chiko0085.testgym.db
import dev.gitlive.firebase.firestore.*
import com.chiko0085.testgym.formatEpochToDate
import com.chiko0085.testgym.getCurrentTimeMillis
// Pastikan package warna sesuai dengan lokasimu
import com.chiko0085.testgym.ui.theme.AccentBlue
import com.chiko0085.testgym.ui.theme.AccentBlueDark
import com.chiko0085.testgym.ui.theme.CardDark
import com.chiko0085.testgym.ui.theme.DarkBgEnd
import com.chiko0085.testgym.ui.theme.DarkBgStart
import com.chiko0085.testgym.ui.theme.TextSub
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.round

fun formatDecimal(value: Double): String {
    return (round(value * 10) / 10.0).toString()
}

@Composable
fun MemberMainScreen(
    initialMember: Member,
    memberList: List<Member>,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val currentMember = memberList.find { it.id == initialMember.id } ?: initialMember

    val bgGradient = Brush.verticalGradient(listOf(DarkBgStart, DarkBgEnd))

    Box(modifier = Modifier.fillMaxSize().background(bgGradient)) {
        Scaffold(
            containerColor = Color.Transparent, // Tembus pandang agar gradien terlihat
            bottomBar = {
                NavigationBar(
                    containerColor = DarkBgEnd,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("Home") },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = AccentBlue, selectedTextColor = AccentBlue, unselectedIconColor = TextSub, unselectedTextColor = TextSub, indicatorColor = CardDark)
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
                        label = { Text("Padel") },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = AccentBlue, selectedTextColor = AccentBlue, unselectedIconColor = TextSub, unselectedTextColor = TextSub, indicatorColor = CardDark)
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                        label = { Text("Scan") },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = AccentBlue, selectedTextColor = AccentBlue, unselectedIconColor = TextSub, unselectedTextColor = TextSub, indicatorColor = CardDark)
                    )
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = { Icon(Icons.Default.Info, contentDescription = null) },
                        label = { Text("About") },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = AccentBlue, selectedTextColor = AccentBlue, unselectedIconColor = TextSub, unselectedTextColor = TextSub, indicatorColor = CardDark)
                    )
                    NavigationBarItem(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        icon = { Icon(Icons.Default.Person, contentDescription = null) },
                        label = { Text("Profil") },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = AccentBlue, selectedTextColor = AccentBlue, unselectedIconColor = TextSub, unselectedTextColor = TextSub, indicatorColor = CardDark)
                    )
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                when (selectedTab) {
                    0 -> MemberHomeScreen(currentMember)
                    1 -> PadelScreen(currentMember)
                    2 -> MemberScanScreen(title = "QR Kehadiran", onResult = { println("DEBUG: Scan Result: $it") })
                    3 -> AboutUsScreen()
                    4 -> MemberProfileScreen(currentMember, onLogout)
                }
            }
        }
    }
}

@Composable
fun MemberHomeScreen(member: Member) {
    var weightInput by remember { mutableStateOf(if((member.weight ?: 0.0) > 0) (member.weight?.toString() ?: "") else "") }
    var heightInput by remember { mutableStateOf(if((member.height ?: 0.0) > 0) (member.height?.toString() ?: "") else "") }
    var showTutorialCamera by remember { mutableStateOf(false) }

    var selectedTrainer by remember { mutableStateOf<Trainer?>(null) }
    val trainers = remember { mutableStateListOf<Trainer>() }

    // --- LOGIKA WAKTU KALENDER ---
    // Membaca waktu saat ini menggunakan fungsi KMP
    val currentTime = com.chiko0085.testgym.getCurrentTimeMillis()
    // Menghitung sisa hari kalender (Jika Expired Date <= 0, berarti sistem lama, anggap sama dengan remainingDays)
    val calendarDaysLeft = if (member.expiredDate <= 0L) member.remainingDays else ((member.expiredDate - currentTime) / 86400000L).toInt()
    // Member aktif jika kuota > 0 DAN hari di kalender > 0
    val isActive = member.remainingDays > 0 && calendarDaysLeft > 0

    // Menarik data Trainer langsung dari Firebase
    LaunchedEffect(Unit) {
        try {
            val trainersSnapshot = db.collection("trainers").get()
            val dbTrainers = trainersSnapshot.documents.map { it.data<Trainer>() }
            trainers.clear()
            trainers.addAll(dbTrainers)
        } catch (e: Exception) { println("Gagal ambil data trainer: ${e.message}") }
    }

    if (showTutorialCamera) {
        Box(modifier = Modifier.fillMaxSize()) {
            MemberScanScreen(title = "QR Code Alat", onResult = { println("DEBUG: Tutorial Scan Result: $it") })
            IconButton(
                onClick = { showTutorialCamera = false },
                modifier = Modifier.padding(16.dp).align(Alignment.TopEnd).background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) { Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White) }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Selamat Datang,", fontSize = 16.sp, color = TextSub)
            Text(member.name, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Spacer(modifier = Modifier.height(24.dp))

            // --- TAMBAHAN: KALENDER MINI ---
            MemberCalendarCard()

            Spacer(modifier = Modifier.height(24.dp))

            // KARTU SISA KUOTA NEON GLOW (DENGAN LOGIKA WAKTU KALENDER)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Box(modifier = Modifier.background(Brush.linearGradient(listOf(AccentBlueDark, AccentBlue))).padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Sisa Kuota Latihan", color = Color.White.copy(alpha = 0.8f))
                        Text(
                            text = if (isActive) "${member.remainingDays}X" else "HABIS",
                            fontSize = 64.sp, fontWeight = FontWeight.Black, color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // --- INFO TANGGAL DAFTAR & EXPIRED ---
                        val joinStr = formatEpochToDate(member.joinDate).split(" ").take(3).joinToString(" ")
                        val expStr = formatEpochToDate(member.expiredDate).split(" ").take(3).joinToString(" ")
                        
                        Surface(color = Color.Black.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp)) {
                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Mulai: $joinStr", fontSize = 12.sp, color = Color.White.copy(alpha = 0.9f))
                                Text("Sampai: $expStr", fontSize = 12.sp, color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isActive) "Berlaku $calendarDaysLeft Hari Lagi" else "Silakan Perpanjang Paket",
                            fontWeight = FontWeight.Bold, color = if (isActive) Color.White else Color(0xFFF87171)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { openWebLink("https://wa.me/+628123456789") },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)), // Emerald Green
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Call, null, modifier = Modifier.size(18.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Button(
                    onClick = { showTutorialCamera = true },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CardDark),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp), tint = AccentBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("QR Alat", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentBlue)
                }
            }

            Spacer(modifier = Modifier.height(32.dp)) // Menambahkan jarak antar section

            // --- CAROUSEL PERSONAL TRAINER ---
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Personal Trainer Kami", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color.White)
                Text("Pilih ahlinya untuk capai body goals-mu!", fontSize = 12.sp, color = TextSub)
                Spacer(modifier = Modifier.height(16.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(end = 16.dp)
                ) {
                    items(trainers) { trainer ->
                        TrainerCard(trainer = trainer, onClick = { selectedTrainer = trainer })
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- KALKULATOR BMI (DARK THEME) ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("BMI & Ideal Weight Calculator", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
                    Text("Pantau kondisi fisikmu", fontSize = 12.sp, color = TextSub)
                    Spacer(modifier = Modifier.height(16.dp))

                    val textFieldColors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentBlue, unfocusedBorderColor = TextSub.copy(alpha = 0.3f),
                        cursorColor = AccentBlue, focusedLabelColor = AccentBlue, unfocusedLabelColor = TextSub
                    )

                    OutlinedTextField(
                        value = weightInput, onValueChange = { weightInput = it },
                        label = { Text("Berat (kg)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors, singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = heightInput, onValueChange = { heightInput = it },
                        label = { Text("Tinggi (cm)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors, singleLine = true
                    )

                    val weight = weightInput.toDoubleOrNull() ?: 0.0
                    val height = heightInput.toDoubleOrNull() ?: 0.0

                    if (weight > 0 && height > 0) {
                        val heightInMeters = height / 100.0
                        val bmi = weight / heightInMeters.pow(2.0)
                        val category = when {
                            bmi < 18.5 -> "Kurus"
                            bmi < 24.9 -> "Normal"
                            bmi < 29.9 -> "Overweight"
                            else -> "Obesitas"
                        }

                        val idealLow = 18.5 * heightInMeters.pow(2.0)
                        val idealHigh = 24.9 * heightInMeters.pow(2.0)

                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            color = if(category == "Normal") Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("BMI: ${formatDecimal(bmi)} ($category)", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = if(category == "Normal") Color(0xFF34D399) else Color(0xFFF87171))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Berat Badan Ideal Anda:", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White)
                                Text("${formatDecimal(idealLow)} kg - ${formatDecimal(idealHigh)} kg", fontWeight = FontWeight.Black, fontSize = 20.sp, color = AccentBlue)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { member.weight = weight; member.height = height },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                        ) { Text("Simpan ke Profil Lokal", color = Color.White) }
                    }
                }
            }
        }
    }

    if (selectedTrainer != null) {
        TrainerProfileDialog(
            trainer = selectedTrainer!!,
            onDismiss = { selectedTrainer = null },
            onContact = { 
                // Menggunakan nomor simulasi atau jika ada field phone di Trainer bisa dipakai
                openWebLink("https://wa.me/628123456789") 
                selectedTrainer = null 
            }
        )
    }
}

@Composable
fun MemberCalendarCard() {
    val now = getCurrentTimeMillis()
    val dateStr = formatEpochToDate(now) // "dd MMMM yyyy HH:mm"
    val parts = dateStr.split(" ")
    val day = parts.getOrNull(0) ?: ""
    val month = parts.getOrNull(1) ?: ""
    val year = parts.getOrNull(2) ?: ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AccentBlue.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(month.take(3).uppercase(), color = AccentBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(day, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Hari Ini", color = TextSub, fontSize = 11.sp)
                Text("$day $month $year", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- DESAIN KARTU TRAINER TANPA RATING (DARK MODE) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainerCard(trainer: Trainer, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(160.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(shape = CircleShape, modifier = Modifier.size(64.dp), color = AccentBlue) {
                Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.padding(12.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(trainer.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White, maxLines = 1)
            Spacer(modifier = Modifier.height(4.dp))
            Text(trainer.specialization, fontSize = 11.sp, color = TextSub, maxLines = 1)
        }
    }
}

// --- DESAIN PROFIL TRAINER TANPA RATING (DARK MODE) ---
@Composable
fun TrainerProfileDialog(trainer: Trainer, onDismiss: () -> Unit, onContact: () -> Unit) {
    AlertDialog(
        containerColor = CardDark,
        onDismissRequest = onDismiss,
        title = { Text("Profil Coach", fontWeight = FontWeight.Bold, color = Color.White) },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, modifier = Modifier.size(60.dp), color = AccentBlue.copy(alpha = 0.2f)) {
                        Icon(Icons.Default.Person, null, modifier = Modifier.padding(12.dp), tint = AccentBlue)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(trainer.name, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color.White)
                        Text(trainer.specialization, fontSize = 14.sp, color = TextSub)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                Column(horizontalAlignment = Alignment.Start) {
                    Text("Pengalaman", fontSize = 12.sp, color = TextSub)
                    Text(trainer.experience, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("Tentang Coach:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text(trainer.description, fontSize = 13.sp, color = Color.LightGray, lineHeight = 20.sp)

                Spacer(modifier = Modifier.height(24.dp))
                Text("Jadwal Melatih:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AccentBlue)
                Spacer(modifier = Modifier.height(8.dp))
                if (trainer.schedules.isEmpty()) {
                    Text("Belum ada jadwal publik.", fontSize = 12.sp, color = Color.Gray)
                } else {
                    trainer.schedules.forEach { schedule ->
                        Surface(
                            modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth(),
                            color = Color.White.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DateRange, null, tint = AccentBlue, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(schedule, fontSize = 13.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onContact, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))) {
                Text("Hubungi via WhatsApp", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Tutup", color = Color.Gray) }
        }
    )
}

// --- SCREEN LAINNYA DI TEMA DARK MODE ---
@Composable
fun PadelScreen(member: Member) {
    var selectedDateMillis by remember { mutableStateOf(getCurrentTimeMillis()) }
    var reservations by remember { mutableStateOf<List<Reservation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(selectedDateMillis) {
        isLoading = true
        try {
            val dateStr = formatEpochToDate(selectedDateMillis).split(" ").take(3).joinToString(" ")
            val dayStart = com.chiko0085.testgym.parseDateToMillis(dateStr) ?: selectedDateMillis
            
            val snapshot = db.collection("reservations")
                .where("date", equalTo = dayStart)
                .get()
            
            val dbReservations = snapshot.documents.map { it.data<Reservation>() }
            val fullList = mutableListOf<Reservation>()
            // Batasi jam buka: 08:00 sampai 00:00 (jam 23 ke 24)
            for (i in 8..23) {
                val found = dbReservations.find { it.hour == i }
                fullList.add(found ?: Reservation(id = "${dayStart}_$i", date = dayStart, hour = i, status = "Empty"))
            }
            reservations = fullList
        } catch (e: Exception) {
            println("ERROR: Gagal ambil reservasi: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Reservasi Lapangan Padel", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Pilih Tanggal", color = TextSub, fontSize = 12.sp)
                    Text(formatEpochToDate(selectedDateMillis).split(" ").take(3).joinToString(" "), 
                        color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Row {
                    IconButton(onClick = { selectedDateMillis -= 86400000L }) { 
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) 
                    }
                    IconButton(onClick = { selectedDateMillis += 86400000L }) { 
                        Icon(Icons.Default.ArrowForward, null, tint = Color.White)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                androidx.compose.material3.CircularProgressIndicator(color = AccentBlue)
            }
        } else {
            androidx.compose.foundation.lazy.LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                items(reservations) { res ->
                    MemberReservationCard(res, onBook = {
                        val dateStr = formatEpochToDate(res.date).split(" ").take(3).joinToString(" ")
                        val hourStr = if (res.hour < 10) "0${res.hour}:00" else "${res.hour}:00"
                        val message = "Halo Admin Youth Gym, saya ${member.name} ingin booking lapangan Padel untuk tanggal $dateStr jam $hourStr. Apakah masih tersedia?"
                        
                        // Encode manual sederhana untuk spasi
                        val encodedMessage = message.replace(" ", "%20")
                        openWebLink("https://wa.me/628123456789?text=$encodedMessage")
                    })
                }
            }
        }
    }
}

@Composable
fun MemberReservationCard(reservation: Reservation, onBook: () -> Unit) {
    val hourStr = if (reservation.hour < 10) "0${reservation.hour}:00" else "${reservation.hour}:00"
    val nextHour = if (reservation.hour + 1 < 10) "0${reservation.hour + 1}:00" else "${reservation.hour + 1}:00"
    val isAvailable = reservation.status == "Empty"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (isAvailable) CardDark else CardDark.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("$hourStr - $nextHour", color = Color.White, fontWeight = FontWeight.Bold)
                Text(
                    text = if (isAvailable) "Tersedia" else "Sudah Dipesan",
                    color = if (isAvailable) Color(0xFF34D399) else Color(0xFFF87171),
                    fontSize = 12.sp
                )
            }
            
            if (isAvailable) {
                Button(
                    onClick = onBook,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Booking", color = Color.White, fontSize = 12.sp)
                }
            } else {
                Text(reservation.reservedByName, color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun AboutUsScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("About Us", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        
        // Deskripsi Youth Gym
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardDark)) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Youth Gym", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AccentBlue)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Aplikasi Manajemen Fasilitas Olahraga Pintar yang dirancang untuk memudahkan member dalam memantau sisa kuota latihan dan reservasi fasilitas olahraga modern.", color = Color.LightGray, textAlign = TextAlign.Justify)
            }
        }

        // Fasilitas Gym (Gallery)
        Text("Fasilitas Gym", fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.fillMaxWidth())
        val gymImages = listOf(
            Res.drawable.gym1, Res.drawable.gym2, Res.drawable.gym3, Res.drawable.gym4,
            Res.drawable.gym5, Res.drawable.gym6, Res.drawable.gym7, Res.drawable.gym8
        )
        FacilityGallery(images = gymImages)

        // Lapangan Padel (Gallery)
        Text("Lapangan Padel", fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.fillMaxWidth())
        val padelImages = listOf(
            Res.drawable.padel1, Res.drawable.padel2, Res.drawable.padel3, Res.drawable.padel4,
            Res.drawable.padel5, Res.drawable.padel6, Res.drawable.padel7, Res.drawable.padel8
        )
        FacilityGallery(images = padelImages)

        // Lokasi Google Maps
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardDark)) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = Color(0xFFF87171))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Lokasi Kami", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { openWebLink("https://maps.app.goo.gl/1qQr52B4W7vgrKa47") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Place, null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Buka di Google Maps", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FacilityGallery(images: List<org.jetbrains.compose.resources.DrawableResource>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start), // Dari kiri ke kanan
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 4 
    ) {
        images.forEach { res ->
            Card(
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f) // Membuat gambar fleksibel mengisi ruang
                    .widthIn(min = 80.dp, max = 200.dp) // Minimal 80dp (HP), Maksimal 200dp (Laptop/Layar Lebar)
                    .aspectRatio(1f) // Tetap kotak
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp)), // Tambah border halus
                colors = CardDefaults.cardColors(containerColor = CardDark)
            ) {
                Image(
                    painter = painterResource(res),
                    contentDescription = "Facility Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun MemberProfileScreen(member: Member, onLogout: () -> Unit) {
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf(member.name) }
    var weight by remember { mutableStateOf(if((member.weight ?: 0.0) > 0) (member.weight?.toString() ?: "") else "") }
    var height by remember { mutableStateOf(if((member.height ?: 0.0) > 0) (member.height?.toString() ?: "") else "") }

    // --- LOGIKA WAKTU KALENDER (SAMA SEPERTI HOME) ---
    val currentTime = com.chiko0085.testgym.getCurrentTimeMillis()
    val calendarDaysLeft = if (member.expiredDate <= 0L) member.remainingDays else ((member.expiredDate - currentTime) / 86400000L).toInt()
    val isActive = member.remainingDays > 0 && calendarDaysLeft > 0

    Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Profil Saya", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        Spacer(modifier = Modifier.height(24.dp))
        Surface(modifier = Modifier.size(80.dp), shape = CircleShape, color = AccentBlue.copy(alpha = 0.2f)) {
            Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, null, modifier = Modifier.size(50.dp), tint = AccentBlue) }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // STATUS AKTIF / EXPIRED
        Surface(color = if (isActive) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
            Text(
                text = if (isActive) "Status: AKTIF (Sisa $calendarDaysLeft Hari)" else "Status: EXPIRED",
                color = if (isActive) Color(0xFF34D399) else Color(0xFFF87171),
                fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        val tfColors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = AccentBlue, unfocusedBorderColor = TextSub.copy(alpha = 0.5f), focusedLabelColor = AccentBlue, unfocusedLabelColor = TextSub)

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Lengkap") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = tfColors)
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("Berat (kg)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = tfColors)
            OutlinedTextField(value = height, onValueChange = { height = it }, label = { Text("Tinggi (cm)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = tfColors)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                val updatedWeight = weight.toDoubleOrNull() ?: 0.0
                val updatedHeight = height.toDoubleOrNull() ?: 0.0
                val updatedMember = member.copy(name = name, weight = updatedWeight, height = updatedHeight)
                scope.launch {
                    try {
                        db.collection("members").document(member.id).set(updatedMember)
                        member.name = name; member.weight = updatedWeight; member.height = updatedHeight
                    } catch (_: Exception) {}
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
        ) { Text("Simpan Perubahan", fontWeight = FontWeight.Bold, color = Color.White) }
        Spacer(modifier = Modifier.weight(1f))
        TextButton(onClick = onLogout) { Text("Logout / Keluar", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold) }
    }
}