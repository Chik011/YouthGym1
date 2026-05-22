package com.chiko0085.testgym

// IMPORT YANG BENAR:

// Import khusus untuk Supabase
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.chiko0085.testgym.model.Member
import com.chiko0085.testgym.model.GymPackage
import com.chiko0085.testgym.ui.screens.AdminDashboard
import com.chiko0085.testgym.ui.screens.LoginScreen
import com.chiko0085.testgym.ui.screens.MemberMainScreen
import io.github.jan.supabase.postgrest.postgrest

@Composable
@Preview
fun App() {
    MaterialTheme {
        var currentScreen by remember { mutableStateOf("login") }
        var loggedInMemberId by remember { mutableStateOf<String?>(null) }
        var totalRevenue by remember { mutableDoubleStateOf(0.0) }

        // List ini sekarang kosong di awal, akan diisi dari Cloud!
        val memberList = remember { mutableStateListOf<Member>() }

        val gymPackages = remember {
            mutableStateListOf(
                GymPackage("1", "Paket Hemat", 100000.0, 30),
                GymPackage("2", "Paket Pro", 250000.0, 90)
            )
        }

        // --- MESIN PENYEDOT DATA SUPABASE ---
        LaunchedEffect(Unit) {
            try {
                // Mengambil semua data dari tabel "members"
                val membersFromDb = supabase.postgrest["members"].select().decodeList<Member>()

                // Masukkan data dari server ke list aplikasi
                memberList.clear()
                memberList.addAll(membersFromDb)

                println("INFO DATABASE: Sukses mengambil ${membersFromDb.size} member dari Supabase!")
            } catch (e: Exception) {
                println("INFO DATABASE: Gagal mengambil data. Error: ${e.message}")
            }
        }
        // ------------------------------------

        when (currentScreen) {
            "login" -> LoginScreen(
                onLoginSuccess = { role, member ->
                    if (role == "admin") {
                        currentScreen = "admin_dashboard"
                    } else if (member != null) {
                        loggedInMemberId = member.id
                        currentScreen = "member_main"
                    }
                },
                memberList = memberList
            )

            "admin_dashboard" -> AdminDashboard(
                members = memberList,
                gymPackages = gymPackages,
                totalRevenue = totalRevenue,
                onUpdateRevenue = { totalRevenue = it },
                onLogout = { currentScreen = "login" }
            )

            "member_main" -> {
                val member = memberList.find { it.id == loggedInMemberId }
                if (member != null) {
                    MemberMainScreen(
                        initialMember = member,
                        memberList = memberList,
                        onLogout = {
                            loggedInMemberId = null
                            currentScreen = "login"
                        }
                    )
                } else {
                    currentScreen = "login"
                }
            }
        }
    }
}