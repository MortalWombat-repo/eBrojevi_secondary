package com.example.ebrojevi.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class CameraScreenViewModel : ViewModel() {
    private val _state = MutableStateFlow(CameraScreenState())
    val state: StateFlow<CameraScreenState> = _state

    fun onTextDetected(text: String) {
        _state.update { it.copy(recognizedText = text) }
    }
}