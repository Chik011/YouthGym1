package com.chiko0085.testgym.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import com.chiko0085.testgym.model.Admin
import com.chiko0085.testgym.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminManagementScreen(
    admins: SnapshotStateList<Admin>,
    onBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var adminToEdit by remember { mutableStateOf<Admin?>(null) }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E293B))))) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Kelola Akun Admin", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { showAddDialog = true }, containerColor = Color(0xFF3B82F6)) {
                    Icon(Icons.Default.Add, null, tint = Color.White)
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(admins) { admin ->
                    AdminCard(
                        admin = admin,
                        onEdit = { adminToEdit = it },
                        onDelete = {
                            if (admin.role != "super_admin") {
                                admins.remove(admin)
                                scope.launch {
                                    try {
                                        db.collection("admins").document(admin.username).delete()
                                    } catch (e: Exception) {}
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AdminDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { newAdmin ->
                admins.add(newAdmin)
                scope.launch {
                    try {
                        db.collection("admins").document(newAdmin.username).set(newAdmin)
                    } catch (e: Exception) {}
                }
                showAddDialog = false
            }
        )
    }

    if (adminToEdit != null) {
        AdminDialog(
            admin = adminToEdit,
            onDismiss = { adminToEdit = null },
            onConfirm = { updatedAdmin ->
                val index = admins.indexOfFirst { it.username == updatedAdmin.username }
                if (index != -1) admins[index] = updatedAdmin
                scope.launch {
                    try {
                        db.collection("admins").document(updatedAdmin.username).set(updatedAdmin)
                    } catch (e: Exception) {}
                }
                adminToEdit = null
            }
        )
    }
}

@Composable
fun AdminCard(admin: Admin, onEdit: (Admin) -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.6f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(admin.username, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Role: ${admin.role}", color = Color.LightGray, fontSize = 14.sp)
                Text("Izin: ${admin.permissions.filter { it != "dashboard" && it != "profile" }.joinToString(", ")}", color = Color.Gray, fontSize = 12.sp)
            }
            if (admin.role != "super_admin") {
                IconButton(onClick = { onEdit(admin) }) {
                    Icon(Icons.Default.Edit, null, tint = Color(0xFF3B82F6))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null, tint = Color(0xFFF87171))
                }
            }
        }
    }
}

@Composable
fun AdminDialog(
    admin: Admin? = null,
    onDismiss: () -> Unit,
    onConfirm: (Admin) -> Unit
) {
    var username by remember { mutableStateOf(admin?.username ?: "") }
    var password by remember { mutableStateOf(admin?.password ?: "") }
    val allPermissions = listOf(
        "dashboard", "revenue_view", "revenue_reset", "revenue_detail", "logs", 
        "members", "members_add", "members_checkin", "members_edit", "members_delete", "members_extend", "members_pt",
        "packages", "pt_packages", "trainers", "workouts", "reservations", "wa_broadcast"
    )
    val selectedPermissions = remember { mutableStateListOf<String>().apply { 
        if (admin != null) addAll(admin.permissions)
        else addAll(listOf("dashboard"))
    } }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E293B),
        title = { Text(if (admin == null) "Tambah Admin" else "Edit Admin", color = Color.White) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { if (admin == null) username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = admin == null,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Izin Akses:", color = Color.White, fontWeight = FontWeight.Bold)
                
                val permissionLabels = mapOf(
                    "dashboard" to "Dashboard (Ringkasan)",
                    "revenue_view" to "Lihat Total Pendapatan",
                    "revenue_reset" to "Reset Pendapatan",
                    "revenue_detail" to "Lihat Detail Keuangan",
                    "logs" to "Lihat History Aktivitas Admin",
                    "members" to "Menu Member (Semua Akses)",
                    "members_add" to "• Tambah Member",
                    "members_checkin" to "• Check-in Member",
                    "members_edit" to "• Edit Member",
                    "members_delete" to "• Hapus Member",
                    "members_extend" to "• Perpanjang Paket Member",
                    "members_pt" to "• Sewa Personal Trainer",
                    "packages" to "Menu Paket Gym",
                    "pt_packages" to "Menu Paket PT",
                    "trainers" to "Menu Personal Trainer",
                    "workouts" to "Menu Progres Latihan",
                    "reservations" to "Menu Jadwal Padel",
                    "wa_broadcast" to "WhatsApp Broadcast"
                )

                allPermissions.forEach { permission ->
                    val label = permissionLabels[permission] ?: permission.replace("_", " ").capitalize()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (selectedPermissions.contains(permission)) {
                                    selectedPermissions.remove(permission)
                                } else {
                                    selectedPermissions.add(permission)
                                }
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedPermissions.contains(permission),
                            onCheckedChange = { checked ->
                                if (checked) selectedPermissions.add(permission)
                                else selectedPermissions.remove(permission)
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = AccentBlue,
                                uncheckedColor = Color.Gray
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            color = if (selectedPermissions.contains(permission)) Color.White else Color.LightGray,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(Admin(username, password, "admin", selectedPermissions.toList()))
            }) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = Color.Gray)
            }
        }
    )
}

fun String.capitalize() = this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
