package com.example.ebrojevi.camera

import android.graphics.Bitmap

data class CameraScreenState(
    val recognizedText: String = "",
    val isLoading: Boolean = false,
    val displayedText: String = "",
    val lastBitmap: Bitmap? = null,

    )
