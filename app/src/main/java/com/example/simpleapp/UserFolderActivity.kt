package com.example.simpleapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.simpleapp.ui.theme.SimpleappTheme
import com.example.simpleapp.ui.theme.ThemeState
import com.example.simpleapp.ui.theme.screens.UserFoldersScreen

class UserFoldersActivity : LocaleAwareActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeState.load(this)
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
                UserFoldersScreen(onBack = { finish() })
            }
        }
    }

    companion object {
        fun createIntent(context: Context): Intent =
            Intent(context, UserFoldersActivity::class.java)
    }
}