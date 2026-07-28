// LoginScreen.kt - Layar masuk aplikasi

package com.chiko0085.testgym.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chiko0085.testgym.model.Admin
import com.chiko0085.testgym.model.Member
import com.chiko0085.testgym.model.Trainer
import com.chiko0085.testgym.ui.theme.TextSub
import com.chiko0085.testgym.openEmailClient
private val NightBlack   = Color(0xFF080C14)
private val DeepNavy     = Color(0xFF0A1628)
private val NavyMid      = Color(0xFF0D1F3C)
private val RoyalBlue    = Color(0xFF1A4A8A)
private val ElectricBlue = Color(0xFF2563EB)
private val NeonBlue     = Color(0xFF3B82F6)
private val ArcBlue      = Color(0xFF60A5FA)
private val TextPrimary  = Color(0xFFF1F5F9)
private val ErrorRed     = Color(0xFFEF4444)
private val GlassWhite   = Color(0x14FFFFFF)
private val GlassBorder  = Color(0x26FFFFFF)

// Komponen layar login
@Composable
fun LoginScreen(
    onLoginSuccess: (String, Any?) -> Unit,
    memberList: List<Member>,
    trainerList: List<Trainer>,
    adminList: List<Admin>
) {
    var username by remember { mutableStateOf(com.chiko0085.testgym.getSetting("draft_username") ?: "") }
    var password by remember { mutableStateOf(com.chiko0085.testgym.getSetting("draft_password") ?: "") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isVisible by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { isVisible = true }

    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.70f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    val glowRadius by infiniteTransition.animateFloat(
        initialValue = 280f,
        targetValue = 380f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowRadius"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(NightBlack, DeepNavy, NavyMid)))
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(ElectricBlue.copy(alpha = glowAlpha * 0.45f), Color.Transparent),
                        center = Offset(size.width * 0.85f, size.height * 0.15f),
                        radius = glowRadius * 1.6f
                    ),
                    radius = glowRadius * 1.6f,
                    center = Offset(size.width * 0.85f, size.height * 0.15f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(RoyalBlue.copy(alpha = glowAlpha * 0.35f), Color.Transparent),
                        center = Offset(size.width * 0.12f, size.height * 0.88f),
                        radius = glowRadius * 1.2f
                    ),
                    radius = glowRadius * 1.2f,
                    center = Offset(size.width * 0.12f, size.height * 0.88f)
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(320.dp)
                .offset(x = 120.dp, y = (-160).dp)
                .clip(CircleShape)
                .background(ElectricBlue.copy(alpha = 0.08f))
                .blur(80.dp)
                .align(Alignment.TopEnd)
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = (-80).dp, y = 100.dp)
                .clip(CircleShape)
                .background(ArcBlue.copy(alpha = 0.06f))
                .blur(60.dp)
                .align(Alignment.BottomStart)
        )

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(600)) + slideInVertically(
                animationSpec = tween(700, easing = EaseOutBack),
                initialOffsetY = { it / 4 }
            )
        ) {
            Card(
                modifier = Modifier.width(400.dp).padding(20.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(colors = listOf(GlassWhite, Color(0x0AFFFFFF))))
                        .drawBehind {
                            drawRoundRect(
                                brush = Brush.linearGradient(colors = listOf(GlassBorder, NeonBlue.copy(alpha = 0.40f), GlassBorder)),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(28.dp.toPx()),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.2f)
                            )
                        }
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(36.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Brush.radialGradient(colors = listOf(ElectricBlue.copy(alpha = 0.35f), RoyalBlue.copy(alpha = 0.15f))))
                                .drawBehind {
                                    drawCircle(
                                        brush = Brush.radialGradient(colors = listOf(NeonBlue.copy(alpha = glowAlpha * 0.6f), Color.Transparent)),
                                        radius = size.minDimension * 0.75f
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = "Logo", modifier = Modifier.size(38.dp), tint = ArcBlue)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "YOUTH GYM",
                            style = TextStyle(
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 5.sp,
                                brush = Brush.horizontalGradient(colors = listOf(ArcBlue, Color(0xFFBAE6FD))),
                                shadow = Shadow(color = ElectricBlue.copy(alpha = 0.6f), offset = Offset(0f, 2f), blurRadius = 16f)
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Box(modifier = Modifier.width(48.dp).height(2.dp).background(Brush.horizontalGradient(colors = listOf(Color.Transparent, NeonBlue, Color.Transparent)), shape = RoundedCornerShape(50)))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Silakan masuk ke akun Anda", fontSize = 13.sp, color = TextSub, letterSpacing = 0.3.sp)
                        Spacer(modifier = Modifier.height(32.dp))

                        ElegantTextField(
                            value = username,
                            onValueChange = { 
                                username = it
                                errorMessage = "" 
                                com.chiko0085.testgym.saveSetting("draft_username", it)
                            },
                            label = "Username",
                            leadingContent = { Icon(Icons.Default.Person, null, tint = if (username.isNotEmpty()) NeonBlue else TextSub, modifier = Modifier.size(20.dp)) },
                            isError = errorMessage.isNotEmpty()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        ElegantTextField(
                            value = password,
                            onValueChange = { 
                                password = it
                                errorMessage = "" 
                                com.chiko0085.testgym.saveSetting("draft_password", it)
                            },
                            label = "Password",
                            leadingContent = { Icon(Icons.Default.Lock, null, tint = if (password.isNotEmpty()) NeonBlue else TextSub, modifier = Modifier.size(20.dp)) },
                            trailingContent = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = "Toggle password", tint = TextSub, modifier = Modifier.size(20.dp))
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            isError = errorMessage.isNotEmpty()
                        )

                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                            TextButton(onClick = { showForgotPasswordDialog = true }) {
                                Text("Lupa Password?", color = ArcBlue, fontSize = 12.sp)
                            }
                        }

                        AnimatedVisibility(
                            visible = errorMessage.isNotEmpty(),
                            enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { -8 }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 10.dp).clip(RoundedCornerShape(8.dp)).background(ErrorRed.copy(alpha = 0.12f)).padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(ErrorRed))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = errorMessage, color = ErrorRed, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        Button(
                            onClick = {
                                if (username.isBlank() || password.isBlank()) {
                                    errorMessage = "Username dan Password tidak boleh kosong!"
                                    return@Button
                                }
                                
                                val admin = adminList.find { it.username == username && it.password == password }
                                if (admin != null) {
                                    com.chiko0085.testgym.saveSetting("session_role", admin.role)
                                    com.chiko0085.testgym.saveSetting("session_user_id", admin.username)
                                    onLoginSuccess(admin.role, admin)
                                    return@Button
                                }
                                
                                val member = memberList.find { it.username == username && it.password == password }
                                if (member != null) {
                                    com.chiko0085.testgym.saveSetting("session_role", "member")
                                    com.chiko0085.testgym.saveSetting("session_user_id", member.id)
                                    onLoginSuccess("member", member)
                                    return@Button
                                }

                                val trainer = trainerList.find { it.username == username && it.password == password }
                                if (trainer != null) {
                                    com.chiko0085.testgym.saveSetting("session_role", "trainer")
                                    com.chiko0085.testgym.saveSetting("session_user_id", trainer.id)
                                    onLoginSuccess("trainer", trainer)
                                    return@Button
                                }
                                errorMessage = "Login Gagal"
                            },
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(colors = listOf(RoyalBlue, ElectricBlue, NeonBlue)), shape = RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "MASUK", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 3.sp, color = TextPrimary, style = TextStyle(shadow = Shadow(color = ElectricBlue, blurRadius = 8f)))
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        Text(text = "© Youth Gym Management System", fontSize = 11.sp, color = TextSub.copy(alpha = 0.5f), letterSpacing = 0.5.sp)
                    }
                }
            }
        }
    }

    if (showForgotPasswordDialog) {
        ForgotPasswordDialog(
            memberList = memberList,
            onDismiss = { showForgotPasswordDialog = false }
        )
    }
}

// Dialog pemulihan kata sandi
@Composable
private fun ForgotPasswordDialog(
    memberList: List<Member>,
    onDismiss: () -> Unit
) {
    var identifier by remember { mutableStateOf("") }
    var resultMessage by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepNavy,
        titleContentColor = TextPrimary,
        textContentColor = TextSub,
        title = { Text("Lupa Password", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Masukkan Username atau Email Anda untuk memulihkan kata sandi.",
                    fontSize = 14.sp,
                    color = TextSub
                )

                OutlinedTextField(
                    value = identifier,
                    onValueChange = { identifier = it; resultMessage = "" },
                    label = { Text("Username / Email", color = TextSub) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = NeonBlue,
                        unfocusedBorderColor = GlassBorder,
                        cursorColor = NeonBlue
                    )
                )

                if (resultMessage.isNotEmpty()) {
                    Text(
                        text = resultMessage,
                        color = if (isError) ErrorRed else ArcBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val member = memberList.find {
                        it.username.equals(identifier, ignoreCase = true) ||
                                it.email.equals(identifier, ignoreCase = true)
                    }
                    if (member != null) {
                        isError = false
                        resultMessage = "Akun ditemukan! Silakan cek email Anda."
                        openEmailClient(
                            member.email,
                            "Pemulihan Kata Sandi Youth Gym",
                            "Halo ${member.name},\n\nAnda meminta pemulihan kata sandi.\nUsername: ${member.username}\nPassword: ${member.password}\n\nSilakan gunakan data tersebut untuk masuk kembali."
                        )
                    } else {
                        isError = true
                        resultMessage = "Akun tidak ditemukan!"
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
            ) {
                Text("Kirim Detail", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = TextSub)
            }
        }
    )
}

// Input teks kustom yang elegan
@Composable
private fun ElegantTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    isError: Boolean = false
) {
    val isFocused = value.isNotEmpty()
    val borderColor = when {
        isError -> ErrorRed.copy(alpha = 0.8f)
        isFocused -> NeonBlue.copy(alpha = 0.7f)
        else -> GlassBorder
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.linearGradient(colors = listOf(Color(0x1A1E40AF), Color(0x0D1E3A5F))))
            .drawBehind {
                drawRoundRect(color = borderColor, cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx()), style = androidx.compose.ui.graphics.drawscope.Stroke(1f))
            }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, color = if (isFocused) ArcBlue else TextSub, fontSize = 13.sp) },
            leadingIcon = leadingContent,
            trailingIcon = trailingContent,
            visualTransformation = visualTransformation,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            isError = isError,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = NeonBlue,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                errorBorderColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent,
                focusedLabelColor = ArcBlue,
                unfocusedLabelColor = TextSub,
                errorLabelColor = ErrorRed,
            )
        )
    }
}