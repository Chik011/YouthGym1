package com.chiko0085.testgym.ui.screens.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF1D4ED8), Color(0xFF3B82F6))))
                ) {
                    Column(modifier = Modifier.padding(28.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountCircle, null, tint = Color.White, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Total Revenue", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                        }
                        Text("Rp ${formatRupiah(animatedRevenue.toDouble())}", color = Color.White, fontSize = if(isDesktop) 46.sp else 32.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${members.size} Total Members Aktif", color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
                        TextButton(onClick = onResetRevenue) {
                            Icon(Icons.Default.Refresh, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset Pendapatan", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                        }
                    }

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
                        Icon(Icons.Default.Download, contentDescription = null, tint = Color(0xFF1D4ED8))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Unduh Laporan Excel (CSV)", color = Color(0xFF1D4ED8), fontWeight = FontWeight.Bold)
                    }
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
