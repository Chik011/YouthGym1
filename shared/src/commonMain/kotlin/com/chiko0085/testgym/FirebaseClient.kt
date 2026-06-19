// FirebaseClient.kt - Konfigurasi client Firebase

package com.chiko0085.testgym

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.storage.storage

// Inisialisasi Firestore via lazy loading
val db by lazy { 
    try {
        initFirebase()
    } catch (e: Exception) {
        println("DEBUG: initFirebase call from db lazy failed: ${e.message}")
    }
    Firebase.firestore 
}

// Inisialisasi Storage via lazy loading
val storage by lazy { Firebase.storage }
