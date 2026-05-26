package com.chiko0085.testgym

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chiko0085.testgym.Trainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainerMainScreen(trainer: Trainer, onLogout: () -> Unit) {
    val bgGradient = Brush.verticalGradient(listOf(Color(0xFF000000), Color(0xFF0A192F)))

    // Data Dummy Jadwal Melatih (Nanti bisa disambungkan ke database)
    val schedules = listOf(
        "Senin, 10:00 WIB - Sesi Bulking dengan Budi",
        "Senin, 16:00 WIB - Sesi Cardio dengan Siska",
        "Selasa, 09:00 WIB - Pendampingan Pemula (Riko)",
        "Rabu, 14:00 WIB - Sesi Powerlifting dengan Andi"
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Trainer Dashboard", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Logout", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().background(bgGradient).padding(padding)) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

                // Kartu Profil Trainer (Unlimited)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF112240)),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(60.dp).clip(CircleShape).background(Color(0xFF3B82F6)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Coach ${trainer.name}", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                            Text(trainer.specialization, color = Color.Gray, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = Color(0xFF10B981).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("STAFF / UNLIMITED ACCESS", color = Color(0xFF34D399), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("Jadwal Melatih Anda", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Tetap semangat membangun body goals member!", color = Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(12.dp))

                // List Jadwal
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(schedules) { schedule ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF112240).copy(alpha = 0.7f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DateRange, null, tint = Color(0xFF3B82F6))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(schedule, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }
}