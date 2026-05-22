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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chiko0085.testgym.exportToExcel
import com.chiko0085.testgym.model.GymPackage
import com.chiko0085.testgym.model.Member
import com.chiko0085.testgym.supabase
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

    if (showPackageList) {
        PackageManagementScreen(packages = gymPackages, onBack = { showPackageList = false })
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Admin Dashboard", fontWeight = FontWeight.Bold) },
                    actions = {
                        // TOMBOL EXPORT EXCEL (VERSI RAPI & BERGARIS)
                        IconButton(onClick = {
                            // Membuat struktur tabel HTML dengan CSS (Excel bisa membacanya menjadi desain tabel!)
                            val htmlContent = buildString {
                                append("""
                                <html xmlns:o="urn:schemas-microsoft-com:office:office"
                                      xmlns:x="urn:schemas-microsoft-com:office:excel"
                                      xmlns="http://www.w3.org/TR/REC-html40">
                                <head>
                                    <meta charset="utf-8">
                                    <style>
                                        /* Mengatur desain tabel, garis border, dan jarak (padding) */
                                        table { border-collapse: collapse; width: 100%; font-family: Arial, sans-serif; }
                                        th { background-color: #4CAF50; color: white; border: 1px solid #000000; padding: 12px; font-weight: bold; text-align: center; }
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

                                                    // Mengisi baris data member
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

                            // Panggil fungsi simpan dengan data HTML
                            exportToExcel(htmlContent)
                            showExportSuccess = true
                        }) {
                            Icon(Icons.Default.Download, contentDescription = "Export Excel", tint = Color(0xFF4CAF50))
                        }

                        IconButton(onClick = { showPackageList = true }) {
                            Icon(Icons.Default.List, contentDescription = "Packages", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Member")
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)
            ) {
                // Revenue Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Total Pendapatan", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text("Rp ${formatRupiah(totalRevenue)}", fontSize = 28.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari Member...") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Daftar Member", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Gray)
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
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Logout", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // --- DIALOG NOTIFIKASI EXPORT ---
    if (showExportSuccess) {
        AlertDialog(
            onDismissRequest = { showExportSuccess = false },
            title = { Text("Ekspor Berhasil", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50)) },
            text = { Text("Data member berhasil diekspor ke Excel (CSV).\nSilakan cek folder Profil / Home di laptop Anda (Data_Member_Gym.csv).") },
            confirmButton = {
                Button(onClick = { showExportSuccess = false }) { Text("Tutup") }
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
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
                    ) { Text("Ya, Check-In") }
                }
            },
            dismissButton = {
                TextButton(onClick = { memberToCheckIn = null }) {
                    Text(if (member.remainingDays > 0) "Batal" else "Tutup")
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
                        val randomId = "CH-" + Random.nextInt(1000, 9999).toString()
                        val newMember = Member(randomId, n, u, p, d, 0.0, 0.0)
                        supabase.postgrest["members"].insert(newMember)
                        members.add(newMember)
                        onUpdateRevenue(totalRevenue + price)
                        showAddDialog = false
                    } catch (e: Exception) {
                        println("Gagal tambah member: ${e.message}")
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
                        supabase.postgrest["members"].update(updated) {
                            filter { eq("id", updated.id) }
                        }
                        val idx = members.indexOfFirst { it.id == updated.id }
                        if (idx != -1) members[idx] = updated
                        memberToEdit = null
                    } catch (e: Exception) {
                        println("Gagal update member: ${e.message}")
                    }
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
                            supabase.postgrest["members"].delete {
                                filter { eq("id", memberToDelete?.id!!) }
                            }
                            members.removeAll { it.id == memberToDelete?.id }
                            memberToDelete = null
                        } catch (e: Exception) {
                            println("Gagal hapus member: ${e.message}")
                        }
                    }
                }) { Text("Hapus", color = Color.Red, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { memberToDelete = null }) { Text("Batal") }
            }
        )
    }
}

@Composable
fun MemberCard(member: Member, onCheckIn: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(45.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
                Text(member.name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(member.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    text = "${member.remainingDays} Hari",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (member.remainingDays > 0) MaterialTheme.colorScheme.secondary else Color.Red
                )
            }
            // Ikon Check-In Berubah Menjadi Centang Hijau
            IconButton(onClick = onCheckIn) { Icon(Icons.Default.CheckCircle, contentDescription = "Check-in Manual", tint = Color(0xFF4CAF50)) }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFF44336)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMemberDialog(
    packages: List<GymPackage>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Int, Double) -> Unit
) {
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
                        value = selectedPackageName,
                        onValueChange = {},
                        label = { Text("Pilih Paket") },
                        readOnly = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { expanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                        }
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
                shape = RoundedCornerShape(8.dp)
            ) { Text("Simpan Member", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
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
            Button(onClick = { onConfirm(member.copy(name=name, username=user, password=pass, remainingDays=days.toIntOrNull()?:0)) }) { Text("Update") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackageManagementScreen(packages: MutableList<GymPackage>, onBack: () -> Unit) {
    val scope = rememberCoroutineScope() // Jalur internet untuk paket
    var packageToEdit by remember { mutableStateOf<GymPackage?>(null) }
    var showAddPackage by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manajemen Paket Harga") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddPackage = true }) { Icon(Icons.Default.Add, null) }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(packages) { pkg ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(pkg.name, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                            Text("Harga: Rp ${formatRupiah(pkg.price)}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Text("Durasi: ${pkg.durationDays} Hari", fontSize = 14.sp)
                        }
                        IconButton(onClick = { packageToEdit = pkg }) { Icon(Icons.Default.Edit, null, tint = Color.Gray) }
                        IconButton(onClick = {
                            // DELETE PAKET DARI SUPABASE
                            scope.launch {
                                try {
                                    supabase.postgrest["gym_packages"].delete {
                                        filter { eq("id", pkg.id) }
                                    }
                                    packages.remove(pkg)
                                } catch (e: Exception) {
                                    println("Gagal hapus paket: ${e.message}")
                                }
                            }
                        }) { Icon(Icons.Default.Delete, null, tint = Color.Red) }
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
                        // INSERT PAKET KE SUPABASE
                        supabase.postgrest["gym_packages"].insert(newPkg)
                        packages.add(newPkg)
                        showAddPackage = false
                    } catch (e: Exception) {
                        println("Gagal tambah paket: ${e.message}")
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
                        // UPDATE PAKET KE SUPABASE
                        supabase.postgrest["gym_packages"].update(updatedPkg) {
                            filter { eq("id", updatedPkg.id) }
                        }
                        val idx = packages.indexOfFirst { it.id == updatedPkg.id }
                        if (idx != -1) packages[idx] = updatedPkg
                        packageToEdit = null
                    } catch (e: Exception) {
                        println("Gagal update paket: ${e.message}")
                    }
                }
            }
        )
    }
}

@Composable
fun PackageDialog(
    title: String,
    initialPackage: GymPackage? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Int) -> Unit
) {
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
            Button(onClick = { onConfirm(name, price.toDoubleOrNull() ?: 0.0, days.toIntOrNull() ?: 0) }) { Text("Simpan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}