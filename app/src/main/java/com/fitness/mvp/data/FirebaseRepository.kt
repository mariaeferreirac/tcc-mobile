package com.fitness.mvp.data

import com.fitness.mvp.model.FeedbackData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

class FirebaseRepository {

    private val db: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    /**
     * Envia o feedback do treino para o Firestore.
     * MOCK: Simula sucesso para testes de interface, já que o google-services.json não foi configurado.
     */
    suspend fun saveFeedback(feedback: FeedbackData): Result<String> {
        return try {
            // Simula latência de rede
            delay(1000.milliseconds)
            
            // Sucesso simulado
            Result.success("mock_doc_id_${System.currentTimeMillis()}")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
