package dev.ajithgoveas.khatape.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.ajithgoveas.khatape.ui.components.KhataPeAppTopBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val currentTheme by viewModel.themeState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Logic for clearing data
    val onClearData = {
        viewModel.clearAllData { success ->
            coroutineScope.launch {
                snackBarHostState.showSnackbar(
                    if (success) "Database cleared 🧹" else "Failed to clear data"
                )
            }
        }
    }

    if (showDeleteDialog) {
        DeleteSafetyDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                onClearData()
            }
        )
    }

    Scaffold(
        topBar = {
            KhataPeAppTopBar(
                title = "Settings",
                subtitle = "Preferences & App Customization",
                emoji = "⚙️"
            )
        },
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            /*
            item { SettingSectionTitle("Appearance") }
            item {
                SettingsCard(
                    title = "App Theme",
                    description = "Current: ${currentTheme.name.lowercase().capitalize()}",
                    icon = Icons.Outlined.ColorLens,
                    content = {
                        ThemeDropDown(
                            currentTheme = currentTheme,
                            onThemeSelected = { viewModel.setTheme(it) }
                        )
                    }
                )
            }
            */

            item { SettingSectionTitle("Account & Data") }
            item {
                SettingsCard(
                    title = "Wipe All Data",
                    description = "Permanent deletion of all records",
                    icon = Icons.Outlined.DeleteForever,
                    color = MaterialTheme.colorScheme.error,
                    onClick = { showDeleteDialog = true }
                )
            }

            item { SettingSectionTitle("About") }
            item {
                SettingsCard(
                    title = "Credits",
                    description = "Developed by Ajith Goveas\nInspired by modern M3 design.",
                    icon = Icons.Outlined.Info
                )
            }

            item { AppSignature() }
        }
    }
}

@Composable
private fun SettingSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun SettingsCard(
    title: String,
    description: String,
    icon: ImageVector,
    color: Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null,
    content: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        // If onClick is null, we disable the click ripple entirely for a better feel
        onClick = onClick ?: {},
        enabled = onClick != null
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Leading Icon
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )

            // 2. Text Column (The "Spacer")
            // Weight(1f) fills available space, pushing 'content' to the far right
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = color,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp // Improves readability for longer descriptions
                )
            }

            // 3. Trailing Content (Dropdown, Switch, etc.)
            // Only adds padding if content is actually present
            content?.let {
                Box(modifier = Modifier.padding(start = 8.dp)) {
                    it()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeDropDown(
    currentTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Box {
        Card(
            onClick = { isExpanded = true },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = when (currentTheme) {
                        AppTheme.LIGHT -> Icons.Default.LightMode
                        AppTheme.DARK -> Icons.Default.DarkMode
                        AppTheme.SYSTEM -> Icons.Default.SettingsSuggest
                    },
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = currentTheme.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelLarge
                )

                // FIXED: Call the composable directly here
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
            }
        }

        // Rounded Aesthetic Menu
        MaterialTheme(
            shapes = MaterialTheme.shapes.copy(
                extraSmall = RoundedCornerShape(16.dp)
            )
        ) {
            DropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest)
            ) {
                AppTheme.entries.forEach { theme ->
                    DropdownMenuItem(
                        text = { Text(theme.name.lowercase().capitalize()) },
                        leadingIcon = {
                            Icon(
                                imageVector = when (theme) {
                                    AppTheme.LIGHT -> Icons.Default.LightMode
                                    AppTheme.DARK -> Icons.Default.DarkMode
                                    AppTheme.SYSTEM -> Icons.Default.SettingsSuggest
                                },
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        onClick = {
                            onThemeSelected(theme)
                            isExpanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AppSignature() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "KhataPe v1.0.0",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Made with ❤️ in Mangaluru",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun String.capitalize() = this.replaceFirstChar { it.uppercase() }

@Composable
fun DeleteSafetyDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Wipe Database?") },
        text = { Text("This will delete all Khatas. This action is irreversible.") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Confirm Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(28.dp)
    )
}