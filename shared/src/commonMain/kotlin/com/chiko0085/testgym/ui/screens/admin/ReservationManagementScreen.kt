package com.chiko0085.testgym.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chiko0085.testgym.db
import com.chiko0085.testgym.formatEpochToDate
import com.chiko0085.testgym.getCurrentTimeMillis
import com.chiko0085.testgym.model.Reservation
import com.chiko0085.testgym.ui.components.ReservationSlotCard
import com.chiko0085.testgym.ui.theme.*
import kotlinx.coroutines.launch

// Layar manajemen reservasi padel
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
            val snapshot = db.collection("reservations").where { "date" equalTo dayStart }.get()
            val dbReservations = snapshot.documents.map { it.data<Reservation>() }
            val fullList = mutableListOf<Reservation>()
            for (i in 8..23) {
                val found = dbReservations.find { it.hour == i }
                fullList.add(found ?: Reservation(id = "${dayStart}_$i", date = dayStart, hour = i, status = "Empty"))
            }
            reservations = fullList
        } catch (e: Exception) {
        } finally {
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(DarkBgStart, DarkBgEnd)))) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { TopAppBar(title = { Text("Manajemen Jadwal Padel", color = Color.White) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent), navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) } }) }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Tanggal: ${formatEpochToDate(selectedDateMillis).split(" ").take(3).joinToString(" ")}", color = Color.White, fontWeight = FontWeight.Bold)
                    Row {
                        IconButton(onClick = { selectedDateMillis -= 86400000L }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                        IconButton(onClick = { selectedDateMillis += 86400000L }) { Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White) }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = AccentBlue) }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                        items(reservations) { res ->
                            ReservationSlotCard(res, onToggleStatus = { newStatus, name ->
                                scope.launch {
                                    try {
                                        val updated = res.copy(status = newStatus, reservedByName = name)
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
