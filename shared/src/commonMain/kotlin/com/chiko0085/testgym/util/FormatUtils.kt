package com.chiko0085.testgym.util

import kotlin.math.round

// Fungsi pemformatan mata uang ke Rupiah
fun formatRupiah(number: Double): String {
    val formatter = number.toLong().toString()
    val reversed = formatter.reversed()
    val chunks = reversed.chunked(3)
    return chunks.joinToString(".").reversed()
}

// Fungsi utilitas format desimal
fun formatDecimal(value: Double): String {
    return (round(value * 10) / 10.0).toString()
}
