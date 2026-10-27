package com.example.simpleapp.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.ContextCompat

object MediaPermissions {

    val required: Array<String> = when {
        Build.VERSION.SDK_INT >= 34 -> arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
        )

        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES
        )

        else -> arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    fun isGranted(context: Context): Boolean = required.any { permission ->
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}

object GalleryRepository {

    const val ALL_FOLDERS_ID = -1L

    private val PROJECTION = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DISPLAY_NAME,
        MediaStore.Images.Media.BUCKET_ID,
        MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
        MediaStore.Images.Media.SIZE,
        MediaStore.Images.Media.DATE_ADDED,
        MediaStore.Images.Media.DATE_MODIFIED,
        MediaStore.Images.Media.WIDTH,
        MediaStore.Images.Media.HEIGHT,
        MediaStore.Images.Media.MIME_TYPE
    )

    fun loadImages(context: Context): List<ImageItem> {
        if (!MediaPermissions.isGranted(context)) return emptyList()

        val result = mutableListOf<ImageItem>()
        val resolver = context.applicationContext.contentResolver

        resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            PROJECTION,
            null,
            null,
            null
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val bucketIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
            val bucketNameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val dateModifiedColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
            val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
            val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
            val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)

                val dateAddedSeconds = cursor.getLong(dateAddedColumn)
                val dateModifiedSeconds = cursor.getLong(dateModifiedColumn)
                val dateMillis = when {
                    dateAddedSeconds > 0 -> dateAddedSeconds * 1000L
                    dateModifiedSeconds > 0 -> dateModifiedSeconds * 1000L
                    else -> 0L
                }

                result += ImageItem(
                    id = id,
                    uri = ImageItem.buildUri(id),
                    name = cursor.getString(nameColumn).orEmpty(),
                    folderId = cursor.getLong(bucketIdColumn),
                    folderPath = cursor.getString(bucketNameColumn).orEmpty(),
                    sizeBytes = cursor.getLong(sizeColumn),
                    dateMillis = dateMillis,
                    width = cursor.getInt(widthColumn),
                    height = cursor.getInt(heightColumn),
                    mimeType = cursor.getString(mimeColumn).orEmpty()
                )
            }
        }
        return result
    }

    fun deleteImages(context: Context, uris: List<Uri>): Boolean {
        if (uris.isEmpty()) return false
        return try {
            val resolver = context.applicationContext.contentResolver
            uris.forEach { resolver.delete(it, null, null) }
            true
        } catch (error: Exception) {
            false
        }
    }
}