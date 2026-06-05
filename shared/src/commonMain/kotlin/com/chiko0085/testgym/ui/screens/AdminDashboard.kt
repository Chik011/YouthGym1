package com.chiko0085.testgym.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chiko0085.testgym.db
import dev.gitlive.firebase.firestore.*
import com.chiko0085.testgym.formatEpochToDate
import com.chiko0085.testgym.getCurrentTimeMillis
import com.chiko0085.testgym.model.Admin
import com.chiko0085.testgym.model.GymPackage
import com.chiko0085.testgym.model.Member
import com.chiko0085.testgym.ui.theme.*
import com.chiko0085.testgym.exportToExcel
import kotlinx.coroutines.launch
import kotlin.random.Random
import com.chiko0085.testgym.Trainer
import com.chiko0085.testgym.model.Reservation

// Primary Colors dari Theme
val DarkBgStart = Color(0xFF0F172A)
val DarkBgEnd = Color(0xFF1E293B)
val CardDark = Color(0xFF1E293B)
val AccentBlue = Color(0xFF3B82F6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    members: MutableList<Member>,
    gymPackages: SnapshotStateList<GymPackage>,
    trainers: SnapshotStateList<Trainer>,
    totalRevenue: Double,
    adminAccount: Admin,
    onUpdateAdmin: (Admin) -> Unit,
    onUpdateRevenue: (Double) -> Unit,
    onLogout: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf("dashboard") }
    
    // --- STATE UNTUK EDIT & HAPUS ---
    var memberToEdit by remember { mutableStateOf<Member?>(null) }
    var memberToDelete by remember { mutableStateOf<Member?>(null) }
    var memberToCheckIn by remember { mutableStateOf<Member?>(null) }
    var trainerForSchedule by remember { mutableStateOf<Trainer?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }
    var snackbarHostState = remember { SnackbarHostState() }

    val scope = rememberCoroutineScope()
    val filteredMembers = members.filter { it.name.contains(searchQuery, ignoreCase = true) || it.id.contains(searchQuery) }

    // Logic Switch Screen
    when (currentScreen) {
        "packages" -> PackageManagementScreen(gymPackages, onBack = { currentScreen = "dashboard" })
        "trainers" -> TrainerManagementScreen(trainers, onBack = { currentScreen = "dashboard" })
        "reservations" -> ReservationManagementScreen(onBack = { currentScreen = "dashboard" })
        "profile" -> AdminProfileScreen(adminAccount, onUpdateAdmin, onBack = { currentScreen = "dashboard" })
        else -> {
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(DarkBgStart, DarkBgEnd)))) {
                Scaffold(
                    containerColor = Color.Transparent,
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        TopAppBar(
                            title = {
                                Column {
                                    Text("Admin Panel", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                    Text("Youth Gym Management", fontSize = 12.sp, color = Color.LightGray)
                                }
                            },
                            actions = {
                                TextButton(onClick = { currentScreen = "packages" }) { 
                                    Text("Paket", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold) 
                                }
                                TextButton(onClick = { currentScreen = "trainers" }) { 
                                    Text("Trainer", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold) 
                                }
                                TextButton(onClick = { currentScreen = "reservations" }) { 
                                    Text("Padel", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                IconButton(onClick = { currentScreen = "profile" }) {
                                    Icon(Icons.Default.Settings, "Profil", tint = Color.White)
                                }
                                IconButton(onClick = onLogout) { Icon(Icons.Default.ExitToApp, "Logout", tint = Color(0xFFF87171)) }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                        )
                    },
                    floatingActionButton = {
                        FloatingActionButton(onClick = { showAddDialog = true }, containerColor = AccentBlue, shape = CircleShape) {
                            Icon(Icons.Default.Add, "Tambah Member", tint = Color.White)
                        }
                    }
                ) { padding ->
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            // Card Ringkasan
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = AccentBlue.copy(alpha = 0.9f))
                            ) {
                                Column(modifier = Modifier.padding(24.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AccountCircle, null, tint = Color.White, modifier = Modifier.size(32.dp))
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text("Total Revenue", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                                    }
                                    Text("Rp ${formatRupiah(totalRevenue)}", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("${members.size} Total Members Aktif", color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
                                        TextButton(onClick = { showResetDialog = true }) {
                                            Icon(Icons.Default.Refresh, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Reset Pendapatan", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = {
                                            val html = generateRevenueHtml(members)
                                            exportToExcel(html)
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Laporan berhasil diunduh ke folder Downloads")
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null, tint = AccentBlue)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Unduh Laporan Excel (CSV)", color = AccentBlue, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        item {
                            // --- TAMBAHAN: KALENDER MINI ---
                            CalendarCard()
                        }

                        item {
                            // Search Bar
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Cari Member (Nama/ID)...", color = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                leadingIcon = { Icon(Icons.Default.Search, null, tint = AccentBlue) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = CardDark,
                                    unfocusedContainerColor = CardDark,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = AccentBlue,
                                    unfocusedBorderColor = Color.Transparent
                                )
                            )
                        }

                        item {
                            Text("Daftar Member", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color.White)
                        }

                        item {
                            Text("Personal Trainer Aktif", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AccentBlue, modifier = Modifier.padding(vertical = 8.dp))
                            TrainerListRow(trainers, onTrainerClick = { trainerForSchedule = it })
                        }

                        item {
                            Text("Daftar Member Gym", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AccentBlue, modifier = Modifier.padding(vertical = 8.dp))
                        }

                        items(filteredMembers) { member ->
                            MemberCard(
                                member = member,
                                onCheckIn = { memberToCheckIn = member },
                                onEdit = { memberToEdit = member },
                                onDelete = { memberToDelete = member }
                            )
                        }

                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }

    // --- DIALOG KONFIRMASI RESET PENDAPATAN ---
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Pendapatan?") },
            text = { Text("Tindakan ini akan menghapus histori pembayaran semua member di database. Member tetap ada, namun nilai pembayaran mereka akan menjadi 0. Lanjutkan?") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                members.forEach { member ->
                                    val updated = member.copy(pricePaid = 0.0)
                                    db.collection("members").document(member.id).update(mapOf("pricePaid" to 0.0))
                                    val idx = members.indexOfFirst { it.id == member.id }
                                    if (idx != -1) members[idx] = updated
                                }
                                onUpdateRevenue(0.0)
                                showResetDialog = false
                                snackbarHostState.showSnackbar("Pendapatan berhasil direset ke 0")
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("Gagal reset: ${e.message}")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF87171))
                ) { Text("Ya, Reset", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Batal", color = Color.Gray) }
            }
        )
    }

    // --- DIALOG KONFIRMASI CHECK-IN ---
    if (memberToCheckIn != null) {
        AlertDialog(
            onDismissRequest = { memberToCheckIn = null },
            title = { Text("Konfirmasi Check-in") },
            text = { Text("Apakah Anda yakin ingin melakukan check-in untuk ${memberToCheckIn?.name}? Sisa hari akan berkurang 1.") },
            confirmButton = {
                Button(
                    onClick = {
                        val member = memberToCheckIn!!
                        scope.launch {
                            if (member.remainingDays > 0) {
                                val updated = member.copy(remainingDays = member.remainingDays - 1)
                                val idx = members.indexOfFirst { it.id == member.id }
                                if (idx != -1) members[idx] = updated
                                try {
                                    val data = mapOf("remainingDays" to updated.remainingDays)
                                    db.collection("members").document(updated.id).update(data)
                                    snackbarHostState.showSnackbar("Check-in berhasil untuk ${member.name}")
                                } catch (e: Exception) { 
                                    println("DEBUG: Update Cloud gagal: ${e.message}") 
                                }
                            } else {
                                snackbarHostState.showSnackbar("Gagal: Sisa hari sudah habis!")
                            }
                            memberToCheckIn = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) { Text("Ya, Check-in", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { memberToCheckIn = null }) { Text("Batal", color = Color.Gray) }
            }
        )
    }

    // --- DIALOG TAMBAH MEMBER ---
    if (showAddDialog) {
        AddMemberDialog(
            packages = gymPackages,
            onDismiss = { showAddDialog = false },
            onConfirm = { n, u, p, d, price, joinDate, pkgName ->
                scope.launch {
                    try {
                        val expirationTime = joinDate + (d * 86400000L) 
                        val randomId = "CH-" + Random.nextInt(1000, 9999).toString()
                        val newMember = Member(randomId, n, u, p, d, joinDate, expirationTime, 0.0, 0.0, price, pkgName)

                        // Update Lokal
                        members.add(newMember)
                        onUpdateRevenue(totalRevenue + price)
                        showAddDialog = false
                        
                        // Update Cloud - Pakai Map agar aman di Desktop
                        val data = mapOf(
                            "id" to randomId,
                            "name" to n,
                            "username" to u,
                            "password" to p,
                            "remainingDays" to d,
                            "joinDate" to joinDate,
                            "expiredDate" to expirationTime,
                            "weight" to 0.0,
                            "height" to 0.0,
                            "pricePaid" to price,
                            "packageName" to pkgName
                        )
                        db.collection("members").document(randomId).set(data)
                        println("DEBUG: Member baru berhasil disimpan ke Cloud")
                    } catch (e: Exception) { 
                        println("WARNING: Member hanya tersimpan lokal karena error Cloud: ${e.message}") 
                    }
                }
            }
        )
    }

    // --- DIALOG UPDATE MEMBER ---
    if (memberToEdit != null) {
        EditMemberDialog(
            member = memberToEdit!!,
            onDismiss = { memberToEdit = null },
            onConfirm = { updated ->
                scope.launch {
                    try {
                        // Update Lokal
                        val idx = members.indexOfFirst { it.id == updated.id }
                        if (idx != -1) members[idx] = updated
                        memberToEdit = null
                        
                        // Update Cloud - Pakai Map
                        val data = mapOf(
                            "id" to updated.id,
                            "name" to updated.name,
                            "username" to updated.username,
                            "password" to updated.password,
                            "remainingDays" to updated.remainingDays,
                            "joinDate" to updated.joinDate,
                            "expiredDate" to updated.expiredDate,
                            "weight" to updated.weight,
                            "height" to updated.height
                        )
                        db.collection("members").document(updated.id).set(data)
                    } catch (e: Exception) { println("WARNING: Gagal update ke Cloud: ${e.message}") }
                }
            }
        )
    }

    // --- DIALOG DELETE MEMBER ---
    if (memberToDelete != null) {
        AlertDialog(
            onDismissRequest = { memberToDelete = null },
            title = { Text("Hapus Member") },
            text = { Text("Yakin ingin menghapus ${memberToDelete?.name} dari Database?") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        try {
                            val idToRemove = memberToDelete?.id!!
                            // Update Lokal
                            members.removeAll { it.id == idToRemove }
                            memberToDelete = null
                            
                            // Update Cloud
                            db.collection("members").document(idToRemove).delete()
                        } catch (e: Exception) { println("WARNING: Gagal hapus dari Cloud: ${e.message}") }
                    }
                }) { Text("Hapus", color = Color.Red, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { memberToDelete = null }) { Text("Batal", color = Color.Gray) }
            }
        )
    }

    // --- DIALOG TAMBAH JADWAL TRAINER ---
    if (trainerForSchedule != null) {
        var dayText by remember { mutableStateOf("") }
        var timeText by remember { mutableStateOf("") }
        var selectedMemberName by remember { mutableStateOf("Pilih Member") }
        var memberDropdownExpanded by remember { mutableStateOf(false) }
        var editingIndex by remember { mutableStateOf<Int?>(null) }
        
        AlertDialog(
            onDismissRequest = { trainerForSchedule = null },
            title = { Text("Kelola Jadwal: Coach ${trainerForSchedule?.name}") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Daftar Jadwal:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black) // Judul hitam
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Box(modifier = Modifier.heightIn(max = 200.dp)) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            val currentSchedules = trainerForSchedule?.schedules ?: emptyList()
                            itemsIndexed(currentSchedules) { index, schedule ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White), // Latar putih
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(schedule, fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f)) // Text hitam
                                        Row {
                                            IconButton(onClick = { 
                                                // Parser sederhana untuk edit
                                                try {
                                                    val parts = schedule.split(", ", " - ")
                                                    if(parts.size >= 3) {
                                                        dayText = parts[0]
                                                        timeText = parts[1]
                                                        selectedMemberName = parts[2].replace("Melatih ", "")
                                                    } else {
                                                        dayText = schedule
                                                    }
                                                } catch(e: Exception) {
                                                    dayText = schedule
                                                }
                                                editingIndex = index
                                            }, modifier = Modifier.size(24.dp)) {
                                                Icon(Icons.Default.Edit, null, tint = AccentBlue, modifier = Modifier.size(16.dp))
                                            }
                                            IconButton(onClick = {
                                                val trainer = trainerForSchedule!!
                                                val newList = trainer.schedules.toMutableList()
                                                newList.removeAt(index)
                                                val updated = trainer.copy(schedules = newList)
                                                scope.launch {
                                                    db.collection("trainers").document(trainer.id).update(mapOf("schedules" to newList))
                                                    val idx = trainers.indexOfFirst { it.id == trainer.id }
                                                    if (idx != -1) trainers[idx] = updated
                                                    trainerForSchedule = updated
                                                }
                                            }, modifier = Modifier.size(24.dp)) {
                                                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                            }
                                            IconButton(onClick = {
                                                val trainer = trainerForSchedule!!
                                                val newList = trainer.schedules.toMutableList()
                                                newList.removeAt(index)
                                                val updated = trainer.copy(schedules = newList)
                                                scope.launch {
                                                    db.collection("trainers").document(trainer.id).update(mapOf("schedules" to newList))
                                                    val idx = trainers.indexOfFirst { it.id == trainer.id }
                                                    if (idx != -1) trainers[idx] = updated
                                                    trainerForSchedule = updated
                                                }
                                            }, modifier = Modifier.size(24.dp)) {
                                                Icon(Icons.Default.Delete, null, tint = Color(0xFFF87171), modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color.Gray.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Form Jadwal:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AccentBlue)
                    
                    OutlinedTextField(
                        value = dayText, onValueChange = { dayText = it },
                        label = { Text("Hari (Cth: Senin)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black, 
                            unfocusedTextColor = Color.Black,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = timeText, onValueChange = { timeText = it },
                        label = { Text("Jam (Cth: 10:00 WIB)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black, 
                            unfocusedTextColor = Color.Black,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Dropdown Pilih Member
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedMemberName, onValueChange = {}, 
                            label = { Text("Pilih Member") }, 
                            readOnly = true, 
                            shape = RoundedCornerShape(12.dp), 
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = { IconButton(onClick = { memberDropdownExpanded = true }) { Icon(Icons.Default.ArrowDropDown, null) } },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black, 
                                unfocusedTextColor = Color.Black,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                        DropdownMenu(expanded = memberDropdownExpanded, onDismissRequest = { memberDropdownExpanded = false }) {
                            members.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text(m.name) },
                                    onClick = {
                                        selectedMemberName = m.name
                                        memberDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (dayText.isNotBlank() && timeText.isNotBlank() && selectedMemberName != "Pilih Member") {
                            val trainer = trainerForSchedule!!
                            val newList = trainer.schedules.toMutableList()
                            val formattedSchedule = "$dayText, $timeText - Melatih $selectedMemberName"
                            
                            if (editingIndex != null) {
                                newList[editingIndex!!] = formattedSchedule
                            } else {
                                newList.add(formattedSchedule)
                            }
                            
                            val updated = trainer.copy(schedules = newList)
                            scope.launch {
                                try {
                                    db.collection("trainers").document(trainer.id).update(mapOf("schedules" to newList))
                                    val idx = trainers.indexOfFirst { it.id == trainer.id }
                                    if (idx != -1) trainers[idx] = updated
                                    dayText = ""; timeText = ""; selectedMemberName = "Pilih Member"
                                    editingIndex = null
                                    trainerForSchedule = updated
                                    snackbarHostState.showSnackbar("Berhasil!")
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("Gagal: ${e.message}")
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) { Text(if (editingIndex != null) "Update" else "Tambah", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { trainerForSchedule = null }) { Text("Tutup", color = Color.Gray) }
            }
        )
    }
}

// --- PERBARUAN: MEMBER CARD MENAMPILKAN TANGGAL DAFTAR DAN EXPIRED ---
@Composable
fun MemberCard(member: Member, onCheckIn: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    // Memformat tanggal jadi teks (Contoh: 23 Agustus 2026)
    val joinDateString = com.chiko0085.testgym.formatEpochToDate(member.joinDate)
    val expiredDateString = com.chiko0085.testgym.formatEpochToDate(member.expiredDate)
    val isActive = member.remainingDays > 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(45.dp).clip(CircleShape).background(AccentBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(member.name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(member.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)

                // --- TANGGAL MUNCUL DI SINI ---
                Text("Daftar: $joinDateString", fontSize = 11.sp, color = Color.Gray)
                Text(
                    text = "Expired: $expiredDateString (${member.remainingDays} Hari)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isActive) Color.LightGray else Color(0xFFF87171)
                )
                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    color = if (isActive) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if (isActive) "AKTIF" else "EXPIRED",
                        color = if (isActive) Color(0xFF34D399) else Color(0xFFF87171),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Button(
                onClick = onCheckIn,
                modifier = Modifier.padding(start = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981).copy(alpha = 0.1f)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Check-in Manual", color = Color(0xFF34D399), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.LightGray) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFF87171)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMemberDialog(packages: List<GymPackage>, onDismiss: () -> Unit, onConfirm: (String, String, String, Int, Double, Long, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var days by remember { mutableStateOf("") }
    var selectedPrice by remember { mutableDoubleStateOf(0.0) }
    var expanded by remember { mutableStateOf(false) }
    var selectedPackageName by remember { mutableStateOf("Pilih Paket (Opsional)") }
    
    // Default join date adalah hari ini (WIB)
    val now = com.chiko0085.testgym.getCurrentTimeMillis()
    val fullDate = com.chiko0085.testgym.formatEpochToDate(now) // "dd MMMM yyyy HH:mm"
    var joinDateStr by remember { mutableStateOf(fullDate.split(" ").take(3).joinToString(" ")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Member Baru", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Lengkap") }, leadingIcon = { Icon(Icons.Default.Person, null) }, shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = user, onValueChange = { user = it }, label = { Text("Username") }, shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Password") }, leadingIcon = { Icon(Icons.Default.Lock, null) }, shape = RoundedCornerShape(12.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedPackageName, onValueChange = {}, label = { Text("Pilih Paket") }, readOnly = true, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { IconButton(onClick = { expanded = true }) { Icon(Icons.Default.ArrowDropDown, null) } }
                    )
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        packages.forEach { pkg ->
                            DropdownMenuItem(
                                text = { Text("${pkg.name} - Rp ${formatRupiah(pkg.price ?: 0.0)}") },
                                onClick = {
                                    selectedPackageName = pkg.name
                                    days = pkg.durationDays.toString()
                                    selectedPrice = pkg.price ?: 0.0
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(value = days, onValueChange = { days = it }, label = { Text("Masa Aktif (Hari)") }, shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = joinDateStr, onValueChange = { joinDateStr = it }, label = { Text("Tgl Daftar (Cth: 10 Juni 2026)") }, shape = RoundedCornerShape(12.dp), placeholder = { Text("10 Juni 2026") })
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    val millis = parseDateToMillis(joinDateStr) ?: com.chiko0085.testgym.getCurrentTimeMillis()
                    onConfirm(name, user, pass, days.toIntOrNull() ?: 0, selectedPrice, millis, selectedPackageName) 
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) { Text("Simpan Member", fontWeight = FontWeight.Bold, color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal", color = Color.Gray) } }
    )
}

@Composable
fun EditMemberDialog(member: Member, onDismiss: () -> Unit, onConfirm: (Member) -> Unit) {
    var name by remember { mutableStateOf(member.name) }
    var user by remember { mutableStateOf(member.username) }
    var pass by remember { mutableStateOf(member.password) }
    var days by remember { mutableStateOf(member.remainingDays.toString()) }
    val fullDate = com.chiko0085.testgym.formatEpochToDate(member.joinDate)
    var joinDateStr by remember { mutableStateOf(fullDate.split(" ").take(3).joinToString(" ")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Data Member", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama") }, shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = user, onValueChange = { user = it }, label = { Text("Username") }, shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Password") }, shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = days, onValueChange = { days = it }, label = { Text("Sisa Hari") }, shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = joinDateStr, onValueChange = { joinDateStr = it }, label = { Text("Tgl Daftar (Cth: 10 Juni 2026)") }, shape = RoundedCornerShape(12.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    val newJoin = parseDateToMillis(joinDateStr) ?: member.joinDate
                    val d = days.toIntOrNull() ?: 0
                    val newExp = newJoin + (d * 86400000L)
                    onConfirm(member.copy(name=name, username=user, password=pass, remainingDays=d, joinDate=newJoin, expiredDate=newExp)) 
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) { Text("Update", color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal", color = Color.Gray) } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackageManagementScreen(packages: SnapshotStateList<GymPackage>, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var packageToEdit by remember { mutableStateOf<GymPackage?>(null) }
    var showAddPackage by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(DarkBgStart, DarkBgEnd)))) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Manajemen Paket Harga", color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    navigationIcon = {
                        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { showAddPackage = true }, containerColor = AccentBlue) {
                    Icon(Icons.Default.Add, null, tint = Color.White)
                }
            }
        ) { padding ->
            LazyColumn(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(packages) { pkg ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardDark)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(pkg.name, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color.White)
                                Text("Harga: Rp ${formatRupiah(pkg.price ?: 0.0)}", color = AccentBlue, fontWeight = FontWeight.Bold)
                                Text("Durasi: ${pkg.durationDays} Hari", fontSize = 14.sp, color = Color.LightGray)
                            }
                            IconButton(onClick = { packageToEdit = pkg }) { Icon(Icons.Default.Edit, null, tint = Color.LightGray) }
                            IconButton(onClick = {
                                scope.launch {
                                    try {
                                        val idToRemove = pkg.id
                                        // Hapus Lokal
                                        packages.remove(pkg)
                                        
                                        // Hapus Cloud
                                        db.collection("gym_packages").document(idToRemove).delete()
                                        println("DEBUG: Berhasil hapus paket ${pkg.name} dari cloud")
                                    } catch (e: Exception) {
                                        println("ERROR: Gagal hapus paket dari cloud: ${e.message}")
                                        e.printStackTrace()
                                    }
                                }
                            }) { Icon(Icons.Default.Delete, null, tint = Color(0xFFF87171)) }
                        }
                    }
                }
            }
        }
    }

    if (showAddPackage) {
        PackageDialog(
            title = "Tambah Paket",
            onDismiss = { showAddPackage = false },
            onConfirm = { n, p, d ->
                scope.launch {
                    try {
                        val randomId = Random.nextInt(100000, 999999).toString()
                        val newPkg = GymPackage(randomId, n, p, d)
                        
                        // Tambah ke list lokal dulu agar UI update
                        packages.add(newPkg)
                        showAddPackage = false

                        // Coba simpan ke Cloud
                        val data = mapOf(
                            "id" to randomId,
                            "name" to n,
                            "price" to p,
                            "durationDays" to d
                        )
                        db.collection("gym_packages").document(randomId).set(data)
                        println("DEBUG: Berhasil simpan paket ke Cloud")
                    } catch (e: Exception) {
                        println("WARNING: Gagal simpan ke Cloud, tapi list lokal terupdate: ${e.message}")
                    }
                }
            }
        )
    }

    if (packageToEdit != null) {
        PackageDialog(
            title = "Edit Paket",
            initialPackage = packageToEdit,
            onDismiss = { packageToEdit = null },
            onConfirm = { n, p, d ->
                scope.launch {
                    try {
                        val updatedPkg = packageToEdit!!.copy(name = n, price = p, durationDays = d)
                        
                        // Update lokal
                        val idx = packages.indexOfFirst { it.id == updatedPkg.id }
                        if (idx != -1) packages[idx] = updatedPkg
                        packageToEdit = null

                        // Update Cloud
                        val data = mapOf(
                            "id" to updatedPkg.id,
                            "name" to n,
                            "price" to p,
                            "durationDays" to d
                        )
                        db.collection("gym_packages").document(updatedPkg.id).set(data)
                    } catch (e: Exception) {
                        println("WARNING: Gagal update ke Cloud: ${e.message}")
                    }
                }
            }
        )
    }
}

@Composable
fun PackageDialog(title: String, initialPackage: GymPackage? = null, onDismiss: () -> Unit, onConfirm: (String, Double, Int) -> Unit) {
    var name by remember { mutableStateOf(initialPackage?.name ?: "") }
    var price by remember { mutableStateOf(initialPackage?.price?.toLong()?.toString() ?: "") }
    var days by remember { mutableStateOf(initialPackage?.durationDays?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name, 
                    onValueChange = { name = it }, 
                    label = { Text("Nama Paket") }, 
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = price, 
                    onValueChange = { price = it }, 
                    label = { Text("Harga (Angka saja)") }, 
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = days, 
                    onValueChange = { days = it }, 
                    label = { Text("Durasi (Hari)") }, 
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = price.filter { it.isDigit() }.toDoubleOrNull() ?: 0.0
                    val d = days.filter { it.isDigit() }.toIntOrNull() ?: 0
                    if (name.isNotBlank()) {
                        onConfirm(name, p, d)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) { Text("Simpan", color = Color.White) }
        },
        dismissButton = { 
            TextButton(onClick = onDismiss) { Text("Batal", color = Color.Gray) } 
        }
    )
}

// --- FUNGSI MANAJEMEN TRAINER ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainerManagementScreen(trainers: SnapshotStateList<Trainer>, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var showAddTrainer by remember { mutableStateOf(false) }
    var trainerToEdit by remember { mutableStateOf<Trainer?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(DarkBgStart, DarkBgEnd)))) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Manajemen Personal Trainer", color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) } }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { showAddTrainer = true }, containerColor = AccentBlue) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Trainer", tint = Color.White)
                }
            }
        ) { padding ->
            LazyColumn(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(trainers) { trainer ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardDark)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(trainer.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                                Text("Username: ${trainer.username}", color = AccentBlue, fontSize = 12.sp)
                                Text("Exp: ${trainer.experience}", color = Color.Gray, fontSize = 12.sp)
                                Text(trainer.specialization, color = Color.LightGray, fontSize = 14.sp)
                            }
                            IconButton(onClick = { trainerToEdit = trainer }) { Icon(Icons.Default.Edit, null, tint = Color.LightGray) }
                            IconButton(onClick = {
                                scope.launch {
                                    try {
                                        val idToRemove = trainer.id
                                        trainers.remove(trainer)
                                        db.collection("trainers").document(idToRemove).delete()
                                    } catch (e: Exception) {}
                                }
                            }) { Icon(Icons.Default.Delete, null, tint = Color(0xFFF87171)) }
                        }
                    }
                }
            }
        }
    }

    if (showAddTrainer) {
        TrainerDialog(onDismiss = { showAddTrainer = false }, onConfirm = { n, u, p ->
            scope.launch {
                try {
                    val newId = "PT-" + Random.nextInt(100, 999).toString()
                    val s = "Personal Trainer" // Default specialization
                    val e = "1 Tahun" // Default experience
                    val r = 5.0 // Default rate
                    val d = "Trainer di Youth Gym" // Default description
                    val newTrainer = Trainer(newId, n, u, p, s, e, r, d)
                    
                    // Lokal
                    trainers.add(newTrainer)
                    showAddTrainer = false
                    
                    // Cloud
                    val data = mapOf(
                        "id" to newId,
                        "name" to n,
                        "username" to u,
                        "password" to p,
                        "specialization" to s,
                        "experience" to e,
                        "rate" to r,
                        "description" to d
                    )
                    db.collection("trainers").document(newId).set(data)
                } catch (ex: Exception) {}
            }
        })
    }

    if (trainerToEdit != null) {
        EditTrainerDialog(
            trainer = trainerToEdit!!,
            onDismiss = { trainerToEdit = null },
            onConfirm = { updated ->
                scope.launch {
                    try {
                        // Update Lokal
                        val idx = trainers.indexOfFirst { it.id == updated.id }
                        if (idx != -1) trainers[idx] = updated
                        trainerToEdit = null
                        
                        // Update Cloud
                        val data = mapOf(
                            "id" to updated.id,
                            "name" to updated.name,
                            "username" to updated.username,
                            "password" to updated.password,
                            "specialization" to updated.specialization,
                            "experience" to updated.experience,
                            "rate" to updated.rating,
                            "description" to updated.description
                        )
                        db.collection("trainers").document(updated.id).set(data)
                    } catch (e: Exception) {}
                }
            }
        )
    }
}

@Composable
fun EditTrainerDialog(trainer: Trainer, onDismiss: () -> Unit, onConfirm: (Trainer) -> Unit) {
    var name by remember { mutableStateOf(trainer.name) }
    var user by remember { mutableStateOf(trainer.username) }
    var pass by remember { mutableStateOf(trainer.password) }
    var exp by remember { mutableStateOf(trainer.experience) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Data Trainer", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = user, onValueChange = { user = it }, label = { Text("Username") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Password") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = exp, onValueChange = { exp = it }, label = { Text("Pengalaman (Cth: 2 Tahun)") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    onConfirm(trainer.copy(name = name, username = user, password = pass, experience = exp)) 
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) { Text("Update", color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal", color = Color.Gray) } }
    )
}


@Composable
fun TrainerDialog(onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Akun Trainer", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Lengkap") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = user, onValueChange = { user = it }, label = { Text("Username") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Password") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank() && user.isNotBlank()) onConfirm(name, user, pass) }, 
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                shape = RoundedCornerShape(8.dp)
            ) { Text("Simpan", color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal", color = Color.Gray) } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProfileScreen(admin: Admin, onUpdate: (Admin) -> Unit, onBack: () -> Unit) {
    var username by remember { mutableStateOf(admin.username) }
    var password by remember { mutableStateOf(admin.password) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(DarkBgStart, DarkBgEnd)))) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text("Pengaturan Profil Admin", color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) } }
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Ubah Kredensial Login", color = AccentBlue, fontWeight = FontWeight.Bold)
                
                OutlinedTextField(
                    value = username, onValueChange = { username = it },
                    label = { Text("Username Baru") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = AccentBlue, unfocusedBorderColor = Color.Gray)
                )

                OutlinedTextField(
                    value = password, onValueChange = { password = it },
                    label = { Text("Password Baru") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = AccentBlue, unfocusedBorderColor = Color.Gray)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val updated = Admin(username, password)
                                db.collection("settings").document("admin_account").set(updated)
                                onUpdate(updated)
                                snackbarHostState.showSnackbar("Profil Admin berhasil diperbarui!")
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("Gagal memperbarui: ${e.message}")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Text("Simpan Perubahan", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

fun formatRupiah(amount: Double): String {
    val str = amount.toLong().toString()
    var result = ""
    var count = 0
    for (i in str.length - 1 downTo 0) {
        result = str[i] + result
        count++
        if (count == 3 && i > 0) {
            result = "." + result
            count = 0
        }
    }
    return result
}

@Composable
fun TrainerListRow(trainers: SnapshotStateList<Trainer>, onTrainerClick: (Trainer) -> Unit) {
    if (trainers.isEmpty()) {
        Text("Belum ada trainer terdaftar", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp))
    } else {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(trainers) { trainer ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    modifier = Modifier.width(140.dp),
                    onClick = { onTrainerClick(trainer) }
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(shape = CircleShape, modifier = Modifier.size(40.dp), color = AccentBlue.copy(alpha = 0.2f)) {
                            Icon(Icons.Default.Person, null, modifier = Modifier.padding(8.dp), tint = AccentBlue)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(trainer.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White, maxLines = 1)
                        Text("Trainer", fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarCard() {
    val now = com.chiko0085.testgym.getCurrentTimeMillis()
    val dateStr = com.chiko0085.testgym.formatEpochToDate(now) // "dd MMMM yyyy HH:mm"
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
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tampilan Ikon Kalender Bergaya
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AccentBlue.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(month.take(3).uppercase(), color = AccentBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(day, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text("Hari Ini", color = Color.Gray, fontSize = 12.sp)
                Text("$day $month $year", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Waktu Indonesia Barat (WIB)", color = AccentBlue.copy(alpha = 0.8f), fontSize = 11.sp)
            }
        }
    }
}

fun generateRevenueHtml(members: List<Member>): String {
    val sortedMembers = members.sortedBy { it.joinDate }
    var cumulativeRevenue = 0.0
    
    val htmlBuilder = StringBuilder()
    htmlBuilder.append("""
        <html xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:x="urn:schemas-microsoft-com:office:excel" xmlns="http://www.w3.org/TR/REC-html40">
        <head>
            <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
            <style>
                table { border-collapse: collapse; width: 100%; font-family: sans-serif; }
                th { background-color: #3B82F6; color: white; border: 1px solid #ddd; padding: 12px; text-align: left; }
                td { border: 1px solid #ddd; padding: 10px; }
                tr:nth-child(even) { background-color: #f2f2f2; }
                .total-row { background-color: #1E293B; color: white; font-weight: bold; }
                .header-title { font-size: 24px; color: #3B82F6; font-weight: bold; margin-bottom: 20px; text-align: center; }
            </style>
        </head>
        <body>
            <div class="header-title">LAPORAN PENDAPATAN YOUTH GYM</div>
            <table>
                <thead>
                    <tr>
                        <th>No</th>
                        <th>ID Member</th>
                        <th>Nama Member</th>
                        <th>Paket</th>
                        <th>Tanggal Transaksi</th>
                        <th>Pendapatan</th>
                        <th>Total Akumulasi</th>
                    </tr>
                </thead>
                <tbody>
    """.trimIndent())

    sortedMembers.forEachIndexed { index, m ->
        cumulativeRevenue += (m.pricePaid ?: 0.0)
        val dateStr = com.chiko0085.testgym.formatEpochToDate(m.joinDate).split(" ").take(3).joinToString(" ")
        
        htmlBuilder.append("""
            <tr>
                <td>${index + 1}</td>
                <td>${m.id}</td>
                <td>${m.name}</td>
                <td>${m.packageName}</td>
                <td>$dateStr</td>
                <td>Rp ${formatRupiah(m.pricePaid ?: 0.0)}</td>
                <td>Rp ${formatRupiah(cumulativeRevenue)}</td>
            </tr>
        """.trimIndent())
    }

    htmlBuilder.append("""
                </tbody>
                <tfoot>
                    <tr class="total-row">
                        <td colspan="6" style="text-align: right;">TOTAL PENDAPATAN AKHIR</td>
                        <td>Rp ${formatRupiah(cumulativeRevenue)}</td>
                    </tr>
                </tfoot>
            </table>
        </body>
        </html>
    """.trimIndent())
    
    return htmlBuilder.toString()
}

fun parseDateToMillis(dateStr: String): Long? {
    return com.chiko0085.testgym.parseDateToMillis(dateStr)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationManagementScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
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

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(DarkBgStart, DarkBgEnd)))) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Manajemen Jadwal Padel", color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    navigationIcon = {
                        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                    }
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Tanggal: ${formatEpochToDate(selectedDateMillis).split(" ").take(3).joinToString(" ")}", color = Color.White, fontWeight = FontWeight.Bold)
                    Row {
                        IconButton(onClick = { selectedDateMillis -= 86400000L }) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
                        IconButton(onClick = { selectedDateMillis += 86400000L }) { Icon(Icons.Default.ArrowForward, null, tint = Color.White) }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentBlue)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                        items(reservations) { res ->
                            ReservationSlotCard(res, onToggleStatus = { newStatus ->
                                scope.launch {
                                    try {
                                        val updated = res.copy(status = newStatus, reservedByName = if(newStatus == "Booked") "Admin" else "")
                                        db.collection("reservations").document(updated.id).set(updated)
                                        reservations = reservations.map { if (it.id == res.id) updated else it }
                                    } catch (e: Exception) {}
                                }
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReservationSlotCard(reservation: Reservation, onToggleStatus: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                val hourStr = if (reservation.hour < 10) "0${reservation.hour}:00" else "${reservation.hour}:00"
                val nextHour = if (reservation.hour + 1 < 10) "0${reservation.hour + 1}:00" else "${reservation.hour + 1}:00"
                Text("$hourStr - $nextHour", color = Color.White, fontWeight = FontWeight.Bold)
                Text(if (reservation.status == "Empty") "Kosong" else "Terisi (${reservation.reservedByName})", 
                    color = if (reservation.status == "Empty") Color(0xFF34D399) else Color(0xFFF87171),
                    fontSize = 12.sp)
            }
            
            Row {
                Button(
                    onClick = { onToggleStatus("Empty") },
                    colors = ButtonDefaults.buttonColors(containerColor = if (reservation.status == "Empty") Color(0xFF10B981) else Color.Gray.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Kosong", fontSize = 10.sp, color = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { onToggleStatus("Booked") },
                    colors = ButtonDefaults.buttonColors(containerColor = if (reservation.status == "Booked") Color(0xFFEF4444) else Color.Gray.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Terisi", fontSize = 10.sp, color = Color.White)
                }
            }
        }
    }
}
