package com.example.simpleapp.ui.theme.screens.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.simpleapp.data.ImageItem

private val GRID_PADDING = 4.dp

@Composable
fun ImageGrid(
    images: List<ImageItem>,
    favoriteIds: Set<Long>,
    gridColumns: Int,
    onImageClick: (ImageItem) -> Unit,
    onImageLongClick: (ImageItem) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(GRID_PADDING)
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(gridColumns),
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(GRID_PADDING),
        verticalArrangement = Arrangement.spacedBy(GRID_PADDING)
    ) {
        items(items = images, key = { it.id }) { item ->
            ImageThumbnail(
                item = item,
                modifier = Modifier,
                isFavorite = item.id in favoriteIds,
                onClick = { onImageClick(item) },
                onLongClick = { onImageLongClick(item) }
            )
        }
    }
}