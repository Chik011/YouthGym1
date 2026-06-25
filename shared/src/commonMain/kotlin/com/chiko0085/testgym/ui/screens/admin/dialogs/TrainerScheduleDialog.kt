package com.chiko0085.testgym.ui.screens.admin.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chiko0085.testgym.db
import com.chiko0085.testgym.model.Member
import com.chiko0085.testgym.model.Trainer
import com.chiko0085.testgym.ui.theme.AccentBlue
import com.chiko0085.testgym.ui.theme.CardDark
import kotlinx.coroutines.launch

// Dialog kelola jadwal trainer
@Composable
fun TrainerScheduleDialog(
    trainer: Trainer,
    trainers: SnapshotStateList<Trainer>,
    members: List<Member>,
    onDismiss: () -> Unit,
    onUpdateTrainer: (Trainer) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var dayText by remember { mutableStateOf("") }
    var timeText by remember { mutableStateOf("") }
    var selectedMemberName by remember { mutableStateOf("Pilih Member") }
    var memberDropdownExpanded by remember { mutableStateOf(false) }
    var dayDropdownExpanded by remember { mutableStateOf(false) }
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()

    val availableDays = trainer.availability

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardDark,
        title = { Text("Kelola Jadwal: Coach ${trainer.name}", color = Color.White) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                Text("Daftar Jadwal:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))

                Box(modifier = Modifier.heightIn(max = 200.dp)) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val currentSchedules = trainer.schedules
                        itemsIndexed(currentSchedules) { index, schedule ->
                            Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)), shape = RoundedCornerShape(8.dp)) {
                                Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(schedule, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                                    Row {
                                        IconButton(onClick = {
                                            try {
                                                val parts = schedule.split(", ", " - ")
                                                if(parts.size >= 3) {
                                                    dayText = parts[0]
                                                    timeText = parts[1]
                                                    selectedMemberName = parts[2].replace("Melatih ", "")
                                                } else {
                                                    dayText = schedule
                                                }
                                            } catch(e: Exception) { dayText = schedule }
                                            editingIndex = index
                                        }, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Edit, null, tint = AccentBlue, modifier = Modifier.size(16.dp)) }
                                        IconButton(onClick = {
                                            val newList = trainer.schedules.toMutableList()
                                            newList.removeAt(index)
                                            val updated = trainer.copy(schedules = newList)
                                            scope.launch {
                                                db.collection("trainers").document(trainer.id).update(mapOf("schedules" to newList))
                                                val idx = trainers.indexOfFirst { it.id == trainer.id }
                                                if (idx != -1) trainers[idx] = updated
                                                onUpdateTrainer(updated)
                                            }
                                        }, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Delete, null, tint = Color(0xFFF87171), modifier = Modifier.size(16.dp)) }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(16.dp))

                Text("Form Jadwal Baru:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AccentBlue)
                Spacer(modifier = Modifier.height(8.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = dayText, onValueChange = {}, label = { Text("Pilih Hari") }, readOnly = true, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { IconButton(onClick = { dayDropdownExpanded = true }) { Icon(Icons.Default.ArrowDropDown, null, tint = Color.White) } },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = AccentBlue, unfocusedBorderColor = Color.Gray)
                    )
                    DropdownMenu(expanded = dayDropdownExpanded, onDismissRequest = { dayDropdownExpanded = false }, modifier = Modifier.background(CardDark)) {
                        listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu").forEach { day ->
                            val isAvailable = availableDays.contains(day)
                            DropdownMenuItem(
                                text = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(day, color = if(isAvailable) Color.White else Color.Gray)
                                        if(!isAvailable) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("(PT Tidak Tersedia)", color = Color.Red, fontSize = 10.sp)
                                        }
                                    }
                                }, 
                                onClick = { 
                                    if(isAvailable) {
                                        dayText = day
                                        dayDropdownExpanded = false 
                                    }
                                },
                                enabled = isAvailable
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = timeText, onValueChange = { timeText = it }, label = { Text("Jam (Cth: 10:00 WIB)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = AccentBlue, unfocusedBorderColor = Color.Gray))
                Spacer(modifier = Modifier.height(8.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedMemberName, onValueChange = {}, label = { Text("Pilih Member") }, readOnly = true, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { IconButton(onClick = { memberDropdownExpanded = true }) { Icon(Icons.Default.ArrowDropDown, null, tint = Color.White) } },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = AccentBlue, unfocusedBorderColor = Color.Gray)
                    )
                    DropdownMenu(expanded = memberDropdownExpanded, onDismissRequest = { memberDropdownExpanded = false }, modifier = Modifier.background(CardDark)) {
                        members.forEach { m -> DropdownMenuItem(text = { Text(m.name, color = Color.White) }, onClick = { selectedMemberName = m.name; memberDropdownExpanded = false }) }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (dayText.isNotBlank() && timeText.isNotBlank() && selectedMemberName != "Pilih Member") {
                        val newList = trainer.schedules.toMutableList()
                        val formattedSchedule = "$dayText, $timeText - Melatih $selectedMemberName"

                        if (editingIndex != null) { newList[editingIndex!!] = formattedSchedule } else { newList.add(formattedSchedule) }

                        val updated = trainer.copy(schedules = newList)
                        scope.launch {
                            try {
                                db.collection("trainers").document(trainer.id).update(mapOf("schedules" to newList))
                                val idx = trainers.indexOfFirst { it.id == trainer.id }
                                if (idx != -1) trainers[idx] = updated
                                dayText = ""; timeText = ""; selectedMemberName = "Pilih Member"
                                editingIndex = null
                                onUpdateTrainer(updated)
                                snackbarHostState.showSnackbar("Berhasil diperbarui!")
                            } catch (e: Exception) { snackbarHostState.showSnackbar("Gagal: ${e.message}") }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) { Text(if (editingIndex != null) "Update" else "Tambah", color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Tutup", color = Color.Gray) } }
    )
}
