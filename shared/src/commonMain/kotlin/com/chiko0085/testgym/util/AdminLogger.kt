package com.chiko0085.testgym.util

import com.chiko0085.testgym.db
import com.chiko0085.testgym.getCurrentTimeMillis
import com.chiko0085.testgym.model.AdminLog
import kotlin.random.Random

object AdminLogger {
    suspend fun log(adminUsername: String, action: String, details: String) {
        try {
            val id = "LOG-${Random.nextInt(100000, 999999)}"
            val log = AdminLog(
                id = id,
                adminUsername = adminUsername,
                action = action,
                details = details,
                timestamp = getCurrentTimeMillis()
            )
            db.collection("admin_logs").document(id).set(log)
        } catch (e: Exception) {
            println("DEBUG: Gagal mencatat log admin: ${e.message}")
        }
    }
}
