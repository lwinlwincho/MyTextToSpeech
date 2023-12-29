package com.example.mytexttospeech

import android.app.ProgressDialog
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.mytexttospeech.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityMainBinding

    private var textToSpeech: TextToSpeech? = null
    private var status = 0
    private var mediaPlayer: MediaPlayer? = null

    private var mProcessed = false
    private var mProgressDialog: ProgressDialog? = null

    private val exoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(this).build()
    }

    private val utteranceID = "lc_tts"
    private var filePath: String? = null
    private var isDirectoryCreated: Boolean = false

    private val file = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        "Text to speech audio"
    )

    private val paragraph =
        "TextToSpeech API : Synthesizes speech from text for immediate playback or to create a sound file.\n" +
                "        A TextToSpeech instance can only be used to synthesize text once it has completed its initialization.\n" +
                "        Implement the TextToSpeech.OnInitListener to be notified of the completion of the initialization.\n" +
                "        When you are done using the TextToSpeech instance, call the shutdown() method to release the native\n" +
                "        resources used by the TextToSpeech engine."


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        textToSpeech = TextToSpeech(this, this)
        mProgressDialog = ProgressDialog(this)
        mediaPlayer = MediaPlayer()

        mProgressDialog!!.setCancelable(true)
        mProgressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        mProgressDialog!!.setMessage("Please wait ...")

        binding.btnSpeak.setOnClickListener {
            if (status == TextToSpeech.SUCCESS) {

                binding.btnSpeak.text = "Pause"
                if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
                    playMediaPlayer(1)
                    binding.btnSpeak.text = "Speak"

                } else {
                    //create directory
                    if (!file.exists()) {
                        isDirectoryCreated = file.mkdirs()
                        if (!isDirectoryCreated) {
                            Log.d("File", "file cannot create")
                        }
                    }
                    file.mkdirs()

                    //save tts as audio file
                    filePath = "${file.absolutePath}/$utteranceID${System.currentTimeMillis()}.wav"
                    textToSpeech!!.synthesizeToFile(
                        paragraph,
                        null,
                        filePath?.let { path -> File(path) },
                        utteranceID
                    )
                    playMediaPlayer(0)
                }
                //  mProgressDialog!!.show()

            } else {
                Toast.makeText(
                    baseContext,
                    "TextToSpeech Engine is not initialized",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.btnPause.setOnClickListener {
            playMediaPlayer(1)
        }

        mediaPlayer!!.setOnCompletionListener {
            binding.btnSpeak.text = "Pause"
        }

        /* binding.etContent.addTextChangedListener(object : TextWatcher {
             override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
             override fun beforeTextChanged(
                 s: CharSequence, start: Int, count: Int,
                 after: Int
             ) {}
             override fun afterTextChanged(s: Editable) {
                 mProcessed = false
                 mediaPlayer!!.reset()
                 binding.btnSpeak.text = "Speak"
             }
         })*/
    }

    override fun onInit(status: Int) {
        this.status = status
        textToSpeech?.let { setUpTextToSpeech(it) }
    }

    @Suppress("deprecation")
    private fun setUpTextToSpeech(tts: TextToSpeech) {
        textToSpeech = tts

        //Creating Text to speech engine is finished
        textToSpeech!!.setOnUtteranceCompletedListener {
            mProcessed = true
            initializeMediaPlayer()
            playMediaPlayer(0)
        }
    }

    private fun initializeMediaPlayer() {
        val uri = filePath?.let { File(it).toUri() }
        mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
        try {
            if (uri != null) {
                mediaPlayer?.setDataSource(applicationContext, uri)
            }
            mediaPlayer?.prepare()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playMediaPlayer(status: Int) {
        mProgressDialog?.dismiss()
        if (status == 0) {
            mediaPlayer?.start()
        }
        if (status == 1) {
            mediaPlayer?.pause()
        }
    }

    override fun onPause() {
        textToSpeech?.stop()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        super.onPause()
    }

    override fun onStop() {
        textToSpeech?.stop()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        super.onStop()
    }

    override fun onDestroy() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        super.onDestroy()
    }


    private fun play(url: String) {
        if (!exoPlayer.isPlaying) {
            val mediaItem = MediaItem.Builder().setUri(url).build()
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()
            exoPlayer.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)
                    if (playbackState == ExoPlayer.STATE_ENDED) {
//                        previousUrl = ""
//                        ivAudioTool.pauseAnimation()
//                        ivAudioTool.progress = 0f
                    }
                }
            })
            //  previousUrl = url
        } else {
            exoPlayer.pause()
            // previousUrl = ""
            //ivAudioTool.pauseAnimation()
            //ivAudioTool.progress = 0f
        }
    }


    /* private var textToSpeechConverter: TextToSpeechConverter? = null

     override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)
         binding = ActivityMainBinding.inflate(layoutInflater)
         setContentView(binding.root)

         initTTS()

         binding.btnSpeak.setOnClickListener {
             requestForTextSpeak(
                 " TextToSpeech API : Synthesizes speech from text for immediate playback or to create a sound file.\n" +
                         "        A TextToSpeech instance can only be used to synthesize text once it has completed its initialization.\n" +
                         "        Implement the TextToSpeech.OnInitListener to be notified of the completion of the initialization.\n" +
                         "        When you are done using the TextToSpeech instance, call the shutdown() method to release the native\n" +
                         "        resources used by the TextToSpeech engine.",
                 "en"
             )
         }
     }

     private fun initTTS() {
         textToSpeechConverter = TextToSpeechConverter(this, object : OnTTSListener {
             override fun onReadyForSpeak() {
             }

             override fun onError(error: String) {
                 showToast(error)
             }
         })
     }

     private fun requestForTextSpeak(text: String, langCode: String) {
         textToSpeechConverter?.speakText(text, langCode)
     }

     private fun showToast(message: String) {
         Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
     }

     override fun onPause() {
         textToSpeechConverter?.onStopTTS()
         super.onPause()
     }

     override fun onStop() {
         textToSpeechConverter?.onStopTTS()
         super.onStop()
     }

     override fun onDestroy() {
         textToSpeechConverter?.onShutdownTTS()
         super.onDestroy()
     }*/
}