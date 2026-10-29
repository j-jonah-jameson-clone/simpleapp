package com.example.simpleapp.ui.theme.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.simpleapp.R
import com.example.simpleapp.data.SettingsStore
import com.example.simpleapp.ui.theme.SimpleappTheme
import com.example.simpleapp.ui.theme.screens.components.SectionSpacer
import com.example.simpleapp.ui.theme.screens.components.SectionTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val preferences = remember { SettingsStore(context) }

    var gridColumns by remember { mutableIntStateOf(preferences.gridColumns) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SectionTitle(stringResource(R.string.settings_theme))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = true,
                    onClick = {},
                    label = { Text(stringResource(R.string.theme_light)) }
                )
                FilterChip(
                    selected = false,
                    enabled = false,
                    onClick = {},
                    label = { Text(stringResource(R.string.theme_dark)) }
                )
                FilterChip(
                    selected = false,
                    enabled = false,
                    onClick = {},
                    label = { Text(stringResource(R.string.theme_system)) }
                )
            }
            Text(
                text = stringResource(R.string.settings_fixed_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            SectionSpacer()

            SectionTitle(stringResource(R.string.settings_language))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = true,
                    onClick = {},
                    label = { Text(stringResource(R.string.language_ru)) }
                )
                FilterChip(
                    selected = false,
                    enabled = false,
                    onClick = {},
                    label = { Text(stringResource(R.string.language_en)) }
                )
            }
            Text(
                text = stringResource(R.string.settings_fixed_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            SectionSpacer()

            SectionTitle(stringResource(R.string.settings_grid))
            Text(
                text = pluralStringResource(
                    R.plurals.grid_columns_value,
                    gridColumns,
                    gridColumns
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (SettingsStore.MIN_GRID_COLUMNS..SettingsStore.MAX_GRID_COLUMNS).forEach { columns ->
                    FilterChip(
                        selected = columns == gridColumns,
                        onClick = {
                            gridColumns = columns
                            preferences.gridColumns = columns
                        },
                        label = { Text("$columns") }
                    )
                }
            }

            SectionSpacer()

            SectionTitle(stringResource(R.string.settings_about))
            ListItem(
                headlineContent = { Text(stringResource(R.string.about_version)) },
                supportingContent = { Text(stringResource(R.string.about_version_value)) }
            )
            HorizontalDivider()
            ListItem(
                headlineContent = { Text(stringResource(R.string.about_developer)) },
                supportingContent = { Text(stringResource(R.string.about_developer_name)) }
            )
        }
    }
}


@Preview(name = "Настройки", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun SettingsScreenPreview() {
    SimpleappTheme {
        SettingsScreen(onBack = {})
    }
}