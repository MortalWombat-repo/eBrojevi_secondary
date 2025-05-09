package com.example.ebrojevi.camera

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CameraScreenViewModel : ViewModel() {
    private val _state = MutableStateFlow(CameraScreenState())
    val state: StateFlow<CameraScreenState> = _state

    private val textRecognizer =
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun processImage(bitmap: Bitmap) {
        _state.update { it.copy(isLoading = true, displayedText = "Processing image...") }
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val input = InputImage.fromBitmap(bitmap, /* rotationDegrees= */ 0)
                val result = textRecognizer.process(input).await()
                _state.update {
                    it.copy(
                        //isLoading = false,
                        displayedText = "Extracting text...",
                        recognizedText = result.text,
                        lastBitmap = bitmap
                    )
                }
                val listOfExtractedNumbers = extractENumbersFromTheText(state.value.recognizedText)
                Log.d("EStringLogger", listOfExtractedNumbers.toString())
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        recognizedText = "Error: ${e.localizedMessage}",
                        lastBitmap = bitmap
                    )
                }
            }
        }
    }

    private fun extractENumbersFromTheText(inputValue: String): List<String> {
        val pattern = Regex("""\bE\d{3}[a-zA-Z]?\b""")
        return pattern.findAll(inputValue)
            .map { it.value }
            .toList()
    }
}