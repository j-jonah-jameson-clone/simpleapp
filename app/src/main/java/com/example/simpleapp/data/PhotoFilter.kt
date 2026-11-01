package com.example.simpleapp.data

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.ColorMatrix
import com.example.simpleapp.R

enum class PhotoFilter(
    @StringRes val titleRes: Int,
    private val matrix: FloatArray?
) {

    ORIGINAL(
        titleRes = R.string.filter_none,
        matrix = null
    ),

    FILM(
        titleRes = R.string.filter_film,
        matrix = floatArrayOf(
            0.79f, 0.27f, 0.07f, 0f, 8f,
            0.12f, 0.89f, 0.06f, 0f, 4f,
            0.10f, 0.19f, 0.70f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )
    ),

    PORTRAIT(
        titleRes = R.string.filter_portrait,
        matrix = floatArrayOf(
            1.08f, 0.02f, 0f, 0f, 4f,
            0.01f, 1.05f, 0.01f, 0f, -4f,
            0f, 0f, 0.98f, 0f, -6f,
            0f, 0f, 0f, 1f, 0f
        )
    ),

    MONOCHROME(
        titleRes = R.string.filter_mono,
        matrix = floatArrayOf(
            0.344f, 0.675f, 0.131f, 0f, -19f,
            0.344f, 0.675f, 0.131f, 0f, -19f,
            0.344f, 0.675f, 0.131f, 0f, -19f,
            0f, 0f, 0f, 1f, 0f
        )
    );

    val colorMatrix: ColorMatrix? = matrix?.let(::ColorMatrix)
}