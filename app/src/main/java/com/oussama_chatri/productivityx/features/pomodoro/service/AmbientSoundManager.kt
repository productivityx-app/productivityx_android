package com.oussama_chatri.productivityx.features.pomodoro.service

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.util.Log
import com.oussama_chatri.productivityx.R
import com.oussama_chatri.productivityx.features.pomodoro.presentation.state.AmbientSound
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AmbientSoundManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var mediaPlayer: MediaPlayer? = null
    private var currentSound: AmbientSound = AmbientSound.NONE
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null
    private val TAG = "AmbientSoundManager"

    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> stopSound()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> mediaPlayer?.pause()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> mediaPlayer?.setVolume(0.2f, 0.2f)
            AudioManager.AUDIOFOCUS_GAIN -> {
                mediaPlayer?.setVolume(1.0f, 1.0f)
                mediaPlayer?.start()
            }
        }
    }

    fun playSound(sound: AmbientSound) {
        if (currentSound == sound && mediaPlayer?.isPlaying == true) return

        stopSound()
        currentSound = sound

        if (sound == AmbientSound.NONE) return

        val resId: Int? = when (sound) {
            AmbientSound.RAIN -> R.raw.rain
            AmbientSound.CAFE -> R.raw.coffe
            AmbientSound.WHITE_NOISE -> R.raw.white_noise
            AmbientSound.NATURE -> R.raw.nature
            AmbientSound.NONE -> null
        }

        resId?.let { id ->
            try {
                // Request Audio Focus
                val focusResult = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                        .setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build()
                        )
                        .setAcceptsDelayedFocusGain(true)
                        .setOnAudioFocusChangeListener(audioFocusChangeListener)
                        .build()
                    audioFocusRequest = request
                    audioManager.requestAudioFocus(request)
                } else {
                    @Suppress("DEPRECATION")
                    audioManager.requestAudioFocus(
                        audioFocusChangeListener,
                        AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN
                    )
                }

                if (focusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    Log.e(TAG, "Audio focus request denied")
                    currentSound = AmbientSound.NONE
                    return
                }

                // Use direct instantiation to set AudioAttributes before prepare()
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    val afd = context.resources.openRawResourceFd(id)
                    if (afd == null) {
                        Log.e(TAG, "Resource not found for id $id")
                        currentSound = AmbientSound.NONE
                        return
                    }
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    afd.close()
                    isLooping = true
                    setOnErrorListener { mp, what, extra ->
                        Log.e(TAG, "MediaPlayer error: what=$what extra=$extra")
                        mp.release()
                        mediaPlayer = null
                        currentSound = AmbientSound.NONE
                        true
                    }
                    prepare()
                    start()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while playing sound: ${e.message}", e)
                currentSound = AmbientSound.NONE
                mediaPlayer?.release()
                mediaPlayer = null
            }
        }
    }

    fun stopSound() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
            audioFocusRequest = null
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(audioFocusChangeListener)
        }

        mediaPlayer?.let {
            try {
                if (it.isPlaying) {
                    it.stop()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while stopping sound: ${e.message}", e)
            } finally {
                it.release()
            }
        }
        mediaPlayer = null
        currentSound = AmbientSound.NONE
    }

    fun release() {
        stopSound()
    }
}
