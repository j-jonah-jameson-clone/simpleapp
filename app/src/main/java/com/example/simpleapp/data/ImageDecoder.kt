package com.example.simpleapp.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.LruCache
import android.util.Size
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ImageDecoder {

    private val cache: LruCache<String, Bitmap> by lazy {
        val maxMemoryKb = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        object : LruCache<String, Bitmap>(maxMemoryKb / 8) {
            override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount / 1024
        }
    }

    suspend fun decodeScaled(
        context: Context,
        item: ImageItem,
        targetSize: Int
    ): Bitmap? = withContext(Dispatchers.IO) {
        val key = "${item.id}:$targetSize"
        cache.get(key)?.let { return@withContext it }

        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            decodeWithThumbnailApi(context, item, targetSize)
        } else {
            null
        } ?: decodeWithBitmapFactory(context, item, targetSize)

        if (bitmap != null) cache.put(key, bitmap)
        bitmap
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun decodeWithThumbnailApi(
        context: Context,
        item: ImageItem,
        targetSize: Int
    ): Bitmap? = try {
        context.applicationContext.contentResolver.loadThumbnail(
            item.uri,
            Size(targetSize, targetSize),
            null
        )
    } catch (error: Exception) {
        null
    }

    private fun decodeWithBitmapFactory(
        context: Context,
        item: ImageItem,
        targetSize: Int
    ): Bitmap? {
        val resolver = context.applicationContext.contentResolver

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(item.uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, bounds)
        }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        val options = BitmapFactory.Options().apply {
            inSampleSize = calculateSampleSize(
                sourceWidth = bounds.outWidth,
                sourceHeight = bounds.outHeight,
                targetSize = targetSize
            )
            inPreferredConfig = Bitmap.Config.RGB_565
        }
        return resolver.openInputStream(item.uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }
    }

    fun calculateSampleSize(sourceWidth: Int, sourceHeight: Int, targetSize: Int): Int {
        if (targetSize <= 0) return 1
        var sampleSize = 1
        var currentWidth = sourceWidth
        var currentHeight = sourceHeight
        while (currentWidth / 2 >= targetSize && currentHeight / 2 >= targetSize) {
            currentWidth /= 2
            currentHeight /= 2
            sampleSize *= 2
        }
        return sampleSize
    }

    fun clearCache() = cache.evictAll()
}