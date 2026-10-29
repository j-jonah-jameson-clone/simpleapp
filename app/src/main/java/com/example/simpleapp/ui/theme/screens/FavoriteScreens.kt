package com.example.simpleapp.ui.theme.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.simpleapp.R
import com.example.simpleapp.data.ImageItem
import com.example.simpleapp.ui.theme.SimpleappTheme
import com.example.simpleapp.ui.theme.screens.components.AppBottomBar
import com.example.simpleapp.ui.theme.screens.components.AppTopBar
import com.example.simpleapp.ui.theme.screens.components.FavoritesEmptyState
import com.example.simpleapp.ui.theme.screens.components.ImageGrid

@Composable
fun FavoritesScreen(
    favorites: List<ImageItem>,
    gridColumns: Int,
    onOpenImage: (ImageItem) -> Unit,
    onSettingsClick: () -> Unit,
    onRemoveFavorite: (ImageItem) -> Unit,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    var pendingRemoval by remember { mutableStateOf<ImageItem?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AppTopBar(
                title = stringResource(R.string.title_favorites),
                onSettingsClick = onSettingsClick
            )
        },
        bottomBar = {
            AppBottomBar(selectedTab = MainTab.FAVORITES, onTabSelected = onTabSelected)
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (favorites.isEmpty()) {
                FavoritesEmptyState()
            } else {
                ImageGrid(
                    images = favorites,
                    favoriteIds = favorites.map { it.id }.toSet(),
                    gridColumns = gridColumns,
                    onImageClick = onOpenImage,
                    onImageLongClick = { item -> pendingRemoval = item }
                )
            }
        }
    }

    val removalTarget = pendingRemoval
    if (removalTarget != null) {
        AlertDialog(
            onDismissRequest = { pendingRemoval = null },
            title = { Text(stringResource(R.string.favorite_remove_title)) },
            text = { Text(stringResource(R.string.favorite_remove_message, removalTarget.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingRemoval = null
                        onRemoveFavorite(removalTarget)
                    }
                ) {
                    Text(stringResource(R.string.action_remove_favorite))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingRemoval = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}


@Preview(name = "Избранное — пусто", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun FavoritesEmptyPreview() {
    SimpleappTheme {
        FavoritesScreen(
            favorites = emptyList(),
            gridColumns = 3,
            onOpenImage = {},
            onSettingsClick = {},
            onRemoveFavorite = {},
            onTabSelected = {}
        )
    }
}