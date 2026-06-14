package com.fitness.mvp.ui

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.fitness.mvp.R
import com.fitness.mvp.databinding.ActivityWorkoutBinding
import com.fitness.mvp.model.Exercise
import com.fitness.mvp.model.defaultExercises

/**
 * WorkoutActivity – Tela principal de execução do exercício.
 *
 * Fluxo completo:
 *   Exercício 1, Série 1 → Descanso → Exercício 1, Série 2 → Descanso →
 *   Exercício 1, Série 3 → (entre exercícios) → Exercício 2, Série 1 → … →
 *   Exercício 2, Série 3 → FeedbackActivity
 *
 * Receives via intent:
 *   EXTRA_EXERCISE_INDEX (Int): índice do exercício atual na lista
 *   EXTRA_SERIES_INDEX   (Int): índice da série atual (0-based)
 */
class WorkoutActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_EXERCISE_INDEX = "exercise_index"
        const val EXTRA_SERIES_INDEX   = "series_index"
    }

    private lateinit var binding: ActivityWorkoutBinding

    private val exercises: List<Exercise> = defaultExercises()
    private var exerciseIndex = 0
    private var seriesIndex   = 0

    private var countDownTimer: CountDownTimer? = null
    private var isPaused = false
    private var remainingMillis = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Receber estado via intent (navegação de volta do descanso)
        exerciseIndex = intent.getIntExtra(EXTRA_EXERCISE_INDEX, 0)
        seriesIndex   = intent.getIntExtra(EXTRA_SERIES_INDEX, 0)

        setupUI()
        startExerciseTimer()
    }

    private fun setupUI() {
        val exercise = exercises[exerciseIndex]
        val totalExercises = exercises.size
        val totalSeries = exercise.sets

        // Header
        binding.tvExerciseCounter.text = getString(R.string.exercise_counter, exerciseIndex + 1, totalExercises)

        // Emoji e nome do exercício
        binding.tvExerciseEmoji.text = exercise.emoji
        binding.tvExerciseName.text  = exercise.name

        // Stats card
        binding.tvSeries.text   = getString(R.string.next_exercise_info, (seriesIndex + 1).toString(), totalSeries.toString())
        binding.tvDuration.text = getString(R.string.stat_unit_format, exercise.durationSeconds.toString(), "s")
        binding.tvRest.text     = getString(R.string.stat_unit_format, exercise.restSeconds.toString(), "s")

        // Inicializa o timer display
        remainingMillis = exercise.durationSeconds * 1000L
        updateTimerDisplay(remainingMillis)

        // Botão Pausar
        binding.btnPauseWorkout.setOnClickListener {
            if (isPaused) resumeTimer() else pauseTimer()
        }

        // Botão Finalizar
        binding.btnFinishWorkout.setOnClickListener {
            showConfirmFinishDialog()
        }
    }

    // ─────────────────────────────────────────────
    //  Timer do exercício
    // ─────────────────────────────────────────────

    private fun startExerciseTimer() {
        val exercise = exercises[exerciseIndex]
        startTimer(exercise.durationSeconds * 1000L) {
            onExerciseFinished()
        }
    }

    private fun startTimer(durationMs: Long, onFinish: () -> Unit) {
        countDownTimer?.cancel()
        remainingMillis = durationMs

        countDownTimer = object : CountDownTimer(durationMs, 100) {
            override fun onTick(millisUntilFinished: Long) {
                remainingMillis = millisUntilFinished
                updateTimerDisplay(millisUntilFinished)
            }
            override fun onFinish() {
                updateTimerDisplay(0)
                onFinish()
            }
        }.start()
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        isPaused = true
        binding.btnPauseWorkout.text = getString(R.string.resume_workout)
    }

    private fun resumeTimer() {
        isPaused = false
        binding.btnPauseWorkout.text = getString(R.string.pause_workout)
        startTimer(remainingMillis) { onExerciseFinished() }
    }

    private fun updateTimerDisplay(millis: Long) {
        val totalSeconds = (millis / 1000).toInt()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        binding.tvTimer.text = "%d:%02d".format(minutes, seconds)
    }

    // ─────────────────────────────────────────────
    //  Lógica de progressão
    // ─────────────────────────────────────────────

    private fun onExerciseFinished() {
        val exercise   = exercises[exerciseIndex]
        val nextSeries = seriesIndex + 1

        when {
            // Ainda tem séries no mesmo exercício → vai pro descanso
            nextSeries < exercise.sets -> {
                goToRest(
                    restSeconds   = exercise.restSeconds,
                    nextExerciseIndex = exerciseIndex,
                    nextSeriesIndex   = nextSeries,
                    nextExerciseName  = exercise.name,
                    nextSeriesLabel   = getString(R.string.series_counter, nextSeries + 1),
                )
            }

            // Terminou as séries, tem próximo exercício → vai pro descanso entre exercícios
            (exerciseIndex + 1 < exercises.size) -> {
                val nextExercise = exercises[exerciseIndex + 1]
                goToRest(
                    restSeconds       = exercise.restSeconds,
                    nextExerciseIndex = exerciseIndex + 1,
                    nextSeriesIndex   = 0,
                    nextExerciseName  = nextExercise.name,
                    nextSeriesLabel   = getString(R.string.series_counter, 1)
                )
            }

            // Treino completo! → Feedback
            else -> {
                goToFeedback()
            }
        }
    }

    private fun goToRest(
        restSeconds: Int,
        nextExerciseIndex: Int,
        nextSeriesIndex: Int,
        nextExerciseName: String,
        nextSeriesLabel: String
    ) {
        countDownTimer?.cancel()
        val intent = Intent(this, RestActivity::class.java).apply {
            putExtra(RestActivity.EXTRA_REST_SECONDS,        restSeconds)
            putExtra(RestActivity.EXTRA_NEXT_EXERCISE_INDEX, nextExerciseIndex)
            putExtra(RestActivity.EXTRA_NEXT_SERIES_INDEX,   nextSeriesIndex)
            putExtra(RestActivity.EXTRA_NEXT_EXERCISE_NAME,  nextExerciseName)
            putExtra(RestActivity.EXTRA_NEXT_SERIES_LABEL,   nextSeriesLabel)
        }
        startActivity(intent)
        finish()
    }

    private fun goToFeedback() {
        countDownTimer?.cancel()
        startActivity(Intent(this, FeedbackActivity::class.java))
        finish()
    }

    // ─────────────────────────────────────────────
    //  Dialog confirmar finalizar
    // ─────────────────────────────────────────────

    private fun showConfirmFinishDialog() {
        countDownTimer?.cancel()

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_confirm_finish)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        dialog.findViewById<Button>(R.id.btnDialogContinue).setOnClickListener {
            dialog.dismiss()
            // Retoma o timer
            startTimer(remainingMillis) { onExerciseFinished() }
        }

        dialog.findViewById<Button>(R.id.btnDialogEnd).setOnClickListener {
            dialog.dismiss()
            goToFeedback()
        }

        dialog.setOnCancelListener {
            startTimer(remainingMillis) { onExerciseFinished() }
        }

        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }

    // Desabilitar back button durante o treino (ou mostrar dialog)
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        showConfirmFinishDialog()
    }
}
