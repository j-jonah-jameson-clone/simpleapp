package com.example.simpleapp.ui.theme.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.simpleapp.R
import com.example.simpleapp.data.UserFolder
import com.example.simpleapp.data.UserFoldersStore
import com.example.simpleapp.ui.theme.SimpleappTheme
import com.example.simpleapp.ui.theme.screens.components.EmptyState
import com.example.simpleapp.ui.theme.screens.components.FolderNameDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserFoldersScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val store = remember { UserFoldersStore(context) }

    var folders by remember { mutableStateOf(store.loadFolders()) }
    var creating by remember { mutableStateOf(false) }
    var renaming by remember { mutableStateOf<UserFolder?>(null) }
    var deleting by remember { mutableStateOf<UserFolder?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_user_folders)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { creating = true },
                icon = { Icon(Icons.Default.CreateNewFolder, contentDescription = null) },
                text = { Text(stringResource(R.string.action_create_folder)) }
            )
        }
    ) { padding ->
        if (folders.isEmpty()) {
            EmptyState(
                icon = Icons.Default.FolderOpen,
                title = stringResource(R.string.empty_user_folders),
                hint = stringResource(R.string.empty_user_folders_hint),
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding() + 88.dp
                ),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(items = folders, key = { it.id }) { folder ->
                    FolderRow(
                        folder = folder,
                        onRename = { renaming = folder },
                        onDelete = { deleting = folder }
                    )
                }
            }
        }
    }

    if (creating) {
        FolderNameDialog(
            titleRes = R.string.folder_create_title,
            confirmRes = R.string.action_create_folder,
            onDismiss = { creating = false },
            onConfirm = { name ->
                store.createFolder(name)
                folders = store.loadFolders()
                creating = false
            }
        )
    }

    renaming?.let { folder ->
        FolderNameDialog(
            titleRes = R.string.folder_rename_title,
            confirmRes = R.string.action_rename_folder,
            initialName = folder.name,
            onDismiss = { renaming = null },
            onConfirm = { name ->
                store.renameFolder(folder.id, name)
                folders = store.loadFolders()
                renaming = null
            }
        )
    }

    deleting?.let { folder ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text(stringResource(R.string.folder_delete_title)) },
            text = {
                Text(stringResource(R.string.folder_delete_message, folder.name))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        store.deleteFolder(folder.id)
                        folders = store.loadFolders()
                        deleting = null
                    }
                ) {
                    Text(stringResource(R.string.action_delete_folder))
                }
            },
            dismissButton = {
                TextButton(onClick = { deleting = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun FolderRow(
    folder: UserFolder,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuVisible by remember { mutableStateOf(false) }

    ListItem(
        modifier = modifier.clickable { menuVisible = true },
        headlineContent = { Text(folder.name) },
        supportingContent = {
            Text(pluralCount(folder.count))
        },
        leadingContent = {
            Icon(Icons.Default.Folder, contentDescription = null)
        },
        trailingContent = {
            Box {
                IconButton(onClick = { menuVisible = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.action_folder_menu)
                    )
                }
                DropdownMenu(
                    expanded = menuVisible,
                    onDismissRequest = { menuVisible = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_rename_folder)) },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            menuVisible = false
                            onRename()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_delete_folder)) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                        onClick = {
                            menuVisible = false
                            onDelete()
                        }
                    )
                }
            }
        }
    )
}

@Composable
private fun pluralCount(count: Int): String =
    pluralStringResource(R.plurals.folder_images_count, count, count)

@Preview(name = "Мои папки", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun UserFoldersScreenPreview() {
    SimpleappTheme {
        UserFoldersScreen(onBack = {})
    }
}