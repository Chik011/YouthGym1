package com.chiko0085.testgym.ui.screens.trainer.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chiko0085.testgym.model.ExerciseRecord
import com.chiko0085.testgym.model.Member
import com.chiko0085.testgym.model.Trainer
import com.chiko0085.testgym.model.WorkoutSession
import com.chiko0085.testgym.ui.theme.AccentBlue
import com.chiko0085.testgym.ui.theme.CardDark
import com.chiko0085.testgym.getCurrentTimeMillis
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(
    currentTrainer: Trainer,
    onDismiss: () -> Unit,
    onSave: (Trainer) -> Unit
) {
    var name by remember { mutableStateOf(currentTrainer.name) }
    var specialization by remember { mutableStateOf(currentTrainer.specialization) }
    var experience by remember { mutableStateOf(currentTrainer.experience) }
    var description by remember { mutableStateOf(currentTrainer.description) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardDark,
        title = { Text("Edit Profil Coach", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Lengkap") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = Color.Gray
                    )
                )
                OutlinedTextField(
                    value = specialization,
                    onValueChange = { specialization = it },
                    label = { Text("Spesialisasi") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = Color.Gray
                    )
                )
                OutlinedTextField(
                    value = experience,
                    onValueChange = { experience = it },
                    label = { Text("Pengalaman (Tahun)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = Color.Gray
                    )
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Bio / Deskripsi") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = Color.Gray
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(currentTrainer.copy(
                        name = name,
                        specialization = specialization,
                        experience = experience,
                        description = description
                    ))
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text("Simpan", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = Color.Gray)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsDialog(
    currentTrainer: Trainer,
    onDismiss: () -> Unit,
    onSave: (Trainer) -> Unit
) {
    var username by remember { mutableStateOf(currentTrainer.username) }
    var password by remember { mutableStateOf(currentTrainer.password) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardDark,
        title = { Text("Pengaturan Akun", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = Color.Gray
                    )
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = Color.Gray
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(currentTrainer.copy(username = username, password = password))
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text("Simpan", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = Color.Gray)
            }
        }
    )
}

@Composable
fun AttendanceDialog(
    memberList: List<Member>,
    onDismiss: () -> Unit,
    onMarkAttendance: (Member) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardDark,
        title = { Text("Absensi Member PT", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Box(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                if (memberList.isEmpty()) {
                    Text("Belum ada member yang terdaftar.", color = Color.Gray)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(memberList) { member ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Person, null, tint = AccentBlue, modifier = Modifier.size(32.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(member.name, color = Color.White, fontWeight = FontWeight.Bold)
                                        val currentTimeLocal = com.chiko0085.testgym.getCurrentTimeMillis()
                                        val isExpired = member.ptExpiredDate > 0 && currentTimeLocal > member.ptExpiredDate
                                        
                                        if (isExpired) {
                                            Text("PAKET HANGUS (Sudah > 1 Bulan)", color = Color(0xFFF87171), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        } else {
                                            Text("Sisa Sesi: ${member.remainingPtSessions}", color = Color.Gray, fontSize = 12.sp)
                                        }
                                        
                                        Button(
                                            onClick = { onMarkAttendance(member) },
                                            enabled = member.remainingPtSessions > 0 || (member.ptExpiredDate > 0 && currentTimeLocal > member.ptExpiredDate),
                                            colors = ButtonDefaults.buttonColors(containerColor = if (isExpired) Color(0xFFEF4444) else AccentBlue),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp).padding(top = 4.dp)
                                        ) {
                                            Text(if (isExpired) "Reset Hangus" else "Hadir", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup", color = Color.White)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutManagementDialog(
    memberList: List<Member>,
    trainer: Trainer,
    onDismiss: () -> Unit,
    onSaveWorkout: (WorkoutSession) -> Unit
) {
    var selectedMember by remember { mutableStateOf<Member?>(null) }
    var exerciseName by remember { mutableStateOf("") }
    var sets by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    val exercises = remember { mutableStateListOf<ExerciseRecord>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardDark,
        title = { Text("Program Latihan Member", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (selectedMember == null) {
                    Text("Pilih Member:", color = AccentBlue, fontWeight = FontWeight.Bold)
                    memberList.filter { it.remainingPtSessions > 0 }.forEach { member ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { selectedMember = member },
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
                        ) {
                            Text(member.name, modifier = Modifier.padding(12.dp), color = Color.White)
                        }
                    }
                    if (memberList.none { it.remainingPtSessions > 0 }) {
                        Text("Tidak ada member aktif PT.", color = Color.Gray)
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Member: ${selectedMember?.name}", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        TextButton(onClick = { selectedMember = null }) { Text("Ganti", color = AccentBlue) }
                    }
                    
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    
                    Text("Tambah Latihan:", color = AccentBlue, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = exerciseName,
                        onValueChange = { exerciseName = it },
                        label = { Text("Nama Latihan (cth: Bench Press)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = sets,
                            onValueChange = { sets = it },
                            label = { Text("Sets") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                        OutlinedTextField(
                            value = reps,
                            onValueChange = { reps = it },
                            label = { Text("Reps") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                    }
                    
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text("Beban (cth: 50kg / BW)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    
                    Button(
                        onClick = {
                            if (exerciseName.isNotEmpty()) {
                                exercises.add(ExerciseRecord(exerciseName, sets.toIntOrNull() ?: 0, reps, weight))
                                exerciseName = ""; sets = ""; reps = ""; weight = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue.copy(alpha = 0.3f))
                    ) {
                        Icon(Icons.Default.Add, null)
                        Text("Tambahkan ke List")
                    }
                    
                    if (exercises.isNotEmpty()) {
                        Text("List Latihan:", color = Color.White, fontWeight = FontWeight.Bold)
                        exercises.forEachIndexed { index, ex ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f))
                            ) {
                                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("${index + 1}. ${ex.name} (${ex.sets}x${ex.reps}) - ${ex.weight}", color = Color.LightGray, modifier = Modifier.weight(1f), fontSize = 13.sp)
                                    IconButton(onClick = { exercises.removeAt(index) }) {
                                        Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                    
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Catatan Latihan") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedMember != null && exercises.isNotEmpty()) {
                        onSaveWorkout(
                            WorkoutSession(
                                id = "WKT-${Random.nextInt(10000, 99999)}",
                                memberId = selectedMember!!.id,
                                memberName = selectedMember!!.name,
                                trainerId = trainer.id,
                                trainerName = trainer.name,
                                date = getCurrentTimeMillis(),
                                exercises = exercises.toList(),
                                notes = notes
                            )
                        )
                    }
                },
                enabled = selectedMember != null && exercises.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text("Simpan Sesi Latihan", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = Color.Gray)
            }
        }
    )
}
