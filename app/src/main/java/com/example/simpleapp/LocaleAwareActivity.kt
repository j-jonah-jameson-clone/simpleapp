package com.example.simpleapp

import android.content.Context
import androidx.activity.ComponentActivity
import com.example.simpleapp.data.AppLocale

abstract class LocaleAwareActivity : ComponentActivity() {

    private lateinit var appliedLanguageTag: String

    override fun attachBaseContext(newBase: Context) {
        appliedLanguageTag = AppLocale.storedTag(newBase)
        super.attachBaseContext(AppLocale.wrap(newBase, appliedLanguageTag))
    }

    override fun onResume() {
        super.onResume()
        if (appliedLanguageTag != AppLocale.storedTag(this)) {
            recreate()
        }
    }
}