package com.example.simpleapp.ui.theme.screens

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.simpleapp.R

enum class MainTab(@param:StringRes val titleRes: Int, val icon: ImageVector) {
    GALLERY(R.string.title_gallery, Icons.Default.PhotoLibrary),
    FAVORITES(R.string.title_favorites, Icons.Default.Favorite)
}