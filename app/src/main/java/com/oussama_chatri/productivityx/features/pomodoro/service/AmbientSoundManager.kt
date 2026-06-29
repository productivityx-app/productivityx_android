package com.oussama_chatri.productivityx.features.pomodoro.service

import android.content.Context
import android.media.MediaPlayer
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

    fun playSound(sound: AmbientSound) {
        if (currentSound == sound) return

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
                mediaPlayer = MediaPlayer.create(context, id)
                mediaPlayer?.isLooping = true
                mediaPlayer?.setOnErrorListener { mp, _, _ ->
                    mp.release()
                    mediaPlayer = null
                    true
                }
                mediaPlayer?.start()
            } catch (e: Exception) {
                currentSound = AmbientSound.NONE
                mediaPlayer = null
            }
        }
    }

    fun stopSound() {
        mediaPlayer?.let {
            try {
                if (it.isPlaying) {
                    it.stop()
                }
            } catch (e: Exception) {
                // Ignore
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
