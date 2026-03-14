# Interval Walk Tracker (IWT) -- Product Requirements Document v2

## 1. Product Summary

Interval Walk Tracker is an Android fitness application that implements
the **Japanese Interval Walking Training (IWT)** method. The method
alternates **3 minutes of fast walking and 3 minutes of slow walking**
repeatedly for several sets.

The application helps users: - Follow interval walking sessions - Track
steps using device sensors - Monitor workout performance - View
historical progress

The application is designed to be **offline-first**, lightweight, and
reliable using **SQLite in‑memory storage during runtime**.

------------------------------------------------------------------------

# 2. Core Training Method

## Interval Walking Training (IWT)

Cycle structure:

FAST WALK -- 3 minutes\
SLOW WALK -- 3 minutes

1 Cycle = 6 minutes

Typical session examples:

  Session Length   Sets
  ---------------- --------
  18 minutes       3 sets
  24 minutes       4 sets
  30 minutes       5 sets

Each set consists of:

FAST → SLOW

------------------------------------------------------------------------

# 3. Target Users

Primary users:

• Fitness beginners\
• Office workers\
• People without fitness watches\
• People seeking short efficient workouts

------------------------------------------------------------------------

# 4. Key Product Goals

1.  Provide guided interval walking.
2.  Accurately track steps using device sensors.
3.  Track workouts and historical performance.
4.  Provide motivational statistics.
5.  Operate completely offline.

------------------------------------------------------------------------

# 5. Core Features

## 5.1 Dashboard

Displays user's activity summary.

Metrics:

• Today's Steps\
• Sets Completed\
• Remaining Sets\
• Total Workout Minutes\
• Fast Walking Minutes\
• Slow Walking Minutes

Filters:

• Daily\
• Monthly\
• Yearly

UI Components:

-   Progress Cards
-   Activity Summary
-   Start Workout Button
-   History Button

------------------------------------------------------------------------

# 5.2 Workout Session Screen

This screen runs the active walking session.

Display Elements:

Top Area - Current Set - Total Sets

Middle Area - Mode Indicator (FAST / SLOW) - Countdown Timer

Bottom Area - Step Counter - Pause Button - Stop Button

Example UI:

Set 2 / 5

FAST WALK

02:31

Steps: 2104

------------------------------------------------------------------------

# 5.3 Session Summary Screen

Shown after workout completion.

Metrics:

• Total Steps\
• Total Duration\
• Sets Completed\
• Fast Walking Time\
• Slow Walking Time

Actions:

-   Save Session
-   Return to Dashboard

------------------------------------------------------------------------

# 5.4 History Screen

Displays past sessions.

List View:

Date\
Duration\
Steps\
Sets Completed

Filter:

Daily\
Monthly\
Yearly

------------------------------------------------------------------------

# 6. Business Logic

## 6.1 Session State Machine

States:

IDLE\
FAST\
SLOW\
PAUSED\
COMPLETED

State transitions:

IDLE → FAST\
FAST → SLOW\
SLOW → FAST\
FAST → COMPLETED\
SLOW → COMPLETED

------------------------------------------------------------------------

## 6.2 Timer Logic

Timer durations:

FAST = 180 seconds\
SLOW = 180 seconds

Pseudo logic:

    startWorkout()

    mode = FAST
    set = 1
    time = 180

    while set <= totalSets

        runTimer()

        if mode == FAST
            mode = SLOW

        else
            mode = FAST
            set++

    endSession()

------------------------------------------------------------------------

# 7. Step Tracking

Uses Android **Step Counter Sensor**.

Sensor Type:

TYPE_STEP_COUNTER

Steps are calculated relative to session start.

Logic:

    sessionSteps = currentSensorSteps - sessionStartSteps

Edge cases:

• Sensor unavailable\
• Device reboot resets sensor\
• Background interruptions

------------------------------------------------------------------------

# 8. Alerts & Notifications

Switching alerts:

FAST → SLOW

Message:

"Switch to Slow Walk"

SLOW → FAST

Message:

"Switch to Fast Walk"

Alert types:

• vibration\
• sound\
• visual indicator

------------------------------------------------------------------------

# 9. Data Storage Strategy

The app uses:

SQLite **in‑memory database** for runtime session hydration.

Advantages:

• fast read/write\
• minimal disk usage\
• simplified architecture

------------------------------------------------------------------------

# 10. Database Schema

## WalkSession

  Field              Type
  ------------------ -----------
  id                 integer
  date               timestamp
  duration_minutes   integer
  steps              integer
  total_sets         integer
  completed_sets     integer
  fast_minutes       integer
  slow_minutes       integer

------------------------------------------------------------------------

## StepLog

  Field       Type
  ----------- -----------
  id          integer
  timestamp   timestamp
  steps       integer

------------------------------------------------------------------------

# 11. App Architecture

Recommended architecture:

MVVM

Layers:

UI Layer -- Jetpack Compose\
ViewModel Layer\
Repository Layer\
Sensor Manager\
Timer Manager\
SQLite Storage

------------------------------------------------------------------------

# 12. Project Structure

Recommended Android project layout:

    intervalwalktracker
    │
    ├── app
    │
    ├── data
    │   ├── database
    │   │   ├── AppDatabase.kt
    │   │   ├── WalkSessionDao.kt
    │   │   └── Entities.kt
    │   │
    │   ├── repository
    │   │   └── SessionRepository.kt
    │
    ├── sensors
    │   └── StepSensorManager.kt
    │
    ├── timer
    │   └── IntervalTimerManager.kt
    │
    ├── viewmodel
    │   ├── WorkoutViewModel.kt
    │   └── DashboardViewModel.kt
    │
    ├── ui
    │   ├── screens
    │   │   ├── DashboardScreen.kt
    │   │   ├── WorkoutScreen.kt
    │   │   ├── SummaryScreen.kt
    │   │   └── HistoryScreen.kt
    │   │
    │   ├── components
    │   │   ├── TimerDisplay.kt
    │   │   ├── ModeIndicator.kt
    │   │   ├── StepCounter.kt
    │   │   └── ProgressCard.kt
    │
    └── navigation
        └── AppNavigation.kt

------------------------------------------------------------------------

# 13. Jetpack Compose UI Structure

DashboardScreen

    Column
       Header
       ProgressCards
       ActivitySummary
       StartWorkoutButton

WorkoutScreen

    Column
       SetIndicator
       ModeIndicator
       TimerDisplay
       StepCounter
       Controls

------------------------------------------------------------------------

# 14. Navigation Flow

App Flow:

Launch App

→ Dashboard

User presses Start

→ Workout Screen

Workout Finished

→ Summary Screen

Return

→ Dashboard

------------------------------------------------------------------------

# 15. Performance Requirements

The application must:

• operate offline\
• maintain timer accuracy\
• maintain step counter accuracy\
• minimize battery usage

------------------------------------------------------------------------

# 16. Error Handling

Case 1 -- Sensor unavailable

Display:

"Step sensor not supported on this device"

Case 2 -- Timer interruption

Resume timer automatically.

------------------------------------------------------------------------

# 17. Future Features

Planned improvements:

• GPS route tracking\
• calorie estimation\
• wearable integration\
• Google Health Connect sync\
• reminders\
• social leaderboard

------------------------------------------------------------------------

# 18. Development Phases

Phase 1 -- Project setup\
Phase 2 -- Timer engine\
Phase 3 -- Step sensor integration\
Phase 4 -- Dashboard statistics\
Phase 5 -- Session persistence\
Phase 6 -- UI polishing

------------------------------------------------------------------------

# 19. Success Metrics

User metrics:

• sessions per week\
• steps per session\
• session completion rate

Technical metrics:

• crash rate\
• sensor reliability\
• timer precision

------------------------------------------------------------------------

# End of PRD v2
