# Fitness MVP - Android Studio

## Estrutura do Projeto

```
app/
├── src/main/
│   ├── java/com/fitness/mvp/
│   │   ├── ui/
│   │   │   ├── DashboardActivity.kt      ← Tela inicial (HU003)
│   │   │   ├── WorkoutActivity.kt        ← Tela do treino
│   │   │   ├── RestActivity.kt           ← Tela de descanso com timer
│   │   │   └── FeedbackActivity.kt       ← Tela de feedback (RPE 0-10)
│   │   ├── model/
│   │   │   └── Exercise.kt               ← Data classes
│   │   ├── data/
│   │   │   └── FirebaseRepository.kt     ← Envio do feedback
│   │   └── utils/
│   │       └── CountdownTimer.kt         ← Timer utilitário
│   └── res/
│       ├── layout/
│       │   ├── activity_dashboard.xml
│       │   ├── activity_workout.xml
│       │   ├── activity_rest.xml
│       │   └── activity_feedback.xml
│       └── values/
│           ├── colors.xml
│           ├── strings.xml
│           └── themes.xml
```

## Fluxo
1. App abre → **DashboardActivity** (tela inicial, HU003)
   - Calendário do mês atual (dia de hoje em azul, dias de treino em verde — visual/MVP)
   - Card "Próximo Treino" com botão **"Iniciar Treino"** (único elemento funcional)
   - Card "Seu Progresso" (visual)
   - Bottom nav: apenas "Início" é funcional; os demais itens mostram "Em breve"
2. Toca em "Iniciar Treino" → **WorkoutActivity** (Exercício 1, Série 1)
3. Timer do exercício → ao fim → **RestActivity** (descanso 60s)
4. RestActivity → WorkoutActivity (próxima série/exercício)
5. Repete até o fim do Treino (2 exercícios x 3 séries)
6. → **FeedbackActivity** (RPE 0-10) → salva no Firebase → volta para o **Dashboard**

## Setup Firebase
1. Criar projeto no Firebase Console
2. Adicionar app Android com package `com.fitness.mvp`
3. Baixar `google-services.json` → colocar em `app/`
4. Habilitar Firestore Database no Firebase Console

