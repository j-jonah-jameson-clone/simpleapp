package com.example.simpleapp.data

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

object FilteredImageSaver {

    private const val MIME_TYPE = "image/jpeg"

    private const val EXTENSION = "jpg"

    private const val JPEG_QUALITY = 95

    suspend fun saveCopy(
        context: Context,
        source: ImageItem,
        filter: PhotoFilter,
        albumName: String
    ): String? = withContext(Dispatchers.IO) {
        runCatching { save(context.applicationContext, source, filter, albumName) }.getOrNull()
    }

    private suspend fun save(
        context: Context,
        source: ImageItem,
        filter: PhotoFilter,
        albumName: String
    ): String? {
        val matrix = filter.matrixValues() ?: return null

        val original = ImageDecoder.decodeFullSize(context, source) ?: return null

        val filtered = try {
            renderWithMatrix(original, matrix)
        } finally {
            original.recycle()
        }

        val displayName = buildDisplayName(source.name, filter.fileNameSuffix)

        val saved = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveToMediaStore(context, displayName, albumName, filtered)
            } else {
                saveToPicturesAlbum(context, displayName, albumName, filtered)
            }
        } finally {
            filtered.recycle()
        }

        return if (saved) displayName else null
    }

    private fun renderWithMatrix(source: Bitmap, matrix: FloatArray): Bitmap {
        val result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val paint = Paint(Paint.FILTER_BITMAP_FLAG).apply {
            colorFilter = ColorMatrixColorFilter(ColorMatrix(matrix))
        }
        Canvas(result).drawBitmap(source, 0f, 0f, paint)
        return result
    }

    private fun buildDisplayName(sourceName: String, suffix: String): String {
        val dot = sourceName.lastIndexOf('.')
        val base = if (dot > 0) sourceName.substring(0, dot) else sourceName
        return "${base.ifBlank { "image" }}_$suffix.$EXTENSION"
    }

    private fun saveToMediaStore(
        context: Context,
        displayName: String,
        albumName: String,
        bitmap: Bitmap
    ): Boolean {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, MIME_TYPE)
            put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                "${Environment.DIRECTORY_PICTURES}${File.separator}$albumName"
            )
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }

        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: return false

        val written = try {
            resolver.openOutputStream(uri)?.use { output -> writeJpeg(bitmap, output) } ?: false
        } catch (error: Exception) {
            false
        }

        if (!written) {
            resolver.delete(uri, null, null)
            return false
        }

        val published = ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }
        resolver.update(uri, published, null, null)
        return true
    }

    @Suppress("DEPRECATION")
    private fun saveToPicturesAlbum(
        context: Context,
        displayName: String,
        albumName: String,
        bitmap: Bitmap
    ): Boolean {
        val album = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            albumName
        )
        if (!album.exists() && !album.mkdirs()) return false

        val file = File(album, displayName)
        val written = try {
            FileOutputStream(file).use { output -> writeJpeg(bitmap, output) }
        } catch (error: Exception) {
            false
        }

        if (!written) {
            file.delete()
            return false
        }

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, MIME_TYPE)
            put(MediaStore.MediaColumns.DATA, file.absolutePath)
        }
        return context.contentResolver
            .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) != null
    }

    private fun writeJpeg(bitmap: Bitmap, output: OutputStream): Boolean {
        val compressed = bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
        if (!compressed) throw IOException("изображение не сжалось")
        return true
    }
}