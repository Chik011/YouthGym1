package com.chiko0085.testgym.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chiko0085.testgym.model.Member
import com.chiko0085.testgym.openWebLink
import com.chiko0085.testgym.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsAppBroadcastScreen(
    members: List<Member>,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var messageText by remember { mutableStateOf("") }
    val selectedMembers = remember { mutableStateListOf<Member>() }
    
    val filteredMembers = members.filter { 
        it.name.contains(searchQuery, ignoreCase = true) || it.phoneNumber.contains(searchQuery)
    }.filter { it.phoneNumber.isNotEmpty() }

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(DarkBgStart, DarkBgEnd)))) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("WhatsApp Broadcast", color = Color.White) },
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
                // Input Pesan
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Tulis Pesan Broadcast", color = AccentBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            placeholder = { Text("Halo, ini dari Youth Gym...", color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = AccentBlue
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search & Select All
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Cari Member...", color = Color.Gray) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = {
                        if (selectedMembers.size == filteredMembers.size) {
                            selectedMembers.clear()
                        } else {
                            selectedMembers.clear()
                            selectedMembers.addAll(filteredMembers)
                        }
                    }) {
                        Text(if (selectedMembers.size == filteredMembers.size) "Deselect All" else "Select All", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Pilih Penerima (${selectedMembers.size} Terpilih)", color = Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredMembers) { member ->
                        val isSelected = selectedMembers.contains(member)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isSelected) selectedMembers.remove(member)
                                    else selectedMembers.add(member)
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) AccentBlue.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.03f)
                            ),
                            border = if (isSelected) BorderStroke(1.dp, AccentBlue) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        if (checked) selectedMembers.add(member)
                                        else selectedMembers.remove(member)
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(member.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(member.phoneNumber, color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        selectedMembers.forEach { member ->
                            var phone = member.phoneNumber.filter { it.isDigit() }
                            if (phone.startsWith("0")) {
                                phone = "62" + phone.substring(1)
                            } else if (!phone.startsWith("62")) {
                                phone = "62$phone"
                            }
                            
                            val url = "https://wa.me/$phone?text=${messageText.replace(" ", "%20")}"
                            openWebLink(url)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .height(56.dp),
                    enabled = selectedMembers.isNotEmpty() && messageText.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)), // WA Green
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color.White)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Kirim ke WhatsApp (${selectedMembers.size} Orang)", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
