package com.example.simpleapp.data

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

object AppIntents {

    fun openAppSettings(context: Context) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun buildShareIntent(context: Context, image: ImageItem, chooserTitle: String): Intent {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = image.mimeType
            putExtra(Intent.EXTRA_STREAM, image.uri)
            clipData = ClipData.newUri(context.contentResolver, image.name, image.uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return Intent.createChooser(sendIntent, chooserTitle)
    }
}