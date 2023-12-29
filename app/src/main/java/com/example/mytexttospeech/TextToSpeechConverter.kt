package com.example.mytexttospeech

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.example.mytexttospeech.Constants.getUriSpeaking
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TextToSpeechConverter(
    private val context: Context,
    private val onTTSListener: OnTTSListener
) {

    private val ttsTAG = "ttsTAG"
    private var mediaPlayer: MediaPlayer? = null

    fun speakText(message: String, langCode: String) {
        try {
            speakNow(getUriSpeaking(message, langCode))
        } catch (ex: Exception) {
            onTTSListener.onError("Oops! Something went wrong. Try again, please!")
            Log.e(ttsTAG, "speakText: ${ex.message}")
        }
    }

    private fun speakNow(uri: Uri?) {
        if (uri == null) {
            return
        }
        try {
            stopTTSMediaPlayer()
            mediaPlayer = MediaPlayer()
            mediaPlayer?.apply {
                setDataSource(context, uri)
                setOnPreparedListener { mp: MediaPlayer ->
                    try {
                        mp.start()
                    } catch (ex: Exception) {
                        onTTSListener.onError("Oops! Something went wrong. Try again, please!")
                        Log.e(ttsTAG, "speakNow: ${ex.message}")
                    }
                }
                prepareAsync()
            }
        } catch (ex: Exception) {
            onTTSListener.onError("Oops! Something went wrong. Try again, please!")
            Log.e(ttsTAG, "speakNow: ${ex.message}")
        }
    }

    fun stopTTSMediaPlayer() {
        try {
            try {
                mediaPlayer?.let {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            it.release()
                        } catch (ex: Exception) {
                            Log.e(ttsTAG, "stopTTSMediaPlayer: ${ex.message}")
                        }
                    }
                }
            } catch (ex: Exception) {
                Log.e(ttsTAG, "stopTTSMediaPlayer: ${ex.message}")
            }
            mediaPlayer = null
        } catch (ex: Exception) {
            Log.e(ttsTAG, "stopTTSMediaPlayer: ${ex.message}")
        }
    }
}