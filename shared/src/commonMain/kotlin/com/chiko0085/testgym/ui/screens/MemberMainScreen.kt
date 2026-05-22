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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chiko0085.testgym.model.Member
import com.chiko0085.testgym.openWebLink
import com.chiko0085.testgym.supabase
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.round

// Fungsi bantuan untuk menggantikan String.format Java
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

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
                    label = { Text("Padel") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                    label = { Text("Scan") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                    label = { Text("About") }
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Profil") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> MemberHomeScreen(currentMember)
                1 -> PadelScreen()
                2 -> MemberScanScreen(title = "QR Kehadiran (Mock)")
                3 -> AboutUsScreen()
                4 -> MemberProfileScreen(currentMember, onLogout)
            }
        }
    }
}

@Composable
fun MemberHomeScreen(member: Member) {
    var weightInput by remember { mutableStateOf(if(member.weight > 0) member.weight.toString() else "") }
    var heightInput by remember { mutableStateOf(if(member.height > 0) member.height.toString() else "") }
    var showTutorialCamera by remember { mutableStateOf(false) }

    val mainGradient = Brush.linearGradient(
        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
    )

    if (showTutorialCamera) {
        Box(modifier = Modifier.fillMaxSize()) {
            MemberScanScreen(title = "QR Code Alat (Tutorial - Mock)")
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Selamat Datang,", fontSize = 16.sp, color = Color.Gray)
            Text(member.name, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Box(modifier = Modifier.background(mainGradient).padding(32.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Sisa Kuota Latihan", color = Color.White.copy(alpha = 0.8f))
                        Text("${member.remainingDays}", fontSize = 64.sp, fontWeight = FontWeight.Black, color = Color.White)
                        Text("HARI LAGI", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        openWebLink("https://wa.me/+628123456789")
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Call, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { showTutorialCamera = true },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("QR Alat", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("BMI & Ideal Weight Calculator", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text("Pantau kondisi fisikmu", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it },
                        label = { Text("Berat (kg)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = heightInput,
                        onValueChange = { heightInput = it },
                        label = { Text("Tinggi (cm)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
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
                            color = if(category == "Normal") Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                // Menggunakan fungsi formatDecimal pengganti String.format
                                Text("BMI: ${formatDecimal(bmi)} ($category)", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = if(category == "Normal") Color(0xFF2E7D32) else Color(0xFFC62828))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Berat Badan Ideal Anda:", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text("${formatDecimal(idealLow)} kg - ${formatDecimal(idealHigh)} kg", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color(0xFF1976D2))
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                member.weight = weight
                                member.height = height
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Simpan ke Profil")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PadelScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Layanan Padel", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reservasi Lapangan", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Text("Booking jadwal main Padel Anda secara real-time.", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { /* Logic */ }, modifier = Modifier.fillMaxWidth()) {
                    Text("Cek Ketersediaan")
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ThumbUp, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Penyewaan Alat", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Text("Sewa raket Padel dan bola berkualitas.", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(onClick = { /* Logic */ }, modifier = Modifier.fillMaxWidth()) {
                    Text("Lihat Katalog Alat")
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Face, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Layanan Padel (Coach)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Text("Butuh pelatih? Kami menyediakan jasa coach profesional.", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { /* Logic */ }, modifier = Modifier.fillMaxWidth()) {
                    Text("Hubungi Coach")
                }
            }
        }
    }
}

@Composable
fun AboutUsScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("About Us", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(24.dp))
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(4.dp)) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Youth Gym", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Aplikasi Manajemen Fasilitas Olahraga Pintar yang dirancang untuk memudahkan member dalam memantau sisa kuota latihan dan reservasi fasilitas olahraga modern.", textAlign = TextAlign.Justify)
            }
        }
    }
}

@Composable
fun MemberScanScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray)) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Search, contentDescription = null, tint = Color.White, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(title, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Fitur Kamera dimatikan sementara di mode KMP", color = Color.LightGray, fontSize = 12.sp)
        }
    }
}

@Composable
fun MemberProfileScreen(member: Member, onLogout: () -> Unit) {
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf(member.name) }
    var weight by remember { mutableStateOf(if(member.weight > 0) member.weight.toString() else "") }
    var height by remember { mutableStateOf(if(member.height > 0) member.height.toString() else "") }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Profil Saya", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(modifier = Modifier.height(24.dp))
        Surface(modifier = Modifier.size(80.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
            Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, null, modifier = Modifier.size(50.dp), tint = MaterialTheme.colorScheme.primary) }
        }
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Lengkap") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("Berat (kg)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = height, onValueChange = { height = it }, label = { Text("Tinggi (cm)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                // Jangan sekadar simpan lokal, tembak ke Supabase!
                val updatedWeight = weight.toDoubleOrNull() ?: 0.0
                val updatedHeight = height.toDoubleOrNull() ?: 0.0
                val updatedMember = member.copy(name = name, weight = updatedWeight, height = updatedHeight)

                scope.launch {
                    try {
                        // UPDATE DATABASENYA
                        supabase.postgrest["members"].update(updatedMember) {
                            filter { eq("id", member.id) }
                        }

                        // Update lokal agar layar tidak perlu dimuat ulang
                        member.name = name
                        member.weight = updatedWeight
                        member.height = updatedHeight
                        println("Profil sukses disimpan di Cloud!")
                    } catch (e: Exception) {
                        println("Gagal simpan profil: ${e.message}")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Simpan Perubahan", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.weight(1f))
        TextButton(onClick = onLogout) { Text("Logout / Keluar", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }
    }
}