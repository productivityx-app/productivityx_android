package com.oussama_chatri.productivityx.core.ui.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object Spacing {
    val xxs  = 2.dp
    val xs   = 4.dp
    val sm   = 8.dp
    val md   = 12.dp
    val lg   = 16.dp
    val xl   = 20.dp
    val xxl  = 24.dp
    val xxxl = 32.dp
    val huge = 48.dp
    val section   = 24.dp
    val sectionLarge = 32.dp
    val page = 48.dp
}

object LayoutTokens {
    val gridCell = 16.dp
    val gridGap  = 16.dp
    val contentMaxWidth = 600.dp

    val ItemSpacing    = Spacing.sm
    val SectionSpacing = Spacing.section
    val ScreenPadding  = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.sm)
    val CardPadding    = PaddingValues(Spacing.lg)
    val ListPadding    = PaddingValues(horizontal = Spacing.lg)
    val DialogPadding  = PaddingValues(Spacing.xxl)
}

object Breakpoints {
    val Compact  = 0.dp
    val Medium   = 600.dp
    val Expanded = 840.dp

    enum class WindowSizeClass { Compact, Medium, Expanded }

    fun classify(width: Dp): WindowSizeClass = when {
        width < Medium  -> WindowSizeClass.Compact
        width < Expanded -> WindowSizeClass.Medium
        else             -> WindowSizeClass.Expanded
    }

    fun horizontalPadding(windowWidth: Dp): PaddingValues = when {
        windowWidth < Medium  -> PaddingValues(horizontal = Spacing.lg)
        windowWidth < Expanded -> PaddingValues(horizontal = Spacing.xxxl)
        else                   -> PaddingValues(horizontal = Spacing.huge)
    }

    fun contentMaxWidth(windowWidth: Dp): Dp = when {
        windowWidth < Medium  -> Dp.Unspecified
        else                   -> LayoutTokens.contentMaxWidth
    }
}

@Composable
fun PxScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable (() -> Unit) = {},
    bottomBar: @Composable (() -> Unit) = {},
    snackbarHost: @Composable (() -> Unit) = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier
            .statusBarsPadding()
            .navigationBarsPadding()
            .consumeWindowInsets(WindowInsets(0, 0, 0, 0)),
        topBar = topBar,
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
        content = content,
    )
}
