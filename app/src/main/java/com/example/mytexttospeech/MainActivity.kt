package com.example.mytexttospeech

import android.app.ProgressDialog
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
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

    private var file : File? = null
    var firstTime = true

    private val paragraph =
        "<speak> TextToSpeech API <break time=\"2s\"/> : Synthesizes speech from text for immediate playback or to create a sound file.\n" +
                "        A TextToSpeech instance can only be used to synthesize text once it has completed its initialization.\n" +
                "        Implement the TextToSpeech.OnInitListener to be notified of the completion of the initialization.\n" +
                "        When you are done using the TextToSpeech instance, call the shutdown() method to release the native\n" +
                "        resources used by the TextToSpeech engine. </speak>"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        textToSpeech = TextToSpeech(this, this, "com.google.android.tts")
        mProgressDialog = ProgressDialog(this)
        mediaPlayer = MediaPlayer()

        mProgressDialog!!.setCancelable(true)
        mProgressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        mProgressDialog!!.setMessage("Please wait ...")
        file = File(
            this.getExternalFilesDir(null),
            "ttsAudio"
        )
        binding.btnSpeak.setOnClickListener {
            if (mProcessed){
                if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
                    playMediaPlayer(1)
                }else{
                    playMediaPlayer(0)
                }
            }else{
                if (status == TextToSpeech.SUCCESS) {
                    //create directory
                    if (!file?.exists()!!) {
                        isDirectoryCreated = file?.mkdirs()!!
                        if (!isDirectoryCreated) {
                            Log.d("File", "file cannot create")
                        }
                    }

                    val params = Bundle()
                    params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceID);

                    //save tts as audio file
                    filePath = "${file?.absolutePath}/$utteranceID${System.currentTimeMillis()}.wav"
                    textToSpeech!!.synthesizeToFile(
                        paragraph,
                        params,
                        filePath?.let { path -> File(path) },
                        utteranceID
                    )

                    if(firstTime) {
                        mProgressDialog!!.show()
                        firstTime = false
                    }

                } else {
                    Toast.makeText(
                        baseContext,
                        "TextToSpeech Engine is not initialized",
                        Toast.LENGTH_SHORT
                    ).show()
                }
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
        Log.e("APP", textToSpeech?.engines.toString())
        textToSpeech?.let { setUpTextToSpeech(it) }
    }

    @Suppress("deprecation")
    private fun setUpTextToSpeech(tts: TextToSpeech) {
        textToSpeech = tts

        //Creating Text to speech engine is finished
//        textToSpeech!!.setOnUtteranceCompletedListener {
//
//        }

        textToSpeech!!.setOnUtteranceProgressListener(object : UtteranceProgressListener(){

            override fun onBeginSynthesis(
                utteranceId: String?,
                sampleRateInHz: Int,
                audioFormat: Int,
                channelCount: Int
            ) {
                super.onBeginSynthesis(utteranceId, sampleRateInHz, audioFormat, channelCount)
                Toast.makeText(applicationContext,"Systhesis Start", Toast.LENGTH_SHORT).show()
                Log.e("APP","Systhesis start")
            }


            override fun onStart(p0: String?) {
                Log.e("APP","onStart")
            }

            override fun onDone(p0: String?) {
                Log.e("APP", "onDone")
                mProcessed = true
                initializeMediaPlayer()
                playMediaPlayer(0)
            }

            override fun onError(p0: String?) {
                Toast.makeText(applicationContext,p0.toString(),Toast.LENGTH_SHORT).show()
                Log.e("APP",p0.toString())
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                super.onError(utteranceId, errorCode)
                //Toast.makeText(applicationContext,errorCode.toString(),Toast.LENGTH_SHORT).show()
                Log.e("APP",errorCode.toString())
            }

            override fun onAudioAvailable(utteranceId: String?, audio: ByteArray?) {
                super.onAudioAvailable(utteranceId, audio)
                Log.e("APP",audio.toString())
            }

            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                super.onRangeStart(utteranceId, start, end, frame)
                Log.e("APP","onRange start")
            }

            override fun onStop(utteranceId: String?, interrupted: Boolean) {
                super.onStop(utteranceId, interrupted)
                Log.e("APP","onStop")
            }
        })
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
            binding.btnSpeak.text = "Pause"
        }
        if (status == 1) {
            mediaPlayer?.pause()
            binding.btnSpeak.text = "Speak"
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