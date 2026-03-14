# Interval Walk Tracker (IWT) -- Product Requirements Document v3

## 1. Product Summary

Interval Walk Tracker is a production-grade Android fitness application that implements the **Japanese Interval Walking Training (IWT)** method. The method alternates **3 minutes of fast walking and 3 minutes of slow walking** repeatedly for several sets.

The application helps users:
- Follow guided interval walking sessions.
- Track steps accurately using hardware sensors.
- Monitor real-time performance with audio coaching.
- View historical progress and motivational statistics.

The application is built with a **modern, offline-first architecture** using Jetpack Compose, Hilt, and Room.

------------------------------------------------------------------------

# 2. Core Training Method

## Interval Walking Training (IWT)

Cycle structure:
- FAST WALK -- 3 minutes
- SLOW WALK -- 3 minutes
- 1 Cycle (Set) = 6 minutes

Typical session examples:
- 18 minutes: 3 sets
- 24 minutes: 4 sets
- 30 minutes: 5 sets

------------------------------------------------------------------------

# 3. Key Product Goals

1.  **Guided Workout:** Provide visual and audio cues for interval transitions.
2.  **Precision Tracking:** Use hardware sensors (`TYPE_STEP_COUNTER`) for battery-efficient step counting.
3.  **Background Reliability:** Maintain workout state and timers using a Foreground Service.
4.  **Data Integrity:** Centralize data access through the Repository pattern with Room persistence.
5.  **Scalable Architecture:** Implement DI (Hilt) and MVVM for maintainability.

------------------------------------------------------------------------

# 4. Core Features

## 4.1 Dashboard
- **Metrics:** Today's Steps, Completed Sets, Remaining Sets (Daily), Total Duration, Fast/Slow split.
- **Filters:** Daily, Monthly, Yearly summaries using `StateFlow` and logic-aware filtering.
- **Controls:** Target set slider and "Start Workout" entry point.

## 4.2 Workout Session Screen
- **Real-time UI:** Current Set indicator, Countdown Timer, and Mode Indicator (Fast/Slow).
- **Controls:** Pause/Resume and Stop functionality.
- **Background Support:** Active notification allows workout tracking outside the app.

## 4.3 Audio Coaching
- **Technology:** Android Text-to-Speech (TTS).
- **Triggers:** Workout start, phase transitions ("Switch to slow walking"), and set completion.

## 4.4 History Screen
- **Session List:** Detailed breakdown of past workouts including steps, duration, and sets completed.
- **Navigation:** Deep-link integration from Dashboard.

------------------------------------------------------------------------

# 5. Technical Architecture

## 5.1 Pattern: MVVM + Repository + DI
- **Model:** Room Entities (`WalkSession`, `StepLog`).
- **View:** Declarative Jetpack Compose screens.
- **ViewModel:** Hilt-injected ViewModels handling UI state and business logic.
- **Repository:** `SessionRepository` as the single source of truth for all data operations.
- **Dependency Injection:** Hilt for managing the lifecycle of singletons (Database, Managers, Repository).

## 5.2 Layers
- **DI Layer (`di/`):** Hilt modules for providing Database, Repository, and System Managers.
- **Data Layer (`data/`):** Room Database, DAOs, and the abstraction Repository.
- **Service Layer (`service/`):** `WorkoutService` (Foreground) manages the workout lifecycle and audio cues.
- **Logic Layer:** 
    - `timer/`: `IntervalTimerManager` handles core timing logic.
    - `sensors/`: `StepSensorManager` handles hardware sensor registration.
    - `audio/`: `AudioCoachManager` manages TTS lifecycle.

------------------------------------------------------------------------

# 6. Data Storage Strategy

- **Database:** Room (SQLite).
- **Configuration:** In-memory configuration for runtime session hydration (as per PRD requirements), with the option for persistent storage.
- **Schema:**
    - `WalkSession`: id, date, steps, totalSets, completedSets, durationMinutes, fastMinutes, slowMinutes.
    - `StepLog`: id, timestamp, steps.

------------------------------------------------------------------------

# 7. Step Tracking & Sensors

- **Sensor Type:** `TYPE_STEP_COUNTER`.
- **Optimization:** Uses `SENSOR_DELAY_NORMAL` to minimize battery consumption while maintaining accuracy for walking.
- **Logic:** `sessionSteps = currentSensorValue - startSensorValue`.

------------------------------------------------------------------------

# 8. Project Structure

```
com.binodnagarkoti.intervalwalktracker
├── audio/          # Text-to-Speech management
├── data/           # Database and Repository
│   ├── database/
│   └── repository/
├── di/             # Hilt Dependency Injection modules
├── sensors/        # Hardware sensor management
├── service/        # Foreground Service implementation
├── timer/          # Workout timer business logic
├── ui/             # Jetpack Compose UI
│   ├── components/ # Reusable UI widgets
│   ├── navigation/ # Compose Navigation setup
│   └── screens/    # Feature-specific screens
└── viewmodel/      # Hilt ViewModels
```

------------------------------------------------------------------------

# 9. Performance & Security

- **Lifecycle Awareness:** Uses `collectAsStateWithLifecycle` to prevent memory leaks and unnecessary processing.
- **Permissions:** Runtime handling for `ACTIVITY_RECOGNITION` and `POST_NOTIFICATIONS`.
- **Efficiency:** Foreground Service ensures timing accuracy even when the CPU is in deep sleep.

------------------------------------------------------------------------

# 10. Success Metrics

- **Technical:** Zero `ClassCastException` on startup, <1% crash rate, and stable background execution on Android 14+.
- **User:** High session completion rate due to clear audio guidance and accurate tracking.

------------------------------------------------------------------------
# End of PRD v3
