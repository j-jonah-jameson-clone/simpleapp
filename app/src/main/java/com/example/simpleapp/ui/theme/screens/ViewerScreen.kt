package com.example.simpleapp.ui.theme.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.simpleapp.R
import com.example.simpleapp.data.ImageDecoder
import com.example.simpleapp.data.ImageItem
import com.example.simpleapp.ui.theme.SimpleappTheme
import kotlin.math.max

private val ViewerBackground = Color(0xFF101012)

private const val MAX_SCALE = 5f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewerScreen(
    images: List<ImageItem>,
    startIndex: Int,
    favoriteIds: Set<Long>,
    onBack: () -> Unit,
    onToggleFavorite: (ImageItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(
        initialPage = startIndex.coerceIn(0, images.lastIndex)
    ) { images.size }

    val current = images[pagerState.currentPage]

    var isZoomed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ViewerBackground)
    ) {
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = !isZoomed,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            ViewerPage(
                image = images[page],
                isCurrent = page == pagerState.currentPage,
                onZoomChange = { isZoomed = it }
            )
        }

        TopAppBar(
            modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
            title = {
                Text(
                    text = current.name,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.action_back),
                        tint = Color.White
                    )
                }
            },
            actions = {
                val isFavorite = current.id in favoriteIds
                IconButton(onClick = { onToggleFavorite(current) }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = stringResource(
                            if (isFavorite) R.string.viewer_favorite_remove
                            else R.string.viewer_favorite_add
                        ),
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Black.copy(alpha = 0.4f)
            )
        )
    }
}

@Composable
private fun ViewerPage(
    image: ImageItem,
    isCurrent: Boolean,
    onZoomChange: (Boolean) -> Unit
) {
    val context = LocalContext.current

    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(scale, isCurrent) {
        if (isCurrent) onZoomChange(scale > 1f)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val targetPixels = with(LocalDensity.current) {
            max(maxWidth.toPx(), maxHeight.toPx()).toInt()
        }
        val bitmap by produceState<Bitmap?>(initialValue = null, image.id, targetPixels) {
            value = ImageDecoder.decodeScaled(context, image, targetPixels)
        }

        val shownBitmap = bitmap
        if (shownBitmap == null) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White
            )
        } else {
            Image(
                bitmap = shownBitmap.asImageBitmap(),
                contentDescription = image.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
                    .pointerInput(image.id) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, MAX_SCALE)
                            offset = if (scale > 1f) offset + pan else Offset.Zero
                        }
                    }
                    .pointerInput(image.id) {
                        detectTapGestures(
                            onDoubleTap = {
                                if (scale > 1f) {
                                    scale = 1f
                                    offset = Offset.Zero
                                } else {
                                    scale = 2f
                                }
                            }
                        )
                    }
            )
        }
    }
}

@Composable
fun ViewerLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ViewerBackground),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color.White)
    }
}


@Preview(name = "Просмотр изображения", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ViewerScreenPreview() {
    SimpleappTheme {
        ViewerScreen(
            images = listOf(
                ImageItem(
                    id = 1L,
                    uri = android.net.Uri.EMPTY,
                    name = "IMG_2024.jpg",
                    folderId = -2L,
                    folderPath = "Pictures",
                    sizeBytes = 2_400_000L,
                    dateMillis = 1_726_425_000_000L,
                    width = 4032,
                    height = 3024,
                    mimeType = "image/jpeg"
                )
            ),
            startIndex = 0,
            favoriteIds = emptySet(),
            onBack = {},
            onToggleFavorite = {}
        )
    }
}