package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.data.FlashMode
import com.example.ui.theme.CameraBlack
import java.util.concurrent.Executors

@Composable
fun CameraPreviewView(
    isFrontCamera: Boolean,
    flashMode: FlashMode,
    gridEnabled: Boolean,
    zoomRatio: Float = 1.0f,
    imageCaptureHolder: (ImageCapture) -> Unit,
    onTapFocus: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var camera by remember { mutableStateOf<Camera?>(null) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }

    val cameraSelector = if (isFrontCamera) {
        CameraSelector.DEFAULT_FRONT_CAMERA
    } else {
        CameraSelector.DEFAULT_BACK_CAMERA
    }

    val imageCapture = remember(isFrontCamera) {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .build()
    }

    LaunchedEffect(imageCapture) {
        imageCaptureHolder(imageCapture)
    }

    // Flash mode update
    LaunchedEffect(flashMode, imageCapture) {
        imageCapture.flashMode = when (flashMode) {
            FlashMode.OFF -> ImageCapture.FLASH_MODE_OFF
            FlashMode.AUTO -> ImageCapture.FLASH_MODE_AUTO
            FlashMode.ON -> ImageCapture.FLASH_MODE_ON
        }
    }

    // Zoom ratio update
    LaunchedEffect(zoomRatio, camera) {
        try {
            camera?.cameraControl?.setZoomRatio(zoomRatio)
        } catch (e: Exception) {
            // device might not support requested zoom
        }
    }

    Box(modifier = modifier.fillMaxSize().background(CameraBlack)) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    previewView = this
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        onTapFocus(offset)
                        val pView = previewView ?: return@detectTapGestures
                        val factory = SurfaceOrientedMeteringPointFactory(
                            pView.width.toFloat(),
                            pView.height.toFloat()
                        )
                        val point = factory.createPoint(offset.x, offset.y)
                        val action = FocusMeteringAction.Builder(point).build()
                        camera?.cameraControl?.startFocusAndMetering(action)
                    }
                },
            update = { view ->
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    try {
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(view.surfaceProvider)
                        }

                        cameraProvider.unbindAll()
                        camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture
                        )
                    } catch (e: Exception) {
                        Log.e("RealShotCamera", "Camera binding failed: ${e.message}")
                    }
                }, ContextCompat.getMainExecutor(context))
            }
        )

        // Rule of thirds grid
        if (gridEnabled) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeColor = Color.White.copy(alpha = 0.25f)
                val strokeWidth = 1.dp.toPx()

                // Horizontal grid lines
                drawLine(
                    color = strokeColor,
                    start = Offset(0f, size.height / 3f),
                    end = Offset(size.width, size.height / 3f),
                    strokeWidth = strokeWidth
                )
                drawLine(
                    color = strokeColor,
                    start = Offset(0f, 2 * size.height / 3f),
                    end = Offset(size.width, 2 * size.height / 3f),
                    strokeWidth = strokeWidth
                )

                // Vertical grid lines
                drawLine(
                    color = strokeColor,
                    start = Offset(size.width / 3f, 0f),
                    end = Offset(size.width / 3f, size.height),
                    strokeWidth = strokeWidth
                )
                drawLine(
                    color = strokeColor,
                    start = Offset(2 * size.width / 3f, 0f),
                    end = Offset(2 * size.width / 3f, size.height),
                    strokeWidth = strokeWidth
                )
            }
        }
    }
}

/**
 * Utility to capture high-quality Bitmap from CameraX ImageCapture.
 */
fun captureBitmapFromCamera(
    context: Context,
    imageCapture: ImageCapture?,
    onSuccess: (Bitmap) -> Unit,
    onError: (Exception) -> Unit
) {
    if (imageCapture == null) {
        onError(IllegalStateException("Camera capture is not initialized"))
        return
    }

    val executor = ContextCompat.getMainExecutor(context)
    imageCapture.takePicture(
        executor,
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(imageProxy: ImageProxy) {
                try {
                    val bitmap = imageProxyToBitmap(imageProxy)
                    imageProxy.close()
                    onSuccess(bitmap)
                } catch (e: Exception) {
                    imageProxy.close()
                    onError(e)
                }
            }

            override fun onError(exception: ImageCaptureException) {
                onError(exception)
            }
        }
    )
}

private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap {
    val plane = imageProxy.planes[0]
    val buffer = plane.buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    val original = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

    val rotation = imageProxy.imageInfo.rotationDegrees
    if (rotation != 0) {
        val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
        return Bitmap.createBitmap(original, 0, 0, original.width, original.height, matrix, true)
    }
    return original
}
