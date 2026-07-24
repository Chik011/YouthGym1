package com.chiko0085.testgym.model

import kotlinx.serialization.Serializable

@Serializable
data class WorkoutSession(
    val id: String = "",
    val memberId: String,
    val memberName: String,
    val trainerId: String,
    val trainerName: String,
    val date: Long,
    val exercises: List<ExerciseRecord> = emptyList(),
    val notes: String = ""
)

@Serializable
data class ExerciseRecord(
    val name: String,
    val sets: Int,
    val reps: String, // String to allow "10-12" or "Max"
    val weight: String = "" // String to allow "BW" or "50kg"
)
