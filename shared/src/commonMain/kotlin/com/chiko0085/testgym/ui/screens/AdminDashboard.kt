package com.chiko0085.testgym.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chiko0085.testgym.exportToExcel
import com.chiko0085.testgym.model.GymPackage
import com.chiko0085.testgym.model.Member
import com.chiko0085.testgym.Trainer
import com.chiko0085.testgym.supabase
import com.chiko0085.testgym.ui.theme.AccentBlue
import com.chiko0085.testgym.ui.theme.AccentBlueDark
import com.chiko0085.testgym.ui.theme.CardDark
import com.chiko0085.testgym.ui.theme.DarkBgEnd
import com.chiko0085.testgym.ui.theme.DarkBgStart
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlin.random.Random

fun formatRupiah(amount: Double): String {
    val str = amount.toLong().toString()
    return str.reversed().chunked(3).joinToString(".").reversed()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    members: MutableList<Member>,
    gymPackages: MutableList<GymPackage>,
    totalRevenue: Double,
    onUpdateRevenue: (Double) -> Unit,
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var showAddDialog by remember { mutableStateOf(false) }
    var memberToEdit by remember { mutableStateOf<Member?>(null) }
    var memberToDelete by remember { mutableStateOf<Member?>(null) }
    var memberToCheckIn by remember { mutableStateOf<Member?>(null) }
    var showExportSuccess by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    var showPackageList by remember { mutableStateOf(false) }
    var showTrainerList by remember { mutableStateOf(false) }

    val bgGradient = Brush.verticalGradient(listOf(DarkBgStart, DarkBgEnd))

    Box(modifier = Modifier.fillMaxSize().background(bgGradient)) {
        if (showTrainerList) {
            TrainerManagementScreen(onBack = { showTrainerList = false })
        } else if (showPackageList) {
            PackageManagementScreen(packages = gymPackages, onBack = { showPackageList = false })
        } else {
            Scaffold(
                containerColor = Color.Transparent, // Biarkan transparan agar Box background terlihat
                topBar = {
                    TopAppBar(
                        title = { Text("Admin Dashboard", fontWeight = FontWeight.Bold, color = Color.White) },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            actionIconContentColor = AccentBlue
                        ),
                        actions = {
                            IconButton(onClick = {
                                val htmlContent = buildString {
                                    append("""
                                    <html xmlns:o="urn:schemas-microsoft-com:office:office"
                                          xmlns:x="urn:schemas-microsoft-com:office:excel"
                                          xmlns="http://www.w3.org/TR/REC-html40">
                                    <head>
                                        <meta charset="utf-8">
                                        <style>
                                            table { border-collapse: collapse; width: 100%; font-family: Arial, sans-serif; }
                                            th { background-color: #3B82F6; color: white; border: 1px solid #000000; padding: 12px; font-weight: bold; text-align: center; }
                                            td { border: 1px solid #000000; padding: 8px; text-align: left; vertical-align: middle; }
                                            .center-text { text-align: center; }
                                            .title { font-size: 24px; font-weight: bold; color: #333333; margin-bottom: 20px; }
                                        </style>
                                    </head>
                                    <body>
                                        <div class="title">Laporan Data Member - Youth Gym</div>
                                        <table>
                                            <tr>
                                                <th>ID Member</th>
                                                <th>Nama Lengkap</th>
                                                <th>Username</th>
                                                <th>Sisa Kuota (Hari)</th>
                                                <th>Berat Badan (kg)</th>
                                                <th>Tinggi Badan (cm)</th>
                                            </tr>
                                    """.trimIndent())
                                    members.forEach { member ->
                                        append("""
                                            <tr>
                                                <td class="center-text">${member.id}</td>
                                                <td>${member.name}</td>
                                                <td>${member.username}</td>
                                                <td class="center-text"><b>${member.remainingDays}</b></td>
                                                <td class="center-text">${member.weight}</td>
                                                <td class="center-text">${member.height}</td>
                                            </tr>
                                        """.trimIndent())
                                    }
                                    append("""
                                        </table>
                                    </body>
                                    </html>
                                    """.trimIndent())
                                }
                                exportToExcel(htmlContent)
                                showExportSuccess = true
                            }) {
                                Icon(Icons.Default.Download, contentDescription = "Export Excel")
                            }
                            IconButton(onClick = { showTrainerList = true }) {
                                Icon(Icons.Default.Face, contentDescription = "Trainer")
                            }
                            IconButton(onClick = { showPackageList = true }) {
                                Icon(Icons.Default.List, contentDescription = "Packages")
                            }
                        }
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { showAddDialog = true },
                        containerColor = AccentBlue,
                        contentColor = Color.White
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah Member")
                    }
                }
            ) { padding ->
                Column(
                    modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)
                ) {
                    // Revenue Card - Gradien Biru
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Brush.linearGradient(listOf(AccentBlueDark, AccentBlue)))
                                .padding(24.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(40.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text("Total Pendapatan", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White.copy(alpha = 0.8f))
                                    Text("Rp ${formatRupiah(totalRevenue)}", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Kotak Pencarian Tema Gelap
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Cari Member...", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AccentBlue) },
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = CardDark,
                            cursorColor = AccentBlue,
                            focusedContainerColor = CardDark.copy(alpha = 0.5f),
                            unfocusedContainerColor = CardDark.copy(alpha = 0.3f)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Daftar Member", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(members.filter { it.name.contains(searchQuery, true) }) { member ->
                            MemberCard(
                                member = member,
                                onCheckIn = { memberToCheckIn = member },
                                onEdit = { memberToEdit = member },
                                onDelete = { memberToDelete = member }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text("Logout", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }

    // --- DIALOG NOTIFIKASI EXPORT ---
    if (showExportSuccess) {
        AlertDialog(
            onDismissRequest = { showExportSuccess = false },
            title = { Text("Ekspor Berhasil", fontWeight = FontWeight.Bold, color = AccentBlue) },
            text = { Text("Data member berhasil diekspor ke Excel.\nSilakan cek folder Profil / Home di laptop Anda (Laporan_Member_YouthGym.xls).") },
            confirmButton = {
                Button(
                    onClick = { showExportSuccess = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) { Text("Tutup", color = Color.White) }
            }
        )
    }

    // --- DIALOG CHECK-IN MANUAL ---
    if (memberToCheckIn != null) {
        val member = memberToCheckIn!!
        AlertDialog(
            onDismissRequest = { memberToCheckIn = null },
            title = { Text("Check-In Manual", fontWeight = FontWeight.Bold) },
            text = {
                if (member.remainingDays > 0) {
                    Text("Apakah Anda yakin ingin melakukan Check-In untuk ${member.name}? Kuota akan berkurang dari ${member.remainingDays} menjadi ${member.remainingDays - 1}.")
                } else {
                    Text("Gagal! Kuota latihan ${member.name} sudah habis (0 Hari). Silakan perpanjang paket.", color = Color.Red)
                }
            },
            confirmButton = {
                if (member.remainingDays > 0) {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        onClick = {
                            scope.launch {
                                try {
                                    val updatedMember = member.copy(remainingDays = member.remainingDays - 1)
                                    supabase.postgrest["members"].update(updatedMember) {
                                        filter { eq("id", member.id) }
                                    }
                                    val idx = members.indexOfFirst { it.id == member.id }
                                    if (idx != -1) members[idx] = updatedMember
                                    memberToCheckIn = null
                                } catch (e: Exception) {
                                    println("Gagal Check-In: ${e.message}")
                                }
                            }
                        }
                    ) { Text("Ya, Check-In", color = Color.White) }
                }
            },
            dismissButton = {
                TextButton(onClick = { memberToCheckIn = null }) {
                    Text(if (member.remainingDays > 0) "Batal" else "Tutup", color = Color.Gray)
                }
            }
        )
    }

    // --- DIALOG CREATE MEMBER ---
    if (showAddDialog) {
        AddMemberDialog(
            packages = gymPackages,
            onDismiss = { showAddDialog = false },
            onConfirm = { n, u, p, d, price ->
                scope.launch {
                    try {
                        // --- LOGIKA BARU: HITUNG TANGGAL OTOMATIS ---
                        val currentTime = com.chiko0085.testgym.getCurrentTimeMillis()
                        val expirationTime = currentTime + (d * 86400000L) // Sisa hari * 1 Hari dalam milidetik

                        val randomId = "CH-" + Random.nextInt(1000, 9999).toString()

                        // Memasukkan joinDate dan expiredDate
                        val newMember = Member(randomId, n, u, p, d, currentTime, expirationTime, 0.0, 0.0)

                        supabase.postgrest["members"].insert(newMember)
                        members.add(newMember)
                        onUpdateRevenue(totalRevenue + price)
                        showAddDialog = false
                    } catch (e: Exception) { println("Gagal tambah member: ${e.message}") }
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
                        supabase.postgrest["members"].update(updated) { filter { eq("id", updated.id) } }
                        val idx = members.indexOfFirst { it.id == updated.id }
                        if (idx != -1) members[idx] = updated
                        memberToEdit = null
                    } catch (e: Exception) { println("Gagal update member: ${e.message}") }
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
                            supabase.postgrest["members"].delete { filter { eq("id", memberToDelete?.id!!) } }
                            members.removeAll { it.id == memberToDelete?.id }
                            memberToDelete = null
                        } catch (e: Exception) { println("Gagal hapus member: ${e.message}") }
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
fun PackageManagementScreen(packages: MutableList<GymPackage>, onBack: () -> Unit) {
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
                                        supabase.postgrest["gym_packages"].delete { filter { eq("id", pkg.id) } }
                                        packages.remove(pkg)
                                    } catch (e: Exception) {}
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
                        supabase.postgrest["gym_packages"].insert(newPkg)
                        packages.add(newPkg)
                        showAddPackage = false
                    } catch (e: Exception) {}
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
                        supabase.postgrest["gym_packages"].update(updatedPkg) { filter { eq("id", updatedPkg.id) } }
                        val idx = packages.indexOfFirst { it.id == updatedPkg.id }
                        if (idx != -1) packages[idx] = updatedPkg
                        packageToEdit = null
                    } catch (e: Exception) {}
                }
            }
        )
    }
}

@Composable
fun PackageDialog(title: String, initialPackage: GymPackage? = null, onDismiss: () -> Unit, onConfirm: (String, Double, Int) -> Unit) {
    var name by remember { mutableStateOf(initialPackage?.name ?: "") }
    var price by remember { mutableStateOf(initialPackage?.price?.toString() ?: "") }
    var days by remember { mutableStateOf(initialPackage?.durationDays?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Paket") }, shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Harga (Rp)") }, shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = days, onValueChange = { days = it }, label = { Text("Durasi (Hari)") }, shape = RoundedCornerShape(12.dp))
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, price.toDoubleOrNull() ?: 0.0, days.toIntOrNull() ?: 0) }, colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)) { Text("Simpan", color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal", color = Color.Gray) } }
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
            val dbTrainers = supabase.postgrest["trainers"].select().decodeList<Trainer>()
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
                                        supabase.postgrest["trainers"].delete { filter { eq("id", trainer.id) } }
                                        trainers.remove(trainer)
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
                    supabase.postgrest["trainers"].insert(newTrainer)
                    trainers.add(newTrainer)
                    showAddTrainer = false
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