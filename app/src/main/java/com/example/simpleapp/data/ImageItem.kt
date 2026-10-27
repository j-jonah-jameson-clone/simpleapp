package com.example.simpleapp.data

import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore

data class ImageItem(
    val id: Long,
    val uri: Uri,
    val name: String,
    val folderId: Long,
    val folderPath: String,
    val sizeBytes: Long,
    val dateMillis: Long,
    val width: Int,
    val height: Int,
    val mimeType: String
) {
    companion object {
        fun buildUri(id: Long): Uri =
            ContentUris.withAppendedId(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                id
            )
    }
}