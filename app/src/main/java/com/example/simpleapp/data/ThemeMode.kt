package com.example.simpleapp.data

enum class ThemeMode(val storageValue: String) {

    LIGHT("light"),
    DARK("dark"),
    SYSTEM("system");

    fun resolve(systemInDarkTheme: Boolean): Boolean = when (this) {
        LIGHT -> false
        DARK -> true
        SYSTEM -> systemInDarkTheme
    }

    companion object {
        val DEFAULT = SYSTEM

        fun fromStorage(value: String?): ThemeMode =
            entries.firstOrNull { it.storageValue == value } ?: DEFAULT
    }
}