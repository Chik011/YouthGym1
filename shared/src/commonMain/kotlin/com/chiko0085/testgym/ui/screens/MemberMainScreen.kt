// =============================================================================
// FILE: MemberScreens.kt
// DESKRIPSI: File utama yang berisi semua tampilan (screen) untuk pengguna member
//            aplikasi Youth Gym. File ini mencakup:
//              - Navigasi bawah (Bottom Navigation)
//              - Halaman Home (beranda member)
//              - Kartu kalender mini
//              - Kartu & dialog profil trainer
//              - Halaman Padel (reservasi lapangan)
//              - Halaman About Us (tentang gym)
//              - Galeri fasilitas
//              - Halaman Profil member
// TEKNOLOGI: Jetpack Compose Multiplatform (KMP), Firebase Firestore, Material3
// =============================================================================

package com.chiko0085.testgym.ui.screens

// --- IMPORT COMPOSE & UI ---
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
// Import resource gambar dari folder commonMain/composeResources/drawable/
import youthgym.shared.generated.resources.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- IMPORT MODEL DATA ---
// Model Member menyimpan data seperti nama, sisa kuota, tanggal expired, dll.
import com.chiko0085.testgym.model.Member
// Model Reservation menyimpan data pemesanan slot lapangan padel.
import com.chiko0085.testgym.model.Reservation
// Model Trainer menyimpan data pelatih (nama, spesialisasi, jadwal, dll.)
import com.chiko0085.testgym.Trainer

// --- IMPORT UTILITAS APLIKASI ---
// openWebLink: membuka link browser/WhatsApp secara platform-specific (Android/iOS/Desktop)
import com.chiko0085.testgym.openWebLink
// db: instance Firebase Firestore yang sudah dikonfigurasi di root proyek
import com.chiko0085.testgym.db
// formatEpochToDate: mengonversi timestamp (milidetik) menjadi string tanggal yang bisa dibaca
import com.chiko0085.testgym.formatEpochToDate
// getCurrentTimeMillis: mendapatkan waktu saat ini dalam milidetik (cross-platform)
import com.chiko0085.testgym.getCurrentTimeMillis

// --- IMPORT FIREBASE FIRESTORE ---
import dev.gitlive.firebase.firestore.*

// --- IMPORT WARNA TEMA APLIKASI ---
// Semua warna kustom didefinisikan di file ui/theme/Color.kt
import com.chiko0085.testgym.ui.theme.AccentBlue      // Warna biru aksen utama
import com.chiko0085.testgym.ui.theme.AccentBlueDark  // Biru gelap untuk variasi
import com.chiko0085.testgym.ui.theme.CardDark         // Warna latar belakang kartu (gelap)
import com.chiko0085.testgym.ui.theme.DarkBgEnd        // Warna akhir gradient latar
import com.chiko0085.testgym.ui.theme.DarkBgStart      // Warna awal gradient latar
import com.chiko0085.testgym.ui.theme.TextSub          // Warna teks sekunder (abu-abu)

import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.round

// =============================================================================
// FUNGSI UTILITAS
// =============================================================================

/**
 * [formatDecimal]
 * Membulatkan angka desimal menjadi 1 digit di belakang koma.
 *
 * CARA KERJA:
 *   - Kalikan nilai dengan 10, bulatkan, lalu bagi 10 lagi.
 *   - Contoh: 72.456 → 72.5
 *
 * DIGUNAKAN UNTUK: menampilkan hasil kalkulasi BMI dan berat badan ideal
 * agar tidak terlalu panjang (misalnya 22.3456789 → 22.3).
 *
 * @param value Nilai desimal yang akan diformat
 * @return String angka dengan 1 desimal
 */
fun formatDecimal(value: Double): String {
    return (round(value * 10) / 10.0).toString()
}


// =============================================================================
// SECTION 1: MAIN SCREEN (LAYAR UTAMA MEMBER)
// =============================================================================

/**
 * [MemberMainScreen]
 * Layar utama yang menjadi "kontainer" bagi semua sub-layar member.
 * Menggunakan Scaffold dengan BottomNavigationBar untuk navigasi antar tab.
 *
 * ALUR NAVIGASI (Bottom Navigation Bar):
 *   Tab 0 → Home (MemberHomeScreen)
 *   Tab 1 → Padel (PadelScreen) — reservasi lapangan
 *   Tab 2 → Scan (MemberScanScreen) — scan QR kehadiran
 *   Tab 3 → About (AboutUsScreen) — informasi gym
 *   Tab 4 → Profil (MemberProfileScreen) — edit data diri & logout
 *
 * DESAIN:
 *   - Latar belakang menggunakan gradient vertikal dari DarkBgStart → DarkBgEnd.
 *   - Scaffold containerColor transparan agar gradient tetap terlihat.
 *
 * @param initialMember Data awal member saat pertama kali login
 * @param memberList Daftar semua member (untuk mendapatkan data terbaru via ID)
 * @param onLogout Callback yang dipanggil ketika member menekan tombol logout
 */
@Composable
fun MemberMainScreen(
    initialMember: Member,
    memberList: List<Member>,
    onLogout: () -> Unit,
    onUpdateMember: (Member) -> Unit
) {
    // State untuk melacak tab mana yang sedang aktif (default: tab 0 = Home)
    var selectedTab by remember { mutableIntStateOf(0) }

    // Ambil data member terbaru dari list berdasarkan ID.
    // Ini penting agar perubahan data (nama, kuota, dll.) langsung terlihat
    // tanpa harus restart aplikasi.
    val currentMember = memberList.find { it.id == initialMember.id } ?: initialMember

    // Gradient latar belakang gelap dari atas ke bawah
    val bgGradient = Brush.verticalGradient(listOf(DarkBgStart, DarkBgEnd))

    Box(modifier = Modifier.fillMaxSize().background(bgGradient)) {
        Scaffold(
            // Transparan agar gradient di belakang tetap terlihat
            containerColor = Color.Transparent,
            bottomBar = {
                // --- BOTTOM NAVIGATION BAR ---
                // 5 item navigasi: Home, Padel, Scan, About, Profil
                NavigationBar(
                    containerColor = DarkBgEnd,
                    tonalElevation = 8.dp
                ) {
                    // Tab 0: Home
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("Home") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentBlue, selectedTextColor = AccentBlue,
                            unselectedIconColor = TextSub, unselectedTextColor = TextSub,
                            indicatorColor = CardDark
                        )
                    )
                    // Tab 1: Padel
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
                        label = { Text("Padel") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentBlue, selectedTextColor = AccentBlue,
                            unselectedIconColor = TextSub, unselectedTextColor = TextSub,
                            indicatorColor = CardDark
                        )
                    )
                    // Tab 2: Scan QR
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                        label = { Text("Scan") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentBlue, selectedTextColor = AccentBlue,
                            unselectedIconColor = TextSub, unselectedTextColor = TextSub,
                            indicatorColor = CardDark
                        )
                    )
                    // Tab 3: About Us
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = { Icon(Icons.Default.Info, contentDescription = null) },
                        label = { Text("About") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentBlue, selectedTextColor = AccentBlue,
                            unselectedIconColor = TextSub, unselectedTextColor = TextSub,
                            indicatorColor = CardDark
                        )
                    )
                    // Tab 4: Profil Member
                    NavigationBarItem(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        icon = { Icon(Icons.Default.Person, contentDescription = null) },
                        label = { Text("Profil") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentBlue, selectedTextColor = AccentBlue,
                            unselectedIconColor = TextSub, unselectedTextColor = TextSub,
                            indicatorColor = CardDark
                        )
                    )
                }
            }
        ) { padding ->
            // Konten berubah sesuai tab yang dipilih
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                when (selectedTab) {
                    0 -> MemberHomeScreen(currentMember)
                    1 -> PadelScreen(currentMember)
                    2 -> {
                        // State untuk mencegah kamera memotong kuota berkali-kali
                        var isProcessing by remember { mutableStateOf(false) }
                        var showResultDialog by remember { mutableStateOf(false) }
                        var dialogMessage by remember { mutableStateOf("") }
                        val scope = rememberCoroutineScope()

                        Box(modifier = Modifier.fillMaxSize()) {
                            MemberScanScreen(
                                title = "QR Kehadiran",
                                onResult = { qrData ->
                                    // Cegah scan berulang jika sedang diproses atau dialog masih terbuka
                                    if (!isProcessing && !showResultDialog) {
                                        isProcessing = true

                                        // Validasi isi QR Code (Misal: QR dari Admin berisi teks "CHECKIN_YOUTH_GYM")
                                        if (qrData == "CHECKIN_YOUTH_GYM" || qrData.isNotEmpty()) {

                                            if (currentMember.remainingDays > 0) {
                                                scope.launch {
                                                    try {
                                                        // 1. Kurangi Kuota (Logika Bisnis)
                                                        val newQuota = currentMember.remainingDays - 1
                                                        val updatedMember = currentMember.copy(remainingDays = newQuota)

                                                        // 2. Tembak ke Database Firebase
                                                        // Ini otomatis mengirim data terbaru ke Admin Dashboard!
                                                        db.collection("members").document(currentMember.id).set(updatedMember)

                                                        // 3. Update UI Lokal lewat App.kt
                                                        onUpdateMember(updatedMember)

                                                        // 4. Tampilkan pesan berhasil
                                                        dialogMessage = "Check-in Berhasil!\nSisa kuota kamu sekarang: $newQuota sesi."
                                                        showResultDialog = true
                                                    } catch (e: Exception) {
                                                        dialogMessage = "Gagal Check-in. Periksa koneksi internetmu."
                                                        showResultDialog = true
                                                    } finally {
                                                        isProcessing = false
                                                    }
                                                }
                                            } else {
                                                // Jika kuota sudah 0
                                                dialogMessage = "Akses Ditolak!\nKuota latihan kamu sudah habis."
                                                showResultDialog = true
                                                isProcessing = false
                                            }
                                        }
                                    }
                                }
                            )

                            // Dialog Pop-Up Hasil Scan
                            if (showResultDialog) {
                                AlertDialog(
                                    onDismissRequest = {
                                        showResultDialog = false
                                        // Beri jeda sedikit sebelum bisa scan lagi
                                    },
                                    containerColor = Color(0xFF112240),
                                    title = { Text("Hasil Scan", color = Color.White, fontWeight = FontWeight.Bold) },
                                    text = { Text(dialogMessage, color = Color.LightGray) },
                                    confirmButton = {
                                        Button(
                                            onClick = { showResultDialog = false },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                                        ) {
                                            Text("Tutup", color = Color.White)
                                        }
                                    }
                                )
                            }
                        }
                    }
                    3 -> AboutUsScreen()
                    4 -> MemberProfileScreen(currentMember, onLogout)
                }
            }
        }
    }
}


// =============================================================================
// SECTION 2: HOME SCREEN (BERANDA MEMBER)
// =============================================================================

/**
 * [MemberHomeScreen]
 * Layar beranda yang menjadi halaman pertama setelah login.
 * Menampilkan rangkuman informasi penting member secara sekilas.
 *
 * FITUR UTAMA:
 *   1. Kartu kalender mini → menampilkan tanggal hari ini
 *   2. Kartu status member → sisa kuota latihan, masa aktif (hari), sesi PT
 *   3. Tombol aksi → WhatsApp admin & scan QR alat
 *   4. Carousel personal trainer → list trainer yang bisa diklik untuk info detail
 *   5. Kalkulator BMI → hitung BMI & berat badan ideal dari input tinggi & berat
 *
 * LOGIKA PENTING:
 *   - [calendarDaysLeft]: Sisa hari aktif membership dihitung dari [expiredDate].
 *     Jika [expiredDate] ≤ 0 (data lama/sistem lama), gunakan [remainingDays] sebagai fallback.
 *   - [isActive]: Member dianggap aktif HANYA JIKA kuota sesi (remainingDays) > 0
 *     DAN sisa hari kalender > 0.
 *   - [statusColor]: Warna kartu status berubah sesuai kondisi kuota latihan:
 *       Merah  → kuota < 5 sesi (mendesak)
 *       Kuning → kuota 5-10 sesi (peringatan)
 *       Biru   → kuota > 10 sesi (normal/aman)
 *
 * @param member Data member yang sedang login
 */
@Composable
fun MemberHomeScreen(member: Member) {
    // --- STATE KALKULATOR BMI ---
    // Diisi awal dari data tersimpan di profil member (jika ada)
    var weightInput by remember { mutableStateOf(if ((member.weight ?: 0.0) > 0) (member.weight?.toString() ?: "") else "") }
    var heightInput by remember { mutableStateOf(if ((member.height ?: 0.0) > 0) (member.height?.toString() ?: "") else "") }

    // State untuk menampilkan/menyembunyikan kamera QR scanner
    var showTutorialCamera by remember { mutableStateOf(false) }

    // State untuk dialog profil trainer (null = tidak ada dialog)
    var selectedTrainer by remember { mutableStateOf<Trainer?>(null) }

    // List trainer yang diambil dari Firebase (mutableStateListOf agar otomatis recompose)
    val trainers = remember { mutableStateListOf<Trainer>() }

    // --- KALKULASI SISA MASA AKTIF ---
    val currentTime = com.chiko0085.testgym.getCurrentTimeMillis()
    // Jika expiredDate valid (> 0), hitung selisih hari dari sekarang ke expiredDate.
    // Jika tidak valid (sistem lama), pakai remainingDays langsung.
    val calendarDaysLeft = if (member.expiredDate <= 0L) member.remainingDays
    else ((member.expiredDate - currentTime) / 86400000L).toInt()

    // Member aktif jika KEDUA kondisi terpenuhi
    val isActive = member.remainingDays > 0 && calendarDaysLeft > 0

    // --- WARNA STATUS BERDASARKAN SISA KUOTA SESI ---
    val statusColor = when {
        member.remainingDays < 5 -> Color(0xFFF87171)   // Merah: kritis
        member.remainingDays in 5..10 -> Color(0xFFFBBF24) // Kuning: hampir habis
        else -> AccentBlue                                // Biru: aman
    }

    // --- AMBIL DATA TRAINER DARI FIREBASE (REAL-TIME) ---
    LaunchedEffect(Unit) {
        try {
            // Menggunakan .snapshots.collect untuk "mendengarkan" perubahan secara live
            db.collection("trainers").snapshots.collect { snapshot ->
                val dbTrainers = snapshot.documents.map { it.data<Trainer>() }
                trainers.clear()
                trainers.addAll(dbTrainers)
            }
        } catch (e: Exception) {
            println("Gagal mendengarkan data trainer: ${e.message}")
        }
    }

    // --- TAMPILAN KAMERA QR (overlay penuh) ---
    // Ditampilkan ketika tombol "QR Alat" ditekan
    if (showTutorialCamera) {
        Box(modifier = Modifier.fillMaxSize()) {
            MemberScanScreen(
                title = "QR Code Alat",
                onResult = { println("DEBUG: Tutorial Scan Result: $it") }
            )
            // Tombol X untuk menutup kamera
            IconButton(
                onClick = { showTutorialCamera = false },
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopEnd)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }
    } else {
        // --- TAMPILAN UTAMA HOME (SCROLL) ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- BAGIAN 1: SALAM SAMBUTAN ---
            Text("Selamat Datang,", fontSize = 16.sp, color = TextSub)
            Text(member.name, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Spacer(modifier = Modifier.height(24.dp))

            // --- BAGIAN 2: KALENDER MINI ---
            // Komponen MemberCalendarCard menampilkan tanggal hari ini
            MemberCalendarCard()
            Spacer(modifier = Modifier.height(24.dp))

            // --- BAGIAN 3: KARTU STATUS MEMBERSHIP ---
            // Kartu ini adalah ringkasan paling penting bagi member:
            // berapa sisa sesi latihan, sisa hari aktif, dan sesi PT.
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .background(Brush.verticalGradient(listOf(statusColor.copy(alpha = 0.8f), statusColor)))
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Label kartu
                    Text("Sisa Kuota Latihan", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)

                    // Angka besar: tampilkan jumlah sesi atau teks "HABIS"
                    Text(
                        text = if (isActive) "${member.remainingDays}X" else "HABIS",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.3f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // --- DETAIL: MASA AKTIF & SESI PT ---
                    // Dua kolom: kiri = sisa hari, kanan = sisa sesi PT
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Masa Aktif", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                            Text("$calendarDaysLeft Hari", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                        // Divider vertikal sebagai pemisah visual
                        Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color.White.copy(alpha = 0.3f)))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Sesi PT", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                            Text("${member.remainingPtSessions}X", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tampilkan tanggal kedaluwarsa (hanya bagian tanggal, bukan jam)
                    val expStr = formatEpochToDate(member.expiredDate).split(" ").take(3).joinToString(" ")
                    Surface(color = Color.Black.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp)) {
                        Text(
                            "Berlaku hingga: $expStr",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 12.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- BAGIAN 4: TOMBOL AKSI CEPAT ---
            // Dua tombol berdampingan: hubungi admin via WA & scan QR alat
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Tombol WhatsApp → membuka aplikasi WhatsApp ke nomor admin
                Button(
                    onClick = { openWebLink("https://wa.me/+628123456789") },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Call, null, modifier = Modifier.size(18.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                // Tombol QR Alat → membuka kamera scanner untuk baca QR alat gym
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

            Spacer(modifier = Modifier.height(32.dp))

            // --- BAGIAN 5: CAROUSEL PERSONAL TRAINER ---
            // Menampilkan kartu trainer secara horizontal (bisa digeser kanan-kiri)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Personal Trainer Kami", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color.White)
                Text("Pilih ahlinya untuk capai body goals-mu!", fontSize = 12.sp, color = TextSub)
                Spacer(modifier = Modifier.height(16.dp))

                // LazyRow: render kartu trainer secara lazy (efisien untuk list panjang)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(end = 16.dp)
                ) {
                    items(trainers) { trainer ->
                        // Setiap kartu bisa diklik untuk membuka dialog detail trainer
                        TrainerCard(trainer = trainer, onClick = { selectedTrainer = trainer })
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- BAGIAN 6: KALKULATOR BMI ---
            // Fitur interaktif untuk menghitung indeks massa tubuh dan berat ideal
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

                    // Konfigurasi warna field teks agar sesuai tema gelap
                    val textFieldColors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentBlue, unfocusedBorderColor = TextSub.copy(alpha = 0.3f),
                        cursorColor = AccentBlue, focusedLabelColor = AccentBlue, unfocusedLabelColor = TextSub
                    )

                    // Input berat badan
                    OutlinedTextField(
                        value = weightInput, onValueChange = { weightInput = it },
                        label = { Text("Berat (kg)") }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp), colors = textFieldColors, singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Input tinggi badan
                    OutlinedTextField(
                        value = heightInput, onValueChange = { heightInput = it },
                        label = { Text("Tinggi (cm)") }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp), colors = textFieldColors, singleLine = true
                    )

                    // Konversi input ke Double; default 0.0 jika input tidak valid
                    val weight = weightInput.toDoubleOrNull() ?: 0.0
                    val height = heightInput.toDoubleOrNull() ?: 0.0

                    // Tampilkan hasil hanya jika kedua input valid (> 0)
                    if (weight > 0 && height > 0) {
                        // --- PERHITUNGAN BMI ---
                        // Rumus BMI: berat (kg) / tinggi² (m)
                        val heightInMeters = height / 100.0
                        val bmi = weight / heightInMeters.pow(2.0)

                        // Kategorisasi BMI standar WHO
                        val category = when {
                            bmi < 18.5 -> "Kurus"
                            bmi < 24.9 -> "Normal"
                            bmi < 29.9 -> "Overweight"
                            else -> "Obesitas"
                        }

                        // --- PERHITUNGAN BERAT BADAN IDEAL ---
                        // Batas bawah: BMI 18.5 × tinggi², Batas atas: BMI 24.9 × tinggi²
                        val idealLow = 18.5 * heightInMeters.pow(2.0)
                        val idealHigh = 24.9 * heightInMeters.pow(2.0)

                        Spacer(modifier = Modifier.height(16.dp))
                        // Kartu hasil BMI: hijau jika Normal, merah jika tidak Normal
                        Surface(
                            color = if (category == "Normal") Color(0xFF10B981).copy(alpha = 0.15f)
                            else Color(0xFFEF4444).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "BMI: ${formatDecimal(bmi)} ($category)",
                                    fontWeight = FontWeight.Bold, fontSize = 18.sp,
                                    color = if (category == "Normal") Color(0xFF34D399) else Color(0xFFF87171)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Berat Badan Ideal Anda:", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White)
                                Text(
                                    "${formatDecimal(idealLow)} kg - ${formatDecimal(idealHigh)} kg",
                                    fontWeight = FontWeight.Black, fontSize = 20.sp, color = AccentBlue
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        // Tombol simpan: menyimpan berat & tinggi ke objek member secara lokal (in-memory)
                        // CATATAN: Ini bukan update ke Firebase. Untuk simpan ke cloud, gunakan db.collection("members")...
                        Button(
                            onClick = { member.weight = weight; member.height = height },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                        ) {
                            Text("Simpan ke Profil Lokal", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // --- DIALOG PROFIL TRAINER ---
    // Muncul saat member mengetuk salah satu kartu trainer
    if (selectedTrainer != null) {
        TrainerProfileDialog(
            trainer = selectedTrainer!!,
            onDismiss = { selectedTrainer = null },
            onContact = {
                // Arahkan ke WhatsApp admin (bisa diganti ke nomor trainer jika ada field phone)
                openWebLink("https://wa.me/628123456789")
                selectedTrainer = null
            }
        )
    }
}


// =============================================================================
// SECTION 3: KALENDER MINI
// =============================================================================

/**
 * [MemberCalendarCard]
 * Komponen kartu kecil yang menampilkan tanggal hari ini.
 * Diletakkan di bagian atas HomeScreen sebagai informasi kontekstual.
 *
 * CARA KERJA:
 *   1. Ambil timestamp sekarang dengan getCurrentTimeMillis()
 *   2. Format ke string "dd MMMM yyyy HH:mm" menggunakan formatEpochToDate()
 *   3. Pecah string berdasarkan spasi → ambil bagian hari, bulan, tahun
 *
 * CONTOH OUTPUT:
 *   Tanggal: "15 Juni 2025"
 *   Ikon kalender berwarna biru dengan label bulan singkat (JUN) dan tanggal besar (15)
 */
@Composable
fun MemberCalendarCard() {
    val now = getCurrentTimeMillis()
    val dateStr = formatEpochToDate(now) // Contoh: "15 Juni 2025 08:30"
    val parts = dateStr.split(" ")
    val day = parts.getOrNull(0) ?: ""     // "15"
    val month = parts.getOrNull(1) ?: ""   // "Juni"
    val year = parts.getOrNull(2) ?: ""    // "2025"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Kotak ikon kalender dengan latar biru transparan
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AccentBlue.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // 3 huruf pertama nama bulan, kapital (misal: JUN)
                    Text(month.take(3).uppercase(), color = AccentBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    // Angka tanggal besar
                    Text(day, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            // Teks tanggal lengkap di sebelah ikon
            Column {
                Text("Hari Ini", color = TextSub, fontSize = 11.sp)
                Text("$day $month $year", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}


// =============================================================================
// SECTION 4: KOMPONEN TRAINER
// =============================================================================

/**
 * [TrainerCard]
 * Kartu ringkas untuk menampilkan info trainer di carousel HomeScreen.
 * Dapat diklik untuk membuka dialog detail trainer.
 *
 * TAMPILAN:
 *   - Ikon avatar lingkaran biru (placeholder, bisa diganti foto asli)
 *   - Nama trainer (max 1 baris, terpotong jika terlalu panjang)
 *   - Spesialisasi trainer (max 1 baris)
 *
 * @param trainer Objek Trainer dari Firebase yang akan ditampilkan
 * @param onClick Callback yang dipanggil saat kartu ditekan
 */
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
            // Avatar lingkaran dengan ikon orang (placeholder)
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

/**
 * [TrainerProfileDialog]
 * Dialog modal yang menampilkan profil lengkap trainer ketika kartu trainer ditekan.
 *
 * ISI DIALOG:
 *   - Avatar, nama, dan spesialisasi trainer
 *   - Informasi pengalaman (tahun/deskripsi)
 *   - Deskripsi singkat tentang trainer
 *   - Jadwal melatih (dari list schedules di Firestore)
 *   - Tombol "Hubungi via WhatsApp" → arahkan ke WA admin/trainer
 *   - Tombol "Tutup" untuk menutup dialog
 *
 * @param trainer Data trainer yang akan ditampilkan
 * @param onDismiss Callback saat dialog ditutup (klik di luar atau tombol Tutup)
 * @param onContact Callback saat tombol WhatsApp ditekan
 */
@Composable
fun TrainerProfileDialog(trainer: Trainer, onDismiss: () -> Unit, onContact: () -> Unit) {
    AlertDialog(
        containerColor = CardDark,
        onDismissRequest = onDismiss,
        title = { Text("Profil Coach", fontWeight = FontWeight.Bold, color = Color.White) },
        text = {
            Column {
                // --- HEADER: Avatar + Nama + Spesialisasi ---
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

                // --- INFO PENGALAMAN ---
                Column(horizontalAlignment = Alignment.Start) {
                    Text("Pengalaman", fontSize = 12.sp, color = TextSub)
                    Text(trainer.experience, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- DESKRIPSI TRAINER ---
                Text("Tentang Coach:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text(trainer.description, fontSize = 13.sp, color = Color.LightGray, lineHeight = 20.sp)

                Spacer(modifier = Modifier.height(24.dp))

                // --- JADWAL MELATIH ---
                Text("Jadwal Melatih:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AccentBlue)
                Spacer(modifier = Modifier.height(8.dp))

                if (trainer.schedules.isEmpty()) {
                    Text("Belum ada jadwal publik.", fontSize = 12.sp, color = Color.Gray)
                } else {
                    // Tampilkan setiap jadwal sebagai baris dengan ikon kalender
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
            // Tombol konfirmasi: hubungi via WA
            Button(
                onClick = onContact,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
            ) {
                Text("Hubungi via WhatsApp", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Tutup", color = Color.Gray) }
        }
    )
}


// =============================================================================
// SECTION 5: PADEL SCREEN (RESERVASI LAPANGAN)
// =============================================================================

/**
 * [PadelScreen]
 * Layar untuk melihat ketersediaan slot lapangan padel dan melakukan booking.
 *
 * FITUR:
 *   1. Navigasi tanggal (tombol panah kiri/kanan untuk ganti hari)
 *   2. List slot jam (08:00 - 23:00) dengan status "Tersedia" atau "Sudah Dipesan"
 *   3. Tombol "Booking" → otomatis membuat pesan WhatsApp ke admin dengan info:
 *      nama member, tanggal, dan jam yang dipilih
 *
 * LOGIKA PENGAMBILAN DATA (Firebase):
 *   - LaunchedEffect dipicu setiap kali [selectedDateMillis] berubah
 *   - Query Firestore: cari dokumen di koleksi "reservations" dengan field
 *     "date" sama dengan awal hari (timestamp 00:00 hari itu)
 *   - Slot yang tidak ada di database dianggap kosong (status = "Empty")
 *   - Slot ditampilkan dari jam 8 sampai jam 23 (16 slot per hari)
 *
 * @param member Data member yang sedang login (untuk pesan WA otomatis)
 */
@Composable
fun PadelScreen(member: Member) {
    // Timestamp tanggal yang sedang dipilih (default: hari ini)
    var selectedDateMillis by remember { mutableStateOf(getCurrentTimeMillis()) }
    // List slot reservasi yang akan ditampilkan
    var reservations by remember { mutableStateOf<List<Reservation>>(emptyList()) }
    // State loading saat mengambil data dari Firebase
    var isLoading by remember { mutableStateOf(false) }

    // Ambil data reservasi setiap kali tanggal berubah
    LaunchedEffect(selectedDateMillis) {
        isLoading = true
        try {
            // Konversi timestamp ke string tanggal lalu ke awal hari (00:00)
            val dateStr = formatEpochToDate(selectedDateMillis).split(" ").take(3).joinToString(" ")
            val dayStart = com.chiko0085.testgym.parseDateToMillis(dateStr) ?: selectedDateMillis

            // Query Firebase: ambil reservasi untuk tanggal ini
            val snapshot = db.collection("reservations")
                .where("date", equalTo = dayStart)
                .get()

            val dbReservations = snapshot.documents.map { it.data<Reservation>() }

            // Bangun list penuh (jam 8-23); slot yang tidak ada di DB = kosong
            val fullList = mutableListOf<Reservation>()
            for (i in 8..23) {
                val found = dbReservations.find { it.hour == i }
                fullList.add(found ?: Reservation(
                    id = "${dayStart}_$i",
                    date = dayStart,
                    hour = i,
                    status = "Empty"
                ))
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

        // --- NAVIGASI TANGGAL ---
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
                    Text(
                        formatEpochToDate(selectedDateMillis).split(" ").take(3).joinToString(" "),
                        color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp
                    )
                }
                Row {
                    // Tombol kiri: mundur 1 hari (86400000 ms = 1 hari)
                    IconButton(onClick = { selectedDateMillis -= 86400000L }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                    // Tombol kanan: maju 1 hari
                    IconButton(onClick = { selectedDateMillis += 86400000L }) {
                        Icon(Icons.Default.ArrowForward, null, tint = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- LIST SLOT RESERVASI ---
        if (isLoading) {
            // Tampilkan indikator loading saat menunggu data Firebase
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentBlue)
            }
        } else {
            // LazyColumn: render kartu slot secara efisien (hanya render yang terlihat)
            androidx.compose.foundation.lazy.LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(reservations) { res ->
                    MemberReservationCard(res, onBook = {
                        // Buat pesan WA otomatis dengan info booking
                        val dateStr = formatEpochToDate(res.date).split(" ").take(3).joinToString(" ")
                        val hourStr = if (res.hour < 10) "0${res.hour}:00" else "${res.hour}:00"
                        val message = "Halo Admin Youth Gym, saya ${member.name} ingin booking lapangan Padel untuk tanggal $dateStr jam $hourStr. Apakah masih tersedia?"
                        // Encode spasi menjadi %20 untuk URL yang valid
                        val encodedMessage = message.replace(" ", "%20")
                        openWebLink("https://wa.me/628123456789?text=$encodedMessage")
                    })
                }
            }
        }
    }
}

/**
 * [MemberReservationCard]
 * Kartu untuk satu slot jam reservasi lapangan padel.
 *
 * TAMPILAN:
 *   - Jam slot (misal: "08:00 - 09:00")
 *   - Status: "Tersedia" (hijau) atau "Sudah Dipesan" (merah)
 *   - Jika tersedia: tombol "Booking" berwarna biru
 *   - Jika sudah dipesan: tampilkan nama pemesan
 *
 * @param reservation Data slot reservasi (jam, status, nama pemesan)
 * @param onBook Callback saat tombol Booking ditekan
 */
@Composable
fun MemberReservationCard(reservation: Reservation, onBook: () -> Unit) {
    // Format jam: tambahkan leading zero jika perlu (misal: 8 → "08:00")
    val hourStr = if (reservation.hour < 10) "0${reservation.hour}:00" else "${reservation.hour}:00"
    val nextHour = if (reservation.hour + 1 < 10) "0${reservation.hour + 1}:00" else "${reservation.hour + 1}:00"
    val isAvailable = reservation.status == "Empty"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        // Kartu yang sudah dipesan tampil lebih pudar (alpha 0.5)
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
            // Tampilkan tombol booking HANYA jika slot kosong
            if (isAvailable) {
                Button(
                    onClick = onBook,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Booking", color = Color.White, fontSize = 12.sp)
                }
            } else {
                // Tampilkan nama pemesan jika slot sudah terisi
                Text(reservation.reservedByName, color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}


// =============================================================================
// SECTION 6: ABOUT US SCREEN (TENTANG GYM)
// =============================================================================

/**
 * [AboutUsScreen]
 * Layar informasi tentang Youth Gym dan fasilitasnya.
 *
 * KONTEN:
 *   1. Deskripsi singkat Youth Gym
 *   2. Galeri foto fasilitas gym (8 gambar dalam grid FlowRow)
 *   3. Galeri foto lapangan padel (8 gambar dalam grid FlowRow)
 *   4. Kartu lokasi dengan tombol "Buka di Google Maps"
 *
 * CATATAN UNTUK PENGEMBANG:
 *   - Gambar gym: gym1.jpg s/d gym8.jpg (di folder commonMain/composeResources/drawable/)
 *   - Gambar padel: padel1.jpg s/d padel8.jpg
 *   - Link Google Maps: ganti URL di openWebLink() jika lokasi berubah
 */
@Composable
fun AboutUsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("About Us", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)

        // --- DESKRIPSI SINGKAT YOUTH GYM ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Youth Gym", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AccentBlue)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Aplikasi Manajemen Fasilitas Olahraga Pintar yang dirancang untuk memudahkan member dalam memantau sisa kuota latihan dan reservasi fasilitas olahraga modern.",
                    color = Color.LightGray, textAlign = TextAlign.Justify
                )
            }
        }

        // --- GALERI FASILITAS GYM ---
        Text("Fasilitas Gym", fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.fillMaxWidth())
        val gymImages = listOf(
            Res.drawable.gym1, Res.drawable.gym2, Res.drawable.gym3, Res.drawable.gym4,
            Res.drawable.gym5, Res.drawable.gym6, Res.drawable.gym7, Res.drawable.gym8
        )
        FacilityGallery(images = gymImages)

        // --- GALERI LAPANGAN PADEL ---
        Text("Lapangan Padel", fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.fillMaxWidth())
        val padelImages = listOf(
            Res.drawable.padel1, Res.drawable.padel2, Res.drawable.padel3, Res.drawable.padel4,
            Res.drawable.padel5, Res.drawable.padel6, Res.drawable.padel7, Res.drawable.padel8
        )
        FacilityGallery(images = padelImages)

        // --- KARTU LOKASI (GOOGLE MAPS) ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark)
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = Color(0xFFF87171))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Lokasi Kami", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    // Ganti URL ini jika lokasi gym berubah
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

/**
 * [FacilityGallery]
 * Komponen galeri foto fasilitas dalam layout grid responsif.
 *
 * CARA KERJA:
 *   - Menggunakan [FlowRow] (layout eksperimental) agar gambar otomatis wrap
 *     ke baris baru jika sudah penuh.
 *   - Maksimal 4 item per baris (menyesuaikan di HP, tablet, dll.)
 *   - Setiap gambar memiliki aspect ratio 1:1 (persegi)
 *   - Lebar gambar fleksibel: min 80dp (HP kecil), max 200dp (layar lebar/laptop)
 *
 * UNTUK MENGGANTI GAMBAR:
 *   Letakkan file gambar baru di: shared/src/commonMain/composeResources/drawable/
 *   Lalu daftarkan di list gymImages / padelImages di AboutUsScreen.
 *
 * @param images List resource gambar yang akan ditampilkan
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FacilityGallery(images: List<org.jetbrains.compose.resources.DrawableResource>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 4 // Maksimal 4 kolom per baris
    ) {
        images.forEach { res ->
            Card(
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)           // Mengisi ruang yang tersedia secara merata
                    .widthIn(min = 80.dp, max = 200.dp)  // Responsif untuk berbagai ukuran layar
                    .aspectRatio(1f)      // Selalu persegi (1:1)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = CardDark)
            ) {
                Image(
                    painter = painterResource(res),
                    contentDescription = "Facility Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop // Crop agar mengisi seluruh area kartu
                )
            }
        }
    }
}


// =============================================================================
// SECTION 7: PROFILE SCREEN (PROFIL MEMBER)
// =============================================================================

/**
 * [MemberProfileScreen]
 * Layar untuk melihat dan mengedit data profil member, serta melakukan logout.
 *
 * FITUR:
 *   1. Avatar ikon lingkaran
 *   2. Badge status AKTIF / EXPIRED
 *   3. Form edit: nama lengkap, berat badan, tinggi badan
 *   4. Tombol "Simpan Perubahan" → update data ke Firebase Firestore
 *   5. Tombol "Logout" → kembali ke layar login
 *
 * LOGIKA PENYIMPANAN:
 *   - Data baru dibungkus dalam objek Member baru (copy) untuk keamanan
 *   - Di-update ke Firestore: db.collection("members").document(member.id).set(...)
 *   - Jika update berhasil, data lokal (in-memory) juga diperbarui
 *   - Jika gagal (exception), tidak ada crash — error diabaikan secara diam-diam
 *     (TODO: Idealnya tampilkan snackbar error ke pengguna)
 *
 * @param member Data member yang sedang login
 * @param onLogout Callback yang dipanggil saat tombol logout ditekan
 */
@Composable
fun MemberProfileScreen(member: Member, onLogout: () -> Unit) {
    // CoroutineScope untuk operasi async (update Firebase)
    val scope = rememberCoroutineScope()

    // State form Data Diri
    var name by remember { mutableStateOf(member.name) }
    var weight by remember { mutableStateOf(if ((member.weight ?: 0.0) > 0) (member.weight?.toString() ?: "") else "") }
    var height by remember { mutableStateOf(if ((member.height ?: 0.0) > 0) (member.height?.toString() ?: "") else "") }

    // State form Keamanan Akun
    var username by remember { mutableStateOf(member.username) }
    var password by remember { mutableStateOf(member.password) }
    var passwordVisible by remember { mutableStateOf(false) }

    // State untuk UI Feedback (Loading, Snackbar, & Dialog Konfirmasi)
    var isSaving by remember { mutableStateOf(false) }
    var showSecurityDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // --- KALKULASI SISA MASA AKTIF ---
    val currentTime = com.chiko0085.testgym.getCurrentTimeMillis()
    val calendarDaysLeft = if (member.expiredDate <= 0L) member.remainingDays
    else ((member.expiredDate - currentTime) / 86400000L).toInt()
    val isActive = member.remainingDays > 0 && calendarDaysLeft > 0

    // --- FUNGSI HELPER UNTUK MENYIMPAN DATA ---
    // Dipisahkan agar bisa dipanggil langsung atau melalui dialog konfirmasi
    val performSave = {
        isSaving = true
        val updatedWeight = weight.toDoubleOrNull() ?: 0.0
        val updatedHeight = height.toDoubleOrNull() ?: 0.0

        val updatedMember = member.copy(
            name = name,
            weight = updatedWeight,
            height = updatedHeight,
            username = username,
            password = password
        )

        scope.launch {
            try {
                db.collection("members").document(member.id).set(updatedMember)
                // Perbarui data lokal
                member.name = name
                member.weight = updatedWeight
                member.height = updatedHeight
                member.username = username
                member.password = password

                snackbarHostState.showSnackbar("Data berhasil disimpan!")
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Gagal menyimpan perubahan. Coba lagi.")
            } finally {
                isSaving = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Profil Saya", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Spacer(modifier = Modifier.height(24.dp))

            // --- AVATAR LINGKARAN ---
            Surface(modifier = Modifier.size(80.dp), shape = CircleShape, color = AccentBlue.copy(alpha = 0.2f)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(50.dp), tint = AccentBlue)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // --- BADGE STATUS AKTIF / EXPIRED ---
            Surface(
                color = if (isActive) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isActive) "Status: AKTIF (Sisa $calendarDaysLeft Hari)" else "Status: EXPIRED",
                    color = if (isActive) Color(0xFF34D399) else Color(0xFFF87171),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            val tfColors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                focusedBorderColor = AccentBlue, unfocusedBorderColor = TextSub.copy(alpha = 0.5f),
                focusedLabelColor = AccentBlue, unfocusedLabelColor = TextSub
            )

            // --- FORM EDIT PROFIL (DATA FISIK) ---
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Nama Lengkap") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp), colors = tfColors, singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = weight, onValueChange = { weight = it },
                    label = { Text("Berat (kg)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp), colors = tfColors, singleLine = true
                )
                OutlinedTextField(
                    value = height, onValueChange = { height = it },
                    label = { Text("Tinggi (cm)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp), colors = tfColors, singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(16.dp))

            // --- FORM EDIT KEAMANAN AKUN ---
            Text("Keamanan Akun", fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = username, onValueChange = { username = it },
                label = { Text("Username Login") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp), colors = tfColors, singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("Password Baru") },
                visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.CheckCircle else Icons.Filled.Lock
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(image, "Toggle Password Visibility", tint = TextSub)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp), colors = tfColors, singleLine = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- TOMBOL SIMPAN PERUBAHAN ---
            Button(
                onClick = {
                    if (isSaving) return@Button

                    // Cek apakah ada perubahan di Username atau Password
                    val isAccountChanged = username != member.username || password != member.password

                    if (isAccountChanged) {
                        // Jika ada perubahan kredensial, tampilkan dialog konfirmasi
                        showSecurityDialog = true
                    } else {
                        // Jika hanya ganti nama/berat/tinggi, langsung simpan
                        performSave()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("Simpan Perubahan", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- TOMBOL LOGOUT ---
            TextButton(onClick = onLogout) {
                Text("Logout / Keluar", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(80.dp))
        }

        // --- DIALOG KONFIRMASI KEAMANAN (VERIFIKASI) ---
        if (showSecurityDialog) {
            AlertDialog(
                onDismissRequest = { showSecurityDialog = false },
                containerColor = Color(0xFF112240),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = "Warning", tint = Color(0xFFFBBF24))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verifikasi Keamanan", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Text(
                        "Anda mendeteksi perubahan pada Username atau Password.\n\nPastikan data yang dimasukkan sudah benar karena ini akan digunakan untuk login Anda selanjutnya. Apakah Anda yakin ingin menyimpannya?",
                        color = Color.LightGray,
                        lineHeight = 20.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showSecurityDialog = false
                            performSave() // Jalankan fungsi simpan jika disetujui
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF87171)), // Warna tombol merah agar user lebih waspada
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Ya, Saya Yakin", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSecurityDialog = false }) {
                        Text("Batal", color = Color.Gray)
                    }
                }
            )
        }

        // --- SNACKBAR HOST UNTUK MENAMPILKAN PESAN ---
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = Color(0xFF1E293B),
                contentColor = Color.White,
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}