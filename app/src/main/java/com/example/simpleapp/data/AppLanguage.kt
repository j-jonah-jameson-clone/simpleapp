package com.example.simpleapp.data

enum class AppLanguage(val storageValue: String, val languageTag: String) {

    RUSSIAN("ru", "ru"),
    ENGLISH("en", "en");

    companion object {
        val DEFAULT = RUSSIAN

        fun fromStorage(value: String?): AppLanguage =
            entries.firstOrNull { it.storageValue == value } ?: DEFAULT
    }
}