package com.chiko0085.testgym

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.storage.storage

val db by lazy { 
    // Pastikan inisialisasi dipanggil sebelum akses firestore
    try {
        initFirebase()
    } catch (e: Exception) {
        println("DEBUG: initFirebase call from db lazy failed: ${e.message}")
    }
    Firebase.firestore 
}

val storage by lazy { Firebase.storage }
