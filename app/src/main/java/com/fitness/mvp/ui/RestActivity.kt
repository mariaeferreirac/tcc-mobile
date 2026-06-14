package com.fitness.mvp.ui

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import com.fitness.mvp.R
import com.fitness.mvp.databinding.ActivityRestBinding

/**
 * RestActivity – Tela de descanso com timer circular.
 *
 * Quando o tempo acaba (ou o usuário pula), volta para WorkoutActivity
 * com os índices do próximo exercício/série.
 */
class RestActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_REST_SECONDS        = "rest_seconds"
        const val EXTRA_NEXT_EXERCISE_INDEX = "next_exercise_index"
        const val EXTRA_NEXT_SERIES_INDEX   = "next_series_index"
        const val EXTRA_NEXT_EXERCISE_NAME  = "next_exercise_name"
        const val EXTRA_NEXT_SERIES_LABEL   = "next_series_label"
    }

    private lateinit var binding: ActivityRestBinding

    private var countDownTimer: CountDownTimer? = null
    private var totalRestMillis = 60_000L
    private var remainingMillis = 60_000L

    private var nextExerciseIndex = 0
    private var nextSeriesIndex   = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Dados recebidos da WorkoutActivity
        val restSeconds     = intent.getIntExtra(EXTRA_REST_SECONDS, 60)
        nextExerciseIndex   = intent.getIntExtra(EXTRA_NEXT_EXERCISE_INDEX, 0)
        nextSeriesIndex     = intent.getIntExtra(EXTRA_NEXT_SERIES_INDEX, 0)
        val nextExerciseName = intent.getStringExtra(EXTRA_NEXT_EXERCISE_NAME) ?: "Próximo exercício"
        val nextSeriesLabel  = intent.getStringExtra(EXTRA_NEXT_SERIES_LABEL)  ?: "Série 1"

        totalRestMillis  = restSeconds * 1000L
        remainingMillis  = totalRestMillis

        // Texto "Próximo: Série X / Nome do exercício"
        binding.tvNextExercise.text = getString(R.string.next_exercise_info, nextExerciseName, nextSeriesLabel)

        // Pular descanso
        binding.btnSkipRest.setOnClickListener { goToNextExercise() }

        // Finalizar treino a partir do descanso
        binding.btnFinishFromRest.setOnClickListener { goToFeedback() }

        startRestTimer()
    }

    private fun startRestTimer() {
        countDownTimer?.cancel()

        countDownTimer = object : CountDownTimer(totalRestMillis, 100) {
            override fun onTick(millisUntilFinished: Long) {
                remainingMillis = millisUntilFinished

                // Número dos segundos
                val seconds = (millisUntilFinished / 1000).toInt() + 1
                binding.tvRestTimer.text = seconds.toString()

                // Progress circular: de 100 → 0 conforme o tempo passa
                val progress = ((millisUntilFinished.toFloat() / totalRestMillis) * 100).toInt()
                binding.progressTimer.progress = progress
            }

            override fun onFinish() {
                binding.tvRestTimer.text = "0"
                binding.progressTimer.progress = 0
                goToNextExercise()
            }
        }.start()
    }

    private fun goToNextExercise() {
        countDownTimer?.cancel()
        val intent = Intent(this, WorkoutActivity::class.java).apply {
            putExtra(WorkoutActivity.EXTRA_EXERCISE_INDEX, nextExerciseIndex)
            putExtra(WorkoutActivity.EXTRA_SERIES_INDEX,   nextSeriesIndex)
        }
        startActivity(intent)
        finish()
    }

    private fun goToFeedback() {
        countDownTimer?.cancel()
        startActivity(Intent(this, FeedbackActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        // Bloqueia voltar durante o descanso
    }
}
