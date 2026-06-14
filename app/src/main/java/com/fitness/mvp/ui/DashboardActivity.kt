package com.fitness.mvp.ui

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fitness.mvp.R
import com.fitness.mvp.databinding.ActivityDashboardBinding
import java.util.Calendar

/**
 * DashboardActivity – Tela inicial do app (HU003).
 *
 * Mostra um calendário do mês atual, o card "Próximo Treino" e o card
 * "Seu Progresso". O único elemento funcional é o botão "Iniciar Treino",
 * que leva para o fluxo de treino já existente (WorkoutActivity → RestActivity
 * → ... → FeedbackActivity).
 *
 * Os demais elementos (menu, perfil, itens da bottom nav) são apenas visuais
 * neste MVP e exibem um aviso "Em breve".
 */
class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    private val monthNames = listOf(
        "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
        "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro",
    )

    // Dias "de treino" exibidos em verde no calendário (apenas visual/MVP).
    // Em uma próxima etapa isso pode vir do histórico real de treinos no Firestore.
    private val workoutDays = setOf(2, 4, 6, 9, 11, 13, 16, 18, 20, 23, 25, 27, 30)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCalendar()
        setupClickListeners()
    }

    // ─────────────────────────────────────────────
    //  Ações
    // ─────────────────────────────────────────────

    private fun setupClickListeners() {
        // Botões que iniciam o fluxo de treino
        val startWorkoutAction = {
            startActivity(Intent(this, WorkoutActivity::class.java))
        }

        binding.btnStartWorkout.setOnClickListener { startWorkoutAction() }
        binding.navTreino.setOnClickListener { startWorkoutAction() }

        // Itens ainda não implementados neste MVP
        val placeholders = listOf(
            binding.ivMenu,
            binding.profileIcon,
            binding.navHistorico,
            binding.navRanking,
            binding.navPerfil,
        )
        placeholders.forEach { view ->
            view.setOnClickListener { showComingSoon() }
        }
    }

    private fun showComingSoon() {
        Toast.makeText(this, getString(R.string.coming_soon), Toast.LENGTH_SHORT).show()
    }

    // ─────────────────────────────────────────────
    //  Calendário
    // ─────────────────────────────────────────────

    private fun setupCalendar() {
        val calendar = Calendar.getInstance()
        val today = calendar[Calendar.DAY_OF_MONTH]
        val monthIndex = calendar[Calendar.MONTH]
        val year = calendar[Calendar.YEAR]

        binding.tvMonthYear.text = getString(R.string.month_year_format, monthNames[monthIndex], year)

        // Move para o dia 1 do mês para descobrir em que coluna ele começa
        calendar[Calendar.DAY_OF_MONTH] = 1
        val firstDayOfWeek = calendar[Calendar.DAY_OF_WEEK] // 1 = Domingo ... 7 = Sábado
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        binding.calendarGrid.removeAllViews()

        var row = createWeekRow()
        var cellsInRow = 0

        // Células vazias antes do dia 1
        repeat(firstDayOfWeek - 1) {
            row.addView(createDayCell(day = null, isToday = false, isWorkoutDay = false))
            cellsInRow++
        }

        // Dias do mês
        for (day in 1..daysInMonth) {
            row.addView(
                createDayCell(
                    day = day,
                    isToday = day == today,
                    isWorkoutDay = workoutDays.contains(day)
                )
            )
            cellsInRow++

            if (cellsInRow == 7) {
                binding.calendarGrid.addView(row)
                row = createWeekRow()
                cellsInRow = 0
            }
        }

        // Completa a última linha com células vazias, se necessário
        if ((cellsInRow in 1..6)) {
            repeat(7 - cellsInRow) {
                row.addView(createDayCell(day = null, isToday = false, isWorkoutDay = false))
            }
            binding.calendarGrid.addView(row)
        }
    }

    private fun createWeekRow(): LinearLayout {
        return LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
        }
    }

    /**
     * Cria uma célula do calendário. Cada célula ocupa 1/7 da largura da linha
     * e contém, centralizado, um círculo de 32dp com o número do dia.
     * - Hoje: círculo azul, texto branco/negrito
     * - Dia de treino: círculo verde claro
     * - Dia normal: sem fundo
     */
    private fun createDayCell(day: Int?, isToday: Boolean, isWorkoutDay: Boolean): FrameLayout {
        val cellHeightPx = dpToPx(40)
        val circleSizePx = dpToPx(32)

        val cell = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, cellHeightPx, 1f)
        }

        if (day != null) {
            val label = TextView(this).apply {
                layoutParams = FrameLayout.LayoutParams(circleSizePx, circleSizePx).apply {
                    gravity = Gravity.CENTER
                }
                gravity = Gravity.CENTER
                text = day.toString()
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)

                when {
                    isToday -> {
                        setBackgroundResource(R.drawable.bg_circle_blue)
                        setTextColor(getColor(R.color.text_white))
                        setTypeface(typeface, Typeface.BOLD)
                    }
                    isWorkoutDay -> {
                        setBackgroundResource(R.drawable.bg_circle_green)
                        setTextColor(getColor(R.color.text_primary))
                    }
                    else -> {
                        setTextColor(getColor(R.color.text_primary))
                    }
                }
            }
            cell.addView(label)
        }

        return cell
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()
}
