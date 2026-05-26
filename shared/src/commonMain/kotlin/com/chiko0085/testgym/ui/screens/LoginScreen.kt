//no 1 package com.chiko0085.testgym.ui.screens
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.input.PasswordVisualTransformation
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.chiko0085.testgym.model.Member
//
//@Composable
//fun LoginScreen(
//    onLoginSuccess: (String, Member?) -> Unit,
//    memberList: List<Member>
//) {
//    var username by remember { mutableStateOf("") }
//    var password by remember { mutableStateOf("") }
//    var errorMessage by remember { mutableStateOf("") }
//
//    Column(
//        modifier = Modifier.fillMaxSize().padding(24.dp),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text("Youth Gym", fontSize = 32.sp, fontWeight = FontWeight.Bold)
//        Spacer(modifier = Modifier.height(48.dp))
//
//        OutlinedTextField(
//            value = username,
//            onValueChange = { username = it },
//            label = { Text("Username", color = Color.Black) },
//            modifier = Modifier.fillMaxWidth(),
//            textStyle = TextStyle(color = Color.Black, fontWeight = FontWeight.Bold),
//            colors = OutlinedTextFieldDefaults.colors(
//                focusedTextColor = Color.Black,
//                unfocusedTextColor = Color.Black,
//                focusedLabelColor = Color.Black,
//                unfocusedLabelColor = Color.Black
//            )
//        )
//        Spacer(modifier = Modifier.height(16.dp))
//        OutlinedTextField(
//            value = password,
//            onValueChange = { password = it },
//            label = { Text("Password", color = Color.Black) },
//            visualTransformation = PasswordVisualTransformation(),
//            modifier = Modifier.fillMaxWidth(),
//            textStyle = TextStyle(color = Color.Black, fontWeight = FontWeight.Bold),
//            colors = OutlinedTextFieldDefaults.colors(
//                focusedTextColor = Color.Black,
//                unfocusedTextColor = Color.Black,
//                focusedLabelColor = Color.Black,
//                unfocusedLabelColor = Color.Black
//            )
//        )
//
//        if (errorMessage.isNotEmpty()) {
//            Text(errorMessage, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
//        }
//
//        Spacer(modifier = Modifier.height(32.dp))
//        Button(
//            onClick = {
//                if (username == "admin" && password == "admin123") {
//                    onLoginSuccess("admin", null)
//                } else {
//                    val member = memberList.find { it.username == username && it.password == password }
//                    if (member != null) {
//                        onLoginSuccess("member", member)
//                    } else {
//                        errorMessage = "Username atau Password salah!"
//                    }
//                }
//            },
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text("Login")
//        }
//    }
//}

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
import com.chiko0085.testgym.model.Member
import com.chiko0085.testgym.Trainer // <-- IMPORT TRAINER
import com.chiko0085.testgym.ui.theme.TextSub

// ── Palette ───────────────────────────────────────────────────────────────
private val NightBlack   = Color(0xFF080C14)
private val DeepNavy     = Color(0xFF0A1628)
private val NavyMid      = Color(0xFF0D1F3C)
private val RoyalBlue    = Color(0xFF1A4A8A)
private val ElectricBlue = Color(0xFF2563EB)
private val NeonBlue     = Color(0xFF3B82F6)
private val ArcBlue      = Color(0xFF60A5FA)
private val SteelGray    = Color(0xFF1E293B)
private val SlateGray    = Color(0xFF334155)
private val TextPrimary  = Color(0xFFF1F5F9)
private val ErrorRed     = Color(0xFFEF4444)
private val GlassWhite   = Color(0x14FFFFFF)
private val GlassBorder  = Color(0x26FFFFFF)

@Composable
fun LoginScreen(
    onLoginSuccess: (String, Any?) -> Unit, // <-- UBAH KE Any? AGAR BISA TERIMA MEMBER & TRAINER
    memberList: List<Member>,
    trainerList: List<Trainer> // <-- TAMBAHKAN PARAMETER TRAINER LIST
) {
    var username       by remember { mutableStateOf("") }
    var password       by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage   by remember { mutableStateOf("") }
    var isVisible      by remember { mutableStateOf(false) }

    // Trigger entrance animation
    LaunchedEffect(Unit) { isVisible = true }

    // Pulsing glow animation on the accent orb
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue  = 0.70f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    val glowRadius by infiniteTransition.animateFloat(
        initialValue = 280f,
        targetValue  = 380f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowRadius"
    )

    // ── Root background ───────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(NightBlack, DeepNavy, NavyMid)
                )
            )
            .drawBehind {
                // Large radial accent glow — top-right
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            ElectricBlue.copy(alpha = glowAlpha * 0.45f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.85f, size.height * 0.15f),
                        radius = glowRadius * 1.6f
                    ),
                    radius = glowRadius * 1.6f,
                    center = Offset(size.width * 0.85f, size.height * 0.15f)
                )
                // Secondary glow — bottom-left
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            RoyalBlue.copy(alpha = glowAlpha * 0.35f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.12f, size.height * 0.88f),
                        radius = glowRadius * 1.2f
                    ),
                    radius = glowRadius * 1.2f,
                    center = Offset(size.width * 0.12f, size.height * 0.88f)
                )
            },
        contentAlignment = Alignment.Center
    ) {

        // ── Decorative blurred orbs (depth layer) ────────────────────────
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

        // ── Card entrance animation ───────────────────────────────────────
        AnimatedVisibility(
            visible = isVisible,
            enter   = fadeIn(tween(600)) + slideInVertically(
                animationSpec = tween(700, easing = EaseOutBack),
                initialOffsetY = { it / 4 }
            )
        ) {
            Card(
                modifier  = Modifier
                    .width(400.dp)
                    .padding(20.dp),
                shape     = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors    = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    GlassWhite,
                                    Color(0x0AFFFFFF)
                                )
                            )
                        )
                        .drawBehind {
                            // Thin luminous border
                            drawRoundRect(
                                brush       = Brush.linearGradient(
                                    colors  = listOf(
                                        GlassBorder,
                                        NeonBlue.copy(alpha = 0.40f),
                                        GlassBorder
                                    )
                                ),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(28.dp.toPx()),
                                style        = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.2f)
                            )
                        }
                ) {
                    Column(
                        modifier             = Modifier
                            .fillMaxWidth()
                            .padding(36.dp),
                        horizontalAlignment  = Alignment.CenterHorizontally
                    ) {

                        // ── Logo orb ──────────────────────────────────────
                        Box(
                            modifier         = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            ElectricBlue.copy(alpha = 0.35f),
                                            RoyalBlue.copy(alpha = 0.15f)
                                        )
                                    )
                                )
                                .drawBehind {
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                NeonBlue.copy(alpha = glowAlpha * 0.6f),
                                                Color.Transparent
                                            )
                                        ),
                                        radius = size.minDimension * 0.75f
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector     = Icons.Default.FitnessCenter,
                                contentDescription = "Logo",
                                modifier        = Modifier.size(38.dp),
                                tint            = ArcBlue
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // ── Brand title ───────────────────────────────────
                        Text(
                            text       = "YOUTH GYM",
                            style      = TextStyle(
                                fontSize   = 26.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 5.sp,
                                brush      = Brush.horizontalGradient(
                                    colors = listOf(ArcBlue, Color(0xFFBAE6FD))
                                ),
                                shadow = Shadow(
                                    color   = ElectricBlue.copy(alpha = 0.6f),
                                    offset  = Offset(0f, 2f),
                                    blurRadius = 16f
                                )
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Thin divider
                        Box(
                            modifier = Modifier
                                .width(48.dp)
                                .height(2.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            NeonBlue,
                                            Color.Transparent
                                        )
                                    ),
                                    shape = RoundedCornerShape(50)
                                )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text     = "Silakan masuk ke akun Anda",
                            fontSize = 13.sp,
                            color    = TextSub,
                            letterSpacing = 0.3.sp
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // ── Username field ────────────────────────────────
                        ElegantTextField(
                            value          = username,
                            onValueChange  = { username = it; errorMessage = "" },
                            label          = "Username",
                            leadingContent = {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (username.isNotEmpty()) NeonBlue else TextSub,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            isError = errorMessage.isNotEmpty()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // ── Password field ────────────────────────────────
                        ElegantTextField(
                            value          = password,
                            onValueChange  = { password = it; errorMessage = "" },
                            label          = "Password",
                            leadingContent = {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (password.isNotEmpty()) NeonBlue else TextSub,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingContent = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector     = if (passwordVisible) Icons.Default.Visibility
                                        else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password",
                                        tint = TextSub,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None
                            else PasswordVisualTransformation(),
                            isError = errorMessage.isNotEmpty()
                        )

                        // ── Error message ─────────────────────────────────
                        AnimatedVisibility(
                            visible = errorMessage.isNotEmpty(),
                            enter   = fadeIn(tween(300)) + slideInVertically(tween(300)) { -8 }
                        ) {
                            Row(
                                modifier         = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ErrorRed.copy(alpha = 0.12f))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(ErrorRed)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text       = errorMessage,
                                    color      = ErrorRed,
                                    fontSize   = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // ── Login button ──────────────────────────────────
                        Button(
                            onClick = {
                                if (username.isBlank() || password.isBlank()) {
                                    errorMessage = "Username dan Password tidak boleh kosong!"
                                    return@Button
                                }

                                // LOGIKA LOGIN MULTI-ROLE (ADMIN, MEMBER, TRAINER)
                                if (username == "admin" && password == "admin123") {
                                    onLoginSuccess("admin", null)
                                } else {
                                    val member = memberList.find {
                                        it.username == username && it.password == password
                                    }
                                    if (member != null) {
                                        onLoginSuccess("member", member)
                                        return@Button
                                    }

                                    val trainer = trainerList.find {
                                        it.username == username && it.password == password
                                    }
                                    if (trainer != null) {
                                        onLoginSuccess("trainer", trainer)
                                        return@Button
                                    }

                                    errorMessage = "Username atau Password salah!"
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape  = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(RoyalBlue, ElectricBlue, NeonBlue)
                                        ),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .drawBehind {
                                        drawRoundRect(
                                            brush        = Brush.horizontalGradient(
                                                colors   = listOf(
                                                    NeonBlue.copy(alpha = 0.6f),
                                                    Color.Transparent,
                                                    ArcBlue.copy(alpha = 0.6f)
                                                )
                                            ),
                                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(14.dp.toPx()),
                                            style        = androidx.compose.ui.graphics.drawscope.Stroke(1.5f)
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text          = "MASUK",
                                    fontSize      = 14.sp,
                                    fontWeight    = FontWeight.ExtraBold,
                                    letterSpacing = 3.sp,
                                    color         = TextPrimary,
                                    style         = TextStyle(
                                        shadow = Shadow(
                                            color      = ElectricBlue,
                                            blurRadius = 8f
                                        )
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // ── Footer ────────────────────────────────────────
                        Text(
                            text          = "© Youth Gym Management System",
                            fontSize      = 11.sp,
                            color         = TextSub.copy(alpha = 0.5f),
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}

// ── Reusable Elegant TextField ─────────────────────────────────────────────
@Composable
private fun ElegantTextField(
    value               : String,
    onValueChange       : (String) -> Unit,
    label               : String,
    leadingContent      : @Composable (() -> Unit)? = null,
    trailingContent     : @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    isError             : Boolean = false
) {
    val isFocused = value.isNotEmpty()
    val borderColor = when {
        isError  -> ErrorRed.copy(alpha = 0.8f)
        isFocused -> NeonBlue.copy(alpha = 0.7f)
        else     -> GlassBorder
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0x1A1E40AF),
                        Color(0x0D1E3A5F)
                    )
                )
            )
            .drawBehind {
                drawRoundRect(
                    color        = borderColor,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx()),
                    style        = androidx.compose.ui.graphics.drawscope.Stroke(1f)
                )
            }
    ) {
        OutlinedTextField(
            value               = value,
            onValueChange       = onValueChange,
            label               = {
                Text(
                    label,
                    color    = if (isFocused) ArcBlue else TextSub,
                    fontSize = 13.sp
                )
            },
            leadingIcon         = leadingContent,
            trailingIcon        = trailingContent,
            visualTransformation = visualTransformation,
            modifier            = Modifier.fillMaxWidth(),
            shape               = RoundedCornerShape(12.dp),
            singleLine          = true,
            isError             = isError,
            colors              = OutlinedTextFieldDefaults.colors(
                focusedTextColor        = TextPrimary,
                unfocusedTextColor      = TextPrimary,
                cursorColor             = NeonBlue,
                focusedBorderColor      = Color.Transparent,
                unfocusedBorderColor    = Color.Transparent,
                errorBorderColor        = Color.Transparent,
                focusedContainerColor   = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                errorContainerColor     = Color.Transparent,
                focusedLabelColor       = ArcBlue,
                unfocusedLabelColor     = TextSub,
                errorLabelColor         = ErrorRed,
            )
        )
    }
}