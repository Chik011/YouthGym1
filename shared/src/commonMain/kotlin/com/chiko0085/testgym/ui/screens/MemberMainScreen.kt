// MemberMainScreen.kt - Tampilan utama member gym

package com.chiko0085.testgym.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import youthgym.shared.generated.resources.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.chiko0085.testgym.model.Member
import com.chiko0085.testgym.model.Reservation
import com.chiko0085.testgym.model.Trainer

import com.chiko0085.testgym.openWebLink
import com.chiko0085.testgym.db
import com.chiko0085.testgym.formatEpochToDate
import com.chiko0085.testgym.getCurrentTimeMillis
import com.chiko0085.testgym.rememberImagePicker
import com.chiko0085.testgym.isStorageSupported
import com.chiko0085.testgym.createStorageData

import dev.gitlive.firebase.firestore.*

import com.chiko0085.testgym.ui.theme.AccentBlue
import com.chiko0085.testgym.ui.theme.AccentBlueDark
import com.chiko0085.testgym.ui.theme.CardDark
import com.chiko0085.testgym.ui.theme.DarkBgEnd
import com.chiko0085.testgym.ui.theme.DarkBgStart
import com.chiko0085.testgym.ui.theme.TextSub

import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.round

// Fungsi utilitas format desimal
fun formatDecimal(value: Double): String {
    return (round(value * 10) / 10.0).toString()
}

// Layar kontainer navigasi member
@Composable
fun MemberMainScreen(
    initialMember: Member,
    memberList: List<Member>,
    onLogout: () -> Unit,
    onUpdateMember: (Member) -> Unit,
    onUpdatePhotoClick: (ByteArray) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val currentMember = memberList.find { it.id == initialMember.id } ?: initialMember
    val bgGradient = Brush.verticalGradient(listOf(DarkBgStart, DarkBgEnd))

    Box(modifier = Modifier.fillMaxSize().background(bgGradient)) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                NavigationBar(
                    containerColor = DarkBgEnd,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("Dahsboard") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentBlue, selectedTextColor = AccentBlue,
                            unselectedIconColor = TextSub, unselectedTextColor = TextSub,
                            indicatorColor = CardDark
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
                        label = { Text("Padel") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentBlue, selectedTextColor = AccentBlue,
                            unselectedIconColor = TextSub, unselectedTextColor = TextSub,
                            indicatorColor = CardDark
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Default.PhotoCamera, contentDescription = null) },
                        label = { Text("Scan") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentBlue, selectedTextColor = AccentBlue,
                            unselectedIconColor = TextSub, unselectedTextColor = TextSub,
                            indicatorColor = CardDark
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = { Icon(Icons.Default.Info, contentDescription = null) },
                        label = { Text("About") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentBlue, selectedTextColor = AccentBlue,
                            unselectedIconColor = TextSub, unselectedTextColor = TextSub,
                            indicatorColor = CardDark
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        icon = { Icon(Icons.Default.Person, contentDescription = null) },
                        label = { Text("Profil") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentBlue, selectedTextColor = AccentBlue,
                            unselectedIconColor = TextSub, unselectedTextColor = TextSub,
                            indicatorColor = CardDark
                        )
                    )
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                when (selectedTab) {
                    0 -> MemberHomeScreen(currentMember)
                    1 -> PadelScreen(currentMember)
                    2 -> {
                        var isProcessing by remember { mutableStateOf(false) }
                        var showResultDialog by remember { mutableStateOf(false) }
                        var dialogMessage by remember { mutableStateOf("") }
                        val scope = rememberCoroutineScope()

                        Box(modifier = Modifier.fillMaxSize()) {
                            MemberScanScreen(
                                title = "QR Kehadiran",
                                onResult = { qrData ->
                                    if (!isProcessing && !showResultDialog) {
                                        isProcessing = true
                                        if (qrData == "CHECKIN_YOUTH_GYM" || qrData.isNotEmpty()) {
                                            if (currentMember.remainingDays > 0) {
                                                scope.launch {
                                                    try {
                                                        val newQuota = currentMember.remainingDays - 1
                                                        val updatedMember = currentMember.copy(remainingDays = newQuota)
                                                        db.collection("members").document(currentMember.id).set(updatedMember)
                                                        onUpdateMember(updatedMember)
                                                        dialogMessage = "Check-in Berhasil!\nSisa kuota kamu sekarang: $newQuota sesi."
                                                        showResultDialog = true
                                                    } catch (e: Exception) {
                                                        dialogMessage = "Gagal Check-in. Periksa koneksi internetmu."
                                                        showResultDialog = true
                                                    } finally {
                                                        isProcessing = false
                                                    }
                                                }
                                            } else {
                                                dialogMessage = "Akses Ditolak!\nKuota latihan kamu sudah habis."
                                                showResultDialog = true
                                                isProcessing = false
                                            }
                                        }
                                    }
                                }
                            )

                            if (showResultDialog) {
                                AlertDialog(
                                    onDismissRequest = { showResultDialog = false },
                                    containerColor = Color(0xFF112240),
                                    title = { Text("Hasil Scan", color = Color.White, fontWeight = FontWeight.Bold) },
                                    text = { Text(dialogMessage, color = Color.LightGray) },
                                    confirmButton = {
                                        Button(
                                            onClick = { showResultDialog = false },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                                        ) {
                                            Text("Tutup", color = Color.White)
                                        }
                                    }
                                )
                            }
                        }
                    }
                    3 -> AboutUsScreen()
                    4 -> MemberProfileScreen(currentMember, onLogout, onUpdatePhotoClick)
                }
            }
        }
    }
}

// Halaman beranda member
@Composable
fun MemberHomeScreen(member: Member) {
    var weightInput by remember { mutableStateOf(if ((member.weight ?: 0.0) > 0) (member.weight?.toString() ?: "") else "") }
    var heightInput by remember { mutableStateOf(if ((member.height ?: 0.0) > 0) (member.height?.toString() ?: "") else "") }
    var showTutorialCamera by remember { mutableStateOf(false) }
    var selectedTrainer by remember { mutableStateOf<Trainer?>(null) }
    val trainers = remember { mutableStateListOf<Trainer>() }

    val currentTime = com.chiko0085.testgym.getCurrentTimeMillis()
    val calendarDaysLeft = if (member.expiredDate <= 0L) member.remainingDays
    else ((member.expiredDate - currentTime) / 86400000L).toInt()

    val isActive = member.remainingDays > 0 && calendarDaysLeft > 0

    val statusColor = when {
        member.remainingDays < 5 -> Color(0xFFF87171)
        member.remainingDays in 5..10 -> Color(0xFFFBBF24)
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
                shape = RoundedCornerShape(24.dp),
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
                    Text(
                        text = if (isActive) "${member.remainingDays}" else "HABIS",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.3f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Masa Aktif", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                            Text("$calendarDaysLeft Hari", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color.White.copy(alpha = 0.3f)))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Sesi PT", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                            Text("${member.remainingPtSessions}X", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    val expStr = formatEpochToDate(member.expiredDate).split(" ").take(3).joinToString(" ")
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
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("BMI & Ideal Weight Calculator", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
                    Text("Pantau kondisi fisikmu", fontSize = 12.sp, color = TextSub)
                    Spacer(modifier = Modifier.height(16.dp))

                    val textFieldColors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentBlue, unfocusedBorderColor = TextSub.copy(alpha = 0.3f),
                        cursorColor = AccentBlue, focusedLabelColor = AccentBlue, unfocusedLabelColor = TextSub
                    )

                    OutlinedTextField(
                        value = weightInput, onValueChange = { weightInput = it },
                        label = { Text("Berat (kg)") }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp), colors = textFieldColors, singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = heightInput, onValueChange = { heightInput = it },
                        label = { Text("Tinggi (cm)") }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp), colors = textFieldColors, singleLine = true
                    )

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

// Layar reservasi lapangan padel
@Composable
fun PadelScreen(member: Member) {
    var selectedDateMillis by remember { mutableStateOf(getCurrentTimeMillis()) }
    var reservations by remember { mutableStateOf<List<Reservation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(selectedDateMillis) {
        isLoading = true
        try {
            val dateStr = formatEpochToDate(selectedDateMillis).split(" ").take(3).joinToString(" ")
            val dayStart = com.chiko0085.testgym.parseDateToMillis(dateStr) ?: selectedDateMillis
            val snapshot = db.collection("reservations")
                .where("date", equalTo = dayStart)
                .get()

            val dbReservations = snapshot.documents.map { it.data<Reservation>() }
            val fullList = mutableListOf<Reservation>()
            for (i in 8..23) {
                val found = dbReservations.find { it.hour == i }
                fullList.add(found ?: Reservation(
                    id = "${dayStart}_$i",
                    date = dayStart,
                    hour = i,
                    status = "Empty"
                ))
            }
            reservations = fullList
        } catch (e: Exception) {
            println("ERROR: Gagal ambil reservasi: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Reservasi Lapangan Padel", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Pilih Tanggal", color = TextSub, fontSize = 12.sp)
                    Text(
                        formatEpochToDate(selectedDateMillis).split(" ").take(3).joinToString(" "),
                        color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp
                    )
                }
                Row {
                    IconButton(onClick = { selectedDateMillis -= 86400000L }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                    IconButton(onClick = { selectedDateMillis += 86400000L }) {
                        Icon(Icons.Default.ArrowForward, null, tint = Color.White)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentBlue)
            }
        } else {
            androidx.compose.foundation.lazy.LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(reservations) { res ->
                    MemberReservationCard(res, onBook = {
                        val dateStr = formatEpochToDate(res.date).split(" ").take(3).joinToString(" ")
                        val hourStr = if (res.hour < 10) "0${res.hour}:00" else "${res.hour}:00"
                        val message = "Halo Admin Youth Gym, saya ${member.name} ingin booking lapangan Padel untuk tanggal $dateStr jam $hourStr. Apakah masih tersedia?"
                        val encodedMessage = message.replace(" ", "%20")
                        openWebLink("https://wa.me/6285166322618?text=$encodedMessage")
                    })
                }
            }
        }
    }
}

// Kartu slot reservasi
@Composable
fun MemberReservationCard(reservation: Reservation, onBook: () -> Unit) {
    val hourStr = if (reservation.hour < 10) "0${reservation.hour}:00" else "${reservation.hour}:00"
    val nextHour = if (reservation.hour + 1 < 10) "0${reservation.hour + 1}:00" else "${reservation.hour + 1}:00"
    val isAvailable = reservation.status == "Empty"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (isAvailable) CardDark else CardDark.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("$hourStr - $nextHour", color = Color.White, fontWeight = FontWeight.Bold)
                Text(
                    text = if (isAvailable) "Tersedia" else "Sudah Dipesan",
                    color = if (isAvailable) Color(0xFF34D399) else Color(0xFFF87171),
                    fontSize = 12.sp
                )
            }
            if (isAvailable) {
                Button(
                    onClick = onBook,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Booking", color = Color.White, fontSize = 12.sp)
                }
            } else {
                Text(reservation.reservedByName, color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

// Halaman informasi gym
@Composable
fun AboutUsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("About Us", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Youth Gym", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AccentBlue)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Aplikasi Manajemen Fasilitas Olahraga Pintar yang dirancang untuk memudahkan member dalam memantau sisa kuota latihan dan reservasi fasilitas olahraga modern.",
                    color = Color.LightGray, textAlign = TextAlign.Justify
                )
            }
        }
        Text("Fasilitas Gym", fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.fillMaxWidth())
        val gymImages = listOf(
            Res.drawable.gym1, Res.drawable.gym2, Res.drawable.gym3, Res.drawable.gym4,
            Res.drawable.gym5, Res.drawable.gym6, Res.drawable.gym7, Res.drawable.gym8
        )
        FacilityGallery(images = gymImages)
        Text("Lapangan Padel", fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.fillMaxWidth())
        val padelImages = listOf(
            Res.drawable.padel1, Res.drawable.padel2, Res.drawable.padel3, Res.drawable.padel4,
            Res.drawable.padel5, Res.drawable.padel6, Res.drawable.padel7, Res.drawable.padel8
        )
        FacilityGallery(images = padelImages)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark)
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = Color(0xFFF87171))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Lokasi Kami", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { openWebLink("https://maps.app.goo.gl/1qQr52B4W7vgrKa47") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Place, null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Buka di Google Maps", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

// Grid galeri fasilitas
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FacilityGallery(images: List<org.jetbrains.compose.resources.DrawableResource>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 4
    ) {
        images.forEach { res ->
            Card(
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .widthIn(min = 80.dp, max = 200.dp)
                    .aspectRatio(1f)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = CardDark)
            ) {
                Image(
                    painter = painterResource(res),
                    contentDescription = "Facility Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

// Halaman profil member
@Composable
fun MemberProfileScreen(member: Member, onLogout: () -> Unit, onUpdatePhotoClick: (ByteArray) -> Unit) {
    val scope = rememberCoroutineScope()
    var isUploading by remember { mutableStateOf(false) }

    LaunchedEffect(member.profileImageUrl) {
        isUploading = false
    }

    val launchImagePicker = rememberImagePicker(
        onResult = { imageBytes ->
            isUploading = true
            onUpdatePhotoClick(imageBytes)
        }
    )

    var name by remember { mutableStateOf(member.name) }
    var weight by remember { mutableStateOf(if ((member.weight ?: 0.0) > 0) (member.weight?.toString() ?: "") else "") }
    var height by remember { mutableStateOf(if ((member.height ?: 0.0) > 0) (member.height?.toString() ?: "") else "") }
    var gender by remember { mutableStateOf(member.gender) }
    var username by remember { mutableStateOf(member.username) }
    var password by remember { mutableStateOf(member.password) }
    var passwordVisible by remember { mutableStateOf(false) }

    var isSaving by remember { mutableStateOf(false) }
    var showSecurityDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val currentTime = com.chiko0085.testgym.getCurrentTimeMillis()
    val calendarDaysLeft = if (member.expiredDate <= 0L) member.remainingDays
    else ((member.expiredDate - currentTime) / 86400000L).toInt()
    val isActive = member.remainingDays > 0 && calendarDaysLeft > 0

    val performSave = {
        isSaving = true
        val updatedWeight = weight.toDoubleOrNull() ?: 0.0
        val updatedHeight = height.toDoubleOrNull() ?: 0.0
        val updatedMember = member.copy(
            name = name,
            weight = updatedWeight,
            height = updatedHeight,
            username = username,
            password = password,
            gender = gender
        )
        scope.launch {
            try {
                db.collection("members").document(member.id).set(updatedMember)
                member.name = name
                member.weight = updatedWeight
                member.height = updatedHeight
                member.username = username
                member.password = password
                member.gender = gender
                snackbarHostState.showSnackbar("Data berhasil disimpan!")
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Gagal menyimpan perubahan. Coba lagi.")
            } finally {
                isSaving = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Profil Saya", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Spacer(modifier = Modifier.height(24.dp))

            Box(contentAlignment = Alignment.BottomEnd) {
                Surface(
                    modifier = Modifier
                        .size(100.dp)
                        .border(2.dp, AccentBlue, CircleShape),
                    shape = CircleShape,
                    color = AccentBlue.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isUploading) {
                            CircularProgressIndicator(color = AccentBlue, modifier = Modifier.size(30.dp))
                        } else if (member.profileImageUrl.isNotEmpty()) {
                            KamelImage(
                                resource = asyncPainterResource(data = member.profileImageUrl),
                                contentDescription = "Profile",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            val defaultIcon = if (gender == "Perempuan") Icons.Default.Face else Icons.Default.Person
                            Icon(defaultIcon, null, modifier = Modifier.size(60.dp), tint = AccentBlue)
                        }
                    }
                }
                Surface(
                    modifier = Modifier
                        .size(32.dp)
                        .offset(x = 4.dp, y = 4.dp)
                        .clickable { launchImagePicker() },
                    shape = CircleShape,
                    color = AccentBlue,
                    border = androidx.compose.foundation.BorderStroke(2.dp, CardDark)
                ) {
                    Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.padding(6.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = if (isActive) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isActive) "Status: AKTIF (Sisa $calendarDaysLeft Hari)" else "Status: EXPIRED",
                    color = if (isActive) Color(0xFF34D399) else Color(0xFFF87171),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            val tfColors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                focusedBorderColor = AccentBlue, unfocusedBorderColor = TextSub.copy(alpha = 0.5f),
                focusedLabelColor = AccentBlue, unfocusedLabelColor = TextSub
            )

            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Nama Lengkap") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp), colors = tfColors, singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text("Pilih Icon Profil / Gender", color = Color.White, fontSize = 14.sp, modifier = Modifier.align(Alignment.Start))
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (gender == "Laki-laki") AccentBlue.copy(alpha = 0.2f) else Color.Transparent)
                        .border(1.dp, if (gender == "Laki-laki") AccentBlue else Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .clickable { gender = "Laki-laki" }
                        .padding(12.dp)
                ) {
                    Icon(Icons.Default.Person, null, tint = if (gender == "Laki-laki") AccentBlue else Color.Gray)
                    Text("Laki-laki", color = if (gender == "Laki-laki") Color.White else Color.Gray, fontSize = 12.sp)
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (gender == "Perempuan") Color(0xFFEC4899).copy(alpha = 0.2f) else Color.Transparent)
                        .border(1.dp, if (gender == "Perempuan") Color(0xFFEC4899) else Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .clickable { gender = "Perempuan" }
                        .padding(12.dp)
                ) {
                    Icon(Icons.Default.Face, null, tint = if (gender == "Perempuan") Color(0xFFEC4899) else Color.Gray)
                    Text("Perempuan", color = if (gender == "Perempuan") Color.White else Color.Gray, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = weight, onValueChange = { weight = it },
                    label = { Text("Berat (kg)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp), colors = tfColors, singleLine = true
                )
                OutlinedTextField(
                    value = height, onValueChange = { height = it },
                    label = { Text("Tinggi (cm)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp), colors = tfColors, singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(16.dp))

            Text("Keamanan Akun", fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = username, onValueChange = { username = it },
                label = { Text("Username Login") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp), colors = tfColors, singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("Password Baru") },
                visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.CheckCircle else Icons.Filled.Lock
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(image, "Toggle Password Visibility", tint = TextSub)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp), colors = tfColors, singleLine = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (isSaving) return@Button
                    val isAccountChanged = username != member.username || password != member.password
                    if (isAccountChanged) {
                        showSecurityDialog = true
                    } else {
                        performSave()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("Simpan Perubahan", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onLogout) {
                Text("Logout / Keluar", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(80.dp))
        }

        if (showSecurityDialog) {
            AlertDialog(
                onDismissRequest = { showSecurityDialog = false },
                containerColor = Color(0xFF112240),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = "Warning", tint = Color(0xFFFBBF24))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verifikasi Keamanan", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Text(
                        "Anda mendeteksi perubahan pada Username atau Password.\n\nPastikan data yang dimasukkan sudah benar karena ini akan digunakan untuk login Anda selanjutnya. Apakah Anda yakin ingin menyimpannya?",
                        color = Color.LightGray,
                        lineHeight = 20.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showSecurityDialog = false
                            performSave()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF87171)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Ya, Saya Yakin", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSecurityDialog = false }) {
                        Text("Batal", color = Color.Gray)
                    }
                }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = Color(0xFF1E293B),
                contentColor = Color.White,
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}