package com.example.simpleapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.simpleapp.data.GalleryRepository
import com.example.simpleapp.data.ImageDecoder
import com.example.simpleapp.data.ImageItem
import com.example.simpleapp.data.SettingsStore
import com.example.simpleapp.ui.theme.SimpleappTheme
import com.example.simpleapp.ui.theme.screens.ViewerLoading
import com.example.simpleapp.ui.theme.screens.ViewerScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ViewerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.Transparent.toArgb()),
            navigationBarStyle = SystemBarStyle.dark(Color.Transparent.toArgb())
        )
        super.onCreate(savedInstanceState)

        val imageIds = intent.getLongArrayExtra(EXTRA_IMAGE_IDS)?.toList()
        val startId = intent.getLongExtra(EXTRA_START_ID, NO_ID)
        if (imageIds.isNullOrEmpty() || startId == NO_ID) {
            finish()
            return
        }

        val preferences = SettingsStore(this)

        setContent {
            SimpleappTheme {
                var images by remember { mutableStateOf<List<ImageItem>?>(null) }
                var favoriteIds by remember { mutableStateOf(preferences.favoriteIds) }

                LaunchedEffect(imageIds) {
                    val loaded = withContext(Dispatchers.IO) {
                        val byId = GalleryRepository.loadImages(this@ViewerActivity)
                            .associateBy { it.id }
                        imageIds.mapNotNull { byId[it] }
                    }
                    if (loaded.isEmpty()) finish() else images = loaded
                }

                val shownImages = images
                if (shownImages == null) {
                    ViewerLoading()
                } else {
                    ViewerScreen(
                        images = shownImages,
                        startIndex = shownImages
                            .indexOfFirst { it.id == startId }
                            .coerceAtLeast(0),
                        favoriteIds = favoriteIds,
                        onBack = { finish() },
                        onToggleFavorite = { image ->
                            preferences.toggleFavorite(image.id)
                            favoriteIds = preferences.favoriteIds
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ImageDecoder.clearCache()
    }

    companion object {
        private const val EXTRA_IMAGE_IDS = "extra_image_ids"
        private const val EXTRA_START_ID = "extra_start_id"

        private const val NO_ID = -1L

        fun createIntent(context: Context, images: List<ImageItem>, startId: Long): Intent =
            Intent(context, ViewerActivity::class.java).apply {
                putExtra(EXTRA_IMAGE_IDS, images.map { it.id }.toLongArray())
                putExtra(EXTRA_START_ID, startId)
            }
    }
}