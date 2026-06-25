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

import com.chiko0085.testgym.ui.screens.member.*

// Layar kontainer navigasi member
@Composable
fun MemberMainScreen(
    initialMember: Member,
    onLogout: () -> Unit,
    onUpdateMember: (Member) -> Unit,
    onUpdatePhotoClick: (ByteArray) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    
    // Gunakan state untuk member agar reaktif terhadap snapshot
    var currentMember by remember { mutableStateOf(initialMember) }

    // Dapatkan data member paling update dari Firebase secara real-time
    LaunchedEffect(initialMember.id) {
        try {
            db.collection("members").document(initialMember.id).snapshots.collect { snapshot ->
                if (snapshot.exists) {
                    val updated = snapshot.data<Member>()
                    currentMember = updated
                    onUpdateMember(updated)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

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