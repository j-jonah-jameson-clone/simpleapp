package com.example.simpleapp.data

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object AppLocale {

    fun storedTag(base: Context): String = SettingsStore(base).language.languageTag

    fun wrap(base: Context, languageTag: String): Context {
        val locale = Locale.forLanguageTag(languageTag)
        Locale.setDefault(locale)

        val configuration = Configuration(base.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        return base.createConfigurationContext(configuration)
    }
}