package com.example.simpleapp.data

enum class ImageSortOrder {

    DATE_DESC,

    DATE_ASC,

    SIZE_DESC,

    SIZE_ASC;

    fun apply(images: List<ImageItem>): List<ImageItem> = when (this) {
        DATE_DESC -> images.sortedByDescending { it.dateMillis }
        DATE_ASC -> images.sortedBy { it.dateMillis }
        SIZE_DESC -> images.sortedByDescending { it.sizeBytes }
        SIZE_ASC -> images.sortedBy { it.sizeBytes }
    }
}