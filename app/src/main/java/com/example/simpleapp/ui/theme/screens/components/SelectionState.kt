package com.example.simpleapp.ui.theme.screens.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Stable
class SelectionState {

    var ids: Set<Long> by mutableStateOf<Set<Long>>(emptySet())
        private set

    val isActive: Boolean get() = ids.isNotEmpty()

    val count: Int get() = ids.size

    fun toggle(imageId: Long) {
        ids = if (imageId in ids) ids - imageId else ids + imageId
    }

    fun startWith(imageId: Long) {
        ids = ids + imageId
    }

    fun selectAll(imageIds: Collection<Long>) {
        ids = imageIds.toSet()
    }

    fun clear() {
        ids = emptySet()
    }
}

@Composable
fun rememberSelectionState(): SelectionState = remember { SelectionState() }