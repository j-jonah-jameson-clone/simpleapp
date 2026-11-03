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
import android.graphics.Matrix
import android.media.ExifInterface

object ImageDecoder {

    private const val MAX_FULL_SIZE = 4096
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

    suspend fun decodeFullSize(context: Context, item: ImageItem): Bitmap? =
        withContext(Dispatchers.IO) {
            decodeWithBitmapFactory(
                context = context,
                item = item,
                targetSize = MAX_FULL_SIZE,
                config = Bitmap.Config.ARGB_8888
            )
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
        targetSize: Int,
        config: Bitmap.Config = Bitmap.Config.RGB_565
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
            inPreferredConfig = config
        }
        val decoded = resolver.openInputStream(item.uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        } ?: return null

        return applyExifOrientation(context, item, decoded)
    }

    private fun applyExifOrientation(
        context: Context,
        item: ImageItem,
        bitmap: Bitmap
    ): Bitmap {
        val orientation = try {
            context.contentResolver.openInputStream(item.uri)?.use { stream ->
                ExifInterface(stream).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            }
        } catch (error: Exception) {
            null
        } ?: return bitmap

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            else -> return bitmap
        }

        return try {
            val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            if (rotated !== bitmap) bitmap.recycle()
            rotated
        } catch (error: Exception) {
            bitmap
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