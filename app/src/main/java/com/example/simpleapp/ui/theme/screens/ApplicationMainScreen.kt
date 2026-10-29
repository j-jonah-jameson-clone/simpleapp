package com.example.simpleapp.ui.theme.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.simpleapp.SettingsActivity
import com.example.simpleapp.ViewerActivity
import com.example.simpleapp.data.GalleryRepository
import com.example.simpleapp.data.ImageItem
import com.example.simpleapp.data.MediaPermissions
import com.example.simpleapp.ui.theme.screens.components.AppBottomBar
import com.example.simpleapp.ui.theme.screens.components.AppTopBar
import com.example.simpleapp.ui.theme.screens.components.FavoritesEmptyState
import com.example.simpleapp.ui.theme.screens.components.LoadingState
import com.example.simpleapp.ui.theme.screens.components.PermissionRequiredState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.simpleapp.data.SettingsStore

@Composable
fun ApplicationMainScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val preferences = remember { SettingsStore(context) }

    var selectedTab by rememberSaveable { mutableStateOf(MainTab.GALLERY) }
    var gridColumns by remember { mutableIntStateOf(preferences.gridColumns) }
    var favoriteIds by remember { mutableStateOf(preferences.favoriteIds) }

    var images by remember { mutableStateOf<List<ImageItem>?>(null) }
    var hasPermission by remember { mutableStateOf(MediaPermissions.isGranted(context)) }
    var reloadKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(hasPermission, reloadKey) {
        if (!hasPermission) {
            images = emptyList()
            return@LaunchedEffect
        }
        val loaded = withContext(Dispatchers.IO) { GalleryRepository.loadImages(context) }
        images = loaded
    }

    val favoriteImages = remember(images, favoriteIds) {
        images?.let { GalleryRepository.filterFavorites(it, favoriteIds) }.orEmpty()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        hasPermission = result.values.any { granted -> granted } ||
                MediaPermissions.isGranted(context)
    }

    val settingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        gridColumns = preferences.gridColumns
    }

    val openSettings: () -> Unit = {
        settingsLauncher.launch(SettingsActivity.createIntent(context))
    }

    val openViewer: () -> Unit = {
        context.startActivity(ViewerActivity.createIntent(context))
    }

    val openAppSettings: () -> Unit = {
        context.startActivity(
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", context.packageName, null)
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    if (!hasPermission) {
        MainStateScaffold(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            onSettingsClick = openSettings,
            modifier = modifier
        ) {
            PermissionRequiredState(
                onRequestClick = { permissionLauncher.launch(MediaPermissions.required) },
                onOpenSettingsClick = openAppSettings
            )
        }
        return
    }

    if (images == null) {
        MainStateScaffold(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            onSettingsClick = openSettings,
            modifier = modifier
        ) {
            LoadingState()
        }
        return
    }

    when (selectedTab) {
        MainTab.GALLERY -> GalleryScreen(
            images = images.orEmpty(),
            favoriteIds = favoriteIds,
            gridColumns = gridColumns,
            onOpenImage = { openViewer() },
            onSettingsClick = openSettings,
            onTabSelected = { selectedTab = it },
            modifier = modifier
        )

        MainTab.FAVORITES -> FavoritesScreen(
            favorites = favoriteImages,
            gridColumns = gridColumns,
            onOpenImage = { openViewer() },
            onSettingsClick = openSettings,
            onRemoveFavorite = { item ->
                preferences.toggleFavorite(item.id)
                favoriteIds = preferences.favoriteIds
            },
            onTabSelected = { selectedTab = it },
            modifier = modifier
        )
    }
}

@Composable
private fun MainStateScaffold(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AppTopBar(
                title = stringResource(selectedTab.titleRes),
                onSettingsClick = onSettingsClick
            )
        },
        bottomBar = {
            AppBottomBar(selectedTab = selectedTab, onTabSelected = onTabSelected)
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            content()
        }
    }
}