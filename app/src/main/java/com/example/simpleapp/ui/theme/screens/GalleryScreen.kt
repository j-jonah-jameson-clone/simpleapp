package com.example.simpleapp.ui.theme.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.simpleapp.R
import com.example.simpleapp.data.ImageItem
import com.example.simpleapp.ui.theme.SimpleappTheme
import com.example.simpleapp.ui.theme.screens.components.AppBottomBar
import com.example.simpleapp.ui.theme.screens.components.AppTopBar
import com.example.simpleapp.ui.theme.screens.components.GalleryEmptyState
import com.example.simpleapp.ui.theme.screens.components.ImageGrid

@Composable
fun GalleryScreen(
    images: List<ImageItem>,
    favoriteIds: Set<Long>,
    gridColumns: Int,
    onOpenImage: (ImageItem) -> Unit,
    onSettingsClick: () -> Unit,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AppTopBar(
                title = stringResource(R.string.title_gallery),
                onSettingsClick = onSettingsClick
            )
        },
        bottomBar = {
            AppBottomBar(selectedTab = MainTab.GALLERY, onTabSelected = onTabSelected)
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (images.isEmpty()) {
                GalleryEmptyState()
            } else {
                ImageGrid(
                    images = images,
                    favoriteIds = favoriteIds,
                    gridColumns = gridColumns,
                    onImageClick = onOpenImage,
                    onImageLongClick = {}
                )
            }
        }
    }
}


@Preview(name = "Галерея — пусто", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun GalleryEmptyPreview() {
    SimpleappTheme {
        GalleryScreen(
            images = emptyList(),
            favoriteIds = emptySet(),
            gridColumns = 3,
            onOpenImage = {},
            onSettingsClick = {},
            onTabSelected = {}
        )
    }
}