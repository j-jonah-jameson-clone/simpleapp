package com.example.simpleapp.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.simpleapp.data.SettingsStore
import com.example.simpleapp.data.ThemeMode

object ThemeState {

    var mode by mutableStateOf(ThemeMode.DEFAULT)
        private set

    fun load(context: Context) {
        mode = SettingsStore(context).themeMode
    }

    fun update(context: Context, newMode: ThemeMode) {
        SettingsStore(context).themeMode = newMode
        mode = newMode
    }
}