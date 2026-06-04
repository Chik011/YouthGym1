package com.chiko0085.testgym.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.chiko0085.testgym.formatEpochToDate
import com.chiko0085.testgym.getCurrentTimeMillis
import com.chiko0085.testgym.model.GymPackage
import com.chiko0085.testgym.model.Member
import com.chiko0085.testgym.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.random.Random
import com.chiko0085.testgym.Trainer

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
    totalRevenue: Double,
    onUpdateRevenue: (Double) -> Unit,
    onLogout: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf("dashboard") }
    
    // --- STATE UNTUK EDIT & HAPUS ---
    var memberToEdit by remember { mutableStateOf<Member?>(null) }
    var memberToDelete by remember { mutableStateOf<Member?>(null) }

    val scope = rememberCoroutineScope()
    val filteredMembers = members.filter { it.name.contains(searchQuery, ignoreCase = true) || it.id.contains(searchQuery) }

    // Logic Switch Screen
    when (currentScreen) {
        "packages" -> PackageManagementScreen(gymPackages, onBack = { currentScreen = "dashboard" })
        "trainers" -> TrainerManagementScreen(onBack = { currentScreen = "dashboard" })
        else -> {
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(DarkBgStart, DarkBgEnd)))) {
                Scaffold(
                    containerColor = Color.Transparent,
                    topBar = {
                        TopAppBar(
                            title = {
                                Column {
                                    Text("Admin Panel", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                    Text("Youth Gym Management", fontSize = 12.sp, color = Color.LightGray)
                                }
                            },
                            actions = {
                                IconButton(onClick = { currentScreen = "packages" }) { Icon(Icons.AutoMirrored.Filled.List, "Paket", tint = Color.White) }
                                IconButton(onClick = { currentScreen = "trainers" }) { Icon(Icons.Default.Person, "Trainer", tint = Color.White) }
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
                    Column(modifier = Modifier.padding(padding).padding(16.dp)) {
                        // Card Ringkasan
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
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
                                Text("${members.size} Total Members Aktif", color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
                            }
                        }

                        // Search Bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Cari Member (Nama/ID)...", color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
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

                        Text("Daftar Member", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color.White, modifier = Modifier.padding(bottom = 12.dp))

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                            items(filteredMembers) { member ->
                                MemberCard(
                                    member = member,
                                    onCheckIn = {
                                        scope.launch {
                                            if (member.remainingDays > 0) {
                                                val updated = member.copy(remainingDays = member.remainingDays - 1)
                                                val idx = members.indexOfFirst { it.id == member.id }
                                                if (idx != -1) members[idx] = updated
                                                // Simpan perubahan ke Cloud
                                                try {
                                                    val data = mapOf("remainingDays" to updated.remainingDays)
                                                    db.collection("members").document(updated.id).update(data)
                                                } catch (e: Exception) { println("DEBUG: Update Cloud gagal: ${e.message}") }
                                            }
                                        }
                                    },
                                    onEdit = { memberToEdit = member },
                                    onDelete = { memberToDelete = member }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // --- DIALOG TAMBAH MEMBER ---
    if (showAddDialog) {
        AddMemberDialog(
            packages = gymPackages,
            onDismiss = { showAddDialog = false },
            onConfirm = { n, u, p, d, price ->
                scope.launch {
                    try {
                        val currentTime = com.chiko0085.testgym.getCurrentTimeMillis()
                        val expirationTime = currentTime + (d * 86400000L) 
                        val randomId = "CH-" + Random.nextInt(1000, 9999).toString()
                        val newMember = Member(randomId, n, u, p, d, currentTime, expirationTime, 0.0, 0.0)

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
                            "joinDate" to currentTime,
                            "expiredDate" to expirationTime,
                            "weight" to 0.0,
                            "height" to 0.0
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
            IconButton(onClick = onCheckIn) { Icon(Icons.Default.CheckCircle, contentDescription = "Check-in", tint = Color(0xFF34D399)) }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.LightGray) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFF87171)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMemberDialog(packages: List<GymPackage>, onDismiss: () -> Unit, onConfirm: (String, String, String, Int, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var days by remember { mutableStateOf("") }
    var selectedPrice by remember { mutableDoubleStateOf(0.0) }
    var expanded by remember { mutableStateOf(false) }
    var selectedPackageName by remember { mutableStateOf("Pilih Paket (Opsional)") }

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
                                text = { Text("${pkg.name} - Rp ${formatRupiah(pkg.price)}") },
                                onClick = {
                                    selectedPackageName = pkg.name
                                    days = pkg.durationDays.toString()
                                    selectedPrice = pkg.price
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(value = days, onValueChange = { days = it }, label = { Text("Masa Aktif (Hari)") }, shape = RoundedCornerShape(12.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, user, pass, days.toIntOrNull() ?: 0, selectedPrice) },
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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Data Member", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama") }, shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = user, onValueChange = { user = it }, label = { Text("Username") }, shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Password") }, shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = days, onValueChange = { days = it }, label = { Text("Sisa Hari") }, shape = RoundedCornerShape(12.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(member.copy(name=name, username=user, password=pass, remainingDays=days.toIntOrNull()?:0)) },
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
                                Text("Harga: Rp ${formatRupiah(pkg.price)}", color = AccentBlue, fontWeight = FontWeight.Bold)
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
fun TrainerManagementScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val trainers = remember { mutableStateListOf<Trainer>() }
    var showAddTrainer by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val dbTrainers = db.collection("trainers").get().documents.map { it.data<Trainer>() }
            trainers.clear()
            trainers.addAll(dbTrainers)
        } catch (e: Exception) {}
    }

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
                                Text(trainer.specialization, color = Color.LightGray, fontSize = 14.sp)
                            }
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
        TrainerDialog(onDismiss = { showAddTrainer = false }, onConfirm = { n, u, p, s, e, r, d ->
            scope.launch {
                try {
                    val newId = "PT-" + Random.nextInt(100, 999).toString()
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
}

@Composable
fun TrainerDialog(onDismiss: () -> Unit, onConfirm: (String, String, String, String, String, Double, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var spec by remember { mutableStateOf("") }
    var exp by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Akun Trainer", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Lengkap") })
                OutlinedTextField(value = user, onValueChange = { user = it }, label = { Text("Username") })
                OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Password") })
                OutlinedTextField(value = spec, onValueChange = { spec = it }, label = { Text("Spesialisasi (Cth: Cardio)") })
                OutlinedTextField(value = exp, onValueChange = { exp = it }, label = { Text("Pengalaman (Cth: 5 Tahun)") })
                OutlinedTextField(value = rate, onValueChange = { rate = it }, label = { Text("Rating (Cth: 4.8)") })
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Deskripsi Singkat") })
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, user, pass, spec, exp, rate.toDoubleOrNull() ?: 5.0, desc) }, colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)) { Text("Simpan", color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal", color = Color.Gray) } }
    )
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