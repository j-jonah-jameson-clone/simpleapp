package com.example.simpleapp.ui.theme.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.simpleapp.R
import com.example.simpleapp.data.GalleryRepository
import com.example.simpleapp.data.ImageFolder
import com.example.simpleapp.data.ImageItem
import com.example.simpleapp.data.ImageSortOrder
import com.example.simpleapp.ui.theme.SimpleappTheme
import com.example.simpleapp.ui.theme.screens.components.AppBottomBar
import com.example.simpleapp.ui.theme.screens.components.AppTopBar
import com.example.simpleapp.ui.theme.screens.components.GalleryEmptyState
import com.example.simpleapp.ui.theme.screens.components.ImageGrid

@Composable
fun GalleryScreen(
    images: List<ImageItem>,
    folders: List<ImageFolder>,
    favoriteIds: Set<Long>,
    gridColumns: Int,
    sortOrder: ImageSortOrder,
    selectedFolderId: Long,
    onSortOrderChange: (ImageSortOrder) -> Unit,
    onFolderChange: (Long) -> Unit,
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
            ) {
                SortMenuButton(current = sortOrder, onSelect = onSortOrderChange)
                FolderMenuButton(
                    folders = folders,
                    selectedFolderId = selectedFolderId,
                    onSelect = onFolderChange
                )
            }
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

@Composable
private fun SortMenuButton(
    current: ImageSortOrder,
    onSelect: (ImageSortOrder) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Sort,
                contentDescription = stringResource(R.string.menu_sort)
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ImageSortOrder.entries.forEach { order ->
                DropdownMenuItem(
                    text = { Text(stringResource(order.labelRes())) },
                    onClick = {
                        expanded = false
                        onSelect(order)
                    },
                    trailingIcon = {
                        if (order == current) {
                            Icon(Icons.Default.Check, contentDescription = null)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun FolderMenuButton(
    folders: List<ImageFolder>,
    selectedFolderId: Long,
    onSelect: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = stringResource(R.string.menu_folder)
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            folders.forEach { folder ->
                DropdownMenuItem(
                    text = { Text("${folder.name} (${folder.count})") },
                    onClick = {
                        expanded = false
                        onSelect(folder.id)
                    },
                    trailingIcon = {
                        if (folder.id == selectedFolderId) {
                            Icon(Icons.Default.Check, contentDescription = null)
                        }
                    }
                )
            }
        }
    }
}

private fun ImageSortOrder.labelRes(): Int = when (this) {
    ImageSortOrder.DATE_DESC -> R.string.sort_date_desc
    ImageSortOrder.DATE_ASC -> R.string.sort_date_asc
    ImageSortOrder.SIZE_DESC -> R.string.sort_size_desc
    ImageSortOrder.SIZE_ASC -> R.string.sort_size_asc
}


@Preview(name = "Галерея — пусто", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun GalleryEmptyPreview() {
    SimpleappTheme {
        GalleryScreen(
            images = emptyList(),
            folders = emptyList(),
            favoriteIds = emptySet(),
            gridColumns = 3,
            sortOrder = ImageSortOrder.DATE_DESC,
            selectedFolderId = GalleryRepository.ALL_FOLDERS_ID,
            onSortOrderChange = {},
            onFolderChange = {},
            onOpenImage = {},
            onSettingsClick = {},
            onTabSelected = {}
        )
    }
}