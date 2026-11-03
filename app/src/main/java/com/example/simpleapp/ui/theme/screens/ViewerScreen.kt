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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import com.example.simpleapp.data.ExifReader
import com.example.simpleapp.data.ImageDetails
import com.example.simpleapp.data.formatAperture
import com.example.simpleapp.data.formatDateTime
import com.example.simpleapp.data.formatExposure
import com.example.simpleapp.data.formatFileSize
import com.example.simpleapp.data.formatFocalLength
import com.example.simpleapp.data.formatImageFormat
import com.example.simpleapp.data.formatIso
import com.example.simpleapp.data.formatResolution
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.filled.Share
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.ColorFilter
import com.example.simpleapp.data.PhotoFilter
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.rememberCoroutineScope
import com.example.simpleapp.data.FilteredImageSaver
import kotlinx.coroutines.launch

private val ViewerBackground = Color(0xFF101012)

private const val MAX_SCALE = 5f

private sealed interface SaveState {

    data object InProgress : SaveState

    data class Saved(val fileName: String) : SaveState

    data object Failed : SaveState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewerScreen(
    images: List<ImageItem>,
    startIndex: Int,
    favoriteIds: Set<Long>,
    onBack: () -> Unit,
    onToggleFavorite: (ImageItem) -> Unit,
    onShare: (ImageItem) -> Unit,
    onCopySaved: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(
        initialPage = startIndex.coerceIn(0, images.lastIndex)
    ) { images.size }

    val current = images[pagerState.currentPage]

    var showInfo by remember { mutableStateOf(false) }

    var details by remember(current.id) { mutableStateOf<ImageDetails?>(null) }

    val context = LocalContext.current

    var filter by rememberSaveable { mutableStateOf(PhotoFilter.ORIGINAL) }

    var showFilters by remember { mutableStateOf(false) }

    var saveState by remember { mutableStateOf<SaveState?>(null) }

    val scope = rememberCoroutineScope()

    val albumName = stringResource(R.string.filter_album_name)

    LaunchedEffect(filter, current.id) { saveState = null }

    LaunchedEffect(showInfo, current.id) {
        if (showInfo && details == null) {
            details = ExifReader.loadDetails(context, current)
        }
    }

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
                filter = filter,
                onZoomChange = { isZoomed = it }
            )
        }
        if (showInfo) {
            ImageInfoDialog(
                image = current,
                details = details,
                onDismiss = { showInfo = false }
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
                IconButton(onClick = { showFilters = !showFilters }) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = stringResource(R.string.action_filters),
                        tint = Color.White
                    )
                }
                IconButton(onClick = { onShare(current) }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = stringResource(R.string.share_chooser_title),
                        tint = Color.White
                    )
                }
                IconButton(onClick = { showInfo = true }) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = stringResource(R.string.action_info),
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Black.copy(alpha = 0.4f)
            )
        )
        if (showFilters) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                if (filter.hasEffect) {
                    SaveFilteredBar(
                        state = saveState,
                        onSave = {
                            saveState = SaveState.InProgress
                            scope.launch {
                                val saved = FilteredImageSaver.saveCopy(
                                    context = context,
                                    source = current,
                                    filter = filter,
                                    albumName = albumName
                                )
                                saveState = if (saved == null) {
                                    SaveState.Failed
                                } else {
                                    onCopySaved()
                                    SaveState.Saved(saved)
                                }
                            }
                        }
                    )
                }
                FilterRow(selected = filter, onSelect = { filter = it })
            }
        }
    }
}

@Composable
private fun ViewerPage(
    image: ImageItem,
    isCurrent: Boolean,
    filter: PhotoFilter,
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
                colorFilter = filter.colorMatrix?.let { ColorFilter.colorMatrix(it) },
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

@Composable
private fun ImageInfoDialog(
    image: ImageItem,
    details: ImageDetails?,
    onDismiss: () -> Unit
) {
    val unknown = stringResource(R.string.info_unknown)
    val camera = details?.let { listOfNotNull(it.cameraMake, it.cameraModel).joinToString(" ") }
        ?.takeIf { it.isNotBlank() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.info_title)) },
        text = {
            if (details == null) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    InfoRow(stringResource(R.string.info_name), details.name)
                    InfoRow(stringResource(R.string.info_date), formatDateTime(details.dateMillis))
                    InfoRow(
                        stringResource(R.string.info_resolution),
                        formatResolution(details.width, details.height) ?: unknown
                    )
                    InfoRow(stringResource(R.string.info_size), formatFileSize(details.sizeBytes))
                    InfoRow(
                        stringResource(R.string.info_format),
                        formatImageFormat(details.mimeType) ?: unknown
                    )
                    InfoRow(stringResource(R.string.info_folder), details.folder)
                    InfoRow(stringResource(R.string.info_camera), camera ?: unknown)
                    InfoRow(
                        stringResource(R.string.info_iso),
                        formatIso(details.iso) ?: unknown
                    )
                    InfoRow(
                        stringResource(R.string.info_exposure),
                        formatExposure(details.exposureSeconds) ?: unknown
                    )
                    InfoRow(
                        stringResource(R.string.info_aperture),
                        formatAperture(details.aperture) ?: unknown
                    )
                    InfoRow(
                        stringResource(R.string.info_focal_length),
                        formatFocalLength(details.focalLengthMillimeters) ?: unknown
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close))
            }
        }
    )
}

@Composable
private fun FilterRow(
    selected: PhotoFilter,
    onSelect: (PhotoFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.4f))
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items = PhotoFilter.entries, key = { it.name }) { item ->
            FilterChip(
                selected = item == selected,
                onClick = { onSelect(item) },
                label = { Text(stringResource(item.titleRes)) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.White.copy(alpha = 0.15f),
                    labelColor = Color.White,
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

@Composable
private fun SaveFilteredBar(
    state: SaveState?,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val saving = state is SaveState.InProgress

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (state) {
            is SaveState.Saved -> SaveMessage(
                text = stringResource(R.string.save_success, state.fileName),
                container = MaterialTheme.colorScheme.primary
            )

            SaveState.Failed -> SaveMessage(
                text = stringResource(R.string.save_error),
                container = MaterialTheme.colorScheme.error
            )

            else -> Unit
        }

        FilledTonalButton(onClick = onSave, enabled = !saving) {
            if (saving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.action_save_filtered))
        }
    }
}

@Composable
private fun SaveMessage(text: String, container: Color) {
    Surface(color = container, shape = MaterialTheme.shapes.small) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
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
            onToggleFavorite = {},
            onShare = {}
        )
    }
}