package com.example.simpleapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.simpleapp.ui.theme.SimpleappTheme
import com.example.simpleapp.ui.theme.screens.SettingsScreen

class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.Transparent.toArgb(),
                Color.Transparent.toArgb()
            ),
            navigationBarStyle = SystemBarStyle.light(
                Color.Transparent.toArgb(),
                Color.Transparent.toArgb()
            )
        )
        super.onCreate(savedInstanceState)

        setContent {
            SimpleappTheme {
                SettingsScreen(onBack = { finish() })
            }
        }
    }

    companion object {
        fun createIntent(context: Context): Intent =
            Intent(context, SettingsActivity::class.java)
    }
}