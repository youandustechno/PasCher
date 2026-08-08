package com.monasoftware.pascher.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.monasoftware.pascher.data.preferences.UserPreferencesRepository.DarkThemeConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit
) {
    val darkThemeConfig by viewModel.darkThemeConfig.collectAsState()
    val isExperimentalFeatureEnabled by viewModel.isExperimentalFeatureEnabled.collectAsState()
    val displayName by viewModel.displayName.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                windowInsets = TopAppBarDefaults.windowInsets
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Profile",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = displayName,
                onValueChange = viewModel::updateDisplayName,
                label = { Text("Display Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Text(
                text = "This name will be visible to others during co-watching sessions.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text(
                text = "Appearance",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Dark Theme",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            SettingsDialogThemeChooserRow(
                darkThemeConfig = darkThemeConfig,
                onConfigChange = viewModel::updateDarkThemeConfig
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text(
                text = "Features",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = isExperimentalFeatureEnabled,
                        onClick = { viewModel.toggleExperimentalFeature(!isExperimentalFeatureEnabled) },
                        role = Role.Switch
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Experimental Recommendations",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Enable AI-powered movie suggestions (Beta)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isExperimentalFeatureEnabled,
                    onCheckedChange = { viewModel.toggleExperimentalFeature(it) }
                )
            }
        }
    }
}

@Composable
fun SettingsDialogThemeChooserRow(
    darkThemeConfig: DarkThemeConfig,
    onConfigChange: (DarkThemeConfig) -> Unit
) {
    Column(Modifier.selectableGroup()) {
        SettingsDialogThemeChooserOption(
            text = "System Default",
            selected = darkThemeConfig == DarkThemeConfig.FOLLOW_SYSTEM,
            onClick = { onConfigChange(DarkThemeConfig.FOLLOW_SYSTEM) }
        )
        SettingsDialogThemeChooserOption(
            text = "Light",
            selected = darkThemeConfig == DarkThemeConfig.LIGHT,
            onClick = { onConfigChange(DarkThemeConfig.LIGHT) }
        )
        SettingsDialogThemeChooserOption(
            text = "Dark",
            selected = darkThemeConfig == DarkThemeConfig.DARK,
            onClick = { onConfigChange(DarkThemeConfig.DARK) }
        )
    }
}

@Composable
fun SettingsDialogThemeChooserOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        Spacer(Modifier.width(8.dp))
        Text(text)
    }
}
