package com.example.simpleapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.simpleapp.ui.theme.SimpleappTheme
import com.example.simpleapp.ui.theme.ThemeState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ThemeState.load(this)
        enableEdgeToEdge()

        setContent {
            SimpleappTheme {
                AppRoot()
            }
        }
    }
}