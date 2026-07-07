package com.oussama_chatri.productivityx.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.oussama_chatri.productivityx.R
import com.oussama_chatri.productivityx.core.ui.theme.ProductivityXTheme
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import kotlin.math.roundToInt

enum class PxTextFieldState { Default, Success, Loading, Verified, Error }

@Composable
fun PxTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    onPasswordToggle: (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    supportingText: String? = null,
    maxLines: Int = 1,
    maxLength: Int = Int.MAX_VALUE,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onDone: (() -> Unit)? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    showClearButton: Boolean = false,
    fieldState: PxTextFieldState = PxTextFieldState.Default,
    onVoiceInput: (() -> Unit)? = null,
) {
    val visualTransformation = when {
        isPassword && !isPasswordVisible -> PasswordVisualTransformation()
        else -> VisualTransformation.None
    }

    val currentError = isError || fieldState == PxTextFieldState.Error
    val showTrailingClear = showClearButton && value.isNotEmpty() && enabled && !readOnly
    val infiniteTransition = rememberInfiniteTransition(label = "fieldLoading")
    val loadingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "loadingAlpha",
    )

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { newVal ->
                if (newVal.length <= maxLength) onValueChange(newVal)
            },
            modifier = Modifier.fillMaxWidth(),
            label = label?.let { { Text(it) } },
            placeholder = placeholder?.let {
                { Text(it, color = PxColors.OnSurfaceDim) }
            },
            leadingIcon = leadingIcon?.let {
                {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = when {
                            currentError -> PxColors.Error
                            fieldState == PxTextFieldState.Success -> PxColors.Success
                            else -> PxColors.OnSurfaceDim
                        },
                    )
                }
            },
            trailingIcon = when {
                isPassword -> {
                    {
                        IconButton(onClick = { onPasswordToggle?.invoke() }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Outlined.Visibility
                                else Icons.Outlined.VisibilityOff,
                                contentDescription = stringResource(R.string.cd_password_toggle),
                                modifier = Modifier.size(20.dp),
                                tint = PxColors.OnSurfaceDim,
                            )
                        }
                    }
                }
                fieldState == PxTextFieldState.Success -> {
                    {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = PxColors.Success,
                        )
                    }
                }
                fieldState == PxTextFieldState.Loading -> {
                    {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(PxColors.Primary.copy(alpha = loadingAlpha)),
                        )
                    }
                }
                showTrailingClear -> {
                    {
                        IconButton(onClick = { onValueChange("") }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Clear",
                                modifier = Modifier.size(20.dp),
                                tint = PxColors.OnSurfaceDim,
                            )
                        }
                    }
                }
                trailingIcon != null -> {
                    {
                        IconButton(onClick = { onTrailingIconClick?.invoke() }) {
                            Icon(
                                imageVector = trailingIcon,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = PxColors.OnSurfaceDim,
                            )
                        }
                    }
                }
                onVoiceInput != null -> {
                    {
                        IconButton(onClick = { onVoiceInput.invoke() }) {
                            Icon(
                                imageVector = Icons.Filled.Mic,
                                contentDescription = "Voice input",
                                modifier = Modifier.size(20.dp),
                                tint = PxColors.OnSurfaceDim,
                            )
                        }
                    }
                }
                else -> null
            },
            isError = currentError,
            visualTransformation = visualTransformation,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction,
            ),
            keyboardActions = KeyboardActions(
                onDone = { onDone?.invoke() },
                onNext = {},
            ),
            singleLine = maxLines == 1,
            maxLines = maxLines,
            enabled = enabled,
            readOnly = readOnly,
            shape = RoundedCornerShape(10.dp),
            prefix = prefix,
            suffix = suffix,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = PxColors.SurfaceVariant,
                unfocusedContainerColor = PxColors.SurfaceVariant,
                disabledContainerColor = PxColors.SurfaceVariant,
                focusedBorderColor = when {
                    currentError -> PxColors.Error
                    fieldState == PxTextFieldState.Success -> PxColors.Success
                    else -> PxColors.Primary
                },
                unfocusedBorderColor = PxColors.Outline,
                errorBorderColor = PxColors.Error,
                focusedLabelColor = when {
                    currentError -> PxColors.Error
                    fieldState == PxTextFieldState.Success -> PxColors.Success
                    else -> PxColors.Primary
                },
                unfocusedLabelColor = PxColors.OnSurfaceDim,
                errorLabelColor = PxColors.Error,
                focusedTextColor = PxColors.OnBackground,
                unfocusedTextColor = PxColors.OnBackground,
                cursorColor = PxColors.Primary,
                errorCursorColor = PxColors.Error,
            ),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            val helperText = when {
                currentError && errorMessage != null -> errorMessage
                fieldState == PxTextFieldState.Verified -> "Verified"
                else -> supportingText
            }
            if (helperText != null) {
                Text(
                    text = helperText,
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        currentError -> PxColors.Error
                        fieldState == PxTextFieldState.Verified -> PxColors.Success
                        else -> PxColors.OnSurfaceDim
                    },
                )
            }
            if (maxLength != Int.MAX_VALUE) {
                val progress = (value.length.toFloat() / maxLength).coerceIn(0f, 1f)
                val progressColor by animateColorAsState(
                    targetValue = when {
                        progress >= 1f -> PxColors.Error
                        progress >= 0.8f -> PxColors.Warning
                        else -> PxColors.Primary
                    },
                    animationSpec = tween(200),
                    label = "counterColor",
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${value.length}/$maxLength",
                        style = MaterialTheme.typography.labelSmall,
                        color = progressColor,
                    )
                    Spacer(Modifier.height(2.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .width(48.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = progressColor,
                        trackColor = PxColors.Outline.copy(alpha = 0.3f),
                    )
                }
            }
        }
    }
}

@Composable
fun PxSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search",
    onVoiceInput: (() -> Unit)? = null,
    enabled: Boolean = true,
) {
    PxTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = placeholder,
        leadingIcon = Icons.Filled.Search,
        showClearButton = true,
        trailingIcon = if (onVoiceInput != null) Icons.Filled.Mic else null,
        onTrailingIconClick = onVoiceInput,
        enabled = enabled,
    )
}

@Composable
fun FormField(
    label: String,
    modifier: Modifier = Modifier,
    required: Boolean = false,
    error: String? = null,
    helper: String? = null,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier) {
        Text(
            text = if (required) "$label *" else label,
            style = MaterialTheme.typography.labelLarge,
            color = if (error != null) PxColors.Error else PxColors.OnSurface,
        )
        Spacer(Modifier.height(6.dp))
        content()
        if (error != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = PxColors.Error,
            )
        } else if (helper != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = helper,
                style = MaterialTheme.typography.bodySmall,
                color = PxColors.OnSurfaceDim,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F14)
@Composable
private fun PxTextFieldPreview() {
    ProductivityXTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            PxTextField(value = "test@email.com", onValueChange = {}, label = "Email address")
            PxTextField(value = "", onValueChange = {}, label = "With max", maxLength = 50)
            PxTextField(value = "error", onValueChange = {}, label = "With error", isError = true, errorMessage = "Invalid input")
            PxSearchField(value = "", onValueChange = {}, placeholder = "Search notes")
            PxTextField(value = "Success", onValueChange = {}, label = "Success state", fieldState = PxTextFieldState.Success)
            FormField(label = "Full Name", required = true, error = null, helper = "Enter your first and last name") {
                PxTextField(value = "John Doe", onValueChange = {}, label = null)
            }
        }
    }
}
