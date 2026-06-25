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
import com.chiko0085.testgym.model.Member
import com.chiko0085.testgym.model.PtPackage
import com.chiko0085.testgym.ui.theme.*
import com.chiko0085.testgym.util.formatRupiah
import kotlinx.coroutines.launch
import kotlin.random.Random

// Layar manajemen paket PT
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PtPackageManagementScreen(packages: SnapshotStateList<PtPackage>, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var packageToEdit by remember { mutableStateOf<PtPackage?>(null) }
    var showAddPackage by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(DarkBgStart, DarkBgEnd)))) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { TopAppBar(title = { Text("Manajemen Paket PT", color = Color.White) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent), navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) } }) },
            floatingActionButton = { FloatingActionButton(onClick = { showAddPackage = true }, containerColor = AccentBlue) { Icon(Icons.Default.Add, null, tint = Color.White) } }
        ) { padding ->
            LazyColumn(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(packages) { pkg ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardDark)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(pkg.name, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color.White)
                                Text("Harga: Rp ${formatRupiah(pkg.price ?: 0.0)}", color = AccentBlue, fontWeight = FontWeight.Bold)
                                Text("Sesi: ${pkg.sessions} Sesi", fontSize = 14.sp, color = Color.LightGray)
                            }
                            IconButton(onClick = { packageToEdit = pkg }) { Icon(Icons.Default.Edit, null, tint = Color.LightGray) }
                            IconButton(onClick = {
                                scope.launch {
                                    try {
                                        val idToRemove = pkg.id
                                        packages.remove(pkg)
                                        db.collection("pt_packages").document(idToRemove).delete()
                                    } catch (e: Exception) {}
                                }
                            }) { Icon(Icons.Default.Delete, null, tint = ErrorRedLight) }
                        }
                    }
                }
            }
        }
    }

    if (showAddPackage) {
        PtPackageDialog(title = "Tambah Paket PT", onDismiss = { showAddPackage = false }, onConfirm = { n, p, s ->
            scope.launch {
                try {
                    val randomId = "PT-PKG-" + Random.nextInt(1000, 9999).toString()
                    val newPkg = PtPackage(randomId, n, p, s)
                    packages.add(newPkg)
                    showAddPackage = false
                    db.collection("pt_packages").document(randomId).set(mapOf("id" to randomId, "name" to n, "price" to p, "sessions" to s))
                } catch (e: Exception) {}
            }
        })
    }

    if (packageToEdit != null) {
        PtPackageDialog(title = "Edit Paket PT", initialPackage = packageToEdit, onDismiss = { packageToEdit = null }, onConfirm = { n, p, s ->
            scope.launch {
                try {
                    val updatedPkg = packageToEdit!!.copy(name = n, price = p, sessions = s)
                    val idx = packages.indexOfFirst { it.id == updatedPkg.id }
                    if (idx != -1) packages[idx] = updatedPkg
                    packageToEdit = null
                    db.collection("pt_packages").document(updatedPkg.id).update(mapOf("name" to n, "price" to p, "sessions" to s))
                } catch (e: Exception) {}
            }
        })
    }
}

// Dialog input paket PT
@Composable
fun PtPackageDialog(title: String, initialPackage: PtPackage? = null, onDismiss: () -> Unit, onConfirm: (String, Double, Int) -> Unit) {
    var name by remember { mutableStateOf(initialPackage?.name ?: "") }
    var price by remember { mutableStateOf(initialPackage?.price?.toLong()?.toString() ?: "") }
    var sessions by remember { mutableStateOf(initialPackage?.sessions?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Paket PT") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Harga (Angka saja)") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = sessions, onValueChange = { sessions = it }, label = { Text("Jumlah Sesi") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, price.filter { it.isDigit() }.toDoubleOrNull() ?: 0.0, sessions.filter { it.isDigit() }.toIntOrNull() ?: 0) }, colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)) { Text("Simpan", color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal", color = Color.Gray) } }
    )
}

// Dialog sewa paket PT
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyPtPackageDialog(
    member: Member,
    packages: List<PtPackage>,
    onDismiss: () -> Unit,
    onConfirm: (PtPackage) -> Unit
) {
    var selectedPackage by remember { mutableStateOf<PtPackage?>(null) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sewa Paket PT: ${member.name}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Pilih paket Personal Trainer untuk menambahkan sesi latihan member.", fontSize = 14.sp)
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedPackage?.name ?: "Pilih Paket PT",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Paket PT") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { IconButton(onClick = { expanded = true }) { Icon(Icons.Default.ArrowDropDown, null) } }
                    )
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        packages.forEach { pkg ->
                            DropdownMenuItem(text = { Text("${pkg.name} - ${pkg.sessions} Sesi (Rp ${formatRupiah(pkg.price ?: 0.0)})") }, onClick = { selectedPackage = pkg; expanded = false })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { selectedPackage?.let { onConfirm(it) } }, colors = ButtonDefaults.buttonColors(containerColor = AccentBlue), enabled = selectedPackage != null) { Text("Beli Paket", color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal", color = Color.Gray) } }
    )
}
