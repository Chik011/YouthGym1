package com.chiko0085.testgym.ui.screens.member

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chiko0085.testgym.db
import com.chiko0085.testgym.getCurrentTimeMillis
import com.chiko0085.testgym.model.Member
import com.chiko0085.testgym.rememberImagePicker
import com.chiko0085.testgym.ui.theme.AccentBlue
import com.chiko0085.testgym.ui.theme.CardDark
import com.chiko0085.testgym.ui.theme.TextSub
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.launch

// Halaman profil member
@Composable
fun MemberProfileScreen(member: Member, onLogout: () -> Unit, onUpdatePhotoClick: (ByteArray) -> Unit) {
    val scope = rememberCoroutineScope()
    var isUploading by remember { mutableStateOf(false) }

    LaunchedEffect(member.profileImageUrl) {
        isUploading = false
    }

    val launchImagePicker = rememberImagePicker(
        onResult = { imageBytes ->
            isUploading = true
            onUpdatePhotoClick(imageBytes)
        }
    )

    var name by remember { mutableStateOf(member.name) }
    var weight by remember { mutableStateOf(if ((member.weight ?: 0.0) > 0) (member.weight?.toString() ?: "") else "") }
    var height by remember { mutableStateOf(if ((member.height ?: 0.0) > 0) (member.height?.toString() ?: "") else "") }
    var gender by remember { mutableStateOf(member.gender) }
    var username by remember { mutableStateOf(member.username) }
    var password by remember { mutableStateOf(member.password) }
    var passwordVisible by remember { mutableStateOf(false) }

    var isSaving by remember { mutableStateOf(false) }
    var showSecurityDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val currentTime = getCurrentTimeMillis()
    val calendarDaysLeft = if (member.expiredDate <= 0L) maxOf(0, member.remainingDays)
    else maxOf(0, ((member.expiredDate - currentTime) / 86400000L).toInt())
    
    val displayDays = maxOf(member.remainingDays, calendarDaysLeft)
    val isActive = displayDays > 0

    val performSave = {
        isSaving = true
        val updatedWeight = weight.toDoubleOrNull() ?: 0.0
        val updatedHeight = height.toDoubleOrNull() ?: 0.0
        val updatedMember = member.copy(
            name = name,
            weight = updatedWeight,
            height = updatedHeight,
            username = username,
            password = password,
            gender = gender
        )
        scope.launch {
            try {
                db.collection("members").document(member.id).set(updatedMember)
                member.name = name
                member.weight = updatedWeight
                member.height = updatedHeight
                member.username = username
                member.password = password
                member.gender = gender
                snackbarHostState.showSnackbar("Data berhasil disimpan!")
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Gagal menyimpan perubahan. Coba lagi.")
            } finally {
                isSaving = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Profil Saya", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Spacer(modifier = Modifier.height(24.dp))

            Box(contentAlignment = Alignment.BottomEnd) {
                Surface(
                    modifier = Modifier
                        .size(100.dp)
                        .border(2.dp, AccentBlue, CircleShape),
                    shape = CircleShape,
                    color = AccentBlue.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isUploading) {
                            CircularProgressIndicator(color = AccentBlue, modifier = Modifier.size(30.dp))
                        } else if (member.profileImageUrl.isNotEmpty()) {
                            KamelImage(
                                resource = asyncPainterResource(data = member.profileImageUrl),
                                contentDescription = "Profile",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            val defaultIcon = if (gender == "Perempuan") Icons.Default.Face else Icons.Default.Person
                            Icon(defaultIcon, null, modifier = Modifier.size(60.dp), tint = AccentBlue)
                        }
                    }
                }
                Surface(
                    modifier = Modifier
                        .size(32.dp)
                        .offset(x = 4.dp, y = 4.dp)
                        .clickable { launchImagePicker() },
                    shape = CircleShape,
                    color = AccentBlue,
                    border = androidx.compose.foundation.BorderStroke(2.dp, CardDark)
                ) {
                    Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.padding(6.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = if (isActive) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                // Tampilkan tanggal yang sudah dikoreksi secara visual
                val finalExpiredDate = if (member.remainingDays > 0 && member.expiredDate < currentTime) {
                    currentTime + (member.remainingDays * 86400000L)
                } else {
                    member.expiredDate
                }
                val expStr = com.chiko0085.testgym.formatEpochToDate(finalExpiredDate).split(" ").take(3).joinToString(" ")
                
                Text(
                    text = if (isActive) "Status: AKTIF (Hingga $expStr)" else "Status: EXPIRED",
                    color = if (isActive) Color(0xFF34D399) else Color(0xFFF87171),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            val tfColors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                focusedBorderColor = AccentBlue, unfocusedBorderColor = TextSub.copy(alpha = 0.5f),
                focusedLabelColor = AccentBlue, unfocusedLabelColor = TextSub
            )

            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Nama Lengkap") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp), colors = tfColors, singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text("Pilih Icon Profil / Gender", color = Color.White, fontSize = 14.sp, modifier = Modifier.align(Alignment.Start))
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (gender == "Laki-laki") AccentBlue.copy(alpha = 0.2f) else Color.Transparent)
                        .border(1.dp, if (gender == "Laki-laki") AccentBlue else Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .clickable { gender = "Laki-laki" }
                        .padding(12.dp)
                ) {
                    Icon(Icons.Default.Person, null, tint = if (gender == "Laki-laki") AccentBlue else Color.Gray)
                    Text("Laki-laki", color = if (gender == "Laki-laki") Color.White else Color.Gray, fontSize = 12.sp)
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (gender == "Perempuan") Color(0xFFEC4899).copy(alpha = 0.2f) else Color.Transparent)
                        .border(1.dp, if (gender == "Perempuan") Color(0xFFEC4899) else Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .clickable { gender = "Perempuan" }
                        .padding(12.dp)
                ) {
                    Icon(Icons.Default.Face, null, tint = if (gender == "Perempuan") Color(0xFFEC4899) else Color.Gray)
                    Text("Perempuan", color = if (gender == "Perempuan") Color.White else Color.Gray, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = weight, onValueChange = { weight = it },
                    label = { Text("Berat (kg)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp), colors = tfColors, singleLine = true
                )
                OutlinedTextField(
                    value = height, onValueChange = { height = it },
                    label = { Text("Tinggi (cm)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp), colors = tfColors, singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(16.dp))

            Text("Keamanan Akun", fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = username, onValueChange = { username = it },
                label = { Text("Username Login") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp), colors = tfColors, singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("Password Baru") },
                visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.CheckCircle else Icons.Filled.Lock
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(image, "Toggle Password Visibility", tint = TextSub)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp), colors = tfColors, singleLine = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (isSaving) return@Button
                    val isAccountChanged = username != member.username || password != member.password
                    if (isAccountChanged) {
                        showSecurityDialog = true
                    } else {
                        performSave()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("Simpan Perubahan", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onLogout) {
                Text("Logout / Keluar", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(80.dp))
        }

        if (showSecurityDialog) {
            AlertDialog(
                onDismissRequest = { showSecurityDialog = false },
                containerColor = Color(0xFF112240),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = "Warning", tint = Color(0xFFFBBF24))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verifikasi Keamanan", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Text(
                        "Anda mendeteksi perubahan pada Username atau Password.\n\nPastikan data yang dimasukkan sudah benar karena ini akan digunakan untuk login Anda selanjutnya. Apakah Anda yakin ingin menyimpannya?",
                        color = Color.LightGray,
                        lineHeight = 20.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showSecurityDialog = false
                            performSave()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF87171)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Ya, Saya Yakin", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSecurityDialog = false }) {
                        Text("Batal", color = Color.Gray)
                    }
                }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = Color(0xFF1E293B),
                contentColor = Color.White,
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}
