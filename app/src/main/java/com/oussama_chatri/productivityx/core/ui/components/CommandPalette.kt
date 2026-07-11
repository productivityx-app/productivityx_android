package com.oussama_chatri.productivityx.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.NoteAdd
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.oussama_chatri.productivityx.R
import com.oussama_chatri.productivityx.core.ui.theme.PxColors

data class CommandItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val keywords: List<String> = emptyList(),
    val onClick: () -> Unit,
)

fun defaultCommands(onAction: (String) -> Unit) = listOf(
    CommandItem("create_note", "Create Note", Icons.Outlined.NoteAdd, listOf("note", "new"), { onAction("create_note") }),
    CommandItem("add_task", "Add Task", Icons.Outlined.Add, listOf("task", "todo"), { onAction("add_task") }),
    CommandItem("start_pomodoro", "Start Pomodoro", Icons.Outlined.Timer, listOf("timer", "focus"), { onAction("start_pomodoro") }),
    CommandItem("add_event", "Add Event", Icons.Outlined.Schedule, listOf("event", "calendar"), { onAction("add_event") }),
    CommandItem("open_settings", "Go to Settings", Icons.Outlined.Settings, listOf("settings", "prefs"), { onAction("open_settings") }),
    CommandItem("search", "Search Everything", Icons.Outlined.Search, listOf("search", "find"), { onAction("search") }),
)

@Composable
fun CommandPalette(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    commands: List<CommandItem> = defaultCommands {},
    onCommand: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val updatedCommands = remember(commands) {
        commands.map { it.copy(onClick = { onCommand(it.id); onDismiss() }) }
    }
    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isOpen) {
        if (isOpen) {
            query = ""
            focusRequester.requestFocus()
        }
    }

    AnimatedVisibility(
        visible = isOpen,
        enter = fadeIn(spring()) + slideInVertically(spring()) { -it / 3 },
        exit = fadeOut(spring()) + slideOutVertically(spring()) { -it / 3 },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.TopCenter,
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(top = 80.dp)
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(16.dp),
                color = PxColors.Surface,
                tonalElevation = 8.dp,
                shadowElevation = 16.dp,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        placeholder = { Text("Type a command...", color = PxColors.OnSurfaceDim) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Search, contentDescription = null, tint = PxColors.OnSurfaceDim)
                        },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                Icon(
                                    Icons.Outlined.Close,
                                    contentDescription = stringResource(R.string.cd_clear_input),
                                    tint = PxColors.OnSurfaceDim,
                                    modifier = Modifier.clickable { query = "" },
                                )
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PxColors.OnBackground,
                            unfocusedTextColor = PxColors.OnBackground,
                            cursorColor = PxColors.Primary,
                            focusedBorderColor = PxColors.Primary,
                            unfocusedBorderColor = PxColors.SurfaceVariant,
                            focusedContainerColor = PxColors.Background,
                            unfocusedContainerColor = PxColors.Background,
                        ),
                    )

                    Spacer(Modifier.height(12.dp))

                    val filtered = remember(query, updatedCommands) {
                        if (query.isBlank()) updatedCommands
                        else {
                            val q = query.lowercase()
                            updatedCommands.filter {
                                it.label.lowercase().contains(q) ||
                                        it.keywords.any { kw -> kw.lowercase().contains(q) }
                            }
                        }
                    }

                    Text(
                        text = if (query.isBlank()) "Recent" else "Commands",
                        style = MaterialTheme.typography.labelSmall,
                        color = PxColors.OnSurfaceDim,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Spacer(Modifier.height(4.dp))

                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(filtered) { cmd ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { cmd.onClick() }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    cmd.icon,
                                    contentDescription = null,
                                    tint = PxColors.Primary,
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = cmd.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = PxColors.OnBackground,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

