package com.example.simpleapp.ui.theme.screens

import android.app.Activity
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.simpleapp.R
import com.example.simpleapp.data.GalleryRepository
import com.example.simpleapp.data.ImageDecoder
import com.example.simpleapp.data.ImageFolder
import com.example.simpleapp.data.ImageItem
import com.example.simpleapp.data.ImageSortOrder
import com.example.simpleapp.ui.theme.SimpleappTheme
import com.example.simpleapp.ui.theme.screens.components.AppBottomBar
import com.example.simpleapp.ui.theme.screens.components.AppTopBar
import com.example.simpleapp.ui.theme.screens.components.GalleryEmptyState
import com.example.simpleapp.ui.theme.screens.components.ImageGrid
import com.example.simpleapp.ui.theme.screens.components.SelectionTopBar
import com.example.simpleapp.ui.theme.screens.components.rememberSelectionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
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
    onImagesDeleted: (List<Long>) -> Unit,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val selection = rememberSelectionState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDeleteError by remember { mutableStateOf(false) }

    var pendingDeleteIds by remember { mutableStateOf<List<Long>>(emptyList()) }

    val deleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            ImageDecoder.clearCache()
            selection.clear()
            onImagesDeleted(pendingDeleteIds)
        }
        pendingDeleteIds = emptyList()
    }

    fun requestDelete(items: List<ImageItem>) {
        val uris: List<Uri> = items.map { it.uri }
        pendingDeleteIds = items.map { it.id }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val request = MediaStore.createDeleteRequest(context.contentResolver, uris)
                deleteLauncher.launch(
                    IntentSenderRequest.Builder(request.intentSender).build()
                )
            } catch (error: Exception) {
                pendingDeleteIds = emptyList()
                showDeleteError = true
            }
        } else {
            scope.launch {
                val succeeded = withContext(Dispatchers.IO) {
                    GalleryRepository.deleteImages(context, uris)
                }
                if (succeeded) {
                    ImageDecoder.clearCache()
                    selection.clear()
                    onImagesDeleted(pendingDeleteIds)
                } else {
                    showDeleteError = true
                }
                pendingDeleteIds = emptyList()
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            if (selection.isActive) {
                SelectionTopBar(
                    selectedCount = selection.count,
                    onBack = { selection.clear() },
                    onSelectAll = { selection.selectAll(images.map { it.id }) },
                    onDeleteClick = { showDeleteDialog = true }
                )
            } else {
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
                    selectedIds = selection.ids,
                    onImageClick = { item ->
                        if (selection.isActive) selection.toggle(item.id) else onOpenImage(item)
                    },
                    onImageLongClick = { item -> selection.startWith(item.id) }
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_confirm_title)) },
            text = {
                Text(
                    pluralStringResource(
                        R.plurals.delete_confirm_message,
                        selection.count,
                        selection.count
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        requestDelete(images.filter { it.id in selection.ids })
                    }
                ) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    if (showDeleteError) {
        AlertDialog(
            onDismissRequest = { showDeleteError = false },
            title = { Text(stringResource(R.string.delete_error_title)) },
            text = { Text(stringResource(R.string.delete_error)) },
            confirmButton = {
                TextButton(onClick = { showDeleteError = false }) {
                    Text(stringResource(R.string.action_close))
                }
            }
        )
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
            onImagesDeleted = {},
            onTabSelected = {}
        )
    }
}