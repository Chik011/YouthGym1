package com.chiko0085.testgym.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chiko0085.testgym.model.Trainer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import com.chiko0085.testgym.model.Member
import com.chiko0085.testgym.rememberImagePicker
import org.jetbrains.compose.resources.painterResource
import youthgym.shared.generated.resources.*
import com.chiko0085.testgym.ui.screens.trainer.dialogs.*

// Layar utama trainer
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainerMainScreen(
    trainer: Trainer,
    memberList: List<Member>,
    onLogout: () -> Unit,
    onSaveProfile: (Trainer) -> Unit,
    onUpdatePhotoClick: (ByteArray) -> Unit,
    onUpdateMember: (Member) -> Unit,
    onSaveWorkout: (com.chiko0085.testgym.model.WorkoutSession) -> Unit
) {
    val bgGradient = Brush.verticalGradient(listOf(Color(0xFF000000), Color(0xFF0A192F)))
    val scope = rememberCoroutineScope()

    var displayTrainer by remember(trainer) { mutableStateOf(trainer) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showAccountDialog by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }
    var showAttendanceDialog by remember { mutableStateOf(false) }
    var showWorkoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(trainer.profileImageUrl) {
        isUploading = false
    }

    val schedules = displayTrainer.schedules.ifEmpty {
        listOf("Belum ada jadwal melatih yang ditugaskan oleh admin.")
    }

    val launchImagePicker = rememberImagePicker(
        onResult = { imageBytes ->
            isUploading = true
            onUpdatePhotoClick(imageBytes)
            scope.launch {
                delay(15000)
                isUploading = false
            }
        }
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Trainer Workspace", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    TextButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = Color(0xFFEF4444))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Logout", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().background(bgGradient).padding(padding)) {
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {

                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF112240)),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {

                            Box(contentAlignment = Alignment.BottomEnd) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1E293B))
                                        .border(2.dp, Color(0xFF3B82F6), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isUploading) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                                    } else if (displayTrainer.profileImageUrl.isNotEmpty()) {
                                        if (displayTrainer.profileImageUrl.startsWith("res:")) {
                                            val resourceName = displayTrainer.profileImageUrl.removePrefix("res:")
                                            val painter = when {
                                                resourceName.contains("chiko") -> painterResource(Res.drawable.pt_chiko)
                                                resourceName.contains("gabriel") -> painterResource(Res.drawable.pt_gabriel)
                                                resourceName.contains("marchel") -> painterResource(Res.drawable.pt_marchel)
                                                else -> null
                                            }
                                            if (painter != null) {
                                                Image(
                                                    painter = painter,
                                                    contentDescription = "Profile Photo",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            } else {
                                                Icon(Icons.Default.Person, null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                                            }
                                        } else {
                                            KamelImage(
                                                resource = asyncPainterResource(data = displayTrainer.profileImageUrl),
                                                contentDescription = "Profile Photo",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    } else {
                                        Icon(Icons.Default.Person, null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                                    }
                                }

                                Surface(
                                    modifier = Modifier.size(28.dp).offset(x = 4.dp, y = 4.dp).clickable {
                                        launchImagePicker()
                                    },
                                    shape = CircleShape,
                                    color = Color(0xFF3B82F6),
                                    border = BorderStroke(2.dp, Color(0xFF112240))
                                ) {
                                    Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.padding(6.dp))
                                }
                            }

                            Spacer(modifier = Modifier.width(20.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text("Coach ${displayTrainer.name}", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(displayTrainer.description.ifEmpty { "Belum ada bio/portofolio yang ditulis." }, color = Color.LightGray, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 16.sp)
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(onClick = { showEditDialog = true }, modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape).size(36.dp)) {
                                    Icon(Icons.Default.Settings, contentDescription = "Edit Profile", tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                                IconButton(onClick = { showAccountDialog = true }, modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape).size(36.dp)) {
                                    Icon(Icons.Default.Lock, contentDescription = "Edit Akun", tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            TrainerStatItem(icon = Icons.Default.CheckCircle, title = "Spesialisasi", value = displayTrainer.specialization.ifEmpty { "-" }, color = Color(0xFF10B981))
                            TrainerStatItem(icon = Icons.Default.Face, title = "Pengalaman", value = "${displayTrainer.experience} Thn", color = Color(0xFF8B5CF6))
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { showAttendanceDialog = true }
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Absensi", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Member PT", color = Color.Gray, fontSize = 10.sp)
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { showWorkoutDialog = true }
                            ) {
                                Icon(Icons.Default.Build, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Latihan", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Input Sesi", color = Color.Gray, fontSize = 10.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
                Text("Agenda Latihan", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
                    items(schedules) { schedule ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFF3B82F6).copy(alpha = 0.2f)) {
                                    Icon(Icons.Default.DateRange, null, tint = Color(0xFF3B82F6), modifier = Modifier.padding(10.dp))
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text("Sesi Melatih", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(schedule, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showEditDialog) {
            EditProfileDialog(
                currentTrainer = displayTrainer,
                onDismiss = { showEditDialog = false },
                onSave = { updatedTrainer ->
                    onSaveProfile(updatedTrainer)
                    displayTrainer = updatedTrainer
                    showEditDialog = false
                }
            )
        }

        if (showAccountDialog) {
            AccountSettingsDialog(
                currentTrainer = displayTrainer,
                onDismiss = { showAccountDialog = false },
                onSave = { updatedTrainer ->
                    onSaveProfile(updatedTrainer)
                    displayTrainer = updatedTrainer
                    showAccountDialog = false
                }
            )
        }

        if (showAttendanceDialog) {
            AttendanceDialog(
                memberList = memberList,
                onDismiss = { showAttendanceDialog = false },
                onMarkAttendance = { member ->
                    val currentTime = com.chiko0085.testgym.getCurrentTimeMillis()
                    
                    // Cek jika sudah lewat sebulan (ptExpiredDate)
                    if (member.ptExpiredDate > 0 && currentTime > member.ptExpiredDate) {
                        // Paket HANGUS karena sudah sebulan
                        val updatedMember = member.copy(remainingPtSessions = 0)
                        onUpdateMember(updatedMember)
                    } else if (member.remainingPtSessions > 0) {
                        // Masih berlaku, kurangi 1 sesi
                        val updatedMember = member.copy(remainingPtSessions = member.remainingPtSessions - 1)
                        onUpdateMember(updatedMember)
                    }
                }
            )
        }

        if (showWorkoutDialog) {
            WorkoutManagementDialog(
                memberList = memberList,
                trainer = displayTrainer,
                onDismiss = { showWorkoutDialog = false },
                onSaveWorkout = { session ->
                    onSaveWorkout(session)
                    showWorkoutDialog = false
                }
            )
        }
    }
}



// Item statistik trainer
@Composable
fun TrainerStatItem(icon: ImageVector, title: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(6.dp))
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(title, color = Color.Gray, fontSize = 10.sp)
    }
}

