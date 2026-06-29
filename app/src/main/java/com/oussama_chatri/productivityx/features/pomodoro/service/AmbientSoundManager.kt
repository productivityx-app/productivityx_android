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

        val resId: Int? = when (sound) {
            AmbientSound.RAIN -> null // Replace with actual R.raw.rain if available
            AmbientSound.CAFE -> null 
            AmbientSound.WHITE_NOISE -> null
            AmbientSound.NATURE -> null
            AmbientSound.NONE -> null
        }

        resId?.let { id ->
            mediaPlayer = MediaPlayer.create(context, id)
            mediaPlayer?.isLooping = true
            mediaPlayer?.start()
        }
    }

    fun stopSound() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
        currentSound = AmbientSound.NONE
    }
}
