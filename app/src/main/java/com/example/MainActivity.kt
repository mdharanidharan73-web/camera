package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ScreenState
import com.example.ui.CameraScreen
import com.example.ui.EnhancedResultScreen
import com.example.ui.GalleryScreen
import com.example.ui.components.EnhancingOverlay
import com.example.ui.components.SettingsDialog
import com.example.ui.theme.RealShotTheme
import com.example.viewmodel.CameraViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: CameraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RealShotTheme {
                RealShotApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun RealShotApp(viewModel: CameraViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        when (uiState.currentScreen) {
            ScreenState.CAMERA -> {
                CameraScreen(
                    viewModel = viewModel,
                    uiState = uiState
                )
            }

            ScreenState.ENHANCING -> {
                EnhancingOverlay(
                    originalBitmap = uiState.originalBitmap
                )
            }

            ScreenState.RESULT -> {
                val orig = uiState.originalBitmap
                if (orig != null) {
                    EnhancedResultScreen(
                        originalBitmap = orig,
                        enhancedBitmap = uiState.enhancedBitmap,
                        errorMessage = uiState.errorMessage,
                        isNetworkError = uiState.isNetworkError,
                        onBack = { viewModel.retakePhoto() },
                        onRetake = { viewModel.retakePhoto() },
                        onSave = { viewModel.saveCurrentToGallery() },
                        onShare = { viewModel.shareCurrentPhoto() },
                        onRetry = { viewModel.retryEnhancement() },
                        onUseOriginal = { viewModel.useOriginalPhoto() }
                    )
                } else {
                    viewModel.retakePhoto()
                }
            }

            ScreenState.GALLERY -> {
                GalleryScreen(
                    photos = uiState.galleryPhotos,
                    photoStorage = viewModel.photoStorage,
                    onPhotoSelected = { photo -> viewModel.selectGalleryPhoto(photo) },
                    onBack = { viewModel.closeGallery() }
                )
            }
        }

        // Settings Dialog
        if (uiState.isSettingsOpen) {
            SettingsDialog(
                settingsRepository = viewModel.settingsRepository,
                isFrontCamera = uiState.isFrontCamera,
                onToggleCameraLens = { viewModel.toggleCameraLens() },
                onDismiss = { viewModel.closeSettings() }
            )
        }
    }
}
