package com.binodnagarkoti.intervalwalktracker.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class AudioCoachManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val pendingMessages = mutableListOf<String>()

    init {
        initializeTTS()
    }

    private fun initializeTTS() {
        try {
            tts = TextToSpeech(context, this)
        } catch (e: Exception) {
            Log.e("AudioCoachManager", "Error creating TTS: ${e.message}")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("AudioCoachManager", "Language not supported")
            } else {
                isInitialized = true
                Log.d("AudioCoachManager", "TTS Initialized successfully")
                // Speak any pending messages
                synchronized(pendingMessages) {
                    for (msg in pendingMessages) {
                        tts?.speak(msg, TextToSpeech.QUEUE_ADD, null, null)
                    }
                    pendingMessages.clear()
                }
            }
        } else {
            Log.e("AudioCoachManager", "Initialization failed with status: $status")
        }
    }

    fun speak(message: String) {
        if (isInitialized) {
            tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            Log.d("AudioCoachManager", "TTS not initialized. Queuing message: $message")
            synchronized(pendingMessages) {
                pendingMessages.add(message)
            }
            if (tts == null) {
                initializeTTS()
            }
        }
    }

    fun shutdown() {
        Log.d("AudioCoachManager", "Shutting down TTS")
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
