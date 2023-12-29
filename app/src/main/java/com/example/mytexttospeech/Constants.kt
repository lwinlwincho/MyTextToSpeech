package com.example.mytexttospeech

import android.net.Uri
import android.util.Log
import java.net.URLEncoder

object Constants {
    var URL_SPEAKING = "https://translate.google.com/translate_tts?ie=UTF-8"
    var URL_SPEAKING_SECRET = "&client=tw-ob"
    var URL_TRANSLATION_SECRET = "UTF-8"

    fun getUriSpeaking(nativeText: String, langCode: String): Uri? {
        var speakWord = nativeText
        try {
            if (speakWord.length > 150) {
                speakWord = speakWord.substring(0, 150).trim { it <= ' ' }
                if (speakWord.contains(" ")) {
                    speakWord = speakWord.substring(
                        0,
                        Math.min(speakWord.lastIndexOf(' '), speakWord.length)
                    )
                }
            }
        } catch (ex: Exception) {
            Log.e("getUriSpeakingTAG", "getUriSpeaking: ${ex.message}")
        }
        try {
            return Uri.parse(
                "$URL_SPEAKING&q=" + URLEncoder.encode(
                    speakWord,
                    URL_TRANSLATION_SECRET
                ) + "&tl=" + langCode + URL_SPEAKING_SECRET
            )
        } catch (ex1: Exception) {
            Log.e("getUriSpeakingTAG", "getUriSpeaking: ${ex1.message}")
            try {
                return if (speakWord.contains(" ")) {
                    Uri.parse(
                        "$URL_SPEAKING&q=" + speakWord.replace(
                            " ".toRegex(),
                            "%20"
                        ) + "&tl=" + langCode + URL_SPEAKING_SECRET
                    )
                } else {
                    Uri.parse("$URL_SPEAKING&q=$speakWord&tl=$langCode$URL_SPEAKING_SECRET")
                }
            } catch (ex2: Exception) {
                Log.e("getUriSpeakingTAG", "getUriSpeaking: ${ex2.message}")
            }
        }
        return null
    }
}