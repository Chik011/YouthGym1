package com.chiko0085.testgym.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
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
import com.chiko0085.testgym.model.WorkoutSession
import com.chiko0085.testgym.ui.theme.AccentBlue
import com.chiko0085.testgym.ui.theme.CardDark
import com.chiko0085.testgym.ui.theme.DarkBgEnd
import com.chiko0085.testgym.ui.theme.DarkBgStart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutHistoryScreen(
    sessions: List<WorkoutSession>,
    onBack: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(DarkBgStart, DarkBgEnd)))) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Riwayat Progres Latihan", color = Color.White, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            if (sessions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Belum ada data progres latihan.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(sessions.sortedByDescending { it.date }) { session ->
                        WorkoutSessionCard(session)
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutSessionCard(session: WorkoutSession) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AccentBlue.copy(alpha = 0.2f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Default.DateRange, null, tint = AccentBlue, modifier = Modifier.padding(8.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(formatEpochToDate(session.date), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Coach: ${session.trainerName}", color = Color.Gray, fontSize = 12.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Member: ${session.memberName}", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            session.exercises.forEachIndexed { index, ex ->
                Text(
                    text = "${index + 1}. ${ex.name} | ${ex.sets} set x ${ex.reps} | ${ex.weight}",
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
            
            if (session.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.03f)
                ) {
                    Text(
                        text = "Catatan: ${session.notes}",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}
