package com.chiko0085.testgym.ui.screens.member

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chiko0085.testgym.db
import com.chiko0085.testgym.formatEpochToDate
import com.chiko0085.testgym.getCurrentTimeMillis
import com.chiko0085.testgym.model.Member
import com.chiko0085.testgym.model.Trainer
import com.chiko0085.testgym.openWebLink
import com.chiko0085.testgym.ui.screens.MemberScanScreen
import com.chiko0085.testgym.ui.theme.AccentBlue
import com.chiko0085.testgym.ui.theme.CardDark
import com.chiko0085.testgym.ui.theme.TextSub
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.flow.collect
import org.jetbrains.compose.resources.painterResource
import youthgym.shared.generated.resources.Res
import youthgym.shared.generated.resources.pt_chiko
import youthgym.shared.generated.resources.pt_gabriel
import youthgym.shared.generated.resources.pt_marchel
import kotlin.math.pow
import kotlin.math.round

// Fungsi utilitas format desimal
fun formatDecimal(value: Double): String {
    return (round(value * 10) / 10.0).toString()
}

// Halaman beranda member
@Composable
fun MemberHomeScreen(member: Member) {
    var weightInput by remember { mutableStateOf(if ((member.weight ?: 0.0) > 0) (member.weight?.toString() ?: "") else "") }
    var heightInput by remember { mutableStateOf(if ((member.height ?: 0.0) > 0) (member.height?.toString() ?: "") else "") }
    var showTutorialCamera by remember { mutableStateOf(false) }
    var selectedTrainer by remember { mutableStateOf<Trainer?>(null) }
    val trainers = remember { mutableStateListOf<Trainer>() }

    val currentTime = getCurrentTimeMillis()
    val calendarDaysLeft = if (member.expiredDate <= 0L) maxOf(0, member.remainingDays)
    else maxOf(0, ((member.expiredDate - currentTime) / 86400000L).toInt())

    // Gunakan nilai tertinggi antara hitungan kalender atau manual remainingDays
    val displayDays = maxOf(member.remainingDays, calendarDaysLeft)
    val isActive = displayDays > 0

    val statusColor = when {
        displayDays < 5 -> Color(0xFFF87171)
        displayDays in 5..10 -> Color(0xFFFBBF24)
        else -> AccentBlue
    }

    LaunchedEffect(Unit) {
        try {
            db.collection("trainers").snapshots.collect { snapshot ->
                val dbTrainers = snapshot.documents.map { it.data<Trainer>() }
                val updatedTrainers = dbTrainers.map { trainer ->
                    val nameLower = trainer.name.lowercase()
                    when {
                        nameLower.contains("chiko") -> if (trainer.profileImageUrl.isEmpty()) trainer.copy(profileImageUrl = "res:pt_chiko") else trainer
                        nameLower.contains("gabriel") -> if (trainer.profileImageUrl.isEmpty()) trainer.copy(profileImageUrl = "res:pt_gabriel") else trainer
                        nameLower.contains("marchel") -> if (trainer.profileImageUrl.isEmpty()) trainer.copy(profileImageUrl = "res:pt_marchel") else trainer
                        else -> trainer
                    }
                }
                trainers.clear()
                trainers.addAll(updatedTrainers)
            }
        } catch (e: Exception) {
            println("Gagal mendengarkan data trainer: ${e.message}")
        }
    }

    if (showTutorialCamera) {
        Box(modifier = Modifier.fillMaxSize()) {
            MemberScanScreen(
                title = "QR Code Alat",
                onResult = { scannedText ->
                    showTutorialCamera = false
                    val cleanedText = scannedText.trim()
                    if (cleanedText.startsWith("http://", ignoreCase = true) ||
                        cleanedText.startsWith("https://", ignoreCase = true)) {
                        openWebLink(cleanedText)
                    } else if (cleanedText.contains(".") && !cleanedText.contains(" ")) {
                        openWebLink("https://$cleanedText")
                    }
                }
            )
            IconButton(
                onClick = { showTutorialCamera = false },
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopEnd)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Selamat Datang,", fontSize = 16.sp, color = TextSub)
                    Text(member.name, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(CardDark)
                        .border(1.dp, AccentBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (member.profileImageUrl.isNotEmpty()) {
                        KamelImage(
                            resource = asyncPainterResource(data = member.profileImageUrl),
                            contentDescription = "Profile",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        val defaultIcon = if (member.gender == "Perempuan") Icons.Default.Face else Icons.Default.Person
                        Icon(defaultIcon, null, tint = Color.Gray, modifier = Modifier.size(30.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            MemberCalendarCard()
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .background(Brush.verticalGradient(listOf(statusColor.copy(alpha = 0.8f), statusColor)))
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Sisa Kuota Latihan", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(140.dp)) {
                        CircularProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.fillMaxSize(),
                            color = Color.White.copy(alpha = 0.2f),
                            strokeWidth = 8.dp
                        )
                        CircularProgressIndicator(
                            progress = { (displayDays / 30f).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxSize(),
                            color = Color.White,
                            strokeWidth = 8.dp,
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                        Text(
                            text = if (isActive) "$displayDays" else "0",
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.3f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Masa Aktif", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                            Text("$displayDays Hari", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color.White.copy(alpha = 0.3f)))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Sesi PT", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                            Text("${member.remainingPtSessions}X", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Logika tampilan tanggal: Jika expiredDate lama/ngaco tapi hari masih ada, hitung visual saja
                    val finalExpiredDate = if (member.remainingDays > 0 && member.expiredDate < currentTime) {
                        currentTime + (member.remainingDays * 86400000L)
                    } else {
                        member.expiredDate
                    }
                    
                    val expStr = com.chiko0085.testgym.formatEpochToDate(finalExpiredDate).split(" ").take(3).joinToString(" ")
                    Surface(color = Color.Black.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp)) {
                        Text(
                            "Berlaku hingga: $expStr",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 12.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { 
                        val message = "Halo Admin Youth Gym, saya ${member.name}. Saya ingin bertanya mengenai..."
                        val encodedMessage = message.replace(" ", "%20")
                        openWebLink("https://wa.me/6285166322618?text=$encodedMessage") 
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Call, null, modifier = Modifier.size(18.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Button(
                    onClick = { showTutorialCamera = true },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CardDark),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp), tint = AccentBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("QR Alat", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentBlue)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Personal Trainer Kami", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color.White)
                Text("Pilih ahlinya untuk capai body goals-mu!", fontSize = 12.sp, color = TextSub)
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(end = 16.dp)
                ) {
                    items(trainers) { trainer ->
                        TrainerCard(trainer = trainer, onClick = { selectedTrainer = trainer })
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.MonitorWeight, null, tint = AccentBlue, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("BMI Calculator", fontWeight = FontWeight.Black, fontSize = 22.sp, color = Color.White)
                    Text("Ketahui berat ideal & status kesehatanmu", fontSize = 13.sp, color = TextSub)
                    Spacer(modifier = Modifier.height(24.dp))

                    val textFieldColors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentBlue, unfocusedBorderColor = TextSub.copy(alpha = 0.3f),
                        cursorColor = AccentBlue, focusedLabelColor = AccentBlue, unfocusedLabelColor = TextSub,
                        focusedContainerColor = Color(0xFF1E293B), unfocusedContainerColor = Color(0xFF0F172A)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = weightInput, onValueChange = { weightInput = it },
                            label = { Text("Berat (kg)") }, modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp), colors = textFieldColors, singleLine = true
                        )
                        OutlinedTextField(
                            value = heightInput, onValueChange = { heightInput = it },
                            label = { Text("Tinggi (cm)") }, modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp), colors = textFieldColors, singleLine = true
                        )
                    }

                    val weight = weightInput.toDoubleOrNull() ?: 0.0
                    val height = heightInput.toDoubleOrNull() ?: 0.0

                    if (weight > 0 && height > 0) {
                        val heightInMeters = height / 100.0
                        val bmi = weight / heightInMeters.pow(2.0)
                        val category = when {
                            bmi < 18.5 -> "Kurus"
                            bmi < 24.9 -> "Normal"
                            bmi < 29.9 -> "Overweight"
                            else -> "Obesitas"
                        }
                        val idealLow = 18.5 * heightInMeters.pow(2.0)
                        val idealHigh = 24.9 * heightInMeters.pow(2.0)

                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            color = if (category == "Normal") Color(0xFF10B981).copy(alpha = 0.15f)
                            else Color(0xFFEF4444).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "BMI: ${formatDecimal(bmi)} ($category)",
                                    fontWeight = FontWeight.Bold, fontSize = 18.sp,
                                    color = if (category == "Normal") Color(0xFF34D399) else Color(0xFFF87171)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Berat Badan Ideal Anda:", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White)
                                Text(
                                    "${formatDecimal(idealLow)} kg - ${formatDecimal(idealHigh)} kg",
                                    fontWeight = FontWeight.Black, fontSize = 20.sp, color = AccentBlue
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { member.weight = weight; member.height = height },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                        ) {
                            Text("Simpan ke Profil Lokal", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    if (selectedTrainer != null) {
        TrainerProfileDialog(
            trainer = selectedTrainer!!,
            onDismiss = { selectedTrainer = null },
            onContact = {
                val trainer = selectedTrainer!!
                val phone = when {
                    trainer.name.lowercase().contains("chiko") -> "62895329092414"
                    trainer.name.lowercase().contains("gabriel") -> "6281281685858"
                    trainer.name.lowercase().contains("marchel") -> "6282213337514"
                    else -> "6285166322618"
                }
                val message = "Halo Coach ${trainer.name}, saya ${member.name}. Saya tertarik untuk mengambil program latihan dengan Anda."
                val encodedMessage = message.replace(" ", "%20")
                openWebLink("https://wa.me/$phone?text=$encodedMessage")
                selectedTrainer = null
            }
        )
    }
}

// Kartu kalender hari ini
@Composable
fun MemberCalendarCard() {
    val now = getCurrentTimeMillis()
    val dateStr = formatEpochToDate(now)
    val parts = dateStr.split(" ")
    val day = parts.getOrNull(0) ?: ""
    val month = parts.getOrNull(1) ?: ""
    val year = parts.getOrNull(2) ?: ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AccentBlue.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(month.take(3).uppercase(), color = AccentBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(day, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Hari Ini", color = TextSub, fontSize = 11.sp)
                Text("$day $month $year", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Kartu trainer di carousel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainerCard(trainer: Trainer, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(160.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(shape = CircleShape, modifier = Modifier.size(64.dp), color = AccentBlue.copy(alpha = 0.2f)) {
                if (trainer.profileImageUrl.isNotEmpty()) {
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
                            Icon(Icons.Default.Person, null, tint = AccentBlue, modifier = Modifier.padding(12.dp))
                        }
                    } else {
                        KamelImage(
                            resource = asyncPainterResource(data = trainer.profileImageUrl),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    Icon(Icons.Default.Person, null, tint = AccentBlue, modifier = Modifier.padding(12.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(trainer.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White, maxLines = 1)
            Spacer(modifier = Modifier.height(4.dp))
            Text(trainer.specialization, fontSize = 11.sp, color = TextSub, maxLines = 1)
        }
    }
}

// Dialog profil coach
@Composable
fun TrainerProfileDialog(trainer: Trainer, onDismiss: () -> Unit, onContact: () -> Unit) {
    AlertDialog(
        containerColor = CardDark,
        onDismissRequest = onDismiss,
        title = { Text("Profil Coach", fontWeight = FontWeight.Bold, color = Color.White) },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, modifier = Modifier.size(60.dp), color = AccentBlue.copy(alpha = 0.2f)) {
                        if (trainer.profileImageUrl.isNotEmpty()) {
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
                                    Icon(Icons.Default.Person, null, modifier = Modifier.padding(12.dp), tint = AccentBlue)
                                }
                            } else {
                                KamelImage(
                                    resource = asyncPainterResource(data = trainer.profileImageUrl),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        } else {
                            Icon(Icons.Default.Person, null, modifier = Modifier.padding(12.dp), tint = AccentBlue)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(trainer.name, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color.White)
                        Text(trainer.specialization, fontSize = 14.sp, color = TextSub)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Column(horizontalAlignment = Alignment.Start) {
                    Text("Pengalaman", fontSize = 12.sp, color = TextSub)
                    Text(trainer.experience, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text("Tentang Coach:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text(trainer.description, fontSize = 13.sp, color = Color.LightGray, lineHeight = 20.sp)
                Spacer(modifier = Modifier.height(24.dp))
                Text("Jadwal Melatih:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AccentBlue)
                Spacer(modifier = Modifier.height(8.dp))
                if (trainer.schedules.isEmpty()) {
                    Text("Belum ada jadwal publik.", fontSize = 12.sp, color = Color.Gray)
                } else {
                    trainer.schedules.forEach { schedule ->
                        Surface(
                            modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth(),
                            color = Color.White.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DateRange, null, tint = AccentBlue, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(schedule, fontSize = 13.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onContact,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
            ) {
                Text("Hubungi via WhatsApp", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Tutup", color = Color.Gray) }
        }
    )
}
