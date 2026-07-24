package com.chiko0085.testgym.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chiko0085.testgym.formatEpochToDate
import com.chiko0085.testgym.getCurrentTimeMillis
import com.chiko0085.testgym.model.Member
import com.chiko0085.testgym.model.Transaction
import com.chiko0085.testgym.ui.theme.*
import com.chiko0085.testgym.util.formatRupiah

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevenueDetailScreen(
    members: List<Member>,
    transactions: List<Transaction>,
    onBack: () -> Unit
) {
    // Filter State
    val months = listOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
    
    val currentYear = formatEpochToDate(getCurrentTimeMillis()).split(" ").last().toIntOrNull() ?: 2024
    val years = (2024..currentYear + 1).toList().map { it.toString() }
    
    var selectedMonthIndex by remember { mutableStateOf(-1) } 
    var selectedYear by remember { mutableStateOf(currentYear.toString()) }
    var selectedFilterType by remember { mutableStateOf("Semua") } 
    var yearDropdownExpanded by remember { mutableStateOf(false) }

    // Gabungkan data Transaksi nyata dengan data Member lama (Legacy)
    val allData = remember(members, transactions) {
        val list = mutableListOf<Transaction>()
        
        // 1. Masukkan semua data dari tabel transactions
        list.addAll(transactions)
        
        // 2. Masukkan data member yang punya pricePaid tapi tidak ada di transactions (Legacy)
        // Kita anggap sebagai pendaftaran member pada joinDate
        members.filter { (it.pricePaid ?: 0.0) > 0 }.forEach { member ->
            // Cek apakah member ini sudah tercatat di tabel transactions baru
            // (Mencegah double count)
            val alreadyInTx = transactions.any { it.memberName == member.name && it.amount == member.pricePaid }
            if (!alreadyInTx) {
                list.add(
                    Transaction(
                        id = "LEG-${member.id}",
                        memberName = member.name,
                        amount = member.pricePaid ?: 0.0,
                        type = "Gym Package",
                        description = "Pembayaran Member (Legacy)",
                        timestamp = member.joinDate
                    )
                )
            }
        }
        list.sortedByDescending { it.timestamp }
    }

    val filteredTransactions = remember(allData, selectedFilterType, selectedMonthIndex, selectedYear) {
        val now = getCurrentTimeMillis()
        when (selectedFilterType) {
            "Hari Ini" -> {
                val startOfDay = now - (now % 86400000L)
                allData.filter { it.timestamp >= startOfDay }
            }
            "Bulan Ini" -> {
                val dateNowParts = formatEpochToDate(now).split(" ")
                val currentMonthName = dateNowParts[1]
                val currentYearName = dateNowParts[2]
                allData.filter { tx ->
                    val txDate = formatEpochToDate(tx.timestamp)
                    txDate.contains(currentMonthName, ignoreCase = true) && txDate.contains(currentYearName)
                }
            }
            "Per Bulan" -> {
                if (selectedMonthIndex == -1) emptyList()
                else {
                    val monthName = months[selectedMonthIndex]
                    allData.filter { tx ->
                        val txDate = formatEpochToDate(tx.timestamp)
                        txDate.contains(monthName, ignoreCase = true) && txDate.contains(selectedYear)
                    }
                }
            }
            "Semua" -> allData
            else -> allData
        }
    }

    val totalAmount = filteredTransactions.sumOf { it.amount }

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(DarkBgStart, DarkBgEnd)))) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Detail Keuangan", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).padding(horizontal = 16.dp)) {
                // Info Total
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = AccentBlue.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        val filterLabel = when(selectedFilterType) {
                            "Per Bulan" -> if(selectedMonthIndex != -1) "${months[selectedMonthIndex]} $selectedYear" else "Pilih Bulan & Tahun"
                            else -> selectedFilterType
                        }
                        Text(
                            text = "Total Pendapatan ($filterLabel)", 
                            color = Color.LightGray, 
                            fontSize = 14.sp
                        )
                        Text("Rp ${formatRupiah(totalAmount)}", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                        
                        if (allData.isNotEmpty()) {
                             Text("${allData.size} Data Tercatat (Termasuk Legacy)", color = AccentBlue, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Selector Tipe Filter (Tab-like)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Hari Ini", "Bulan Ini", "Per Bulan", "Semua").forEach { type ->
                        val isSelected = selectedFilterType == type
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { 
                                    selectedFilterType = type 
                                    if (type != "Per Bulan") {
                                        selectedMonthIndex = -1
                                    } else if (selectedMonthIndex == -1) {
                                        val dateParts = formatEpochToDate(getCurrentTimeMillis()).split(" ")
                                        selectedMonthIndex = months.indexOf(dateParts[1])
                                        selectedYear = dateParts[2]
                                    }
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) AccentBlue else Color.White.copy(alpha = 0.05f),
                            border = if (isSelected) null else BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                        ) {
                            Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                Text(type, color = if (isSelected) Color.White else Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Selector Bulan & Tahun
                if (selectedFilterType == "Per Bulan") {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.wrapContentSize()) {
                            OutlinedButton(
                                onClick = { yearDropdownExpanded = true },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.5f)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text(selectedYear, fontSize = 12.sp)
                                Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp))
                            }
                            DropdownMenu(expanded = yearDropdownExpanded, onDismissRequest = { yearDropdownExpanded = false }) {
                                years.forEach { year ->
                                    DropdownMenuItem(text = { Text(year) }, onClick = { selectedYear = year; yearDropdownExpanded = false })
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                            items(months.size) { index ->
                                val isMonthSelected = selectedMonthIndex == index
                                Surface(
                                    modifier = Modifier.clickable { selectedMonthIndex = index },
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isMonthSelected) Color(0xFF10B981) else Color.White.copy(alpha = 0.05f),
                                    border = if (isMonthSelected) null else BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                                ) {
                                    Text(months[index], modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp), color = if (isMonthSelected) Color.White else Color.LightGray, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Daftar Transaksi", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))

                if (filteredTransactions.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Tidak ada transaksi.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
                        items(filteredTransactions) { tx ->
                            TransactionCard(tx)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionCard(tx: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = when(tx.type) {
                    "Gym Package" -> SuccessGreen.copy(alpha = 0.1f)
                    "PT Package" -> AccentBlue.copy(alpha = 0.1f)
                    else -> Color.Yellow.copy(alpha = 0.1f)
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = when(tx.type) {
                        "PT Package" -> Icons.Default.Info
                        else -> Icons.Default.CalendarMonth
                    },
                    contentDescription = null,
                    tint = when(tx.type) {
                        "Gym Package" -> SuccessGreenLight
                        "PT Package" -> AccentBlue
                        else -> Color.Yellow
                    },
                    modifier = Modifier.padding(8.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(tx.memberName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(tx.description, color = Color.Gray, fontSize = 12.sp)
                Text(formatEpochToDate(tx.timestamp), color = Color.DarkGray, fontSize = 10.sp)
            }
            Text("Rp ${formatRupiah(tx.amount)}", color = SuccessGreenLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}
