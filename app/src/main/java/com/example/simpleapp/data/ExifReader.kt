package com.example.simpleapp.data

import android.content.Context
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ImageDetails(
    val name: String,
    val dateMillis: Long,
    val width: Int,
    val height: Int,
    val sizeBytes: Long,
    val mimeType: String,
    val folder: String,
    val cameraMake: String?,
    val cameraModel: String?,
    val iso: Int?,
    val exposureSeconds: Double?,
    val aperture: Double?,
    val focalLengthMillimeters: Float?
)

object ExifReader {

    suspend fun loadDetails(context: Context, item: ImageItem): ImageDetails {
        val exif = readExif(context, item)
        return ImageDetails(
            name = item.name,
            dateMillis = item.dateMillis,
            width = item.width,
            height = item.height,
            sizeBytes = item.sizeBytes,
            mimeType = item.mimeType,
            folder = item.folderPath,
            cameraMake = exif?.make,
            cameraModel = exif?.model,
            iso = exif?.iso,
            exposureSeconds = exif?.exposureSeconds,
            aperture = exif?.aperture,
            focalLengthMillimeters = exif?.focalLengthMillimeters
        )
    }

    private data class ExifValues(
        val make: String?,
        val model: String?,
        val iso: Int?,
        val exposureSeconds: Double?,
        val aperture: Double?,
        val focalLengthMillimeters: Float?
    )

    private suspend fun readExif(context: Context, item: ImageItem): ExifValues? =
        withContext(Dispatchers.IO) {
            try {
                context.applicationContext.contentResolver
                    .openInputStream(item.uri)
                    ?.use { stream ->
                        val exif = ExifInterface(stream)

                        val make = exif.getAttribute(ExifInterface.TAG_MAKE)?.trim().orEmpty()
                        val model = exif.getAttribute(ExifInterface.TAG_MODEL)?.trim().orEmpty()

                        val iso = exif.getAttributeInt(ExifInterface.TAG_ISO_SPEED_RATINGS, 0)
                            .takeIf { it > 0 }

                        ExifValues(
                            make = make.ifEmpty { null },
                            model = model.ifEmpty { null },
                            iso = iso,
                            exposureSeconds = exif
                                .getAttributeDouble(ExifInterface.TAG_EXPOSURE_TIME, 0.0)
                                .takeIf { it > 0.0 },
                            aperture = exif
                                .getAttributeDouble(ExifInterface.TAG_F_NUMBER, 0.0)
                                .takeIf { it > 0.0 },
                            focalLengthMillimeters = exif
                                .getAttributeDouble(ExifInterface.TAG_FOCAL_LENGTH, 0.0)
                                .takeIf { it > 0.0 }
                                ?.toFloat()
                        )
                    }
            } catch (error: Exception) {
                null
            }
        }
}