package com.example.simpleapp.ui.theme.screens.components

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import com.example.simpleapp.R
import com.example.simpleapp.data.UserFoldersStore

@Composable
fun FolderPickerDialog(
    imageIds: List<Long>,
    @StringRes titleRes: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val store = remember { UserFoldersStore(context) }
    var folders by remember { mutableStateOf(store.loadFolders()) }
    var creating by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = { Text(stringResource(titleRes)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (folders.isEmpty()) {
                    Text(
                        text = stringResource(R.string.folders_dialog_empty),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                folders.forEach { folder ->
                    val inside = imageIds.count { imageId -> folder.contains(imageId) }
                    val state = when {
                        inside == 0 -> ToggleableState.Off
                        inside == imageIds.size -> ToggleableState.On
                        else -> ToggleableState.Indeterminate
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (state == ToggleableState.On) {
                                    imageIds.forEach { imageId ->
                                        store.removeImage(folder.id, imageId)
                                    }
                                } else {
                                    imageIds.forEach { imageId ->
                                        store.addImage(folder.id, imageId)
                                    }
                                }
                                folders = store.loadFolders()
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TriStateCheckbox(state = state, onClick = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = folder.name,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = pluralStringResource(
                                R.plurals.folder_images_count,
                                folder.count,
                                folder.count
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { creating = true }) {
                Text(stringResource(R.string.action_create_folder))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close))
            }
        }
    )

    if (creating) {
        FolderNameDialog(
            titleRes = R.string.folder_create_title,
            confirmRes = R.string.action_create_folder,
            onDismiss = { creating = false },
            onConfirm = { name ->
                store.createFolder(name)?.let { created ->
                    imageIds.forEach { imageId ->
                        store.addImage(created.id, imageId)
                    }
                }
                folders = store.loadFolders()
                creating = false
            }
        )
    }
}