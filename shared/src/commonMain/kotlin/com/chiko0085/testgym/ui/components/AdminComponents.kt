package com.chiko0085.testgym.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chiko0085.testgym.formatEpochToDate
import com.chiko0085.testgym.getCurrentTimeMillis
import com.chiko0085.testgym.model.Member
import com.chiko0085.testgym.model.Reservation
import com.chiko0085.testgym.ui.theme.*

// Kartu list member
@Composable
fun MemberCard(
    member: Member,
    onCheckIn: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onExtend: () -> Unit,
    onBuyPt: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(member.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (member.remainingDays > 0) SuccessGreen.copy(alpha = 0.2f) else ErrorRed.copy(alpha = 0.2f),
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Text(
                                if (member.remainingDays > 0) "Aktif" else "Expired",
                                color = if (member.remainingDays > 0) SuccessGreenLight else ErrorRedLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("ID: ${member.id} • ${member.gender}", color = AccentBlue, fontSize = 12.sp)
                    Text("Paket: ${member.packageName}", color = Color.LightGray, fontSize = 12.sp)
                    Text("HP: ${member.phoneNumber}", color = Color.LightGray, fontSize = 12.sp)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Sisa Masa Aktif: ${member.remainingDays} Hari", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    LinearProgressIndicator(
                        progress = { (member.remainingDays / 30f).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth(0.7f).height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = if (member.remainingDays > 10) SuccessGreen else if (member.remainingDays > 0) WarningYellow else ErrorRed,
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Sisa Sesi PT: ${member.remainingPtSessions}", color = AccentBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    IconButton(onClick = onCheckIn) { Icon(Icons.Default.CheckCircle, "Check-in", tint = SuccessGreen) }
                    IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Edit", tint = Color.LightGray) }
                    IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Hapus", tint = ErrorRedLight) }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onExtend,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Refresh, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Perpanjang Paket", color = Color.White, fontSize = 11.sp)
                }
                Button(
                    onClick = onBuyPt,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sewa PT", color = Color.White, fontSize = 11.sp)
                }
            }
        }
    }
}

// Kartu kalender mini
@Composable
fun CalendarCard() {
    val now = getCurrentTimeMillis()
    val dateStr = formatEpochToDate(now)
    val parts = dateStr.split(" ")
    val day = parts.getOrNull(0) ?: ""
    val month = parts.getOrNull(1) ?: ""
    val year = parts.getOrNull(2) ?: ""

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = CardDark)) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(60.dp).clip(RoundedCornerShape(12.dp)).background(AccentBlue.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
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

// Kartu slot reservasi admin
@Composable
fun ReservationSlotCard(reservation: Reservation, onToggleStatus: (String, String) -> Unit) {
    var showNameDialog by remember { mutableStateOf(false) }
    var inputName by remember { mutableStateOf("") }

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardDark)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                val hourStr = if (reservation.hour < 10) "0${reservation.hour}:00" else "${reservation.hour}:00"
                val nextHour = if (reservation.hour + 1 < 10) "0${reservation.hour + 1}:00" else "${reservation.hour + 1}:00"
                Text("$hourStr - $nextHour", color = Color.White, fontWeight = FontWeight.Bold)
                Text(if (reservation.status == "Empty") "Kosong" else "Terisi: ${reservation.reservedByName}", color = if (reservation.status == "Empty") SuccessGreenLight else ErrorRedLight, fontSize = 12.sp)
            }
            Row {
                Button(onClick = { onToggleStatus("Empty", "") }, colors = ButtonDefaults.buttonColors(containerColor = if (reservation.status == "Empty") SuccessGreen else Color.Gray.copy(alpha = 0.3f)), shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp), modifier = Modifier.height(32.dp)) { Text("Kosong", fontSize = 10.sp, color = Color.White) }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { showNameDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = if (reservation.status == "Booked") ErrorRed else Color.Gray.copy(alpha = 0.3f)), shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp), modifier = Modifier.height(32.dp)) { Text("Terisi", fontSize = 10.sp, color = Color.White) }
            }
        }
    }

    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            containerColor = CardDark,
            title = { Text("Siapa yang mengisi?", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = inputName,
                    onValueChange = { inputName = it },
                    label = { Text("Nama Member/Penyewa") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = AccentBlue, unfocusedBorderColor = Color.Gray)
                )
            },
            confirmButton = {
                Button(onClick = {
                    onToggleStatus("Booked", inputName)
                    showNameDialog = false
                    inputName = ""
                }, colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)) { Text("Simpan", color = Color.White) }
            },
            dismissButton = { TextButton(onClick = { showNameDialog = false }) { Text("Batal", color = Color.Gray) } }
        )
    }
}

