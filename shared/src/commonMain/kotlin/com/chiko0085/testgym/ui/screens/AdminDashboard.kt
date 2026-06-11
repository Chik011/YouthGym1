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
import com.chiko0085.testgym.exportToExcel
import kotlinx.coroutines.launch
import kotlin.random.Random
import com.chiko0085.testgym.Trainer
import com.chiko0085.testgym.model.Reservation
import org.jetbrains.compose.resources.painterResource
import youthgym.shared.generated.resources.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image

// Primary Colors dari Theme
val DarkBgStart = Color(0xFF0F172A)
val DarkBgEnd = Color(0xFF1E293B)
val CardDark = Color(0xFF1E293B)
val AccentBlue = Color(0xFF3B82F6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    members: MutableList<Member>,
    gymPackages: SnapshotStateList<GymPackage>,
    ptPackages: SnapshotStateList<PtPackage>,
    trainers: SnapshotStateList<Trainer>,
    totalRevenue: Double,
    adminAccount: Admin,
    onUpdateAdmin: (Admin) -> Unit,
    onUpdateRevenue: (Double) -> Unit,
    onLogout: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf("dashboard") }

    // --- STATE UNTUK EDIT & HAPUS ---
    var memberToEdit by remember { mutableStateOf<Member?>(null) }
    var memberToDelete by remember { mutableStateOf<Member?>(null) }
    var memberToCheckIn by remember { mutableStateOf<Member?>(null) }
    var memberToBuyPtPackage by remember { mutableStateOf<Member?>(null) }
    var trainerForSchedule by remember { mutableStateOf<Trainer?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }
    var snackbarHostState = remember { SnackbarHostState() }

    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(DarkBgStart, DarkBgEnd)))) {
        val isDesktop = maxWidth > 800.dp

        if (isDesktop) {
            // --- LAYOUT DESKTOP (Side Navigation) ---
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
                    val navItems = listOf(
                        Triple("dashboard", "Home", Icons.Default.Home),
                        Triple("members", "Member", Icons.Default.Person),
                        Triple("packages", "Paket Gym", Icons.Default.ShoppingCart),
                        Triple("pt_packages", "Paket PT", Icons.Default.Star),
                        Triple("trainers", "Trainer", Icons.Default.Face),
                        Triple("reservations", "Padel", Icons.Default.DateRange),
                        Triple("profile", "Settings", Icons.Default.Settings)
                    )

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
                        totalRevenue = totalRevenue,
                        adminAccount = adminAccount,
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
                        
                        val navItems = listOf(
                            Triple("dashboard", "Dashboard Home", Icons.Default.Home),
                            Triple("members", "Kelola Member", Icons.Default.Person),
                            Triple("packages", "Harga Paket Gym", Icons.Default.ShoppingCart),
                            Triple("pt_packages", "Harga Paket PT", Icons.Default.Star),
                            Triple("trainers", "Daftar Trainer", Icons.Default.Face),
                            Triple("reservations", "Jadwal Padel", Icons.Default.DateRange),
                            Triple("profile", "Pengaturan Akun", Icons.Default.Settings)
                        )

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
                            totalRevenue = totalRevenue,
                            adminAccount = adminAccount,
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

    // --- DIALOGS (Tetap sama) ---
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

    if (showAddDialog) {
        AddMemberDialog(
            packages = gymPackages,
            onDismiss = { showAddDialog = false },
            onConfirm = { n, u, p, e, g, hp, d, pt, price, joinDate, pkgName ->
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
                            email = e,
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
                            "email" to e,
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
                    } catch (ex: Exception) {}
                }
            }
        )
    }

    if (memberToEdit != null) {
        EditMemberDialog(
            member = memberToEdit!!,
            onDismiss = { memberToEdit = null },
            onConfirm = { updated ->
                scope.launch {
                    try {
                        val idx = members.indexOfFirst { it.id == updated.id }
                        if (idx != -1) members[idx] = updated
                        memberToEdit = null

                        val data = mapOf(
                            "name" to updated.name,
                            "username" to updated.username,
                            "password" to updated.password,
                            "email" to updated.email,
                            "phoneNumber" to updated.phoneNumber,
                            "remainingDays" to updated.remainingDays,
                            "remainingPtSessions" to updated.remainingPtSessions,
                            "joinDate" to updated.joinDate,
                            "expiredDate" to updated.expiredDate,
                            "weight" to updated.weight,
                            "height" to updated.height,
                            "gender" to updated.gender
                        )
                        db.collection("members").document(updated.id).update(data)
                    } catch (e: Exception) {}
                }
            }
        )
    }

    if (memberToDelete != null) {
        AlertDialog(
            onDismissRequest = { memberToDelete = null },
            containerColor = CardDark,
            title = { Text("Hapus Member", color = Color.White) },
            text = { Text("Yakin ingin menghapus ${memberToDelete?.name} dari Database?", color = Color.LightGray) },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        try {
                            val idToRemove = memberToDelete?.id!!
                            members.removeAll { it.id == idToRemove }
                            memberToDelete = null
                            db.collection("members").document(idToRemove).delete()
                        } catch (e: Exception) {}
                    }
                }) { Text("Hapus", color = Color.Red, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { memberToDelete = null }) { Text("Batal", color = Color.Gray) } }
        )
    }

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

/**
 * Komponen Konten Admin yang bisa berganti layar
 */
@Composable
fun AdminContent(
    currentScreen: String,
    members: MutableList<Member>,
    gymPackages: SnapshotStateList<GymPackage>,
    ptPackages: SnapshotStateList<PtPackage>,
    trainers: SnapshotStateList<Trainer>,
    totalRevenue: Double,
    adminAccount: Admin,
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
        "profile" -> AdminProfileScreen(adminAccount, onUpdateAdmin, onBack = { onNavigate("dashboard") })
        else -> AdminHomeScreen(
            members = members,
            trainers = trainers,
            totalRevenue = totalRevenue,
            onResetRevenue = onResetRevenue,
            onTrainerClick = onTrainerSchedule,
            snackbarHostState = snackbarHostState,
            isDesktop = isDesktop
        )
    }
}

@Composable
fun AdminHomeScreen(
    members: List<Member>,
    trainers: SnapshotStateList<Trainer>,
    totalRevenue: Double,
    onResetRevenue: () -> Unit,
    onTrainerClick: (Trainer) -> Unit,
    snackbarHostState: SnackbarHostState,
    isDesktop: Boolean
) {
    var filterExpanded by remember { mutableStateOf(false) }
    var selectedFilterText by remember { mutableStateOf("Semua Waktu") }
    val filterOptions = listOf(
        "Semua Waktu", "1 Bulan Terakhir", "2 Bulan Terakhir", "3 Bulan Terakhir",
        "4 Bulan Terakhir", "5 Bulan Terakhir", "6 Bulan Terakhir", "7 Bulan Terakhir",
        "8 Bulan Terakhir", "9 Bulan Terakhir", "10 Bulan Terakhir", "11 Bulan Terakhir", "1 Tahun Terakhir"
    )
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = if(isDesktop) 32.dp else 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            // Card Ringkasan
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = AccentBlue.copy(alpha = 0.9f))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountCircle, null, tint = Color.White, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Total Revenue", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                    }
                    Text("Rp ${formatRupiah(totalRevenue)}", color = Color.White, fontSize = if(isDesktop) 42.sp else 28.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${members.size} Total Members Aktif", color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
                        TextButton(onClick = onResetRevenue) {
                            Icon(Icons.Default.Refresh, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset Pendapatan", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                        }
                    }

                    // --- KODE FILTER DROPDOWN INTERAKTIF ---
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Rentang Filter Excel:", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                        Box {
                            TextButton(
                                onClick = { filterExpanded = true },
                                colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.background(Color.White.copy(alpha = 0.15f))
                            ) {
                                Text(selectedFilterText, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp))
                            }
                            DropdownMenu(
                                expanded = filterExpanded,
                                onDismissRequest = { filterExpanded = false }
                            ) {
                                filterOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            selectedFilterText = option
                                            filterExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val nowTime = getCurrentTimeMillis()
                            val filteredForExcel = when (selectedFilterText) {
                                "1 Bulan Terakhir" -> members.filter { it.joinDate >= nowTime - (1 * 30 * 86400000L) }
                                "2 Bulan Terakhir" -> members.filter { it.joinDate >= nowTime - (2 * 30 * 86400000L) }
                                "3 Bulan Terakhir" -> members.filter { it.joinDate >= nowTime - (3 * 30 * 86400000L) }
                                "4 Bulan Terakhir" -> members.filter { it.joinDate >= nowTime - (4 * 30 * 86400000L) }
                                "5 Bulan Terakhir" -> members.filter { it.joinDate >= nowTime - (5 * 30 * 86400000L) }
                                "6 Bulan Terakhir" -> members.filter { it.joinDate >= nowTime - (6 * 30 * 86400000L) }
                                "7 Bulan Terakhir" -> members.filter { it.joinDate >= nowTime - (7 * 30 * 86400000L) }
                                "8 Bulan Terakhir" -> members.filter { it.joinDate >= nowTime - (8 * 30 * 86400000L) }
                                "9 Bulan Terakhir" -> members.filter { it.joinDate >= nowTime - (9 * 30 * 86400000L) }
                                "10 Bulan Terakhir" -> members.filter { it.joinDate >= nowTime - (10 * 30 * 86400000L) }
                                "11 Bulan Terakhir" -> members.filter { it.joinDate >= nowTime - (11 * 30 * 86400000L) }
                                "1 Tahun Terakhir" -> members.filter { it.joinDate >= nowTime - (365 * 86400000L) }
                                else -> members
                            }

                            val html = generateRevenueHtml(filteredForExcel, selectedFilterText)
                            exportToExcel(html)
                            scope.launch {
                                snackbarHostState.showSnackbar("Laporan ($selectedFilterText) berhasil diunduh")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = AccentBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Unduh Laporan Excel (CSV)", color = AccentBlue, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item { CalendarCard() }

        item {
            Text("Personal Trainer Aktif", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AccentBlue, modifier = Modifier.padding(vertical = 8.dp))
            TrainerListRow(trainers, onTrainerClick = onTrainerClick)
        }

        item { Spacer(modifier = Modifier.height(if(isDesktop) 32.dp else 80.dp)) }
    }
}

@Composable
fun TrainerScheduleDialog(
    trainer: Trainer,
    trainers: SnapshotStateList<Trainer>,
    members: List<Member>,
    onDismiss: () -> Unit,
    onUpdateTrainer: (Trainer) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var dayText by remember { mutableStateOf("") }
    var timeText by remember { mutableStateOf("") }
    var selectedMemberName by remember { mutableStateOf("Pilih Member") }
    var memberDropdownExpanded by remember { mutableStateOf(false) }
    var dayDropdownExpanded by remember { mutableStateOf(false) }
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()

    val availableDays = trainer.availability

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardDark,
        title = { Text("Kelola Jadwal: Coach ${trainer.name}", color = Color.White) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                Text("Daftar Jadwal:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))

                Box(modifier = Modifier.heightIn(max = 200.dp)) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val currentSchedules = trainer.schedules
                        itemsIndexed(currentSchedules) { index, schedule ->
                            Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)), shape = RoundedCornerShape(8.dp)) {
                                Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(schedule, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                                    Row {
                                        IconButton(onClick = {
                                            try {
                                                val parts = schedule.split(", ", " - ")
                                                if(parts.size >= 3) {
                                                    dayText = parts[0]
                                                    timeText = parts[1]
                                                    selectedMemberName = parts[2].replace("Melatih ", "")
                                                } else {
                                                    dayText = schedule
                                                }
                                            } catch(e: Exception) { dayText = schedule }
                                            editingIndex = index
                                        }, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Edit, null, tint = AccentBlue, modifier = Modifier.size(16.dp)) }
                                        IconButton(onClick = {
                                            val newList = trainer.schedules.toMutableList()
                                            newList.removeAt(index)
                                            val updated = trainer.copy(schedules = newList)
                                            scope.launch {
                                                db.collection("trainers").document(trainer.id).update(mapOf("schedules" to newList))
                                                val idx = trainers.indexOfFirst { it.id == trainer.id }
                                                if (idx != -1) trainers[idx] = updated
                                                onUpdateTrainer(updated)
                                            }
                                        }, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Delete, null, tint = Color(0xFFF87171), modifier = Modifier.size(16.dp)) }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(16.dp))

                Text("Form Jadwal Baru:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AccentBlue)
                Spacer(modifier = Modifier.height(8.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = dayText, onValueChange = {}, label = { Text("Pilih Hari") }, readOnly = true, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { IconButton(onClick = { dayDropdownExpanded = true }) { Icon(Icons.Default.ArrowDropDown, null, tint = Color.White) } },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = AccentBlue, unfocusedBorderColor = Color.Gray)
                    )
                    DropdownMenu(expanded = dayDropdownExpanded, onDismissRequest = { dayDropdownExpanded = false }, modifier = Modifier.background(CardDark)) {
                        listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu").forEach { day ->
                            val isAvailable = availableDays.contains(day)
                            DropdownMenuItem(
                                text = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(day, color = if(isAvailable) Color.White else Color.Gray)
                                        if(!isAvailable) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("(PT Tidak Tersedia)", color = Color.Red, fontSize = 10.sp)
                                        }
                                    }
                                }, 
                                onClick = { 
                                    if(isAvailable) {
                                        dayText = day
                                        dayDropdownExpanded = false 
                                    }
                                },
                                enabled = isAvailable
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = timeText, onValueChange = { timeText = it }, label = { Text("Jam (Cth: 10:00 WIB)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = AccentBlue, unfocusedBorderColor = Color.Gray))
                Spacer(modifier = Modifier.height(8.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedMemberName, onValueChange = {}, label = { Text("Pilih Member") }, readOnly = true, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { IconButton(onClick = { memberDropdownExpanded = true }) { Icon(Icons.Default.ArrowDropDown, null, tint = Color.White) } },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = AccentBlue, unfocusedBorderColor = Color.Gray)
                    )
                    DropdownMenu(expanded = memberDropdownExpanded, onDismissRequest = { memberDropdownExpanded = false }, modifier = Modifier.background(CardDark)) {
                        members.forEach { m -> DropdownMenuItem(text = { Text(m.name, color = Color.White) }, onClick = { selectedMemberName = m.name; memberDropdownExpanded = false }) }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (dayText.isNotBlank() && timeText.isNotBlank() && selectedMemberName != "Pilih Member") {
                        val newList = trainer.schedules.toMutableList()
                        val formattedSchedule = "$dayText, $timeText - Melatih $selectedMemberName"

                        if (editingIndex != null) { newList[editingIndex!!] = formattedSchedule } else { newList.add(formattedSchedule) }

                        val updated = trainer.copy(schedules = newList)
                        scope.launch {
                            try {
                                db.collection("trainers").document(trainer.id).update(mapOf("schedules" to newList))
                                val idx = trainers.indexOfFirst { it.id == trainer.id }
                                if (idx != -1) trainers[idx] = updated
                                dayText = ""; timeText = ""; selectedMemberName = "Pilih Member"
                                editingIndex = null
                                onUpdateTrainer(updated)
                                snackbarHostState.showSnackbar("Berhasil diperbarui!")
                            } catch (e: Exception) { snackbarHostState.showSnackbar("Gagal: ${e.message}") }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) { Text(if (editingIndex != null) "Update" else "Tambah", color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Tutup", color = Color.Gray) } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackageManagementScreen(packages: SnapshotStateList<GymPackage>, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()

    var memberToBuyPtPackage by remember { mutableStateOf<Member?>(null) }
    var packageToEdit by remember { mutableStateOf<GymPackage?>(null) }
    var showAddPackage by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(DarkBgStart, DarkBgEnd)))) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { TopAppBar(title = { Text("Manajemen Paket Harga", color = Color.White) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent), navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) } }) },
            floatingActionButton = { FloatingActionButton(onClick = { showAddPackage = true }, containerColor = AccentBlue) { Icon(Icons.Default.Add, null, tint = Color.White) } }
        ) { padding ->
            LazyColumn(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(packages) { pkg ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardDark)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(pkg.name, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color.White)
                                Text("Harga: Rp ${formatRupiah(pkg.price ?: 0.0)}", color = AccentBlue, fontWeight = FontWeight.Bold)
                                Text("Durasi: ${pkg.durationDays} Hari", fontSize = 14.sp, color = Color.LightGray)
                            }
                            IconButton(onClick = { packageToEdit = pkg }) { Icon(Icons.Default.Edit, null, tint = Color.LightGray) }
                            IconButton(onClick = {
                                scope.launch {
                                    try {
                                        val idToRemove = pkg.id
                                        packages.remove(pkg)
                                        db.collection("gym_packages").document(idToRemove).delete()
                                    } catch (e: Exception) {}
                                }
                            }) { Icon(Icons.Default.Delete, null, tint = Color(0xFFF87171)) }
                        }
                    }
                }
            }
        }
    }

    if (showAddPackage) {
        PackageDialog(title = "Tambah Paket", onDismiss = { showAddPackage = false }, onConfirm = { n, p, d ->
            scope.launch {
                try {
                    val randomId = Random.nextInt(100000, 999999).toString()
                    val newPkg = GymPackage(randomId, n, p, d)
                    packages.add(newPkg)
                    showAddPackage = false
                    db.collection("gym_packages").document(randomId).set(mapOf("id" to randomId, "name" to n, "price" to p, "durationDays" to d))
                } catch (e: Exception) {}
            }
        })
    }

    if (packageToEdit != null) {
        PackageDialog(title = "Edit Paket", initialPackage = packageToEdit, onDismiss = { packageToEdit = null }, onConfirm = { n, p, d ->
            scope.launch {
                try {
                    val updatedPkg = packageToEdit!!.copy(name = n, price = p, durationDays = d)
                    val idx = packages.indexOfFirst { it.id == updatedPkg.id }
                    if (idx != -1) packages[idx] = updatedPkg
                    packageToEdit = null
                    db.collection("gym_packages").document(updatedPkg.id).set(mapOf("id" to updatedPkg.id, "name" to n, "price" to p, "durationDays" to d))
                } catch (e: Exception) {}
            }
        })
    }
}

@Composable
fun PackageDialog(title: String, initialPackage: GymPackage? = null, onDismiss: () -> Unit, onConfirm: (String, Double, Int) -> Unit) {
    var name by remember { mutableStateOf(initialPackage?.name ?: "") }
    var price by remember { mutableStateOf(initialPackage?.price?.toLong()?.toString() ?: "") }
    var days by remember { mutableStateOf(initialPackage?.durationDays?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Paket") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Harga (Angka saja)") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = days, onValueChange = { days = it }, label = { Text("Durasi (Hari)") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, price.filter { it.isDigit() }.toDoubleOrNull() ?: 0.0, days.filter { it.isDigit() }.toIntOrNull() ?: 0) }, colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)) { Text("Simpan", color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal", color = Color.Gray) } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainerManagementScreen(trainers: SnapshotStateList<Trainer>, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()

    var memberToBuyPtPackage by remember { mutableStateOf<Member?>(null) }
    var showAddTrainer by remember { mutableStateOf(false) }
    var trainerToEdit by remember { mutableStateOf<Trainer?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(DarkBgStart, DarkBgEnd)))) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Manajemen Personal Trainer", color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) } }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { showAddTrainer = true }, containerColor = AccentBlue) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Trainer", tint = Color.White)
                }
            }
        ) { padding ->
            LazyColumn(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(trainers) { trainer ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardDark)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(trainer.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                                Text("Username: ${trainer.username}", color = AccentBlue, fontSize = 12.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("Gender: ${trainer.gender}", color = Color.LightGray, fontSize = 12.sp)
                                    Text("Exp: ${trainer.experience}", color = Color.LightGray, fontSize = 12.sp)
                                }
                                Text("Skill: ${trainer.specialization}", color = Color.LightGray, fontSize = 14.sp)
                            }
                            IconButton(onClick = { trainerToEdit = trainer }) { Icon(Icons.Default.Edit, null, tint = Color.LightGray) }
                            IconButton(onClick = {
                                scope.launch {
                                    try {
                                        val idToRemove = trainer.id
                                        trainers.remove(trainer)
                                        db.collection("trainers").document(idToRemove).delete()
                                    } catch (e: Exception) {}
                                }
                            }) { Icon(Icons.Default.Delete, null, tint = Color(0xFFF87171)) }
                        }
                    }
                }
            }
        }
    }

    if (showAddTrainer) {
        TrainerDialog(onDismiss = { showAddTrainer = false }, onConfirm = { n, u, p, g, s, e ->
            scope.launch {
                try {
                    val newId = "PT-" + Random.nextInt(100, 999).toString()
                    val r = 5.0
                    val d = "Trainer di Youth Gym"
                    val newTrainer = Trainer(newId, n, u, p, g, s, e, r, d)

                    trainers.add(newTrainer)
                    showAddTrainer = false

                    val data = mapOf(
                        "id" to newId,
                        "name" to n,
                        "username" to u,
                        "password" to p,
                        "gender" to g,
                        "specialization" to s,
                        "experience" to e,
                        "rate" to r,
                        "description" to d,
                        "profileImageUrl" to ""
                    )
                    db.collection("trainers").document(newId).set(data)
                } catch (ex: Exception) {}
            }
        })
    }

    if (trainerToEdit != null) {
        EditTrainerDialog(
            trainer = trainerToEdit!!,
            onDismiss = { trainerToEdit = null },
            onConfirm = { updated ->
                scope.launch {
                    try {
                        val idx = trainers.indexOfFirst { it.id == updated.id }
                        if (idx != -1) trainers[idx] = updated
                        trainerToEdit = null

                        val data = mapOf(
                            "name" to updated.name,
                            "username" to updated.username,
                            "password" to updated.password,
                            "gender" to updated.gender,
                            "specialization" to updated.specialization,
                            "experience" to updated.experience,
                            "rate" to updated.rating,
                            "description" to updated.description,
                            "profileImageUrl" to updated.profileImageUrl
                        )
                        db.collection("trainers").document(updated.id).update(data)
                    } catch (e: Exception) {}
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainerDialog(onDismiss: () -> Unit, onConfirm: (String, String, String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Laki-laki") }
    var skill by remember { mutableStateOf("") }
    var exp by remember { mutableStateOf("") }
    var genderExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Akun Trainer", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Lengkap") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = user, onValueChange = { user = it }, label = { Text("Username") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Password") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = gender, onValueChange = {}, label = { Text("Jenis Kelamin") }, readOnly = true, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { IconButton(onClick = { genderExpanded = true }) { Icon(Icons.Default.ArrowDropDown, null) } }
                    )
                    DropdownMenu(expanded = genderExpanded, onDismissRequest = { genderExpanded = false }) {
                        listOf("Laki-laki", "Perempuan").forEach { item ->
                            DropdownMenuItem(text = { Text(item) }, onClick = { gender = item; genderExpanded = false })
                        }
                    }
                }
                OutlinedTextField(value = skill, onValueChange = { skill = it }, label = { Text("Skill / Spesialisasi (Cth: Muay Thai)") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = exp, onValueChange = { exp = it }, label = { Text("Pengalaman (Cth: 3 Tahun)") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank() && user.isNotBlank()) onConfirm(name, user, pass, gender, skill, exp) },
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                shape = RoundedCornerShape(8.dp)
            ) { Text("Simpan", color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal", color = Color.Gray) } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTrainerDialog(trainer: Trainer, onDismiss: () -> Unit, onConfirm: (Trainer) -> Unit) {
    var name by remember { mutableStateOf(trainer.name) }
    var user by remember { mutableStateOf(trainer.username) }
    var pass by remember { mutableStateOf(trainer.password) }
    var gender by remember { mutableStateOf(trainer.gender) }
    var skill by remember { mutableStateOf(trainer.specialization) }
    var exp by remember { mutableStateOf(trainer.experience) }
    var genderExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Data Trainer", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = user, onValueChange = { user = it }, label = { Text("Username") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Password") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = gender, onValueChange = {}, label = { Text("Jenis Kelamin") }, readOnly = true, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { IconButton(onClick = { genderExpanded = true }) { Icon(Icons.Default.ArrowDropDown, null) } }
                    )
                    DropdownMenu(expanded = genderExpanded, onDismissRequest = { genderExpanded = false }) {
                        listOf("Laki-laki", "Perempuan").forEach { item ->
                            DropdownMenuItem(text = { Text(item) }, onClick = { gender = item; genderExpanded = false })
                        }
                    }
                }
                OutlinedTextField(value = skill, onValueChange = { skill = it }, label = { Text("Skill / Spesialisasi") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = exp, onValueChange = { exp = it }, label = { Text("Pengalaman") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(trainer.copy(name = name, username = user, password = pass, gender = gender, specialization = skill, experience = exp)) }, colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)) { Text("Update", color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal", color = Color.Gray) } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProfileScreen(admin: Admin, onUpdate: (Admin) -> Unit, onBack: () -> Unit) {
    var username by remember { mutableStateOf(admin.username) }
    var password by remember { mutableStateOf(admin.password) }
    val scope = rememberCoroutineScope()

    var memberToBuyPtPackage by remember { mutableStateOf<Member?>(null) }
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
                                val updated = Admin(username, password)
                                db.collection("settings").document("admin_account").set(updated)
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

fun formatRupiah(amount: Double): String {
    val str = amount.toLong().toString()
    var result = ""
    var count = 0
    for (i in str.length - 1 downTo 0) {
        result = str[i] + result
        count++
        if (count == 3 && i > 0) {
            result = "." + result
            count = 0
        }
    }
    return result
}

@Composable
fun TrainerListRow(trainers: SnapshotStateList<Trainer>, onTrainerClick: (Trainer) -> Unit) {
    if (trainers.isEmpty()) {
        Text("Belum ada trainer terdaftar", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp))
    } else {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
            items(trainers) { trainer ->
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardDark), modifier = Modifier.width(140.dp), onClick = { onTrainerClick(trainer) }) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(shape = CircleShape, modifier = Modifier.size(40.dp), color = AccentBlue.copy(alpha = 0.2f)) {
                            if (trainer.profileImageUrl.startsWith("res:")) {
                                val resourceName = trainer.profileImageUrl.removePrefix("res:")
                                val painter = when {
                                    resourceName.contains("chiko") -> painterResource(Res.drawable.pt_chiko)
                                    resourceName.contains("gabriel") -> painterResource(Res.drawable.pt_gabriel)
                                    resourceName.contains("marchel") -> painterResource(Res.drawable.pt_marchel)
                                    else -> null
                                }
                                if (painter != null) {
                                    Image(
                                        painter = painter,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(Icons.Default.Person, null, modifier = Modifier.padding(8.dp), tint = AccentBlue)
                                }
                            } else {
                                Icon(Icons.Default.Person, null, modifier = Modifier.padding(8.dp), tint = AccentBlue)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(trainer.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White, maxLines = 1)
                        Text("Trainer", fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMemberDialog(
    packages: List<GymPackage>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, String, Int, Int, Double, Long, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Laki-laki") }
    var phone by remember { mutableStateOf("") }
    var ptSessions by remember { mutableStateOf("0") }
    var selectedPackage by remember { mutableStateOf<GymPackage?>(null) }
    val joinDate = getCurrentTimeMillis()
    var expanded by remember { mutableStateOf(false) }
    var genderExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Member Baru", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Lengkap") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Nomor HP") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = ptSessions, onValueChange = { ptSessions = it }, label = { Text("Sesi PT") }, modifier = Modifier.fillMaxWidth())

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = gender, onValueChange = {}, label = { Text("Jenis Kelamin") }, readOnly = true, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { IconButton(onClick = { genderExpanded = true }) { Icon(Icons.Default.ArrowDropDown, null) } }
                    )
                    DropdownMenu(expanded = genderExpanded, onDismissRequest = { genderExpanded = false }) {
                        listOf("Laki-laki", "Perempuan").forEach { item ->
                            DropdownMenuItem(text = { Text(item) }, onClick = { gender = item; genderExpanded = false })
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedPackage?.name ?: "Pilih Paket",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Paket") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { IconButton(onClick = { expanded = true }) { Icon(Icons.Default.ArrowDropDown, null) } }
                    )
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        packages.forEach { pkg ->
                            DropdownMenuItem(
                                text = { Text("${pkg.name} - Rp ${formatRupiah(pkg.price ?: 0.0)}") },
                                onClick = {
                                    selectedPackage = pkg
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && username.isNotBlank() && password.isNotBlank() && email.isNotBlank() && phone.isNotBlank() && selectedPackage != null) {
                        selectedPackage?.let { pkg ->
                            onConfirm(name, username, password, email, gender, phone, pkg.durationDays, ptSessions.toIntOrNull() ?: 0, pkg.price ?: 0.0, joinDate, pkg.name)
                        }
                    }
                },
                enabled = name.isNotBlank() && username.isNotBlank() && password.isNotBlank() && email.isNotBlank() && phone.isNotBlank() && selectedPackage != null,
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) { Text("Simpan", color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal", color = Color.Gray) } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMemberDialog(
    member: Member,
    onDismiss: () -> Unit,
    onConfirm: (Member) -> Unit
) {
    var name by remember { mutableStateOf(member.name) }
    var username by remember { mutableStateOf(member.username) }
    var password by remember { mutableStateOf(member.password) }
    var email by remember { mutableStateOf(member.email) }
    var phone by remember { mutableStateOf(member.phoneNumber) }
    var remainingDays by remember { mutableStateOf(member.remainingDays.toString()) }
    var ptSessions by remember { mutableStateOf(member.remainingPtSessions.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Data Member", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Nomor HP") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = remainingDays, onValueChange = { remainingDays = it }, label = { Text("Sisa Hari") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = ptSessions, onValueChange = { ptSessions = it }, label = { Text("Sesi PT") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = member.copy(
                        name = name,
                        username = username,
                        password = password,
                        email = email,
                        phoneNumber = phone,
                        remainingDays = remainingDays.toIntOrNull() ?: member.remainingDays,
                        remainingPtSessions = ptSessions.toIntOrNull() ?: member.remainingPtSessions
                    )
                    onConfirm(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) { Text("Update", color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal", color = Color.Gray) } }
    )
}

@Composable
fun MemberCard(
    member: Member,
    onCheckIn: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onExtend: () -> Unit,
    onBuyPt: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(member.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                    Text("ID: ${member.id} • ${member.gender}", color = AccentBlue, fontSize = 12.sp)
                    Text("Paket: ${member.packageName}", color = Color.LightGray, fontSize = 12.sp)
                    Text("HP: ${member.phoneNumber}", color = Color.LightGray, fontSize = 12.sp)
                    Text(
                        "Sisa: ${member.remainingDays} Hari • PT: ${member.remainingPtSessions} Sesi",
                        color = if (member.remainingDays > 0) Color(0xFF34D399) else Color(0xFFF87171),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                Row {
                    IconButton(onClick = onCheckIn) { Icon(Icons.Default.CheckCircle, "Check-in", tint = Color(0xFF10B981)) }
                    IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Edit", tint = Color.LightGray) }
                    IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Hapus", tint = Color(0xFFF87171)) }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onExtend,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Refresh, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Perpanjang Paket", color = Color.White, fontSize = 11.sp)
                }
                Button(
                    onClick = onBuyPt,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sewa PT", color = Color.White, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun CalendarCard() {
    val now = getCurrentTimeMillis()
    val dateStr = formatEpochToDate(now)
    val parts = dateStr.split(" ")
    val day = parts.getOrNull(0) ?: ""
    val month = parts.getOrNull(1) ?: ""
    val year = parts.getOrNull(2) ?: ""

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = CardDark)) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(60.dp).clip(RoundedCornerShape(12.dp)).background(AccentBlue.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(month.take(3).uppercase(), color = AccentBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(day, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Hari Ini", color = Color.Gray, fontSize = 12.sp)
                Text("$day $month $year", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Waktu Indonesia Barat (WIB)", color = AccentBlue.copy(alpha = 0.8f), fontSize = 11.sp)
            }
        }
    }
}

fun generateRevenueHtml(members: List<Member>, filterRange: String = "Semua Waktu"): String {
    val sortedMembers = members.sortedBy { it.joinDate }
    var cumulativeRevenue = 0.0

    val htmlBuilder = StringBuilder()
    htmlBuilder.append("""
        <html xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:x="urn:schemas-microsoft-com:office:excel" xmlns="http://www.w3.org/TR/REC-html40">
        <head>
            <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
            <style>
                table { border-collapse: collapse; width: 100%; font-family: sans-serif; }
                th { background-color: #3B82F6; color: white; border: 1px solid #ddd; padding: 12px; text-align: left; }
                td { border: 1px solid #ddd; padding: 10px; }
                tr:nth-child(even) { background-color: #f2f2f2; }
                .total-row { background-color: #1E293B; color: white; font-weight: bold; }
                .header-title { font-size: 24px; color: #3B82F6; font-weight: bold; margin-bottom: 20px; text-align: center; }
            </style>
        </head>
        <body>
            <div class="header-title">LAPORAN PENDAPATAN YOUTH GYM ($filterRange)</div>
            <table>
                <thead>
                    <tr>
                        <th>No</th>
                        <th>ID Member</th>
                        <th>Nama Member</th>
                        <th>Paket</th>
                        <th>Tanggal Transaksi</th>
                        <th>Pendapatan</th>
                        <th>Total Akumulasi</th>
                    </tr>
                </thead>
                <tbody>
    """.trimIndent())

    sortedMembers.forEachIndexed { index, m ->
        cumulativeRevenue += (m.pricePaid ?: 0.0)
        val dateStr = formatEpochToDate(m.joinDate).split(" ").take(3).joinToString(" ")

        htmlBuilder.append("""
            <tr>
                <td>${index + 1}</td>
                <td>${m.id}</td>
                <td>${m.name}</td>
                <td>${m.packageName}</td>
                <td>$dateStr</td>
                <td>Rp ${formatRupiah(m.pricePaid ?: 0.0)}</td>
                <td>Rp ${formatRupiah(cumulativeRevenue)}</td>
            </tr>
        """.trimIndent())
    }

    htmlBuilder.append("""
                </tbody>
                <tfoot>
                    <tr class="total-row">
                        <td colspan="6" style="text-align: right;">TOTAL PENDAPATAN AKHIR</td>
                        <td>Rp ${formatRupiah(cumulativeRevenue)}</td>
                    </tr>
                </tfoot>
            </table>
        </body>
        </html>
    """.trimIndent())

    return htmlBuilder.toString()
}

fun parseDateToMillis(dateStr: String): Long? {
    return com.chiko0085.testgym.parseDateToMillis(dateStr)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationManagementScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()

    var memberToBuyPtPackage by remember { mutableStateOf<Member?>(null) }
    var selectedDateMillis by remember { mutableStateOf(getCurrentTimeMillis()) }
    var reservations by remember { mutableStateOf<List<Reservation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(selectedDateMillis) {
        isLoading = true
        try {
            val dateStr = formatEpochToDate(selectedDateMillis).split(" ").take(3).joinToString(" ")
            val dayStart = com.chiko0085.testgym.parseDateToMillis(dateStr) ?: selectedDateMillis

            val snapshot = db.collection("reservations").where("date", equalTo = dayStart).get()
            val dbReservations = snapshot.documents.map { it.data<Reservation>() }
            val fullList = mutableListOf<Reservation>()
            for (i in 8..23) {
                val found = dbReservations.find { it.hour == i }
                fullList.add(found ?: Reservation(id = "${dayStart}_$i", date = dayStart, hour = i, status = "Empty"))
            }
            reservations = fullList
        } catch (e: Exception) {
        } finally {
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(DarkBgStart, DarkBgEnd)))) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { TopAppBar(title = { Text("Manajemen Jadwal Padel", color = Color.White) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent), navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) } }) }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Tanggal: ${formatEpochToDate(selectedDateMillis).split(" ").take(3).joinToString(" ")}", color = Color.White, fontWeight = FontWeight.Bold)
                    Row {
                        IconButton(onClick = { selectedDateMillis -= 86400000L }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                        IconButton(onClick = { selectedDateMillis += 86400000L }) { Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White) }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = AccentBlue) }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                        items(reservations) { res ->
                            ReservationSlotCard(res, onToggleStatus = { newStatus ->
                                scope.launch {
                                    try {
                                        val updated = res.copy(status = newStatus, reservedByName = if(newStatus == "Booked") "Admin" else "")
                                        db.collection("reservations").document(updated.id).set(updated)
                                        reservations = reservations.map { if (it.id == res.id) updated else it }
                                    } catch (e: Exception) {}
                                }
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReservationSlotCard(reservation: Reservation, onToggleStatus: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardDark)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                val hourStr = if (reservation.hour < 10) "0${reservation.hour}:00" else "${reservation.hour}:00"
                val nextHour = if (reservation.hour + 1 < 10) "0${reservation.hour + 1}:00" else "${reservation.hour + 1}:00"
                Text("$hourStr - $nextHour", color = Color.White, fontWeight = FontWeight.Bold)
                Text(if (reservation.status == "Empty") "Kosong" else "Terisi (${reservation.reservedByName})", color = if (reservation.status == "Empty") Color(0xFF34D399) else Color(0xFFF87171), fontSize = 12.sp)
            }
            Row {
                Button(onClick = { onToggleStatus("Empty") }, colors = ButtonDefaults.buttonColors(containerColor = if (reservation.status == "Empty") Color(0xFF10B981) else Color.Gray.copy(alpha = 0.3f)), shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp), modifier = Modifier.height(32.dp)) { Text("Kosong", fontSize = 10.sp, color = Color.White) }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { onToggleStatus("Booked") }, colors = ButtonDefaults.buttonColors(containerColor = if (reservation.status == "Booked") Color(0xFFEF4444) else Color.Gray.copy(alpha = 0.3f)), shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp), modifier = Modifier.height(32.dp)) { Text("Terisi", fontSize = 10.sp, color = Color.White) }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 🌟 KODE BARU: HALAMAN MANAJEMEN MEMBER DENGAN STRUKTUR SEGMEN FILTER AKTIF / EXPIRED
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberManagementScreen(
    members: List<Member>,
    gymPackages: List<GymPackage>,
    ptPackages: List<PtPackage>, // Tambahkan ini
    onCheckIn: (Member) -> Unit,
    onEdit: (Member) -> Unit,
    onDelete: (Member) -> Unit,
    onAddMember: () -> Unit,
    onUpdateRevenue: (Double) -> Unit,
    totalRevenue: Double,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filterOptions = listOf("Semua", "Aktif", "Tidak Aktif")
    val pagerState = rememberPagerState(pageCount = { filterOptions.size })
    var sortOrder by remember { mutableStateOf("Terbaru") } // Opsi: Terbaru, Terlama, A-Z, Z-A
    var sortExpanded by remember { mutableStateOf(false) }
    var memberToExtend by remember { mutableStateOf<Member?>(null) }
    val scope = rememberCoroutineScope()

    var memberToBuyPtPackage by remember { mutableStateOf<Member?>(null) }

    // Fungsi pembantu untuk memproses list berdasarkan filter dan sort
    fun getFilteredAndSortedList(filter: String): List<Member> {
        return members.filter { member ->
            val matchesSearch = member.name.contains(searchQuery, ignoreCase = true) || member.id.contains(searchQuery)
            val isActive = member.remainingDays > 0
            val matchesFilter = when (filter) {
                "Aktif" -> isActive
                "Tidak Aktif" -> !isActive
                else -> true
            }
            matchesSearch && matchesFilter
        }.let { list ->
            when (sortOrder) {
                "A-Z" -> list.sortedBy { it.name }
                "Z-A" -> list.sortedByDescending { it.name }
                "Terbaru" -> list.sortedByDescending { it.joinDate }
                "Terlama" -> list.sortedBy { it.joinDate }
                else -> list
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(DarkBgStart, DarkBgEnd)))) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Manajemen Member Gym", color = Color.White, fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = onAddMember) {
                            Icon(Icons.Default.Add, "Tambah Member", tint = Color.White)
                        }
                    }
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).padding(16.dp)) {
                // Search Field Bertema Gelap
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari Member (Nama/ID)...", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = AccentBlue) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardDark,
                        unfocusedContainerColor = CardDark,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Baris Filter Tab Segmented (Semua, Aktif, Tidak Aktif)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    filterOptions.forEachIndexed { index, filterOption ->
                        val isSelected = pagerState.currentPage == index
                        Button(
                            onClick = { 
                                scope.launch { pagerState.animateScrollToPage(index) }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) AccentBlue else CardDark,
                                contentColor = if (isSelected) Color.White else Color.LightGray
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(filterOption, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Baris Urutan (Dropdown Menu)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Sort, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Text("Urutkan:", color = Color.Gray, fontSize = 11.sp)
                    
                    Box {
                        TextButton(
                            onClick = { sortExpanded = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = AccentBlue),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(sortOrder, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp))
                        }
                        
                        DropdownMenu(
                            expanded = sortExpanded,
                            onDismissRequest = { sortExpanded = false }
                        ) {
                            val options = listOf("Terbaru", "Terlama", "A-Z", "Z-A")
                            options.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        sortOrder = option
                                        sortExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // List Tampilan Member Berdasarkan Hasil Filter dengan Drag/Swipe Support
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.Top
                ) { pageIndex ->
                    val currentFilter = filterOptions[pageIndex]
                    val currentList = getFilteredAndSortedList(currentFilter)
                    
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (currentList.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Text("Tidak ada data member found.", color = Color.Gray, fontSize = 14.sp)
                                }
                            }
                        } else {
                            items(currentList) { member ->
                                MemberCard(
                                    member = member,
                                    onCheckIn = { onCheckIn(member) },
                                    onEdit = { onEdit(member) },
                                    onDelete = { onDelete(member) },
                                    onExtend = { memberToExtend = member },
                                    onBuyPt = { memberToBuyPtPackage = member }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (memberToExtend != null) {
        ExtendMembershipDialog(
            member = memberToExtend!!,
            packages = gymPackages,
            onDismiss = { memberToExtend = null },
            onConfirm = { pkg ->
                scope.launch {
                    try {
                        val m = memberToExtend!!
                        val updatedRemaining = m.remainingDays + pkg.durationDays
                        val updatedPricePaid = (m.pricePaid ?: 0.0) + pkg.price!!
                        
                        val updated = m.copy(
                            remainingDays = updatedRemaining,
                            pricePaid = updatedPricePaid,
                            packageName = pkg.name
                        )
                        
                        db.collection("members").document(m.id).update(mapOf(
                            "remainingDays" to updatedRemaining,
                            "pricePaid" to updatedPricePaid,
                            "packageName" to pkg.name
                        ))
                        
                        // Update local state if needed (handled via members list in AdminDashboard)
                        val idx = members.indexOfFirst { it.id == m.id }
                        if (idx != -1) {
                            if (members is MutableList) {
                                (members as MutableList<Member>)[idx] = updated
                            }
                        }
                        
                        onUpdateRevenue(totalRevenue + (pkg.price ?: 0.0))
                        memberToExtend = null
                    } catch (e: Exception) {}
                }
            }
        )
    }

    if (memberToBuyPtPackage != null) {
        BuyPtPackageDialog(
            member = memberToBuyPtPackage!!,
            packages = ptPackages,
            onDismiss = { memberToBuyPtPackage = null },
            onConfirm = { pkg ->
                scope.launch {
                    try {
                        val m = memberToBuyPtPackage!!
                        val updatedPtSessions = m.remainingPtSessions + pkg.sessions
                        val updatedPricePaid = (m.pricePaid ?: 0.0) + pkg.price!!
                        
                        val updated = m.copy(
                            remainingPtSessions = updatedPtSessions,
                            pricePaid = updatedPricePaid
                        )
                        
                        db.collection("members").document(m.id).update(mapOf(
                            "remainingPtSessions" to updatedPtSessions,
                            "pricePaid" to updatedPricePaid
                        ))
                        
                        val idx = members.indexOfFirst { it.id == m.id }
                        if (idx != -1) {
                            if (members is MutableList) {
                                (members as MutableList<Member>)[idx] = updated
                            }
                        }
                        
                        onUpdateRevenue(totalRevenue + (pkg.price ?: 0.0))
                        memberToBuyPtPackage = null
                    } catch (e: Exception) {}
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtendMembershipDialog(
    member: Member,
    packages: List<GymPackage>,
    onDismiss: () -> Unit,
    onConfirm: (GymPackage) -> Unit
) {
    var selectedPackage by remember { mutableStateOf<GymPackage?>(null) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Perpanjang Paket: ${member.name}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Pilih paket baru untuk menambahkan masa aktif member.", fontSize = 14.sp)
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedPackage?.name ?: "Pilih Paket",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Paket") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { IconButton(onClick = { expanded = true }) { Icon(Icons.Default.ArrowDropDown, null) } }
                    )
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        packages.forEach { pkg ->
                            DropdownMenuItem(
                                text = { Text("${pkg.name} - Rp ${formatRupiah(pkg.price ?: 0.0)}") },
                                onClick = {
                                    selectedPackage = pkg
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { selectedPackage?.let { onConfirm(it) } },
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) { Text("Perpanjang", color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal", color = Color.Gray) } }
    )
}

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
                            }) { Icon(Icons.Default.Delete, null, tint = Color(0xFFF87171)) }
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
                            DropdownMenuItem(
                                text = { Text("${pkg.name} - ${pkg.sessions} Sesi (Rp ${formatRupiah(pkg.price ?: 0.0)})") },
                                onClick = {
                                    selectedPackage = pkg
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { selectedPackage?.let { onConfirm(it) } },
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                enabled = selectedPackage != null
            ) { Text("Beli Paket", color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal", color = Color.Gray) } }
    )
}
