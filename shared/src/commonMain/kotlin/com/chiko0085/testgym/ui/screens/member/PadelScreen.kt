package com.chiko0085.testgym.ui.screens.member

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chiko0085.testgym.db
import com.chiko0085.testgym.formatEpochToDate
import com.chiko0085.testgym.getCurrentTimeMillis
import com.chiko0085.testgym.model.Member
import com.chiko0085.testgym.model.Reservation
import com.chiko0085.testgym.openWebLink
import com.chiko0085.testgym.ui.theme.AccentBlue
import com.chiko0085.testgym.ui.theme.CardDark
import com.chiko0085.testgym.ui.theme.TextSub
import dev.gitlive.firebase.firestore.where

// Layar reservasi lapangan padel
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
                .where { "date" equalTo dayStart }
                .get()

            val dbReservations = snapshot.documents.map { it.data<Reservation>() }
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
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Pilih Tanggal", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    val days = (0..14).map { getCurrentTimeMillis() + it * 86400000L }
                    items(days) { dayMillis ->
                        val dateStr = formatEpochToDate(dayMillis).split(" ")
                        val dayNum = dateStr.getOrNull(0) ?: ""
                        val monthStr = dateStr.getOrNull(1)?.take(3) ?: ""
                        
                        val dateStrFull = dateStr.take(3).joinToString(" ")
                        val selectedStrFull = formatEpochToDate(selectedDateMillis).split(" ").take(3).joinToString(" ")
                        val isSelected = dateStrFull == selectedStrFull

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) AccentBlue else Color.White.copy(alpha = 0.05f),
                            modifier = Modifier.width(64.dp).height(80.dp),
                            onClick = { selectedDateMillis = dayMillis }
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(monthStr, color = if (isSelected) Color.White else TextSub, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(dayNum, color = if (isSelected) Color.White else Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("Jadwal Tersedia", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(12.dp))
        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentBlue)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(reservations) { res ->
                    MemberReservationCard(res, onBook = {
                        val dateStr = formatEpochToDate(res.date).split(" ").take(3).joinToString(" ")
                        val hourStr = if (res.hour < 10) "0${res.hour}:00" else "${res.hour}:00"
                        val message = "Halo Admin Youth Gym, saya ${member.name} ingin booking lapangan Padel untuk tanggal $dateStr jam $hourStr. Apakah masih tersedia?"
                        val encodedMessage = message.replace(" ", "%20")
                        openWebLink("https://wa.me/6285166322618?text=$encodedMessage")
                    })
                }
            }
        }
    }
}

// Kartu slot reservasi
@Composable
fun MemberReservationCard(reservation: Reservation, onBook: () -> Unit) {
    val hourStr = if (reservation.hour < 10) "0${reservation.hour}:00" else "${reservation.hour}:00"
    val nextHour = if (reservation.hour + 1 < 10) "0${reservation.hour + 1}:00" else "${reservation.hour + 1}:00"
    val isAvailable = reservation.status == "Empty"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isAvailable) CardDark else Color(0xFF0F172A).copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(if (isAvailable) 4.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isAvailable) AccentBlue.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(if (reservation.hour < 10) "0${reservation.hour}" else "${reservation.hour}", color = if (isAvailable) AccentBlue else Color.Gray, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("$hourStr - $nextHour", color = if(isAvailable) Color.White else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        text = if (isAvailable) "Tersedia" else "Terisi oleh: ${reservation.reservedByName}",
                        color = if (isAvailable) Color(0xFF34D399) else Color(0xFFF87171),
                        fontSize = 12.sp
                    )
                }
            }
            if (isAvailable) {
                Button(
                    onClick = onBook,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Text("Booking", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}
