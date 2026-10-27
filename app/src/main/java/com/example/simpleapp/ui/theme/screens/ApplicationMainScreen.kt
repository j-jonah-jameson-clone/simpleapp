package com.example.simpleapp.ui.theme.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import com.example.simpleapp.SettingsActivity
import com.example.simpleapp.ui.theme.screens.components.AppTopBar

@Composable
fun ApplicationMainScreen(modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableStateOf(MainTab.GALLERY) }
    val context = LocalContext.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AppTopBar(
                title = stringResource(selectedTab.titleRes),
                onSettingsClick = { context.startActivity(SettingsActivity.createIntent(context)) }
            )
        },
        bottomBar = {
            NavigationBar {
                MainTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = tab == selectedTab,
                        onClick = { selectedTab = tab },
                        icon = { },
                        label = { Text(text = stringResource(tab.titleRes)) }
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Здесь появится ${stringResource(selectedTab.titleRes).lowercase()}")
        }
    }
}