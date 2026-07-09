package com.oussama_chatri.productivityx.features.home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oussama_chatri.productivityx.core.ui.theme.PxColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorBottomSheet(
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PxColors.Surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        CalculatorContent()
    }
}

@Composable
private fun CalculatorContent() {
    var displayValue by remember { mutableStateOf("0") }
    var previousValue by remember { mutableStateOf("") }
    var operation by remember { mutableStateOf("") }
    var shouldClearOnNextDigit by remember { mutableStateOf(false) }

    fun handleKeyPress(key: String) {
        when (key) {
            "AC" -> {
                displayValue = "0"
                previousValue = ""
                operation = ""
                shouldClearOnNextDigit = false
            }
            "⌫" -> {
                if (displayValue.length > 1) {
                    displayValue = displayValue.dropLast(1)
                } else {
                    displayValue = "0"
                }
            }
            "÷", "×", "-", "+" -> {
                if (operation.isNotEmpty() && !shouldClearOnNextDigit) {
                    // Calculate intermediate result
                    val result = performOperation(previousValue, displayValue, operation)
                    displayValue = result
                    previousValue = result
                } else {
                    previousValue = displayValue
                }
                operation = key
                shouldClearOnNextDigit = true
            }
            "=" -> {
                if (operation.isNotEmpty()) {
                    val result = performOperation(previousValue, displayValue, operation)
                    displayValue = result
                    previousValue = ""
                    operation = ""
                    shouldClearOnNextDigit = true
                }
            }
            "." -> {
                if (shouldClearOnNextDigit) {
                    displayValue = "0."
                    shouldClearOnNextDigit = false
                } else if (!displayValue.contains(".")) {
                    displayValue += "."
                }
            }
            else -> { // Digits 0-9
                if (shouldClearOnNextDigit || displayValue == "0") {
                    displayValue = key
                    shouldClearOnNextDigit = false
                } else {
                    displayValue += key
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = displayValue,
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 56.sp,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.End
            ),
            color = PxColors.OnSurface,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        )

        val keys = listOf(
            listOf("AC", "⌫", "÷", "×"),
            listOf("7", "8", "9", "-"),
            listOf("4", "5", "6", "+"),
            listOf("1", "2", "3", "="),
            listOf("0", ".", "")
        )

        keys.forEachIndexed { rowIndex, row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                row.forEach { key ->
                    if (key.isNotEmpty()) {
                        val isOp = key in listOf("÷", "×", "-", "+", "=")
                        val isClear = key in listOf("AC", "⌫")
                        val weight = if (key == "0") 2f else 1f
                        
                        // For the equals button, make it span vertically if needed or keep it simple
                        // We will keep a simple grid for now.
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(weight)
                                .aspectRatio(weight * 1f) // Ensure it's roughly square or pill-shaped
                                .clip(if (weight == 1f) CircleShape else RoundedCornerShape(100.dp))
                                .background(
                                    when {
                                        isOp -> PxColors.Primary
                                        isClear -> PxColors.SurfaceVariant
                                        else -> PxColors.Background
                                    }
                                )
                                .clickable { handleKeyPress(key) }
                        ) {
                            Text(
                                text = key,
                                style = MaterialTheme.typography.titleLarge,
                                color = when {
                                    isOp -> Color.White
                                    isClear -> PxColors.OnSurfaceDim
                                    else -> PxColors.OnBackground
                                }
                            )
                        }
                    } else if (rowIndex == 4) {
                        // Empty space to balance the "0" taking 2 weights, but "0" doesn't actually span in a 1-weight grid unless we pad it.
                        // Wait, 0 takes weight 2, . takes 1, the remaining space must be filled.
                        // The last row has 3 items: "0", ".", "". Wait, previous rows had 4 items.
                        // "0" (wt 2), "." (wt 1). Total weight = 3. Previous rows had total weight = 4.
                        // We need to add one more dummy spacer of weight 1 to make it align with column 4!
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            if (rowIndex < keys.size - 1) {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

private fun performOperation(val1: String, val2: String, op: String): String {
    try {
        val num1 = val1.toDoubleOrNull() ?: return val2
        val num2 = val2.toDoubleOrNull() ?: return val2
        val result = when (op) {
            "+" -> num1 + num2
            "-" -> num1 - num2
            "×" -> num1 * num2
            "÷" -> if (num2 != 0.0) num1 / num2 else Double.NaN
            else -> num2
        }
        
        // Remove trailing .0 if integer
        if (result.isNaN()) return "Error"
        if (result == result.toLong().toDouble()) {
            return result.toLong().toString()
        }
        return result.toString()
    } catch (e: Exception) {
        return "Error"
    }
}
