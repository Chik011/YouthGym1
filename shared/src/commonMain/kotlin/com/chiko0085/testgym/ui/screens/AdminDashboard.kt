// AdminDashboard.kt - Panel kendali admin gym

package com.chiko0085.testgym.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chiko0085.testgym.db
import dev.gitlive.firebase.firestore.*
import com.chiko0085.testgym.formatEpochToDate
import com.chiko0085.testgym.getCurrentTimeMillis
import com.chiko0085.testgym.model.*
import com.chiko0085.testgym.ui.theme.*
import com.chiko0085.testgym.util.*
import com.chiko0085.testgym.ui.components.*
import com.chiko0085.testgym.ui.screens.admin.*
import com.chiko0085.testgym.ui.screens.admin.dialogs.*
import com.chiko0085.testgym.exportToExcel
import kotlinx.coroutines.launch
import kotlin.random.Random
import com.chiko0085.testgym.model.Trainer
import com.chiko0085.testgym.model.Reservation
import org.jetbrains.compose.resources.painterResource
import youthgym.shared.generated.resources.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image

val DarkBgStart = Color(0xFF0F172A)
val DarkBgEnd = Color(0xFF1E293B)
val CardDark = Color(0xFF1E293B)
val AccentBlue = Color(0xFF3B82F6)

// Komponen dashboard admin utama
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    members: MutableList<Member>,
    gymPackages: SnapshotStateList<GymPackage>,
    ptPackages: SnapshotStateList<PtPackage>,
    trainers: SnapshotStateList<Trainer>,
    admins: SnapshotStateList<Admin>,
    workoutSessions: SnapshotStateList<com.chiko0085.testgym.model.WorkoutSession>,
    transactions: SnapshotStateList<com.chiko0085.testgym.model.Transaction>,
    totalRevenue: Double,
    loggedInAdmin: Admin,
    onUpdateAdmin: (Admin) -> Unit,
    onUpdateRevenue: (Double) -> Unit,
    onLogout: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf("dashboard") }

    var memberToEdit by remember { mutableStateOf<Member?>(null) }
    var memberToDelete by remember { mutableStateOf<Member?>(null) }
    var memberToCheckIn by remember { mutableStateOf<Member?>(null) }
    var memberToBuyPtPackage by remember { mutableStateOf<Member?>(null) }
    var trainerForSchedule by remember { mutableStateOf<Trainer?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }
    var snackbarHostState = remember { SnackbarHostState() }

    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val allNavItems = listOf(
        Triple("dashboard", "Home", Icons.Default.Home),
        Triple("members", "Member", Icons.Default.Person),
        Triple("packages", "Paket Gym", Icons.Default.ShoppingCart),
        Triple("pt_packages", "Paket PT", Icons.Default.Star),
        Triple("trainers", "Trainer", Icons.Default.Face),
        Triple("workouts", "Progres", Icons.Default.Info),
        Triple("reservations", "Padel", Icons.Default.DateRange),
        Triple("wa_broadcast", "Broadcast", Icons.Default.Share),
        Triple("revenue_detail", "Keuangan", Icons.AutoMirrored.Filled.List),
        Triple("logs", "History", Icons.Default.History),
        Triple("admins", "Admin", Icons.Default.Lock),
        Triple("profile", "Settings", Icons.Default.Settings)
    )

    val navItems = allNavItems.filter { item ->
        loggedInAdmin.role == "super_admin" || 
        loggedInAdmin.permissions.contains(item.first) || 
        item.first == "profile"
    }

    LaunchedEffect(navItems) {
        if (navItems.isNotEmpty() && navItems.none { it.first == currentScreen }) {
            currentScreen = navItems.first().first
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(DarkBgStart, DarkBgEnd)))) {
        val isDesktop = maxWidth > 800.dp

        if (isDesktop) {
            // Layout Side Navigation Desktop
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    containerColor = Color.Black.copy(alpha = 0.3f),
                    header = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 16.dp)) {
                            Icon(Icons.Default.AccountCircle, "Admin", tint = AccentBlue, modifier = Modifier.size(40.dp))
                            Text("Admin", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                ) {
                    navItems.forEach { (screen, label, icon) ->
                        NavigationRailItem(
                            selected = currentScreen == screen,
                            onClick = { currentScreen = screen },
                            icon = { Icon(icon, null) },
                            label = { Text(label, fontSize = 10.sp) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = AccentBlue,
                                unselectedIconColor = Color.LightGray,
                                selectedTextColor = AccentBlue,
                                unselectedTextColor = Color.LightGray,
                                indicatorColor = AccentBlue.copy(alpha = 0.1f)
                            )
                        )
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    NavigationRailItem(
                        selected = false,
                        onClick = onLogout,
                        icon = { Icon(Icons.Default.ExitToApp, null, tint = Color(0xFFF87171)) },
                        label = { Text("Logout", color = Color(0xFFF87171), fontSize = 10.sp) }
                    )
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    AdminContent(
                        currentScreen = currentScreen,
                        members = members,
                        gymPackages = gymPackages,
                        ptPackages = ptPackages,
                        trainers = trainers,
                        admins = admins,
                        workoutSessions = workoutSessions,
                        transactions = transactions,
                        totalRevenue = totalRevenue,
                        loggedInAdmin = loggedInAdmin,
                        onUpdateAdmin = onUpdateAdmin,
                        onUpdateRevenue = onUpdateRevenue,
                        onLogout = onLogout,
                        onNavigate = { currentScreen = it },
                        onCheckIn = { memberToCheckIn = it },
                        onEditMember = { memberToEdit = it },
                        onDeleteMember = { memberToDelete = it },
                        onAddMember = { showAddDialog = true },
                        onResetRevenue = { showResetDialog = true },
                        onTrainerSchedule = { trainerForSchedule = it },
                        snackbarHostState = snackbarHostState,
                        isDesktop = true
                    )
                }
            }
        } else {
            // Layout Navigation Drawer Mobile
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = CardDark,
                        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
                    ) {
                        Spacer(Modifier.height(24.dp))
                        Column(Modifier.padding(24.dp)) {
                            Text("Admin Panel", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Youth Gym Management", fontSize = 14.sp, color = Color.LightGray)
                        }
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                        
                        navItems.forEach { (screen, label, icon) ->
                            NavigationDrawerItem(
                                label = { Text(label) },
                                selected = currentScreen == screen,
                                onClick = {
                                    currentScreen = screen
                                    scope.launch { drawerState.close() }
                                },
                                icon = { Icon(icon, null) },
                                colors = NavigationDrawerItemDefaults.colors(
                                    unselectedContainerColor = Color.Transparent,
                                    selectedContainerColor = AccentBlue.copy(alpha = 0.1f),
                                    selectedTextColor = AccentBlue,
                                    unselectedTextColor = Color.LightGray,
                                    selectedIconColor = AccentBlue,
                                    unselectedIconColor = Color.LightGray
                                ),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.weight(1f))
                        NavigationDrawerItem(
                            label = { Text("Keluar (Logout)") },
                            selected = false,
                            onClick = onLogout,
                            icon = { Icon(Icons.Default.ExitToApp, null) },
                            colors = NavigationDrawerItemDefaults.colors(
                                unselectedTextColor = Color(0xFFF87171), 
                                unselectedIconColor = Color(0xFFF87171),
                                selectedTextColor = Color(0xFFF87171),
                                selectedIconColor = Color(0xFFF87171),
                                unselectedContainerColor = Color.Transparent,
                                selectedContainerColor = Color.Transparent
                            ),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            ) {
                Scaffold(
                    containerColor = Color.Transparent,
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = when(currentScreen) {
                                        "dashboard" -> "Admin Home"
                                        "members" -> "Member Gym"
                                        "packages" -> "Paket Gym"
                                        "pt_packages" -> "Paket PT"
                                        "trainers" -> "Personal Trainer"
                                        "reservations" -> "Jadwal Padel"
                                        "workouts" -> "Progres Latihan"
                                        "wa_broadcast" -> "WhatsApp Broadcast"
                                        "revenue_detail" -> "Detail Keuangan"
                                        "logs" -> "History Aktivitas"
                                        else -> "Profil Admin"
                                    },
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, "Menu", tint = Color.White)
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                        )
                    }
                ) { padding ->
                    Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                        AdminContent(
                            currentScreen = currentScreen,
                            members = members,
                            gymPackages = gymPackages,
                            ptPackages = ptPackages,
                            trainers = trainers,
                            admins = admins,
                            workoutSessions = workoutSessions,
                            transactions = transactions,
                            totalRevenue = totalRevenue,
                            loggedInAdmin = loggedInAdmin,
                            onUpdateAdmin = onUpdateAdmin,
                            onUpdateRevenue = onUpdateRevenue,
                            onLogout = onLogout,
                            onNavigate = { currentScreen = it },
                            onCheckIn = { memberToCheckIn = it },
                            onEditMember = { memberToEdit = it },
                            onDeleteMember = { memberToDelete = it },
                            onAddMember = { showAddDialog = true },
                            onResetRevenue = { showResetDialog = true },
                            onTrainerSchedule = { trainerForSchedule = it },
                            snackbarHostState = snackbarHostState,
                            isDesktop = false
                        )
                    }
                }
            }
        }
    }

    // Dialog reset pendapatan
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            containerColor = CardDark,
            title = { Text("Reset Pendapatan?", color = Color.White) },
            text = { Text("Tindakan ini akan menghapus histori pembayaran semua member di database. Member tetap ada, namun nilai pembayaran mereka akan menjadi 0. Lanjutkan?", color = Color.LightGray) },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                members.forEach { member ->
                                    val updated = member.copy(pricePaid = 0.0)
                                    db.collection("members").document(member.id).update(mapOf("pricePaid" to 0.0))
                                    val idx = members.indexOfFirst { it.id == member.id }
                                    if (idx != -1) members[idx] = updated
                                }
                                com.chiko0085.testgym.util.AdminLogger.log(loggedInAdmin.username, "Reset Pendapatan", "Mereset histori pembayaran semua member menjadi 0")
                                onUpdateRevenue(0.0)
                                showResetDialog = false
                                snackbarHostState.showSnackbar("Pendapatan berhasil direset ke 0")
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("Gagal reset: ${e.message}")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF87171))
                ) { Text("Ya, Reset", color = Color.White) }
            },
            dismissButton = { TextButton(onClick = { showResetDialog = false }) { Text("Batal", color = Color.Gray) } }
        )
    }

    // Dialog konfirmasi check-in
    if (memberToCheckIn != null) {
        AlertDialog(
            onDismissRequest = { memberToCheckIn = null },
            containerColor = CardDark,
            title = { Text("Konfirmasi Check-in", color = Color.White) },
            text = { Text("Apakah Anda yakin ingin melakukan check-in untuk ${memberToCheckIn?.name}? Sisa hari akan berkurang 1.", color = Color.LightGray) },
            confirmButton = {
                Button(
                    onClick = {
                        val member = memberToCheckIn!!
                        memberToCheckIn = null
                        scope.launch {
                            if (member.remainingDays > 0) {
                                val updated = member.copy(remainingDays = member.remainingDays - 1)
                                val idx = members.indexOfFirst { it.id == member.id }
                                if (idx != -1) members[idx] = updated
                                try {
                                    val data = mapOf("remainingDays" to updated.remainingDays)
                                    db.collection("members").document(updated.id).update(data)
                                    com.chiko0085.testgym.util.AdminLogger.log(loggedInAdmin.username, "Check-in Member", "Check-in member ${member.name} (Sisa hari: ${updated.remainingDays})")
                                    snackbarHostState.showSnackbar("Check-in berhasil untuk ${member.name}")
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("Gagal check-in: ${e.message}")
                                }
                            } else {
                                snackbarHostState.showSnackbar("Gagal check-in: Sisa hari sudah habis!")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) { Text("Ya, Check-in", color = Color.White) }
            },
            dismissButton = { TextButton(onClick = { memberToCheckIn = null }) { Text("Batal", color = Color.Gray) } }
        )
    }

    // Dialog tambah member baru
    if (showAddDialog) {
        AddMemberDialog(
            packages = gymPackages,
            onDismiss = { showAddDialog = false },
            onConfirm = { n, u, p, g, hp, d, pt, price, joinDate, pkgName ->
                scope.launch {
                    try {
                        val expirationTime = joinDate + (d * 86400000L)
                        val randomId = "CH-" + Random.nextInt(1000, 9999).toString()
                        val newMember = Member(
                            id = randomId,
                            name = n,
                            username = u,
                            password = p,
                            remainingDays = d,
                            joinDate = joinDate,
                            expiredDate = expirationTime,
                            weight = 0.0,
                            height = 0.0,
                            gender = g,
                            phoneNumber = hp,
                            email = "",
                            pricePaid = price,
                            packageName = pkgName,
                            remainingPtSessions = pt
                        )

                        members.add(newMember)
                        onUpdateRevenue(totalRevenue + price)
                        showAddDialog = false

                        val data = mapOf(
                            "id" to randomId,
                            "name" to n,
                            "username" to u,
                            "password" to p,
                            "email" to "",
                            "gender" to g,
                            "phoneNumber" to hp,
                            "remainingDays" to d,
                            "remainingPtSessions" to pt,
                            "joinDate" to joinDate,
                            "expiredDate" to expirationTime,
                            "weight" to 0.0,
                            "height" to 0.0,
                            "pricePaid" to price,
                            "packageName" to pkgName
                        )
                        db.collection("members").document(randomId).set(data)

                        // Catat Transaksi
                        val txId = "TX-${Random.nextInt(100000, 999999)}"
                        val tx = com.chiko0085.testgym.model.Transaction(txId, n, price, "Gym Package", "Pendaftaran Member Baru: $pkgName", joinDate)
                        db.collection("transactions").document(txId).set(tx)

                        com.chiko0085.testgym.util.AdminLogger.log(loggedInAdmin.username, "Tambah Member", "Menambahkan member baru $n dengan paket $pkgName")
                    } catch (ex: Exception) {}
                }
            }
        )
    }

    // Dialog edit member
    if (memberToEdit != null) {
        EditMemberDialog(
            member = memberToEdit!!,
            onDismiss = { memberToEdit = null },
            onConfirm = { updated ->
                scope.launch {
                    try {
                        // Hitung ulang expiredDate berdasarkan remainingDays yang baru diinput
                        val newExpiredDate = getCurrentTimeMillis() + (updated.remainingDays * 86400000L)
                        val finalMember = updated.copy(expiredDate = newExpiredDate)
                        
                        val idx = members.indexOfFirst { it.id == finalMember.id }
                        if (idx != -1) members[idx] = finalMember
                        memberToEdit = null

                        val data = mapOf(
                            "name" to finalMember.name,
                            "username" to finalMember.username,
                            "password" to finalMember.password,
                            "email" to "",
                            "phoneNumber" to finalMember.phoneNumber,
                            "remainingDays" to finalMember.remainingDays,
                            "remainingPtSessions" to finalMember.remainingPtSessions,
                            "joinDate" to finalMember.joinDate,
                            "expiredDate" to finalMember.expiredDate,
                            "weight" to finalMember.weight,
                            "height" to finalMember.height,
                            "gender" to finalMember.gender
                        )
                        db.collection("members").document(finalMember.id).update(data)
                        com.chiko0085.testgym.util.AdminLogger.log(loggedInAdmin.username, "Edit Member", "Memperbarui data member ${finalMember.name}")
                    } catch (e: Exception) {}
                }
            }
        )
    }

    // Dialog hapus member
    if (memberToDelete != null) {
        AlertDialog(
            onDismissRequest = { memberToDelete = null },
            containerColor = CardDark,
            title = { Text("Hapus Member", color = Color.White) },
            text = { Text("Yakin ingin menghapus ${memberToDelete?.name} dari Database?", color = Color.LightGray) },
            confirmButton = {
                val idToRemove = memberToDelete?.id ?: ""
                val nameToRemove = memberToDelete?.name ?: ""
                TextButton(onClick = {
                    scope.launch {
                        try {
                            members.removeAll { it.id == idToRemove }
                            db.collection("members").document(idToRemove).delete()
                            com.chiko0085.testgym.util.AdminLogger.log(loggedInAdmin.username, "Hapus Member", "Menghapus member $nameToRemove dari database")
                            memberToDelete = null
                        } catch (e: Exception) {}
                    }
                }) { Text("Hapus", color = Color.Red, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { memberToDelete = null }) { Text("Batal", color = Color.Gray) } }
        )
    }

    // Dialog jadwal trainer
    if (trainerForSchedule != null) {
        TrainerScheduleDialog(
            trainer = trainerForSchedule!!,
            trainers = trainers,
            members = members,
            onDismiss = { trainerForSchedule = null },
            onUpdateTrainer = { trainerForSchedule = it },
            snackbarHostState = snackbarHostState
        )
    }
}

// Router konten admin
@Composable
fun AdminContent(
    currentScreen: String,
    members: MutableList<Member>,
    gymPackages: SnapshotStateList<GymPackage>,
    ptPackages: SnapshotStateList<PtPackage>,
    trainers: SnapshotStateList<Trainer>,
    admins: SnapshotStateList<Admin>,
    workoutSessions: SnapshotStateList<com.chiko0085.testgym.model.WorkoutSession>,
    transactions: SnapshotStateList<com.chiko0085.testgym.model.Transaction>,
    totalRevenue: Double,
    loggedInAdmin: Admin,
    onUpdateAdmin: (Admin) -> Unit,
    onUpdateRevenue: (Double) -> Unit,
    onLogout: () -> Unit,
    onNavigate: (String) -> Unit,
    onCheckIn: (Member) -> Unit,
    onEditMember: (Member) -> Unit,
    onDeleteMember: (Member) -> Unit,
    onAddMember: () -> Unit,
    onResetRevenue: () -> Unit,
    onTrainerSchedule: (Trainer) -> Unit,
    snackbarHostState: SnackbarHostState,
    isDesktop: Boolean
) {
    when (currentScreen) {
        "members" -> MemberManagementScreen(
            members = members,
            gymPackages = gymPackages,
            ptPackages = ptPackages,
            admin = loggedInAdmin,
            onCheckIn = onCheckIn,
            onEdit = onEditMember,
            onDelete = onDeleteMember,
            onAddMember = onAddMember,
            onUpdateRevenue = onUpdateRevenue,
            totalRevenue = totalRevenue,
            onBack = { onNavigate("dashboard") }
        )
        "packages" -> PackageManagementScreen(gymPackages, onBack = { onNavigate("dashboard") })
        "pt_packages" -> PtPackageManagementScreen(ptPackages, onBack = { onNavigate("dashboard") })
        "trainers" -> TrainerManagementScreen(trainers, onBack = { onNavigate("dashboard") })
        "reservations" -> ReservationManagementScreen(onBack = { onNavigate("dashboard") })
        "workouts" -> WorkoutHistoryScreen(workoutSessions, onBack = { onNavigate("dashboard") })
        "wa_broadcast" -> WhatsAppBroadcastScreen(members, onBack = { onNavigate("dashboard") })
        "revenue_detail" -> RevenueDetailScreen(
            members = members,
            transactions = transactions,
            onBack = { onNavigate("dashboard") }
        )
        "logs" -> AdminLogScreen(onBack = { onNavigate("dashboard") })
        "admins" -> AdminManagementScreen(admins, onBack = { onNavigate("dashboard") })
        "profile" -> AdminProfileScreen(loggedInAdmin, onUpdateAdmin, onBack = { onNavigate("dashboard") })
        else -> AdminHomeScreen(
            members = members,
            trainers = trainers,
            transactions = transactions,
            admin = loggedInAdmin,
            totalRevenue = totalRevenue,
            onResetRevenue = onResetRevenue,
            onTrainerClick = onTrainerSchedule,
            onViewDetail = { onNavigate("revenue_detail") },
            snackbarHostState = snackbarHostState,
            isDesktop = isDesktop
        )
    }
}



// 