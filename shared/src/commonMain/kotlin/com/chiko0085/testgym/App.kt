package com.chiko0085.testgym

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.chiko0085.testgym.model.Admin
import com.chiko0085.testgym.model.GymPackage
import com.chiko0085.testgym.model.Member
import com.chiko0085.testgym.model.PtPackage
import com.chiko0085.testgym.ui.screens.AdminDashboard
import com.chiko0085.testgym.ui.screens.LoginScreen
import com.chiko0085.testgym.ui.screens.MemberMainScreen
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.storage.storage
import kotlinx.coroutines.launch

@Composable
fun App() {
    MaterialTheme {
        var currentScreen by remember { mutableStateOf("login") }
        var loggedInMember by remember { mutableStateOf<Member?>(null) }
        var loggedInTrainer by remember { mutableStateOf<Trainer?>(null) }

        // Data global yang bersifat reactive
        val members = remember { mutableStateListOf<Member>() }
        val gymPackages = remember { mutableStateListOf<GymPackage>() }
        val ptPackages = remember { mutableStateListOf<PtPackage>() }
        val trainers = remember { mutableStateListOf<Trainer>() }
        var totalRevenue by remember { mutableDoubleStateOf(0.0) }
        var adminAccount by remember { mutableStateOf(Admin()) }
        val scope = rememberCoroutineScope()

        // Inisialisasi Firebase & Sync Data dalam satu aliran agar tidak race condition
        LaunchedEffect(Unit) {
            try {
                // 1. Inisialisasi Firebase
                initFirebase()
                
                // 2. Ambil Paket
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
                
                // 3. Ambil Member
                val dbMembers = db.collection("members").get().documents.map { it.data<Member>() }
                members.clear()
                members.addAll(dbMembers)

                // 4. Ambil PT Packages
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

                // 5. Ambil Trainer
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
                
                // 6. Hitung revenue asli dari total bayar member
                totalRevenue = members.sumOf { it.pricePaid ?: 0.0 }

                // 7. Ambil Data Admin
                try {
                    val adminDoc = db.collection("settings").document("admin_account").get()
                    if (adminDoc.exists) {
                        adminAccount = adminDoc.data<Admin>()
                    } else {
                        // Jika belum ada di cloud, buat default
                        db.collection("settings").document("admin_account").set(Admin())
                    }
                } catch (e: Exception) {
                    println("DEBUG: Gagal ambil admin account: ${e.message}")
                }

                println("DEBUG: Sync Cloud berhasil. Revenue: $totalRevenue, Trainer: ${trainers.size}")
                
            } catch (e: Exception) {
                println("DEBUG: Gagal sync data dari cloud: ${e.message}")
                e.printStackTrace()
                // Fallback jika cloud gagal
                if (gymPackages.isEmpty()) {
                     gymPackages.addAll(listOf(
                        GymPackage("1", "Daily Pass", 25000.0, 1),
                        GymPackage("2", "Monthly Basic", 250000.0, 30),
                        GymPackage("3", "3 Months Pro", 650000.0, 90)
                    ))
                }
            }
        }

        when (currentScreen) {
            "login" -> LoginScreen(
                onLoginSuccess = { role, userObj ->
                    when (role) {
                        "admin" -> currentScreen = "admin"
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
                adminAccount = adminAccount
            )
            "admin" -> AdminDashboard(
                members = members,
                gymPackages = gymPackages,
                ptPackages = ptPackages,
                trainers = trainers,
                totalRevenue = totalRevenue,
                adminAccount = adminAccount,
                onUpdateAdmin = { adminAccount = it },
                onUpdateRevenue = { totalRevenue = it },
                onLogout = { currentScreen = "login" }
            )
            "member" -> MemberMainScreen(
                initialMember = loggedInMember!!,
                memberList = members,
                onLogout = { currentScreen = "login" },
                onUpdateMember = { updated ->
                    val index = members.indexOfFirst { it.id == updated.id }
                    if (index != -1) members[index] = updated
                    if (loggedInMember?.id == updated.id) loggedInMember = updated
                },
                onUpdatePhotoClick = { imageBytes ->
                    scope.launch {
                        try {
                            if (!isStorageSupported()) {
                                println("DEBUG: Firebase Storage tidak didukung di platform ini (Desktop).")
                                return@launch
                            }
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
                            println("DEBUG: Upload foto member berhasil! URL: $downloadUrl")

                        } catch (e: Exception) {
                            println("DEBUG: Gagal mengunggah foto member ke Storage: ${e.message}")
                            e.printStackTrace()
                        }
                    }
                }
            )
            "trainer" -> {
                // Gunakan state trainer terbaru dari list trainers utama agar reactive jika admin nambah jadwal
                val liveTrainer = trainers.find { it.id == loggedInTrainer?.id } ?: loggedInTrainer!!

                TrainerMainScreen(
                    trainer = liveTrainer,
                    memberList = members,
                    onLogout = { currentScreen = "login" },
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
                                if (!isStorageSupported()) {
                                    println("DEBUG: Firebase Storage tidak didukung di platform ini (Desktop).")
                                    return@launch
                                }
                                val storageRef = Firebase.storage.reference.child("trainers/${liveTrainer.id}/profile.jpg")
                                storageRef.putData(createStorageData(imageBytes))
                                val downloadUrl = storageRef.getDownloadUrl()
                                val updatedTrainer = liveTrainer.copy(profileImageUrl = downloadUrl)
                                val index = trainers.indexOfFirst { it.id == updatedTrainer.id }
                                if (index != -1) trainers[index] = updatedTrainer
                                if (loggedInTrainer?.id == updatedTrainer.id) loggedInTrainer = updatedTrainer
                                db.collection("trainers").document(updatedTrainer.id).set(updatedTrainer)
                                println("DEBUG: Upload foto berhasil! URL: $downloadUrl")

                            } catch (e: Exception) {
                                println("DEBUG: Gagal mengunggah foto ke Storage: ${e.message}")
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
                                println("DEBUG: Kouta PT ${updated.name} berhasil dikurangi. Sisa: ${updated.remainingPtSessions}")
                            } catch (e: Exception) {
                                println("DEBUG: Gagal update kouta member: ${e.message}")
                            }
                        }
                    }
                )
            }
        }
    }
}