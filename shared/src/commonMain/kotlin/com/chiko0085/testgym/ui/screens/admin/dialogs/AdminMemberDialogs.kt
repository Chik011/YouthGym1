package com.chiko0085.testgym.ui.screens.admin.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chiko0085.testgym.getCurrentTimeMillis
import com.chiko0085.testgym.model.GymPackage
import com.chiko0085.testgym.model.Member
import com.chiko0085.testgym.ui.theme.AccentBlue
import com.chiko0085.testgym.util.formatRupiah

// Dialog tambah member
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMemberDialog(
    packages: List<GymPackage>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, Int, Int, Double, Long, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Laki-laki") }
    var phone by remember { mutableStateOf("") }
    var ptSessions by remember { mutableStateOf("0") }
    var selectedPackage by remember { mutableStateOf<GymPackage?>(null) }
    val joinDate = getCurrentTimeMillis()
    var expanded by remember { mutableStateOf(false) }
    var genderExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Member Baru", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Lengkap") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Nomor HP") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = ptSessions, onValueChange = { ptSessions = it }, label = { Text("Sesi PT") }, modifier = Modifier.fillMaxWidth())
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
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedPackage?.name ?: "Pilih Paket",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Paket") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { IconButton(onClick = { expanded = true }) { Icon(Icons.Default.ArrowDropDown, null) } }
                    )
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        packages.forEach { pkg ->
                            DropdownMenuItem(
                                text = { Text("${pkg.name} - Rp ${formatRupiah(pkg.price ?: 0.0)}") },
                                onClick = {
                                    selectedPackage = pkg
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && username.isNotBlank() && password.isNotBlank() && phone.isNotBlank() && selectedPackage != null) {
                        selectedPackage?.let { pkg ->
                            onConfirm(name, username, password, gender, phone, pkg.durationDays, ptSessions.toIntOrNull() ?: 0, pkg.price ?: 0.0, joinDate, pkg.name)
                        }
                    }
                },
                enabled = name.isNotBlank() && username.isNotBlank() && password.isNotBlank() && phone.isNotBlank() && selectedPackage != null,
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) { Text("Simpan", color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal", color = Color.Gray) } }
    )
}

// Dialog edit member
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMemberDialog(
    member: Member,
    onDismiss: () -> Unit,
    onConfirm: (Member) -> Unit
) {
    var name by remember { mutableStateOf(member.name) }
    var username by remember { mutableStateOf(member.username) }
    var password by remember { mutableStateOf(member.password) }
    var phone by remember { mutableStateOf(member.phoneNumber) }
    var remainingDays by remember { mutableStateOf(member.remainingDays.toString()) }
    var ptSessions by remember { mutableStateOf(member.remainingPtSessions.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Data Member", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Nomor HP") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = remainingDays, onValueChange = { remainingDays = it }, label = { Text("Sisa Hari") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = ptSessions, onValueChange = { ptSessions = it }, label = { Text("Sesi PT") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = member.copy(
                        name = name,
                        username = username,
                        password = password,
                        phoneNumber = phone,
                        remainingDays = remainingDays.toIntOrNull() ?: member.remainingDays,
                        remainingPtSessions = ptSessions.toIntOrNull() ?: member.remainingPtSessions
                    )
                    onConfirm(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) { Text("Update", color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal", color = Color.Gray) } }
    )
}

