package com.oussama_chatri.productivityx.core.ui.animation

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

enum class FeedbackLevel {
    ALL,
    HAPTIC_ONLY,
    REDUCED,
    NONE,
}

data class FeedbackConfig(
    val level: FeedbackLevel = FeedbackLevel.ALL,
    val soundEnabled: Boolean = true,
    val reduceMotion: Boolean = false,
)

class FeedbackManager(
    private val config: FeedbackConfig = FeedbackConfig(),
) {
    companion object {
        private var _instance: FeedbackManager? = null

        fun getInstance(): FeedbackManager {
            if (_instance == null) {
                _instance = FeedbackManager()
            }
            return _instance!!
        }

        fun init(config: FeedbackConfig) {
            _instance = FeedbackManager(config)
        }
    }

    fun getConfig(): FeedbackConfig = config

    fun hapticClick(haptic: HapticFeedback) {
        if (config.level == FeedbackLevel.NONE || config.level == FeedbackLevel.REDUCED) return
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    fun hapticLightClick(haptic: HapticFeedback) {
        if (config.level == FeedbackLevel.NONE) return
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    fun hapticSuccess(haptic: HapticFeedback) {
        if (config.level == FeedbackLevel.NONE || config.level == FeedbackLevel.REDUCED) return
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    fun hapticError(haptic: HapticFeedback) {
        if (config.level == FeedbackLevel.NONE || config.level == FeedbackLevel.REDUCED) return
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    fun hapticLongPress(haptic: HapticFeedback) {
        if (config.level == FeedbackLevel.NONE || config.level == FeedbackLevel.REDUCED) return
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    fun hapticSwipe(haptic: HapticFeedback) {
        if (config.level == FeedbackLevel.NONE) return
        haptic.performHapticFeedback(composeHapticTypeForSwipe())
    }

    private fun composeHapticTypeForSwipe(): HapticFeedbackType {
        return HapticFeedbackType.LongPress
    }

    fun playSound(context: Context, soundEffect: Int = SoundEffectConstants.CLICK) {
        if (!config.soundEnabled || config.level == FeedbackLevel.NONE) return
    }

    fun triggerSystemHaptic(context: Context, constant: Int = HapticFeedbackConstants.KEYBOARD_TAP) {
        if (config.level == FeedbackLevel.NONE || config.level == FeedbackLevel.REDUCED) return
        try {
            val view = androidx.compose.ui.platform.ComposeView(context)
            view.performHapticFeedback(constant)
        } catch (_: Exception) {
            fallbackVibrate(context)
        }
    }

    private fun fallbackVibrate(context: Context) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            vibrator?.vibrate(
                VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } catch (_: Exception) {}
    }

    fun isReduceMotionEnabled(): Boolean = config.reduceMotion
}

@Composable
fun rememberFeedbackManager(): FeedbackManager {
    return remember { FeedbackManager.getInstance() }
}

object FeedbackDefaults {
    val HapticClick: HapticFeedbackType = HapticFeedbackType.TextHandleMove
    val HapticSuccess: HapticFeedbackType = HapticFeedbackType.LongPress
    val HapticError: HapticFeedbackType = HapticFeedbackType.LongPress
    val HapticLongPress: HapticFeedbackType = HapticFeedbackType.LongPress
}
