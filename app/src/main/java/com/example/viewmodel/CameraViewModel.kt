package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.DslrPhotoEngine
import com.example.ai.OpenAiEnhancer
import com.example.data.AnalysisReport
import com.example.data.CameraMode
import com.example.data.EnhancementEngine
import com.example.data.FlashMode
import com.example.data.PhotoRecord
import com.example.data.PhotoStorage
import com.example.data.SampleSceneType
import com.example.data.ScreenState
import com.example.data.SettingsRepository
import com.example.ui.components.captureBitmapFromCamera
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

data class CameraUiState(
    val currentScreen: ScreenState = ScreenState.CAMERA,
    val currentMode: CameraMode = CameraMode.PHOTO,
    val zoomRatio: Float = 1.0f,
    val flashMode: FlashMode = FlashMode.OFF,
    val isFrontCamera: Boolean = false,
    val gridEnabled: Boolean = false,
    val isCapturing: Boolean = false,
    val currentPhoto: PhotoRecord? = null,
    val originalBitmap: Bitmap? = null,
    val enhancedBitmap: Bitmap? = null,
    val errorMessage: String? = null,
    val isNetworkError: Boolean = false,
    val galleryPhotos: List<PhotoRecord> = emptyList(),
    val isSettingsOpen: Boolean = false
)

class CameraViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context get() = getApplication()
    val settingsRepository = SettingsRepository(context)
    val photoStorage = PhotoStorage(context)

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    private var activeImageCapture: ImageCapture? = null

    fun setImageCapture(capture: ImageCapture) {
        activeImageCapture = capture
    }

    fun setCameraMode(mode: CameraMode) {
        _uiState.update { it.copy(currentMode = mode) }
    }

    fun setZoomRatio(zoom: Float) {
        _uiState.update { it.copy(zoomRatio = zoom) }
    }

    fun toggleFlash() {
        val next = when (_uiState.value.flashMode) {
            FlashMode.OFF -> FlashMode.AUTO
            FlashMode.AUTO -> FlashMode.ON
            FlashMode.ON -> FlashMode.OFF
        }
        _uiState.update { it.copy(flashMode = next) }
    }

    fun toggleCameraLens() {
        _uiState.update { it.copy(isFrontCamera = !it.isFrontCamera) }
    }

    fun openSettings() {
        _uiState.update { it.copy(isSettingsOpen = true) }
    }

    fun closeSettings() {
        _uiState.update { it.copy(isSettingsOpen = false) }
    }

    fun openGallery() {
        _uiState.update { it.copy(currentScreen = ScreenState.GALLERY) }
    }

    fun closeGallery() {
        _uiState.update { it.copy(currentScreen = ScreenState.CAMERA) }
    }

    fun retakePhoto() {
        _uiState.update {
            it.copy(
                currentScreen = ScreenState.CAMERA,
                originalBitmap = null,
                enhancedBitmap = null,
                errorMessage = null,
                isNetworkError = false
            )
        }
    }

    fun selectGalleryPhoto(photo: PhotoRecord) {
        viewModelScope.launch {
            val orig = photoStorage.loadBitmapFromFile(photo.originalPath)
            val enh = photo.enhancedPath?.let { photoStorage.loadBitmapFromFile(it) }
            if (orig != null) {
                _uiState.update {
                    it.copy(
                        currentScreen = ScreenState.RESULT,
                        currentPhoto = photo,
                        originalBitmap = orig,
                        enhancedBitmap = enh ?: orig,
                        errorMessage = null
                    )
                }
            }
        }
    }

    fun capturePhoto() {
        if (_uiState.value.isCapturing) return
        _uiState.update { it.copy(isCapturing = true) }

        val imageCapture = activeImageCapture
        if (imageCapture == null) {
            captureSampleScene(SampleSceneType.PORTRAIT_LOWLIGHT)
            return
        }

        captureBitmapFromCamera(
            context = context,
            imageCapture = imageCapture,
            onSuccess = { bitmap ->
                processCapturedBitmap(bitmap)
            },
            onError = { _ ->
                _uiState.update { it.copy(isCapturing = false) }
                captureSampleScene(SampleSceneType.PORTRAIT_LOWLIGHT)
            }
        )
    }

    fun captureSampleScene(type: SampleSceneType) {
        val sampleBitmap = photoStorage.createSampleScene(type)
        processCapturedBitmap(sampleBitmap)
    }

    private fun processCapturedBitmap(original: Bitmap) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isCapturing = false,
                    currentScreen = ScreenState.ENHANCING,
                    originalBitmap = original,
                    enhancedBitmap = null,
                    errorMessage = null
                )
            }

            // Immediately preserve the original photograph
            val origFile = photoStorage.saveTempImage(original, "RealShot_Original")

            runEnhancementWorkflow(original, origFile.absolutePath)
        }
    }

    fun retryEnhancement() {
        val orig = _uiState.value.originalBitmap ?: return
        val origPath = _uiState.value.currentPhoto?.originalPath ?: ""
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    currentScreen = ScreenState.ENHANCING,
                    errorMessage = null
                )
            }
            runEnhancementWorkflow(orig, origPath)
        }
    }

    fun useOriginalPhoto() {
        val orig = _uiState.value.originalBitmap ?: return
        _uiState.update {
            it.copy(
                currentScreen = ScreenState.RESULT,
                enhancedBitmap = orig,
                errorMessage = null
            )
        }
    }

    private suspend fun runEnhancementWorkflow(original: Bitmap, origPath: String) {
        val isAiEnabled = settingsRepository.aiEnhancementEnabled

        if (!isAiEnabled) {
            // Apply on-device DSLR RAW engine directly
            applyLocalDslrEnhancement(original, origPath)
            return
        }

        val isNetworkAvailable = isOnline()
        if (!isNetworkAvailable) {
            _uiState.update {
                it.copy(
                    currentScreen = ScreenState.RESULT,
                    enhancedBitmap = null,
                    isNetworkError = true,
                    errorMessage = "Internet connection required for AI enhancement."
                )
            }
            return
        }

        try {
            // Priority 1: Communicate with secure backend (keeps OpenAI API key on backend)
            val backendUrl = settingsRepository.backendUrl
            val apiKey = settingsRepository.getEffectiveOpenAiKey()

            val enhancedBitmap = try {
                com.example.ai.BackendApiClient.enhanceViaBackend(
                    source = original,
                    backendUrl = backendUrl
                )
            } catch (backendError: Exception) {
                // If backend is not running or direct key is configured, fallback to direct OpenAiEnhancer
                if (apiKey.isNotBlank()) {
                    val model = settingsRepository.openAiModel
                    OpenAiEnhancer.enhanceWithOpenAi(
                        source = original,
                        apiKey = apiKey,
                        model = model
                    )
                } else {
                    // Seamless on-device DSLR RAW fallback
                    applyLocalDslrEnhancement(original, origPath, isFallback = true)
                    return
                }
            }

            finishEnhancementSuccess(
                original = original,
                origPath = origPath,
                enhanced = enhancedBitmap,
                engine = EnhancementEngine.OPENAI
            )
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    currentScreen = ScreenState.RESULT,
                    enhancedBitmap = null,
                    isNetworkError = !isOnline(),
                    errorMessage = e.message ?: "Enhancement failed"
                )
            }
        }
    }

    private suspend fun applyLocalDslrEnhancement(
        original: Bitmap,
        origPath: String,
        isFallback: Boolean = false
    ) {
        val (enhanced, report) = DslrPhotoEngine.enhancePhoto(
            source = original,
            strength = settingsRepository.enhancementStrength,
            mode = _uiState.value.currentMode
        )

        finishEnhancementSuccess(
            original = original,
            origPath = origPath,
            enhanced = enhanced,
            engine = EnhancementEngine.LOCAL_DSLR
        )
    }

    private suspend fun finishEnhancementSuccess(
        original: Bitmap,
        origPath: String,
        enhanced: Bitmap,
        engine: EnhancementEngine
    ) {
        val enhFile = photoStorage.saveTempImage(enhanced, "RealShot")

        val record = PhotoRecord(
            id = UUID.randomUUID().toString(),
            originalPath = origPath,
            enhancedPath = enhFile.absolutePath,
            timestamp = System.currentTimeMillis(),
            cameraMode = _uiState.value.currentMode,
            engineUsed = engine
        )

        // Automatically save to Gallery if enabled
        if (settingsRepository.autoSaveToGallery) {
            photoStorage.saveToGallery(enhanced, isEnhanced = true)
            if (settingsRepository.saveOriginalBackup) {
                photoStorage.saveToGallery(original, isEnhanced = false)
            }
        }

        _uiState.update {
            it.copy(
                currentScreen = ScreenState.RESULT,
                currentPhoto = record,
                enhancedBitmap = enhanced,
                errorMessage = null,
                isNetworkError = false,
                galleryPhotos = listOf(record) + it.galleryPhotos
            )
        }
    }

    fun saveCurrentToGallery() {
        val bitmap = _uiState.value.enhancedBitmap ?: _uiState.value.originalBitmap ?: return
        viewModelScope.launch {
            val uri = photoStorage.saveToGallery(bitmap, isEnhanced = _uiState.value.enhancedBitmap != null)
            if (uri != null) {
                Toast.makeText(context, "Saved to RealShot AI album in Gallery!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Saved photo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun shareCurrentPhoto() {
        val bitmap = _uiState.value.enhancedBitmap ?: _uiState.value.originalBitmap ?: return
        viewModelScope.launch {
            try {
                val tempFile = File(context.cacheDir, "shared_realshot.jpg")
                FileOutputStream(tempFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                }
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    tempFile
                )
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/jpeg"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(intent, "Share RealShot Photo").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            } catch (e: Exception) {
                Toast.makeText(context, "Share: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isOnline(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
