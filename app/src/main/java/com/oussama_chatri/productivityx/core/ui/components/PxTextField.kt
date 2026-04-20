package com.oussama_chatri.productivityx.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oussama_chatri.productivityx.R
import com.oussama_chatri.productivityx.core.ui.theme.ProductivityXTheme
import com.oussama_chatri.productivityx.core.ui.theme.PxColors

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
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onDone: (() -> Unit)? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false
) {
    val visualTransformation = when {
        isPassword && !isPasswordVisible -> PasswordVisualTransformation()
        else -> VisualTransformation.None
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
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
                        tint = if (isError) PxColors.Error else PxColors.OnSurfaceDim
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
                                tint = PxColors.OnSurfaceDim
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
                                tint = PxColors.OnSurfaceDim
                            )
                        }
                    }
                }
                else -> null
            },
            isError = isError,
            visualTransformation = visualTransformation,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onDone = { onDone?.invoke() },
                onNext = {}
            ),
            singleLine = maxLines == 1,
            maxLines = maxLines,
            enabled = enabled,
            readOnly = readOnly,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = PxColors.SurfaceVariant,
                unfocusedContainerColor = PxColors.SurfaceVariant,
                disabledContainerColor = PxColors.SurfaceVariant,
                focusedBorderColor = PxColors.Primary,
                unfocusedBorderColor = PxColors.Outline,
                errorBorderColor = PxColors.Error,
                focusedLabelColor = PxColors.Primary,
                unfocusedLabelColor = PxColors.OnSurfaceDim,
                errorLabelColor = PxColors.Error,
                focusedTextColor = PxColors.OnBackground,
                unfocusedTextColor = PxColors.OnBackground,
                cursorColor = PxColors.Primary,
                errorCursorColor = PxColors.Error
            )
        )

        // Error or supporting text below field
        val helperText = if (isError && errorMessage != null) errorMessage else supportingText
        if (helperText != null) {
            Text(
                text = helperText,
                style = MaterialTheme.typography.bodySmall,
                color = if (isError) PxColors.Error else PxColors.OnSurfaceDim,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F14)
@Composable
private fun PxTextFieldPreview() {
    ProductivityXTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            PxTextField(
                value = "test@email.com",
                onValueChange = {},
                label = "Email address",
                leadingIcon = Icons.Outlined.Visibility
            )
        }
    }
}