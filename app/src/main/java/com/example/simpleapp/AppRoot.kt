package com.example.simpleapp

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.simpleapp.ui.theme.screens.ApplicationMainScreen


@Composable
fun AppRoot(modifier: Modifier = Modifier) {
    ApplicationMainScreen(modifier = modifier)
}