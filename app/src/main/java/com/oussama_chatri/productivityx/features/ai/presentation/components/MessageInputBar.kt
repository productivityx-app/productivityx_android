package com.oussama_chatri.productivityx.features.ai.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.oussama_chatri.productivityx.core.util.rememberDebouncedClick

@Composable
fun MessageInputBar(
    value      : String,
    onValueChange : (String) -> Unit,
    onSend     : () -> Unit,
    isStreaming : Boolean,
    modifier   : Modifier = Modifier,
) {
    val canSend = value.isNotBlank() && !isStreaming

    HorizontalDivider(color = Color(0xFF252533))

    Row(
        modifier          = modifier
            .fillMaxWidth()
            .background(Color(0xFF252533))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF1A1A24))
                .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            if (value.isEmpty()) {
                Text(
                    text  = "Message…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF888899),
                )
            }
            BasicTextField(
                value           = value,
                onValueChange   = onValueChange,
                textStyle       = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFEEEEF5)),
                cursorBrush     = SolidColor(Color(0xFF6366F1)),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction      = ImeAction.Send,
                ),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onSend = { if (canSend) onSend() }
                ),
            )
        }

        Spacer(Modifier.width(10.dp))

        IconButton(
            onClick  = rememberDebouncedClick(onClick = onSend),
            enabled  = canSend,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (canSend) Color(0xFF6366F1) else Color(0xFF252533)),
        ) {
            Icon(
                imageVector        = Icons.Outlined.Send,
                contentDescription = "Send",
                tint               = if (canSend) Color.White else Color(0xFF888899),
                modifier           = Modifier.size(20.dp),
            )
        }
    }
}
