package com.example.ebrojevi.camera

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Log
import android.view.OrientationEventListener
import android.view.Surface
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.guava.await
import java.util.concurrent.Executors

@Composable
fun CameraScreenRoot(
    viewModel: CameraScreenViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CameraScreen(
        state = state,
        onImageCaptured = viewModel::processImage
    )
}

@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraScreen(
    state: CameraScreenState,
    onImageCaptured: (Bitmap) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mainExecutor = ContextCompat.getMainExecutor(context)
    Executors.newSingleThreadExecutor()

    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    val previewUseCase = remember { Preview.Builder().build() }
    val imageCaptureUseCase = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    DisposableEffect(context) {
        val listener = object : OrientationEventListener(context) {
            override fun onOrientationChanged(angle: Int) {
                val rotation = when (angle) {
                    in 45 until 135 -> Surface.ROTATION_270
                    in 135 until 225 -> Surface.ROTATION_180
                    in 225 until 315 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }
                previewUseCase.targetRotation = rotation
                imageCaptureUseCase.targetRotation = rotation
            }
        }
        listener.enable()
        onDispose { listener.disable() }
    }

    LaunchedEffect(hasPermission, previewView) {
        if (!hasPermission) return@LaunchedEffect

        val cameraProvider = ProcessCameraProvider.getInstance(context).await()
        cameraProvider.unbindAll()

        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            previewUseCase,
            imageCaptureUseCase
        )
        previewUseCase.surfaceProvider = previewView.surfaceProvider
    }

    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(250.dp))
            Spacer(modifier = Modifier.height(50.dp))
            Text(state.displayedText)
        } else {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(650.dp)
            ) {
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.matchParentSize()
                )

                capturedBitmap?.let { bmp ->
                    val ratio = if (bmp.width < bmp.height) 3f / 4f else 4f / 3f
                    Box(
                        Modifier
                            .matchParentSize()
                            .aspectRatio(ratio)
                            .background(Color.Black.copy(alpha = 0.3f))
                    ) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
            Spacer(Modifier.height(50.dp))
            Button(onClick = {
                imageCaptureUseCase.takePicture(
                    mainExecutor,
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(imageProxy: ImageProxy) {
                            val bmp = imageProxy.toBitmap()
                            imageProxy.close()
                            onImageCaptured(bmp)
                        }

                        override fun onError(exception: ImageCaptureException) {
                            Log.e(
                                "CameraCapture",
                                "Image capture failed: ${exception.message}",
                                exception
                            )
                        }
                    }
                )
            }) {
                Text("SNAP!")
            }
        }
    }
}