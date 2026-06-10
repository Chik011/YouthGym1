package com.chiko0085.testgym

import androidx.compose.runtime.*
import com.chiko0085.testgym.model.Admin
import com.chiko0085.testgym.model.GymPackage
import com.chiko0085.testgym.model.Member
import com.chiko0085.testgym.ui.screens.AdminDashboard
import com.chiko0085.testgym.ui.screens.LoginScreen
import com.chiko0085.testgym.ui.screens.MemberMainScreen
import androidx.compose.material3.MaterialTheme
import kotlinx.coroutines.launch
import dev.gitlive.firebase.storage.storage

@Composable
fun App() {
    MaterialTheme {
        // CoroutineScope untuk memanggil fungsi Firebase (suspend function) di dalam callback
        val scope = rememberCoroutineScope()

        var currentScreen by remember { mutableStateOf("login") }
        var loggedInMember by remember { mutableStateOf<Member?>(null) }
        var loggedInTrainer by remember { mutableStateOf<Trainer?>(null) }

        // Data global yang bersifat reactive
        val members = remember { mutableStateListOf<Member>() }
        val gymPackages = remember { mutableStateListOf<GymPackage>() }
        val trainers = remember { mutableStateListOf<Trainer>() }
        var totalRevenue by remember { mutableDoubleStateOf(0.0) }
        var adminAccount by remember { mutableStateOf(Admin()) }

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

                // 4. Ambil Trainer
                val dbTrainers = db.collection("trainers").get().documents.map { it.data<Trainer>() }
                trainers.clear()
                trainers.addAll(dbTrainers)

                // 5. Hitung revenue asli dari total bayar member
                totalRevenue = members.sumOf { it.pricePaid ?: 0.0 }

                // 6. Ambil Data Admin
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
                    if (role == "admin") {
                        currentScreen = "admin"
                    } else if (role == "member") {
                        loggedInMember = userObj as? Member
                        currentScreen = "member"
                    } else if (role == "trainer") {
                        loggedInTrainer = userObj as? Trainer
                        currentScreen = "trainer"
                    }
                },
                memberList = members,
                trainerList = trainers,
                adminAccount = adminAccount
            )
            "admin" -> AdminDashboard(
                members = members,
                gymPackages = gymPackages,
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
                onLogout = {
                    loggedInMember = null
                    currentScreen = "login"
                },
                // TAMBAHAN BARU: Callback untuk update data ke UI & Database
                onUpdateMember = { updatedMember ->
                    // 1. Update data user yang sedang login
                    loggedInMember = updatedMember
                    // 2. Update list global (agar Admin & UI lain langsung sinkron)
                    val index = members.indexOfFirst { it.id == updatedMember.id }
                    if (index != -1) {
                        members[index] = updatedMember
                    }
                }
            )
            "trainer" -> {
                // Gunakan state trainer terbaru dari list trainers utama agar reactive jika admin nambah jadwal
                val liveTrainer = trainers.find { it.id == loggedInTrainer?.id } ?: loggedInTrainer!!
                TrainerMainScreen(
                    trainer = liveTrainer,
                    onLogout = {
                        loggedInTrainer = null
                        currentScreen = "login"
                    },
                    onSaveProfile = { updatedTrainer ->
                        // Logika UPDATE ke Firebase menggunakan CoroutineScope
                        scope.launch {
                            try {
                                // Update ke database
                                db.collection("trainers").document(updatedTrainer.id).set(updatedTrainer)

                                // Update list lokal agar UI di seluruh aplikasi (seperti daftar admin) ikut sinkron
                                val index = trainers.indexOfFirst { it.id == updatedTrainer.id }
                                if (index != -1) {
                                    trainers[index] = updatedTrainer
                                }
                                println("DEBUG: Profil trainer berhasil disimpan ke Firebase")
                            } catch (e: Exception) {
                                println("DEBUG: Gagal menyimpan profil trainer: ${e.message}")
                            }
                        }
                    },
                    onUpdatePhotoClick = {
                        // TODO: Implementasi KMP Image Picker (contoh menggunakan library Peekaboo nantinya)
                        println("DEBUG: Buka galeri/kamera untuk ganti foto profil")
                    }
                )
            }
        }
    }
}