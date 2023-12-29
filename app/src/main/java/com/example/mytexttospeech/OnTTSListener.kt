package com.example.mytexttospeech

interface OnTTSListener {
    fun onReadyForSpeak()
    fun onError(error: String)
}