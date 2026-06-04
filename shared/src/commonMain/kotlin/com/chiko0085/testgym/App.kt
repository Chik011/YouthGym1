package com.chiko0085.testgym

import androidx.compose.runtime.*
import com.chiko0085.testgym.model.GymPackage
import com.chiko0085.testgym.model.Member
import com.chiko0085.testgym.ui.screens.AdminDashboard
import com.chiko0085.testgym.ui.screens.LoginScreen
import com.chiko0085.testgym.ui.screens.MemberMainScreen
import com.chiko0085.testgym.TrainerMainScreen
import androidx.compose.material3.MaterialTheme

@Composable
fun App() {
    // Inisialisasi Firebase sesuai platform
    LaunchedEffect(Unit) {
        initFirebase()
    }

    MaterialTheme {
        var currentScreen by remember { mutableStateOf("login") }
        var loggedInMember by remember { mutableStateOf<Member?>(null) }
        var loggedInTrainer by remember { mutableStateOf<Trainer?>(null) }

        // Data global yang bersifat reactive
        val members = remember { mutableStateListOf<Member>() }
        val gymPackages = remember { mutableStateListOf<GymPackage>() }
        val trainers = remember { mutableStateListOf<Trainer>() }
        var totalRevenue by remember { mutableDoubleStateOf(0.0) }

        // Sync Data dari Cloud saat Startup
        LaunchedEffect(Unit) {
            try {
                // Ambil Paket
                val dbPackages = db.collection("gym_packages").get().documents.map { it.data<GymPackage>() }
                if (dbPackages.isNotEmpty()) {
                    gymPackages.clear()
                    gymPackages.addAll(dbPackages)
                } else {
                    // Default fallback jika cloud kosong
                    gymPackages.addAll(listOf(
                        GymPackage("1", "Daily Pass", 25000.0, 1),
                        GymPackage("2", "Monthly Basic", 250000.0, 30),
                        GymPackage("3", "3 Months Pro", 650000.0, 90)
                    ))
                }
                
                // Ambil Member
                val dbMembers = db.collection("members").get().documents.map { it.data<Member>() }
                members.clear()
                members.addAll(dbMembers)

                // Ambil Trainer
                val dbTrainers = db.collection("trainers").get().documents.map { it.data<Trainer>() }
                trainers.clear()
                trainers.addAll(dbTrainers)
                
                // Hitung revenue simulasi (Misal: per member 250k)
                totalRevenue = members.size * 250000.0
                
            } catch (e: Exception) {
                println("DEBUG: Gagal sync data dari cloud: ${e.message}")
                // Jika cloud gagal (khususnya Desktop), list lokal tetap bisa digunakan
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
                trainerList = trainers
            )
            "admin" -> AdminDashboard(
                members = members,
                gymPackages = gymPackages,
                totalRevenue = totalRevenue,
                onUpdateRevenue = { totalRevenue = it },
                onLogout = { currentScreen = "login" }
            )
            "member" -> MemberMainScreen(
                initialMember = loggedInMember!!,
                memberList = members,
                onLogout = { currentScreen = "login" }
            )
            "trainer" -> TrainerMainScreen(
                trainer = loggedInTrainer!!,
                onLogout = { currentScreen = "login" }
            )
        }
    }
}