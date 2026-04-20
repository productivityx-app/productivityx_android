//package com.oussama_chatri.productivityx.core.ui.components
//
//import androidx.compose.animation.animateColorAsState
//import androidx.compose.animation.core.tween
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.text.BasicTextField
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.focus.FocusRequester
//import androidx.compose.ui.focus.focusRequester
//import androidx.compose.ui.graphics.SolidColor
//import androidx.compose.ui.text.TextRange
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.input.ImeAction
//import androidx.compose.ui.text.input.KeyboardType
//import androidx.compose.ui.text.input.TextFieldValue
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.oussama_chatri.productivityx.core.ui.theme.ProductivityXTheme
//import com.oussama_chatri.productivityx.core.ui.theme.PxColors
//
///**
// * 6-digit OTP input displayed as individual boxes.
// *
// * Improvements over original:
// * - Auto-focuses on first composition
// * - Cursor is always at end of input (prevents mid-string cursor positioning issues)
// * - Active cell highlighted with Primary border
// * - Filled cells show SurfaceVariant background
// * - Accepts only digits
// */
//@Composable
//fun OtpInputField(
//    value: String,
//    onValueChange: (String) -> Unit,
//    modifier: Modifier = Modifier,
//    length: Int = 6,
//    onComplete: ((String) -> Unit)? = null
//) {
//    val focusRequester = remember { FocusRequester() }
//
//    // Auto-focus when this composable enters composition
//    LaunchedEffect(Unit) {
//        focusRequester.requestFocus()
//    }
//
//    // Trigger onComplete callback when all digits are entered
//    LaunchedEffect(value) {
//        if (value.length == length) {
//            onComplete?.invoke(value)
//        }
//    }
//
//    // Hidden BasicTextField that captures actual input
//    BasicTextField(
//        value = TextFieldValue(
//            text = value,
//            selection = TextRange(value.length) // keep cursor at end
//        ),
//        onValueChange = { tfv ->
//            val filtered = tfv.text.filter { it.isDigit() }.take(length)
//            if (filtered != value) onValueChange(filtered)
//        },
//        modifier = Modifier
//            .focusRequester(focusRequester)
//            .width(1.dp)
//            .height(1.dp), // visually hidden — boxes below are the display
//        keyboardOptions = KeyboardOptions(
//            keyboardType = KeyboardType.NumberPassword,
//            imeAction = ImeAction.Done
//        ),
//        cursorBrush = SolidColor(PxColors.Primary),
//        decorationBox = { }
//    )
//
//    // Visual OTP boxes
//    Row(
//        modifier = modifier.fillMaxWidth(),
//        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
//    ) {
//        repeat(length) { index ->
//            val char = value.getOrNull(index)
//            val isFocused = index == value.length && value.length < length
//
//            val borderColor by animateColorAsState(
//                targetValue = when {
//                    isFocused -> PxColors.Primary
//                    char != null -> PxColors.Primary.copy(alpha = 0.5f)
//                    else -> PxColors.Outline
//                },
//                animationSpec = tween(150),
//                label = "otpBorder$index"
//            )
//
//            val bgColor by animateColorAsState(
//                targetValue = if (char != null) PxColors.SurfaceVariant else PxColors.Surface,
//                animationSpec = tween(150),
//                label = "otpBg$index"
//            )
//
//            Box(
//                modifier = Modifier
//                    .weight(1f)
//                    .height(56.dp)
//                    .clip(RoundedCornerShape(10.dp))
//                    .background(bgColor)
//                    .border(
//                        width = if (isFocused) 2.dp else 1.5.dp,
//                        color = borderColor,
//                        shape = RoundedCornerShape(10.dp)
//                    ),
//                contentAlignment = Alignment.Center
//            ) {
//                if (char != null) {
//                    Text(
//                        text = char.toString(),
//                        style = MaterialTheme.typography.titleLarge.copy(
//                            fontSize = 22.sp,
//                            fontWeight = FontWeight.Bold
//                        ),
//                        color = PxColors.OnBackground,
//                        textAlign = TextAlign.Center
//                    )
//                } else if (isFocused) {
//                    // blinking cursor indicator
//                    Box(
//                        modifier = Modifier
//                            .width(2.dp)
//                            .height(24.dp)
//                            .background(PxColors.Primary)
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Preview(showBackground = true, backgroundColor = 0xFF0F0F14)
//@Composable
//private fun OtpInputPreview() {
//    ProductivityXTheme {
//        OtpInputField(value = "123", onValueChange = {})
//    }
//}