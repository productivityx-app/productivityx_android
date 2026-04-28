package com.oussama_chatri.productivityx.core.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun rememberDebouncedClick(
    thresholdMs: Long = 600L,
    onClick: () -> Unit,
): () -> Unit {
    val lastClickTime = remember { androidx.compose.runtime.mutableLongStateOf(0L) }
    return remember(onClick) {
        {
            val now = System.currentTimeMillis()
            if (now - lastClickTime.longValue > thresholdMs) {
                lastClickTime.longValue = now
                onClick()
            }
        }
    }
}