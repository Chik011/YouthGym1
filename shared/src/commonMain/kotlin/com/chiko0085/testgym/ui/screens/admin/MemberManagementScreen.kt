package com.chiko0085.testgym.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chiko0085.testgym.db
import com.chiko0085.testgym.model.GymPackage
import com.chiko0085.testgym.model.Member
import com.chiko0085.testgym.model.PtPackage
import com.chiko0085.testgym.ui.components.MemberCard
import com.chiko0085.testgym.ui.theme.*
import com.chiko0085.testgym.util.formatRupiah
import kotlinx.coroutines.launch

// Layar manajemen member dengan filter
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberManagementScreen(
    members: List<Member>,
    gymPackages: List<GymPackage>,
    ptPackages: List<PtPackage>,
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
    var sortOrder by remember { mutableStateOf("Terbaru") }
    var sortExpanded by remember { mutableStateOf(false) }
    var memberToExtend by remember { mutableStateOf<Member?>(null) }
    var memberToBuyPtPackage by remember { mutableStateOf<Member?>(null) }
    val scope = rememberCoroutineScope()

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
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) } },
                    actions = { IconButton(onClick = onAddMember) { Icon(Icons.Default.Add, "Tambah Member", tint = Color.White) } }
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).padding(16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari Member (Nama/ID)...", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = AccentBlue) },
                    colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = CardDark, unfocusedContainerColor = CardDark, focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = AccentBlue, unfocusedBorderColor = Color.Transparent)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    filterOptions.forEachIndexed { index, filterOption ->
                        val isSelected = pagerState.currentPage == index
                        Button(
                            onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) AccentBlue else CardDark, contentColor = if (isSelected) Color.White else Color.LightGray),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) { Text(filterOption, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Sort, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Text("Urutkan:", color = Color.Gray, fontSize = 11.sp)
                    Box {
                        TextButton(onClick = { sortExpanded = true }, colors = ButtonDefaults.textButtonColors(contentColor = AccentBlue), modifier = Modifier.height(32.dp)) {
                            Text(sortOrder, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp))
                        }
                        DropdownMenu(expanded = sortExpanded, onDismissRequest = { sortExpanded = false }) {
                            listOf("Terbaru", "Terlama", "A-Z", "Z-A").forEach { option ->
                                DropdownMenuItem(text = { Text(option) }, onClick = { sortOrder = option; sortExpanded = false })
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.Top) { pageIndex ->
                    val currentFilter = filterOptions[pageIndex]
                    val currentList = getFilteredAndSortedList(currentFilter)
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                        if (currentList.isEmpty()) {
                            item { Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("Tidak ada data member found.", color = Color.Gray, fontSize = 14.sp) } }
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
                        val newExpiredDate = com.chiko0085.testgym.getCurrentTimeMillis() + (updatedRemaining * 86400000L)
                        val updatedPricePaid = (m.pricePaid ?: 0.0) + pkg.price!!
                        val updated = m.copy(
                            remainingDays = updatedRemaining,
                            expiredDate = newExpiredDate,
                            pricePaid = updatedPricePaid,
                            packageName = pkg.name
                        )
                        db.collection("members").document(m.id).update(mapOf(
                            "remainingDays" to updatedRemaining,
                            "expiredDate" to newExpiredDate,
                            "pricePaid" to updatedPricePaid,
                            "packageName" to pkg.name
                        ))
                        val idx = members.indexOfFirst { it.id == m.id }
                        if (idx != -1) (members as MutableList<Member>)[idx] = updated
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
                        // Set masa aktif PT 30 hari dari hari ini
                        val newPtExpiredDate = com.chiko0085.testgym.getCurrentTimeMillis() + (30L * 24 * 60 * 60 * 1000)
                        val updatedPricePaid = (m.pricePaid ?: 0.0) + pkg.price!!
                        val updated = m.copy(
                            remainingPtSessions = updatedPtSessions,
                            ptExpiredDate = newPtExpiredDate,
                            pricePaid = updatedPricePaid
                        )
                        db.collection("members").document(m.id).update(mapOf(
                            "remainingPtSessions" to updatedPtSessions,
                            "ptExpiredDate" to newPtExpiredDate,
                            "pricePaid" to updatedPricePaid
                        ))
                        val idx = members.indexOfFirst { it.id == m.id }
                        if (idx != -1) (members as MutableList<Member>)[idx] = updated
                        onUpdateRevenue(totalRevenue + (pkg.price ?: 0.0))
                        memberToBuyPtPackage = null
                    } catch (e: Exception) {}
                }
            }
        )
    }
}

// Dialog perpanjang membership
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
                            DropdownMenuItem(text = { Text("${pkg.name} - Rp ${formatRupiah(pkg.price ?: 0.0)}") }, onClick = { selectedPackage = pkg; expanded = false })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { selectedPackage?.let { onConfirm(it) } }, colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)) { Text("Perpanjang", color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal", color = Color.Gray) } }
    )
}
