package com.oussama_chatri.productivityx.core.util

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import com.oussama_chatri.productivityx.BuildConfig

object PerformanceMonitor {
    private const val TAG = "PerformanceMonitor"

    @Composable
    fun TrackRecomposition(name: String) {
        if (!BuildConfig.DEBUG) return
        val count = remember { Ref(0) }
        SideEffect {
            count.value++
            Log.d(TAG, "Recomposition: $name - Count: ${count.value}")
        }
    }

    private class Ref<T>(var value: T)
}
