package com.chiko0085.testgym.ui.screens

import androidx.compose.runtime.Composable

@Composable
expect fun MemberScanScreen(title: String, onResult: (String) -> Unit)
