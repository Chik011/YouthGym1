package com.chiko0085.testgym.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chiko0085.testgym.db
import com.chiko0085.testgym.model.Trainer
import com.chiko0085.testgym.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.random.Random

// Layar manajemen personal trainer
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainerManagementScreen(trainers: SnapshotStateList<Trainer>, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var showAddTrainer by remember { mutableStateOf(false) }
    var trainerToEdit by remember { mutableStateOf<Trainer?>(null) }

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
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("Gender: ${trainer.gender}", color = Color.LightGray, fontSize = 12.sp)
                                    Text("Exp: ${trainer.experience}", color = Color.LightGray, fontSize = 12.sp)
                                }
                                Text("Skill: ${trainer.specialization}", color = Color.LightGray, fontSize = 14.sp)
                            }
                            IconButton(onClick = { trainerToEdit = trainer }) { Icon(Icons.Default.Edit, null, tint = Color.LightGray) }
                            IconButton(onClick = {
                                scope.launch {
                                    try {
                                        val idToRemove = trainer.id
                                        trainers.remove(trainer)
                                        db.collection("trainers").document(idToRemove).delete()
                                    } catch (e: Exception) {}
                                }
                            }) { Icon(Icons.Default.Delete, null, tint = ErrorRedLight) }
                        }
                    }
                }
            }
        }
    }

    if (showAddTrainer) {
        TrainerDialog(onDismiss = { showAddTrainer = false }, onConfirm = { n, u, p, g, s, e ->
            scope.launch {
                try {
                    val newId = "PT-" + Random.nextInt(100, 999).toString()
                    val r = 5.0
                    val d = "Trainer di Youth Gym"
                    val newTrainer = Trainer(newId, n, u, p, g, s, e, r, d)
                    trainers.add(newTrainer)
                    showAddTrainer = false
                    val data = mapOf(
                        "id" to newId,
                        "name" to n,
                        "username" to u,
                        "password" to p,
                        "gender" to g,
                        "specialization" to s,
                        "experience" to e,
                        "rate" to r,
                        "description" to d,
                        "profileImageUrl" to ""
                    )
                    db.collection("trainers").document(newId).set(data)
                } catch (ex: Exception) {}
            }
        })
    }

    if (trainerToEdit != null) {
        EditTrainerDialog(
            trainer = trainerToEdit!!,
            onDismiss = { trainerToEdit = null },
            onConfirm = { updated ->
                scope.launch {
                    try {
                        val idx = trainers.indexOfFirst { it.id == updated.id }
                        if (idx != -1) trainers[idx] = updated
                        trainerToEdit = null
                        val data = mapOf(
                            "name" to updated.name,
                            "username" to updated.username,
                            "password" to updated.password,
                            "gender" to updated.gender,
                            "specialization" to updated.specialization,
                            "experience" to updated.experience,
                            "rate" to updated.rating,
                            "description" to updated.description,
                            "profileImageUrl" to updated.profileImageUrl
                        )
                        db.collection("trainers").document(updated.id).update(data)
                    } catch (e: Exception) {}
                }
            }
        )
    }
}

// Dialog input trainer
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainerDialog(onDismiss: () -> Unit, onConfirm: (String, String, String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Laki-laki") }
    var skill by remember { mutableStateOf("") }
    var exp by remember { mutableStateOf("") }
    var genderExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Akun Trainer", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Lengkap") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = user, onValueChange = { user = it }, label = { Text("Username") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Password") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = gender, onValueChange = {}, label = { Text("Jenis Kelamin") }, readOnly = true, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { IconButton(onClick = { genderExpanded = true }) { Icon(Icons.Default.ArrowDropDown, null) } }
                    )
                    DropdownMenu(expanded = genderExpanded, onDismissRequest = { genderExpanded = false }) {
                        listOf("Laki-laki", "Perempuan").forEach { item ->
                            DropdownMenuItem(text = { Text(item) }, onClick = { gender = item; genderExpanded = false })
                        }
                    }
                }
                OutlinedTextField(value = skill, onValueChange = { skill = it }, label = { Text("Skill / Spesialisasi (Cth: Muay Thai)") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = exp, onValueChange = { exp = it }, label = { Text("Pengalaman (Cth: 3 Tahun)") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank() && user.isNotBlank()) onConfirm(name, user, pass, gender, skill, exp) },
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                shape = RoundedCornerShape(8.dp)
            ) { Text("Simpan", color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal", color = Color.Gray) } }
    )
}

// Dialog edit trainer
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTrainerDialog(trainer: Trainer, onDismiss: () -> Unit, onConfirm: (Trainer) -> Unit) {
    var name by remember { mutableStateOf(trainer.name) }
    var user by remember { mutableStateOf(trainer.username) }
    var pass by remember { mutableStateOf(trainer.password) }
    var gender by remember { mutableStateOf(trainer.gender) }
    var skill by remember { mutableStateOf(trainer.specialization) }
    var exp by remember { mutableStateOf(trainer.experience) }
    var genderExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Data Trainer", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = user, onValueChange = { user = it }, label = { Text("Username") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Password") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = gender, onValueChange = {}, label = { Text("Jenis Kelamin") }, readOnly = true, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { IconButton(onClick = { genderExpanded = true }) { Icon(Icons.Default.ArrowDropDown, null) } }
                    )
                    DropdownMenu(expanded = genderExpanded, onDismissRequest = { genderExpanded = false }) {
                        listOf("Laki-laki", "Perempuan").forEach { item ->
                            DropdownMenuItem(text = { Text(item) }, onClick = { gender = item; genderExpanded = false })
                        }
                    }
                }
                OutlinedTextField(value = skill, onValueChange = { skill = it }, label = { Text("Skill / Spesialisasi") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = exp, onValueChange = { exp = it }, label = { Text("Pengalaman") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(trainer.copy(name = name, username = user, password = pass, gender = gender, specialization = skill, experience = exp)) }, colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)) { Text("Update", color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal", color = Color.Gray) } }
    )
}
