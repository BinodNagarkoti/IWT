package com.binodnagarkoti.intervalwalktracker.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.binodnagarkoti.intervalwalktracker.MainActivity
import com.binodnagarkoti.intervalwalktracker.audio.AudioCoachManager
import com.binodnagarkoti.intervalwalktracker.data.database.StepLog
import com.binodnagarkoti.intervalwalktracker.data.database.WalkSession
import com.binodnagarkoti.intervalwalktracker.data.repository.SessionRepository
import com.binodnagarkoti.intervalwalktracker.sensors.StepSensorManager
import com.binodnagarkoti.intervalwalktracker.timer.IntervalTimerManager
import com.binodnagarkoti.intervalwalktracker.timer.TimerState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WorkoutService : Service() {

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    @Inject lateinit var timerManager: IntervalTimerManager
    @Inject lateinit var stepSensorManager: StepSensorManager
    @Inject lateinit var audioCoachManager: AudioCoachManager
    @Inject lateinit var repository: SessionRepository
    private var toneGenerator: ToneGenerator? = null

    private val CHANNEL_ID = "workout_channel"
    private val NOTIFICATION_ID = 1
    private var totalSets = 5
    private var fastSeconds = 180
    private var slowSeconds = 180
    private var wakeLock: PowerManager.WakeLock? = null

    inner class LocalBinder : Binder() {
        fun getService(): WorkoutService = this@WorkoutService
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("WorkoutService", "Service onCreate")
        toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
        createNotificationChannel()
        observeTimerState()
        acquireWakeLock()
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "IntervalWalkTracker:WorkoutWakeLock")
        wakeLock?.acquire(10 * 60 * 1000L /*10 minutes fallback*/)
        Log.d("WorkoutService", "WakeLock acquired")
    }

    private fun releaseWakeLock() {
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
            Log.d("WorkoutService", "WakeLock released")
        }
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        Log.d("WorkoutService", "onStartCommand action: $action")
        when (action) {
            "START" -> {
                totalSets = intent.getIntExtra("SETS", 5)
                fastSeconds = intent.getIntExtra("FAST_SECONDS", 180)
                slowSeconds = intent.getIntExtra("SLOW_SECONDS", 180)
                startWorkout(totalSets, fastSeconds, slowSeconds)
            }
            "STOP" -> {
                saveAndStopWorkout()
            }
        }
        return START_NOT_STICKY
    }

    private fun startWorkout(sets: Int, fast: Int, slow: Int) {
        timerManager.startTimer(sets, fast, slow)
        stepSensorManager.startTracking()
        
        // Ensure WakeLock is held for the duration
        if (wakeLock?.isHeld == false) {
            wakeLock?.acquire((sets * (fast + slow) * 1000L) + 60000L)
        }

        val notification = createNotification("Workout Started", "Preparing...")
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun observeTimerState() {
        serviceScope.launch {
            combine(
                timerManager.timerState,
                timerManager.timeLeft,
                timerManager.currentSet
            ) { state, time, set ->
                Triple(state, time, set)
            }.collect { (state, time, set) ->
                handleStateChange(state, time, set)
            }
        }
    }

    private fun handleStateChange(state: TimerState, time: Int, set: Int) {
        when (state) {
            TimerState.FAST -> {
                if (time == fastSeconds) {
                    vibrateAndBeep()
                    if (set == 1) {
                        audioCoachManager.speak("Workout started. Begin fast walking.")
                    } else {
                        val remaining = totalSets - set + 1
                        val setsWord = if (remaining == 1) "set" else "sets"
                        audioCoachManager.speak("Set completed. $remaining $setsWord remaining. Switch to fast walking.")
                    }
                }
                updateNotification(state, time, set)
            }
            TimerState.SLOW -> {
                if (time == slowSeconds) {
                    vibrateAndBeep()
                    audioCoachManager.speak("Switch to slow walking.")
                }
                updateNotification(state, time, set)
            }
            TimerState.COMPLETED -> {
                vibrateAndBeep()
                audioCoachManager.speak("Workout complete. Great job.")
                saveAndStopWorkout()
            }
            TimerState.PAUSED -> {
                updateNotification(state, time, set)
            }
            else -> {}
        }
    }

    private fun vibrateAndBeep() {
        vibrate()
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP)
        } catch (e: Exception) {
            Log.e("WorkoutService", "Error playing beep: ${e.message}")
        }
    }

    private fun vibrate() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(500)
        }
    }

    private fun saveAndStopWorkout() {
        serviceScope.launch {
            val currentSet = timerManager.currentSet.value
            val currentState = timerManager.timerState.value
            val steps = stepSensorManager.steps.value
            val fastSecondsTotal = timerManager.totalFastSeconds.value
            val slowSecondsTotal = timerManager.totalSlowSeconds.value
            
            if (fastSecondsTotal > 0 || slowSecondsTotal > 0 || steps > 0) {
                val completedSets = if (currentState == TimerState.COMPLETED) {
                    totalSets
                } else {
                    (currentSet - 1).coerceAtLeast(0)
                }

                val session = WalkSession(
                    date = System.currentTimeMillis(),
                    durationMinutes = (fastSecondsTotal + slowSecondsTotal) / 60,
                    steps = steps,
                    totalSets = totalSets,
                    completedSets = completedSets,
                    fastMinutes = fastSecondsTotal / 60,
                    slowMinutes = slowSecondsTotal / 60
                )
                repository.insertSession(session)
                repository.insertStepLog(StepLog(timestamp = System.currentTimeMillis(), steps = steps))
            }
            
            stopWorkout()
        }
    }

    private fun stopWorkout() {
        timerManager.stopTimer()
        stepSensorManager.stopTracking()
        releaseWakeLock()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Workout Tracking",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(title: String, content: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(state: TimerState, time: Int, set: Int) {
        val mins = time / 60
        val secs = time % 60
        val timeStr = String.format("%02d:%02d", mins, secs)
        val content = "Set $set / $totalSets | $state | $timeStr"
        
        val notification = createNotification("Active Workout", content)
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        Log.d("WorkoutService", "Service onDestroy")
        serviceScope.cancel()
        audioCoachManager.shutdown()
        toneGenerator?.release()
        toneGenerator = null
        releaseWakeLock()
        super.onDestroy()
    }
}
