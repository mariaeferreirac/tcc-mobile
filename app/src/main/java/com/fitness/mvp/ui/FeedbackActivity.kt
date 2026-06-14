package com.fitness.mvp.ui

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.lifecycle.lifecycleScope
import com.fitness.mvp.R
import com.fitness.mvp.data.FirebaseRepository
import com.fitness.mvp.databinding.ActivityFeedbackBinding
import com.fitness.mvp.model.FeedbackData
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * FeedbackActivity – Tela de avaliação de esforço percebido (RPE 0-10).
 *
 * O usuário toca em um dos 11 cards (0 a 10) para selecionar o RPE.
 * Ao clicar em "Enviar Avaliação", salva no Firebase Firestore.
 */
class FeedbackActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFeedbackBinding
    private val repository = FirebaseRepository()

    private var selectedRpe: Int = -1

    // Mapeamento: valor RPE → card view
    private val rpeCards: Map<Int, MaterialCardView> by lazy {
        mapOf(
            0  to binding.cardRpe0,
            1  to binding.cardRpe1,
            2  to binding.cardRpe2,
            3  to binding.cardRpe3,
            4  to binding.cardRpe4,
            5  to binding.cardRpe5,
            6  to binding.cardRpe6,
            7  to binding.cardRpe7,
            8  to binding.cardRpe8,
            9  to binding.cardRpe9,
            10 to binding.cardRpe10,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRpeCards()

        binding.btnSendFeedback.setOnClickListener {
            if (selectedRpe >= 0) sendFeedback()
        }
    }

    private fun setupRpeCards() {
        rpeCards.forEach { (rpe, card) ->
            card.setOnClickListener { selectRpe(rpe) }
        }
    }

    private fun selectRpe(rpe: Int) {
        // Reseta todos os cards
        rpeCards.values.forEach { card ->
            card.strokeWidth  = dpToPx(1)
            card.strokeColor  = "#DEE2E6".toColorInt()
            card.cardElevation = dpToPx(2).toFloat()
        }

        // Destaca o card selecionado
        val selectedCard = rpeCards[rpe]
        selectedCard?.strokeWidth  = dpToPx(2)
        selectedCard?.strokeColor  = "#3B5BDB".toColorInt()
        selectedCard?.cardElevation = dpToPx(6).toFloat()

        selectedRpe = rpe

        // Habilita o botão de envio
        binding.btnSendFeedback.isEnabled = true
        binding.btnSendFeedback.alpha     = 1.0f
    }

    private fun sendFeedback() {
        val feedback = FeedbackData(
            workoutId = "treino_1_nivel_1",
            rpe       = selectedRpe
        )

        // Feedback visual de envio
        binding.btnSendFeedback.isEnabled = false
        binding.btnSendFeedback.text = getString(R.string.sending_feedback)

        lifecycleScope.launch {
            val result = repository.saveFeedback(feedback)
            result.fold(
                onSuccess = {
                    // Mostra que foi enviado
                    binding.btnSendFeedback.text = getString(R.string.sent_feedback)
                    
                    // Pequeno delay para o usuário ver a confirmação
                    delay(1500.milliseconds)
                    
                    // Volta para o Dashboard
                    finish()
                },
                onFailure = {
                    Toast.makeText(
                        this@FeedbackActivity,
                        getString(R.string.feedback_error),
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.btnSendFeedback.isEnabled = true
                    binding.btnSendFeedback.text = getString(R.string.send_feedback)
                }
            )
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Permite voltar da tela de feedback
        super.onBackPressed()
    }
}
