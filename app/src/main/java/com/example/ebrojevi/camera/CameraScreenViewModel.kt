package com.example.ebrojevi.camera

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class CameraScreenViewModel : ViewModel() {
    private val _state = MutableStateFlow(CameraScreenState())
    val state: StateFlow<CameraScreenState> = _state

    //TODO Combine these function into one function for state update
    fun isButtonPressed(isPressed: Boolean) {
        _state.update { currentState ->
            currentState.copy(
                isButtonPressed = isPressed,
                displayedText = "Processing image..."
            )
        }
    }

    fun onTextDetected(text: String) {
        _state.update { currentState ->
            currentState.copy(
                recognizedText = text
            )
        }
    }
}