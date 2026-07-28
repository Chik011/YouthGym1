// App.kt - Titik masuk utama aplikasi Compose

package com.chiko0085.testgym

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.chiko0085.testgym.model.*
import com.chiko0085.testgym.ui.screens.AdminDashboard
import com.chiko0085.testgym.ui.screens.LoginScreen
import com.chiko0085.testgym.ui.screens.MemberMainScreen
import com.chiko0085.testgym.ui.screens.TrainerMainScreen
import com.chiko0085.testgym.util.AdminLogger
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.*
import dev.gitlive.firebase.storage.storage
import kotlinx.coroutines.launch

// Komponen utama aplikasi
@Composable
fun App() {
    MaterialTheme {
        var currentScreen by remember { mutableStateOf("login") }
        var loggedInMember by remember { mutableStateOf<Member?>(null) }
        var loggedInTrainer by remember { mutableStateOf<Trainer?>(null) }

        val members = remember { mutableStateListOf<Member>() }
        val gymPackages = remember { mutableStateListOf<GymPackage>() }
        val ptPackages = remember { mutableStateListOf<PtPackage>() }
        val trainers = remember { mutableStateListOf<Trainer>() }
        val admins = remember { mutableStateListOf<Admin>() }
        val workoutSessions = remember { mutableStateListOf<com.chiko0085.testgym.model.WorkoutSession>() }
        val transactions = remember { mutableStateListOf<com.chiko0085.testgym.model.Transaction>() }
        var totalRevenue by remember { mutableDoubleStateOf(0.0) }
        var loggedInAdmin by remember { mutableStateOf<Admin?>(null) }
        
        // --- SISTEM UPDATE OTOMATIS ---
        var showUpdateDialog by remember { mutableStateOf(false) }
        var latestVersionName by remember { mutableStateOf("") }
        val currentVersion = 1 // Update angka ini setiap kali Anda build versi baru (2, 3, dst)
        val downloadUrl = "https://drive.google.com/drive/folders/17ORzovSJAXRiHUel96c-az02xCgBiMR_?usp=sharing"
        
        val scope = rememberCoroutineScope()

        // Sync data dari Firebase
        LaunchedEffect(Unit) {
            try {
                initFirebase()

                // Cek Update Versi dari Firestore
                try {
                    val versionDoc = db.collection("settings").document("app_version").get()
                    if (versionDoc.exists) {
                        val remoteVersion = versionDoc.get<Long?>("version_code")?.toInt() ?: 1
                        latestVersionName = versionDoc.get<String?>("version_name") ?: "v1.0.0"
                        if (remoteVersion > currentVersion) {
                            showUpdateDialog = true
                        }
                    } else {
                        // Jika dokumen belum ada, buat default di Firestore
                        db.collection("settings").document("app_version").set(mapOf(
                            "version_code" to 1,
                            "version_name" to "v1.0.0"
                        ))
                    }
                } catch (e: Exception) {
                    println("DEBUG: Gagal cek update: ${e.message}")
                }
                
                val dbPackages = db.collection("gym_packages").get().documents.map { it.data<GymPackage>() }
                if (dbPackages.isNotEmpty()) {
                    gymPackages.clear()
                    gymPackages.addAll(dbPackages)
                } else {
                    gymPackages.addAll(listOf(
                        GymPackage("1", "Daily Pass", 25000.0, 1),
                        GymPackage("2", "Monthly Basic", 250000.0, 30),
                        GymPackage("3", "3 Months Pro", 650000.0, 90)
                    ))
                }
                
                val dbMembers = db.collection("members").get().documents.map { it.data<Member>() }
                members.clear()
                members.addAll(dbMembers)

                try {
                    val dbPtPackages = db.collection("pt_packages").get().documents.map { it.data<PtPackage>() }
                    if (dbPtPackages.isNotEmpty()) {
                        ptPackages.clear()
                        ptPackages.addAll(dbPtPackages)
                    } else {
                        ptPackages.addAll(listOf(
                            PtPackage("1", "Starter PT", 150000.0, 1),
                            PtPackage("2", "Basic PT", 600000.0, 5),
                            PtPackage("3", "Pro PT", 1000000.0, 10)
                        ))
                    }
                } catch (e: Exception) {
                    println("DEBUG: Gagal ambil pt_packages: ${e.message}")
                }

                val dbTrainers = db.collection("trainers").get().documents.map { it.data<Trainer>() }
                val updatedTrainers = dbTrainers.map { trainer ->
                    val nameLower = trainer.name.lowercase()
                    when {
                        nameLower.contains("chiko") -> if (trainer.profileImageUrl.isEmpty()) trainer.copy(profileImageUrl = "res:pt_chiko") else trainer
                        nameLower.contains("gabriel") -> if (trainer.profileImageUrl.isEmpty()) trainer.copy(profileImageUrl = "res:pt_gabriel") else trainer
                        nameLower.contains("marchel") -> if (trainer.profileImageUrl.isEmpty()) trainer.copy(profileImageUrl = "res:pt_marchel") else trainer
                        else -> trainer
                    }
                }
                trainers.clear()
                trainers.addAll(updatedTrainers)
                
                try {
                    val dbWorkouts = db.collection("workout_sessions").get().documents.map { it.data<com.chiko0085.testgym.model.WorkoutSession>() }
                    workoutSessions.clear()
                    workoutSessions.addAll(dbWorkouts)
                } catch (e: Exception) {
                    println("DEBUG: Gagal ambil workout_sessions: ${e.message}")
                }
                
                try {
                    val dbTransactions = db.collection("transactions").get().documents.map { it.data<com.chiko0085.testgym.model.Transaction>() }
                    transactions.clear()
                    transactions.addAll(dbTransactions)
                } catch (e: Exception) {
                    println("DEBUG: Gagal ambil transactions: ${e.message}")
                }
                
                totalRevenue = members.sumOf { it.pricePaid ?: 0.0 }

                try {
                    val dbAdmins = db.collection("admins").get().documents.map { it.data<Admin>() }
                    admins.clear()
                    if (dbAdmins.isEmpty()) {
                        val fullPermissions = listOf(
                            "dashboard", "revenue_view", "revenue_reset", "members", "members_add", "members_checkin", 
                            "members_edit", "members_delete", "members_extend", "members_pt", 
                            "packages", "pt_packages", "trainers", "workouts", "reservations", "wa_broadcast"
                        )
                        val superAdmin = Admin("sadmin", "sadmin123", "super_admin", fullPermissions)
                        val defaultAdmin = Admin("admin", "admin123", "admin", fullPermissions)
                        admins.addAll(listOf(superAdmin, defaultAdmin))
                        db.collection("admins").document("sadmin").set(superAdmin)
                        db.collection("admins").document("admin").set(defaultAdmin)
                    } else {
                        admins.addAll(dbAdmins)
                    }
                } catch (e: Exception) {
                    println("DEBUG: Gagal ambil admins: ${e.message}")
                }

                // Auto-login dari sesi tersimpan jika belum logout
                val savedRole = getSetting("session_role")
                val savedUserId = getSetting("session_user_id")
                if (!savedRole.isNullOrEmpty() && !savedUserId.isNullOrEmpty()) {
                    when (savedRole) {
                        "admin", "super_admin" -> {
                            val admin = admins.find { it.username == savedUserId }
                            if (admin != null) {
                                loggedInAdmin = admin
                                currentScreen = "admin"
                            }
                        }
                        "member" -> {
                            val member = members.find { it.id == savedUserId || it.username == savedUserId }
                            if (member != null) {
                                loggedInMember = member
                                currentScreen = "member"
                            }
                        }
                        "trainer" -> {
                            val trainer = trainers.find { it.id == savedUserId || it.username == savedUserId }
                            if (trainer != null) {
                                loggedInTrainer = trainer
                                currentScreen = "trainer"
                            }
                        }
                    }
                }
                
            } catch (e: Exception) {
                e.printStackTrace()
                if (gymPackages.isEmpty()) {
                     gymPackages.addAll(listOf(
                        GymPackage("1", "Daily Pass", 25000.0, 1),
                        GymPackage("2", "Monthly Basic", 250000.0, 30),
                        GymPackage("3", "3 Months Pro", 650000.0, 90)
                    ))
                }
            }
        }

        val performLogout = {
            clearSetting("session_role")
            clearSetting("session_user_id")
            clearSetting("draft_password")
            loggedInAdmin = null
            loggedInMember = null
            loggedInTrainer = null
            currentScreen = "login"
        }

        // Navigasi layar
        when (currentScreen) {
            "login" -> LoginScreen(
                onLoginSuccess = { role, userObj ->
                    when (role) {
                        "admin", "super_admin" -> {
                            loggedInAdmin = userObj as? Admin
                            currentScreen = "admin"
                        }
                        "member" -> {
                            val member = userObj as? Member
                            if (member != null) {
                                loggedInMember = member
                                currentScreen = "member"
                            }
                        }
                        "trainer" -> {
                            val trainer = userObj as? Trainer
                            if (trainer != null) {
                                loggedInTrainer = trainer
                                currentScreen = "trainer"
                            }
                        }
                    }
                },
                memberList = members,
                trainerList = trainers,
                adminList = admins
            )
            "admin" -> AdminDashboard(
                members = members,
                gymPackages = gymPackages,
                ptPackages = ptPackages,
                trainers = trainers,
                admins = admins,
                workoutSessions = workoutSessions,
                transactions = transactions,
                totalRevenue = totalRevenue,
                loggedInAdmin = loggedInAdmin!!,
                onUpdateAdmin = { updated ->
                    val idx = admins.indexOfFirst { it.username == updated.username }
                    if (idx != -1) admins[idx] = updated
                    if (loggedInAdmin?.username == updated.username) loggedInAdmin = updated
                },
                onUpdateRevenue = { totalRevenue = it },
                onLogout = performLogout
            )
            "member" -> MemberMainScreen(
                initialMember = loggedInMember!!,
                onLogout = performLogout,
                onUpdateMember = { updated ->
                    val index = members.indexOfFirst { it.id == updated.id }
                    if (index != -1) members[index] = updated
                    if (loggedInMember?.id == updated.id) loggedInMember = updated
                },
                onUpdatePhotoClick = { imageBytes ->
                    scope.launch {
                        try {
                            if (!isStorageSupported()) return@launch
                            val memberId = loggedInMember?.id ?: return@launch
                            val storageRef = Firebase.storage.reference.child("members/$memberId/profile.jpg")
                            storageRef.putData(createStorageData(imageBytes))
                            val downloadUrl = storageRef.getDownloadUrl()
                            
                            val currentM = members.find { it.id == memberId } ?: return@launch
                            val updatedMember = currentM.copy(profileImageUrl = downloadUrl)
                            
                            val index = members.indexOfFirst { it.id == updatedMember.id }
                            if (index != -1) members[index] = updatedMember
                            if (loggedInMember?.id == updatedMember.id) loggedInMember = updatedMember
                            
                            db.collection("members").document(updatedMember.id).set(updatedMember)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            )
            "trainer" -> {
                val liveTrainer = trainers.find { it.id == loggedInTrainer?.id } ?: loggedInTrainer!!

                TrainerMainScreen(
                    trainer = liveTrainer,
                    memberList = members,
                    onLogout = performLogout,
                    onSaveProfile = { updated ->
                        val index = trainers.indexOfFirst { it.id == updated.id }
                        if (index != -1) trainers[index] = updated
                        if (loggedInTrainer?.id == updated.id) loggedInTrainer = updated
                        scope.launch {
                            try {
                                db.collection("trainers").document(updated.id).set(updated)
                            } catch (e: Exception) {
                                println("DEBUG: Gagal update trainer: ${e.message}")
                            }
                        }
                    },
                    onUpdatePhotoClick = { imageBytes ->
                        scope.launch {
                            try {
                                if (!isStorageSupported()) return@launch
                                val storageRef =
                                    Firebase.storage.reference.child("trainers/${liveTrainer.id}/profile.jpg")
                                storageRef.putData(createStorageData(imageBytes))
                                val downloadUrl = storageRef.getDownloadUrl()
                                val updatedTrainer = liveTrainer.copy(profileImageUrl = downloadUrl)
                                val index = trainers.indexOfFirst { it.id == updatedTrainer.id }
                                if (index != -1) trainers[index] = updatedTrainer
                                if (loggedInTrainer?.id == updatedTrainer.id) loggedInTrainer =
                                    updatedTrainer
                                db.collection("trainers").document(updatedTrainer.id)
                                    .set(updatedTrainer)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    },
                    onUpdateMember = { updated ->
                        val index = members.indexOfFirst { it.id == updated.id }
                        if (index != -1) members[index] = updated
                        scope.launch {
                            try {
                                db.collection("members").document(updated.id).set(updated)
                            } catch (e: Exception) {
                                println("DEBUG: Gagal update kouta member: ${e.message}")
                            }
                        }
                    },
                    onSaveWorkout = { session ->
                        scope.launch {
                            try {
                                db.collection("workout_sessions").document(session.id).set(session)
                            } catch (e: Exception) {
                                println("DEBUG: Gagal simpan sesi latihan: ${e.message}")
                            }
                        }
                    }
                )
            }
        }

        // --- DIALOG UPDATE FORCE ---
        if (showUpdateDialog) {
            AlertDialog(
                onDismissRequest = { /* Force update tidak bisa di-dismiss */ },
                containerColor = Color(0xFF112240),
                title = { 
                    Text(
                        "Update Tersedia ($latestVersionName)", 
                        color = Color.White, 
                        fontWeight = FontWeight.Bold 
                    ) 
                },
                text = { 
                    Text(
                        "Versi aplikasi Anda sudah terlalu lama. Silakan unduh versi terbaru untuk tetap dapat menggunakan aplikasi.",
                        color = Color.LightGray
                    ) 
                },
                confirmButton = {
                    Button(
                        onClick = { openWebLink(downloadUrl) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3B82F6)
                        )
                    ) {
                        Text("Download Sekarang", color = Color.White)
                    }
                }
            )
        }
    }
}
