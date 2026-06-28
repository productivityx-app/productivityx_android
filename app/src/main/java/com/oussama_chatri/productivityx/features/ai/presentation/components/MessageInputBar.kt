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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.oussama_chatri.productivityx.R
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
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

    HorizontalDivider(color = PxColors.SurfaceVariant)

    Row(
        modifier          = modifier
            .fillMaxWidth()
            .background(PxColors.SurfaceVariant)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(PxColors.Surface)
                .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            if (value.isEmpty()) {
                Text(
                    text  = stringResource(R.string.ai_message_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = PxColors.OnSurfaceDim,
                )
            }
            BasicTextField(
                value           = value,
                onValueChange   = onValueChange,
                textStyle       = MaterialTheme.typography.bodyMedium.copy(color = PxColors.OnBackground),
                cursorBrush     = SolidColor(PxColors.Primary),
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
                .background(if (canSend) PxColors.Primary else PxColors.SurfaceVariant),
        ) {
            Icon(
                imageVector        = Icons.Outlined.Send,
                contentDescription = stringResource(R.string.cd_send_message),
                tint               = if (canSend) Color.White else PxColors.OnSurfaceDim,
                modifier           = Modifier.size(20.dp),
            )
        }
    }
}
