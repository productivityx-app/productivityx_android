package com.oussama_chatri.productivityx.core.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oussama_chatri.productivityx.core.ui.theme.ProductivityXTheme
import com.oussama_chatri.productivityx.core.ui.theme.PxColors

enum class PxButtonVariant { Primary, Outlined, Ghost }

@Composable
fun PxButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    variant: PxButtonVariant = PxButtonVariant.Primary
) {
    val isEnabled = enabled && !isLoading
    val shape = RoundedCornerShape(8.dp)
    val height = Modifier.height(48.dp)

    when (variant) {
        PxButtonVariant.Primary -> Button(
            onClick = onClick,
            modifier = modifier.then(height),
            enabled = isEnabled,
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = PxColors.Primary,
                contentColor = Color.White,
                disabledContainerColor = PxColors.Primary.copy(alpha = 0.5f),
                disabledContentColor = Color.White.copy(alpha = 0.7f)
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(text, fontWeight = FontWeight.SemiBold)
            }
        }

        PxButtonVariant.Outlined -> OutlinedButton(
            onClick = onClick,
            modifier = modifier.then(height),
            enabled = isEnabled,
            shape = shape,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = PxColors.Primary,
                disabledContentColor = PxColors.OnSurfaceDim
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = PxColors.Primary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(text, fontWeight = FontWeight.Medium)
            }
        }

        PxButtonVariant.Ghost -> TextButton(
            onClick = onClick,
            modifier = modifier.then(height),
            enabled = isEnabled,
            shape = shape
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = PxColors.Primary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(text, color = PxColors.Primary, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F14)
@Composable
private fun PxButtonPreview() {
    ProductivityXTheme {
        PxButton(text = "Sign in", onClick = {}, isLoading = false)
    }
}