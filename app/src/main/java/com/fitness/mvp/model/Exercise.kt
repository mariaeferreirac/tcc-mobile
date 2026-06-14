package com.fitness.mvp.model

data class Exercise(
    val id: Int,
    val name: String,
    val sets: Int,
    val durationSeconds: Int,   // duração de cada série em segundos
    val restSeconds: Int,        // descanso entre séries
    val emoji: String = "🏋️"
)

data class WorkoutSession(
    val workoutId: String = "treino_1",
    val workoutName: String = "Treino 1 – Nível 1",
    val exercises: List<Exercise> = defaultExercises()
)

data class FeedbackData(
    val workoutId: String,
    val rpe: Int,               // Rate of Perceived Exertion 0-10
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String = "usuario_01"
)

fun defaultExercises(): List<Exercise> = listOf(
    Exercise(
        id = 1,
        name = "Supino Reto",
        sets = 3,
        durationSeconds = 45,
        restSeconds = 60,
        emoji = "🏋️"
    ),
    Exercise(
        id = 2,
        name = "Agachamento Livre",
        sets = 3,
        durationSeconds = 45,
        restSeconds = 60,
        emoji = "🦵"
    )
)
