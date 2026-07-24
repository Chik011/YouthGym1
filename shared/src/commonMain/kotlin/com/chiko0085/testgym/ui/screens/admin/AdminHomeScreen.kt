package com.chiko0085.testgym.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chiko0085.testgym.exportToExcel
import com.chiko0085.testgym.util.generateRevenueHtml
import com.chiko0085.testgym.getCurrentTimeMillis
import com.chiko0085.testgym.model.Member
import com.chiko0085.testgym.model.Trainer
import com.chiko0085.testgym.model.Transaction
import com.chiko0085.testgym.ui.components.CalendarCard
import com.chiko0085.testgym.ui.theme.AccentBlue
import com.chiko0085.testgym.ui.theme.CardDark
import com.chiko0085.testgym.util.formatRupiah
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import youthgym.shared.generated.resources.Res
import youthgym.shared.generated.resources.pt_chiko
import youthgym.shared.generated.resources.pt_gabriel
import youthgym.shared.generated.resources.pt_marchel

// Beranda utama admin
@Composable
fun AdminHomeScreen(
    members: List<Member>,
    trainers: SnapshotStateList<Trainer>,
    transactions: List<Transaction>,
    admin: com.chiko0085.testgym.model.Admin,
    totalRevenue: Double,
    onResetRevenue: () -> Unit,
    onTrainerClick: (Trainer) -> Unit,
    onViewDetail: () -> Unit,
    snackbarHostState: SnackbarHostState,
    isDesktop: Boolean
) {
    var selectedFilterText by remember { mutableStateOf("Hari Ini") }
    var filterExpanded by remember { mutableStateOf(false) }
    val filterOptions = listOf("Hari Ini", "7 Hari Terakhir", "30 Hari Terakhir", "Semua Waktu")
    val scope = rememberCoroutineScope()

    var startAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        startAnimation = true
    }

    val animatedRevenue by animateFloatAsState(
        targetValue = if (startAnimation) totalRevenue.toFloat() else 0f,
        animationSpec = tween(durationMillis = 1500),
        label = "revenue_anim"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = if(isDesktop) 32.dp else 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { 
            Spacer(modifier = Modifier.height(16.dp))
            CalendarCard() 
        }

        if (admin.role == "super_admin" || admin.permissions.contains("revenue_view")) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.6f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Laporan Keuangan", color = AccentBlue, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Ringkasan transaksi gym", color = Color.Gray, fontSize = 12.sp)
                            }
                            
                            Box {
                                TextButton(
                                    onClick = { filterExpanded = true },
                                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
                                    modifier = Modifier.background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                ) {
                                    Text(selectedFilterText, fontSize = 12.sp)
                                    Icon(Icons.Default.ArrowDropDown, null)
                                }
                                DropdownMenu(expanded = filterExpanded, onDismissRequest = { filterExpanded = false }) {
                                    filterOptions.forEach { option ->
                                        DropdownMenuItem(text = { Text(option) }, onClick = { selectedFilterText = option; filterExpanded = false })
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        val currentTotal = remember(transactions, selectedFilterText) {
                            val now = getCurrentTimeMillis()
                            when (selectedFilterText) {
                                "Hari Ini" -> {
                                    val startOfDay = now - (now % 86400000L)
                                    transactions.filter { it.timestamp >= startOfDay }.sumOf { it.amount }
                                }
                                "7 Hari Terakhir" -> transactions.filter { it.timestamp >= now - (7 * 86400000L) }.sumOf { it.amount }
                                "30 Hari Terakhir" -> transactions.filter { it.timestamp >= now - (30 * 86400000L) }.sumOf { it.amount }
                                else -> totalRevenue
                            }
                        }

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("Rp", color = Color.Gray, fontSize = 18.sp, modifier = Modifier.padding(bottom = 6.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(formatRupiah(currentTotal), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = onViewDetail,
                                modifier = Modifier.weight(1f).height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.List, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("View Detail", fontSize = 13.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    val nowTime = getCurrentTimeMillis()
                                    val filteredForExcel = when (selectedFilterText) {
                                        "Hari Ini" -> {
                                            val startOfDay = nowTime - (nowTime % 86400000L)
                                            members.filter { it.joinDate >= startOfDay }
                                        }
                                        "7 Hari Terakhir" -> members.filter { it.joinDate >= nowTime - (7 * 86400000L) }
                                        "30 Hari Terakhir" -> members.filter { it.joinDate >= nowTime - (30 * 86400000L) }
                                        else -> members
                                    }
                                    val html = generateRevenueHtml(filteredForExcel, selectedFilterText)
                                    exportToExcel(html)
                                    scope.launch { snackbarHostState.showSnackbar("Laporan ($selectedFilterText) diunduh") }
                                },
                                modifier = Modifier.weight(1f).height(48.dp),
                                border = BorderStroke(1.dp, AccentBlue),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentBlue)
                            ) {
                                Icon(Icons.Default.Download, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Download Excel", fontSize = 13.sp)
                            }
                        }
                        
                        if (admin.role == "super_admin" || admin.permissions.contains("revenue_reset")) {
                            Spacer(modifier = Modifier.height(12.dp))
                            TextButton(onClick = onResetRevenue, modifier = Modifier.fillMaxWidth()) {
                                Text("Reset Histori Pendapatan", color = Color.Red.copy(alpha = 0.6f), fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        item {
            Text("Personal Trainer Aktif", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AccentBlue, modifier = Modifier.padding(vertical = 8.dp))
            TrainerListRow(trainers, onTrainerClick = onTrainerClick)
        }

        item { Spacer(modifier = Modifier.height(if(isDesktop) 32.dp else 80.dp)) }
    }
}

// Baris list trainer di home
@OptIn(ExperimentalMaterial3Api::class)
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
