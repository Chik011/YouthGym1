package com.chiko0085.testgym.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chiko0085.testgym.db
import com.chiko0085.testgym.model.Admin
import com.chiko0085.testgym.ui.theme.AccentBlue
import com.chiko0085.testgym.ui.theme.DarkBgEnd
import com.chiko0085.testgym.ui.theme.DarkBgStart
import kotlinx.coroutines.launch

// Layar profil admin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProfileScreen(admin: Admin, onUpdate: (Admin) -> Unit, onBack: () -> Unit) {
    var username by remember { mutableStateOf(admin.username) }
    var password by remember { mutableStateOf(admin.password) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(DarkBgStart, DarkBgEnd)))) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text("Pengaturan Profil Admin", color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) } }
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Ubah Kredensial Login", color = AccentBlue, fontWeight = FontWeight.Bold)
                OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username Baru") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = AccentBlue, unfocusedBorderColor = Color.Gray))
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password Baru") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = AccentBlue, unfocusedBorderColor = Color.Gray))
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val updated = admin.copy(username = username, password = password)
                                // Jika username berubah, kita harus menghapus dokumen lama dan membuat yang baru
                                if (admin.username != username) {
                                    db.collection("admins").document(admin.username).delete()
                                }
                                db.collection("admins").document(username).set(updated)
                                onUpdate(updated)
                                snackbarHostState.showSnackbar("Profil Admin berhasil diperbarui!")
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("Gagal memperbarui: ${e.message}")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) { Text("Simpan Perubahan", fontWeight = FontWeight.Bold, color = Color.White) }
            }
        }
    }
}
